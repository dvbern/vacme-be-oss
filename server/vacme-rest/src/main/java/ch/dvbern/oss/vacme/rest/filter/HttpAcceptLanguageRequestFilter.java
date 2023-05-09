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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import ch.dvbern.oss.vacme.i18n.I18NConst;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@Provider
@PreMatching
@RequestScoped
@Slf4j
public class HttpAcceptLanguageRequestFilter implements ContainerRequestFilter {
	private List<Locale> locales = new ArrayList<>();

	@Override
	public void filter(ContainerRequestContext requestContext) {
		var acceptLanguageHeader = requestContext.getHeaderString("Accept-Language");
		locales = parseAcceptLanguageHeader(acceptLanguageHeader);
	}

	private List<Locale> parseAcceptLanguageHeader(@Nullable String acceptLanguageHeader) {
		var header = trimToEmpty(acceptLanguageHeader);
		if (header.isEmpty()) {
			return List.of();
		}

		try {
			var localeRange = Locale.LanguageRange.parse(header);
			return Locale.filter(localeRange, I18NConst.SUPPORTED_LOCALES);
		} catch (IllegalArgumentException exception) {
			LOG.warn("Invalid Accept-Language header received. Using default value", exception);
			return List.of();
		}
	}

	public List<Locale> getLocales() {
		return locales;
	}
}
