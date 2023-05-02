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

package ch.dvbern.oss.vacme.jax.stats;

import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
public class ImpfstoffTagesReportJax {
	// Impfstoff resp. null fuer die Zeile mit Impfstoff unbekannt
	@Nullable String impfstoffName;
	@Nullable String impfstoffDisplayName;

	// Geplante noch nicht wahrgenommene Termine, die als Empfehlung diesen Impfstoff haben
	@NotNull long pendentTermin1;
	@NotNull long pendentTermin2;
	@NotNull long pendentTerminN;

	// Durchgefuehrte Impfungen (ad hoc oder mit Termin)
	@NotNull long durchgefuehrtImpfung1;
	@NotNull long durchgefuehrtImpfung2;
	@NotNull long durchgefuehrtImpfungN;

	// "Total" ist das Total der noch offenen und der durchgefuehrten, also inkl. AdHoc.
	@SuppressWarnings("unused") // wird uebermittelt
	public long getTotal1() {
		return durchgefuehrtImpfung1 + pendentTermin1;
	}
	@SuppressWarnings("unused") // wird uebermittelt
	public long getTotal2() {
		return durchgefuehrtImpfung2 + pendentTermin2;
	}
	@SuppressWarnings("unused") // wird uebermittelt
	public long getTotalN() {
		return durchgefuehrtImpfungN + pendentTerminN;
	}
}
