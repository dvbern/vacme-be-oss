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

package ch.dvbern.oss.vacme.scheduler;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.security.RunAs;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.core.StreamingOutput;

import ch.dvbern.oss.vacme.entities.base.ApplicationProperty;
import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueStatus;
import ch.dvbern.oss.vacme.entities.documentqueue.IDocumentQueueService;
import ch.dvbern.oss.vacme.entities.documentqueue.SpracheParamJax;
import ch.dvbern.oss.vacme.entities.documentqueue.VonBisSpracheParamJax;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungErwachsenDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungKindDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungZHDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.AbrechnungZHKindDocQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueue;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueueResult;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.SpracheabhDocQueue;
import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.embeddables.FileBlob;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.impfen.Krankheit;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueueTyp;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.jax.applicationhealth.ResultDTO;
import ch.dvbern.oss.vacme.jax.registration.LatLngJax;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadBaseJax;
import ch.dvbern.oss.vacme.repo.ApplicationHealthRepo;
import ch.dvbern.oss.vacme.repo.ApplicationPropertyRepo;
import ch.dvbern.oss.vacme.repo.BoosterQueueRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierFileRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.KrankheitRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungFileRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.reports.abrechnung.ReportAbrechnungServiceBean;
import ch.dvbern.oss.vacme.reports.abrechnungZH.ReportAbrechnungZHServiceBean;
import ch.dvbern.oss.vacme.reports.reportingImpfungen.ReportingImpfungenReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingKantonKantonsarzt.ReportingKantonReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingKantonKantonsarzt.ReportingKantonsarztReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingOdiImpfungenTerminbuchungen.ReportingOdiImpfungenReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingOdiImpfungenTerminbuchungen.ReportingOdiTerminbuchungenReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingOdis.ReportingOdisReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingTerminslots.ReportingTerminslotsReportServiceBean;
import ch.dvbern.oss.vacme.service.ApplicationPropertyService;
import ch.dvbern.oss.vacme.service.CheckFreieZweittermineService;
import ch.dvbern.oss.vacme.service.GeocodeService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.ImpfslotService;
import ch.dvbern.oss.vacme.service.KrankheitService;
import ch.dvbern.oss.vacme.service.MailService;
import ch.dvbern.oss.vacme.service.OrtDerImpfungService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.SettingsService;
import ch.dvbern.oss.vacme.service.StatsService;
import ch.dvbern.oss.vacme.service.SystemAdministrationService;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.service.ZertifikatRunnerService;
import ch.dvbern.oss.vacme.service.benutzer.BenutzerMassenmutationRunnerService;
import ch.dvbern.oss.vacme.service.booster.BoosterRunnerService;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertBatchType;
import ch.dvbern.oss.vacme.service.d3api.ArchivierungService;
import ch.dvbern.oss.vacme.service.documentqueue.DocumentQueueRunnerService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.service.massenmutation.MassenverarbeitungRunnerService;
import ch.dvbern.oss.vacme.service.onboarding.OnboardingBatchType;
import ch.dvbern.oss.vacme.service.onboarding.OnboardingRunnerService;
import ch.dvbern.oss.vacme.service.vmdl.VMDLServiceAbstract;
import ch.dvbern.oss.vacme.service.vmdl.VMDLServiceFactory;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_ASYNC_DOCUMENT_CREATION_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_FREIGABE_JOB_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_RULE_ENGINE_JOB_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_STATUSMOVER_JOB_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_COVIDAPI_ONLINE_PS_BATCHJOB_LOCK;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_COVIDAPI_POST_PS_BATCHJOB_LOCK;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_COVIDAPI_REVOCATION_ONLINE_PS_BATCHJOB_LOCK;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_COVIDAPI_REVOCATION_POST_PS_BATCHJOB_LOCK;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_COVID_CERT_BATCHJOB_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_COVID_CERT_BATCHJOB_POST_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_COVID_CERT_BATCHJOB_REVOC_ONLINE_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_COVID_CERT_BATCHJOB_REVOC_POST_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_CRON_HAS_IMPFUNG_FOR_KANTON_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_DEACTIVATE_UNUSED_USERACCOUNTS_JOB_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_ONBOARDING_BRIEF_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_REGISTRIERUNG_AUTO_ABSCHLIESSEN_JOB_DISABLED;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.SYSTEM_INTERNAL_ADMIN;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@RunAs(SYSTEM_INTERNAL_ADMIN)
public class SystemAdminRunnerService {

	public static final int TIMESPAN_AUTOMATIC_UNLOCK_BATCH_JOB_MINUTES = 60;

