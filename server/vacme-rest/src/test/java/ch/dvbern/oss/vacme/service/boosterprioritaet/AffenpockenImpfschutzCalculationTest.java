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

import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.AffenpockenImpfschutzRule;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.IBoosterPrioritaetRule;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
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

class AffenpockenImpfschutzCalculationTest {

	private AffenpockenImpfschutzcalculationService affenpockenImpfschutzcalculationService;
	private ImpfstoffInfosForRules specifiedImpfstoffe;
	private final Impfstoff affenpockenImpfstoff = TestdataCreationUtil.createImpfstoffAffenpocken();
	private static final int MIN_AGE = 18;
	public static final int FREIGABE_OFFSET_NO_IMPFSCHUTZ= 4;
	public static final ChronoUnit FREIGABE_OFFSET_NO_IMPFSCHUTZ_UNIT= ChronoUnit.WEEKS;
	public static final int FREIGABE_OFFSET_IMPFSCHUTZ  = 2;
	public static final ChronoUnit FREIGABE_OFFSET_IMPFSCHUTZ_UNIT  = ChronoUnit.YEARS;

	@BeforeEach
	void setUp() {
		System.setProperty("vacme.mandant", "BE");
		this.specifiedImpfstoffe = createSpecifiedImpfstoffeForRules();
		affenpockenImpfschutzcalculationService = new AffenpockenImpfschutzcalculationService(specifiedImpfstoffe, Mockito.mock(VacmeSettingsService.class));
	}

	@NonNull
	private ImpfstoffInfosForRules createSpecifiedImpfstoffeForRules() {
		List<Impfstoff> allImpfstoffeForTest = List.of(
			TestdataCreationUtil.createImpfstoffAffenpocken()
		);
		return new ImpfstoffInfosForRules(allImpfstoffeForTest);
	}

	private void initRules() {
		final List<IBoosterPrioritaetRule> rules = new ArrayList<>();

		rules.add(AffenpockenImpfschutzRule.createMinAgeRule(
			specifiedImpfstoffe,
			MIN_AGE,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ,
			FREIGABE_OFFSET_NO_IMPFSCHUTZ_UNIT,
			FREIGABE_OFFSET_IMPFSCHUTZ,
			FREIGABE_OFFSET_IMPFSCHUTZ_UNIT,
			0,null
		));

		// rule die alle Regs matched und nur das immunisiertBis berechnet
		rules.add(AffenpockenImpfschutzRule.createOhneFreigabeRule(specifiedImpfstoffe));
		affenpockenImpfschutzcalculationService.rules.clear();
		affenpockenImpfschutzcalculationService.rules.addAll(rules);
	}

