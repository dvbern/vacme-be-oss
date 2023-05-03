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

package ch.dvbern.oss.vacme.service;

import ch.dvbern.oss.vacme.service.onboarding.OnboardingHashIdService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OnboardingHashIdServiceTest {


	@ParameterizedTest
	@CsvSource({
		"1, BEYNZBN421",
		"2, BEXADRA420",
		"3, BE7N4BN425",
		"4, BEKNMDA421",
		"5, BEWAYXN429",
		"200000, BEBXRE9E420"
	})
	void getOnboardingHash(Long inputSequence, String output) {
		System.setProperty("vacme.mandant", "BE");
		OnboardingHashIdService onboardingHashIdService = new OnboardingHashIdService();
		String onboardingHash = onboardingHashIdService.getOnboardingHash(inputSequence, "42" );
		Assertions.assertNotNull(onboardingHash);
		Assertions.assertEquals(output, onboardingHash);

		boolean validOnboardingCode = onboardingHashIdService.isValidOnboardingCode(onboardingHash);
		Assertions.assertTrue(validOnboardingCode);
	}


	@ParameterizedTest
	@CsvSource({
		"BE4YRMO420",
		"BEW49NG427",
		"BE9YXMO427",
		"BE3RR9Q427",
	})
	void checkInvalidCode(String code) {
		System.setProperty("vacme.mandant", "BE");
		OnboardingHashIdService onboardingHashIdService = new OnboardingHashIdService();

		boolean validOnboardingCode = onboardingHashIdService.isValidOnboardingCode(code);
		Assertions.assertFalse(validOnboardingCode);
	}

	@ParameterizedTest
	@CsvSource({
		"ZHP78BD744",
		"ZH3BKM9894",
		"ZHZRM54931",
		"ZH4XN3X282",
		"ZHDYBNM149",
		"ZH333335332",
		"ZH33338M281",
	})
	void checkvalidCode(String code) {
		System.setProperty("vacme.mandant", "ZH");
		OnboardingHashIdService onboardingHashIdService = new OnboardingHashIdService();

		boolean validOnboardingCode = onboardingHashIdService.isValidOnboardingCode(code);
		Assertions.assertTrue(validOnboardingCode);
	}
}

