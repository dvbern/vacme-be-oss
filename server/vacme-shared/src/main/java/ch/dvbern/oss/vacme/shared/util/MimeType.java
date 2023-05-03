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

package ch.dvbern.oss.vacme.shared.util;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

import javax.activation.MimeTypeParseException;
import javax.activation.MimetypesFileTypeMap;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.google.common.base.MoreObjects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

public class MimeType implements Serializable, Comparable<MimeType> {
	private static final long serialVersionUID = -8715756994701371237L;
	private static final Logger LOG = LoggerFactory.getLogger(MimeType.class);

	public static final String APPLICATION_PDF_TEXT = "application/pdf";
	public static final MimeType APPLICATION_PDF = new MimeType(APPLICATION_PDF_TEXT);
	public static final String IMAGE_JPEG_TEXT = "application/jpeg";
	public static final MimeType IMAGE_JPEG = new MimeType(IMAGE_JPEG_TEXT);
	public static final String IMAGE_PNG_TEXT = "application/png";
	public static final MimeType IMAGE_PNG = new MimeType(IMAGE_PNG_TEXT);
	public static final String APPLICATION_OCTET_STREAM_TEXT = "application/octet-stream";
	public static final MimeType APPLICATION_OCTET_STREAM = new MimeType(APPLICATION_OCTET_STREAM_TEXT);
	public static final String APPLICATION_XML_TEXT = "application/xml";
	public static final MimeType APPLICATION_XML = new MimeType(APPLICATION_XML_TEXT);

	@NotNull
	@NotEmpty
	private final String mimeType;

	public MimeType(String mimeType) {
		this.mimeType = validated(mimeType);
	}

	public static String validated(String mimeTypeText) {
		try {
			String clean = trimToEmpty(mimeTypeText);

			var intermediate = new javax.activation.MimeType(clean);

			var result = intermediate.getBaseType();

			return result;
		} catch (IllegalArgumentException | MimeTypeParseException ignored) {
			throw new IllegalArgumentException("Illegal mime type syntax: >>>" + mimeTypeText + "<<<");
		}

	}

	public static MimeType parse(String mimeTypeText) {
		return new MimeType(mimeTypeText);
	}

	public static boolean isSyntaxValid(String mimeTypeText) {
		try {
			validated(mimeTypeText);

			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public String getMimeType() {
		return mimeType;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		MimeType other = (MimeType) o;
		return Objects.equals(getMimeType(), other.getMimeType());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getMimeType());
	}

	@Override
	public int compareTo(@Nullable MimeType o) {
		return o == null
				? -1
				: getMimeType().compareTo(o.getMimeType());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("mimeType", mimeType)
				.toString();
	}

	public static MimeType fromRawFilename(String fileName) {
		// see java resource "mime.types" for valid entries.

		MimetypesFileTypeMap mimetypesMap = new MimetypesFileTypeMap();
		// getContentType never returns null!
		String contentType = mimetypesMap.getContentType(fileName.toLowerCase(Locale.getDefault()));
		return new MimeType(contentType);
	}

	public static MimeType fromFilename(ch.dvbern.oss.vacme.shared.util.CleanFileName fileName) {
		return fromRawFilename(fileName.getFileName());
	}
}
