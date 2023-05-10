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

package ch.dvbern.oss.vacme.shared.errors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static java.util.stream.Collectors.joining;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AppValidationMessage {
	// !!! PLEASE SORT this + translations.properties ALPHABETICALLY !!!
	DELETE_NOT_POSSIBLE_BECAUSE_ZERTIFIKAT(1),
	EXISTING_ORTDERIMPFUNG(1),
	EXISTING_REGISTRIERUNG(1),
	EXISTING_VACME_IMPFUNGEN_CANNOT_ADD_EXTERN_GEIMPFT(0),
	EXISTING_VACME_IMPFUNGEN_CANNOT_EDIT_EXTERN_GEIMPFT(0),
	EXISTING_VACME_IMPFUNGEN_CANNOT_REMOVE_GRUNDIMMUN(0),
	EXISTING_VACME_IMPFUNGEN_CANNOT_REDUCE_EXTERN_NUMBER(0),
	ILLEGAL_STATE(1),
	IMPFFOLGE_NOT_EXISTING(1),
	IMPFFOLGE_NUMBER_NOT_EXISTING(1),
	IMPFTERMINE_FALSCHER_ABSTAND(3),
	IMPFTERMINE_KAPAZITAET(1),
	IMPFTERMIN_BESETZT(1),
	IMPFTERMIN_IMPFSTOFF_NICHT_ZUGELASSEN(1),
	IMPFTERMIN_FALSCHER_ODI(1),
	IMPFTERMIN_FALSCHES_DATUM(1),
	IMPFTERMIN_FALSCHE_IMPFFOLGE(1),
	IMPFTERMIN_IMPFUNG_BEFORE_FIRST(1),
	IMPFTERMIN_IMPFUNG_BEFORE_PREVIOUS(1),
	IMPFTERMIN_IMPFUNG_BETWEEN_OTHERS(0),
	IMPFTERMIN_IMPFUNG_TOO_EARLY(1),
	IMPFTERMIN_KONTROLLE_TOO_EARLY(1),
	IMPFTERMIN_NICHT_GESETZT(0),
	IMPFTERMIN_INVALID_ORDER(2),
	IMPFTERMIN_INVALID_CORRECTION(1),
	IMPFTERMIN_SCHON_WAHRGENOMMEN(1),
	IMPFTERMIN_WITH_EXISTING_IMPFUNG(1),
	IMPFTERMIN_WITH_EXISTING_RESERVATION(1),
	IMPFUNG_DATE_NOT_AVAILABLE(1),
	IMPFUNG_FALSCHER_IMPFSTOFF(2),
	IMPFUNG_FALSCHER_ODI(2),
	IMPFUNG_LOESCHEN_ERSTE(1),
	IMPFUNG_LOESCHEN_ZWEITE(1),
	IMPFUNG_LOESCHEN_N(3),
	IMPFUNG_ZUVIELE_DOSEN(0),
	IMPFUNG_ZUVIELE_DOSEN_KORREKTUR(2),
	IMPFUNG_GRUNDIMMUNISIERUNG_INVALID(1),
	INVALID_CREDENTIALS(0),
	INVALID_EMAIL(1),
	INVALID_IBAN(1),
	JOB_IN_PROGRESS(2),
	KC_GROUP_DOES_NOT_EXIST(1),
	KC_USER_ALREADY_EXIST(0),
	KC_USER_DOES_NOT_EXIST(0),
	KRANKHEIT_MISSMATCH(2),
	ODI_IMPORT_FEHLER(2),
	MISSING_GRUNDIMMUNISIERUNG(0),
	NO_IMPFUNG_OR_IMPFSCHUTZ(0),
	NO_USER_FOR_REGISTRATION(1),
	NO_VACME_IMPFUNG(0),
	NO_ZERTIFIKAT_PDF(1),
	NO_ZERTIFIKAT_QRCODE(1),
	NOT_ALLOWED(0),
	NOT_ALLOWED_FOR_KRANKHEIT(2),
	NOT_YET_IMPLEMENTED(1),
	ODI_DEACTIVATED(1),
	ODI_DOES_ALREADY_EXIST(1),
	ODI_FILTER_AGE_VALIDATION(0),
	ODI_FILTER_GESCHLECHT_VALIDATION(0),
	ODI_FILTER_GESCHLECHT_VALIDATION_UNPARSABLE(0),
	ODI_FILTER_PRIORITAET_PUNKT_VALIDATION(0),
	ODI_FILTER_PRIORITAET_VALIDATION(0),
	ODI_FILTER_REGISTRIERUNGSEINGANG_VALIDATION(0),
	ODI_FILTER_REGISTRIERUNGSEINGANG_VALIDATION_UNPARSABLE(0),
	ODI_FILTER_CHRONISCHE_KRANKHEITEN_VALIDATION(0),
	ODI_FILTER_CHRONISCHE_KRANKHEITEN_VALIDATION_UNPARSABLE(0),
	ODI_FILTER_BERUFLICHE_TAETIGKEIT_VALIDATION(0),
	ODI_FILTER_BERUFLICHE_TAETIGKEIT_VALIDATION_UNPARSABLE(0),
	ODI_FILTER_LEBENSUMSTAENDE_VALIDATION(0),
	ODI_FILTER_LEBENSUMSTAENDE_VALIDATION_UNPARSABLE(0),
	ONBOARDING_CREATION_FAILED(1),
	ONBOARDING_FINISH_FAILED(1),
	ONBOARDING_CODE_LOCKED(1),
	ONBOARDING_INVALID_CHECKSUM(1),
	ONBOARDING_INVALID_CODE(1),
	POSITIVER_TEST_DATUM_INVALID(1),
	REGISTRIERUNG_CANNOT_GENERATE_ZERTIFIKAT(0),
	REGISTRIERUNG_CANNOT_GENERATE_ZERTIFIKAT_NICHT_ZUGELASSEN(0),
	REGISTRIERUNG_DOES_ALREADY_EXIST(0),
	REGISTRIERUNG_DOKUMENTSTATUS_FALSCH(1),
	REGISTRIERUNG_VERSTORBEN(1),
	REGISTRIERUNG_WRONG_STATUS(2),
	REGISTRIERUNG_ZERTIFIKAT_ALREADY_GENERATED(2),
	REPORT_EMPTY(0),
	STRICT_MIGRATION_ODI_FEHLER(2),
	TERMINBESTAETIGUNG_KEIN_OFFENER_TERMIN(0),
	UNHANDLED_CONFIRMATION_TYPE(2),
	UNKNOWN_IMPFTERMIN(1),
	UNKNOWN_ODI(1),
	UNKNOWN_REGISTRIERUNGSNUMMER(1),
	UNKNOWN_REGISTRIERUNGSNUMMER_KRANKHEIT(2),
	UNKNOWN_ZERTIFIKAT(1),
	UPLOAD_FAILED(0),
	UPLOAD_INVALID_FILE_TYPE(1),
	USER_HAS_WRONG_ROLE(1),
	USER_NOT_IN_ODI(1),
	USER_HAS_MULTIPLE_ODIS_WITH_SAME_GLN(1),
	USER_NOT_FOUND(0),
	USER_HAS_NO_ACTIVE_ODI(0),
	ZERTIFIKAT_GENERIERUNG_FEHLER(0),
	ZERTIFIKAT_REVOCATION_FEHLER(0),
	ZERTIFIKAT_POST_PLZ_FEHLER(1);
	@Getter
	private final int requiredArgs;

	public Supplier<AppValidationException> supply(Serializable... args) {
		return () -> create(args);
	}

	public AppValidationException create(Serializable... args) {
		validateArgs(args);

		var ex = new AppValidationException(this, args);
		return ex;
	}

	private void validateArgs(Serializable... args) {
		if (args.length != requiredArgs) {
			String argsAsText = Arrays.stream(args)
				.map(String::valueOf)
				.collect(joining(","));

			throw new IllegalArgumentException(String.format(
				"Need %d arguments but got: %d: %s",
				requiredArgs, args.length, argsAsText));
		}
	}
}
