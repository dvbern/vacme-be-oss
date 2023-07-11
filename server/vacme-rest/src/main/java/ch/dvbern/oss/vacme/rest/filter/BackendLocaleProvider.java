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

package ch.dvbern.oss.vacme.rest.filter;

import java.util.Locale;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.ws.rs.Produces;

import ch.dvbern.oss.vacme.i18n.I18N;
import ch.dvbern.oss.vacme.i18n.I18NConst;
import ch.dvbern.oss.vacme.i18n.LocaleProvider;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequestScoped
public class BackendLocaleProvider implements LocaleProvider {

	private final HttpAcceptLanguageRequestFilter httpAcceptLanguageRequestFilter;

	@Inject
	public BackendLocaleProvider(HttpAcceptLanguageRequestFilter filter) {
		this.httpAcceptLanguageRequestFilter = filter;
	}

	@Override
	public @Nullable Locale currentLocale() {
		return provideLocale();
	}

	@Produces
	@Default
	public Locale provideLocale() {
		var i18n = new I18N();
		var locales = httpAcceptLanguageRequestFilter.getLocales();

		// best case: desired locale directly supported
		for (Locale locale : locales) {
			if (i18n.isSupported(locale)) {
				return locale;
			}
		}

		// best effort
		for (Locale locale : locales) {
			return i18n.resolveLocale(locale);
		}

		return I18NConst.DEFAULT_LOCALE;
	}
}
