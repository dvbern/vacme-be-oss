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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
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

import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.FachRolle;
import ch.dvbern.oss.vacme.jax.OrtDerImpfungDisplayNameJax;
import ch.dvbern.oss.vacme.jax.registration.OdiExistenceJax;
import ch.dvbern.oss.vacme.jax.registration.OdiFilterJax;
import ch.dvbern.oss.vacme.jax.registration.OdiUserDisplayNameJax;
import ch.dvbern.oss.vacme.jax.registration.OrtDerImpfungJax;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.GeocodeService;
import ch.dvbern.oss.vacme.service.KeyCloakService;
import ch.dvbern.oss.vacme.service.OdiFilterService;
import ch.dvbern.oss.vacme.service.OrtDerImpfungService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_REGISTRATION_OI;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_IMPFDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_MEDIZINISCHE_NACHDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_NACHDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_BENUTZER_REPORTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_BENUTZER_VERWALTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_DOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_LOGISTIK_REPORTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_MEDIZINISCHER_REPORTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_ORT_VERWALTER;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_ORT_DER_IMPFUNG))
@Path(VACME_WEB + "/ortderimpfung/")
public class OrtDerImpfungResource {

	private final Authorizer authorizer;
	private final OrtDerImpfungRepo ortDerImpfungRepo;
	private final OrtDerImpfungService ortDerImpfungService;
	private final KeyCloakService keyCloakService;
	private final OdiFilterService odiFilterService;
	private final GeocodeService geocodeService;

	@Inject
	public OrtDerImpfungResource(
		Authorizer authorizer,
		OrtDerImpfungRepo ortDerImpfungRepo,
		OrtDerImpfungService ortDerImpfungService,
		KeyCloakService keyCloakService,
		OdiFilterService odiFilterService,
		GeocodeService geocodeService) {
		this.authorizer = authorizer;
		this.ortDerImpfungRepo = ortDerImpfungRepo;
		this.ortDerImpfungService = ortDerImpfungService;
		this.keyCloakService = keyCloakService;
		this.odiFilterService = odiFilterService;
		this.geocodeService = geocodeService;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/management/erfassen")
	@RolesAllowed(AS_REGISTRATION_OI)
	public OrtDerImpfungJax erfassen(
		@NonNull @NotNull @Valid OrtDerImpfungJax ortDerImpfungJax
	) {
		OrtDerImpfung ortDerImpfung = ortDerImpfungService.ortDerImpfungErfassen(ortDerImpfungJax);
		return new OrtDerImpfungJax(ortDerImpfung);
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/management/get/{ortDerImpfungId}")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	public OrtDerImpfungJax getOrtDerImpfung(
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId
	) {
		OrtDerImpfung ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(ortDerImpfungId));

		authorizer.checkReadAuthorization(ortDerImpfung);

		return new OrtDerImpfungJax(ortDerImpfung);
	}

