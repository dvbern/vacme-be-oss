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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.jax.registration.OdiUserJax;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.BenutzerService;
import ch.dvbern.oss.vacme.service.KeyCloakService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.keycloak.representations.idm.UserRepresentation;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_REGISTRATION_OI;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_BENUTZER_VERWALTER;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_ODIBENUTZER))
@Path(VACME_WEB + "/odibenutzer")
public class OdiBenutzerResource {

	private final KeyCloakService keyCloakService;
	private final Authorizer authorizer;
	private final BenutzerService benutzerService;

	@Inject
	public OdiBenutzerResource(
		@NonNull KeyCloakService keyCloakService,
		@NonNull Authorizer authorizer,
		@NonNull BenutzerService benutzerService
	) {
		this.keyCloakService = keyCloakService;
		this.authorizer = authorizer;
		this.benutzerService = benutzerService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/create/group/{groupname}")
	@RolesAllowed({ OI_BENUTZER_VERWALTER, AS_REGISTRATION_OI })
	@Operation(description = "erstellt einen Benutzer fuer einen bestimmten OdI")
	public OdiUserJax createUserForGroup(
		@NonNull @NotNull @PathParam("groupname") String groupname,
		@NonNull @NotNull @Valid OdiUserJax userJax
	) {
		this.authorizer.checkAllowedByOdiIdentifier(groupname);
		this.authorizer.checkAllowedToGiveRole(userJax, keyCloakService);

		final OdiUserJax user = keyCloakService.createOrUpdateUser(userJax, groupname);
		return user;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("leavegroup/id/{id}/group/{groupname}")
	@RolesAllowed({ OI_BENUTZER_VERWALTER, AS_REGISTRATION_OI })
	@Operation(description = "Entfernt den Benutzer mit der id aus der gruppe/dem odi mit identifier groupname")
	public Response leavegroup(
		@NonNull @NotNull @PathParam("id") String id,
		@NonNull @NotNull @PathParam("groupname") String groupname
	) {
		this.authorizer.checkAllowedByOdiIdentifier(groupname);
		keyCloakService.leaveGroup(id, groupname);
		benutzerService.removeOdiFromBenutzerIfExists(Benutzer.toId(UUID.fromString(id)), groupname);
		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("/toggleEnable/id/{id}/group/{groupname}")
	@RolesAllowed({ OI_BENUTZER_VERWALTER, AS_REGISTRATION_OI })
	@Operation(description = "Toggled das Enabled flag des Benutzers mit der id")
	public Response toggleEnabled(
		@NonNull @NotNull @PathParam("id") String id,
		@NonNull @NotNull @PathParam("groupname") String groupname) {
		this.authorizer.checkAllowedByOdiIdentifier(groupname);
		UserRepresentation kcUserRep = keyCloakService.toggleEnabled(id);

		Optional<Benutzer> benutzerOpt = benutzerService.getById(Benutzer.toId(UUID.fromString(id)));
		benutzerOpt.ifPresent(benutzer -> {
			benutzer.setDeaktiviert(!kcUserRep.isEnabled());
			if (kcUserRep.isEnabled()) {
				benutzer.setTimestampLastUnlocked(LocalDateTime.now());
			}
		});

		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/group/{groupname}")
	@RolesAllowed({ OI_BENUTZER_VERWALTER, AS_REGISTRATION_OI })
	@Operation(description = "Liest die Benutzer dieser Gruppe aus dem IAM System")
	public List<OdiUserJax> getUsersFromGroup(
		@NonNull @NotNull @PathParam("groupname") String groupname,
		@NotNull @QueryParam("first") int first,
		@NotNull @QueryParam("max") int max
	) {
		this.authorizer.checkAllowedByOdiIdentifier(groupname);
		final List<OdiUserJax> userFromGroup = keyCloakService.getUsersInGroup(groupname, true, first, max);
		return userFromGroup;
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/group/{groupname}/user/{username}")
	@RolesAllowed({ OI_BENUTZER_VERWALTER, AS_REGISTRATION_OI })
	@Operation(description = "Liest den Benutzer der Gruppe mit dem gegegbenen Username aus dem IAM System")
	public OdiUserJax getUserFromGroup(
		@NonNull @NotNull @PathParam("groupname") String groupname,
		@NonNull @NotNull @PathParam("username") String username
	) {
		this.authorizer.checkAllowedByOdiIdentifier(groupname);
		final OdiUserJax user = keyCloakService.getUserByGroupAndUsername(username, groupname);

		return user;
	}
}

