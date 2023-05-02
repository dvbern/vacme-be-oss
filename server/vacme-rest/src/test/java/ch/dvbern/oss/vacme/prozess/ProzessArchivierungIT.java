package ch.dvbern.oss.vacme.prozess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.jax.ArchivierungDataRow;
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
public class ProzessArchivierungIT extends AbstractProzessIT {

	@Test
	void covidImpfungenShouldBeArchived() {
		final Impfdossier dossier =
			createImpfdossierCovidWithBoosteram(LocalDate.now().minusMonths(14), LocalDate.now().minusMonths(13));

		List<ArchivierungDataRow> dataRows = fragebogenRepo.getAbgeschlossenNotArchiviertDataOlderThan(LocalDateTime.now());
		Assertions.assertTrue(
			dataRows.stream().anyMatch(archivierungJax -> archivierungJax.getFragebogen().getRegistrierung().equals(dossier.getRegistrierung())),
			"Covid Impfung soll archiviert werden");
	}

	@Test
	void affenpockenImpfungenShouldNotBeArchived() {
		final Impfdossier dossier =
			createImpfdossierAffenpockenWithImpfungenAm(LocalDate.now().minusMonths(14), LocalDate.now().minusMonths(13));

		List<ArchivierungDataRow> dataRows = fragebogenRepo.getAbgeschlossenNotArchiviertDataOlderThan(LocalDateTime.now());
		Assertions.assertFalse(
			dataRows.stream().anyMatch(archivierungJax -> archivierungJax.getFragebogen().getRegistrierung().equals(dossier.getRegistrierung())),
			"Affenpocken Impfung soll nicht archiviert werden");
	}
}
