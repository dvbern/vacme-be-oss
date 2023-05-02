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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.embeddables.DateTimeRange;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.jax.migration.ImpfungMigrationJax;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.errors.PlzMappingException;
import ch.dvbern.oss.vacme.shared.util.Constants;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

@Slf4j
public final class ValidationUtil {

	private ValidationUtil() {
	}

	public static void validateNotArchiviert(@NonNull Registrierung registrierung) {
		if (registrierung.getTimestampArchiviert() != null) {
			throw AppValidationMessage.REGISTRIERUNG_WRONG_STATUS.create(
				registrierung.getRegistrierungsnummer(),
				"NOT ARCHIVIERT");
		}
	}

	public static void validateCanGetZertifikat(@NonNull Impfdossier impfdossier) {
		if (impfdossier.getRegistrierung().isAnonymisiert() ||
			!impfdossier.abgeschlossenMitVollstaendigemImpfschutz() || // kein vollstaendiger impfschutz
			impfdossier.getRegistrierung().isAbgleichElektronischerImpfausweis() != Boolean.TRUE ||
			impfdossier.getRegistrierung().getRegistrierungsEingang() == RegistrierungsEingang.UNBEKANNT) {
			throw AppValidationMessage.REGISTRIERUNG_CANNOT_GENERATE_ZERTIFIKAT.create();
		}
	}

	public static void validateStatusOneOf(
		@NonNull Impfdossier impfdossier,
		@NonNull ImpfdossierStatus... expectedStatus
	) {
		String expectedStates = Arrays.stream(expectedStatus).map(Enum::name).collect(Collectors.joining(" / "));
		if (Arrays.stream(expectedStatus).noneMatch(dossierStatus -> dossierStatus == impfdossier.getDossierStatus())) {
			throw AppValidationMessage.REGISTRIERUNG_WRONG_STATUS.create(impfdossier.getRegistrierung()
				.getRegistrierungsnummer(), expectedStates);
		}
	}

	public static void validateNoTermine(
		@NonNull ImpfinformationDto infos
	) {
		if (infos.getImpfdossier().getBuchung().getImpftermin1() != null
			|| infos.getImpfdossier().getBuchung().getImpftermin2() != null) {
			String msg = "Die Registrierung hat schon Termine";
			throw AppValidationMessage.ILLEGAL_STATE.create(msg);
		}
	}

	public static void validateAnzahlImpfungen(
		@NonNull Impffolge impffolge,
		@NonNull ImpfinformationDto impfinformationen) {
		switch (impffolge) {
		case ERSTE_IMPFUNG:
		case BOOSTER_IMPFUNG:
			return;
		case ZWEITE_IMPFUNG:
			// Wir muessen fuer die zweite Impfung die Anzahl der benoetigten Dosen der ERSTEN Impfung validieren!
			if (impfinformationen.getImpfung1() != null) {
				var brauchtNur1Impfung = ImpfinformationenUtil.willBeGrundimmunisiertAfterErstimpfungImpfstoff(
					impfinformationen.getImpfung1().getImpfstoff(),
					impfinformationen.getExternesZertifikat());
				if (brauchtNur1Impfung) {
					// kann nicht 2 impfungen haben wenn nur 1 dosis beneotigt
					throw AppValidationMessage.IMPFUNG_ZUVIELE_DOSEN.create();
				}
			}
			return;
		default:
			throw new IllegalStateException("Unexpected value: " + impffolge);
		}
	}

	public static boolean isFirstImpfungToday(@NonNull Impfung firstImfpung) {
		return DateUtil.isToday(firstImfpung.getTimestampImpfung());
	}

	/**
	 * Validates if the second control is triggered on the same day the first vaccination has been made, therefore it's
	 * too early for the next control
	 */
	public static void validateSecondKontrolleOnSameDay(@NonNull Impfung firstImfpung) {
		LocalDateTime timestampImpfung = firstImfpung.getTimestampImpfung();
		if (isFirstImpfungToday(firstImfpung)) {
			throw AppValidationMessage.IMPFTERMIN_KONTROLLE_TOO_EARLY.create(DateUtil.formatDate(timestampImpfung));
		}
	}

