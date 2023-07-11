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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static java.util.Collections.enumeration;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

class TLImplTest {

	private final HashMap<String, @Nullable String> translationsBE = new HashMap<>();
	private final HashMap<String, @Nullable String> translationsZH = new HashMap<>();

	{
		translationsBE.put("hello", "world");
		translationsBE.put("nullValue", null);
		translationsBE.put("empty", "");
		translationsBE.put("single-argument", "hello {0}");
		translationsBE.put("arguments-in-order", "prefix {0} infix {1} suffix");
		translationsBE.put("arguments-inverse-order", "prefix {1} infix {0} suffix");
		translationsBE.put("cantonligeischt1", "Bärn isch angersch geil");
		translationsBE.put("cantonligeischt-with-arguments", "Chum mir gö i {0}");
	}
	{
		translationsBE.put("cantonligeischt", "Züri isch vil schnäller");
		translationsBE.put("cantonligeischt-with-arguments", "Chum mir gönd in {0}");
	}

	private final ResourceBundle rb = new ResourceBundle() {
		@SuppressWarnings("override.return.invalid")
		@Override
		protected @Nullable Object handleGetObject(String key) {
			return translationsBE.get(key);
		}

		@Override
		public Enumeration<String> getKeys() {
			return requireNonNull(enumeration(translationsBE.keySet()));
		}
	};

	private final ResourceBundle rb_ZH = new ResourceBundle() {
		@SuppressWarnings("override.return.invalid")
		@Override
		protected @Nullable Object handleGetObject(String key) {
			return translationsZH.get(key);
		}

		@Override
		public Enumeration<String> getKeys() {
			return requireNonNull(enumeration(translationsZH.keySet()));
		}
	};

	@Nested
	class TranslateTest {

		private final TLImpl impl = new TLImpl(Locale.GERMAN, rb_ZH, rb);

		@ParameterizedTest
		@CsvSource(delimiter = ':',
				value = {
						"hello: null: null: world",
						"nullValue: null: null: ???nullValue???",
						"missing: null: null: ???missing???",
						"empty: null: null: ''",
						"single-argument: world: : hello world",
						"arguments-in-order: first: second : prefix first infix second suffix",
						"arguments-inverse-order: first: second: prefix second infix first suffix",
						"cantonligeischt: null: null: Züri isch vil schnäller",
						"cantonligeischt-with-arguments: Zoo: null: Chum mir gönd in Zoo",
				})
		void should_produce_expected_translations(String key, String arg1, String arg2, String expected) {
			String actual = impl.translate(key, arg1, arg2);

			assertThat(actual)
					.isEqualTo(expected);

		}
	}

	@Nested
	class GetLocaleTest {
		@Test
		void should_return_the_locale() {
			TLImpl de = new TLImpl(Locale.GERMAN, null, null);
			assertThat(de.getLocale())
					.isEqualTo(Locale.GERMAN);

			TLImpl fr = new TLImpl(Locale.FRENCH, null, null);
			assertThat(fr.getLocale())
					.isEqualTo(Locale.FRENCH);
		}
	}
}
