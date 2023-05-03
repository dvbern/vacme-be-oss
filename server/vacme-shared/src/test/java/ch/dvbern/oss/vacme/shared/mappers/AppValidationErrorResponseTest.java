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

package ch.dvbern.oss.vacme.shared.mappers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.shared.errors.mappers.AppValidationErrorResponse;
import ch.dvbern.oss.vacme.shared.errors.mappers.AppValidationErrorResponse.Violation;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AppValidationErrorResponseTest {

	@Value
	static class Child {
		@NotNull
		private final String childValue;
	}

	@Value
	static class Parent {
		@NotEmpty
		private final String parentValue;

		@Valid
		private final List<Child> children;

		public Parent(@Nullable String parentValue, String... childValues) {
			this.parentValue = parentValue;
			children = Arrays.stream(childValues)
					.map(Child::new)
					.collect(Collectors.toList());
		}
	}

	private Validator validator = null;

	@BeforeEach
	void beforeEach() {
		validator = Validation.buildDefaultValidatorFactory()
				.getValidator();
	}

	private List<Violation> asList(Set<ConstraintViolation<?>> beanViolations) {
		var response = new AppValidationErrorResponse(beanViolations);

		// original ordering is mostly random => keep the test stable
		var result = response.getViolations().stream().
				sorted(Comparator.comparing(Violation::getPath))
				.collect(Collectors.toList());

		return result;
	}

	@Nested
	class PathTest {
		private Set<ConstraintViolation<?>> validateObject(Object o) {
			Object violationsObject = validator.validate(o);

			@SuppressWarnings("unchecked")
			Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) violationsObject;
			return violations;
		}

		private List<Violation> validate(Parent parent) {
			var result = asList(validateObject(parent));
			return result;
		}

		@Test
		@SuppressWarnings("JUnitTestMethodWithNoAssertions")
		void parent_path() {
			Parent given = new Parent(null);

			var responseViolations = validate(given);

			var parentBar = responseViolations.get(0);
			ViolationAssert.assertThat(parentBar)
					.hasPath("parentValue");
		}

		@Test
		void builds_child_paths_beanvalidation_style() {
			Parent given = new Parent("validParent", "validChild", null);

			var responseViolations = validate(given);

			assertThat(responseViolations)
					.hasSize(1);

			var childSecond = responseViolations.get(0);
			ViolationAssert.assertThat(childSecond)
					.hasPath("children[1].childValue");
		}

		@Test
		void combine_multiple_path_entries_on_multiple_hierarchies() {
			Parent given = new Parent(null, "validChild", null, null);

			var responseViolations = validate(given);

			assertThat(responseViolations)
					.hasSize(3); // one parent + 2 children validations fail

			var childSecond = responseViolations.get(0);
			ViolationAssert.assertThat(childSecond)
					.hasPath("children[1].childValue");

			var childThird = responseViolations.get(1);
			ViolationAssert.assertThat(childThird)
					.hasPath("children[2].childValue");

			var parent = responseViolations.get(2);
			ViolationAssert.assertThat(parent)
					.hasPath("parentValue");

		}
	}

	@Nested
	class MethodCallTest {
		class MyRestResource {
			public void doStuff(@Valid Parent ignored) {
				// nop
			}
		}

		private Method doStuffMethod = null;
		private MyRestResource instance = null;

		@BeforeEach
		void beforeEach() throws NoSuchMethodException {
			doStuffMethod = MyRestResource.class.getMethod("doStuff", Parent.class);

			instance = new MyRestResource();
		}

		private Set<ConstraintViolation<?>> validateCall(Parent parent) {
			Object violationsObject = validator.forExecutables()
					.validateParameters(instance, doStuffMethod, new Object[] { parent });

			@SuppressWarnings("unchecked")
			Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) violationsObject;
			return violations;
		}

		@Test
		@SuppressWarnings("JUnitTestMethodWithNoAssertions")
		void path_to_invalid_parent() {
			var violations = asList(validateCall(
					new Parent(null)));

			var parent = violations.get(0);
			ViolationAssert.assertThat(parent)
					.hasPath("parentValue");

		}

		@Test
		@SuppressWarnings("JUnitTestMethodWithNoAssertions")
		void path_to_invalid_child() {
			var violations = asList(validateCall(
					new Parent("asdf", "validChild", null)));

			var childOne = violations.get(0);
			ViolationAssert.assertThat(childOne)
					.hasPath("children[1].childValue");

		}

		@Test
		@SuppressWarnings("JUnitTestMethodWithNoAssertions")
		void paths_to_invalid_combinations() {
			var violations = asList(validateCall(
					new Parent(null, "validChild", null, null)));

			var childSecond = violations.get(0);
			ViolationAssert.assertThat(childSecond)
					.hasPath("children[1].childValue");

			var childThird = violations.get(1);
			ViolationAssert.assertThat(childThird)
					.hasPath("children[2].childValue");

			var parent = violations.get(2);
			ViolationAssert.assertThat(parent)
					.hasPath("parentValue");

		}

	}
}