	/**
	 * Validates if the second vaccination is on the same day the first vaccination has been made, therefore it's
	 * too early for the next vaccinations
	 */
	public static void validateSecondImpfungOnSameDay(
		@NonNull Impfung firstImpfung,
		@NonNull LocalDateTime timestamp) {
		LocalDateTime timestampImpfung = firstImpfung.getTimestampImpfung();
		if (DateUtil.isSameDay(timestampImpfung, timestamp)) {
			throw AppValidationMessage.IMPFTERMIN_IMPFUNG_TOO_EARLY.create(DateUtil.formatDate(timestampImpfung));
		}
	}

	/**
	 * Validates if the second vaccination is before the first vaccination has been made, therefore it's
	 * too early for the next vaccinations
	 */
	public static void validateSecondImpfungBeforeFirst(
		@NonNull Impfung firstImpfung,
		@NonNull LocalDateTime timestamp) {
		validateNewImpfungNotBeforePrevious(
			firstImpfung,
			timestamp,
			AppValidationMessage.IMPFTERMIN_IMPFUNG_BEFORE_FIRST);
	}

	public static void validateNewImpfungNotBeforePrevious(
		@NonNull ImpfInfo prevImpfung,
		@NonNull LocalDateTime timestamp,
		@NonNull AppValidationMessage appMsg1Param
	) {
		LocalDateTime timestampImpfung = prevImpfung.getTimestampImpfung();
		if (timestampImpfung != null && timestamp.isBefore(timestampImpfung)) {
			throw appMsg1Param.create(DateUtil.formatDate(timestampImpfung));
		}
	}

	public static void validateDatumAbfolgeOfImpfungenStillValid(
		@NonNull List<ImpfInfo> impfinfosOrderedByImpffolgeNr,
		@NonNull Impfung impfung
	) {
		if (impfinfosOrderedByImpffolgeNr.isEmpty() || impfinfosOrderedByImpffolgeNr.size() == 1) {
			return;
		}
		int indexOfCurrent = findIndexOfImpfungInImpfInfoList(impfinfosOrderedByImpffolgeNr, impfung);

		for (int i = 0; i < impfinfosOrderedByImpffolgeNr.size(); i++) {
			if (indexOfCurrent == i) {
				continue; // don't validate against impfung itself
			}
			ImpfInfo compared = impfinfosOrderedByImpffolgeNr.get(i);
			if (i < indexOfCurrent) {
				ValidationUtil.validateNewImpfungNotBeforePreviousTimestamp(
					compared,
					impfung.getTimestampImpfung(),
					AppValidationMessage.IMPFTERMIN_IMPFUNG_BETWEEN_OTHERS);
			} else if (i > indexOfCurrent - 1) {
				ValidationUtil.validateNewImpfungNotBeforePreviousTimestamp(
					impfung,
					compared.getTimestampImpfung(),
					AppValidationMessage.IMPFTERMIN_IMPFUNG_BETWEEN_OTHERS);
			}
		}
	}

	public static void validateImpffolgenummernContinuous(@NonNull ImpfinformationDto impfinformationDto) {
		List<Impfung> impfungen = ImpfinformationenUtil.getImpfungenOrderedByImpffolgeNr(impfinformationDto);

		Integer lastImpffolgeNr = null;
		LocalDateTime lastImpfungTimestamp = null;
		boolean firstRun = true;

		for (Impfung impfung : impfungen) {
			Integer impffolgeNr = ImpfinformationenService.getImpffolgeNr(impfinformationDto, impfung);
			LocalDateTime impfungTimestamp = impfung.getTimestampImpfung();

			if (firstRun) {
				lastImpffolgeNr = impffolgeNr;
				lastImpfungTimestamp = impfungTimestamp;
				firstRun = false;

				validateImpfolgeNrOfFirstImpfungIfExternesZertifikatPresent(impffolgeNr, impfinformationDto);

				continue;
			}

			if (lastImpffolgeNr + 1 != impffolgeNr || !lastImpfungTimestamp.isBefore(impfungTimestamp)) {
				throw AppValidationMessage.IMPFTERMIN_IMPFUNG_BETWEEN_OTHERS.create();
			}
			lastImpffolgeNr = impffolgeNr;
			lastImpfungTimestamp = impfungTimestamp;
		}
	}

