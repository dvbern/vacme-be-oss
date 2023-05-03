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

import ch.dvbern.oss.vacme.entities.impfen.AuslandArt;
import ch.dvbern.oss.vacme.entities.impfen.Krankenkasse;
import ch.dvbern.oss.vacme.entities.registration.*;
import ch.dvbern.oss.vacme.jax.base.AdresseJax;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRegistrierungJax {

	@Nullable
	private String language;

	@NonNull
	private Geschlecht geschlecht;

	@NonNull
	private String name;

	@NonNull
	private String vorname;

	@NonNull
	private LocalDate geburtsdatum;

	@NonNull
	private AdresseJax adresse;

	@Nullable
	private String mail;

	@Nullable
	private String telefon;

	@Nullable
	private String bemerkung;

	@NonNull
	private Krankenkasse krankenkasse;

	@NonNull
	private String krankenkasseKartenNr;

	@Nullable
	private AuslandArt auslandArt;

	private boolean abgleichElektronischerImpfausweis;

	private boolean contactTracing;

	private boolean immobil;

	@NonNull
	private ChronischeKrankheiten chronischeKrankheiten;

	@NonNull
	private BeruflicheTaetigkeit beruflicheTaetigkeit;

	@NonNull
	private Lebensumstaende lebensumstaende;

	@NonNull
	private AmpelColor ampelColor;

	private Boolean schutzstatus;


	public Fragebogen toEntity() {
		Registrierung registrierung = new Registrierung();
		if (StringUtils.isNotEmpty(language)) {
			registrierung.setSprache(Sprache.from(language));
		}
		registrierung.setGeschlecht(geschlecht);
		registrierung.setName(name);
		registrierung.setVorname(vorname);
		registrierung.setGeburtsdatum(geburtsdatum);
		registrierung.setAdresse(adresse.toEntity());
		registrierung.setMail(mail);
		registrierung.setTelefon(telefon);
		registrierung.setBemerkung(bemerkung);
		registrierung.setKrankenkasse(krankenkasse);
		registrierung.setKrankenkasseKartenNrAndArchive(krankenkasseKartenNr);
		registrierung.setAuslandArt(auslandArt);
		registrierung.setImmobil(immobil);
		registrierung.setAbgleichElektronischerImpfausweis(abgleichElektronischerImpfausweis);
		registrierung.setContactTracing(contactTracing);
		registrierung.setSchutzstatus(schutzstatus);

		Fragebogen fragebogen = new Fragebogen();
		fragebogen.setBeruflicheTaetigkeit(beruflicheTaetigkeit);
		fragebogen.setLebensumstaende(lebensumstaende);
		fragebogen.setChronischeKrankheiten(chronischeKrankheiten);
		fragebogen.setAmpel(ampelColor);

		fragebogen.setRegistrierung(registrierung);
		return fragebogen;
	}
}
