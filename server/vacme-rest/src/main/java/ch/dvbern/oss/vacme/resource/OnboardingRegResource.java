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

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.onboarding.Onboarding;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.jax.registration.StartOnboardingJax;
import ch.dvbern.oss.vacme.rest.auth.BenutzerSyncFilter;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.service.onboarding.OnboardingBatchType;
import ch.dvbern.oss.vacme.service.onboarding.OnboardingService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.CC_AGENT;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.IMPFWILLIGER;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_INITIALREG;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_ONBOARDING))
@Path(VACME_INITIALREG + "/onboarding/")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OnboardingRegResource {

	public static final String ONBOARDING_TOKEN_COOKIE_NAME = "onboardingToken";
	private final OnboardingService onboardingService;
	private final UserPrincipal userPrincipal;
	private final BenutzerSyncFilter benutzerSyncFilter;
	private final RegistrierungService registrierungService;
	private final VacmeSettingsService vacmeSettingsService;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("startOnboarding")
	@PermitAll // before the person has created the keycloak login
	public Response startOnboardingBeforeKeycloakRegistration(StartOnboardingJax startOnboardingJax) {

		if (!onboardingService.isValidOnboardingCode(startOnboardingJax.getOnboardingcode())) {
			throw AppValidationMessage.ONBOARDING_INVALID_CHECKSUM.create(startOnboardingJax.getOnboardingcode());
		}

		// Man sollte eigentlich nicht eingeloggt sein. Aber es kann auch sein, dass man eingeloggt ist, weil man ein Timeout hatte und
		// es nochmal probieren will!

		// Man hat noch keinen User, deshalb muessen wir das hier als Systemadmin ausfuehren
		this.benutzerSyncFilter.switchToAdmin();
		// validate and count bad tries
		Onboarding validatedOnboarding = onboardingService.startOnboardingValidateAndIncreaseNumOfTries(
			startOnboardingJax.getOnboardingcode(),
			startOnboardingJax.getGeburtsdatum());

		// Onboarding starten
		String token = onboardingService.startOnboarding(validatedOnboarding, startOnboardingJax.getLanguage());

		// Readable Cookie for XSRF Protection (the Cookie can only be read from our Domain)
		int cookieTimeoutSeconds = vacmeSettingsService.getOnboardingTokenTTLMinutes() * 60;
		NewCookie onboardingTokenCookie = new NewCookie(ONBOARDING_TOKEN_COOKIE_NAME, token,
			"/", null, null, cookieTimeoutSeconds, vacmeSettingsService.isCookieSecure(), true);

		ResponseBuilder builder = Response.ok();
		return builder.cookie(onboardingTokenCookie).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("finishOnboarding")
	@RolesAllowed({ IMPFWILLIGER }) // after the person has registered in keycloak
	public Response finishOnboardingAfterKeycloakRegistration(
		@CookieParam(ONBOARDING_TOKEN_COOKIE_NAME) String passedToken
	) {

		if (passedToken == null) {
			LOG.info("VACME-ONBOARDING-COMPLETION Cookie is missing");
			throw AppValidationMessage.ONBOARDING_FINISH_FAILED.create("no token cookie");
		}

		// Der Benutzer hat sich jetzt registriert und will sich mit der Registrierung verknuepfen
		Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();
		if (userPrincipal.userHasRoleOtherThanImpfwilliger(currentBenutzer.getRoles())) {
			LOG.error("VACME-ONBOARDING-COMPLETION Benutzer hat nicht nur Impfwilliger Rolle. Dies ist nicht "
				+ "erwartet");
			throw AppValidationMessage.ONBOARDING_FINISH_FAILED.create("wrong role");
		}

		onboardingService.finishOnboardingAfterKeycloakRegistration(passedToken, currentBenutzer);
		// 'delete' cookie
		NewCookie onboardingTokenCookie = new NewCookie(ONBOARDING_TOKEN_COOKIE_NAME, null,
			"/", null, null, 1, vacmeSettingsService.isCookieSecure(), true);

		return Response.ok().cookie(onboardingTokenCookie).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("triggerOnboardingLetter/{regNummer}")
	@RolesAllowed({ CC_AGENT })
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
		onboardingService.triggerLetterGeneration(Registrierung.toId(registrierung.getId()), OnboardingBatchType.POST);
		LOG.info("VACME-ONBOARDING: Manually triggered letter for {}", registrierungsNummer);

		return Response.ok().build();
	}

}
