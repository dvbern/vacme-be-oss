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

import ch.dvbern.oss.vacme.entities.impfen.AuslandArt;
import ch.dvbern.oss.vacme.entities.impfen.Krankenkasse;
import ch.dvbern.oss.vacme.entities.registration.BeruflicheTaetigkeit;
import ch.dvbern.oss.vacme.entities.registration.ChronischeKrankheiten;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Lebensumstaende;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.jax.base.AdresseJax;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor
public class SelfserviceEditJax {

	// ADRESSE
	@NonNull
	private AdresseJax adresse;

	// KRANKENKASSENNUMMER
	@NonNull
	private Krankenkasse krankenkasse;

	@NonNull
	private String krankenkasseKartenNr;

	@Nullable
	private AuslandArt auslandArt;

	// FRAGEBOGEN
	@Nullable
	private ChronischeKrankheiten chronischeKrankheiten;

	@Nullable
	private BeruflicheTaetigkeit beruflicheTaetigkeit;

	@Nullable
	private Lebensumstaende lebensumstaende;

	//REGISTRIERUNG
	@Nullable
	private String bemerkung;

	private Boolean keinKontakt;

	@Nullable
	private LocalDateTime timestampInfoUpdate;

	@Nullable
	private LocalDateTime registrationTimestamp;

	public SelfserviceEditJax(@NonNull Registrierung registrierung, @NonNull Fragebogen fragebogen) {
		this.adresse = AdresseJax.from(registrierung.getAdresse());

		this.krankenkasse = registrierung.getKrankenkasse();
		this.krankenkasseKartenNr = registrierung.getKrankenkasseKartenNr();
		this.auslandArt = registrierung.getAuslandArt();

		this.bemerkung = registrierung.getBemerkung();

		this.keinKontakt = registrierung.getKeinKontakt();

		this.chronischeKrankheiten = fragebogen.getChronischeKrankheiten();
		this.beruflicheTaetigkeit = fragebogen.getBeruflicheTaetigkeit();
		this.lebensumstaende = fragebogen.getLebensumstaende();

		this.timestampInfoUpdate = registrierung.getTimestampInfoUpdate();
		this.registrationTimestamp = registrierung.getRegistrationTimestamp();
	}

}