	/**
	 * Unsere erste ImpffolgeNr muss eine Nummer hoeher sein als die Anzahl Impfungen die in einem allfaellig
	 * vorhandenen
	 * ExternenZertifikat erfasst wurden
	 */
	private static void validateImpfolgeNrOfFirstImpfungIfExternesZertifikatPresent(
		Integer firstVacmeImpffolgeNr,
		ImpfinformationDto impfinformationDto) {
		if (impfinformationDto.getExternesZertifikat() != null
			&& impfinformationDto.getExternesZertifikat().getAnzahlImpfungen() + 1 != firstVacmeImpffolgeNr) {
			throw AppValidationMessage.IMPFTERMIN_IMPFUNG_BETWEEN_OTHERS.create();
		}
	}

	private static int findIndexOfImpfungInImpfInfoList(
		@NonNull List<ImpfInfo> orderedImpfInfos,
		@NonNull Impfung impfung
	) {
		for (int i = 0; i < orderedImpfInfos.size(); i++) {
			ImpfInfo impfInfo = orderedImpfInfos.get(i);
			if (impfInfo.getId().equals(impfung.getId())) {
				return i;
			}
		}
		throw AppValidationMessage.ILLEGAL_STATE.create("Zu korrigierende Impfung muss in Impfinfoliste vorhanden "
			+ "sein");
	}

	public static void validateNewImpfungNotBeforePreviousTimestamp(
		@NonNull ImpfInfo prevImpfung,
		@Nullable LocalDateTime timestamp,
		@NonNull AppValidationMessage appMsg0Param
	) {
		LocalDateTime timestampImpfung = prevImpfung.getTimestampImpfung();
		if (timestamp != null && timestampImpfung != null && !timestamp.isAfter(timestampImpfung)) {
			throw appMsg0Param.create();
		}
	}

	public static void validateBoosterImpfungEinTagNachBisherige(
		@NonNull ImpfinformationDto impfinformationen,
		@NonNull LocalDateTime timestampOfImpfung,
		boolean validateSameDayImpfungen
	) {
		List<Impfung> previousImpfungen =
			Lists.newArrayList(ImpfinformationenUtil.getImpfungenOrderedByImpffolgeNr(impfinformationen));
		previousImpfungen.sort(Comparator.comparing(Impfung::getTimestampImpfung));
		Collections.reverse(previousImpfungen); // newest first

		if (validateSameDayImpfungen) {
			previousImpfungen.forEach(impfung -> validateSecondImpfungOnSameDay(impfung, timestampOfImpfung));
		}
		previousImpfungen.forEach(impfung -> validateNewImpfungNotBeforePrevious(impfung, timestampOfImpfung,
			AppValidationMessage.IMPFTERMIN_IMPFUNG_BEFORE_PREVIOUS));

		// Boostertermin muss nach dem ExternenZertifikat sein
		final ImpfInfo newestExtImpfInfo = impfinformationen.getExternesZertifikat(); // auch unvollstaendige zaehlen
		if (newestExtImpfInfo != null) {
			validateNewImpfungNotBeforePrevious(newestExtImpfInfo, timestampOfImpfung,
				AppValidationMessage.IMPFTERMIN_IMPFUNG_BEFORE_PREVIOUS);
		}
	}

