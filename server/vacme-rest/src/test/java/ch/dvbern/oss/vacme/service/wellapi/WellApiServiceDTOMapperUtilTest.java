/*
 *
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.oss.vacme.service.wellapi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeAppointmentRequestDto;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeApprovalPeriodRequestDto;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier.FSME;

class WellApiServiceDTOMapperUtilTest {

	@BeforeEach
	void setUp() {
		System.setProperty("vacme.mandant", Mandant.BE.name());
	}

	@Test
	void mapImpfterminToWellAppointment() {

		// setup
		LocalDate extzertDate = LocalDate.of(2022, 1, 1);
		LocalDate impfungDate = LocalDate.of(2022, 4, 1);
		ImpfinformationBuilder builder = new ImpfinformationBuilder();

		builder.create(FSME)
			.withAge(50)
			.withSchnellschema(true)
			.withExternesZertifikatOhneTest(
			TestdataCreationUtil.createImpfstoffEncepur(),
			2,
			extzertDate
		);

		builder.withBooster(impfungDate, TestdataCreationUtil.createImpfstoffEncepur());
		Objects.requireNonNull(builder.getInfos().getBoosterImpfungen()).get(0).getTermin().getImpfslot().setKrankheitIdentifier(FSME);

		// test
		UUID wellUserId = UUID.randomUUID();
		VacMeAppointmentRequestDto mappedObject =
			WellApiServiceDTOMapperUtil.mapImpfterminToWellAppointment(
				wellUserId,
				3,
				builder.getInfos().getBoosterImpfungen().get(0).getTermin()
			);

		Impftermin impftermin = builder.getInfos().getBoosterImpfungen().get(0).getTermin();

		// verify
		Assertions.assertEquals(3, mappedObject.getDoseNumber());
		Assertions.assertEquals(impftermin.getId().toString(), mappedObject.getVacmeAppointmentId());
		Assertions.assertEquals(
			DateUtil.getDate(impftermin.getImpfslot().getZeitfenster().getVon()),
			mappedObject.getAppointmentStart());
		Assertions.assertEquals(
			DateUtil.getDate(impftermin.getImpfslot().getZeitfenster().getBis()),
			mappedObject.getAppointmentEnd());
		Assertions.assertEquals(FSME.name(), mappedObject.getDiseaseName());
		Assertions.assertEquals(
			impftermin.getImpfslot().getOrtDerImpfung().getName(),
			mappedObject.getAddress().getName());
		Assertions.assertEquals(
			impftermin.getImpfslot().getOrtDerImpfung().getAdresse().getAdresse1(),
			mappedObject.getAddress().getStreet());
		Assertions.assertEquals(
			impftermin.getImpfslot().getOrtDerImpfung().getAdresse().getPlz(),
			mappedObject.getAddress().getZipCode());
		Assertions.assertEquals(
			impftermin.getImpfslot().getOrtDerImpfung().getAdresse().getOrt(),
			mappedObject.getAddress().getCity());

		Assertions.assertEquals(wellUserId, mappedObject.getUserId());

	}

	@Test
	void mapImpfschutzToApprovalPeriod() {

		// test
		LocalDateTime freigabeDateForTest = LocalDate.of(2021, 2, 2).atStartOfDay();
		Impfschutz impfschutzToMap = TestdataCreationUtil.createImpfschutz();
		impfschutzToMap.setFreigegebenNaechsteImpfungAb(freigabeDateForTest);

		UUID wellUserId = UUID.randomUUID();
		VacMeApprovalPeriodRequestDto mappedImpfschutz =
			WellApiServiceDTOMapperUtil.mapImpfschutzToApprovalPeriod(
				wellUserId,
				KrankheitIdentifier.FSME,
				4,
				impfschutzToMap

			);

		// verify
		Assertions.assertEquals(impfschutzToMap.getId().toString(), mappedImpfschutz.getVacmeApprovalPeriodId());
		Assertions.assertEquals(FSME.name(), mappedImpfschutz.getDiseaseName());
		Assertions.assertEquals(4, mappedImpfschutz.getDoseNumber());
		Assertions.assertEquals(DateUtil.getDate(freigabeDateForTest), mappedImpfschutz.getApprovalPeriodDate());
		Assertions.assertEquals(wellUserId, mappedImpfschutz.getUserId());

	}

}
