package ch.dvbern.oss.vacme.prozess;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadCovidJax;
import ch.dvbern.oss.vacme.repo.vmdl.VMDLRepo;
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
public class ProzessVmdlIT extends AbstractProzessIT {

	@Inject
	private VMDLRepo vmdlRepo;

	@Test
	void covidImpfungShouldBeSentToVmdl() {
		// Dossier mit Covid Impfungen erstellen
		final Impfdossier dossierCovid =
			createImpfdossierCovidWithBoosteram(LocalDate.now(), LocalDate.now());
		final Impfung newestImpfung = getNewestImpfung(dossierCovid.getRegistrierung(), KrankheitIdentifier.COVID);
		Assertions.assertNotNull(newestImpfung);
		// Es gibt zwei Varianten, wie die VMDL Daten gelesen werden, wir testen grad beide
		// Variante mit 3 Queries
		final List<VMDLUploadCovidJax> foundQuery1 = vmdlRepo.getVMDLPendenteImpfungen3QueriesCovid(10, "Test");
		Assertions.assertTrue(
			foundQuery1.stream().anyMatch(vmdlUploadJax -> vmdlUploadJax.getImpfung().equals(newestImpfung)),
			"Covid Impfung soll nach VMDL geschickt werden");
		// Variante mit 2 Queries
		final List<VMDLUploadCovidJax> foundQuery2 = vmdlRepo.getVMDLPendenteImpfungen2QueriesCovid(10, "Test");
		Assertions.assertTrue(
			foundQuery2.stream().anyMatch(vmdlUploadJax -> vmdlUploadJax.getImpfung().equals(newestImpfung)),
			"Covid Impfung soll nach VMDL geschickt werden");
	}

	@Test
	void affenpockenImpfungShouldNotBeSentToVmdl() {
		// Dossier mit Covid Impfungen erstellen
		final Impfdossier dossierAffenpocken =
			createImpfdossierAffenpockenWithImpfungenAm(LocalDate.now(), LocalDate.now());
		final Impfung newestImpfung =
			getNewestImpfung(dossierAffenpocken.getRegistrierung(), KrankheitIdentifier.AFFENPOCKEN);
		Assertions.assertNotNull(newestImpfung);
		// Es gibt zwei Varianten, wie die VMDL Daten gelesen werden, wir testen grad beide
		// Variante mit 3 Queries
		final List<VMDLUploadCovidJax> foundQuery1 = vmdlRepo.getVMDLPendenteImpfungen3QueriesCovid(10, "Test");
		Assertions.assertFalse(
			foundQuery1.stream().anyMatch(vmdlUploadJax -> vmdlUploadJax.getImpfung().equals(newestImpfung)),
			"Affenpocken Impfung soll nicht nach VMDL geschickt werden");
		// Variante mit 2 Queries
		final List<VMDLUploadCovidJax> foundQuery2 = vmdlRepo.getVMDLPendenteImpfungen2QueriesCovid(10, "Test");
		Assertions.assertFalse(
			foundQuery2.stream().anyMatch(vmdlUploadJax -> vmdlUploadJax.getImpfung().equals(newestImpfung)),
			"Affenpocken Impfung soll nicht nach VMDL geschickt werden");
	}
}