	private final StatsService statsService;
	private final CurrentIdentityAssociation association;
	private final ApplicationHealthRepo applicationHealthRepo;
	private final MailService mailService;
	private final VMDLServiceFactory vmdlServiceFactory;
	private final RegistrierungFileRepo registrierungFileRepo;
	private final ImpfdossierFileRepo impfdossierFileRepo;
	private final ImpfterminRepo impfterminRepo;
	private final RegistrierungRepo registrierungRepo;
	private final ArchivierungService archivierungService;
	private final CheckFreieZweittermineService checkFreieZweittermineService;
	private final OrtDerImpfungService ortDerImpfungService;
	private final SystemAdministrationService systemAdministrationService;
	private final ImpfslotService impfslotService;
	private final ApplicationPropertyRepo applicationPropertyRepo;
	private final ZertifikatRunnerService zertifikatRunnerService;
	private final OnboardingRunnerService onboardingRunnerService;
	private final BoosterRunnerService boosterRunnerService;
	private final BoosterQueueRepo boosterQueueRepo;
	private final RegistrierungService registrierungService;
	private final ReportAbrechnungServiceBean abrechnungServiceBean;
	private final ReportingImpfungenReportServiceBean reportingImpfungenReportServiceBean;
	private final ReportingTerminslotsReportServiceBean reportingTerminslotsReportServiceBean;
	private final ReportAbrechnungZHServiceBean abrechnungZHServiceBean;
	private final ReportingKantonReportServiceBean reportingKantonReportServiceBean;
	private final ReportingKantonsarztReportServiceBean reportingKantonsarztReportServiceBean;
	private final ReportingOdiImpfungenReportServiceBean reportingOdiImpfungenReportServiceBean;
	private final ReportingOdiTerminbuchungenReportServiceBean reportingOdiTerminbuchungenReportServiceBean;
	private final ReportingOdisReportServiceBean reportingOdisReportServiceBean;
	private final IDocumentQueueService documentQueueService;
	private final ObjectMapper objectMapper;
	private final DocumentQueueRunnerService documentqueueRunnerService;
	private final MassenverarbeitungRunnerService massenmutationRunnerService;
	private final BenutzerMassenmutationRunnerService benutzerMassenmutationRunnerService;
	private final GeocodeService geocodingService;
	private final SettingsService settingsService;
	private final ImpfinformationenService impfinformationenService;
	private final ImpfdossierService impfdossierService;
	private final ApplicationPropertyService applicationPropertyService;
	private final VacmeSettingsService vacmeSettingsService;
	private final KrankheitRepo krankheitRepo;
	private final KrankheitService krankheitService;

	@Transactional
	void runStatTask() {
		// Task soll asl Systembenutzer laufen
		runAsInternalSystemAdmin();

		statsService.takeKennzahlenSnapshot();
	}