	@ParameterizedTest
	@CsvSource({
		// Alter	| Prio | Anzahl GI | Anzahl Booster | LetzteImpfung | FreigabeEkif | FreigabeSelbstzahler 		| ImpfschutzBis
		// Alter 10,keine Impfung erlaubt
		"10         , S	   , 0         , 0              ,               ,              ,  							,",
		"10         , S	   , 0         , 1              , 01.01.2021    ,              ,  							,",
		"10         , S	   , 0         , 2              , 01.01.2021    ,              ,  							,",
		"10         , S	   , 0         , 3              , 01.01.2021    ,              ,  							,",
		// Alter 14,keine Impfung erlaubt
		"14         , S	   , 0         , 0              ,               ,              ,  							,",
		"14         , S	   , 0         , 1              , 01.01.2021    ,              ,  							,",
		"14         , S	   , 0         , 2              , 01.01.2021    ,              ,  							,",
		"14         , S	   , 0         , 3              , 01.01.2021    ,              ,  							,",
		// Alter 20                                                                                                                                                                                                                        : beliebig viele Booster erlaubt
		"18         , M	   , 0         , 0              ,               , today        ,  today 					,",
		"18         , M	   , 0         , 1              , 01.01.2021    , 29.01.2021   ,  29.01.2021 				,",
		"18         , M	   , 0         , 2              , 01.01.2021    , 01.01.2023   ,  01.01.2023 				,01.01.2023",
		"18         , M	   , 0         , 3              , 01.01.2021    , 01.01.2023   ,  01.01.2023 				,01.01.2023",
		// Alter 20, aber Gruppe B beliebig viele Booster erlaubt
		"20         , B	   , 0         , 0              ,               , today        , today      				,",
		"20         , B	   , 0         , 1              , 01.01.2021    , 29.01.2021   , 29.01.2021 				,",
		"20         , B	   , 0         , 2              , 01.01.2021    , 01.01.2023   , 01.01.2023 				,01.01.2023",
		"20         , B	   , 0         , 3              , 01.01.2021    , 01.01.2023   , 01.01.2023 				,01.01.2023",
		// Alter 65                                                                                                                                                                                                                        : beliebig viele Booster erlaubt
		"65         , M    , 0         , 0              ,               , today        , today      				,",
		"65         , M	   , 0         , 1              , 01.01.2021    , 29.01.2021   , 29.01.2021 				,",
		"65         , M	   , 0         , 2              , 01.01.2021    , 01.01.2023   , 01.01.2023 				,01.01.2023",
		"65         , M	   , 0         , 3              , 01.01.2021    , 01.01.2023   , 01.01.2023 				,01.01.2023",
	})
	public void affenpockenHerbst22(
		int alter,
		String sPrioritaet,
		int anzahlGrundimmunisierungen,
		int anzahlBooster,
		String sDatumLetzteImpfung,
		String sDatumExpectedFreigabeEkif,
		String sDatumExpectedFreigabeSelbstzahler,
		String sDatumExpectedImpfschutzBis
	) {
		initRules();
		ImpfinformationBuilder builder =
			createBuilder(alter, sPrioritaet, anzahlGrundimmunisierungen, anzahlBooster, sDatumLetzteImpfung);
		calculateImpfschutzAndCheckResults(null, sDatumExpectedFreigabeEkif, sDatumExpectedFreigabeSelbstzahler,
			sDatumExpectedImpfschutzBis,
			builder);
	}

