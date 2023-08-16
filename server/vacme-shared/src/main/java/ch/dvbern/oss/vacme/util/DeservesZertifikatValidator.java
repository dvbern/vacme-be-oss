/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import java.util.List;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.validators.CheckRegistrierungVollstaendigerImpfschutzValidator;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Prueft, ob fuer eine Impfung bzw. fuer irgendeine Impfung einer Registrierung ein
 * Zertifikat moeglich ist.
 */
@Slf4j
public final class DeservesZertifikatValidator {

	private DeservesZertifikatValidator() {
	}

	public static boolean deservesZertifikatForAnyImpfung(@NonNull ImpfinformationDto infos) {
		if (!infos.getKrankheitIdentifier().isSupportsZertifikat()) {
			return false;
		}
		final List<Impfung> allImpfungen =
			ImpfinformationenUtil.getImpfungenOrderedByImpffolgeNr(infos);
		for (Impfung impfung : allImpfungen) {
			if (deservesZertifikat(infos, impfung)) {
				return true;
			}
		}
		return false;
	}

	public static void deservesZertifikatOrThrowException(@NonNull ImpfinformationDto infos, @Nullable Impfung impfung) {
		if (!deservesAndWantsZertifikat(infos, impfung)) {
			throw AppValidationMessage.REGISTRIERUNG_CANNOT_GENERATE_ZERTIFIKAT.create();
		}
	}

	public static boolean deservesZertifikat(
		@NonNull ImpfinformationDto infos,
		@Nullable Impfung impfung
	) {
		if (!infos.getKrankheitIdentifier().isSupportsZertifikat()) {
			return false;
		}
		ExternesZertifikat externesZertifikat = infos.getExternesZertifikat();
		// Validierung: Die Impfung darf nicht null sein
		if (impfung == null) {
			return false;
		}

		// Nochmals validieren, dass die Reg in einem richtigen Status ist
		final boolean valid = CheckRegistrierungVollstaendigerImpfschutzValidator.isValid(infos, impfung);
		if (!valid) {
			LOG.error("VACME-ZERTIFIKAT: Registration {} did not pass validation. See "
					+ "CheckRegistrierungVollstaendigerImpfschutzValidator",
				infos.getRegistrierung().getRegistrierungsnummer());
			return false;
		}

		// Validierung: der Impfstoff der letzten Impfung muss in der Schweiz zugelassen sein (sollte vorher auch
		// validiert worden sein!)
		if (ZulassungsStatus.ZUGELASSEN != impfung.getImpfstoff().getZulassungsStatus()
				&& ZulassungsStatus.EMPFOHLEN != impfung.getImpfstoff().getZulassungsStatus()) {
			return false;
		}

		// Bei Impfung 1 und 2 muss es sich um einen VACME-Abschluss handeln. Fuer externe Zertifikate wird
		// kein Zertifikat ausgestellt
		if (impfung.getTermin().getImpffolge() != Impffolge.BOOSTER_IMPFUNG
			&& infos.getImpfdossier().getVollstaendigerImpfschutzTyp() != null
			&& infos.getImpfdossier().getVollstaendigerImpfschutzTyp().isExternesZertifikat()) {
			return false;
		}

		if (impfung.getTermin().getImpffolge() == Impffolge.ERSTE_IMPFUNG) {
			// Bei erster Impfung plus Corona: Zertifikat nur erhaeltlich, wenn die Impfung NACH Corona war
			if (infos.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().isGenesen()) {
				if (ImpfinformationenUtil.isRelevantImpfungBeforeCoronaTest(infos.getImpfdossier(),
					impfung.getTimestampImpfung())) {
					return false;
				}
			} else {
				if (externesZertifikat != null) {
					boolean needsOnlyOneImpfung =
						ImpfinformationenUtil.willBeGrundimmunisiertAfterErstimpfungImpfstoff(impfung.getImpfstoff(),
						externesZertifikat);
					if(!needsOnlyOneImpfung){
						return false;
					}
				} else {
					// Wenn es kein externes Zertifikat gibt kommt es nur auf die Anz Dosen des 1. Impfst an
					if (impfung.getImpfstoff().getAnzahlDosenBenoetigt() != 1) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static boolean deservesAndWantsZertifikat(
		@NonNull ImpfinformationDto infos,
		@Nullable Impfung impfung
	) {
		if (!infos.getKrankheitIdentifier().isSupportsZertifikat()) {
			return false;
		}
		Registrierung registrierung =  infos.getRegistrierung();
		if (!deservesZertifikat(infos, impfung)) {
			return false;
		}
		// Elektronischer Abgleich (sollte gar nicht passieren!)
		if (!registrierung.isAbgleichElektronischerImpfausweis()) {
			return false;
		}
		return true;
	}
}
