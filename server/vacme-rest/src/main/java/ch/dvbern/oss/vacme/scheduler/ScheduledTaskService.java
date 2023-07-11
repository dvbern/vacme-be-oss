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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;

/**
 * Service zum starten periodischer Tasks
 */
@ApplicationScoped
@Slf4j
public class ScheduledTaskService {

	private final SystemAdminRunnerService systemAdminRunnerService;

	@Inject
	public ScheduledTaskService(
		SystemAdminRunnerService systemAdminRunnerService) {
		this.systemAdminRunnerService = systemAdminRunnerService;
	}

	@Scheduled(cron = "{vacme.cron.stat.kennzahlrunner}")
		//	@Scheduled(cron="*/30 * * * * ?")  // for testing
	void schedule() {
		systemAdminRunnerService.runStatTask();
	}

	@Scheduled(cron = "{vacme.cron.stat.dbvalidation}")
	void scheduleDbValidationQueries() {
		systemAdminRunnerService.runDbValidationTask();
	}

	@Scheduled(cron = "{vacme.cron.healthcheck.zertifikat.job.lock}")
	void scheduleHealthCheckZertifikatJobLock() {
		systemAdminRunnerService.runHealthCheckZertifikatJobLock();
	}

	@Scheduled(cron = "{vacme.cron.healthcheck.geocoding}")
	void scheduleHealthCheckGeocoding() {
		systemAdminRunnerService.runHealthCheckGeocoding();
	}

	@Scheduled(cron = "{vacme.cron.stat.vmdl.upload}")
	void scheduleVMDLUpload() {
		systemAdminRunnerService.runVMDLUploadTask();
	}

	@Scheduled(cron = "{vacme.cron.service.reporting.anzahlerstimpfungen.mail}")
	void scheduleServiceReportingAnzahlErstimpfungenMail() {
		systemAdminRunnerService.runServiceReportingAnzahlErstimpfungenMailTask();
	}

	@Scheduled(cron = "{vacme.cron.service.reporting.anzahlzweitbooster.mail}")
	void scheduleServiceReportingAnzahlZweitBoosterMail() {
		systemAdminRunnerService.runServiceReportingAnzahlZweitBoosterMailTask();
	}

	@Scheduled(cron = "{vacme.cron.service.reporting.anzahl.impfungen.mail}")
	void scheduleServiceReportingAnzahlImpfungenMail() {
		systemAdminRunnerService.runServiceReportingAnzahlImpfungenMailTask();
	}

	@Scheduled(cron = "{vacme.cron.reservierung.expired.clear}")
	void scheduleImpfterminReservationReset() {
		systemAdminRunnerService.runImpfterminReservationResetTask();
	}

	@Scheduled(cron = "{vacme.cron.archivierung}")
	void scheduleArchivierung() {
		systemAdminRunnerService.runArchivierungD3();
	}

	@Scheduled(cron = "{vacme.cron.auto.abschliessen}")
	void scheduleAutoAbschliessen() {
		systemAdminRunnerService.runRegistrierungAutomatischAbschliessen();
	}

	@Scheduled(cron = "{vacme.cron.check.zweittermine}")
	void scheduleCheckForZweittermine() {
		systemAdminRunnerService.runCheckForZweittermine();
	}

	@Scheduled(cron = "{vacme.cron.update.odi.no.termin}")
	void scheduleMarkOdiNoTermin() {
		systemAdminRunnerService.runUpdateOdiNoTermin();
	}

	@Scheduled(cron = "{vacme.cron.update.global.no.termin}")
	void scheduleGlobalNoTermin() {
		systemAdminRunnerService.runUpdateGlobalNoTermin();
	}

	@Scheduled(cron = "{vacme.cron.update.impfungen.for.kanton}")
	void scheduleHasAtleastOneImpfungViewableByKanton() {
		systemAdminRunnerService.runUpdateHasAtleastOneImpfungViewableByKanton();
	}

	@Scheduled(cron = "{vacme.cron.covidcert.api.creation}")
	void scheduleCovidCertProcessingTask() {
		systemAdminRunnerService.runCovidCertUploadTask();
	}

	@Scheduled(cron = "{vacme.cron.covidcert.api.post.creation}")
	void scheduleCovidCertPostProcessingTask() {
		systemAdminRunnerService.runCovidCertUploadTaskNonOnline();
	}

	@Scheduled(cron = "{vacme.cron.onboarding.post.creation}")
	void scheduleOnboardingNotificationCreation() {
		systemAdminRunnerService.runOnboardingLetterGenerationTask();
	}

	@Scheduled(cron = "{vacme.cron.clear.covidcert.tokens}")
	void scheduleCovidCertClearTokensTask() {
		systemAdminRunnerService.runCovidCertClearTokensTask();
	}

	@Scheduled(cron = "{vacme.cron.booster.immunisiert.status.move}")
	void scheduledBoosterImmunisiertStatusImpfschutzService() {
		systemAdminRunnerService.runBoosterImmunisiertStatusImpfschutzService(false);
	}

	@Scheduled(cron = "{vacme.cron.booster.freigabe.status.move}")
	void scheduledBoosterFreigabeStatusService() {
		systemAdminRunnerService.runBoosterFreigabeStatusService(false);
	}

	@Scheduled(cron = "{vacme.cron.booster.engine.recalculate}")
	void scheduleEngineCalc() {
		systemAdminRunnerService.runBoosterRuleRecalc(false);
	}

	@Scheduled(cron = "{vacme.cron.priority.update}")
	void schedulePriorityUpdateForGrowingChildren() {
		systemAdminRunnerService.runPriorityUpdateForGrowingChildren();
	}

	@Scheduled(cron = "{vacme.cron.doccreation.cleanup}")
	void scheduleAsyncDocumentQueueCleanup() {
		systemAdminRunnerService.runAsyncDocumentCleanup();
	}

	@Scheduled(cron = "{vacme.cron.doccreation.create}")
	void scheduleAsyncDocCreation() {
		systemAdminRunnerService.runAsyncDocCreation();
	}

	@Scheduled(cron = "{vacme.cron.massenverarbeitungqueue.process}")
	void scheduleMassenverarbeitungQueueProcessing() {
		systemAdminRunnerService.runMassenverarbeitungQueueProcessing();
	}

	@Scheduled(cron = "{vacme.cron.disable.unused.users}")
	void scheduleInactiveUserDisableTask() {
		systemAdminRunnerService.runInactiveOdiUserDisableTask();
	}

	@Scheduled(cron = "{vacme.cron.handle.kontrolle.abgelaufen}")
	void scheduleHandleKontrolleAbgelaufen() {
		systemAdminRunnerService.scheduleHandleKontrolleAbgelaufen();
	}
}
