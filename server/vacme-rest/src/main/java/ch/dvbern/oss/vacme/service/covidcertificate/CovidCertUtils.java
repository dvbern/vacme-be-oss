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

package ch.dvbern.oss.vacme.service.covidcertificate;

import ch.dvbern.oss.vacme.entities.base.ZertifikatInfo;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public final class CovidCertUtils {

	private CovidCertUtils() {
		// util
	}

	@NotNull
	public static String normalizeJson(@NotNull String payload) {
		return payload.replaceAll("[\\n\\r\\t ]", "");
	}

	@NonNull
	public static Pair<Integer, Integer> calculateZahlVorUndNachSchraegstrich(
		@NonNull ImpfinformationDto infos,
		@NonNull Registrierung registrierung,
		Impfung impfung
	) {
		int countAnerkannteImpfungen = CovidCertUtils.getZahlVorDemSchraegstrich(infos, impfung);
		int zahlNachSchraegstrich = CovidCertUtils.calculateZahlNachSchraegstrich(infos, registrierung, countAnerkannteImpfungen);

		return Pair.of(countAnerkannteImpfungen, zahlNachSchraegstrich);
	}

	/**
	 * Logik der Zahl nach dem Schraegstrich: Siehe VACME-1748 fuer die neuen Regeln ab
	 * Januar 2022, die der EU-Logik entsprechen sollen
	 */
	public static int calculateZahlNachSchraegstrich(
		@NonNull ImpfinformationDto infos,
		@NonNull Registrierung registrierung,
		int impffolgeNr
	) {
		// Ab der 2. Impfung muessen wir pruefen ob die 2. Impfung bereits eine Boosterimpfung war. Wenn ja steht hinten eine 1 (x/1)
		if (secondImpfungWasBooster(infos)) {
			return 1;
		}

		if (infos.getImpfdossier().abgeschlossenMitVollstaendigemImpfschutz()) {
			// abgeschlossen: 2/2, 3/3, 4/4 etc.
			return impffolgeNr;
		} else {
			if (infos.getImpfung1() == null) {
				throw AppValidationMessage.ILLEGAL_STATE.create("nicht abgeschlossen und keine Erstimpfung -> kann kein Zertifikat generieren");
			}

			// nicht abgeschlossen: 1/2
			return infos.getImpfung1().getImpfstoff().getAnzahlDosenBenoetigt();
		}
	}

	private static boolean secondImpfungWasBooster(@NonNull ImpfinformationDto infos) {
		// Falls die Person ihre zweite Impfung in Vacme vorgenommen hat und dies bereits ein Booster war:
		// Wir wollen immer ein X/1 Zertifikat erstellen.
		// Externe Zertifikat gelten immer als Grundimmunisierung!
		// Non-WHO-Impfungen zaehlen dabei nicht.
		Integer nonWhoImpfungenCount = countNonWhoImpfungenAndNichtZugelassene(infos.getExternesZertifikat());
		Integer zweiteImpfungNummerOhneNonWho = 2 + nonWhoImpfungenCount;
		Impfung secondImpfung = ImpfinformationenService.readImpfungForImpffolgeNr(infos, Impffolge.BOOSTER_IMPFUNG, zweiteImpfungNummerOhneNonWho);
		return secondImpfung != null && !secondImpfung.isGrundimmunisierung();
	}

	@NonNull
	public static String readImpfungCounterString(@NonNull ZertifikatInfo zertifikatInfo) {
		Validate.notNull(zertifikatInfo.getNumberOfDoses(), "NumberOfDoses muss gesetzt sein");
		Validate.notNull(zertifikatInfo.getTotalNumberOfDoses(), "TotalNumberOfDoses muss gesetzt sein");
		int zahlVorSchraegstrich = zertifikatInfo.getNumberOfDoses();
		int zahlNachSchraegstrich = zertifikatInfo.getTotalNumberOfDoses();
		return zahlVorSchraegstrich + "/" + zahlNachSchraegstrich;
	}

	// Anzahl gemachte Impfungen, aber Non-WHO-Impfstoffe zaehlen nicht
	@NonNull
	public static Integer getZahlVorDemSchraegstrich(@NonNull ImpfinformationDto infos, @NonNull Impfung impfung) {
		Integer originalZahl = ImpfinformationenService.getImpffolgeNr(infos, impfung);
		Integer nonWhoImpfungen = countNonWhoImpfungenAndNichtZugelassene(infos.getExternesZertifikat());
		return originalZahl - nonWhoImpfungen;
	}

	@NonNull
	public static Integer countNonWhoImpfungenAndNichtZugelassene(@Nullable ExternesZertifikat externesZertifikat) {
		if (externesZertifikat == null) {
			return 0;
		}
		if (externesZertifikat.getImpfstoff().getZulassungsStatus() == ZulassungsStatus.NICHT_ZUGELASSEN
			|| externesZertifikat.getImpfstoff().getZulassungsStatus() == ZulassungsStatus.NICHT_WHO_ZUGELASSEN) {
			return externesZertifikat.getAnzahlImpfungen();
		}
		return 0;
	}
}
