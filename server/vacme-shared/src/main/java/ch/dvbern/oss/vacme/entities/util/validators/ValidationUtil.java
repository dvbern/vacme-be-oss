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

package ch.dvbern.oss.vacme.entities.util.validators;

import java.util.UUID;

import ch.dvbern.oss.vacme.entities.base.AbstractEntity;
import ch.dvbern.oss.vacme.entities.base.EntityID;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;

import static java.util.Objects.requireNonNull;

public final class ValidationUtil {

	private ValidationUtil() {
	}

	/**
	 * Checks that a given ID matches the id of an entity.
	 * Useful mostly for PUT requets: check that the ID of an updated entity matches its parent entities ID.
	 */
	public static <Entity extends AbstractEntity<UUID> & EntityID<Entity>>
	void validateId(
			ID<Entity> id,
			Entity entity) {
		requireNonNull(id);
		requireNonNull(entity);

		if (!id.equals(entity.toId())) {
			throw new AppFailureException(String.format("ID does not match: %s != %s (entity: %s)",
					id, entity.getId(), entity));
		}
	}
}
