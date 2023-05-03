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

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.repo.BenutzerRepo;
import ch.dvbern.oss.vacme.shared.util.Constants;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.MDC;

import static java.util.Objects.requireNonNull;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
@RequestScoped
@Slf4j
public class BenutzerSyncFilter implements ContainerRequestFilter {

	@Inject
	JsonWebToken jsonWebToken;

	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	BenutzerRepo benutzerRepo;

	private @Nullable UserPrincipal userPrincipal;

	/**
	 * Dies laueft fuer jeden Benutzer und fuegt den Benutzer in die DB ein bzw updatet ihn.
	 */
	@Override
	public void filter(ContainerRequestContext context) {
		if (securityIdentity.isAnonymous()) {
			userPrincipal = new UserPrincipalImpl(null, benutzerRepo);
			return;
		}
		UUID uuid = UUID.fromString(jsonWebToken.getSubject());// keycloak sends us its uuid as a string

		ID<Benutzer> benutzerId = Benutzer.toId(uuid);
		userPrincipal = new UserPrincipalImpl(benutzerId, benutzerRepo);
		MDC.put(Constants.LOG_MDC_VACMEUSER_ID, uuid.toString()); // add id to mapped diagnostic cntxt for logging
	}

	// TODO vacme-1896
	private void storeLastLoginInfoForBenutzer(@NonNull ContainerRequestContext context, @NonNull Benutzer benutzer) {
		String session = getSessionIdClaim();
		if (session != null) {
			boolean changed = false;
			if (benutzer.getLastUsedSessionId() == null  || !benutzer.getLastUsedSessionId().equals(session)) {
				benutzer.setLastUsedSessionId(session);
				benutzer.setTimestampLastSessionId(LocalDateTime.now());
				changed = true;
			} else{
				LOG.debug("Session {} for user {} was already stored", benutzer.getId(), session);
			}

			String forwaredForHeaderIps = extractForwaredForHeaderIps(context);
			if (benutzer.getLastSeenIp() == null
				|| (forwaredForHeaderIps != null && !benutzer.getLastSeenIp().equals(forwaredForHeaderIps))) {
				benutzer.setLastSeenIp(forwaredForHeaderIps);
				changed = true;
			}
			if (changed) {
				benutzerRepo.merge(benutzer);
			}
		} else {
			LOG.debug("Session was null");
		}
	}

	@Nullable
	private String extractForwaredForHeaderIps(@NonNull ContainerRequestContext context) {
		return  context.getHeaderString("X-Forwarded-For");
	}

	@Nullable
	private String getSessionIdClaim() {
		return  jsonWebToken.getClaim(Constants.SESSION_ID_CLAIM);
	}

	@Produces
	@RequestScoped
	public UserPrincipal produceUserPrincipal() {
		// zuerst pruefen ob wir allenfalls systemadmin sind weil wir zB einen interne Timerfunktion am laufen haben
		boolean isInternalAdmin =
			securityIdentity.getRoles().stream().anyMatch(BenutzerRolleName.SYSTEM_INTERNAL_ADMIN::equals);
		if (isInternalAdmin) {
			userPrincipal = new UserPrincipalImpl(Benutzer.toId(DBConst.SYSTEM_ADMIN_ID), benutzerRepo);
		}

		String msg = "User principal was not initialized in BenutzerSyncFilter";
		if (userPrincipal == null) {
			LOG.error(msg);
		}
		return requireNonNull(userPrincipal, msg);

	}

	public void switchToAdmin() {
		this.userPrincipal = new UserPrincipalImpl(Benutzer.toId(DBConst.SYSTEM_ADMIN_ID), benutzerRepo);
	}
}
