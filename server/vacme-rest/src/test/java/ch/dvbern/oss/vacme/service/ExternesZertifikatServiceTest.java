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

package ch.dvbern.oss.vacme.service;

import java.time.LocalDate;
import java.util.Optional;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.MissingForGrundimmunisiert;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.helper.TestImpfstoff;
import ch.dvbern.oss.vacme.jax.registration.ExternGeimpftJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfstoffJax;
import ch.dvbern.oss.vacme.repo.ExternesZertifikatRepo;
import ch.dvbern.oss.vacme.repo.ImpfempfehlungChGrundimmunisierungRepo;
import ch.dvbern.oss.vacme.repo.ImpfstoffRepo;
import ch.dvbern.oss.vacme.repo.KrankheitRepo;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

public class ExternesZertifikatServiceTest {

	private ExternesZertifikatService externesZertifikatService;
	private ExternesZertifikatRepo externesZertifikatRepo;
	private ImpfstoffService impfstoffService;

	@Nullable
	private ExternesZertifikat persistedZertifikat;

	@BeforeAll
	static void setUp() {
		System.setProperty("vacme.mandant", "BE");
	}

	@BeforeEach
	public void before() {
		// ImpfstoffService
		ImpfstoffRepo impfstoffRepo = Mockito.mock(ImpfstoffRepo.class);
		ImpfempfehlungChGrundimmunisierungRepo empfehlungRepo = Mockito.mock(ImpfempfehlungChGrundimmunisierungRepo.class);
		KrankheitRepo krankheitRepo = Mockito.mock(KrankheitRepo.class);
		impfstoffService = new ImpfstoffService(impfstoffRepo, empfehlungRepo, krankheitRepo);
		Mockito.when(impfstoffRepo.getById(any())).thenAnswer(invocation -> {
			ID<Impfstoff> argument = invocation.getArgument(0, ID.class);
			return Optional.ofNullable(TestImpfstoff.getImpfstoffById(argument.getId()));
		});

		// ExternesZertifikatRepo
		externesZertifikatRepo = Mockito.mock(ExternesZertifikatRepo.class);
		// - find
		Mockito
			.doAnswer(invocation -> Optional.ofNullable(persistedZertifikat))
			.when(externesZertifikatRepo).findExternesZertifikatForDossier(any());
		// - create
		Mockito.doAnswer(invocation -> {
			persistedZertifikat = invocation.getArgument(0, ExternesZertifikat.class);
			return persistedZertifikat;
		}).when(externesZertifikatRepo).create(any(ExternesZertifikat.class));
		// - remove
		Mockito.doAnswer(invocation -> {
			persistedZertifikat = null;
			return null;
		}).when(externesZertifikatRepo).remove(any());
		// - update
		Mockito.doAnswer(invocation -> {
			persistedZertifikat = invocation.getArgument(0, ExternesZertifikat.class);
			return persistedZertifikat;
		}).when(externesZertifikatRepo).update(any(ExternesZertifikat.class));

		// ExternesZertifikatService
		externesZertifikatService = new ExternesZertifikatService(
			externesZertifikatRepo,
			impfstoffService,
			Mockito.mock(UserPrincipal.class),
			Mockito.mock(ImpfinformationenService.class),
			Mockito.mock(BoosterService.class),
			Mockito.mock(DossierService.class),
			Mockito.mock(ImpfdossierService.class));
	}

	@Test
	public void testIsGrundimmunisiert() {
		ExternesZertifikat externesZertifikat = new ExternesZertifikat();
		final Impfstoff moderna = TestImpfstoff.createImpfstoffForTest(TestImpfstoff.TEST_MODERNA);
		Assertions.assertNotNull(moderna);
		externesZertifikat.setImpfstoff(moderna);
		externesZertifikat.setAnzahlImpfungen(2);
		externesZertifikat.setGenesen(false);
		Assertions.assertTrue(externesZertifikat.isGrundimmunisiert(KrankheitIdentifier.COVID));
	}

