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

package ch.dvbern.oss.vacme.util;

import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
public class Gsm0338 {

	private static final char[] BASIC_CHARS = {
		// Basic Character Set
		'@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '\r', 'Å', 'å',
		'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', 'Æ', 'æ', 'ß', 'É',
		' ', '!', '"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
		'¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
		'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§',
		'¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
		'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à',
		// Basic Character Set Extension
		'\f', '^', '{', '}', '\\', '[', '~', ']', '|', '€'
	};

	private static boolean[] ENCODEABLE_BY_ORD_UP_TO_253 = new boolean[254];
	private static Set<Character> ENCODEABLE_REST = new HashSet<>();

	static {
		for (int i = 0; i < BASIC_CHARS.length; i++) {
			char ch = BASIC_CHARS[i];
			if (ch <= 253) {
				ENCODEABLE_BY_ORD_UP_TO_253[ch] = true;
			} else {
				ENCODEABLE_REST.add(ch);
			}
		}
	}

	public static boolean isValidGsm0338(@NonNull String javaString) {
		final int length = javaString.length();
		for (int i = 0; i < length; ++i) {
			char ch = javaString.charAt(i);
			if (!isValidGsm0338Char(ch)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidGsm0338Char(char ch) {
		if (ch <= 253) {
			return ENCODEABLE_BY_ORD_UP_TO_253[ch];
		}
		return ENCODEABLE_REST.contains(ch);
	}

	@NonNull
	public static String transformToGsm0338String(@NonNull String message) {
		message = replaceKnownNonGsmCharacters(message);
		message = replaceUnknownNonGsmCharacters(message);
		return message;
	}

	@NonNull
	public static String replaceKnownNonGsmCharacters(@NonNull String message) {
		message = message.replace("Á", "A");
		message = message.replace("Ă", "A");
		message = message.replace("À", "A");
		message = message.replace("Â", "A");
		message = message.replace("á", "a");
		message = message.replace("â", "a");
		message = message.replace("ą", "a");
		message = message.replace("ã", "a");
		message = message.replace("ǎ", "a");
		message = message.replace("Č", "C");
		message = message.replace("Ć", "C");
		message = message.replace("ć", "c");
		message = message.replace("ç", "c");
		message = message.replace("č", "c");
		message = message.replace("Đ", "D");
		message = message.replace("Ď", "D");
		message = message.replace("đ", "d");
		message = message.replace("Ë", "E");
		message = message.replace("Ê", "E");
		message = message.replace("È", "E");
		message = message.replace("ē", "e");
		message = message.replace("ě", "e");
		message = message.replace("ễ", "e");
		message = message.replace("ę", "e");
		message = message.replace("ě", "e");
		message = message.replace("ė", "e");
		message = message.replace("ë", "e");
		message = message.replace("ê", "e");
		message = message.replace("ğ", "g");
		message = message.replace("İ", "I");
		message = message.replace("ï", "i");
		message = message.replace("ı", "i");
		message = message.replace("î", "i");
		message = message.replace("Í", "i");
		message = message.replace("í", "i");
		message = message.replace("ķ", "k");
		message = message.replace("Ľ", "L");
		message = message.replace("Ł", "L");
		message = message.replace("ľ", "l");
		message = message.replace("ł", "l");
		message = message.replace("ň", "n");
		message = message.replace("ń", "n");
		message = message.replace("Ó", "O");
		message = message.replace("Õ", "O");
		message = message.replace("Ò", "O");
		message = message.replace("ọ", "o");
		message = message.replace("ő", "o");
		message = message.replace("ō", "o");
		message = message.replace("ô", "o");
		message = message.replace("ó", "o");
		message = message.replace("õ", "o");
		message = message.replace("õ", "o");
		message = message.replace("Ř", "R");
		message = message.replace("ř", "r");
		message = message.replace("Š", "S");
		message = message.replace("Ş", "S");
		message = message.replace("š", "s");
		message = message.replace("ś", "s");
		message = message.replace("ș", "s");
		message = message.replace("ş", "s");
		message = message.replace("Ú", "U");
		message = message.replace("û", "u");
		message = message.replace("ú", "u");
		message = message.replace("ű", "u");
		message = message.replace("ū", "u");
		message = message.replace("ű", "u");
		message = message.replace("ý", "y");
		message = message.replace("Ž", "Z");
		message = message.replace("Ż", "Z");
		message = message.replace("Ż", "Z");
		message = message.replace("ž", "z");
		message = message.replace("ź", "z");
		message = message.replace("ż", "z");
		message = message.replace("ż", "z");
		message = message.replace("–", "-");
		message = message.replace("—", "-");
		message = message.replace("’", "'");
		message = message.replace("‘", "'");
		message = message.replace("`", "'");
		message = message.replace("´", "'");
		message = message.replace("ˈ", "'");
		message = message.replace("“", "\"");
		message = message.replace("”", "\"");
		message = message.replace("„", "\"");
		message = message.replace("‟", "\"");
		message = message.replace("\u200B", " ");
		message = message.replace("\u202A", " ");
		message = message.replace("\u202C", " ");
		message = message.replace("\u200F", " ");
		message = message.replace("\u00A0", " ");

		return message;
	}

	@NonNull
	public static String replaceUnknownNonGsmCharacters(@NonNull String javaString) {
		// Schnelltest, wenn alles GSM0338 ist, lassen wir den String unveraendert
		if (isValidGsm0338(javaString)) {
			return javaString;
		}

		// Sonst: alle Zeichen ausserhalb GSM0338 durch '?' ersetzen und loggen, damit wir sie noch in die Liste von bekannten Ausnahmen aufnehmen koennen
		StringBuilder sb = new StringBuilder();

		final int length = javaString.length();
		for (int i = 0; i < length; ++i) {
			char ch = javaString.charAt(i);
			if (isValidGsm0338Char(ch)) {
				sb.append(ch);
			} else {
				sb.append('?');
				LOG.warn("VACME-SMS: Unknown non-GSM_03.38 character found: {}", ch);
			}
		}
		return sb.toString();
	}
}
