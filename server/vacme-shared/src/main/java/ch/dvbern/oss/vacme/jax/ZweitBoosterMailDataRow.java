/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.jax;

import java.time.LocalDate;

import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Getter
@Setter
public class ZweitBoosterMailDataRow {
	private boolean isSelbstzahlerImpfung;
	private LocalDate geburtsdatum;
	private Boolean immunsupprimiert;
	private Prioritaet prioritaet;

	@QueryProjection
	public ZweitBoosterMailDataRow(
		boolean isSelbstzahlerImpfung,
		@NonNull LocalDate geburtsdatum,
		@NonNull Boolean immunsupprimiert,
		@NonNull Prioritaet prioritaet
	) {
		this.isSelbstzahlerImpfung = isSelbstzahlerImpfung;
		this.geburtsdatum = geburtsdatum;
		this.immunsupprimiert = immunsupprimiert;
		this.prioritaet = prioritaet;
	}
}
