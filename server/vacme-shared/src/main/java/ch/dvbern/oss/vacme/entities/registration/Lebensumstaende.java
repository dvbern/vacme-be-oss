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

@Schema(enumeration = {"MIT_BESONDERS_GEFAEHRDETEN_PERSON", "EINZELHAUSHALT", "FAMILIENHAUSHALT", "GEMEINSCHAFTEN",
	"MASSENUNTERKUENFTEN", "ANDERE"})
public enum Lebensumstaende {
	MIT_BESONDERS_GEFAEHRDETEN_PERSON,	// Lebe im selben Haushalt mit einer besonders gefaehrdeten Person
	EINZELHAUSHALT,                 	// Leben im Einzelhaushalt
	FAMILIENHAUSHALT,               	// Leben im Familienhaushalt
	GEMEINSCHAFTEN, 					// Leben in Gemeinschaften und Gemeinschaftseinrichtungen mit mehr als 10 Personen
	MASSENUNTERKUENFTEN,            	// Leben in Massenunterkuenften
	ANDERE,								// Andere
	UNBEKANNT							// Erstellt duerch die notfall app
}
