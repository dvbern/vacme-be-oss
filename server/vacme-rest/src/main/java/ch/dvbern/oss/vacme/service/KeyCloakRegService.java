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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.keyclaok.KeycloakAdapter;
import ch.dvbern.oss.vacme.keyclaok.RealmVacmeReg;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.KeyCloakServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * Service fuer den Keycloak VACME Realm (i.e. nur fuer die Portalapplikation)
 */
@Slf4j
@RequestScoped
@Transactional
public class KeyCloakRegService {

	KeycloakAdapter realmResourceReg;
	private static final String MOBILENUMMER = Constants.MOBILE_NUMMER_CLAIM;

	@Inject
	public KeyCloakRegService(
		@NonNull  @RealmVacmeReg KeycloakAdapter realmResourceReg
	) {
		this.realmResourceReg = realmResourceReg;
	}

	public void removeUser(@NonNull @NotNull String id) {
		UsersResource usersRessource = realmResourceReg.getClient().users();
		UserResource userResource = usersRessource.get(id);

		try {
			userResource.remove();
		} catch (Exception e) {
			LOG.error("could not remove user {} from keycloak: {}", id, e.getMessage());
			throw e;
		}
		LOG.info("Removed user with userId {}", id);
	}

	public void removeUserIfExists(@NonNull @NotNull String id) {
		UsersResource usersRessource = realmResourceReg.getClient().users();
		UserResource userResource = usersRessource.get(id);
		try {
			userResource.remove();
			LOG.info("Removed user with userId {}", id);
		} catch (Exception e) {
			LOG.error("could not remove user {} from keycloak: {}", id, e.getMessage());
		}
	}

	/**
	 * Mit diesem Service koennen Mobile und Email eines Benutzers im 'vacme' Realm angepasst werden
	 */
	public void updateUserLoginDataForRegistrierung(@NonNull Benutzer benutzer, @Nullable String mail, @Nullable String telephone) {
		UserRepresentation representation = findUserByUserId(benutzer.getId());
		if (StringUtils.isNotBlank(mail)) {
			representation.setEmail(mail);
		}
		Map<String, List<String>> attributes = representation.getAttributes();
		if (StringUtils.isNotBlank(telephone)) {
			attributes.put(MOBILENUMMER, List.of(telephone));
		}

		final UserResource userResourceToUpdate = realmResourceReg.getClient().users().get(representation.getId());
		userResourceToUpdate.update(representation);
	}


	@NonNull
	public UserRepresentation findUserByUserId(@NonNull UUID userId) {
		UserResource userResource = realmResourceReg.getClient().users().get(userId.toString());
		if(userResource == null ){
			throw AppValidationMessage.USER_NOT_FOUND.create();
		}
		UserRepresentation representation =  userResource.toRepresentation();
		if (representation == null) {
			throw AppValidationMessage.USER_NOT_FOUND.create();
		}
		return representation;
	}


}
