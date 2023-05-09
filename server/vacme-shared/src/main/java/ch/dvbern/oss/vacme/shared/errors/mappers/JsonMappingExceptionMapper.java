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

package ch.dvbern.oss.vacme.shared.errors.mappers;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;

import static ch.dvbern.oss.vacme.shared.util.Util.ifNull;

@Provider
@Slf4j
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

	@Override
	public Response toResponse(JsonMappingException exception) {

		try {
			Throwable cause = exception.getCause();
			if (cause instanceof ConstraintViolationException) {

				ConstraintViolationException cv = (ConstraintViolationException) cause;
				return buildValidationErrorResponse(new AppValidationErrorResponse(cv.getConstraintViolations()));

			}

			if (cause instanceof AppValidationException) {

				// this case happens when e.g. the emailaddress is parsed and fails
				// cause of this we get a message from the caused exception but the path from the jsonexception
				AppValidationException av = (AppValidationException) cause;
				return buildValidationErrorResponse(new AppValidationErrorResponse(
						ifNull(av.getMessage(), ""),
						av.getValidationMessage().name(),
						exception.getPath()));

			}
		} catch (RuntimeException rte) {
			LOG.error("Error while building the error message", rte);
			return buildFailureResponse(new AppFailureErrorResponse(
					AppFailureMessage.INTERNAL_ERROR,
					"error building the validation error message, see server logfile for details"));
		}

		LOG.error("Unhandled exception", exception);

		return buildFailureResponse(
				new AppFailureErrorResponse(
						AppFailureMessage.JSON_MAPPING, exception.getPath()));

	}

	private Response buildFailureResponse(AppFailureErrorResponse failure) {
		return Response
				.serverError()
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity(failure)
				.build();

	}

	public static Response buildValidationErrorResponse(AppValidationErrorResponse validation) {
		Response result = Response
				.status(Status.BAD_REQUEST)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity(validation)
				.build();
		return result;
	}

}
