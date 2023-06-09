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

package ch.dvbern.oss.vacme.betrug;

import java.util.Date;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class BetrugTestPayload implements Comparable<BetrugTestPayload> {

	public String id;

	@Override
	public int compareTo(@NotNull BetrugTestPayload o) {
		return this.date.compareTo(o.date);
	}

	public Date date;
	public String name;
	public String vorname;
	public String geburtsdatum;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BetrugTestPayload payload = (BetrugTestPayload) o;
		return Objects.equals(name, payload.name) && Objects.equals(vorname, payload.vorname) && Objects.equals(geburtsdatum, payload.geburtsdatum);
	}

	@Override
	public String toString() {
		return "Payload{" +
			"name='" + name + '\'' +
			", vorname='" + vorname + '\'' +
			", geburtsdatum='" + geburtsdatum + '\'' +
			'}';
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, vorname, geburtsdatum);
	}
}

