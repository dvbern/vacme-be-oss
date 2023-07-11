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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class CovidCertKeyUtil {

	public static final String PKCS_12 = "PKCS12";

	private CovidCertKeyUtil() {
	}

	@NonNull
	public static PrivateKey loadPrivateKey(@NonNull String pathOfKey, @NonNull String password, @NonNull String keyAlias
	) throws UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, FileNotFoundException {
		try (
			InputStream keyStoreData = CovidCertKeyUtil.getInputStreamForKey(pathOfKey)) {
			KeyStore keyStore;
			keyStore = KeyStore.getInstance(PKCS_12);
			keyStore.load(keyStoreData, password.toCharArray());
			return (PrivateKey) keyStore.getKey(keyAlias, password.toCharArray());
		}
	}

	@NonNull
	public static X509Certificate loadCert(@NonNull String pathOfKey, @NonNull String password, @NonNull String keyAlias
	) throws UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, FileNotFoundException {
		try (
			InputStream keyStoreData = CovidCertKeyUtil.getInputStreamForKey(pathOfKey)) {
			KeyStore keyStore;
			keyStore = KeyStore.getInstance(PKCS_12);
			keyStore.load(keyStoreData, password.toCharArray());
			return (X509Certificate) keyStore.getCertificate(keyAlias);
		}
	}

	@Nullable
	public static InputStream getInputStreamForKey(@NonNull String pathToLoad) throws FileNotFoundException {
		if (pathToLoad.startsWith("classpath:")) {
			String actualPath = pathToLoad.replace("classpath:", "");
			return CovidCertSignatureService.class.getResourceAsStream(actualPath);
		}
		return new FileInputStream(pathToLoad);
	}
}
