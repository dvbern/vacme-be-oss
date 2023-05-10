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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import ch.dvbern.oss.vacme.entities.util.IdNotNullEntityListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.Type;

import static ch.dvbern.oss.vacme.entities.util.DBConst.DB_UUID_LENGTH;

@MappedSuperclass
@EntityListeners(IdNotNullEntityListener.class)
public abstract class AbstractUUIDEntity<T extends AbstractUUIDEntity<T>> extends AbstractEntity<UUID>
		implements EntityID<T> {
	private static final long serialVersionUID = -4819644657001248089L;

	@Schema(required = true)
	@Id
	@Type(type="org.hibernate.type.UUIDCharType")
	@Column(nullable = false, updatable = false, length = DB_UUID_LENGTH)
	@NonNull
	private UUID id = UUID.randomUUID();


	@Override
	public ID<T> toId() {
		@SuppressWarnings("unchecked")
		var result = new ID<>(id, (Class<T>) getClass());

		return result;
	}

	@Override
	@NonNull
	public UUID getId() {
		return id;
	}

	@Override
	public T setId(UUID id) {
		this.id = id;

		@SuppressWarnings("unchecked")
		T t = (T) this;
		return t;
	}

}
