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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.repo.BenutzerRepo;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import org.apache.commons.lang3.concurrent.Memoizer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class UserPrincipalImpl implements UserPrincipal {

	@Nullable
	private final ID<Benutzer> benutzerId;

	@NonNull
	private final Memoizer<ID<Benutzer>, Optional<Benutzer>> benutzerCache;

	public UserPrincipalImpl(
		@Nullable ID<Benutzer> benutzerId,
		@NonNull BenutzerRepo repo
	) {
		this.benutzerId = benutzerId;
		benutzerCache = new Memoizer<>(repo::getById);
	}

	@Override
	@NonNull
	public Optional<Benutzer> getBenutzer() {
		if (benutzerId == null) {
			return Optional.empty();
		}

		try {
			// Lazy load so we execute the queryies only when a transaction is active.
			Optional<Benutzer> benutzer = benutzerCache.compute(benutzerId);
			return benutzer;
		} catch (InterruptedException e) {
			throw new IllegalStateException("Should not see me: " + benutzerId, e);
		}
	}

	@Override
	@NonNull
	public Optional<ID<Benutzer>> getBenutzerIdOpt() {
		if (benutzerId == null) {
			return Optional.empty();
		}
		return Optional.of(benutzerId);
	}

	@Override
	public Benutzer getBenutzerOrThrowException() {
		return getBenutzer()
			.orElseThrow(() -> new AppFailureException("no user logged in"));
	}

	@Override
	public boolean isCallerInAnyOfRole(@Nonnull List<BenutzerRolle> roles) {
		checkNotNull(roles);
		return roles.stream().anyMatch(this::isCallerInRole);
	}

	@Override
	public boolean isCallerInAnyOfRole(@NonNull BenutzerRolle... roles) {
		checkNotNull(roles);
		return isCallerInAnyOfRole(Arrays.asList(roles));
	}

	@Override
	public boolean userHasRoleOtherThanImpfwilliger(@Nonnull List<BenutzerRolle> roles) {
		checkNotNull(roles);
		return roles.stream().anyMatch(benutzerRolle -> !(BenutzerRolle.getImpfwilligerRoles()).contains(benutzerRolle));
	}

	@Override
	public boolean isCallerInRole(@Nonnull BenutzerRolle role) {
		checkNotNull(role, "role that is asked for must be set");
		return getBenutzerOrThrowException()
			.getBerechtigungen()
			.stream()
			.anyMatch(benutzerBerechtigung -> benutzerBerechtigung.getRolle() == role);
	}
}
