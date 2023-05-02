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

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MonatTest {

	@Test
	public void testConstructor() {
		LocalDate von = LocalDate.of(2015, 1, 1);
		LocalDate bis = LocalDate.of(2015, 1, 31);
		LocalDate stichtag = LocalDate.of(2015, 1, 15);
		Monat expected = new Monat(von, bis);

		assertEquals(von, expected.getGueltigAb());
		assertEquals(bis, expected.getGueltigBis());

		assertEquals(expected, new Monat(stichtag));
		assertEquals(expected, new Monat(von, bis));
		assertEquals(expected, new Monat(DateRange.of(von, bis)));

		// und jetzt hoffen, das wir nicht genau um Mitternacht laufen :)
		LocalDate now = LocalDate.now();
		Monat expectedNow = new Monat(now);
		Monat actualNow = new Monat();
		assertEquals(expectedNow, actualNow);
	}

	@Test
	public void testSetGueltigAb_Illegal() {
		Monat monat = new Monat(LocalDate.of(2010, 1, 1));

		assertThrows(
				IllegalArgumentException.class,
				() -> monat.setGueltigAb(LocalDate.of(2015, 1, 5))
		);

	}

	@Test
	public void testSetGueltigAb() {
		Monat monat = new Monat(LocalDate.of(2010, 1, 1));
		monat.setGueltigAb(LocalDate.of(2015, 1, 1));

		Monat expected = new Monat(LocalDate.of(2015, 1, 1), LocalDate.of(2015, 1, 31));
		assertEquals(expected, monat);
	}

	@Test
	public void testSetGueltigBis_Illegal() {
		Monat monat = new Monat(LocalDate.of(2010, 1, 1));

		assertThrows(
				IllegalArgumentException.class,
				() -> monat.setGueltigBis(LocalDate.of(2015, 1, 5))
		);
	}

	@Test
	public void testSetGueltigBis() {
		Monat monat = new Monat(LocalDate.of(2010, 1, 1));
		monat.setGueltigBis(LocalDate.of(2015, 1, 31));

		Monat expected = new Monat(LocalDate.of(2015, 1, 1), LocalDate.of(2015, 1, 31));
		assertEquals(expected, monat);
	}

	@Test
	public void testMonatShouldNotSpanMultipleYears() {
		//noinspection ResultOfObjectAllocationIgnored
		assertThrows(
				IllegalArgumentException.class,
				() -> new Monat(LocalDate.of(2010, 1, 1), LocalDate.of(2015, 1, 31))
		);
	}
}
