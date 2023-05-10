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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Nested
@TestInstance(PER_CLASS)
class CleanFileNameTest {
	@ParameterizedTest
	@ValueSource(strings = {
			"/asdf.bin",
			"\\asdf.bin",
			"../asdf.bin",
			"..\\asdf.bin",
			"/etc/asdf.bin",
			"/etc\\asdf.bin",
			"/../asdf.bin",
			"/..\\asdf.bin",
	})
	void shoulRemovePaths(String input) {
		assertThat(CleanFileName.parse(input).getFileName())
				.isEqualTo("asdf.bin");
	}

	Set<Character> controlCharactersExceptNull() {
		// See e.g.: http://jkorpela.fi/chars/c0.html
		// 0-31 + 127
		return Stream.concat(
				// skip Null (0) as this is handled specially (i.e.: throws, see shouldThrowOnNullCharacter)
				IntStream.rangeClosed(1, 31).mapToObj(charCode -> (char) charCode),
				Stream.of((char) 127)
		).collect(toSet());
	}

	@Test
	void shouldThrowOnNullCharacter() {
		assertThrows(
				Exception.class,
				() -> CleanFileName.parse("asdf\u0000fdsa")
		);
	}

	@ParameterizedTest
	@MethodSource("controlCharactersExceptNull")
	void shouldRemoveControlCharacters(Character controlCharacter) {
		//noinspection StringConcatenationMissingWhitespace
		var filename = "asdf" + controlCharacter + "fdsa.bin";

		String actual = CleanFileName.parse(filename).getFileName();

		assertThat(actual.toCharArray())
				.doesNotContain(controlCharacter);
	}
}
