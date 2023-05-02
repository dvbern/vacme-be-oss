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

package ch.dvbern.oss.vacme.entities.documentqueue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueueResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IDocumentQueueService {

	void addJobToQueueIfAllowedForBenutzer(@NonNull DocumentQueue abrechnungDocQueue);

	@NonNull
	DocumentQueue getDocumentQueueItem(@NonNull Long docId);

	void saveResult(@NonNull DocumentQueue documentQueueItem, @Nullable DocumentQueueResult documentQueueResult);

	void cleanupExpiredResults();

	@NonNull
	Collection<DocumentQueue> findJobsForBenutzer(@Nullable Benutzer benutzer);

	@NonNull
	List<DocumentQueue> findUnfinishedDocumentQueueItems();

	@NonNull
	Optional<DocumentQueue> getDocumentQueueByResultId(@Nullable UUID docResultId);

	@NonNull
	DocumentQueueResult getDocumentQueueResultItem(@NonNull UUID docResultId);

	void markDocumentQueueItemAsInProgress(@NonNull DocumentQueue documentQueueItem);
}