	// ACHTUNG: Diese Resource wird auch vom WebShop aufgerufen, Anpassungen muessen mit Reber Informatik abgesprochen werden
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/management/all")
	@RolesAllowed({
		AS_REGISTRATION_OI,
		OI_ORT_VERWALTER,
		OI_KONTROLLE,
		OI_DOKUMENTATION,
		OI_BENUTZER_VERWALTER,
		OI_BENUTZER_REPORTER,
		OI_IMPFVERANTWORTUNG,
		OI_LOGISTIK_REPORTER,
		OI_MEDIZINISCHER_REPORTER,
		KT_NACHDOKUMENTATION,
		KT_MEDIZINISCHE_NACHDOKUMENTATION,
		KT_IMPFDOKUMENTATION,
	})
	public List<OrtDerImpfungJax> getAllOrtDerImpfungJax() {
		return ortDerImpfungService.findAllForCurrentBenutzer()
			.stream()
			.map(OrtDerImpfungJax::new)
			.collect(Collectors.toList());
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/myodis")
	@RolesAllowed({
		AS_REGISTRATION_OI,
		OI_ORT_VERWALTER,
		OI_KONTROLLE,
		OI_DOKUMENTATION,
		OI_BENUTZER_VERWALTER,
		OI_BENUTZER_REPORTER,
		OI_IMPFVERANTWORTUNG,
		OI_LOGISTIK_REPORTER,
		OI_MEDIZINISCHER_REPORTER
	})
	public List<OrtDerImpfungDisplayNameJax> getOrteDerImpfungForCurrentBenutzer() {
		return ortDerImpfungService.findAllForCurrentBenutzer()
			.stream()
			.map(OrtDerImpfungDisplayNameJax::new)
			.collect(Collectors.toList());
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("/management/aktualisieren")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	public Response aktualisieren(
		@NonNull @NotNull @Valid OrtDerImpfungJax ortDerImpfungJax
	) {
		OrtDerImpfung ortDerImpfung = ortDerImpfungRepo
			.getById(OrtDerImpfung.toId(ortDerImpfungJax.getId()))
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_ODI.create(ortDerImpfungJax.getId()));
		authorizer.checkUpdateAuthorization(ortDerImpfung);

		ortDerImpfungService.updateOrtDerImpfung(ortDerImpfung, ortDerImpfungJax);
		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/management/check/{ortDerImpfungIdentifier}")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	public OdiExistenceJax checkOrtDerImpfungExists(
		@NonNull @NotNull @PathParam("ortDerImpfungIdentifier") String ortDerImpfungIdentifier
	) {

		boolean existence = keyCloakService.checkGroupExists(ortDerImpfungIdentifier);
		Optional<OrtDerImpfung> byOdiIdentifier = this.ortDerImpfungRepo.getByOdiIdentifier(ortDerImpfungIdentifier);

		//check consistency
		if (existence && byOdiIdentifier.isEmpty()) {
			LOG.info("ODI existiert in KC aber nicht bei uns. Dies ist der erwartete Fall");
		} else if (!existence && byOdiIdentifier.isPresent()) {
			LOG.error("Der OdI exitiert in unserer DB aber nicht in KC. Dies ist ein Fehler");
			throw AppValidationMessage.KC_GROUP_DOES_NOT_EXIST.create(ortDerImpfungIdentifier);
		} else if (existence) {
			LOG.error("Der OdI existiert bei uns und in KC -> Fehler kann nicht nochmal erfassen");
			throw AppValidationMessage.ODI_DOES_ALREADY_EXIST.create(ortDerImpfungIdentifier);
		} else{
			LOG.error("Der OdI existiert weder bei uns noch in KC");
			throw AppValidationMessage.KC_GROUP_DOES_NOT_EXIST.create(ortDerImpfungIdentifier);
		}

		return new OdiExistenceJax(ortDerImpfungIdentifier, existence);

	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/management/fachverantwortungbab")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	@Operation(description = "Liest alle Benutzer aus IAM die in der Rolle FachverwantwortungBAB sind")
	public List<OdiUserDisplayNameJax> getAllFachverantwortungbab() {
		return keyCloakService.getUserInRole(FachRolle.FACHVERANTWORTUNG_BAB);
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/management/organisationsverantwortung")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	@Operation(description = "Liest alle Benutzer aus IAM die in der Rolle ORGANISATIONSVERANTWORTUNG sind")
	public List<OdiUserDisplayNameJax> getAllOrganisationsverantwortung() {
		return keyCloakService.getUserInRole(FachRolle.ORGANISATIONSVERANTWORTUNG);
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/odifilter/{ortDerImpfungId}")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	@Operation(description = "Liest alle Filters vom Ort der Impfung")
	public List<OdiFilterJax> getFilters(
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId
	) {
		OrtDerImpfung ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(ortDerImpfungId));
		authorizer.checkReadAuthorization(ortDerImpfung);

		return ortDerImpfung.getFilters().stream()
			.map(OdiFilterJax::new)
			.collect(Collectors.toList());
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/odifilter/{ortDerImpfungId}")
	@RolesAllowed({ AS_REGISTRATION_OI })
	public Response updateFilters(
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId,
		@NonNull @NotNull List<OdiFilterJax> filters
	) {
		OrtDerImpfung ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(ortDerImpfungId));
		authorizer.checkUpdateAuthorization(ortDerImpfung);

		this.odiFilterService.updateFilters(ortDerImpfung, filters);
		this.odiFilterService.removeOrphaned();

		return Response.ok().build();
	}
}
