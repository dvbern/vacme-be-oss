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

package ch.dvbern.oss.vacme.shared.errors;

import ch.dvbern.oss.vacme.entities.covidcertificate.CovidCertErrorcode;
import ch.dvbern.oss.vacme.entities.covidcertificate.RestError;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Exception that is used when there are problems creating a certificate
 */
public class CovidCertApiFailureException extends RuntimeException  {

	private static final long serialVersionUID = -1104206699839600550L;

	private RestError errorFromApi;

	@Nullable
	private CovidCertErrorcode covidCertErrorcode;

	public CovidCertApiFailureException(String message) {
		super(message, null);
	}

	public CovidCertApiFailureException(String message, @Nullable Throwable cause) {
		super(message, cause);
	}

	public CovidCertApiFailureException(@NonNull RestError restError, @Nullable CovidCertErrorcode covidCertErrorcode) {
		super(restError.getErrorMessage(), null);
		errorFromApi = restError;

		this.covidCertErrorcode = covidCertErrorcode;
	}

	public RestError getErrorFromApi() {
		return errorFromApi;
	}

	@Nullable
	public CovidCertErrorcode getCovidCertErrorcode() {
		return covidCertErrorcode;
	}
}
