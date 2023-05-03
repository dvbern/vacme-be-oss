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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.benutzer.BenutzerBerechtigung;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueueResult;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.jax.registration.OdiUserJax;
import ch.dvbern.oss.vacme.repo.BenutzerRepo;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.service.benutzer.UpdateBenutzerDTO;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.KeyCloakUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolle.IMPFWILLIGER;

@Slf4j
@RequestScoped
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BenutzerService {

	@ConfigProperty(name = "vacme.keycloak.config.web")
	String keyCloakConfigWeb;

	private final BenutzerRepo repo;
	private final KeyCloakService keyCloakService;
	private final OrtDerImpfungRepo odiRepo;
	private final UserPrincipal userPrincipal;

	@Nullable
	private String readIssuer() {
		String basepath = "/keycloak-configs/web/";
		String keycloakPath = basepath + keyCloakConfigWeb;

		try (InputStream inputStream = BenutzerService.class.getResourceAsStream(keycloakPath)) {
			JSONParser jsonParser = new JSONParser();
			Objects.requireNonNull(inputStream);
			InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

			Object obj = jsonParser.parse(reader);
			JSONObject jsonObject = (JSONObject) obj;

			String serverUrl = (String) jsonObject.get("auth-server-url");
			return serverUrl + "realms/vacme-web";

		} catch (IOException | ParseException e) {
			LOG.error("Could not determine Issuer", e);
		}
		return null;
	}

	@NonNull
	public Benutzer create(@NonNull @NotNull OdiUserJax userJax) {
		Objects.requireNonNull(userJax.getId(), "Die User-ID aus Keycloak muss jetzt gesetzt sein");
		ID<Benutzer> benutzerId = new ID<>(UUID.fromString(userJax.getId()), Benutzer.class);
		String issuer = readIssuer();
		UpdateBenutzerDTO updateBenutzerDTO = new UpdateBenutzerDTO(
			userJax.getUsername(),
			userJax.getLastName(),
			userJax.getFirstName(),
			userJax.getEmail(),
			userJax.getPhone(),
			userJax.getGlnNummer(),
			issuer,
			null);

		// Der Benutzer ist neu: Er hat vorerst 1 Rolle und 0 Odis.
		// Weitere Rollen/Odis muessen ueber save() hinzugefuegt werden
		Set<String> roles = new HashSet<>();
		roles.add(userJax.getFachRolle().getKeyCloakRoleName());
		Set<BenutzerRolle> benutzerRolles = KeyCloakUtil.mapRoles(roles);

		return save(benutzerId, updateBenutzerDTO, benutzerRolles, new ArrayList<>());
	}

	public void addOdiToBenutzer(
		@NonNull Benutzer benutzer,
		@NonNull List<String> rolesOfFachverantwortlicher,
		@NonNull OrtDerImpfung ortDerImpfung
	) {
		// Berechtigt fuer alle bestehenden plus den neuen ODI
		List<OrtDerImpfung> assignedOdi = new ArrayList<>(benutzer.getOrtDerImpfung());
		assignedOdi.add(ortDerImpfung);
		mergeOrtDerImpfung(benutzer, assignedOdi);

		// Rollen
		Set<BenutzerRolle> benutzerRolles = KeyCloakUtil.mapRoles(new HashSet<>(rolesOfFachverantwortlicher));
		mergeRollen(benutzer, benutzerRolles);

		repo.merge(benutzer);
	}

	public void removeOdiFromBenutzerIfExists(
		@NonNull ID<Benutzer> benutzerId,
		@NonNull String groupName
	) {
		final Optional<Benutzer> benutzerOptional = repo.getById(benutzerId);
		if (benutzerOptional.isPresent()) {
			Benutzer benutzer = benutzerOptional.get();
			final OrtDerImpfung ortDerImpfung =
				odiRepo.getByOdiIdentifier(groupName)
					.orElseThrow(() -> AppValidationMessage.UNKNOWN_ODI.create(groupName));

			// Berechtigt fuer alle bestehenden minus den uebergebenen ODI
			List<OrtDerImpfung> assignedOdi = new ArrayList<>(benutzer.getOrtDerImpfung());
			assignedOdi.remove(ortDerImpfung);
			mergeOrtDerImpfung(benutzer, assignedOdi);
			repo.merge(benutzer);
		}
	}

	@NonNull
	public Benutzer save(
		@NonNull ID<Benutzer> benutzerId,
		@NonNull UpdateBenutzerDTO changes,
		@NonNull Set<BenutzerRolle> assignedRoles,
		@NonNull List<OrtDerImpfung> assignedOdi
	) {
		Optional<Benutzer> benOpt = repo.getById(benutzerId);

		if (benOpt.isPresent()) {
			Benutzer dbBenutzer = benOpt.get();
			changes.applyTo(dbBenutzer);
			mergeOrtDerImpfung(dbBenutzer, assignedOdi);
			mergeRollen(dbBenutzer, assignedRoles);

			return dbBenutzer;
		} else {
			var benutzer = new Benutzer(benutzerId.getId());
			changes.applyTo(benutzer);
			repo.create(benutzer);
			mergeOrtDerImpfung(benutzer, assignedOdi);
			mergeRollen(benutzer, assignedRoles);
			return benutzer;
		}
	}

	private void mergeOrtDerImpfung(@NonNull Benutzer benutzer, @NonNull List<OrtDerImpfung> assignedOdi) {

		HashSet<OrtDerImpfung> removedOdis = new HashSet<>(benutzer.getOrtDerImpfung());
		removedOdis.removeAll(assignedOdi); // existierende minus aktuell gewollte  = removed
		HashSet<OrtDerImpfung> addedOdis = new HashSet<>(assignedOdi);
		addedOdis.removeAll(benutzer.getOrtDerImpfung());

		benutzer.getOrtDerImpfung().removeAll(removedOdis);
		benutzer.getOrtDerImpfung().addAll(addedOdis);
	}

	private void mergeRollen(@NonNull Benutzer benutzer, @NonNull Set<BenutzerRolle> assignedRollen) {

		Set<BenutzerRolle> existingRollen = benutzer
			.getBerechtigungen().stream()
			.map(BenutzerBerechtigung::getRolle)
			.collect(Collectors.toSet());

		HashSet<BenutzerRolle> removedRoles = new HashSet<>(existingRollen);
		removedRoles.removeAll(assignedRollen); // existierende minus aktuell gewollte  = removed
		HashSet<BenutzerRolle> newRoles = new HashSet<>(assignedRollen);
		newRoles.removeAll(existingRollen);

		deleteRemovedRoles(benutzer, removedRoles);
		addMissingRoles(benutzer, newRoles);
	}

	private void deleteRemovedRoles(@NonNull Benutzer benutzer, @NonNull Set<BenutzerRolle> toRemove) {
		for (BenutzerRolle roleToRemove : toRemove) {
			LOG.debug("Removing Berechtigung from Benutzer: {}, {}", benutzer.getBenutzername(), roleToRemove);
			benutzer.berechtigungenHelper()
				.deleteChildren(child -> child.isSameBerechtigung(roleToRemove));
		}
	}

	private void addMissingRoles(@NonNull Benutzer benutzer, @NonNull Set<BenutzerRolle> newlyActiveRoles) {
		for (BenutzerRolle newlyActiveRole : newlyActiveRoles) {
			LOG.debug("Adding Berechtigung for Benutzer: {}, {}", benutzer.getBenutzername(), newlyActiveRole);
			BenutzerBerechtigung newBerechtigung = benutzer.berechtigungenHelper().createChild();
			newBerechtigung.setRolle(newlyActiveRole);
			benutzer.getBerechtigungen().add(newBerechtigung);
		}
	}

	public void delete(@NonNull ID<Benutzer> benutzerId) {
		repo.delete(benutzerId);
	}

	@NonNull
	public Optional<Benutzer> getById(@NonNull ID<Benutzer> id) {
		return repo.getById(id);
	}

	@NonNull
	public Set<OrtDerImpfung> getOdisOfBenutzer(@NonNull ID<Benutzer> id) {
		return repo.getOdisOfBenutzer(id);
	}

	@NonNull
	public List<Benutzer> getBenutzerByRolleAndOrtDerImpfung(
		@NonNull OrtDerImpfung ordDerImpfung,
		@NonNull BenutzerRolle rolle
	) {
		return repo.findByRolleAndOrtDerImpfung(ordDerImpfung, rolle);
	}

	@NonNull
	public List<Benutzer> getByGLNAndRolle(@NonNull String glnNummer, @NonNull BenutzerRolle rolle) {
		return repo.getByGLNAndRolle(glnNummer, rolle);
	}

	/**
	 * Gibt den Benutzer mit dem ensprechenden Benutzernamen aus dem vacme Realm (Portal) zurueck
	 *
	 * @param benutzername benutzername in vacme portal der gesucht wird
	 * @return gefundener Benutzer oder Fehler
	 */
	public Benutzer getByBenutzernameFromRegapp(@NonNull String benutzername) {
		return repo.getByBenutzernameFromRegapp(benutzername)
			.orElseThrow(() -> AppFailureException.entityNotFound(Benutzer.class, benutzername));

	}

	@NonNull
	public List<ID<Benutzer>> getVacmeWebBenutzerToDeactivate(
		int minutesInactive,
		@Nullable String issuer,
		@Nullable Integer limit) {
		LocalDateTime cutoffDateTimeForOldSessions = LocalDateTime.now().minusMinutes(minutesInactive);
		LocalDateTime installationDate = Constants.EARLIEST_COLLECTED_LOGIN_TIMESTAMP;
		if (cutoffDateTimeForOldSessions.isBefore(installationDate)) {
			LOG.warn(
				"VACME-DEACTIVATE-USERS: Vacme has no information about login times prior to the {} and thus can not "
					+ "accurately report which users have not logged in since {}. Returning empty List"
				,
				installationDate,
				cutoffDateTimeForOldSessions);
			return Collections.emptyList();
		}
		return repo.getAllActiveSubordinateBenutzerWhereLastSessionAndLastUnlockedIsOlderThanMinutesOrNull(
			minutesInactive,
			issuer,
			limit);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean deactivateVacmeWebUserInKeycloakAndVacme(@NonNull ID<Benutzer> benutzerID) {
		try {
			Benutzer currentBenutzer = getById(benutzerID)
				.orElseThrow(() -> AppFailureException.entityNotFound(
					DocumentQueueResult.class,
					benutzerID.toString()));
			Validate.notNull(currentBenutzer, "Benutzer to deactivate must be set");
			Validate.isTrue(
				!currentBenutzer.getRoles().contains(BenutzerRolle.IMPFWILLIGER),
				"Benutzer to deactivate may not have Role Impfwilliger");
			Validate.isTrue(
				currentBenutzer.getIssuer().equals(readIssuer()),
				"Benutzer to deactivate must be in vacme-web realm. Expected '" + readIssuer() + "' but was '"
					+ currentBenutzer.getIssuer() + '\'');
			keyCloakService.disableUser(currentBenutzer.getId().toString());
			currentBenutzer.setDeaktiviert(true);
			repo.merge(currentBenutzer);

			LOG.info(
				"VACME-DEACTIVATE-USERS: Deactivated Benutzer: '{}', lastLogin: '{}', roles: '{}', odis: '{}'",
				currentBenutzer.getId(),
				currentBenutzer.getTimestampLastSessionId(),
				currentBenutzer.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")),
				currentBenutzer.getOrtDerImpfung()
					.stream()
					.map(OrtDerImpfung::getName)
					.collect(Collectors.joining(",")));
			return true;
		} catch (Exception exception) {
			LOG.error("VACME-DEACTIVATE-USERS: Error during deactivation of Benutzer for {} ", benutzerID, exception);
			return false;
		}
	}

	@NonNull
	public Benutzer getBenutzerOfOnlineRegistrierung(@NonNull Registrierung registrierung) {
		// check if it's a user or call center agent
		if (userPrincipal.isCallerInRole(IMPFWILLIGER)) {
			// therefore we know that the current user matches the user tied to the registration
			return userPrincipal.getBenutzerOrThrowException();
		}
		// otherwise we retrieve the user referenced in the registration
		return retrieveBenutzerOwningReg(registrierung);
	}

	private Benutzer retrieveBenutzerOwningReg(@NonNull Registrierung registrierung) {
		UUID benutzerId = registrierung.getBenutzerId();
		Objects.requireNonNull(benutzerId, "Bei OnlineRegistrierungen ist die BenutzerId zwingend");
		return this.repo.getById(Benutzer.toId(benutzerId))
			.orElseThrow(() -> AppFailureException.entityNotFound(Benutzer.class, benutzerId));
	}
}
