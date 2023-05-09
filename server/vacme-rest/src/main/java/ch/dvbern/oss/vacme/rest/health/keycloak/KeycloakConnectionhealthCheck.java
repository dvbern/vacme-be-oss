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

package ch.dvbern.oss.vacme.rest.health.keycloak;

import java.util.List;

import javax.ws.rs.NotAuthorizedException;

import ch.dvbern.oss.vacme.keyclaok.KeycloakAdapter;
import io.quarkus.security.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.keycloak.representations.idm.RoleRepresentation;

@Slf4j
public class KeycloakConnectionhealthCheck {


	protected HealthCheckResponse getHealthCheckResponse(String healthcheckName, KeycloakAdapter adapterToCheck, String keycloakUsername,
		String keycloakClientId) {

		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named(healthcheckName);

		try {
			StopWatch started = StopWatch.createStarted();
			//just read a role to check if connection works
			List<RoleRepresentation> someRole =
				adapterToCheck.getClient().roles().list(0,1);
			if (someRole == null || someRole.isEmpty()) {
				responseBuilder.down().withData("no roles found", keycloakUsername);

			}
			started.stop();
			responseBuilder.up().withData("duration_ms", started.getTime());

		} catch (Exception e) {
			// cannot access the database
			if (e.getCause() != null && e.getCause().getClass().equals(NotAuthorizedException.class)) {
				LOG.error("Could not establish M2M connection for client '{}' and user '{}'. Got NotAuthorized (401) "
					+ "Maybe the client-secret or the username of the technical user is wrong", keycloakClientId, keycloakUsername, e);
			}
			else if (e.getCause() != null && e.getCause().getClass().equals(ForbiddenException.class)) {
				LOG.error("Could not read user through keycloak admin client from quarkus.  Got ForbiddenException (403) for client "
					+ "'{}' and user '{}'."
					+ "Maybe the client-secret or the username of the technical user is wrong", keycloakClientId, keycloakUsername, e);
			} else{
				LOG.error("Could not establish M2M connection for client '{}' and user '{}'", keycloakClientId, keycloakUsername, e);
			}
			responseBuilder.down()
				.withData("client id", keycloakClientId)
				.withData("client user", keycloakUsername);
		}

		return responseBuilder.build();
	}
}
