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

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import ch.dvbern.oss.vacme.shared.util.MimeType;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Converter(autoApply = true)
// spec is not absolutely clear if null return is allowed but the implementation does the right thing on null
@SuppressWarnings("override.return.invalid")
public class MimeTypeConverter
		extends XmlAdapter<String, MimeType>
		implements AttributeConverter<MimeType, String>,
		com.fasterxml.jackson.databind.util.Converter<MimeType, String> {

	@Override
	public @Nullable String convertToDatabaseColumn(@Nullable MimeType attribute) {
		return asString(attribute);
	}

	@Override
	public @Nullable MimeType convertToEntityAttribute(@Nullable String dbData) {
		return fromString(dbData);
	}

	@Override
	public @Nullable MimeType unmarshal(@Nullable String v) {
		return fromString(v);
	}

	@Override
	public @Nullable String marshal(@Nullable MimeType v) {
		return asString(v);
	}

	private @Nullable String asString(@Nullable MimeType data) {
		return data == null ? null : data.getMimeType();
	}

	private @Nullable MimeType fromString(@Nullable String text) {
		return isBlank(text) ? null : new MimeType(text);
	}

	@Override
	public @Nullable String convert(@Nullable MimeType value) {
		return asString(value);
	}

	@Override
	public JavaType getInputType(TypeFactory typeFactory) {
		return SimpleType.constructUnsafe(MimeType.class);
	}

	@Override
	public JavaType getOutputType(TypeFactory typeFactory) {
		return SimpleType.constructUnsafe(String.class);
	}
}
