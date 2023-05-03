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

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import ch.dvbern.oss.vacme.entities.base.AbstractEntity;

/**
 * This Entity Listener simply checks if the ID of a bean is not null prior to updates or inserts.
 * This only makes sense if the id is set programmatically
 */
public class IdNotNullEntityListener {

	@PrePersist
	protected void prePersist(AbstractEntity<?> entity) {
		validate(entity);
	}

	@PreUpdate
	public void preUpdate(AbstractEntity<?> entity) {
		validate(entity);
	}

	void validate(AbstractEntity<?> entity) {
		if (!this.isIdValid(entity)) {
			throw new IllegalStateException("Entity id must not be null!");
		}
	}

	boolean isIdValid(AbstractEntity<?> entity) {
		return entity.getId() != null;
	}
}
