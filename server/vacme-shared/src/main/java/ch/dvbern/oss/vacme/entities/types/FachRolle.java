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

package ch.dvbern.oss.vacme.entities.types;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Dies sind die Aggregierten Rollen
 */
@Getter
@Slf4j
@Schema(enumeration = {
	"APPLIKATIONS_SUPPORT", "FACHVERANTWORTUNG_BAB", "FACHVERANTWORTUNG_BAB_DELEGIERT",
	"ORGANISATIONSVERANTWORTUNG", "FACHSUPERVISION", "NACHDOKUMENTATION",
	"MEDIZINISCHE_NACHDOKUMENTATION", "ORGANISATIONSSUPERVISION", "FACHPERSONAL",
	"PERSONAL", "PERSONAL_TELEFON",
})
public enum FachRolle {


	// Fachrollen
	APPLIKATIONS_SUPPORT ("ApplikationsSupport"),
	FACHVERANTWORTUNG_BAB("Fachverantwortung BAB"),
	FACHVERANTWORTUNG_BAB_DELEGIERT("Fachverantwortung BAB delegiert"),
	ORGANISATIONSVERANTWORTUNG("Organisationsverantwortung"),
	FACHSUPERVISION("Fachsupervision"),
	NACHDOKUMENTATION("Nachdokumentation"),
	MEDIZINISCHE_NACHDOKUMENTATION("Medizinische Nachdokumentation"),
	ORGANISATIONSSUPERVISION("Organisationssupervision"),
	FACHPERSONAL("Fachpersonal"),
	PERSONAL("Personal"),
	PERSONAL_TELEFON("Personal Telefon");

	private final String keyCloakRoleName;

	FachRolle(String keyCloakRoleName) {
		this.keyCloakRoleName = keyCloakRoleName;
	}

	@Nullable
	public static FachRolle fromKeyCloakRoleName(String keyCloakRoleName) {
		for(FachRolle e: FachRolle.values()) {
			if(e.keyCloakRoleName.equals(keyCloakRoleName)) {
				return e;
			}
		}
		LOG.warn("Fachrolle aus KC konnte nicht gemapped werden " + keyCloakRoleName);
		return null;// not found
	}

	public String getKeyCloakRoleName() {
		return keyCloakRoleName;
	}

	@NonNull
	public static List<String> getAllRoleNames() {
		List<String> roleNames = new ArrayList<>();
		for (FachRolle value : FachRolle.values()) {
			roleNames.add(value.keyCloakRoleName);
		}
		return roleNames;
	}
}
