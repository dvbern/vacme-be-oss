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

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueue;
import ch.dvbern.oss.vacme.scheduler.SystemAdminRunnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.util.TimingUtil.calculateGenerationSpeed;

@ApplicationScoped
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DocumentQueueRunnerService {



	private final SystemAdminRunnerService systemAdminRunnerService;

	@Transactional(TxType.NOT_SUPPORTED)
	public int performDocumentGenerationRun(@NonNull List<DocumentQueue> currentQueueItems) {
		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;
		try {
			if (!currentQueueItems.isEmpty()) {
				LOG.info("VACME-DOC-CREATION: Starting to calculate {} documents",
					currentQueueItems.size());
			}

			for (DocumentQueue queueItem : currentQueueItems) {
				try {
					// ich glaube wir koennen hier nichts checken weil wir fast immer neu berechnen koennen / wollen
					LOG.debug("VACME-DOC-CREATION: Erstelle document fuer  Benutzer {}", queueItem.getBenutzer().getBenutzername());
					boolean success = this.systemAdminRunnerService.processDocumentQueueItem(queueItem.getId());

					if (success) {
						successCounter++;
					}
				} finally {
					totalCounter++;
				}
			}
		} finally {
			stopWatch.stop();
			if (!currentQueueItems.isEmpty()) {
				LOG.info(
					"VACME-DOC-CREATION: Document Erstellung beendet. Es wurden {} Docs von total {} QueueItems"
						+ " in {}ms berechnet. {} ms/stk",
					successCounter, currentQueueItems.size(), stopWatch.getTime(TimeUnit.MILLISECONDS),
					calculateGenerationSpeed(totalCounter,
						stopWatch.getTime(TimeUnit.MILLISECONDS)));
			}
		}
		return successCounter;
	}

}
