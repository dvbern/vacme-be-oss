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

package ch.dvbern.oss.vacme.util;

import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
class ImpfterminOffsetWuerfelTest {


	@Test
	void wuerfleOffsetIfHighCapacityAndDeterministicAtLowCapacity() {
		final ImpfterminOffsetWuerfel wuerfel = new ImpfterminOffsetWuerfel(30, 3, 20, true);
		final Impftermin terminWithContinuousOffset = createImpftermin(20, 0, 0);
		for (int i = 0; i < 50; i++) {
			long gebucht = i;
			wuerfel.wuerfleOffset(terminWithContinuousOffset, (test) -> gebucht);
			Assertions.assertEquals(
				(i % 3) * 10,
				terminWithContinuousOffset.getOffsetInMinutes(),
				"Max. Anzahl Termine nicht erreicht, deterministischer Offset erwartet");
		}
		final Impftermin terminMitOffset = createImpftermin(21,0,0);
		for (int i = 0; i < 50; i++) {
			wuerfel.wuerfleOffset(terminMitOffset, (test) -> {
				throw new IllegalStateException("This should never be called for slot Size < 21");
			});
			System.out.println(terminMitOffset.getOffsetInMinutes());
		}
	}

	@Test
	void wuerfleOffsetIfHighCapacityAndNoOffsetIfLow() {
		 final ImpfterminOffsetWuerfel wuerfel = new ImpfterminOffsetWuerfel(30, 3, 20, false);
		final Impftermin terminWithContinuousOffset = createImpftermin(20, 0, 0);
		for (int i = 0; i < 50; i++) {
			long gebucht = i;
			wuerfel.wuerfleOffset(terminWithContinuousOffset, (test) -> gebucht);
			Assertions.assertEquals(
				0,
				terminWithContinuousOffset.getOffsetInMinutes(),
				"Max. Anzahl Termine nicht erreicht, 0 Offset erwartet");
		}
		final Impftermin terminMitOffset = createImpftermin(21,0,0);
		for (int i = 0; i < 50; i++) {
			wuerfel.wuerfleOffset(terminMitOffset, (test) -> {
				throw new IllegalStateException("This should never be called for slot Size < 21");
			});
			System.out.println(terminMitOffset.getOffsetInMinutes());
		}
	}

	@Test
	void wuerfleOffsetIfHighCapacityAndNoOffsetIfLowMixedSlots() {
		final ImpfterminOffsetWuerfel wuerfel = new ImpfterminOffsetWuerfel(30, 3, 20, false);
		final Impftermin terminWithContinuousOffset = createImpftermin(8, 8, 4);
		for (int i = 0; i < 50; i++) {
			long gebucht = i;
			wuerfel.wuerfleOffset(terminWithContinuousOffset, (test) -> gebucht);
			Assertions.assertEquals(
				0,
				terminWithContinuousOffset.getOffsetInMinutes(),
				"Max. Anzahl Termine nicht erreicht, 0 Offset erwartet");
		}
	}

	@NonNull
	private Impftermin createImpftermin(int kapazitaetOfSlotErsteImpfung,
		int kapazitaetOfSlotZweiteImpfung,
		int kapazitaetOfSlotBoosterImpfung
	) {
		Impfslot slot = new Impfslot();
		slot.setKapazitaetErsteImpfung(kapazitaetOfSlotErsteImpfung);
		slot.setKapazitaetZweiteImpfung(kapazitaetOfSlotZweiteImpfung);
		slot.setKapazitaetBoosterImpfung(kapazitaetOfSlotBoosterImpfung);
		Impftermin termin = new Impftermin();
		termin.setImpfslot(slot);
		return termin;
	}
}
