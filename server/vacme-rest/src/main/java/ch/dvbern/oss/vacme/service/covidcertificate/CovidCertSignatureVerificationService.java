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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.service.covidcertificate.CovidCertUtils.normalizeJson;

/**
 * This class is only used for testing the our Signature Process
 */
@Dependent
public class CovidCertSignatureVerificationService {

	private X509Certificate certificate;

	@ConfigProperty(name = "covid-cert-api/mp-rest/keyStore")
	String privateKeyPath;

	@ConfigProperty(name = "covid-cert-api/mp-rest/keyStorePassword")
	String password;

	@ConfigProperty(name = "vacme.covidapi.ps.key.alias", defaultValue = "1")
	String keyAlias;

	public CovidCertSignatureVerificationService() {
		// needed for cdi
	}

	public CovidCertSignatureVerificationService(X509Certificate certificate) {
		this.certificate = certificate;
	}

	@PostConstruct
	void init() {
		initCert();
	}

	private void initCert() {
		if (certificate == null) {
			try {
				certificate = CovidCertKeyUtil.loadCert(privateKeyPath, password, keyAlias);
			} catch (UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
				throw new AppFailureException("Could not initialize keys for signing", e);
			}
		}
	}

	public boolean verify(@NonNull String signedPayload, @NonNull String b64SignatureString) throws NoSuchAlgorithmException, InvalidKeyException,
		SignatureException {
		String normalizedPayload = normalizeJson(signedPayload);
		byte[] sigStringBytes = normalizedPayload.getBytes(StandardCharsets.UTF_8);
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initVerify(this.certificate);
		signature.update(sigStringBytes);
		byte[] signatureBytes = Base64.getDecoder().decode(b64SignatureString);
		boolean verified = signature.verify(signatureBytes);
		return verified;
	}
}
