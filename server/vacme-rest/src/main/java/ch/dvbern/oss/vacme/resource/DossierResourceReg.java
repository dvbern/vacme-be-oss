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

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.onboarding.Onboarding;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OdiNoFreieTermine;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.enums.Kundengruppe;
import ch.dvbern.oss.vacme.enums.KundengruppeFilter;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.jax.OrtDerImpfungDisplayNameJax;
import ch.dvbern.oss.vacme.jax.registration.DashboardJax;
import ch.dvbern.oss.vacme.jax.registration.ErkrankungJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfdossierSummaryJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfdossiersOverviewJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfslotJax;
import ch.dvbern.oss.vacme.jax.registration.NextFreierTerminJax;
import ch.dvbern.oss.vacme.jax.registration.OrtDerImpfungBuchungJax;
import ch.dvbern.oss.vacme.jax.registration.OrtDerImpfungDisplayNameExtendedJax;
import ch.dvbern.oss.vacme.jax.registration.RegistrierungsCodeJax;
import ch.dvbern.oss.vacme.jax.registration.SelectOrtDerImpfungJax;
import ch.dvbern.oss.vacme.jax.registration.TerminbuchungJax;
import ch.dvbern.oss.vacme.jax.registration.ZertifikatJax;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.ApplicationPropertyCacheService;
import ch.dvbern.oss.vacme.service.DossierService;
import ch.dvbern.oss.vacme.service.FragebogenService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.KrankheitPropertyCacheService;
import ch.dvbern.oss.vacme.service.NextTerminCacheService;
import ch.dvbern.oss.vacme.service.OdiNoFreieTermineService;
import ch.dvbern.oss.vacme.service.OrtDerImpfungService;
import ch.dvbern.oss.vacme.service.PdfService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.TerminbuchungService;
import ch.dvbern.oss.vacme.service.ZertifikatRunnerService;
import ch.dvbern.oss.vacme.service.ZertifikatService;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertBatchType;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.service.onboarding.OnboardingService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.QRCodeUtil;
import ch.dvbern.oss.vacme.util.RestUtil;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import ch.dvbern.oss.vacme.visitor.DocumentFileTyp;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.hibernate.exception.ConstraintViolationException;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.CC_AGENT;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.CC_BENUTZER_VERWALTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.IMPFTERMINCLIENT;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.IMPFWILLIGER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.GEBUCHT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.GEBUCHT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.KONTROLLIERT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ODI_GEWAEHLT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ODI_GEWAEHLT_BOOSTER;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_INITIALREG;
import static ch.dvbern.oss.vacme.util.ZertifikatDownloadUtil.zertifikatBlobToDownloadResponse;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_DOSSIER))
@Path(VACME_INITIALREG + "/dossier")
public class DossierResourceReg { // todo homa affenpocken, das muessen wir anschauen vs DossierRessource. Vielleicht Downloads fuer Reg in ein DownloadReg

	@ConfigProperty(name = "vacme.terminreservation.enabled", defaultValue = "false")
	protected boolean terminReservationEnabled;

