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
import java.util.ResourceBundle;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
class TLProducerTest {

	private final LocaleProvider ignored = new LocaleProvider() {
		@Override
		public @Nullable Locale currentLocale() {
			throw new IllegalStateException("should not be needed for tests");
		}
	};

	@BeforeAll
	static void setUp() {
		System.setProperty("vacme.mandant", "BE");
	}

	@CsvSource({
			"de, de",
			"fr, de",
			"it, de"
	})
	@ParameterizedTest
	void should_load_bundle_for_locale(String localeLanguage, String expectedTranslation) {
		ResourceBundle rb = new TLProducer(ignored).loadBundle(Locale.GERMAN);
		assertThat(rb.getString("required-for-testing"))
				.isEqualTo(expectedTranslation);
	}

	@CsvSource({
		"de, de_for_zh",
		"fr, de_for_zh",
		"it, de_for_zh"
	})
	@ParameterizedTest
	void should_load_bundle_for_localeAndMandant(String localeLanguage, String expectedTranslation) {
			System.setProperty("vacme.mandant", "ZH");
		ResourceBundle rb = new TLProducer(ignored).loadBundle(Locale.GERMAN);
		assertThat(rb.getString("required-for-testing"))
			.isEqualTo(expectedTranslation);
	}

	@Test
	void should_call_initBundle_only_once() {
		TLProducer spy = Mockito.spy(new TLProducer(ignored));
		Mockito.doCallRealMethod().when(spy).initBundle(eq(Locale.GERMAN));

		spy.loadBundle(Locale.GERMAN);
		spy.loadBundle(Locale.GERMAN);

		Mockito.verify(spy).initBundle(eq(Locale.GERMAN));
	}

	@Test
	void should_take_locale_from_localeProvider() {
		// given
		// must be one of the supported locales
		Locale expectedLocale = I18NConst.SWISS_ITALIAN;

		LocaleProvider spy = Mockito.spy(LocaleProvider.class);
		Mockito.doReturn(expectedLocale).when(spy).currentLocale();

		// when
		TLProducer producer = new TLProducer(spy);
		TL tl = producer.produceTL();

		// then
		Mockito.verify(spy, times(1)).currentLocale();
		// for completeness' sake:
		assertThat(tl.getLocale())
				.isEqualTo(expectedLocale);
	}
}
