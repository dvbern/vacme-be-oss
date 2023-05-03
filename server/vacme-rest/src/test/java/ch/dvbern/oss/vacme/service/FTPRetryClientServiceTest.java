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

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFileTyp;
import ch.dvbern.oss.vacme.repo.RegistrierungFileRepo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FTPRetryClientServiceTest {


	@Test
	public void testFtpFailure(){
		FTPClientService ftpMock = mock(FTPClientService.class);
		Mockito.when(ftpMock.putFileToPath(any(), anyString())).thenReturn(false);

		RegistrierungFileRepo dokServiceMock = mock(RegistrierungFileRepo.class);
		FTPRetryClientService serviceUnderTest = new FTPRetryClientService(ftpMock, dokServiceMock);
		Registrierung reg = new Registrierung();
		byte[] content = new byte[1];

		serviceUnderTest.tryToPutFilesToFtp("test.pdf", null,reg, content, RegistrierungFileTyp.REGISTRIERUNG_BESTAETIGUNG_FTP_FAIL);

		verify(ftpMock, times(2)).putFileToPath(any(), any());
		verify(dokServiceMock, times(1)).createRegistrierungFile(any());
	}

	@Test
	public void testSuccess(){
		FTPClientService ftpMock = mock(FTPClientService.class);
		Mockito.when(ftpMock.putFileToPath(any(), anyString())).thenReturn(true);

		RegistrierungFileRepo dokServiceMock = mock(RegistrierungFileRepo.class);
		FTPRetryClientService serviceUnderTest = new FTPRetryClientService(ftpMock, dokServiceMock);
		Registrierung reg = new Registrierung();
		byte[] content = new byte[1];

		serviceUnderTest.tryToPutFilesToFtp("test.pdf", null, reg, content, RegistrierungFileTyp.REGISTRIERUNG_BESTAETIGUNG_FTP_FAIL);

		verify(ftpMock, times(1)).putFileToPath(any(), any());
		verify(dokServiceMock, times(0)).createRegistrierungFile(any());
	}
}
