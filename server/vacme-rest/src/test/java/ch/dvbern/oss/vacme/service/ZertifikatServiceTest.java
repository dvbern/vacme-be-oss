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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.jax.registration.ZertifikatJax;
import ch.dvbern.oss.vacme.repo.ApplicationPropertyRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.repo.ZertifikatRepo;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertApiService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.ObjectMapperTestUtil;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ZertifikatServiceTest {

	private ZertifikatService serviceUnderTest;
	private String testpayload = "{\n"
		+ "\t\"name\": {\n"
		+ "\t\t\"familyName\": \"Genevieve\",\n"
		+ "\t\t\"givenName\": \"Duffy\"\n"
		+ "\t},\n"
		+ "\t\"dateOfBirth\": \"1924-04-15\",\n"
		+ "\t\"language\": \"de\",\n"
		+ "\t\"vaccinationInfo\": [\n"
		+ "\t\t{\n"
		+ "\t\t\t\"medicinalProductCode\": \"EU/1/20/1507\",\n"
		+ "\t\t\t\"numberOfDoses\": 2,\n"
		+ "\t\t\t\"totalNumberOfDoses\": 2,\n"
		+ "\t\t\t\"vaccinationDate\": \"2021-01-02\",\n"
		+ "\t\t\t\"countryOfVaccination\": \"CH\"\n"
		+ "\t\t}\n"
		+ "\t],\n"
		+ "\t\"address\": null\n"
		+ "}";

	private ImpfstoffService impfstoffServiceMock;

	@BeforeEach
	void setUp() {
		ZertifikatRepo zertRepo = Mockito.mock(ZertifikatRepo.class);
		CovidCertApiService covidCertApiService = Mockito.mock(CovidCertApiService.class);
		ApplicationPropertyRepo applicationPropertyRepo = Mockito.mock(ApplicationPropertyRepo.class);
		RegistrierungRepo regRepo = Mockito.mock(RegistrierungRepo.class);

		ImpfinformationenService impfinfoService = Mockito.mock(ImpfinformationenService.class);
		ObjectMapper mapper = ObjectMapperTestUtil.createObjectMapperForTest();
		impfstoffServiceMock = Mockito.mock(ImpfstoffService.class);
		VacmeSettingsService vacmeSettingsService = Mockito.mock(VacmeSettingsService.class);

		serviceUnderTest = new ZertifikatService(
			zertRepo,
			covidCertApiService, applicationPropertyRepo, regRepo, impfinfoService, mapper, impfstoffServiceMock, vacmeSettingsService);
	}

	@Test
	void zertifikatComparatorTest() {

		List<ZertifikatJax> listToOrder = new ArrayList<>();

		// 1 Dose
		ZertifikatJax z1 = createZertjaxAndAddToList(
			listToOrder,
			1,
			LocalDate.of(2020, 1, 1),
			LocalDateTime.of(2020, 1, 1, 0, 0)
		);
		ZertifikatJax z2 = createZertjaxAndAddToList(
			listToOrder,
			1,
			LocalDate.of(2020, 4, 1),
			LocalDateTime.of(2020, 4, 1, 0, 0)
		);
		ZertifikatJax z3 = createZertjaxAndAddToList(
			listToOrder,
			1,
			LocalDate.of(2020, 2, 1),
			LocalDateTime.of(2020, 2, 1, 10, 0)
		);
		ZertifikatJax z4 = createZertjaxAndAddToList(
			listToOrder,
			1,
			LocalDate.of(2020, 2, 1),
			LocalDateTime.of(2020, 2, 1, 13, 0)
		);

		//2 Dosen
		ZertifikatJax z5 = createZertjaxAndAddToList(
			listToOrder,
			2,
			LocalDate.of(2020, 2, 1),
			LocalDateTime.of(2020, 2, 1, 0, 0)
		);

		// 3 Dosen
		ZertifikatJax z6 = createZertjaxAndAddToList(
			listToOrder,
			3,
			LocalDate.of(2020, 8, 1),
			LocalDateTime.of(2020, 8, 1, 0, 0)
		);
		ZertifikatJax z7 = createZertjaxAndAddToList(
			listToOrder,
			3,
			LocalDate.of(2020, 2, 1),
			LocalDateTime.of(2020, 2, 1, 0, 0)
		);

		listToOrder.sort(serviceUnderTest.comparatorSortingByHighestDoseNr()
		);

		Assertions.assertEquals(z6, listToOrder.get(0));
		Assertions.assertEquals(z7, listToOrder.get(1));
		Assertions.assertEquals(z5, listToOrder.get(2));
		Assertions.assertEquals(z2, listToOrder.get(3));
		Assertions.assertEquals(z4, listToOrder.get(4));
		Assertions.assertEquals(z3, listToOrder.get(5));
		Assertions.assertEquals(z1, listToOrder.get(6));

	}

	@Test
	void zertifikat_should_show_correct_impfstoff_if_zertifkat_has_impfung() {

		Impfung impfung = new Impfung();
		impfung.setImpfstoff(TestdataCreationUtil.createImpfstoffModerna());
		Zertifikat zert = new Zertifikat();
		zert.setRevoked(false);
		zert.setImpfung(impfung);

		zert.setPayload(testpayload);

		List<Zertifikat> zertList = List.of(zert);
		List<ZertifikatJax> zertifikatJaxes = serviceUnderTest.mapToZertifikatJax(zertList);
		Assertions.assertEquals(1, zertifikatJaxes.size());
		ZertifikatJax zertifikatJax = zertifikatJaxes.get(0);
		Assertions.assertNotNull(zertifikatJax);
		Assertions.assertEquals("EU/1/20/1507", zertifikatJax.getImpfstoffJax().getCovidCertProdCode());
		Assertions.assertEquals("Moderna - Spikevax", zertifikatJax.getImpfstoffJax().getDisplayName(),
			"If Impfung is present we should always match the actual Ipmfstoff even if covidCertProdCode "
				+ "is same for multiple Impfstoffe");

	}

	@Test
	void zertifikat_should_fallback_to_reading_impfstoff_by_covid_cert_prodcode_if_impfung_not_set_for_zert() {

		Zertifikat zert = new Zertifikat();
		zert.setRevoked(false);
		zert.setPayload(testpayload);

		// since Moderna Monavalent and Moderna Bivalent have the same covidCertProdCode either could be returned from db
		Mockito.when(impfstoffServiceMock.findByCovidCertProdCode("EU/1/20/1507"))
			.thenReturn(TestdataCreationUtil.createImpfstoffModernaBivalent());

		List<Zertifikat> zertList = List.of(zert);
		List<ZertifikatJax> zertifikatJaxes = serviceUnderTest.mapToZertifikatJax(zertList);
		Assertions.assertEquals(1, zertifikatJaxes.size());
		ZertifikatJax zertifikatJax = zertifikatJaxes.get(0);
		Assertions.assertNotNull(zertifikatJax);
		Assertions.assertEquals("EU/1/20/1507", zertifikatJax.getImpfstoffJax().getCovidCertProdCode());
		Assertions.assertEquals("Moderna - Spikevax® Bivalent Original / Omicron", zertifikatJax.getImpfstoffJax().getDisplayName(),
			"If Impfung is not present we could potentially match the wrong Impfstoff. This should only happen as a fallback");

	}

	private ZertifikatJax createZertjaxAndAddToList(List<ZertifikatJax> list, int numDoses, LocalDate vaccDate,
		LocalDateTime creationTime) {
		Zertifikat zert = new Zertifikat();
		zert.setRevoked(false);

		zert.setImpfung(new Impfung());
		ZertifikatJax zertJax = new ZertifikatJax(zert);
		zertJax.setTimestampZertifikatRevoked(null);
		zertJax.setNumberOfDoses(numDoses);
		zertJax.setVaccinationDate(vaccDate);
		zertJax.setTimestampZertifikatErstellt(creationTime);
		list.add(zertJax);
		return zertJax;

	}
}
