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

/**
 * Typ einer Datei, welche zu einer Registrierung gehoert.
 * WICHTIG: Wird hier ein neuer Dokumenttyp eingefeugt welcher per Brief verschickt werden soll muss
 * der neue Typ dem Betrieb gemeldet werden damit das Query angepasst werden kann welches den Export zur
 * Post macht
 */
public enum RegistrierungFileTyp {

	REGISTRIERUNG_BESTAETIGUNG,
	REGISTRIERUNG_BESTAETIGUNG_FTP_FAIL,
	ONBOARDING_LETTER,
	ONBOARDING_LETTER_FTP_FAIL
}
