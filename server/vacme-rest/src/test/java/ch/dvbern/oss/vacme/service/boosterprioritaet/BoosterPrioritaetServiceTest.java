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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Erkrankung;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.BeruflicheTaetigkeit;
import ch.dvbern.oss.vacme.entities.registration.ChronischeKrankheiten;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Lebensumstaende;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.BoosterAgePunktePrioritaetImpfstoffRule;
import ch.dvbern.oss.vacme.service.boosterprioritaet.rules.IBoosterPrioritaetRule;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetServiceTestConstants.FREIGABE_MONTHS_NACH_IMPFUNG;
import static ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetServiceTestConstants.FREIGABE_MONTHS_NACH_KRANKHEIT;
import static ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetServiceTestConstants.FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG;
import static ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetServiceTestConstants.FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG;
import static ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetServiceTestConstants.FREIGBAME_MONTHS_NACH_IMPFUNG_AB_80_JAEHRIG;
import static ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioritaetServiceTestConstants.FREIGBAME_MONTHS_NACH_IMPFUNG_PRIO_A;
import static ch.dvbern.oss.vacme.service.boosterprioritaet.rules.BoosterAgePunktePrioritaetImpfstoffRule.IMPFSCHUTZ_DAUER_DAYS;

class BoosterPrioritaetServiceTest {

	private BoosterPrioritaetService boosterPrioritaetService;

	private ImpfstoffInfosForRules specifiedImpfstoffe;

	private final Impfstoff moderna = TestdataCreationUtil.createImpfstoffModerna();
	private final Impfstoff modernaBivalent = TestdataCreationUtil.createImpfstoffModernaBivalent();
	private final Impfstoff pfizer = TestdataCreationUtil.createImpfstoffPfizer();
	private final Impfstoff pfizerBivalent =TestdataCreationUtil.createImpfstoffPfizerBivalent();
	private final Impfstoff pfizerKinder = TestdataCreationUtil.createImpfstoffPfizerKinder();
	private final Impfstoff janssen = TestdataCreationUtil.createImpfstoffJanssen();
	private final Impfstoff astra = TestdataCreationUtil.createImpfstoffAstraZeneca();
	private final Impfstoff sinovac = TestdataCreationUtil.createImpfstoffSinovac();
	private final Impfstoff sinopharm = TestdataCreationUtil.createImpfstoffSinopharm();
	private final Impfstoff covaxin = TestdataCreationUtil.createImpfstoffCovaxin();
	private final Impfstoff novavax = TestdataCreationUtil.createImpfstoffNovavax();
	private final Impfstoff covishield = TestdataCreationUtil.createImpfstoffCovishield();
	private final Impfstoff covovax = TestdataCreationUtil.createImpfstoffCOVOVAX();
	private final Impfstoff sputnikV = TestdataCreationUtil.createImpfstoffSputnikV();
	private final Impfstoff zifivax = TestdataCreationUtil.createImpfstoffZifivax();

	private boolean enablePfizerOnlyForU30ForTest = false;
	private boolean ruleOnlyValidForSelbstzahlerFreigabe = false;

	private static final LocalDate GEBURTSDATUM_65_JAEHRIG = LocalDate.now().minusYears(65);
	private static final LocalDate GEBURTSDATUM_25_JAEHRIG = LocalDate.now().minusYears(25);
	private static final LocalDate GEBURTSDATUM_MORGEN_12_JAEHRIG = LocalDate.now().minusYears(12).plusDays(1); // wird morgen 12
	private static final LocalDate GEBURTSDATUM_13_JAEHRIG = LocalDate.now().minusYears(13);
	private static final LocalDate GEBURTSDATUM_10_JAEHRIG = LocalDate.now().minusYears(10);
	private static final LocalDate GEBURTSDATUM_MORGEN_80_JAEHRIG = LocalDate.now().minusYears(80).plusDays(1); // wird morgen 80

	private static final LocalDate DATUM_LETZTE_GRUNDIMPFUNG = LocalDate.now().minusMonths(9);
	private static final LocalDate DATUM_BOOSTER_GRUNDIMPFUNG = DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(11);

	@BeforeEach
	void setUp() {
		System.setProperty("vacme.mandant", "BE");
		this.specifiedImpfstoffe = createSpecifiedImpfstoffeForRules();
		boosterPrioritaetService = new BoosterPrioritaetService(specifiedImpfstoffe);
		boosterPrioritaetService.freigabeOffsetKrankheitMonate = Optional.of(0);
		boosterPrioritaetService.freigabeOffsetKrankheitTage = Optional.of(28);
		initRulesForTest();
	}

	@NonNull
	private ImpfstoffInfosForRules createSpecifiedImpfstoffeForRules(){
		List<Impfstoff> allImpfstoffeForTest = List.of(
			TestdataCreationUtil.createImpfstoffModerna(),
			TestdataCreationUtil.createImpfstoffModernaBivalent(),
			TestdataCreationUtil.createImpfstoffPfizer(),
			TestdataCreationUtil.createImpfstoffPfizerKinder(),
			TestdataCreationUtil.createImpfstoffPfizerBivalent(),
			TestdataCreationUtil.createImpfstoffAstraZeneca(),
			TestdataCreationUtil.createImpfstoffJanssen(),
			TestdataCreationUtil.createImpfstoffCovaxin(),
			TestdataCreationUtil.createImpfstoffSinopharm(),
			TestdataCreationUtil.createImpfstoffSinovac(),
			TestdataCreationUtil.createImpfstoffCovishield(),
			TestdataCreationUtil.createImpfstoffCOVOVAX(),
			TestdataCreationUtil.createImpfstoffNovavax(),
			TestdataCreationUtil.createImpfstoffSputnikLight(),
			TestdataCreationUtil.createImpfstoffSputnikV(),
			TestdataCreationUtil.createImpfstoffConvidecia(),
			TestdataCreationUtil.createImpfstoffKazakhstan(),
			TestdataCreationUtil.createImpfstoffAbdala(),
			TestdataCreationUtil.createImpfstoffZifivax()
		);
		return new ImpfstoffInfosForRules(allImpfstoffeForTest);
	}

