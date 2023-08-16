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
import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ApplicationProperty;
import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.base.QApplicationProperty;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@Transactional
public class ApplicationPropertyRepo {

	private final Db db;

	@Inject
	public ApplicationPropertyRepo(Db db) {
		this.db = db;
	}

	public void create(@NonNull ApplicationProperty applicationProperty) {
		db.persist(applicationProperty);
		db.flush();
	}

	public void update(@NonNull ApplicationProperty applicationProperty) {
		db.merge(applicationProperty);
		db.flush();
	}

	@NonNull
	public List<ApplicationProperty> findAll() {
		return db.findAll(QApplicationProperty.applicationProperty);
	}

	@NonNull
	public Optional<ApplicationProperty> getByKey(@NonNull ApplicationPropertyKey key) {
		var result = db.selectFrom(QApplicationProperty.applicationProperty)
			.where(QApplicationProperty.applicationProperty.name.eq(key))
			.fetchOne();
		return result;
	}

	/**
	 * used to aquire the batch job lock that exists to prevent concurrent execution of certapi tasks
	 * @param lockKey key of the lock
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void aquireBatchJobLock(@NonNull ApplicationPropertyKey lockKey) {

		// beim setzen des locks verwenden wir pessimistic da wir sicher sein muessen das wir nur 1 transaction dies erfolgreich tut
		ApplicationProperty applicationProperty = readBatchjobLockFromDatabase(lockKey, LockModeType.PESSIMISTIC_WRITE);
		if (Boolean.parseBoolean(applicationProperty.getValue())) {
			throw new AppFailureException("BatchjobLock was already Aquired by another Process (value was true in db)");
		}
		applicationProperty.setValue(Boolean.TRUE.toString());
		db.merge(applicationProperty);
	}

	/**
	 * used to release the batch job lock that exists prevent concurrent execution of certapi tasks
	 * @param lockKey key of the lock to release
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void releaseBatchJobLock(ApplicationPropertyKey lockKey) {
		// beim releasen muessen wir kein lock haben
		ApplicationProperty applicationProperty = readBatchjobLockFromDatabase(lockKey, LockModeType.OPTIMISTIC);
		applicationProperty.setValue(Boolean.FALSE.toString());
		db.merge(applicationProperty);
	}


	@NonNull
	private ApplicationProperty readBatchjobLockFromDatabase(ApplicationPropertyKey lockKey, @NonNull LockModeType mode) {
		return db.selectFrom(QApplicationProperty.applicationProperty)
			.where(QApplicationProperty.applicationProperty.name.eq(lockKey))
			.setLockMode(mode) // wir wollen hier exklusiv updaten daher write lock
			.fetchOne()
			.orElseThrow(() -> AppFailureException.entityNotFound(ApplicationProperty.class, lockKey));
	}
}
