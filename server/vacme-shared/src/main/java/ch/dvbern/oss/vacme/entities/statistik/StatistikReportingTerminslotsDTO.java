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

package ch.dvbern.oss.vacme.entities.statistik;

import lombok.Value;

/**
 * DTO to create a statistic Report for the Terminsolts
 */
@Value
public class StatistikReportingTerminslotsDTO {


	public static final  String[] CSV_HEADER = {
		"Ort_der_Impfung_Name",
		"Ort_der_Impfung_GLN",
		"Slot_Kapazitaet_Impfung_1",
		"Slot_Kapazitaet_Impfung_2",
		"Slot_Kapazitaet_Impfung_N",
		"Slot_Datum",
		"Slot_Von",
		"Slot_Bis"
	};

	String ortDerImpfungName;
	String ortDerImpfungGLN;
	String slotKapazitaetImpfung1;
	String slotKapazitaetImpfung2;
	String slotKapazitaetImpfungN;
	String slotDatum;
	String slotVon;
	String slotBis;

	public String[] getFieldList(){
		String[] strings = new String[8];
		strings[0] = ortDerImpfungName;
		strings[1] = ortDerImpfungGLN;
		strings[2] = slotKapazitaetImpfung1;
		strings[3] = slotKapazitaetImpfung2;
		strings[4] = slotKapazitaetImpfungN;
		strings[5] = slotDatum;
		strings[6] = slotVon;
		strings[7] = slotBis;
		return strings;
	}
}
