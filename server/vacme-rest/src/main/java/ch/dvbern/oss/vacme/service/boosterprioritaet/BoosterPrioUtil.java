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

package ch.dvbern.oss.vacme.service.boosterprioritaet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.IBoosterPrioritaetRule;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.EnumUtil;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationenUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang.DATA_MIGRATION;
import static ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang.MASSENUPLOAD;

public final class BoosterPrioUtil {

	private BoosterPrioUtil() {
	}

	/**
	 * Ermittelt die neuste Impfung unter Betrachtung der internen VacMe Impfungen und der extern erfassten Impfinfos
	 *
	 * @param impfinformationDto interne (bzw. in Vacme dokumenteirte und gepruefte) Impfungen
	 * @return Datum der neusten Impfung
	 */
	@Nullable
	public static LocalDate getDateOfNewestImpfung(@NonNull ImpfinformationDto impfinformationDto) {
		ImpfInfo newestVacmeImpfung = getNewestVacmeImpfung(impfinformationDto);
		ImpfInfo externeImpfInfoOrNull = impfinformationDto.getExternesZertifikat(); // auch unvollstaendige zaehlen
		ImpfInfo impfInfo = getNewerImpfInfo(newestVacmeImpfung, externeImpfInfoOrNull);
		return impfInfo == null || impfInfo.getTimestampImpfung() == null
			? null
			: impfInfo.getTimestampImpfung().toLocalDate();
	}

	/**
	 * Extract a list of ImpfInfo objects from the ImpfiformationDTO and order that list by Impfdatum in natural
	 * ascending Order.
	 * If an Impfinformation has no date present (Impfdatum is null) then it will be first in order
	 * @param impfinformationDto input object
	 * @return List of ordered Impfinfos
	 */
	@NonNull
	public static List<ImpfInfo> getImpfinfosOrderedByTimestampImpfung(@NonNull ImpfinformationDto impfinformationDto) {
		List<ImpfInfo> impfInfos = new ArrayList<>();
		impfInfos.add(impfinformationDto.getExternesZertifikat());
		impfInfos.add(impfinformationDto.getImpfung1());
		impfInfos.add(impfinformationDto.getImpfung2());
		if (impfinformationDto.getBoosterImpfungen() != null) {
			impfInfos.addAll(impfinformationDto.getBoosterImpfungen());
		}
		return impfInfos.stream().filter(Objects::nonNull).sorted(
			Comparator.comparing(ImpfInfo::getTimestampImpfung, Comparator.nullsFirst(Comparator.naturalOrder()))
		).collect(Collectors.toList());
	}

	@NonNull
	public static List<ImpfInfo> getImpfinfosOrderedByImpffolgeNr(@NonNull ImpfinformationDto impfinformationDto) {
		List<ImpfInfo> impfInfos = new ArrayList<>();
		if (impfinformationDto.getExternesZertifikat() != null) {
			impfInfos.add(impfinformationDto.getExternesZertifikat());
		}

		List<Impfung> impfungenOrderedByImpffolgeNr =
			ImpfinformationenUtil.getImpfungenOrderedByImpffolgeNr(impfinformationDto);
		impfInfos.addAll(impfungenOrderedByImpffolgeNr);

		return impfInfos;
	}

	@Nullable
	private static ImpfInfo getNewerImpfInfo(@Nullable ImpfInfo vacMeImpfInfo, @Nullable ImpfInfo externImpfInfo) {
		if (vacMeImpfInfo == null && externImpfInfo == null) {
			return null;
		}
		if (vacMeImpfInfo == null || vacMeImpfInfo.getTimestampImpfung() == null) {
			return externImpfInfo;
		}
		if (externImpfInfo == null || externImpfInfo.getTimestampImpfung() == null) {
			return vacMeImpfInfo;
		}

		if (externImpfInfo.getTimestampImpfung().isAfter(vacMeImpfInfo.getTimestampImpfung())) {
			return externImpfInfo;
		} else {
			return vacMeImpfInfo;
		}
	}

	@Nullable
	private static Impfung getNewestVacmeImpfung(@NonNull ImpfinformationDto impfinformationDto) {
		return ImpfinformationenService.getNewestVacmeImpfung(impfinformationDto);
	}

	/**
	 * prueft ob das Dossier die Bedingungen erfuellt um nach FREIGEGEBEN_BOOSTER verschoben zu werden.
	 */
	public static boolean meetsCriteriaForFreigabeBooster(@NonNull Impfdossier dossier, @Nullable Impfschutz impfschutz) {
		return
			(dossier.getDossierStatus().equals(ImpfdossierStatus.IMMUNISIERT))
				&& impfschutz != null
				&& impfschutz.getFreigegebenNaechsteImpfungAb() != null
				&& !impfschutz.getFreigegebenNaechsteImpfungAb().isAfter(LocalDateTime.now())
				&& !Boolean.TRUE.equals(dossier.getRegistrierung().getVerstorben());
	}

