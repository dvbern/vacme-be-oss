package ch.dvbern.oss.vacme.service.benutzer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.service.BenutzerService;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.util.TimingUtil.calculateGenerationSpeed;

@ApplicationScoped
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BenutzerMassenmutationRunnerService {

	private final VacmeSettingsService vacmeSettingsService;
	private final BenutzerService benutzerService;

	public void performBenutzerInactiveOdiUserSperrenTask() {
		int minutesInactive = vacmeSettingsService.getBenutzerDeactivateAfterInactiveTimeMinutes();
		String issuerToSearchUsersFor = getVacmeWebIssuerForCurrentSystem();
		List<ID<Benutzer>> vacmeWebBenutzerToDeactivate =
			this.benutzerService.getVacmeWebBenutzerToDeactivate(
				minutesInactive,
				issuerToSearchUsersFor,
				vacmeSettingsService.getDeactivateUnusedUseraccountsBatchsize());

		if (!vacmeWebBenutzerToDeactivate.isEmpty()) {
			LOG.info("VACME-DEACTIVATE-USERS: Found {} Benutzer that did not log-in since {} that were issued by {}. "
					+ "They will be deactivated in Keycloak",
				vacmeWebBenutzerToDeactivate.size(),
				LocalDateTime.now().minusMinutes(minutesInactive),
				issuerToSearchUsersFor);
			deactivateListOfBenutzer(vacmeWebBenutzerToDeactivate);
		}
	}

	/**
	 * @return the issuer for which we are deactivating users
	 */
	private String getVacmeWebIssuerForCurrentSystem() {
		return vacmeSettingsService.getKeycloakWebIssuerUrl();
	}

	private int deactivateListOfBenutzer(@NonNull List<ID<Benutzer>> vacmeWebBenutzerToDeactivate) {
		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;
		try {
			LOG.info(
				"VACME-DEACTIVATE-USERS: Starting to perform deactivation action for {} Benutzer",
				vacmeWebBenutzerToDeactivate.size());

			for (ID<Benutzer> currBenId : vacmeWebBenutzerToDeactivate) {
				try {
					LOG.info("VACME-DEACTIVATE-USERS: Starting to deactivate Benutzer {}", currBenId.getId());
					boolean success = benutzerService.deactivateVacmeWebUserInKeycloakAndVacme(currBenId);
					if (success) {
						successCounter++;
					}
				} finally {
					totalCounter++;
					sleepForAWhile(); // to give keycloak some breathing room
				}
			}
		} finally {
			stopWatch.stop();
			LOG.info(
				"VACME-DEACTIVATE-USERS: Massenverarbeitung beendet. Es wurden {} Benutzer von total {} Benutzer"
					+ " in {}ms verarbeitet. {} ms/stk",
				successCounter, vacmeWebBenutzerToDeactivate.size(), stopWatch.getTime(TimeUnit.MILLISECONDS),
				calculateGenerationSpeed(
					totalCounter,
					stopWatch.getTime(TimeUnit.MILLISECONDS)));
		}
		return successCounter;
	}

	private void sleepForAWhile() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			LOG.error("Thread sleep was interrupted", e);
		}
	}
}
