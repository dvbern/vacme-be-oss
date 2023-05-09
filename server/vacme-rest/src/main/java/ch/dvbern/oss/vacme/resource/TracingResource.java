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
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.tracing.TracingResponseJax;
import ch.dvbern.oss.vacme.service.TracingService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.TRACING;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_TRACING))
@Path(VACME_WEB + "/tracing")
public class TracingResource {

	private final TracingService tracingService;

	@Inject
	public TracingResource(TracingService tracingService){
		this.tracingService = tracingService;
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/findByImpfcode/{impfcode}")
	@RolesAllowed(TRACING)
	@Operation(hidden = true)
	public TracingResponseJax findByRegistrierungsnummer (
		@NonNull @NotNull @Valid @PathParam("impfcode") String registrierungsnummer
	) {
		// TODO Affenpocken: Tracing aktuell nur fuer Covid
		return tracingService.findByRegistrierungsnummer(KrankheitIdentifier.COVID, registrierungsnummer);
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/findByCertificatUVCI/{uvci}")
	@RolesAllowed(TRACING)
	@Operation(hidden = true)
	public TracingResponseJax findByCertificatUVCI (
		@NonNull @NotNull @Valid @PathParam("uvci") String uvci
	) {
		// TODO Affenpocken: Tracing aktuell nur fuer Covid
		return tracingService.findByCertificatUVCI(KrankheitIdentifier.COVID, uvci);
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/findByKrankenkassennummer/{krankenkassennummer}")
	@RolesAllowed(TRACING)
	@Operation(hidden = true)
	public TracingResponseJax findByKrankenkassennummer (
		@NonNull @NotNull @Valid @PathParam("krankenkassennummer") String krankenkassennummer
	) {
		// TODO Affenpocken: Tracing aktuell nur fuer Covid
		return tracingService.findByKrankenkassennummer(KrankheitIdentifier.COVID, krankenkassennummer);
	}
}
