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

package ch.dvbern.oss.vacme.entities.umfrage;

/**
 * Gruppen fuer Umfragen. Jede Gruppe braucht ein eigenes programmatisches Query.
 */
public enum UmfrageGruppe {

	GRUPPE_1, // Gruppe registriert aber nicht geimpft, 16-24
	GRUPPE_2, // Gruppe mind. 1x geimpft, 16-24
	GRUPPE_3, // Gruppe registriert aber nicht geimpft, 25-49, Registrierungsdatum egal
	GRUPPE_4, // Gruppe mind. 1x geimpft, 25-49, Registrierungsdatum egal
	GRUPPE_5, // Gruppe registriert aber nicht geimpft, 25-49, nach 08.09. registriert
	GRUPPE_6; // Gruppe mind. 1x geimpft, 25-49, nach 08.09. registriert
}
