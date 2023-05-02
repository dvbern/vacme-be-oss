package ch.dvbern.oss.vacme.prozess;

import java.util.List;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.service.WHoch2QueriesService;
import ch.dvbern.oss.vacme.testing.MariaDBProfile;
import ch.dvbern.oss.vacme.testing.MariaDBTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@QuarkusTestResource(MariaDBTestResource.class)
@TestProfile(MariaDBProfile.class)
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
public class ProzessWHoch2QueriesIT extends AbstractProzessIT {

	@Inject
	WHoch2QueriesService wHoch2QueriesService;

	@Test
	void wHoch2QueriesWorking() {
		final List wHoch2QueryRegistrierungen = wHoch2QueriesService.getWHoch2QueryRegistrierungen();
		Object[] headerRowReg = (Object[]) wHoch2QueryRegistrierungen.get(0);
		Assertions.assertEquals("Registrierungs_ID", headerRowReg[0]);

		final List wHoch2QueryImpfungen = wHoch2QueriesService.getWHoch2QueryImpfungen();
		Object[] headerRowImpf = (Object[]) wHoch2QueryImpfungen.get(0);
		Assertions.assertEquals("Registrierungs_ID", headerRowImpf[0]);

		final List wHoch2QueryImpfslots = wHoch2QueriesService.getWHoch2QueryImpfslots();
		Object[] headerRowSlots = (Object[]) wHoch2QueryImpfslots.get(0);
		Assertions.assertNotNull(headerRowSlots[0]);
	}

	@Test
	void wHoch2EdorexQueriesWorking() {
		final List wHoch2EdorexQueryRegistrierungen = wHoch2QueriesService.getWHoch2EdorexQueryRegistrierungen();
		Object[] headerRowReg = (Object[]) wHoch2EdorexQueryRegistrierungen.get(0);
		Assertions.assertEquals("Registrierungs_ID", headerRowReg[0]);

		final List wHoch2EdorexQueryImpfungen = wHoch2QueriesService.getWHoch2EdorexQueryImpfungen();
		Object[] headerRowImpf = (Object[]) wHoch2EdorexQueryImpfungen.get(0);
		Assertions.assertEquals("Registrierungs_ID", headerRowImpf[0]);

		final List wHoch2EdorexQueryImpfslots = wHoch2QueriesService.getWHoch2EdorexQueryImpfslots();
		Object[] headerRowSlots = (Object[]) wHoch2EdorexQueryImpfslots.get(0);
		Assertions.assertNotNull(headerRowSlots[0]);
	}
}
