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

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.util.CacheUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jetbrains.annotations.NotNull;

/**
 * This is an in-memory cache that should cache the value of the noFreieTermine property of each Krankheit for fast
 * reads from Memory
 */
@ApplicationScoped
@Slf4j
public class KrankheitPropertyCacheService {
	@ConfigProperty(name = "vacme.cache.no.freietermine.ttl.sconds", defaultValue = CacheUtil.DEFAULT_CACHE_TIME)
	protected String noFreietermineCacheTimeToLive;

	private final KrankheitService krankheitService;

	private LoadingCache<KrankheitIdentifier, Boolean> noTerminCache;

	@Inject
	public KrankheitPropertyCacheService(KrankheitService krankheitService) {
		this.krankheitService = krankheitService;
	}

	@PostConstruct
	void init() {
		// do init in postConstruct so all injected configs are ready to be used

		// Loader function
		CacheLoader<KrankheitIdentifier, Boolean> loader;
		loader = new CacheLoader<>() {
			@Override
			public Boolean load(@NotNull KrankheitIdentifier key) {
				return krankheitService.getNoFreieTermine(key);
			}
		};

		// cache Settings
		noTerminCache = CacheBuilder.newBuilder()
			.expireAfterWrite(CacheUtil.getConfiguredTTL(
				"vacme.cache.no.freietermine.ttl.sconds",
				noFreietermineCacheTimeToLive), TimeUnit.SECONDS)
			.maximumSize(10)
			.build(loader);
	}

	public boolean noFreieTermin(@NonNull KrankheitIdentifier krankheit) {
		return noTerminCache.getUnchecked(krankheit);
	}
}
