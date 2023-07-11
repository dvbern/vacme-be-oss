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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.jax.impfslot.TermineAbsagenJax;
import ch.dvbern.oss.vacme.jax.registration.DashboardJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfslotJax;
import ch.dvbern.oss.vacme.jax.registration.NextFreierTerminJax;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.DossierService;
import ch.dvbern.oss.vacme.service.FragebogenService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.OrtDerImpfungService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.TerminbuchungService;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_REGISTRATION_OI;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_DOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_TERMINBUCHUNG))
@Path(VACME_WEB + "/terminbuchung")
public class TerminbuchungResource {

	private final OrtDerImpfungService ortDerImpfungService;
	private final TerminbuchungService terminbuchungService;
	private final RegistrierungService registrierungService;
	private final DossierService dossierService;
	private final ImpfterminRepo impfterminRepo;
	private final Authorizer authorizer;
	private final ImpfinformationenService impfinformationenService;
	private final FragebogenService fragebogenService;
	private final ImpfdossierService impfdossierService;
	private final VacmeSettingsService vacmeSettingsService;

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("createAdHocTermin1AndBucheTermin2/krankheit/{krankheit}/{registrierungsnummer}/{odiId}/{impfslot2Id}")
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
	public DashboardJax createAdHocTermin1AndBucheTermin2(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @PathParam("odiId") UUID odiId,
		@NonNull @PathParam("impfslot2Id") UUID impfslot2Id
	) {
		Objects.requireNonNull(registrierungsnummer);
		Objects.requireNonNull(odiId);
		Objects.requireNonNull(impfslot2Id);
		// Fachlich this method should only be called in a COVID context. PathParam is added for future extensibility
		// and consistency.
		Validate.isTrue(krankheit == KrankheitIdentifier.COVID);

		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				krankheit);
		final Registrierung registrierung = infos.getRegistrierung();


		authorizer.checkUpdateAuthorization(registrierung);

