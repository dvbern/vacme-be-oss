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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhoneNumberUtilTest {

	@Test
	void getVorwahl() {
		assertEquals("79", PhoneNumberUtil.getVorwahl("+41791231212"));
		assertEquals("79", PhoneNumberUtil.getVorwahl("+41 79 123 12 12"));
		assertEquals("79", PhoneNumberUtil.getVorwahl("0791231212"));
		assertEquals("79", PhoneNumberUtil.getVorwahl("079 123 12 12"));
		assertEquals("79", PhoneNumberUtil.getVorwahl("0791231212"));
	}

	@Test
	public void isMobileNumber() {
		Assertions.assertTrue(PhoneNumberUtil.isMobileNumber("+41 75 123 12 12"));
		Assertions.assertTrue(PhoneNumberUtil.isMobileNumber("+41 76 123 12 12"));
		Assertions.assertTrue(PhoneNumberUtil.isMobileNumber("+41 77 123 12 12"));
		Assertions.assertTrue(PhoneNumberUtil.isMobileNumber("+41 78 123 12 12"));
		Assertions.assertTrue(PhoneNumberUtil.isMobileNumber("+41 79 123 12 12"));

		Assertions.assertFalse(PhoneNumberUtil.isMobileNumber("+41 74 123 12 12"));
		Assertions.assertFalse(PhoneNumberUtil.isMobileNumber("+41 80 123 12 12"));
		Assertions.assertFalse(PhoneNumberUtil.isMobileNumber("034 402 45 12"));
		Assertions.assertFalse(PhoneNumberUtil.isMobileNumber(""));
		Assertions.assertFalse(PhoneNumberUtil.isMobileNumber("asdf"));
	}


	@Test
	public void processMobileNumberTest() {
		assertEquals("+41751231212", PhoneNumberUtil.processMobileNumber("+41 75 123 12 12"));
		assertEquals("+41781231212", PhoneNumberUtil.processMobileNumber("781231212"));
		assertEquals("+41781231212", PhoneNumberUtil.processMobileNumber("0781231212"));
		assertEquals("+41781231212", PhoneNumberUtil.processMobileNumber("+41781231212"));
		assertEquals("+41781231212", PhoneNumberUtil.processMobileNumber("0041781231212"));
		assertEquals("+41781231212", PhoneNumberUtil.processMobileNumber("078 123 1212"));
		assertEquals("+41781231212", PhoneNumberUtil.processMobileNumber("078 123 12 12"));
	}

	@Test
	public void foreignNumberTest() {
		assertEquals("+260978365113", PhoneNumberUtil.processMobileNumber("+260978365113"));
		Assertions.assertTrue(PhoneNumberUtil.isMobileNumber("+260978365113"));
		assertEquals("+46720258275", PhoneNumberUtil.processMobileNumber("+46 72 025 82 75"));
		Assertions.assertTrue(PhoneNumberUtil.isMobileNumber("+46 72 025 82 75"));

		assertEquals("+26097836511345", PhoneNumberUtil.processMobileNumber("+26097836511345"));
		Assertions.assertFalse(PhoneNumberUtil.isMobileNumber("+26097836511345"));
		assertEquals("+4672025827522", PhoneNumberUtil.processMobileNumber("+46 72 025 82 75 22"));
		Assertions.assertFalse(PhoneNumberUtil.isMobileNumber("+46 72 025 82 75 22"));
	}

	@Test
	public void isSwissNumber() {
		Assertions.assertTrue(PhoneNumberUtil.isSwissNumber("+41 75 123 12 12"));
		Assertions.assertTrue(PhoneNumberUtil.isSwissNumber("+41 31 123 12 12"));
		Assertions.assertTrue(PhoneNumberUtil.isSwissNumber("+41 78 123 12 12"));

		Assertions.assertFalse(PhoneNumberUtil.isSwissNumber("+260978365113"));
		Assertions.assertFalse(PhoneNumberUtil.isSwissNumber("+46 72 025 82 75"));
	}
}
