package ch.dvbern.oss.vacme.service.benutzer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ApplicationProperty;
import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.service.ApplicationPropertyService;
import ch.dvbern.oss.vacme.service.BenutzerService;
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
public class BenutzerMassenmutationRunnerService {

	// default is 30 days in minutes (60 * 24 * 30)
	@ConfigProperty(name = "vacme.deactivate.unused.useraccounts.after.minutes", defaultValue = "43200")
	int vacmeDeactivateUnusedUseraccountsAfterMinutes;

	@ConfigProperty(name = "vacme.oidc.web.auth.server.url") // configured as VACME_OIDC_WEB_AUTH_SERVER_URL
	protected String vacmeWebOidcIssuerUrl;

	@ConfigProperty(name = "vacme.deactivate.unused.useraccounts.batchsize", defaultValue = "200")
	int vacmeDeactivateUnusedUseraccountsBatchsize;


	private final ApplicationPropertyService applicationPropertyService;
	private final BenutzerService benutzerService;

	public void performBenutzerInactiveOdiUserSperrenTask() {
		int minutesInactive = getBenutzerDeactivateAfterInactiveTimeMinutes();
		String issuerToSearchUsersFor = getVacmeWebIssuerForCurrentSystem();
		List<ID<Benutzer>> vacmeWebBenutzerToDeactivate =
			this.benutzerService.getVacmeWebBenutzerToDeactivate(minutesInactive, issuerToSearchUsersFor, vacmeDeactivateUnusedUseraccountsBatchsize);

		if (!vacmeWebBenutzerToDeactivate.isEmpty()) {
			LOG.info("VACME-DEACTIVATE-USERS: Found {} Benutzer that did not log-in since {} that were issued by {}. "
					+ "They will be deactivated in Keycloak",
				vacmeWebBenutzerToDeactivate.size(), LocalDateTime.now().minusMinutes(minutesInactive), issuerToSearchUsersFor);
			deactivateListOfBenutzer(vacmeWebBenutzerToDeactivate);
		}
	}

	/**
	 * @return the issuer for which we are deactivating users
	 */
	private String getVacmeWebIssuerForCurrentSystem() {
		return vacmeWebOidcIssuerUrl;
	}

	/**
	 *
	 * @return the time in minutes without login after which a user-account may be deactivated
	 */
	private int getBenutzerDeactivateAfterInactiveTimeMinutes() {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.VACME_DEACTIVATE_UNUSED_USERACCOUNTS_AFTER_MINUTES);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Integer.parseInt(applicationProperty.getValue()))
			.orElseGet(() -> vacmeDeactivateUnusedUseraccountsAfterMinutes);
	}

	private int deactivateListOfBenutzer(@NonNull List<ID<Benutzer>> vacmeWebBenutzerToDeactivate) {
		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;
		try {
			LOG.info("VACME-DEACTIVATE-USERS: Starting to perform deactivation action for {} Benutzer",
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
				calculateGenerationSpeed(totalCounter,
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