	public static int countNumberOfBoosterImpfungen(@NonNull List<ImpfInfo> orderedImpfInfos) {
		return (int) orderedImpfInfos.stream().filter(i -> i != null && !i.gehoertZuGrundimmunisierung()).count();
	}

	@NonNull
	public static Optional<Impfschutz> mergeImpfschutzOptionals(
		@NonNull Optional<Impfschutz> impfschutzOptional1,
		@NonNull Optional<Impfschutz> impfschutzOptional2
	){
		if (impfschutzOptional1.isPresent()) {
			// 1 vorhanden, 2 nicht: 1
			if (impfschutzOptional2.isEmpty()) {
				return impfschutzOptional1;
			}
			// beide vorhanden: frueheres nehmen
			return Optional.of(BoosterPrioUtil.mergeImpfschuetze(impfschutzOptional1.get(), impfschutzOptional2.get()));
		}
		// 1 nicht vorhanden: 2
		return impfschutzOptional2;
	}

	public static void orderListByAnzahlMonateBisFreigabe(@NonNull List<IBoosterPrioritaetRule> rules) {
		Comparator<IBoosterPrioritaetRule> comparator =
			Comparator.comparing(
				IBoosterPrioritaetRule::getAnzahlMonateBisFreigabe, Comparator.nullsLast(Comparator.naturalOrder())
			);
		rules.sort(comparator);
	}

	/**
	 * This merges two exsisting Impfschutz objects into one.
	 * The merge-logic always takes the value for a field that gives the best result for the
	 * Impfwillige-Person
	 * @param impfschutzA
	 * @param impfschutzB
	 * @return new object that contains the merged Impfschutz
	 */
	@NonNull
	public static Impfschutz mergeImpfschuetze(
		@NonNull Impfschutz impfschutzA,
		@NonNull Impfschutz impfschutzB
	) {
		@Nullable LocalDateTime mergedImmunisiertBis =
			DateUtil.getLaterDateTimeOrNull(impfschutzA.getImmunisiertBis(), impfschutzB.getImmunisiertBis());
		@Nullable LocalDateTime mergedFreigegebenAb =
			DateUtil.getEarlierDateTimeOrNull(impfschutzA.getFreigegebenNaechsteImpfungAb(), impfschutzB.getFreigegebenNaechsteImpfungAb());
		@Nullable LocalDateTime mergedFreigegebenAbSelbstzahler =
			DateUtil.getEarlierDateTimeOrNull(impfschutzA.getFreigegebenAbSelbstzahler(), impfschutzB.getFreigegebenAbSelbstzahler());
		Set<UUID> mergedErlaubteImpfstoffe =
			new HashSet<>(impfschutzA.getErlaubteImpfstoffeCollection());
		mergedErlaubteImpfstoffe.addAll(impfschutzB.getErlaubteImpfstoffeCollection());

		return new Impfschutz(
			mergedImmunisiertBis,
			mergedFreigegebenAb,
			mergedFreigegebenAbSelbstzahler,
			mergedErlaubteImpfstoffe,
			impfschutzA.isBenachrichtigungBeiFreigabe());
	}

	public static boolean calculateDefaultBenachrichtigung(@NonNull Registrierung registrierung) {
		// BENACHRICHTIGUNG BEI FREIGABE
		boolean benachrichtigungBeiFreigabe;
		if (MandantUtil.getMandant() == Mandant.ZH) {
			// Nicht benachrichtigt, werden anonymisierte oder Migrationsdaten aus Heimen
			benachrichtigungBeiFreigabe = !registrierung.isAnonymisiert()
				&& isNotExternaluserOfHeim(registrierung);
		} else {
			// Bei Massenupload und Datenmigration soll keine Benachrichtigung geschickt werden
			benachrichtigungBeiFreigabe = EnumUtil.isNoneOf(registrierung.getRegistrierungsEingang(), MASSENUPLOAD, DATA_MIGRATION);
		}
		return benachrichtigungBeiFreigabe;
	}

	private static boolean isNotExternaluserOfHeim(@NonNull Registrierung registrierung) {
		return registrierung.getExternalId() == null
			|| (!registrierung.getExternalId().startsWith("T_HEIM")
				&& !registrierung.getExternalId().startsWith("HEIM"));
	}
}
