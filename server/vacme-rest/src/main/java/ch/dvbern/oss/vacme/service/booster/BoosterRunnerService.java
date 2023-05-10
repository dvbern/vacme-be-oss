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

package ch.dvbern.oss.vacme.service.booster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import ch.dvbern.oss.vacme.entities.booster.RegistrierungQueue;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.repo.BoosterQueueRepo;
import ch.dvbern.oss.vacme.service.ApplicationPropertyService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioUtil;
import ch.dvbern.oss.vacme.shared.util.Constants;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMMUNISIERT;
import static ch.dvbern.oss.vacme.util.TimingUtil.calculateGenerationSpeed;

@ApplicationScoped
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoosterRunnerService {

	private final ApplicationPropertyService applicationPropertyService;
	private final BoosterService boosterService;
	private final BoosterQueueRepo boosterQueueRepo;
	private final ImpfdossierService impfdossierService;

	@ConfigProperty(name = "vacme.booster.move.immunisiert.batchsize", defaultValue = "1")
	long moveVollstGeimpfteToImmunisiertBatchSize = 1;

	@ConfigProperty(name = "vacme.booster.rule.engine.job.batchsize", defaultValue = "1")
	long vacmeBoosterRuleEngineJobBatchSize = 1;

	@ConfigProperty(name = "vacme.booster.move.freigegeben.batchsize", defaultValue = "1")
	long moveImmunisiertToFreigegebenBatchSize = 1;

	@ConfigProperty(name = "vacme.booster.rule.engine.job.partitions", defaultValue = "3")
	long numberOfPartitions = 3;

	@ConfigProperty(name = "vacme.freigabe.sms.sleeptime.ms", defaultValue = "50")
	long freigabeJobSMSSleepTime = 50;


	private long getBoosterRuleEngineJobBatchSize() {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.VACME_BOOSTER_RULE_ENGINE_JOB_BATCH_SIZE);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Long.parseLong(applicationProperty.getValue()))
			.orElseGet(() -> vacmeBoosterRuleEngineJobBatchSize);
	}

	private long getMoveVollstGeimpfteToImmunisiertBatchSize() {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.VACME_BOOSTER_STATUSMOVER_JOB_BATCH_SIZE);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Long.parseLong(applicationProperty.getValue()))
			.orElseGet(() -> moveVollstGeimpfteToImmunisiertBatchSize);
	}

	private long getMoveImmunisierteToFreigegebeneBoosterBatchSize() {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.VACME_BOOSTER_FREIGABE_JOB_BATCH_SIZE);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Long.parseLong(applicationProperty.getValue()))
			.orElseGet(() -> moveImmunisiertToFreigegebenBatchSize);
	}

	private long getFreigabeSMSSleepTime() {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.VACME_BOOSTER_FREIGABE_SMS_SLEEP_TIME_MS);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Long.parseLong(applicationProperty.getValue()))
			.orElseGet(() -> freigabeJobSMSSleepTime);
	}

	private long getNumberOfPartionsForBoosterRuleRecalculation() {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.VACME_BOOSTER_RULE_ENGINE_JOB_PARTITIONS);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Long.parseLong(applicationProperty.getValue()))
			.orElseGet(() -> numberOfPartitions);
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public List<String> performMoveOfAbgeschlosseneToImmunisiert() {

		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;
		List<String> regNumsToMove = new ArrayList<>();
		List<String> regNumsMoved = new ArrayList<>();
		try {
			regNumsToMove = boosterService.findRegsToMoveToImmunisiert(getMoveVollstGeimpfteToImmunisiertBatchSize());

			LOG.info("VACME-BOOSTER-IMMUNISIERT: Starting to move Status of a batch of up to {} Registrierungen. Found {}",
				getMoveVollstGeimpfteToImmunisiertBatchSize(), regNumsToMove.size());

			for (String regNum : regNumsToMove) {
				try {
					// TODO Affenpocken: Falls jemals andere Krankheiten nicht im Booster-Status beginnen, hier das find anpassen
					final Impfdossier impfdossier =
						this.impfdossierService.findImpfdossierForRegnumAndKrankheit(regNum,
							KrankheitIdentifier.COVID);
					if (!(impfdossier.abgeschlossenMitCorona() || impfdossier.abgeschlossenMitVollstaendigemImpfschutz())) {
						LOG.warn("VACME-BOOSTER-IMMUNISIERT: Registration {} in Status {} and vollstaendigerImpfschutzTyp {} does not meet criteria for moving. Maybe it was already processed?",
							impfdossier.getRegistrierung().getRegistrierungsnummer(),
							impfdossier.getDossierStatus(),
							impfdossier.getVollstaendigerImpfschutzTyp());
						continue;
					}

					LOG.debug("VACME-BOOSTER-IMMUNISIERT: Verschiebe Status fuer {} auf {} von {}",  regNum,
						IMMUNISIERT, impfdossier.getDossierStatus());
					boolean success = boosterService.moveStatusToImmunisiertForRegistrierung(regNum);
					if (success) {
						successCounter++;
						regNumsMoved.add(regNum);
					}
				} finally {
					totalCounter++;
				}
			}

		} finally {
			stopWatch.stop();
			LOG.info(
				"VACME-BOOSTER-IMMUNISIERT: Statusverschiebung beendet. Es wurden {} Regs von total {} Registrierungen in {}ms verschoben. {} ms/stk",
				successCounter, regNumsToMove.size(), stopWatch.getTime(TimeUnit.MILLISECONDS), calculateGenerationSpeed(totalCounter,
					stopWatch.getTime(TimeUnit.MILLISECONDS)));
		}
		return regNumsMoved;
	}

	public void performMoveOfImmunisiertToFreigegebenBooster() {
		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;
		List<UUID> dossierIdsToMove = new ArrayList<>();
		try {
			dossierIdsToMove = boosterService.findDossiersToMoveToFreigegebenBooster(getMoveImmunisierteToFreigegebeneBoosterBatchSize());
			long smsSleepTime =  getFreigabeSMSSleepTime();
			LOG.debug("VACME-BOOSTER-FREIGABEMOVE: Starting to move Status of a batch of up to {} Dossiers. Found {}",
				getMoveVollstGeimpfteToImmunisiertBatchSize(), dossierIdsToMove.size());

			for (UUID dossierId : dossierIdsToMove) {
				try {
					final Impfdossier impfdossier = impfdossierService.getImpfdossierById(Impfdossier.toId(dossierId));

					Registrierung registrierung = impfdossier.getRegistrierung();
					Impfschutz impfschutz = impfdossier.getImpfschutz();
					if (!BoosterPrioUtil.meetsCriteriaForFreigabeBooster(impfdossier, impfschutz)) {
						LOG.warn("VACME-BOOSTER-FREIGABEMOVE: Registration {} in Status {}  does not meet criteria for freigabe. Maybe it was already processed?",
							registrierung.getRegistrierungsnummer(), impfdossier.getDossierStatus());
						continue;
					}

					LOG.debug("VACME-BOOSTER-FREIGABEMOVE: Verschiebe Status fuer {} auf {} von {}",  dossierId, FREIGEGEBEN_BOOSTER, impfdossier.getDossierStatus());
					boolean success = boosterService.moveStatusToFreigegebenAndSendBenachrichtigung(impfdossier);

					// SMS-Bremse bei den Online-Regs
					if (impfdossier.getRegistrierung().getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
						sleepForAWhile(smsSleepTime);
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
			if (dossierIdsToMove.size() > 0 || stopWatch.getTime(TimeUnit.MILLISECONDS) > Long.parseLong(Constants.DB_QUERY_SLOW_THRESHOLD)) {
				LOG.info(
					"VACME-BOOSTER-FREIGABEMOVE: Statusverschiebung beendet. Es wurden {} Dossiers von total {} "
						+ "Dossiers in {}ms verschoben. {} ms/stk",
					successCounter, dossierIdsToMove.size(), stopWatch.getTime(TimeUnit.MILLISECONDS),
					calculateGenerationSpeed(totalCounter,
						stopWatch.getTime(TimeUnit.MILLISECONDS)));
			}
		}
	}

	private void sleepForAWhile(long sleepTimeMs) {
		try {
			Thread.sleep(sleepTimeMs);
		} catch (InterruptedException e) {
			LOG.error("Thread sleep was interrupted while sleeping " + sleepTimeMs, e);
		}
	}

	public int performImpfschutzCalculation(@NonNull List<RegistrierungQueue> currentQueueItems) {
		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;
		try {
			LOG.info("VACME-BOOSTER-RULE_ENGINE: Starting to calculate Impfschutz for {} Regs",
				currentQueueItems.size());

			for (RegistrierungQueue queueItem : currentQueueItems) {
				try {
					// ich glaube wir koennen hier nichts checken weil wir fast immer neu berechnen koennen / wollen
					LOG.debug("VACME-BOOSTER-RULE_ENGINE: Berechne Impfschutz fuer Registrierung {}", queueItem.getRegistrierungNummer());
					boolean success = boosterService.calculateImpfschutz(queueItem, queueItem.getKrankheitIdentifier());
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
				"VACME-BOOSTER-RULE_ENGINE: Impfschutzberechnung beendet. Es wurden {} Regs von total {} Registrierungen"
					+ " in {}ms berechnet. {} ms/stk",
				successCounter, currentQueueItems.size(), stopWatch.getTime(TimeUnit.MILLISECONDS),
				calculateGenerationSpeed(totalCounter,
					stopWatch.getTime(TimeUnit.MILLISECONDS)));
		}
		return successCounter;
	}

	public void performImpfschutzCalculationByQueue() {
		List<RegistrierungQueue> queueEntries = boosterQueueRepo.findRegsToRecalculateImpfschutzFromQueue(getBoosterRuleEngineJobBatchSize());
		// if we are not using multiple workpartitions do not bother starting extra worker-threads
		long numberOfPartitions = this.getNumberOfPartionsForBoosterRuleRecalculation();
		if (numberOfPartitions == 1 || numberOfPartitions == 0) {
			// calculation is synchronous on one thread
			performImpfschutzCalculation(queueEntries);
			return;
		}

		// async, calculation will be partitioned into multiple workloads and performed by multiple threads
		StopWatch stopWatch = StopWatch.createStarted();
		LOG.info(
			"VACME-BOOSTER-RULE_ENGINE: Starting to recalculate Booster Rules for queued Registrierungen. Task will be split into {} partitions",
			numberOfPartitions);
		Map<Long, List<RegistrierungQueue>> partitionMap = queueEntries.stream()
			.collect(Collectors.groupingBy(entry -> entry.getId() % numberOfPartitions));

		List<CompletableFuture<Integer>> recalculationTaks = new ArrayList<>();
		for (List<RegistrierungQueue> currentQueueItems : partitionMap.values()) {

			// potentielles Improvement: hier die verschiedenen Partitionen
			// per expliziten webservicecall ueber ip auf quarkus auf mehrere nodes verteilen

			CompletableFuture<Integer> recalculatePartitionTask = Uni.createFrom().item(currentQueueItems)
				.emitOn(Infrastructure.getDefaultWorkerPool())
				.onItem().transform(this::performImpfschutzCalculation) // actual calculation
				.subscribe().asCompletionStage();

			recalculationTaks.add(recalculatePartitionTask);
		}

		Integer totalProcessedSuccess = 0;

		for (CompletableFuture<Integer> integerUni : recalculationTaks) {
			try {
				Integer successfullCalculationsInTask = integerUni.get(3, TimeUnit.MINUTES); // wait max 3 Minutes
				totalProcessedSuccess += successfullCalculationsInTask;
			} catch (InterruptedException e) {
				LOG.error("VACME-BOOSTER-RULE_ENGINE: Thread was interrupted while processing {} queueEntries ",
					queueEntries.size(), e);
			} catch (ExecutionException e) {
				LOG.error("VACME-BOOSTER-RULE_ENGINE: An Exception escaped while processing {} queueEntries for "
					+ "recalculation", queueEntries.size(), e);
			} catch (TimeoutException e) {
				LOG.error("VACME-BOOSTER-RULE_ENGINE: Waiting for the calculation result of the {} qued entries took "
					+ "to long", queueEntries.size());
			}
		}
		LOG.info("VACME-BOOSTER-RULE_ENGINE: Processed {} of {} queueItems successfully in {} partitions. Total time {}ms",
			totalProcessedSuccess, queueEntries.size(), numberOfPartitions, stopWatch.getTime(TimeUnit.MILLISECONDS));
	}
}
