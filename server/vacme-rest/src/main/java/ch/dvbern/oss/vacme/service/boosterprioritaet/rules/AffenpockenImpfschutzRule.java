/*
 *
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

package ch.dvbern.oss.vacme.service.boosterprioritaet.rules;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.impfen.Erkrankung;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioUtil;
import ch.dvbern.oss.vacme.service.boosterprioritaet.ImpfstoffInfosForRules;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class implemetns a single rule for a Affenpocken Impfschutz calculation
 * It takes certain ranges like alterVon alterBis and anzahlErhalternerImpfungen for which it can calculate
 * an Impfschutz
 */
public class AffenpockenImpfschutzRule implements IBoosterPrioritaetRule {

	@Nullable
	private final Integer alterVon;

	@Nullable
	private final Integer alterBis;

	@Nullable
	private final Integer giltAbAnzahlErhaltenerImpfungen;

	@Nullable
	private final Integer giltBisAnzahlErhaltenerImpfungen;

	@Nullable
	private final Integer freigabeOffsetLetzteImpfungWithoutImpfschutz;
	private final Integer freigabeOffsetLetzteImpfungWithImpfschutz;

	@Nullable
	private final ChronoUnit freigabeOffsetLetzteImpfungWithoutImpfschutzUnit;
	private final ChronoUnit freigabeOffsetLetzteImpfungWithImpfschutzUnit;

	@NonNull
	private final ImpfstoffInfosForRules specifiedImpfstoffe;


	public AffenpockenImpfschutzRule(
		@NonNull ImpfstoffInfosForRules specifiedImpfstoffe,
		@Nullable Integer alterVon,
		@Nullable Integer alterBis,
		@Nullable Integer freigabeOffsetLetzteImpfungWithoutImpfschutz,
		@Nullable ChronoUnit freigabeOffsetLetzteImpfungWithoutImpfschutzUnit,
		@Nullable Integer freigabeOffsetLetzteImpfungWithImpfschutz,
		@Nullable ChronoUnit freigabeOffsetLetzteImpfungWithImpfschutzUnit,
		@Nullable Integer giltAbAnzahlErhaltenerImpfungen,
		@Nullable Integer giltBisAnzahlErhaltenerImpfungen
	) {
		this.alterVon = alterVon;
		this.alterBis = alterBis;
		this.freigabeOffsetLetzteImpfungWithoutImpfschutz = freigabeOffsetLetzteImpfungWithoutImpfschutz;
		this.freigabeOffsetLetzteImpfungWithImpfschutz = freigabeOffsetLetzteImpfungWithImpfschutz;
		this.freigabeOffsetLetzteImpfungWithoutImpfschutzUnit = freigabeOffsetLetzteImpfungWithoutImpfschutzUnit;
		this.freigabeOffsetLetzteImpfungWithImpfschutzUnit = freigabeOffsetLetzteImpfungWithImpfschutzUnit;
		this.giltAbAnzahlErhaltenerImpfungen = giltAbAnzahlErhaltenerImpfungen;
		this.giltBisAnzahlErhaltenerImpfungen = giltBisAnzahlErhaltenerImpfungen;
		this.specifiedImpfstoffe = specifiedImpfstoffe;
		validateUnitAndOffsetIsPresent();
	}

	private void validateUnitAndOffsetIsPresent() {
		Validate.isTrue((freigabeOffsetLetzteImpfungWithoutImpfschutz == null
				&& freigabeOffsetLetzteImpfungWithoutImpfschutzUnit == null)
			|| (freigabeOffsetLetzteImpfungWithoutImpfschutz != null
				&& freigabeOffsetLetzteImpfungWithoutImpfschutzUnit != null),
			"freigabeOffsetLetzteImpfungWithoutImpfschutz and freigabeOffsetLetzteImpfungWithoutImpfschutzUnit "
				+ "must be specified together (both either set or unset)");

		Validate.isTrue((freigabeOffsetLetzteImpfungWithImpfschutz == null
				&& freigabeOffsetLetzteImpfungWithImpfschutzUnit == null)
			|| (freigabeOffsetLetzteImpfungWithImpfschutz != null
				&& freigabeOffsetLetzteImpfungWithImpfschutzUnit != null),
			"freigabeOffsetLetzteImpfungWithImpfschutz and freigabeOffsetLetzteImpfungWithImpfschutzUnit "
				+ "must be specified together (both either set or unset)");
	}

