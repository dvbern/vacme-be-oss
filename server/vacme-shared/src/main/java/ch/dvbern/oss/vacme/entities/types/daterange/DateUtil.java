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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import ch.dvbern.oss.vacme.entities.embeddables.DateRange;
import ch.dvbern.oss.vacme.entities.embeddables.DateTimeRange;
import ch.dvbern.oss.vacme.entities.embeddables.Monat;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.YEARS;

public final class DateUtil {

	public static final int MAX_YEAR = 9999;
	public static final LocalDate BEGIN_OF_TIME = LocalDate.of(1900, 1, 1);
	public static final LocalDate END_OF_TIME = LocalDate.of(MAX_YEAR, 12, 31);
	public static final LocalDateTime BEGIN_OF_DATETIME = LocalDateTime.of(BEGIN_OF_TIME, LocalTime.MIDNIGHT);
	public static final LocalDateTime END_OF_DATETIME = LocalDateTime.of(END_OF_TIME, LocalTime.MIDNIGHT);

	public static final TemporalField ISO_WEEK_FIELD = IsoFields.WEEK_OF_WEEK_BASED_YEAR;
	public static final TemporalField ISO_WEEK_BASED_YEAR_FIELD = IsoFields.WEEK_BASED_YEAR;

	public static final Function<Locale, DateTimeFormatter> DEFAULT_DATE_FORMAT =
			(locale) -> DateTimeFormatter.ofPattern("dd.MM.yyyy", locale);
	public static final Function<Locale, DateTimeFormatter> LONG_DATE_FORMAT =
		(locale) -> DateTimeFormatter.ofPattern("d. MMMM yyyy", locale);
	public static final Function<Locale, DateTimeFormatter> DEFAULT_DATE_TIME_FORMAT =
		(locale) -> DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", locale);
	public static final Function<Locale, DateTimeFormatter> DEFAULT_KALENDER_DATE_WITHOUT_YEAR_FORMAT =
			(locale) -> DateTimeFormatter.ofPattern("dd. MMM.", locale);
	public static final Function<Locale, DateTimeFormatter> DEFAULT_KALENDER_DATE_FORMAT =
			(locale) -> DateTimeFormatter.ofPattern("dd. MMM. yyyy", locale);
	public static final Function<Locale, DateTimeFormatter> DEFAULT_DATETIME_FORMAT =
			(locale) -> DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", locale);
	public static final Function<Locale, DateTimeFormatter> DEFAULT_DATETIME_FORMAT_NO_SPACE =
		(locale) -> DateTimeFormatter.ofPattern("dd.MM.yyyy_HH_mm_ss", locale);
	public static final Function<Locale, DateTimeFormatter> FILENAME_DATETIME_PATTERN =
			(locale) -> DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss", locale);
	public static final Function<Locale, DateTimeFormatter> FILENAME_DATE_PATTERN =
			(locale) -> DateTimeFormatter.ofPattern("yyyy-MM-dd", locale);
	public static final Function<Locale, DateTimeFormatter> DEFAULT_MONTH_LONG_FORMAT =
			(locale) -> DateTimeFormatter.ofPattern("MMMM yyyy", locale);
	public static final Function<Locale, DateTimeFormatter> DEFAULT_MONTH_NAME_ONLY_FORMAT =
			(locale) -> DateTimeFormatter.ofPattern("MMMM", locale);
	public static final Function<Locale, DateTimeFormatter> DEFAULT_TIME_FORMAT =
			(locale) -> DateTimeFormatter.ofPattern("HH:mm", locale);

	public static final BigDecimal DAYS_IN_WEEK = BigDecimal.valueOf(7, 0);
	public static final String DEFAULT_DATERANGE_SEPARATOR = " - ";

