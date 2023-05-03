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
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.service.covidcertificate.CovidCertUtils.normalizeJson;

@Dependent
public class CovidCertSignatureService {

	private PrivateKey privateKey;

	@ConfigProperty(name = "covid-cert-api/mp-rest/keyStore")
	String privateKeyPath;

	@ConfigProperty(name = "covid-cert-api/mp-rest/keyStorePassword")
	String password;

	@ConfigProperty(name = "vacme.covidapi.ps.key.alias", defaultValue = "1")
	String keyAlias;

	public CovidCertSignatureService() {
		// needed for cdi
	}

	public CovidCertSignatureService(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	@PostConstruct
	void init() {
		initKeys();
	}

	private void initKeys() {
		if (privateKey == null) {
			try {
				privateKey = CovidCertKeyUtil.loadPrivateKey(privateKeyPath, password, keyAlias);
			}  catch (UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
				throw new AppFailureException("Could not initialize keys for signing", e);
			}
		}
	}

	@NonNull
	public String sign(@NonNull String payload) throws NoSuchAlgorithmException, InvalidKeyException,
		SignatureException {

		// canonicalize
		String normalizedJson = normalizeJson(payload);
		byte[] bytes = normalizedJson.getBytes(StandardCharsets.UTF_8);
		// sign
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(this.privateKey);
		signature.update(bytes);
		String signatureString = Base64.getEncoder().encodeToString(signature.sign());
		return signatureString;
	}
}
