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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.jax.registration.ZertifizierungsTokenJax;
import ch.dvbern.oss.vacme.service.ZertifikatService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_IMPFDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_MEDIZINISCHE_NACHDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_NACHDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_ZERTIFIKAT_AUSSTELLER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_DOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_ZERTIFIKAT))
@Path(VACME_WEB + "/zertifikat/")
public class ZertifikatResource {

	private final ZertifikatService zertifikatService;

	@Inject
	public ZertifikatResource(
		@NonNull ZertifikatService zertifikatService
	) {
		this.zertifikatService = zertifikatService;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/token")
	@RolesAllowed(KT_ZERTIFIKAT_AUSSTELLER)
	public Response tokenErfassen(
		@NonNull @NotNull @Valid ZertifizierungsTokenJax zertifizierungsTokenJax
	) {
		zertifikatService.createToken(zertifizierungsTokenJax);
		return Response.ok().build();
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/tokens")
	@RolesAllowed(KT_ZERTIFIKAT_AUSSTELLER)
	public Response clearAllCovidCertTokens(
	) {
		zertifikatService.clearAllCovidCertTokens();
		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/hasValidToken")
	@RolesAllowed({KT_ZERTIFIKAT_AUSSTELLER, OI_KONTROLLE, OI_DOKUMENTATION,
		KT_NACHDOKUMENTATION, KT_MEDIZINISCHE_NACHDOKUMENTATION, KT_IMPFDOKUMENTATION})
	public boolean hasValidToken() {
		return zertifikatService.hasValidToken();
	}
}
