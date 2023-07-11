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

package ch.dvbern.oss.vacme.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.types.FachRolle;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class KeyCloakUtil {

	public static final String[] KEYCLOAK_FIXED_ROLES = { "default-roles-vacme","default-roles-vacme-web" };


	@NonNull
	public static Set<BenutzerRolle> mapRoles(@NonNull Set<String> roles) {
		Set<BenutzerRolle> set = new HashSet<>();
		List<String> rolesWithoutFixedOrCompositeRoles = filterOutKeycloakFixedRoles(filterOutFachlicheCompositeRoles(roles));

		for (String role : rolesWithoutFixedOrCompositeRoles) {
			try {
				BenutzerRolle benutzerRolle = BenutzerRolle.valueOf(role);
				set.add(benutzerRolle);
			} catch (IllegalArgumentException exception) {
				LOG.warn("Could not map rollenstring {} to a Benutzerrolle", role);
			}
		}
		return set;
	}

	private static List<String> filterOutKeycloakFixedRoles(Collection<String> roles) {

		List<String> remainingRoles =
			roles.stream().filter(
				s -> Arrays.stream(KEYCLOAK_FIXED_ROLES).noneMatch(keycloakDefaultRole -> keycloakDefaultRole.equals(s))
			).collect(Collectors.toList());
		return remainingRoles;
	}

	/**
	 * Wir matchen intern nur gegen die non-composite Rollen. Das heisst nicht gegen die fachlichen Rollen
	 *
	 * @param roles Liste mit Rollenstrings zum Filtern
	 * @return Liste mit Rollenstrings ohne solche die eine fachliche/composite Rolle matchen
	 */
	private static List<String> filterOutFachlicheCompositeRoles(@NotNull Collection <String> roles) {
		List<String> remainingRoles =
			roles.stream().filter(
				s -> Arrays.stream(FachRolle.values()).noneMatch(fachRolle -> fachRolle.getKeyCloakRoleName().equals(s))
			).collect(Collectors.toList());
		return remainingRoles;
	}
}
