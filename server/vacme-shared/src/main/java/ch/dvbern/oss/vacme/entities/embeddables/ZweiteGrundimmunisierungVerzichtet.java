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


package ch.dvbern.oss.vacme.entities.embeddables;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@Embeddable
public class ZweiteGrundimmunisierungVerzichtet implements Serializable {

	private static final long serialVersionUID = -6371802459880460501L;

	@Nullable
	@Column(length = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	@Size(max = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	private String zweiteImpfungVerzichtetGrund;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime zweiteImpfungVerzichtetZeit;

	@NotNull
	@Column(nullable = false)
	private boolean genesen = false;

	@Nullable
	@Column(nullable = true)
	private LocalDate positivGetestetDatum;
}
