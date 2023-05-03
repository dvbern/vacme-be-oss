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

import java.util.Set;
import java.util.function.Function;

import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;

@Value
public class StatistikReportingKantonDTO {

	public static final  String[] CSV_HEADER = {
		"Registrierungs_ID",
		"Registriert_am",
		"RegistrierungsEingang",
		"Geschlecht",
		"Immobil",
		"Geburtsjahr",
		"PLZ",
		"Abgleich_elektronischer_Impfausweis",
		"Abgleich_Contact_tracing",
		"Vollstaendiger_Impfschutz",
		"Chronische_Krankheiten",
		"Lebensumstaende",
		"Beruf",
		"Imfgruppe",
		"Verstorben",
		"Immunisiert_bis",
		"Freigegeben_naechste_Impfung_ab",
		"erlaubte_impfstoffe_fuer_booster"
	};

	String registrierungsId;
	String registriertAm;
	String registrierungsEingang;
	String geschlecht;
	String immobil;
	String geburtsjahr;
	String plz;
	String abgleichElektronischerImpfausweis;
	String contactTracing;
	String vollstaendigerImpfschutz;
	String chronischeKrankheiten;
	String lebensumstaende;
	String beruf;
	String imfgruppe;
	String verstorben;
	String immunisiertBis;
	String freigegebenNaechsteImpfungAb;
	String erlaubteImpfstoffeFuerBooster;

	@SuppressWarnings("UnusedAssignment")
	public String[] getFieldList(){
		return getFieldList(StatistikUtil::mapToImpfstoffString);
	}

	public String[] getFieldList(@NonNull Set<Impfstoff> impfstoffe){
		return getFieldList(csvWithImpfstoffIds -> StatistikUtil.mapToImpfstoffString(csvWithImpfstoffIds, impfstoffe));
	}


	public String[] getFieldList(Function<String, String> impfstoffMappingFunction){
		int i = 0;

		String[] strings = new String[18];
		strings[i++] = registrierungsId;
		strings[i++] = registriertAm;
		strings[i++] = registrierungsEingang;
		strings[i++] = geschlecht;
		strings[i++] = immobil;
		strings[i++] = geburtsjahr;
		strings[i++] = plz;
		strings[i++] = abgleichElektronischerImpfausweis;
		strings[i++] = contactTracing;
		strings[i++] = vollstaendigerImpfschutz;
		strings[i++] = chronischeKrankheiten;
		strings[i++] = lebensumstaende;
		strings[i++] = beruf;
		strings[i++] = imfgruppe;
		strings[i++] = verstorben;
		strings[i++] = immunisiertBis;
		strings[i++] = freigegebenNaechsteImpfungAb;
		strings[i++] = impfstoffMappingFunction.apply(erlaubteImpfstoffeFuerBooster);
		return strings;
	}
}
