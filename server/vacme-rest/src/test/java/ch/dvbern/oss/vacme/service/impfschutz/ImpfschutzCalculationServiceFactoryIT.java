/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
  version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
  will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service.impfschutz;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
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
class ImpfschutzCalculationServiceFactoryIT {

	@Inject
	ImpfschutzCalculationServiceFactory factory;

	@Test
	void createImpfschutzCalculationService() {
		for (KrankheitIdentifier krankheitIdentifier : KrankheitIdentifier.values()) {
			factory.createImpfschutzCalculationService(krankheitIdentifier);
			Assertions.assertTrue(true, "Fuer alle Krankheiten ist ein CalculationService vorhanden");
		}
	}
}
