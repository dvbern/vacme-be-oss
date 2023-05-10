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

package ch.dvbern.oss.vacme.resource;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.onboarding.OnboardingBatchType;
import ch.dvbern.oss.vacme.service.onboarding.OnboardingService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_BENUTZER_VERWALTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_MEDIZINISCHE_NACHDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_NACHDOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_DOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_ONBOARDING))
@Path(VACME_WEB + "/onboarding/")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OnboardingResource {
	private final OnboardingService onboardingService;
	private final RegistrierungService registrierungService;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("triggerOnboardingLetter/{regNummer}")
	@RolesAllowed({ OI_KONTROLLE, OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, AS_BENUTZER_VERWALTER,
		KT_NACHDOKUMENTATION, KT_MEDIZINISCHE_NACHDOKUMENTATION
	})
	public Response triggerOnboardingLetter(
		@NonNull @NotNull @PathParam("regNummer") String registrierungsNummer
	) {
		Registrierung registrierung = this.registrierungService.findRegistrierung(registrierungsNummer);

		if (Boolean.TRUE.equals(registrierung.getVerstorben())) {
			LOG.error(
				"VACME-ONBOARDING: Versand abgebrochen, Registrierung ist als verstorben markiert {}",
				registrierung.getRegistrierungsnummer());
			throw AppValidationMessage.REGISTRIERUNG_VERSTORBEN.create(registrierung.getRegistrierungsnummer());
		}

		// Brief ausloesen
		onboardingService.triggerLetterGeneration(registrierungsNummer, OnboardingBatchType.POST);
		LOG.info("VACME-ONBOARDING: Manually triggered letter for {}", registrierungsNummer);

		return Response.ok().build();
	}

}
