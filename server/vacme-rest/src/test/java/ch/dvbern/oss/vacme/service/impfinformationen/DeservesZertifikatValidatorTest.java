package ch.dvbern.oss.vacme.service.impfinformationen;

import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.util.DeservesZertifikatValidator;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
class DeservesZertifikatValidatorTest {

	private ImpfinformationBuilder helper = new ImpfinformationBuilder();

	private final LocalDate date1 = LocalDate.of(2021, Month.JANUARY, 1);
	private final LocalDate date2 = LocalDate.of(2021, Month.FEBRUARY, 2);
	private final LocalDate date3 = LocalDate.of(2021, Month.MARCH, 3);

	private final Impfstoff moderna = TestdataCreationUtil.createImpfstoffModerna();
	private final Impfstoff janssen = TestdataCreationUtil.createImpfstoffJanssen();
	private final Impfstoff astraZeneca = TestdataCreationUtil.createImpfstoffAstraZeneca();

	@BeforeEach
	void setUp() {
		helper.create(KrankheitIdentifier.COVID);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
	}

	@Test
	void deservesZertifikat_vacme_janssen() {
		// 1. Impfung Jansson (VACME)
		helper.withImpfung1(date1, janssen);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
		// Booster
		helper.withBooster(date3, moderna);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), true);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
	}

	@Test
	void deservesZertifikat_vacme_moderna() {
		// 1. Impfung Moderna (VACME)
		helper.withImpfung1(date1, moderna);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// 2. Impfung Moderna (VACME)
		helper.withImpfung2(date2, moderna);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung2(), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
		// Booster
		helper.withBooster(date3, moderna);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung2(), true);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
	}

	@Test
	public void deservesZertifikat_vacme_astraZeneca() {
		// 1. Impfung AstraZeneca (VACME)
		helper.withImpfung1(date1, astraZeneca);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// 2. Impfung AstraZeneca (VACME)
		helper.withImpfung2(date2, astraZeneca);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung2(), false);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// 3. Impfung Booster
		helper.withBooster(date3, moderna);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung2(), false);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
	}

	@Test
	void deservesZertifikat_vacme_covid_vor_impfung() {
		// 1. Impfung Moderna (VACME)
		helper.withImpfung1(date2, moderna);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// 2. Corona VOR Impfung
		helper.withCoronaTest(date1);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
		// Booster
		helper.withBooster(date3, moderna);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), true);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
	}

	@Test
	void deservesZertifikat_vacme_covid_nach_impfung() {
		// 1. Impfung Moderna (VACME)
		helper.withImpfung1(date1, moderna);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// 2. Corona NACH Impfung
		helper.withCoronaTest(date2);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// Booster
		helper.withBooster(date3, moderna);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
	}

	@Test
	void deservesZertifikat_vacme_covid_ohneTestdatum() {
		// 1. Impfung Moderna (VACME)
		helper.withImpfung1(date2, moderna);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// 2. Corona ohne Datum (muss nachtraeglich auf null gesetzt werden, da wir dies neu gar nicht mehr zulassen)
		helper.withCoronaTest(date3);
		final ImpfinformationDto infosNachCorona = helper.getInfos();
		infosNachCorona.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setPositivGetestetDatum(null);
		assertDeservesZertifikat(infosNachCorona, infosNachCorona.getImpfung1(), true);
		assertDeservesAnyZertifikat(infosNachCorona, true);
	}

	@Test
	void deservesZertifikat_extern_moderna() {
		// Externes Zertifikat: 2 * Moderna
		helper.withExternesZertifikat(moderna, 2,  date2, null);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// Booster
		helper.withBooster(date3, moderna);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
	}

	@Test
	void deservesZertifikat_extern_janssen() {
		// Externes Zertifikat: 2 * Moderna
		helper.withExternesZertifikat(janssen, 1,  date2, null);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// Booster
		helper.withBooster(date3, moderna);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
	}

	@Test
	void deservesZertifikat_extern_covid_vor_impfung() {
		helper.withExternesZertifikat(janssen, 1,  date2, date1);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// Booster
		helper.withBooster(date3, moderna);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
	}

	@Test
	void deservesZertifikat_extern_covid_nach_impfung() {
		helper.withExternesZertifikat(janssen, 1,  date1, date2);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		// Booster
		helper.withBooster(date3, moderna);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), true);
		assertDeservesAnyZertifikat(helper.getInfos(), true);
	}

	@Test
	void deservesZertifikat_booster_nichtZugelassen() {
		helper
			.withImpfung1(date1, astraZeneca)
			.withImpfung2(date2, astraZeneca)
			.withBooster(date3, astraZeneca);
		assertDeservesAnyZertifikat(helper.getInfos(), false);
		Assertions.assertNotNull(helper.getInfos().getBoosterImpfungen());
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung1(), false);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getImpfung2(), false);
		assertDeservesZertifikat(helper.getInfos(), helper.getInfos().getBoosterImpfungen().get(0), false);
	}

	void assertDeservesZertifikat(@NonNull ImpfinformationDto infos, @Nullable Impfung impfung, boolean expected) {
		Assertions.assertNotNull(infos);
		Assertions.assertNotNull(infos.getRegistrierung());
		Assertions.assertNotNull(impfung);
		Assertions.assertEquals(expected, DeservesZertifikatValidator.deservesZertifikat(infos, impfung));
	}

	void assertDeservesAnyZertifikat(@NonNull ImpfinformationDto infos, boolean expected) {
		Assertions.assertNotNull(infos);
		Assertions.assertNotNull(infos.getRegistrierung());
		Assertions.assertEquals(expected, DeservesZertifikatValidator.deservesZertifikatForAnyImpfung(infos));
	}
}
