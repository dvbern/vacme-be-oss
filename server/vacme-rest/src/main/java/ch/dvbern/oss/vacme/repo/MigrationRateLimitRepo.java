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

import ch.dvbern.oss.vacme.entities.migration.MigrationRateLimit;
import ch.dvbern.oss.vacme.entities.migration.QMigrationRateLimit;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Service to Access Rate Limit table
 */
@RequestScoped
@Transactional
public class MigrationRateLimitRepo {

	private final Db db;
	@Inject
	public MigrationRateLimitRepo(Db db) {
		this.db = db;
	}

	public void update(@NonNull MigrationRateLimit migrationRateLimit) {
		db.merge(migrationRateLimit);
		db.flush();
	}

	/**
	 *
	 * @return the one and only entry in the table. Our API does not support multiple callers having their own
	 * rate limiting
	 *
	 */
	public Optional<MigrationRateLimit> get() {
		return db.selectFrom(QMigrationRateLimit.migrationRateLimit)
			.orderBy(QMigrationRateLimit.migrationRateLimit.timestampLastRequest.desc())
			.fetch()
			.stream()
			.findFirst();
	}
}