	public static boolean isCorrectOrtDerImpfung(@NonNull OrtDerImpfung ortDerImpfung, @NonNull Impftermin termin) {
		return termin.getImpfslot().getOrtDerImpfung().equals(ortDerImpfung);
	}

	public static boolean isCorrectDatum(@NonNull Impftermin termin, @NonNull LocalDateTime dateTime) {
		return DateUtil.isSameDay(termin.getImpfslot().getZeitfenster().getVon(), dateTime);
	}

	public static void validateCorrectDatum(@NonNull Impftermin termin, @NonNull LocalDateTime dateTime) {
		if (!isCorrectDatum(termin, dateTime)) {
			throw AppValidationMessage.IMPFTERMIN_FALSCHES_DATUM.create(
				DateUtil.formatDate(termin.getImpfslot().getZeitfenster().getVon()));
		}
	}

	public static void validateDaysBetweenImpfungen(
		@NonNull DateTimeRange termin1,
		@NonNull DateTimeRange termin2,
		int minimumDaysBetweenImpfungen,
		int maximumDaysBetweenImpfungen
	) {
		final long between = DateUtil.getDaysBetween(
			termin1.getVon().toLocalDate().atStartOfDay(), termin2.getVon().toLocalDate().atStartOfDay());

		if (between < minimumDaysBetweenImpfungen) {
			// wir sollten nie in die Situation kommen diesen Logoutput printen zu muessen, soll auf gui abgefangen
			// werden
			LOG.error("VACME-ERROR: Impfung1 wegen ungueltigem Minimalabstand zwischen Impfungen blockiert");
			throw AppValidationMessage.IMPFTERMINE_FALSCHER_ABSTAND.create(between, minimumDaysBetweenImpfungen,
				maximumDaysBetweenImpfungen);
		}
		if (between > maximumDaysBetweenImpfungen) {
			LOG.error("VACME-ERROR: Impfung1 wegen ungueltigem Maximalabstand zwischen Impfungen blockiert");
			throw AppValidationMessage.IMPFTERMINE_FALSCHER_ABSTAND.create(between, minimumDaysBetweenImpfungen,
				maximumDaysBetweenImpfungen);
		}
	}

	public static void validateFirstTerminBeforeSecond(
		@Nullable Impftermin impftermin1,
		@Nullable Impftermin impftermin2) {
		if (impftermin1 != null && impftermin2 != null) {

			LocalDateTime termin1Date = impftermin1.getImpfslot().getZeitfenster().getVon();
			LocalDateTime termin2Date = impftermin2.getImpfslot().getZeitfenster().getVon();
			boolean correctOrder = termin1Date.isBefore(termin2Date);
			if (!correctOrder) {
				throw AppValidationMessage.IMPFTERMIN_INVALID_ORDER.create(
					DateUtil.formatDate(termin1Date),
					DateUtil.formatDate(termin2Date));
			}
		}
	}

	public static void validateNotFuture(@NonNull Impftermin impftermin) {
		LocalDateTime terminDate = impftermin.getImpfslot().getZeitfenster().getVon();
		if (terminDate.isAfter(LocalDateTime.now())) {
			throw AppValidationMessage.IMPFTERMIN_INVALID_CORRECTION.create("Termin can not be in future");
		}
	}

	public static void validateNotAfterTomorrow(@NonNull Impftermin impftermin) {
		LocalDateTime terminDate = impftermin.getImpfslot().getZeitfenster().getVon();
		if (!terminDate.isBefore(LocalDate.now().plusDays(1).atStartOfDay())) {
			throw AppValidationMessage.IMPFTERMIN_FALSCHES_DATUM.create("Termin can not be in future");
		}
	}

