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

package ch.dvbern.oss.vacme.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.repo.BenutzerRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.service.benutzer.UpdateBenutzerDTO;
import ch.dvbern.oss.vacme.service.wellapi.WellApiInitalDataSenderService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.KeyCloakUtil;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.keycloak.admin.client.resource.UserResource;
import org.slf4j.MDC;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolle.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolle.OI_ORT_VERWALTER;

@Slf4j
@Transactional
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class EnsureBenutzerService {

	private final JsonWebToken jsonWebToken;
	private final SecurityIdentity securityIdentity;
	private final BenutzerService benutzerService;
	private final BenutzerRepo benutzerRepo;
	private final OrtDerImpfungService ortDerImpfungService;
	private final KeyCloakService keyCloakService;
	private final VacmeSettingsService vacmeSettingsService;
	private final RegistrierungRepo registrierungRepo;
	private final WellApiInitalDataSenderService wellApiInitalDataSenderService;

	public boolean ensureBenutzerActive() {
		UUID uuid = UUID.fromString(jsonWebToken.getSubject());

		Optional<Benutzer> benutzer = benutzerRepo.getById(Benutzer.toId(uuid));

		// Benutzer does not exist on DB, hence check if 30 day disabled rule applies
		if (benutzer.isEmpty()) {

			Set<BenutzerRolle> benutzerRolles = KeyCloakUtil.mapRoles(securityIdentity.getRoles());
			if (benutzerRolles.isEmpty()) {
				// (Fachapp-)Benutzer hat keine Vacme Rolle -> koennte ein MIK Benutzer sein...
				// -> wird wie deaktiviert behandelt
				return false;
			}
			Set<BenutzerRolle> alwaysActiveRolles =
				new HashSet<>(Arrays.asList(OI_IMPFVERANTWORTUNG, OI_ORT_VERWALTER));

			if (benutzerRolles.stream().anyMatch(alwaysActiveRolles::contains)) {
				return true;
			}

			UserResource userResource = keyCloakService.getUser(uuid.toString());
			long creationTimestamp = userResource.toRepresentation().getCreatedTimestamp();
			LocalDateTime creationDateTime =
				LocalDateTime.ofInstant(Instant.ofEpochMilli(creationTimestamp), ZoneId.systemDefault());

			if (DateUtil.getMinutesBetween(creationDateTime, LocalDateTime.now())
				> vacmeSettingsService.getBenutzerDeactivateAfterInactiveTimeMinutes()) {
				keyCloakService.removeVacmeRolesFromUser(uuid.toString());
				return false;
			}
		}
		return true;
	}

	public void ensureBenutzer() {

		String preferred_username = jsonWebToken.getClaim("preferred_username");
		String familyName = jsonWebToken.getClaim("family_name");
		String givenName = jsonWebToken.getClaim("given_name");
		String email = jsonWebToken.getClaim("email");
		String issuer = jsonWebToken.getIssuer();
		UUID uuid = UUID.fromString(jsonWebToken.getSubject());// keycloak sends us its uuid as a string

		JsonArray oidIdentifiers =
			jsonWebToken.getClaim(Constants.MEMBER_OF_CLAIM); // string array mit ort der Impfung identifiers
		Optional<String> mobileNumberClaim =
			jsonWebToken.claim(Constants.MOBILE_NUMMER_CLAIM);// string mit der mobilen Nummer

		Optional<String> wellIdClaim = jsonWebToken.claim(Constants.WELL_ID_CLAIM);// string mit der WellID
		String wellId = wellIdClaim.orElse(null);
		Set<BenutzerRolle> benutzerRolles = KeyCloakUtil.mapRoles(securityIdentity.getRoles());
		String mobileNum = mobileNumberClaim.orElse("");
		Optional<String> glnClaim = jsonWebToken.claim(Constants.GLN_CLAIM);// string mit der gln wenn vorhanden
		String glnNum = glnClaim.orElse(null);

		List<OrtDerImpfung> assignedOrteDerImpung = new ArrayList<>();
		if (oidIdentifiers != null) {

			for (JsonValue oidIdentifierJsonVal : oidIdentifiers) {
				String oidIdentifier = "";
				if (ValueType.STRING == oidIdentifierJsonVal.getValueType()) {
					oidIdentifier = ((JsonString) oidIdentifierJsonVal).getString();

				}

				try {
					OrtDerImpfung odi = ortDerImpfungService.getByOdiIdentifier(oidIdentifier);
					assignedOrteDerImpung.add(odi);
				} catch (AppValidationException ex) {
					LOG.warn(
						"Could not load ort der Impfung with identifier {} assigned to user {} ",
						oidIdentifier,
						preferred_username);
				}
			}
		} else {
			LOG.warn("Configuration error: OIDC Client has no memberof mapper configured");
		}

		// Die Daten auf dem Benutzer in Vacme anpassen
		UpdateBenutzerDTO updateBenutzerDTO =
			new UpdateBenutzerDTO(preferred_username, familyName, givenName, email, mobileNum, glnNum, issuer, wellId);
		ID<Benutzer> benutzerId = Benutzer.toId(uuid);
		Benutzer benutzer = benutzerService.save(benutzerId, updateBenutzerDTO, benutzerRolles, assignedOrteDerImpung);

		// Die Mobilenummer auch auf der Registrierung in Vacme anpassen
		if (benutzer.getRoles().contains(BenutzerRolle.IMPFWILLIGER)) {
			updateMobileNumberOnRegistrierung(uuid, mobileNumberClaim);
		}

		// Falls es sich um einen Well-Benutzer handelt, dies an Well melden
		if (StringUtils.isNotEmpty(wellId)) {
			wellApiInitalDataSenderService.sendAllAvailableDataToWell(wellId, benutzer);
		}

		storeLastLoginInfoForBenutzer(benutzer);
	}

	private void updateMobileNumberOnRegistrierung(@NonNull UUID uuid, @NonNull Optional<String> mobileNumberClaim) {
		final Optional<Registrierung> registrierungOptional = registrierungRepo.getByUserId(uuid);
		if (registrierungOptional.isPresent() && mobileNumberClaim.isPresent()) {
			final Registrierung registrierung = registrierungOptional.get();
			if (!mobileNumberClaim.get().equalsIgnoreCase(registrierung.getTelefon())) {
				registrierung.setTelefon(mobileNumberClaim.get());
				registrierungRepo.update(registrierung);
			}
		}
	}

	private void storeLastLoginInfoForBenutzer(@NonNull Benutzer benutzer) {
		String session = getSessionIdClaim();
		if (session != null) {
			boolean changed = false;
			if (benutzer.getLastUsedSessionId() == null || !benutzer.getLastUsedSessionId().equals(session)) {
				benutzer.setLastUsedSessionId(session);
				benutzer.setTimestampLastSessionId(LocalDateTime.now());
				benutzer.setDeaktiviert(false);
				changed = true;
			} else {
				LOG.debug("Session {} for user {} was already stored", benutzer.getId(), session);
			}

			String forwaredForHeaderIps = extractForwaredForHeaderIps();
			if (benutzer.getLastSeenIp() == null
				|| (forwaredForHeaderIps != null && !benutzer.getLastSeenIp().equals(forwaredForHeaderIps))) {
				benutzer.setLastSeenIp(forwaredForHeaderIps);
				changed = true;
			}
			if (changed) {
				benutzerRepo.merge(benutzer);
			}
		} else {
			LOG.debug("Session was null");
		}
	}

	@Nullable
	private String getSessionIdClaim() {
		return jsonWebToken.getClaim(Constants.SESSION_ID_CLAIM);
	}

	@Nullable
	private String extractForwaredForHeaderIps() {
		return MDC.get(Constants.LOG_MDC_FORWARDED_FOR);
	}
}
