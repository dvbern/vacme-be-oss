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

package ch.dvbern.oss.vacme.entities.util.validators;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("dereference.of.nullable")
class IsInPastValidatorTest {

	private static final LocalDateTime NOW = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

	private @Nullable IsInPastValidator validator = null;
	private @Nullable IsInPast annotationMock = null;

	@BeforeEach
	public void beforeEach() {
		validator = new IsInPastValidator() {
			@Override
			protected LocalDateTime now() {
				return NOW;
			}
		};
		annotationMock = Mockito.mock(IsInPast.class);
		validator.initialize(annotationMock);
	}

	@Nested
	class WithReferenceAllowedTest {

		@BeforeEach
		public void beforeEach() {
			Mockito.when(annotationMock.allowNow())
					.thenReturn(true);
		}

		@ParameterizedTest
		@CsvSource({
				"1976-01-01T14:00:00, true",
				"2019-12-31T23:59:59, true",
				"2020-01-01T00:00:00, true",
				"2020-01-01T00:00:01, false",
				"2200-01-01T00:00:01, false",

		})
		public void shouldValidateLocalDateTime(LocalDateTime reference, boolean expected) {
			boolean actual = validator.validate(reference);

			assertThat(actual)
					.isEqualTo(expected);
		}

		@ParameterizedTest
		@CsvSource({
				"1976-01-01, true",
				"2019-12-31, true",
				"2020-01-01, true",
				"2020-01-02, false",
				"2200-01-01, false",

		})
		public void shouldValidateLocalDate(LocalDate reference, boolean expected) {
			boolean actual = validator.validate(reference);

			assertThat(actual)
					.isEqualTo(expected);
		}

	}

	@Nested
	class WithReferenceDeniedTest {

		@BeforeEach
		public void beforeEach() {
			Mockito.when(annotationMock.allowNow())
					.thenReturn(false);
		}

		@ParameterizedTest
		@CsvSource({
				"1976-01-01T14:00:00, true",
				"2019-12-31T23:59:59, true",
				"2020-01-01T00:00:00, false",
				"2020-01-01T00:00:01, false",
				"2200-01-01T00:00:00, false",

		})
		public void shouldValidateLocalDateTime(LocalDateTime reference, boolean expected) {
			boolean actual = validator.validate(reference);

			assertThat(actual)
					.isEqualTo(expected);
		}

		@ParameterizedTest
		@CsvSource({
				"1976-01-01, true",
				"2019-12-31, true",
				"2020-01-01, false",
				"2020-01-02, false",
				"2200-01-01, false",

		})
		public void shouldValidateLocalDate(LocalDate reference, boolean expected) {
			boolean actual = validator.validate(reference);

			assertThat(actual)
					.isEqualTo(expected);
		}

	}
}
