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

import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("EmptyClass")
class MimeTypeTest {

	@Nested
	class FromFilenameTest {
		@ParameterizedTest
		@CsvSource({
				"foo.bin,application/octet-stream",
				"foo.apng,image/apng",
				"foo.bmp,image/bmp",
				"foo.gif,image/gif",
				"foo.ico,image/x-icon",
				"foo.cur,image/x-icon",
				"foo.jpg,image/jpeg",
				"foo.jpeg,image/jpeg",
				"foo.jfif,image/jpeg",
				"foo.pjpeg,image/jpeg",
				"foo.pjp,image/jpeg",
				"foo.png,image/png",
				"foo.svg,image/svg+xml",
				"foo.tif,image/tiff",
				"foo.tiff,image/tiff",
				"foo.webp,image/webp",
		})
		void shouldResolveCorrectMimeType(String filename, String expectedMimeType) {
			var mimeType = MimeType.fromFilename(CleanFileName.parse(filename));

			assertThat(mimeType.getMimeType())
					.isEqualTo(expectedMimeType);
		}

		@ParameterizedTest
		@CsvSource({
				"'foo.bmp',image/bmp",
				"' foo.bmp',image/bmp",
				"'foo .bmp',image/bmp",
				"'foo.bmp ',image/bmp", // see CleanFileName in action
				"' foo.bmp ',image/bmp", // see CleanFileName in action
				"'foo bmp',application/octet-stream", // no extension at all!
		})
		void shouldConsiderWhitespace(String filename, String expectedMimeType) {
			var mimeType = MimeType.fromFilename(CleanFileName.parse(filename));

			assertThat(mimeType.getMimeType())
					.isEqualTo(expectedMimeType);
		}

		@ParameterizedTest
		@CsvSource({
				"foo.bmp,image/bmp",
				"foo.Bmp,image/bmp",
				"foo.BmP,image/bmp",
				"foo.BMP,image/bmp",
		})
		void shouldIgnoreCase(String filename, String expectedMimeType) {
			var mimeType = MimeType.fromFilename(CleanFileName.parse(filename));

			assertThat(mimeType.getMimeType())
					.isEqualTo(expectedMimeType);
		}
	}
}
