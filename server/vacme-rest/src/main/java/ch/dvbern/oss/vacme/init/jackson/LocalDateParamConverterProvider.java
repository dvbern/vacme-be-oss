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

package ch.dvbern.oss.vacme.init.jackson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.shared.util.InputValidationUtil.cleanToNull;

/**
 * *sigh*.... JavaTimeModule does not provide ParamConverters :(
 */
@Provider
public class LocalDateParamConverterProvider implements ParamConverterProvider {
	private static class LocalDateParamConverter<T> implements ParamConverter<T> {
		private final Class<T> rawType;

		public LocalDateParamConverter(Class<T> rawType) {
			this.rawType = rawType;
		}

		@Nullable
		@Override
		public T fromString(String value) {
			try {
				String normalized = cleanToNull(value);
				return rawType.cast(normalized == null ? null : LocalDate.parse(normalized));
			} catch (Exception e) {
				throw new BadRequestException("Cannot parse date parameter: " + value, e);
			}
		}

		@Nullable
		@Override
		public String toString(T value) {
			return value == null ? null : ((LocalDate) value).toString();
		}
	}

	@Nullable
	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
		if (!LocalDate.class.equals(rawType)) {
			return null;
		}

		return new LocalDateParamConverter<>(rawType);
	}
}
