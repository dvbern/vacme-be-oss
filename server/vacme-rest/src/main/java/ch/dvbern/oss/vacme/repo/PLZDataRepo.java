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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.plz.PLZData;
import ch.dvbern.oss.vacme.entities.plz.PLZMedstat;
import ch.dvbern.oss.vacme.entities.plz.QPLZData;
import ch.dvbern.oss.vacme.entities.plz.QPLZMedstat;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@Transactional
public class PLZDataRepo {

	private final Db db;

	@Inject
	public PLZDataRepo(Db db) {
		this.db = db;
	}

	public void create(@NonNull PLZData plzData) {
		db.persist(plzData);
		db.flush();
	}

	public void createAll(@NonNull List<PLZData> plzData) {
		plzData.forEach(db::persist);
		db.flush();
	}

	public void createAllMedstat(@NonNull List<PLZMedstat> plzData) {
		plzData.forEach(db::persist);
		db.flush();
	}

	public void update(@NonNull PLZData plzData) {
		db.merge(plzData);
		db.flush();
	}

	@NonNull
	public List<PLZData> findAll() {
		return db.findAll(QPLZData.pLZData);
	}

	public void dropAll() {
		Query nativeQuery = this.db.getEntityManager().createNativeQuery("DELETE FROM PLZData");
		int i = nativeQuery.executeUpdate();
		LOG.info("All plzData removed. entries: {}", i);
	}

	public void dropAllMedstat() {
		Query nativeQuery = this.db.getEntityManager().createNativeQuery("DELETE FROM PLZMedstat");
		int i = nativeQuery.executeUpdate();
		LOG.info("All plzMedstat removed. entries: {}", i);
	}

	@NonNull
	public List<PLZData> findOrteForPLZ(String plz) {
		return db.selectFrom(QPLZData.pLZData).where(QPLZData.pLZData.plz.eq(plz)).fetch();
	}

	@NonNull
	public Optional<PLZMedstat> findMedstatForPLZ(String plz) {
		return db.selectFrom(QPLZMedstat.pLZMedstat).where(QPLZMedstat.pLZMedstat.plz.eq(plz))
			.fetchOne();
	}
}


