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

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;

/**
 * Dieser Filter prueft ob der Request an die Fachapplikation oder an die Registrierungsapplikation geht und
 * gibt den TentntResovler string zurueck der dann in den application.properties fuer
 * quarkus.oidc.{tenant}.auth-server-url eingesetzt wird
 *
 *
 */
@ApplicationScoped
public class OpenIdConnectTenantResolver implements TenantResolver {


    @Override
    public String resolve(RoutingContext context) {

		if (isInitialregRequest(context)) {
			return ClientApplications.VACME_INITIALREG;
		}

        // return null for default config
		return null;
    }

    public boolean isInitialregRequest(RoutingContext context){
		String path = context.request().path();
		String[] parts = path.split("/");
		if (parts.length == 0) {
			// resolve to default tenant configuration
			return false;
		}
		if (parts.length > 3) {
			String initialRegIdString = parts[3];
			return ClientApplications.VACME_INITIALREG.equals(initialRegIdString);
		}

		return false;

	}
}
