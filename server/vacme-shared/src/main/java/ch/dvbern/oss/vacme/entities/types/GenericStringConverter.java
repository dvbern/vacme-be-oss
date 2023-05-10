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

package ch.dvbern.oss.vacme.entities.types;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.shared.util.InputValidationUtil.cleanToNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GenericStringConverter<Value> implements AttributeConverter<Value, String> {

	private final Function<Value, String> toDBValue;
	private final Function<String, Value> fromDBValue;
	private final Supplier<String> dbValueIfEmpty;

	protected GenericStringConverter(
			Function<Value, String> toDBValue,
			Function<String, Value> fromDBValue
	) {
		this(toDBValue, fromDBValue, () -> "");
	}

	@Override
	public String convertToDatabaseColumn(@Nullable Value attribute) {
		if (attribute == null) {
			return dbValueIfEmpty.get();
		}

		var result = toDBValue.apply(attribute);

		return result;
	}

	@Override
	public @Nullable Value convertToEntityAttribute(@Nullable String textValue) {
		try {
			boolean empty = StringUtils.isEmpty(textValue);
			if (empty) {
				return null;
			}

			var result = fromDBValue.apply(textValue);

			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException(textValue, e);
		}
	}

	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class GenericSerializer<Value> extends JsonSerializer<Value> {
		private final Class<Value> clazz;
		private final Function<Value, String> toJSONString;

		@Override
		public Class<Value> handledType() {
			return clazz;
		}

		@Override
		public void serialize(Value value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			if (value != null) {
				var textValue = toJSONString.apply(value);
				gen.writeString(textValue);
			}
		}
	}

	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class GenericDeserializer<Value> extends JsonDeserializer<Value> {
		private final Class<Value> clazz;
		private final Function<String, Value> fromJSONString;

		@Override
		public Class<?> handledType() {
			return clazz;
		}

		@Nullable
		@Override
		public Value deserialize(
				JsonParser p,
				DeserializationContext ctxt
		) throws IOException, JsonProcessingException {
			String textValue = cleanToNull(p.getValueAsString());
			if (textValue == null) {
				return null;
			}
			var result = fromJSONString.apply(textValue);

			return result;
		}
	}

	public static <Value> void registerJackson(
			SimpleModule module,
			Class<Value> clazz,
			Function<Value, String> toJSONString,
			Function<String, Value> fromJSONString
	) {

		module.addSerializer(new GenericSerializer<>(clazz, toJSONString));
		module.addDeserializer(clazz, new GenericDeserializer<>(clazz, fromJSONString));
	}

}
