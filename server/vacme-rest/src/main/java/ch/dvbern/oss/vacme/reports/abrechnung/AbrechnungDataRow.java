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

package ch.dvbern.oss.vacme.reports.abrechnung;

import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigDecimal;

/**
 * DTO fuer den Abrechnung Report
 */
@Getter
@Setter
@NoArgsConstructor
public class AbrechnungDataRow {

	private @Nullable OrtDerImpfung ortDerImpfung;
	private @Nullable String verantwortlicherGln;
	private @Nullable String ovName;
	private @Nullable String ovVorname;
	private @Nullable String ovMail;
	private @Nullable String fvName;
	private @Nullable String fvVorname;
	private @Nullable String fvMail;
	private @Nullable String fvGlnNummer;
	private long krankenkasseAndereCount;
	private long krankenkasseEdaCount;
	private long krankenkasseAuslandCount;
	private long krankenkasseOKPCount;
	private long selbstzahlendeCount;
	private long totalImpfungenCount;
	private BigDecimal totalGewichtetDosen = BigDecimal.ZERO;

}
