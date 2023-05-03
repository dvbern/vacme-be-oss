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

package ch.dvbern.oss.vacme.rest;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.rest.filter.HeaderFilter;
import ch.dvbern.oss.vacme.service.EnsureBenutzerService;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_INITIALREG;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@Transactional
@ApplicationScoped
@Tags(@Tag(name = OpenApiConst.TAG_BENUTZER))
@Path(VACME_INITIALREG + "/auth")
public class BenutzerResourceReg {

	public static final String COOKIE_PATH = "/";
	public static final String COOKIE_DOMAIN = null;

	@Inject
	EnsureBenutzerService ensureBenutzerService;

	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	HeaderFilter headerFilter;

	//constructor injection not working because of token

	@Nullable
	@POST
	@Path("/ensureUser")
	@PermitAll
	public Response ensureBenutzer() {

		if (securityIdentity.isAnonymous()) {
			return Response.ok().build();
		}
		ensureBenutzerService.ensureBenutzer();
		return Response.ok().build();
	}

	@POST
	@Path("/logout")
	@PermitAll
	public Response logout(){
		// homa todo kill cookie and session once we have a login on the backend
		return Response.ok().build();
	}

	@Nullable
	@GET
	@Path("/refreshXSRFToken")
	@PermitAll
	public Response refreshXSRFToken() {
		LOG.debug("/refreshXSRFToken");
		// we already know we are logged in because this endpoint is configured thusly (keycloak filtered)
		boolean cookieSecure = isCookieSecureQ();

		// Readable Cookie for XSRF Protection (the Cookie can only be read from our Domain)
		NewCookie xsrfCookie = new NewCookie(Constants.COOKIE_XSRF_TOKEN, UUID.randomUUID().toString(),
			COOKIE_PATH, COOKIE_DOMAIN, "XSRF", Constants.COOKIE_TIMEOUT_SECONDS, cookieSecure, false);

		return Response.ok().cookie(xsrfCookie).build();
	}

	private boolean isCookieSecureQ() {
		Config config = ConfigProvider.getConfig();
		final boolean forceCookieSecureFlag = config.getValue("vacme.force.cookie.secure.flag", Boolean.class);
		return isRequestProtocolSecureQ() || forceCookieSecureFlag;
	}

	private boolean isRequestProtocolSecureQ() {
		// get protocol of original request if present

		final String originalProtocol = headerFilter.getOriginalProtocol();
		if (originalProtocol != null) {
			return originalProtocol.startsWith("https");
		}

		return headerFilter.isRequestIsSecure();
	}
}
