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

import java.util.Random;
import java.util.function.Function;

import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ImpfterminOffsetWuerfel {

	private final boolean slotOffsetDeterministicWhenLowCapacity;
	private int slotOffsetGroups;
	private long slotOffsetMaxTermineToDivide;

	private Random wuerfel = new Random();
	private int minutesPerGroup;

	public ImpfterminOffsetWuerfel(int slotDuration, int slotOffsetGroups, long slotOffsetMaxTermineToDivide, boolean slotOffsetDeterministicWhenLowCapacity) {
		this.slotOffsetGroups = slotOffsetGroups;
		this.slotOffsetMaxTermineToDivide = slotOffsetMaxTermineToDivide;
		if (slotOffsetGroups > 0) {
			this.minutesPerGroup = slotDuration / slotOffsetGroups;
		}
		this.slotOffsetDeterministicWhenLowCapacity = slotOffsetDeterministicWhenLowCapacity;
	}

	public void wuerfleOffset(@NonNull Impftermin termin, Function<Impftermin, Long> countGebuchtFunction) {
		if (needsTerminOffset(termin)) {
			termin.setOffsetInMinutes(wuerfleOffset());
		} else if(slotOffsetDeterministicWhenLowCapacity) {
			long countGebuchteTermine = countGebuchtFunction.apply(termin);
			long mod = (countGebuchteTermine % slotOffsetGroups) * minutesPerGroup;
			termin.setOffsetInMinutes(Math.toIntExact(mod));
		}
	}

	private boolean needsTerminOffset(@NonNull Impftermin termin) {
		return termin.getImpfslot().getKapazitaetTotal() > slotOffsetMaxTermineToDivide;
	}

	private int wuerfleOffset() {
		int group = wuerfel.nextInt(slotOffsetGroups);
		// Group in Minuten umrechnen (Group 2von3 bei 30min-Slots -> 20)
		return group * minutesPerGroup;
	}
}
