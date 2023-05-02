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

package ch.dvbern.oss.vacme.service.documentqueue;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueType;
import ch.dvbern.oss.vacme.entities.documentqueue.IDocumentQueueService;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueueResult;
import ch.dvbern.oss.vacme.repo.DocumentQueueRepo;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DocumentQueueService implements IDocumentQueueService {

	private final DocumentQueueRepo documentQueueRepo;

	@SuppressWarnings("CdiInjectInspection")
	@ConfigProperty(name = "vacme.cron.doccreation.maxjobs", defaultValue = "2")
	public int maxNumOfJobsPerType = 2;


	@Override
	public void addJobToQueueIfAllowedForBenutzer(@NonNull DocumentQueue abrechnungDocQueue) {
		this.checkCurrentBenutzerHasNotReachedLimitOfRunningJobsOfSameType(abrechnungDocQueue.getTyp(), abrechnungDocQueue.getBenutzer());
		this.storeJob(abrechnungDocQueue);
	}

	@Override
	@NonNull
	public DocumentQueue getDocumentQueueItem(@NonNull Long docId){
		return this.documentQueueRepo.getDocumentQueueItem(docId);
	}

	private void storeJob(@NonNull DocumentQueue documentQueue) {
		this.documentQueueRepo.create(documentQueue);
	}

	private void checkCurrentBenutzerHasNotReachedLimitOfRunningJobsOfSameType(@NonNull DocumentQueueType typ, @NonNull Benutzer benutzer) {
		List<DocumentQueue> documentQueueItems = documentQueueRepo.findUnfinishedDocumentQueueItems(typ, benutzer);

		List<DocumentQueue> unfinishedQueueItems = filterOutAbgelaufene(documentQueueItems);
		if (unfinishedQueueItems.size() > maxNumOfJobsPerType) {
			throw AppValidationMessage.JOB_IN_PROGRESS.create(benutzer.getBenutzername(), typ);
		}
	}

	@NonNull
	private List<DocumentQueue> filterOutAbgelaufene(@NonNull List<DocumentQueue> documentQueueItems) {
		return documentQueueItems.stream()
			.filter(documentQueue -> documentQueue.getTimestampErstellt().isAfter(LocalDateTime.now().minusHours(24)))
			.collect(Collectors.toList());
	}

	@Override
	public void saveResult(@NonNull DocumentQueue documentQueueItem, @Nullable DocumentQueueResult documentQueueResult) {
		documentQueueItem.setDocumentQueueResult(documentQueueResult);
		documentQueueRepo.update(documentQueueItem);
	}

	@Override
	public void cleanupExpiredResults() {
		documentQueueRepo.cleanupExpiredResults();
	}

	@NonNull
	@Override
	public Collection<DocumentQueue> findJobsForBenutzer(@Nullable Benutzer benutzer) {
		if (benutzer == null) {
			return Collections.emptyList();
		}
		return documentQueueRepo.findJobsForBenutzer(benutzer);
	}

	@NonNull
	@Override
	public List<DocumentQueue> findUnfinishedDocumentQueueItems(){
		return documentQueueRepo.findUnfinishedDocumentQueueItems();
	}

	@Override
	@NonNull
	public Optional<DocumentQueue> getDocumentQueueByResultId(@Nullable UUID docResultId){
		return documentQueueRepo.getDocumentQueueByResultId(docResultId);
	}

	@Override
	@NonNull
	public DocumentQueueResult getDocumentQueueResultItem(@NonNull UUID docResultId) {
		return documentQueueRepo.getDocumentQueueResultItem(docResultId);
	}

	@Override
	public void markDocumentQueueItemAsInProgress(@NonNull DocumentQueue documentQueueItem) {
		documentQueueRepo.markDocumentQueueItemAsInProgressNewTransaction(documentQueueItem);
	}
}
