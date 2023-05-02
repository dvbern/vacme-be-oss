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

package ch.dvbern.oss.vacme.rest_client.vmdl;

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import ch.dvbern.oss.vacme.jax.vmdl.VMDLAuthResponseJax;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

public abstract class VMDLRequestAuthTokenFactory implements ClientHeadersFactory {

	private LocalDateTime expiration;
	private String tokenTyp;
	private String token;

	@Inject
	@RestClient
	VMDLAuthRestClientService vmdlAuthRestClientService;

	@ConfigProperty(name = "vmdl.tenant_id")
	String tenantID;

	@ConfigProperty(name = "vmdl.username")
	String username;

	@ConfigProperty(name = "vmdl.password")
	String password;


	@Override
	public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
		String grantType = "password";
		String scope = String.format("api://%s/user_impersonation", getClientID());
		if (expiration == null || !expiration.isAfter(LocalDateTime.now())) {
			VMDLAuthResponseJax response = vmdlAuthRestClientService.aquireToken(tenantID, grantType, username, getClientID(), password, scope);
			expiration = LocalDateTime.now().plusSeconds(response.getExpiresIn()).minusSeconds(5);
			tokenTyp = response.getTokenType();
			token = response.getAccessToken();
		}
		MultivaluedMap<String, String> result = new MultivaluedMapImpl<>();
		result.add(HttpHeaders.AUTHORIZATION, tokenTyp + " " + token);
		return result;
	}

	protected abstract String getClientID();
}
