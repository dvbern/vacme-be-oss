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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.ApplicationMessage;
import ch.dvbern.oss.vacme.entities.registration.Sprache;
import ch.dvbern.oss.vacme.jax.base.SettingsJax;
import ch.dvbern.oss.vacme.jax.base.TranslatedTextJax;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.DISTANCE_BETWEEN_IMPFUNGEN_DISIRED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_AFTER;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_BEFORE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.KORREKTUR_EMAIL_TELEPHONE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.MINUTES_BETWEEN_INFO_UPDATES;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.MINUTES_BETWEEN_PHONENUMBER_UPDATE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.ODI_FILTER_WELL_FOR_GALENICA_ENABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.SELBSTZAHLER_FACHAPPLIKATION_ENABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.SELBSTZAHLER_PORTAL_ENABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.SHOW_ONBOARDING_WELCOME_TEXT;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_FREIGABE_NOTIFICATION_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_GEOCODING_ENABLED;
import static ch.dvbern.oss.vacme.shared.util.Constants.GENERAL_INFOTEXT_DE;
import static ch.dvbern.oss.vacme.shared.util.Constants.GENERAL_INFOTEXT_EN;
import static ch.dvbern.oss.vacme.shared.util.Constants.GENERAL_INFOTEXT_FR;

@ApplicationScoped
@Slf4j
public class SettingsService {

	private final ApplicationPropertyService applicationPropertyService;
	private final ApplicationMessageService applicationMessageService;

	private static final String CACHE_KEY = "settings";
	private LoadingCache<String, Optional<SettingsJax>> settingsCache;

	@ConfigProperty(name = "vacme.terminreservation.enabled", defaultValue = "false")
	protected boolean terminReservationEnabled;

	@ConfigProperty(name = "well.url")
	protected String wellUrl;


	@Inject
	public SettingsService(
		@NonNull ApplicationPropertyService applicationPropertyService,
		@NonNull ApplicationMessageService applicationMessageService
	) {
		this.applicationPropertyService = applicationPropertyService;
		this.applicationMessageService = applicationMessageService;
	}

	@PostConstruct
	void init() {
		// do init in postConstruct so all injected configs are ready to be used
		CacheLoader<String, Optional<SettingsJax>> loaderForSettings;
		loaderForSettings = new CacheLoader<String, Optional<SettingsJax>>() {
			@Override
			public Optional<SettingsJax> load(String s) throws Exception {
				final SettingsJax settings = readSettings();
				return Optional.of(settings);
			}
		};
		// cache Settings
		settingsCache = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.of(8, ChronoUnit.MINUTES))
			.maximumSize(1)
			.build(loaderForSettings);
	}

	@NonNull
	private SettingsJax readSettings() {
		SettingsJax jax = new SettingsJax();
		jax.setDistanceImpfungenDesired(
			applicationPropertyService.getByKey(DISTANCE_BETWEEN_IMPFUNGEN_DISIRED).getValueAsInteger());
		jax.setDistanceImpfungenToleranceBefore(
			applicationPropertyService.getByKey(DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_BEFORE).getValueAsInteger());
		jax.setDistanceImpfungenToleranceAfter(
			applicationPropertyService.getByKey(DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_AFTER).getValueAsInteger());
		jax.setShowOnboardingWelcomeText(applicationPropertyService.getByKey(
			SHOW_ONBOARDING_WELCOME_TEXT).getValueAsBoolean());
		jax.setEmailKorrekturEnabled(applicationPropertyService.getByKey(
			KORREKTUR_EMAIL_TELEPHONE).getValueAsBoolean());
		jax.setBoosterFreigabeNotificationDisabled(applicationPropertyService.getByKey(
			VACME_BOOSTER_FREIGABE_NOTIFICATION_DISABLED).getValueAsBoolean());
		List<TranslatedTextJax> listOfInfotexts =
			this.getInfotexts();
		jax.setGeneralInfotexts(listOfInfotexts);
		jax.setGeocodingEnabled(applicationPropertyService.getByKey(VACME_GEOCODING_ENABLED).getValueAsBoolean());
		jax.setMinutesBetweenInfoUpdates(applicationPropertyService.getByKey(MINUTES_BETWEEN_INFO_UPDATES).getValueAsInteger());
		jax.setMinutesBetweenNumberUpdates(applicationPropertyService.getByKey(MINUTES_BETWEEN_PHONENUMBER_UPDATE).getValueAsInteger());
		jax.setSelbstzahlerFachapplikationEnabled(
			applicationPropertyService.getByKey(SELBSTZAHLER_FACHAPPLIKATION_ENABLED).getValueAsBoolean());
		jax.setSelbstzahlerPortalEnabled(
			applicationPropertyService.getByKey(SELBSTZAHLER_PORTAL_ENABLED).getValueAsBoolean());
		jax.setReservationsEnabled(terminReservationEnabled);
		jax.setOdiFilterWellForGalenicaEnabled(
			applicationPropertyService.getByKey(ODI_FILTER_WELL_FOR_GALENICA_ENABLED).getValueAsBoolean());
		jax.setWellUrl(wellUrl);
		return jax;
	}

	@NonNull
	private List<TranslatedTextJax> getInfotexts() {

		List<TranslatedTextJax> result = new ArrayList<>();
		Optional<ApplicationMessage> byKeyOptionalDE =
			this.applicationMessageService.getByTitle(GENERAL_INFOTEXT_DE);
		byKeyOptionalDE.ifPresent(applicationProperty -> result.add(new TranslatedTextJax(
			(Sprache.DE),
			applicationProperty.getHtmlContent())));
		Optional<ApplicationMessage> byKeyOptionalFR =
			this.applicationMessageService.getByTitle(GENERAL_INFOTEXT_FR);
		byKeyOptionalFR.ifPresent(applicationProperty -> result.add(new TranslatedTextJax(
			(Sprache.FR),
			applicationProperty.getHtmlContent())));
		Optional<ApplicationMessage> byKeyOptionalEN =
			this.applicationMessageService.getByTitle(GENERAL_INFOTEXT_EN);
		byKeyOptionalEN.ifPresent(applicationProperty -> result.add(new TranslatedTextJax(
			(Sprache.EN),
			applicationProperty.getHtmlContent())));
		return result;
	}

	@NonNull
	public SettingsJax getSettings() {
		final AppFailureException exception = new AppFailureException("Could not read settings");
		try {
			return settingsCache.get(CACHE_KEY).orElseThrow(() -> exception);
		} catch (ExecutionException ignored) {
			throw exception;
		}
	}
}
