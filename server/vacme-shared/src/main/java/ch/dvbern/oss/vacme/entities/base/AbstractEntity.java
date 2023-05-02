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
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.entities.util.DefaultEntityListener;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst.Format;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;

import static java.util.Objects.requireNonNull;

@MappedSuperclass
@EntityListeners(DefaultEntityListener.class)
@Audited
@Getter
@Setter
@Slf4j
public abstract class AbstractEntity<Id> implements IdSupplier<Id>, Serializable {

	private static final long serialVersionUID = -979317154050183445L;

	@Override
	public abstract Id getId();

	@SuppressWarnings("unused")
	public abstract AbstractEntity<Id> setId(Id id);

	@Version
	@Column(nullable = false)
	// No validation: it's managed by JPA
	private long version;

	@Column(nullable = false, updatable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(type = SchemaType.STRING, format = Format.DATE_TIME, implementation = String.class)
	// No validation: managed by DefaultEntityListener
	private LocalDateTime timestampErstellt = LocalDateTime.now();

	@Column(nullable = false, updatable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(type = SchemaType.STRING, format = Format.DATE_TIME, implementation = String.class)
	// No validation: managed by DefaultEntityListener
	private LocalDateTime timestampMutiert = LocalDateTime.now();

	// Wert darf nicht leer sein, aber kein @NotNull, da Wert erst im @PrePersist gesetzt
	// wir verwenden hier die Hibernate spezifische Annotation, weil diese vererbt wird
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String userErstellt;

	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String userMutiert;

	// @SuppressFBWarnings(value = "BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS", justification = "Es wird Hibernate"
	// 	+ ".getClass genutzt um von Proxies (LazyInit) die konkrete Klasse zu erhalten")
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		//noinspection ObjectEquality
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
			return false;
		}

		AbstractEntity<?> that = (AbstractEntity<?>) o;
		requireNonNull(getId());
		requireNonNull(that.getId());

		return getId().equals(that.getId());
	}

	@Override
	@SuppressWarnings("dereference.of.nullable")
	public int hashCode() {
		//noinspection ProhibitedExceptionCaught
		try {
			requireNonNull(getId());
		} catch (NullPointerException e) { // NOPMD.AvoidCatchingNPE
			throw new IllegalArgumentException("NULL ID in " + this, e);
		}
		return getId().hashCode();
	}

	@Override
	public String toString() {
		return toStringHelper()
			.toString();
	}

	protected MoreObjects.ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(this)
			.add("id", getId());
	}

	@JsonIgnore
	public boolean isNew() {
		return userErstellt == null;
	}
}