	public static final LocalTime START_OF_DAY = LocalTime.of(0, 0);
	public static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59, 999999999);

	private DateUtil() {
		// utility class
	}

	/**
	 * Gibt ein DateRange zurueck, wenn gueltigAb gesetzt ist, sonst Optional.empty()
	 *
	 * @param gueltigAb wird zum gueltigAb und falls gueltigBis NULL ist auch zum gueltigBis im DateRange
	 * @param gueltigBis falls nicht null, wir dies zum GueltigBis im DateRange
	 * @return Gibt ein DateRange zurueck, wenn gueltigAb gesetzt ist, sonst Optional.empty()
	 */
	public static Optional<DateRange> getOptionalDateRange(
			@Nullable LocalDate gueltigAb,
			@Nullable LocalDate gueltigBis) {

		if (Objects.nonNull(gueltigBis)) {
			checkNotNull(gueltigAb);
		}

		if (Objects.nonNull(gueltigAb)) {
			return Optional.of(DateRange.of(gueltigAb, Objects.nonNull(gueltigBis) ? gueltigBis : gueltigAb));
		}

		return Optional.empty();
	}

	public static String formatKalenderwoche(LocalDate stichtag, Locale locale) {
		checkNotNull(stichtag);

		DateRange dateRange = DateRange.of(stichtag).withFullWeeks();
		String ab = dateRange.getGueltigAb().format(DEFAULT_KALENDER_DATE_WITHOUT_YEAR_FORMAT.apply(locale));
		String bis = dateRange.getGueltigBis().format(DEFAULT_KALENDER_DATE_FORMAT.apply(locale));

		return String.format("KW %02d, %s - %s", dateRange.getGueltigAb().get(DateUtil.ISO_WEEK_FIELD), ab, bis);
	}


	@NonNull
	public static String formatDate(@Nullable LocalDateTime datetimeOrNull) {
		if (datetimeOrNull == null) {
			return "";
		}
		return DateUtil.formatDate(datetimeOrNull.toLocalDate(), Locale.GERMAN);
	}


	@NonNull
	public static String formatDate(@Nullable LocalDate date) {
		if (date == null) {
			return "";
		}
		return DateUtil.formatDate(date, Locale.GERMAN);
	}

	@NonNull
	public static String formatDate(@Nullable LocalDate dateOrNull, @NonNull Locale locale) {
		if (dateOrNull == null) {
			return "";
		}
		return DEFAULT_DATE_FORMAT.apply(locale).format(dateOrNull);
	}

	@NonNull
	public static String formatLongDate(@Nullable LocalDate dateOrNull, @NonNull Locale locale) {
		if (dateOrNull == null) {
			return "";
		}
		return LONG_DATE_FORMAT.apply(locale).format(dateOrNull);
	}


	public static String formatDateRange(DateRange range, String separator, Locale locale) {
		checkNotNull(range);
		checkNotNull(separator);

		String text = DEFAULT_DATE_FORMAT.apply(locale).format(range.getGueltigAb())
				+ separator
				+ DEFAULT_DATE_FORMAT.apply(locale).format(range.getGueltigBis());

		return text;
	}

	@NonNull
	public static String formatDateTimeRange(@NonNull DateTimeRange range) {
		checkNotNull(range);
		String text = DEFAULT_DATE_FORMAT.apply(Locale.GERMAN).format(range.getVon().toLocalDate())
			+ ", "
			+ formatLocalTime(range.getVon())
			+ " - "
			+ formatLocalTime(range.getBis());
		return text;
	}

	@NonNull
	public static String formatLocalTime(@NonNull LocalDateTime time) {
		return DEFAULT_TIME_FORMAT.apply(Locale.GERMAN).format(time.toLocalTime());
	}

	/**
	 * See formatDateRange(DateRange, String) using #DEFAULT_DATERANGE_SEPARATOR
	 */

	public static String formatDateRange(DateRange range, Locale locale) {
		checkNotNull(range);

		return formatDateRange(range, DEFAULT_DATERANGE_SEPARATOR, locale);
	}

	public static DateRange ganzesJahr(LocalDate stichtag) {
		checkNotNull(stichtag);

		LocalDate gueltigAb = stichtag.with(TemporalAdjusters.firstDayOfYear());
		LocalDate gueltigBis = gueltigAb.with(TemporalAdjusters.lastDayOfYear());

		return DateRange.of(gueltigAb, gueltigBis);
	}

	public static DateRange ganzesJahr(int jahr) {
		return ganzesJahr(LocalDate.of(jahr, 1, 1));
	}

	public static DateRange vorJahr(int jahr) {
		return ganzesJahr(LocalDate.of(jahr - 1, 1, 1));
	}

	public static DateRange vorJahr(LocalDate stichtag) {
		return vorJahr(stichtag.getYear());
	}

	public static boolean isFirstOfMonth(LocalDate stichtag) {
		return stichtag.equals(stichtag.with(TemporalAdjusters.firstDayOfMonth()));
	}

	public static boolean isLastOfMonth(LocalDate stichtag) {
		return stichtag.equals(stichtag.with(TemporalAdjusters.lastDayOfMonth()));
	}

	/**
	 * Inklusive von und bis!
	 *
	 * @return list of months ordered ascending
	 */

	public static List<Monat> monthsBetween(TemporalAccessor von, TemporalAccessor bis) {
		checkNotNull(von);
		checkNotNull(bis);

		YearMonth start = YearMonth.from(von);
		YearMonth end = YearMonth.from(bis);

		long monate = start.until(end, ChronoUnit.MONTHS);

		return LongStream.rangeClosed(0, monate)
				.mapToObj(i -> Monat.of(start.plusMonths(i)))
				.sorted()
				.collect(Collectors.toList());
	}

	public static long getAge(@NonNull LocalDate geburtsdatum) {
		return getYearsBetween(geburtsdatum, LocalDate.now());
	}

	public static long getYearsBetween(@NonNull LocalDate von, @NonNull LocalDate bis) {
		return YEARS.between(von, bis);
	}

	public static long getDaysBetween(LocalDateTime von, LocalDateTime bis) {
		return DAYS.between(von, bis);
	}

	public static long getMinutesBetween(LocalDateTime von, LocalDateTime bis) {
		return MINUTES.between(von, bis);
	}

	public static String formatMonthLong(Locale locale, Monat monat) {
		return DateUtil.DEFAULT_MONTH_LONG_FORMAT.apply(locale)
				.format(monat.getGueltigAb());
	}

	public static String formatMonthName(Locale locale, Monat monat) {
		return DateUtil.DEFAULT_MONTH_NAME_ONLY_FORMAT.apply(locale)
				.format(monat.getGueltigAb());
	}

	@NonNull
	public static String formatDateTime(@NonNull LocalDateTime date) {
		checkNotNull(date);
		String text = DEFAULT_DATETIME_FORMAT_NO_SPACE.apply(Locale.GERMAN).format(date);
		return text;
	}

	public static LocalDate getEarlier(LocalDate date1, LocalDate date2) {
		return date1.isBefore(date2) ? date1 : date2;
	}

	public static LocalDateTime getEarlier(LocalDateTime date1, LocalDateTime date2) {
		return date1.isBefore(date2) ? date1 : date2;
	}

	@NonNull
	public static LocalDate getLaterDate(@NonNull LocalDate date1, @NonNull LocalDate date2) {
		Validate.notNull(date1);
		Validate.notNull(date2);
		LocalDate dateTime = getLaterDateOrNull(date1, date2);
		Validate.notNull(dateTime);
		return dateTime;
	}

	@Nullable
	public static LocalDate getLaterDateOrNull(@Nullable LocalDate date1, @Nullable LocalDate date2) {
		if (date1 != null) {
			// 1 vorhanden, 2 nicht: 1
			if (date2 == null) {
				return date1;
			}
			// beide vorhanden: spaeteres nehmen
			return date1.isAfter(date2) ? date1 : date2;
		}
		// 1 nicht vorhanden: 2
		return date2;
	}

	@Nullable
	public static LocalDate getLatestDateOrNull(@Nullable LocalDate... dates) {
		if (dates == null) {
			return null;
		}
		return Arrays
			.stream(dates)
			.filter(Objects::nonNull)
			.max(LocalDate::compareTo)
			.orElse(null);
	}

	@NonNull
	public static LocalDateTime getLaterDateTime(@NonNull LocalDateTime date1, @NonNull LocalDateTime date2) {
		Validate.notNull(date1);
		Validate.notNull(date2);
		LocalDateTime dateTime = getLaterDateTimeOrNull(date1, date2);
		Validate.notNull(dateTime);
		return dateTime;
	}

	@Nullable
	public static LocalDateTime getLaterDateTimeOrNull(@Nullable LocalDateTime date1, @Nullable LocalDateTime date2) {
		if (date1 != null) {
			// 1 vorhanden, 2 nicht: 1
			if (date2 == null) {
				return date1;
			}
			// beide vorhanden: spaeteres nehmen
			return date1.isAfter(date2) ? date1 : date2;
		}
		// 1 nicht vorhanden: 2
		return date2;
	}

	@Nullable
	public static LocalDateTime getEarlierDateTimeOrNull(@Nullable LocalDateTime date1, @Nullable LocalDateTime date2) {
		if (date1 != null) {
			// 1 vorhanden, 2 nicht: 1
			if (date2 == null) {
				return date1;
			}
			// beide vorhanden: spaeteres nehmen
			return date1.isBefore(date2) ? date1 : date2;
		}
		// 1 nicht vorhanden: 2
		return date2;
	}

	public static Date getDate(LocalDate localDate) {
		Instant instant = localDate
				.atStartOfDay(ZoneId.systemDefault())
				.toInstant();
		return Date.from(instant);
	}

	public static Date getDate(LocalDateTime localDateTime) {
		Instant instant = localDateTime
				.atZone(ZoneId.systemDefault())
				.toInstant();
		return Date.from(instant);
	}

	public static LocalDateTime getLocalDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static List<LocalDate> getDayOfWeekDates(DayOfWeek dayOfWeek, DateRange range) {
		LocalDate firstMatch = range.getGueltigAb().with(TemporalAdjusters.nextOrSame(dayOfWeek));

		if (firstMatch.isAfter(range.getGueltigBis())) {
			return Collections.emptyList();
		}

		long weeks = firstMatch.until(range.getGueltigBis(), ChronoUnit.WEEKS);

		return LongStream.rangeClosed(0, weeks)
				.mapToObj(firstMatch::plusWeeks)
				.collect(Collectors.toList());
	}

	/**
	 * Whether the given Stichtag is within the optional gueltigkeit.<br>
	 * If gueltigAb and gueltigBis are not set, returns true.<br>
	 * If only gueltigBis is not set, checks that the stichtag is not before gueltigAb.
	 */
	public static boolean stichtagWithinOptionalGueltigkeit(
			LocalDate stichtag,
			@Nullable LocalDate gueltigAb,
			@Nullable LocalDate gueltigBis) {

		if (gueltigBis != null) {
			return contains(stichtag, checkNotNull(gueltigAb), gueltigBis);
		}

		if (gueltigAb != null) {
			return !gueltigAb.isAfter(stichtag);
		}

		return true;
	}

	/**
	 * gueltigAb <= date <= gueltigBis
	 */
	public static boolean contains(
			LocalDate stichtag,
			LocalDate gueltigAb,
			LocalDate gueltigBis) {

		return !(stichtag.isBefore(gueltigAb) || stichtag.isAfter(gueltigBis));
	}

	@NonNull
	public static LocalDateTime getLastHalfHour(@NonNull LocalDateTime time) {
		return time.truncatedTo(ChronoUnit.HOURS).plusMinutes(30 * (time.getMinute() / 30));
	}

	@NonNull
	public static LocalDateTime getNextHalfHour(@NonNull LocalDateTime time) {
		return getLastHalfHour(time).plusMinutes(30);
	}

	public static boolean isToday(@NonNull LocalDateTime datum) {
		return LocalDate.now().equals(datum.toLocalDate());
	}

	public static boolean isSameDay(@NonNull LocalDateTime datum1, @NonNull LocalDateTime datum2) {
		return datum1.toLocalDate().equals(datum2.toLocalDate());
	}


}