	public static void validateImpfungMinDate(@NonNull Impftermin impftermin) {
		LocalDateTime terminDate = impftermin.getImpfslot().getZeitfenster().getVon();
		LocalDateTime mindateForImpfungen = impftermin.getImpfslot().getKrankheitIdentifier().getMindateForImpfungen();

		if (terminDate.isBefore(mindateForImpfungen)) {
			String msg =
				String.format("Termin can not be before start of vaccination campaign %s", mindateForImpfungen);
			throw AppValidationMessage.IMPFTERMIN_INVALID_CORRECTION.create(msg);
		}
	}

	@NonNull
	public static String toString(@NonNull Set<? extends ConstraintViolation<?>> constraintViolations) {
		return constraintViolations.stream()
			.map((cv) -> cv == null ? "null" : cv.getPropertyPath() + ": " + cv.getMessage())
			.collect(Collectors.joining(", "));
	}

	public static void validateNotFieber(ImpfungMigrationJax impfRequest) {
		if (impfRequest.isFieber()) {
			throw new AppFailureException("Impfungen mit fieber=TRUE duerfen nicht erfasst werden");
		}
	}

	public static void validateEinwillung(ImpfungMigrationJax impfRequest) {
		if (!impfRequest.isEinwilligungImpfung()) {
			throw new AppFailureException("Impfungen mit einwilligung=false duerfen nicht erfasst werden");
		}
	}

	public static void validateNeueKrankheit(ImpfungMigrationJax impfRequest) {
		if (impfRequest.isNeueKrankheit()) {
			throw new AppFailureException("Impfungen mit neueKrankheit=true duerfen nicht erfasst werden");
		}
	}

	public static void validateFlags(ImpfungMigrationJax impfRequest) {
		validateNotFieber(impfRequest);
		validateEinwillung(impfRequest);
		validateNeueKrankheit(impfRequest);
	}

	@NonNull
	public static String validateAndNormalizePlz(@NonNull String plz) {
		String normalizedPlz;
		//remove whitespaces
		normalizedPlz = plz.strip();
		normalizedPlz = StringUtils.normalizeSpace(normalizedPlz);
		normalizedPlz = normalizedPlz.replace(" ", "");
		//remove non numeric
		normalizedPlz = normalizedPlz.replaceAll("[^\\d]", "");// remove all non numeric
		// check length
		int length = normalizedPlz.length();
		if (length != Constants.SWISS_PLZ_LENGTH || normalizedPlz.startsWith("0")) {
			throw new PlzMappingException("plz not valid: " + plz);
		}
		return normalizedPlz;
	}

	public static void validateKrankheitIdentifier(@NotNull ImpfinformationDto infos) {
		Validate.isTrue(
			infos.getKrankheitIdentifier() == infos.getImpfdossier().getKrankheitIdentifier(),
			String.format(
				"Krankheit auf Impfdossier %s muss Krankheit auf Impfinformation %s entsprechen",
				infos.getImpfdossier().getKrankheitIdentifier(),
				infos.getKrankheitIdentifier()));
	}

	public static void validateImpfstoffZulassung(Impfstoff impfstoff, boolean extern) {
		switch (impfstoff.getZulassungsStatus()) {
		case ZUGELASSEN:
		case EMPFOHLEN:
			break; // sehr gut
		case EXTERN_ZUGELASSEN:
			if (!extern) { // nur extern zugelassen, ungueltig fuer intern
				LOG.error(
					"VACME-ERROR: Es wurde versucht einen Imfpstoff ({}) zu verwenden der nur fuer externe zugelassen "
						+ "ist",
					impfstoff.getDisplayName());
				throw AppValidationMessage.IMPFTERMIN_IMPFSTOFF_NICHT_ZUGELASSEN.create(impfstoff.getDisplayName());
			}
			break; // extern zugelassen, auch gut
		default:
			// nicht zugelassen
			throw AppValidationMessage.IMPFTERMIN_IMPFSTOFF_NICHT_ZUGELASSEN.create(impfstoff.getDisplayName());
		}
	}

