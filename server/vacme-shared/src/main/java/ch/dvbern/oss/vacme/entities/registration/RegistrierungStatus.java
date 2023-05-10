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

package ch.dvbern.oss.vacme.entities.registration;

import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.NEU;

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
public enum RegistrierungStatus {

	REGISTRIERT,				      	// Registierung okay (email)
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
	KONTROLLIERT_BOOSTER,
	SEE_DOSSIERSTATUS;

	@NonNull
	public static RegistrierungStatus toRegistrierungStatus(@NonNull ImpfdossierStatus status) {
		if (status == NEU) {
			return RegistrierungStatus.REGISTRIERT;
		}
		return RegistrierungStatus.valueOf(status.name());
	}
}
