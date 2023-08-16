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

package ch.dvbern.oss.vacme.entities.migration;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_Migration_Impfung", columnNames = "impfung_id")
	}
)
public class Migration extends AbstractUUIDEntity<Migration> {

	private static final long serialVersionUID = 8232997231572890243L;

	@NotNull
	@NonNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Migration_Impfung"), nullable = false)
	private Impfung impfung;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_ENUM_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String impfortGLN;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_ENUM_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String verantwortlicherPersonGLN;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_ENUM_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String durchfuehrendPersonGLN;

	public static ID<Migration> toId(UUID id) {
		return new ID<>(id, Migration.class);
	}
}
