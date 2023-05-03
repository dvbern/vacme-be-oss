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

import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

class InputValidationUtilTest {

	@Nested
	@TestInstance(PER_CLASS)
	class RemoveAllControlCharactersTest {

		Set<Character> controlCharacters() {
			// See e.g.: http://jkorpela.fi/chars/c0.html
			// 0-31 + 127
			return Stream.concat(
					IntStream.rangeClosed(0, 31).mapToObj(charCode -> (char) charCode),
					Stream.of((char) 127)
			).collect(toSet());
		}

		@ParameterizedTest
		@MethodSource("controlCharacters")
		void shouldReplaceAllControlCharacters(char controlCharacter) {
			String actual = InputValidationUtil.removeAllControlCharacters(String.valueOf(controlCharacter));

			assertThat(actual)
					.describedAs(charDebug(controlCharacter))
					.isEqualTo("");
		}

		@ParameterizedTest
		@MethodSource("controlCharacters")
		void shouldHandleMultipleCharacters(char controlCharacter) {
			//noinspection StringConcatenationMissingWhitespace
			String input = "a" + controlCharacter + "b" + controlCharacter + "c";
			String actual = InputValidationUtil.removeAllControlCharacters(input);

			assertThat(actual)
					.describedAs(charDebug(controlCharacter))
					.isEqualTo("abc");
		}

		@Test
		void shouldLeaveNormalCharacterIntact() {
			String actual = InputValidationUtil.removeAllControlCharacters("Hello World!");

			assertThat(actual)
					.isEqualTo("Hello World!");
		}
	}

	@Nested
	@TestInstance(PER_CLASS)
	@SuppressWarnings("FieldNamingConvention")
	class RemoveUnprintableControlCharactersTest {
		private static final String TAB = "\t"; // 0x09
		private static final String LF = "\n";  // 0x0A
		private static final String CR = "\r";  // 0x0C

		Set<Character> controlCharactersExceptPrintable() {
			// See e.g.: http://jkorpela.fi/chars/c0.html
			return Stream.concat(
					Stream.concat(
							Stream.concat(
									Stream.concat(
											IntStream.rangeClosed(0, 8).mapToObj(charCode -> (char) charCode),
											Stream.of((char) 11)),
									Stream.of((char) 12)),
							IntStream.rangeClosed(14, 31).mapToObj(charCode -> (char) charCode)),
					Stream.of((char) 127)
			)
					.collect(toSet());
		}

		@ParameterizedTest
		@MethodSource("controlCharactersExceptPrintable")
		void shouldReplaceUnprintables(char controlCharacter) {
			String actual = InputValidationUtil.removeUnprintableControlCharacters(String.valueOf(controlCharacter));

			assertThat(actual)
					.describedAs(charDebug(controlCharacter))
					.isEqualTo("");
		}

		@ParameterizedTest
		@MethodSource("controlCharactersExceptPrintable")
		void shouldHandleMultipleCharacters(char controlCharacter) {
			//noinspection StringConcatenationMissingWhitespace
			String input = "a" + controlCharacter + "b" + controlCharacter + "c";
			String actual = InputValidationUtil.removeUnprintableControlCharacters(input);

			assertThat(actual)
					.describedAs(charDebug(controlCharacter))
					.isEqualTo("abc");
		}

		@ParameterizedTest
		@ValueSource(strings = {
				TAB,
				LF,
				CR,
		})
		void shoulKeepPrintablesIntact(String controlChar) {
			assertThat(InputValidationUtil.removeUnprintableControlCharacters(controlChar))
					.isEqualTo(controlChar);
		}

		@ParameterizedTest
		@ValueSource(strings = {
				TAB,
				LF,
				CR,
		})
		void shouldKeepMultiplePrintablesIntact(String controlChar) {
			String input = "a" + controlChar + "b" + controlChar + "c";

			assertThat(InputValidationUtil.removeUnprintableControlCharacters(input))
					.isEqualTo(input);
		}

		@Test
		void shouldLeaveNormalCharacterIntact() {
			String actual = InputValidationUtil.removeUnprintableControlCharacters("Hello World!");

			assertThat(actual)
					.isEqualTo("Hello World!");
		}

	}

	private static String charDebug(char character) {
		return String.format("Character: %s=0x%02X", Character.getName(character), (int) character);
	}
}
