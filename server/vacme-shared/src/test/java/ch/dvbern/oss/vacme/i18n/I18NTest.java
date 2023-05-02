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

package ch.dvbern.oss.vacme.i18n;

import java.util.Locale;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class I18NTest {

	@ParameterizedTest
	@CsvSource({
			"de-CH, de-CH",
			"de, de-CH",
			"fr-CH, fr-CH",
			"fr, fr-CH",
			"it-CH, it-CH",
			"it, it-CH",
			"en-US, en-CH",
			"en, en-CH",
			// fallback to DEFAULT_LOCALE
			"de, de-CH",
			"'', de-CH",
			", de-CH"
	})
	public void should_fall_back_correctly(@Nullable String languageTag, String expectedFallback) {
		Locale locale = languageTag == null ? null : Locale.forLanguageTag(languageTag);

		Locale actual = new I18N().resolveLocale(locale);
		Locale expected = Locale.forLanguageTag(expectedFallback);

		assertThat(actual)
				.isEqualTo(expected);
	}

}