	@CsvSource({
		// Moderna (2)
		"true, TEST_MODERNA, 2, false",
		"true, TEST_MODERNA, 1, true",

		"false, TEST_MODERNA, 1, false",

		// Johnson (1)
		"true, TEST_JOHNSON, 1, false",
		"true, TEST_JOHNSON, 2, false",
		"true, TEST_JOHNSON, 1, true",

		"false, TEST_JOHNSON, 0, true", // Johnson mit 0 Impfungen und genesen -> NEIN!

		// Kazachstan: braucht 2 Dosen, aber ist nicht als grundimmunisierung akzeptiert (1->2, 2->1)
		"false, TEST_KAZAKH, 1, false",
		"false, TEST_KAZAKH, 2, false",
		"false, TEST_KAZAKH, 3, false",
		"false, TEST_KAZAKH, 4, false",
		"false, TEST_KAZAKH, 1, true",
		"false, TEST_KAZAKH, 2, true",
		"false, TEST_KAZAKH, 3, true",
		"false, TEST_KAZAKH, 4, true",

		// Abdala: // braucht 3 Dosen (1->2, 2->1, 3->0)
		"false, TEST_ABDALA, 1, false",
		"false, TEST_ABDALA, 2, false",
		"true, TEST_ABDALA, 3, false",
		"false, TEST_ABDALA, 1, true",
		"true, TEST_ABDALA, 2, true", // 2 Impfungen plus genesen = grundimmunisiert
		"true, TEST_ABDALA, 3, true",

		// Convidecia: gilt nicht als grundimmunisiert (1->2)
		"false, TEST_CONVIDECIA, 1, false",
		"false, TEST_CONVIDECIA, 2, false",
		"false, TEST_CONVIDECIA, 1, true",
		"false, TEST_CONVIDECIA, 2, true",
	})
	@ParameterizedTest
	public void testIsGrundimmunisiert(boolean expectedGrundimmunisiert, TestImpfstoff testImpfstoff, Integer anzahlImpfungen, boolean genesen) {
		ExternesZertifikat externesZertifikat = new ExternesZertifikat();
		Impfstoff impfstoff = TestImpfstoff.createImpfstoffForTest(testImpfstoff);
		Assertions.assertNotNull(impfstoff);
		externesZertifikat.setImpfstoff(impfstoff);
		externesZertifikat.setAnzahlImpfungen(anzahlImpfungen);
		externesZertifikat.setGenesen(genesen);
		Assertions.assertEquals(externesZertifikat.isGrundimmunisiert(KrankheitIdentifier.COVID), expectedGrundimmunisiert);
	}

