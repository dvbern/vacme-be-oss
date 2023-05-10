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

package ch.dvbern.oss.vacme.service.massenmutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ApplicationProperty;
import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueue;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueueTyp;
import ch.dvbern.oss.vacme.service.ApplicationPropertyService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.util.TimingUtil.calculateGenerationSpeed;

@ApplicationScoped
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MassenverarbeitungRunnerService {

	private final ApplicationPropertyService applicationPropertyService;
	private final MassenverarbeitungService massenmutationService;

	@ConfigProperty(name = "vacme.massenverarbeitung.queue.job.batchsize", defaultValue = "200")
	long vacmeMassenverarbeitungQueueProcessingBatchSize = 200;

	@ConfigProperty(name = "vacme.booster.rule.engine.job.partitions", defaultValue = "3")
	long numberOfPartitions = 3;

	private long getNumberOfPartitions() {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_PARTITIONS);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Long.parseLong(applicationProperty.getValue()))
			.orElseGet(() -> numberOfPartitions);
	}

	public void performImpfungActionByQueue(@NonNull MassenverarbeitungQueueTyp typ) {
		List<MassenverarbeitungQueue> queueEntries = massenmutationService.findMassenverarbeitungQueueItemsToProcess(
			getMassenverarbeitungQueueProcessingJobBatchSize(), typ);

		if (queueEntries.isEmpty()) {
			// Nothing to do
			return;
		}

		// if we are not using multiple workpartitions do not bother starting extra worker-threads
		long numberOfPartitions = this.getNumberOfPartitions();
		if (numberOfPartitions == 1 || numberOfPartitions == 0) {
			// calculation is synchronous on one thread
			performImpfungAction(queueEntries);
			return;
		}

		// async, calculation will be partitioned into multiple workloads and performed by multiple threads
		StopWatch stopWatch = StopWatch.createStarted();
		LOG.info(
			"VACME-MASSENVERARBEITUNG: Starting to Process MassenverarbeitungQueue Items. Task will be split into {} partitions",
			numberOfPartitions);
		Map<Long, List<MassenverarbeitungQueue>> partitionMap = queueEntries.stream()
			.collect(Collectors.groupingBy(entry -> entry.getId() % numberOfPartitions));

		List<CompletableFuture<Integer>> recalculationTaks = new ArrayList<>();
		for (List<MassenverarbeitungQueue> currentQueueItems : partitionMap.values()) {

			// potentielles Improvement: hier die verschiedenen Partitionen
			// per expliziten webservicecall ueber ip auf quarkus auf mehrere nodes verteilen

			CompletableFuture<Integer> recalculatePartitionTask = Uni.createFrom().item(currentQueueItems)
				.emitOn(Infrastructure.getDefaultWorkerPool())
				.onItem().transform(this::performImpfungAction) // actual calculation
				.subscribe().asCompletionStage();

			recalculationTaks.add(recalculatePartitionTask);
		}

		Integer totalProcessedSuccess = 0;

		for (CompletableFuture<Integer> integerUni : recalculationTaks) {
			try {
				Integer successfullCalculationsInTask = integerUni.get(3, TimeUnit.MINUTES); // wait max 3 Minutes
				totalProcessedSuccess += successfullCalculationsInTask;
			} catch (InterruptedException e) {
				LOG.error("VACME-MASSENVERARBEITUNG: Thread was interrupted while processing {} queueEntries ",
					queueEntries.size(), e);
			} catch (ExecutionException e) {
				LOG.error("VACME-MASSENVERARBEITUNG: An Exception escaped while processing {} queueEntries", queueEntries.size(), e);
			} catch (TimeoutException e) {
				LOG.error("VACME-MASSENVERARBEITUNG: Waiting for the calculation result of the {} qued entries took "
					+ "to long", queueEntries.size());
			}
		}
		LOG.info("VACME-MASSENVERARBEITUNG: Processed {} of {} queueItems successfully in {} partitions. Total time {}ms",
			totalProcessedSuccess, queueEntries.size(), numberOfPartitions, stopWatch.getTime(TimeUnit.MILLISECONDS));
	}

	private long getMassenverarbeitungQueueProcessingJobBatchSize() {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_BATCH_SIZE);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Long.parseLong(applicationProperty.getValue()))
			.orElseGet(() -> vacmeMassenverarbeitungQueueProcessingBatchSize);
	}

	private int performImpfungAction(@NonNull List<MassenverarbeitungQueue> currentQueueItems) {
		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;
		try {
			LOG.info("VACME-MASSENVERARBEITUNG: Starting to perform Action for {} Impfungen",
				currentQueueItems.size());

			for (MassenverarbeitungQueue queueItem : currentQueueItems) {
				try {
					boolean success = false;
					if (queueItem.getTyp() == MassenverarbeitungQueueTyp.IMPFUNG_EXTERALIZE) {
						LOG.debug("VACME-MASSENVERARBEITUNG: Starting to Externalize Impfung {}", queueItem.getImpfungId());
						success  = massenmutationService.externalizeImpfungAndRemoveFromVmdl(queueItem);
					}
					if (queueItem.getTyp() == MassenverarbeitungQueueTyp.IMPFUNG_ODI_MOVE) {
						LOG.debug("VACME-MASSENVERARBEITUNG: Starting to move Impfung to another Odi {}", queueItem.getImpfungId());
						success  = massenmutationService.moveImpfungToOdi(queueItem);
					}
					if (queueItem.getTyp() == MassenverarbeitungQueueTyp.REGISTRIERUNG_DELETE) {
						LOG.debug("VACME-MASSENVERARBEITUNG: Starting to delete Registrierungen {}", queueItem.getRegistrierungNummer());
						success  = massenmutationService.deleteRegistrierung(queueItem);
					}
					if (queueItem.getTyp() == MassenverarbeitungQueueTyp.ENSURE_IMPFDOSSIER_PRESENT) {
						LOG.debug("VACME-MASSENVERARBEITUNG: Starting to create Impfdossier for Registrierunge {}", queueItem.getRegistrierungNummer());
						success  = massenmutationService.createCovidImpfossierForRegistrierung(queueItem);
					}
					if (queueItem.getTyp() == MassenverarbeitungQueueTyp.ODI_LAT_LNG_CALCULATE) {
						LOG.debug("VACME-MASSENVERARBEITUNG: Starting to calculate LatLng for Odi {}", queueItem.getOdiId());
						success = massenmutationService.calculateOdiLatLng(queueItem);
					}
					if (queueItem.getTyp() == MassenverarbeitungQueueTyp.IMPFUNG_LOESCHEN) {
						LOG.debug("VACME-MASSENVERARBEITUNG: Starting to delete Impfung {}", queueItem.getImpfungId());
						success = massenmutationService.loeschenImpfungInVacmeAndVmdlAndRevokeZertifikat(queueItem);
					}
					if (queueItem.getTyp() == MassenverarbeitungQueueTyp.IMPFGRUPPE_FREIGEBEN) {
						LOG.debug("VACME-MASSENVERARBEITUNG: Starting to set dossierStatus to Freigegeben for  Registrierungen with "
							+ "unlocked Prioritaet groups {}", queueItem.getImpfdossierId());
						success = massenmutationService.impfgruppeFreigeben(queueItem);
					}

					if (success) {
						successCounter++;
					}
				} finally {
					totalCounter++;
				}
			}
		} finally {
			stopWatch.stop();
			LOG.info(
				"VACME-MASSENVERARBEITUNG: Massenverarbeitung beendet. Es wurden {} Items von total {} Items"
					+ " in {}ms berechnet. {} ms/stk",
				successCounter, currentQueueItems.size(), stopWatch.getTime(TimeUnit.MILLISECONDS),
				calculateGenerationSpeed(totalCounter,
					stopWatch.getTime(TimeUnit.MILLISECONDS)));
		}
		return successCounter;
	}

}
