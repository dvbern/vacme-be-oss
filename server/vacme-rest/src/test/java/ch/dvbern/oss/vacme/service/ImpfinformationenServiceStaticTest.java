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
import java.util.stream.Stream;

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;

public class ImpfinformationenServiceStaticTest {

	private ImpfinformationenService impfinformationenService = new ImpfinformationenService(
		Mockito.mock(ImpfungRepo.class),
		Mockito.mock(ImpfdossierRepo.class)
	);

	@ParameterizedTest
	@MethodSource("provideNumberOfBoosters")
	void testGetImpfungenCount(
		KrankheitIdentifier krankheitIdentifier,
		int numberOfBoosters,
		int numberOfExtImpfungen,
		int expectedNumberOfImpfungen,
		boolean expectedToHaveVacmeImpfung
	) {

		Impfstoff impfstoff = TestdataCreationUtil.createImpfstoffModerna();
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder = builder
			.create(krankheitIdentifier)
			.withExternesZertifikat(
				impfstoff,
				numberOfExtImpfungen,
				LocalDate.of(2020, 1, 1),
				LocalDate.of(2020, 1, 2))
			.withName("Muster", "Hans")
			.withBirthday(LocalDate.of(1984, 12, 12));
		if (krankheitIdentifier.isSupportsImpffolgenEinsUndZwei()) {
			builder = builder
				.withImpfung1(LocalDate.of(2021, 1, 9), impfstoff)
				.withImpfung2(LocalDate.of(2021, 2, 6), impfstoff);
		}
		Impfstoff impfstoffBooster = TestdataCreationUtil.createImpfstoffPfizer();
		for (int i = 0; i < numberOfBoosters; i++) {
			builder = builder
				.withBooster(LocalDate.of(2021, 9, 6).plusYears(i + 1), impfstoffBooster);
		}
		final ImpfinformationDto infos = builder.getInfos();
		int numberOfImpfung = ImpfinformationenService.getNumberOfImpfung(infos);
		Assertions.assertEquals(expectedNumberOfImpfungen, numberOfImpfung);
		if (numberOfImpfung != 0) {
			Assertions.assertEquals(expectedToHaveVacmeImpfung, impfinformationenService.hasVacmeImpfungen(infos));
		}

	}

	private static Stream<Arguments> provideNumberOfBoosters() {
		return Stream.of(
			Arguments.of(KrankheitIdentifier.COVID, 0, 0, 2, true),
			Arguments.of(KrankheitIdentifier.COVID, 1, 0, 3, true),
			Arguments.of(KrankheitIdentifier.COVID, 2, 0, 4, true),
			Arguments.of(KrankheitIdentifier.COVID, 3, 0, 5, true),
			Arguments.of(KrankheitIdentifier.AFFENPOCKEN, 0, 0, 0, true),
			Arguments.of(KrankheitIdentifier.AFFENPOCKEN, 1, 0, 1, true),
			Arguments.of(KrankheitIdentifier.AFFENPOCKEN, 2, 0, 2, true),
			Arguments.of(KrankheitIdentifier.AFFENPOCKEN, 3, 0, 3, true),

			Arguments.of(KrankheitIdentifier.COVID, 0, 2, 4, true),
			Arguments.of(KrankheitIdentifier.COVID, 1, 2, 5, true),
			Arguments.of(KrankheitIdentifier.COVID, 2, 2, 6, true),
			Arguments.of(KrankheitIdentifier.COVID, 3, 2, 7, true),
			Arguments.of(KrankheitIdentifier.AFFENPOCKEN, 0, 3, 3, false),
			Arguments.of(KrankheitIdentifier.AFFENPOCKEN, 1, 3, 4, true),
			Arguments.of(KrankheitIdentifier.AFFENPOCKEN, 2, 3, 5, true),
			Arguments.of(KrankheitIdentifier.AFFENPOCKEN, 3, 3, 6, true)
		);
	}

	@Test
	void testSettingAImpfung1StatusShouldFailForAffenpockenAndNotFailForCovid() {

		Impfstoff impfstoff = TestdataCreationUtil.createImpfstoffModerna();
		ImpfinformationBuilder builderAffenpocken = new ImpfinformationBuilder();
		builderAffenpocken = builderAffenpocken
			.create(KrankheitIdentifier.AFFENPOCKEN)
			.withName("Muster", "Hans")
			.withBirthday(LocalDate.of(1984, 12, 12));
		ImpfinformationDto infos = builderAffenpocken.getInfos();

		Impfung impfung1 =
			TestdataCreationUtil.createImpfungWithImpftermin(
				LocalDate.of(2021, 1, 1).atStartOfDay(),
				Impffolge.ERSTE_IMPFUNG);
		impfung1.setImpfstoff(impfstoff);

		//setNextStatus could maybe be static
		ImpfdokumentationService impfdokumentationService =
			new ImpfdokumentationService(null, null, null, null, null, null, null, null);
		try {
			impfdokumentationService.setNextStatus(infos, Impffolge.ERSTE_IMPFUNG, impfung1, false, false);
			Assertions.fail("Setting an Impfung1 status should fail for Affenpocken");
		} catch (Exception e) {
			Assertions.assertNotNull(e);
			Assertions.assertTrue(e.getMessage().contains("Status IMPFUNG_1_DURCHGEFUEHRT ist fuer  AFFENPOCKEN nicht erlaubt da diese Krankheit keine Impffolgen 1/2 unterstuetzt"));
		}

		ImpfinformationBuilder builderCovid = new ImpfinformationBuilder();
		builderCovid = builderCovid
			.create(KrankheitIdentifier.COVID)
			.withName("Muster", "Hans")
			.withBirthday(LocalDate.of(1984, 12, 12));
		infos = builderCovid.getInfos();
		impfdokumentationService.setNextStatus(infos, Impffolge.ERSTE_IMPFUNG, impfung1, false, false);
		Assertions.assertEquals(IMPFUNG_1_DURCHGEFUEHRT,infos.getImpfdossier().getDossierStatus());

	}
}
