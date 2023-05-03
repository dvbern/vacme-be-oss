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

package ch.dvbern.oss.vacme.entities.embeddables;

import java.time.LocalTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TimeRangeTest {
	@ParameterizedTest
	@CsvSource({
			"00:00:00, 00:00:00",
			"00:00:00, 00:00:01",
			"00:00:00, 23:59:59",
			"23:59:59, 23:59:59",
			"23:59:59.0000, 23:59:59.0000",
			"23:59:59.0000, 23:59:59.9999",
	})
	public void shouldValidateOnValidInput(LocalTime start, LocalTime end) {
		TimeRange timeRange = TimeRange.of(start, end);

		boolean actual = timeRange.hasValidRange();

		assertThat(actual)
				.isTrue();
	}

	@ParameterizedTest
	@CsvSource({
			"00:00:01, 00:00:00",
			"23:59:59, 00:00:00",
			"23:59:59, 23:59:58",
			"23:59:59.9999, 23:59:59.0000",
			"23:59:59.9999, 23:59:59.9998",
	})
	public void shouldFailOnInvalidInput(LocalTime start, LocalTime end) {
		TimeRange timeRange = TimeRange.of(start, end);

		boolean actual = timeRange.hasValidRange();

		assertThat(actual)
				.isFalse();
	}

}
