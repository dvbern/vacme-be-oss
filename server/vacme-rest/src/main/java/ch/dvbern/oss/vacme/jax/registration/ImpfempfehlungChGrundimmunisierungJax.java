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

package ch.dvbern.oss.vacme.jax.registration;

import java.util.UUID;

import ch.dvbern.oss.vacme.entities.impfen.ImpfempfehlungChGrundimmunisierung;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ImpfempfehlungChGrundimmunisierungJax {

	@Nullable
	private UUID id;

	@NonNull
	private int anzahlVerabreicht;

	@NonNull
	private int notwendigFuerChGrundimmunisierung;

	public static ImpfempfehlungChGrundimmunisierungJax from(@NonNull ImpfempfehlungChGrundimmunisierung entity) {
		return new ImpfempfehlungChGrundimmunisierungJax(
			entity.getId(),
			entity.getAnzahlVerabreicht(),
			entity.getNotwendigFuerChGrundimmunisierung());
	}

	public ImpfempfehlungChGrundimmunisierung applyTo(@NonNull ImpfempfehlungChGrundimmunisierung impfempfehlungChGrundimmunisierung) {
		impfempfehlungChGrundimmunisierung.setAnzahlVerabreicht(anzahlVerabreicht);
		impfempfehlungChGrundimmunisierung.setNotwendigFuerChGrundimmunisierung(notwendigFuerChGrundimmunisierung);
		return impfempfehlungChGrundimmunisierung;
	}
}
