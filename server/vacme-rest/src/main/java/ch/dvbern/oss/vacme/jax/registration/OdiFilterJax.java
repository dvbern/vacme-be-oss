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

import java.math.BigDecimal;

import ch.dvbern.oss.vacme.entities.terminbuchung.OdiFilter;
import ch.dvbern.oss.vacme.entities.terminbuchung.OdiFilterTyp;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * DTO to transfer data of a filter that restricts who can see an OdI
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class OdiFilterJax {
	@NonNull
	private OdiFilterTyp typ;

	@Nullable
	private BigDecimal minimalWert;

	@Nullable
	private BigDecimal maximalWert;

	@Nullable
	private String stringArgument;

	public OdiFilterJax(@NonNull OdiFilter odiFilter) {
		this.typ = odiFilter.getTyp();
		this.minimalWert = odiFilter.getMinimalWert();
		this.maximalWert = odiFilter.getMaximalWert();
		this.stringArgument = odiFilter.getStringArgument();
	}

	public OdiFilter toEntity() {
		OdiFilter filter = new OdiFilter();
		filter.setTyp(typ);
		filter.setMinimalWert(minimalWert);
		filter.setMaximalWert(maximalWert);
		filter.setStringArgument(stringArgument);
		return filter;
	}
}
