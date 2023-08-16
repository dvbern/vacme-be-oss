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
import java.util.Objects;
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
 * This class implemetns a single rule for a FSME Impfschutz calculation
 * It takes certain ranges like alterVon alterBis and anzahlErhalternerImpfungen for which it can calculate
 * an Impfschutz
 */
public class FSMEImpfschutzRule implements IBoosterPrioritaetRule {

	@Nullable
	private final Integer alterVon;

	@Nullable
	private final Integer alterBis;

	@Nullable
	private final Integer giltAbAnzahlErhaltenerImpfungen;

	@Nullable
	private final Integer giltBisAnzahlErhaltenerImpfungen;

	@Nullable
	private final Integer freigabeOffset;

	@Nullable
	private final ChronoUnit freigabeOffsetUnit;

	@NonNull
	private final ImpfstoffInfosForRules specifiedImpfstoffe;

	@Nullable
	private final Set<UUID> appliesForImpfstoffe;


	public FSMEImpfschutzRule(
		@NonNull ImpfstoffInfosForRules specifiedImpfstoffe,
		@Nullable Set<UUID> appliesForImpfstoffe,
		@Nullable Integer alterVon,
		@Nullable Integer alterBis,
		@Nullable Integer freigabeOffset,
		@Nullable ChronoUnit freigabeOffsetUnit,
		@Nullable Integer giltAbAnzahlErhaltenerImpfungen,
		@Nullable Integer giltBisAnzahlErhaltenerImpfungen
	) {
		this.alterVon = alterVon;
		this.alterBis = alterBis;
		this.freigabeOffset = freigabeOffset;
		this.freigabeOffsetUnit = freigabeOffsetUnit;
		this.giltAbAnzahlErhaltenerImpfungen = giltAbAnzahlErhaltenerImpfungen;
		this.giltBisAnzahlErhaltenerImpfungen = giltBisAnzahlErhaltenerImpfungen;
		this.specifiedImpfstoffe = specifiedImpfstoffe;
		this.appliesForImpfstoffe = appliesForImpfstoffe;
		validateUnitAndOffsetIsPresent();
	}

	private void validateUnitAndOffsetIsPresent() {
		Validate.isTrue(
			(freigabeOffset == null && freigabeOffsetUnit == null)
				|| (freigabeOffset != null && freigabeOffsetUnit != null),
			"freigabeOffset and freigabeOffsetUnit must be specified together (both either set or unset)");
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
		int anzahlImpfungen = orderedImpfInfos.stream().map(ImpfInfo::getAnzahlImpfungen).reduce(0, Integer::sum);

		ImpfInfo newestImpfInfo =
			Iterables.getLast(orderedImpfInfos, null); // latest Vacme Impfung or externesZertifikat

		// CHECK IF RULE APPLIES OR RETURN EMPTY
		if (!matchesAnyImpfstoff(newestImpfInfo)) {
			return Optional.empty();
		}
		if (!matchesAlterRange(alter)) {
			return Optional.empty();
		}

		// CALCULATIONS
		boolean hasGrundimmunisierung = impfinformationDto.getImpfdossier().getVollstaendigerImpfschutzTyp() != null;

		Set<UUID> erlaubteImpfstoffe = calculateErlaubteImpfstoffe(newestImpfInfo);
		LocalDate stichtagThatWillEnterAltersRange = calculateStichtagThatWillEnterAltersRange(registrierung, alter);

		LocalDateTime freigegebenAb = calculateFreigegebenAbEkif(newestImpfInfo, stichtagThatWillEnterAltersRange, erlaubteImpfstoffe, anzahlImpfungen);
		boolean benachrichtigungBeiFreigabe = BoosterPrioUtil.calculateDefaultBenachrichtigung(registrierung);

		LocalDateTime immunisiertBis = hasGrundimmunisierung ? freigegebenAb : null;

		// IMPFSCHUTZ
		Impfschutz impfschutz = new Impfschutz(
			immunisiertBis,
			freigegebenAb,
			null,
			erlaubteImpfstoffe,
			benachrichtigungBeiFreigabe);

		return Optional.of(impfschutz);
	}

