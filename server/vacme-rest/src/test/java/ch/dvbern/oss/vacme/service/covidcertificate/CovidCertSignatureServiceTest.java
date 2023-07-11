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

package ch.dvbern.oss.vacme.service.covidcertificate;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import io.smallrye.jwt.auth.principal.ParseException;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import static ch.dvbern.oss.vacme.service.covidcertificate.CovidCertUtils.normalizeJson;

class CovidCertSignatureServiceTest {

	private CovidCertSignatureService covidCertSignatureService;
	private CovidCertSignatureVerificationService covidCertSignatureSVerificationervice;

	@BeforeEach
	void setUp() throws InvalidKeySpecException, NoSuchAlgorithmException, CertificateException {

		String dummyPrivateKey = "-----BEGIN PRIVATE KEY-----\n"
			+ "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2V5RxA+v8zPk/\n"
			+ "W/Ec6yWsB5rxgZmW1ec5RWoNCrKUhNTMvk47cdYzFaNeWHktl+Y8b50lR+NSGM7G\n"
			+ "FTfeBatyubml9gq+NoIwPHM7KaFzHbH/PkDFN9X6Vp4WbS+nSiOFOWA2B7Nykg/3\n"
			+ "UknOE2RJvHQUhTB62G96iOLCnS2e2KVXVIxxxdmKllmu6xd4O/uJOuhxSTXr5zQf\n"
			+ "Jw2MBb2LPOAdcdh9/53vnW/e3Wkz4RyWNMX56Cu+3JSbt+w3LpLuAY86SABuo6sv\n"
			+ "6B95qKeI7buaYOswB386JO14e9WQ7S9YEo2lTo2EsxNg7qq7nK+y50XFvN2cTyCm\n"
			+ "yJ8ZQR7vAgMBAAECggEAN/HUAf7Fi1kqSfXAGvLMqqTktZ9hS8WSPWCeQqUbGHVO\n"
			+ "wwjKgPOS2DaV2q8DcuktgzJtgRA23yvsWRsOeoi7yyXPn2tHbYfU1s7dPfQubF7a\n"
			+ "i2kCv1+7v6SOuWvDmKynDEuHyNwBAzBsRKITMe6CMRtodBlPQAmi3nIcPMCzzA3A\n"
			+ "ttAcstUk0oXqqhcspHl06d2ui6Ubpb42nbw5COMRoH7VxmcOWg1Yp3ooxHEfPMyC\n"
			+ "TznU6Mdrh738nz2NkRiO81huBqytBM24YTcxqtIfHsww8Z9EfhZbqYEWmM6Px3gx\n"
			+ "eGBzIXV8cKYWJZGwJaU7WE7BnOydFYFAuXmw80w0yQKBgQDgASmITdNIhKG47EXm\n"
			+ "TD7zoL5tH3wYE0KaD5zviXGufEZmTV+e7f2v8P8f/CrmFJKzvjf63dvUEfcTDNnN\n"
			+ "Jj0lglWxFmTLxvyDrAKFAYGjoOMRzpT2dWp59XADVIKa9FfboFSMVrGXZErv38NF\n"
			+ "HzBHrzKc4126x5TbgYDLRystlwKBgQDQYwKRXbTPX73uRsGmiTBG4XhOkFy5lkmu\n"
			+ "XKGXProyML15IOzXe8fqaBOMLlMBCdtYvDsZl4cJ/dzZsICSfrtuqCKVumKDNXxm\n"
			+ "QDcF9JZKN6+2VSrptIx1wok0sqq83I9ORivYu5TFG7vlWnr6rKHdxKugrs5O2U6D\n"
			+ "eFlIiZV0aQKBgQC4x5yoCHLKdkYlkhmTAMBJHgcXYwptna2qAkkVu04gfflguGCx\n"
			+ "bGaBNQ5vJweIJd5iEP6CnXKR/IPQniAoP2vfPiVL4EojYrC4OTMW3og/Hx9QeBWf\n"
			+ "PHopwpG03YijI/45eIdmALmcRaofN3kP0stzxoy8qD2QLsSXUbuZc8qlXwKBgAGv\n"
			+ "a0vlqBqtOYn9xH8mSN+p3yEzhaxyjLH+SchBV5wkTMP4AjJT6+/3W4EctsshW0bC\n"
			+ "bu/sC8mG5gxUoxWMNNPWREFVuoo5GoJxLoC706NSA/fDhI9TSHeDVTy53Sud1QLb\n"
			+ "Cmu8SJ23dbOLIcd5Me3rM0Aface1di8WABLYGf9RAoGAHKugH62g+YiwXDtD6fI4\n"
			+ "eo4klaGwuw1rXZ9phywdWWqZobt3hO3JU4ivK6nM2SpDt2q/UJAWUmU/ddHMvsEJ\n"
			+ "qtequhynb4tYRlh5wiYZezgfpbfU6F+AsJYktWS9JeWyDMYsoqhe1QRZaQ8rQy68\n"
			+ "t0aDVgAPgANp1qcGYNxY8nI=\n"
			+ "-----END PRIVATE KEY-----";


		String dummyCert = "-----BEGIN CERTIFICATE-----\n"
			+ "MIIDJjCCAg6gAwIBAgIEYKzIrzANBgkqhkiG9w0BAQsFADBVMQswCQYDVQQGEwJD\n"
			+ "SDENMAsGA1UECAwEQmVybjEQMA4GA1UECgwHRFYgQmVybjEMMAoGA1UECwwDV2Vi\n"
			+ "MRcwFQYDVQQDDA52YWNtZS10ZXN0LWtleTAeFw0yMTA1MjUwOTUxNDNaFw0zMTA1\n"
			+ "MjUwOTUxNDNaMFUxCzAJBgNVBAYTAkNIMQ0wCwYDVQQIDARCZXJuMRAwDgYDVQQK\n"
			+ "DAdEViBCZXJuMQwwCgYDVQQLDANXZWIxFzAVBgNVBAMMDnZhY21lLXRlc3Qta2V5\n"
			+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtleUcQPr/Mz5P1vxHOsl\n"
			+ "rAea8YGZltXnOUVqDQqylITUzL5OO3HWMxWjXlh5LZfmPG+dJUfjUhjOxhU33gWr\n"
			+ "crm5pfYKvjaCMDxzOymhcx2x/z5AxTfV+laeFm0vp0ojhTlgNgezcpIP91JJzhNk\n"
			+ "Sbx0FIUwethveojiwp0tntilV1SMccXZipZZrusXeDv7iTrocUk16+c0HycNjAW9\n"
			+ "izzgHXHYff+d751v3t1pM+EcljTF+egrvtyUm7fsNy6S7gGPOkgAbqOrL+gfeain\n"
			+ "iO27mmDrMAd/OiTteHvVkO0vWBKNpU6NhLMTYO6qu5yvsudFxbzdnE8gpsifGUEe\n"
			+ "7wIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBA0rVpGx03XVb7EKZWYqBlHUmDrhj6\n"
			+ "f+0ymyzb/N7vfE9o62YkObb5/YgQ0yjTFWayWUvKcbQI0cybLxO62rsDFKk5XZ1a\n"
			+ "+DAmkBD/FybKte2kZ7iQsJxA7xWgsQgq+8uDL1TZPxonukC287lxsajUkFYdtqPo\n"
			+ "iihE9hSHk5Z0cEPURVdlkUXcgNXxyb+inGc9cXc+PPIp2xnWuwHGBvRF2jz35Sq5\n"
			+ "4CS5RYGaOR4OLUOar3V4SPQQCme/EzXDe2XyMo4TSbKELt2eopwWxPGT+8HWmMEP\n"
			+ "BxmvO/Xfvcdb7PTCZr5ZAcp+SZzqS9mMnmUwuwF3qXYL8N3crUG7DwNk\n"
			+ "-----END CERTIFICATE-----";



		String dummyPrivateKeyString = transfromToPKKeystring(dummyPrivateKey);

		PrivateKey privateKey = getPrivateKey(dummyPrivateKeyString);
		covidCertSignatureService = new CovidCertSignatureService(privateKey);

		String dummyCertString = transfromToCertKeystring(dummyCert);
		X509Certificate certificate = getCertificate(dummyCertString);
		covidCertSignatureSVerificationervice = new CovidCertSignatureVerificationService(certificate);
	}