	@ParameterizedTest
	@CsvSource(
		useHeadersInDisplayName = false,
		delimiterString = ";",
		value = {
    //"# ; ANZ_ExZ ; IMPFSTOFF   ; DATUM_BEKANNT ; IMMUNSUPRIMIERT ; ANZ_VAC ; LAST_VAC_DATE ; VOLLST                          ; Freigabe   ; Impfschutz",
	"1   ; 1       ; unkown_vacc ; 01.01.1950    ; false           ; 0       ;               ; null                            ; 29.01.1950 ; ",
	"2   ; 2       ; unkown_vacc ; 01.01.1950    ; false           ; 0       ;               ; null                            ; 29.01.1950 ; ",
	"3   ; 3       ; unkown_vacc ; 01.01.1950    ; false           ; 0       ;               ; null                            ; 29.01.1950 ; ",
	"4   ; 1       ; unkown_vacc ;               ; false           ; 0       ;               ; null                            ; today      ; ",
	"5   ; 2       ; unkown_vacc ;               ; false           ; 0       ;               ; null                            ; today      ; ",
	"6   ; 3       ; unkown_vacc ;               ; false           ; 0       ;               ; null                            ; today      ; ",
	"7   ; 1       ; unkown_vacc ; 01.01.1960    ; true            ; 0       ;               ; null                            ; 29.01.1960 ; ",
	"8   ; 2       ; unkown_vacc ; 01.01.1960    ; true            ; 0       ;               ; null                            ; 29.01.1960 ; ",
	"9   ; 3       ; unkown_vacc ; 01.01.1960    ; true            ; 0       ;               ; null                            ; 29.01.1960 ; ",
	"10  ; 1       ; unkown_vacc ;               ; true            ; 0       ;               ; null                            ; today      ; ",
	"11  ; 2       ; unkown_vacc ;               ; true            ; 0       ;               ; null                            ; today      ; ",
	"12  ; 3       ; unkown_vacc ;               ; true            ; 0       ;               ; null                            ; today      ; ",
	"13  ; 1       ; MVA-BN      ; 01.01.1950    ; false           ; 0       ;               ; null                            ; 29.01.1950 ; ",
	"14  ; 2       ; MVA-BN      ; 01.01.1950    ; false           ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.1952 ; 01.01.1952  ",
	"15  ; 3       ; MVA-BN      ; 01.01.1950    ; false           ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.1952 ; 01.01.1952  ",
	"16  ; 1       ; MVA-BN      ;               ; false           ; 0       ;               ; null                            ; today      ; ",
	"17  ; 2       ; MVA-BN      ;               ; false           ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; today      ; today       ",
	"18  ; 3       ; MVA-BN      ;               ; false           ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; today      ; today       ",
	"19  ; 1       ; MVA-BN      ; 01.01.1950    ; true            ; 0       ;               ; null                            ; 29.01.1950 ; ",
	"20  ; 2       ; MVA-BN      ; 01.01.1950    ; true            ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.1952 ; 01.01.1952   ", // sonderfall weil insgesamt 2 mal mva-bn geimpft aber immunsuprimiert
	"21  ; 3       ; MVA-BN      ; 01.01.1950    ; true            ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.01.1952 ; 01.01.1952   ", // sonderfall weil insgesamt 2 mal mva-bn geimpft aber immunsuprimiert
	"22  ; 1       ; MVA-BN      ;               ; true            ; 0       ;               ; null                            ; today      ; ",
	"23  ; 2       ; MVA-BN      ;               ; true            ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; today      ; today        ",
	"24  ; 3       ; MVA-BN      ;               ; true            ; 0       ;               ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; today      ; today        ",
	"25  ; 1       ; unkown_vacc ; 01.01.1950    ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"26  ; 2       ; unkown_vacc ; 01.01.1950    ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"27  ; 3       ; unkown_vacc ; 01.01.1950    ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"28  ; 1       ; unkown_vacc ;               ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"29  ; 2       ; unkown_vacc ;               ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"30  ; 3       ; unkown_vacc ;               ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"31  ; 1       ; unkown_vacc ; 01.01.1950    ; true            ; 1       ; 01.10.2022    ; null                            ; 29.10.2022 ; ",
	"32  ; 2       ; unkown_vacc ; 01.01.1950    ; true            ; 1       ; 01.10.2022    ; null                            ; 29.10.2022 ; ",
	"33  ; 3       ; unkown_vacc ; 01.01.1950    ; true            ; 1       ; 01.10.2022    ; null                            ; 29.10.2022 ; ",
	"34  ; 1       ; unkown_vacc ;               ; true            ; 1       ; 01.10.2022    ; null                            ; 29.10.2022 ; ",
	"35  ; 2       ; unkown_vacc ;               ; true            ; 1       ; 01.10.2022    ; null                            ; 29.10.2022 ; ",
	"36  ; 3       ; unkown_vacc ;               ; true            ; 1       ; 01.10.2022    ; null                            ; 29.10.2022 ; ",
	"37  ; 1       ; MVA-BN      ; 01.01.1950    ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"38  ; 2       ; MVA-BN      ; 01.01.1950    ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"39  ; 3       ; MVA-BN      ; 01.01.1950    ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"40  ; 1       ; MVA-BN      ;               ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"41  ; 2       ; MVA-BN      ;               ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"42  ; 3       ; MVA-BN      ;               ; false           ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"43  ; 1       ; MVA-BN      ; 01.01.1950    ; true            ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ", // sonderfall weil insgesamt 2 mal mva-bn geimpft aber immunsuprimiert
	"44  ; 2       ; MVA-BN      ; 01.01.1950    ; true            ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"45  ; 3       ; MVA-BN      ; 01.01.1950    ; true            ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"46  ; 1       ; MVA-BN      ;               ; true            ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"47  ; 2       ; MVA-BN      ;               ; true            ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"48  ; 3       ; MVA-BN      ;               ; true            ; 1       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"49  ; 1       ; MVA-BN      ; 01.01.1950    ; false           ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"50  ; 2       ; MVA-BN      ; 01.01.1950    ; false           ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"51  ; 3       ; MVA-BN      ; 01.01.1950    ; false           ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"52  ; 1       ; MVA-BN      ;               ; false           ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"53  ; 2       ; MVA-BN      ;               ; false           ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"54  ; 3       ; MVA-BN      ;               ; false           ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"55  ; 1       ; MVA-BN      ; 01.01.1950    ; true            ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"56  ; 2       ; MVA-BN      ; 01.01.1950    ; true            ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"57  ; 3       ; MVA-BN      ; 01.01.1950    ; true            ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"58  ; 1       ; MVA-BN      ;               ; true            ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024   ",
	"59  ; 2       ; MVA-BN      ;               ; true            ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"60  ; 3       ; MVA-BN      ;               ; true            ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXTERNESZERTIFIKAT ; 01.10.2024 ; 01.10.2024   ",
	"61  ; 2       ; unkown_vacc ; 01.01.1950    ; true            ; 2       ; 01.10.2022    ; VOLLSTAENDIG_EXT_PLUS_VACME     ; 01.10.2024 ; 01.10.2024 ",
		}

	)
	public void testAffenpockenFreigabedatumAndImpfschutz(
		int row,
		int anzExtZert,
		String impfstoffEZ,
		String sDatumEZ,
		boolean immunsuprimiert,
		int anzImpfungen,
		String sDatumImpfung,
		String expectedGrundimmunisiert,
		String expectedFreigabedatum,
		String expectedImpfschutzBis
	) {
		@Nullable LocalDate lastImpfungsdatumEz = parseDate(sDatumEZ);
		@Nullable LocalDate lastImpfungvacme = parseDate(sDatumImpfung);
		Impfstoff impfstoff = parseImpfstoff(impfstoffEZ);
		initRules();
		ImpfinformationBuilder builder = createBuilder(anzExtZert,
			impfstoff,
			lastImpfungsdatumEz,
			immunsuprimiert,
			anzImpfungen,
			lastImpfungvacme
		);
		calculateImpfschutzAndCheckResults(expectedGrundimmunisiert, expectedFreigabedatum, expectedFreigabedatum, expectedImpfschutzBis, builder);
	}

