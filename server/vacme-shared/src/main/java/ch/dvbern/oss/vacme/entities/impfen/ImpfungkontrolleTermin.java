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

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
public class ImpfungkontrolleTermin extends AbstractUUIDEntity<ImpfungkontrolleTermin> {

	private static final long serialVersionUID = 4717001837244431694L;

	@NotNull @NonNull
	@Column(nullable = false)
	private LocalDateTime timestampKontrolle;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	@Size(max = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	private String bemerkung;

	@NotNull
	@Column(nullable = false)
	private boolean identitaetGeprueft = false;

	@Nullable
	@Column(nullable = true)
	private Boolean selbstzahlende;

}
