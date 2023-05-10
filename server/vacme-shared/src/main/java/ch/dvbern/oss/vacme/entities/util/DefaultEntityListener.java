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

package ch.dvbern.oss.vacme.entities.util;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.AbstractEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import org.eclipse.microprofile.config.ConfigProvider;

public class DefaultEntityListener {

	@PrePersist
	protected void prePersist(AbstractEntity<?> entity) {
		LocalDateTime now = now();
		String benutzer = loggedInUser();

		entity.setTimestampErstellt(now);
		entity.setUserErstellt(benutzer);
		entity.setTimestampMutiert(now);
		entity.setUserMutiert(benutzer);

		validate(entity);
	}

	@PreUpdate
	public void preUpdate(AbstractEntity<?> entity) {
		LocalDateTime now = now();
		String benutzer = loggedInUser();

		entity.setTimestampMutiert(now);
		entity.setUserMutiert(benutzer);

		validate(entity);
	}

	void validate(AbstractEntity<?> entity) {
		if (entity.getVersion() < 0) {
			throw new IllegalStateException(String.format(
				"Entity version must be >= 0 but was: %d, id: %s@%s, entity: %s",
				entity.getVersion(), entity.getId(), entity.getClass().getName(), entity));
		}

		if (entity.getTimestampErstellt() == null
			|| entity.getUserErstellt() == null
			|| entity.getTimestampMutiert() == null
			|| entity.getUserMutiert() == null) {
			throw new IllegalStateException(String.format(
				"Entity changelog fields wrong: %s/%s/%s/%s, entity: %s",
				entity.getTimestampErstellt(), entity.getUserErstellt(), entity.getTimestampMutiert(),
				entity.getUserMutiert(),
				entity));
		}
	}

	String loggedInUser() {
		// Limitation of Quarkus: Injection into EntityListeners does not work (yet)
		// See: https://github.com/quarkusio/quarkus/issues/6948

		UserPrincipal userPrincipal = CDI.current().select(UserPrincipal.class).get();
		if (userPrincipal.getBenutzerIdOpt().isPresent()) {
			ID<Benutzer> benutzerID = userPrincipal.getBenutzerIdOpt().get();
			return benutzerID.getId().toString();// im normalfall user login id aus keycloak  zurueck

		} else {
			Optional<Boolean> securityDisabled = ConfigProvider.getConfig()
				.getOptionalValue("vacme.authorization.disable", Boolean.class);
			if (securityDisabled.isPresent() && securityDisabled.get()) {
				return "anonymous@dvbern.ch";
			}
			throw new IllegalStateException("UserPrincipal not set?");

		}
	}

	LocalDateTime now() {
		return LocalDateTime.now();
	}
}
