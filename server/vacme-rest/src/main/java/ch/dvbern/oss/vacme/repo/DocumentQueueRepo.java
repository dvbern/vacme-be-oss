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

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.base.IDL;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueStatus;
import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueType;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueueResult;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.entities.documentqueue.entities.QDocumentQueue.documentQueue;
import static ch.dvbern.oss.vacme.entities.documentqueue.entities.QDocumentQueueResult.documentQueueResult;
import static ch.dvbern.oss.vacme.shared.errors.AppFailureException.entityNotFound;

@RequestScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DocumentQueueRepo {

	@SuppressWarnings("CdiInjectInspection")
	@ConfigProperty(name = "vacme.cron.doccreation.cleanup.maxage.seconds")
	long maxDocumentqueueResultAgeSeconds = 60 * 60 * 4; // 4 hours

	private final Db db;

	public void create(@NonNull DocumentQueue docQueueItem) {
		Long nextId = this.getNextDocumentQueueId();
		docQueueItem.setId(nextId);
		db.persist(docQueueItem);
	}

	public void update(@NonNull DocumentQueue docQueueItem) {
		db.merge(docQueueItem);
	}

	@NonNull
	public DocumentQueue getDocumentQueueItem(@NonNull Long docId) {
		IDL<DocumentQueue> id = IDL.parse(docId, DocumentQueue.class);
		return db.get(id).orElseThrow(() -> entityNotFound(id.getEntityClazz(),id));
	}

	@NonNull
	public List<DocumentQueue> findUnfinishedDocumentQueueItems(@NonNull DocumentQueueType typ, @NonNull Benutzer benutzer) {
		return db.select(documentQueue)
			.from(documentQueue)
			.where(documentQueue.typ.eq(typ)
				.and(documentQueue.benutzer.eq(benutzer))
				.and(documentQueue.status.in(DocumentQueueStatus.FAILED_RETRY, DocumentQueueStatus.NEW)))
			.orderBy(documentQueue.timestampErstellt.asc()) // Die aeltesten zuerst
			.fetch();
	}

	@NonNull
	public List<DocumentQueue> findUnfinishedDocumentQueueItems() {
		return db.select(documentQueue)
			.from(documentQueue)
				.where(documentQueue.status.in(DocumentQueueStatus.FAILED_RETRY, DocumentQueueStatus.NEW, DocumentQueueStatus.IN_PROGRESS))
			.orderBy(documentQueue.timestampErstellt.asc()) // Die aeltesten zuerst
			.fetch();
	}


	@Transactional(Transactional.TxType.SUPPORTS)
	/* use transaction if there is one in the context (keep in mind sequences can't be rolled back)
	otherwise run without */
	public long getNextDocumentQueueId() {
		BigInteger nextValue =
			(BigInteger) db.getEntityManager()
				.createNativeQuery("SELECT NEXT VALUE FOR document_queue_sequence;")
				.getSingleResult();
		return nextValue.longValue();
	}

	public void cleanupExpiredResults() {
		List<DocumentQueue> finishedDocumentQueueItems = findFinishedDocumentQueueItems();
		for (DocumentQueue finishedDocumentQueueItem : finishedDocumentQueueItems) {
			db.remove(finishedDocumentQueueItem);
			LOG.info("VACME-DOC-QUEUE: Removed generated document from queue");
		}

		List<DocumentQueue> oldFailedDocumentQueues = findOldFailedDocumentQueues();
		for (DocumentQueue oldFailedDocumentQueue : oldFailedDocumentQueues) {
			db.remove(oldFailedDocumentQueue);
			LOG.info("VACME-DOC-QUEUE: Deleted old Failed Document from Queue {}", oldFailedDocumentQueue.getId());
		}
	}

	@NonNull
	private List<DocumentQueue> findFinishedDocumentQueueItems() {
		return db.select(documentQueue)
			.from(documentQueue)
			.where((documentQueue.status.in(DocumentQueueStatus.SUCCESS))
			.and(documentQueue.resultTimestamp.lt(LocalDateTime.now().minusSeconds(maxDocumentqueueResultAgeSeconds))))
			.orderBy(documentQueue.resultTimestamp.asc()) // Die aeltesten zuerst
			.fetch();
	}

	@NonNull
	private List<DocumentQueue> findOldFailedDocumentQueues() {
		long deleteFailedDocumentTimeoutSeconds = maxDocumentqueueResultAgeSeconds * 4; // 4 times as long as we keep result
		return db.select(documentQueue)
			.from(documentQueue)
			.where((documentQueue.status.in(DocumentQueueStatus.FAILED, DocumentQueueStatus.FAILED_RETRY))
			.and(documentQueue.resultTimestamp.lt(LocalDateTime.now().minusSeconds(deleteFailedDocumentTimeoutSeconds))))
			.orderBy(documentQueue.resultTimestamp.asc()) // Die aeltesten zuerst
			.fetch();
	}

	@NonNull
	public Collection<DocumentQueue> findJobsForBenutzer(@NonNull Benutzer benutzer) {
		return db.select(documentQueue)
			.from(documentQueue)
			.where(documentQueue.benutzer.eq(benutzer))
			.orderBy(documentQueue.timestampErstellt.desc()) // Die aeltesten zuerst
			.fetch();
	}

	@NonNull
	public Optional<DocumentQueue> getDocumentQueueByResultId(@Nullable UUID docResultId){
		return db.select(documentQueue)
			.from(documentQueue)
			.innerJoin(documentQueueResult).on(documentQueueResult.eq(documentQueue.documentQueueResult))
			.where(documentQueueResult.id.eq(docResultId))
			.fetchOne();
	}

	@NonNull
	public DocumentQueueResult getDocumentQueueResultItem(@NonNull UUID docResultId) {
		ID<DocumentQueueResult> documentQueueResultID = DocumentQueueResult.toId(docResultId);
		return db.get(documentQueueResultID)
			.orElseThrow(() -> AppFailureException.entityNotFound(DocumentQueueResult.class, docResultId.toString()));
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void markDocumentQueueItemAsInProgressNewTransaction(@NonNull DocumentQueue documentQueueItem) {
		LOG.debug("VACME-DOC-CREATION: Marking DocumentQueue as IN_PROGRESS {}", documentQueueItem.getId().toString());
		documentQueueItem.setStatus(DocumentQueueStatus.IN_PROGRESS);
		this.update(documentQueueItem);
	}
}
