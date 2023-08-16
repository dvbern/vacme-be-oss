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

package ch.dvbern.oss.vacme.jax.stats;

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
public class ImpfzentrumTagesReportDetailAusstehendEntryJax {

	@NonNull
	private String registrierungsnummer;

	@NonNull
	private String anzeige;

	public static ImpfzentrumTagesReportDetailAusstehendEntryJax from(@NonNull Registrierung registrierung) {
		return new ImpfzentrumTagesReportDetailAusstehendEntryJax(
			registrierung.getRegistrierungsnummer(),
			toDescription(registrierung));
	}

	/**
	 * Creates a Description that contains only the initials and the birthyear of a person for privacy reasons
	 * @param registrierung to create desciption from
	 * @return description string
	 */
	private static String toDescription(@NonNull Registrierung registrierung) {
		return registrierung.getRegistrierungsnummer() + " ("
			+ registrierung.getVorname().substring(0, 1) + ". "
			+ registrierung.getName().substring(0, 1) + "., "
			+ registrierung.getGeburtsdatum().getYear()  + ")";
	}

}
