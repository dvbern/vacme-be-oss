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

import org.checkerframework.checker.nullness.qual.Nullable;

public class I18N {

	public Locale resolveLocale(@Nullable Locale locale) {
		if (locale == null) {
			return I18NConst.DEFAULT_LOCALE;
		}

		if (isSupported(locale)) {
			return locale;
		}

		Locale fallback = I18NConst.SUPPORTED_LOCALES.stream()
				.filter(sl -> sl.getLanguage().equals(locale.getLanguage()))
				.findAny()
				.orElse(I18NConst.DEFAULT_LOCALE);

		return fallback;
	}

	public boolean isSupported(Locale locale) {
		return I18NConst.SUPPORTED_LOCALES.contains(locale);
	}

}