	private void runAsInternalSystemAdmin() {
		// Task soll als Systembenutzer laufen
		Builder builder = new Builder();
		QuarkusSecurityIdentity internalAdmin =
			builder.addRole(SYSTEM_INTERNAL_ADMIN).setPrincipal(() -> SYSTEM_INTERNAL_ADMIN).build();
		association.setIdentity(internalAdmin);
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runDbValidationTask() {
		if (vacmeSettingsService.isCronDbValidationJobDisabled()) {
			LOG.info("VACME-HEALTH: Validation is disabled (vacme.cron.stat.dbvalidation.disabled)");
			return;
		}
		LOG.info("VACME-HEALTH: Starting Validation");
		boolean allSuccessful =
			// meldeResultat(applicationHealthRepo.runSleepQuery()) && todo reviewer of VACME-1933: comment in for testing
			meldeResultat(applicationHealthRepo.getHealthCheckInvalidImpfslots())
				&& meldeResultat(applicationHealthRepo.getHealthCheckVollstaendigerImpfschutzKeineImpfungen())
				&& meldeResultat(applicationHealthRepo.getHealthCheckDoppeltGeimpftOhneVollsaendigerImpfschutz())
				&& meldeResultat(applicationHealthRepo.getHealthCheckAbgeschlossenOhneVollsaendigerImpfschutz())
				&& meldeResultat(applicationHealthRepo.getHealthCheckNichtAbgeschlossenAberVollstaendigerImpfschutz())
				&& meldeResultat(applicationHealthRepo.getHealthCheckAbgeschlossenOhneCoronaAberVollstaendigerImpfschutz())
				&& meldeResultat(applicationHealthRepo.getHealthCheckFailedZertifikatRevocations())
				&& meldeResultat(applicationHealthRepo.getHealthCheckFailedZertifikatRecreations())
				&& meldeResultat(applicationHealthRepo.getHealthCheckGebuchteTermine())
				&& meldeResultat(applicationHealthRepo.getHealthCheckVerwaisteImpfungen())
				&& meldeResultat(applicationHealthRepo.getHealthCheckRegistrierungenMitImpfungNichtAmTermindatum())
				&& meldeResultat(applicationHealthRepo.getHealthCheckFalschVerknuepfteZertifikate());

		// Abschluss-Mail
		if (vacmeSettingsService.getMailAdmin() != null) {
			final String subject = "Health-Checks " + (allSuccessful ? "Everything OKAY" : "FAILED");
			mailService.sendTextMail(vacmeSettingsService.getMailAdmin(), subject, subject, false);
		}

		LOG.info("VACME-HEALTH: Validation finished with result {}", allSuccessful);
	}

	void runHealthCheckZertifikatJobLock() {
		runAsInternalSystemAdmin();
		ResultDTO resultDTO = new ResultDTO();
		resultDTO.start("Health-Check Zertifikat-Batchjobs");
		resultDTO.setSuccess(true);

		if (!isBatchJobDisabled(VACME_COVID_CERT_BATCHJOB_DISABLED)) {
			checkIfNotModified(VACME_COVIDAPI_ONLINE_PS_BATCHJOB_LOCK, resultDTO);
		}
		if (!isBatchJobDisabled(VACME_COVID_CERT_BATCHJOB_REVOC_ONLINE_DISABLED)) {
			checkIfNotModified(VACME_COVIDAPI_REVOCATION_ONLINE_PS_BATCHJOB_LOCK, resultDTO);
		}
		if (!isBatchJobDisabled(VACME_COVID_CERT_BATCHJOB_POST_DISABLED)) {
			checkIfNotModified(VACME_COVIDAPI_POST_PS_BATCHJOB_LOCK, resultDTO);
		}
		if (!isBatchJobDisabled(VACME_COVID_CERT_BATCHJOB_REVOC_POST_DISABLED)) {
			checkIfNotModified(VACME_COVIDAPI_REVOCATION_POST_PS_BATCHJOB_LOCK, resultDTO);
		}

		if (resultDTO.isSuccess()) {
			resultDTO.addInfo("All Batchjobs running!");
		}
		resultDTO.finish(resultDTO.isSuccess());
		meldeResultat(resultDTO);
	}

	public void runHealthCheckGeocoding() {

		if (!this.settingsService.getSettings().isGeocodingEnabled()) {
			LOG.info("VACME-HEALTH: Skipping Geocoding Health-Check because Geocoding is disabled");
			return;
		}

		ResultDTO resultDTO = new ResultDTO();
		resultDTO.start("Health-Check Geocoding");
		resultDTO.setSuccess(true);

		if (vacmeSettingsService.getGeocodeApiKeyOptional().isEmpty()) {
			resultDTO.setSuccess(false);
			resultDTO.addInfo("No ApiKey set for the Geocoding API, despite Geocoding being enabled in Settings!");
		} else {
			final Adresse adresse = new Adresse();
			adresse.setAdresse1("Nussbaumstrasse 21");
			adresse.setOrt("Bern");
			adresse.setPlz("3000");

			final LatLngJax result = geocodingService.geocodeAdresse(adresse);

			if (result.getLat() == null || result.getLng() == null) {
				resultDTO.setSuccess(false);
				resultDTO.addInfo("Geocoding failed");
			}
		}

		resultDTO.finish(resultDTO.isSuccess());
		meldeResultat(resultDTO);
	}

	private void checkIfNotModified(@NonNull ApplicationPropertyKey keyOfLockProperty, @NonNull ResultDTO resultDTO) {
		final ApplicationProperty property = applicationPropertyRepo.getByKey(keyOfLockProperty)
			.orElseThrow(() -> AppFailureException.entityNotFound(ApplicationProperty.class, keyOfLockProperty));
		final LocalDateTime lastModified = property.getTimestampMutiert();
		if (DateUtil.getMinutesBetween(lastModified, LocalDateTime.now())
			> TIMESPAN_AUTOMATIC_UNLOCK_BATCH_JOB_MINUTES) {
			resultDTO.addInfo(String.format(
				"Batchjob %s was locked for more than %d minutest and is now automatically unlocked.",
				keyOfLockProperty,
				TIMESPAN_AUTOMATIC_UNLOCK_BATCH_JOB_MINUTES));
			resultDTO.setSuccess(false);
			applicationPropertyService.save(property.getName(), Boolean.FALSE.toString());
		}
	}

	private boolean meldeResultat(@NonNull ResultDTO result) {
		if (!result.isSuccess()) {
			if (vacmeSettingsService.getMailAdmin() != null) {
				mailService.sendTextMail(
					vacmeSettingsService.getMailAdmin(),
					result.getTitle(),
					result.getInfo(),
					false);
			} else {
				LOG.warn("VACME-HEALTH: Mail not sent because there is no admin email set");
			}
		}
		return result.isSuccess();
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runVMDLUploadTask() {
		if (vacmeSettingsService.isVmdlCronDisabled()) {
			return;
		}
		// Task soll als Systembenutzer laufen
		runAsInternalSystemAdmin();

		runVMDLUploadTaskForAllKrankheiten();
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runVMDLUploadTaskForAllKrankheiten() {
		Arrays.stream(KrankheitIdentifier.values())
			.filter(KrankheitIdentifier::isSupportsVMDL)
			.forEach(k -> {
				VMDLServiceAbstract<? extends VMDLUploadBaseJax> vmdlService = vmdlServiceFactory.createVMDLService(k);
				vmdlService.doUploadVMDLDataForGenericBatch();
			});
	}

	public void runServiceReportingAnzahlErstimpfungenMailTask() {
		// Task laeuft am 1. Tag jedes Monats morgens um 1:
		// Wir melden alle Erstimpfungen des vergangenen Monats
		var von = LocalDate.now().minusMonths(1).with(firstDayOfMonth());
		var bis = von.with(TemporalAdjusters.lastDayOfMonth());
		for (Mandant mandant : Mandant.values()) {
			if (mandant.isActive()) {
				systemAdministrationService.runServiceReportingAnzahlErstimpfungenMailTask(mandant, von, bis);
			}
		}
	}

	public void runServiceReportingAnzahlZweitBoosterMailTask() {
		if (vacmeSettingsService.isReportingAnzahlZweitBoosterDisabled()) {
			return;
		}
		systemAdministrationService.runServiceReportingAnzahlZweitBoosterMailTask();
	}

	public void runServiceReportingAnzahlImpfungenMailTask() {
		if (vacmeSettingsService.isReportingAnzahlImpfungenDisabled()) {
			return;
		}
		systemAdministrationService.runServiceReportingAnzahlImpfungenMailTask();
	}

	public void setBenutzernameGesendetTimestamp(@NonNull Benutzer benutzer) {
		// Task soll asl Systembenutzer laufen, da wir in diesem Fall nicht eingeloggt sind
		runAsInternalSystemAdmin();

		benutzer.setBenutzernameGesendetTimestamp(LocalDateTime.now());
	}

	public void runImpfterminReservationResetTask() {
		impfterminRepo.abgelaufeneTerminReservationenAufheben();
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runCovidCertUploadTask() {
		// Wir machen zuerst alle Stornierungen. Dies damit das Stornierungs-SMS zuerst geschickt wird
		// und erst danach die Info ueber das neu erzeugte Zertifikat
		if (!isBatchJobDisabled(VACME_COVID_CERT_BATCHJOB_REVOC_ONLINE_DISABLED)) {
			// Task soll als Systembenutzer laufen
			runAsInternalSystemAdmin();
			zertifikatRunnerService.revokeBatchOfCovidCertificates(CovidCertBatchType.REVOCATION_ONLINE);
		}
		if (!isBatchJobDisabled(VACME_COVID_CERT_BATCHJOB_DISABLED)) {
			// Task soll als Systembenutzer laufen
			runAsInternalSystemAdmin();
			zertifikatRunnerService.generateBatchOfCovidCertificates(CovidCertBatchType.ONLINE);
		}
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runCovidCertUploadTaskNonOnline() {
		if (!isBatchJobDisabled(VACME_COVID_CERT_BATCHJOB_REVOC_POST_DISABLED)) {
			// Task soll asl Systembenutzer laufen
			runAsInternalSystemAdmin();
			zertifikatRunnerService.revokeBatchOfCovidCertificates(CovidCertBatchType.REVOCATION_POST);
		}
		if (!isBatchJobDisabled(VACME_COVID_CERT_BATCHJOB_POST_DISABLED)) {
			// Task soll asl Systembenutzer laufen
			runAsInternalSystemAdmin();
			zertifikatRunnerService.generateBatchOfCovidCertificates(CovidCertBatchType.POST);
		}
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runOnboardingLetterGenerationTask() {
		if (!isBatchJobDisabled(VACME_ONBOARDING_BRIEF_DISABLED)) {
			// Task soll als Systembenutzer laufen
			runAsInternalSystemAdmin();
			onboardingRunnerService.generateBatchOfOnboardingLetters(OnboardingBatchType.POST);
		}
	}

	@Transactional
	public void runCovidCertClearTokensTask() {
		zertifikatRunnerService.clearCovidCertTokens();
	}

	private boolean isBatchJobDisabled(@NonNull ApplicationPropertyKey keyToCheckIfEnabled) {
		return isBatchJobDisabled(keyToCheckIfEnabled, () -> false);
	}

	private boolean isBatchJobDisabled(
		@NonNull ApplicationPropertyKey keyToCheckIfEnabled,
		@NonNull Supplier<Boolean> defaultSupplier) {
		Optional<ApplicationProperty> disabledInDB = applicationPropertyRepo.getByKey(keyToCheckIfEnabled);
		return disabledInDB
			.map(applicationProperty -> Boolean.parseBoolean(applicationProperty.getValue()))
			.orElseGet(defaultSupplier);
	}

	public void runArchivierungD3() {
		if (vacmeSettingsService.isArchivierungD3Disabled()) {
			return;
		}
		runAsInternalSystemAdmin();
		archivierungService.archive();
	}

	public void runRegistrierungAutomatischAbschliessen() {
		if (isBatchJobDisabled(VACME_REGISTRIERUNG_AUTO_ABSCHLIESSEN_JOB_DISABLED)) {
			return;
		}
		runAsInternalSystemAdmin();

		LocalDateTime pastDate =
			LocalDate.now().minusDays(vacmeSettingsService.getAutomatischAbschliessenZeitDays()).atStartOfDay();
		List<Registrierung> regsToAutoclose = registrierungRepo.getErsteImpfungNoZweiteSince(pastDate);
		for (Registrierung registrierung : regsToAutoclose) {
			ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
				registrierung.getRegistrierungsnummer(),
				KrankheitIdentifier.COVID);

			infos.getImpfdossier().setStatusToAutomatischAbgeschlossen(infos);
			registrierungRepo.update(registrierung);
			// Der 2. Termin (wenn vorhanden) liegt in der Vergangenheit und wird explizit nicht friegeben damit
			// es noech moeglich ware eine statistische Auswertung ueber no-shows zu machen
		}
	}

	public void runCheckForZweittermine() {
		runAsInternalSystemAdmin();

		checkFreieZweittermineService.analyseFreieZweittermine();
	}

	public void runUpdateOdiNoTermin() {
		if (vacmeSettingsService.isCronUpdateNoFreieTermineProOdiDisabled()) {
			LOG.debug("Batchjob runUpdateOdiNoTermin is disabled");
			return;
		}
		LOG.debug("Batchjob runUpdateOdiNoTermin is enabled");
		runAsInternalSystemAdmin();
		StopWatch stopWatch = StopWatch.createStarted();
		ortDerImpfungService.updateOdiNoTermin();
		stopWatch.stop();
		if (stopWatch.getTime() > Constants.DB_QUERY_SLOW_THRESHOLD_LONG) {
			LOG.warn(
				"VACME-NO-FREIE-TERMINE: Update vom noFreieTermine1, noFreieTermine2 und noFreieTermineN flag im "
					+ "OrtDerImpfung took {}",
				stopWatch.getTime());
		}
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void runUpdateGlobalNoTermin() {
		if (vacmeSettingsService.isCronUpdateNoFreieTermineProKranhkeitDisabled()) {
			return;
		}
		runAsInternalSystemAdmin();
		// Das globale (pro krankheit) freieTermine betrachtet Termine1 ODER TermineN. Erst wenn weder noch vorhanden
		// sind, wird die Meldung angezeigt
		for (KrankheitIdentifier krankheit : KrankheitIdentifier.values()) {
			boolean noFreieTermine = !impfslotService.hasAtLeastFreieImpftermine(
				vacmeSettingsService.getCronUpdateNoFreieTermineProKrankheitMinTermine(),
				krankheit);
			Optional<Krankheit> optKrankheit = krankheitRepo.getByIdentifier(krankheit);
			if (optKrankheit.isPresent()) {
				final Krankheit krankheitEntity = optKrankheit.get();
				if (krankheitEntity.isNoFreieTermine() != noFreieTermine) {
					krankheitEntity.setNoFreieTermine(noFreieTermine);
					krankheitRepo.update(krankheitEntity);
				}
			}
		}
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void runUpdateHasAtleastOneImpfungViewableByKanton() {
		if (isBatchJobDisabled(VACME_CRON_HAS_IMPFUNG_FOR_KANTON_DISABLED)) {
			return;
		}

		runAsInternalSystemAdmin();
		for (KrankheitIdentifier krankheitIdentifier : KrankheitIdentifier.values()) {
			Krankheit krankheit = krankheitService.getByIdentifier(krankheitIdentifier);
			StopWatch stopWatch = StopWatch.createStarted();
			boolean getHasAtleastOneImpfungViewableByKanton = krankheitService.getHasAtleastOneImpfungViewableByKanton(
				krankheitIdentifier,
				KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG);
			stopWatch.stop();
			LOG.info(
				"HasAtleastOneImpfungViewableByKanton: Query ran in {}ms",
				stopWatch.getTime(TimeUnit.MILLISECONDS));
			if (krankheit.isHasAtleastOneImpfungViewableByKanton() != getHasAtleastOneImpfungViewableByKanton) {
				krankheit.setHasAtleastOneImpfungViewableByKanton(getHasAtleastOneImpfungViewableByKanton);
				krankheitRepo.update(krankheit);
			}
		}
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public boolean processDocumentQueueItem(Long queueItemId) {
		runAsInternalSystemAdmin();
		DocumentQueue documentQueueItem = documentQueueService.getDocumentQueueItem(queueItemId);
		if (documentQueueItem.getStatus() != DocumentQueueStatus.NEW && documentQueueItem.getStatus()
			!= DocumentQueueStatus.FAILED_RETRY) {
			LOG.info(
				"VACME-DOC-QUEUE: Queue Item mit id {} ist nicht im Status NEW oder FAILED_RETRY sondern im Status {}."
					+ " Vielleicht ist es schon in Bearbeitung",
				queueItemId,
				documentQueueItem.getStatus());
			return false;
		}
		DocumentQueueResult result = null;
		try {
			documentQueueService.markDocumentQueueItemAsInProgress(documentQueueItem);
			documentQueueItem = documentQueueService.getDocumentQueueItem(queueItemId);
			LOG.info("VACME-DOC-QUEUE: Starting Async generation of DocumentQueueItem {}, typ ({}) for user '{}'",
				documentQueueItem.getId(), documentQueueItem.getTyp(),
				documentQueueItem.getBenutzer().getBenutzername());

			byte[] content = triggerGenerationFunctionForDocQueueItem(documentQueueItem);

			storeDocumentQueueItemResultFile(documentQueueItem, content);
			result = documentQueueItem.getDocumentQueueResult();

			LOG.info("VACME-DOC-QUEUE: Finished Async generation of DocumentQueueItem {} in {}ms", documentQueueItem,
				documentQueueItem.calculateProcessingTimeMs());
			// trigger email success
			documentQueueItem.sendFinishedDocumentQueueJobSuccessMail(mailService, objectMapper);
			return true;
		} catch (Exception e) {
			String typ = documentQueueItem.getTyp().toString();
			LOG.error("VACME-DOC-QUEUE: Could not generate Document of type {}", typ, e);
			// trigger email failure
			String errMsgRootcause = ExceptionUtils.getRootCauseMessage(e);
			documentQueueItem.sendFinishedDocumentQueueJobFailureMail(mailService, objectMapper, errMsgRootcause);
			documentQueueItem.markFailed(StringUtils.abbreviate(errMsgRootcause, DBConst.DB_BEMERKUNGEN_MAX_LENGTH));

			return false;
		} finally {
			documentQueueService.saveResult(documentQueueItem, result);

		}
	}

	@Transactional(TxType.NOT_SUPPORTED)
	private byte[] triggerGenerationFunctionForDocQueueItem(DocumentQueue documentQueueItem) {
		switch (documentQueueItem.getTyp()) {

		case ABRECHNUNG:
			AbrechnungDocQueue abrechnungDocQueue = (AbrechnungDocQueue) documentQueueItem;
			VonBisSpracheParamJax vonBisParam = abrechnungDocQueue.getVonBisSpracheParam(objectMapper);
			Locale locale = vonBisParam.getSprache().getLocale();
			byte[] bytes = abrechnungServiceBean.generateExcelReportAbrechnung(locale, vonBisParam.getVon(),
				vonBisParam.getBis());
			return bytes;
		case ABRECHNUNG_ERWACHSEN:
			AbrechnungErwachsenDocQueue abrechnungErwachsenDocQueue = (AbrechnungErwachsenDocQueue) documentQueueItem;
			VonBisSpracheParamJax vonBisErwachsenParam =
				abrechnungErwachsenDocQueue.getVonBisSpracheParam(objectMapper);
			Locale localeErwachsen = vonBisErwachsenParam.getSprache().getLocale();
			return abrechnungServiceBean.generateExcelReportAbrechnungErwachsen(
				localeErwachsen,
				vonBisErwachsenParam.getVon(),
				vonBisErwachsenParam.getBis());
		case ABRECHNUNG_KIND:
			AbrechnungKindDocQueue abrechnungKindDocQueue = (AbrechnungKindDocQueue) documentQueueItem;
			VonBisSpracheParamJax vonBisKindParam = abrechnungKindDocQueue.getVonBisSpracheParam(objectMapper);
			Locale localeKind = vonBisKindParam.getSprache().getLocale();
			return abrechnungServiceBean.generateExcelReportAbrechnungKind(localeKind, vonBisKindParam.getVon(),
				vonBisKindParam.getBis());

		case ABRECHNUNG_ZH:
			AbrechnungZHDocQueue abrZhDocQuee = (AbrechnungZHDocQueue) documentQueueItem;
			VonBisSpracheParamJax vonBisParamAbrZhParam = abrZhDocQuee.getVonBisSpracheParam(objectMapper);

			return abrechnungZHServiceBean.generateExcelReportAbrechnung(
				vonBisParamAbrZhParam.getSprache().getLocale(),
				vonBisParamAbrZhParam.getVon(),
				vonBisParamAbrZhParam.getBis());
		case ABRECHNUNG_ZH_KIND:
			AbrechnungZHKindDocQueue abrZhKindDocQuee = (AbrechnungZHKindDocQueue) documentQueueItem;
			VonBisSpracheParamJax vonBisParamAbrZhKindParam = abrZhKindDocQuee.getVonBisSpracheParam(objectMapper);

			return abrechnungZHServiceBean.generateExcelReportAbrechnungKind(
				vonBisParamAbrZhKindParam.getSprache().getLocale(),
				vonBisParamAbrZhKindParam.getVon(),
				vonBisParamAbrZhKindParam.getBis());
		case IMPFUNGEN_REPORT_CSV:
			StreamingOutput streamingOutput = reportingImpfungenReportServiceBean.generateStatisticsExport();
			return streamingOutputToByteArray(streamingOutput);

		case IMPFSLOTS_REPORT_CSV:
			return streamingOutputToByteArray(reportingTerminslotsReportServiceBean.generateStatisticsExport());
		case REGISTRIERUNGEN_KANTON_CSV:
			return streamingOutputToByteArray(reportingKantonReportServiceBean.generateStatisticsExport());
		case REGISTRIERUNGEN_KANTONSARZT_CSV:
			return streamingOutputToByteArray(reportingKantonsarztReportServiceBean.generateStatisticsExport());
		case ODI_REPORT_CSV:
			return reportingOdisReportServiceBean.generateStatisticsExport();
		case ODI_IMPFUNGEN:
			SpracheabhDocQueue docQueueWithSprache = (SpracheabhDocQueue) documentQueueItem;
			SpracheParamJax spracheParam = docQueueWithSprache.getSpracheParam(objectMapper);
			return reportingOdiImpfungenReportServiceBean.generateExcelReportOdiImpfungen(
				spracheParam.getSprache().getLocale(), docQueueWithSprache.getBenutzer().toId());
		case ODI_TERMINBUCHUNGEN:
			SpracheabhDocQueue docQueueTerminbuchung = (SpracheabhDocQueue) documentQueueItem;
			SpracheParamJax sprachparamTerminbuchung = docQueueTerminbuchung.getSpracheParam(objectMapper);
			return reportingOdiTerminbuchungenReportServiceBean.generateExcelReportOdiTerminbuchungen(
				sprachparamTerminbuchung.getSprache().getLocale(),
				docQueueTerminbuchung.getBenutzer().toId()
			);
		default:
			throw new AppFailureException("Unhandeled Document Type " + documentQueueItem.getTyp());
		}

	}

	private byte[] streamingOutputToByteArray(StreamingOutput streamingOutput) {

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			streamingOutput.write(out);
			return out.toByteArray();
		} catch (Exception e) {
			throw new AppFailureException("problem writing to stream ", e);
		}
	}

	private void storeDocumentQueueItemResultFile(DocumentQueue documentQueueItem, byte[] content) {
		DocumentQueueResult documentQueueResult = new DocumentQueueResult();
		String filename = documentQueueItem.calculateFilename(objectMapper);

		CleanFileName cleanFileName = new CleanFileName(filename);
		FileBlob file = FileBlob.of(cleanFileName, MimeType.APPLICATION_OCTET_STREAM, content);
		documentQueueResult.setFileBlob(file);
		documentQueueItem.setDocumentQueueResult(documentQueueResult);
		documentQueueItem.markSuccessful();

	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runBoosterImmunisiertStatusImpfschutzService(boolean forcerun) {
		// Batch Job der nach Immunisiert schiebt
		if (!isBatchJobDisabled(VACME_BOOSTER_STATUSMOVER_JOB_DISABLED) || forcerun) {
			List<String> movedToImmunisiertRegnums;
			// Task soll als Systembenutzer laufen
			runAsInternalSystemAdmin();
			movedToImmunisiertRegnums = boosterRunnerService.performMoveOfAbgeschlosseneToImmunisiert();

			// Regs die nach Immunisiert geschoben wuren koennen evtl noch grad weiter.
			// Daher fuer diese Berechnung uber die Rule Engine triggern
			if (!movedToImmunisiertRegnums.isEmpty()) {
				this.boosterQueueRepo.createRegistrierungQueueItems(movedToImmunisiertRegnums);
				LOG.info(
					"VACME-BOOSTER-IMMUNISIERT: Einfuegen von {} QueueItems zur Impfschutzneuberechnung beendet",
					movedToImmunisiertRegnums.size());
			}
		}
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runBoosterFreigabeStatusService(boolean forcerun) {
		// Batch job der nach FreigegebenBooster schiebt
		if (!isBatchJobDisabled(VACME_BOOSTER_FREIGABE_JOB_DISABLED) || forcerun) {
			// Task soll als Systembenutzer laufen
			runAsInternalSystemAdmin();
			boosterRunnerService.performMoveOfImmunisiertToFreigegebenBooster();
		}
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runBoosterRuleRecalc(boolean forcerun) {
		if (!isBatchJobDisabled(VACME_BOOSTER_RULE_ENGINE_JOB_DISABLED) || forcerun) {
			runAsInternalSystemAdmin();
			boosterRunnerService.performImpfschutzCalculationByQueue();
		}
	}

	@Transactional
	public void runPriorityUpdateForGrowingChildren() {
		runAsInternalSystemAdmin();
		registrierungService.runPriorityUpdateForGrowingChildren();
	}

	@Transactional
	public void runAsyncDocumentCleanup() {
		documentQueueService.cleanupExpiredResults();
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void runAsyncDocCreation() {
		if (!isBatchJobDisabled(VACME_ASYNC_DOCUMENT_CREATION_DISABLED)) {

			runAsInternalSystemAdmin();
			List<DocumentQueue> unfinishedDocumentQueueItems = documentQueueService.findUnfinishedDocumentQueueItems();

			List<DocumentQueue> inProgressDocs = unfinishedDocumentQueueItems.stream()
				.filter(documentQueue -> DocumentQueueStatus.IN_PROGRESS == documentQueue.getStatus())
				.collect(Collectors.toList());

			LOG.info("VACME-DOC-CREATION: Aktuell sind noch {} Dokumenterstellung(en) im Status {}",
				inProgressDocs.size(), DocumentQueueStatus.IN_PROGRESS);
			unfinishedDocumentQueueItems.removeAll(inProgressDocs);

			documentqueueRunnerService.performDocumentGenerationRun(unfinishedDocumentQueueItems);
		}
	}

	public void runMassenverarbeitungQueueProcessing() {
		if (!isBatchJobDisabled(VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_DISABLED)) {
			// Task soll als Systembenutzer laufen
			runAsInternalSystemAdmin();
			massenmutationRunnerService.performImpfungActionByQueue(MassenverarbeitungQueueTyp.IMPFUNG_EXTERALIZE);
			massenmutationRunnerService.performImpfungActionByQueue(MassenverarbeitungQueueTyp.IMPFUNG_ODI_MOVE);
			massenmutationRunnerService.performImpfungActionByQueue(MassenverarbeitungQueueTyp.REGISTRIERUNG_DELETE);
			massenmutationRunnerService.performImpfungActionByQueue(MassenverarbeitungQueueTyp.ODI_LAT_LNG_CALCULATE);
			massenmutationRunnerService.performImpfungActionByQueue(MassenverarbeitungQueueTyp.IMPFUNG_LOESCHEN);
			massenmutationRunnerService.performImpfungActionByQueue(MassenverarbeitungQueueTyp.IMPFGRUPPE_FREIGEBEN);
		}
	}

	public void runInactiveOdiUserDisableTask() {
		if (!isBatchJobDisabled(VACME_DEACTIVATE_UNUSED_USERACCOUNTS_JOB_DISABLED)) {
			// Task soll als Systembenutzer laufen
			runAsInternalSystemAdmin();
			benutzerMassenmutationRunnerService.performBenutzerInactiveOdiUserSperrenTask();
		}
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void scheduleHandleKontrolleAbgelaufen() {
		runAsInternalSystemAdmin();
		final List<UUID> dossiersToCheck =
			impfdossierService.findImpfdossiersInStatusKontrolliert(vacmeSettingsService.getCronKontrolleAbgelaufenBatchsize());
		LOG.info("VACME-KONTROLLE-ABGELAUFEN: Processing {} Dossiers", dossiersToCheck.size());
		for (UUID dossierID : dossiersToCheck) {
			try {
				impfdossierService.handleGueltigkeitKontrolleAbgelaufen(Impfdossier.toId(dossierID));
			} catch (Exception e) {
				LOG.error(
					"VACME-KONTROLLE-ABGELAUFEN: Fehler bei handleGueltigkeitKontrolleAbgelaufen fuer Dossier {} ",
					dossierID,
					e);
			}
		}
	}
}
