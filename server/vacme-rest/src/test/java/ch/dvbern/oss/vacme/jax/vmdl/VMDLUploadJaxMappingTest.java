package ch.dvbern.oss.vacme.jax.vmdl;

import java.time.LocalDate;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Geschlecht;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.vacme.util.ObjectMapperTestUtil.createObjectMapperForTest;

class VMDLUploadJaxMappingTest {

	private final Impfstoff moderna = TestdataCreationUtil.createImpfstoffModerna();
	private final ImpfinformationBuilder helper = new ImpfinformationBuilder();


	@BeforeAll
	static void setUp() {
		System.setProperty("vacme.mandant", "BE");
	}

	@Test
	void testMappingOfImpfungWithBooster() {
		helper
			.create(KrankheitIdentifier.COVID)
			.withImpfung1(LocalDate.of(2021, 1,1), moderna)
			.withImpfung2(LocalDate.of(2021,6,1), moderna)
			.withBooster(LocalDate.of(2021, 10, 1), moderna)
			.withBooster(LocalDate.of(2022,2,1), moderna)
			.withBooster(LocalDate.of(2022,3,1), moderna);
		ImpfinformationDto infos = helper.getInfos();
		Fragebogen fragebogen = helper.getFragebogen();

		Assertions.assertNotNull(infos.getImpfung1());
		Assertions.assertNotNull(infos.getImpfung2());
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getBoosterImpfungen());

		VMDLUploadCovidJax
			vmdlUploadCovidJax1 = new VMDLUploadCovidJax(infos.getImpfung1(), infos.getImpfdossier(), fragebogen,"rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax2 = new VMDLUploadCovidJax(infos.getImpfung2(), infos.getImpfdossier(), fragebogen,"rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax3 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(0), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(3), "rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax4 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(1), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(4), ",rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax5 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(2), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(5), ",rputTest");

