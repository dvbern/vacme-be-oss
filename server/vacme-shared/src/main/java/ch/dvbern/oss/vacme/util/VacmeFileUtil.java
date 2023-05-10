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

package ch.dvbern.oss.vacme.util;

import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFile;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFileTyp;
import ch.dvbern.oss.vacme.entities.embeddables.FileBlob;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFile;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFileTyp;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifikatFile;
import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class VacmeFileUtil {

	private VacmeFileUtil() {
	}

	@NonNull
	public static RegistrierungFile createRegistrierungFile(
		@NonNull RegistrierungFileTyp fileTyp,
		@NonNull Registrierung registrierung,
		@NonNull byte[] content
	) {
		RegistrierungFile registrierungFile = new RegistrierungFile();
		registrierungFile.setRegistrierung(registrierung);
		registrierungFile.setFileTyp(fileTyp);
		FileBlob file = getFileBlob(fileTyp.name().toLowerCase(), MimeType.APPLICATION_PDF, content);
		registrierungFile.setFileBlob(file);
		return registrierungFile;
	}

	@NonNull
	public static ImpfdossierFile createImpfdossierFile(
		@NonNull ImpfdossierFileTyp fileTyp,
		@NonNull Impfdossier impfdossier,
		@NonNull byte[] content
	) {
		ImpfdossierFile impfdossierFile = new ImpfdossierFile();
		impfdossierFile.setImpfdossier(impfdossier);
		impfdossierFile.setFileTyp(fileTyp);
		FileBlob file = getFileBlob(fileTyp.name().toLowerCase(), MimeType.APPLICATION_PDF, content);
		impfdossierFile.setFileBlob(file);
		return impfdossierFile;
	}

	@NonNull
	public static ZertifikatFile createZertifikatFile(
		@NonNull Registrierung registrierung,
		@NonNull MimeType type,
		@NonNull byte[] content
	) {
		ZertifikatFile registrierungFile = new ZertifikatFile();
		FileBlob file = getFileBlob(registrierung.getRegistrierungsnummer(), type, content);
		registrierungFile.setFileBlob(file);
		return registrierungFile;
	}

	@NonNull
	private static FileBlob getFileBlob(
		@NonNull String fileTyp,
		@NonNull MimeType mimeType,
		@NonNull byte[] content
	) {
		CleanFileName cleanFileName = new CleanFileName(fileTyp);
		FileBlob file = FileBlob.of(cleanFileName, mimeType, content);
		return file;
	}
}
