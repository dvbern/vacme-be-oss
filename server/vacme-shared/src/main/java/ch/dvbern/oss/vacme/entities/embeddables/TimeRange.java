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
import java.time.LocalTime;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.util.validators.CheckTimeRange;

import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtil.END_OF_DAY;

@Embeddable
@CheckTimeRange
public class TimeRange implements Serializable {

	private static final long serialVersionUID = -2321415284318466343L;

	@NotNull
	private LocalTime von;

	@NotNull
	private LocalTime bis;

	/**
	 * Only makes sense when used by JPA!.
	 */
	protected TimeRange() {
		von = LocalTime.now();
		bis = END_OF_DAY;
	}

	protected TimeRange(LocalTime von, LocalTime bis) {
		this.von = von;
		this.bis = bis;
	}

	public static TimeRange of(LocalTime von, LocalTime bis) {
		return new TimeRange(von, bis);
	}

	public static TimeRange of(TimeRange other) {
		return new TimeRange(other.getVon(), other.getBis());
	}

	/**
	 * von <= time <= bis
	 */
	public boolean isInRange(LocalTime time) {
		return !(time.isBefore(getVon()) || time.isAfter(getBis()));
	}

	public boolean hasValidRange() {
		return !getVon().isAfter(getBis());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TimeRange)) {
			return false;
		}

		TimeRange timeRange = (TimeRange) o;

		if (!getVon().equals(timeRange.getVon())) {
			return false;
		}

		return getBis().equals(timeRange.getBis());

	}

	@Override
	//CSOFF: MagicNumberCheck
	public int hashCode() {
		int result = getVon().hashCode();
		result = 31 * result + getBis().hashCode();

		return result;
	}

	public LocalTime getVon() {
		return von;
	}

	public void setVon(LocalTime von) {
		this.von = von;
	}

	public LocalTime getBis() {
		return bis;
	}

	public void setBis(LocalTime bis) {
		this.bis = bis;
	}
}
