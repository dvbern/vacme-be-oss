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

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.migration.Migration;
import ch.dvbern.oss.vacme.entities.migration.QMigration;
import ch.dvbern.oss.vacme.service.HashIdService;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Optional;

@RequestScoped
@Transactional
public class MigrationRepo {

	private final Db db;
	private final EntityManager em;
	private final HashIdService hashIdService;

	@Inject
	public MigrationRepo(Db db, EntityManager em, HashIdService hashIdService) {
		this.db = db;
		this.em = em;
		this.hashIdService = hashIdService;
	}

	public void create(@NonNull Migration migration) {
		db.persist(migration);
		db.flush();
	}

	public void update(@NonNull Migration migration) {
		db.merge(migration);
		db.flush();
	}

	public Optional<Migration> getByImpfung(Impfung impfung) {
		return db.selectFrom(QMigration.migration)
			.where(QMigration.migration.impfung.eq(impfung))
			.fetchOne();
	}

	public void delete(@NonNull ID<Migration> id) {
		db.remove(id);
		db.flush();
	}
}
