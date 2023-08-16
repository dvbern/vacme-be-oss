/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service.boosterprioritaet;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@AllArgsConstructor
public class CovidImpfschutzcalculationConfigDTO {

	private boolean enablePfizerOnlyForU30 = false;

	private int minAgeZh = 16;

	private int minAgeBe = 16;

	private int freigabeOffsetImpfungMonate = 4;

	private int freigabeOffsetImpfungTage = 0;

	@Nullable
	private Integer freigabeOffsetKrankheitMonate;

	@Nullable
	private Integer freigabeOffsetKrankheitTage;

	@Nullable
	private LocalDate cutoffDateSelbstzahler;
}
