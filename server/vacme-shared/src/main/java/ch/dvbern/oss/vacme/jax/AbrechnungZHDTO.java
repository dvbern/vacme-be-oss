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

import java.math.BigDecimal;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.impfen.Krankenkasse;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * DTO fuer das Lesen der Daten von der DB fuer den Abrechnung ZH Report
 */
@Getter
@Setter
@NoArgsConstructor
public class AbrechnungZHDTO {

	private @Nullable Boolean older = null;
	private @Nullable OrtDerImpfung ortDerImpfung;
	private @Nullable Krankenkasse krankenkasse = null;
	private @Nullable Long krankenkassenCount = null;
	private @Nullable UUID impfstoffId = null;
	private @Nullable Boolean grundimmunisierung = null;
	private @Nullable Double menge = null;
	private @Nullable Boolean selbstzahlende = null;

	@QueryProjection
	public AbrechnungZHDTO(
		@Nullable Boolean older,
		@Nullable OrtDerImpfung ortDerImpfung,
		@Nullable Krankenkasse krankenkasse,
		@Nullable Long count,
		@Nullable UUID impfstoffId,
		@Nullable Boolean grundimmunisierung,
		@Nullable BigDecimal menge,
		@Nullable Boolean selbstzahlende
	) {
		this.older = older;
		this.ortDerImpfung = ortDerImpfung;
		this.krankenkasse = krankenkasse;
		this.krankenkassenCount = count;
		this.impfstoffId = impfstoffId;
		this.grundimmunisierung = grundimmunisierung;
		this.menge = menge != null ? menge.doubleValue() : null;
		this.selbstzahlende = selbstzahlende;
	}

	public boolean isYounger() {
		return getOlder() != null && !getOlder().booleanValue();
	}
}
