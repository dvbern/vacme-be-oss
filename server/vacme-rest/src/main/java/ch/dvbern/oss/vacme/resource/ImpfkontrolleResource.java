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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.registration.BeruflicheTaetigkeit;
import ch.dvbern.oss.vacme.entities.registration.ChronischeKrankheiten;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Geschlecht;
import ch.dvbern.oss.vacme.entities.registration.Lebensumstaende;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.registration.ImpfkontrolleJax;
import ch.dvbern.oss.vacme.jax.registration.RegistrierungsCodeJax;
import ch.dvbern.oss.vacme.jax.registration.ZweiteImpfungVerzichtenJax;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.FragebogenService;
import ch.dvbern.oss.vacme.service.ImpfkontrolleService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_KONTROLLE))
@Path(VACME_WEB + "/kontrolle")
public class ImpfkontrolleResource {

	private final ImpfkontrolleService impfkontrolleService;
	private final FragebogenService fragebogenService;
	private final Authorizer authorizer;
	private final ImpfinformationenService impfinformationenService;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("registrieren")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public ImpfkontrolleJax registrieren(ImpfkontrolleJax impfkontrolleJax) {
		Fragebogen fragebogen = impfkontrolleJax.toFragebogenForRegistrierung();
		throwIfContainsUnbekannt(fragebogen);
		final Registrierung created = impfkontrolleService.createRegistrierung(fragebogen, impfkontrolleJax);

		ImpfkontrolleJax impfkontrolleJaxReloaded = find(
			created.getRegistrierungsnummer(),
			impfkontrolleJax.getKrankheitIdentifier());
		return impfkontrolleJaxReloaded;
	}

