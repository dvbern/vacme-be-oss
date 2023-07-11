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

package ch.dvbern.oss.vacme.rest.auth;

import java.security.AccessControlException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.AbstractEntity;
import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueue;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.types.FachRolle;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.registration.OdiUserJax;
import ch.dvbern.oss.vacme.service.KeyCloakService;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.keycloak.representations.idm.UserRepresentation;

@RequestScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class Authorizer {

	private final UserPrincipal userPrincipal;
	private final VacmeSettingsService vacmeSettingsService;

	public void checkReadAuthorization(@Nullable Registrierung registierung) {
		if (vacmeSettingsService.isAuthorizationDisabled()) {
			return;
		}
		if (registierung != null) {
			// Alle anderen Rollen sind nicht datenabhaengig, d.h. koennen ueber die Annotationen geregelt werden
			if (userPrincipal.isCallerInRole(BenutzerRolle.IMPFWILLIGER)) {
				if (registierung.getBenutzerId() == null
					|| !registierung.getBenutzerId().equals(userPrincipal.getBenutzerOrThrowException().getId())) {
					throwViolation(registierung);
				}
			}
			// CC_AGENT may read all registrations. If a Dossier is present prefer checking the dossier authorization
		}
	}

	public void checkUpdateAuthorization(@Nullable Registrierung registierung) {
		if (vacmeSettingsService.isAuthorizationDisabled()) {
			return;
		}
		if (registierung != null) {
			checkReadAuthorization(registierung);
		}
	}

	public void checkReadAuthorization(@Nullable Impfdossier impfdossier) {
		if (vacmeSettingsService.isAuthorizationDisabled()) {
			return;
		}
		if (impfdossier != null) {
			// Alle anderen Rollen sind nicht datenabhaengig, d.h. koennen ueber die Annotationen geregelt werden
			if (userPrincipal.isCallerInRole(BenutzerRolle.IMPFWILLIGER)) {
				checkReadAuthorization(impfdossier.getRegistrierung());
			} else if (userPrincipal.isCallerInRole(BenutzerRolle.CC_AGENT)) {
				// CC_AGENT may only read dossier if it is for a krankheit that supports Callcenters
				if (!impfdossier.getKrankheitIdentifier().isSupportsCallcenter()) {
					throwViolation(impfdossier);
				}
			}
		}
	}

	public void checkUpdateAuthorization(@Nullable Impfdossier impfdossier) {
		if (vacmeSettingsService.isAuthorizationDisabled()) {
			return;
		}
		if (impfdossier != null) {
			checkReadAuthorization(impfdossier);
		}
	}

	public void checkReadAuthorization(@Nullable OrtDerImpfung odi) {
		if (vacmeSettingsService.isAuthorizationDisabled()) {
			return;
		}

		if (userPrincipal.isCallerInRole(BenutzerRolle.AS_REGISTRATION_OI)) {
			// Benutzer mit Rolle AS_REGISTRATION_OI duerfen alle
			return;
		}

		// Hier sind die Details eines ODI gemeint, also nicht OrtDerImpfungDisplayNameJax
		// Alle anderen Personen, die Lesen duerfen, sind nicht eingeschraenkt auf bestimmte ODIs.
		if (userPrincipal.isCallerInAnyOfRole(BenutzerRolle.getOrtDerImpfungRoles())) {
			checkUpdateAuthorization(odi);
		}
	}

	public void checkUpdateAuthorization(@Nullable OrtDerImpfung odi) {
		if (vacmeSettingsService.isAuthorizationDisabled()) {
			return;
		}

		if (userPrincipal.isCallerInRole(BenutzerRolle.AS_REGISTRATION_OI)) {
			// Benutzer mit Rolle AS_REGISTRATION_OI duerfen alle verwalten
			return;
		}

		if (odi != null) {
			// Schreibzugriff haben sowieso nur diejenigen Rollen, welche zu einem ODI gehoeren. Die Rolle muss also
			// hier nicht mehr ueberprueft werden
			final Benutzer benutzer = userPrincipal.getBenutzerOrThrowException();
			if (benutzer.getOrtDerImpfung().stream().noneMatch(odi::equals)) {
				throwViolation(odi);
			}
		}
	}

	private void throwViolation(AbstractEntity<?> abstractEntity) {
		if (vacmeSettingsService.isAuthorizationDisabled()) {
			return;
		}
		throw new AccessControlException(
			"Access Violation"
				+ " for Entity: " + abstractEntity.getClass().getSimpleName() + "(id=" + abstractEntity.getId() + "):"
				+ " for current user: " + userPrincipal.getBenutzerOrThrowException().getBenutzername()
				+ ", insertUser: " + abstractEntity.getUserErstellt()
		);
	}

	public void checkAllowedToGiveRole(@NonNull @NotNull @Valid OdiUserJax userJax, KeyCloakService keyCloakService) {
		// Die beiden Rollen ORGANISATIONSVERANTWORTUNG und FACHVERANTWORTUNG_BAB werden beim
		// Erstellen eines ODI bereits definiert. Es koennen keine weiteren Benutzer mit dieser Rolle
		// erfasst werden.
		if (userJax.getFachRolle() == FachRolle.ORGANISATIONSVERANTWORTUNG ||
			userJax.getFachRolle() == FachRolle.FACHVERANTWORTUNG_BAB) {
			throw new AccessControlException(
				"Access Violation"
					+ " Attempted to create User with a restricted role " + userJax.getFachRolle());
		}

		// Die Rolle FACHVERANTWORTUNG_BAB_DELEGIERT darf nur neu vergeben werden von einem Benutzer mit der Rolle OI_FACHBAB_DELEGIEREN
		if (userJax.getFachRolle() == FachRolle.FACHVERANTWORTUNG_BAB_DELEGIERT) {
			var has_oi_fach_bab_delegieren = userPrincipal.isCallerInRole(BenutzerRolle.OI_FACHBAB_DELEGIEREN);
			if (!has_oi_fach_bab_delegieren) {
				final Optional<UserRepresentation> existungUserOpt =
					keyCloakService.findUserByUsername(userJax.getUsername());
				var had_delegiert_already = existungUserOpt.isPresent()
					&& keyCloakService.isUserInRole(existungUserOpt.get(), FachRolle.FACHVERANTWORTUNG_BAB_DELEGIERT);
				// Wenn der User vorher schon die Rolle hatte, darf er sie behalten, aber neu zuweisen kann nur OI_FACHBAB_DELEGIEREN
				if (!had_delegiert_already) {
					throw new AccessControlException(
						"Access Violation"
							+ " Attempted to create User with a restricted role " + userJax.getFachRolle());
				}
			}
		}
	}

	public void checkAllowedByOdiIdentifier(String groupname) {
		if (userPrincipal.getBenutzer().isPresent()) {

			if (userPrincipal.isCallerInRole(BenutzerRolle.AS_REGISTRATION_OI)) {
				// Benutzer mit Rolle AS_REGISTRATION_OI duerfen alle verwalten
				return;
			}

			if (userPrincipal.getBenutzer().isPresent() &&
				userPrincipal.getBenutzer().get().getOrtDerImpfung()
					.stream()
					.noneMatch(ortDerImpfung -> ortDerImpfung.getIdentifier().equals(groupname))) {
				String username = userPrincipal.getBenutzer().isPresent() ?
					userPrincipal.getBenutzer().get().getBenutzername() : "null";
				LOG.debug("user {} is not allowed to manipulate odi {}", username, groupname);
				throw new AccessControlException(
					"Access Violation"
						+ " for Entity: OrtDerImpfung" + "(identifier=" + groupname + "):"
						+ " for current user: " + username);

			}
		}
	}

	/**
	 * prueft ob der Benutzer dem Odi zugeordnet ist. Achtung wenn man das fuer Callcenter oder normale Benutzer
	 * aufruft wird
	 * eine Exception geworfen weil diese keine Zuordnung haben. Das heisst man muss sicherstellen dass dies nur im
	 * "web" aufgerufen wird
	 */
	public void checkBenutzerAssignedToOdi(OrtDerImpfung ortDerImpfung) {
		if (isUserIndependentFromOdis()) {
			return;
		}
		if (!isBenutzerAssignedToOdi(ortDerImpfung)) {
			throw AppValidationMessage.USER_NOT_IN_ODI.create(ortDerImpfung.getName());
		}
	}

	/**
	 * prueft ob der Benutzer dem Odi zugeordnet ist. Achtung wenn man das fuer Callcenter oder normale Benutzer
	 * aufruft wird
	 * eine Exception geworfen weil diese keine Zuordnung haben. Das heisst man muss sicherstellen dass dies nur im
	 * "web" aufgerufen wird
	 */
	public void checkBenutzerAssignedToAtLeastOneOdi(@NonNull Collection<OrtDerImpfung> orteDerImpfung) {
		boolean allowedForAny = isBenutzerAssignedToAtLeastOneOdi(orteDerImpfung);
		if (!allowedForAny) {
			String odiNames =
				orteDerImpfung.stream().map(OrtDerImpfung::getName).collect(Collectors.joining(","));
			throw AppValidationMessage.USER_NOT_IN_ODI.create(odiNames);
		}
	}

	private boolean isBenutzerAssignedToAtLeastOneOdi(@NotNull Collection<OrtDerImpfung> orteDerImpfung) {
		return orteDerImpfung.stream().anyMatch(this::isBenutzerAssignedToOdi);
	}

	public void checkBenutzerAssignedToAtLeastOneActiveOdi() {
		if (!isBenutzerAssignedToAtLeastOneActiveOdi()) {
			throw AppValidationMessage.USER_HAS_NO_ACTIVE_ODI.create();
		}
	}

	private boolean isBenutzerAssignedToAtLeastOneActiveOdi() {
		if (isUserIndependentFromOdis()) {
			return true;
		}
		Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();
		final Set<OrtDerImpfung> odis = currentBenutzer.getOrtDerImpfung();
		return odis.stream().anyMatch(odi -> !odi.isDeaktiviert());
	}

	public boolean isBenutzerAssignedToOdi(OrtDerImpfung ortDerImpfung) {
		Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();

		// homa kommentar: ich glaube wir machen hier 2 mal den gleichen check da wir mit contains auch die id der odi pruefen
		var userBelongsToOdi = currentBenutzer.getOrtDerImpfung().contains(ortDerImpfung) ||
			currentBenutzer.getOrtDerImpfung()
				.stream()
				.map(AbstractUUIDEntity::getId)
				.anyMatch(ortDerImpfung.getId()::equals);

		return userBelongsToOdi;
	}

	public boolean isUserIndependentFromOdis() {
		return userPrincipal.isCallerInAnyOfRole(
			BenutzerRolle.KT_NACHDOKUMENTATION,
			BenutzerRolle.KT_MEDIZINISCHE_NACHDOKUMENTATION,
			BenutzerRolle.AS_BENUTZER_VERWALTER,
			BenutzerRolle.AS_REGISTRATION_OI);
	}

	public void checkReadAuthorization(@NonNull DocumentQueue docQueue) {
		if (!docQueue.getBenutzer().getId().equals(userPrincipal.getBenutzerOrThrowException().getId())) {
			throwViolation(docQueue);
		}
	}

	public boolean isUserFachBABOrKanton() {
		return userPrincipal.isCallerInRole(BenutzerRolle.OI_IMPFVERANTWORTUNG)
			|| isUserKanton();
	}

	public void checkCallcenterAllowedForKrankheit(@NonNull KrankheitIdentifier krankheitIdentifier) {
		if (userPrincipal.isCallerInRole(BenutzerRolle.CC_AGENT)) {
			if (!krankheitIdentifier.isSupportsCallcenter()) {
				throw AppValidationMessage.NOT_ALLOWED_FOR_KRANKHEIT.create("authorizer", krankheitIdentifier);
			}
		}
	}

	public void checkRoleAllowedForImpfung(@NonNull Impfung impfung) {
		// If you have a KT role and the Impfung is not visible to you, error out.
		// (Note Fachlich you should never have a KT and non KT role at the same time)
		if (isUserKanton()) {
			if (!KantonaleBerechtigung.isEditableForKanton(impfung)) {
				throwViolation(impfung);
			}
		}
	}

	public boolean isUserKanton() {
		return userPrincipal.isCallerInAnyOfRole(BenutzerRolle.getKantonRoles());
	}
}
