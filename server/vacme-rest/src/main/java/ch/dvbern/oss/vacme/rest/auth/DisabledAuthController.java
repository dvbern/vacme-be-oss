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

package ch.dvbern.oss.vacme.rest.auth;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.interceptor.Interceptor;


import io.quarkus.security.spi.runtime.AuthorizationController;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * if you have a very good reason to disable all authorization you can make quarkus skip authorization by
 * setting the vacme.authorization.disable = true
 */
@Alternative
@Priority(Interceptor.Priority.LIBRARY_AFTER)
@ApplicationScoped
public class DisabledAuthController extends AuthorizationController {

	@ConfigProperty(name = "vacme.authorization.disable", defaultValue = "false")
	boolean disableAuthorization;

	@Override
	public boolean isAuthorizationEnabled() {
		return !disableAuthorization;
	}
}