		final OrtDerImpfung ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(odiId));
		terminbuchungService.createAdHocTermin1AndBucheTermin2(infos, ortDerImpfung, Impfslot.toId(impfslot2Id));
		authorizer.checkBenutzerAssignedToOdi(ortDerImpfung);

		// Daten neu laden, damit die eben erstellten Termine / Statusanpassungen auf dem Dossier sind
		ImpfinformationDto impfinformationen = this.impfinformationenService.getImpfinformationen(
			registrierung.getRegistrierungsnummer(),
			krankheit);
		Fragebogen fragebogen = this.fragebogenService.findFragebogenByRegistrierungsnummer(
			registrierungsnummer);

		return new DashboardJax(impfinformationen, fragebogen);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("reservieren/{registrierungsnummer}/{impfslotId}/{impffolge}")
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
	public Response reservieren(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("impfslotId") UUID impfslotId,
		@NonNull @NotNull @PathParam("impffolge") Impffolge impffolge
	) {
		// Falls die Reservation ausgeschaltet ist, wollen wir direkt abbrechen
		if (!vacmeSettingsService.isTerminReservationEnabled()) {
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
	@Path("umbuchenGrundimmunisierung/krankheit/{krankheit}/{registrierungsnummer}/{impfslot1Id}/{impfslot2Id}")
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
	public DashboardJax umbuchenGrundimmunisierung(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @PathParam("impfslot1Id") UUID impfslot1Id,
		@NonNull @PathParam("impfslot2Id") UUID impfslot2Id
	) {
		Objects.requireNonNull(registrierungsnummer);
		Objects.requireNonNull(impfslot1Id);
		Objects.requireNonNull(impfslot2Id);
		// Fachlich this method should only be called in a COVID context. PathParam is added for future extensibility
		// and consistency.
		Validate.isTrue(KrankheitIdentifier.COVID == krankheit, "Method should only be called in a COVID context");

		final Impfdossier impfdossier =
			impfdossierService.findImpfdossierForRegnumAndKrankheitOptional(registrierungsnummer, krankheit)
				.orElseThrow(() -> AppFailureException.entityNotFound(Impfdossier.class, registrierungsnummer));

		final Registrierung registrierung = impfdossier.getRegistrierung();

		authorizer.checkUpdateAuthorization(registrierung);

		if (ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(impfdossier.getDossierStatus())) {
			// Wir durften hier nicht hinkommen, sondern umbuchenBooster aufrufen
			throw AppValidationMessage.ILLEGAL_STATE.create("Umbuchung von Booster-Terminen muss andere Methode verwenden");
		}

		// UMBUCHEN
		terminbuchungService.umbuchenGrundimmunisierung(impfdossier, Impfslot.toId(impfslot1Id), Impfslot.toId(impfslot2Id));

		return reloadToReturn(impfdossier);
	}

	private DashboardJax reloadToReturn(@NonNull Impfdossier impfdossier) {
		// nach dem Umbuchen ist im Termin N der neue Termin, nun pruefen wir noch ob der User berechtigt ist
		Objects.requireNonNull(impfdossier.getBuchung().getGewuenschterOdi(), "Gewuenschter ODI wird beim buchen gesetzt");
		authorizer.checkBenutzerAssignedToOdi(impfdossier.getBuchung().getGewuenschterOdi());

		ImpfinformationDto impfinformationen = this.impfinformationenService.getImpfinformationen(
			impfdossier.getRegistrierung().getRegistrierungsnummer(),
			impfdossier.getKrankheitIdentifier());
		Fragebogen fragebogen = this.fragebogenService.findFragebogenByRegistrierungsnummer(
			impfdossier.getRegistrierung().getRegistrierungsnummer());
		return new DashboardJax(impfinformationen, fragebogen);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("umbuchenBooster/krankheit/{krankheit}/{registrierungsnummer}/{impfslotNId}")
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
	public DashboardJax umbuchenBooster(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @PathParam("impfslotNId") UUID impfslotNId
	) {
		Objects.requireNonNull(registrierungsnummer);
		Objects.requireNonNull(impfslotNId);

		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				krankheit);
		final Registrierung registrierung = infos.getRegistrierung();

		authorizer.checkUpdateAuthorization(registrierung);

		if (!ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(infos.getImpfdossier().getDossierStatus())) {
			// wir duerften hier nicht hinkommen, sondern umbuchenGrundimmunisierung() aufrufen
			throw AppValidationMessage.ILLEGAL_STATE.create("Umbuchung von Booster-Terminen muss andere Methode verwenden");
		}

		// UMBUCHEN
		terminbuchungService.umbuchenBooster(infos, Impfslot.toId(impfslotNId));

		return reloadToReturn(infos.getImpfdossier());
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/termine/nextfrei/krankheit/{krankheit}/{ortDerImpfungId}/{impffolge}")
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
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
		// it would be possible to use the NextTerminCacheService here
		LocalDateTime nextFreierImpftermin = ortDerImpfungService
			.getNextFreierImpftermin(OrtDerImpfung.toId(ortDerImpfungId), impffolge, nextDate, false, krankheit);
		if (nextFreierImpftermin == null) {
			return null;
		}
		return new NextFreierTerminJax(nextFreierImpftermin);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/termine/frei/krankheit/{krankheit}/{ortDerImpfungId}/{impffolge}")
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
	public List<ImpfslotJax> getFreieImpftermine(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId,
		@NonNull @NotNull @PathParam("impffolge") Impffolge impffolge,
		@NonNull @NotNull @Parameter(description = "Datum zu dem der erste passende Gegenstuecktermin gefunden werden soll")
			NextFreierTerminJax date
	) {
		return ortDerImpfungService
			.getFreieImpftermine(OrtDerImpfung.toId(ortDerImpfungId), impffolge, date.getNextDate().toLocalDate(), krankheit)
			.stream()
			.map(ImpfslotJax::new)
			.collect(Collectors.toList());
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, OI_KONTROLLE })
	@Path("/cancel/krankheit/{krankheit}/{registrierungsnummer}")
	public DashboardJax terminAbsagen(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierungsnummer,
				krankheit);

		authorizer.checkUpdateAuthorization(infos.getRegistrierung());

		dossierService.odiAndTermineAbsagen(infos);

		// Daten neu laden, damit die geaenderten Termine / Status auf dem Dossier angepasst sind
		ImpfinformationDto impfinformationen = this.impfinformationenService.getImpfinformationen(
			infos.getRegistrierung().getRegistrierungsnummer(),
			krankheit);
		Fragebogen fragebogen = this.fragebogenService.findFragebogenByRegistrierungsnummer(
			registrierungsnummer);
		return new DashboardJax(impfinformationen, fragebogen);
	}

	@POST
	@TransactionConfiguration(timeout = 6000000)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@RolesAllowed({ AS_REGISTRATION_OI })
	@Path("/absagen/")
	@Operation(description = "Absagen aller Termine eines bestimmten Datums und Impffolge eines OdI falls zB. aus logistischen Gruenden an einem Tag nicht geimpft werden "
		+ "kann")
	public Response termineAbsagenForOdiAndDatum(
		@Valid @NonNull TermineAbsagenJax absagenJax
	) {
		// TODO Affenpocken: VACME-2340 Wir brauchen auch die krankheit hier da Booster Termin Covid != Booster Termin Affenpocken
		OrtDerImpfung ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(absagenJax.getOdiId()));

		authorizer.checkUpdateAuthorization(ortDerImpfung);

		Impffolge impffolge = absagenJax.getImpffolge();
		LocalDate datum = absagenJax.getDatum();
		final List<ID<Impftermin>> gebuchteTermineIds = impfterminRepo.findGebuchteTermine(ortDerImpfung, impffolge, datum, datum);

		LOG.info("VACME-INFO: Es werden {} Termine abgesagt am {} vom OdI {}",
			gebuchteTermineIds.size(), DateUtil.formatDate(datum, Locale.GERMAN), ortDerImpfung.getName());
		for (ID<Impftermin> terminId : gebuchteTermineIds) {
			terminbuchungService.terminAbsagenForOdiAndDatum(terminId);

		}
		// Geloescht werden nicht nur die gebuchten, sondern ALLE Termine dieses Tages
		terminbuchungService.deleteAllTermineOfOdiAndDatum(ortDerImpfung, impffolge, datum);

		return Response.ok().build();
	}
}
