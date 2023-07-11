/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.rest.health;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.rest.health.keycloak.KeycloakConnectionhealthCheck;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertSignatureService;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertSignatureVerificationService;
import io.smallrye.health.api.Wellness;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

@Wellness
@ApplicationScoped
@Slf4j
public class CovidCertificationKeypairConfigHealthCheck extends KeycloakConnectionhealthCheck implements HealthCheck {

	private CovidCertSignatureService covidCertSignatureService;
	private CovidCertSignatureVerificationService covidCertSignatureVerificationService;

	@Inject
	public CovidCertificationKeypairConfigHealthCheck(
		CovidCertSignatureService covidCertSignatureService,
		CovidCertSignatureVerificationService covidCertSignatureVerificationService

	) {
		this.covidCertSignatureService = covidCertSignatureService;
		this.covidCertSignatureVerificationService = covidCertSignatureVerificationService;
	}

    @Override
    public HealthCheckResponse call() {
		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("CovidCert Keypair Setup health check");

		String testpayload = "{\n"
			+ "  \"name\": {\n"
			+ "    \"familyName\": \"Federer\",\n"
			+ "    \"givenName\": \"Roger\"\n"
			+ "  },\n"
			+ "  \"dateOfBirth\": \"1950-06-04\",\n"
			+ "  \"language\": \"de\",\n"
			+ "  \"otp\": \"string\",\n"
			+ "  \"recoveryInfo\": [\n"
			+ "    {\n"
			+ "      \"dateOfFirstPositiveTestResult\": \"2021-10-03\",\n"
			+ "      \"countryOfTest\": \"CH\"\n"
			+ "    }\n"
			+ "  ]\n"
			+ "}";

		try {
			String b64SigString = covidCertSignatureService.sign(testpayload);
			boolean passesVerification = this.covidCertSignatureVerificationService.verify(testpayload, b64SigString);
			if (passesVerification) {
				responseBuilder.up().withData("signatureMatches", passesVerification);;
			} else{
				responseBuilder.down().withData("signatureMatches", passesVerification);
			}
		} catch (Exception ex) {
			responseBuilder.down()
				.withData("Exception", ex.getMessage());

		}
		return responseBuilder.build();

	}

}
