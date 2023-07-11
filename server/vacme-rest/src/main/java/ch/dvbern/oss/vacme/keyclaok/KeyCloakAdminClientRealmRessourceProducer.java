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

package ch.dvbern.oss.vacme.keyclaok;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;

@ApplicationScoped
public class KeyCloakAdminClientRealmRessourceProducer {

	@Inject
	VacmeSettingsService vacmeSettingsService;

	@Produces
	public KeycloakAdapter getRealm() {
		final String keycloakWebRealm = vacmeSettingsService.getKeycloakWebRealm();
		var keycloak = KeycloakBuilder.builder()
				.serverUrl(vacmeSettingsService.getKeycloakServerUrl())
				.realm(keycloakWebRealm)
				.grantType(OAuth2Constants.PASSWORD)
				.clientId(vacmeSettingsService.getKeycloakClientId())
				.clientSecret(vacmeSettingsService.getKeycloakWebClientSecret())
				.username(vacmeSettingsService.getKeycloakUsername())
				.password(vacmeSettingsService.getKeycloakPassword())
				.build();
		return new KeycloakAdapter(keycloak.realm(keycloakWebRealm), "web", keycloakWebRealm);
	}

	@Produces
	@RealmVacmeReg
	public KeycloakAdapter regRealm() {
		final String keycloakRegRealm = vacmeSettingsService.getKeycloakRegRealm();
		var keycloakReg = KeycloakBuilder.builder()
				.serverUrl(vacmeSettingsService.getKeycloakServerUrl())
				.realm(keycloakRegRealm)
				.grantType(OAuth2Constants.PASSWORD)
				.clientId(vacmeSettingsService.getKeycloakClientId())
				.clientSecret(vacmeSettingsService.getKeycloakRegClientSecret())
				.username(vacmeSettingsService.getKeycloakUsername())
				.password(vacmeSettingsService.getKeycloakPassword())
				.build();
			return new KeycloakAdapter(keycloakReg.realm(keycloakRegRealm),"reg", keycloakRegRealm);
	}
}


