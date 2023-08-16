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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.oss.vacme.shared.util.Util.coalesce;
import static ch.dvbern.oss.vacme.shared.util.Util.coalesceString;
import static ch.dvbern.oss.vacme.shared.util.Util.findEnumBy;
import static ch.dvbern.oss.vacme.shared.util.Util.getEnum;
import static ch.dvbern.oss.vacme.shared.util.Util.ifNull;
import static ch.dvbern.oss.vacme.shared.util.Util.ifValue;
import static ch.dvbern.oss.vacme.shared.util.Util.isIn;
import static ch.dvbern.oss.vacme.shared.util.UtilTest.FooEnum.DUPLICATE1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("EmptyClass")
public class UtilTest {

	@AllArgsConstructor
	@Getter
	public static class FormattingTester {
		private final String text;
	}

	@Nested
	class CoalesceTest {
		@Test
		void should_return_empty_on_empty_input() {
			assertThat(coalesce())
					.isEmpty();
		}

		@ParameterizedTest
		@CsvSource(value = {
				"a,a,b,c",
				"b,null,b,c",
				"a,a,null,c",
				"b,null,b,null",
				"a,a,null,null",
				"c,null,null,c",
				"X,null,null,null",
		}, nullValues = "null")
		void should_return_first_nonnull_option(
				@Nullable String expected,
				@Nullable String a,
				@Nullable String b,
				@Nullable String c
		) {
			assertThat(coalesce(a, b, c)
					.orElse("X"))
					.isEqualTo(expected);
		}
	}

	@Nested
	class CoalesceStringTest {
		@Test
		void should_return_empty_on_empty_input() {
			assertThat(coalesceString())
					.isEmpty();
		}

		@ParameterizedTest
		@CsvSource(value = {
				"a,a,b,c",
				"b,null,b,c",
				"a,a,null,c",
				"b,null,b,null",
				"a,a,null,null",
				"c,null,null,c",
				"X,null,null,null",
		}, nullValues = "null")
		void should_return_first_nonnull_option(
				@Nullable String expected,
				@Nullable String a,
				@Nullable String b,
				@Nullable String c
		) {
			assertThat(coalesceString(a, b, c)
					.orElse("X"))
					.isEqualTo(expected);
		}

		@ParameterizedTest
		@CsvSource(value = {
				"a,a,b,c",
				"b,,b,c",
				"a,a,,c",
				"b,,b,",
				"a,a,,",
				"c,,,c",
				"X,,,",
		}, nullValues = "null")
		void should_return_first_nonblank_option(
				@Nullable String expected,
				@Nullable String a,
				@Nullable String b,
				@Nullable String c
		) {
			assertThat(coalesceString(a, b, c)
					.orElse("X"))
					.isEqualTo(expected);
		}

		@ParameterizedTest
		@CsvSource(value = {
				"c,,null,c",
				"c,null,,c",
				"X,,null,null",
				"X,null,null,",
				"X,,null,",
				"X,null,,",
		}, nullValues = "null")
		void should_return_first_nonblank_nonblank_option(
				@Nullable String expected,
				@Nullable String a,
				@Nullable String b,
				@Nullable String c
		) {
			assertThat(coalesceString(a, b, c)
					.orElse("X"))
					.isEqualTo(expected);
		}
	}

	@Nested
	public class IfNullTest {

		@Nested
		class EagerTest {
			@Test
			void should_return_value_if_not_null() {
				Object value = new Object();
				Object fallback = new Object();

				assertThat(ifNull(value, fallback))
						.isEqualTo(value);
			}

			@Test
			void should_returnfallback_if_null() {
				Object value = null;
				Object fallback = new Object();

				assertThat(ifNull(value, fallback))
						.isEqualTo(fallback);
			}
		}

		@Nested
		class LazyTest {
			@Test
			void should_return_value_if_not_null() {
				Object value = new Object();
				Object fallback = new Object();

				assertThat(ifNull(value, () -> fallback))
						.isEqualTo(value);
			}

			class ValueHolder {
				@SuppressWarnings("PublicField")
				public String value = null;
			}

			@Test
			void should_not_call_fallback_if_not_null() {
				ValueHolder valueHolder = new ValueHolder();
				String value = "value";

				assertThat(ifNull(value, () -> {
					valueHolder.value = "fail";
					return "fail";
				}))
						.isEqualTo(value);

				assertThat(valueHolder.value)
						.isNull();
			}

			@Test
			void should_returnfallback_if_null() {
				Object value = null;
				Object fallback = new Object();

				assertThat(ifNull(value, () -> fallback))
						.isEqualTo(fallback);
			}

		}
	}

	public enum FooEnum {
		BAR("banana"),
		HELLO("world"),
		DUPLICATE1("duplicate"),
		DUPLICATE2("duplicate");

		private final String text;

