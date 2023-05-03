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

package ch.dvbern.oss.vacme.service.plz;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
public class PLZCacheService {

	private final PLZService plzService;

	private LoadingCache<String, Optional<String>> plzToKantonskuerzelCache;
	private LoadingCache<String, Optional<String>> plzToMedstatCache;

	@Inject
	public PLZCacheService(PLZService plzService) {
		this.plzService = plzService;
	}

	@PostConstruct
	void init() {
		// do init in postConstruct so all injected configs are ready to be used

		// Loader function
		CacheLoader<String, Optional<String>> loaderForPlzToKantonskuerzel;
		loaderForPlzToKantonskuerzel = new CacheLoader<>() {
			@Override
			public Optional<String> load(@NonNull String plz) {
				if (StringUtils.isNotEmpty(plz)) {
					String plzToSearch = plz.trim();
					return plzService.findBestMatchingKantonFor(plzToSearch);
				}
				return Optional.empty();
			}
		};

		// cache Settings
		plzToKantonskuerzelCache = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.of(26, ChronoUnit.HOURS))
			.maximumSize(500)
			.build(loaderForPlzToKantonskuerzel);


		// Loader function
		CacheLoader<String, Optional<String>> loaderForPlzToMedstat;
		loaderForPlzToMedstat = new CacheLoader<>() {
			@Override
			public Optional<String> load(@NonNull String plz) {
				if (StringUtils.isNotEmpty(plz)) {
					String plzToSearch = plz.trim();
					return plzService.findMedstatForPLZ(plzToSearch);
				}
				return Optional.empty();
			}
		};

		// cache Settings
		plzToMedstatCache = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.of(26, ChronoUnit.HOURS))
			.maximumSize(500)
			.build(loaderForPlzToMedstat);

	}

	@NonNull
	public Optional<String> findBestMatchingKantonFor(@NonNull String plz) {
		try {
			return plzToKantonskuerzelCache.get(plz);

		} catch (ExecutionException e) {
			return Optional.empty();
		}
	}


	@NonNull
	public Optional<String> findBestMatchingMedStatFor(@NonNull String plz) {
		try {
			return plzToMedstatCache.get(plz);

		} catch (ExecutionException e) {
			return Optional.empty();
		}
	}
}
