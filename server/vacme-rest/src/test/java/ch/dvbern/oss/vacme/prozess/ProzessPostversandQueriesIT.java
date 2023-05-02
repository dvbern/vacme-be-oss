package ch.dvbern.oss.vacme.prozess;

import java.util.List;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.service.PostversandQueriesService;
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
public class ProzessPostversandQueriesIT extends AbstractProzessIT {

	@Inject
	PostversandQueriesService postversandQueriesService;

	@Test
	void postversandQueriesWorking() {

		final List postversandQueryTerminBestaetigung_regnum =
			postversandQueriesService.getPostversandQueryTerminBestaetigung_regnum();
		Assertions.assertNotNull(postversandQueryTerminBestaetigung_regnum);

		final List postversandQueryTerminBestaetigung_dumpfile =
			postversandQueriesService.getPostversandQueryTerminBestaetigung_dumpfile();
		Assertions.assertNotNull(postversandQueryTerminBestaetigung_dumpfile);

		final List postversandQueryTerminBestaetigung_regID =
			postversandQueriesService.getPostversandQueryTerminBestaetigung_regID();
		Assertions.assertNotNull(postversandQueryTerminBestaetigung_regID);

		final List postversandQueryTerminBestaetigung_update =
			postversandQueriesService.getPostversandQueryTerminBestaetigung_update();
		Assertions.assertNotNull(postversandQueryTerminBestaetigung_update);

		final List postversandQueryTerminAbsage_regnum =
			postversandQueriesService.getPostversandQueryTerminAbsage_regnum();
		Assertions.assertNotNull(postversandQueryTerminAbsage_regnum);

		final List postversandQueryTerminAbsage_dumpfile =
			postversandQueriesService.getPostversandQueryTerminAbsage_dumpfile();
		Assertions.assertNotNull(postversandQueryTerminAbsage_dumpfile);

		final List postversandQueryTerminAbsage_regID =
			postversandQueriesService.getPostversandQueryTerminAbsage_regID();
		Assertions.assertNotNull(postversandQueryTerminAbsage_regID);

		final List postversandQueryTerminAbsage_update =
			postversandQueriesService.getPostversandQueryTerminAbsage_update();
		Assertions.assertNotNull(postversandQueryTerminAbsage_update);

		final List postversandQueryRegistrierungBestaetigung_regnum =
			postversandQueriesService.getPostversandQueryRegistrierungBestaetigung_regnum();
		Assertions.assertNotNull(postversandQueryRegistrierungBestaetigung_regnum);

		final List postversandQueryRegistrierungBestaetigung_dumpfile =
			postversandQueriesService.getPostversandQueryRegistrierungBestaetigung_dumpfile();
		Assertions.assertNotNull(postversandQueryRegistrierungBestaetigung_dumpfile);

		final List postversandQueryRegistrierungBestaetigung_regID =
			postversandQueriesService.getPostversandQueryRegistrierungBestaetigung_regID();
		Assertions.assertNotNull(postversandQueryRegistrierungBestaetigung_regID);

		final List postversandQueryRegistrierungBestaetigung_update =
			postversandQueriesService.getPostversandQueryRegistrierungBestaetigung_update();
		Assertions.assertNotNull(postversandQueryRegistrierungBestaetigung_update);

		final List postversandQueryFreigabeBoosterInfo_regnum =
			postversandQueriesService.getPostversandQueryFreigabeBoosterInfo_regnum();
		Assertions.assertNotNull(postversandQueryFreigabeBoosterInfo_regnum);

		final List postversandQueryFreigabeBoosterInfo_regID =
			postversandQueriesService.getPostversandQueryFreigabeBoosterInfo_regID();
		Assertions.assertNotNull(postversandQueryFreigabeBoosterInfo_regID);

		final List postversandQueryFreigabeBoosterInfo_dumpfile =
			postversandQueriesService.getPostversandQueryFreigabeBoosterInfo_dumpfile();
		Assertions.assertNotNull(postversandQueryFreigabeBoosterInfo_dumpfile);

		final List postversandQueryFreigabeBoosterInfo_update =
			postversandQueriesService.getPostversandQueryFreigabeBoosterInfo_update();
		Assertions.assertNotNull(postversandQueryFreigabeBoosterInfo_update);

		final List postversandQueryTerminZertifikatStornierung_regnum =
			postversandQueriesService.getPostversandQueryTerminZertifikatStornierung_regnum();
		Assertions.assertNotNull(postversandQueryTerminZertifikatStornierung_regnum);

		final List postversandQueryTerminZertifikatStornierung_regID =
			postversandQueriesService.getPostversandQueryTerminZertifikatStornierung_regID();
		Assertions.assertNotNull(postversandQueryTerminZertifikatStornierung_regID);

		final List postversandQueryTerminZertifikatStornierung_dumpfile =
			postversandQueriesService.getPostversandQueryTerminZertifikatStornierung_dumpfile();
		Assertions.assertNotNull(postversandQueryTerminZertifikatStornierung_dumpfile);

		final List postversandQueryTerminZertifikatStornierung_update =
			postversandQueriesService.getPostversandQueryTerminZertifikatStornierung_update();
		Assertions.assertNotNull(postversandQueryTerminZertifikatStornierung_update);

		final List postversandQueryOnboardingLetter_regnum =
			postversandQueriesService.getPostversandQueryOnboardingLetter_regnum();
		Assertions.assertNotNull(postversandQueryOnboardingLetter_regnum);

		final List postversandQueryOnboardingLetter_regID =
			postversandQueriesService.getPostversandQueryOnboardingLetter_regID();
		Assertions.assertNotNull(postversandQueryOnboardingLetter_regID);

		final List postversandQueryOnboardingLetter_dumpfile =
			postversandQueriesService.getPostversandQueryOnboardingLetter_dumpfile();
		Assertions.assertNotNull(postversandQueryOnboardingLetter_dumpfile);

		final List postversandQueryOnboardingLetter_update =
			postversandQueriesService.getPostversandQueryOnboardingLetter_update();
		Assertions.assertNotNull(postversandQueryOnboardingLetter_update);

	}
}
