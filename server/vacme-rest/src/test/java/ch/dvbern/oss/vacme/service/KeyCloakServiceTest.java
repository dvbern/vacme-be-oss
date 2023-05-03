/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.keyclaok.KeycloakAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.Mockito;

class KeyCloakServiceTest {
	private KeyCloakService keyCloakService;

	@BeforeEach
	void setUp() {
		keyCloakService = new KeyCloakService(
			new KeycloakAdapter(Mockito.mock(RealmResource.class), "testclient", "testrealm"),
			Mockito.mock(UserPrincipal.class),
			Mockito.mock(SmsService.class)
		);
	}

	@Test
	void isVacmeRoleFilterTest() {
		// FachRolle (in Keycloak erfasst)
		var keycloakFachRoleNames = new String[] {
			"ApplikationsSupport",
			"Fachverantwortung BAB",
			"Fachverantwortung BAB delegiert",
			"Organisationsverantwortung",
			"Fachsupervision",
			"Nachdokumentation",
			"Medizinische Nachdokumentation",
			"Organisationssupervision",
			"Fachpersonal",
			"Personal",
			"Personal Telefon" };
		for (String keycloakFachRoleName : keycloakFachRoleNames) {
			RoleRepresentation rep = new RoleRepresentation(keycloakFachRoleName, keycloakFachRoleName, false);
			Assertions.assertTrue(keyCloakService.isVacmeRole(rep), rep.getName());
		}

		// BenutzerRolle (in Keycloak erfasst)
		var keycloakBenutzerRoleNames = new String[] {
			"IMPFWILLIGER",
			"IMPFTERMINCLIENT",
			"OI_KONTROLLE",
			"OI_DOKUMENTATION",
			"OI_IMPFVERANTWORTUNG",
			"OI_FACHBAB_DELEGIEREN",
			"OI_BENUTZER_VERWALTER",
			"OI_ORT_VERWALTER",
			"OI_BENUTZER_REPORTER",
			"OI_LOGISTIK_REPORTER",
			"OI_MEDIZINISCHER_REPORTER",
			"KT_IMPFVERANTWORTUNG",
			"KT_BENUTZER_REPORTER",
			"KT_LOGISTIK_REPORTER",
			"KT_MEDIZINISCHER_REPORTER",
			"KT_BENUTZER_VERWALTER",
			"KT_ZERTIFIKAT_AUSSTELLER",
			"KT_NACHDOKUMENTATION",
			"KT_MEDIZINISCHE_NACHDOKUMENTATION",
			"KT_IMPFDOKUMENTATION",
			"AS_REGISTRATION_OI",
			"AS_BENUTZER_VERWALTER",
			"MIG_MIGRATION_ADM",
			"SYSTEM_INTERNAL_ADMIN",
			"TRACING" };
		for (String keycloakBenutzerRoleName : keycloakBenutzerRoleNames) {
			RoleRepresentation rep = new RoleRepresentation(keycloakBenutzerRoleName, keycloakBenutzerRoleName, false);
			Assertions.assertTrue(keyCloakService.isVacmeRole(rep));
		}
	}

	@Test
	void isVacmeRoleFilterTestDoesNotMatchDefault() {
		// Keycloak Rollen, die KEINE Vacme-Rollen sind
		var keycloakNonVacmeRoleNames = new String[] {
			"default-roles-vacme-web",
			"default-roles-vacme",
		};
		for (String keycloakNonVacmeRoleName : keycloakNonVacmeRoleNames) {
			RoleRepresentation rep1 = new RoleRepresentation(keycloakNonVacmeRoleName, keycloakNonVacmeRoleName, false);
			Assertions.assertFalse(keyCloakService.isVacmeRole(rep1));
		}
	}
}
