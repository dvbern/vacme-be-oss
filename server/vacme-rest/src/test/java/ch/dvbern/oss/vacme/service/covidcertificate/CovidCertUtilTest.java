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

package ch.dvbern.oss.vacme.service.covidcertificate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.helper.TestImpfstoff;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.DeservesZertifikatValidator;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.provider.CsvSource;

class CovidCertUtilTest {

	@ParameterizedTest(name = "[{index}] {arguments}") // arguments ist kuerzer als argumentsWithNames
	// @formatter:off
	@CsvSource({
		// TODO noch hinzufuegen: a) trotzdemVollstaendigGrundimmunisieren b) Faelle, bei denen es kein Impfzertifikat gibt

		// Faelle aus Excel "Zertifikate X von 1 Definition" Version 2.12 > "Covid19UseCases"
		/*
		extern                          VacMe Grundimmunisierung                     VacMe Booster                         Impfschutztyp	                      expected  expected     Quelle
		anzahl, genesen, Impfstoff      Impfstoff1, Impfstoff2, genesen,             Booster1, booster1IsGrund, Booster2,        	                              x/y       deservesZert Fallnummer
		*/

		// Primovaccination CH
		"1, false, TEST_PFIZER,         TEST_PFIZER,  , false,                       , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            2, 2,    true        , N°4: 2xPfizer (ext+vacme)",
		"0, false, ,                    TEST_PFIZER,  , false,                       , , ,                                 ,                                       1, 2,    false       , N°5: 1xPfizer & missing 2nd dose (vacme)",
		"0, false, ,                    TEST_PFIZER,  , true,                        , , ,                                 VOLLSTAENDIG_VACME_GENESEN,             1, 1,    true        , N°6: genesen+Pfizer (vacme)",
		"0, false, ,                    TEST_MODERNA, TEST_PFIZER, false,            , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    true        , N°8: Moderna+Pfizer (vacme)",
		"1, false, TEST_MODERNA,        TEST_PFIZER,  , false,                       , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            2, 2,    true        , N°8: Moderna+Pfizer (ext+vacme)",
		"0, false, ,                    TEST_PFIZER, TEST_JOHNSON, false,            , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    true        , N°9: Pfizer+Janssen (vacme)",
		"1, false, TEST_PFIZER,         TEST_JOHNSON, , false,                       , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    true        , N°9: Pfizer+Janssen (ext+vacme)",
		"0, false, ,                    TEST_PFIZER, TEST_PFIZER, false, TEST_PFIZER , , ,                                 VOLLSTAENDIG_VACME,                     3, 3,    true        , N°10: 3xPfizer (vacme)",
		"1, false, TEST_PFIZER,         TEST_PFIZER, TEST_PFIZER, false,             , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            3, 3,    true        , N°10: 3xPfizer (1ext+2vacme)",
		"2, false, TEST_PFIZER,         ,  , false,                                  TEST_PFIZER, , ,                      VOLLSTAENDIG_EXTERNESZERTIFIKAT,        3, 3,    true        , N°10: 3xPfizer (2ext+1vacme)",

		// Primovaccination non-CH
		"1, false, TEST_ASTRA,          TEST_ASTRA, , false,                         , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            2, 2,    false       , N°11: 2xAZ (ext+vacme)",
		"0, false, ,                    TEST_ASTRA, TEST_ASTRA, false,               , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    false       , N°11: 2xAZ (vacme)",
		"1, false, TEST_COVAXIN,        TEST_COVAXIN, , false,                       , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            2, 2,    false       , N°13: 2xCovaxin (ext+vacme)",

		// Primovaccination MixMatch
		"1, false, TEST_ASTRA,          TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            2, 2,    true        , N°15: AZ + Pfizer (ext+vacme)",
		"0, false, ,                    TEST_ASTRA, TEST_PFIZER, false,              , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    true        , N°15: AZ + Pfizer (vacme)",
		"1, false, TEST_SINOPH,         TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            2, 2,    true        , N°16: Sinopharm + Pfizer", // Neu: grundimmunisiert
		"2, false, TEST_SINOPH,         TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            3, 3,    true        , N°17: 2xSinopharm + Pfizer",
		"1, false, TEST_SPUTN_V,        TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            1, 1,    true        , N°18: Sputnik5 + Pfizer",
		"2, false, TEST_SINOPH,         TEST_SINOPH, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            3, 3,    false       , N°20: 3xSinopharm",
		"1, false, TEST_ZIFIVAX,        TEST_PFIZER, TEST_PFIZER, false,             , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    true        , N°21: Zifivac + 2Pfizer",
		"2, false, TEST_ZIFIVAX,        TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            1, 1,    true        , N°22: 2xZifivac + Pfizer",

		// Janssen
		"0, false, ,                    TEST_JOHNSON, , false,                       , , ,                                 VOLLSTAENDIG_VACME,                     1, 1,    true        , N°23: 1xJanssen",
		"0, false, ,                    TEST_JOHNSON, , false,                       TEST_JOHNSON, , ,                     VOLLSTAENDIG_VACME,                     2, 1,    true        , N°24: Janssen (vacme) + Janssen",
		"1, false, TEST_JOHNSON,        , , false,                                   TEST_JOHNSON, , ,                     VOLLSTAENDIG_EXTERNESZERTIFIKAT,        2, 1,    true        , N°24: Janssen (ext) + Janssen",
		"1, true, TEST_JOHNSON,         , , false,                                   TEST_JOHNSON, , ,                     VOLLSTAENDIG_EXTERNESZERTIFIKAT_GENESEN,2, 1,    true        , N°25b: genesen+Janssen (ext) + Janssen",
		"1, false, TEST_JOHNSON,        , , false,                                   TEST_PFIZER, grund, ,                 VOLLSTAENDIG_EXTERNESZERTIFIKAT,        2, 2,    true        , N°26: Janssen + Pfizer (ext+vacme)",
		"0, false, ,                    TEST_JOHNSON, , false,                       TEST_PFIZER,             grund, ,     VOLLSTAENDIG_VACME,                     2, 2,    true        , N°26: Janssen + Pfizer (vacme)",
		"1, false, TEST_PFIZER,         TEST_JOHNSON, , false,                       , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    true        , N°27: Pfizer+Janssen",
		"1, false, TEST_PFIZER,         TEST_JOHNSON, , false,                       TEST_JOHNSON, , ,                     VOLLSTAENDIG_VACME,                     3, 3,    true        , N°28b: Pfizer+Janssen",
		"1, false, TEST_JOHNSON,        , , false,                                   TEST_PFIZER, grund, ,                 VOLLSTAENDIG_EXTERNESZERTIFIKAT,        2, 2,    true        , N°29a: Janssen + Pfizer (ext+vacme)",
		"0, false, ,                    TEST_JOHNSON, , false,                       TEST_PFIZER,             grund, ,     VOLLSTAENDIG_VACME,                     2, 2,    true        , N°29a: Janssen + Pfizer (vacme)",
		"1, false, TEST_JOHNSON,        , , false,                                   TEST_PFIZER, grund, TEST_PFIZER,      VOLLSTAENDIG_EXTERNESZERTIFIKAT,        3, 3,    true        , N°29b: Janssen + Pfizer (ext+vacme) + Pfizer",
		"0, false, ,                    TEST_JOHNSON, , false,                       TEST_PFIZER, grund, TEST_PFIZER,      VOLLSTAENDIG_VACME,                     3, 3,    true        , N°29b: Janssen + Pfizer (vacme)+ Pfizer",
		"0, false, ,                    TEST_PFIZER, TEST_PFIZER, false,             , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    true        , N°30: 2x Pfizer (vacme)",
		"1, false, TEST_PFIZER,         TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            2, 2,    true        , N°30: 2x Pfizer (ext+vacme)",
		"0, false, ,                    TEST_PFIZER, TEST_PFIZER, false,             TEST_JOHNSON , , ,                    VOLLSTAENDIG_VACME,                     3, 3,    true        , N°30b: 2x Pfizer (vacme) + Janssen",
		"1, false, TEST_PFIZER,         TEST_PFIZER, , false,                        TEST_JOHNSON, , ,                     VOLLSTAENDIG_EXT_PLUS_VACME,            3, 3,    true        , N°30b: 2x Pfizer (ext+vacme) + Janssen",
		"2, false, TEST_PFIZER,         , , false,                                   TEST_JOHNSON, , ,                     VOLLSTAENDIG_EXTERNESZERTIFIKAT,        3, 3,    true        , N°30b: 2x Pfizer (ext) + Janssen",
		"0, false, ,                    TEST_JOHNSON, , false,                       , , ,                                 VOLLSTAENDIG_VACME,                     1, 1,    true        , N°31: 1x Janssen",
		"0, false, ,                    TEST_JOHNSON, , false,                       TEST_PFIZER, , ,                      VOLLSTAENDIG_VACME,                     2, 1,    true        , N°31: 1x Janssen (vacme) + Pfizer",
		"1, false, TEST_JOHNSON,        , , false,                                   TEST_PFIZER , , ,                     VOLLSTAENDIG_EXTERNESZERTIFIKAT,        2, 1,    true        , N°31: 1x Janssen (ext) + Pfizer",
		"1, true, TEST_JOHNSON,         , , false,                                   TEST_PFIZER , , ,                     VOLLSTAENDIG_EXTERNESZERTIFIKAT_GENESEN,2, 1,    true        , N°32: 1x Janssen+genesen (ext) + Pfizer",
		"0, false, ,                    TEST_JOHNSON, , true,                        , , ,                                 VOLLSTAENDIG_VACME,                     1, 1,    true        , N°32: genesen+Janssen",
		"0, false, ,                    TEST_JOHNSON, , true,                        TEST_PFIZER, , ,                      VOLLSTAENDIG_VACME,                     2, 1,    true        , N°32b: genesen+Janssen (vacme) + Pfizer",
		"1, true, TEST_JOHNSON,         , , false,                                   TEST_PFIZER, , ,                      VOLLSTAENDIG_EXTERNESZERTIFIKAT_GENESEN,2, 1,    true        , N°32b: genesen+Janssen (ext) + Pfizer",
		"2, false, TEST_COVAXIN,        , , false,                                   TEST_JOHNSON, , ,                     VOLLSTAENDIG_EXTERNESZERTIFIKAT,        3, 3,    true        , N°33: 2xCovaxin + Janssen",

		// TODO 34-51

		// Booster to the supplemented primovaccination
		"0, false, ,                    TEST_ASTRA, TEST_PFIZER, false,              , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    true        , N°52a: AZ+Pfizer (vacme)",
		"1, false, TEST_ASTRA,          TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            2, 2,    true        , N°52a: AZ (ext) + Pfizer (vacme)",
		"0, false, ,                    TEST_ASTRA, TEST_PFIZER, false,              TEST_PFIZER, , ,                      VOLLSTAENDIG_VACME,                     3, 3,    true        , N°52b: AZ+Pfizer (vacme) + Pfizer",
		"1, false, TEST_ASTRA,          TEST_PFIZER, , false,                        TEST_PFIZER, , ,                      VOLLSTAENDIG_EXT_PLUS_VACME,            3, 3,    true        , N°52b: AZ (ext) + Pfizer (vacme) + Pfizer",
		"1, false, TEST_SINOPH,         TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            2, 2,    true        , N°53a: Sinopharm + Pfizer", // Neu: grundimmunisiert
		"1, false, TEST_SINOPH,         TEST_PFIZER, , false,                        TEST_PFIZER, , ,                      VOLLSTAENDIG_EXT_PLUS_VACME,            3, 3,    true        , N°53b: Sinopharm + Pfizer + Pfizer",// Neu: grundimmunisiert nach 1 Pfizer
		"1, true, TEST_SINOPH,          TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_GENESEN_PLUS_VACME,    2, 2,    true        , N°54: Sinopharm genesen-Zertifikat + Pfizer", // Abweichung zur Tabelle, dort erwarten sie 2/1
		"2, false, TEST_SINOPH,         TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            3, 3,    true        , N°55: 2 Sinopharm+Pfizer",
		"2, false, TEST_SINOPH,         TEST_PFIZER, , false,                        TEST_PFIZER, , ,                      VOLLSTAENDIG_EXT_PLUS_VACME,            4, 4,    true        , N°55: 2 Sinopharm+Pfizer + Pfizer",
		"1, false, TEST_SPUTN_V,        TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            1, 1,    true        , N°56a: SputnikV + Pfizer",
		"1, false, TEST_SPUTN_V,        TEST_PFIZER, , false,                        TEST_PFIZER, , ,                      VOLLSTAENDIG_EXT_PLUS_VACME,            2, 1,    true        , N°56b: SputnikV + Pfizer + Pfizer", // changed in V2.12
		"2, false, TEST_SPUTN_V,        , , false,                                   TEST_PFIZER, , ,                      VOLLSTAENDIG_EXTERNESZERTIFIKAT,        1, 1,    true        , N°57: 2 SputnikV + Pfizer",
		"3, false, TEST_SINOPH,         , , false,                                   TEST_PFIZER, , ,                      VOLLSTAENDIG_EXTERNESZERTIFIKAT,        4, 4,    true        , N°58: 3 Sinopharm + Pfizer",
		"1, false, TEST_SINOPH,         TEST_PFIZER, , false,                        TEST_PFIZER, grund, ,                 VOLLSTAENDIG_EXT_PLUS_VACME,            3, 3,    true        , N°59 a: 1 Sinoph + 2 Pfizer", // Neu: schon nach ERSTE_IMPFUNG grundimmunisiert
		"1, false, TEST_SINOPH,         TEST_PFIZER, , false,                        TEST_PFIZER, grund, TEST_PFIZER,      VOLLSTAENDIG_EXT_PLUS_VACME,            4, 4,    true        , N°59 b: 1 Sinoph + 2 Pfizer + Pfizer", // Neu: schon nach ERSTE_IMPFUNG grundimmunisiert
		"1, false, TEST_ZIFIVAX,        TEST_PFIZER, TEST_PFIZER, false,             , , ,                                 VOLLSTAENDIG_VACME,                     2, 2,    true        , N°60 a: 1 Zifi + 2 Pfizer",
		"1, false, TEST_ZIFIVAX,        TEST_PFIZER, TEST_PFIZER, false,             TEST_PFIZER, , ,                      VOLLSTAENDIG_VACME,                     3, 3,    true        , N°60 b: 1 Zifi + 2 Pfizer + Pfizer",
		"2, false, TEST_ZIFIVAX,        TEST_PFIZER, , false,                        , , ,                                 VOLLSTAENDIG_EXT_PLUS_VACME,            1, 1,    true        , N°61 a: 2 Zifi + 1 Pfizer",
		"2, false, TEST_ZIFIVAX,        TEST_PFIZER, , false,                        TEST_PFIZER, , ,                      VOLLSTAENDIG_EXT_PLUS_VACME,            2, 1,    true        , N°61 b: 2 Zifi + 1 Pfizer + Pfizer", // changed in V2.12

		"2, true, TEST_ABDALA,          ,  , false,                                  TEST_MODERNA, ,  ,                    VOLLSTAENDIG_EXTERNESZERTIFIKAT_GENESEN,1, 1,    true        , frei: 2 non-WHO+genesen (ext) + Moderna",
	})
	// @formatter:on
	public void testZahlVorUndNachSchraegstrich(ArgumentsAccessor accessor) {
		Arguments args = new MyArgumentAggregator().aggregateArguments(accessor);

		// parse params and setup scenario
		ImpfinformationDto infos = setup(args);
		Impfung aktuelleImpfung = ImpfinformationenService.getNewestVacmeImpfung(infos);
		Assertions.assertNotNull(aktuelleImpfung);

		// calculate Zertifikat: x/y
		var output = CovidCertUtils.calculateZahlVorUndNachSchraegstrich(infos, infos.getRegistrierung(), aktuelleImpfung);

		// compare Nummerierung
		String expected = "" + args.expectedZahlVorSchraegstrich + "/" + args.expectedZahlNachSchraegstrich;
		String actual = "" + output.getLeft() + "/" + output.getRight();
		Assertions.assertEquals(expected, actual, "nummerierung " + args.quelle);

		// compare Grundimmunisierung
		Assertions.assertEquals(
			args.vollstaendigerImpfschutzTyp != null,
			infos.getImpfdossier().getVollstaendigerImpfschutzTyp() != null,
			"grundimmunisiert " + args.quelle);
		Assertions.assertEquals(
			args.vollstaendigerImpfschutzTyp,
			infos.getImpfdossier().getVollstaendigerImpfschutzTyp(),
			"vollstaendigerImpfschutzTyp " + args.quelle);

		final boolean deservesZertifikat = DeservesZertifikatValidator.deservesZertifikat(
			infos, aktuelleImpfung);
		Assertions.assertEquals(
			args.expectedDeservesZertifikat,
			deservesZertifikat,
			"deservesZertifikat " + args.quelle);
	}

