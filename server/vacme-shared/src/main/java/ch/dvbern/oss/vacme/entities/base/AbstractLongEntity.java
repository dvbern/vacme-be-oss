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

package ch.dvbern.oss.vacme.entities.base;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.util.IdNotNullEntityListener;

/**
 * Entity mit einer Long ID die nicht automatisch gesetzt wird
 * @param <T>
 */
@MappedSuperclass
@EntityListeners(IdNotNullEntityListener.class)
public abstract class AbstractLongEntity<T extends AbstractLongEntity<T>> extends AbstractEntity<Long> {

	private static final long serialVersionUID = 8718148073668223848L;

	@Id
	@NotNull
	@Column(updatable = false)
	private Long id;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public T setId(Long id) {
		this.id = id;

		@SuppressWarnings("unchecked")
		T t = (T) this;
		return t;
	}
}