	@CsvSource({

		// nothing
		"false, TEST_MODERNA, 0, false,    false, TEST_MODERNA, 0, false,        false",  // vorher nicht, nachher nicht
		"false, TEST_MODERNA, 0, false,    true, TEST_MODERNA, 1, false,        false", // vorher nicht, nachher teil
		"false, TEST_MODERNA, 0, false,    true, TEST_MODERNA, 2, false,        true", // vorher nicht, nachher ganz

		// create
		"true, TEST_MODERNA, 1, true,    true, TEST_MODERNA, 2, false,        true", // vorher ganz, nachher ganz

		// remove
		"true, TEST_MODERNA, 1, true,    true, TEST_MODERNA, 1, false,        false", // vorher ganz, nachher teil
		"true, TEST_MODERNA, 1, true,    false, TEST_MODERNA, 1, false,        false", // vorher ganz, nachher nicht

		// update
		"true, TEST_MODERNA, 2, false,    true, TEST_MODERNA, 3, true,        true", // vorher ganz, nachher ganz (anders)

	})
	@ParameterizedTest
	public void testCreateUpdateRemove(
		boolean geimpftAlt, TestImpfstoff testImpfstoffAlt, int anzahlImpfungenAlt, boolean genesenAlt,
		boolean geimpftNeu, TestImpfstoff testImpfstoffNeu, int anzahlImpfungenNeu, boolean genesenNeu, boolean grundimmunisiertNeu) {
		Registrierung registrierung = new Registrierung();
		Impfdossier dossier = TestdataCreationUtil.createDummyImpfdossier(KrankheitIdentifier.COVID, registrierung);
		final ImpfinformationDto infos =
			TestdataCreationUtil.createImpfinformationen(KrankheitIdentifier.COVID, dossier.getRegistrierung(), null, null, dossier, null, null);

		// alt
		this.persistedZertifikat = null;
		if (geimpftAlt) {
			assert testImpfstoffAlt != null;
			Impfstoff impfstoffAlt = TestImpfstoff.createImpfstoffForTest(testImpfstoffAlt);
			Assertions.assertNotNull(impfstoffAlt);
			ExternesZertifikat zertifikatAlt = new ExternesZertifikat();
			zertifikatAlt.setImpfstoff(impfstoffAlt);
			zertifikatAlt.setAnzahlImpfungen(anzahlImpfungenAlt);
			zertifikatAlt.setGenesen(genesenAlt);
			zertifikatAlt.setImpfdossier(dossier);
			this.persistedZertifikat = zertifikatAlt;
		}

		// neu
		ExternGeimpftJax externGeimpftJaxNeu = new ExternGeimpftJax();
		Impfstoff impfstoffNeu = geimpftNeu ? TestImpfstoff.createImpfstoffForTest(testImpfstoffNeu) : null;
		ImpfstoffJax impfstoffNeuJax = impfstoffNeu != null ? ImpfstoffJax.from(impfstoffNeu) : null;
		externGeimpftJaxNeu.setImpfstoff(impfstoffNeuJax);
		externGeimpftJaxNeu.setAnzahlImpfungen(anzahlImpfungenNeu);
		externGeimpftJaxNeu.setGenesen(genesenNeu);
		externGeimpftJaxNeu.setExternGeimpft(geimpftNeu);
		externGeimpftJaxNeu.setLetzteImpfungDate(LocalDate.now().minusMonths(1));
		// assertione vorher
		Assertions.assertEquals(grundimmunisiertNeu, ExternesZertifikat.isGrundimmunisiert(KrankheitIdentifier.COVID, impfstoffNeu, anzahlImpfungenNeu, genesenNeu, null));
		Assertions.assertFalse(ExternesZertifikat.isGrundimmunisiert(KrankheitIdentifier.COVID, impfstoffNeu, anzahlImpfungenNeu, genesenNeu, true));

		// createUpdateRemove
		ExternesZertifikat updatedZertifikat = externesZertifikatService.createUpdateOrRemoveExternGeimpft(
			infos, externGeimpftJaxNeu, false);

		// assertions nachher
		// nein - nein
		if (!geimpftAlt && !geimpftNeu) {
			Assertions.assertNull(updatedZertifikat);
		}
		// nein - ja
		if (!geimpftAlt && geimpftNeu) {
			Assertions.assertNotNull(updatedZertifikat);
			Assertions.assertEquals(updatedZertifikat.getImpfdossier(), dossier);
			Assertions.assertNotNull(impfstoffNeu);
			Assertions.assertEquals(updatedZertifikat.getImpfstoff().getCode(), impfstoffNeu.getCode());
			Assertions.assertEquals(updatedZertifikat.isGenesen(), genesenNeu);
			Assertions.assertEquals(updatedZertifikat.isGrundimmunisiert(dossier.getKrankheitIdentifier()), grundimmunisiertNeu);
		}
		// ja - nein
		if (geimpftAlt && !geimpftNeu) {
			Assertions.assertNull(updatedZertifikat);
		}
		// ja - ja
		if (geimpftAlt && geimpftNeu) {
			Assertions.assertNotNull(updatedZertifikat);
			Assertions.assertEquals(updatedZertifikat.getImpfdossier(), dossier);
			Assertions.assertNotNull(impfstoffNeu);
			Assertions.assertEquals(updatedZertifikat.getImpfstoff().getCode(), impfstoffNeu.getCode());
			Assertions.assertEquals(updatedZertifikat.isGenesen(), genesenNeu);
			Assertions.assertEquals(updatedZertifikat.isGrundimmunisiert(dossier.getKrankheitIdentifier()), grundimmunisiertNeu);
		}

	}

