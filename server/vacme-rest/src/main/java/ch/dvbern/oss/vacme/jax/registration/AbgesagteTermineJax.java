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

import ch.dvbern.oss.vacme.entities.registration.AbgesagteTermine;
import ch.dvbern.oss.vacme.jax.OrtDerImpfungDisplayNameJax;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@AllArgsConstructor
public class AbgesagteTermineJax {

	@NonNull
	private OrtDerImpfungDisplayNameJax ortDerImpfung;

	@Nullable
	private LocalDateTime termin1;

	@Nullable
	private LocalDateTime termin2;

	@Nullable
	private LocalDateTime terminn;

	public static AbgesagteTermineJax of(@NonNull AbgesagteTermine abgesagteTermine) {
		return new AbgesagteTermineJax(
			new OrtDerImpfungDisplayNameJax(abgesagteTermine.getOrtDerImpfung()),
			abgesagteTermine.getTermin1(),
			abgesagteTermine.getTermin2(),
			abgesagteTermine.getTerminN()
		);
	}
}
