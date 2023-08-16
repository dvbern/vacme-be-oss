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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.init.AppLifecycleBean;
import io.quarkus.runtime.configuration.ProfileManager;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import static java.util.Objects.requireNonNull;


@ApplicationScoped
@Readiness
public class SimpleHealthCheck implements HealthCheck {
	private static final String APP_NAME = "backend application";

	private final AppLifecycleBean appLifecycleBean;

	@Inject
	public SimpleHealthCheck(AppLifecycleBean appLifecycleBean) {
		this.appLifecycleBean = requireNonNull(appLifecycleBean);
	}

	@Override
	public HealthCheckResponse call() {
		String activeProfile = ProfileManager.getActiveProfile();
		if (appLifecycleBean.isInitialized()) {
			return HealthCheckResponse.builder().name(APP_NAME)
				.withData("profile", activeProfile)
				.up()
				.build();
		}

		return HealthCheckResponse.builder()
			.name(APP_NAME)
			.withData("initialized", false)
			.withData("profile", activeProfile)
			.build();
	}
}