	private X509Certificate getCertificate(String certificateString) throws CertificateException {
		Validate.isTrue(!certificateString.contains("-----BEGIN CERTIFICATE-----"),"Please specify Cert as a Raw String");
		byte[] certificateData = Base64.getDecoder().decode(certificateString);
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		return  (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
	}

	/**
	 * if the keystring is pem format strip all nonessential parts
	 *
	 * @return raw keystring in Base64
	 */
	private String transfromToPKKeystring(String pemKeyString) {
		return pemKeyString
			.replace("-----BEGIN PRIVATE KEY-----", "")
			.replaceAll(System.lineSeparator(), "")
			.replace("-----END PRIVATE KEY-----", "")
			.replace("\n", "");
	}


	private String transfromToCertKeystring(String pemKeyString) {
		return pemKeyString
			.replace("-----BEGIN CERTIFICATE-----", "")
			.replaceAll(System.lineSeparator(), "")
			.replace("-----END CERTIFICATE-----", "")
			.replace("\n", "");
	}



	@Test
	void normalizationTest() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, ParseException {
		String testpayload = "{\n"
			+ "  \"name\": {\n"
			+ "    \"familyName\": \"Federer\",\n"
			+ "    \"givenName\": \"Roger Adrian\"\n"
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

		String s = normalizeJson(testpayload);
		String expectedString = "{\"name\":{\"familyName\":\"Federer\",\"givenName\":\"RogerAdrian\"},"
			+ "\"dateOfBirth\":\"1950-06-04\",\"language\":\"de\",\"otp\":\"string\","
			+ "\"recoveryInfo\":[{\"dateOfFirstPositiveTestResult\":\"2021-10-03\",\"countryOfTest\":\"CH\"}]}";
		Assertions.assertEquals(expectedString, s);


		String testpayloadOther = "{"
			+ "  \"name\": {"
			+ "    \"familyName\": \"Federer\",\n"
			+ "    \"givenName\": \"Roger Adrian\"\n"
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
		String otherNormalized = normalizeJson(testpayloadOther);
		Assertions.assertEquals(s, otherNormalized);
	}

	@Test
	void signTest() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, ParseException {
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

		String b64SigString = covidCertSignatureService.sign(testpayload);
		String expectedSigString = "Ur2BihJB6hzjAgDH7cxMJolgGOKSGe1NUW7ytKIzsY5n1EXtAD+RTBgBGOsAfcoR4cIHzDqg"
			+ "+ZW4w1pP5UksHY942Te/SK8Y9fsnXv0QtXMCacZTJBmm4xydTDA583CpmcEfmXXH3juAI3ARGBdhuAzSSme/r6wne"
			+ "+fZF7iVUotEFVeHFBHlU7A5n68qoy9nI1zjTkc8zTHO/nkc4DRkduxTi+sFTOTLkmluFl"
			+ "+vE8LxwtgigdGavMDwRARLM7sKXmW6VJ6K8De0WmSlT"
			+ "/Pj5QMiOKq4h5a4WsozcGWYgiYrI3rGjbyvo4HihPCZIgHtGgZQvSqbGEmBLydrDT+ljw==";
		Assertions.assertEquals(expectedSigString, b64SigString);
		boolean passesVerification = this.covidCertSignatureSVerificationervice.verify(testpayload, b64SigString);
		Assertions.assertTrue(passesVerification, "Expect Signature to validate");

		boolean failsVerification = this.covidCertSignatureSVerificationervice.verify("invalid Payload", b64SigString);
		Assert.assertFalse(failsVerification);

		String changedPayload = testpayload.replace("Roger", "Markus");
		boolean changedNameVerification = this.covidCertSignatureSVerificationervice.verify(changedPayload, b64SigString);
		Assert.assertFalse(changedNameVerification);

	}

	@Test
	void invalidCertUsedToCheckSignatureTest() throws CertificateException, InvalidKeySpecException,
		NoSuchAlgorithmException, SignatureException, InvalidKeyException {

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

		String b64SigString = covidCertSignatureService.sign(testpayload);

		String invalidCert = "-----BEGIN CERTIFICATE-----\n"
			+ "MIICqDCCAZCgAwIBAgIEYK0VmzANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtp\n"
			+ "bnZhbGlkY2VydDAeFw0yMTA1MjUxNTE5NTVaFw0yMjA1MjUxNTE5NTVaMBYxFDAS\n"
			+ "BgNVBAMMC2ludmFsaWRjZXJ0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC\n"
			+ "AQEA1qZoKQL2N4xJSIDfjd7u5ZrAgxUSJCpPWgefDYNzMw/wrGfpg/VnuEpqTZKW\n"
			+ "PH/gcxC9yhmqvPiJdShpYT+IXppIw43SsjlvrkovFYDRxfelPvYrKJaDah4e+0AT\n"
			+ "QGaDSj21xlyrHiNUlRubF/y4Jc0yyfjRZReIDDY0u4L0Gyl8aL2yuwrZmHErk69Q\n"
			+ "+wNdrdctME3fX9nkpkOHjdUQMovMPc4Gut5aPdkZgR69ZPaxncG9/H5aa5zfEKrP\n"
			+ "lloIJgygJBLhdSIsBMNl3j+opaBEjsoO4Pwf3NoHRCVXZoRf3lkI8OXVMx3361g1\n"
			+ "c5HNjlCKDg+gZ415B2ygHpP5qQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBJ8XT0\n"
			+ "DdCtz2uIORd/mNipLWLHF2cUOvlEyi37st+wNjMPiiIlrKO8BMcqcMfGRBrKuEGq\n"
			+ "JvGlRYJnPe+QqHsJpiCndVnrG4bo9zuUXbNyiEKCM1ANYtnT5juPeiMgEZDdSXmm\n"
			+ "uFZFNdXKP+CCRiX9zD2wJYswiy8Qv6ECPp2j/oyJ2qDlVMIq1c64RNqWYM+fLLPc\n"
			+ "2h0IvjYhZcY7IRBf9Q4/xYmWT1CQ3IrwAaHuwgobrCzuldVnAkbLzFPO6iQy7r4u\n"
			+ "1dfJGFDWF7tM52CmgVn2lf6Zd9RMkGK5hBh58JK40Mw10xw27mq0eFbQsfeii4XW\n"
			+ "pFs0nDfU/s6ejL0u\n"
			+ "-----END CERTIFICATE-----";
		String invalidCertString = transfromToCertKeystring(invalidCert);

		X509Certificate certificate = getCertificate(invalidCertString);
		CovidCertSignatureVerificationService wronglyConfiguredCertVerService = new CovidCertSignatureVerificationService(certificate);
		boolean verify = wronglyConfiguredCertVerService.verify(testpayload, b64SigString);
		Assertions.assertFalse(verify, "Wrong cert is used hence verification should fail");

	}

	private PrivateKey getPrivateKey(String privateKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] encoded = Base64.getDecoder().decode(privateKeyString);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		return keyFactory.generatePrivate(keySpec);
	}
}


