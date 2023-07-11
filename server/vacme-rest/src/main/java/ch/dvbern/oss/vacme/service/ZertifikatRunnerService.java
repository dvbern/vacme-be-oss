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

package ch.dvbern.oss.vacme.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.ZertifikatCreationDTO;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifikatQueue;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertBatchType;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.errors.NoTokenFailureException;
import ch.dvbern.oss.vacme.shared.errors.PlzMappingException;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Transactional(TxType.NOT_SUPPORTED)
public class ZertifikatRunnerService {

	private final ZertifikatService zertifikatService;
	private final ConfirmationService confirmationService;
	private final RegistrierungService registrierungService;
	private final VacmeSettingsService vacmeSettingsService;
	private final ApplicationPropertyService applicationPropertyService;
	private final ImpfinformationenService impfinformationenService;
	private final ObjectMapper mapper;


	@Transactional(TxType.NOT_SUPPORTED)
	public boolean createCovidCertAndNotify(@NonNull String regNum, @NonNull ID<Impfung> impfungId, @NonNull CovidCertBatchType batchType) {
		return createZertifikatForRegistrierung(regNum, impfungId, batchType).success;
	}

	@Transactional(TxType.SUPPORTS)
	public ZertifikatResultDto createZertifikatForRegistrierung(@NonNull String regNum, @NonNull ID<Impfung> impfungId, @NonNull CovidCertBatchType batchType) {
		try {
			// validate and create Zertifikat in own transaction
			Zertifikat zertifikat = zertifikatService.validateAndTriggerCertificateCreation(regNum, impfungId, batchType);
			LOG.info("VACME-ZERTIFIKAT: Created Zertifikat {} for {}", zertifikat.getUvci(), regNum);

			// send  sms in own transaction
			Registrierung registrierung = this.registrierungService.findRegistrierung(regNum);
			confirmationService.sendZertifikatsbenachrichtigung(registrierung, zertifikat, batchType);
			return new ZertifikatResultDto(true);
		} catch (AppValidationException ex) {
			return new ZertifikatResultDto(ex);
		} catch (NoTokenFailureException ex) {
			LOG.error("VACME-ZERTIFIKAT: Found no valid token for certificate generation. Aborting job");
			throw ex; // rethrow to cancel the batch job!
		} catch (Exception exception) {
			LOG.error("VACME-ZERTIFIKAT: Error during Zertifikat creation for {} ",
				regNum, exception);
			return new ZertifikatResultDto(exception);
		}
	}

	public static class ZertifikatResultDto {
		public boolean success;
		@Nullable
		public Exception exception;

		public ZertifikatResultDto(boolean success) {
			this.success = success;
		}

		public ZertifikatResultDto(Exception exception) {
			this.exception = exception;
			this.success = false;
		}
	}

	@Transactional(TxType.SUPPORTS)
	public void createZertifikatForRegistrierung(
		@NonNull Registrierung registrierung,
		@NonNull ID<Impfung> impfungId,
		@NonNull CovidCertBatchType recrationType
	) throws Exception {
		if (recrationType.isPost()) {
			checkCanSendZertifikatPerPost(registrierung);
		}
		LOG.info("VACME-ZERTIFIKAT: Neues Zertifikat erstellt mit typ {} fuer Registrierung {}",
			recrationType,
			registrierung.getRegistrierungsnummer());

		// Da in diesem Fall keine Anpassungen gemacht wurden, ist keine Stornierung notwendig
		// damit das generateZertifkat Flag zur Sicherheit true ist falls die Generierung schief laeuft
		zertifikatService.markForRegenerationNewTransaction(impfungId);
		ZertifikatResultDto result = this.createZertifikatForRegistrierung(
			registrierung.getRegistrierungsnummer(),
			impfungId,
			recrationType);
		if (!result.success) {
			Exception exception = result.exception != null ? result.exception :
				AppValidationMessage.ZERTIFIKAT_GENERIERUNG_FEHLER.create();
			LOG.error("VACME-ZERTIFIKAT: Fehler beim erstellen von Zertifikaten bei erzwungenem Postversand fuer {}",
				registrierung.getRegistrierungsnummer(), exception);
			throw exception;
		}
	}

