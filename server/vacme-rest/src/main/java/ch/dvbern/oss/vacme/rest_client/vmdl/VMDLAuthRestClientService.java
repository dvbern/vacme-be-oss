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

import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.rest_client.RestClientLoggingFilter;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLAuthResponseJax;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path("/")
@RegisterRestClient(configKey="vmdl-auth-api")
@RegisterProvider(RestClientLoggingFilter.class)
@ClientHeaderParam(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_FORM_URLENCODED)
@RolesAllowed(BenutzerRolleName.SYSTEM_INTERNAL_ADMIN)
public interface VMDLAuthRestClientService {

    @POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{tenant_id}/oauth2/v2.0/token")
	@RolesAllowed(BenutzerRolleName.SYSTEM_INTERNAL_ADMIN)
    VMDLAuthResponseJax aquireToken(@NonNull @NotNull @PathParam("tenant_id") String tenantId,
                                    @NonNull @NotNull @FormParam("grant_type") String grantType,
                                    @NonNull @NotNull @FormParam("username") String username,
                                    @NonNull @NotNull @FormParam("client_id") String clientID,
                                    @NonNull @NotNull @FormParam("password") String password,
                                    @NonNull @NotNull @FormParam("scope") String scope);
}
