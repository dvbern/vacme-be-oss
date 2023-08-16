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

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import ch.dvbern.oss.vacme.entities.embeddables.Monat;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.entities.util.validators.CheckDateRange;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtil.BEGIN_OF_TIME;
import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtil.END_OF_TIME;
import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtil.ISO_WEEK_BASED_YEAR_FIELD;
import static com.google.common.base.Preconditions.checkNotNull;

// NOTICE do not tend to use lombok in this class, it will not work
// since {@link Monat} is extending it and you would get the error:
// "A method overriding another method must not redefine the parameter constraint configuration"
@Embeddable
@CheckDateRange
@Getter
@Setter
public class DateRange implements Serializable, Comparable<DateRange> {

	private static final long serialVersionUID = 8244737446639845584L;
	public static final String GUELTIG_AB_PARAMETER = "gueltigAb";
	public static final String GUELTIG_BIS_PARAMETER = "gueltigBis";

	@NotNull
	@Column(nullable = false)
	private LocalDate gueltigAb;

	@NotNull
	@Column(nullable = false)
	private LocalDate gueltigBis;

	protected DateRange(LocalDate gueltigAb, LocalDate gueltigBis) {
		this.gueltigAb = checkNotNull(gueltigAb);
		this.gueltigBis = checkNotNull(gueltigBis);
	}

	protected DateRange() {
		this(LocalDate.now(), END_OF_TIME);
	}

	protected DateRange(LocalDate stichtag) {
		this(stichtag, stichtag);
	}

	protected DateRange(DateRange gueltigkeit) {
		this(gueltigkeit.gueltigAb, gueltigkeit.gueltigBis);
	}

	/**
	 * von <= bis
	 */
	public boolean hasValidRange() {
		return !gueltigAb.isAfter(gueltigBis);
	}

	public static SortedSet<DateRange> findZeitraeume(SortedSet<LocalDate> stichtage) {
		SortedSet<DateRange> zeitraeume = new TreeSet<>();
		LocalDate von = stichtage.first();
		stichtage.remove(stichtage.first());

		for (LocalDate stichtag : stichtage) {
			// der Stichtag ist der Tag an dem die Aenderung aktiv wird => der RZeitraum endet einen Tag davor;
			DateRange zeitraum = DateRange.of(von, stichtag.minusDays(1));
			zeitraeume.add(zeitraum);

			von = stichtag;
		}

		return zeitraeume;
	}

	public static DateRange of(LocalDate gueltigAb, LocalDate gueltigBis) {
		return new DateRange(gueltigAb, gueltigBis);
	}

	/**
	 * stichtag == gueltigAb == gueltigBis
	 */
	public static DateRange of(LocalDate stichtag) {
		return new DateRange(stichtag);
	}

	/**
	 * Creates a copy.
	 */
	public static DateRange of(DateRange gueltigkeit) {
		return new DateRange(gueltigkeit);
	}

	/**
	 * Truncates time.
	 */
	public static DateRange of(LocalDateTime gueltigAb, LocalDateTime gueltigBis) {
		return new DateRange(gueltigAb.toLocalDate(), gueltigBis.toLocalDate());
	}

	public static DateRange ofFullRange() {
		return new DateRange(BEGIN_OF_TIME, END_OF_TIME);
	}

	/**
	 * Von jetzt bis zur Unendlichkeit
	 */
	public static DateRange ofTodayOnwards() {
		return startingOn(LocalDate.now());
	}

	/**
	 * Von Stichtag bis zur Unendlichkeit
	 */
	public static DateRange startingOn(LocalDate stichtag) {
		return new DateRange(stichtag, END_OF_TIME);
	}

	/**
	 * true, when the other DateRange is completely contained in this DateRange
	 */
	public boolean contains(DateRange other) {
		return !(gueltigAb.isAfter(other.gueltigAb) || gueltigBis.isBefore(other.gueltigBis));
	}