		FooEnum(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	@Nested
	class FindEnumByTest {
		@Test
		public void should_find_nothing_on_false_predicate() {
			Optional<FooEnum> actual = findEnumBy(FooEnum.class, e -> "nothing".equals(e.getText()));
			assertThat(actual)
					.isEmpty();
		}

		@ParameterizedTest
		@CsvSource({
				"BAR,banana",
				"HELLO,world"
		})
		public void should_find_the_entry(FooEnum expected, String param) {
			Optional<FooEnum> actual = findEnumBy(FooEnum.class, e -> param.equals(e.getText()));
			assertThat(actual)
					.hasValue(expected);
		}

		@Test
		public void should_find_the_first_duplicate() {
			Optional<FooEnum> actual = findEnumBy(FooEnum.class, e -> "duplicate".equals(e.getText()));
			assertThat(actual)
					.hasValue(DUPLICATE1);
		}
	}

	@Nested
	class GetEnumTest {
		@Test
		public void should_throw_on_false_predicate() {
			assertThatThrownBy(() -> getEnum(FooEnum.class, e -> "nothing".equals(e.getText())))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@ParameterizedTest
		@CsvSource({
				"BAR,banana",
				"HELLO,world"
		})
		public void should_find_the_entry(FooEnum expected, String param) {
			FooEnum actual = getEnum(FooEnum.class, e -> param.equals(e.getText()));
			assertThat(actual)
					.isEqualTo(expected);
		}

		@Test
		public void should_find_the_first_duplicate() {
			FooEnum actual = getEnum(FooEnum.class, e -> "duplicate".equals(e.getText()));
			assertThat(actual)
					.isEqualTo(DUPLICATE1);
		}
	}

	@Nested
	class FormatFirstWithOptionalOthersTest {
		@Test
		public void should_format_no_entry_as_empty_string() {
			List<String> input = List.of();

			String s = Util.formatFirstWithOptionalOthers(input, Function.identity());

			Assertions.assertThat(s)
					.isEqualTo("");
		}

		@Test
		public void should_format_one_entry_as_the_entry_itself() {
			List<String> input = List.of("alpha");

			String s = Util.formatFirstWithOptionalOthers(input, Function.identity());

			Assertions.assertThat(s)
					.isEqualTo("alpha");
		}

		@Test
		public void should_wrap_additional_entries_in_braces() {
			List<String> input = List.of("alpha", "beta", "gamma");

			String s = Util.formatFirstWithOptionalOthers(input, Function.identity());

			Assertions.assertThat(s)
					.isEqualTo("alpha (beta, gamma)");
		}

		@Test
		public void should_format_distinct_entries_only_once() {
			var input = List.of(
					new FormattingTester("alpha"),
					new FormattingTester("beta"),
					new FormattingTester("gamma"),
					new FormattingTester("gamma"),
					new FormattingTester("beta"),
					new FormattingTester("alpha"));

			String s = Util.formatFirstWithOptionalOthers(input, FormattingTester::getText);

			Assertions.assertThat(s)
					.isEqualTo("alpha (beta, gamma)");
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Nested
	class IfValueTest {
		@Data
		class Foo {
			@SuppressWarnings("FieldMayBeStatic")
			private final String value = "hello";
		}

		@Nested
		class NullableVariantTest {
			@Test
			public void should_return_resolver_if_value_present() {
				String actual = ifValue(new Foo(), Foo::getValue);

				assertThat(actual)
						.isEqualTo("hello");
			}

			@Test
			public void should_return_resolver_if_value_missing() {
				var actual = ifValue(null, Foo::getValue);

				assertThat(actual)
						.isEqualTo(null);
			}
		}

		@Nested
		class WithFallbackResolverTest {
			@Test
			public void should_return_resolver_if_value_present() {
				String actual = ifValue(new Foo(), Foo::getValue, () -> "fallback");

				assertThat(actual)
						.isEqualTo("hello");
			}

			@Test
			public void should_return_resolver_if_value_missing() {
				var actual = ifValue(null, Foo::getValue, () -> "fallback");

				assertThat(actual)
						.isEqualTo("fallback");
			}

		}
	}

	@Nested
	class IsInTest {
		@Test
		void shouldFailOnNullAndEmptyHaystack() {
			var actual = isIn(null);

			assertThat(actual)
					.isFalse();
		}

		@Test
		void shouldAcceptOnNullAndHaystackContainingNull() {
			var actual = isIn(null, (String) null);

			assertThat(actual)
					.isTrue();
		}

		@Test
		void shouldFailOnEmptyHaystack() {
			var actual = isIn("asdf");

			assertThat(actual)
					.isFalse();
		}

		@Test
		void shouldFailOnHaystackNotContainingNeedle_1() {
			var actual = isIn("asdf", "fdas");

			assertThat(actual)
					.isFalse();
		}

		@Test
		void shouldFailOnHaystackNotContainingNeedle_2() {
			var actual = isIn("asdf", "foobar", "fdas", "xyzzy");

			assertThat(actual)
					.isFalse();
		}

		@Test
		void shouldAcceptOnHaystackContainingJustNeedle() {
			var actual = isIn("asdf", "asdf");

			assertThat(actual)
					.isTrue();
		}

		@Test
		void shouldAcceptOnHaystackContainingNeedle() {
			var actual = isIn("asdf", "fdas", "asdf", "xyzzy");

			assertThat(actual)
					.isTrue();
		}
	}

}
