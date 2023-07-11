/*
 *
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.enums.PartnerMarker;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.util.Assert;
import org.mockito.Mockito;

class PublicResourceTestForWellConfig {

	private PublicResource serviceUnderTest;

	private static final String EXPECTED_WELL_LOCAL_REG_CONFIG = "{\n"
		+ "\t\"realm\": \"vacme\",\n"
		+ "\t\"auth-server-url\": \"http://well-local-vacme.ch/:8180/auth\",\n"
		+ "\t\"ssl-required\": \"external\",\n"
		+ "\t\"resource\": \"vacme-initial-app-localhost-well\",\n"
		+ "\t\"public-client\": true,\n"
		+ "\t\"confidential-port\": 0\n"
		+ '}';

	public static final String EXPECTED_DEFAULT_REG_CONFIG = "{\n"
		+ "\t\"realm\": \"vacme\",\n"
		+ "\t\"auth-server-url\": \"http://localhost:8180/auth\",\n"
		+ "\t\"ssl-required\": \"external\",\n"
		+ "\t\"resource\": \"vacme-initial-app-localhost\",\n"
		+ "\t\"public-client\": true,\n"
		+ "\t\"confidential-port\": 0\n"
		+ '}';

	public static final String EXPECTED_DEFAULT_WEB_CONFIG = "{\n"
		+ "\t\"realm\": \"vacme-web\",\n"
		+ "\t\"auth-server-url\": \"http://localhost:8180/auth/\",\n"
		+ "\t\"ssl-required\": \"external\",\n"
		+ "\t\"resource\": \"vacme-web-app-localhost\",\n"
		+ "\t\"public-client\": true,\n"
		+ "\t\"confidential-port\": 0\n"
		+ '}';

	// to restore default value after test
	private String regConfigBeforeTest;
	private String webConfigBeforeTest;

	@BeforeEach
	void setUp() {
		this.serviceUnderTest = new PublicResource(
			Mockito.mock(RegistrierungService.class)
		);

		regConfigBeforeTest = System.getProperty("vacme.keycloak.config.reg");
		webConfigBeforeTest = System.getProperty("vacme.keycloak.config.web");
	}

	@AfterEach
	void tearDown() {
		if (regConfigBeforeTest == null) {
			System.clearProperty("vacme.keycloak.config.reg");
		} else {
			System.setProperty("vacme.keycloak.config.reg", regConfigBeforeTest);
		}

		if (webConfigBeforeTest == null) {
			System.clearProperty("vacme.keycloak.config.web");
		} else {
			System.setProperty("vacme.keycloak.config.web", webConfigBeforeTest);
		}
	}

	@Test
	void givenWellQueryparam_whenLoadKeycloakConfig_thenReturenWellConfig() {
		System.setProperty("vacme.keycloak.config.reg", "local-vacme-dvbern-ch_reg_local_keycloak.json");

		Response resp = this.serviceUnderTest.loadKeycloakConfig("vacme", PartnerMarker.WELL);
		Assertions.assertNotNull(resp.getEntity(), "Response should contain a string entity (json)");
		String json = resp.getEntity().toString();
		Assert.equals(EXPECTED_WELL_LOCAL_REG_CONFIG, json);
	}

	@Test
	void givenNoQueryparam_whenLoadKeycloakConfig_thenReturenDefaultConfig() {
		System.setProperty("vacme.keycloak.config.reg", "local-vacme-dvbern-ch_reg_local_keycloak.json");

		Response resp = this.serviceUnderTest.loadKeycloakConfig("vacme", null);
		Assertions.assertNotNull(resp.getEntity(), "Response should contain a string entity (json)");
		String json = resp.getEntity().toString();
		Assert.equals(EXPECTED_DEFAULT_REG_CONFIG, json);
	}

	@Test
	void givenWellQueryparam_whenLoadKeycloakConfigForFachapp_thenReturenDefaultConfig() {
		System.setProperty("vacme.keycloak.config.reg", "local-vacme-dvbern-ch_reg_local_keycloak.json");
		System.setProperty("vacme.keycloak.config.web", "local-vacme-dvbern-ch_web_local_keycloak.json");

		Response resp = this.serviceUnderTest.loadKeycloakConfig("vacme-web", PartnerMarker.WELL);
		Assertions.assertNotNull(resp.getEntity(), "Response should contain a string entity (json)");
		String json = resp.getEntity().toString();
		Assert.equals(EXPECTED_DEFAULT_WEB_CONFIG, json);
	}

	@Test
	void givenNoQueryparam_whenLoadKeycloakConfigForFachapp_thenReturenDefaultConfig() {
		System.setProperty("vacme.keycloak.config.reg", "local-vacme-dvbern-ch_reg_local_keycloak.json");
		System.setProperty("vacme.keycloak.config.web", "local-vacme-dvbern-ch_web_local_keycloak.json");

		Response resp = this.serviceUnderTest.loadKeycloakConfig("vacme-web",null);
		Assertions.assertNotNull(resp.getEntity(), "Response should contain a string entity (json)");
		String json = resp.getEntity().toString();
		Assert.equals(EXPECTED_DEFAULT_WEB_CONFIG, json);
	}
}
