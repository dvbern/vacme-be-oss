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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.enums.Mandant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(uniqueConstraints = @UniqueConstraint(name = "UC_MandantProperty_name_mandantIdentifier",
	columnNames = { "name", "mandant" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MandantProperty extends AbstractUUIDEntity<MandantProperty> {

	private static final long serialVersionUID = 6760712521061831965L;

	@NotNull
	@NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	private MandantPropertyKey name;

	@NotEmpty
	@NonNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String value;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Mandant mandant;

	public Boolean getValueAsBoolean() {
		return Boolean.valueOf(value);
	}
}