	private void checkCanSendZertifikatPerPost(@NonNull Registrierung registrierung) {
		try {
			ValidationUtil.validateAndNormalizePlz(registrierung.getAdresse().getPlz());
		} catch (PlzMappingException e) {
			throw AppValidationMessage.ZERTIFIKAT_POST_PLZ_FEHLER.create(registrierung.getAdresse().getPlz());
		}
	}

	public boolean revokeZertifikateForRegistrierung(@NonNull String registrierungsNummer, @NonNull CovidCertBatchType batchType) {
		boolean success = true;
		// Diese Methode schaut nicht darauf, was in der Queue ist, sondern storniert alle vorhandenen Zertifikate der Reg
		final List<Zertifikat> zertifikateToRevoke = zertifikatService.getAllNonRevokedZertifikate(registrierungsNummer);
		for (Zertifikat zertifikat : zertifikateToRevoke) {
			success = success && revokeZertifikatAndNotify(zertifikat.toId(), batchType);
		}
		return success;
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public boolean revokeZertifikatFromQueue(@NonNull ID<ZertifikatQueue> queueItemId, @NonNull CovidCertBatchType batchType) {
		final ZertifikatQueue queueItem = zertifikatService.getZertifikatQueueItem(queueItemId);
		try {
			if (!queueItem.needsToRevoke()) {
				LOG.warn("VACME-ZERTIFIKAT: Zertifikat {} is not ready to be revoked. Maybe it was already processed?", queueItem.getId());
				return true;
			}
			if (queueItem.isAlreadyRevoked()) {
				LOG.warn("VACME-ZERTIFIKAT: Zertifikat {} is already revoked.", queueItem.getId());
				// QueueItem als erledigt markieren
				queueItem.markSuccessful();
				return true;
			}
			Objects.requireNonNull(queueItem.getZertifikatToRevoke());
			revokeZertifikatAndNotify(queueItem.getZertifikatToRevoke().toId(), batchType);
			queueItem.markSuccessful();
			return true;
		} catch (NoTokenFailureException ex) {
			LOG.error("VACME-ZERTIFIKAT: Found no valid token for certificate revocation. Aborting job");
			throw ex; // rethrow to cancel job
		} catch (Exception exception) {
			LOG.error("VACME-ZERTIFIKAT: Error during Zertifikat revocation for {} ",
				queueItem.getId(), exception);
			queueItem.markFailed(exception.getMessage());
			return false;
		} finally {
			zertifikatService.updateZertifikatQueue(queueItem);
		}
	}

	/**
	 * revoziert ein einzelnes zertifikat ueber das api und informiert den Benutzer per POST oder SMS ueber die
	 * Revozierung
	 *
	 * @param zertifikatId id des Zertifikates welches revoziert wird
	 * @param batchType post oder online
	 * @return true wenn erfolgreich, sonst false
	 */
	@Transactional(TxType.SUPPORTS)
	private boolean revokeZertifikatAndNotify(@NonNull ID<Zertifikat> zertifikatId, @NonNull CovidCertBatchType batchType) {
		final Zertifikat zertifikatToRevoke = zertifikatService.getZertifikatById(zertifikatId);
		if (zertifikatToRevoke.getRevoked()) {
			LOG.warn("VACME-ZERTIFIKAT: Zertifikat {} is already revoked.", zertifikatToRevoke.getId());
			return true;
		}
		boolean revoked = zertifikatService.triggerCertificateRevocation(zertifikatToRevoke.toId());

		// send  sms in own transaction
		if (revoked) {
			Objects.requireNonNull(zertifikatToRevoke);
			final Registrierung registrierung = zertifikatToRevoke.getRegistrierung();
			Objects.requireNonNull(registrierung);
			LOG.info("VACME-ZERTIFIKAT: Revoked Zertifikate for {}", registrierung.getRegistrierungsnummer());
			// send  sms in own transaction
			confirmationService.sendZertifikatRevocationBenachrichtigung(zertifikatToRevoke, batchType);
		}
		return revoked;
	}



	/**
	 * actual logic to run the batchjob
	 *
	 * @param batchType specify which Registrations we are processing
	 */
	public void generateBatchOfCovidCertificates(@NonNull CovidCertBatchType batchType) {
		if (isAlreadyInProgress(batchType)) {
			LOG.info("VACME-ZERTIFIKAT: ({}) Batchjob already in progress, aborting.", batchType);
			return;
		}
		aquireBatchJobLock(batchType);
		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;
		List<ZertifikatCreationDTO> zertifikatGenerationDTOs = new ArrayList<>();
		try {
		zertifikatGenerationDTOs = zertifikatService.findImpfungenForZertifikatsGeneration(batchType, vacmeSettingsService.getCovidApiBatchSize());

			LOG.info("VACME-ZERTIFIKAT: ({}) Starting to generate a batch of up to {} CovidCertificates. Found {}",
				batchType, vacmeSettingsService.getCovidApiBatchSize(), zertifikatGenerationDTOs.size());

			for (ZertifikatCreationDTO dto : zertifikatGenerationDTOs) {

				try {
					if (!zertifikatService.hasValidToken()) {
						LOG.info("VACME-ZERTIFIKAT: ({}) Es gibt kein gueltiges Token fuer die Zertifikat Erstellung. "
							+ "Breche ab.", batchType);
						return;
					}
					Registrierung registrierung = this.registrierungService.findRegistrierung(dto.getRegistrierungsnummer());
					final ID<Impfung> idImpfung = Impfung.toId(dto.getImpfungId());
					final Impfung impfung = impfinformationenService.getImpfungById(idImpfung);


					if (!impfung.isGenerateZertifikat()) {
						LOG.warn("VACME-ZERTIFIKAT: ({}) Registration {} did not have generateZertifikat = true on Impfung {}. Maybe it was already processed?",
							batchType, registrierung.getRegistrierungsnummer(), dto.getImpfungId());
						continue;
					}

					LOG.info("VACME-ZERTIFIKAT: ({}) Erstelle Zertifikat fuer {}, impfung {}", batchType, dto.getRegistrierungsnummer(), dto.getImpfungId());
					boolean success = createCovidCertAndNotify(dto.getRegistrierungsnummer(), idImpfung, batchType);
					if (success) {
						successCounter++;
					}
					sleepForAWhile();
				} finally {
					totalCounter++;
				}
			}
		} finally {
			stopWatch.stop();
			LOG.info(
				"VACME-ZERTIFIKAT: ({}) Zertifikaterstellung beendet. Es wurden {} Zertifikate von total {} Impfungen in {}s generiert. {} ms/stk",
				batchType, successCounter, zertifikatGenerationDTOs.size(), stopWatch.getTime(TimeUnit.SECONDS), calculateCertSpeed(totalCounter,
					stopWatch.getTime()));
			releaseBatchJobLock(batchType);
		}
	}


	/**
	 * Method triggered by scheduled job that searches the queue for certs to trigger a revocation for
	 *
	 * @param batchType denotes if we are processing online regs or regs that need to be notified via postal service
	 */
	public void revokeBatchOfCovidCertificates(@NonNull CovidCertBatchType batchType) {
		if (isAlreadyInProgress(batchType)) {
			LOG.info("VACME-ZERTIFIKAT: ({}) Batchjob already in progress, aborting.", batchType);
			return;
		}
		aquireBatchJobLock(batchType);
		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;

		List<UUID> queueItemIds = zertifikatService.findZertifikateForZertifikatsRevocation(batchType, vacmeSettingsService.getCovidApiBatchSize());

		LOG.info("VACME-ZERTIFIKAT: ({}) Starting to revoke a batch of up to {} CovidCertificates. Found {}",
			batchType, vacmeSettingsService.getCovidApiBatchSize(), queueItemIds.size());

		try {
			for (UUID queueId : queueItemIds) {
				try {
					if (!zertifikatService.hasValidToken()) {
						LOG.info("VACME-ZERTIFIKAT: ({}) Es gibt kein gueltiges Token fuer die Zertifikat Erstellung. "
							+ "Breche ab.", batchType);
						return;
					}
					LOG.info("VACME-ZERTIFIKAT: ({}) Storniere Zertifikat fuer QueueItem {}", batchType, queueId);
					boolean success = revokeZertifikatFromQueue(new ID<>(queueId, ZertifikatQueue.class), batchType);
					if (success) {
						successCounter++;
					}
					sleepForAWhile();
				} finally {
					totalCounter++;
				}
			}
		} finally {
			stopWatch.stop();
			LOG.info(
				"VACME-ZERTIFIKAT: ({}) Zertifikatstornierung beendet. Es wurden {} Zertifikate von total {} QueueItems in {}s storniert. {} ms/stk",
				batchType, successCounter, queueItemIds.size(), stopWatch.getTime(TimeUnit.SECONDS), calculateCertSpeed(totalCounter, stopWatch.getTime()));
			releaseBatchJobLock(batchType);
		}
	}

	private void aquireBatchJobLock(@NonNull CovidCertBatchType batchType) {
		ApplicationPropertyKey lockKey = CovidCertBatchType.mapBatchTypeToLockKey(batchType);
		this.applicationPropertyService.aquireBatchJobLock(lockKey);
	}

	private void releaseBatchJobLock(@NonNull CovidCertBatchType batchType) {
		ApplicationPropertyKey lockKey = CovidCertBatchType.mapBatchTypeToLockKey(batchType);
		this.applicationPropertyService.releaseBatchJobLock(lockKey);
	}

	private boolean isAlreadyInProgress(@NonNull CovidCertBatchType batchType) {
		ApplicationPropertyKey applicationPropertyKey = CovidCertBatchType.mapBatchTypeToLockKey(batchType);
		return applicationPropertyService.isBatchJobAlreadyInProgress(applicationPropertyKey);
	}

	private String calculateCertSpeed(int processed, long time) {
		if (processed == 0) {
			return "0";
		}
		return String.format("%.2f", (float) time / processed);
	}

	private void sleepForAWhile() {
		try {
			Thread.sleep(vacmeSettingsService.getCovidApiSleeptime());
		} catch (InterruptedException e) {
			LOG.error("Thread sleep was interrupted", e);
		}
	}

	public void clearCovidCertTokens() {
		this.zertifikatService.clearExpiredCovidCertTokens();
	}

	@Nullable
	public LocalDateTime getTimestampOfLastPostversand(@NonNull Impfdossier impfdossier) {
		if (!impfdossier.abgeschlossenMitVollstaendigemImpfschutz()) {
			return null;
		}
		final Optional<Zertifikat> optional = this.zertifikatService.getNewestNonRevokedZertifikat(impfdossier.getRegistrierung());
		if (optional.isPresent()) {
			try {
				final Zertifikat zertifikat = optional.get();
				JsonNode jsonNode = mapper.readTree(zertifikat.getPayload());
				final JsonNode address = jsonNode.get("address");
				if (address != null) {
					final JsonNode streetAndNr = address.get("streetAndNr");
					//noinspection VariableNotUsedInsideIf
					if (streetAndNr != null) {
						return zertifikat.getTimestampErstellt();
					}
				}
			} catch (JsonProcessingException e) {
				LOG.warn("Could not parse payload in Zertifikat");
				return null;
			}
		}
		// Kein Zertifikat oder keines mit Postversand
		return null;
	}

	/**
	 * Need to gracefully shutdown  that all locks have been
	 * released before datasource shutdown.
	 *
	 * @param event ignored
	 */
	void destroy(@Observes @BeforeDestroyed(ApplicationScoped.class) Object event) {
		LOG.info("Shutdown triggered, try to release locks");
		for (CovidCertBatchType type : CovidCertBatchType.values()) {
			try {
				if (isAlreadyInProgress(type)) {
					LOG.info("Gracefully release the locks for the batchjob in the db on shutdown {}", type);
					releaseBatchJobLock(type);
				}
			} catch (Exception e) {
				LOG.warn("Could not gracefully release locks for covic-cert api batchjob on shutdown {}, {}", e.getClass().getSimpleName(), e.getMessage());
			}
		}
	}
}
