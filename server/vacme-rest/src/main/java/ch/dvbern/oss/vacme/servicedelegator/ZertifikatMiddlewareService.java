/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.servicedelegator;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.ZertifikatService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.util.ZertifikatDownloadUtil.zertifikatBlobToDownloadResponse;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ZertifikatMiddlewareService {

	private final Authorizer authorizer;
	private final ZertifikatService zertifikatService;
	private final ImpfdossierService impfdossierService;

	@NonNull
	public Response downloadZertifikat(
		@NonNull @NotNull @PathParam("registrierungsnummer") String registrierungsnummer,
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheitIdentifier
	) {
		final Optional<Impfdossier> impfdossierOptional =
			impfdossierService.findImpfdossierForRegnumAndKrankheitOptional(registrierungsnummer, krankheitIdentifier);
		if (impfdossierOptional.isPresent()) {
			final Impfdossier impfdossier = impfdossierOptional.get();

			authorizer.checkReadAuthorization(impfdossier);
			Optional<Zertifikat> zertifikatOpt = zertifikatService.getBestMatchingZertifikat(impfdossier);
			if (zertifikatOpt.isPresent()) {
				return zertifikatBlobToDownloadResponse(zertifikatService.getZertifikatPdf(zertifikatOpt.get()));
			}
		}
		throw AppValidationMessage.NO_ZERTIFIKAT_PDF.create(registrierungsnummer);
	}
}
