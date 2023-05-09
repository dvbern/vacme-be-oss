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

package ch.dvbern.oss.vacme.entities.terminbuchung;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

/**
 * This Entity represents some restrictions that may be attached to multiple OdIs to specify the conditions
 * under which the OdI will be selectable for the person that is trying to book a vaccination slot
 */
@Entity
@Audited
@Table
@Getter
@Setter
@NoArgsConstructor
public class OdiFilter extends AbstractUUIDEntity<OdiFilter> {

	private static final long serialVersionUID = -6020809158982623072L;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private OdiFilterTyp typ;

	@Nullable
	@Column(nullable = true)
	private BigDecimal minimalWert;

	@Nullable
	@Column(nullable = true)
	private BigDecimal maximalWert;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_ENUM_LENGTH)
	private String stringArgument;

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		return builder
			.append(typ)
			.append(maximalWert != null ? maximalWert.doubleValue() : null)
			.append(minimalWert != null ? minimalWert.doubleValue() : null)
			.append(stringArgument)
			.build();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OdiFilter) {
			EqualsBuilder builder = new EqualsBuilder();
			OdiFilter compared = (OdiFilter) obj;
			boolean sameWert = true;
			if (maximalWert != null && compared.maximalWert != null) {
				sameWert = maximalWert.compareTo(compared.maximalWert) == 0;
			} else {
				if (maximalWert != null || compared.maximalWert != null) {
					return false;
				}
			}
			if (minimalWert != null && compared.minimalWert != null) {
				sameWert &= minimalWert.compareTo(compared.minimalWert) == 0;
			} else {
				if (minimalWert != null || compared.minimalWert != null) {
					return false;
				}
			}
			return builder
				.append(typ, compared.typ)
				.append(stringArgument, compared.stringArgument)
				.build() && sameWert;
		}
		return false;
	}
}
