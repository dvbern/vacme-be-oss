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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImpfschutzJax {

	@Nullable
	private LocalDateTime immunisiertBis;

	@Nullable
	private LocalDateTime freigegebenNaechsteImpfungAb;

	@Nullable
	private LocalDateTime freigegebenAbSelbstzahler;

	@NonNull
	private List<UUID> erlaubteImpfstoffe = new ArrayList<>();

	public static ImpfschutzJax from(@NonNull Impfschutz impfschutz) {
		ImpfschutzJax impfschutzJax = new ImpfschutzJax();
		impfschutzJax.immunisiertBis = impfschutz.getImmunisiertBis();
		impfschutzJax.freigegebenNaechsteImpfungAb = impfschutz.getFreigegebenNaechsteImpfungAb();
		impfschutzJax.freigegebenAbSelbstzahler = impfschutz.getFreigegebenAbSelbstzahler();
		impfschutzJax.erlaubteImpfstoffe.addAll(impfschutz.getErlaubteImpfstoffeCollection());
		return impfschutzJax;
	}
}
