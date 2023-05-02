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

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.registration.ImpfungArchive;
import ch.dvbern.oss.vacme.entities.registration.QImpfungArchive;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@Transactional
@Slf4j
public class ImpfungArchiveRepo {

	private final Db db;

	@Inject
	public ImpfungArchiveRepo(Db db) {
		this.db = db;
	}

	public Optional<ImpfungArchive> getByRegistrierung(Registrierung reg) {
		return db.selectFrom(QImpfungArchive.impfungArchive)
			.where(QImpfungArchive.impfungArchive.registrierung.eq(reg))
			.fetchOne();
	}


	public void create(@NonNull ImpfungArchive archive) {
		db.persist(archive);
		db.flush();
	}

	public void update(@NonNull ImpfungArchive archive) {
		db.merge(archive);
		db.flush();
	}
	public void delete(@NonNull ImpfungArchive archive) {
		db.remove(archive);
		db.flush();
	}

	@NonNull
	public Optional<ImpfungArchive> getById(@NonNull ID<ImpfungArchive> id) {
		return db.get(id);
	}

	public void deleteById(@NonNull ID<ImpfungArchive> id) {
		ImpfungArchive attachedArchive = this.getById(id).orElseThrow(() -> AppFailureException.entityNotFound(ImpfungArchive.class, id.toString()));
		db.remove(attachedArchive);
	}
}