	/**
	 * checks if the Rule is applicable to the Impfstoff of the last Impfinfo
	 * @param newestImpfinfo Last Impfung, will be used to determine the Impfstoff of the last Impfung used to decide
	 * if the rule is applicable
	 * @return true if rule is applicable, false otherwise
	 */
	private boolean matchesAnyImpfstoff(@Nullable ImpfInfo newestImpfinfo) {
		if (newestImpfinfo != null && appliesForImpfstoffe != null) {
			return this.appliesForImpfstoffe.stream().anyMatch(uuid -> newestImpfinfo.getImpfstoff().getId().equals(uuid));
		}
		return true;
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
		if (freigabeOffsetUnit != null
			&& freigabeOffset != null
		) {
			long estimatedDaysInDuration =
				freigabeOffsetUnit.getDuration().multipliedBy(
					freigabeOffset).toDays();
			long estimatedDaysInAMonth = ChronoUnit.MONTHS.getDuration().toDays();
			long months = Math.floorDiv(estimatedDaysInDuration, estimatedDaysInAMonth);
			return Long.valueOf(months).intValue();
		}
		return 0;
	}

	@NonNull
	private Set<UUID> calculateErlaubteImpfstoffe(ImpfInfo newestImpfInfo) {
		if (newestImpfInfo == null) {
			return specifiedImpfstoffe.getImpfstoffeEmpfohlenForBoosterForKrankheit(KrankheitIdentifier.FSME)
				.stream()
				.map(Impfstoff::getId)
				.collect(Collectors.toSet());
		}
		return Set.of(newestImpfInfo.getImpfstoff().getId()); // only the last Impfstoff is allowed
	}

	@Nullable
	private LocalDateTime calculateFreigegebenAbEkif(
		@Nullable ImpfInfo newestImpfInfo,
		@Nullable LocalDate alterRegelGueltigAb,
		@NonNull Set<UUID> erlaubteImpfstoffe,
		int anzahlImpfungen
	) {
		// Wenn die erlaubten Impfstoffe leer sind: Freigabedatum = null!
		if (erlaubteImpfstoffe.isEmpty()) {
			return null;
		}

		LocalDateTime freigegebenAb = null;

		// check if rule is relevant based on the number of already taken Impfungen for this Krankheit
		boolean regelRelevant = isRegelForCurrentImpfung(anzahlImpfungen);

		if (regelRelevant) {
			freigegebenAb = calcFreigegebenAbBasedOnImpfung(newestImpfInfo);

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
		@Nullable ImpfInfo newestImpfInfo
	) {
		LocalDateTime freigegebenAbBasedOnImpfung = null;
		if (freigabeOffset != null
			&& freigabeOffsetUnit != null
		) {
			boolean isLastImpfungDateUnknown = newestImpfInfo == null;
			if (isLastImpfungDateUnknown) {
				freigegebenAbBasedOnImpfung =
					LocalDate.now().atStartOfDay(); // Assume no Impfung -> Sofort Freigegeben
			} else {
				LocalDateTime lastImpfungTimestamp = newestImpfInfo.getTimestampImpfung();
				Objects.requireNonNull(lastImpfungTimestamp, "Last Impfdatum must be known for FSME");
				freigegebenAbBasedOnImpfung = lastImpfungTimestamp.plus(
					freigabeOffset,
					freigabeOffsetUnit).toLocalDate().atStartOfDay();

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

	public static IBoosterPrioritaetRule createOhneFreigabeRule(
		@NonNull ImpfstoffInfosForRules specifiedImpfstoffe
	) {
		return new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			null,
			null, null,
			null, null,
			null, null
		);
	}
}
