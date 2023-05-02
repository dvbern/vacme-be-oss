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

package ch.dvbern.oss.vacme.service.onboarding;

import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hashids.Hashids;

import static ch.dvbern.oss.vacme.i18n.MandantUtil.getMandantProperty;

/**
 * Onboarding hascode is composed as follows
 *
 *  [mandant-prefix] [5 digit hashcode] [2 random digits] [1 check digit]
 */
@ApplicationScoped
@Slf4j
public class OnboardingHashIdService {

	@ConfigProperty(name = "vacme.onboarding.hashids.alphabet", defaultValue = onboardingIDAlphabet)
	String alphabet = onboardingIDAlphabet;

	@ConfigProperty(name = "vacme.onboarding.hashids.minLength", defaultValue = "5")
	int minLength = 5;

	@ConfigProperty(name = "vacme.onboarding.hashids.salt")
	String salt;

	private static final String onboardingIDAlphabet = "2345789ABCDEFHKMNPRSTWXYZ"; // ohne verwechselbare Buchstaben

	/**
	 * we need to define a mapping from every possible char to a number. The number is the index position of
	 * the char in this string
	 */
	private static final String charToNumerAlphabet = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 *
	 * @param number from sequence
	 * @param randomElementDigits additional random digits to ensure code is not deterministic
	 * @return onboarding hash calculated based on the input
	 */
	public String getOnboardingHash(long number, String randomElementDigits) {
		String usedSalt = salt;
		if (usedSalt == null) {
			usedSalt = "ONBOARDINGHASH" + getMandantProperty();
		}
		Hashids hashids = new Hashids(usedSalt, this.minLength, alphabet);
		String rawCode = hashids.encode(number);
		rawCode += randomElementDigits;
		String pruefziffer = getPruefziffer(rawCode);
		return getMandantProperty() + rawCode + pruefziffer;
	}

	private String getPruefziffer(String rawCode) {
		char[] chars = rawCode.toCharArray();
		StringBuilder digitString = new StringBuilder();
		for (char aChar : chars) {
			digitString.append(convertCharToInt(aChar));
		}

		String checkDigit = null;
		try {
			checkDigit = LuhnCheckDigit.LUHN_CHECK_DIGIT.calculate(digitString.toString());
			return checkDigit;
		} catch (CheckDigitException e) {
			throw new AppFailureException("Could not generate check digit from code " + rawCode, e);
		}

	}

	public  boolean isValidOnboardingCode(String fullCode) {
		int prefixLength = getMandantProperty().length();
		if (!isCorrectMandantPrefix(fullCode)) {
			return false;
		}
		char[] relevantChars = fullCode.substring(prefixLength, fullCode.length() -1 ).toCharArray();
		StringBuilder digitString = new StringBuilder();
		for (char aChar : relevantChars) {
			try {
				digitString.append(convertCharToInt(aChar));
			} catch (IllegalArgumentException ex) {
				LOG.debug("VACME-ONBOARDING: User entered invalid char in code " + ex.getMessage() );
				return false;
			}
		}
		String checkDigit = fullCode.substring(fullCode.length() - 1);
		digitString.append(checkDigit);
		return LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(digitString.toString());

	}

	private boolean isCorrectMandantPrefix(String fullCode) {
		if (StringUtils.isEmpty(fullCode)) {
			return false;
		} else{
			if (StringUtils.isEmpty(getMandantProperty())) {
				return true;
			}
			return fullCode.startsWith(getMandantProperty());
		}

	}

	private int convertCharToInt(Character c){
		// algorithmus geht nur mit zahlen
		int i = charToNumerAlphabet.indexOf(c);
		if (i == -1) {
			throw new IllegalArgumentException("Character " + c + " is not a valid character");
		}
		return i;

	}
}
