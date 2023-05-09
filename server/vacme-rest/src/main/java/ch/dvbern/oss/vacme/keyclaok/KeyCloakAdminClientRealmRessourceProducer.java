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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;

@ApplicationScoped
public class KeyCloakAdminClientRealmRessourceProducer {

	@ConfigProperty(name = "vacme.keycloak.serverUrl", defaultValue = "http://localhost:8180/")
	protected String keycloakServerUrl;
	@ConfigProperty(name = "vacme.keycloak.clientId", defaultValue = "")
	protected String keycloakClientId;
	@ConfigProperty(name = "vacme.keycloak.username", defaultValue = "")
	protected String keycloakUsername;
	@ConfigProperty(name = "vacme.keycloak.password", defaultValue = "")
	protected String keycloakPassword;

	// Web-Realm
	@ConfigProperty(name = "vacme.keycloak.realm", defaultValue = "vacme-web")
	protected String keycloakRealm;
	@ConfigProperty(name = "vacme.keycloak.clientSecret", defaultValue = "")
	protected String keycloakClientSecret;

	// Reg-Realm
	@ConfigProperty(name = "vacme.keycloak.reg.realm", defaultValue = "vacme")
	protected String keycloakRealmReg;
	@ConfigProperty(name = "vacme.keycloak.reg.clientSecret", defaultValue = "")
	protected String keycloakRegClientSecret;

	@Produces
	public KeycloakAdapter getRealm() {
			var keycloak = KeycloakBuilder.builder()
				.serverUrl(keycloakServerUrl)
				.realm(keycloakRealm)
				.grantType(OAuth2Constants.PASSWORD)
				.clientId(keycloakClientId)
				.clientSecret(keycloakClientSecret)
				.username(keycloakUsername)
				.password(keycloakPassword)
				.build();
		return new KeycloakAdapter(keycloak.realm(keycloakRealm), "web", keycloakRealm);
	}

	@Produces
	@RealmVacmeReg
	public KeycloakAdapter regRealm() {
			var keycloakReg = KeycloakBuilder.builder()
				.serverUrl(keycloakServerUrl)
				.realm(keycloakRealmReg)
				.grantType(OAuth2Constants.PASSWORD)
				.clientId(keycloakClientId)
				.clientSecret(keycloakRegClientSecret)
				.username(keycloakUsername)
				.password(keycloakPassword)
				.build();
			return new KeycloakAdapter(keycloakReg.realm(keycloakRealmReg),"reg", keycloakRealmReg);
	}
}


