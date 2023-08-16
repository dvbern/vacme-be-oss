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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.QImpfstoff;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@Transactional
public class ImpfstoffRepo {

	private final Db db;

	@Inject
	public ImpfstoffRepo(Db db) {
		this.db = db;
	}

	public void create(@NonNull Impfstoff impfstoff) {
		db.persist(impfstoff);
		db.flush();
	}

	@NonNull
	public Optional<Impfstoff> getById(@NonNull ID<Impfstoff> id) {
		return db.get(id);
	}

	@NonNull
	public List<Impfstoff> findAll() {
		return db.findAll(QImpfstoff.impfstoff);
	}

	@NonNull
	public List<Impfstoff> findAllImpfstoffeWithKrankheiten() {
		return db
			.selectFrom(QImpfstoff.impfstoff)
			.leftJoin(QImpfstoff.impfstoff.krankheiten).fetchJoin()
			.fetch();
	}

	public void update(@NonNull Impfstoff impfstoff) {
		db.merge(impfstoff);
		db.flush();
	}
}
