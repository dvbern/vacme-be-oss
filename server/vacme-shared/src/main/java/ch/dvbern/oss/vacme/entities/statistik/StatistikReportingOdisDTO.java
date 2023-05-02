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
 * DTO to create a statistics about the OdIs
 */
@Value
public class StatistikReportingOdisDTO {

	public static final  String[] CSV_HEADER = {
		"Name",
		"GLN",
		"Adresse_1",
		"Adresse_2",
		"PLZ",
		"Ort",
		"Identifier",
		"Mobil",
		"Oeffentlich",
		"Terminverwaltung",
		"Typ",
		"ZSR",
		"Deaktiviert"
	};

	String name;
	String gln;
	String adresse1;
	String adresse2;
	String plz;
	String ort;
	String identifier;
	String mobil;
	String oeffentlich;
	String terminverwaltung;
	String typ;
	String zsr;
	String deaktiviert;

	public String[] getFieldList(){
		String[] strings = new String[13];
		strings[0] = name;
		strings[1] = gln;
		strings[2] = adresse1;
		strings[3] = adresse2;
		strings[4] = plz;
		strings[5] = ort;
		strings[6] = identifier;
		strings[7] = mobil;
		strings[8] = oeffentlich;
		strings[9] = terminverwaltung;
		strings[10] = typ;
		strings[11] = zsr;
		strings[12] = deaktiviert;
		return strings;
	}
}
