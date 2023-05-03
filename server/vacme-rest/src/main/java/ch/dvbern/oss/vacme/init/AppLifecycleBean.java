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

package ch.dvbern.oss.vacme.init;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.flywaydb.core.Flyway;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
@Slf4j
public class AppLifecycleBean {
	private final boolean quarkusRunFlywayOnStartup;


	private boolean initialized = false;

	// You can Inject the object if you want to use it manually
	@Inject
	Flyway flyway;

	@Inject
	public AppLifecycleBean(
			@ConfigProperty(name = "quarkus.flyway.migrate-at-start", defaultValue = "false") boolean quarkusRunFlywayOnStartup
	) {
		this.quarkusRunFlywayOnStartup = quarkusRunFlywayOnStartup;
	}

	void onStart(@Observes StartupEvent ev) {
		String activeProfile = ProfileManager.getActiveProfile();
		LOG.info("The application is starting... (Profile: {})", activeProfile);


		printFlywayVersion();

		initialized = true;

		LOG.info("The application started (Profile: {})", activeProfile);
	}

	void onStop(@Observes ShutdownEvent ev) {
		LOG.info("The application is stopping...");
	}

	public boolean isInitialized() {
		return initialized;
	}

	private void printFlywayVersion() {
		if (quarkusRunFlywayOnStartup && flyway.info().current() != null) {
			LOG.info("Flyway version: {}", flyway.info().current().getVersion().toString());
		}
	}

}