	@Override
	public @NonNull Optional<Impfschutz> calculateImpfschutz(
		@NonNull Fragebogen fragebogen,
		@NonNull ImpfinformationDto impfinformationDto,
		@NonNull List<ImpfInfo> orderedImpfInfos,
		@NonNull List<Erkrankung> orderedErkrankungen
	) {
		Registrierung registrierung = fragebogen.getRegistrierung();
		int alter = (int) DateUtil.getAge(registrierung.getGeburtsdatum());
		int anzahlImpfungen = orderedImpfInfos.size();

		ImpfInfo newestImpfInfo =
			Iterables.getLast(orderedImpfInfos, null); // latest Vacme Impfung or externesZertifikat

		// CHECK IF RULE APPLIES OR RETURN EMPTY
		if (!matchesAlterRange(alter)) {
			return Optional.empty();
		}

		// CALCULATIONS
		boolean hasGrundimmunisierung = impfinformationDto.getImpfdossier().getVollstaendigerImpfschutzTyp() != null;

		Set<UUID> erlaubteImpfstoffe = calculateErlaubteImpfstoffe();
		LocalDate stichtagThatWillEnterAltersRange = calculateStichtagThatWillEnterAltersRange(registrierung, alter);

		LocalDateTime freigegebenAb = calculateFreigegebenAbEkif(
			hasGrundimmunisierung, newestImpfInfo, stichtagThatWillEnterAltersRange, erlaubteImpfstoffe, anzahlImpfungen);
		LocalDateTime freigegebenAbSelbstzahler = calculateFreigegebenAbSelbstzahler(
			hasGrundimmunisierung, newestImpfInfo, stichtagThatWillEnterAltersRange, erlaubteImpfstoffe, anzahlImpfungen);
		boolean benachrichtigungBeiFreigabe = BoosterPrioUtil.calculateDefaultBenachrichtigung(registrierung);

		LocalDateTime immunisiertBis = hasGrundimmunisierung ? freigegebenAb : null;

		// IMPFSCHUTZ
		Impfschutz impfschutz = new Impfschutz(
			immunisiertBis,
			freigegebenAb,
			freigegebenAbSelbstzahler,
			erlaubteImpfstoffe,
			benachrichtigungBeiFreigabe);

		return Optional.of(impfschutz);
	}

	private boolean matchesAlterRange(int alter) {
		if (alterVon != null) {
			if (alter < alterVon) {
				if (alterVon - alter == 1) {
					// Erreicht die Person das Alter innerhalb eines Jahres, erhaelt sie einen Impfschutz ab dem
					// naechsten Geburtstag
				} else {
					return false;
				}
			}
		}
		if (alterBis != null) {
			if (alter > alterBis) {
				return false;
			}
		}
		return true;
	}

	@Override
	public @Nullable Integer getAnzahlMonateBisFreigabe() {
		if (freigabeOffsetLetzteImpfungWithoutImpfschutzUnit != null
			&& freigabeOffsetLetzteImpfungWithoutImpfschutz != null
		) {
			long estimatedDaysInDuration =
				freigabeOffsetLetzteImpfungWithoutImpfschutzUnit.getDuration().multipliedBy(
				freigabeOffsetLetzteImpfungWithoutImpfschutz).toDays();
			long estimatedDaysInAMonth = ChronoUnit.MONTHS.getDuration().toDays();
			long months = Math.floorDiv(estimatedDaysInDuration, estimatedDaysInAMonth);
			return Long.valueOf(months).intValue();
		}
		return 0;
	}

	@NonNull
	private Set<UUID> calculateErlaubteImpfstoffe() {
		Set<UUID> erlaubteImpfstoffe =
			specifiedImpfstoffe.getImpfstoffeEmpfohlenForBoosterForKrankheit(KrankheitIdentifier.AFFENPOCKEN)
				.stream()
				.map(Impfstoff::getId)
				.collect(Collectors.toSet());
		return erlaubteImpfstoffe;
	}

	@Nullable
	private LocalDateTime calculateFreigegebenAbEkif(
		boolean hasGrundimmunisierung,
		@Nullable ImpfInfo newestImpfInfo,
		@Nullable LocalDate alterRegelGuelitigAb,
		@NonNull Set<UUID> erlaubteImpfstoffe,
		int anzahlBoosterImpfungen
	) {
		return calculateFreigegebenAb(
			hasGrundimmunisierung,
			newestImpfInfo,
			alterRegelGuelitigAb,
			erlaubteImpfstoffe,
			false,
			anzahlBoosterImpfungen
		);
	}

	@Nullable
	private LocalDateTime calculateFreigegebenAbSelbstzahler(
		boolean hasGrundimmunisierung,
		@Nullable ImpfInfo newestImpfInfo,
		@Nullable LocalDate alterRegelGuelitigAb,
		@NonNull Set<UUID> erlaubteImpfstoffe,
		int anzahlBoosterImpfungen
	) {
		return calculateFreigegebenAb(
			hasGrundimmunisierung,
			newestImpfInfo,
			alterRegelGuelitigAb,
			erlaubteImpfstoffe,
			true,
			anzahlBoosterImpfungen
		);
	}

