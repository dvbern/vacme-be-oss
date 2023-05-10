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

package ch.dvbern.oss.vacme.service.covidcertificate;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@RequestScoped
public class CovidCertSignatureHeaderFactory implements ClientHeadersFactory {

	private final SignatureHeaderBean signatureHeaderBean;

	@Inject
	public CovidCertSignatureHeaderFactory(
		SignatureHeaderBean signatureHeaderBean
	) {
		this.signatureHeaderBean = signatureHeaderBean;
	}

	@Override
	public MultivaluedMap<String, String> update(
		MultivaluedMap<String, String> incomingHeaders,
		MultivaluedMap<String, String> clientOutgoingHeaders) {

		MultivaluedMap<String, String> returnVal = new MultivaluedHashMap<>();
		returnVal.putAll(clientOutgoingHeaders);
		returnVal.putSingle(signatureHeaderBean.getSignatureHeaderName(), signatureHeaderBean.getSignature());
		return returnVal;
	}
}
