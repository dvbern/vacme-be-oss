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

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.embeddables.FileBytes;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.fhir.FhirService;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.ImpfdossierFileService;
import ch.dvbern.oss.vacme.service.PdfService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.RestUtil;
import ch.dvbern.oss.vacme.visitor.DocumentFileTyp;
import com.github.HonoluluHenk.httpcontentdisposition.Disposition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_MEDIZINISCHER_REPORTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_DOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_DOWNLOAD))
@Path(VACME_WEB + "/download")
public class DownloadResource {

	private static final String DOCUMENTS = "documents";

	private final Authorizer authorizer;
	private final RegistrierungService registrierungService;
	private final PdfService pdfService;
	private final ImpfdossierFileService impfdossierFileService;
	private final ImpfinformationenService impfinformationenService;
	private final FhirService fhirService;


	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path(DOCUMENTS + "/registrierungsbestaetigung/{registrierungsnummer}")
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
	public Response downloadRegistrierungsBestaetigung(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);

		authorizer.checkReadAuthorization(registrierung);

		final byte[] content = pdfService.createRegistrationsbestaetigung(registrierung);
		return createDocumentResponse(DocumentFileTyp.REGISTRIERUNG_BESTAETIGUNG, registrierungsnummer, content);
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path(DOCUMENTS + "/terminbestaetigung/{registrierungsnummer}/krankheit/{krankheit}")
	@RolesAllowed({OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE  })
	public Response downloadTerminBestaetigung(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit
	) {
		final ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			krankheit);

		authorizer.checkReadAuthorization(infos.getRegistrierung());

		Impftermin boosterTerminOrNull = null;
		if (ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(infos.getImpfdossier().getDossierStatus())) {
			final Optional<Impftermin> pendingBoosterTermin = ImpfinformationenService.getPendingBoosterTermin(infos);
			if (pendingBoosterTermin.isPresent()) {
				boosterTerminOrNull = pendingBoosterTermin.get();
			}
		}
		final byte[] content = pdfService.createTerminbestaetigung(infos.getImpfdossier(), boosterTerminOrNull);
		return createDocumentResponse(DocumentFileTyp.REGISTRIERUNG_BESTAETIGUNG, registrierungsnummer, content);
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path(DOCUMENTS + "/impfdokumentation/krankheit/{krankheit}/{registrierungsnummer}")
	@RolesAllowed({OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG })
	public Response downloadImpfdokumentation(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		final ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			krankheit);

		authorizer.checkReadAuthorization(infos.getRegistrierung());

		final byte[] content = pdfService.createImpfdokumentation(infos);
		return createDocumentResponse(DocumentFileTyp.IMPF_DOKUMENTATION, registrierungsnummer, content);
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path(DOCUMENTS + "/fhirimpfdokumentation/{registrierungsnummer}")
	@RolesAllowed(KT_MEDIZINISCHER_REPORTER)
	public Response downloadFhirImpfdokumentation(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		// TODO Affenpocken
		final ImpfinformationDto impfinformationen =
			impfinformationenService.getImpfinformationen(registrierungsnummer, KrankheitIdentifier.COVID);

		// FHIR Impfdokumentation wird nur erstellt, wenn mind. 1 VacMe-Impfung existiert
		if (!impfinformationenService.hasVacmeImpfungen(impfinformationen)) {
			throw AppValidationMessage.NO_VACME_IMPFUNG.create();
		}

		final byte[] content = fhirService.createFhirImpfdokumentationXML(impfinformationen);
		return createXMLDocumentResponse("fhirImpfdokumentation", registrierungsnummer, content);
	}

	private Response createDocumentResponse(DocumentFileTyp typ, String registrierungsnummer, byte[] content) {
		String translatedFileName = ServerMessageUtil.translateEnumValue(typ, Locale.GERMAN, registrierungsnummer);
		CleanFileName fileName = new CleanFileName(translatedFileName);
		final FileBytes downloadFile = FileBytes.of(
			fileName,
			MimeType.APPLICATION_PDF,
			content,
			LocalDateTime.now()
		);
		return RestUtil.buildFileResponse(Disposition.ATTACHMENT, downloadFile);
	}

	private Response createXMLDocumentResponse(String filename, String registrierungsnummer, byte[] content) {
		CleanFileName fileName = new CleanFileName(filename + '_' + registrierungsnummer);
		final FileBytes downloadFile = FileBytes.of(
			fileName,
			MimeType.APPLICATION_XML,
			content,
			LocalDateTime.now()
		);
		return RestUtil.buildFileResponse(Disposition.ATTACHMENT, downloadFile);
	}
}
