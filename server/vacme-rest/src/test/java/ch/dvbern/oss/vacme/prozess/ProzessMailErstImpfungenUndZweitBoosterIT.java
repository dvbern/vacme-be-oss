package ch.dvbern.oss.vacme.prozess;

import java.time.LocalDate;
import java.time.Month;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.testing.MariaDBProfile;
import ch.dvbern.oss.vacme.testing.MariaDBTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(MariaDBTestResource.class)
@TestProfile(MariaDBProfile.class)
@QuarkusTest
public class ProzessMailErstImpfungenUndZweitBoosterIT extends AbstractProzessIT {

	@Inject
	RegistrierungRepo registrierungRepo;

	private final LocalDate zeitraumVon = LocalDate.of(2022, Month.FEBRUARY, 1);
	private final LocalDate zeitraumBis = LocalDate.now().plusDays(1); // Damit die "heute" erfassten Impfungen drin sind
	private final LocalDate datumVorZeitraum = LocalDate.of(2022, Month.JANUARY, 15);
	private final LocalDate datumVorKalenderjahr = LocalDate.of(2021, Month.JANUARY, 15);
	private final LocalDate datumImZeitraum = LocalDate.of(2022, Month.MARCH, 15);


	@Test
	void anzahlErstimpfungenAndZweitBoosterMail() {
		// Achtung, dies kann nicht wirklich getestet werden, da nicht das Impfungsdatum zaehlt, sondern
		// das Erfassungsdatum der Impfung. Im Test ist dies natürlich immer heute
		createImpfdossierCovidWithBoosteram(datumVorKalenderjahr, datumImZeitraum);
		createImpfdossierCovidWithBoosteram(datumVorZeitraum, datumImZeitraum);
		createImpfdossierCovidWithBoosteram(datumImZeitraum, datumImZeitraum);

		long anzahlErst =
			registrierungRepo.getAnzahlErstimpfungen(OrtDerImpfungTyp.IMPFZENTRUM, zeitraumVon, zeitraumBis);
		long anzahlBooster =
			registrierungRepo.getAnzahlBoosterOrGrundimunisierungGT3Covid(OrtDerImpfungTyp.IMPFZENTRUM, zeitraumVon, zeitraumBis);
		long anzahlBoosterOhneImpfungImKalenderjahr =
			registrierungRepo.getAnzahlCovidBoosterOhneErstimpfungOderBoosterImKalenderjahr(OrtDerImpfungTyp.IMPFZENTRUM, zeitraumVon, zeitraumBis);
		int anzahlZweitOderMehrBooster = impfungRepo.getAllZweitOderMehrBooster().size();

		Assertions.assertEquals(
			0,
			anzahlErst,
			"Alle erfassten Impfungen sind zwar Covid, aber Booster");
		Assertions.assertEquals(
			6,
			anzahlBooster,
			"Alle Impfungen sind Covid und gelten als 'im Zeitraum', weil das Erfassungsdatum der Impfung zaehlt");
		Assertions.assertEquals(
			0,
			anzahlBoosterOhneImpfungImKalenderjahr,
			"Da alle Impfungen zwar Covid sind und als 'im Zeitraum' gelten (Erfassungdatum der Impfung) sind auch jeweils beide im Kalenderjahr");
		Assertions.assertEquals(
			3,
			anzahlZweitOderMehrBooster,
			"Alle drei Dossiers haben je einen 1. und einen 2. Booster");

		// Jetzt noch ein paar Affenpocken Impfungen erstellen. Dies darf die Statistik nicht verändern
		createImpfdossierAffenpockenWithImpfungenAm(datumVorKalenderjahr, datumImZeitraum);
		createImpfdossierAffenpockenWithImpfungenAm(datumVorZeitraum, datumImZeitraum);
		createImpfdossierAffenpockenWithImpfungenAm(datumImZeitraum, datumImZeitraum);

		anzahlErst =
			registrierungRepo.getAnzahlErstimpfungen(OrtDerImpfungTyp.IMPFZENTRUM, zeitraumVon, zeitraumBis);
		anzahlBooster =
			registrierungRepo.getAnzahlBoosterOrGrundimunisierungGT3Covid(OrtDerImpfungTyp.IMPFZENTRUM, zeitraumVon, zeitraumBis);
		anzahlBoosterOhneImpfungImKalenderjahr =
			registrierungRepo.getAnzahlCovidBoosterOhneErstimpfungOderBoosterImKalenderjahr(OrtDerImpfungTyp.IMPFZENTRUM, zeitraumVon, zeitraumBis);
		anzahlZweitOderMehrBooster = impfungRepo.getAllZweitOderMehrBooster().size();

		Assertions.assertEquals(
			0,
			anzahlErst,
			"Affenpocken-Impfungen kommen nicht zusätzlich ins Mail");
		Assertions.assertEquals(
			6,
			anzahlBooster,
			"Affenpocken-Impfungen kommen nicht zusätzlich ins Mail");
		Assertions.assertEquals(
			0,
			anzahlBoosterOhneImpfungImKalenderjahr,
			"Affenpocken-Impfungen kommen nicht zusätzlich ins Mail");
		Assertions.assertEquals(
			3,
			anzahlZweitOderMehrBooster,
			"Affenpocken-Impfungen kommen nicht zusätzlich ins Mail");
	}
}
