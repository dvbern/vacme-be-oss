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

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.jax.umfrage.CreateUmfrageJax;
import ch.dvbern.oss.vacme.jax.umfrage.SendUmfrageJax;
import ch.dvbern.oss.vacme.service.UmfrageService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_UMFRAGE))
@Path(VACME_WEB + "/umfrage")
public class UmfrageResource {

	private final UmfrageService umfrageService;

	public UmfrageResource(
		UmfrageService umfrageService
	) {
		this.umfrageService = umfrageService;
	}

	@TransactionConfiguration(timeout = 6000000)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")),
		description = "OK",
		responseCode = "200")
	@Path("create")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	public Response createUmfrage(
		@NotNull @NonNull CreateUmfrageJax jax
	) {
		try {
			StreamingOutput streamingOutput =
				umfrageService.createUmfrageAndDownloadCsv(jax.getGruppe(), jax.getLimit());
			LOG.info("VACME-UMFRAGE: Umfrage und CSV erstellt fuer Gruppe {}", jax.getGruppe());
			return Response.ok(streamingOutput).build();
		} catch (Exception e) {
			LOG.error("VACME-UMFRAGE: Umfrage und CSV konnten nicht erstellt werden", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = 6000000)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("send")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	public Response sendUmfrage(
		@NotNull @NonNull SendUmfrageJax jax
	) {
		umfrageService.sendUmfrage(jax.getGruppe());
		return Response.ok().build();
	}

	@TransactionConfiguration(timeout = 6000000)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("sendReminder")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	public Response sendReminder(
		@NotNull @NonNull SendUmfrageJax jax
	) {
		umfrageService.sendUmfrageReminder(jax.getGruppe());
		return Response.ok().build();
	}
}
