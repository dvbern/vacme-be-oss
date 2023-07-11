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

import java.util.List;
import java.util.UUID;

import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.MassenverarbeitungQueueRepo;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.service.massenmutation.MassenverarbeitungService;
import ch.dvbern.oss.vacme.service.vmdl.VMDLServiceFactory;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MassenverarbeitungServiceTest {

	MassenverarbeitungService serviceUnderTest;

	@BeforeEach
	void setUp() {
		serviceUnderTest = new MassenverarbeitungService(
			Mockito.mock(MassenverarbeitungQueueRepo.class),
			Mockito.mock(VMDLServiceFactory.class),
			Mockito.mock(ImpfungRepo.class),
			Mockito.mock(KorrekturService.class),
			Mockito.mock(OrtDerImpfungRepo.class),
			Mockito.mock(RegistrierungRepo.class),
			Mockito.mock(RegistrierungService.class),
			Mockito.mock(FragebogenService.class),
			Mockito.mock(GeocodeService.class),
			Mockito.mock(ImpfdossierService.class),
			Mockito.mock(ImpfdossierRepo.class)
		);
	}

	@Test
	void testParseImpfungenEmpty() {
		List<UUID> uuids = serviceUnderTest.parseAsImpfungenUUIDs("");
		Assertions.assertNotNull(uuids);
		Assertions.assertTrue(uuids.isEmpty());
	}

	@Test
	void testParseImpfungenRandomIds() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			builder.append(UUID.randomUUID());
			builder.append("\n");
		}
		List<UUID> uuids = serviceUnderTest.parseAsImpfungenUUIDs(builder.toString());
		Assertions.assertNotNull(uuids);
		assertEquals(100, uuids.size());
	}

	@Test
	void testParseImpfungenInvalid() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			builder.append(i).append("gugus");
			builder.append("\n");
		}
		try {
			List<UUID> uuids = serviceUnderTest.parseAsImpfungenUUIDs(builder.toString());
			Assertions.fail("Should throw because invalid UUID");
		} catch (AppValidationException e) {
			// nothing
		}
	}

	@Test
	void testParseRegistrierungsnummern() {
		VacmeSettingsService vacmeSettingsService = Mockito.mock(VacmeSettingsService.class);
		Mockito.when(vacmeSettingsService.getHashidAlphabet()).thenReturn("123456789ABCDEFGHIKLMNPQRSTUVWXYZ");
		Mockito.when(vacmeSettingsService.getHashidSalt()).thenReturn("testingSalt");
		Mockito.when(vacmeSettingsService.getHashidMinLength()).thenReturn(6);

		StringBuilder builder = new StringBuilder();
		HashIdService hashIdService = new HashIdService(vacmeSettingsService);

		for (long i = 1; i <= 1000; i++) {
			builder.append(hashIdService.getHashFromNumber(i));
			builder.append("\n");
		}
		List<String> regnums = serviceUnderTest.parseAsRegistrierungsnummern(builder.toString());
		assertEquals(1000, regnums.size());

		try {
			serviceUnderTest.parseAsRegistrierungsnummern("ABCDE%+");
			Assertions.fail("Parser should throw because Registrierungsnummern must be alphanumeric");
		}catch (AppValidationException ex){
			Assertions.assertTrue(ex.getMessage().contains("alphanumeric"));
		}

		try {
			serviceUnderTest.parseAsRegistrierungsnummern("ABCDEF,ANOTHER");
			Assertions.fail("Parser should throw because we expect only one column");
		}catch (AppValidationException ex){
			Assertions.assertTrue(ex.getMessage().contains("one column"));
		}
	}

	@Test
	void testParseImpfungenToIdRandomIds() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			builder.append(UUID.randomUUID());
			builder.append(',');
			builder.append(UUID.randomUUID());
			builder.append("\n");
		}
		List<Pair<UUID, UUID>> uuids = serviceUnderTest.parseAsImpfungenAndOdiUUIDs(builder.toString());
		Assertions.assertNotNull(uuids);
		assertEquals(100, uuids.size());
	}

	@Test
	void testParseImpfungenToIdRandomIdsInvalid() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			builder.append("gugus");
			builder.append(',');
			builder.append(UUID.randomUUID());
			builder.append("\n");
		}
		try {
			List<Pair<UUID, UUID>> uuids = serviceUnderTest.parseAsImpfungenAndOdiUUIDs(builder.toString());
		} catch (AppValidationException e) {
			// nothing
		}
	}
}