	@CsvSource({

		// nothing
		"false, TEST_MODERNA, 0, false,    false, TEST_MODERNA, 0, false,        false",  // vorher nicht, nachher nicht
		"false, TEST_MODERNA, 0, false,    true, TEST_MODERNA, 1, false,        false", // vorher nicht, nachher teil
		"false, TEST_MODERNA, 0, false,    true, TEST_MODERNA, 2, false,        true", // vorher nicht, nachher ganz

		// create
		"true, TEST_MODERNA, 1, true,    true, TEST_MODERNA, 2, false,        true", // vorher ganz, nachher ganz

		// remove
		"true, TEST_MODERNA, 1, true,    true, TEST_MODERNA, 1, false,        false", // vorher ganz, nachher teil
		"true, TEST_MODERNA, 1, true,    false, TEST_MODERNA, 1, false,        false", // vorher ganz, nachher nicht

		// update
		"true, TEST_MODERNA, 2, false,    true, TEST_MODERNA, 3, true,        true", // vorher ganz, nachher ganz (anders)

	})
	@ParameterizedTest
	public void testSaveImpfling(
		boolean geimpftAlt, TestImpfstoff testImpfstoffAlt, int anzahlImpfungenAlt, boolean genesenAlt,
		boolean geimpftNeu, TestImpfstoff testImpfstoffNeu, int anzahlImpfungenNeu, boolean genesenNeu, boolean grundimmunisiertNeu) {
		Registrierung registrierung = new Registrierung();
		final Impfdossier dossier = TestdataCreationUtil.createDummyImpfdossier(
			KrankheitIdentifier.COVID,
			registrierung);

		// alt
		this.persistedZertifikat = null;
		if (geimpftAlt) {
			assert testImpfstoffAlt != null;
			Impfstoff impfstoffAlt = TestImpfstoff.createImpfstoffForTest(testImpfstoffAlt);
			Assertions.assertNotNull(impfstoffAlt);
			ExternesZertifikat zertifikatAlt = new ExternesZertifikat();
			zertifikatAlt.setImpfstoff(impfstoffAlt);
			zertifikatAlt.setAnzahlImpfungen(anzahlImpfungenAlt);
			zertifikatAlt.setGenesen(genesenAlt);
			zertifikatAlt.setImpfdossier(dossier);
			this.persistedZertifikat = zertifikatAlt;
		}

		// neu
		ExternGeimpftJax externGeimpftJaxNeu = new ExternGeimpftJax();
		Impfstoff impfstoffNeu = geimpftNeu ? TestImpfstoff.createImpfstoffForTest(testImpfstoffNeu) : null;
		ImpfstoffJax impfstoffNeuJax = impfstoffNeu != null ? ImpfstoffJax.from(impfstoffNeu) : null;
		externGeimpftJaxNeu.setImpfstoff(impfstoffNeuJax);
		externGeimpftJaxNeu.setAnzahlImpfungen(anzahlImpfungenNeu);
		externGeimpftJaxNeu.setGenesen(genesenNeu);
		externGeimpftJaxNeu.setExternGeimpft(geimpftNeu);
		externGeimpftJaxNeu.setLetzteImpfungDate(LocalDate.now().minusMonths(1));
		// assertione vorher
		Assertions.assertEquals(grundimmunisiertNeu, ExternesZertifikat.isGrundimmunisiert(KrankheitIdentifier.COVID, impfstoffNeu, anzahlImpfungenNeu, genesenNeu, null));
		Assertions.assertFalse(ExternesZertifikat.isGrundimmunisiert(KrankheitIdentifier.COVID, impfstoffNeu, anzahlImpfungenNeu, genesenNeu, true));

		// createUpdateRemove
		ImpfinformationDto impfinformationDto =
			new ImpfinformationDto(KrankheitIdentifier.COVID, registrierung, null, null,
				dossier, persistedZertifikat);

		externesZertifikatService.saveExternGeimpftImpfling(impfinformationDto, externGeimpftJaxNeu, false);

		// assertions nachher
		// nein - nein
		if (!geimpftAlt && !geimpftNeu) {
			Assertions.assertNull(persistedZertifikat);
		}
		// nein - ja
		if (!geimpftAlt && geimpftNeu) {
			Assertions.assertNotNull(persistedZertifikat);
			Assertions.assertEquals(persistedZertifikat.getImpfdossier(), dossier);
			Assertions.assertNotNull(impfstoffNeu);
			Assertions.assertEquals(persistedZertifikat.getImpfstoff().getCode(), impfstoffNeu.getCode());
			Assertions.assertEquals(persistedZertifikat.isGenesen(), genesenNeu);
			Assertions.assertEquals(persistedZertifikat.isGrundimmunisiert(dossier.getKrankheitIdentifier()), grundimmunisiertNeu);
		}
		// ja - nein
		if (geimpftAlt && !geimpftNeu) {
			Assertions.assertNull(persistedZertifikat);
		}
		// ja - ja
		if (geimpftAlt && geimpftNeu) {
			Assertions.assertNotNull(persistedZertifikat);
			Assertions.assertEquals(persistedZertifikat.getImpfdossier(), dossier);
			Assertions.assertNotNull(impfstoffNeu);
			Assertions.assertEquals(persistedZertifikat.getImpfstoff().getCode(), impfstoffNeu.getCode());
			Assertions.assertEquals(persistedZertifikat.isGenesen(), genesenNeu);
			Assertions.assertEquals(persistedZertifikat.isGrundimmunisiert(dossier.getKrankheitIdentifier()), grundimmunisiertNeu);
		}

		if (geimpftNeu && grundimmunisiertNeu) {
			Assertions.assertNotNull(dossier.getVollstaendigerImpfschutzTyp());
		} else {
			Assertions.assertNull(dossier.getVollstaendigerImpfschutzTyp());
		}

	}

