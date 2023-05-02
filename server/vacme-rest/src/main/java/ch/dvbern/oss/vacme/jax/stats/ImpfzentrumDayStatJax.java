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

package ch.dvbern.oss.vacme.jax.stats;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Comparator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
public class ImpfzentrumDayStatJax implements Comparator<ImpfzentrumDayStatJax>, Serializable {

	private static final long serialVersionUID = -8210008098250853396L;

	private LocalDate datum;

	private int numberTermin1;

	private int numberTermin2;

	private int numberTerminN;

	private int kapazitaetTermin1;

	private int kapazitaetTermin2;

	private int kapazitaetTerminN;

	private int numberImpfung1;

	private int numberImpfung2;

	private int numberImpfungN;

	public ImpfzentrumDayStatJax(LocalDate localDate) {
		this.datum = localDate;
	}

	public void increaseKapazitaetTermin1(int kapazitaet) {
		this.kapazitaetTermin1 = this.kapazitaetTermin1 + kapazitaet;
	}

	public void increaseKapazitaetTermin2(int kapazitaet) {
		this.kapazitaetTermin2 = this.kapazitaetTermin2 + kapazitaet;
	}

	public void increaseKapazitaetTerminN(int kapazitaet) {
		this.kapazitaetTerminN = this.kapazitaetTerminN + kapazitaet;
	}

	public void increaseNumberTermin1() {
		this.numberTermin1 = this.numberTermin1 + 1;
	}

	public void increaseNumberTermin2() {
		this.numberTermin2 = this.numberTermin2 + 1;
	}

	public void increaseNumberTerminN() {
		this.numberTerminN = this.numberTerminN + 1;
	}

	public void increaseNumberImpfung1() {
		this.numberImpfung1 = this.numberImpfung1 + 1;
	}

	public void increaseNumberImpfung2() {
		this.numberImpfung2 = this.numberImpfung2 + 1;
	}

	public void increaseNumberImpfungN() {
		this.numberImpfungN = this.numberImpfungN + 1;
	}

	@Override
	public int compare(ImpfzentrumDayStatJax a, ImpfzentrumDayStatJax b) {
		return a.datum.compareTo(b.datum);
	}

}
