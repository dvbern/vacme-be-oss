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

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImpfkontrolleTerminJax {

	@Nullable
	private String bemerkung;

	@NotNull
	private boolean identitaetGeprueft = false;

	@Nullable
	private Boolean selbstzahlende;


	@NonNull
	public static ImpfkontrolleTerminJax from(@Nullable ImpfungkontrolleTermin entity) {
		if (entity == null) {
			return new ImpfkontrolleTerminJax();
		}
		return new ImpfkontrolleTerminJax(
			entity.getBemerkung(),
			entity.isIdentitaetGeprueft(),
			entity.getSelbstzahlende()
		);
	}

	public void apply(@Nullable ImpfungkontrolleTermin entity) {
		if (entity == null) {
			entity = new ImpfungkontrolleTermin();
		}
		entity.setTimestampKontrolle(LocalDateTime.now());
		entity.setBemerkung(bemerkung);
		entity.setIdentitaetGeprueft(identitaetGeprueft);
		entity.setSelbstzahlende(selbstzahlende);
	}
}
