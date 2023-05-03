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

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Result of PersonalienSuche
 */
@Getter
@Setter
@NoArgsConstructor
public class RegistrierungSearchResponseWithRegnummerJax {

	@NonNull
	private String regNummer;

	@NonNull
	private String name;

	@NonNull
	private String vorname;

	/**
	 * Dieser Konstruktor soll fuer Callcenter-User verwendet werden, wenn sie mithilfe der UVCI-Nummer gesucht haben.
	 * Deshalb duerfen sie die regNummer erfahren, nicht wie in PersonalienJax
	 */
	public RegistrierungSearchResponseWithRegnummerJax(@NonNull Registrierung registrierung) {
		this.regNummer = registrierung.getRegistrierungsnummer();
		this.name = registrierung.getName();
		this.vorname = registrierung.getVorname();
	}
}
