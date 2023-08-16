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

package ch.dvbern.oss.vacme.rest.auth;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

/**
 * Ths filter is used to restrict logins through the vacme-impftermin-client to users that have certain roles
 */
@Provider
@PreMatching
public class ImpfterminClientSecurityFilter implements ContainerRequestFilter {

	public static final String VACME_IMPFTERMIN_CLIENT = "vacme-impftermin-client";

	@Inject
	JsonWebToken jsonWebToken;

	@Inject
	SecurityIdentity securityIdentity;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if (isTokenForImpfterminClient()) {
			if (userHasImpfterminclientRealmRole()) {
				return; // user is allowed to continue since he has the role that allows usage of the client
			}
			String msg = String.format("User is not authorized for client %s", VACME_IMPFTERMIN_CLIENT);
			requestContext.abortWith(Response.status(FORBIDDEN).entity(msg).build());
		}
	}

	private boolean userHasImpfterminclientRealmRole() {
		return securityIdentity.getRoles().contains(BenutzerRolleName.IMPFTERMINCLIENT);
	}

	private boolean isTokenForImpfterminClient() {
		if (jsonWebToken != null) {
			String azp = jsonWebToken.getClaim("azp");
			return VACME_IMPFTERMIN_CLIENT.equals(azp);
		}
		return false;
	}
}
