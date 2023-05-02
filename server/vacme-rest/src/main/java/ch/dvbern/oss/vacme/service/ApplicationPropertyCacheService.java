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

import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.util.CacheUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jetbrains.annotations.NotNull;

import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.COVID_ZERTIFIKAT_ENABLED;

/**
 * This is an in-memory cache that should cache the value of the COVID_ZERTIFIKAT_ENABLED ApplicationProperty for fast
 * reads from Memory
 */
@ApplicationScoped
@Slf4j
public class ApplicationPropertyCacheService {

	@ConfigProperty(name = "vacme.cache.zertifikat.enabled.ttl.sconds", defaultValue = CacheUtil.DEFAULT_CACHE_TIME)
	protected String zertifikatEnabledCacheTimeToLive;

	private final ApplicationPropertyService applicationPropertyService;
	private LoadingCache<ApplicationPropertyKey, Boolean> zertifikatEnabledCache;

	@Inject
	public ApplicationPropertyCacheService(ApplicationPropertyService applicationPropertyService) {
		this.applicationPropertyService = applicationPropertyService;
	}

	@PostConstruct
	void init() {
		// do init in postConstruct so all injected configs are ready to be used

		// Loader function
		CacheLoader<ApplicationPropertyKey, Boolean> loader;
		loader = new CacheLoader<>() {
			@Override
			public Boolean load(@NotNull ApplicationPropertyKey key) throws Exception {
				switch (key) {
				case COVID_ZERTIFIKAT_ENABLED:
					return applicationPropertyService.isZertifikatEnabled();
				default:
					throw new UnsupportedOperationException("Currently only "
						+ COVID_ZERTIFIKAT_ENABLED
						+ " is a supported cached ApplicationProperty value");
				}
			}
		};

		// cache Settings
		zertifikatEnabledCache = CacheBuilder.newBuilder()
			.expireAfterWrite(CacheUtil.getConfiguredTTL(
				"vacme.cache.zertifikat.enabled.ttl.sconds",
				zertifikatEnabledCacheTimeToLive), TimeUnit.SECONDS)
			.maximumSize(10)
			.build(loader);
	}

	public boolean isZertifikatEnabled() {
		return zertifikatEnabledCache.getUnchecked(COVID_ZERTIFIKAT_ENABLED);
	}
}
