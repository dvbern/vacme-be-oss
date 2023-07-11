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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.keyclaok.KeycloakAdapter;
import ch.dvbern.oss.vacme.keyclaok.RealmVacmeReg;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import io.smallrye.health.api.Wellness;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Wellness
@ApplicationScoped
@Slf4j
public class KeycloakPortalapplikationConnectionHealthCheck extends KeycloakConnectionhealthCheck implements HealthCheck {

	private final KeycloakAdapter regRealmKeycloakAdapter;
	private final VacmeSettingsService vacmeSettingsService;

	@Inject
	public KeycloakPortalapplikationConnectionHealthCheck(
		@RealmVacmeReg KeycloakAdapter regRealmKeycloakAdapter, //vacme
		VacmeSettingsService vacmeSettingsService
	) {
		this.regRealmKeycloakAdapter = regRealmKeycloakAdapter;
		this.vacmeSettingsService = vacmeSettingsService;
	}

    @Override
    public HealthCheckResponse call() {
		String healthcheckName = "Keycloak Portalapplikation Machine2Machine connection health check";
		return getHealthCheckResponse(
			healthcheckName,
			regRealmKeycloakAdapter,
			vacmeSettingsService.getKeycloakUsername(),
			vacmeSettingsService.getKeycloakClientId());
	}
}
