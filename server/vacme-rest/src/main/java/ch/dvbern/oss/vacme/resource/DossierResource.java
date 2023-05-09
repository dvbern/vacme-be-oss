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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.jax.FileInfoJax;
import ch.dvbern.oss.vacme.jax.registration.DashboardJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfdossierSummaryJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfdossiersOverviewJax;
import ch.dvbern.oss.vacme.jax.registration.RegistrierungBasicInfoJax;
import ch.dvbern.oss.vacme.jax.registration.ZertifikatJax;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.ApplicationPropertyCacheService;
import ch.dvbern.oss.vacme.service.BenutzerService;
import ch.dvbern.oss.vacme.service.FragebogenService;
import ch.dvbern.oss.vacme.service.ImpfdossierFileService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.KrankheitPropertyCacheService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.ZertifikatRunnerService;
import ch.dvbern.oss.vacme.service.ZertifikatRunnerService.ZertifikatResultDto;
import ch.dvbern.oss.vacme.service.ZertifikatService;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertBatchType;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jetbrains.annotations.Nullable;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_BENUTZER_VERWALTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_MEDIZINISCHE_NACHDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_NACHDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_DOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;
import static ch.dvbern.oss.vacme.util.ZertifikatDownloadUtil.zertifikatBlobToDownloadResponse;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_DOSSIER))
@Path(VACME_WEB + "/dossier")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DossierResource {

	private final Authorizer authorizer;
	private final RegistrierungService registrierungService;
	private final ImpfdossierFileService impfdossierFileService;
	private final FragebogenService fragebogenService;
	private final ZertifikatService zertifikatService;
	private final ApplicationPropertyCacheService propertyCacheService;
	private final ZertifikatRunnerService zertifikatRunnerService;
	private final ImpfinformationenService impfinformationenService;
	private final BenutzerService benutzerService;
	private final KrankheitPropertyCacheService krankheitPropertyCacheService;

	private final ImpfdossierService impfdossierService;

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/dossier-overview/{registrierungsnummer}")
	@RolesAllowed({ OI_KONTROLLE, OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, AS_BENUTZER_VERWALTER })
	public ImpfdossiersOverviewJax getImpfdossiersOverview(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		final Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);

		authorizer.checkReadAuthorization(registrierung);

		List<ImpfdossierSummaryJax> dossierSummaryList = new ArrayList<>();
		for (KrankheitIdentifier krankheit : KrankheitIdentifier.values()) {
			Optional<ImpfinformationDto> infosOpt =
				this.impfinformationenService.getImpfinformationenOptional(registrierungsnummer, krankheit);
			boolean noFreieTermin = krankheitPropertyCacheService.noFreieTermin(krankheit);
			infosOpt.ifPresent(impfinformationDto -> dossierSummaryList.add(ImpfdossierSummaryJax.of(
				impfinformationDto,
				noFreieTermin)));
		}

		return ImpfdossiersOverviewJax.of(registrierung, dossierSummaryList);
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/dashboard/{registrierungsnummer}")
	@RolesAllowed({ OI_KONTROLLE, OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, AS_BENUTZER_VERWALTER })
	public DashboardJax getDashboardRegistrierung(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		LOG.warn("TODO Affenpocken: Diese Methode wird temporaer verwendet an Stellen, wo es Krankheitsunabhaengig sein soll. Noch zu definieren, wie der Returnwert sein wird");
		// TODO Affenpocken: VACME-2406 Diese Methode wird temporaer verwendet an Stellen, wo es Krankheitsunabhaengig sein soll. Noch zu definieren, wie der Returnwert sein wird
		final DashboardJax dashboardJax =
			getDashboardImpfdossier(registrierungsnummer, KrankheitIdentifier.COVID);
		// TODO Wir blenden die (Dossier-)Kommentare aus, da das Dashboard hier Krankheitsunabhängig sein sollte
		dashboardJax.setKommentare(null);
		return dashboardJax;
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/dashboard/{registrierungsnummer}/krankheit/{krankheit}")
	@RolesAllowed({ OI_KONTROLLE, OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, AS_BENUTZER_VERWALTER })
	public DashboardJax getDashboardImpfdossier(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit
	) {
		ImpfinformationDto impfinformationen = this.impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			krankheit);
		Fragebogen fragebogen = this.fragebogenService.findFragebogenByRegistrierungsnummer(
			registrierungsnummer);
		Registrierung registrierung = impfinformationen.getRegistrierung();
		authorizer.checkReadAuthorization(registrierung);

		boolean hasCovidZertifikat = false;
		LocalDateTime timestampLetzterPostversand = null;

		if (krankheit.isSupportsZertifikat()) {
			if (ImpfdossierStatus.getStatusWithPossibleZertifikat()
				.contains(impfinformationen.getImpfdossier().getDossierStatus())) {
				hasCovidZertifikat = zertifikatService.hasCovidZertifikat(registrierungsnummer);
				timestampLetzterPostversand =
					zertifikatRunnerService.getTimestampOfLastPostversand(impfinformationen.getImpfdossier());
			}
		}

		return new DashboardJax(
			impfinformationen,
			fragebogen,
			hasCovidZertifikat,
			timestampLetzterPostversand
		);
	}

	// todo Affenpocken, wie wollen wir das definitiv handhaben?
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/dashboard/{registrierungsnummer}/krankheit/{krankheit}")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public void createImpfdossier(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheitIdentifier
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		this.impfdossierService.getOrCreateImpfdossier(registrierung, krankheitIdentifier);
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/dashboard/kvk-nummer/{kvkNummer}")
	@RolesAllowed({ OI_KONTROLLE, OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG })
	public List<RegistrierungBasicInfoJax> searchDashboardRegistrierung(
		@NonNull @NotNull @PathParam("kvkNummer") String kvkNummer
	) {
		return registrierungService.searchRegistrierungByKvKNummer(kvkNummer)
			.stream()
			.map(RegistrierungBasicInfoJax::from)
			.collect(Collectors.toList());
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("krankheit/{krankheit}/{registrierungsnummer}/fileinfo")
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
	@Operation(description = "Gibt die Metainfos der hochgeladenen Files zurueck")
	public List<FileInfoJax> getFileInfo(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		Impfdossier impfdossier = impfdossierService.getOrCreateImpfdossier(registrierung, krankheit);
		authorizer.checkReadAuthorization(registrierung);

		return impfdossierFileService.getUploadedDocInfos(impfdossier);
	}

	@Nullable
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{registrierungsnummer}/username")
	@RolesAllowed({ KT_NACHDOKUMENTATION })
	@Operation(description = "Gibt die benutzername fuer registrierung zurueck")
	public String getUsername(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		authorizer.checkReadAuthorization(registrierung);
		if (registrierung.getBenutzerId() == null) {
			return null;
		}
		return benutzerService.getById(Benutzer.toId(registrierung.getBenutzerId()))
			.map(Benutzer::getBenutzername)
			.orElse(null);
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
		schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path("download/zertifikat/{registrierungsnummer}")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public Response downloadZertifikat(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);

		authorizer.checkReadAuthorization(registrierung);
		Optional<Zertifikat> zertifikatOpt = zertifikatService.getBestMatchingZertifikat(registrierung);
		if (zertifikatOpt.isPresent()) {
			return zertifikatBlobToDownloadResponse(zertifikatService.getZertifikatPdf(zertifikatOpt.get()));
		} else {
			throw AppValidationMessage.NO_ZERTIFIKAT_PDF.create(registrierungsnummer);
		}
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
		schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path("download/zertifikatwithid/{zertifikatid}/{registrierungsnummer}")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public Response downloadZertifikatWithId(
		@NonNull @NotNull @PathParam("zertifikatid") UUID zertifikatId,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		authorizer.checkReadAuthorization(registrierung);

		Zertifikat zertifikat = zertifikatService.getZertifikatById(new ID(zertifikatId, Zertifikat.class));
		return zertifikatBlobToDownloadResponse(this.zertifikatService.getZertifikatPdf(zertifikat));
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("list/zertifikate/{registrierungsnummer}")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public List<ZertifikatJax> getAllZertifikate(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer) {

		ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			KrankheitIdentifier.COVID);
		authorizer.checkReadAuthorization(infos.getRegistrierung());

		List<Zertifikat> zertifikatList =
			zertifikatService.getAllZertifikateRegardlessOfRevocation(infos.getRegistrierung());

		return zertifikatService.mapToZertifikatJax(zertifikatList);

	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create/zertifikat/{registrierungsnummer}")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	@Schema(type = SchemaType.STRING, format = "binary")
	public Response createAndDownload(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) throws Exception {
		final ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			KrankheitIdentifier.COVID);
		authorizer.checkUpdateAuthorization(infos.getRegistrierung());
		final Impfung newestImpfung = ImpfinformationenService.getNewestVacmeImpfung(infos);
		final boolean zertifikatPending = newestImpfung != null && newestImpfung.isGenerateZertifikat();
		if (zertifikatPending) {
			// In diesen Fall versuchen wir, fuer die neueste Impfung ein Zertifikat zu erstellen

			Objects.requireNonNull(newestImpfung);
			final ID<Impfung> idOfNewestImpfung = Impfung.toId(newestImpfung.getId());
			ZertifikatResultDto result = zertifikatRunnerService.createZertifikatForRegistrierung(
				registrierungsnummer,
				idOfNewestImpfung,
				CovidCertBatchType.ONLINE);
			if (!result.success) {
				throw result.exception != null ?
					result.exception :
					AppValidationMessage.ZERTIFIKAT_GENERIERUNG_FEHLER.create();
			}
		}
		return Response.ok().build();
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("deleteRegistrierung/{registrierungsnummer}")
	@RolesAllowed({ AS_BENUTZER_VERWALTER, KT_NACHDOKUMENTATION, KT_MEDIZINISCHE_NACHDOKUMENTATION })
	public Response deleteRegistrierung(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Objects.requireNonNull(registrierungsnummer);
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		final Fragebogen fragebogen = fragebogenService.findFragebogenByRegistrierungsnummer(registrierungsnummer);

		registrierungService.deleteRegistrierung(registrierung, fragebogen);
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("isZertifikatEnabled")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public boolean isZertifikatEnabled() {
		return propertyCacheService.isZertifikatEnabled();
	}
}

