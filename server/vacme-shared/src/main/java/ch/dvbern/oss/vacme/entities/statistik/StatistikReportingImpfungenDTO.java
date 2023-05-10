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

@Value
public class StatistikReportingImpfungenDTO {

	public static final  String[] CSV_HEADER = {
		"Registrierungs_ID",
		"Ort_der_Impfung_ID",
		"Ort_der_Impfung_Name",
		"Ort_der_Impfung_GLN",
		"Ort_der_Impfung_Typ",
		"Termin_Impfung",
		"Impfung_am",
		"Impfstoff_Name",
		"Impfstoff_ID",
		"Impfung_extern",
		"Grundimmunisierung",
		"Impffolgenummer",
		"Impfung_selbstzahlende",
		"Immunsupprimiert",
		"Krankheit"
	};

	String registrierungsId;
	String odiIdentifier;
	String odiName;
	String odiGln;
	String odiTyp;
	String terminImpfung;
	String impfungAm;
	String impfstoffName;
	String impfstoffId;
	String impfungExtern;
	String grundimmunisierung;
	String impffolgenummer;
	String selbstzahlende;
	String immunsupprimiert;
	String krankheit;

	@SuppressWarnings("UnusedAssignment")
	public String[] getFieldList(){
		int i = 0;

		String[] strings = new String[15];
		strings[i++] = registrierungsId;
		strings[i++] = odiIdentifier;
		strings[i++] = odiName;
		strings[i++] = odiGln;
		strings[i++] = odiTyp;
		strings[i++] = terminImpfung;
		strings[i++] = impfungAm;
		strings[i++] = impfstoffName;
		strings[i++] = impfstoffId;
		strings[i++] = impfungExtern;
		strings[i++] = grundimmunisierung;
		strings[i++] = impffolgenummer;
		strings[i++] = selbstzahlende;
		strings[i++] = immunsupprimiert;
		strings[i++] = krankheit;
		return strings;
	}
}