	private final Authorizer authorizer;
	private final DossierService dossierService;
	private final RegistrierungService registrierungService;
	private final RegistrierungRepo registrierungRepo;
	private final OrtDerImpfungService ortDerImpfungService;
	private final PdfService pdfService;
	private final TerminbuchungService terminbuchungService;
	private final FragebogenService fragebogenService;
	private final NextTerminCacheService nextTerminCacheService;
	private final UserPrincipal userPrincipal;
	private final ZertifikatService zertifikatService;
	private final ZertifikatRunnerService zertifikatRunnerService;
	private final ApplicationPropertyCacheService propertyCacheService;
	private final ImpfinformationenService impfinformationenService;
	private final OnboardingService onboardingService;
	private final TransactionManager tm;
	private final BoosterService boosterService;
	private final ImpfdossierService impfdossierService;
	private final KrankheitPropertyCacheService krankheitPropertyCacheService;
	private final OdiNoFreieTermineService odiNextFreieTermineService;


	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/impfdossier/{registrierungsnummer}/krankheit/{krankheit}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public ImpfdossierSummaryJax getOrCreateImpfdossier(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheitIdentifier
	) {
		authorizer.checkCallcenterAllowedForKrankheit(krankheitIdentifier);
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		// to prevent unintended recalculation of impfschutz we check if the dossier exists already, well calls this on each login
		Optional<Impfdossier> dossierOpt =
			impfdossierService.findImpfdossierForRegnumAndKrankheitOptional(registrierungsnummer, krankheitIdentifier);
		if (dossierOpt.isEmpty()) {
			this.impfdossierService.getOrCreateImpfdossier(registrierung, krankheitIdentifier);
			// since we know it is a new dossier we should recalculate the Impfschutz in case we start in N-Impfung mode
			boosterService.recalculateImpfschutzAndStatusmovesForSingleRegWithReload(registrierung, krankheitIdentifier);
		}
		return getImpfDossierSummaryJax(registrierungsnummer, krankheitIdentifier);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/accept-leistungserbringer-agb/krankheit/{krankheit}/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER })
	public ImpfdossierSummaryJax acceptLeistungserbringerAgb(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheitIdentifier
	) {
		impfdossierService.acceptLeistungserbringerAgb(registrierungsnummer, krankheitIdentifier);
		return getImpfDossierSummaryJax(registrierungsnummer, krankheitIdentifier);
	}

	@NonNull
	private ImpfdossierSummaryJax getImpfDossierSummaryJax(
		@NonNull String registrierungsnummer,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		Optional<ImpfinformationDto> infosOpt =
			this.impfinformationenService.getImpfinformationenOptional(registrierungsnummer, krankheitIdentifier);
		boolean noFreieTermine = krankheitPropertyCacheService.noFreieTermin(krankheitIdentifier);
		return ImpfdossierSummaryJax.of(infosOpt.orElseThrow(), noFreieTermine);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/termine/frei/krankheit/{krankheit}/{ortDerImpfungId}/{impffolge}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT, IMPFTERMINCLIENT })
	public List<ImpfslotJax> getFreieImpftermine(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId,
		@NonNull @NotNull @PathParam("impffolge") Impffolge impffolge,
		@NonNull @NotNull
		@Parameter(description = "Datum zu dem der erste passende Gegenstuecktermin gefunden werden soll")
			NextFreierTerminJax date
	) {
		return ortDerImpfungService
			.getFreieImpftermine(OrtDerImpfung.toId(ortDerImpfungId), impffolge, date.getNextDate().toLocalDate(), krankheit)
			.stream()
			.map(ImpfslotJax::new)
			.collect(Collectors.toList());
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/termine/nextfrei/krankheit/{krankheit}/{ortDerImpfungId}/{impffolge}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT, IMPFTERMINCLIENT })
	@Schema(format = OpenApiConst.Format.DATE_TIME)
	@Nullable
	public NextFreierTerminJax getNextFreierImpftermin(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId,
		@NonNull @NotNull @PathParam("impffolge") Impffolge impffolge,
		@Nullable @Parameter(description = "Datum zu dem der erste passende Gegenstuecktermin gefunden werden soll")
			NextFreierTerminJax otherTerminDate
	) {
		LocalDateTime nextDate = otherTerminDate != null ? otherTerminDate.getNextDate() : null;
		LocalDateTime nextFreierImpftermin;

		// erstimpfungen with no other Termindate and Boostertermine are cached
		nextFreierImpftermin =
			nextTerminCacheService.getNextFreierImpfterminThroughCache(
				OrtDerImpfung.toId(ortDerImpfungId),
				impffolge,
				nextDate,
				nextDate != null,
				krankheit); // if the otherTerminDate is null then the limitMaxFuture argument does not really matter but cache
		// expects it to be false
		if (nextFreierImpftermin == null) {
			return null;
		}
		return new NextFreierTerminJax(nextFreierImpftermin);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/termine/nextfrei/{ortDerImpfungId}/umbuchung")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	@Schema(format = OpenApiConst.Format.DATE_TIME)
	@Nullable
	public NextFreierTerminJax getNextFreierZweitterminUmbuchung(
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId,
		@Nullable @Parameter(description = "Datum zu dem der erste passende Gegenstuecktermin gefunden werden soll")
			NextFreierTerminJax otherTerminDate
	) {
		LocalDateTime nextDate = otherTerminDate != null ? otherTerminDate.getNextDate() : null;
		LocalDateTime nextFreierImpftermin = ortDerImpfungService.getNextFreierImpftermin(
			OrtDerImpfung.toId(ortDerImpfungId), Impffolge.ZWEITE_IMPFUNG, nextDate, false, KrankheitIdentifier.COVID);
		if (nextFreierImpftermin == null) {
			return null;
		}
		return new NextFreierTerminJax(nextFreierImpftermin);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("/buchen")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response termineBuchen(
		@NonNull @NotNull @Valid TerminbuchungJax termin
	) {
		String registrierungsnummer = termin.getRegistrierungsnummer();

		final ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				termin.getKrankheit());
		Registrierung registrierung = infos.getRegistrierung();
		Impfdossier dossier = infos.getImpfdossier();

		authorizer.checkUpdateAuthorization(dossier);

		// Wenn wir hier hin kommen, hat sich der Benutzer - wenn er nicht sowieso schon mobil war - als immobiler
		// nachtraeglich fuer ein stationaeres ODI entschieden
		registrierung.setImmobil(false);

		try {
			if (ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(dossier.getDossierStatus())) {
				// Wir sind schon im booster
				dossierService.termineBuchenBooster(
					infos,
					Impfslot.toId(termin.getSlotNId()));
			} else {
				dossierService.termineBuchenGrundimmunisierung(
					infos,
					Impfslot.toId(termin.getSlot1Id()),
					Impfslot.toId(termin.getSlot2Id()));
			}
		} catch (Exception exception) {
			Throwable rootCause = ExceptionUtils.getRootCause(exception);
			if (rootCause != null) {

				if (rootCause.getClass().equals(SQLIntegrityConstraintViolationException.class) ||
					ConstraintViolationException.class.equals(rootCause.getClass())
				) {
					throw AppValidationMessage.IMPFTERMIN_BESETZT.create("");
				}

			}
			throw exception;
		}
		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("/krankheit/{krankheit}/selectOdi")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response selectOrtDerImpfung(
		@PathParam("krankheit") @NonNull @NotNull KrankheitIdentifier krankheit,
		@NonNull @NotNull @Valid SelectOrtDerImpfungJax selectJax
	) {
		String registrierungsnummer = selectJax.getRegistrierungsnummer();
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				krankheit);

		authorizer.checkUpdateAuthorization(infos.getImpfdossier());

		dossierService.selectOrtDerImpfung(
			infos,
			OrtDerImpfung.toId(selectJax.getOdiId()));
		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("/selectNichtVerwalteterOdi/krankheit/{krankheit}/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response selectNichtVerwalteterOrtDerImpfung(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				krankheit);

		authorizer.checkUpdateAuthorization(infos.getImpfdossier());

		dossierService.selectNichtVerwalteterOrtDerImpfung(infos);

		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/dossier-overview/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public ImpfdossiersOverviewJax getImpfdossiersOverview(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer) {
		final Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);

		authorizer.checkReadAuthorization(registrierung);

		List<ImpfdossierSummaryJax> dossierSummaryList = new ArrayList<>();
		for (KrankheitIdentifier krankheit : KrankheitIdentifier.values()) {
			// CC-AGENTS duerfen nur fuer sie erlaubte Dossiers sehen
			if (userPrincipal.isCallerInRole(BenutzerRolle.CC_AGENT)) {
				if (!krankheit.isSupportsCallcenter()) {
					continue;
				}
			}
			Optional<ImpfinformationDto> infosOpt =
				this.impfinformationenService.getImpfinformationenOptional(registrierungsnummer, krankheit);
			boolean noFreieTermine = krankheitPropertyCacheService.noFreieTermin(krankheit);
			infosOpt.ifPresent(impfinformationDto -> dossierSummaryList.add(ImpfdossierSummaryJax.of(impfinformationDto, noFreieTermine)));
		}

		return ImpfdossiersOverviewJax.of(registrierung, dossierSummaryList);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("reservieren/{registrierungsnummer}/krankheit/{krankheit}/{impfslotId}/{impffolge}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response reservieren(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("impfslotId") UUID impfslotId,
		@NonNull @NotNull @PathParam("impffolge") Impffolge impffolge
	) {
		authorizer.checkCallcenterAllowedForKrankheit(krankheit);
		// Falls die Reservation ausgeschaltet ist, wollen wir direkt abbrechen
		if (!terminReservationEnabled) {
			return Response.ok().build();
		}

		Objects.requireNonNull(registrierungsnummer);
		Objects.requireNonNull(impfslotId);
		Objects.requireNonNull(impffolge);

		final Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);

		authorizer.checkUpdateAuthorization(registrierung);

		terminbuchungService.reservieren(registrierung, Impfslot.toId(impfslotId), impffolge);
		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("umbuchen")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response umbuchen(
		@NonNull @NotNull @Valid TerminbuchungJax termin
	) {
		String registrierungsnummer = termin.getRegistrierungsnummer();
		ImpfinformationDto impfinformationen =
			this.impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				termin.getKrankheit());
		Impfdossier dossier = impfinformationen.getImpfdossier();

		authorizer.checkUpdateAuthorization(dossier);

		if (ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(dossier.getDossierStatus())) {
			// Booster
			// muss min. freigebeben_booster sein zum selber buchen!
			ValidationUtil.validateStatusOneOf(
				dossier,
				FREIGEGEBEN_BOOSTER, ODI_GEWAEHLT_BOOSTER, GEBUCHT_BOOSTER, KONTROLLIERT_BOOSTER);

			terminbuchungService.umbuchenBooster(
				impfinformationen,
				Impfslot.toId(termin.getSlotNId()));
		} else {
			// Impfung 1/2
			// muss mind. freigegeben sein zum selber buchen!
			ValidationUtil.validateStatusOneOf(
				dossier,
				FREIGEGEBEN,
				ODI_GEWAEHLT,
				GEBUCHT,
				IMPFUNG_1_KONTROLLIERT,
				IMPFUNG_1_DURCHGEFUEHRT,
				IMPFUNG_2_KONTROLLIERT);

			terminbuchungService.umbuchenGrundimmunisierung(
				impfinformationen.getImpfdossier(),
				Impfslot.toId(termin.getSlot1Id()),
				Impfslot.toId(termin.getSlot2Id()));
		}

		return Response.ok().build();
	}

	/**
	 * Cancel the selected ODI and additionally cancel any booked termin
	 *
	 * @param registrierungsnummer no-doc
	 * @return status code
	 */
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	@Path("/cancel/krankheit/{krankheit}/{registrierungsnummer}")
	public Response odiAndTermineAbsagen(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				krankheit);

		authorizer.checkUpdateAuthorization(infos.getImpfdossier());

		dossierService.odiAndTermineAbsagen(infos);

		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/dashboard/krankheit/{krankheit}/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public DashboardJax getDashboardRegistrierung(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		ImpfinformationDto impfinformationen =
			this.impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				krankheit);
		Registrierung registrierung = impfinformationen.getRegistrierung();
		Impfdossier dossier = impfinformationen.getImpfdossier();

		authorizer.checkReadAuthorization(dossier);
		Fragebogen fragebogen = this.fragebogenService.findFragebogenByRegistrierungsnummer(registrierungsnummer);
		boolean hasCovidZertifikat = false;
		LocalDateTime timestampLetzterPostversand = null;
		if (ImpfdossierStatus.getStatusWithPossibleZertifikat().contains(dossier.getDossierStatus())) {
			hasCovidZertifikat = zertifikatService.hasCovidZertifikat(registrierungsnummer);
			timestampLetzterPostversand = zertifikatRunnerService.getTimestampOfLastPostversand(dossier);
		}

		return new DashboardJax(
			impfinformationen,
			fragebogen,
			hasCovidZertifikat,
			timestampLetzterPostversand
		);
	}

	@GET
	@Nullable
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/onboarding/dashboard/{onboardingCode}")
	@RolesAllowed({ CC_AGENT, CC_BENUTZER_VERWALTER })
	public RegistrierungsCodeJax getDashboardRegistrierungByOnboardingCode(
		@NonNull @NotNull @PathParam("onboardingCode") String onboardingCode
	) {
		Optional<Onboarding> onboardingByCode = this.onboardingService.findOnboardingByCode(onboardingCode);
		if (onboardingByCode.isPresent()) {
			Onboarding onboarding = onboardingByCode.get();
			Registrierung registrierung = onboarding.getRegistrierung();

			return RegistrierungsCodeJax.from(registrierung);
		}
		return null;
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/odi/all/krankheit/{krankheit}/{registrierungsnummer}/{kundengruppe}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT, IMPFTERMINCLIENT })
	public List<OrtDerImpfungDisplayNameExtendedJax> getAllOrteDerImpfungDisplayName(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("kundengruppe") KundengruppeFilter kundengruppeFilter
	) {
		Kundengruppe kundengruppe = mapKundengruppeFilterToKundengruppe(kundengruppeFilter);
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				krankheit);
		Fragebogen fragebogen = fragebogenService.findFragebogenByRegistrierungsnummer(
			registrierungsnummer);
		authorizer.checkReadAuthorization(infos.getImpfdossier());

		return ortDerImpfungService.findAllActivePublicFiltered(fragebogen, infos, kundengruppe)
				.stream()
				.map(ortDerImpfung -> {
					OrtDerImpfungDisplayNameExtendedJax odiJax = null;

					if (ortDerImpfung.isTerminverwaltung()) {

						final OdiNoFreieTermine odiFreieTermine =
							odiNextFreieTermineService.getOrCreateByOdiAndKrankheit(ortDerImpfung, krankheit);

						odiJax = new OrtDerImpfungDisplayNameExtendedJax(ortDerImpfung, odiFreieTermine);

						if (krankheit.isSupportsImpffolgenEinsUndZwei()) {
							if (!odiFreieTermine.isNoFreieTermine1()) {
								LocalDateTime nextTermin1 = nextTerminCacheService.getNextFreierImpfterminThroughCache(
									ortDerImpfung.toId(),
									Impffolge.ERSTE_IMPFUNG,
									null,
									false,
									krankheit);
								odiJax.setNextTermin1Date(nextTermin1);
							}
							if (!odiFreieTermine.isNoFreieTermine2()) {
								LocalDateTime nextTermin2 = nextTerminCacheService.getNextFreierImpfterminThroughCache(
									ortDerImpfung.toId(),
									Impffolge.ZWEITE_IMPFUNG,
									null,
									false,
									krankheit);
								odiJax.setNextTermin2Date(nextTermin2);
							}
						}

						if (!odiFreieTermine.isNoFreieTermineN()) {
							LocalDateTime nextTerminN = nextTerminCacheService.getNextFreierImpfterminThroughCache(
								ortDerImpfung.toId(),
								Impffolge.BOOSTER_IMPFUNG,
								null,
								false,
								krankheit);
							odiJax.setNextTerminNDate(nextTerminN);
						}
					}
					else {
						// Keine Terminverwaltung. Wir brauchen nichts von der DB zu lesen und geben ein
						// Dummy-OdiNoFreieTermine mit in der es fuer keine Impffolge freie Termine gibt
						odiJax = new OrtDerImpfungDisplayNameExtendedJax(ortDerImpfung, OdiNoFreieTermine.createDummy(krankheit, ortDerImpfung));
					}

					return odiJax;
				})
				.collect(Collectors.toList());
	}

	@Nullable
	static Kundengruppe mapKundengruppeFilterToKundengruppe(@NonNull KundengruppeFilter kundengruppeFilter) {
		Kundengruppe kundengruppe = null;
		if (kundengruppeFilter != KundengruppeFilter.ALL) {
			kundengruppe = Kundengruppe.valueOf(kundengruppeFilter.name());
		}
		return kundengruppe;
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/impfterminclient/odi")
	@RolesAllowed({ IMPFTERMINCLIENT })
	/**
	 * Wird von uns nicht direkt verwendet, aber von Rimpfli (externe api) (apiV1RegDossierImpfterminclientOdiGet)
	 */
	public List<OrtDerImpfungDisplayNameJax> getAllOrteDerImpfungForImpfterminclient() {
		return ortDerImpfungService.findAllActivePublic()
			.stream()
			.map(OrtDerImpfungDisplayNameJax::new)
			.collect(Collectors.toList());
	}


	@GET()
	@Produces({ "image/gif" })
	@Path("/qr-code/{code}")
	@PermitAll
	public Response getQrCode(@NotNull @PathParam("code") String code) {
		return generateQrCode(code, null);

	}
	@GET()
	@Produces({ "image/gif" })
	@Path("/qr-code/{code}/{krankheit}")
	@PermitAll
	public Response getQrCodeWithKrankheit(
		@NotNull @PathParam("code") String code,
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit
	) {
		return generateQrCode(code, krankheit);
	}

	private Response generateQrCode(@NonNull String code, @Nullable KrankheitIdentifier krankheit) {
		String url = QRCodeUtil.generateQrCodeUrl(code, krankheit);
		Response.ResponseBuilder responseBuilder;
		try {
			responseBuilder = Response.ok(QRCodeUtil.createQRImage(url, 300));
			responseBuilder.header("Content-Disposition", "attachment; filename=\"qrcode.gif\"");
			return responseBuilder.build();
		} catch (WriterException | IOException e) {
			LOG.error("Could not download qr-code", e);
			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path("download/registrierungsbestaetigung/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response downloadRegistrierungsbestaetigung(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);

		authorizer.checkReadAuthorization(registrierung);

		final byte[] content = pdfService.createRegistrationsbestaetigung(registrierung);
		return createDownloadResponse(DocumentFileTyp.REGISTRIERUNG_BESTAETIGUNG, registrierungsnummer, content);
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path("download/terminbestaetigung/krankheit/{krankheit}/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response downloadTerminbestaetigung(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(registrierungsnummer, krankheit);
		final Impfdossier impfdossier = infos.getImpfdossier();

		authorizer.checkReadAuthorization(impfdossier);
		if (!impfdossier.getBuchung().isNichtVerwalteterOdiSelected()
			&& impfdossier.getBuchung().getImpftermin1() == null
			&& impfdossier.getBuchung().getGewuenschterOdi() == null) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Kein Impftermin oder Gewuenschter ODI");
		}

		Impftermin boosterTerminOrNull = findBoosterTerminOrNull(infos);
		final byte[] content = pdfService.createTerminbestaetigung(impfdossier, boosterTerminOrNull);
		return createDownloadResponse(DocumentFileTyp.TERMIN_BESTAETIGUNG, registrierungsnummer, content);
	}

	@Nullable
	private Impftermin findBoosterTerminOrNull(
		@NonNull ImpfinformationDto infos
	) {
		Impftermin boosterTerminOrNull = null;
		Impfdossier impfdossier = infos.getImpfdossier();
		if (ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(impfdossier.getDossierStatus())) {
			final Optional<Impftermin> pendingBoosterTermin = ImpfinformationenService.getPendingBoosterTermin(infos);
			if (pendingBoosterTermin.isPresent()) {
				boosterTerminOrNull = pendingBoosterTermin.get();
			}
		}
		return boosterTerminOrNull;
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("terminbestaetigung/erneutsenden/krankheit/{krankheit}/{registrierungsnummer}")
	@RolesAllowed({ CC_AGENT })
	public RegistrierungsCodeJax terminbestaetigungErneutSenden(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(registrierungsnummer, krankheit);
		authorizer.checkReadAuthorization(infos.getImpfdossier());

		Impftermin boosterTerminOrNull = findBoosterTerminOrNull(infos);
		terminbuchungService.terminbestaetigungErneutSenden(infos.getImpfdossier(), boosterTerminOrNull);

		return RegistrierungsCodeJax.from(infos.getRegistrierung());
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/odibuchung/{odiId}/krankheit/{krankheit}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public OrtDerImpfungBuchungJax getOrtDerImpfungBuchung(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("odiId") UUID odiId
	) {
		OrtDerImpfung odi = ortDerImpfungService.getById(OrtDerImpfung.toId(odiId));
		OdiNoFreieTermine nextFreieTermine = null;
		if (odi.isTerminverwaltung()) {
			nextFreieTermine = odiNextFreieTermineService.getOrCreateByOdiAndKrankheit(odi, krankheit);
		} else {
			nextFreieTermine = OdiNoFreieTermine.createDummy(krankheit, odi);
		}
		return OrtDerImpfungBuchungJax.from(odi, nextFreieTermine);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path("download/impfdokumentation/krankheit/{krankheit}/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response downloadImpfdokumentation(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		final ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			krankheit);

		authorizer.checkCallcenterAllowedForKrankheit(krankheit);
		authorizer.checkReadAuthorization(infos.getRegistrierung());

		final byte[] content = pdfService.createImpfdokumentation(infos);
		return createDownloadResponse(DocumentFileTyp.IMPF_DOKUMENTATION, registrierungsnummer, content);
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path("download/zertifikat/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	/* Das Callcenter soll eigentlich das Zertifikat nicht sehen, ausser es ist eh schon auf der Overview.
		 Aber wenn das Zertifikat ueber die Reg-UUID erreichbar ist, kann das CC jede Person in der Adressaenderung
		 suchen,
		 dann das Zertifikat per RegID herunterladen, dort die UVCI lesen und die Registrierung in der UVCI-Suche
		 komplett oeffnen.
		  DESHALB: per RegNr herunterladen, nicht per RegID */
	public Response downloadBestZertifikatForRegistrierung(
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
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Path("download/zertifikatwithid/{zertifikatid}/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response downloadZertifikatWithId(
		@NonNull @NotNull @PathParam("zertifikatid") UUID zertifikatId,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		authorizer.checkReadAuthorization(registrierung);
		Zertifikat zertifikat = zertifikatService.getZertifikatById(new ID(zertifikatId, Zertifikat.class));
		return zertifikatBlobToDownloadResponse(this.zertifikatService.getZertifikatPdf(zertifikat));
	}

	@NonNull
	private Response createDownloadResponse(
		@NonNull DocumentFileTyp fileTyp,
		@NonNull String registrierungsnummer,
		byte[] content
	) {
		return RestUtil.createDownloadResponse(ServerMessageUtil.translateEnumValue(fileTyp, Locale.GERMAN,
			registrierungsnummer), content, MimeType.APPLICATION_PDF);
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("deleteRegistrierung/{registrierungsnummer}")
	@RolesAllowed(IMPFWILLIGER)
	public Response deleteRegistrierung(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Objects.requireNonNull(registrierungsnummer);
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		final Fragebogen fragebogen = fragebogenService.findFragebogenByRegistrierungsnummer(registrierungsnummer);

		// Auf Seite Registrierung darf nur der Benutzer sich selber loeschen
		authorizer.checkUpdateAuthorization(fragebogen.getRegistrierung());

		registrierungService.deleteRegistrierung(registrierung, fragebogen);
		return Response.ok().build();
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("deleteBenutzer")
	@RolesAllowed(IMPFWILLIGER)
	public Response deleteBenutzer(
	) {
		final Benutzer loggedInBenutzer = userPrincipal.getBenutzerOrThrowException();
		registrierungService.deleteBenutzer(loggedInBenutzer);
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("isZertifikatEnabled")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public boolean isZertifikatEnabled() {
		return propertyCacheService.isZertifikatEnabled();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{registrierungId}/zertifikat/resend")
	@RolesAllowed(CC_AGENT)
	public Response recreatePerPost(
		@NonNull @NotNull @PathParam("registrierungId") UUID registrierungId
	) throws Exception {

		Registrierung registrierung = registrierungService.findRegistrierungById(registrierungId)
			.orElseThrow(() -> AppFailureException.entityNotFound(Registrierung.class, registrierungId.toString()));
		authorizer.checkUpdateAuthorization(registrierung);

		final ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierung.getRegistrierungsnummer(),
				KrankheitIdentifier.COVID);
		authorizer.checkUpdateAuthorization(infos.getImpfdossier());
		// In diesen Fall versuchen wir, fuer die neueste Impfung ein Zertifikat zu erstellen
		ID<Impfung> idOfNewestImpfung = ImpfinformationenService.getNewestVacmeImpfungId(infos);

		this.zertifikatRunnerService.createZertifikatForRegistrierung(registrierung, idOfNewestImpfung, CovidCertBatchType.POST);
		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("list/zertifikate/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public List<ZertifikatJax> getAllZertifikate(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			KrankheitIdentifier.COVID);
		authorizer.checkReadAuthorization(infos.getImpfdossier());

		List<Zertifikat> zertifikatList =
			zertifikatService.getAllZertifikateRegardlessOfRevocation(infos.getRegistrierung());

		return zertifikatService.mapToZertifikatJax(zertifikatList);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("erkrankungen/{registrierungsnummer}/{rollback}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public DashboardJax updateErkrankungen(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("rollback") boolean rollback,
		@Nullable @Parameter List<ErkrankungJax> erkrankungJaxList
	) throws SystemException {
		// TODO Affenpocken: Aktuell sind nur Krankheiten fuer Covid unterstuetzt
		ImpfinformationDto infos =
			this.impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				KrankheitIdentifier.COVID);
		Registrierung registrierung = infos.getRegistrierung();
		authorizer.checkUpdateAuthorization(infos.getImpfdossier());

		// Hierhin sollten wir nur aus einem BoosterStatus kommen
		if (!ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(infos.getImpfdossier().getDossierStatus())) {
			throw AppValidationMessage.ILLEGAL_STATE.create(
				"Erkrankungen (Selbstdeklaration) darf man nur im Booster-Status bearbeiten");
		}
		// Im Status Booster-Kontrolliert darf man die Erkrankung auch nicht bearbeiten
		if (ImpfdossierStatus.KONTROLLIERT_BOOSTER == infos.getImpfdossier().getDossierStatus()) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Erkrankungen (Selbstdeklaration) darf man nach der Kontrolle nicht bearbeiten");
		}

		Objects.requireNonNull(erkrankungJaxList);
		dossierService.updateErkrankungen(infos, erkrankungJaxList, rollback);

		if (rollback) {
			// Mit rollback=true kann man ausprobieren, was passieren wuerde, wenn man die Erkrankungen speichern wuerde
			tm.setRollbackOnly();
		}

		// Reload, da der Impfschutz (auf dem Dossier) aufgrund der Erkrankungen neu berechnet wurde
		// TODO Affenpocken: Aktuell sind nur Krankheiten fuer Covid unterstuetzt
		ImpfinformationDto impfinformationen =
			this.impfinformationenService.getImpfinformationen(
				registrierung.getRegistrierungsnummer(),
				KrankheitIdentifier.COVID);
		Fragebogen fragebogen = this.fragebogenService.findFragebogenByRegistrierungsnummer(
			registrierungsnummer);
		return new DashboardJax(impfinformationen, fragebogen);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("/mobil/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response changeToMobil(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		final Registrierung registrierung = registrierungRepo
			.getByRegistrierungnummer(registrierungsnummer)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_REGISTRIERUNGSNUMMER.create(registrierungsnummer));

		authorizer.checkUpdateAuthorization(registrierung);

		registrierung.setImmobil(false);

		boosterService.recalculateImpfschutzAndStatusmovesForSingleRegWithReload(registrierung);

		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("/selbstzahler/{registrierungsnummer}/{selbstzahler}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response changeSelbstzahler(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@PathParam("selbstzahler") boolean selbstzahler
	) {
		// TODO Affenpocken: Selbstzahler aktuell nur COVID
		final Impfdossier impfdossier =
			impfdossierService
				.findImpfdossierForRegnumAndKrankheitOptional(registrierungsnummer, KrankheitIdentifier.COVID)
				.orElseThrow(() -> AppValidationMessage.UNKNOWN_REGISTRIERUNGSNUMMER.create(registrierungsnummer));

		authorizer.checkUpdateAuthorization(impfdossier);

		impfdossier.getBuchung().setSelbstzahler(selbstzahler);
		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("/phoneNumberUpdate/{registrierungsnummer}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	@Schema(description = "Wird aufgerufen wenn die registrierte Person per Popup gebeten wird ihre Mobile Nummer upzudaten"
		+ " weil sie nicht unseren Erwartungen entspricht. Verhindert, dass das Popup mehrmals in einer gegebenen Zeit angezeigt wird")
	public Response setTimestampPhonenumberUpdate(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		final Registrierung registrierung = registrierungRepo
			.getByRegistrierungnummer(registrierungsnummer)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_REGISTRIERUNGSNUMMER.create(registrierungsnummer));
		authorizer.checkUpdateAuthorization(registrierung);
		registrierung.setTimestampPhonenumberUpdate(LocalDateTime.now());
		return Response.ok().build();
	}
}

