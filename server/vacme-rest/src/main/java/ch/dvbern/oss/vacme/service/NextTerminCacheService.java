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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.graalvm.collections.Pair;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class NextTerminCacheService {

	public static final String DEFAULT_CACHE_TIME = "60";

	private final OrtDerImpfungService ortDerImpfungService;
	private final VacmeSettingsService vacmeSettingsService;

	private LoadingCache<ID<OrtDerImpfung>, Optional<LocalDateTime>> nextFreeErstimpfungSlotByOdiCache;
	private LoadingCache<ID<OrtDerImpfung>, Optional<LocalDateTime>> nextFreeZweitimpfungSlotByOdiCache;
	private LoadingCache<Pair<ID<OrtDerImpfung>, KrankheitIdentifier>, Optional<LocalDateTime>>
		nextFreeNImpfungSlotByOdiAndKrankheitCache;

	@PostConstruct
	void init() {
		// do init in postConstruct so all injected configs are ready to be used

		// Loader functions
		CacheLoader<ID<OrtDerImpfung>, Optional<LocalDateTime>> loaderForNextSlotWithFreeT1;
		loaderForNextSlotWithFreeT1 = new CacheLoader<>() {
			@Override
			public Optional<LocalDateTime> load(@NonNull ID<OrtDerImpfung> ortDerImpfungId) {
				LocalDateTime nextFreierImpftermin = ortDerImpfungService.getNextFreierImpftermin(ortDerImpfungId,
					Impffolge.ERSTE_IMPFUNG, null, false, KrankheitIdentifier.COVID);
				return Optional.ofNullable(nextFreierImpftermin);
			}
		};

		CacheLoader<ID<OrtDerImpfung>, Optional<LocalDateTime>> loaderForNextSlotWithFreeT2;
		loaderForNextSlotWithFreeT2 = new CacheLoader<>() {
			@Override
			public Optional<LocalDateTime> load(ID<OrtDerImpfung> ortDerImpfungId) {
				LocalDateTime nextFreierImpftermin2 = ortDerImpfungService.getNextFreierImpftermin(ortDerImpfungId,
					Impffolge.ZWEITE_IMPFUNG, null, false, KrankheitIdentifier.COVID);
				return Optional.ofNullable(nextFreierImpftermin2);
			}
		};

		CacheLoader<? super Pair<ID<OrtDerImpfung>, KrankheitIdentifier>, Optional<LocalDateTime>>
			loaderForNextSlotWithFreeTerminN;
		loaderForNextSlotWithFreeTerminN = new CacheLoader<>() {
			@Override
			public @NotNull Optional<LocalDateTime> load(@NonNull Pair<ID<OrtDerImpfung>, KrankheitIdentifier> key) {
				LocalDateTime nextFreierImpfterminN = ortDerImpfungService.getNextFreierImpftermin(key.getLeft(),
					Impffolge.BOOSTER_IMPFUNG, null, false, key.getRight());
				return Optional.ofNullable(nextFreierImpfterminN);
			}
		};

		// cache Settings
		nextFreeErstimpfungSlotByOdiCache = CacheBuilder.newBuilder()
			.expireAfterWrite(getConfiguredTTL(), TimeUnit.SECONDS)
			.maximumSize(1000)
			.build(loaderForNextSlotWithFreeT1);

		nextFreeZweitimpfungSlotByOdiCache = CacheBuilder.newBuilder()
			.expireAfterWrite(getConfiguredTTL(), TimeUnit.SECONDS)
			.maximumSize(1000)
			.build(loaderForNextSlotWithFreeT2);

		nextFreeNImpfungSlotByOdiAndKrankheitCache = CacheBuilder.newBuilder()
			.expireAfterWrite(getConfiguredTTL(), TimeUnit.SECONDS)
			.maximumSize(1000)
			.build(loaderForNextSlotWithFreeTerminN);
	}

	private long getConfiguredTTL() {
		try {
			String cachetime = vacmeSettingsService.getNextfreierTerminCacheTimeToLive();
			if (cachetime == null) {
				LOG.warn("using default config value for vacme.cache.nextfrei.ttl.sconds");
				final Config config =
					ConfigProvider.getConfig(); // read from static config if not set otherwise, makes testing easier
				cachetime =
					config.getOptionalValue("vacme.cache.nextfrei.ttl.sconds", String.class).orElse(DEFAULT_CACHE_TIME);
			}
			return Long.parseLong(cachetime);
		} catch (NumberFormatException exception) {
			LOG.error("Missconfiguration: vacme.cache.nextfrei.ttl.sconds must be numeric, using default value "
				+ DEFAULT_CACHE_TIME);
			return Long.parseLong(DEFAULT_CACHE_TIME);
		}
	}

	@Nullable
	public LocalDateTime getNextFreierImpfterminThroughCache(
		@NonNull ID<OrtDerImpfung> ortDerImpfungId,
		@NonNull Impffolge impffolge,
		@Nullable LocalDateTime otherTerminDate,
		boolean limitMaxFutureDate,
		@NonNull KrankheitIdentifier krankheit
	) {

		if (impffolge == Impffolge.ERSTE_IMPFUNG && otherTerminDate == null && !limitMaxFutureDate) {
			Optional<LocalDateTime> cachedDate = nextFreeErstimpfungSlotByOdiCache.getUnchecked(ortDerImpfungId);
			return cachedDate.orElse(null);
		}
		if (impffolge == Impffolge.ZWEITE_IMPFUNG && otherTerminDate == null && !limitMaxFutureDate) {
			Optional<LocalDateTime> cachedDate = nextFreeZweitimpfungSlotByOdiCache.getUnchecked(ortDerImpfungId);
			return cachedDate.orElse(null);
		}

		if (impffolge == Impffolge.BOOSTER_IMPFUNG && otherTerminDate == null && !limitMaxFutureDate) {
			Optional<LocalDateTime> cachedDate =
				nextFreeNImpfungSlotByOdiAndKrankheitCache.getUnchecked(Pair.create(ortDerImpfungId, krankheit));
			return cachedDate.orElse(null);
		}

		LOG.debug("Cache can only be used for Termine with no otherTermin and no limit"); // e.g. when booking matching 2nd Termin
		return ortDerImpfungService.getNextFreierImpftermin(
			ortDerImpfungId,
			impffolge,
			otherTerminDate,
			limitMaxFutureDate,
			krankheit);
	}
}
