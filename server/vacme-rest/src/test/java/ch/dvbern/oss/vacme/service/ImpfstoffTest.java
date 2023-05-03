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

package ch.dvbern.oss.vacme.service;

import ch.dvbern.oss.vacme.entities.impfen.ImpfempfehlungChGrundimmunisierung;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ImpfstoffTest {

	@ParameterizedTest
	@CsvSource({
		// dosenBenoetigt, zulassungsStatus, valid
		"-1, ZUGELASSEN, false",
		"0, ZUGELASSEN, false",
		"1, ZUGELASSEN, true",
		"2, ZUGELASSEN, true",
		"3, ZUGELASSEN, false",
		"4, ZUGELASSEN, false",
		"5, ZUGELASSEN, false",
		"6, ZUGELASSEN, false",

		"-1, NICHT_WHO_ZUGELASSEN, false",
		"0, NICHT_WHO_ZUGELASSEN, false",
		"1, NICHT_WHO_ZUGELASSEN, true",
		"2, NICHT_WHO_ZUGELASSEN, true",
		"3, NICHT_WHO_ZUGELASSEN, true",
		"4, NICHT_WHO_ZUGELASSEN, true",
		"5, NICHT_WHO_ZUGELASSEN, false",
		"6, NICHT_WHO_ZUGELASSEN, false",
	})
	public void testImpfstoffValidierungDosenBenoetigt(int anzahlDosenBenoetigt, ZulassungsStatus zulassungsStatus, boolean valid) {
		Impfstoff impfstoff = new Impfstoff();
		impfstoff.setAnzahlDosenBenoetigt(anzahlDosenBenoetigt);
		impfstoff.setZulassungsStatus(zulassungsStatus);

		if (valid) {
			ImpfstoffService.validateDosenBenoetigt(impfstoff);
		} else {
			Assertions.assertThrows(AppValidationException.class, () -> ImpfstoffService.validateDosenBenoetigt(impfstoff));
		}
	}

	@ParameterizedTest
	@CsvSource({
		// zulassungsStatus, wenn, dann, valid, Grund
		"ZUGELASSEN, 1, 2, true, ",
		"ZUGELASSEN, 2, 1, true, ",
		"ZUGELASSEN, 3, 1, false, zu grosser wenn-Wert fuer ZUGELASSEN",
		"ZUGELASSEN, 1, 3, false, zu grosser dann-Wert",
		"ZUGELASSEN, 3, 1, false, wenn-Wert grösser als 2 bei ZUGELASSEN",
		"ZUGELASSEN, 0, 1, false, wenn-Wert kleiner als 1 bei ZUGELASSEN",
		"ZUGELASSEN, 2, -1, false, dann-Wert kleiner als 0 bei ZUGELASSEN",

		"NICHT_WHO_ZUGELASSEN, 4, 1, true, ",
		"NICHT_WHO_ZUGELASSEN, 1, 2, true, ",
		"NICHT_WHO_ZUGELASSEN, 2, 2, true, ",
		"NICHT_WHO_ZUGELASSEN, 2, 1, true, ",
		"NICHT_WHO_ZUGELASSEN, 3, 0, true, ",
		"NICHT_WHO_ZUGELASSEN, 3, 1, true, ",
		"NICHT_WHO_ZUGELASSEN, 3, 2, true, ",
		"NICHT_WHO_ZUGELASSEN, 4, 2, true, ",
		"NICHT_WHO_ZUGELASSEN, 4, 0, true, ",
		"NICHT_WHO_ZUGELASSEN, 0, 1, false, wenn-Wert kleiner als 1 bei NICHT_WHO_ZUGELASSEN",
		"NICHT_WHO_ZUGELASSEN, 5, 1, false, wenn-Wert grösser als 4 bei NICHT_WHO_ZUGELASSEN",
		"NICHT_WHO_ZUGELASSEN, 1, 3, false, dann-Wert grösser als 2 bei NICHT_WHO_ZUGELASSEN",
		"NICHT_WHO_ZUGELASSEN, 1, -1, false, dann-Wert kleiner als 0 bei NICHT_WHO_ZUGELASSEN",
	})
	public void testImpfstoffValidierungImpfempfehlungen(ZulassungsStatus zulassungsStatus, Integer wenn, Integer dann, boolean valid) {
		var empfehlung = new ImpfempfehlungChGrundimmunisierung();
		empfehlung.setAnzahlVerabreicht(wenn);
		empfehlung.setNotwendigFuerChGrundimmunisierung(dann);

		if (valid) {
			ImpfstoffService.validateImpfempfehlung(zulassungsStatus, empfehlung);
		} else {
			Assertions.assertThrows(AppValidationException.class, () -> ImpfstoffService.validateImpfempfehlung(zulassungsStatus, empfehlung));
		}
	}
}
