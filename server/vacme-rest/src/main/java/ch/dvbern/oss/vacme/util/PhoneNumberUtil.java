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

package ch.dvbern.oss.vacme.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.dvbern.oss.vacme.shared.util.Constants;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public final class PhoneNumberUtil {

	public static final String SWISS_CODE = "CH";

	private static final Pattern COMPILE = Pattern.compile("[^(+|0-9)]");
	private static final Pattern PATTERN = Pattern.compile(Constants.REGEX_TELEFON);
	public static final String MSG_MOBILE_PREFIX_DEFAULT = "+41";
	private static final String MSG_MOBILE_PREFIX_CONDITION = "07";

	private PhoneNumberUtil() {
	}

	@Nullable
	public static String getVorwahl(@Nullable String phoneNumber) {
		if (phoneNumber == null) {
			return null;
		}
		Matcher m = PATTERN.matcher(phoneNumber);
		if (m.find()) {
			if (m.groupCount() == 3) {
				return m.group(2);
			}
		}
		return null;
	}

	/**
	 * Benutzt google phonelib um zu ueberpruefen ob die nummer eine Mobile nummer ist.
	 * ACHTUNG: Nummern vom typ FIXED_LINE_OR_MOBILE werden als false evaluirt.
	 * @param phoneNumber die zu ueberpruefende nummer
	 * @return ob die nummer vom typ PhoneNumberTyp.MOBILE ist.
	 */
	public static boolean isMobileNumber(@Nullable String phoneNumber) {
		com.google.i18n.phonenumbers.PhoneNumberUtil phoneUtil =
			com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
		try {
			Phonenumber.PhoneNumber phone = phoneUtil.parse(phoneNumber, SWISS_CODE);
			return phoneUtil.isValidNumber(phone) && isMobilePhoneNumberType(phoneUtil.getNumberType(phone));
		} catch (NumberParseException e) {
			return false;
		}
	}

	private static boolean isMobilePhoneNumberType(@NonNull PhoneNumberType type) {
		return EnumUtil.isOneOf(
			type,
			PhoneNumberType.MOBILE, PhoneNumberType.FIXED_LINE_OR_MOBILE, PhoneNumberType.PAGER);
	}

	/**
	 * Benutzt google phonelib um zu ueberpruefen ob die nummer eine schweizer nummer ist.
	 */
	public static boolean isSwissNumber(@Nullable String phoneNumber) {
		com.google.i18n.phonenumbers.PhoneNumberUtil phoneUtil =
			com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
		try {
			Phonenumber.PhoneNumber phone = phoneUtil.parse(phoneNumber, SWISS_CODE);
			return SWISS_CODE.equalsIgnoreCase(phoneUtil.getRegionCodeForNumber(phone));
		} catch (NumberParseException e) {
			return false;
		}
	}

	public static boolean isValidNumber(@Nullable String phoneNumber) {
		com.google.i18n.phonenumbers.PhoneNumberUtil phoneUtil =
			com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
		try {
			Phonenumber.PhoneNumber phone = phoneUtil.parse(phoneNumber, SWISS_CODE);
			return phoneUtil.isPossibleNumber(phone);
		} catch (NumberParseException e) {
			return false;
		}
	}

	/**
	 * Check mobile number normative strcuture
	 */
	@NonNull
	public static String processMobileNumber(@NonNull String mobileNumber) {

		// Leerzeichen aus Mobilenummern entfernen
		mobileNumber = COMPILE.matcher(mobileNumber).replaceAll("");

		mobileNumber = setDefaultCountryCodeIfZero(mobileNumber);

		com.google.i18n.phonenumbers.PhoneNumberUtil phoneUtil =
			com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
		try {
			Phonenumber.PhoneNumber phone = phoneUtil.parse(mobileNumber, SWISS_CODE);
			mobileNumber = phoneUtil.format(
				phone,
				com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.E164);
		} catch (NumberParseException e) {
			LOG.error("Invalid phone number {}", mobileNumber, e);
		}
		return mobileNumber;
	}

	@NonNull
	private static String setDefaultCountryCodeIfZero(@NonNull String mobileNumber) {
		if (mobileNumber.startsWith(MSG_MOBILE_PREFIX_CONDITION)) {
			mobileNumber = MSG_MOBILE_PREFIX_DEFAULT + mobileNumber.substring(1);
		}
		return mobileNumber;
	}
}
