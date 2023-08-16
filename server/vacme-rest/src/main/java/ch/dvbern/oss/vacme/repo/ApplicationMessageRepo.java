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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ApplicationMessage;
import ch.dvbern.oss.vacme.entities.base.QApplicationMessage;
import ch.dvbern.oss.vacme.entities.util.Pager;
import ch.dvbern.oss.vacme.smartdb.Db;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequestScoped
@Transactional
public class ApplicationMessageRepo {

	private final Db db;

	@Inject
	public ApplicationMessageRepo(Db db) {
		this.db = db;
	}

	public void create(@NonNull ApplicationMessage applicationMessage) {
		db.persist(applicationMessage);
		db.flush();
	}

	@NonNull
	public List<ApplicationMessage> getLatestAtDateTime(@NonNull LocalDateTime dateTime, @NonNull Integer amount) {
		QApplicationMessage applicationMessage = QApplicationMessage.applicationMessage;

		return db.select(applicationMessage)
			.from(applicationMessage)
			.where(applicationMessage.zeitfenster.von.lt(dateTime)
				.and(applicationMessage.zeitfenster.bis.gt(dateTime)))
			.orderBy(applicationMessage.timestampMutiert.desc())
			.limit(amount)
			.fetch();
	}

	/**
	 * Bekommt alle die nachrichten, erst jetziger dann zukunftige dann vergangen
	 * @param dateTime referenz zeit
	 * @return die nachrichten
	 */
	@NonNull
	public Pager<ApplicationMessage> getAllAtDateTime(
		@NonNull LocalDateTime dateTime, @NonNull Integer pageSize, @NonNull Integer pageIndex
	) {
		QApplicationMessage applicationMessage = QApplicationMessage.applicationMessage;
		NumberExpression<Integer> orderInteger = new CaseBuilder()
			.when(applicationMessage.zeitfenster.von.lt(dateTime).and(applicationMessage.zeitfenster.bis.gt(dateTime))).then(1)
			.when(applicationMessage.zeitfenster.von.gt(dateTime)).then(2)
			.otherwise(3);

		QueryResults<ApplicationMessage> results = db.select(applicationMessage)
			.from(applicationMessage)
			.orderBy(orderInteger.asc(), applicationMessage.timestampMutiert.desc())
			.offset(pageIndex * pageSize)
			.limit(pageSize)
			.fetchResults();

		return Pager.fromQueryResults(results);
	}

	@NonNull
	public Optional<ApplicationMessage> getById(@NonNull String messageId) {
		QApplicationMessage applicationMessage = QApplicationMessage.applicationMessage;

		return db.select(applicationMessage)
			.from(applicationMessage)
			.where(applicationMessage.id.eq(UUID.fromString(messageId)))
			.fetchOne();
	}

	@NonNull
	public Optional<ApplicationMessage> getByTitle(@NonNull String messageTitle) {
		QApplicationMessage applicationMessage = QApplicationMessage.applicationMessage;

		return db.select(applicationMessage)
			.from(applicationMessage)
			.where(applicationMessage.title.eq(messageTitle))
			.fetchOne();
	}
}