	/**
	 * gueltigAb <= date <= gueltigBis
	 */
	public boolean contains(LocalDate date) {
		return DateUtil.contains(date, gueltigAb, gueltigBis);
	}

	/**
	 * gueltigAb < date && gueltigBis < date
	 */
	public boolean isBefore(LocalDate date) {
		return gueltigAb.isBefore(date) && gueltigBis.isBefore(date);
	}

	/**
	 * gueltigAb > date && gueltigBis > date
	 */
	public boolean isAfter(LocalDate date) {
		return gueltigAb.isAfter(date) && gueltigBis.isAfter(date);
	}

	/**
	 * gueltigAb == date + 1 Day
	 */
	public boolean startsDayAfter(LocalDate date) {
		return gueltigAb.equals(date.plus(1, ChronoUnit.DAYS));
	}

	/**
	 * gueltigBis == date - 1 Day
	 */
	public boolean endsDayBefore(LocalDate date) {
		return gueltigBis.equals(date.minus(1, ChronoUnit.DAYS));
	}

	/**
	 * gueltigBis == gueltigAb
	 */
	@XmlTransient
	@JsonIgnore
	public boolean isStichtag() {
		return gueltigAb.equals(gueltigBis);
	}

	/**
	 * gueltigAb == other.gueltigBis + 1 Day
	 */
	public boolean startsDayAfter(DateRange other) {
		return startsDayAfter(other.gueltigBis);
	}

	/**
	 * gueltigBis == other.gueltigAb - 1 Day
	 */
	public boolean endsDayBefore(DateRange other) {
		return endsDayBefore(other.gueltigAb);
	}

	/**
	 * Neue DateRange, mit gueltigAb auf den vorherigen Montag und gueltigBis auf den naechsten Sonntag setzt.
	 * Use-Case z.B.: einen Stichtag auf die ganze Woche ausdehnen.
	 */
	public DateRange withFullWeeks() {
		LocalDate montag = gueltigAb.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate sonntag = gueltigBis.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

		return DateRange.of(montag, sonntag);
	}

	public List<DateRange> toFullWeekRanges() {
		DateRange gueltigAbWeek = DateRange.of(gueltigAb).withFullWeeks();
		DateRange gueltigBisWeek = DateRange.of(gueltigBis).withFullWeeks();

		if (gueltigAbWeek.intersects(gueltigBisWeek)) {
			// both dates are within the same week
			return Collections.singletonList(gueltigAbWeek);
		}

		if (gueltigAbWeek.endsDayBefore(gueltigBisWeek)) {
			// gueltigAb & gueltigBis are in two adjacent weeks
			return Arrays.asList(gueltigAbWeek, gueltigBisWeek);
		}

		LocalDate ab = gueltigAb.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
		LocalDate bis = gueltigBis.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		List<DateRange> result = new ArrayList<>();

		if (ab.isAfter(gueltigAb)) {
			result.add(gueltigAbWeek);
		}

		result.add(DateRange.of(ab, bis));

		if (bis.isBefore(gueltigBis)) {
			result.add(gueltigBisWeek);
		}

		return result;
	}

	public List<DateRange> toMonthRanges() {
		if (gueltigAb.getMonth() == gueltigBis.getMonth() && gueltigAb.getYear() == gueltigBis.getYear()) {
			// both dates are within the same month
			return Collections.singletonList(DateRange.of(gueltigAb, gueltigBis));
		}

		LocalDate monthAb = gueltigAb.with(TemporalAdjusters.firstDayOfNextMonth());
		LocalDate lastMonthBis = gueltigBis.with(TemporalAdjusters.lastDayOfMonth());

		List<DateRange> result = new ArrayList<>();

		result.add(DateRange.of(gueltigAb, gueltigAb.with(TemporalAdjusters.lastDayOfMonth())));

		while (monthAb.getMonthValue() < lastMonthBis.getMonthValue() || monthAb.getYear() < lastMonthBis.getYear()) {
			result.add(DateRange.of(monthAb, monthAb.with(TemporalAdjusters.lastDayOfMonth())));
			monthAb = monthAb.plusMonths(1);
		}

		result.add(DateRange.of(monthAb, gueltigBis));

		return result;
	}

