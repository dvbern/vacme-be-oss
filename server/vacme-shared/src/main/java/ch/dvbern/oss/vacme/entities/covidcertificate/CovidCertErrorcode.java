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

package ch.dvbern.oss.vacme.entities.covidcertificate;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

public enum CovidCertErrorcode {

	CC_ERROR_NO_VAC_DATA(451, "No vaccination data was specified"),
	CC_ERROR_NO_PERSON_DATA(452, "No person data was specified"),
	CC_ERROR_INVALID_BDAY(453, "Invalid dateOfBirth! Must be younger than 1900-01-01"),
	CC_ERROR_INVALID_MED_PROD(454, "Invalid medicinal product"),
	CC_ERROR_INVALID_DOSE_NUMBER(455, "Invalid number of doses"),
	CC_ERROR_INVALID_VAC_DATE_FUTURE(456, "Invalid vaccination date! Date cannot be in the future"),
	CC_ERROR_INVALID_VAC_COUNTRY(457, "Invalid country of vaccination"),
	CC_ERROR_INVALID_GIVEN_NAME_TOO_LONG(458, "Invalid given name! Must not exceed 50 chars"),
	CC_ERROR_INVALID_FAM_NAME_TOO_LONG(459, "Invalid family name! Must not exceed 50 chars"),
	CC_ERROR_INVALID_NOT_TEST_DATA(460, "No test data was specified"),
	CC_ERROR_INVALID_MEMBER_STATE_TEST(461, "Invalid member state of test"),
	CC_ERROR_INVALID_TEST_MNFCT_COMBO(462, "Invalid type of test and manufacturer code combination! Must either be a PCR Test type and no manufacturer code or give a manufacturer code and the antigen test type code."),
	CC_ERROR_INVALID_TEST_CENTER(463, "Invalid testing center or facility"),
	CC_ERROR_INVALID_TEST_SAMPE_TIME(464, "Invalid sample or result date time! Sample date must be before current date and before result date"),
	CC_ERROR_INVALID_TEST_NO_REC_DATE(465, "No recovery data specified"),
	CC_ERROR_INVALID_TEST_FIRST_POS_DATE(466, "Invalid date of first positive test result"),
	CC_ERROR_INVALID_TEST_COUNTRY(467, "Invalid country of test"),
	CC_ERROR_INVALID_COUNTRY_CODE(468, "Country short form can not be mapped"),
	CC_ERROR_INVALID_LANGUAGE_CODE(469, "The given language does not match any of the supported languages: de, it, fr!"),
	CC_ERROR_DUPLICATE_UVCI(480, "Duplicate UVCI"),
	//If the integrity check fails, the following errors are returned as 403 FORBIDDEN:

	CC_ERROR_INTEGRITY_HASH_DOES_NOT_MATCH(490, "Integrity check failed. The body hash does not match the hash in the header."),
	CC_ERROR_INTEGRITY_SIGNATURE_NOT_PARSABLE(491, "Signature could not be parsed."),
	//	If the otp validation fails, the following errors are returned as 403 FORBIDDEN:

	CC_ERROR_INVALID_OR_MISSING_TOKEN(492, "Invalid or missing bearer token."),
	//	If the payload is too big, the errors are returned as 413 PAYLOAD TOO LARGE:

	CC_ERROR_PAYLOAD_TOO_BIG(493, "Request payload too large, the maximum payload size is: 2048 bytes"),
	//	If the server generates a known internal error, thes are returend as 500 INTERNAL SERVER ERROR:

	CC_ERROR_BIT_COSE_GEN_HDR_FAIL(550, "Creating COSE protected header failed."),
	CC_ERROR_BIT_COSE_GEN_SIG_FAIL(551, "Creating COSE payload failed."),
	CC_ERROR_BIT_COSE_GEN_SIG_DATA_FAIL(552, "Creating COSE signature data failed."),
	CC_ERROR_BIT_GEN_SIG_FAIL(553, "Creating signature failed."),
	CC_ERROR_BIT_COSE_GEN_SIGN1_FAIL(554, "Creating COSE_Sign1 failed."),
	CC_ERROR_BIT_GEN_BARCODE_FAIL(555, "Creating barcode failed.");

	private final int errorCode;
	private final String message;

	CovidCertErrorcode(int errorCode, String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getMessage() {
		return message;
	}

	@Nullable
	public static CovidCertErrorcode fromErrorCode(@Nullable Integer errorCode) {
		if (errorCode == null) {
			return null;
		}
		Optional<CovidCertErrorcode> any =
			Arrays.stream(CovidCertErrorcode.values()).filter(code -> Objects.equals(code.getErrorCode(), errorCode)).findAny();

		return any.orElse(null);
	}
}
