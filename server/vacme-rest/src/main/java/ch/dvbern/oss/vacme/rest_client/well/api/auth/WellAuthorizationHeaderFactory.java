/*
 *
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.oss.vacme.rest_client.well.api.auth;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

@ApplicationScoped
public class WellAuthorizationHeaderFactory implements ClientHeadersFactory {

	private LocalDateTime expiration = null;
	private String token = null;

	@Inject
	@RestClient
	WellAuthRestClientService wellAuthRestClientService;

	@Inject
	VacmeSettingsService vacmeSettingsService;

	@Override
	public MultivaluedMap<String, String> update(
		MultivaluedMap<String, String> incomingHeaders,
		MultivaluedMap<String, String> clientOutgoingHeaders) {
		if (isStoredTokenExpired()) {
			String grantType = WellAPIConstants.GRANT_TYPE_CLIENT_CREDENTIALS;
			String audience = WellAPIConstants.AUDIENCE_PARTNER_API;
			refreshToken(grantType, audience);
		}

		// add Authorization header to outgoing headers
		MultivaluedMap<String, String> result = new MultivaluedMapImpl<>();
		result.add(HttpHeaders.AUTHORIZATION, WellAPIConstants.BEARER + " " + token);
		return result;
	}

	private void refreshToken(String grantType, String audience) {
		WellAuthRepsonseJax tokenResponse =
			wellAuthRestClientService.aquireToken(
				grantType,
				audience,
				vacmeSettingsService.getWellClientId(),
				vacmeSettingsService.getWellClientSecret()
			);
		expiration = LocalDateTime.now().plusSeconds(tokenResponse.getExtExpiresIn()).minusSeconds(5);
		token = tokenResponse.getAccessToken();
	}

	private boolean isStoredTokenExpired() {
		return expiration == null || !expiration.isAfter(LocalDateTime.now());
	}

}
