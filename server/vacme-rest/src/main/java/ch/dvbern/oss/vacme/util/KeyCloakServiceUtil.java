/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.util;

import java.util.Collections;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

public class KeyCloakServiceUtil {

	private KeyCloakServiceUtil() {
		// util
	}

	public static void addRequiredActionToUser(
		@NonNull String userId,
		@NonNull RealmResource realmResource,
		@NonNull String requiredActionId) {
		UsersResource usersRessource = realmResource.users();
		UserRepresentation userRep = usersRessource.get(userId).toRepresentation();
		userRep.setRequiredActions(Collections.singletonList(requiredActionId));
		usersRessource.get(userId).update(userRep);
	}
}
