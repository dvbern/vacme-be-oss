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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.embeddables.DateRange;
import ch.dvbern.oss.vacme.entities.embeddables.Monat;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateUtilTest {
	public static final Locale LOCALE_DE = Locale.forLanguageTag("de-CH");
	public static final Locale LOCALE_FR = Locale.forLanguageTag("fr-CH");

	public static final UUID UUID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID UUID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
	public static final UUID UUID_3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Nullable
	@Contract("null->null; !null->!null")
	public static LocalDate date(@Nullable String date) {
		if (date == null) {
			return null;
		}

		return LocalDate.parse(date);
	}

	@Nullable
	@Contract("null->null; !null->!null")
	public static LocalDateTime dateTime(@Nullable String dateTime) {
		if (dateTime == null) {
			return null;
		}

		return LocalDateTime.parse(dateTime);
	}

	@Test
	public void getOptionalDateRange_shouldCreateRangeWithValues() {
		LocalDate a = date("2020-01-01");
		LocalDate b = date("2020-12-31");
		Optional<DateRange> actual = DateUtil.getOptionalDateRange(a, b);

		Assertions.assertThat(actual).hasValue(DateRange.of(a, b));
	}

	@Test
	public void getOptionalDateRange_shouldCreateRangeWithBisAsVon() {
		LocalDate a = date("2020-01-01");
		Optional<DateRange> actual = DateUtil.getOptionalDateRange(a, null);

		Assertions.assertThat(actual).hasValue(DateRange.of(a, a));
	}

	@Test
	public void getOptionalDateRange_shouldReturnMissingOnBothNullInputs() {
		Optional<DateRange> actual = DateUtil.getOptionalDateRange(null, null);

		Assertions.assertThat(actual)
				.isEmpty();
	}

	@Test
	public void getOptionalDateRange_shouldThrowIfBisIsMissing() {
		LocalDate bis = date("2020-01-01");
		assertThrows(
				NullPointerException.class,
				() -> DateUtil.getOptionalDateRange(null, bis));
	}

	@Test
	public void formatKalenderWoche_shouldFormatAsExpected() {
		String actual = DateUtil.formatKalenderwoche(date("2020-03-13"), LOCALE_DE);

		assertThat(actual, equalTo("KW 11, 09. März. - 15. März. 2020"));
	}

	@Test
	public void formatDateRange_shouldFormatAsExpected() {
		LocalDate von = date("2020-01-01");
		LocalDate bis = date("2020-03-13");
		DateRange range = DateRange.of(von, bis);
		String actual = DateUtil.formatDateRange(range, LOCALE_DE);

		assertThat(actual, is("01.01.2020 - 13.03.2020"));
	}

	@Test
	public void formatDateRange_shouldFormatAsExpected_usingSeparator() {
		LocalDate von = date("2020-01-01");
		LocalDate bis = date("2020-03-13");
		DateRange range = DateRange.of(von, bis);
		String actual = DateUtil.formatDateRange(range, " right until ", LOCALE_DE);

		assertThat(actual, is("01.01.2020 right until 13.03.2020"));
	}

	@Test
	public void ganzesJahr_shouldReturnRangeOfFullYear_forYear() {
		DateRange actual = DateUtil.ganzesJahr(2020);

		assertThat(actual, is(DateRange.of(
				date("2020-01-01"),
				date("2020-12-31"))));
	}

	@Test
	public void ganzesJahr_shouldReturnRangeOfFullYear_forStichtag() {
		DateRange actual = DateUtil.ganzesJahr(date("2020-06-05"));

		assertThat(actual, is(DateRange.of(
				date("2020-01-01"),
				date("2020-12-31"))));
	}

	@Test
	public void testVorJahr() {
		LocalDate now = LocalDate.now();
		DateRange vorjahrExpect = DateRange.of(now.minusYears(1)).withFullYears();

		assertEquals(vorjahrExpect, DateUtil.vorJahr(now));
		assertEquals(vorjahrExpect, DateUtil.vorJahr(now.getYear()));
	}

	@Test
	public void testIsFirstOfMonth() {
		for (Month month : Month.values()) {
			LocalDate first = LocalDate.of(2015, month, 1);
			LocalDate last = LocalDate.from(TemporalAdjusters.lastDayOfMonth().adjustInto(first));
			assertTrue(DateUtil.isFirstOfMonth(first));

			for (int day = 2; day <= last.getDayOfMonth(); day++) {
				assertFalse(DateUtil.isFirstOfMonth(LocalDate.of(2015, month, day)));
			}
		}
	}

	@Test
	public void testIsLastOfMonth() {
		for (Month month : Month.values()) {
			LocalDate first = LocalDate.of(2015, month, 1);
			LocalDate last = LocalDate.from(TemporalAdjusters.lastDayOfMonth().adjustInto(first));
			assertTrue(DateUtil.isLastOfMonth(last));

			for (int day = 1; day < last.getDayOfMonth(); day++) {
				assertFalse(DateUtil.isLastOfMonth(LocalDate.of(2015, month, day)));
			}
		}
	}

	@Test
	public void formatMonthLong_shouldFormatInProperLanguage() {
		Monat monat = Monat.of(YearMonth.of(2020, 5));

		String actualDE = DateUtil.formatMonthLong(LOCALE_DE, monat);
		assertThat(actualDE, equalTo("Mai 2020"));

		String actualFR = DateUtil.formatMonthLong(LOCALE_FR, monat);
		assertThat(actualFR, equalTo("mai 2020"));
	}

	@Test
	public void formatMonthName_shouldFormatInProperLanguage() {
		Monat monat = Monat.of(YearMonth.of(2020, 5));

		String actualDE = DateUtil.formatMonthName(LOCALE_DE, monat);
		assertThat(actualDE, equalTo("Mai"));

		String actualFR = DateUtil.formatMonthName(LOCALE_FR, monat);
		assertThat(actualFR, equalTo("mai"));
	}

	@Test
	public void getEarlier_shouldGetEarlierDate() {
		LocalDate a = date("2020-01-01");
		LocalDate b = date("2020-12-31");
		LocalDate c = date("2021-01-01");

		assertThat(a, equalTo(DateUtil.getEarlier(a, b)));
		assertThat(a, equalTo(DateUtil.getEarlier(b, a)));
		assertThat(a, equalTo(DateUtil.getEarlier(a, a)));

		assertThat(a, equalTo(DateUtil.getEarlier(a, c)));
		assertThat(a, equalTo(DateUtil.getEarlier(c, a)));
		assertThat(c, equalTo(DateUtil.getEarlier(c, c)));
	}

	@Test
	public void getLaterLocalDate_shouldGetLaterDate() {
		LocalDate a = date("2020-01-01");
		LocalDate b = date("2020-12-31");
		LocalDate c = date("2021-01-01");

		assertThat(b, equalTo(DateUtil.getLaterDate(a, b)));
		assertThat(b, equalTo(DateUtil.getLaterDate(b, a)));
		assertThat(b, equalTo(DateUtil.getLaterDate(b, b)));

		assertThat(c, equalTo(DateUtil.getLaterDate(a, c)));
		assertThat(c, equalTo(DateUtil.getLaterDate(c, a)));
		assertThat(c, equalTo(DateUtil.getLaterDate(c, c)));


		assertThat(a, equalTo(DateUtil.getLaterDateOrNull(a, null)));
		assertThat(b, equalTo(DateUtil.getLaterDateOrNull(b, null)));
		assertThat(c, equalTo(DateUtil.getLaterDateOrNull(c, null)));
		assertThat(a, equalTo(DateUtil.getLaterDateOrNull(null, a)));
		assertThat(b, equalTo(DateUtil.getLaterDateOrNull(null, b)));
		assertThat(c, equalTo(DateUtil.getLaterDateOrNull(null, c)));

		assertThat(null, equalTo(DateUtil.getLaterDateOrNull(null, null)));
	}

	@Test
	public void getLatestDateOrNull() {
		LocalDate a = date("2020-01-01");
		LocalDate b = date("2020-12-31");
		LocalDate c = date("2021-01-01");

		assertThat(a, equalTo(DateUtil.getLatestDateOrNull(a, null)));
		assertThat(b, equalTo(DateUtil.getLatestDateOrNull(b, null)));
		assertThat(c, equalTo(DateUtil.getLatestDateOrNull(c, null)));
		assertThat(a, equalTo(DateUtil.getLatestDateOrNull(null, a)));
		assertThat(b, equalTo(DateUtil.getLatestDateOrNull(null, b)));
		assertThat(c, equalTo(DateUtil.getLatestDateOrNull(null, c)));

		assertThat(b, equalTo(DateUtil.getLatestDateOrNull(a, null, b)));
		assertThat(b, equalTo(DateUtil.getLatestDateOrNull(null, b, null)));
		assertThat(c, equalTo(DateUtil.getLatestDateOrNull(c, null, null)));
		assertThat(null, equalTo(DateUtil.getLatestDateOrNull(null, null, null)));
	}

	@Test
	public void getLaterLocalDateTime_shouldGetLaterDate() {
		LocalDateTime a = dateTime("2019-12-31T23:59:59");
		LocalDateTime b = dateTime("2020-01-01T00:00:00");
		LocalDateTime c = dateTime("2020-12-31T23:59:59");

		assertThat(b, equalTo(DateUtil.getLaterDateTime(a, b)));
		assertThat(b, equalTo(DateUtil.getLaterDateTime(b, a)));
		assertThat(b, equalTo(DateUtil.getLaterDateTime(b, b)));

		assertThat(c, equalTo(DateUtil.getLaterDateTime(b, c)));
		assertThat(c, equalTo(DateUtil.getLaterDateTime(c, b)));
		assertThat(c, equalTo(DateUtil.getLaterDateTime(c, c)));


		assertThat(a, equalTo(DateUtil.getLaterDateTimeOrNull(a, null)));
		assertThat(b, equalTo(DateUtil.getLaterDateTimeOrNull(b, null)));
		assertThat(c, equalTo(DateUtil.getLaterDateTimeOrNull(c, null)));
		assertThat(a, equalTo(DateUtil.getLaterDateTimeOrNull(null, a)));
		assertThat(b, equalTo(DateUtil.getLaterDateTimeOrNull(null, b)));
		assertThat(c, equalTo(DateUtil.getLaterDateTimeOrNull(null, c)));

		assertThat(null, equalTo(DateUtil.getLaterDateTimeOrNull(null, null)));
	}

	@Test
	public void testGetDayOfWeekDates_noMatchingDayOfWeek() {
		LocalDate thursday = LocalDate.of(2018, Month.MARCH, 1);
		DateRange singleDay = DateRange.of(thursday, thursday);

		Arrays.stream(DayOfWeek.values())
				.filter(dayOfWeek -> dayOfWeek != thursday.getDayOfWeek())
				.forEach(dayOfWeek -> assertThat(DateUtil.getDayOfWeekDates(dayOfWeek, singleDay), empty()));
	}

	@Test
	public void testGetDayOfWeekDates_singleMatch() {
		LocalDate thursday = LocalDate.of(2018, Month.MARCH, 1);
		DateRange singleDay = DateRange.of(thursday, thursday);

		List<LocalDate> localDates = DateUtil.getDayOfWeekDates(DayOfWeek.THURSDAY, singleDay);
		assertThat(localDates, hasSize(1));
	}

	@Test
	public void testGetDayOfWeekDates_multipleMatchches() {
		LocalDate start = LocalDate.of(2018, Month.MARCH, 1);
		LocalDate end = LocalDate.of(2018, Month.MARCH, 15);

		List<LocalDate> localDates = DateUtil.getDayOfWeekDates(DayOfWeek.THURSDAY, DateRange.of(start, end));
		assertThat(localDates, contains(
				LocalDate.of(2018, Month.MARCH, 1),
				LocalDate.of(2018, Month.MARCH, 8),
				LocalDate.of(2018, Month.MARCH, 15)
		));
	}

	@Test
	public void StichtagWithinOptionalGueltigkeitTest_shouldThrowIfOnlyGueltigAbIsNull() {
		assertThrows(
				NullPointerException.class,
				() -> DateUtil.stichtagWithinOptionalGueltigkeit(date("2020-01-01"), null, date("2020-01-01")));
	}

	@Test
	public void getLastHalfHour() {
		assertEquals(
			LocalTime.of(3, 0, 0, 0),
			DateUtil.getLastHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 12, 55, 23))).toLocalTime());

		assertEquals(
			LocalTime.of(3, 0, 0, 0),
			DateUtil.getLastHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0, 0, 0))).toLocalTime());

		assertEquals(
			LocalTime.of(3, 0, 0, 0),
			DateUtil.getLastHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 29, 59, 59))).toLocalTime());

		assertEquals(
			LocalTime.of(18, 30, 0, 0),
			DateUtil.getLastHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 30, 0, 00))).toLocalTime());

		assertEquals(
			LocalTime.of(18, 0, 0, 0),
			DateUtil.getLastHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0, 0, 00))).toLocalTime());
	}

	@Test
	public void getNextHalfHour() {
		assertEquals(
			LocalTime.of(3, 30, 0, 0),
			DateUtil.getNextHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 12, 55, 23))).toLocalTime());

		assertEquals(
			LocalTime.of(3, 30, 0, 0),
			DateUtil.getNextHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0, 0, 0))).toLocalTime());

		assertEquals(
			LocalTime.of(3, 30, 0, 0),
			DateUtil.getNextHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 29, 59, 59))).toLocalTime());

		assertEquals(
			LocalTime.of(19, 0, 0, 0),
			DateUtil.getNextHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 30, 0, 00))).toLocalTime());

		assertEquals(
			LocalTime.of(18, 30, 0, 0),
			DateUtil.getNextHalfHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0, 0, 00))).toLocalTime());
	}
}
