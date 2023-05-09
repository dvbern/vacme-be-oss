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

import java.util.Locale;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(enumeration = {
	"DE", "FR", "EN",
})
public enum Sprache {

	DE(Locale.GERMAN),
	FR(Locale.FRENCH),
	EN(Locale.ENGLISH);

	private final Locale locale;

	Sprache(@NonNull Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}

	public static Sprache from(@NonNull String sprache) {
		return Sprache.valueOf(sprache.toUpperCase(Locale.ROOT));
	}
}
