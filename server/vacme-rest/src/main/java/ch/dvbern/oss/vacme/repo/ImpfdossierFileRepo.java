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

package ch.dvbern.oss.vacme.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFile;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFileTyp;
import ch.dvbern.oss.vacme.entities.dossier.QImpfdossierFile;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.jax.FileInfoJax;
import ch.dvbern.oss.vacme.jax.QFileInfoJax;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@Transactional
@Slf4j
public class ImpfdossierFileRepo {

	private final Db db;

	@Inject
	public ImpfdossierFileRepo(Db db) {
		this.db = db;
	}

	public void createImpfdossierFile(@NonNull ImpfdossierFile impfdossierFile) {
		if (Boolean.TRUE.equals(impfdossierFile.getImpfdossier().getRegistrierung().getVerstorben())) {
			impfdossierFile.setAbgeholt(true);
			LOG.warn(
				"File-Generierung fuer verstorbene Reg {}. Auslieferung unterdrueckt",
				impfdossierFile.getImpfdossier().getRegistrierung().getRegistrierungsnummer());
		}
		db.persist(impfdossierFile);
		db.flush();
	}

	@NonNull
	public List<ImpfdossierFile> getImpfdossierFiles(
		@NonNull Impfdossier impfdossier,
		@NonNull ImpfdossierFileTyp fileTyp
	) {
		return db
			.selectFrom(QImpfdossierFile.impfdossierFile)
			.where(QImpfdossierFile.impfdossierFile.impfdossier.eq(impfdossier)
				.and(QImpfdossierFile.impfdossierFile.fileTyp.eq(fileTyp)))
			.orderBy(QImpfdossierFile.impfdossierFile.timestampErstellt.desc())
			.fetch();
	}

	@NonNull
	public List<FileInfoJax> getUploadedFilesInfo(@NonNull Impfdossier impfdossier) {
		return db.selectFrom(QImpfdossierFile.impfdossierFile)
			.select(new QFileInfoJax(
				QImpfdossierFile.impfdossierFile.id,
				QImpfdossierFile.impfdossierFile.fileBlob.fileName,
				QImpfdossierFile.impfdossierFile.fileBlob.fileSize)
			)
			.where(QImpfdossierFile.impfdossierFile.impfdossier.eq(impfdossier)
				.and(QImpfdossierFile.impfdossierFile.fileTyp.eq(ImpfdossierFileTyp.IMPFFREIGABE_DURCH_HAUSARZT)))
			.fetch();
	}

	public void deleteImpfdossierFile(@NonNull ID<ImpfdossierFile> id) {
		db.remove(id);
	}

	@NonNull
	public Optional<ImpfdossierFile> getImpfdossierFile(@NonNull UUID fileId) {
		return db.get(ImpfdossierFile.toId(fileId));
	}

	public void deleteAllImpfdossierFilesForDossier(@NonNull Impfdossier dossier) {
		long result = db.delete(QImpfdossierFile.impfdossierFile)
			.where(QImpfdossierFile.impfdossierFile.impfdossier.eq(dossier))
			.execute();
		LOG.info("{} ImpfdossierFiles geloescht", result);
	}
}
