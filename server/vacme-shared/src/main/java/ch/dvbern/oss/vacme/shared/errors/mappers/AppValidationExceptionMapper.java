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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import ch.dvbern.oss.vacme.i18n.TL;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import lombok.extern.slf4j.Slf4j;

import static ch.dvbern.oss.vacme.shared.errors.mappers.JsonMappingExceptionMapper.buildValidationErrorResponse;

@Provider
@Slf4j
@ApplicationScoped
public class AppValidationExceptionMapper implements ExceptionMapper<AppValidationException> {

	// either quarkus compiler complains (when using constuctor with @Inject) or IntelliJ-Inspections
	// I decided to soothe the compiler :)
	@SuppressWarnings("ProtectedField")
	@Inject
	protected TL translate;

	@Override
	public Response toResponse(AppValidationException exception) {

		String key = AppValidationMessage.class.getSimpleName() + '.' + exception.getValidationMessage().name();

		String message = translate.translate(key, (Object[]) exception.getArgs());

		AppValidationErrorResponse response = new AppValidationErrorResponse(message, key, List.of());

		return buildValidationErrorResponse(response);
	}

	public String toMessage(AppValidationException exception) {
		String key = AppValidationMessage.class.getSimpleName() + '.' + exception.getValidationMessage().name();
		return translate.translate(key, (Object[]) exception.getArgs());
	}
}