	/**
	 * DateRange with full range but optional limits that override full range.
	 */
	public static DateRange withNullableLimits(@Nullable LocalDate gueltigAb, @Nullable LocalDate gueltigBis) {
		DateRange dateRange = DateRange.ofFullRange();

		if (gueltigAb != null) {
			dateRange.setGueltigAb(gueltigAb);
		}

		if (gueltigBis != null) {
			dateRange.setGueltigBis(gueltigBis);
		}

		return dateRange;
	}

	/**
	 * Neue DateRange, mit gueltigAb auf den ersten Tag des Monats von gueltigAb
	 * un dem lezten Tag des Monats von gueltigBs.
	 * Kann also mehrere Monate umspannen!
	 */
	public DateRange withFullMonths() {
		LocalDate firstDay = gueltigAb.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate lastDay = gueltigBis.with(TemporalAdjusters.lastDayOfMonth());

		return DateRange.of(firstDay, lastDay);
	}

	/**
	 * Liste mit allen Monaten >= gueltigAb und <= gueltigBis
	 */
	public List<Monat> expandMonths() {
		List<Monat> result = new LinkedList<>();
		LocalDate current = gueltigAb.with(TemporalAdjusters.firstDayOfMonth());

		while (current.compareTo(gueltigBis) <= 0) {
			result.add(new Monat(current));
			current = current.plusMonths(1);
		}

		return result;
	}

	/**
	 * Neue DateRange, mit gueltigAb auf den ersten Tag des Jahres von gueltigAb
	 * und gueltigBis auf den letzten Tag des Jahres von gueltigBis.
	 * Kann also mehrere Jahre umspannen!
	 */
	public DateRange withFullYears() {
		LocalDate firstDay = gueltigAb.with(TemporalAdjusters.firstDayOfYear());
		LocalDate lastDay = gueltigBis.with(TemporalAdjusters.lastDayOfYear());

		return DateRange.of(firstDay, lastDay);
	}

	/**
	 * @return Falls es zwischen dieser DateRange und otherRange eine zeitliche ueberlappung gibt, so wird diese
	 * zurueck gegeben
	 */
	public Optional<DateRange> getOverlap(DateRange otherRange) {
		if (gueltigAb.isAfter(otherRange.gueltigBis) ||
				gueltigBis.isBefore(otherRange.gueltigAb)) {
			return Optional.empty();
		}

		LocalDate ab = otherRange.gueltigAb.isAfter(gueltigAb) ?
				otherRange.gueltigAb :
				gueltigAb;

		LocalDate bis = otherRange.gueltigBis.isBefore(gueltigBis) ?
				otherRange.gueltigBis :
				gueltigBis;

		return Optional.of(DateRange.of(ab, bis));
	}

	/**
	 * {@link #getOverlap(DateRange)}.isPresent()
	 */
	public boolean intersects(DateRange other) {
		return getOverlap(other).isPresent();
	}

	/**
	 * @return Zeitraeume dieser DateRange, welche den Range von {@code other} nicht beinhalten. Falls es keine
	 * Ueberlappung gibt, wird eine leere Liste zurueck gegeben.
	 *
	 * <p>Beispiel</p>
	 * <table summary="Beispiel">
	 * <tr>
	 * <th>Objects</th>
	 * <th>Zeitstrahlen</th>
	 * </tr>
	 * <tr>
	 * <td>this</td>
	 * <td><pre>|--------|</pre></td>
	 * </tr>
	 * <tr>
	 * <td>other</td>
	 * <td><pre>   |---|  </pre></td>
	 * </tr>
	 * <tr>
	 * <td>results</td>
	 * <td><pre>|--|   |-|</pre></td>
	 * </tr>
	 * </table>
	 */
	public List<DateRange> except(DateRange other) {
		if (!intersects(other)) {
			return Collections.emptyList();
		}

		SortedSet<LocalDate> stichtage = Stream.concat(streamStichtage(), other.streamStichtage())
				.collect(Collectors.toCollection(TreeSet::new));

		return findZeitraeume(stichtage).stream()
				.filter(r -> !r.intersects(other))
				.collect(Collectors.toList());
	}

