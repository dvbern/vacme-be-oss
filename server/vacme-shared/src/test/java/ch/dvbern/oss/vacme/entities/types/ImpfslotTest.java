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

package ch.dvbern.oss.vacme.entities.types;

import java.time.LocalDateTime;

import ch.dvbern.oss.vacme.entities.embeddables.DateTimeRange;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImpfslotTest {

	@Test
	public void testSlotFormat() {
		OrtDerImpfung odi = new OrtDerImpfung();
		DateTimeRange slotTime = DateTimeRange.of(LocalDateTime.of(2020, 12, 24, 17, 30),
			LocalDateTime.of(2020, 12, 24, 18, 0));
		Impfslot slot = Impfslot.of(KrankheitIdentifier.COVID, odi, slotTime);

		assertEquals("24.12.2020 17:30", slot.toDateMessage());
	}

	@Test
	public void testSlotFormat2() {
		OrtDerImpfung odi = new OrtDerImpfung();
		DateTimeRange slotTime = DateTimeRange.of(LocalDateTime.of(2020, 3, 26, 8, 0),
			LocalDateTime.of(2021, 12, 19, 17, 40));
		Impfslot slot = Impfslot.of(KrankheitIdentifier.COVID, odi, slotTime);

		assertEquals("26.03.2020 08:00", slot.toDateMessage());
	}
}
