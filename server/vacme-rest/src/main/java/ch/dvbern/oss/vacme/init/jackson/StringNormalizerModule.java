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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.shared.util.InputValidationUtil.clean;

public class StringNormalizerModule extends SimpleModule {
	private static final long serialVersionUID = 2854608270842290484L;

	public StringNormalizerModule() {
		super();
		addDeserializer(String.class, new Deserializer());
		addSerializer(String.class, new Serializer());
	}

	public static class Serializer extends StdSerializer<String> {
		private static final long serialVersionUID = -5605146384696304927L;

		protected Serializer() {
			super(String.class);
		}

		@Override
		public void serialize(@Nullable String value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			gen.writeString(clean(value));
		}
	}

	public static class Deserializer extends StdDeserializer<String> {
		private static final long serialVersionUID = 4742264611415335441L;

		protected Deserializer() {
			super(String.class);
		}

		@Override
		public @Nullable String deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			return clean(p.getValueAsString());
		}
	}
}