	@Test
	public void registrierungWithNoImpfungenShouldBeFreigegeben(){
		initRules();
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder.create(KrankheitIdentifier.AFFENPOCKEN).withAge(20);
		calculateImpfschutzAndCheckResults(null, "today", "today", null, builder);
	}

	private void calculateImpfschutzAndCheckResults(
		@Nullable String sExpectedVollstImpfschutzTyp,
		@Nullable String sDatumExpectedFreigabeEkif,
		@Nullable String sDatumExpectedFreigabeSelbstzahler,
		@Nullable String sExpectedImpfschutzBis, ImpfinformationBuilder builder
	) {
		LocalDate expectedFreigabeEkif = parseDate(sDatumExpectedFreigabeEkif);
		LocalDate expectedFreigabeSelbstzahler = parseDate(sDatumExpectedFreigabeSelbstzahler);
		LocalDate expectedImpfschutzBis = parseDate(sExpectedImpfschutzBis);
		VollstaendigerImpfschutzTyp expectedVollstImpfschutzTyp =
			parseExpectedImpfschutzTyp(sExpectedVollstImpfschutzTyp);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz =
			affenpockenImpfschutzcalculationService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		if (sExpectedVollstImpfschutzTyp != null) {
			if (expectedVollstImpfschutzTyp != null) {
				Assertions.assertEquals(
					expectedVollstImpfschutzTyp,
					infos.getImpfdossier().getVollstaendigerImpfschutzTyp(),
					"vollstaendigerImpfschutzTyp should be null");
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
				impfschutz.getFreigegebenNaechsteImpfungAb());
		}
		if (expectedFreigabeSelbstzahler == null) {
			if (impfschutz != null) {
				Assertions.assertNull(impfschutz.getFreigegebenAbSelbstzahler(), "Selbstzahler Datum should be null");
			}
		} else {
			Assertions.assertNotNull(impfschutz);
			Assertions.assertEquals(
				expectedFreigabeSelbstzahler.atStartOfDay(),
				impfschutz.getFreigegebenAbSelbstzahler(), "Selbstzahler Datum is not as expected");
		}
		if (expectedImpfschutzBis == null){
			if(impfschutz != null){
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
		String sPrioritaet,
		int anzahlGrundimmunisierungen,
		int anzahlBooster,
		String sDatumLetzteImpfung
	) {
		Prioritaet prioritaet = Prioritaet.valueOf(sPrioritaet);
		LocalDate datumLetzteImpfung = parseDate(sDatumLetzteImpfung);

		ImpfinformationBuilder builder = new ImpfinformationBuilder();

		builder.create(KrankheitIdentifier.AFFENPOCKEN)
			.withAge(alter)
			.withPrioritaet(prioritaet);

		for (int i = 1; i <= anzahlGrundimmunisierungen; i++) {
			if (anzahlGrundimmunisierungen > i) {
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung.minusYears(i), affenpockenImpfstoff, true);
			} else {
				// Dies ist die letzte
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung, affenpockenImpfstoff, true);
			}
		}

		for (int i = 1; i <= anzahlBooster; i++) {
			if (anzahlBooster > i) {
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung.minusYears(i), affenpockenImpfstoff, false);
			} else {
				// Dies ist die letzte
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung, affenpockenImpfstoff, false);
			}
		}
		return builder;
	}

	private ImpfinformationBuilder createBuilder(
		int anzExtZert,
		Impfstoff impfstoffEZ,
		@Nullable LocalDate lastImpfungsdatumEz,
		boolean immunsuprimiert,
		int anzImpfungen,
		@Nullable LocalDate lastImpfungvacme
	) {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();

		builder.create(KrankheitIdentifier.AFFENPOCKEN)
			.withAge(50)
			.withPrioritaet(Prioritaet.N)
			.withImmunsuprimiert(immunsuprimiert);

		if (TestdataCreationUtil.createImpfstoffForUnbekannteAffenpockenimpfstoffInKindheit().equals(impfstoffEZ)) {
			builder.withUnbekannterAffenpockenimpfungInKindheit(lastImpfungsdatumEz, anzExtZert);
		} else {
			builder.withExternesZertifikatOhneTest(impfstoffEZ, anzExtZert, lastImpfungsdatumEz);
		}

		for (int i = 1; i <= anzImpfungen; i++) {
			if (anzImpfungen > i) {
				Assertions.assertNotNull(lastImpfungvacme);
				builder.withBooster(lastImpfungvacme.minusYears(i), affenpockenImpfstoff, false);
			} else {
				// Dies ist die letzte
				Assertions.assertNotNull(lastImpfungvacme);
				builder.withBooster(lastImpfungvacme, affenpockenImpfstoff, false);
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
		if("unkown_vacc".equals(impfstoffEZ)) {
			return TestdataCreationUtil.createImpfstoffForUnbekannteAffenpockenimpfstoffInKindheit();
		}
		if("MVA-BN".equals(impfstoffEZ)) {
			return TestdataCreationUtil.createImpfstoffAffenpocken();
		}

		throw new IllegalArgumentException("Unknown impfstoff: " + impfstoffEZ);
	}
}
