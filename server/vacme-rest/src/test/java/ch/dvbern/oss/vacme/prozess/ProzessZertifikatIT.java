package ch.dvbern.oss.vacme.prozess;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.ZertifikatCreationDTO;
import ch.dvbern.oss.vacme.service.ZertifikatService;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertBatchType;
import ch.dvbern.oss.vacme.testing.MariaDBProfile;
import ch.dvbern.oss.vacme.testing.MariaDBTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(MariaDBTestResource.class)
@TestProfile(MariaDBProfile.class)
@QuarkusTest
public class ProzessZertifikatIT extends AbstractProzessIT {

	@Inject
	private ZertifikatService zertifikatService;

	@Test
	void covidImpfungShouldGetCertificate() {
		// Dossier mit Covid Impfungen erstellen
		final Impfdossier dossier =
			createImpfdossierCovidWithBoosteram(LocalDate.now(), LocalDate.now());
		final Impfung newestImpfung = getNewestImpfung(dossier.getRegistrierung(), KrankheitIdentifier.COVID);
		Assertions.assertNotNull(newestImpfung);
		newestImpfung.setGenerateZertifikat(true);
		impfungRepo.update(newestImpfung);
		// Es muss ein Zertifikat erstellt werden
		final List<ZertifikatCreationDTO> zertifikatsGeneration = getImpfungenToGenerateZertifikat();
		Assertions.assertTrue(
			zertifikatsGeneration.stream().anyMatch(dto -> dto.getImpfungId().equals(newestImpfung.getId())),
			"COVID-Impfung soll zu einem Zerfikat führen");
	}

	@Test
	void affenpockenImpfungShouldNotGetCertificate() {
		// Dossier mit Affenpocken Impfungen erstellen
		final Impfdossier dossier =
			createImpfdossierAffenpockenWithImpfungenAm(LocalDate.now(), LocalDate.now());
		final Impfung newestImpfung = getNewestImpfung(dossier.getRegistrierung(), KrankheitIdentifier.AFFENPOCKEN);
		Assertions.assertNotNull(newestImpfung);
		// Sogar, wenn fälschlicherweise das Flag gesetzt ist, darf der Job das Zertifikat nicht erstellen
		newestImpfung.setGenerateZertifikat(true);
		impfungRepo.update(newestImpfung);
		// Es darf kein Zertifikat erstellt werden
		final List<ZertifikatCreationDTO> zertifikatsGeneration = getImpfungenToGenerateZertifikat();
		Assertions.assertFalse(
			zertifikatsGeneration.stream().anyMatch(dto -> dto.getImpfungId().equals(newestImpfung.getId())),
			"AFFENPOCKEN-Impfung soll NICHT zu einem Zerfikat führen");
	}

	@NonNull
	private List<ZertifikatCreationDTO> getImpfungenToGenerateZertifikat() {
		final List<ZertifikatCreationDTO> zertifikatsGeneration = new ArrayList<>();
		zertifikatsGeneration.addAll(zertifikatService.findImpfungenForZertifikatsGeneration(
			CovidCertBatchType.ONLINE,
			10));
		return zertifikatsGeneration;
	}
}