	@CsvSource({

		// Kazachstan: braucht 2 Dosen, aber ist nicht als grundimmunisierung akzeptiert (1->2, 2->1)
		"TEST_KAZAKH, 0, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_KAZAKH, 1, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_KAZAKH, 2, false, BRAUCHT_1_IMPFUNG, false",
		"TEST_KAZAKH, 3, false, BRAUCHT_1_IMPFUNG, false",

		"TEST_KAZAKH, 0, true, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_KAZAKH, 1, true, BRAUCHT_1_IMPFUNG, false",
		"TEST_KAZAKH, 2, true, BRAUCHT_1_IMPFUNG, false",
		"TEST_KAZAKH, 3, true, BRAUCHT_1_IMPFUNG, false",

		// Abdala: braucht 3 Dosen (1->2, 2->1, 3->0)
		"TEST_ABDALA, 0, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_ABDALA, 1, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_ABDALA, 2, false, BRAUCHT_1_IMPFUNG, false",
		"TEST_ABDALA, 3, false, BRAUCHT_0_IMPFUNGEN, true",

		"TEST_ABDALA, 0, true, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_ABDALA, 1, true, BRAUCHT_1_IMPFUNG, false",
		"TEST_ABDALA, 2, true, BRAUCHT_0_IMPFUNGEN, true",
		"TEST_ABDALA, 3, true, BRAUCHT_0_IMPFUNGEN, true",

		// Convidecia: gilt nicht als grundimmunisiert (1->2)
		"TEST_CONVIDECIA, 0, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_CONVIDECIA, 1, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_CONVIDECIA, 2, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_CONVIDECIA, 3, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",

		"TEST_CONVIDECIA, 0, true, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_CONVIDECIA, 1, true, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_CONVIDECIA, 2, true, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_CONVIDECIA, 3, true, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",

		// Moderna: braucht 2
		"TEST_MODERNA, 0, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_MODERNA, 1, false, BRAUCHT_1_IMPFUNG, false",
		"TEST_MODERNA, 2, false, BRAUCHT_0_IMPFUNGEN, true",
		"TEST_MODERNA, 3, false, BRAUCHT_0_IMPFUNGEN, true",

		"TEST_MODERNA, 0, true, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_MODERNA, 1, true, BRAUCHT_0_IMPFUNGEN, true",
		"TEST_MODERNA, 2, true, BRAUCHT_0_IMPFUNGEN, true",
		"TEST_MODERNA, 3, true, BRAUCHT_0_IMPFUNGEN, true",

		// Johnson: braucht 1
		"TEST_JOHNSON, 0, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_JOHNSON, 1, false, BRAUCHT_0_IMPFUNGEN, true",
		"TEST_JOHNSON, 2, false, BRAUCHT_0_IMPFUNGEN, true",
		"TEST_JOHNSON, 3, false, BRAUCHT_0_IMPFUNGEN, true",

		"TEST_JOHNSON, 0, true, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false",
		"TEST_JOHNSON, 1, true, BRAUCHT_0_IMPFUNGEN, true",
		"TEST_JOHNSON, 2, true, BRAUCHT_0_IMPFUNGEN, true",
		"TEST_JOHNSON, 3, true, BRAUCHT_0_IMPFUNGEN, true",

		// Sinopharm: braucht 2 oder 3 (1->1, 2->1, 3->0) (Spezialfall, den wir nicht korrekt abbilden: eigentlich reichen 2, aber man empfiehlt trotzdem eine dritte Impfung)
		"TEST_SINOPH, 0, false, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false", // 0
		"TEST_SINOPH, 1, false, BRAUCHT_1_IMPFUNG, false", // 1 (NEU)
		"TEST_SINOPH, 2, false, BRAUCHT_1_IMPFUNG, false", // 2
		"TEST_SINOPH, 3, false, BRAUCHT_0_IMPFUNGEN, true", // 3

		"TEST_SINOPH, 0, true, BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG, false", // 0 + genesen
		"TEST_SINOPH, 1, true, BRAUCHT_1_IMPFUNG, false", // 1 + genesen
		"TEST_SINOPH, 2, true, BRAUCHT_0_IMPFUNGEN, true", // 2 + genesen
		"TEST_SINOPH, 3, true, BRAUCHT_0_IMPFUNGEN, true", // 3 + genesen
	})
	@ParameterizedTest
	public void testImpfempfehlungGrundimmunisierung(
		TestImpfstoff impfstoffName, int anzahlImpfungenGemacht, boolean genesen,
		MissingForGrundimmunisiert expectedMissingForGrundimmunisiert, boolean expectedGrundimmunisiert) {
		String testcase = impfstoffName + ", " + anzahlImpfungenGemacht + ", " + genesen + ", " +expectedMissingForGrundimmunisiert + ", " +expectedGrundimmunisiert;

			Impfstoff impfstoff = TestImpfstoff.createImpfstoffForTest(impfstoffName);
		Assertions.assertEquals(
			expectedMissingForGrundimmunisiert,
			ExternesZertifikat.calculateAnzahlMissingImpfungen(KrankheitIdentifier.COVID, impfstoff, anzahlImpfungenGemacht, genesen, null),
			testcase);
		Assertions.assertEquals(
			MissingForGrundimmunisiert.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG,
			ExternesZertifikat.calculateAnzahlMissingImpfungen(KrankheitIdentifier.COVID, impfstoff, anzahlImpfungenGemacht, genesen, true),
			testcase);

		Assertions.assertEquals(
			expectedGrundimmunisiert,
			ExternesZertifikat.isGrundimmunisiert(KrankheitIdentifier.COVID, impfstoff, anzahlImpfungenGemacht, genesen, null),
			testcase);
		Assertions.assertFalse(ExternesZertifikat.isGrundimmunisiert(KrankheitIdentifier.COVID, impfstoff, anzahlImpfungenGemacht, genesen, true),
			testcase);
	}
}