		Assertions.assertEquals(1, vmdlUploadCovidJax1.getSerie());
		Assertions.assertEquals(1, vmdlUploadCovidJax2.getSerie());
		Assertions.assertEquals(2, vmdlUploadCovidJax3.getSerie());
		Assertions.assertEquals(3, vmdlUploadCovidJax4.getSerie());
		Assertions.assertEquals(4, vmdlUploadCovidJax5.getSerie());
	}

	@Test
	void testMappingOfImpfungWithThreeGrundimmunisierungenAndBooster() {
		helper
			.create(KrankheitIdentifier.COVID)
			.withImpfung1(LocalDate.of(2021, 1,1), moderna)
			.withImpfung2(LocalDate.of(2021,6,1), moderna)
			.withBooster(LocalDate.of(2021, 10, 1), moderna, true)
			.withBooster(LocalDate.of(2022,2,1), moderna)
			.withBooster(LocalDate.of(2022,3,1), moderna);
		ImpfinformationDto infos = helper.getInfos();
		Fragebogen fragebogen = helper.getFragebogen();

		Assertions.assertNotNull(infos.getImpfung1());
		Assertions.assertNotNull(infos.getImpfung2());
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getBoosterImpfungen());

		VMDLUploadCovidJax vmdlUploadCovidJax1 = new VMDLUploadCovidJax(infos.getImpfung1(), infos.getImpfdossier(), fragebogen,"rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax2 = new VMDLUploadCovidJax(infos.getImpfung2(), infos.getImpfdossier(), fragebogen,"rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax3 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(0), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(3), "rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax4 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(1), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(4), ",rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax5 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(2), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(5), ",rputTest");

		Assertions.assertEquals(1, vmdlUploadCovidJax1.getSerie());
		Assertions.assertEquals(1, vmdlUploadCovidJax2.getSerie());
		Assertions.assertEquals(1, vmdlUploadCovidJax3.getSerie());
		Assertions.assertEquals(3, vmdlUploadCovidJax4.getSerie()); // TODO VACME-1875: sollte 2 sein
		Assertions.assertEquals(4, vmdlUploadCovidJax5.getSerie()); // TODO VACME-1875: sollte 3 sein
	}

	@Test
	void testMappingKrank() {
		helper
			.create(KrankheitIdentifier.COVID)
			.withImpfung1(LocalDate.of(2021, 1,1), moderna)
			.withCoronaTest(LocalDate.of(2021,6,1))
			.withBooster(LocalDate.of(2021, 10, 1), moderna)
			.withBooster(LocalDate.of(2022,2,1), moderna)
			.withBooster(LocalDate.of(2022,3,1), moderna);
		ImpfinformationDto infos = helper.getInfos();
		Fragebogen fragebogen = helper.getFragebogen();

		Assertions.assertNotNull(infos.getImpfung1());
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getBoosterImpfungen());

		VMDLUploadCovidJax vmdlUploadCovidJax1 = new VMDLUploadCovidJax(infos.getImpfung1(), infos.getImpfdossier(), fragebogen,"rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax3 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(0), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(2), "rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax4 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(1), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(3), ",rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax5 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(2), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(4), ",rputTest");

		Assertions.assertEquals(1, vmdlUploadCovidJax1.getSerie());
		Assertions.assertEquals(2, vmdlUploadCovidJax3.getSerie());
		Assertions.assertEquals(2, vmdlUploadCovidJax4.getSerie()); // TODO VACME-1875: sollte 3 sein
		Assertions.assertEquals(3, vmdlUploadCovidJax5.getSerie()); // TODO VACME-1875: sollte 4 sein
	}

	@Test
	void testMappingKrankWithThreeGrundimmunisierungen() {
		helper
			.create(KrankheitIdentifier.COVID)
			.withImpfung1(LocalDate.of(2021, 1,1), moderna)
			.withCoronaTest(LocalDate.of(2021,6,1))
			.withBooster(LocalDate.of(2021, 10, 1), moderna, true)
			.withBooster(LocalDate.of(2022,2,1), moderna)
			.withBooster(LocalDate.of(2022,3,1), moderna);
		ImpfinformationDto infos = helper.getInfos();
		Fragebogen fragebogen = helper.getFragebogen();

		Assertions.assertNotNull(infos.getImpfung1());
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getBoosterImpfungen());

		VMDLUploadCovidJax vmdlUploadCovidJax1 = new VMDLUploadCovidJax(infos.getImpfung1(), infos.getImpfdossier(), fragebogen,"rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax3 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(0), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(2), "rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax4 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(1), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(3), ",rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax5 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(2), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(4), ",rputTest");

		Assertions.assertEquals(1, vmdlUploadCovidJax1.getSerie());
		Assertions.assertEquals(1, vmdlUploadCovidJax3.getSerie());
		Assertions.assertEquals(2, vmdlUploadCovidJax4.getSerie());
		Assertions.assertEquals(3, vmdlUploadCovidJax5.getSerie());
	}

	@Test
	void testJsonMapping() throws JsonProcessingException {
		helper
			.create(KrankheitIdentifier.COVID)
			.withAge(82)
			.withImpfung1(LocalDate.of(2021, 1,1), moderna)
			.withCoronaTest(LocalDate.of(2021,6,1))
			.withBooster(LocalDate.of(2021, 10, 1), moderna, true)
			.withBooster(LocalDate.of(2022,2,1), moderna)
			.withBooster(LocalDate.of(2022,3,1), moderna);
		ImpfinformationDto infos = helper.getInfos();
		Fragebogen fragebogen = helper.getFragebogen();
		fragebogen.setRegistrierung(infos.getRegistrierung());
		fragebogen.getRegistrierung().setGeschlecht(Geschlecht.WEIBLICH);

		Assertions.assertNotNull(infos.getImpfung1());
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getBoosterImpfungen());

		VMDLUploadCovidJax vmdlUploadCovidJax1 = new VMDLUploadCovidJax(infos.getImpfung1(), infos.getImpfdossier(), fragebogen,"rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax3 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(0), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(2), "rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax4 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(1), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(3), ",rputTest");
		VMDLUploadCovidJax vmdlUploadCovidJax5 = new VMDLUploadCovidJax(infos.getBoosterImpfungen().get(2), infos.getImpfdossier(), fragebogen, infos.getImpfdossier().getEintragForImpffolgeNr(4), ",rputTest");

		UUID regId = fragebogen.getRegistrierung().getId();
		String odiIdNoDashes =
			infos.getImpfung1().getTermin().getImpfslot().getOrtDerImpfung().getId().toString().replace("-", "");

		assertJsonFormatIsCorrect(vmdlUploadCovidJax1,  infos.getImpfung1(), regId, odiIdNoDashes);
	}

	private static void assertJsonFormatIsCorrect(
		@NonNull VMDLUploadCovidJax vmdlUploadCovidJax1,
		@NonNull Impfung impfungId,
		@NonNull UUID regId,
		@NonNull String odiIdNoDashes
	) throws JsonProcessingException {
		String expected = String.format(
			"{\"reporting_unit_id\":\"rputTest\",\"vacc_event_id\":\"rputTest-%s\","
				+ "\"person_anonymised_id\":\"%s\",\"person_residence_ctn\":\"UNK\","
				+ "\"person_age\":82,\"person_sex\":2,\"vacc_reason_age\":1,"
				+ "\"vacc_reason_chronic_disease\":1,\"vacc_reason_med_prof\":0,\"vacc_reason_contact_vuln\":0,"
				+ "\"vacc_reason_contact_comm\":0,\"vacc_reason_othr\":0,"
				+ "\"reporting_unit_location_ctn\":\"BE\","
				+ "\"reporting_unit_location_id\":\"%s\","
				+ "\"reporting_unit_location_type\":1,\"vacc_lot_number\":\"123456\",\"vacc_id\":\"30380777700688\","
				+ "\"vacc_date\":\"2021-01-01\",\"vacc_count\":1,\"person_pregnancy\":null,\"medstat\":\"0000\",\"serie\":1,"
				+ "\"person_recovered_from_covid\":1,\"pcr_tested_positive_date\":\"2021-06-01\"}",
			impfungId.getId(),
			regId,
			odiIdNoDashes);
		try {
			ObjectMapper objectMapper = createObjectMapperForTest();
			String jsonString = objectMapper.writeValueAsString(vmdlUploadCovidJax1);
			Assertions.assertEquals(expected, jsonString);

		} catch (JsonProcessingException e) {
			throw e;
		}
	}
}
