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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.BoosterAgePunktePrioritaetImpfstoffRule;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.IBoosterPrioritaetRule;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetServiceTestConstants.CUTOFF_SELBSTZAHLER;
import static ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetServiceTestConstants.TEST_HERBST_2022_BE_CONFIG;
import static ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetServiceTestConstants.TEST_MAERZ_2023_BE_CONFIG;

class CovidImpfschutzcalculationServiceBulkTest {

	private CovidImpfschutzcalculationService boosterPrioritaetService;
	private ImpfstoffInfosForRules specifiedImpfstoffe;
	private final Impfstoff moderna = TestdataCreationUtil.createImpfstoffModerna();
	private static final int MIN_AGE_BE = 16;
	private static final int MIN_AGE_ZH = 16;

	@BeforeEach
	void setUp() {
		System.setProperty("vacme.mandant", "BE");
		this.specifiedImpfstoffe = createSpecifiedImpfstoffeForRules();
		boosterPrioritaetService = new CovidImpfschutzcalculationService(specifiedImpfstoffe, null);
	}

	@NonNull
	private ImpfstoffInfosForRules createSpecifiedImpfstoffeForRules() {
		List<Impfstoff> allImpfstoffeForTest = List.of(
			TestdataCreationUtil.createImpfstoffModerna()
		);
		return new ImpfstoffInfosForRules(allImpfstoffeForTest);
	}

