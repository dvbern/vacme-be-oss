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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFile;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFileTyp;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFile;
import ch.dvbern.oss.vacme.jax.FileInfoJax;
import ch.dvbern.oss.vacme.repo.ImpfdossierFileRepo;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.VacmeFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ImpfdossierFileService {

	private final ImpfdossierFileRepo impfdossierFileRepo;

	@NonNull
	public ImpfdossierFile save(@NonNull ImpfdossierFile impfdossierFile) {
		impfdossierFileRepo.createImpfdossierFile(impfdossierFile);
		return impfdossierFile;
	}

	@NonNull
	public ImpfdossierFile createAndSave(
		byte[] content,
		@NonNull ImpfdossierFileTyp typ,
		@NonNull Impfdossier impfdossier
	) {
		final ImpfdossierFile impfdossierFile = VacmeFileUtil.createImpfdossierFile(typ, impfdossier, content);
		impfdossierFileRepo.createImpfdossierFile(impfdossierFile);
		return impfdossierFile;
	}

	@NonNull
	public List<FileInfoJax> getUploadedDocInfos(@NonNull Impfdossier impfdossier) {
		return impfdossierFileRepo.getUploadedFilesInfo(impfdossier);
	}

	@NonNull
	public ImpfdossierFile getDokument(Registrierung registrierung, UUID fileId) {
		ImpfdossierFile file = impfdossierFileRepo.getImpfdossierFile(fileId)
			.orElseThrow(() -> AppFailureException.entityNotFound(RegistrierungFile.class, fileId.toString()));
		if (!file.getImpfdossier().getRegistrierung().equals(registrierung)) {
			LOG.error(
				"File mit id {} gehoert nicht zu reg mit id {}",
				fileId.toString(),
				registrierung.getId().toString());
			throw AppValidationMessage.NOT_ALLOWED.create();
		}
		return file;
	}

	public void deleteTerminbestaetigung(@NonNull Impfdossier impfdossier) {
		final List<ImpfdossierFile> files =
			impfdossierFileRepo.getImpfdossierFiles(impfdossier, ImpfdossierFileTyp.TERMIN_BESTAETIGUNG);
		for (ImpfdossierFile file : files) {
			impfdossierFileRepo.deleteImpfdossierFile(file.toId());
		}
	}
}
