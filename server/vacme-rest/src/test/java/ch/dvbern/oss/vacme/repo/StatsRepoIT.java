/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.repo;

import javax.inject.Inject;

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
public class StatsRepoIT {
	@Inject
	StatsRepo statsRepo;

	@Test
	void testTotalStatEmpty() {
		Assertions.assertNotNull(statsRepo);
		long registrierungTotal = statsRepo.getAnzahlRegistrierungenCallcenter();

		Assertions.assertEquals(0, registrierungTotal);

	}
}
