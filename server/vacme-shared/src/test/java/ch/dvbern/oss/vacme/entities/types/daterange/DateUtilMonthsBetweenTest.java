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

import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import ch.dvbern.oss.vacme.entities.embeddables.Monat;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.params.provider.Arguments.of;

public class DateUtilMonthsBetweenTest {

	private static Monat monat(Month month, int year) {
		return Monat.of(YearMonth.of(year, month));
	}

	private static final Function<Integer, List<Monat>> ALLE_MONATE =
			(Integer jahr) -> Arrays.stream(Month.values())
					.map(m -> monat(m, jahr))
					.collect(toList());
	protected static final List<Monat> ALLE_MONATE_2019_2020 = Stream.concat(
			ALLE_MONATE.apply(2019).stream(),
			ALLE_MONATE.apply(2020).stream()).collect(toList());

	public static Stream<Arguments> shouldCalculateCorrectly() {
		return Stream.of(
				of(asList(monat(JANUARY, 2020)),
						DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-01-01")),
				of(asList(monat(JANUARY, 2020)),
						DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-01-31")),
				of(List.of(monat(JANUARY, 2020), monat(FEBRUARY, 2020)),
						DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-02-29")),
				of(List.of(monat(JANUARY, 2019), monat(FEBRUARY, 2019)),
						DateUtilTest.date("2019-01-01"), DateUtilTest.date("2019-02-28")),
				// // ganzes Jahr
				of(ALLE_MONATE.apply(2020),
						DateUtilTest.date("2020-01-01"), DateUtilTest.date("2020-12-31")),
				// // mehrere Jahre
				of(ALLE_MONATE_2019_2020,
						DateUtilTest.date("2019-01-01"), DateUtilTest.date("2020-12-31")),
				of(emptyList(),
						DateUtilTest.date("2020-12-31"), DateUtilTest.date("2020-01-01"))
		);
	}

	@ParameterizedTest
	@MethodSource
	public void shouldCalculateCorrectly(List<Monat> expected, TemporalAccessor von, TemporalAccessor bis) {
		List<Monat> actual = DateUtil.monthsBetween(von, bis);

		Assertions.assertThat(actual)
				.isEqualTo(expected);
	}
}
