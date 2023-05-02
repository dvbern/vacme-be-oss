package ch.dvbern.oss.vacme.prozess;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
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
public class ProzessEngineRecalculationIT extends AbstractProzessIT {


	@Test
	void covidImpfungShouldBeMovedToImmunisiert() {
		Impfdossier dossier =
			createImpfdossierCovidWithBoosteram(LocalDate.now(), LocalDate.now());
		final Impfdossier dossierAbgeschlossen = impfdossierRepo.getImpfdossier(dossier.toId());
		dossierAbgeschlossen.setDossierStatus(ImpfdossierStatus.ABGESCHLOSSEN);
		impfdossierRepo.update(dossierAbgeschlossen);

		final List<String> regNummern = this.registrierungRepo.findRegsWithVollstImpfschutzToMoveToImmunisiert(10);

		Assertions.assertTrue(
			regNummern.stream().anyMatch(vmdlUploadJax -> vmdlUploadJax.equals(dossierAbgeschlossen.getRegistrierung().getRegistrierungsnummer())),
			"Covid Impfung soll nach Immunisiert verschoben werden");
	}

	@Test
	void affenpockenImpfungShouldNotBeMovedToImmunisiert() {
		Impfdossier dossier =
			createImpfdossierAffenpockenWithImpfungenAm(LocalDate.now(), LocalDate.now());
		final Impfdossier dossierAbgeschlossen = impfdossierRepo.getImpfdossier(dossier.toId());
		// Status explizit auf ABGESCHLOSSEN setzen, dies kann eigentlich gar nicht vorkommen
		dossierAbgeschlossen.setDossierStatus(ImpfdossierStatus.ABGESCHLOSSEN);
		impfdossierRepo.update(dossierAbgeschlossen);

		final List<String> regNummern = this.registrierungRepo.findRegsWithVollstImpfschutzToMoveToImmunisiert(10);

		Assertions.assertFalse(
			regNummern.stream().anyMatch(vmdlUploadJax -> vmdlUploadJax.equals(dossierAbgeschlossen.getRegistrierung().getRegistrierungsnummer())),
			"Affenpocken Impfung soll nicht nach Immunisiert verschoben werden");
	}
}
