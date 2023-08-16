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

package ch.dvbern.oss.vacme.entities.registration;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_Personenkontrolle_kontrolleTermin1", columnNames = "kontrolleTermin1_id"),
		@UniqueConstraint(name = "UC_Personenkontrolle_kontrolleTermin2", columnNames = "kontrolleTermin2_id")
	}
)
public class Personenkontrolle extends AbstractUUIDEntity<Personenkontrolle> {

	private static final long serialVersionUID = 4717001837244431694L;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_personenkontrolle_kontrolleTermin1"), nullable = true)
	private ImpfungkontrolleTermin kontrolleTermin1;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_personenkontrolle_kontrolleTermin2"), nullable = true)
	private ImpfungkontrolleTermin kontrolleTermin2;
}
