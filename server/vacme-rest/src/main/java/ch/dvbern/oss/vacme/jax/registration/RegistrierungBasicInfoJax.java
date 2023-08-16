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

import java.time.LocalDate;
import java.time.LocalDateTime;

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
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
public class RegistrierungBasicInfoJax {

	@NonNull
	private String registrierungsnummer;

	@NonNull
	private String name;

	@NonNull
	private String vorname;

	@NonNull
	private LocalDate geburtsdatum;

	@Nullable
	private LocalDateTime timestampPhonenumberUpdate;

	@NonNull
	public static RegistrierungBasicInfoJax from(@NonNull Registrierung registrierung) {
		return new RegistrierungBasicInfoJax(
			registrierung.getRegistrierungsnummer(),
			registrierung.getName(),
			registrierung.getVorname(),
			registrierung.getGeburtsdatum(),
			registrierung.getTimestampPhonenumberUpdate()
		);
	}
}
