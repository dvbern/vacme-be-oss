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

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.covidcertificate.CovidCertErrorcode;
import ch.dvbern.oss.vacme.entities.covidcertificate.RestError;
import ch.dvbern.oss.vacme.shared.errors.CovidCertApiFailureException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

/**
 * This mapper will transform http rest api errors from CovidCertApi to an Exception in Vacme
 */
@Priority(4000)
@ApplicationScoped
@Slf4j
public class CovidCertApiExceptionMapper implements ResponseExceptionMapper<CovidCertApiFailureException> {

	private final ObjectMapper objectMapper;

	@Inject
	public CovidCertApiExceptionMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
   public CovidCertApiFailureException toThrowable(Response response) {
	   String errorBody = getBody(response);

		try {
			RestError restError = objectMapper.readValue(errorBody, RestError.class);
			CovidCertErrorcode covidCertErrorcode = CovidCertErrorcode.fromErrorCode(restError.getErrorCode());
			return new CovidCertApiFailureException(restError, covidCertErrorcode);
		} catch (JsonProcessingException e) {
			int status = response.getStatus();
			String msg = String.format("Error in Covid-Cert Api (%s):  %s", status, errorBody);
			return new CovidCertApiFailureException(msg);
		}
   }

	private String getBody(Response response) {
		return response.readEntity(String.class);
	}
}
