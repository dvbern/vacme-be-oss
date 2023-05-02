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

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

public final class InputValidationUtil {
	private InputValidationUtil() {
		// utility class
	}

	private static final Pattern CONTROL_CHARACTERS = Pattern.compile("\\p{Cntrl}*");
	// all Control-Characters except: Tab, LF, CR, see e.g.: http://www.asciitable.com/
	private static final Pattern UNPRINTABLE_CONTROL_CHARACTERS = Pattern.compile(
			"[\\x00\\x01\\x02\\x03\\x04\\x05\\x06\\x07\\x08" /* tab: 0x09 */
					+ /* LF: 0x0A*/ "\\x0B\\x0C" +/* CR: 0x0D*/ "\\x0E\\x0F"
					+ "\\x10\\x11\\x12\\x13\\x14\\x15\\x16\\x17\\x18\\x19"
					+ "\\x1A\\x1B\\x1C\\x1D\\x1E\\x1F"
					+ "\\x7F"
					+ "\\xA0]*");

	@Contract("null -> null; !null -> !null")
	public static @Nullable String clean(@Nullable String text) {
		if (text == null) {
			return null;
		}

		return cleanToEmpty(text);
	}

	@NonNull
	public static String cleanToEmpty(@Nullable String text) {
		String trimmed = trimToEmpty(text);
		String cleaned = removeUnprintableControlCharacters(trimmed);

		return cleaned;
	}

	@Nullable
	public static String cleanToNull(@Nullable String text) {
		String trimmed = trimToNull(text);
		if (trimmed == null) {
			return null;
		}

		String cleaned = removeUnprintableControlCharacters(trimmed);

		return cleaned;
	}

	/**
	 * Remove all Control-Characters (0x00-0X1F, 0x7F)
	 * <p>
	 * See e.g.: <a href="http://www.asciitable.com/">ASCII-Table</a>.
	 * </p>
	 */
	public static String removeAllControlCharacters(String trimmed) {
		return CONTROL_CHARACTERS.matcher(trimmed).replaceAll("");
	}

	/**
	 * Remove all Control-Characters except the printable ones: Tab, LF, CR.
	 * <p>
	 * See e.g.: <a href="http://www.asciitable.com/">ASCII-Table</a>.
	 * </p>
	 */
	public static String removeUnprintableControlCharacters(@NonNull String input) {
		return UNPRINTABLE_CONTROL_CHARACTERS.matcher(input)
				.replaceAll("");
	}

	@Contract("null -> false")
	public static boolean clean(@Nullable Boolean aBoolean) {
		return aBoolean != null && aBoolean;
	}

	@Contract("null -> null; !null -> !null")
	public static @Nullable Integer clean(@Nullable Integer aInteger) {
		// nothing to do for now, just here so all properties can be treated the same
		return aInteger;
	}

	@Contract("null -> null; !null -> !null")
	public static @Nullable LocalDate clean(@Nullable LocalDate aLocalDate) {
		// nothing to do for now, just here so all properties can be treated the same
		return aLocalDate;
	}

	public static @Nullable LocalDateTime clean(@Nullable LocalDateTime aLocalDateTime) {
		return aLocalDateTime;
	}

	@Contract("null -> null; !null -> !null")
	public static @Nullable LocalDate cleanAsDate(@Nullable Date date) {
		if (date == null) {
			return null;
		}

		return date.toLocalDate();
	}

	public static @Nullable BigDecimal clean(@Nullable BigDecimal wochenstunden) {
		// nothing to do for now, just here so all properties can be treated the same
		return wochenstunden;
	}

	public static @Nullable Boolean cleanCharToBoolean(String zeroOrOne) {
		String value = cleanToNull(zeroOrOne);
		if (value == null) {
			return null;
		}

		if ("0".equals(value)) {
			return false;
		}

		if ("1".equals(value)) {
			return true;
		}

		throw new IllegalStateException("Not a valid boolean string: >" + zeroOrOne + '<');
	}

}
