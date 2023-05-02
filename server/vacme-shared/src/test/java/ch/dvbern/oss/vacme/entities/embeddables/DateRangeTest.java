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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtil.END_OF_TIME;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateRangeTest {

	private final DateRange year2015 = DateRange.of(
			LocalDate.of(2015, 1, 1),
			LocalDate.of(2015, 12, 31)
	);

	@Nested
	class HasValidRangeTest {
		@ParameterizedTest
		@CsvSource({
				"2000-01-01, 2000-01-01",
				"2000-01-01, 2000-05-12",
				"2000-01-01, 2000-12-31",
				"2000-12-31, 2000-12-31",
		})
		public void shouldValidateOnValidInput(LocalDate start, LocalDate end) {
			DateRange dateRange = DateRange.of(start, end);

			boolean actual = dateRange.hasValidRange();

			assertThat(actual)
					.isTrue();
		}

		@ParameterizedTest
		@CsvSource({
				"2000-01-01, 1999-12-31",
				"2000-12-31, 2000-01-01",
		})
		public void shouldFailOnInvalidInput(LocalDate start, LocalDate end) {
			DateRange dateRange = DateRange.of(start, end);

			boolean actual = dateRange.hasValidRange();

			assertThat(actual)
					.isFalse();
		}

	}

	@Nested
	class ContainsTest {
		@Test
		public void testContains_shouldBeFalseWhenNotInRange() {
			assertFalse(year2015.contains(LocalDate.of(2014, 12, 31)));
			assertFalse(year2015.contains(LocalDate.of(2016, 1, 1)));
		}

		@Test
		public void testContains_shouldBeTrueWhenInRange() {
			assertTrue(year2015.contains(LocalDate.of(2015, 1, 1)));
			assertTrue(year2015.contains(LocalDate.of(2015, 12, 31)));
			assertTrue(year2015.contains(LocalDate.of(2015, 2, 10)));
		}

		@Test
		public void testContains_shouldBeFalseWhenOutsideRange() {
			assertFalse(year2015.contains(DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31))));
		}

		@Test
		public void testContains_shouldBeFalseWhenPartiallyOutsideRange() {
			assertFalse(year2015.contains(DateRange.of(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31))));
		}

		@Test
		public void testContains_shouldBeTrueWhenEqualRange() {
			assertTrue(year2015.contains(DateRange.of(year2015)));
		}

		@Test
		public void testContains_shouldBeTrueWhenInnerRange() {
			assertTrue(year2015.contains(DateRange.of(LocalDate.of(2015, 1, 1), LocalDate.of(2015, 6, 3))));
		}
	}

	@Nested
	public class EqualsTest {
		@Test
		public void testEquals() {
			assertEquals(year2015, DateRange.of(year2015));
			assertNotEquals(year2015, DateRange.of(LocalDate.of(2015, 1, 2), LocalDate.of(2015, 12, 31)));
			assertNotEquals(year2015, DateRange.of(LocalDate.of(2015, 1, 1), LocalDate.of(2015, 12, 30)));
			assertNotEquals(year2015, DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31)));
		}

	}

	@Nested
	public class OverlapTest {
		@Test
		public void testGetOverlap_shouldBeEqualForIdenticalRanges() {
			assertThat(year2015.getOverlap(DateRange.of(year2015)))
					.hasValue(year2015);
		}

		@Test
		public void testGetOverlap_shouldBeTheSubRange() {
			DateRange subRange1 = DateRange.of(year2015.getGueltigAb(), year2015.getGueltigBis().minusDays(1));
			assertThat(year2015.getOverlap(subRange1))
					.hasValue(subRange1);
			assertThat(subRange1.getOverlap(year2015))
					.hasValue(subRange1);

			DateRange subRange2 = DateRange.of(year2015.getGueltigAb().plusDays(1), year2015.getGueltigBis());
			assertThat(year2015.getOverlap(subRange2))
					.hasValue(subRange2);
			assertThat(subRange2.getOverlap(year2015))
					.hasValue(subRange2);

			DateRange subRange3 = DateRange.of(
					year2015.getGueltigAb().plusDays(1),
					year2015.getGueltigBis().minusDays(1));
			assertThat(year2015.getOverlap(subRange3))
					.hasValue(subRange3);
			assertThat(subRange3.getOverlap(year2015))
					.hasValue(subRange3);
		}

		@Test
		public void testGetOverlap_schnittpunktIstStartOderEnde() {
			LocalDate schnittpunkt = LocalDate.of(1976, 11, 19);
			DateRange schnittpunktRange = DateRange.of(schnittpunkt, schnittpunkt);

			DateRange a = DateRange.of(schnittpunkt.minusYears(1), schnittpunkt);
			DateRange b = DateRange.of(schnittpunkt, schnittpunkt.plusYears(1));

			assertThat(a.getOverlap(b))
					.hasValue(schnittpunktRange);

			// nochmal andersrum
			assertThat(b.getOverlap(a))
					.hasValue(schnittpunktRange);
		}

		@Test
		public void testGetOverlap_shouldBeEmptyWhenNoOverlap() {
			assertFalse(year2015.getOverlap(DateRange.of(
					LocalDate.of(2014, 1, 1),
					LocalDate.of(2014, 12, 31))).isPresent());
		}

		@Test
		public void testGetOverlap_stichtag() {
			LocalDate stichtag = year2015.getGueltigAb();
			DateRange expected = DateRange.of(stichtag, stichtag);

			assertThat(year2015.getOverlap(DateRange.of(stichtag, stichtag)))
					.hasValue(expected);
		}

	}

	@Nested
	class GetDaysTest {
		@Test
		public void testGetDays_shouldCountDaysFromGueltigAbToGueltigBis() {
			LocalDate date = LocalDate.of(2014, 1, 1);
			DateRange singleDay = DateRange.of(date, date);
			assertEquals(1, singleDay.getDays());

			DateRange range = DateRange.of(date, date.plusDays(3));
			assertEquals(4, range.getDays());
		}

	}

	@Nested
	class EndsDayBeforeTest {
		@Test
		public void testEndsDayBefore_localdate() {
			DateRange a = year2015;
			LocalDate adjacent = year2015.getGueltigAb().plusYears(1);
			LocalDate before = adjacent.minusDays(1);
			LocalDate same = year2015.getGueltigAb();
			LocalDate copy = year2015.getGueltigAb().plusDays(0); // copy
			LocalDate after = adjacent.plusDays(1);

			assertTrue(a.endsDayBefore(adjacent));
			assertFalse(a.endsDayBefore(before));
			assertFalse(a.endsDayBefore(same));
			assertFalse(a.endsDayBefore(copy));
			assertFalse(a.endsDayBefore(after));
		}

		@Test
		public void testEndsDayBefore_daterange() {
			DateRange a = year2015;
			DateRange adjacent = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
			DateRange before = DateRange.of(LocalDate.of(2012, 1, 1), LocalDate.of(2012, 12, 31));
			DateRange intersectBefore = DateRange.of(LocalDate.of(2012, 1, 1), LocalDate.of(2015, 10, 10));
			DateRange intersectAfter = DateRange.of(LocalDate.of(2015, 3, 3), LocalDate.of(2016, 12, 31));
			DateRange after = DateRange.of(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 12, 31));

			assertTrue(a.endsDayBefore(adjacent));
			assertFalse(a.endsDayBefore(a)); // same ref
			assertFalse(a.endsDayBefore(before));
			assertFalse(a.endsDayBefore(intersectBefore));
			assertFalse(a.endsDayBefore(intersectAfter));
			assertFalse(a.endsDayBefore(after));
		}
	}

	@Nested
	public class StichtagTest {
		@Test
		public void testStichtag() {
			LocalDate d = LocalDate.now();
			LocalDate other = d.plusDays(1);
			assertTrue(DateRange.of(d).isStichtag());
			assertFalse(DateRange.of(d, other).isStichtag());
		}

	}

	@Nested
	public class WithFullWeeksTest {
		@Test
		public void testWithFullWeeks_stichtag() {
			LocalDate montag = LocalDate.of(2015, 10, 12); // Montag
			assertEquals(DayOfWeek.MONDAY, montag.getDayOfWeek()); // nur zur Sicherheit :)
			LocalDate mittwoch = LocalDate.of(2015, 10, 14); // Mittwoch
			assertEquals(DayOfWeek.WEDNESDAY, mittwoch.getDayOfWeek()); // nur zur Sicherheit :)
			LocalDate sonntag = LocalDate.of(2015, 10, 18); // Sonntag
			assertEquals(DayOfWeek.SUNDAY, sonntag.getDayOfWeek()); // nur zur Sicherheit :)

			// Stichtag
			DateRange stichtag = DateRange.of(mittwoch);
			assertEquals(montag, stichtag.withFullWeeks().getGueltigAb());
			assertEquals(sonntag, stichtag.withFullWeeks().getGueltigBis());
		}

		@Test
		public void testWithFullWeeks_range() {
			LocalDate montag = LocalDate.of(2015, 10, 12); // Montag
			assertEquals(DayOfWeek.MONDAY, montag.getDayOfWeek()); // nur zur Sicherheit :)
			LocalDate mittwoch = LocalDate.of(2015, 10, 14); // Mittwoch
			assertEquals(DayOfWeek.WEDNESDAY, mittwoch.getDayOfWeek()); // nur zur Sicherheit :)
			LocalDate donnerstag = LocalDate.of(2015, 10, 22); // Donnerstag, eine Woche spaeter
			assertEquals(DayOfWeek.THURSDAY, donnerstag.getDayOfWeek()); // nur zur Sicherheit :)
			LocalDate sonntag = LocalDate.of(2015, 10, 25); // Sonntag, eine Woche spaeter
			assertEquals(DayOfWeek.SUNDAY, sonntag.getDayOfWeek()); // nur zur Sicherheit :)

			// Stichtag
			DateRange stichtag = DateRange.of(mittwoch, donnerstag);
			assertEquals(montag, stichtag.withFullWeeks().getGueltigAb());
			assertEquals(sonntag, stichtag.withFullWeeks().getGueltigBis());
		}

	}

	@Nested
	public class WithFullMonthsTest {
		@Test
		public void testWithFullMonths() {
			LocalDate stichtag = LocalDate.of(2015, 5, 5);
			DateRange oneMonth = DateRange.of(stichtag).withFullMonths();
			assertEquals(LocalDate.of(2015, 5, 1), oneMonth.getGueltigAb());
			assertEquals(LocalDate.of(2015, 5, 31), oneMonth.getGueltigBis());

			LocalDate stichtagSchaltjahr = LocalDate.of(2016, 2, 5);
			DateRange oneMonthSchaltjahr = DateRange.of(stichtagSchaltjahr).withFullMonths();
			assertEquals(LocalDate.of(2016, 2, 1), oneMonthSchaltjahr.getGueltigAb());
			assertEquals(LocalDate.of(2016, 2, 29), oneMonthSchaltjahr.getGueltigBis());

			DateRange range = DateRange.of(LocalDate.of(2014, 5, 5), LocalDate.of(2015, 5, 5));
			DateRange multiYear = range.withFullMonths();
			assertEquals(DateRange.of(LocalDate.of(2014, 5, 1), LocalDate.of(2015, 5, 31)), multiYear);
		}

	}

	@Nested
	class WithFullYearsTest {
		@Test
		public void testWithFullYears() {
			LocalDate stichtag = LocalDate.of(2015, 5, 5);
			DateRange oneYear = DateRange.of(stichtag).withFullYears();
			assertEquals(LocalDate.of(2015, 1, 1), oneYear.getGueltigAb());
			assertEquals(LocalDate.of(2015, 12, 31), oneYear.getGueltigBis());

			DateRange range = DateRange.of(LocalDate.of(2014, 5, 5), LocalDate.of(2015, 5, 5));
			DateRange multiYear = range.withFullYears();
			assertEquals(DateRange.of(LocalDate.of(2014, 1, 1), LocalDate.of(2015, 12, 31)), multiYear);
		}
	}

	@Nested
	class CompareToTest {
		private int sign(int value) {
			//noinspection NumericCastThatLosesPrecision
			return (int) Math.signum(value);
		}

		@Test
		public void testCompareTo() {
			DateRange ref = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
			DateRange refSame = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
			DateRange later = DateRange.of(LocalDate.of(9999, 1, 1), LocalDate.of(9999, 12, 31));
			DateRange laterLonger = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(9999, 12, 31));
			DateRange before = DateRange.of(LocalDate.of(1000, 1, 1), LocalDate.of(1000, 12, 31));
			DateRange beforeLonger = DateRange.of(LocalDate.of(1000, 1, 1), LocalDate.of(2016, 12, 31));

			//noinspection EqualsWithItself
			assertEquals(0, ref.compareTo(ref));
			assertEquals(0, ref.compareTo(refSame));
			assertEquals(-1, sign(ref.compareTo(later)));
			assertEquals(-1, sign(ref.compareTo(laterLonger)));
			assertEquals(1, sign(ref.compareTo(before)));
			assertEquals(1, sign(ref.compareTo(beforeLonger)));
		}
	}

	@Nested
	class StreamingTest {
		@Test
		public void testStreamDays_shouldStreamDaysInclusive() {
			LocalDate gueltigAb = LocalDate.of(2018, 1, 1);
			LocalDate gueltigBis = LocalDate.of(2018, 1, 2);
			DateRange dateRange = DateRange.of(gueltigAb, gueltigBis);

			List<LocalDate> dates = dateRange.streamDays().collect(Collectors.toList());

			assertEquals(2, dates.size());
			assertEquals(gueltigAb, dates.get(0));
			assertEquals(gueltigBis, dates.get(1));
		}

		@Test
		public void testStreamDays_shouldStreamSingleDay() {
			LocalDate gueltigAb = LocalDate.of(2018, 1, 1);
			LocalDate gueltigBis = LocalDate.of(2018, 1, 1);
			DateRange dateRange = DateRange.of(gueltigAb, gueltigBis);

			List<LocalDate> dates = dateRange.streamDays().collect(Collectors.toList());

			assertEquals(1, dates.size());
			assertEquals(gueltigAb, dates.get(0));
		}

		@Test
		public void testStreamDays_shouldStreamEmpty() {
			LocalDate gueltigAb = LocalDate.of(2018, 1, 2);
			LocalDate gueltigBis = LocalDate.of(2018, 1, 1);
			DateRange dateRange = DateRange.of(gueltigAb, gueltigBis);

			List<LocalDate> dates = dateRange.streamDays().collect(Collectors.toList());

			assertTrue(dates.isEmpty());
		}
	}

	@Nested
	class ToFullWeekRangesTest {
		@Test
		public void testToFullWeekRanges_shouldReturnASingleWeekForAWeekRange() {
			DateRange weekRange = DateRange.of(LocalDate.now()).withFullWeeks();

			List<DateRange> actualRanges = weekRange.toFullWeekRanges();
			assertEquals(1, actualRanges.size());
			assertEquals(weekRange, actualRanges.get(0));
		}

		@Test
		public void testToFullWeekRanges_shouldReturnASingleWeekForARangeWithinAWeek() {
			// Tuesday to Wednesday in the same week
			DateRange withinAWeekRange = DateRange.of(LocalDate.of(2016, 4, 19), LocalDate.of(2016, 4, 20));

			List<DateRange> actualRanges = withinAWeekRange.toFullWeekRanges();
			assertEquals(1, actualRanges.size());
			assertEquals(withinAWeekRange.withFullWeeks(), actualRanges.get(0));
		}

		@Test
		public void testToFullWeekRanges_shouldReturnASingleTwoWeekForALongRangeOfFullWeeks() {
			DateRange twoWeekRange = DateRange.of(LocalDate.now()).withFullWeeks();
			twoWeekRange.setGueltigBis(twoWeekRange.getGueltigBis().plusWeeks(5));

			List<DateRange> actualRanges = twoWeekRange.toFullWeekRanges();
			assertEquals(1, actualRanges.size());
			assertEquals(twoWeekRange, actualRanges.get(0));
		}

		@Test
		public void testToFullWeekRanges_shouldReturnTwoRangesForARangeIntersectingTwoWeeks() {
			DateRange weednesdayToFridayNextWeek = DateRange.of(LocalDate.of(2016, 4, 13), LocalDate.of(2016, 4, 22));

			List<DateRange> actualRanges = weednesdayToFridayNextWeek.toFullWeekRanges();
			assertEquals(2, actualRanges.size());
			assertEquals(DateRange.of(weednesdayToFridayNextWeek.getGueltigAb()).withFullWeeks(), actualRanges.get(0));
			assertEquals(
					DateRange.of(weednesdayToFridayNextWeek.getGueltigBis()).withFullWeeks(),
					actualRanges.get(1));
		}

		@Test
		public void testToFullWeekRanges_shouldReturnThreeRangesForARangeIntersectingManyWeeks() {
			DateRange tuesdayToThursdayThreeWeeksLater = DateRange.of(LocalDate.of(2016, 4, 5), LocalDate.of(2016, 4,
					28));

			List<DateRange> actualRanges = tuesdayToThursdayThreeWeeksLater.toFullWeekRanges();
			assertEquals(3, actualRanges.size());

			DateRange expectedGueltigAbWeek = DateRange.of(tuesdayToThursdayThreeWeeksLater.getGueltigAb())
					.withFullWeeks();
			assertEquals(expectedGueltigAbWeek, actualRanges.get(0));

			DateRange expectedGueltigBisWeek = DateRange.of(tuesdayToThursdayThreeWeeksLater.getGueltigBis())
					.withFullWeeks();
			assertEquals(expectedGueltigBisWeek, actualRanges.get(2));

			DateRange expectedInbetweenWeeks = DateRange.of(LocalDate.of(2016, 4, 11), LocalDate.of(2016, 4, 24));
			assertEquals(expectedInbetweenWeeks, actualRanges.get(1));
		}

		@Test
		public void testToFullWeekRanges_shouldReturnTwoRangesWhenGueltigAbIsOnAMondayAnGueltigBisIsOnSundayNextWeek() {
			LocalDate monday = LocalDate.of(2016, 4, 4);
			LocalDate sunday = monday.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
			LocalDate mondayNextWeek = monday.plusWeeks(1);
			LocalDate sundayNextWeek = sunday.plusWeeks(1);
			DateRange mondayToSundayOneWeekLater = DateRange.of(monday, sundayNextWeek);

			List<DateRange> actualRanges = mondayToSundayOneWeekLater.toFullWeekRanges();
			assertEquals(2, actualRanges.size());

			DateRange expectedStartWeeks = DateRange.of(monday, sunday);
			assertEquals(expectedStartWeeks, actualRanges.get(0));

			DateRange expectedGueltigBisWeek = DateRange.of(mondayNextWeek, sundayNextWeek);
			assertEquals(expectedGueltigBisWeek, actualRanges.get(1));
		}

		@Test
		public void testToFullWeekRanges_shouldReturnTwoRangesWhenGueltigAbIsOnAMonday() {
			DateRange mondayToThursdayThreeWeeksLater = DateRange.of(LocalDate.of(2016, 4, 4), LocalDate.of(2016, 4,
					28));

			List<DateRange> actualRanges = mondayToThursdayThreeWeeksLater.toFullWeekRanges();
			assertEquals(2, actualRanges.size());

			DateRange expectedStartWeeks = DateRange.of(LocalDate.of(2016, 4, 4), LocalDate.of(2016, 4, 24));
			assertEquals(expectedStartWeeks, actualRanges.get(0));

			DateRange expectedGueltigBisWeek = DateRange.of(LocalDate.of(2016, 4, 25), LocalDate.of(2016, 5, 1));
			assertEquals(expectedGueltigBisWeek, actualRanges.get(1));
		}

		@Test
		public void testToFullWeekRanges_shouldReturnTwoRangesWhenGueltigBisIsOnASunday() {
			DateRange tuesdayToSundayThreeWeeksLater = DateRange.of(LocalDate.of(2016, 4, 5), LocalDate.of(2016, 5,
					1));

			List<DateRange> actualRanges = tuesdayToSundayThreeWeeksLater.toFullWeekRanges();
			assertEquals(2, actualRanges.size());

			DateRange expectedGueltigAbWeek = DateRange.of(LocalDate.of(2016, 4, 4), LocalDate.of(2016, 4, 10));
			assertEquals(expectedGueltigAbWeek, actualRanges.get(0));

			DateRange expectedEndWeeks = DateRange.of(LocalDate.of(2016, 4, 11), LocalDate.of(2016, 5, 1));
			assertEquals(expectedEndWeeks, actualRanges.get(1));
		}
	}

	@Nested
	class ToMonthRangesTest {
		@Test
		public void testToMonthRangesHalfMonth() {
			DateRange range = DateRange.of(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 15));
			List<DateRange> dateRanges = range.toMonthRanges();

			assertEquals(1, dateRanges.size());
			assertEquals(1, dateRanges.get(0).getGueltigAb().getDayOfMonth());
			assertEquals(15, dateRanges.get(0).getGueltigBis().getDayOfMonth());
		}

		@Test
		public void testToMonthRangesOneAndaHalfMonth() {
			DateRange range = DateRange.of(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 2, 15));
			List<DateRange> dateRanges = range.toMonthRanges();

			assertEquals(2, dateRanges.size());
			assertEquals(LocalDate.of(2017, 1, 1), dateRanges.get(0).getGueltigAb());
			assertEquals(LocalDate.of(2017, 1, 31), dateRanges.get(0).getGueltigBis());
			assertEquals(LocalDate.of(2017, 2, 1), dateRanges.get(1).getGueltigAb());
			assertEquals(LocalDate.of(2017, 2, 15), dateRanges.get(1).getGueltigBis());
		}

		@Test
		public void testToMonthRangesTwoHalfMonths() {
			DateRange range = DateRange.of(LocalDate.of(2017, 1, 15), LocalDate.of(2017, 2, 15));
			List<DateRange> dateRanges = range.toMonthRanges();

			assertEquals(2, dateRanges.size());
			assertEquals(LocalDate.of(2017, 1, 15), dateRanges.get(0).getGueltigAb());
			assertEquals(LocalDate.of(2017, 1, 31), dateRanges.get(0).getGueltigBis());
			assertEquals(LocalDate.of(2017, 2, 1), dateRanges.get(1).getGueltigAb());
			assertEquals(LocalDate.of(2017, 2, 15), dateRanges.get(1).getGueltigBis());
		}

		@Test
		public void testToMonthRangesTwoYears() {
			DateRange range = DateRange.of(LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 31));
			List<DateRange> dateRanges = range.toMonthRanges();

			assertEquals(13, dateRanges.size());
		}
	}

	@Nested
	class ExpandMonthsTest {
		@Test
		public void testExpandMonths() {
			DateRange t1 = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 1));
			assertEquals(singletonList(
					new Monat(LocalDate.of(2016, 1, 1))
			), t1.expandMonths());

			DateRange t1a = DateRange.of(LocalDate.of(2016, 1, 31), LocalDate.of(2016, 1, 1));
			assertEquals(singletonList(
					new Monat(LocalDate.of(2016, 1, 1))
			), t1a.expandMonths());

			DateRange t1b = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 31));
			assertEquals(singletonList(
					new Monat(LocalDate.of(2016, 1, 1))
			), t1b.expandMonths());

			DateRange t2 = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 2, 1));
			assertEquals(Arrays.asList(
					new Monat(LocalDate.of(2016, 1, 1)),
					new Monat(LocalDate.of(2016, 2, 1))
			), t2.expandMonths());

			DateRange t3 = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 3, 1));
			assertEquals(Arrays.asList(
					new Monat(LocalDate.of(2016, 1, 1)),
					new Monat(LocalDate.of(2016, 2, 1)),
					new Monat(LocalDate.of(2016, 3, 1))
			), t3.expandMonths());

			DateRange t3a = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 3, 31));
			assertEquals(Arrays.asList(
					new Monat(LocalDate.of(2016, 1, 1)),
					new Monat(LocalDate.of(2016, 2, 1)),
					new Monat(LocalDate.of(2016, 3, 1))
			), t3a.expandMonths());

			DateRange t3b = DateRange.of(LocalDate.of(2016, 1, 31), LocalDate.of(2016, 3, 1));
			assertEquals(Arrays.asList(
					new Monat(LocalDate.of(2016, 1, 1)),
					new Monat(LocalDate.of(2016, 2, 1)),
					new Monat(LocalDate.of(2016, 3, 1))
			), t3b.expandMonths());
		}
	}

	@Nested
	class ExceptTest {
		@Test
		public void testExcept() {
			DateRange t1r1 = DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 14));
			DateRange t1r2 = DateRange.of(LocalDate.of(2016, 8, 12), LocalDate.of(2016, 8, 13));

			assertEquals(Arrays.asList(
					DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 11)),
					DateRange.of(LocalDate.of(2016, 8, 14), LocalDate.of(2016, 8, 14))
			), t1r1.except(t1r2));

			DateRange t2r1 = DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 14));
			DateRange t2r2 = DateRange.of(LocalDate.of(2016, 8, 12), LocalDate.of(2016, 8, 15));

			assertEquals(singletonList(
					DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 11))
			), t2r1.except(t2r2));

			DateRange t3r1 = DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 14));
			DateRange t3r2 = DateRange.of(LocalDate.of(2016, 8, 15), LocalDate.of(2016, 8, 15));

			assertEquals(new ArrayList<DateRange>(), t3r1.except(t3r2));

			DateRange t4r1 = DateRange.of(LocalDate.of(2016, 8, 1), END_OF_TIME);
			DateRange t4r2 = DateRange.of(LocalDate.of(2016, 8, 1), LocalDate.of(2016, 8, 31));

			assertEquals(singletonList(
					DateRange.of(LocalDate.of(2016, 9, 1), END_OF_TIME)
			), t4r1.except(t4r2));

			DateRange t5r1 = DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 14));
			DateRange t5r2 = DateRange.of(LocalDate.of(2016, 8, 9), LocalDate.of(2016, 8, 20));
			assertEquals(new ArrayList<DateRange>(), t5r1.except(t5r2));

		}
	}
}
