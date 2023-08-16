/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.entities.util.validators;

import java.time.LocalDate;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckExternesZertifikatDatumLetzteImpfungValidatorTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void when_affenpocken_andunknown_flag_is_not_set_last_impfung_date_must_not_be_null() {

		Impfdossier impfdossier = new Impfdossier();
		ExternesZertifikat externeImpfinfo = TestdataCreationUtil.createExternesZertifikat(impfdossier, TestdataCreationUtil.createImpfstoffModerna(), 2,
			LocalDate.of(2021,1,1));
		externeImpfinfo.setLetzteImpfungDateUnknown(false);
		externeImpfinfo.getImpfdossier().setKrankheitIdentifier(KrankheitIdentifier.AFFENPOCKEN);
		Set<ConstraintViolation<ExternesZertifikat>> validationErros = validator.validate(externeImpfinfo);
		assertEquals(0, validationErros.size());

		externeImpfinfo.setLetzteImpfungDate(null);
		validationErros = validator.validate(externeImpfinfo);
		assertEquals(1, validationErros.size());
	}

	@Test
	void when_affenpocken_unknown_flag_set_last_impfung_date_must_be_null() {

		Impfdossier impfdossier = new Impfdossier();
		ExternesZertifikat externeImpfinfo = TestdataCreationUtil.createExternesZertifikat(impfdossier, TestdataCreationUtil.createImpfstoffModerna(), 2,
			LocalDate.of(2021,1,1));
		externeImpfinfo.setLetzteImpfungDateUnknown(true);
		externeImpfinfo.getImpfdossier().setKrankheitIdentifier(KrankheitIdentifier.AFFENPOCKEN);
		Set<ConstraintViolation<ExternesZertifikat>> validationErros = validator.validate(externeImpfinfo);
		assertEquals(1, validationErros.size());

		externeImpfinfo.setLetzteImpfungDateUnknown(false);
		validationErros = validator.validate(externeImpfinfo);
		assertEquals(0, validationErros.size());

	}

	@Test
	void when_covid_last_impfung_date_must_never_be_null() {

		Impfdossier impfdossier = new Impfdossier();
		ExternesZertifikat externeImpfinfo = TestdataCreationUtil.createExternesZertifikat(impfdossier, TestdataCreationUtil.createImpfstoffModerna(), 2,
			null);
		externeImpfinfo.setLetzteImpfungDateUnknown(false);
		externeImpfinfo.getImpfdossier().setKrankheitIdentifier(KrankheitIdentifier.COVID);
		Set<ConstraintViolation<ExternesZertifikat>> validationErros = validator.validate(externeImpfinfo);
		assertEquals(1, validationErros.size());

	}

	@Test
	void when_covid_unkown_flag_must_not_be_set() {

		Impfdossier impfdossier = new Impfdossier();
		ExternesZertifikat externeImpfinfo = TestdataCreationUtil.createExternesZertifikat(impfdossier, TestdataCreationUtil.createImpfstoffModerna(), 2,
			LocalDate.of(2021,1,1));
		externeImpfinfo.setLetzteImpfungDateUnknown(true);
		externeImpfinfo.getImpfdossier().setKrankheitIdentifier(KrankheitIdentifier.COVID);
		Set<ConstraintViolation<ExternesZertifikat>> validationErros = validator.validate(externeImpfinfo);
		assertEquals(1, validationErros.size());

	}
}
