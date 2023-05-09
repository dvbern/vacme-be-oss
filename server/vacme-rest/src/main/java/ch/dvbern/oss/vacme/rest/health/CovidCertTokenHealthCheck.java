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

package ch.dvbern.oss.vacme.rest.health;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.zertifikat.ZertifizierungsToken;
import ch.dvbern.oss.vacme.service.ZertifikatService;
import com.google.common.base.Suppliers;
import io.smallrye.health.api.Wellness;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Dieser Healthcheck gibt an welche collation verwendet wird von der hier erstellten jdbc connection
 */
@ApplicationScoped
@Wellness
@Slf4j
public class CovidCertTokenHealthCheck implements HealthCheck {
	public static final int CACHE_DURATION = 240;

	private final Supplier<HealthCheckResponseBuilder> memoizedSupplier;

	private final ZertifikatService zertifikatService;

	@Inject
	public CovidCertTokenHealthCheck(@NonNull ZertifikatService zertifikatService) {
		this.zertifikatService = zertifikatService;
		memoizedSupplier = Suppliers.memoizeWithExpiration(
			this::perfromHealthQuery, CACHE_DURATION, TimeUnit.SECONDS);
	}

	@Override
	public HealthCheckResponse call() {
		// health check is cached for a short time
		HealthCheckResponseBuilder healthCheckResponseBuilder = memoizedSupplier.get();
		return healthCheckResponseBuilder.build();
	}

	private HealthCheckResponseBuilder perfromHealthQuery() {

		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("CovidCert Token wellness health "
			+ "check");
		try {
			Optional<ZertifizierungsToken> zertifizierungstoken = zertifikatService.getZertifizierungstoken();
			if (zertifizierungstoken.isPresent()) {
				responseBuilder.up().withData("token_valid_ts",
					zertifizierungstoken.get().getGueltigkeit().toString());
			} else {
				responseBuilder.up().withData("token_valid_ts", "no-valid-token");
			}
		} catch (Exception e) {
			LOG.error("Error while checking for valid Token ", e);
			responseBuilder.down().withData("token_valid_ts", "error");
		}
		return responseBuilder;
	}
}
