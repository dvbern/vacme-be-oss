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
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class TLProducer {

	private final ConcurrentHashMap<Locale, ResourceBundle> bundleMap = new ConcurrentHashMap<>();
	private final LocaleProvider localeProvider;

	@Inject
	public TLProducer(
		LocaleProvider localeProvider
	) {
		this.localeProvider = requireNonNull(localeProvider);
	}

	// should not need the requestscoped annotation!
	// but since the backendlocaleprovider is requestscoped
	@Produces
	@RequestScoped
	public TL produceTL() {
		Locale locale = new I18N().resolveLocale(localeProvider.currentLocale());
		return new TLImpl(locale, null, null); // use the ServerMessageUtil to load the correct bundles
	}

	@NonNull
	ResourceBundle loadBundle(@NonNull Locale locale) {
		return bundleMap.computeIfAbsent(locale, this::initBundle);
	}

	@NonNull
	ResourceBundle initBundle(@NonNull Locale locale) {
		return ServerMessageUtil.getResourceBundle(locale);
	}
}