	private void initRulesBE(@NonNull BoosterPrioritaetServiceTestConfig config) {
		final List<IBoosterPrioritaetRule> rules = new ArrayList<>();

		// 1. Booster
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			MIN_AGE_BE,
			config.getFreigabeMonthsNachImpfung(), 0,
			config.getFreigabeMonthsNachKrankheit(), 0,
			0, null,
			false, false, null));

		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createOhneFreigabeRule(
			specifiedImpfstoffe,
			false)); // rule die alle Regs matched und nur das immunisiertBis berechnet

		// rule die alle Regs matched und
		// nur das immunisiertBis berechnet
		boosterPrioritaetService.rules.clear();
		boosterPrioritaetService.rules.addAll(rules);
	}

	private void initRulesZH(@NonNull BoosterPrioritaetServiceTestConfig config) {
		final List<IBoosterPrioritaetRule> rules = new ArrayList<>();
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			MIN_AGE_ZH,
			config.getFreigabeMonthsNachImpfung(), 0,
			config.getFreigabeMonthsNachKrankheit(), 0,
			0, null,
			false, false, CUTOFF_SELBSTZAHLER));

		// Alle ab 12 sind als Selbstzahler freigegeben
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			12,
			config.getFreigabeMonthsNachImpfung(), 0,
			config.getFreigabeMonthsNachKrankheit(), 0,
			0, null,
			false, true, null));

		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createOhneFreigabeRule(
			specifiedImpfstoffe,
			false)); // rule die alle Regs matched und nur das immunisiertBis berechnet

		// rule die alle Regs matched und
		// nur das immunisiertBis berechnet
		boosterPrioritaetService.rules.clear();
		boosterPrioritaetService.rules.addAll(rules);
	}

	@ParameterizedTest
	@CsvSource({
		// Alter	| Prio | Anzahl GI | Anzahl Booster | LetzteImpfung | FreigabeEkif | FreigabeSelbstzahler
		// Alter 10, kein Booster erlaubt
		"10         , S	   , 2         , 0              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 0              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 1              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 2              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 3              , 01.01.2021    ,              , ",
		// Alter 11, kein Booster erlaubt
		"11         , S	   , 2         , 0              , 01.01.2021    ,              , ",
		"11         , S	   , 3         , 0              , 01.01.2021    ,              , ",
		"11         , S	   , 3         , 1              , 01.01.2021    ,              , ",
		"11         , S	   , 3         , 2              , 01.01.2021    ,              , ",
		"11         , S	   , 3         , 3              , 01.01.2021    ,              , ",
		// Alter 14, kein Booster erlaubt
		"14         , S	   , 2         , 0              , 01.01.2021    ,              , ",
		"14         , S	   , 3         , 0              , 01.01.2021    ,              , ",
		"14         , S	   , 3         , 1              , 01.01.2021    ,              , ",
		"14         , S	   , 3         , 2              , 01.01.2021    ,              , ",
		"14         , S	   , 3         , 3              , 01.01.2021    ,              , ",
		// Alter 15, Booster erlaubt am Tag des 16 Geburtstags
		"15         , S	   , 2         , 0              , 01.01.2021    , today+1y     , today+1y ",
		"15         , S	   , 3         , 0              , 01.01.2021    , today+1y     , today+1y ",
		"15         , S	   , 3         , 1              , 01.01.2021    , today+1y     , today+1y ",
		"15         , S	   , 3         , 2              , 01.01.2021    , today+1y     , today+1y ",
		"15         , S	   , 3         , 3              , 01.01.2021    , today+1y     , today+1y ",
		// Alter 20, beliebig viele Booster erlaubt
		"20         , M	   , 2         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , M	   , 3         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , M	   , 3         , 1              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , M	   , 3         , 2              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , M	   , 3         , 3              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		// Alter 20, beliebig viele Booster erlaubt
		"20         , B	   , 2         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , B	   , 3         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , B	   , 3         , 1              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , B	   , 3         , 2              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , B	   , 3         , 3              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		// Alter 65: beliebig viele Booster erlaubt
		"65         , M	   , 2         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"65         , M    , 3         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"65         , M	   , 3         , 1              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"65         , M	   , 3         , 2              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"65         , M	   , 3         , 3              , 01.01.2021    , 01.05.2021   , 01.05.2021",
	})
	public void herbst22_bern(
		int alter,
		String sPrioritaet,
		int anzahlGrundimmunisierungen,
		int anzahlBooster,
		String sDatumLetzteImpfung,
		String sDatumExpectedFreigabeEkif,
		String sDatumExpectedFreigabeSelbstzahler
	) {
		initRulesBE(BoosterPrioritaetServiceTestConstants.TEST_HERBST_2022_BE_CONFIG);
		ImpfinformationBuilder builder =
			createBuilder(alter, sPrioritaet, anzahlGrundimmunisierungen, anzahlBooster, sDatumLetzteImpfung, null, 0);
		calculateImpfschutzAndCheckResults(sDatumExpectedFreigabeEkif, sDatumExpectedFreigabeSelbstzahler, builder);
	}

	@ParameterizedTest
	@CsvSource({
		// Alter	| Prio | Anzahl GI | Anzahl Booster | LetzteImpfung | FreigabeEkif | FreigabeSelbstzahler
		// Alter 10, kein Booster erlaubt
		"10         , S	   , 2         , 0              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 0              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 1              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 2              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 3              , 01.01.2021    ,              , ",
		// Alter 11, kein Booster erlaubt
		"11         , S	   , 2         , 0              , 01.01.2021    ,              , ",
		"11         , S	   , 3         , 0              , 01.01.2021    ,              , ",
		"11         , S	   , 3         , 1              , 01.01.2021    ,              , ",
		"11         , S	   , 3         , 2              , 01.01.2021    ,              , ",
		"11         , S	   , 3         , 3              , 01.01.2021    ,              , ",
		// Alter 14, kein Booster erlaubt
		"14         , S	   , 2         , 0              , 01.01.2021    ,              , ",
		"14         , S	   , 3         , 0              , 01.01.2021    ,              , ",
		"14         , S	   , 3         , 1              , 01.01.2021    ,              , ",
		"14         , S	   , 3         , 2              , 01.01.2021    ,              , ",
		"14         , S	   , 3         , 3              , 01.01.2021    ,              , ",
		// Alter 15, Booster erlaubt am Tag des 16 Geburtstags
		"15         , S	   , 2         , 0              , 01.01.2021    , today+1y     , today+1y ",
		"15         , S	   , 3         , 0              , 01.01.2021    , today+1y     , today+1y ",
		"15         , S	   , 3         , 1              , 01.01.2021    , today+1y     , today+1y ",
		"15         , S	   , 3         , 2              , 01.01.2021    , today+1y     , today+1y ",
		"15         , S	   , 3         , 3              , 01.01.2021    , today+1y     , today+1y ",
		// Alter 20, beliebig viele Booster erlaubt
		"20         , M	   , 2         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , M	   , 3         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , M	   , 3         , 1              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , M	   , 3         , 2              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , M	   , 3         , 3              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		// Alter 20, beliebig viele Booster erlaubt
		"20         , B	   , 2         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , B	   , 3         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , B	   , 3         , 1              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , B	   , 3         , 2              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , B	   , 3         , 3              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		// Alter 65: beliebig viele Booster erlaubt
		"65         , M	   , 2         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"65         , M    , 3         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"65         , M	   , 3         , 1              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"65         , M	   , 3         , 2              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"65         , M	   , 3         , 3              , 01.01.2021    , 01.07.2021   , 01.07.2021",
	})
	public void maerz23_bern(
		int alter,
		String sPrioritaet,
		int anzahlGrundimmunisierungen,
		int anzahlBooster,
		String sDatumLetzteImpfung,
		String sDatumExpectedFreigabeEkif,
		String sDatumExpectedFreigabeSelbstzahler
	) {
		initRulesBE(TEST_MAERZ_2023_BE_CONFIG);
		ImpfinformationBuilder builder =
			createBuilder(alter, sPrioritaet, anzahlGrundimmunisierungen, anzahlBooster, sDatumLetzteImpfung, null, 0);
		calculateImpfschutzAndCheckResults(sDatumExpectedFreigabeEkif, sDatumExpectedFreigabeSelbstzahler, builder);
	}

	@ParameterizedTest
	@CsvSource({
		// Alter	| Prio | Anzahl GI | Anzahl Booster | LetzteImpfung | FreigabeEkif | FreigabeSelbstzahler
		// Alter 10, kein Booster erlaubt
		"10         , S	   , 2         , 0              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 0              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 1              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 2              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 3              , 01.01.2021    ,              , ",
		// Alter 11, kein Booster erlaubt, fuer Selbstzahler freigegeben am Geburtstag
		"11         , S	   , 2         , 0              , 01.01.2021    ,              , today+1y  ",
		"11         , S	   , 3         , 0              , 01.01.2021    ,              , today+1y  ",
		"11         , S	   , 3         , 1              , 01.01.2021    ,              , today+1y  ",
		"11         , S	   , 3         , 2              , 01.01.2021    ,              , today+1y  ",
		"11         , S	   , 3         , 3              , 01.01.2021    ,              , today+1y  ",
		// Alter 14, kein Booster erlaubt, fuer Selbstzahler aber freigegeben
		"14         , S	   , 2         , 0              , 01.01.2021    ,              , 01.05.2021",
		"14         , S	   , 3         , 0              , 01.01.2021    ,              , 01.05.2021",
		"14         , S	   , 3         , 1              , 01.01.2021    ,              , 01.05.2021",
		"14         , S	   , 3         , 2              , 01.01.2021    ,              , 01.05.2021",
		"14         , S	   , 3         , 3              , 01.01.2021    ,              , 01.05.2021",
		// Alter 15, 1. Booster erlaubt am Tag des 16 Geburtstags, fuer Selbstzahler freigegeben
		"15         , S	   , 2         , 0              , 01.01.2021    , today+1y     , 01.05.2021 ",
		"15         , S	   , 3         , 0              , 01.01.2021    , today+1y     , 01.05.2021 ",
		"15         , S	   , 3         , 1              , 01.01.2021    , today+1y     , 01.05.2021 ",
		"15         , S	   , 3         , 2              , 01.01.2021    , today+1y     , 01.05.2021 ",
		"15         , S	   , 3         , 3              , 01.01.2021    , today+1y     , 01.05.2021 ",
		// Alter 20, beliebig viele Booster erlaubt
		"20         , M	   , 2         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , M	   , 3         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , M	   , 3         , 1              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , M	   , 3         , 2              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , M	   , 3         , 3              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		// Alter 20, beliebig viele Booster erlaubt
		"20         , B	   , 2         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , B	   , 3         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , B	   , 3         , 1              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , B	   , 3         , 2              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"20         , B	   , 3         , 3              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		// Alter 65: beliebig viele Booster erlaubt
		"65         , M	   , 2         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"65         , M    , 3         , 0              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"65         , M	   , 3         , 1              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"65         , M	   , 3         , 2              , 01.01.2021    , 01.05.2021   , 01.05.2021",
		"65         , M	   , 3         , 3              , 01.01.2021    , 01.05.2021   , 01.05.2021",
	})
	public void herbst22_zuerich(
		int alter,
		String sPrioritaet,
		int anzahlGrundimmunisierungen,
		int anzahlBooster,
		String sDatumLetzteImpfung,
		String sDatumExpectedFreigabeEkif,
		String sDatumExpectedFreigabeSelbstzahler
	) {
		initRulesZH(TEST_HERBST_2022_BE_CONFIG);
		ImpfinformationBuilder builder =
			createBuilder(alter, sPrioritaet, anzahlGrundimmunisierungen, anzahlBooster, sDatumLetzteImpfung, null, 0);
		calculateImpfschutzAndCheckResults(sDatumExpectedFreigabeEkif, sDatumExpectedFreigabeSelbstzahler, builder);
	}

	@ParameterizedTest
	@CsvSource({
		// Alter	| Prio | Anzahl GI | Anzahl Booster | LetzteImpfung | FreigabeEkif | FreigabeSelbstzahler
		// Alter 10, kein Booster erlaubt
		"10         , S	   , 2         , 0              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 0              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 1              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 2              , 01.01.2021    ,              , ",
		"10         , S	   , 3         , 3              , 01.01.2021    ,              , ",
		// Alter 11, kein Booster erlaubt, fuer Selbstzahler freigegeben am Geburtstag
		"11         , S	   , 2         , 0              , 01.01.2021    ,              , today+1y  ",
		"11         , S	   , 3         , 0              , 01.01.2021    ,              , today+1y  ",
		"11         , S	   , 3         , 1              , 01.01.2021    ,              , today+1y  ",
		"11         , S	   , 3         , 2              , 01.01.2021    ,              , today+1y  ",
		"11         , S	   , 3         , 3              , 01.01.2021    ,              , today+1y  ",
		// Alter 14, kein Booster erlaubt, fuer Selbstzahler aber freigegeben
		"14         , S	   , 2         , 0              , 01.01.2021    ,              , 01.07.2021",
		"14         , S	   , 3         , 0              , 01.01.2021    ,              , 01.07.2021",
		"14         , S	   , 3         , 1              , 01.01.2021    ,              , 01.07.2021",
		"14         , S	   , 3         , 2              , 01.01.2021    ,              , 01.07.2021",
		"14         , S	   , 3         , 3              , 01.01.2021    ,              , 01.07.2021",
		// Alter 15, 1. Booster erlaubt am Tag des 16 Geburtstags, fuer Selbstzahler freigegeben
		"15         , S	   , 2         , 0              , 01.01.2021    , today+1y     , 01.07.2021 ",
		"15         , S	   , 3         , 0              , 01.01.2021    , today+1y     , 01.07.2021 ",
		"15         , S	   , 3         , 1              , 01.01.2021    , today+1y     , 01.07.2021 ",
		"15         , S	   , 3         , 2              , 01.01.2021    , today+1y     , 01.07.2021 ",
		"15         , S	   , 3         , 3              , 01.01.2021    , today+1y     , 01.07.2021 ",
		// Alter 20, beliebig viele Booster erlaubt
		"20         , M	   , 2         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , M	   , 3         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , M	   , 3         , 1              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , M	   , 3         , 2              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , M	   , 3         , 3              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		// Alter 20, beliebig viele Booster erlaubt
		"20         , B	   , 2         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , B	   , 3         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , B	   , 3         , 1              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , B	   , 3         , 2              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"20         , B	   , 3         , 3              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		// Alter 65: beliebig viele Booster erlaubt
		"65         , M	   , 2         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"65         , M    , 3         , 0              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"65         , M	   , 3         , 1              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"65         , M	   , 3         , 2              , 01.01.2021    , 01.07.2021   , 01.07.2021",
		"65         , M	   , 3         , 3              , 01.01.2021    , 01.07.2021   , 01.07.2021",
	})
	public void maerz23_zuerich(
		int alter,
		String sPrioritaet,
		int anzahlGrundimmunisierungen,
		int anzahlBooster,
		String sDatumLetzteImpfung,
		String sDatumExpectedFreigabeEkif,
		String sDatumExpectedFreigabeSelbstzahler
	) {
		initRulesZH(TEST_MAERZ_2023_BE_CONFIG);
		ImpfinformationBuilder builder =
			createBuilder(alter, sPrioritaet, anzahlGrundimmunisierungen, anzahlBooster, sDatumLetzteImpfung, null, 0);
		calculateImpfschutzAndCheckResults(sDatumExpectedFreigabeEkif, sDatumExpectedFreigabeSelbstzahler, builder);
	}

	@ParameterizedTest
	@CsvSource(delimiter = ';', value = {
		// Alter | Anzahl GI | Anzahl Booster | LetzteImpfung | AnzahlEZ | datumEZ	   | FreigabeEkif | FreigabeSelbstzahler
		// Alter 10, kein Booster erlaubt
		"10      ; 2         ; 0              ; 08.10.2022    ; 0        ;             ;              ; ",
		"10      ; 2         ; 0              ; 12.10.2022    ; 0        ;             ;              ; ",
		"10      ; 2         ; 1              ; 08.10.2022    ; 0        ;             ;              ; ",
		"10      ; 2         ; 1              ; 12.10.2022    ; 0        ;             ;              ; ",
		// Alter 11,  kein Booster erlaubt; fuer Selbstzahler freigegeben am Geburtstag
		"11      ; 2         ; 0              ; 08.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 2         ; 0              ; 12.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 3         ; 0              ; 08.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 3         ; 0              ; 12.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 2         ; 1              ; 08.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 2         ; 1              ; 12.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 3         ; 1              ; 08.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 3         ; 1              ; 12.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 2         ; 2              ; 08.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 2         ; 2              ; 12.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 3         ; 2              ; 08.10.2022    ; 0        ;             ;              ; today+1y",
		"11      ; 3         ; 2              ; 12.10.2022    ; 0        ;             ;              ; today+1y",
		// Alter 14, kein Booster erlaubt, fuer Selbstzahler aber freigegeben, egal auf welcher seite vom Cutoff (10.10.2022)
		"14      ; 2         ; 0              ; 08.10.2022    ; 0        ;             ;              ; 08.04.2023",
		"14      ; 2         ; 0              ; 12.10.2022    ; 0        ;             ;              ; 12.04.2023",
		"14      ; 2         ; 1              ; 08.10.2022    ; 0        ;             ;              ; 08.04.2023",
		"14      ; 2         ; 1              ; 12.10.2022    ; 0        ;             ;              ; 12.04.2023",
		"14      ; 2         ; 2              ; 08.10.2022    ; 0        ;             ;              ; 08.04.2023",
		"14      ; 2         ; 2              ; 12.10.2022    ; 0        ;             ;              ; 12.04.2023",
		// Alter 15, 1. Booster erlaubt am Tag des 16 Geburtstags; fuer Selbstzahler freigegeben; 2te booster nicht mehr bezahlt
		"15      ; 2         ; 0              ; 08.10.2022    ; 0        ;             ; today+1y     ; 08.04.2023",
		"15      ; 2         ; 0              ; 12.10.2022    ; 0        ;             ; today+1y     ; 12.04.2023",
		"15      ; 2         ; 1              ; 08.10.2022    ; 0        ;             ; today+1y     ; 08.04.2023",
		"15      ; 2         ; 1              ; 12.10.2022    ; 0        ;             ;              ; 12.04.2023",
		// Alter 20, nur Selbstzahler nach 1 Booster wenn letzter Booster nach 10.10.2022 passierte
		"20      ; 2         ; 0              ; 08.10.2022    ; 0        ;             ; 08.04.2023   ; 08.04.2023",
		// Keine erste Booster Impfung -> Immer freigegeben
		"20      ; 2         ; 0              ; 12.10.2022    ; 0        ;             ; 12.04.2023   ; 12.04.2023",
		"20      ; 3         ; 0              ; 08.10.2022    ; 0        ;             ; 08.04.2023   ; 08.04.2023",
		"20      ; 3         ; 0              ; 12.10.2022    ; 0        ;             ; 12.04.2023   ; 12.04.2023",
		"20      ; 2         ; 1              ; 08.10.2022    ; 0        ;             ; 08.04.2023   ; 08.04.2023",
		// Before cutoff immer noch freigegeben
		"20      ; 2         ; 2              ; 08.10.2022    ; 0        ;             ; 08.04.2023   ; 08.04.2023",
		"20      ; 2         ; 1              ; 09.10.2022    ; 0        ;             ; 09.04.2023   ; 09.04.2023",
		"20      ; 2         ; 1              ; 10.10.2022    ; 0        ;             ;              ; 10.04.2023",
		// After cutoff nur noch selbstzahler
		"20      ; 2         ; 1              ; 12.10.2022    ; 0        ;             ;              ; 12.04.2023",
		// After cutoff nur noch selbstzahler
		"20      ; 2         ; 2              ; 12.10.2022    ; 0        ;             ;              ; 12.04.2023",
		"20      ; 0         ; 0              ;               ; 1        ; 08.10.2022  ; 08.04.2023   ; 08.04.2023",
		"20      ; 0         ; 0              ;               ; 2        ; 08.10.2022  ; 08.04.2023   ; 08.04.2023",
		"20      ; 0         ; 0              ;               ; 3        ; 08.10.2022  ; 08.04.2023   ; 08.04.2023",
		"20      ; 0         ; 0              ;               ; 1        ; 12.10.2022  ; 12.04.2023   ; 12.04.2023",
		"20      ; 0         ; 0              ;               ; 2        ; 12.10.2022  ;              ; 12.04.2023",
		// Durch EZ Grundimmunisiert nach deadline -> Selbstzahler
		"20      ; 0         ; 0              ;               ; 3        ; 12.10.2022  ;              ; 12.04.2023",
		"20      ; 1         ; 0              ; 08.10.2022    ; 1        ; 07.10.2022  ; 08.04.2023   ; 08.04.2023",
		// 1 EZ, 1 GU -> Grundimmunisiert, nächste impfung bezahlt in allen fällen
		"20      ; 1         ; 0              ; 12.10.2022    ; 1        ; 11.10.2022  ; 12.04.2023   ; 12.04.2023",
		"20      ; 2         ; 0              ; 08.10.2022    ; 1        ; 07.10.2022  ; 08.04.2023   ; 08.04.2023",
		// 1 EZ, 2 GU -> Grundimmunisiert, nächste impfung bezahlt in allen fällen
		"20      ; 2         ; 0              ; 12.10.2022    ; 1        ; 11.10.2022  ; 12.04.2023   ; 12.04.2023",
		"20      ; 0         ; 1              ; 08.10.2022    ; 2        ; 07.10.2022  ; 08.04.2023   ; 08.04.2023",
		// EZ GU + 1 Booster -> je nach cuttoff bezahlt
		"20      ; 0         ; 1              ; 12.10.2022    ; 2        ; 11.10.2022  ;              ; 12.04.2023",
	})
	public void fruehling23_zuerich(
		int alter,
		int anzahlGrundimmunisierungen,
		int anzahlBooster,
		String sDatumLetzteImpfung,
		int anzahlExternGeimpft,
		String sDatumLetzteImpfungEz,
		String sDatumExpectedFreigabeEkif,
		String sDatumExpectedFreigabeSelbstzahler
	) {
		initRulesZH(TEST_MAERZ_2023_BE_CONFIG);
		ImpfinformationBuilder builder = createBuilder(alter,
			"S",
			anzahlGrundimmunisierungen,
			anzahlBooster,
			sDatumLetzteImpfung,
			sDatumLetzteImpfungEz,
			anzahlExternGeimpft);

		calculateImpfschutzAndCheckResults(sDatumExpectedFreigabeEkif, sDatumExpectedFreigabeSelbstzahler, builder);
	}

	private void calculateImpfschutzAndCheckResults(
		String sDatumExpectedFreigabeEkif,
		String sDatumExpectedFreigabeSelbstzahler,
		ImpfinformationBuilder builder
	) {
		LocalDate expectedFreigabeEkif = parseDate(sDatumExpectedFreigabeEkif);
		LocalDate expectedFreigabeSelbstzahler = parseDate(sDatumExpectedFreigabeSelbstzahler);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz =
			boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		if (expectedFreigabeEkif == null) {
			if (impfschutz != null) {
				Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
			}
		} else {
			Assertions.assertNotNull(impfschutz);
			Assertions.assertEquals(
				expectedFreigabeEkif.atStartOfDay(),
				impfschutz.getFreigegebenNaechsteImpfungAb());
		}
		if (expectedFreigabeSelbstzahler == null) {
			if (impfschutz != null) {
				Assertions.assertNull(impfschutz.getFreigegebenAbSelbstzahler());
			}
		} else {
			Assertions.assertNotNull(impfschutz);
			Assertions.assertEquals(
				expectedFreigabeSelbstzahler.atStartOfDay(),
				impfschutz.getFreigegebenAbSelbstzahler());
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@NonNull
	private ImpfinformationBuilder createBuilder(
		int alter,
		String sPrioritaet,
		int anzahlGrundimmunisierungen,
		int anzahlBooster,
		String sDatumLetzteImpfung,
		@Nullable String sDatumLetzteImpfungEz,
		int anzahlExternGeimpft
	) {
		Prioritaet prioritaet = Prioritaet.valueOf(sPrioritaet);
		LocalDate datumLetzteImpfung = parseDate(sDatumLetzteImpfung);

		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		LocalDate datumLetzteImpfungEz = parseDate(sDatumLetzteImpfungEz);
		builder.create(KrankheitIdentifier.COVID).withAge(alter).withPrioritaet(prioritaet);

		if (datumLetzteImpfungEz != null) {
			builder.withExternesZertifikat(moderna, anzahlExternGeimpft, datumLetzteImpfungEz, null);
		}
		if (anzahlGrundimmunisierungen > 1) {
			Assertions.assertNotNull(datumLetzteImpfung);
			builder.withImpfung1(datumLetzteImpfung.minusYears(1), moderna);
		} else if (anzahlGrundimmunisierungen == 1) {
			// Dies ist die letzte
			Assertions.assertNotNull(datumLetzteImpfung);
			builder.withImpfung1(datumLetzteImpfung, moderna);
		}
		if (anzahlGrundimmunisierungen > 2) {
			Assertions.assertNotNull(datumLetzteImpfung);
			builder.withImpfung2(datumLetzteImpfung.minusYears(1), moderna);
		} else if (anzahlGrundimmunisierungen == 2) {
			// Dies ist die letzte
			Assertions.assertNotNull(datumLetzteImpfung);
			builder.withImpfung2(datumLetzteImpfung, moderna);
		}
		for (int i = 3; i <= anzahlGrundimmunisierungen; i++) {
			if (anzahlGrundimmunisierungen > i) {
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung.minusYears(1), moderna, true);
			} else {
				// Dies ist die letzte
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung, moderna, true);
			}
		}
		for (int i = 1; i <= anzahlBooster; i++) {
			if (anzahlBooster > i) {
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung.minusYears(1), moderna, false);
			} else {
				// Dies ist die letzte
				Assertions.assertNotNull(datumLetzteImpfung);
				builder.withBooster(datumLetzteImpfung, moderna, false);
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
		if ("today+1y".equals(dateString)) {
			return LocalDate.now().plusYears(1);
		}

		DateTimeFormatter formatter = DateUtil.DEFAULT_DATE_FORMAT.apply(Locale.GERMANY);
		return LocalDate.parse(dateString, formatter);
	}
}
