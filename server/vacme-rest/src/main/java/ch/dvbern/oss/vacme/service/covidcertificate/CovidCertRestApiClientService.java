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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.covidcertificate.CovidCertificateCreateResponseDto;
import ch.dvbern.oss.vacme.entities.covidcertificate.RevocationDto;
import ch.dvbern.oss.vacme.entities.covidcertificate.VaccinationCertificateCreateDto;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.rest_client.RestClientLoggingFilter;
import ch.dvbern.oss.vacme.shared.errors.CovidCertApiFailureException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/covidcertificate")
@RegisterRestClient(configKey="covid-cert-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(CovidCertApiExceptionMapper.class)
@RegisterClientHeaders(CovidCertSignatureHeaderFactory.class)
public interface CovidCertRestApiClientService {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("vaccination")
	@RolesAllowed(BenutzerRolleName.SYSTEM_INTERNAL_ADMIN)
	@Operation(description = "Creates a vaccine certificate for the given data.")
	CovidCertificateCreateResponseDto createVaccinationCert(VaccinationCertificateCreateDto vaccinationCertificateCreateDto) throws CovidCertApiFailureException;


	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("revoke")
	@RolesAllowed(BenutzerRolleName.SYSTEM_INTERNAL_ADMIN)
	@Operation(description = "Revokes a Covid certificate with a given UVCI.")
	Response revokeVaccinationCert(RevocationDto revocationDto) throws CovidCertApiFailureException;

}