	@Nullable
	private LocalDateTime calculateFreigegebenAb(
		boolean hasGrundimmunisierung,
		@Nullable ImpfInfo newestImpfInfo,
		@Nullable LocalDate alterRegelGueltigAb,
		@NonNull Set<UUID> erlaubteImpfstoffe,
		boolean selbstzahlerModus,
		int anzahlImpfungen
	) {
		// Wenn die erlaubten Impfstoffe leer sind: Freigabedatum = null!
		if (erlaubteImpfstoffe.isEmpty()) {
			return null;
		}

		LocalDateTime freigegebenAb = null;

		// im Selbstzahlermodus wird ein Friegabedatum fuer selbstzahler berechnet auch wenn
		// die Ekif Empfehlung die Regel noch nicht beinhaltet
		boolean regelRelevant = selbstzahlerModus || isRegelForCurrentImpfung(anzahlImpfungen);

		if (regelRelevant) {
			freigegebenAb = calcFreigegebenAbBasedOnImpfung(hasGrundimmunisierung, newestImpfInfo);

			// Falls ein alternativer Stichtag fuer die Regeln definiert ist (d.h. wenn ich in naher Zukunft das
			// Regel-Alter erreiche) und dieses nach dem bisher berechneten freigegebenAb liegt, so nehmen wir
			// dieses
			if (alterRegelGueltigAb != null
				&& freigegebenAb != null
				&& alterRegelGueltigAb.isAfter(freigegebenAb.toLocalDate())) {
				freigegebenAb = alterRegelGueltigAb.atStartOfDay();
			}
		}
		return freigegebenAb;
	}

	@Nullable
	private LocalDateTime calcFreigegebenAbBasedOnImpfung(
		boolean hasGrundimmunisierung,
		@Nullable ImpfInfo newestImpfInfo
	) {
		LocalDateTime freigegebenAbBasedOnImpfung = null;
		if (freigabeOffsetLetzteImpfungWithoutImpfschutz != null
			&& freigabeOffsetLetzteImpfungWithoutImpfschutzUnit != null
		) {
			boolean isLastImpfungDateUnknown = newestImpfInfo == null || newestImpfInfo.getTimestampImpfung() == null;
			if (isLastImpfungDateUnknown) {
				freigegebenAbBasedOnImpfung =
					LocalDate.now().atStartOfDay(); // assume Impfung was in childhood and is therefore long ago
			} else {
				LocalDateTime lastImpfungTimestamp = newestImpfInfo.getTimestampImpfung();
				if (hasGrundimmunisierung) {
					freigegebenAbBasedOnImpfung = lastImpfungTimestamp.plus(
						freigabeOffsetLetzteImpfungWithImpfschutz,
						freigabeOffsetLetzteImpfungWithImpfschutzUnit).toLocalDate().atStartOfDay();
				} else {
					freigegebenAbBasedOnImpfung = lastImpfungTimestamp.plus(
						freigabeOffsetLetzteImpfungWithoutImpfschutz,
						freigabeOffsetLetzteImpfungWithoutImpfschutzUnit).toLocalDate().atStartOfDay();
				}
			}
		}
		return freigegebenAbBasedOnImpfung;
	}

	private boolean isRegelForCurrentImpfung(int anzahlImpfungen) {
		boolean valid = true;
		if (giltAbAnzahlErhaltenerImpfungen != null) {
			valid = anzahlImpfungen >= giltAbAnzahlErhaltenerImpfungen;
		}
		if (giltBisAnzahlErhaltenerImpfungen != null) {
			valid = valid && anzahlImpfungen <= giltBisAnzahlErhaltenerImpfungen;
		}
		return valid;
	}

	@Nullable
	private LocalDate calculateStichtagThatWillEnterAltersRange(@NonNull Registrierung registrierung, int alter) {
		if (alterVon != null) {
			if (alter < alterVon) {
				if (alterVon - alter == 1) {
					// Erreicht die Person das Alter innerhalb eines Jahres, wird dies beruecksichtigt.
					return registrierung.getGeburtsdatum().plusYears(alterVon);
				}
			}
		}
		return null;
	}

	public static IBoosterPrioritaetRule createMinAgeRule(
		@NonNull ImpfstoffInfosForRules specifiedImpfstoffe,
		int minAge,
		int freigabeOffsetLetzteImpfungNoImpfschutz,
		@NonNull ChronoUnit freigabeOffsetLetzteImpfungNoImpfschutzUnit,
		int freigabeOffsetLetzteImpfungVollstImpfschutz,
		@NonNull ChronoUnit freigabeOffsetLetzteImpfungVollstImpfschutzUnit,
		@Nullable Integer giltAbAnzahlErhaltenerImpfungen,
		@Nullable Integer giltBisAnzahlErhaltenerImpfungen
	) {
		return new AffenpockenImpfschutzRule(
			specifiedImpfstoffe,
			minAge, null,
			freigabeOffsetLetzteImpfungNoImpfschutz, freigabeOffsetLetzteImpfungNoImpfschutzUnit,
			freigabeOffsetLetzteImpfungVollstImpfschutz, freigabeOffsetLetzteImpfungVollstImpfschutzUnit,
			giltAbAnzahlErhaltenerImpfungen, giltBisAnzahlErhaltenerImpfungen
		);
	}

	public static IBoosterPrioritaetRule createOhneFreigabeRule(
		@NonNull ImpfstoffInfosForRules specifiedImpfstoffe
	) {
		return new AffenpockenImpfschutzRule(
			specifiedImpfstoffe,
			null, null,
			null, null,
			null, null,
			null, null
		);
	}
}
