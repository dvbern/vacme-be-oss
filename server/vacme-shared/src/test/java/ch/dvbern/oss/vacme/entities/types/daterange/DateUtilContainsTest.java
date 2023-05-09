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

package ch.dvbern.oss.vacme.entities.types.daterange;

import java.time.LocalDate;
import java.util.Collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilContainsTest {
	public static Collection<Object[]> shouldEvaluateCorrectly() {
		return asList(new Object[][] {
				{ true, DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-12"
						+ "-31") },
				{ true, DateUtilTest.date("2020-05-05"), DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-12"
						+ "-31") },
				{ true, DateUtilTest.date("2020-12-31"), DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-12"
						+ "-31") },

				{ false, DateUtilTest.date("2019-12-31"), DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-12"
						+ "-31") },
				{ false, DateUtilTest.date("2019-01-01"), DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-12"
						+ "-31") },
				{ false, DateUtilTest.date("2021-01-01"), DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-12"
						+ "-31") },
				{ false, DateUtilTest.date("9999-12-31"), DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-12"
						+ "-31") },
		});
	}

	@ParameterizedTest
	@MethodSource
	public void shouldEvaluateCorrectly(boolean expected, LocalDate stichtag, LocalDate von, LocalDate bis) {
		boolean actual = DateUtil.contains(stichtag, von, bis);

		assertThat(actual)
				.isEqualTo(expected);
	}
}
