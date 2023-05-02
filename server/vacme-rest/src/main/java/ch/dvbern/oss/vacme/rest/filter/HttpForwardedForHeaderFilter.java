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

package ch.dvbern.oss.vacme.rest.filter;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import ch.dvbern.oss.vacme.shared.util.Constants;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.MDC;

/**
 * Extract content of X-Forwarded-For header in case we want to use it in logging output
 */
@Provider
@PreMatching
@RequestScoped
public class HttpForwardedForHeaderFilter implements ContainerRequestFilter {


	@Context
	HttpServerRequest request;

	@Override
    public void filter(ContainerRequestContext context) {
		var forwardedForFilter = context.getHeaderString("X-Forwarded-For");
		String ip = forwardedForFilter != null ? forwardedForFilter : "unknown";
		MDC.put(Constants.LOG_MDC_FORWARDED_FOR, ip);
    }
}
