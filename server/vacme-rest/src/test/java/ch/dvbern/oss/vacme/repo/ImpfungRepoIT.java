package ch.dvbern.oss.vacme.repo;

import java.time.LocalDate;
import java.util.Optional;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.prozess.AbstractProzessIT;
import ch.dvbern.oss.vacme.testing.MariaDBProfile;
import ch.dvbern.oss.vacme.testing.MariaDBTestResource;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(MariaDBTestResource.class)
@TestProfile(MariaDBProfile.class)
@QuarkusTest
public class ImpfungRepoIT extends AbstractProzessIT {

	@Test
	void impfinformationenDtoAlwaysWithKrankheit() {

		final Impfdossier dossierCovid =
			createImpfdossierCovidWithBoosteram(LocalDate.now(), LocalDate.now());
		final String regNrWithOnlyCovid = dossierCovid.getRegistrierung().getRegistrierungsnummer();

		final Optional<ImpfinformationDto> infosCovidForDossierWithOnlyCovid =
			impfungRepo.getImpfinformationenOptional(regNrWithOnlyCovid, KrankheitIdentifier.COVID);
		Assertions.assertNotNull(infosCovidForDossierWithOnlyCovid);
		Assertions.assertTrue(infosCovidForDossierWithOnlyCovid.isPresent());
		Assertions.assertEquals(
			KrankheitIdentifier.COVID,
			infosCovidForDossierWithOnlyCovid.get().getKrankheitIdentifier());

		final Optional<ImpfinformationDto> infosAffenpockenForDossierWithOnlyCovid =
			impfungRepo.getImpfinformationenOptional(regNrWithOnlyCovid, KrankheitIdentifier.AFFENPOCKEN);
		Assertions.assertNotNull(infosAffenpockenForDossierWithOnlyCovid);
		Assertions.assertFalse(infosAffenpockenForDossierWithOnlyCovid.isPresent());

		final Impfdossier dossierAffenpocken =
			createImpfdossierAffenpockenWithImpfungenAm(LocalDate.now(), LocalDate.now());
		final String regNrWithOnlyAffenpocken = dossierAffenpocken.getRegistrierung().getRegistrierungsnummer();

		final Optional<ImpfinformationDto> infosAffenpockenForDossierWithOnlyAffenpocken =
			impfungRepo.getImpfinformationenOptional(regNrWithOnlyAffenpocken, KrankheitIdentifier.AFFENPOCKEN);
		Assertions.assertNotNull(infosAffenpockenForDossierWithOnlyAffenpocken);
		Assertions.assertTrue(infosAffenpockenForDossierWithOnlyAffenpocken.isPresent());
		Assertions.assertEquals(
			KrankheitIdentifier.AFFENPOCKEN,
			infosAffenpockenForDossierWithOnlyAffenpocken.get().getKrankheitIdentifier());

		final Optional<ImpfinformationDto> infosCovidForDossierWithOnlyAffenpocken =
			impfungRepo.getImpfinformationenOptional(regNrWithOnlyAffenpocken, KrankheitIdentifier.COVID);
		Assertions.assertNotNull(infosCovidForDossierWithOnlyAffenpocken);
		Assertions.assertFalse(infosCovidForDossierWithOnlyAffenpocken.isPresent());
	}
}
