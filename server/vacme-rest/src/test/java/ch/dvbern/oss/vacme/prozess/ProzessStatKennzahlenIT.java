package ch.dvbern.oss.vacme.prozess;

import java.time.LocalDate;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.statistik.StatistikKennzahl;
import ch.dvbern.oss.vacme.entities.statistik.StatistikKennzahlEintrag;
import ch.dvbern.oss.vacme.repo.StatistikKennzahlEintragRepo;
import ch.dvbern.oss.vacme.service.StatsService;
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
public class ProzessStatKennzahlenIT extends AbstractProzessIT {

	@Inject
	StatsService statsService;

	@Inject
	StatistikKennzahlEintragRepo statistikKennzahlEintragRepo;


	@Test
	void covidImpfungenShouldBeInKennzahlenSnapshot() {
		createImpfdossierCovidWithBoosteram(LocalDate.now(), LocalDate.now());
		Assertions.assertEquals(
			2,
			takeSnapshotAndCountImpfungen(),
			"Covid Impfungen sollen in den Kennzahlen-Snapshot kommen");
	}

	@Test
	void affenpockenImpfungenShouldNotBeInKennzahlenSnapshot() {
		createImpfdossierAffenpockenWithImpfungenAm(LocalDate.now(), LocalDate.now());
		Assertions.assertEquals(
			0,
			takeSnapshotAndCountImpfungen(),
			"Affenpocken Impfungen sollen nicht in den Kennzahlen-Snapshot kommen");
	}

	private long takeSnapshotAndCountImpfungen() {
		statsService.takeKennzahlenSnapshot();
		final long countAllImpfungen = statistikKennzahlEintragRepo.getAll().stream().filter(
			statistikKennzahlEintrag -> (
				statistikKennzahlEintrag.getStatistikKennzahl() == StatistikKennzahl.TOTAL_DURCHGEFUEHRTE_IMPFUNG1
					|| statistikKennzahlEintrag.getStatistikKennzahl() == StatistikKennzahl.TOTAL_DURCHGEFUEHRTE_IMPFUNG2
					|| statistikKennzahlEintrag.getStatistikKennzahl() == StatistikKennzahl.TOTAL_DURCHGEFUEHRTE_IMPFUNGN
			)).mapToLong(StatistikKennzahlEintrag::getWert).sum();
		return countAllImpfungen;
	}
}
