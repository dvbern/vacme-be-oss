package ch.dvbern.oss.vacme.prozess;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.jax.applicationhealth.ResultDTO;
import ch.dvbern.oss.vacme.repo.ApplicationHealthRepo;
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
public class ProzessApplicationHealthCheckIT extends AbstractProzessIT {

	@Inject
	ApplicationHealthRepo applicationHealthRepo;

	@Test
	void allQueriesWorking() {
		final ResultDTO healthCheckInvalidImpfslots =
			applicationHealthRepo.getHealthCheckInvalidImpfslots();
		Assertions.assertNotNull(healthCheckInvalidImpfslots);

		final ResultDTO healthCheckVollstaendigerImpfschutzKeineImpfungen =
			applicationHealthRepo.getHealthCheckVollstaendigerImpfschutzKeineImpfungen();
		Assertions.assertNotNull(healthCheckVollstaendigerImpfschutzKeineImpfungen);

		final ResultDTO healthCheckDoppeltGeimpftOhneVollsaendigerImpfschutz =
			applicationHealthRepo.getHealthCheckDoppeltGeimpftOhneVollsaendigerImpfschutz();
		Assertions.assertNotNull(healthCheckDoppeltGeimpftOhneVollsaendigerImpfschutz);

		final ResultDTO healthCheckAbgeschlossenOhneVollsaendigerImpfschutz =
			applicationHealthRepo.getHealthCheckAbgeschlossenOhneVollsaendigerImpfschutz();
		Assertions.assertNotNull(healthCheckAbgeschlossenOhneVollsaendigerImpfschutz);

		final ResultDTO healthCheckNichtAbgeschlossenAberVollstaendigerImpfschutz =
			applicationHealthRepo.getHealthCheckNichtAbgeschlossenAberVollstaendigerImpfschutz();
		Assertions.assertNotNull(healthCheckNichtAbgeschlossenAberVollstaendigerImpfschutz);

		final ResultDTO healthCheckAbgeschlossenOhneCoronaAberVollstaendigerImpfschutz =
			applicationHealthRepo.getHealthCheckAbgeschlossenOhneCoronaAberVollstaendigerImpfschutz();
		Assertions.assertNotNull(healthCheckAbgeschlossenOhneCoronaAberVollstaendigerImpfschutz);

		final ResultDTO healthCheckFailedZertifikatRevocations =
			applicationHealthRepo.getHealthCheckFailedZertifikatRevocations();
		Assertions.assertNotNull(healthCheckFailedZertifikatRevocations);

		final ResultDTO healthCheckFailedZertifikatRecreations =
			applicationHealthRepo.getHealthCheckFailedZertifikatRecreations();
		Assertions.assertNotNull(healthCheckFailedZertifikatRecreations);

		final ResultDTO healthCheckGebuchteTermine =
			applicationHealthRepo.getHealthCheckGebuchteTermine();
		Assertions.assertNotNull(healthCheckGebuchteTermine);

		final ResultDTO healthCheckVerwaisteImpfungen =
			applicationHealthRepo.getHealthCheckVerwaisteImpfungen();
		Assertions.assertNotNull(healthCheckVerwaisteImpfungen);

		final ResultDTO healthCheckRegistrierungenMitImpfungNichtAmTermindatum =
			applicationHealthRepo.getHealthCheckRegistrierungenMitImpfungNichtAmTermindatum();
		Assertions.assertNotNull(healthCheckRegistrierungenMitImpfungNichtAmTermindatum);

		final ResultDTO healthCheckFalschVerknuepfteZertifikate =
			applicationHealthRepo.getHealthCheckFalschVerknuepfteZertifikate();
		Assertions.assertNotNull(healthCheckFalschVerknuepfteZertifikate);

		final ResultDTO healthCheckRegsOhneCovidDossier =
			applicationHealthRepo.getRegistrierungenOhneCovidDossier();
		Assertions.assertNotNull(healthCheckRegsOhneCovidDossier);
	}
}