	@POST
	@Operation(summary = "Service der aufgerufen wird wenn eine Person auf die 2. Impfung verzichtet")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("verzichtenZweiteImpfung")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public Response verzichtenZweiteImpfung(
		@NonNull ZweiteImpfungVerzichtenJax zweiteImpfungVerzichtenJax
	) {
		final KrankheitIdentifier krankheitIdentifier = zweiteImpfungVerzichtenJax.getKrankheitIdentifier();
		if (!krankheitIdentifier.isSupportsZweiteImpfungVerzichten()) {
			throw AppValidationMessage.NOT_ALLOWED_FOR_KRANKHEIT.create("verzichtenZweiteImpfung", krankheitIdentifier);
		}
		Fragebogen fragebogen = fragebogenService.findFragebogenByRegistrierungsnummer(
			zweiteImpfungVerzichtenJax.getRegistrierungsnummer());
		final Registrierung registrierung = fragebogen.getRegistrierung();
		authorizer.checkReadAuthorization(registrierung);
		// Fuer nicht FachBABs muss geprueft werden, dass die Person in mindestens 1 noch aktiven ODI zustaendig ist
		if (!authorizer.isUserFachBABOrKanton()) {
			authorizer.checkBenutzerAssignedToAtLeastOneActiveOdi();
		}
		final ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				registrierung.getRegistrierungsnummer(),
				krankheitIdentifier);
		impfkontrolleService.zweiteImpfungVerzichten(
			infos,
			zweiteImpfungVerzichtenJax.isVollstaendigerImpfschutz(),
			zweiteImpfungVerzichtenJax.getBegruendung(),
			zweiteImpfungVerzichtenJax.getPositivGetestetDatum());
		return Response.ok().build();
	}

	@POST
	@Operation(summary = "Service der aufgerufen wird wenn nach einem initialen Verzicht auf die 2. Impfung diese doch "
		+ "noch wahrgenommen werden soll")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("wahrnehmenZweiteImpfung/{registrierungsnummer}/krankheit/{krankheit}")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public Response wahrnehmenZweiteImpfung(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit
	) {
		if (!krankheit.isSupportsZweiteImpfungVerzichten()) {
			throw AppValidationMessage.NOT_ALLOWED_FOR_KRANKHEIT.create("wahrnehmenZweiteImpfung", krankheit);
		}
		final ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			krankheit);
		authorizer.checkReadAuthorization(infos.getRegistrierung());
		impfkontrolleService.zweiteImpfungWahrnehmen(infos);
		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/find/{registrierungsnummer}/krankheit/{krankheit}")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public ImpfkontrolleJax find(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit
	) {
		Fragebogen fragebogen = fragebogenService.findFragebogenByRegistrierungsnummer(
			registrierungsnummer);
		final Registrierung registrierung = fragebogen.getRegistrierung();
		authorizer.checkReadAuthorization(registrierung);
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(
				fragebogen.getRegistrierung().getRegistrierungsnummer(),
				krankheit);
		Integer kontrolleNr = ImpfinformationenService.getCurrentKontrolleNr(infos);
		ImpfungkontrolleTermin kontrolleTerminOrNull = ImpfinformationenService.getCurrentKontrolleTerminOrNull(infos);
		Optional<Impfdossiereintrag> impfdossiereintragOpt = ImpfinformationenService.getPendingDossiereintrag(infos);
		Impftermin currentTerminBoosterOrNull = ImpfinformationenService.getPendingBoosterTermin(infos).orElse(null);

		return ImpfkontrolleJax.from(
			krankheit,
			kontrolleNr,
			fragebogen,
			kontrolleTerminOrNull,
			infos,
			currentTerminBoosterOrNull,
			impfdossiereintragOpt.orElse(null)
		);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("kontrolleOk/{impffolge}")
	@RolesAllowed({ OI_KONTROLLE })
	public ImpfkontrolleJax kontrolleOk(
		@NonNull @PathParam("impffolge") String impffolgeParam,
		@NonNull ImpfkontrolleJax impfkontrolleJax
	) {
		Impffolge impffolge = Impffolge.valueOf(impffolgeParam);
		Objects.requireNonNull(impfkontrolleJax.getRegistrierungsnummer());
		Fragebogen fragebogen = fragebogenService.findFragebogenByRegistrierungsnummer(
			impfkontrolleJax.getRegistrierungsnummer());

		authorizer.checkReadAuthorization(fragebogen.getRegistrierung());

		Objects.requireNonNull(impfkontrolleJax.getExternGeimpft());
		impfkontrolleService.kontrolleOk(
			fragebogen, impffolge, null, impfkontrolleJax, impfkontrolleJax.getExternGeimpft());

		ImpfkontrolleJax impfkontrolleJaxReloaded = find(
			fragebogen.getRegistrierung().getRegistrierungsnummer(),
			impfkontrolleJax.getKrankheitIdentifier());
		return impfkontrolleJaxReloaded;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("saveKontrolleNoProceed/{impffolge}")
	@RolesAllowed({ OI_KONTROLLE, OI_IMPFVERANTWORTUNG })
	public RegistrierungsCodeJax saveKontrolleNoProceed(
		@NonNull @PathParam("impffolge") String impffolgeParam,
		@NonNull ImpfkontrolleJax impfkontrolleJax
	) {
		Impffolge impffolge = Impffolge.valueOf(impffolgeParam);
		Objects.requireNonNull(impfkontrolleJax.getRegistrierungsnummer());
		Fragebogen fragebogen = fragebogenService.findFragebogenByRegistrierungsnummer(
			impfkontrolleJax.getRegistrierungsnummer());

		authorizer.checkReadAuthorization(fragebogen.getRegistrierung());

		// apply changes fromImpfkontrolleTerminJax
		impfkontrolleService.saveKontrolleNoProceed(fragebogen, impfkontrolleJax, impffolge);

		return RegistrierungsCodeJax.from(fragebogen.getRegistrierung());
	}

	private void throwIfContainsUnbekannt(@NonNull Fragebogen fragebogen) {
		boolean hasUnbekannt = fragebogen.getRegistrierung().getGeschlecht() == Geschlecht.UNBEKANNT;
		hasUnbekannt = hasUnbekannt || fragebogen.getBeruflicheTaetigkeit() == BeruflicheTaetigkeit.UNBEKANNT;
		hasUnbekannt = hasUnbekannt || fragebogen.getLebensumstaende() == Lebensumstaende.UNBEKANNT;
		hasUnbekannt = hasUnbekannt || fragebogen.getChronischeKrankheiten() == ChronischeKrankheiten.UNBEKANNT;

		if (hasUnbekannt) {
			throw new AppFailureException("Cannot choose UNBEKANNT");
		}
	}
}

