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

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(enumeration = {
	"GES_PERSONAL_MIT_PAT_KONTAKT_INTENSIV", "GES_PERSONAL_MIT_PAT_KONTAKT", "GES_PERSONAL_OHNE_PAT_KONTAKT",
	"BERUF_MIT_KUNDENKONTAKT", "BERUF_MIT_AKT_IN_GRO_ARB_GRUP", "BERUF_MIT_HOMEOFFICE_MOEGLICH",
	"BETREUUNG_VON_GEFAERD_PERSON", "PERSONAL_IN_GEMEINSCHAFTSEIN", "NICHT_ERWERBSTAETIG", "ANDERE", "UNBEKANNT"
})
public enum BeruflicheTaetigkeit {
	GES_PERSONAL_MIT_PAT_KONTAKT_INTENSIV,      // NUR FUER ZH!!!
	GES_PERSONAL_MIT_PAT_KONTAKT, 				// Gesundheitsfachpersonal mit direktem Patientenkontakt
	GES_PERSONAL_OHNE_PAT_KONTAKT,				// Gesundheitsfachpersonal ohne Patientenkontakt
	BERUF_MIT_KUNDENKONTAKT,      				// Beruf mit Kundenkontakt
	BERUF_MIT_AKT_IN_GRO_ARB_GRUP,				// Beruf mit Aktivitaeten in groesseren Arbeitsgruppen
	BERUF_MIT_HOMEOFFICE_MOEGLICH,				// Beruf mit Homeofficemoeglichkeit
	BETREUUNG_VON_GEFAERD_PERSON,				// Betreuung von besonders gefaehrdeter Person
	PERSONAL_IN_GEMEINSCHAFTSEIN,				// Personal in Gemeinschaftseinrichtungen
	NICHT_ERWERBSTAETIG,						// Nicht erwerbstaetig
	ANDERE,                        				// Andere
	UNBEKANNT									// Erstellt duerch die notfall app
}
