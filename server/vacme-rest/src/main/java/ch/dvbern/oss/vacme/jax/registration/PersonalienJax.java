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
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.jax.base.AdresseJax;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Data from a given registrierung, it should not be possible to trace back
 * the registrierungsNummer and other data using this data for privacy reasons
 * except for users having the right role  (KT-NACHDOKUMENTATION)
 */
@Getter
@Setter
@NoArgsConstructor
public class PersonalienJax {

	@NonNull
	private String name;

	@NonNull
	private String vorname;

	@NonNull
	private LocalDate geburtsdatum;

	@NonNull
	private AdresseJax adresse;

	@NonNull
	private UUID registrierungId;

	@Nullable
	private String registrierungNummer;

	private boolean abgleichElektronischerImpfausweis;

	private boolean vollstaendigerImpfschutz;

	@NonNull
	private CurrentZertifikatInfo currentZertifikatInfo = new CurrentZertifikatInfo();

	private @Nullable LocalDateTime timestampLetzterPostversand;

	/**
	 * Dieser Konstruktor soll fuer Callcenter-User verwendet werden. Die Registrierungsnummer
	 * wird hier explizit nicht gemappt, damit keine Verbindung zwischen den Personalien und
	 * der Registrierungsnummer hergestellt werden kann.
	 * @param registrierung
	 */
	public PersonalienJax(@NonNull Registrierung registrierung) {
		this.name = registrierung.getName();
		this.vorname = registrierung.getVorname();
		this.geburtsdatum= registrierung.getGeburtsdatum();
		this.adresse = AdresseJax.from(registrierung.getAdresse());
		this.registrierungId = registrierung.getId();
		// Achtung, hier darf die Registrierungsnummer nicht gesetzt werden!
		this.registrierungNummer = null;
		this.abgleichElektronischerImpfausweis = registrierung.isAbgleichElektronischerImpfausweis();
	}

	public PersonalienJax(
		@NonNull Registrierung registrierung,
		@Nullable Boolean hasZertifikat,
		@Nullable Boolean deservesZertifikat,
		@Nullable LocalDateTime timestampLetzterPostversand,
		@Nullable Boolean vollstaendigerImpfschutz
	) {
		this(registrierung);
		this.currentZertifikatInfo.setHasCovidZertifikat(Boolean.TRUE.equals(hasZertifikat));
		this.currentZertifikatInfo.setDeservesZertifikat(Boolean.TRUE.equals(deservesZertifikat));
		this.timestampLetzterPostversand = timestampLetzterPostversand;
		this.vollstaendigerImpfschutz = Boolean.TRUE.equals(vollstaendigerImpfschutz);
	}

	/**
	 * Diese Methode erlaubt die Verknuepfung von Registrierungsnummer und Personalien.
	 * Dies sind sentitive Informationen und daher darf dies nur durch Kantonsbenutzer verwendet werden!
	 * @param registrierung the data to be passed
	 * @return the jax containing the data
	 */
	public static PersonalienJax createWithRegNumber(@NonNull Registrierung registrierung) {
		PersonalienJax jax = new PersonalienJax(registrierung, null, null, null, null);
		jax.setRegistrierungNummer(registrierung.getRegistrierungsnummer());
		return jax;
	}
}
