package ch.dvbern.oss.vacme.repo;

import ch.dvbern.oss.vacme.entities.zertifikat.ZertifizierungsToken;
import ch.dvbern.oss.vacme.util.ObjectMapperTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZertifikatRepoTest {

	@Test
	void extractAndStoreGueltigkeitForToken() {

		ObjectMapper mapper = ObjectMapperTestUtil.createObjectMapperForTest();
		ZertifikatRepo zertifikatRepo = new ZertifikatRepo(null, mapper);
		String  testtokenString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiI4M2YyYTk4Ny0zYzJlLTRjMGEtODU3Yi04ZTFkMzc4ZmM4ZWUiLCJpc3MiOiJodHRwczovL2NvdmlkY2VydGlmaWNhdGUtYS1tYW5hZ2VtZW50LWEuYmFnLmFkbWluLmNoIiwiaWF0IjoxNjQ3ODE3NTQ5LCJuYmYiOjE2NDc4MTc1NDksInNjb3BlIjoiY292aWRjZXJ0Y3JlYXRpb24iLCJ1c2VyRXh0SWQiOiI1Njk2MjYiLCJpZHBzb3VyY2UiOiJFLUlEIENILUxPR0lOIiwidXNlcnJvbGVzIjpbInRlc3RfYWNjZXNzIiwidGVzdF9hdXRob3JpemF0aW9uIiwiYmFnLWNjLWNyZWF0b3IiXSwidHlwIjoiYXV0aG1hY2hpbmUrand0IiwiZXhwIjoxNjQ3ODEwNzQ5fQ.VNicqSTXsLJ_566PK_29T5yuT_lgtDd8j6-T1Sv5OrJ7X3dj5uUedyuF_Q1J2u1TAMwghXj77SAbkuI6309IAPGV4EHhhB7kX_-45IM2RaYOUCSd9unVJ12Zo7k5ZdTm2cSZ2wPV-ojW0embkKU3sN3B-8-Dou-kmEAOah0AicivF0KXTgjimRavVXd0i7c-hbSGok_-OEtxMcX4jHhYtMMmHD35olN--mIMfSz9TImLUiS7S5fhBR81kKNd8LkB6Z1rMu-CRtnUnqxjbdvk6A3Sb165Pj6lwlc9woK6p8KUc86wv2sZIbHIPCfmV0DjgNLXNoLxD1cEKTnDkUwW3g";

		@NotNull ZertifizierungsToken testtoken = new ZertifizierungsToken();
		testtoken.setToken(testtokenString);
		zertifikatRepo.extractAndStoreGueltigkeitForToken(testtoken);
		Assertions.assertNotNull(testtoken.getGueltigkeit());
	}
}
