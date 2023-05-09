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

package ch.dvbern.oss.vacme.util;

import java.time.LocalDate;
import java.time.Period;

import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class PrioritaetUtil {

	// Chronische Krankheiten
	private static final int SCHWERE_KRANKHEITSVERLAEUFE_WERT = 500000;
	private static final int KRANKHEIT_WERT = 50000;
	private static final int KEINE_WERT = 0;
	private static final int ANDERE_WERT = 0;
	// Beruf
	private static final int GES_PERSONAL_MIT_PAT_KONTAKT_INTENSIV_WERT = 20000;
	private static final int GES_PERSONAL_MIT_PAT_KONTAKT_WERT = 5000;
	private static final int GES_PERSONAL_OHNE_PAT_KONTAKT_WERT = 50;
	private static final int BERUF_MIT_KUNDENKONTAKT_WERT = 0;
	private static final int BERUF_MIT_AKT_IN_GRO_ARB_GRUP_WERT = 0;
	private static final int BERUF_MIT_HOMEOFFICE_MOEGLICH_WERT = 0;
	private static final int BETREUUNG_VON_GEFAERD_PERSON_WERT = 5000;
	private static final int PERSONAL_IN_GEMEINSCHAFTSEIN_WERT = 50;
	private static final int NICHT_ERWERBSTAETIG_WERT = 0;
	private static final int BERUF_ANDERE_WERT = 0;
	// Lebensumstaende
	private static final int MIT_BESONDERS_GEFAEHRDETEN_PERSON_WERT = 500;
	private static final int EINZELHAUSHALT_WERT = 0;
	private static final int FAMILIENHAUSHALT_WERT = 0;
	private static final int GEMEINSCHAFTEN_WERT = 50;
	private static final int MASSENUNTERKUENFTEN_WERT = 50;
	private static final int LEBENSUMSTAENDE_ANDERE_WERT = 0;

	private PrioritaetUtil() {
	}

	@NonNull
	public static Prioritaet calculatePrioritaet(@NonNull Fragebogen fragebogen) {
		Registrierung registrierung = fragebogen.getRegistrierung();
		if (fragebogen.getRegistrierung().getRegistrierungsEingang() == RegistrierungsEingang.ORT_DER_IMPFUNG) {
			return Prioritaet.Z;
		}

		int alter = Period.between(registrierung.getGeburtsdatum(), LocalDate.now()).getYears();
		int punkten = calculatePrioritaetPunkten(fragebogen);
		// Priorisierung
		return Prioritaet.getPrioritaet(punkten, alter);
	}

	public static boolean hasMorePrioritaetPunktThan(int comparativePunkten, @NonNull Fragebogen fragebogen) {
		return calculatePrioritaetPunkten(fragebogen) >= comparativePunkten;
	}

	public static int calculatePrioritaetPunkten(@NonNull Fragebogen fragebogen) {
		int summe = 0;
		// Chronische Krankheiten
		summe += getChronischeKrankeitenSumme(fragebogen);
		// Beruf
		summe += getBerufSumme(fragebogen);
		// Lebensumstaende
		summe += getLebensumstaendeSumme(fragebogen);
		return summe;
	}

	private static int getChronischeKrankeitenSumme(Fragebogen fragebogen) {
		switch (fragebogen.getChronischeKrankheiten()) {
			case SCHWERE_KRANKHEITSVERLAEUFE:
				return SCHWERE_KRANKHEITSVERLAEUFE_WERT;
			case KRANKHEIT:
				return KRANKHEIT_WERT;
			case KEINE:
				return KEINE_WERT;
			default:
				return ANDERE_WERT;
		}
	}

	private static int getBerufSumme(@NonNull Fragebogen fragebogen) {
		switch (fragebogen.getBeruflicheTaetigkeit()) {
			case GES_PERSONAL_MIT_PAT_KONTAKT_INTENSIV:
				return GES_PERSONAL_MIT_PAT_KONTAKT_INTENSIV_WERT;
			case GES_PERSONAL_MIT_PAT_KONTAKT:
				return GES_PERSONAL_MIT_PAT_KONTAKT_WERT;
			case GES_PERSONAL_OHNE_PAT_KONTAKT:
				return GES_PERSONAL_OHNE_PAT_KONTAKT_WERT;
			case BERUF_MIT_KUNDENKONTAKT:
				return BERUF_MIT_KUNDENKONTAKT_WERT;
			case BERUF_MIT_AKT_IN_GRO_ARB_GRUP:
				return BERUF_MIT_AKT_IN_GRO_ARB_GRUP_WERT;
			case BERUF_MIT_HOMEOFFICE_MOEGLICH:
				return BERUF_MIT_HOMEOFFICE_MOEGLICH_WERT;
			case BETREUUNG_VON_GEFAERD_PERSON:
				return BETREUUNG_VON_GEFAERD_PERSON_WERT;
			case PERSONAL_IN_GEMEINSCHAFTSEIN:
				return PERSONAL_IN_GEMEINSCHAFTSEIN_WERT;
			case NICHT_ERWERBSTAETIG:
				return NICHT_ERWERBSTAETIG_WERT;
			case ANDERE:
			default:
				return BERUF_ANDERE_WERT;
		}
	}

	private static int getLebensumstaendeSumme(@NonNull Fragebogen fragebogen) {
		switch (fragebogen.getLebensumstaende()) {
			case MIT_BESONDERS_GEFAEHRDETEN_PERSON:
				return MIT_BESONDERS_GEFAEHRDETEN_PERSON_WERT;
			case EINZELHAUSHALT:
				return EINZELHAUSHALT_WERT;
			case FAMILIENHAUSHALT:
				return FAMILIENHAUSHALT_WERT;
			case GEMEINSCHAFTEN:
				return GEMEINSCHAFTEN_WERT;
			case MASSENUNTERKUENFTEN:
				return MASSENUNTERKUENFTEN_WERT;
			case ANDERE:
			default:
				return LEBENSUMSTAENDE_ANDERE_WERT;
		}
	}
}
