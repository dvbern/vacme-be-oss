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

import javax.validation.ConstraintValidatorContext;

import ch.dvbern.oss.vacme.entities.embeddables.DateRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CheckDateRangeValidatorTest {
	private ConstraintValidatorContext ctxStub = null;

	@BeforeEach
	public void before() {
		ctxStub = Mockito.mock(ConstraintValidatorContext.class);
	}

	private static CheckDateRangeValidator instance() {
		CheckDateRangeValidator validator = new CheckDateRangeValidator();
		return validator;
	}

	@Test
	void shouldValidateOnNullInput() {
		assertThat(instance().isValid(null, ctxStub))
				.isTrue();
	}

	@Test
	void shouldFailOnInvalidInput() {
		DateRange invalidRange = DateRange.of(LocalDate.of(2020, 12, 31), LocalDate.of(2020, 1, 1));

		boolean actual = instance().isValid(invalidRange, ctxStub);

		assertThat(actual)
				.isFalse();
	}

}
