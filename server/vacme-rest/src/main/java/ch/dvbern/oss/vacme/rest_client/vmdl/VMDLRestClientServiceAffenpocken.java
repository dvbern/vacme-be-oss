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

package ch.dvbern.oss.vacme.rest_client.vmdl;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLDeleteJax;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadAffenpockenJax;
import ch.dvbern.oss.vacme.rest_client.RestClientLoggingFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/monkeypox/v1")
@RegisterRestClient(configKey="vmdl-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterClientHeaders(VMDLRequestAuthTokenAffenpockenFactory.class)
@RolesAllowed(BenutzerRolleName.SYSTEM_INTERNAL_ADMIN)
public interface VMDLRestClientServiceAffenpocken {

    @POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/vaccinationData")
	@RolesAllowed(BenutzerRolleName.SYSTEM_INTERNAL_ADMIN)
    void uploadData(@NonNull @NotNull @Parameter(description = "Data to upload to VMDL Interface")
						List<VMDLUploadAffenpockenJax> vmdlUploadJax);

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/vaccinationData")
	@RolesAllowed(BenutzerRolleName.SYSTEM_INTERNAL_ADMIN)
	void deleteData(@NonNull @NotNull @Parameter(description = "Data to be deleted in VMDL Interface")
						List<VMDLDeleteJax> data);
}