	public static void validateGrundimmunisierung(
		@NonNull Impfung impfung,
		@NonNull Impffolge impffolge,
		@Nullable Impfung previousImpfung
	) {
		// gehoert nicht zur Grundimmunisierung
		if (!impfung.isGrundimmunisierung()) {
			if (impffolge == Impffolge.ERSTE_IMPFUNG || impffolge == Impffolge.ZWEITE_IMPFUNG) {
				// Impfung 1 und 2 muessen zur Grundimmunisierung gehoeren (wenn nur 1 noetig, ist eine spaetere 2.
				// bereits in der Booster-Liste)
				throw AppValidationMessage.IMPFUNG_GRUNDIMMUNISIERUNG_INVALID.create(
					"Die Impfung für die erste/zweite Impffolge muss zur Grundimmunisierung "
						+ "gehören");
			}

			// spaetere Impfungen muessen nicht zur Grundim. gehoeren
			return;
		}

		// gehoert zur Grundimmunisierung
		if (previousImpfung == null) {
			return; // wenn es keine vorherige gibt ist es die erste Impfung, das ist korrekt
		}
		if (!previousImpfung.isGrundimmunisierung()) {
			// die vorherige Impfung muss auch zur Grundimm. gehoeren
			throw AppValidationMessage.IMPFUNG_GRUNDIMMUNISIERUNG_INVALID.create(
				"Die vorherige Impfung gehoert nicht zur Grundimmunisierung");
		}
	}

	public static void validateCurrentKontrolleHasNoImpfungYet(
		@NonNull ImpfinformationDto impfinformationen,
		@NonNull Impffolge impffolge,
		@Nullable UUID dossiereintragID
	) {
		// Wenn fuer diese Kontrolle schon geimpft wurde, darf man die Kontrolle nicht mehr abaendern, siehe VACME-1718
		// Szenario: (Booster) Kontrolle in 2 Browser oeffnen, dann in Browser 1 Kontrolle und Impfung machen, dann in
		// Browser 2 Kontrolle ok -> soll Fehler
		// geben
		if (ImpfinformationenService.getImpfungForKontrolle(impfinformationen, impffolge, dossiereintragID)
			.isPresent()) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Es wurde bereits geimpft");
		}
	}

	public static void validateOdiNotDeactivated(OrtDerImpfung ortDerImpfung) {
		if (ortDerImpfung.isDeaktiviert()) {
			throw AppValidationMessage.ODI_DEACTIVATED.create(ortDerImpfung.getName());
		}
	}

	public static void validateImpfstoffForKrankheit(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull Impfstoff impfstoff
	) {
		final boolean valid =
			impfstoff.getKrankheiten().stream().anyMatch(krankheit -> krankheit.getIdentifier() == krankheitIdentifier);
		if (!valid) {
			throw AppValidationMessage.IMPFUNG_FALSCHER_IMPFSTOFF.create(impfstoff.getName(), krankheitIdentifier);
		}
	}

	public static void validateOdiForKrankheit(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull OrtDerImpfung ortDerImpfung
	) {
		final boolean valid =
			ortDerImpfung.getKrankheiten()
				.stream()
				.anyMatch(krankheit -> krankheit.getIdentifier() == krankheitIdentifier);
		if (!valid) {
			throw AppValidationMessage.IMPFUNG_FALSCHER_ODI.create(ortDerImpfung.getName(), krankheitIdentifier);
		}
	}

	public static void validateSlotForKrankheit(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull Impfslot slotN
	) {
		final boolean valid = slotN.getKrankheitIdentifier() == krankheitIdentifier;
		if (!valid) {
			LOG.info(
				"VACME-ERROR: Krankheit missmatch {} bei Impfslot {} fuer Krankheit {}",
				krankheitIdentifier,
				slotN,
				slotN.getKrankheitIdentifier());
			throw AppValidationMessage.KRANKHEIT_MISSMATCH.create(krankheitIdentifier, slotN.getKrankheitIdentifier());
		}
	}
}
