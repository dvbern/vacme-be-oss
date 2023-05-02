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

package ch.dvbern.oss.vacme.entities.benutzer;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
// NoArgsConstructor: JPA only
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Audited
@Table(
	indexes = {
		@Index(name = "benutzerberechtigung_benutzer_fk1_ix", columnList = "benutzer_id"),
	},
	uniqueConstraints = @UniqueConstraint(name = "benutzerberechtigung_ux1",
		columnNames = { "benutzer_id", "rolle" })
)
public class BenutzerBerechtigung extends AbstractUUIDEntity<BenutzerBerechtigung> {
	private static final long serialVersionUID = 1720316901673834886L;

	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "benutzerberechtigung_benutzer_fk1"))
	@NotNull
	private Benutzer benutzer;

	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	@NotNull
	private BenutzerRolle rolle;

	@SuppressWarnings("unused")
	public static ID<BenutzerBerechtigung> toId(UUID id) {
		return new ID<>(id, BenutzerBerechtigung.class);
	}

	public boolean isSameBerechtigung(BenutzerRolle otherRolle) {
		var result = getRolle() == otherRolle;

		return result;
	}
}
