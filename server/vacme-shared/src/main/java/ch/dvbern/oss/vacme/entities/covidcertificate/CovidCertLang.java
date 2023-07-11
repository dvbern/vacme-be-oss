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

package ch.dvbern.oss.vacme.entities.covidcertificate;

import ch.dvbern.oss.vacme.entities.registration.Sprache;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum CovidCertLang {
	DE("de"), FR("fr"), IT("it"), RM("rm");

	private String langCode;

	CovidCertLang(String langCode) {

		this.langCode = langCode;
	}

	public static CovidCertLang fromSprache(@Nullable Sprache sprache) {
		if (sprache == null) {
			return DE;
		}

		switch (sprache) {
		case DE:
		case EN:
			return DE;
		case FR:
			return FR;
		}
		return DE;

	}

	public String getLangCode() {
		return langCode;
	}
}
