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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.terminbuchung.OdiFilter;
import ch.dvbern.oss.vacme.entities.terminbuchung.QOdiFilter;
import ch.dvbern.oss.vacme.entities.terminbuchung.QOrtDerImpfung;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Repo to manage odi Filters
 */
@RequestScoped
@Transactional
public class OdiFilterRepo {

	private final Db db;

	@Inject
	public OdiFilterRepo(Db db) {
		this.db = db;
	}

	public void create(@NonNull OdiFilter odiFilter) {
		db.persist(odiFilter);
		db.flush();
	}

	@NonNull
	public OdiFilter update(@NonNull OdiFilter odiFilter) {
		return db.merge(odiFilter);
	}

	public List<OdiFilter> getAll() {
		return db.selectFrom(QOdiFilter.odiFilter)
			.fetch();
	}

	public void removeOrphaned(){
		List<OdiFilter> orphanedFilters = getOrphanedFilters();
		orphanedFilters.forEach(db::remove);
	}

	private List<OdiFilter> getOrphanedFilters() {
		QOdiFilter aliasFilter = new QOdiFilter("filter");
		List<OdiFilter> orphanedFilters = db.selectFrom(QOdiFilter.odiFilter)
			.where(QOdiFilter.odiFilter.id.notIn(
				db.select(aliasFilter.id)
					.from(QOrtDerImpfung.ortDerImpfung).innerJoin(QOrtDerImpfung.ortDerImpfung.filters, aliasFilter)
					.asSubQuery()
			)).fetch();
		return orphanedFilters;
	}
}
