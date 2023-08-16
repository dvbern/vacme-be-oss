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
import java.util.List;

import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationDtoRecreator;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class BoosterPrioUtilTest {

	@Nullable
	private ExternesZertifikat externeImpfinfo;
	private ImpfinformationDto impfinformationDto;
	private static final LocalDate NEWEST_VACME_IMPFUNG = LocalDate.of(2021, 1, 30);
	private static final LocalDate LATEST_EXT_IMPFDATUM = LocalDate.of(2021, 6, 1);

	/**
	 * Hilfsmethode welche Testsdaten aufsetzt
	 */
	public void initTestregistrierung(boolean withVacmeImpfungen, boolean withExternImpfungen) {

		LocalDate latestVacmeImpfung = withVacmeImpfungen ? NEWEST_VACME_IMPFUNG : null;
		LocalDate latestExtImpfung = withExternImpfungen ? LATEST_EXT_IMPFDATUM : null;
		impfinformationDto = TestdataCreationUtil.createImpfinformationen(
			KrankheitIdentifier.COVID,
			latestVacmeImpfung,
			latestExtImpfung);
		externeImpfinfo = impfinformationDto.getExternesZertifikat();

	}

	@Test
	void testGetNewestImpfdatum() {

		// noch gar keine Impfung vorhanden
		initTestregistrierung(false, false);
		LocalDate dateOfNewestImpfung = BoosterPrioUtil.getDateOfNewestImpfung(impfinformationDto);
		assertNull(dateOfNewestImpfung);

		// nur externe Impfung vorhanden
		initTestregistrierung(false, true);
		dateOfNewestImpfung = BoosterPrioUtil.getDateOfNewestImpfung(impfinformationDto);
		Assertions.assertNotNull(dateOfNewestImpfung);
		Assertions.assertEquals(LATEST_EXT_IMPFDATUM, dateOfNewestImpfung);

		// externe und interne Impfung vorhanden aber externe ist neuer
		initTestregistrierung(true, true);
		dateOfNewestImpfung = BoosterPrioUtil.getDateOfNewestImpfung(impfinformationDto);
		Assertions.assertEquals(LATEST_EXT_IMPFDATUM, dateOfNewestImpfung);

		// nur interne Impfung vorhanden
		initTestregistrierung(true, false);
		dateOfNewestImpfung = BoosterPrioUtil.getDateOfNewestImpfung(impfinformationDto);
		Assertions.assertEquals(NEWEST_VACME_IMPFUNG, dateOfNewestImpfung);

		// heute intern geimpft
		initTestregistrierung(true, true);
		Impfung impfungToday = new Impfung();
		impfungToday.setImpfstoff(TestdataCreationUtil.createImpfstoffModerna());
		impfungToday.setTimestampImpfung(LocalDateTime.now());
		impfinformationDto = ImpfinformationDtoRecreator.from(impfinformationDto)
			.withBoosterImpfungen(List.of(impfungToday)).build();

		dateOfNewestImpfung = BoosterPrioUtil.getDateOfNewestImpfung(impfinformationDto);
		Assertions.assertEquals(LocalDate.now(), dateOfNewestImpfung);

		// gestern extern geimpft
		initTestregistrierung(true, true);
		assert externeImpfinfo != null;
		externeImpfinfo.setLetzteImpfungDate(LocalDate.now().minusDays(1));
		externeImpfinfo.setAnzahlImpfungen(3);
		dateOfNewestImpfung = BoosterPrioUtil.getDateOfNewestImpfung(impfinformationDto);
		Assertions.assertEquals(LocalDate.now().minusDays(1), dateOfNewestImpfung);
	}

	@Test
	void getImpfinfosOrderdByTimestampImpfungUsesExtZertifikatWithoutDateFirstTest() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		ImpfinformationDto infos = builder
			.create(KrankheitIdentifier.AFFENPOCKEN)
			.withUnbekannterAffenpockenimpfungInKindheit(null, 1)
			.getInfos();

		List<ImpfInfo> impfInfoList = BoosterPrioUtil.getImpfinfosOrderedByTimestampImpfung(infos);
		Assertions.assertFalse(impfInfoList.isEmpty());
		Assertions.assertEquals(1,impfInfoList.size());
		Assertions.assertNull(impfInfoList.get(0).getTimestampImpfung());

		ImpfinformationDto infos2 = builder
			.create(KrankheitIdentifier.AFFENPOCKEN)
			.withUnbekannterAffenpockenimpfungInKindheit(LocalDate.of(2021, 1, 1), 1)
			.getInfos();

		List<ImpfInfo> impfInfoList2 = BoosterPrioUtil.getImpfinfosOrderedByTimestampImpfung(infos2);
		Assertions.assertFalse(impfInfoList2.isEmpty());
		Assertions.assertEquals(1,impfInfoList2.size());
		Assertions.assertEquals(LocalDate.of(2021,1,1).atTime(23,59,59), impfInfoList2.get(0).getTimestampImpfung());
	}

	@Test
	void getImpfinfosOrderdByTimestampImpfungReturnsExtZertifikatWithoutDateFirstTest() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		ImpfinformationDto infos = builder
			.create(KrankheitIdentifier.AFFENPOCKEN)
			.withUnbekannterAffenpockenimpfungInKindheit(null, 1)
			.withBooster(LocalDate.of(2020,1,1), TestdataCreationUtil.createImpfstoffAffenpocken())
			.getInfos();

		List<ImpfInfo> impfinfosOrderedByTimestampImpfung = BoosterPrioUtil.getImpfinfosOrderedByTimestampImpfung(infos);
		Assertions.assertFalse(impfinfosOrderedByTimestampImpfung.isEmpty());
		Assertions.assertEquals(2,impfinfosOrderedByTimestampImpfung.size());
		Assertions.assertNull(impfinfosOrderedByTimestampImpfung.get(0).getTimestampImpfung());
		Assertions.assertEquals(LocalDate.of(2020,1,1).atStartOfDay(), impfinfosOrderedByTimestampImpfung.get(1).getTimestampImpfung());
	}

	@Test
	void getImpfinfosOrderdByTimestampImpfungReturnsBoosterImpfungenInOrderTest() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		ImpfinformationDto infos = builder
			.create(KrankheitIdentifier.AFFENPOCKEN)
			.withBooster(LocalDate.of(2020,1,1), TestdataCreationUtil.createImpfstoffAffenpocken())
			.withBooster(LocalDate.of(2022,1,1), TestdataCreationUtil.createImpfstoffAffenpocken())
			.getInfos();

		List<ImpfInfo> impfinfosOrderedByTimestampImpfung = BoosterPrioUtil.getImpfinfosOrderedByTimestampImpfung(infos);
		Assertions.assertFalse(impfinfosOrderedByTimestampImpfung.isEmpty());
		Assertions.assertEquals(2,impfinfosOrderedByTimestampImpfung.size());
		Assertions.assertEquals(LocalDate.of(2020,1,1).atStartOfDay(), impfinfosOrderedByTimestampImpfung.get(0).getTimestampImpfung());
		Assertions.assertEquals(LocalDate.of(2022,1,1).atStartOfDay(), impfinfosOrderedByTimestampImpfung.get(1).getTimestampImpfung());
	}

	@Test
	void getAnzahlBoosterImpfungenTest() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		ImpfinformationDto infos = builder
			.create(KrankheitIdentifier.COVID)
			.withImpfung1(LocalDate.of(2019,1,1), TestdataCreationUtil.createImpfstoffAffenpocken())
			.withImpfung2(LocalDate.of(2020,2,2), TestdataCreationUtil.createImpfstoffAffenpocken())
			.withBooster(LocalDate.of(2021,3,3), TestdataCreationUtil.createImpfstoffAffenpocken(), true)
			.withBooster(LocalDate.of(2022,1,1), TestdataCreationUtil.createImpfstoffAffenpocken())
			.withBooster(LocalDate.of(2022,10,1), TestdataCreationUtil.createImpfstoffAffenpocken())
			.getInfos();

		List<ImpfInfo> impfinfosOrderedByTimestampImpfung = BoosterPrioUtil.getImpfinfosOrderedByTimestampImpfung(infos);
		int count = BoosterPrioUtil.countNumberOfBoosterImpfungen(impfinfosOrderedByTimestampImpfung);
		Assertions.assertEquals(2,  count);
		Assertions.assertFalse(impfinfosOrderedByTimestampImpfung.isEmpty());
		Assertions.assertEquals(5,impfinfosOrderedByTimestampImpfung.size());
		Assertions.assertEquals(LocalDate.of(2019,1,1).atStartOfDay(), impfinfosOrderedByTimestampImpfung.get(0).getTimestampImpfung());
		Assertions.assertEquals(LocalDate.of(2022,10,1).atStartOfDay(), impfinfosOrderedByTimestampImpfung.get(4).getTimestampImpfung());
	}

	@Test
	void getAnzahlBoosterImpfungenAffenpockenTest() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		ImpfinformationDto infos = builder
			.create(KrankheitIdentifier.AFFENPOCKEN)
			.withBooster(LocalDate.of(2021,3,3), TestdataCreationUtil.createImpfstoffAffenpocken(), true)
			.withBooster(LocalDate.of(2022,1,1), TestdataCreationUtil.createImpfstoffAffenpocken())
			.withBooster(LocalDate.of(2022,10,1), TestdataCreationUtil.createImpfstoffAffenpocken())
			.getInfos();

		List<ImpfInfo> impfinfosOrderedByTimestampImpfung = BoosterPrioUtil.getImpfinfosOrderedByTimestampImpfung(infos);
		int count = BoosterPrioUtil.countNumberOfBoosterImpfungen(impfinfosOrderedByTimestampImpfung);
		Assertions.assertEquals(2,  count);
		Assertions.assertFalse(impfinfosOrderedByTimestampImpfung.isEmpty());
		Assertions.assertEquals(3,impfinfosOrderedByTimestampImpfung.size(), "Impfung 1 and 2 are not passed in infos for affenpocken");
		Assertions.assertEquals(LocalDate.of(2021,3,3).atStartOfDay(), impfinfosOrderedByTimestampImpfung.get(0).getTimestampImpfung());
		Assertions.assertEquals(LocalDate.of(2022,10,1).atStartOfDay(), impfinfosOrderedByTimestampImpfung.get(2).getTimestampImpfung());
	}
}