	private ImpfinformationDto setup(Arguments testCase0) {
		return setup(
			testCase0.externAnzahl,
			testCase0.externGenesen,
			testCase0.externImpfstoffT,
			testCase0.impfstoff1t,
			testCase0.impfstoff2t,
			testCase0.genesen,
			testCase0.impfstoffBooster1t,
			testCase0.booster1IsGrund,
			testCase0.impfstoffBooster2t);
	}

	private ImpfinformationDto setup(
		int externAnzahl,
		boolean externGenesen,
		@Nullable Impfstoff impfstoffExtern,
		@Nullable Impfstoff impfstoff1,
		@Nullable Impfstoff impfstoff2,
		boolean genesen,
		@Nullable Impfstoff impfstoffBooster1,
		boolean booster1IsGrundimmunisierung,
		@Nullable Impfstoff impfstoffBooster2
	) {
		// Registrierung, vollstaendig, genesen
		Registrierung registrierung = new Registrierung();
		Impfdossier impfdossier = TestdataCreationUtil.createDummyImpfdossier(KrankheitIdentifier.COVID, registrierung);
		impfdossier.getZweiteGrundimmunisierungVerzichtet().setGenesen(genesen);

		// Impfungen zusammenstellen fuer Grundimmunisierung: extern, impfung1, impfung2
		ExternesZertifikat externesZertifikat = null;
		Impfung impfung1 = null;
		Impfung impfung2 = null;
		List<Impfung> boosterImpfungen = new LinkedList<>();
		int impffolgeNr = 1;

		if (impfstoffExtern != null) {
			externesZertifikat = createExternesZertifikat(externAnzahl, externGenesen, impfstoffExtern);
			externesZertifikat.setImpfdossier(impfdossier);
			impffolgeNr += externAnzahl;
		}
		if (impfstoff1 != null) {
			impfung1 = TestdataCreationUtil.createImpfung(LocalDate.now(), impfstoff1);
			impfung1.getTermin().setImpffolge(Impffolge.ERSTE_IMPFUNG);
			impffolgeNr++;
		}
		if (impfstoff2 != null) {
			Assertions.assertNotNull(impfstoff1);
			impfung2 = TestdataCreationUtil.createImpfung(LocalDate.now(), impfstoff2);
			impfung2.getTermin().setImpffolge(Impffolge.ZWEITE_IMPFUNG);
			impffolgeNr++;
		}

		// Boosterimpfungen
		if (impfstoffBooster1 != null) {
			addBoosterImpfung(impfstoffBooster1, booster1IsGrundimmunisierung, impfdossier, boosterImpfungen, impffolgeNr);
			impffolgeNr++;
		}
		if (impfstoffBooster2 != null) {
			Assertions.assertNotNull(impfstoffBooster1);
			addBoosterImpfung(impfstoffBooster2, false, impfdossier, boosterImpfungen, impffolgeNr);
		}

		ImpfinformationDto infos = new ImpfinformationDto(
			KrankheitIdentifier.COVID,
			registrierung,
			impfung1,
			impfung2,
			impfdossier,
			boosterImpfungen,
			externesZertifikat);

		// vollstaendiger Impfschutz?
		VollstaendigerImpfschutzTyp vollstaendigerImpfschutzTyp1 = calculateImpfschutzTyp(infos);
		if (vollstaendigerImpfschutzTyp1 != null) {
			infos.getImpfdossier().setVollstaendigerImpfschutzTyp(vollstaendigerImpfschutzTyp1);
			infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.IMMUNISIERT);
		}

