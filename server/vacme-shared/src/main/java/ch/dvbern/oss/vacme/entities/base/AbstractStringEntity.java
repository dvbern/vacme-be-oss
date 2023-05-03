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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.util.IdNotNullEntityListener;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static ch.dvbern.oss.vacme.entities.util.DBConst.DB_UUID_LENGTH;

@MappedSuperclass
@EntityListeners(IdNotNullEntityListener.class)
public abstract class AbstractStringEntity<T extends AbstractStringEntity<T>>
		extends AbstractEntity<String> {
	private static final long serialVersionUID = -7385608546644251416L;

	@Id
	@Column(nullable = false, updatable = false, length = DB_UUID_LENGTH)
	@NotNull
	@NotEmpty
	@Schema(required = true)
	private String id = "";

	@Override
	public String getId() {
		return id;
	}

	@Override
	public T setId(String id) {
		this.id = id;

		@SuppressWarnings("unchecked")
		T t = (T) this;
		return t;
	}

	public IDS<T> toId() {
		@SuppressWarnings("unchecked")
		var result = new IDS<>(id, (Class<T>) getClass());

		return result;
	}
}