	private void initRulesForTest() {
		final List<IBoosterPrioritaetRule> rules = new ArrayList<>();

		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe, 80,
			FREIGBAME_MONTHS_NACH_IMPFUNG_AB_80_JAEHRIG, 0,
			6,0,
			1, 1,
			enablePfizerOnlyForU30ForTest, ruleOnlyValidForSelbstzahlerFreigabe, null));
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe, 65,
			FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG, 0,
			6,0,
			0, 0,
			enablePfizerOnlyForU30ForTest, ruleOnlyValidForSelbstzahlerFreigabe, null));
		Set<Prioritaet> prioritaeten = Stream.of("A").map(Prioritaet::valueOfCode).collect(Collectors.toSet());
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createPrioritaetenRule(
			specifiedImpfstoffe,
			prioritaeten,
			FREIGBAME_MONTHS_NACH_IMPFUNG_PRIO_A, 0,
			9,0,
			0, 0,
			enablePfizerOnlyForU30ForTest, ruleOnlyValidForSelbstzahlerFreigabe, null));
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe, 12,
			FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG, 0,
			11,0,
			0, 0,
			enablePfizerOnlyForU30ForTest, ruleOnlyValidForSelbstzahlerFreigabe, null));
		// Standardcase
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createOhneFreigabeRule(
			specifiedImpfstoffe,
			enablePfizerOnlyForU30ForTest));
		// rule die alle Regs matched und
		// nur das immunisiertBis berechnet
		boosterPrioritaetService.rules.clear();
		boosterPrioritaetService.rules.addAll(rules);
	}

	private void initBeRulesForTest() {
		final List<IBoosterPrioritaetRule> rules = new ArrayList<>();
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			80,
			FREIGABE_MONTHS_NACH_IMPFUNG, 0,
			FREIGABE_MONTHS_NACH_KRANKHEIT, 0,
			1, 1,
			enablePfizerOnlyForU30ForTest, ruleOnlyValidForSelbstzahlerFreigabe, null));
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			16,
			FREIGABE_MONTHS_NACH_IMPFUNG, 0,
			FREIGABE_MONTHS_NACH_KRANKHEIT, 0,
			0, 0,
			enablePfizerOnlyForU30ForTest, ruleOnlyValidForSelbstzahlerFreigabe, null));
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createOhneFreigabeRule(
			specifiedImpfstoffe,
			enablePfizerOnlyForU30ForTest)); // rule die alle Regs matched und nur das immunisiertBis berechnet
		boosterPrioritaetService.rules.clear();
		boosterPrioritaetService.rules.addAll(rules);
	}

	private void initZhRulesForTest(
		int minAge,
		int anzahlMonateFreigabeNachImpfung,
		int anzahlTageFreigabeNachImpfung,
		@Nullable Integer anzahlMonateFreigabeNachKrankheit,
		@Nullable Integer anzahlTageFreigabeNachKrankheit
	) {
		final List<IBoosterPrioritaetRule> rules = new ArrayList<>();
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			80,
			anzahlMonateFreigabeNachImpfung, anzahlTageFreigabeNachImpfung,
			anzahlMonateFreigabeNachKrankheit, anzahlTageFreigabeNachKrankheit,
			1, 1,
			enablePfizerOnlyForU30ForTest, ruleOnlyValidForSelbstzahlerFreigabe, null));
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createMinAgeRule(
			specifiedImpfstoffe,
			minAge,
			anzahlMonateFreigabeNachImpfung, anzahlTageFreigabeNachImpfung,
			anzahlMonateFreigabeNachKrankheit, anzahlTageFreigabeNachKrankheit,
			0, 0,
			enablePfizerOnlyForU30ForTest, ruleOnlyValidForSelbstzahlerFreigabe, null));
		rules.add(BoosterAgePunktePrioritaetImpfstoffRule.createOhneFreigabeRule(
			specifiedImpfstoffe,
			enablePfizerOnlyForU30ForTest)); // rule die alle Regs matched und nur das immunisiertBis berechnet
		boosterPrioritaetService.rules.clear();
		boosterPrioritaetService.rules.addAll(rules);
	}

	@Nullable
	private Impfschutz calculateImpfschutzForExterneImpfungDaten(
		boolean vollstaendigerImpfschutz,
		@NonNull LocalDate datumDerLetztenGrundimpfung,
		@Nullable LocalDate datumGenesen,
		@NonNull LocalDate geburtsdatum,
		@NonNull Prioritaet prioritaet,
		@NonNull BeruflicheTaetigkeit beruflicheTaetigkeit,
		@NonNull Lebensumstaende lebensumstaende,
		@NonNull ChronischeKrankheiten chronischeKrankheiten,
		@Nullable LocalDate datumDerBoosterImpfung,
		@NonNull List<Erkrankung> erkrankung
	) {
		Fragebogen fragebogen = createFragebogen(
			geburtsdatum,
			prioritaet,
			beruflicheTaetigkeit,
			lebensumstaende,
			chronischeKrankheiten);
		Impfdossier impfdossier = new Impfdossier();
		impfdossier.setVollstaendigerImpfschutzTyp(vollstaendigerImpfschutz ? VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME : null);
		ExternesZertifikat externeImpfinfo = TestdataCreationUtil.createExternesZertifikat(impfdossier, moderna, 2,
			datumDerLetztenGrundimpfung);

		if (datumGenesen != null) {
			externeImpfinfo.setGenesen(true);
			externeImpfinfo.setPositivGetestetDatum(datumGenesen);
		}

		if (!erkrankung.isEmpty()) {
			impfdossier.setErkrankungen(erkrankung);
		}
		ImpfinformationDto impfinformationDto;
		if (datumDerBoosterImpfung == null) {
			impfinformationDto = new ImpfinformationDto(KrankheitIdentifier.COVID, fragebogen.getRegistrierung(), null, null, impfdossier, externeImpfinfo);
		} else {

			Impfdossiereintrag impfdossiereintrag = new Impfdossiereintrag();
			impfdossiereintrag.setImpfdossier(impfdossier);
			impfdossier.getImpfdossierEintraege().add(impfdossiereintrag);
			impfdossiereintrag.setImpffolgeNr(3);
			Impfung boosterImpfung = createImpfung(datumDerBoosterImpfung);
			// Zum Testen setzen wir die Booster auf zur Grundimmunisierung gehoerend, da sonst der Impfschutz
			// immer null ist
			boosterImpfung.setGrundimmunisierung(true);
			impfdossiereintrag.setImpfterminFromImpfterminRepo(boosterImpfung.getTermin());
			impfinformationDto = new ImpfinformationDto(
				KrankheitIdentifier.COVID,
				fragebogen.getRegistrierung(),
				null,
				null,
				impfdossier,
				List.of(boosterImpfung),
				externeImpfinfo);
		}
		return boosterPrioritaetService.calculateImpfschutz(fragebogen, impfinformationDto).orElse(null);
	}

	@Nullable
	private Impfschutz calculateImpfschutzForVacMeImpfungen(
		boolean vollstaendigerImpfschutz,
		@NonNull LocalDate datumDerLetztenImpfung,
		@NonNull LocalDate geburtsdatum,
		@NonNull Prioritaet prioritaet,
		@NonNull BeruflicheTaetigkeit beruflicheTaetigkeit,
		@NonNull Lebensumstaende lebensumstaende,
		@NonNull ChronischeKrankheiten chronischeKrankheiten,
		@Nullable LocalDate datumDerBoosterImpfung,
		@NotNull List<Impfstoff> impfstoff
	) {
		return calculateImpfschutzForVacMeImpfungen(
			vollstaendigerImpfschutz,
			datumDerLetztenImpfung,
			geburtsdatum,
			prioritaet,
			beruflicheTaetigkeit,
			lebensumstaende,
			chronischeKrankheiten,
			datumDerBoosterImpfung,
			impfstoff,
			false,
			RegistrierungsEingang.ONLINE_REGISTRATION,
			null,
			null);
	}

	@Nullable
	private Impfschutz calculateImpfschutzForVacMeImpfungen(
		boolean vollstaendigerImpfschutz,
		@NonNull LocalDate datumDerLetztenImpfung,
		@NonNull LocalDate geburtsdatum,
		@NonNull Prioritaet prioritaet,
		@NonNull BeruflicheTaetigkeit beruflicheTaetigkeit,
		@NonNull Lebensumstaende lebensumstaende,
		@NonNull ChronischeKrankheiten chronischeKrankheiten,
		@Nullable LocalDate datumDerBoosterImpfung,
		@NotNull List<Impfstoff> impfstoffe,
		boolean immobil,
		@NonNull RegistrierungsEingang eingang,
		@Nullable LocalDate positivGetestetDatum,
		@Nullable List<Erkrankung> erkrankungen
	) {
		Pair<Fragebogen, ImpfinformationDto> fragebogenAndImpfinfo =
			createFragebogenAndImpfinfoPairForTesting(vollstaendigerImpfschutz, datumDerLetztenImpfung, geburtsdatum,
				prioritaet, beruflicheTaetigkeit, lebensumstaende, chronischeKrankheiten, datumDerBoosterImpfung,
				impfstoffe, immobil, eingang, positivGetestetDatum, erkrankungen);

		return calculateImpfschutzForVacMeImpfungen(fragebogenAndImpfinfo);
	}

	@Nullable
	private Impfschutz calculateImpfschutzForVacMeImpfungen(Pair<Fragebogen, ImpfinformationDto> fragebogenAndImpfinfo){
		return boosterPrioritaetService.calculateImpfschutz(fragebogenAndImpfinfo.getLeft(), fragebogenAndImpfinfo.getRight()).orElse(null);
	}

	@NonNull
	private Pair<Fragebogen, ImpfinformationDto> createFragebogenAndImpfinfoPairForTesting(
		boolean vollstaendigerImpfschutz,
		@NonNull LocalDate datumDerLetztenImpfung,
		@NonNull LocalDate geburtsdatum,
		@NonNull Prioritaet prioritaet,
		@NonNull BeruflicheTaetigkeit beruflicheTaetigkeit,
		@NonNull Lebensumstaende lebensumstaende,
		@NonNull ChronischeKrankheiten chronischeKrankheiten,
		@Nullable LocalDate datumDerBoosterImpfung,
		@NonNull List<Impfstoff> impfstoffe,
		boolean immobil,
		@NonNull RegistrierungsEingang eingang,
		@Nullable LocalDate positivGetestetDatum,
		@Nullable List<Erkrankung> erkrankungen
	) {
		Impfdossier impfdossier = new Impfdossier();
		Fragebogen fragebogen = createFragebogen(
			geburtsdatum,
			prioritaet,
			beruflicheTaetigkeit,
			lebensumstaende,
			chronischeKrankheiten);
		impfdossier.setVollstaendigerImpfschutzTyp(vollstaendigerImpfschutz ? VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME : null);
		Impfung impfung1 = createImpfung(impfstoffe.size() == 1 ? datumDerLetztenImpfung : datumDerLetztenImpfung.minusDays(28));
		impfung1.setImpfstoff(impfstoffe.get(0));
		impfung1.setGrundimmunisierung(true);
		impfdossier.getBuchung().setImpftermin1FromImpfterminRepo(impfung1.getTermin()); // allowed for unittest
		Impfung impfung2 = null;
		if (impfstoffe.size() > 1) {
			impfung2 = createImpfung(datumDerLetztenImpfung);
			impfung2.setImpfstoff(impfstoffe.get(1));
			impfung2.setGrundimmunisierung(true);
			impfdossier.getBuchung().setImpftermin2FromImpfterminRepo(impfung2.getTermin());// allowed for unittest
		}
		fragebogen.getRegistrierung().setImmobil(immobil);
		fragebogen.getRegistrierung().setRegistrierungsEingang(eingang);
		impfdossier.getZweiteGrundimmunisierungVerzichtet().setPositivGetestetDatum(positivGetestetDatum);

		ImpfinformationDto impfinformationDto;
		if (erkrankungen != null) {
			impfdossier.setErkrankungen(erkrankungen);
		}
		if (datumDerBoosterImpfung == null) {
			impfinformationDto = new ImpfinformationDto(KrankheitIdentifier.COVID, fragebogen.getRegistrierung(), impfung1, impfung2, impfdossier, null);
		} else {
			Impfdossiereintrag impfdossiereintrag = new Impfdossiereintrag();
			impfdossiereintrag.setImpfdossier(impfdossier);
			impfdossier.getImpfdossierEintraege().add(impfdossiereintrag);
			impfdossiereintrag.setImpffolgeNr(3);
			Impfung boosterImpfung = createImpfung(datumDerBoosterImpfung);
			// Zum Testen setzen wir die Booster auf zur Grundimmunisierung gehoerend, da sonst der Impfschutz
			// immer null ist
			boosterImpfung.setGrundimmunisierung(true);
			boosterImpfung.setImpfstoff(impfstoffe.get(2));
			impfdossiereintrag.setImpfterminFromImpfterminRepo(boosterImpfung.getTermin());
			impfinformationDto =new ImpfinformationDto(
					KrankheitIdentifier.COVID,
					fragebogen.getRegistrierung(),
					impfung1,
					impfung2,
					impfdossier,
					List.of(boosterImpfung),
					null);
		}
		Pair<Fragebogen, ImpfinformationDto> fragebogenAndImpfinfo = Pair.of(fragebogen, impfinformationDto);
		return fragebogenAndImpfinfo;
	}

	private Impfung createImpfung(LocalDate date) {
		Impfung impfung = new Impfung();
		impfung.setTimestampImpfung(date.atStartOfDay());
		impfung.setImpfstoff(moderna);
		Impftermin impftermin = new Impftermin();
		impftermin.setGebuchtFromImpfterminRepo(true);// allowed for unittest
		impfung.setTermin(impftermin);
		return impfung;
	}

	@NotNull
	private Fragebogen createFragebogen(
		LocalDate geburtsdatum,
		Prioritaet prioritaet,
		BeruflicheTaetigkeit beruflicheTaetigkeit,
		Lebensumstaende lebensumstaende,
		ChronischeKrankheiten chronischeKrankheiten
	) {
		Fragebogen fragebogen = TestdataCreationUtil.createFragebogen();
		fragebogen.setBeruflicheTaetigkeit(beruflicheTaetigkeit);
		fragebogen.setLebensumstaende(lebensumstaende);
		fragebogen.setChronischeKrankheiten(chronischeKrankheiten);
		Registrierung registrierung = fragebogen.getRegistrierung();
		registrierung.setPrioritaet(prioritaet);
		registrierung.setGeburtsdatum(geburtsdatum);
		return fragebogen;
	}

	@Test
	public void testExterneImpfungOhneVollstaendigenImpfschutz() {
		Impfschutz impfschutz = calculateImpfschutzForExterneImpfungDaten(
			false,
			DATUM_LETZTE_GRUNDIMPFUNG, null,
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null, Collections.emptyList());
		Assertions.assertNull(impfschutz);
	}

	@Test
	public void testExterneImpfungMitVollstaendigenImpfschutzUndStandardRegel() {
		Impfschutz impfschutz = calculateImpfschutzForExterneImpfungDaten(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG, null,
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null, Collections.emptyList());
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	private LocalDateTime calculateGenesenDatumAfterGrundschutz(LocalDate grundschutzDatum) {
		return grundschutzDatum.plusDays(IMPFSCHUTZ_DAUER_DAYS).atStartOfDay();
	}

	@Test
	public void testExterneImpfungMitVollstaendigenImpfschutzUndGruppeA() {
		Impfschutz impfschutz = calculateImpfschutzForExterneImpfungDaten(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG, null,
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.A,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null, Collections.emptyList());
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_PRIO_A).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testExterneImpfungMitVollstaendigenBoosterUndStandardRegel() {
		Impfschutz impfschutz = calculateImpfschutzForExterneImpfungDaten(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG, null,
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null, Collections.emptyList());
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	void testExterneImpfungMitVollstaendigenImpfschutzUnter12Jahren() {
		Impfschutz impfschutz = calculateImpfschutzForExterneImpfungDaten(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG, null,
			GEBURTSDATUM_10_JAEHRIG,
			Prioritaet.Q,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null, Collections.emptyList());
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testExterneImpfungMitVollstaendigenImpfschutzUnter12JahrenWirdAber12() {
		final LocalDate GEBDAT = LocalDate.now().minusYears(12).plusDays(1); // Wird morgen 12
		final LocalDate letzteImpfung = DATUM_LETZTE_GRUNDIMPFUNG.minusMonths(3);
		Impfschutz impfschutz = calculateImpfschutzForExterneImpfungDaten(
			true,
			letzteImpfung, null,
			GEBDAT,
			Prioritaet.Q,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null, Collections.emptyList());
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(letzteImpfung), impfschutz.getImmunisiertBis());
		Assertions.assertNotNull(impfschutz.getFreigegebenNaechsteImpfungAb());
		Assertions.assertEquals(LocalDate.now().plusDays(1), impfschutz.getFreigegebenNaechsteImpfungAb().toLocalDate());
	}

	@Test
	public void testExterneImpfungMitBoosterImpfschutzUndStandardRegel() {
		Impfschutz impfschutz = calculateImpfschutzForExterneImpfungDaten(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			null,
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			DATUM_BOOSTER_GRUNDIMPFUNG,
			Collections.emptyList());
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_BOOSTER_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_BOOSTER_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testVacMeImpfungOhneVollstaendigenImpfschutz() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			false,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna, moderna));
		Assertions.assertNull(impfschutz);
	}

	@Test
	public void testVacMeImpfungMitVollstaendigenImpfschutzUndStandardRegel() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
		Assertions.assertNotNull(impfschutz.getErlaubteImpfstoffe());
		Assertions.assertEquals(5, impfschutz.getErlaubteImpfstoffeCollection().size());
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.MODERNA_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.MODERNA_BIVALENT_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.PFIZER_BIONTECH_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.PFIZER_BIONTECH_BIVALENT_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.NOVAVAX_UUID));
	}

	@Test
	public void testVacMeImpfungMitVollstaendigenImpfschutzUndGruppeA() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.A,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_PRIO_A).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testVacMeImpfungMitVollstaendigenImpfschutzUnter12Jahren() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_10_JAEHRIG,
			Prioritaet.Q,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testVacMeImpfungMitNovovaxGrundimmunisierung() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder.create(KrankheitIdentifier.COVID)
			.withAge(25)
			.withImpfung1(LocalDate.now().minusMonths(12), novavax)
			.withImpfung2(DATUM_LETZTE_GRUNDIMPFUNG, novavax);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz = boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb()); // todo homa warum ist das 6 Monate
		Assertions.assertEquals(5, impfschutz.getErlaubteImpfstoffeCollection().size());
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.MODERNA_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.MODERNA_BIVALENT_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.PFIZER_BIONTECH_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.PFIZER_BIONTECH_BIVALENT_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.NOVAVAX_UUID));
	}


	@Test
	public void testVacMeImpfungMitNovovaxBooster() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder.create(KrankheitIdentifier.COVID)
			.withAge(25)
			.withImpfung1(LocalDate.now().minusMonths(24), novavax)
			.withImpfung2(LocalDate.now().minusMonths(16), novavax)
			.withBooster(LocalDate.now().minusMonths(10), novavax);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz = boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);
		Assertions.assertNotNull(impfschutz);
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}


	@Test
	public void testVacMeImpfungMitBoosterUndStandardRegel() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			DATUM_BOOSTER_GRUNDIMPFUNG,
			List.of(moderna, moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_BOOSTER_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_BOOSTER_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testVacMeImpfungMitUeber65RegelUndPfizer() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(pfizer, pfizer, pfizer));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testVacMeImpfungMitJanssenOhneKrankheit() {
		LocalDate impfdatum = LocalDate.now();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			impfdatum,
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(janssen));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(impfdatum), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(impfdatum.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testVacMeImpfungMitUeber65RegelUndPfizerMitBooster() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			DATUM_BOOSTER_GRUNDIMPFUNG,
			List.of(pfizer, pfizer, pfizer));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_BOOSTER_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_BOOSTER_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testVacMeImpfungMitUeber65RegelUndPfizerMitBoosterErst74JahreAlt() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_65_JAEHRIG.plusDays(5),
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(pfizer, pfizer, pfizer));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().plusDays(5).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testVacMeImpfungMitUeber65RegelUndImmobil() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(pfizer, pfizer, pfizer),
			true,
			RegistrierungsEingang.MASSENUPLOAD,
			null,
			null);
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
		Assertions.assertFalse(impfschutz.isBenachrichtigungBeiFreigabe());

		Impfschutz impfschutzOk = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(pfizer, pfizer, pfizer),
			false,
			RegistrierungsEingang.CALLCENTER_REGISTRATION,
			null,
			null);
		Assertions.assertNotNull(impfschutzOk);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutzOk.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG).atStartOfDay(), impfschutzOk.getFreigegebenNaechsteImpfungAb());
		Assertions.assertTrue(impfschutzOk.isBenachrichtigungBeiFreigabe());
	}

	@Test
	public void testVacMeImpfungImmobilAndLaterChangedToMobil() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(pfizer, pfizer, pfizer),
			true,
			RegistrierungsEingang.MASSENUPLOAD,
			null,
			null);
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
		Assertions.assertFalse(impfschutz.isBenachrichtigungBeiFreigabe());

		Pair<Fragebogen, ImpfinformationDto> fragebogenAndImpfinfo =
			createFragebogenAndImpfinfoPairForTesting(
				true,
				DATUM_LETZTE_GRUNDIMPFUNG,
				GEBURTSDATUM_65_JAEHRIG,
				Prioritaet.N,
				BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
				Lebensumstaende.FAMILIENHAUSHALT,
				ChronischeKrankheiten.KEINE,
				null,
				List.of(pfizer, pfizer, pfizer),
				true,
				RegistrierungsEingang.CALLCENTER_REGISTRATION,
				null,
				null);
		fragebogenAndImpfinfo.getLeft().getRegistrierung().setImmobil(false); // wechsel auf Mobil
		Impfschutz impfschutzNeuFreigegeben = calculateImpfschutzForVacMeImpfungen(fragebogenAndImpfinfo);
		Assertions.assertNotNull(impfschutzNeuFreigegeben);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutzNeuFreigegeben.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG).atStartOfDay(), impfschutzNeuFreigegeben.getFreigegebenNaechsteImpfungAb());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG).atStartOfDay(), impfschutzNeuFreigegeben.getFreigegebenAbSelbstzahler());
		Assertions.assertTrue(impfschutzNeuFreigegeben.isBenachrichtigungBeiFreigabe());
	}

	@Test
	public void testDateMigrationNoBenarchrichtigung() {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(pfizer, pfizer, pfizer),
			false,
			RegistrierungsEingang.DATA_MIGRATION,
			null,
			null);
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(DATUM_LETZTE_GRUNDIMPFUNG), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(DATUM_LETZTE_GRUNDIMPFUNG.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_65_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
		Assertions.assertFalse(impfschutz.isBenachrichtigungBeiFreigabe());
	}

	@Test
	public void testImpfstoffKombinationen() {
		testImpfstoffKombination(janssen, janssen, 35);
		testImpfstoffKombination(janssen, null, 35);
		testImpfstoffKombination(pfizer, janssen, 35);
		testImpfstoffKombination(moderna, janssen, 35);
		testImpfstoffKombination(sinovac, janssen, 35);
		testImpfstoffKombination(moderna, moderna, 35);
		testImpfstoffKombination(pfizer, pfizer, 35);
		testImpfstoffKombination(pfizer, moderna, 35);
		testImpfstoffKombination(pfizer, moderna, 35);
		testImpfstoffKombination(astra, moderna, 35);
		testImpfstoffKombination(astra, pfizer, 35);
		testImpfstoffKombination(astra, astra, 35);
		testImpfstoffKombination(sinovac, sinovac, 35);
		testImpfstoffKombination(sinopharm, sinopharm, 35);
		testImpfstoffKombination(covaxin, covaxin, 35);
		testImpfstoffKombination(astra, sinovac, 35);
		testImpfstoffKombination(astra, sinopharm, 35);
		testImpfstoffKombination(astra, covaxin, 35);
		testImpfstoffKombination(sinovac, sinopharm, 35);
		testImpfstoffKombination(sinovac, covaxin, 35);
		testImpfstoffKombination(novavax, novavax, 35);
		testImpfstoffKombination(astra, novavax, 35);
		testImpfstoffKombination(sinovac, novavax, 35);
		testImpfstoffKombination(pfizer, novavax, 35);
		testImpfstoffKombination(covishield, covishield, 35);
		testImpfstoffKombination(astra, covishield, 35);
		testImpfstoffKombination(sinovac, covishield, 35);
		testImpfstoffKombination(covovax, covovax, 35);
		testImpfstoffKombination(astra, covovax, 35);
		testImpfstoffKombination(sinovac, covovax, 35);
		// Kinderimpfschutz: wer mit pfizerKinder grundimmunisiert ist darf danach mit moderna oder pfizer boostern wenn er alt genug ist
		testImpfstoffKombination(pfizerKinder, pfizerKinder, 35);
	}

	@Test
	public void testImpfstoffKombinationenU30PfizerOnly() {
		enablePfizerOnlyForU30ForTest = true;
		boosterPrioritaetService.rules.clear();
		initRulesForTest();
		testImpfstoffKombination(janssen, janssen, 25);
		testImpfstoffKombination(janssen, null, 25);
		testImpfstoffKombination(moderna, moderna, 25);
		testImpfstoffKombination(pfizer, pfizer, 25);
		testImpfstoffKombination(pfizer, moderna, 25);
		testImpfstoffKombination(pfizer, moderna, 25);
		testImpfstoffKombination(astra, moderna, 25);
		testImpfstoffKombination(astra, pfizer, 25);
		testImpfstoffKombination(astra, astra, 25);
		testImpfstoffKombination(pfizerKinder, pfizerKinder, 10);
		testImpfstoffKombinationen(); // Run tests again with enablePfizerOnlyForU30ForTest = true;
		Assertions.assertTrue(enablePfizerOnlyForU30ForTest);
	}

	@Test
	public void testImpfstoffKombinationenExtern() {
		boosterPrioritaetService.rules.clear();
		initRulesForTest();

		// extern zugelassen -> Moderna und Pfizer
		testImpfstoffKombination(astra, astra, 55);

		// non WHO -> Moderna und Pfizer
		testImpfstoffKombination(sputnikV, sputnikV, 55);
		testImpfstoffKombination(zifivax, zifivax, 55);

		// Mixmatch extern und CH zugelassen -> CH zugelassen
		testImpfstoffKombination(sputnikV, moderna, 55);
		testImpfstoffKombination(zifivax, pfizer, 55);
		testImpfstoffKombination(astra, pfizer, 55);
	}

	void testImpfstoffKombination(
		@NonNull Impfstoff impfstoff1,
		@Nullable Impfstoff impfstoff2,
		int alter
	) {
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			DATUM_LETZTE_GRUNDIMPFUNG,
			LocalDate.now().minusYears(alter),
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			impfstoff2 == null ? List.of(impfstoff1) : List.of(impfstoff1, impfstoff2),
			false,
			RegistrierungsEingang.ONLINE_REGISTRATION,
			null,
			null);
		Objects.requireNonNull(impfschutz);

		final Set<UUID> erlaubteImpfstoffe = impfschutz.getErlaubteImpfstoffeCollection();
		if (enablePfizerOnlyForU30ForTest && alter <= 25) {
			Assertions.assertEquals(1, erlaubteImpfstoffe.size());
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.PFIZER_BIONTECH_UUID));
		} else {
			// Egal, womit man vorher geimpft war, sind neu nur noch Moderna (beide), Pfizer und Nuvavoxid zugelassen
			Assertions.assertEquals(5, erlaubteImpfstoffe.size());
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.MODERNA_UUID));
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.MODERNA_BIVALENT_UUID));
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.PFIZER_BIONTECH_UUID));
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.PFIZER_BIONTECH_BIVALENT_UUID));
			Assertions.assertTrue(erlaubteImpfstoffe.contains(Constants.NOVAVAX_UUID));
		}
	}

	@Test
	public void testBe65jaehrigerMitPunkte() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.SCHWERE_KRANKHEITSVERLAEUFE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().plusMonths(FREIGABE_MONTHS_NACH_IMPFUNG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testBe80jaehrig() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now().minusYears(1),
			LocalDate.now().minusYears(80),
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.SCHWERE_KRANKHEITSVERLAEUFE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now().minusYears(1)), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().minusYears(1).plusMonths(FREIGABE_MONTHS_NACH_IMPFUNG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testBe13jaehriger() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			LocalDate.now().minusYears(13),
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KRANKHEIT,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testBe20jaehrigerG() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			LocalDate.now().minusYears(20),
			Prioritaet.G,
			BeruflicheTaetigkeit.GES_PERSONAL_MIT_PAT_KONTAKT,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().plusMonths(FREIGABE_MONTHS_NACH_IMPFUNG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh13jaehriger() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			LocalDate.now().minusYears(13),
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh65jaehrigerOhnePunkte() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KRANKHEIT,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().plusMonths(6).plusDays(0).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh65jaehrigerOhnePunkteMitAlterImpfung() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now().minusYears(1),
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KRANKHEIT,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now().minusYears(1)), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().minusYears(1).plusMonths(6).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh25jaehrigerBeiFreigabeAlter() {
		initZhRulesForTest(12, FREIGABE_MONTHS_NACH_IMPFUNG, 0, FREIGABE_MONTHS_NACH_KRANKHEIT, 0);
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now().minusYears(1),
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KRANKHEIT,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now().minusYears(1)), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().minusYears(1).plusMonths(6).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh25jaehrigerB() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.B,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.SCHWERE_KRANKHEITSVERLAEUFE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().plusMonths(6).plusDays(0).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh25jaehrigerXmitSchwerenKrankheiten() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.X,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.SCHWERE_KRANKHEITSVERLAEUFE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().plusMonths(6).plusDays(0).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh25jaehrigerXMitBetreuungGefaehrd() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			GEBURTSDATUM_25_JAEHRIG,
			Prioritaet.X,
			BeruflicheTaetigkeit.BETREUUNG_VON_GEFAERD_PERSON,
			Lebensumstaende.EINZELHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().plusMonths(6).plusDays(0).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh13jaehrigerXWohnMitGefaerd() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			GEBURTSDATUM_13_JAEHRIG,
			Prioritaet.X,
			BeruflicheTaetigkeit.ANDERE,
			Lebensumstaende.MIT_BESONDERS_GEFAEHRDETEN_PERSON,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh13jaehrigerN() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			GEBURTSDATUM_13_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testZh10jaehrigerGruppeBZH() {
		initBeRulesForTest();
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			LocalDate.now(),
			GEBURTSDATUM_10_JAEHRIG,
			Prioritaet.B,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(moderna, moderna));
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(LocalDate.now()), impfschutz.getImmunisiertBis());
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testFreigabeDatumMitCovidNachImpfung1() {
		initZhRulesForTest(16, FREIGABE_MONTHS_NACH_IMPFUNG, 0, 0, 28);
		LocalDate impfdatum = LocalDate.now();
		LocalDate positivGetestetDatum = impfdatum.plusMonths(7);
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
			true,
			impfdatum,
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(pfizer, pfizer, pfizer),
			false,
			RegistrierungsEingang.DATA_MIGRATION,
			positivGetestetDatum,
			null);
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(impfdatum), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(
			positivGetestetDatum.plusMonths(0).plusDays(28).atStartOfDay(),
			impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testDisableKrankheitOffset() {
		initZhRulesForTest(16, FREIGABE_MONTHS_NACH_IMPFUNG, 0, null, null);
		LocalDate impfdatum = LocalDate.now();
		LocalDate positivGetestetDatum = impfdatum.plusMonths(7);
		Pair<Fragebogen, ImpfinformationDto> impfinformationDtoPair = createFragebogenAndImpfinfoPairForTesting(
			true,
			impfdatum,
			GEBURTSDATUM_65_JAEHRIG,
			Prioritaet.N,
			BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
			Lebensumstaende.FAMILIENHAUSHALT,
			ChronischeKrankheiten.KEINE,
			null,
			List.of(pfizer, pfizer, pfizer),
			false,
			RegistrierungsEingang.DATA_MIGRATION,
			positivGetestetDatum,
			null);
		Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(impfinformationDtoPair);
		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(impfdatum), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(
			impfdatum.plusMonths(6).plusDays(0).atStartOfDay(),
			impfschutz.getFreigegebenNaechsteImpfungAb(), "Freigabe should be offset from Impfdatum and "
				+ "not from positivGetestetDatum if offsetKrankheit is NULL");

		initZhRulesForTest(16, FREIGABE_MONTHS_NACH_IMPFUNG, 0, 0, 0);

		Impfschutz impfschutzWithRelKrankheit = calculateImpfschutzForVacMeImpfungen(impfinformationDtoPair);
		Assertions.assertNotNull(impfschutzWithRelKrankheit);
		Assertions.assertEquals(calculateGenesenDatumAfterGrundschutz(impfdatum), impfschutzWithRelKrankheit.getImmunisiertBis());
		Assertions.assertEquals(positivGetestetDatum.atStartOfDay(), impfschutzWithRelKrankheit.getFreigegebenNaechsteImpfungAb(),
			"Freigabe should be positivGetestetDatum");
	}

	@Nested
	class FreigabeMitErkrankungTest {

		@BeforeEach
		public void setUpKrankheitFreigabeOffset(){
			initZhRulesForTest(16, FREIGABE_MONTHS_NACH_IMPFUNG, 0, 0, 28);
		}


		@ParameterizedTest
		@CsvSource({
			// Impfung     | Genesen    | Erkrankung 1 | Erkrankung 2 |  Freigabedatum
			"01.01.2021    , 01.08.2021 ,              ,            ,    29.08.2021",
			"01.01.2021    ,            , 01.08.2021   ,            ,    29.08.2021",
			"01.01.2021    ,            , 01.08.2021   , 01.09.2021 ,    29.09.2021",
			"01.01.2021    ,            , 01.09.2021   , 01.08.2021 ,    29.09.2021",
			"01.01.2021    , 01.10.2021 , 01.09.2021   , 01.08.2021 ,    29.10.2021",
			"01.01.2021    , 01.08.2021 , 01.09.2021   , 01.10.2021 ,    29.10.2021",
			"01.01.2022    , 01.08.2021 , 01.09.2021   , 01.10.2021 ,    01.07.2022"
		})
		// Freigabe 4 Monate nach der letzten Erkrankung (es werden das genesenTestDatum und die Erkrankungen angeschaut)
		public void testFreigabeDatumMitErkrankungen(
			String impfungDatumString,
			String genesenTestDatumString, String erkrankung1DatumString, String erkrankung2DatumString,
			String freigabeDatumString) {

			LocalDate impfungDatum = parseDate(impfungDatumString);
			assert impfungDatum != null;
			LocalDate genesenTestDatum = parseDate(genesenTestDatumString);
			LocalDate erkrankung1Datum = parseDate(erkrankung1DatumString);
			LocalDate erkrankung2Datum = parseDate(erkrankung2DatumString);
			LocalDate expectedFreigabeDatum = parseDate(freigabeDatumString);

			List<Erkrankung> erkrankungen = new LinkedList<>();
			if (erkrankung1Datum != null) {
				Erkrankung erkrankung1 = new Erkrankung();
				erkrankung1.setDate(erkrankung1Datum);
				erkrankungen.add(erkrankung1);
			}
			if (erkrankung2Datum != null) {
				Erkrankung erkrankung2 = new Erkrankung();
				erkrankung2.setDate(erkrankung2Datum);
				erkrankungen.add(erkrankung2);
			}

			Impfschutz impfschutz = calculateImpfschutzForVacMeImpfungen(
				true,
				impfungDatum,
				GEBURTSDATUM_65_JAEHRIG,
				Prioritaet.N,
				BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
				Lebensumstaende.FAMILIENHAUSHALT,
				ChronischeKrankheiten.KEINE,
				null,
				List.of(pfizer, pfizer, pfizer),
				false,
				RegistrierungsEingang.DATA_MIGRATION,
				genesenTestDatum,
				erkrankungen);
			if (expectedFreigabeDatum == null) {
				Assertions.assertNull(impfschutz);
			} else {
				Assertions.assertNotNull(impfschutz);
				Assertions.assertEquals(
					expectedFreigabeDatum.atStartOfDay(),
					impfschutz.getFreigegebenNaechsteImpfungAb());
			}
		}

		@ParameterizedTest
		@CsvSource({
			// Impfung     | GenesenExt | Erkrankung 1 | Erkrankung 2 |  Freigabedatum
			"01.01.2022    , 01.03.2022 ,              ,            ,    01.07.2022",
			"01.01.2022    ,            , 01.02.2022   ,            ,    01.07.2022",
			"01.01.2022    ,            , 01.08.2022   , 01.09.2022 ,    29.09.2022",
			"01.01.2022    ,            , 01.09.2022   , 01.08.2022 ,    29.09.2022",
			"01.01.2022    , 01.10.2022 , 01.09.2022   , 01.08.2022 ,    29.10.2022",
			"01.01.2022    , 01.08.2022 , 01.09.2022   , 01.10.2022 ,    29.10.2022",
			"01.05.2022    , 01.02.2022 ,              ,            ,    01.11.2022"
		})
		// Freigabe 4 Monate nach der letzten Erkrankung (es werden das genesenTestDatum und die Erkrankungen angeschaut)
		public void testFreigabeDatumMitExtErkrankungen(
			String lastExternImpfungDatum,
			String genesenExt,
			String erkrankung1DatumString,
			String erkrankung2DatumString,
			String freigabeDatumString) {

			LocalDate impfungDatum = parseDate(lastExternImpfungDatum);
			assert impfungDatum != null;
			LocalDate genesenTestDatum = parseDate(genesenExt);
			LocalDate erkrankung1Datum = parseDate(erkrankung1DatumString);
			LocalDate erkrankung2Datum = parseDate(erkrankung2DatumString);
			LocalDate expectedFreigabeDatum = parseDate(freigabeDatumString);

			List<Erkrankung> erkrankungen = new LinkedList<>();
			if (erkrankung1Datum != null) {
				Erkrankung erkrankung1 = new Erkrankung();
				erkrankung1.setDate(erkrankung1Datum);
				erkrankungen.add(erkrankung1);
			}
			if (erkrankung2Datum != null) {
				Erkrankung erkrankung2 = new Erkrankung();
				erkrankung2.setDate(erkrankung2Datum);
				erkrankungen.add(erkrankung2);
			}

			Impfschutz impfschutz = calculateImpfschutzForExterneImpfungDaten(
				true,
				impfungDatum,
				genesenTestDatum,
				GEBURTSDATUM_65_JAEHRIG,
				Prioritaet.A,
				BeruflicheTaetigkeit.NICHT_ERWERBSTAETIG,
				Lebensumstaende.FAMILIENHAUSHALT,
				ChronischeKrankheiten.KEINE,
				null,
				erkrankungen);

			if (expectedFreigabeDatum == null) {
				Assertions.assertNull(impfschutz);
			} else {
				Assertions.assertNotNull(impfschutz);
				Assertions.assertEquals(
					expectedFreigabeDatum.atStartOfDay(),
					impfschutz.getFreigegebenNaechsteImpfungAb());
			}
		}

		@ParameterizedTest
		@CsvSource({
			// GrundImm 1 | GrundImm2  | GrundImm3  | Booster1   |  Booster2  | FreigabeEkif | FreigabeSelbstzahler
			"             ,            ,            ,            ,            ,              ,           ",
			"01.01.2022   ,            ,            ,            ,            ,              ,           ",
			"01.01.2022   , 01.02.2022 ,            ,            ,            , 01.08.2022   , 01.08.2022",
			"01.01.2022   , 01.02.2022 , 01.03.2022 ,            ,            , 01.09.2022   , 01.09.2022",
			"01.01.2022   , 01.02.2022 ,            , 01.03.2022 ,            ,              , 01.09.2022",
			"01.01.2022   , 01.02.2022 , 01.03.2022 , 01.04.2022 ,            ,              , 01.10.2022",
			"01.01.2022   , 01.02.2022 , 01.03.2022 , 01.04.2022 , 01.05.2022 ,              , 01.11.2022",
		})
		public void testFreigabeSelbstzahler(
			String sDatumGrundImpfung1,
			String sDatumGrundImpfung2,
			String sDatumGrundImpfung3,
			String sDatumBooster1,
			String sDatumBooster2,
			String sDatumExpectedFreigabeEkif,
			String sDatumExpectedFreigabeSelbstzahler
		) {

			Impfstoff moderna = TestdataCreationUtil.createImpfstoffModerna();

			LocalDate grundimpfung1 = parseDate(sDatumGrundImpfung1);
			LocalDate grundimpfung2 = parseDate(sDatumGrundImpfung2);
			LocalDate grundimpfung3 = parseDate(sDatumGrundImpfung3);
			LocalDate booster1 = parseDate(sDatumBooster1);
			LocalDate booster2 = parseDate(sDatumBooster2);
			LocalDate expectedFreigabeEkif = parseDate(sDatumExpectedFreigabeEkif);
			LocalDate expectedFreigabeSelbstzahler = parseDate(sDatumExpectedFreigabeSelbstzahler);

			ImpfinformationBuilder builder = new ImpfinformationBuilder();
			builder.create(KrankheitIdentifier.COVID).withAge(70);
			if (grundimpfung1 != null) {
				builder.withImpfung1(grundimpfung1, moderna);
			}
			if (grundimpfung2 != null) {
				builder.withImpfung2(grundimpfung2, moderna);
			}
			if (grundimpfung3 != null) {
				builder.withBooster(grundimpfung3, moderna, true);
			}
			if (booster1 != null) {
				builder.withBooster(booster1, moderna);
			}
			if (booster2 != null) {
				builder.withBooster(booster2, moderna);
			}

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
	}

	@Test
	public void test70jaehrigZweiterBooster() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder.create(KrankheitIdentifier.COVID)
			.withAge(70)
			.withImpfung1(LocalDate.now().minusMonths(12), moderna)
			.withImpfung2(LocalDate.now().minusMonths(11), moderna)
			.withBooster(LocalDate.now().minusMonths(7), moderna);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz = boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		Assertions.assertNotNull(impfschutz);
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void test80jaehrigZweiterBooster() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		LocalDate lastImpfungDate = LocalDate.now().minusMonths(7);
		builder.create(KrankheitIdentifier.COVID)
			.withAge(80)
			.withImpfung1(LocalDate.now().minusMonths(12), moderna)
			.withImpfung2(LocalDate.now().minusMonths(11), moderna)
			.withBooster(lastImpfungDate, moderna);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz = boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(lastImpfungDate.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_80_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void test80jaehrigZweiterBoosterMit3Grundimmunisierungen() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		LocalDate lastImpfungDate = LocalDate.now().minusMonths(7);
		builder.create(KrankheitIdentifier.COVID)
			.withAge(80)
			.withImpfung1(LocalDate.now().minusMonths(12), moderna)
			.withImpfung2(LocalDate.now().minusMonths(11), moderna)
			.withBooster(LocalDate.now().minusMonths(10), moderna, true)
			.withBooster(lastImpfungDate, moderna);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz = boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(lastImpfungDate.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_80_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void test80jaehrigDritterBooster() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder.create(KrankheitIdentifier.COVID)
			.withAge(80)
			.withImpfung1(LocalDate.now().minusMonths(12), moderna)
			.withImpfung2(LocalDate.now().minusMonths(11), moderna)
			.withBooster(LocalDate.now().minusMonths(10), moderna)
			.withBooster(LocalDate.now().minusMonths(7), moderna);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz = boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		Assertions.assertNotNull(impfschutz);
		Assertions.assertNull(impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void testMorgen80jaehrigZweiterBooster() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		LocalDate dateBoosterImpfung = LocalDate.now().minusMonths(10);
		builder.create(KrankheitIdentifier.COVID)
			.withBirthday(GEBURTSDATUM_MORGEN_80_JAEHRIG)
			.withImpfung1(LocalDate.now().minusMonths(12), moderna)
			.withImpfung2(LocalDate.now().minusMonths(11), moderna)
			.withBooster(dateBoosterImpfung, moderna);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz = boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		Assertions.assertNotNull(impfschutz);
		Assertions.assertNotNull(impfschutz.getFreigegebenNaechsteImpfungAb());
		Assertions.assertEquals(LocalDate.now().plusDays(1), impfschutz.getFreigegebenNaechsteImpfungAb().toLocalDate());
		Assertions.assertNotNull(impfschutz.getFreigegebenAbSelbstzahler());
		Assertions.assertEquals(dateBoosterImpfung.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_80_JAEHRIG), impfschutz.getFreigegebenAbSelbstzahler().toLocalDate());
	}

	@Test
	public void testMorgen12jaehrigBooster() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		LocalDate dateImpfung2 = LocalDate.now().minusMonths(11);
		builder.create(KrankheitIdentifier.COVID)
			.withBirthday(GEBURTSDATUM_MORGEN_12_JAEHRIG)
			.withImpfung1(LocalDate.now().minusMonths(12), pfizerKinder)
			.withImpfung2(dateImpfung2, pfizerKinder);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz = boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(5, impfschutz.getErlaubteImpfstoffeCollection().size());
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.MODERNA_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.MODERNA_BIVALENT_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.PFIZER_BIONTECH_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.PFIZER_BIONTECH_BIVALENT_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.NOVAVAX_UUID));
		Assertions.assertEquals(dateImpfung2.plusDays(IMPFSCHUTZ_DAUER_DAYS).atStartOfDay(), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(LocalDate.now().plusDays(1).atStartOfDay(), impfschutz.getFreigegebenAbSelbstzahler());
		Assertions.assertEquals(LocalDate.now().plusDays(1).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}

	@Test
	public void test25aehrigrMitPfizerKinderGrundimmunisierungBooster() {
		LocalDate dateImpfung2 = LocalDate.now().minusMonths(12);
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder.create(KrankheitIdentifier.COVID)
			.withBirthday(GEBURTSDATUM_25_JAEHRIG)
			.withImpfung1(LocalDate.now().minusMonths(24), pfizerKinder)
			.withImpfung2(dateImpfung2, pfizerKinder);

		final ImpfinformationDto infos = builder.getInfos();
		Impfschutz impfschutz = boosterPrioritaetService.calculateImpfschutz(builder.getFragebogen(), infos).orElse(null);

		Assertions.assertNotNull(impfschutz);
		Assertions.assertEquals(5, impfschutz.getErlaubteImpfstoffeCollection().size());
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.MODERNA_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.MODERNA_BIVALENT_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.PFIZER_BIONTECH_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.PFIZER_BIONTECH_BIVALENT_UUID));
		Assertions.assertTrue(impfschutz.getErlaubteImpfstoffeCollection().contains(Constants.NOVAVAX_UUID));
		Assertions.assertEquals(dateImpfung2.plusDays(IMPFSCHUTZ_DAUER_DAYS).atStartOfDay(), impfschutz.getImmunisiertBis());
		Assertions.assertEquals(dateImpfung2.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenAbSelbstzahler());
		Assertions.assertEquals(dateImpfung2.plusMonths(FREIGBAME_MONTHS_NACH_IMPFUNG_AB_12_JAEHRIG).atStartOfDay(), impfschutz.getFreigegebenNaechsteImpfungAb());
	}


	@Nullable
	private LocalDate parseDate(@Nullable String dateString) {
		if (dateString == null) {
			return null;
		}

		DateTimeFormatter formatter = DateUtil.DEFAULT_DATE_FORMAT.apply(Locale.GERMANY);
		return LocalDate.parse(dateString, formatter);
	}
}
