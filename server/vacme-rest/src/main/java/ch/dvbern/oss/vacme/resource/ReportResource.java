/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.resource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.documentqueue.SpracheParamJax;
import ch.dvbern.oss.vacme.entities.documentqueue.VonBisSpracheParamJax;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungErwachsenDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungKindDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungZHDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungZHKindDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueueResult;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.ImpfungenCSVDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.RegsKantonCSVDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.RegsKantonsArztCSVDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.ReportingOdiBuchungenDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.ReportingOdisCSVDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.ReportingOdisImpfungenCSVDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.TerminslotsCSVDocQueue;
import ch.dvbern.oss.vacme.entities.embeddables.FileBytes;
import ch.dvbern.oss.vacme.entities.registration.Sprache;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.entities.util.BlobUtil;
import ch.dvbern.oss.vacme.jax.stats.DocumentQueueJax;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.documentqueue.DocumentQueueService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.RestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.HonoluluHenk.httpcontentdisposition.Disposition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_MEDIZINISCHER_REPORTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_LOGISTIK_REPORTER;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_REPORTS))
@Path(VACME_WEB + "/reports")
public class ReportResource {

	private final DocumentQueueService documentQueueService;
	private final ObjectMapper objectMapper;

	private final UserPrincipal userPrincipal;
	private final Authorizer authorizer;

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("kanton/statistik")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateKantonCSV() {

		RegsKantonCSVDocQueue regsKantonCSVDocQueue = new RegsKantonCSVDocQueue();
		SpracheParamJax param = new SpracheParamJax(Sprache.DE); // currently hardcoded
		regsKantonCSVDocQueue.init(param, currentBenutzer(), objectMapper);
		regsKantonCSVDocQueue.addJobToQueue(documentQueueService);

		return Response.accepted().build();

	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("terminslots/statistik")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateReportingTerminslotsCSV() {
		TerminslotsCSVDocQueue reportDocQueueItem = new TerminslotsCSVDocQueue();
		SpracheParamJax param = new SpracheParamJax(Sprache.DE);
		Optional<Benutzer> benutzerOpt = userPrincipal.getBenutzer();
		Validate.isTrue(
			benutzerOpt.isPresent(),
			"Benutzer fuer den der Report erstellt werden soll muss vorhanden sein");

		reportDocQueueItem.init(param, currentBenutzer(), objectMapper);
		reportDocQueueItem.addJobToQueue(documentQueueService);
		return Response.accepted().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("odis/statistik")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateReportingOdisCSV() {
		ReportingOdisCSVDocQueue reportDocQueueItem = new ReportingOdisCSVDocQueue();
		SpracheParamJax param = new SpracheParamJax(Sprache.DE);

		reportDocQueueItem.init(param, currentBenutzer(), objectMapper);
		reportDocQueueItem.addJobToQueue(documentQueueService);
		return Response.accepted().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("kantonsarzt/statistik")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateReportingKantonsarztCSV() {
		RegsKantonsArztCSVDocQueue reportDocQueueItem = new RegsKantonsArztCSVDocQueue();
		SpracheParamJax param = new SpracheParamJax(Sprache.DE);

		reportDocQueueItem.init(param, currentBenutzer(), objectMapper);
		reportDocQueueItem.addJobToQueue(documentQueueService);
		return Response.accepted().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("impfungen/statistik")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateReportingImpfungenCSVAsync() {
		ImpfungenCSVDocQueue impfungenCSVDocQueue = new ImpfungenCSVDocQueue();
		SpracheParamJax param = new SpracheParamJax(Sprache.DE);

		impfungenCSVDocQueue.init(param, currentBenutzer(), objectMapper);
		impfungenCSVDocQueue.addJobToQueue(documentQueueService);
		return Response.accepted().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("odiimpfungen/statistik/{language}")
	@RolesAllowed(BenutzerRolleName.OI_IMPFVERANTWORTUNG)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateReportingOdiImpfungenCSV(
		@NonNull @NotNull @PathParam("language") String language
	) {
		ReportingOdisImpfungenCSVDocQueue reportDocQueueItem = new ReportingOdisImpfungenCSVDocQueue();
		SpracheParamJax param = new SpracheParamJax(Sprache.DE);

		reportDocQueueItem.init(param, currentBenutzer(), objectMapper);
		reportDocQueueItem.addJobToQueue(documentQueueService);
		return Response.accepted().build();
	}


	@POST
	@Consumes(MediaType.WILDCARD)
	@Path("oditerminbuchungen/statistik/{language}")
	@RolesAllowed(BenutzerRolleName.OI_LOGISTIK_REPORTER)
	@Operation(description = "Async version of generateReportingOdiTerminbuchungenCSV")
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateReportingOdiTerminbuchungenAsync(
		@NonNull @NotNull @PathParam("language") String language
	) {

		Sprache sprache = Sprache.valueOf(language.toUpperCase(Locale.ROOT));
		ReportingOdiBuchungenDocQueue reportDocQueueItem = new ReportingOdiBuchungenDocQueue();
		SpracheParamJax param = new SpracheParamJax(sprache);

		reportDocQueueItem.init(param, currentBenutzer(), objectMapper);
		reportDocQueueItem.addJobToQueue(documentQueueService);
		return Response.accepted().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("abrechnung/{language}")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateExcelReportAbrechnungAsync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		Sprache sprache = Sprache.valueOf(language.toUpperCase(Locale.ROOT));
		VonBisSpracheParamJax paramJax = new VonBisSpracheParamJax(dateVon, dateBis, sprache);
		// add to queue
		AbrechnungDocQueue abrechnungDocQueue = createAndStoreAbrechnungDocumentQueueItem(paramJax);

		Validate.notNull(abrechnungDocQueue.getId());

		return Response.accepted().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Path("abrechnung-erwachsen/{language}")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateExcelReportAbrechnungErwachsenAsync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		Sprache sprache = Sprache.valueOf(language.toUpperCase(Locale.ROOT));
		VonBisSpracheParamJax paramJax = new VonBisSpracheParamJax(dateVon, dateBis, sprache);
		// add to queue
		AbrechnungErwachsenDocQueue abrechnungDocQueue = createAndStoreAbrechnungErwachsenDocumentQueueItem(paramJax);

		Validate.notNull(abrechnungDocQueue.getId());

		return Response.accepted().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Path("abrechnung-kind/{language}")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateExcelReportAbrechnungKindAsync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		Sprache sprache = Sprache.valueOf(language.toUpperCase(Locale.ROOT));
		VonBisSpracheParamJax paramJax = new VonBisSpracheParamJax(dateVon, dateBis, sprache);
		// add to queue
		AbrechnungKindDocQueue abrechnungDocQueue = createAndStoreAbrechnungKindDocumentQueueItem(paramJax);

		Validate.notNull(abrechnungDocQueue.getId());

		return Response.accepted().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("abrechnung-zh/{language}")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateExcelReportAbrechnungZHAsync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		Sprache sprache = Sprache.valueOf(language.toUpperCase(Locale.ROOT));
		VonBisSpracheParamJax paramJax = new VonBisSpracheParamJax(dateVon, dateBis, sprache);

		// add to queue
		AbrechnungZHDocQueue abrechnungZHocumentQueue = createAndStoreAbrechnungDocumentQueueItemZH(paramJax);

		Validate.notNull(abrechnungZHocumentQueue.getId());

		return Response.accepted().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("abrechnung-zh-kind/{language}")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Transactional(TxType.NOT_SUPPORTED)
	public Response generateExcelReportAbrechnungZHKindAsync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		Sprache sprache = Sprache.valueOf(language.toUpperCase(Locale.ROOT));
		VonBisSpracheParamJax paramJax = new VonBisSpracheParamJax(dateVon, dateBis, sprache);

		// add to queue
		AbrechnungZHKindDocQueue abrechnungZHKindDocQueue = createAndStoreAbrechnungDocumentQueueItemZHKind(paramJax);

		Validate.notNull(abrechnungZHKindDocQueue.getId());

		return Response.accepted().build();
	}

	@NonNull
	private AbrechnungDocQueue createAndStoreAbrechnungDocumentQueueItem(VonBisSpracheParamJax paramJax) {
		AbrechnungDocQueue abrechnungDocQueue = new AbrechnungDocQueue();
		abrechnungDocQueue.init(paramJax, currentBenutzer(), objectMapper);
		abrechnungDocQueue.addJobToQueue(documentQueueService);
		return abrechnungDocQueue;
	}

	@NonNull
	private AbrechnungErwachsenDocQueue createAndStoreAbrechnungErwachsenDocumentQueueItem(VonBisSpracheParamJax paramJax) {
		AbrechnungErwachsenDocQueue abrechnungDocQueue = new AbrechnungErwachsenDocQueue();
		abrechnungDocQueue.init(paramJax, currentBenutzer(), objectMapper);
		abrechnungDocQueue.addJobToQueue(documentQueueService);
		return abrechnungDocQueue;
	}

	@NonNull
	private AbrechnungKindDocQueue createAndStoreAbrechnungKindDocumentQueueItem(VonBisSpracheParamJax paramJax) {
		AbrechnungKindDocQueue abrechnungDocQueue = new AbrechnungKindDocQueue();
		abrechnungDocQueue.init(paramJax, currentBenutzer(), objectMapper);
		abrechnungDocQueue.addJobToQueue(documentQueueService);
		return abrechnungDocQueue;
	}

	@NonNull
	private AbrechnungZHDocQueue createAndStoreAbrechnungDocumentQueueItemZH(VonBisSpracheParamJax paramJax) {
		AbrechnungZHDocQueue abrechnungDocumentQueue = new AbrechnungZHDocQueue();
		abrechnungDocumentQueue.init(paramJax, currentBenutzer(), objectMapper);
		abrechnungDocumentQueue.addJobToQueue(documentQueueService);
		return abrechnungDocumentQueue;
	}

	@NonNull
	private AbrechnungZHKindDocQueue createAndStoreAbrechnungDocumentQueueItemZHKind(VonBisSpracheParamJax paramJax) {
		AbrechnungZHKindDocQueue abrechnungDocumentQueue = new AbrechnungZHKindDocQueue();
		abrechnungDocumentQueue.init(paramJax, currentBenutzer(), objectMapper);
		abrechnungDocumentQueue.addJobToQueue(documentQueueService);
		return abrechnungDocumentQueue;
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("download/documentqueue/{id}")
	@RolesAllowed({ OI_IMPFVERANTWORTUNG, KT_MEDIZINISCHER_REPORTER, OI_LOGISTIK_REPORTER })
	@Schema(type = SchemaType.STRING, format = "binary")
	public Response downloadDocumentQueueItem(
		@NonNull @NotNull @PathParam("id") UUID docResultId
	) {
		Optional<DocumentQueue> documentQueueByResultId = documentQueueService.getDocumentQueueByResultId(docResultId);
		authorizer.checkReadAuthorization(documentQueueByResultId
			.orElseThrow(() -> AppFailureException.entityNotFound(DocumentQueueResult.class, docResultId.toString())));

		DocumentQueueResult documentQueueResultItem = documentQueueService.getDocumentQueueResultItem(docResultId);
		final FileBytes downloadFile = FileBytes.of(
			documentQueueResultItem.getFileBlob().getFileName(),
			documentQueueResultItem.getFileBlob().getMimeType(),
			BlobUtil.getBlobBytes(documentQueueResultItem.getFileBlob().getData()),
			LocalDateTime.now()
		);

		return RestUtil.buildFileResponse(Disposition.ATTACHMENT, downloadFile);
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("jobs")
	@RolesAllowed({ KT_MEDIZINISCHER_REPORTER, OI_IMPFVERANTWORTUNG, OI_LOGISTIK_REPORTER })
	public List<DocumentQueueJax> findJobsForBenutzer() {
		Optional<Benutzer> benutzerOpt = userPrincipal.getBenutzer();
		Validate.isTrue(benutzerOpt.isPresent(), "Benutzer muss bekannt sein");
		Collection<DocumentQueue> jobsForBenutzer = this.documentQueueService.findJobsForBenutzer(benutzerOpt.get());

		List<DocumentQueueJax> mappedDocumentQueueJobs = jobsForBenutzer.stream()
			.sorted(Comparator.comparing(
				DocumentQueue::getResultTimestamp,
				Comparator.nullsLast(Comparator.naturalOrder())))
			.map(DocumentQueueJax::new)
			.collect(Collectors.toList());

		return mappedDocumentQueueJobs;
	}

	@NonNull
	private Benutzer currentBenutzer() {
		Optional<Benutzer> benutzerOpt = userPrincipal.getBenutzer();
		Validate.isTrue(benutzerOpt.isPresent(), "Benutzer fuer den der Report erstellt werden soll muss vorhanden "
			+ "sein");
		Benutzer benutzer = benutzerOpt.get();
		return benutzer;
	}
}

