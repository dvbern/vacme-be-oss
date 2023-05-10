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

import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtilTest.date;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilStichtagWithinOptionalGueltigkeitTest {
	public static Collection<Object[]> stichtagWithinOptionalGueltigkeit_should() {
		return asList(new Object[][] {
				{ true, date("2020-01-01"), date("2020-01-01"), date("2020-12-31") },
				{ true, date("2020-05-05"), date("2020-01-01"), date("2020-12-31") },
				{ true, date("2020-12-31"), date("2020-01-01"), date("2020-12-31") },

				{ true, date("2020-01-01"), date("2020-01-01"), null },
				{ true, date("2020-05-05"), date("2020-01-01"), null },
				{ true, date("2020-01-01"), null, null },

				{ false, date("2019-12-31"), date("2020-01-01"), date("2020-12-31") },
				{ false, date("2019-01-01"), date("2020-01-01"), date("2020-12-31") },
				{ false, date("2021-01-01"), date("2020-01-01"), date("2020-12-31") },
				{ false, date("9999-12-31"), date("2020-01-01"), date("2020-12-31") },
		});
	}

	@ParameterizedTest
	@MethodSource
	public void stichtagWithinOptionalGueltigkeit_should(
			boolean expected,
			LocalDate stichtag,
			LocalDate von,
			LocalDate bis
	) {

		boolean actual = DateUtil.stichtagWithinOptionalGueltigkeit(stichtag, von, bis);

		assertThat(actual)
				.isEqualTo(expected);
	}
}
