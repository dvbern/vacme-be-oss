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

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.google.common.base.MoreObjects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import static java.util.Objects.requireNonNull;

public class ID<T extends IdSupplier<UUID>> implements Serializable {

	private static final long serialVersionUID = 8292214485247590321L;

	private final UUID id;

	private final Class<T> entityClazz;

	public ID(UUID id, Class<T> entityClazz) {
		this.id = requireNonNull(id);
		this.entityClazz = requireNonNull(entityClazz);
	}

	@Contract("null,_->null; !null,_->!null")
	public static <T extends IdSupplier<String>>
	@Nullable IDS<T> parse(
			@Nullable String id,
			Class<T> entityClazz
	) {
		if (id == null) {
			return null;
		}

		return new IDS<T>(id, entityClazz);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("entityClazz", entityClazz.getSimpleName())
				.toString();
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		ID<?> id1 = (ID<?>) o;

		return Objects.equals(id, id1.id) && Objects.equals(entityClazz, id1.entityClazz);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, entityClazz);
	}

	public UUID getId() {
		return id;
	}

	public Class<T> getEntityClazz() {
		return entityClazz;
	}
}
