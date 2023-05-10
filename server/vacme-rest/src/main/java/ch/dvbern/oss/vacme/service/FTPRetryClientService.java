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

import java.io.ByteArrayInputStream;

import javax.enterprise.context.RequestScoped;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFile;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFileTyp;
import ch.dvbern.oss.vacme.repo.RegistrierungFileRepo;
import ch.dvbern.oss.vacme.util.VacmeFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * FTP Client welcher das File nochmal zu uebermitteln versucht. Bei Fehlern wird das Dokument in der DB gespeichert
 */
@Slf4j
@RequestScoped
@Transactional
public class FTPRetryClientService {

	private final FTPClientService ftpClientService;
	private final RegistrierungFileRepo registrierungFileRepo;

	public FTPRetryClientService(
		@NonNull FTPClientService ftpClientService,
		@NonNull RegistrierungFileRepo registrierungFileRepo
	) {
		this.ftpClientService = ftpClientService;
		this.registrierungFileRepo = registrierungFileRepo;
	}

	public void tryToPutFilesToFtp(String filename,
		@Nullable String prefix,
		Registrierung registrierung,
		byte[] content,
		RegistrierungFileTyp typ
	)
	{
		int maxRetries = 1;
		int tries = 0;
		boolean success = false;
		boolean didRetry = false;
		prefix = prefix == null ? "" : prefix + '_';


		while (!success && tries <= maxRetries) {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(content);

			boolean ftpUploadSuccess = ftpClientService.putFileToPath(byteStream,  prefix  + filename);

			if (ftpUploadSuccess) {
				success = true;
			} else{
				sleepForAShortTime();
				LOG.warn("Could not upload to FTP. Will retry 1 more time for reg {}", registrierung.getRegistrierungsnummer() );
				tries++;
				didRetry = true;
			}

			IOUtils.closeQuietly(byteStream, null);
		}

		if (!success) {
			final RegistrierungFile registrierungFile = VacmeFileUtil.createRegistrierungFile(
				typ, registrierung, content);
			this.registrierungFileRepo.createRegistrierungFile(registrierungFile);
			LOG.warn("Stored file in Datebase after FTP Upload problems");
		} else if (didRetry) {
			LOG.info("FTP Upload success after retry");
		}
	}


	private void sleepForAShortTime()  {
		try {
			Thread.sleep(150);

		} catch (InterruptedException e) {
			LOG.warn("FTP Retry Wait was interrupted");
		}
	}
}
