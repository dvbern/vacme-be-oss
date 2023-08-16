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

package ch.dvbern.oss.vacme.entities;

import java.util.List;
import java.util.Optional;


import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface UserPrincipal {

	Optional<Benutzer> getBenutzer();
	Optional<ID<Benutzer>> getBenutzerIdOpt();
	Benutzer getBenutzerOrThrowException();
	boolean isCallerInAnyOfRole(@NonNull List<BenutzerRolle> roles);
	boolean isCallerInAnyOfRole(@NonNull BenutzerRolle... roles);
	boolean isCallerInRole(@NonNull BenutzerRolle role);
	boolean userHasRoleOtherThanImpfwilliger(@NonNull List<BenutzerRolle> roles);
	boolean isPortalUser();
}
