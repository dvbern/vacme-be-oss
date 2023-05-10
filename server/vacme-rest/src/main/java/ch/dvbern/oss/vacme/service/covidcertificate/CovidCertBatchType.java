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

import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum CovidCertBatchType {

	ONLINE,
	POST,
	REVOCATION_ONLINE,
	REVOCATION_POST;

	@NonNull
	public static ApplicationPropertyKey mapBatchTypeToLockKey(@NonNull CovidCertBatchType type){
		switch (type) {
		case ONLINE:
			return ApplicationPropertyKey.VACME_COVIDAPI_ONLINE_PS_BATCHJOB_LOCK;
		case POST:
			return ApplicationPropertyKey.VACME_COVIDAPI_POST_PS_BATCHJOB_LOCK;
		case REVOCATION_ONLINE:
			return ApplicationPropertyKey.VACME_COVIDAPI_REVOCATION_ONLINE_PS_BATCHJOB_LOCK;
		case REVOCATION_POST:
			return ApplicationPropertyKey.VACME_COVIDAPI_REVOCATION_POST_PS_BATCHJOB_LOCK;
		}
		throw new AppFailureException(String.format("Could not map BatchType %s to lock mode", type));
	}

	@NonNull
	public static CovidCertBatchType mapCreationBatchTypeFromRegistrierungEingangsart(@NonNull RegistrierungsEingang eingang){
		switch (eingang) {
		case ONLINE_REGISTRATION:
			return ONLINE;
		case CALLCENTER_REGISTRATION:
		case ORT_DER_IMPFUNG:
		case MASSENUPLOAD:
		case NOTFALL_PROZESS:
		case DATA_MIGRATION:
			return POST;
		case UNBEKANNT:
			break;
		}
		throw new AppFailureException(String.format("Could not map RegistrierungsEingang %s to batch type", eingang));
	}

	@NonNull
	public static CovidCertBatchType mapRevocationBatchTypeFromRegistrierungEingangsart(@NonNull RegistrierungsEingang eingang){
		switch (eingang) {
		case ONLINE_REGISTRATION:
			return REVOCATION_ONLINE;
		case CALLCENTER_REGISTRATION:
		case ORT_DER_IMPFUNG:
		case MASSENUPLOAD:
		case NOTFALL_PROZESS:
		case DATA_MIGRATION:
			return REVOCATION_POST;
		case UNBEKANNT:
			break;
		}
		throw new AppFailureException(String.format("Could not map RegistrierungsEingang %s to batch type", eingang));
	}

	public boolean isPost() {
		return this == POST || this == REVOCATION_POST;
	}

	public boolean isOnline() {
		return !isPost();
	}
}
