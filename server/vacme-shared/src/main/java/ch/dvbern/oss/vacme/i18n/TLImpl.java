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
import java.util.Objects;
import java.util.ResourceBundle;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TLImpl implements TL {
	private static final Logger LOG = LoggerFactory.getLogger(TLImpl.class);

	public TLImpl(Locale locale, @Nullable ResourceBundle bundle, @Nullable ResourceBundle fallbackBundle) {
		Objects.requireNonNull(this.locale = locale);
		this.bundle = bundle;
		this.fallbackBundle = fallbackBundle;
	}

	private final Locale locale;
	@Nullable
	private final ResourceBundle bundle;
	@Nullable
	private final ResourceBundle fallbackBundle;

	@Override
	public String translate(String key, Object... args) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(args);

		if (this.bundle != null && this.fallbackBundle != null) {
			// meant for testing only
			return ServerMessageUtil.getMessageWithBundles(key, bundle, fallbackBundle, args);
		} else {
			// normal case
			return ServerMessageUtil.getMessage(key, locale, args);
		}

	}

	@Override
	public Locale getLocale() {
		return locale;
	}
}
