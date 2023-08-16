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

package ch.dvbern.oss.vacme.i18n;

import java.util.Locale;
import java.util.Set;

public final class I18NConst {
	public static final Locale SWISS_GERMAN = Locale.forLanguageTag("de-CH");
	public static final Locale SWISS_FRENCH = Locale.forLanguageTag("fr-CH");
	public static final Locale SWISS_ITALIAN = Locale.forLanguageTag("it-CH");
	public static final Locale SWISS_ENGLISH = Locale.forLanguageTag("en-CH");
	public static final Locale DEFAULT_LOCALE = SWISS_GERMAN;

	public static final Set<Locale> SUPPORTED_LOCALES = Set.of(SWISS_GERMAN, SWISS_FRENCH, SWISS_ENGLISH, SWISS_ITALIAN );

	private I18NConst() {
	}
}
