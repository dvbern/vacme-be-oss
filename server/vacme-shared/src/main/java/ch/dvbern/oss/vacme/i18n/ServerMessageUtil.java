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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


import ch.dvbern.oss.vacme.enums.Mandant;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

/**
 * Util welche einfach erlaubt eine Message aus dem server Seitigen Message Bundle zu lesen
 */
public final class ServerMessageUtil {

	private static final String MESSAGE_BUNDLE_NAME_BE = "ch.dvbern.oss.vacme.i18n.translations-be";
	private static final ResourceBundle BUNDLE_BE_DE = ResourceBundle.getBundle(
		MESSAGE_BUNDLE_NAME_BE, Locale.GERMAN, requireNonNull(currentThread().getContextClassLoader()));
	private static final ResourceBundle BUNDLE_BE_FR = ResourceBundle.getBundle(
		MESSAGE_BUNDLE_NAME_BE, Locale.FRENCH, requireNonNull(currentThread().getContextClassLoader()));
	private static final ResourceBundle BUNDLE_BE_EN = ResourceBundle.getBundle(
		MESSAGE_BUNDLE_NAME_BE, Locale.ENGLISH, requireNonNull(currentThread().getContextClassLoader()));

	private static final String MESSAGE_BUNDLE_NAME_ZH = "ch.dvbern.oss.vacme.i18n.translations-zh";
	private static final ResourceBundle BUNDLE_ZH_DE = ResourceBundle.getBundle(
		MESSAGE_BUNDLE_NAME_ZH, Locale.GERMAN, requireNonNull(currentThread().getContextClassLoader()));
	private static final ResourceBundle BUNDLE_ZH_FR = ResourceBundle.getBundle(
		MESSAGE_BUNDLE_NAME_ZH, Locale.FRENCH, requireNonNull(currentThread().getContextClassLoader()));
	private static final ResourceBundle BUNDLE_ZH_EN = ResourceBundle.getBundle(
		MESSAGE_BUNDLE_NAME_ZH, Locale.ENGLISH, requireNonNull(currentThread().getContextClassLoader()));

	private ServerMessageUtil() {
	}

	@NonNull
	private static ResourceBundle selectBundleToUse(@NonNull Locale locale) {
		return getBundle(locale, MandantUtil.getMandant());
	}

	@NonNull
	private static ResourceBundle getBundle(@NonNull Locale locale, Mandant mandant) {
		if (Mandant.BE == mandant) {
			if (locale.getLanguage().equalsIgnoreCase("FR")) {
				return BUNDLE_BE_FR;
			} else if (locale.getLanguage().equalsIgnoreCase("EN")) {
				return BUNDLE_BE_EN;
			}
			return BUNDLE_BE_DE;
		}
		if (Mandant.ZH == mandant) {
			if (locale.getLanguage().equalsIgnoreCase("FR")) {
				return BUNDLE_ZH_FR;
			} else if (locale.getLanguage().equalsIgnoreCase("EN")) {
				return BUNDLE_ZH_EN;
			}
			return BUNDLE_ZH_DE;
		}
		throw new IllegalArgumentException("Unbekannter Mandant " + MandantUtil.getMandantProperty());
	}

	@NonNull
	public static ResourceBundle getResourceBundle(@NonNull Locale locale) {
		return selectBundleToUse(locale);
	}

	/**
	 * Find the message of the current mandant or, if the key is missing, in the fallback mandant; in the given locale
	 */
	public static String getMessage(String key, Locale locale, Object... args) {
		ResourceBundle bundle = selectBundleToUse(locale);
		ResourceBundle fallbackBundle = getBundle(locale, Mandant.BE);
		return getMessageWithBundles(key, bundle, fallbackBundle, args);
	}

	public static String getMessageWithBundles(String key, ResourceBundle bundle, ResourceBundle fallbackBundle,
		Object... args) {
		try {
			String template = readStringFromBundleOrReturnKey(bundle, fallbackBundle, key);
			return MessageFormat.format(template, args);
		} catch (MissingResourceException ignore2) {
			return "???" + key + "???";
		}
	}

	@NonNull
	public static String getMessageAllConfiguredLanguages(String key, String separator, Object... args) {
		String message = ServerMessageUtil.getMessage(key, Locale.GERMAN, args)
			+ separator
			+ ServerMessageUtil.getMessage(key, Locale.FRENCH, args);
		if (Mandant.ZH == MandantUtil.getMandant()) {
			message += separator
				+ ServerMessageUtil.getMessage(key, Locale.ENGLISH, args);
		}
		return message;
	}

	@NonNull
	public static String getMessageAllLanguages(String key, String separator) {
		String message = ServerMessageUtil.getMessage(key, Locale.GERMAN)
			+ separator
			+ ServerMessageUtil.getMessage(key, Locale.FRENCH)
			+ separator
			+ ServerMessageUtil.getMessage(key, Locale.ENGLISH);
		return message;
	}

	@NonNull
	public static String getMessageAllLanguagesWithArgs(String key, String separator, Object... args) {
		String message = ServerMessageUtil.getMessage(key, Locale.GERMAN, args)
			+ separator
			+ ServerMessageUtil.getMessage(key, Locale.FRENCH, args)
			+ separator
			+ ServerMessageUtil.getMessage(key, Locale.ENGLISH, args);
		return message;
	}

	private static String readStringFromBundleOrReturnKey(ResourceBundle bundle, ResourceBundle fallbackBundle, String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException ignore) {
			return fallbackBundle.getString(key);
		}
	}

	/**
	 * Uebersetzt einen Enum-Wert
	 */
	@NonNull
	public static String translateEnumValue(@Nullable final Enum<?> e, Locale locale, Object... args) {
		if (e == null) {
			return StringUtils.EMPTY;
		}
		return getMessage(getKey(e), locale, args);
	}

	/**
	 * Gibt den Bundle-Key fuer einen Enum-Wert zurueck.
	 * Schema: Klassenname_enumWert, also z.B. CodeArtType_MANDANT
	 */
	@NonNull
	private static String getKey(@NonNull Enum<?> e) {
		return e.getClass().getSimpleName() + '_' + e.name();
	}

}
