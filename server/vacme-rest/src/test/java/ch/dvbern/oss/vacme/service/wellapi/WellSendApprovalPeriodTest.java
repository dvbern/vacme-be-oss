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
import java.util.Arrays;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.rest_client.well.api.WellRestClientService;
import ch.dvbern.oss.vacme.service.BenutzerService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;

class WellSendApprovalPeriodTest {

	WellApiService serviceUnderTest;
	private WellRestClientService restApiMock;
	private BenutzerService benutzerServiceMock;

	@BeforeEach
	void setUp() {
		serviceUnderTest = new WellApiService();

		restApiMock = mock(WellRestClientService.class);
		serviceUnderTest.wellRestClientService = restApiMock;
		benutzerServiceMock = mock(BenutzerService.class);
		serviceUnderTest.benutzerService = benutzerServiceMock;

	}

	@Test
	void given_not_well_enabled_krankheit_send_ApprovalPeriod_should_not_call_rest_api() {
		Arrays.stream(KrankheitIdentifier.values()).filter(k -> !k.isWellEnabled()).forEach(kra -> {
			serviceUnderTest.sendApprovalPeriod(UUID.randomUUID(), kra, 3, new Impfschutz());
			Mockito.verify(restApiMock, timeout(500).times(0)).upsertApprovalPeriod(any(), any());
		});

	}

	@Test
	void given_well_enabled_when_no_impfung_yet_sendApprovalPeriod_should_not_call_rest_api() {

		Arrays.stream(KrankheitIdentifier.values()).forEach(kra -> {
			serviceUnderTest.sendApprovalPeriod(UUID.randomUUID(), kra, 1, new Impfschutz());
			Mockito.verify(restApiMock, timeout(500).times(0)).upsertApprovalPeriod(any(), any());
		});

	}

	@Test
	void given_impfschutz_for_fsmewith_no_freigabe_then_deleteApprovalPeriod_should_be_sent() {

		Impfschutz impfschutz = TestdataCreationUtil.createImpfschutz();
		impfschutz.setFreigegebenNaechsteImpfungAb(null);

		serviceUnderTest.sendApprovalPeriod(UUID.randomUUID(), KrankheitIdentifier.FSME, 2, impfschutz);
		Mockito.verify(restApiMock, times(1)).deleteApprovalPeriod(eq(impfschutz.getId().toString()));
		Mockito.verify(restApiMock, timeout(500).times(0)).upsertApprovalPeriod(any(), any());
	}

	@Test
	void given_fsme_and_impfung_exists_then_upsertApprovalPeriod_should_be_sent() {

		Impfschutz impfschutz = TestdataCreationUtil.createImpfschutz();

		serviceUnderTest.sendApprovalPeriod(UUID.randomUUID(), KrankheitIdentifier.FSME, 2, impfschutz);
		// since the upsert is called async we may need to wait a bit, hence the timeout
		Mockito.verify(restApiMock, timeout(500).times(1)).upsertApprovalPeriod(
			any(), eq(impfschutz.getId().toString()));
	}

	@Test
	void given_non_online_user_send_ApprovalPeriod_should_not_call_rest_api() {
		Impfschutz impfschutz = TestdataCreationUtil.createImpfschutz();

		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder
			.create(KrankheitIdentifier.FSME)
			.withBooster(LocalDate.now(), TestdataCreationUtil.createImpfstoffEncepur())
			.withRegistrierungsEingang(RegistrierungsEingang.DATA_MIGRATION);

		Arrays.stream(KrankheitIdentifier.values()).filter(k -> !k.isWellEnabled()).forEach(kra -> {
			serviceUnderTest.sendApprovalPeriod(builder.getInfos(), impfschutz);
			Mockito.verify(restApiMock, timeout(500).times(0)).upsertApprovalPeriod(any(), any());
		});
	}

	@Test
	void given_non_well_linked_user_send_ApprovalPeriod_should_not_call_rest_api() {
		Impfschutz impfschutz = TestdataCreationUtil.createImpfschutz();

		Benutzer b = TestdataCreationUtil.createBenutzer("NotWellLinked", "Norbert", "123");
		Mockito.when(benutzerServiceMock.getBenutzerOfOnlineRegistrierung(any())).thenReturn(b);
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder.create(KrankheitIdentifier.FSME)
			.withRegistrierungsEingang(RegistrierungsEingang.ONLINE_REGISTRATION)
			.withBooster(LocalDate.now(), TestdataCreationUtil.createImpfstoffEncepur());

		serviceUnderTest.sendApprovalPeriod(builder.getInfos(), impfschutz);
		Mockito.verify(restApiMock, timeout(500).times(0)).upsertApprovalPeriod(any(), any());
	}

	@Test
	void given_well_linked_send_ApprovalPeriod_should_call_rest_api() {
		Impfschutz impfschutz = TestdataCreationUtil.createImpfschutz();

		Benutzer b = TestdataCreationUtil.createBenutzerWell();
		Mockito.when(benutzerServiceMock.getBenutzerOfOnlineRegistrierung(any())).thenReturn(b);
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		builder.create(KrankheitIdentifier.FSME)
			.withRegistrierungsEingang(RegistrierungsEingang.ONLINE_REGISTRATION)
			.withBooster(LocalDate.now(), TestdataCreationUtil.createImpfstoffEncepur());

		serviceUnderTest.sendApprovalPeriod(builder.getInfos(), impfschutz);
		Mockito.verify(restApiMock, timeout(500).times(1)).upsertApprovalPeriod(any(), any());
	}
}
