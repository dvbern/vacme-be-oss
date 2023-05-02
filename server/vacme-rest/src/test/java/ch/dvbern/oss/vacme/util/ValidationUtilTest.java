/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.util;

import java.time.LocalDate;
import java.util.Date;

import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Convert;
import org.locationtech.jts.util.Assert;

class ValidationUtilTest {

	@Test
	void validateImpfstoffForKrankheit_when_affenpocken_with_covidimpfstoff_should_fail() {
		try {
			ValidationUtil.validateImpfstoffForKrankheit(
				KrankheitIdentifier.AFFENPOCKEN,
				TestdataCreationUtil.createImpfstoffPfizer());
			Assertions.fail("Covidimpfstoff should not be valid for Affenpocken");
		} catch (AppValidationException ex) {
			Assert.isTrue(ex.getMessage().contains("IMPFUNG_FALSCHER_IMPFSTOFF"));

		}
	}

	@Test
	void validateImpfstoffForKrankheit_when_affenpocken_with_affenpockeinimpfstoff_should_work() {
		try {
			ValidationUtil.validateImpfstoffForKrankheit(
				KrankheitIdentifier.AFFENPOCKEN,
				TestdataCreationUtil.createImpfstoffAffenpocken());

		} catch (Exception ex) {
			Assertions.fail("Affenpockenimpfstoff should not be valid for Affenpocken");
		}
	}
	@ParameterizedTest
	@CsvSource({
		// Moderna (2)
		"COVID         , 2020-12-19 , true",
		"COVID         , 2020-12-20 , false",
		"COVID         , 2020-12-21 , false",
		"AFFENPOCKEN   , 2022-10-31 , true",
		"AFFENPOCKEN   , 2022-11-01 , false",
		"AFFENPOCKEN   , 2022-11-02 , false",
		"FSME          , 1800-01-01 , true",
		"FSME          , 1899-12-31 , true",
		"FSME          , 1900-01-01 , false",
		"FSME          , 2020-12-20 , false",
		"FSME          , 2020-12-21 , false",
	})
	void validatemindateForCovid_when_checked(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull LocalDate dateNew,
		boolean shouldFail
	) {

		try {
			OrtDerImpfung odi = TestdataCreationUtil.createOrtDerImpfung();
			Impftermin termin = TestdataCreationUtil.createImpftermin(odi, dateNew);
			termin.getImpfslot().setKrankheitIdentifier(krankheitIdentifier);

			ValidationUtil.validateImpfungMinDate(
				termin);
			if (shouldFail) {
				Assertions.fail(String.format(
					"The date %s should not be valid since it is before the min date for the krankheit %s",
					dateNew,
					krankheitIdentifier));
			}

		} catch (Exception ex) {
			// expected
			if(!shouldFail) {
				Assertions.fail(String.format(
					"The date %s should be valid since it is after the min date for the krankheit %s",
					dateNew,
					krankheitIdentifier));
			}
		}
	}
}
