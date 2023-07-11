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

package ch.dvbern.oss.vacme.entities.impfen;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Getter
@Setter
@Audited
@NoArgsConstructor
@AllArgsConstructor
@Table(
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_ImpfempfehlungChGrundimmunisierung_impfstoff_anzahlVerabreich", columnNames = { "impfstoff_id", "anzahlVerabreicht" }),
	}
)
public class ImpfempfehlungChGrundimmunisierung extends AbstractUUIDEntity<ImpfempfehlungChGrundimmunisierung> {
	private static final long serialVersionUID = 8814142246781972992L;

	@NotNull
	@Column(nullable = false)
	private int anzahlVerabreicht;

	@NotNull
	@Column(nullable = false)
	private int notwendigFuerChGrundimmunisierung;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ImpfempfehlungChGrundimmunisierung_impfstoff"))
	private Impfstoff impfstoff;

}
