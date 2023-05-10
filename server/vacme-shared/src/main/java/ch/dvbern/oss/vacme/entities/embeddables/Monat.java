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

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
@Embeddable
// Inheritance ist fuer Embeddables nicht definiert... und Hibernate kann das nicht
// => AccessType.PROPERTY und die JPA-Columns nochmal auf dem Getter definieren
@Access(AccessType.PROPERTY)
public class Monat extends DateRange {

	private static final long serialVersionUID = -933182309665802329L;

	public static void validateYearMonth(LocalDate von, LocalDate bis) {
		checkArgument(YearMonth.from(von).equals(YearMonth.from(bis)),
				"The dates are not in the same month and year: %s/%s", von, bis);
	}

	private static DateRange validatedRange(LocalDate firstDayOfMonth, LocalDate lastDayOfMonth) {
		Preconditions.checkArgument(
				DateUtil.isFirstOfMonth(firstDayOfMonth),
				"firstDayOfMonth is not first of month: %s", firstDayOfMonth);

		checkArgument(DateUtil.isLastOfMonth(lastDayOfMonth),
				"lastDayOfMonth is not first of month: %s", lastDayOfMonth);

		validateYearMonth(firstDayOfMonth, lastDayOfMonth);

		return DateRange.of(firstDayOfMonth, lastDayOfMonth);
	}

	/**
	 * Der Monat mit den angegebenen Tagen. Die Tage muessen genau stimmen!
	 */
	protected Monat(LocalDate firstDayOfMonth, LocalDate lastDayOfMonth) {
		super(validatedRange(checkNotNull(firstDayOfMonth), checkNotNull(lastDayOfMonth)));
	}

	protected Monat(YearMonth yearMonth) {
		this(yearMonth.atDay(1));
	}

	/**
	 * Der Monat um die Range. Die Range muss genau stimmen!
	 */
	protected Monat(DateRange monthRange) {
		this(monthRange.getGueltigAb(), monthRange.getGueltigBis());
	}

	/**
	 * Der Monat um den Stichtag
	 */
	protected Monat(LocalDate stichtag) {
		this(DateRange.of(checkNotNull(stichtag)).withFullMonths());
	}

	/**
	 * Der Monat um den heutigen Tag.
	 */
	protected Monat() {
		this(DateRange.of(LocalDate.now()).withFullMonths());
	}

	public static Monat of(LocalDate firstDayOfMonth, LocalDate lastDayOfMonth) {
		return new Monat(firstDayOfMonth, lastDayOfMonth);
	}

	public static Monat of(YearMonth yearMonth) {
		return new Monat(yearMonth);
	}

	public static Monat of(DateRange monthRange) {
		return new Monat(monthRange.withFullMonths());
	}

	public static Monat of(LocalDate stichtag) {
		return new Monat(stichtag);
	}

	public static Monat create() {
		return new Monat();
	}

	public DateRange toDateRange() {
		return DateRange.of(getGueltigAb(), getGueltigBis());
	}

	@NotNull
	@Column(nullable = false, name = "gueltigAb")
	@Override
	public LocalDate getGueltigAb() {
		return super.getGueltigAb();
	}

	/**
	 * Setzt implizit auch gueltigBis!!!
	 */
	@Override
	public void setGueltigAb(LocalDate gueltigAb) {
		checkNotNull(gueltigAb);

		if (!DateUtil.isFirstOfMonth(gueltigAb)) {
			throw new IllegalArgumentException("gueltigAb is not first of month: " + gueltigAb);
		}

		LocalDate last = gueltigAb.with(TemporalAdjusters.lastDayOfMonth());
		super.setGueltigAb(gueltigAb);
		super.setGueltigBis(last);

	}

	@NotNull
	@Column(nullable = false, name = "gueltigBis")
	@Override
	public LocalDate getGueltigBis() {
		return super.getGueltigBis();
	}

	/**
	 * Setzt implizit auch gueltigAb!!!
	 */
	@Override
	public void setGueltigBis(LocalDate gueltigBis) {
		checkNotNull(gueltigBis);

		if (!DateUtil.isLastOfMonth(gueltigBis)) {
			throw new IllegalArgumentException("gueltigBis is not last of month: " + gueltigBis);
		}

		LocalDate first = gueltigBis.with(TemporalAdjusters.firstDayOfMonth());
		super.setGueltigAb(first);
		super.setGueltigBis(gueltigBis);
	}

}
