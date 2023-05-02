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

package ch.dvbern.oss.vacme.enums;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Alle die bis ins Backend kommen, duerfen gemaess Ampel impfen.
 * Der Hausarzt-Check laeuft (zumindest in Phase 1) ausserhalb.
 */
@Schema(enumeration = {
	"REGISTRIERT", "FREIGEGEBEN", "ODI_GEWAEHLT", "GEBUCHT", "IMPFUNG_1_KONTROLLIERT", "IMPFUNG_1_DURCHGEFUEHRT",
	"IMPFUNG_2_KONTROLLIERT", "IMPFUNG_2_DURCHGEFUEHRT", "ABGESCHLOSSEN",
	"AUTOMATISCH_ABGESCHLOSSEN", "ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG", "IMMUNISIERT",
	"FREIGEGEBEN_BOOSTER", "ODI_GEWAEHLT_BOOSTER", "GEBUCHT_BOOSTER", "KONTROLLIERT_BOOSTER",
})
public enum ImpfdossierStatus {

	NOCH_NICHT_MIGRIERT,
	NEU,
	FREIGEGEBEN,						// Gemaess meiner Einstufung freigegeben (Prioritaet)
	ODI_GEWAEHLT,						// ODI gewaehlt (bei ODI ohne Termine)
	GEBUCHT,
	IMPFUNG_1_KONTROLLIERT,
	IMPFUNG_1_DURCHGEFUEHRT,
	IMPFUNG_2_KONTROLLIERT,
	IMPFUNG_2_DURCHGEFUEHRT,
	ABGESCHLOSSEN,						// Alle fuer meinen Impfstoff benoetigten Impfungen sind abgeschlossen
	AUTOMATISCH_ABGESCHLOSSEN,			// Nach einer gewissen Zeitspanne (e.g. 100d) wird eine Reg die nur die 1. Impfung gemacht hat automatisch abgeschlossen
	ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG, //  Es kann Gruende geben warum eine 2. Impfung nicht gemacht wird
	IMMUNISIERT,
	FREIGEGEBEN_BOOSTER,
	ODI_GEWAEHLT_BOOSTER,
	GEBUCHT_BOOSTER,
	KONTROLLIERT_BOOSTER;

	/**
	 *
	 * @return Set mit Status bei denen es moeglich ist ein Zertifikat zu haben
	 */
	@JsonIgnore
	@NonNull
	public static Set<ImpfdossierStatus> getStatusWithPossibleZertifikat(){
		return Set.of(ABGESCHLOSSEN, ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
			IMMUNISIERT, FREIGEGEBEN_BOOSTER, ODI_GEWAEHLT_BOOSTER, GEBUCHT_BOOSTER, KONTROLLIERT_BOOSTER);
	}

	@JsonIgnore
	@NonNull
	public static Set<ImpfdossierStatus> getMindestensGrundimmunisiertOrAbgeschlossen(){
		return Set.of(ABGESCHLOSSEN, AUTOMATISCH_ABGESCHLOSSEN, ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
			IMMUNISIERT, FREIGEGEBEN_BOOSTER, ODI_GEWAEHLT_BOOSTER, GEBUCHT_BOOSTER, KONTROLLIERT_BOOSTER);
	}

	@JsonIgnore
	@NonNull
	public static Set<ImpfdossierStatus> getAnyStatusOfGrundimmunisiert() {
		return Set.of(IMMUNISIERT, FREIGEGEBEN_BOOSTER, ODI_GEWAEHLT_BOOSTER, GEBUCHT_BOOSTER, KONTROLLIERT_BOOSTER);
	}

	@JsonIgnore
	@NonNull
	public static Set<ImpfdossierStatus> isErsteImpfungDoneAndZweitePending() {
		return Set.of(IMPFUNG_1_DURCHGEFUEHRT, IMPFUNG_2_KONTROLLIERT);
	}

	public static Set<ImpfdossierStatus> getImpfung1Or2Exclusive() {
		return Set.of(
			FREIGEGEBEN,
			ODI_GEWAEHLT,
			GEBUCHT,
			IMPFUNG_1_KONTROLLIERT,
			IMPFUNG_1_DURCHGEFUEHRT,
			IMPFUNG_2_KONTROLLIERT,
			IMPFUNG_2_DURCHGEFUEHRT,
			ABGESCHLOSSEN,
			AUTOMATISCH_ABGESCHLOSSEN,
			ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG);
	}
}
