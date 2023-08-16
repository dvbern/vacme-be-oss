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

import java.security.AccessControlException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.registration.CreateRegistrierungJax;
import ch.dvbern.oss.vacme.jax.registration.DashboardJax;
import ch.dvbern.oss.vacme.jax.registration.ExternGeimpftJax;
import ch.dvbern.oss.vacme.jax.registration.LatLngJax;
import ch.dvbern.oss.vacme.jax.registration.PersonalienJax;
import ch.dvbern.oss.vacme.jax.registration.RegistrierungsCodeJax;
import ch.dvbern.oss.vacme.jax.registration.SelfserviceEditJax;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.ExternesZertifikatService;
import ch.dvbern.oss.vacme.service.FragebogenService;
import ch.dvbern.oss.vacme.service.GeocodeService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.ZertifikatRunnerService;
import ch.dvbern.oss.vacme.service.ZertifikatService;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.CC_AGENT;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.IMPFWILLIGER;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_INITIALREG;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_REGISTRIERUNG))
@Path(VACME_INITIALREG + "/registration/")
public class RegistrierungResource {

	private final RegistrierungService registrierungService;
	private final UserPrincipal userPrincipal;
	private final FragebogenService fragebogenService;
	private final Authorizer authorizer;
	private final ZertifikatService zertifikatService;
	private final ZertifikatRunnerService zertifikatRunnerService;
	private final ImpfinformationenService impfinformationenService;
	private final ExternesZertifikatService externeImpfinfoService;
	private final TransactionManager tm;
	private final GeocodeService geocodeService;
	private final BoosterService boosterService;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("registrieren")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public RegistrierungsCodeJax registrieren(CreateRegistrierungJax createRegistrierungJax) {
		Fragebogen fragebogen = createRegistrierungJax.toEntity();
		final Registrierung registrierung = registrierungService.createRegistrierung(fragebogen);
		return RegistrierungsCodeJax.from(registrierung);
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("erneutsenden/{registrierungsnummer}")
	@RolesAllowed({ CC_AGENT })
	public RegistrierungsCodeJax registrierungErneutSenden(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		final Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		authorizer.checkReadAuthorization(registrierung);

		registrierungService.registrierungErneutSenden(registrierung);
		return RegistrierungsCodeJax.from(registrierung);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("externGeimpft/krankheit/{krankheit}/{registrierungsnummer}/{rollback}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public DashboardJax updateExternGeimpft(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit,
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NotNull @PathParam("rollback") boolean rollback,
		@NonNull @NotNull ExternGeimpftJax externGeimpftJax
	) throws SystemException {
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierungsnummer,
			krankheit);
		authorizer.checkUpdateAuthorization(infos.getImpfdossier());

		externeImpfinfoService.saveExternGeimpftImpfling(infos, externGeimpftJax, rollback);

		if (rollback) {
			// Mit rollback=true kann man ausprobieren, was passieren wuerde, wenn man die Erkrankungen speichern wuerde
			tm.setRollbackOnly();
		}

		// Daten neu laden, da der Impfschutz / Status aufgrund des EZ neu berechnet wurde
		ImpfinformationDto impfinformationen =
			this.impfinformationenService.getImpfinformationen(
				infos.getRegistrierung().getRegistrierungsnummer(),
				krankheit);
		Fragebogen fragebogen = this.fragebogenService.findFragebogenByRegistrierungsnummer(registrierungsnummer);
		return new DashboardJax(impfinformationen, fragebogen);
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("acceptElektronischerImpfausweis/{registrierungId}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response acceptElektronischerImpfausweisWithId(
		@NonNull @NotNull @PathParam("registrierungId") UUID registrierungId
	) {
		Registrierung registrierung = registrierungService.findRegistrierungById(registrierungId)
			.orElseThrow(() -> AppFailureException.entityNotFound(Registrierung.class, registrierungId.toString()));
		authorizer.checkReadAuthorization(registrierung);
		registrierungService.acceptElektronischerImpfausweis(registrierung);

		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/my")
	@RolesAllowed(IMPFWILLIGER)
	public @Nullable RegistrierungsCodeJax my() {
		Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();
		Registrierung registrierung = registrierungService.findRegistrierungByUser(currentBenutzer.getId());
		if (registrierung == null) {
			return null;
		}
		authorizer.checkReadAuthorization(registrierung);
		return RegistrierungsCodeJax.from(registrierung);
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/personalien/{registrierungsId}")
	@RolesAllowed({ CC_AGENT })
	public PersonalienJax getPersonalien(
		@NonNull @NotNull @PathParam("registrierungsId") UUID registrierungId) {
		Registrierung registrierung = registrierungService.findRegistrierungById(registrierungId)
			.orElseThrow(() -> AppFailureException.entityNotFound(Registrierung.class, registrierungId.toString()));

		authorizer.checkReadAuthorization(registrierung);
		boolean hasCovidZertifikat = zertifikatService.hasCovidZertifikat(registrierung.getRegistrierungsnummer());
		boolean deservesZertifikat = false;
		LocalDateTime timestampLetzterPostversand = null;
		boolean abgeschlossenMitVollstaendigemImpfschutzCovid = false;

		// TODO Affenpocken: Wir wollen hier rausfinden wie es ums covid cert steht, daher wird nur covid gefragt
		@NonNull Optional<ImpfinformationDto> infosOptional = impfinformationenService.getImpfinformationenOptional(
			registrierung.getRegistrierungsnummer(),
			KrankheitIdentifier.COVID);
		if (infosOptional.isPresent()) {
			final ImpfinformationDto infos = infosOptional.get();
			authorizer.checkReadAuthorization(infos.getImpfdossier());
			deservesZertifikat = ImpfinformationenService.deservesZertifikatForAnyImpfung(infos);
			timestampLetzterPostversand =
				zertifikatRunnerService.getTimestampOfLastPostversand(infos.getImpfdossier());
			abgeschlossenMitVollstaendigemImpfschutzCovid =
				infos.getImpfdossier().abgeschlossenMitVollstaendigemImpfschutz();
		}
		return new PersonalienJax(
			registrierung,
			hasCovidZertifikat,
			deservesZertifikat,
			timestampLetzterPostversand,
			abgeschlossenMitVollstaendigemImpfschutzCovid);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/personalien/{registrierungsId}")
	@RolesAllowed({ IMPFWILLIGER, CC_AGENT })
	public Response updatePersonalien(
		@NonNull @NotNull @PathParam("registrierungsId") UUID registrierungId,
		PersonalienJax personalienJax
	) {
		Registrierung registrierung = registrierungService.findRegistrierungById(registrierungId)
			.orElseThrow(() -> AppFailureException.entityNotFound(Registrierung.class, registrierungId.toString()));
		Fragebogen fragebogen =
			fragebogenService.findFragebogenByRegistrierungsnummer(registrierung.getRegistrierungsnummer());
		authorizer.checkUpdateAuthorization(registrierung);

		registrierungService.updatePersonalien(fragebogen, personalienJax.getAdresse());
		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/selfserviceData")
	@RolesAllowed({ IMPFWILLIGER })
	@Nullable
	public SelfserviceEditJax getSelfserviceEditJax() {
		Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();
		Registrierung registrierung = registrierungService.findRegistrierungByUser(currentBenutzer.getId());
		if (registrierung == null) {
			return null;
		}
		authorizer.checkReadAuthorization(registrierung);
		return getSelfserviceEditJaxBasic(registrierung);
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/selfserviceData/{registrierungsnummer}")
	@RolesAllowed({ CC_AGENT })
	@Nullable
	public SelfserviceEditJax getSelfserviceEditJaxCC(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer
	) {
		Registrierung registrierung;
		try {
			registrierung = registrierungService.findRegistrierung(registrierungsnummer);
			authorizer.checkReadAuthorization(registrierung);
		} catch (AppValidationException | AccessControlException e) {
			return null; // sowohl fuer UNKNOWN_REGISTRIERUNGSNUMMER als auch fuer keine Berechtigung: sonst kann man
			// gueltige Nummern herausfinden
		}

		return getSelfserviceEditJaxBasic(registrierung);
	}

	private SelfserviceEditJax getSelfserviceEditJaxBasic(Registrierung registrierung) {
		Fragebogen fragebogen = fragebogenService.findFragebogenByRegistrierungsnummer(
			registrierung.getRegistrierungsnummer());
		return new SelfserviceEditJax(registrierung, fragebogen);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/selfserviceData")
	@RolesAllowed({ IMPFWILLIGER })
	public Response updateSelfserviceEditData(SelfserviceEditJax personalienJax) {
		Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();
		Registrierung registrierung = registrierungService.findRegistrierungByUser(currentBenutzer.getId());
		if (registrierung == null) {
			LOG.warn("Fuer update selfserviceData muss die Registrierung existieren");
			return Response.noContent().build();
		}
		authorizer.checkReadAuthorization(registrierung);
		return updateSelfserviceEditDataBasic(registrierung, personalienJax);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/selfserviceData/{registrierungsnummer}")
	@RolesAllowed({ CC_AGENT })
	public Response updateSelfserviceEditDataCC(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		SelfserviceEditJax personalienJax) {
		Registrierung registrierung;
		try {
			registrierung = registrierungService.findRegistrierung(registrierungsnummer);
			authorizer.checkUpdateAuthorization(registrierung);
		} catch (AppValidationException | AccessControlException e) {
			return Response.status(Response.Status.NOT_FOUND)
				.build(); // sowohl fuer UNKNOWN_REGISTRIERUNGSNUMMER als auch fuer keine Berechtigung: sonst
			// kann man gueltige Nummern herausfinden
		}
		return updateSelfserviceEditDataBasic(registrierung, personalienJax);
	}

	@GET
	@Path("/geocodeAddress/{registrierungsnummer}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ CC_AGENT, IMPFWILLIGER })
	@Nullable
	public LatLngJax getGeocodedAddress(
		@NonNull @NotNull @PathParam("registrierungsnummer")
		String registrierungsnummer) {
		Registrierung registrierung;
		try {
			registrierung = registrierungService.findRegistrierung(registrierungsnummer);
			authorizer.checkReadAuthorization(registrierung);
		} catch (AppValidationException | AccessControlException e) {
			return null;
			// sowohl fuer UNKNOWN_REGISTRIERUNGSNUMMER als auch fuer keine Berechtigung: sonst
			// kann man gueltige Nummern herausfinden
		}
		LatLngJax geocode = geocodeService.geocodeAdresse(registrierung.getAdresse());

		return geocode;
	}

	private Response updateSelfserviceEditDataBasic(
		@NonNull Registrierung registrierung,
		@NonNull SelfserviceEditJax personalienJax) {
		Fragebogen fragebogen =
			fragebogenService.findFragebogenByRegistrierungsnummer(registrierung.getRegistrierungsnummer());
		authorizer.checkUpdateAuthorization(registrierung);

		// todo Affenpocken, Hier wird scheinbar aktuell nicht nach VMDL geschickt bei anpassungen an Lebensumstaenden. Bug? Muessten wir das nicht noch machen
		registrierungService.updateSelfserviceData(fragebogen, registrierung, personalienJax);
		// Fuer alle Krankheiten neu berechnen
		boosterService.recalculateImpfschutzAndStatusmovesForSingleRegWithReload(registrierung);

		return Response.ok().build();
	}
}
