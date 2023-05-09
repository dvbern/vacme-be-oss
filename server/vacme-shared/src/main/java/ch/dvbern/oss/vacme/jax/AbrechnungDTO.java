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

package ch.dvbern.oss.vacme.jax;

import ch.dvbern.oss.vacme.entities.impfen.Krankenkasse;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * DTO fuer das Lesen der Daten von der DB fuer den Abrechnung Report
 */
@Getter
@Setter
@NoArgsConstructor
public class AbrechnungDTO {

	private @Nullable OrtDerImpfung ortDerImpfung;
	private @Nullable Krankenkasse krankenkasse;
	private @Nullable Long krankenkassenCount;

	@QueryProjection
	public AbrechnungDTO(
		@Nullable OrtDerImpfung ortDerImpfung,
		@Nullable Krankenkasse krankenkasse,
		@Nullable Long count
	) {
		this.ortDerImpfung = ortDerImpfung;
		this.krankenkasse = krankenkasse;
		this.krankenkassenCount = count;
	}
}
