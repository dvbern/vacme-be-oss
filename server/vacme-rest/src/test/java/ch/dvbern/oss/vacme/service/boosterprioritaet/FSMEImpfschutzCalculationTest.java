/*
 *
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software                                                                                                                                                                                                    : you can redistribute it and/or modify
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
 * along with this program.  If not, see <https                                                                                                                                                                                     : //www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service.boosterprioritaet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.FSMEImpfschutzRule;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.IBoosterPrioritaetRule;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

class FSMEImpfschutzCalculationTest {

	private FSMEImpfschutzcalculationService fsmeImpfschutzcalculationService;
	private ImpfstoffInfosForRules specifiedImpfstoffe;
	private static final int MIN_AGE = 6;

	private static final int FREIGABE_OFFSET_NO_IMPFSCHUTZ_FIRST_IMPFUNG = 4;
	private static final ChronoUnit FREIGABE_OFFSET_NO_IMPFSCHUTZ_FIRST_IMPFUNG_UNIT = ChronoUnit.WEEKS;

	private static final int FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_FSME_IMMUNE = 5;
	private static final ChronoUnit FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_UNIT_FSME_IMMUNE = ChronoUnit.MONTHS;

	private static final int FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_ENCEPUR = 9;
	private static final ChronoUnit FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_UNIT_ENCEPUR = ChronoUnit.MONTHS;

	private static final int FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_IMPFUNG_ENCEPUR = 7;
	private static final ChronoUnit FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_IMPFUNG_UNIT_ENCEPUR =
		ChronoUnit.DAYS;

	private static final int FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_SECOND_IMPFUNG_ENCEPUR = 14;
	private static final ChronoUnit FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_SECOND_IMPFUNG_UNIT_ENCEPUR =
		ChronoUnit.DAYS;

	private static final int FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_BOOSTER_ENCEPUR = 12;
	private static final ChronoUnit FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_BOOSTER_UNIT_ENCEPUR =
		ChronoUnit.MONTHS;

	private static final int FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_IMPFUNG_FSME_IMMUNE = 14;
	private static final ChronoUnit FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_IMPFUNG_UNIT_FSME_IMMUNE =
		ChronoUnit.DAYS;

	private static final int FREIGABE_OFFSET_IMPFSCHUTZ = 10;
	private static final ChronoUnit FREIGABE_OFFSET_IMPFSCHUTZ_UNIT = ChronoUnit.YEARS;

	private final Impfstoff encepur = TestdataCreationUtil.createImpfstoffEncepur();
	private final Impfstoff fsmeImmune = TestdataCreationUtil.createImpfstoffFsmeImmune();

	@BeforeEach
	void setUp() {
		System.setProperty("vacme.mandant", "BE");
		this.specifiedImpfstoffe = createSpecifiedImpfstoffeForRules();
		final VacmeSettingsService vacmeSettingsServiceMock = Mockito.mock(VacmeSettingsService.class);
		fsmeImpfschutzcalculationService =
			new FSMEImpfschutzcalculationService(specifiedImpfstoffe, vacmeSettingsServiceMock);
	}

	@NonNull
	private ImpfstoffInfosForRules createSpecifiedImpfstoffeForRules() {
		List<Impfstoff> allImpfstoffeForTest = List.of(
			TestdataCreationUtil.createImpfstoffFsmeImmune(),
			TestdataCreationUtil.createImpfstoffEncepur()
		);
		return new ImpfstoffInfosForRules(allImpfstoffeForTest);
	}

	private void initKonventionelleRules() {
		final List<IBoosterPrioritaetRule> rules = new ArrayList<>();

		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID, Constants.ENCEPUR_UUID),
			MIN_AGE, null,
			0,
			ChronoUnit.DAYS,
			0, 0
		));

		// For both Impfstoffe the first offset is 4 weeks.
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID, Constants.ENCEPUR_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_FIRST_IMPFUNG,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_FIRST_IMPFUNG_UNIT,
			1, 1
		));

		// For FSME-Immune the second offset is 5 months
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_FSME_IMMUNE,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_UNIT_FSME_IMMUNE,
			2, 2
		));

		// For Encepur the second offset is 9 months
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_ENCEPUR,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_UNIT_ENCEPUR,
			2, 2
		));

		// For both Impfstoffe the booster offset is 10 years.
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID, Constants.ENCEPUR_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_IMPFSCHUTZ,
			FREIGABE_OFFSET_IMPFSCHUTZ_UNIT,
			3, null
		));

		// rule die alle Regs matched und nur das immunisiertBis berechnet
		rules.add(FSMEImpfschutzRule.createOhneFreigabeRule(specifiedImpfstoffe));
		fsmeImpfschutzcalculationService.konventionelleRules.clear();
		fsmeImpfschutzcalculationService.konventionelleRules.addAll(rules);
	}

	private void initSchnellschemaRules() {
		final List<IBoosterPrioritaetRule> rules = new ArrayList<>();

		//// SCHNELLSCHEMA ////

		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID, Constants.ENCEPUR_UUID),
			MIN_AGE, null,
			0,
			ChronoUnit.DAYS,
			0, 0
		));

		// For FSME-Immune schnellschema first offset is 14 days.
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_IMPFUNG_FSME_IMMUNE,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_IMPFUNG_UNIT_FSME_IMMUNE,
			1, 1
		));

		// For Encepur schnellschema first offset is 7 days.
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_IMPFUNG_ENCEPUR,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_IMPFUNG_UNIT_ENCEPUR,
			1, 1
		));

		// For FSME-Immune schnellschema the second offset is 5 months
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_FSME_IMMUNE,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_UNIT_FSME_IMMUNE,
			2, 2
		));

		// For Encepur schnellschema the second offset is 14 days
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_SECOND_IMPFUNG_ENCEPUR,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_SECOND_IMPFUNG_UNIT_ENCEPUR,
			2, 2
		));

		// For FSME-Immune schnellschema the booster offset is 10 years.
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.FSME_IMMUNE_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_IMPFSCHUTZ,
			FREIGABE_OFFSET_IMPFSCHUTZ_UNIT,
			3, null
		));

		// For Encepur schnellschema the first booster offset is 12 months.
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_BOOSTER_ENCEPUR,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_SCHNELLSCHEMA_FIRST_BOOSTER_UNIT_ENCEPUR,
			3, 3
		));

		// For Encepur schnellschema the offset for second booster onward is 10 years.
		rules.add(new FSMEImpfschutzRule(
			specifiedImpfstoffe,
			Set.of(Constants.ENCEPUR_UUID),
			MIN_AGE, null,
			FREIGABE_OFFSET_IMPFSCHUTZ,
			FREIGABE_OFFSET_IMPFSCHUTZ_UNIT,
			4, null
		));

		// rule die alle Regs matched und nur das immunisiertBis berechnet
		rules.add(FSMEImpfschutzRule.createOhneFreigabeRule(specifiedImpfstoffe));
		fsmeImpfschutzcalculationService.schnellschemaRules.clear();
		fsmeImpfschutzcalculationService.schnellschemaRules.addAll(rules);
	}

	@ParameterizedTest
	@CsvSource({
		// Alter	| Impfstoff		| ANZ_VAC 		| LetzteImpfung | FreigabeEkif | ImpfschutzBis
		// Alter 4,keine Impfung erlaubt
		"4         , FSME-Immune	, 0              ,               ,              ,",
		"4         , FSME-Immune	, 1              , 01.01.2021    ,              ,",
		"4         , FSME-Immune	, 2              , 01.01.2021    ,              ,",
		"4         , FSME-Immune	, 3              , 01.01.2021    ,              ,",
		"4         , FSME-Immune	, 4              , 01.01.2021    ,              ,",
		// Alter 4,keine Impfung erlaubt
		"4         , Encepur		, 0              ,               ,              ,",
		"4         , Encepur		, 1              , 01.01.2021    ,              ,",
		"4         , Encepur		, 2              , 01.01.2021    ,              ,",
		"4         , Encepur		, 3              , 01.01.2021    ,              ,",
		"4         , Encepur		, 4              , 01.01.2021    ,              ,",
		// Alter 18 beliebig viele Booster erlaubt
		"18         , FSME-Immune	, 0              ,               , today        ,",
		"18         , FSME-Immune	, 1              , 01.01.2021    , 29.01.2021   ,",
		"18         , FSME-Immune	, 2              , 01.01.2021    , 01.06.2021   ,",
		"18         , FSME-Immune	, 3              , 01.01.2021    , 01.01.2031   ,01.01.2031",
		"18         , FSME-Immune	, 4              , 01.01.2021    , 01.01.2031   ,01.01.2031",
		// Alter 18 beliebig viele Booster erlaubt
		"18         , Encepur		, 0              ,               , today        ,",
		"18         , Encepur		, 1              , 01.01.2021    , 29.01.2021   ,",
		"18         , Encepur		, 2              , 01.01.2021    , 01.10.2021   ,",
		"18         , Encepur		, 3              , 01.01.2021    , 01.01.2031   ,01.01.2031",
		"18         , Encepur		, 4              , 01.01.2021    , 01.01.2031   ,01.01.2031",
	})
	public void testFSMEFreigabedatumAndImpfschutz(
		int alter,
		String sImpfstoff,
		int anzahlImpfungen,
		String sDatumLetzteImpfung,
		String sDatumExpectedFreigabeEkif,
		String sDatumExpectedImpfschutzBis
	) {
		initKonventionelleRules();
		Impfstoff impfstoff = parseImpfstoff(sImpfstoff);
		ImpfinformationBuilder builder =
			createBuilder(alter, impfstoff, anzahlImpfungen, sDatumLetzteImpfung, false);
		calculateImpfschutzAndCheckResults(null, sDatumExpectedFreigabeEkif, sDatumExpectedImpfschutzBis, builder);
	}

	@ParameterizedTest
	@CsvSource({
		// Alter | Impfstoff   | ANZ_VAC | LetzteImpfung | FreigabeEkif | ImpfschutzBis
		// Alter 4,keine Impfung erlaubt
		"4       , FSME-Immune , 0       ,               ,              , ",
		"4       , FSME-Immune , 1       , 01.01.2021    ,              , ",
		"4       , FSME-Immune , 2       , 01.01.2021    ,              , ",
		"4       , FSME-Immune , 3       , 01.01.2021    ,              , ",
		"4       , FSME-Immune , 4       , 01.01.2021    ,              , ",
		"4       , FSME-Immune , 5       , 01.01.2021    ,              , ",
		// Alter 4,keine Impfung erlaubt
		"4       , Encepur     , 0       ,               ,              , ",
		"4       , Encepur     , 1       , 01.01.2021    ,              , ",
		"4       , Encepur     , 2       , 01.01.2021    ,              , ",
		"4       , Encepur     , 3       , 01.01.2021    ,              , ",
		"4       , Encepur     , 4       , 01.01.2021    ,              , ",
		"4       , Encepur     , 5       , 01.01.2021    ,              , ",
		// Alter 18 beliebig viele Booster erlaubt
		"18      , FSME-Immune , 0       ,               , today        , ",
		"18      , FSME-Immune , 1       , 01.01.2021    , 15.01.2021   , ",
		"18      , FSME-Immune , 2       , 01.01.2021    , 01.06.2021   , ",
		"18      , FSME-Immune , 3       , 01.01.2021    , 01.01.2031   , 01.01.2031",
		"18      , FSME-Immune , 4       , 01.01.2021    , 01.01.2031   , 01.01.2031",
		// Alter 18 beliebig viele Booster erlaubt
		"18      , Encepur     , 0       ,               , today        , ",
		"18      , Encepur     , 1       , 01.01.2021    , 08.01.2021   , ",
		"18      , Encepur     , 2       , 01.01.2021    , 15.01.2021   , ",
		"18      , Encepur     , 3       , 01.01.2021    , 01.01.2022   , 01.01.2022",
		"18      , Encepur     , 4       , 01.01.2021    , 01.01.2031   , 01.01.2031",
		"18      , Encepur     , 5       , 01.01.2021    , 01.01.2031   , 01.01.2031",

	})
	public void testFSMEFreigabedatumAndImpfschutzSchnellschema(
		int alter,
		String sImpfstoff,
		int anzahlImpfungen,
		String sDatumLetzteImpfung,
		String sDatumExpectedFreigabeEkif,
		String sDatumExpectedImpfschutzBis
	) {
		initSchnellschemaRules();
		Impfstoff impfstoff = parseImpfstoff(sImpfstoff);
		ImpfinformationBuilder builder =
			createBuilder(alter, impfstoff, anzahlImpfungen, sDatumLetzteImpfung, true);
		calculateImpfschutzAndCheckResults(null, sDatumExpectedFreigabeEkif, sDatumExpectedImpfschutzBis, builder);
	}

	@ParameterizedTest
	@CsvSource(
		useHeadersInDisplayName = false,
		delimiterString = ";",
		value = {
		//"# ; ANZ_ExZ ; IMPFSTOFF   ; DATUM_EZ   ; ANZ_VAC ; LAST_VAC_DATE ; VOLLST                          ; Freigabe   ; Impfschutz",
		"1   ; 0       ; FSME-Immune ;            ; 0       ;               ; null                            ; today      ; ",
		"2   ; 1       ; FSME-Immune ; 01.01.2022 ; 0       ;               ; null                            ; 29.01.2022 ; ",
		"3   ; 2       ; FSME-Immune ; 01.01.2021 ; 0       ;               ; null                            ; 01.06.2021 ; ",
		"4   ; 3       ; FSME-Immune ; 01.01.2021 ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2031 ; 01.01.2031",
		"5   ; 4       ; FSME-Immune ; 01.01.2021 ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2031 ; 01.01.2031",
		"6   ; 0       ; Encepur     ;            ; 0       ;               ; null                            ; today      ; ",
		"7   ; 1       ; Encepur     ; 01.01.2022 ; 0       ;               ; null                            ; 29.01.2022 ; ",
		"8   ; 2       ; Encepur     ; 01.01.2021 ; 0       ;               ; null                            ; 01.10.2021 ; ",
		"9   ; 3       ; Encepur     ; 01.01.2021 ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2031 ; 01.01.2031",
		"10  ; 4       ; Encepur     ; 01.01.2021 ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2031 ; 01.01.2031",
		"11  ; 1       ; FSME-Immune ; 01.01.2021 ; 1       ; 01.01.2022    ; null                            ; 01.06.2022 ; ",
		"12  ; 2       ; FSME-Immune ; 01.01.2021 ; 1       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
		"13  ; 3       ; FSME-Immune ; 01.01.2021 ; 1       ; 01.01.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2032 ; 01.01.2032",
		"14  ; 1       ; Encepur     ; 01.01.2021 ; 1       ; 01.01.2022    ; null                            ; 01.10.2022 ; ",
		"15  ; 2       ; Encepur     ; 01.01.2021 ; 1       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
		"16  ; 3       ; Encepur     ; 01.01.2021 ; 1       ; 01.01.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2032 ; 01.01.2032",
		"17  ; 1       ; FSME-Immune ; 01.01.2021 ; 2       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
		"18  ; 2       ; FSME-Immune ; 01.01.2021 ; 2       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
		"19  ; 1       ; Encepur     ; 01.01.2021 ; 2       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
		"20  ; 2       ; Encepur     ; 01.01.2021 ; 2       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
		"21  ; 1       ; FSME-Immune ; 01.01.2021 ; 3       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
		"22  ; 1       ; Encepur     ; 01.01.2021 ; 3       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
		}

	)
	public void testFsmeFreigabedatumAndImpfschutz(
		int row,
		int anzExtZert,
		String sImpfstoff,
		String sDatumEZ,
		int anzImpfungen,
		String sDatumImpfung,
		String expectedGrundimmunisiert,
		String expectedFreigabedatum,
		String expectedImpfschutzBis
	) {
		@Nullable LocalDate lastImpfungsdatumEz = parseDate(sDatumEZ);
		@Nullable LocalDate lastImpfungvacme = parseDate(sDatumImpfung);
		Impfstoff impfstoff = parseImpfstoff(sImpfstoff);
		initKonventionelleRules();
		ImpfinformationBuilder builder = createBuilder(
			anzExtZert,
			impfstoff,
			lastImpfungsdatumEz,
			anzImpfungen,
			lastImpfungvacme,
			false
		);
		calculateImpfschutzAndCheckResults(
			expectedGrundimmunisiert,
			expectedFreigabedatum,
			expectedImpfschutzBis,
			builder);
	}

	@ParameterizedTest
	@CsvSource(
		useHeadersInDisplayName = false,
		delimiterString = ";",
		value = {
			//"# ; ANZ_ExZ ; IMPFSTOFF   ; DATUM_EZ   ; ANZ_VAC ; LAST_VAC_DATE ; VOLLST                          ; Freigabe   ; Impfschutz",
			"1   ; 0       ; FSME-Immune ;            ; 0       ;               ; null                            ; today      ; ",
			"2   ; 1       ; FSME-Immune ; 01.01.2022 ; 0       ;               ; null                            ; 15.01.2022 ; ",
			"3   ; 2       ; FSME-Immune ; 01.01.2021 ; 0       ;               ; null                            ; 01.06.2021 ; ",
			"4   ; 3       ; FSME-Immune ; 01.01.2021 ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2031 ; 01.01.2031",
			"5   ; 4       ; FSME-Immune ; 01.01.2021 ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2031 ; 01.01.2031",
			"6   ; 0       ; Encepur     ;            ; 0       ;               ; null                            ; today      ; ",
			"7   ; 1       ; Encepur     ; 01.01.2022 ; 0       ;               ; null                            ; 08.01.2022 ; ",
			"8   ; 2       ; Encepur     ; 01.01.2021 ; 0       ;               ; null                            ; 15.01.2021 ; ",
			"9   ; 3       ; Encepur     ; 01.01.2021 ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2022 ; 01.01.2022",
			"10  ; 4       ; Encepur     ; 01.01.2021 ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2031 ; 01.01.2031",
			"11  ; 5       ; Encepur     ; 01.01.2021 ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2031 ; 01.01.2031",
			"12  ; 1       ; FSME-Immune ; 01.01.2021 ; 1       ; 01.01.2022    ; null                            ; 01.06.2022 ; ",
			"13  ; 2       ; FSME-Immune ; 01.01.2021 ; 1       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
			"14  ; 3       ; FSME-Immune ; 01.01.2021 ; 1       ; 01.01.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2032 ; 01.01.2032",
			"15  ; 1       ; Encepur     ; 01.01.2021 ; 1       ; 01.01.2022    ; null                            ; 15.01.2022 ; ",
			"16  ; 2       ; Encepur     ; 01.01.2021 ; 1       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2023 ; 01.01.2023",
			"17  ; 3       ; Encepur     ; 01.01.2021 ; 1       ; 01.01.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2032 ; 01.01.2032",
			"18  ; 4       ; Encepur     ; 01.01.2021 ; 1       ; 01.01.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.2032 ; 01.01.2032",
			"19  ; 1       ; FSME-Immune ; 01.01.2021 ; 2       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
			"20  ; 2       ; FSME-Immune ; 01.01.2021 ; 2       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
			"21  ; 1       ; Encepur     ; 01.01.2021 ; 2       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2023 ; 01.01.2023",
			"22  ; 2       ; Encepur     ; 01.01.2021 ; 2       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
			"23  ; 1       ; FSME-Immune ; 01.01.2021 ; 3       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
			"24  ; 1       ; Encepur     ; 01.01.2021 ; 3       ; 01.01.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.01.2032 ; 01.01.2032",
		}

	)
	public void testFsmeFreigabedatumAndImpfschutzSchnellschema(
		int row,
		int anzExtZert,
		String sImpfstoff,
		String sDatumEZ,
		int anzImpfungen,
		String sDatumImpfung,
		String expectedGrundimmunisiert,
		String expectedFreigabedatum,
		String expectedImpfschutzBis
	) {
		@Nullable LocalDate lastImpfungsdatumEz = parseDate(sDatumEZ);
		@Nullable LocalDate lastImpfungvacme = parseDate(sDatumImpfung);
		Impfstoff impfstoff = parseImpfstoff(sImpfstoff);
		initSchnellschemaRules();
		ImpfinformationBuilder builder = createBuilder(
			anzExtZert,
			impfstoff,
			lastImpfungsdatumEz,
			anzImpfungen,
			lastImpfungvacme,
			true
		);
		calculateImpfschutzAndCheckResults(
			expectedGrundimmunisiert,
			expectedFreigabedatum,
			expectedImpfschutzBis,
			builder);
	}

	@Test
	public void registrierungWithNoImpfungenShouldBeFreigegeben() {
		initKonventionelleRules();
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder.create(KrankheitIdentifier.FSME).withAge(20);
		calculateImpfschutzAndCheckResults(null, "today", null, builder);
	}

	private void calculateImpfschutzAndCheckResults(
		@Nullable String sExpectedVollstImpfschutzTyp,
		@Nullable String sDatumExpectedFreigabeEkif,
		@Nullable String sExpectedImpfschutzBis, ImpfinformationBuilder builder
	) {
		LocalDate expectedFreigabeEkif = parseDate(sDatumExpectedFreigabeEkif);
		LocalDate expectedImpfschutzBis = parseDate(sExpectedImpfschutzBis);
		VollstaendigerImpfschutzTyp expectedVollstImpfschutzTyp =
			parseExpectedImpfschutzTyp(sExpectedVollstImpfschutzTyp);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz =
			fsmeImpfschutzcalculationService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		if (sExpectedVollstImpfschutzTyp != null) {
			if (expectedVollstImpfschutzTyp != null) {
				Assertions.assertEquals(
					expectedVollstImpfschutzTyp,
					infos.getImpfdossier().getVollstaendigerImpfschutzTyp(),
					"vollstaendigerImpfschutzTyp is not as expected");
			} else {
				Assertions.assertNull(
					infos.getImpfdossier().getVollstaendigerImpfschutzTyp(),
					"vollstaendigerImpfschutzTyp should be null");
			}
		}
		if (expectedFreigabeEkif == null) {
			if (impfschutz != null) {
				Assertions.assertNull(
					impfschutz.getFreigegebenNaechsteImpfungAb(),
					"freigabeNacheste Impfung should be null");
			}
		} else {
			Assertions.assertNotNull(impfschutz);
			Assertions.assertEquals(
				expectedFreigabeEkif.atStartOfDay(),
				impfschutz.getFreigegebenNaechsteImpfungAb(),
				"freigabeAb is not as expected");
		}
		if (expectedImpfschutzBis == null) {
			if (impfschutz != null) {
				Assertions.assertNull(impfschutz.getImmunisiertBis(), "ImmunisiertBis should be null");
			}
		} else {
			Assertions.assertNotNull(impfschutz);
			Assertions.assertEquals(
				expectedImpfschutzBis.atStartOfDay(),
				impfschutz.getImmunisiertBis(), "ImmunisiertBis is not as expected");
		}
	}

	@Nullable
	private static VollstaendigerImpfschutzTyp parseExpectedImpfschutzTyp(@Nullable String sExpectedVollstImpfschutzTyp) {
		if (sExpectedVollstImpfschutzTyp != null && !"null".equals(sExpectedVollstImpfschutzTyp)) {
			return VollstaendigerImpfschutzTyp.valueOf(sExpectedVollstImpfschutzTyp);
		}
		return null;
	}

	@NonNull
	private ImpfinformationBuilder createBuilder(
		int alter,
		Impfstoff impfstoff,
		int anzahlBooster,
		String sDatumLetzteImpfung,
		boolean schnellschema
	) {
		LocalDate datumLetzteImpfung = parseDate(sDatumLetzteImpfung);

		ImpfinformationBuilder builder = new ImpfinformationBuilder();

		builder.create(KrankheitIdentifier.FSME)
			.withAge(alter)
			.withSchnellschema(schnellschema);

		for (int i = 1; i <= anzahlBooster; i++) {
			if (anzahlBooster > i) {
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung.minusYears(i), impfstoff, false);
			} else {
				// Dies ist die letzte
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung, impfstoff, false);
			}
		}
		return builder;
	}

	private ImpfinformationBuilder createBuilder(
		int anzExtZert,
		Impfstoff impfstoff,
		@Nullable LocalDate lastImpfungsdatumEz,
		int anzImpfungen,
		@Nullable LocalDate lastImpfungvacme,
		boolean schnellschema
	) {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();

		builder.create(KrankheitIdentifier.FSME)
			.withAge(50)
			.withPrioritaet(Prioritaet.N)
			.withSchnellschema(schnellschema);

		if (anzExtZert > 0) {
			builder.withExternesZertifikatOhneTest(impfstoff, anzExtZert, lastImpfungsdatumEz);
		}

		for (int i = 1; i <= anzImpfungen; i++) {
			if (anzImpfungen > i) {
				Assertions.assertNotNull(lastImpfungvacme);
				builder.withBooster(lastImpfungvacme.minusYears(i), impfstoff, false);
			} else {
				// Dies ist die letzte
				Assertions.assertNotNull(lastImpfungvacme);
				builder.withBooster(lastImpfungvacme, impfstoff, false);
			}
		}
		return builder;
	}

	@Nullable
	private LocalDate parseDate(@Nullable String dateString) {
		if (dateString == null) {
			return null;
		}

		if ("today".equals(dateString)) {
			return LocalDate.now();
		}

		DateTimeFormatter formatter = DateUtil.DEFAULT_DATE_FORMAT.apply(Locale.GERMANY);
		return LocalDate.parse(dateString, formatter);
	}

	@NonNull
	private Impfstoff parseImpfstoff(String impfstoffEZ) {
		if ("Encepur".equals(impfstoffEZ)) {
			return TestdataCreationUtil.createImpfstoffEncepur();
		}
		if ("FSME-Immune".equals(impfstoffEZ)) {
			return TestdataCreationUtil.createImpfstoffFsmeImmune();
		}

		throw new IllegalArgumentException("Unknown impfstoff: " + impfstoffEZ);
	}

	@Test
	public void testAllowedImpfstoffAfterEncepur() {
		initKonventionelleRules();
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		LocalDate impfung1Date = LocalDate.now().minusMonths(12);
		LocalDate impfung2Date = LocalDate.now().minusMonths(10);
		LocalDate impfung3Date = LocalDate.now();
		builder.create(KrankheitIdentifier.FSME)
			.withAge(25)
			.withBooster(impfung1Date, TestdataCreationUtil.createImpfstoffEncepur());

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz =
			fsmeImpfschutzcalculationService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);
		Assertions.assertNotNull(impfschutz);
		Assertions.assertNull(impfschutz.getImmunisiertBis());
		Assertions.assertEquals(
			impfung1Date
				.plus(FREIGABE_OFFSET_NO_IMPFSCHUTZ_FIRST_IMPFUNG, FREIGABE_OFFSET_NO_IMPFSCHUTZ_FIRST_IMPFUNG_UNIT)
				.atStartOfDay(),
			impfschutz.getFreigegebenNaechsteImpfungAb());
		Assertions.assertEquals(1, impfschutz.getErlaubteImpfstoffeCollection().size());
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.ENCEPUR_UUID));

		// add impfung2 with fsme_immune
		builder.withBooster(impfung2Date, TestdataCreationUtil.createImpfstoffFsmeImmune());
		final ImpfinformationDto infos2 = builder.getInfos();
		Impfschutz impfschutz2 =
			fsmeImpfschutzcalculationService.calculateImpfschutz(builder.getFragebogen(), infos2).orElse(null);
		Assertions.assertNotNull(impfschutz2);
		Assertions.assertNull(impfschutz2.getImmunisiertBis());
		Assertions.assertEquals(
			impfung2Date
				.plus(
					FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_FSME_IMMUNE,
					FREIGABE_OFFSET_NO_IMPFSCHUTZ_SECOND_IMPFUNG_UNIT_FSME_IMMUNE)
				.atStartOfDay(),
			impfschutz2.getFreigegebenNaechsteImpfungAb());
		Assertions.assertEquals(1, impfschutz2.getErlaubteImpfstoffeCollection().size());
		Assertions.assertTrue(impfschutz2.getErlaubteImpfstoffeCollection().contains(Constants.FSME_IMMUNE_UUID));

		// add impfung3 with ENCEPUR
		builder.withBooster(impfung3Date, TestdataCreationUtil.createImpfstoffEncepur());
		final ImpfinformationDto infos3 = builder.getInfos();
		Impfschutz impfschutz3 =
			fsmeImpfschutzcalculationService.calculateImpfschutz(builder.getFragebogen(), infos3).orElse(null);
		Assertions.assertNotNull(impfschutz3);
		Assertions.assertEquals(impfung3Date
			.plus(FREIGABE_OFFSET_IMPFSCHUTZ, FREIGABE_OFFSET_IMPFSCHUTZ_UNIT)
			.atStartOfDay(), impfschutz3.getImmunisiertBis());
		Assertions.assertEquals(
			impfung3Date
				.plus(FREIGABE_OFFSET_IMPFSCHUTZ, FREIGABE_OFFSET_IMPFSCHUTZ_UNIT)
				.atStartOfDay(),
			impfschutz3.getFreigegebenNaechsteImpfungAb());
		Assertions.assertEquals(1, impfschutz3.getErlaubteImpfstoffeCollection().size());
		Assertions.assertTrue(impfschutz3.getErlaubteImpfstoffeCollection().contains(Constants.ENCEPUR_UUID));

	}

	@Test
	public void testImpfstoffKombinationen() {
		testImpfstoffKombination(null, null, null); //encepur, fsmeImmune
		testImpfstoffKombination(encepur, encepur, encepur); //encepur
		testImpfstoffKombination(fsmeImmune, fsmeImmune, fsmeImmune); //fsmeImmune
		testImpfstoffKombination(encepur, encepur, fsmeImmune); //fsmeImmune
		testImpfstoffKombination(fsmeImmune, encepur, encepur); //encepur
		testImpfstoffKombination(encepur, fsmeImmune, null); // fsmeImmune
		testImpfstoffKombination(encepur, encepur, null); // encepur
		testImpfstoffKombination(encepur, null, null); // encepur
		testImpfstoffKombination(fsmeImmune, null, null); // fsmeImmune
	}

	void testImpfstoffKombination(
		@Nullable Impfstoff impfstoff1,
		@Nullable Impfstoff impfstoff2,
		@Nullable Impfstoff impfstoff3
	) {
		initKonventionelleRules();
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		LocalDate impfung1Date = LocalDate.now().minusMonths(12);
		LocalDate impfung2Date = LocalDate.now().minusMonths(10);
		LocalDate impfung3Date = LocalDate.now();
		builder.create(KrankheitIdentifier.FSME)
			.withAge(25);
		if (impfstoff1 != null) {
			builder.withBooster(impfung1Date, impfstoff1);
		}
		if (impfstoff2 != null) {
			builder.withBooster(impfung2Date, impfstoff2);
		}
		if (impfstoff3 != null) {
			builder.withBooster(impfung3Date, impfstoff3);
		}
		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz =
			fsmeImpfschutzcalculationService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);
		Objects.requireNonNull(impfschutz);

		Impfstoff lastImpfstoff =
			impfstoff3 != null ? impfstoff3 : impfstoff2 != null ? impfstoff2 : impfstoff1 != null ? impfstoff1 : null;

		final Set<UUID> erlaubteImpfstoffe = impfschutz.getErlaubteImpfstoffeCollection();

		if (lastImpfstoff == null) {
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.ENCEPUR_UUID));
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.FSME_IMMUNE_UUID));
		} else if (lastImpfstoff.getId().equals(Constants.ENCEPUR_UUID)) {
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.ENCEPUR_UUID));
		} else if (lastImpfstoff.getId().equals(Constants.FSME_IMMUNE_UUID)) {
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.FSME_IMMUNE_UUID));
		} else {
			throw new IllegalArgumentException("Unknown FSME impfstoff: " + lastImpfstoff);
		}
	}
}
