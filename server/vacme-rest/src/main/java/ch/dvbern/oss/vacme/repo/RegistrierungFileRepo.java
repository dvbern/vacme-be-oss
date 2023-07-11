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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.registration.QRegistrierungFile;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFile;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFileTyp;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequestScoped
@Transactional
@Slf4j
public class RegistrierungFileRepo {

	private final Db db;

	@Inject
	public RegistrierungFileRepo(Db db) {
		this.db = db;
	}

	public void createRegistrierungFile(@NonNull RegistrierungFile registrierungFile) {
		if (Boolean.TRUE.equals(registrierungFile.getRegistrierung().getVerstorben())) {
			registrierungFile.setAbgeholt(true);
			LOG.warn(
				"File-Generierung fuer verstorbene Reg {}. Auslieferung unterdrueckt",
				registrierungFile.getRegistrierung().getRegistrierungsnummer());
		}
		db.persist(registrierungFile);
		db.flush();
	}

	@Nullable
	public RegistrierungFile getRegistrierungFile(
		@NonNull Registrierung registrierung,
		@NonNull RegistrierungFileTyp fileTyp) {
		return db.selectFrom(QRegistrierungFile.registrierungFile)
			.where(QRegistrierungFile.registrierungFile.registrierung.eq(registrierung)
				.and(QRegistrierungFile.registrierungFile.fileTyp.eq(fileTyp)))
			.orderBy(QRegistrierungFile.registrierungFile.timestampErstellt.desc())
			.fetchFirst();
	}

	public void deleteRegistrierungFile(@NonNull ID<RegistrierungFile> id) {
		db.remove(id);
	}

	public void deleteAllRegistrierungFilesForReg(@NonNull Registrierung registrierung) {
		long result = db.delete(QRegistrierungFile.registrierungFile)
			.where(QRegistrierungFile.registrierungFile.registrierung.eq(registrierung))
			.execute();
		LOG.info("{} RegistrierungFiles geloescht", result);
	}
}
