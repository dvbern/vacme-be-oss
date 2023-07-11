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

package ch.dvbern.oss.vacme.entities.registration;

import java.util.HashMap;
import java.util.Map;

import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum Prioritaet {

	A("A", 0, 1000000, 75, 199),
	B("B", 500000, 1000000, 0, 74),
	C("C", 0, 499999, 65, 74),
	D("D", 50000, 499999, 50, 64),
	E("E", 50000, 499999, 18, 49),
	F("F", chooseValue(5000, 20000), 49999, chooseValue(50, 18), 64),
	G("G", 5000, chooseValue(49999, 19999), 18, chooseValue(49, 64)),
	H("H", 500, 4999, 50, 64),
	I("I", 500, 4999, 18, 49),
	K("K", 50, 499, 50, 64),
	L("L", 50, 499, 18, 49),
	M("M", 0, 49, 50, 64),
	N("N", 0, 49, 18, 49),
	O("O", 50000, 499999, 16, 17),
	P("P", 50000, 499999, 12, 15),
	Q("Q", 50000, 499999, 0, 11),
	R("R", 0, 49999, 16, 17),
	S("S", 0, 49999, 12, 15),
	T("T", 0, 49999, 0, 11),
	U("U", -1, -1, -1,-1),
	V("V", -1, -1, -1,-1),
	W("W", -1, -1, -1,-1),        // Migration ZH
	X("X", -1, -1, -1, -1),       // Massenimport durch Ort der Impfung
	Y("Y", -1, -1, -1, -1),       // Ausfallprozess erfasst
	Z("Z", -1, -1, -1, -1);       // Ort der Impfung

	private static int chooseValue(int valueBE, int valueZH) {
		return isBern() ? valueBE : valueZH;
	}

	private static boolean isBern() {
		return Mandant.valueOf(MandantUtil.getMandantProperty()) == Mandant.BE;
	}


	private static final Map<String, Prioritaet> BY_CODE = new HashMap<>();
	static {
		for (Prioritaet p : values()) {
			BY_CODE.put(p.code, p);
		}
	}

	@NonNull
	private final String code;

	private final int minSumme;

	private final int maxSumme;

	private final int minAlter;

	private final int maxAlter;

	Prioritaet(@NonNull String code, int minSumme, int maxSumme, int minAlter, int maxAlter) {
		this.code = code;
		this.minSumme = minSumme;
		this.maxSumme = maxSumme;
		this.minAlter = minAlter;
		this.maxAlter = maxAlter;
	}

	@NonNull
	public String getCode() {
		return code;
	}

	public static Prioritaet valueOfCode(String code) {
		return BY_CODE.get(code);
	}

	public static Prioritaet getPrioritaet(int summe, int alter) {
		for (Prioritaet p : values()) {
			if (p.containsSumme(summe) && p.containsAlter(alter)) {
				return p;
			}
		}
		throw new IllegalStateException(String.format("Summe %d und oder Alter %d nicht in den Prioritäten gemappt", summe, alter));
	}

	private boolean containsSumme(int summe) {
		return minSumme <= summe && summe <= maxSumme;
	}

	private boolean containsAlter(int alter) {
		return minAlter <= alter && alter <= maxAlter;
	}
}