		return infos;
	}

	// Pseudo-Berechnung des vollstaendigen Impfschutztyps. Wir simulieren hier quasi, ob die Registrierung mit unserem Setup grundimmunisiert ist oder nicht.
	@Nullable
	private VollstaendigerImpfschutzTyp calculateImpfschutzTyp(ImpfinformationDto infos) {

		if (infos.getExternesZertifikat() != null && infos.getExternesZertifikat().isGrundimmunisiert(infos.getKrankheitIdentifier())) {
			return infos.getExternesZertifikat().isGenesen()
				? VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXTERNESZERTIFIKAT_GENESEN
				: VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXTERNESZERTIFIKAT;
		}

		if (infos.getImpfung1() != null) {
			ImpfinformationDto infos0 = new ImpfinformationDto(KrankheitIdentifier.COVID, infos.getRegistrierung(), infos.getImpfung1(), null,
				null, infos.getExternesZertifikat());
			if (ImpfinformationenService.willBeGrundimmunisiertAfterErstimpfung(infos.getImpfung1(), infos0)) {
				return infos.getImpfung1().getImpfstoff().getAnzahlDosenBenoetigt() == 1
					? VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME
					: ((infos.getExternesZertifikat() != null && infos.getExternesZertifikat().isGenesen())
					? VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_GENESEN_PLUS_VACME
					: VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_PLUS_VACME);
			}
		}

		if (infos.getImpfung2() != null) {
			return VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME;
		} else {
			if (infos.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().isGenesen()) {
				return VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME_GENESEN;
			}
		}

		return null;

	}

	private void addBoosterImpfung(
		@NonNull Impfstoff impfstoffBooster1,
		boolean isGrundimmunisierung,
		@NonNull Impfdossier impfdossier,
		@NonNull List<Impfung> boosterImpfungen,
		int impffolgeNr
	) {
		Impfung impfung = TestdataCreationUtil.addBoosterToDossier(impfdossier, LocalDateTime.now(), impffolgeNr);
		impfung.setImpfstoff(impfstoffBooster1);
		impfung.setGrundimmunisierung(isGrundimmunisierung);
		boosterImpfungen.add(impfung);
	}

	@NonNull
	private ExternesZertifikat createExternesZertifikat(
		int externAnzahl,
		boolean externGenesen,
		@NonNull Impfstoff impfstoffExtern
	) {
		ExternesZertifikat externesZertifikat;
		externesZertifikat = new ExternesZertifikat();
		externesZertifikat.setImpfstoff(impfstoffExtern);
		externesZertifikat.setGenesen(externGenesen);
		externesZertifikat.setAnzahlImpfungen(externAnzahl);
		externesZertifikat.setLetzteImpfungDate(LocalDate.now().minusMonths(4));
		return externesZertifikat;
	}



	@AllArgsConstructor
	class Arguments {

		int externAnzahl;
		boolean externGenesen;
		Impfstoff externImpfstoffT;
		Impfstoff impfstoff1t;
		Impfstoff impfstoff2t;
		boolean genesen;
		Impfstoff impfstoffBooster1t;
		boolean booster1IsGrund;
		Impfstoff impfstoffBooster2t;
		VollstaendigerImpfschutzTyp vollstaendigerImpfschutzTyp;
		Integer expectedZahlVorSchraegstrich;
		Integer expectedZahlNachSchraegstrich;
		boolean expectedDeservesZertifikat;
		String quelle;
	}

	class MyArgumentAggregator {
		public Arguments aggregateArguments(ArgumentsAccessor accessor) throws ArgumentsAggregationException {
			int i = 0;
			return new Arguments(
				accessor.getInteger(i++),
				accessor.getBoolean(i++),

				TestImpfstoff.createImpfstoffForTest(accessor.get(i++, TestImpfstoff.class)),
				TestImpfstoff.createImpfstoffForTest(accessor.get(i++, TestImpfstoff.class)),
				TestImpfstoff.createImpfstoffForTest(accessor.get(i++, TestImpfstoff.class)),
				accessor.getBoolean(i++),

				TestImpfstoff.createImpfstoffForTest(accessor.get(i++, TestImpfstoff.class)),
				"grund".equals(accessor.getString(i++)),

				TestImpfstoff.createImpfstoffForTest(accessor.get(i++, TestImpfstoff.class)),
				accessor.get(i++, VollstaendigerImpfschutzTyp.class),
				accessor.getInteger(i++),
				accessor.getInteger(i++),
				accessor.getBoolean(i++),
				accessor.getString(i++));
		}
	}

}