	/**
	 * @return counts the number of days between gueltigAb and gueltigBis (inclusive gueltigAb and gueltigBis)
	 */
	@XmlTransient
	@JsonIgnore
	public long getDays() {
		return ChronoUnit.DAYS.between(gueltigAb, gueltigBis) + 1;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DateRange)) {
			return false;
		}

		DateRange other = (DateRange) o;

		return 0 == compareTo(other);
	}

	@Override
	public int hashCode() {
		int result = getGueltigAb().hashCode();
		result = 31 * result + getGueltigBis().hashCode();

		return result;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add(GUELTIG_AB_PARAMETER, gueltigAb)
				.add(GUELTIG_BIS_PARAMETER, gueltigBis)
				.toString();
	}

	/**
	 * Natural ordering: zuerst gueltigAb vergleichen, dann gueltigBis
	 */
	@Override
	public int compareTo(DateRange o) {
		checkNotNull(o);

		int cmp = getGueltigAb().compareTo(o.getGueltigAb());

		if (cmp == 0) {
			cmp = getGueltigBis().compareTo(o.getGueltigBis());
		}

		return cmp;
	}

	/**
	 * Convenience: {@link Stream#of(Object)} {@link #gueltigAb}, {@link #gueltigBis}
	 */
	public Stream<LocalDate> stream() {
		return Stream.of(gueltigAb, gueltigBis);
	}

	/**
	 * Ein Stichtag ist der Tag, ab dem eine Aenderung aktiv wird.
	 * Der erste Stichtag einer DateRange ist also gueltigAb und der Zweite Stichtag ist der Tag <b>nach</b> gueltigBis
	 */
	public Stream<LocalDate> streamStichtage() {
		return Stream.of(gueltigAb, gueltigBis.plusDays(1));
	}

	/**
	 * @return a stream of all days between {@link DateRange#gueltigAb} and {@link DateRange#gueltigBis} (inclusive).
	 */
	public Stream<LocalDate> streamDays() {
		return Stream.iterate(0, i -> i + 1)
				.limit(ChronoUnit.DAYS.between(gueltigAb, gueltigBis) + 1)
				.map(i -> gueltigAb.plusDays(i));
	}

	public String format(DateTimeFormatter formatter, String joiner) {
		if (isStichtag()) {
			return gueltigAb.format(formatter);
		}

		return gueltigAb.format(formatter) + joiner + gueltigBis.format(formatter);
	}

	public String format(DateTimeFormatter formatter) {
		return format(formatter, " - ");
	}

	public static DateRange getIsoWeekYear(int jahr) {

		DateRange jahrRange = DateRange.of(LocalDate.of(jahr, 1, 1)).withFullYears();

		LocalDate gueltigAb;
		LocalDate gueltigBis;
		if (jahrRange.gueltigAb.get(ISO_WEEK_BASED_YEAR_FIELD) == jahrRange.gueltigAb.getYear()) {
			gueltigAb = jahrRange.gueltigAb.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		} else {
			gueltigAb = jahrRange.gueltigAb.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		}

		if (jahrRange.gueltigBis.get(ISO_WEEK_BASED_YEAR_FIELD) == jahrRange.gueltigBis.getYear()) {
			gueltigBis = jahrRange.gueltigBis.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		} else {
			gueltigBis = jahrRange.gueltigBis.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
		}

		return DateRange.of(gueltigAb, gueltigBis);
	}
}
