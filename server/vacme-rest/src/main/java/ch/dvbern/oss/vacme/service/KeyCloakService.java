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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.types.FachRolle;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.jax.registration.OdiUserDisplayNameJax;
import ch.dvbern.oss.vacme.jax.registration.OdiUserJax;
import ch.dvbern.oss.vacme.keyclaok.KeycloakAdapter;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

@Slf4j
@ApplicationScoped
@Transactional
public class KeyCloakService {

	RealmResource realmResource;
	KeycloakAdapter keycloakAdapter;
	final UserPrincipal userPrincipal;
	final SmsService smsService;

	private static final String USERERSTELLT = "userErstellt";
	private static final String MOBILENUMMER = Constants.MOBILE_NUMMER_CLAIM;
	private static final String GLNNUMMER = "GlnNummer"; // so heisst das Attribut in Keycloak
	private static final String DISABLED_DUE_TO_INACTIVITY_TS = "DISABLED_DUE_TO_INACTIVITY";

	@Inject
	public KeyCloakService(
		KeycloakAdapter keycloakAdapter,
		UserPrincipal userPrincipal,
		SmsService smsService
	) {
		this.keycloakAdapter = keycloakAdapter;
		this.realmResource = keycloakAdapter.getClient();
		this.userPrincipal = userPrincipal;
		this.smsService = smsService;
	}

	public boolean createGroup(@NonNull @NotNull String groupName) {
		GroupRepresentation group = new GroupRepresentation();
		group.setName(groupName);
		final Response add = realmResource.groups().add(group);
		if (add.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
			// nicht erfolgreich
			LOG.error("Gruppe {} konnte nicht erstellt werden. Status {}", groupName, add.getStatus());
			return false;
		}
		return true;
	}

	public boolean isUserInRole(@NonNull UserRepresentation userRepresentation, @NonNull FachRolle fachRolle) {
		UsersResource usersRessource = realmResource.users();
		final UserResource userResourceToUpdate = usersRessource.get(userRepresentation.getId());
		final List<RoleRepresentation> realmMappings = userResourceToUpdate.roles().getAll().getRealmMappings();
		if (realmMappings != null) {
			return realmMappings.stream().anyMatch(roleRepresentation -> roleRepresentation.getName().equals(fachRolle.getKeyCloakRoleName()));
		}
		return false;
	}

	@NonNull
	public List<String> getRolesOfUser(@NonNull UserRepresentation userRepresentation) {
		List<String> roles = new ArrayList<>();
		UsersResource usersRessource = realmResource.users();
		final RolesResource rolesResource = realmResource.roles();
		final UserResource userResourceToUpdate = usersRessource.get(userRepresentation.getId());
		final RoleMappingResource roleList = userResourceToUpdate.roles();
		final List<RoleRepresentation> realmMappings = roleList.getAll().getRealmMappings();
		if (realmMappings != null) {
			for (RoleRepresentation realmMapping : realmMappings) {
				final RoleResource roleResource = rolesResource.get(realmMapping.getName());
				final Set<RoleRepresentation> roleComposites = roleResource.getRoleComposites();
				for (RoleRepresentation roleComposite : roleComposites) {
					roles.add(roleComposite.getName());
				}
				roles.add(realmMapping.getName());
			}
		}
		return roles;
	}

	public boolean isUserInGroup(@NonNull UserRepresentation userRepresentation, @NonNull String groupName) {
		UsersResource usersRessource = realmResource.users();
		final UserResource userResourceToUpdate = usersRessource.get(userRepresentation.getId());

		final List<GroupRepresentation> realmMappings = userResourceToUpdate.groups();
		if (realmMappings != null) {
			return realmMappings.stream().anyMatch(roleRepresentation -> roleRepresentation.getName().equals(groupName));
		}
		return false;
	}

	public @NotNull OdiUserJax createUser(@NonNull @NotNull @Valid OdiUserJax userJax, @NonNull @NotNull String groupName) {
		UsersResource usersRessource = realmResource.users();
		return createOdiUser(userJax, groupName, usersRessource, realmResource);
	}

	public @NonNull @NotNull @Valid OdiUserJax createOrUpdateUser(@NonNull @NotNull @Valid OdiUserJax userJax, @NonNull @NotNull String groupName) {

		boolean createMode = true;
		if (userJax.getId() != null) {
			createMode = false;
		}

		UsersResource usersRessource = realmResource.users();

		final Optional<UserRepresentation> userWithSameUsernameOptional = findUserByUsername(userJax.getUsername());
		OdiUserJax keyCloakUserJax;

		if (createMode) {
			// Bei in Vacme erstellten ODI-Benutzern ist immer Email = Benutzername
			// Bei direkt in KeyCloak erstellten Benutzern koennen die Attribute aber unterschiedlich sein,
			// wir muessen sicherstellen, dass sowohl Benutzername als auch Email noch nicht besetzt ist.
			final Optional<UserRepresentation> userWithSameEmailOptional = findUserByEmail(userJax.getEmail());
			if (userWithSameUsernameOptional.isEmpty() && userWithSameEmailOptional.isEmpty()) {
				keyCloakUserJax = createOdiUser(userJax, groupName, usersRessource, realmResource);
			} else {
				LOG.error("User already exist");
				throw AppValidationMessage.KC_USER_ALREADY_EXIST.create();
			}
		} else {
			if (userWithSameUsernameOptional.isEmpty()) {
				LOG.error("User does not exist");
				throw AppValidationMessage.KC_USER_DOES_NOT_EXIST.create();
			} else {
				LOG.info("Update User");
				keyCloakUserJax = updateOdiUser(userJax, usersRessource, userWithSameUsernameOptional, realmResource);
			}
		}

		return keyCloakUserJax;

	}

	@NonNull
	public Optional<UserRepresentation> findUserByUsername(@NonNull String username) {
		return realmResource.users().search(username, true)
			.stream()
			.findFirst();
	}

	@NonNull
	public Optional<UserRepresentation> findUserByEmail(@NonNull String email) {
		return realmResource.users().search(null, null, null, email, null, null)
			.stream()
			.findFirst();
	}

	@NonNull
	public Optional<UserRepresentation> findUserByUsernameAndEmail(@NonNull String username, @NonNull String email) {
		// Die Suche nach Attribute ist nicht exakt: Bei Suche nach muster@mail.ch wird auch felix.muster@mail.ch
		// zurueckgegeben. Darum muss das Suchresultat noch manuell gefiltert werden!
		return realmResource.users().search(username, null, null, email, null, null)
		.stream()
		.filter(userRepresentation -> (userRepresentation.getEmail().equalsIgnoreCase(email)
			|| userRepresentation.getUsername().equalsIgnoreCase(username)))
		.findFirst();
	}

	@NotNull
	private OdiUserJax updateOdiUser(@NotNull OdiUserJax userJax, UsersResource usersRessource,
		Optional<UserRepresentation> userResource, RealmResource realmResource) {
		OdiUserJax keyCloakUserJax;
		final UserRepresentation updateUser = userResource.get();

		final UserResource userResourceToUpdate = usersRessource.get(updateUser.getId());

		mapFieldsToKeycloakUser(userJax, updateUser);

		userResourceToUpdate.update(updateUser);
		keyCloakUserJax = mapKeycloakUserToJax(updateUser, usersRessource);

		// Join Role
		if (userJax.getFachRolle() != null) {
			boolean hasRole = false;
			// get all Roles from KC but filter for only known FachRolles and BenutzerRolles. KC specific roles like the default-roles will be untouched
			final List<RoleRepresentation> realmMappings = userResourceToUpdate.roles().getAll().getRealmMappings()
				.stream()
				.filter(this::isVacmeRole)
				.collect(Collectors.toList());
			if (!realmMappings.isEmpty()) {
				hasRole =
					realmMappings.stream().anyMatch(roleRepresentation -> roleRepresentation.getName().equals(userJax.getFachRolle().getKeyCloakRoleName()));
			}

			if (!hasRole) {
				LOG.info("Role of user has changed");
				// remove all role
				if (!realmMappings.isEmpty()) {
					userResourceToUpdate.roles().realmLevel()
						.remove(realmMappings);
				}
				RoleRepresentation roleRepresentation = realmResource.roles().get(userJax.getFachRolle().getKeyCloakRoleName()).toRepresentation();

				userResourceToUpdate.roles().realmLevel()
					.add(Arrays.asList(roleRepresentation));
			}
		}

		return keyCloakUserJax;
	}

	boolean isVacmeRole(@NonNull RoleRepresentation roleRepresentation){
		return FachRolle.getAllRoleNames().stream().anyMatch(fr -> fr.equals(roleRepresentation.getName()))
			|| Arrays.stream(BenutzerRolle.values()).anyMatch(s -> s.name().equals(roleRepresentation.getName()));
	}

	private void mapFieldsToKeycloakUser(@NotNull OdiUserJax userJax, UserRepresentation updateUser) {
		updateUser.setFirstName(userJax.getFirstName());
		updateUser.setLastName(userJax.getLastName());
		updateUser.setEmail(userJax.getEmail());

		Map<String, List<String>> attributes = updateUser.getAttributes() == null ? new HashMap<>() : updateUser.getAttributes();
		final Optional<ID<Benutzer>> benutzerIdOpt = userPrincipal.getBenutzerIdOpt();
		benutzerIdOpt.ifPresent(benutzerID -> attributes.put(USERERSTELLT,
			Arrays.asList(benutzerID.getId().toString())));
		attributes.put(MOBILENUMMER, Arrays.asList(userJax.getPhone()));
		if (userJax.getGlnNummer() != null && !userJax.getGlnNummer().isEmpty()) {
			attributes.put(GLNNUMMER, Arrays.asList(userJax.getGlnNummer()));
		}
		updateUser.setAttributes(attributes);
	}

	@NotNull
	private OdiUserJax createOdiUser(@NotNull OdiUserJax userJax, @NotNull String groupName,
		UsersResource usersRessource, RealmResource realmResource) {
		OdiUserJax keyCloakUserJax;
		//Define user
		UserRepresentation user = new UserRepresentation();
		user.setEnabled(true);
		user.setUsername(userJax.getUsername());
		mapFieldsToKeycloakUser(userJax, user);

		// Create user (requires manage-users role)
		Response response = usersRessource.create(user);

		String userId = CreatedResponseUtil.getCreatedId(response);
		userJax.setId(userId);

		keyCloakUserJax = setDefaultPassword(userJax, true);

		LOG.info("User created with userId: {}", userId);

		joinGroup(Objects.requireNonNull(keyCloakUserJax.getId()), groupName);

		// Join Role
		if (userJax.getFachRolle() != null) {
			final UserResource userResourceToUpdate = usersRessource.get(keyCloakUserJax.getId());
			RoleRepresentation roleRepresentation = realmResource.roles().get(userJax.getFachRolle().getKeyCloakRoleName()).toRepresentation();

			userResourceToUpdate.roles().realmLevel()
				.add(Arrays.asList(roleRepresentation));
		}

		return keyCloakUserJax;
	}

	public @NonNull @NotNull @Valid OdiUserJax setDefaultPassword(@NonNull @NotNull @Valid OdiUserJax userJax, boolean temporaryPw) {
		// Define password credential
		final String id = userJax.getId();
		Objects.requireNonNull(id);

		CredentialRepresentation passwordCred = new CredentialRepresentation();
		passwordCred.setTemporary(temporaryPw);
		passwordCred.setType(CredentialRepresentation.PASSWORD);
		final String defaultPassword = generateDefaultPassword();
		passwordCred.setValue(defaultPassword);

		// Momentan immer Deutsch da wir keine andere Infos haben.
		smsService.sendPasswordSMS(defaultPassword, Locale.GERMAN, userJax.getPhone());

		// Get realm
		UsersResource usersRessource = realmResource.users();
		UserResource userResource = usersRessource.get(id);

		// Set password credential
		userResource.resetPassword(passwordCred);
		LOG.info("User with userId {} password changed", id);

		return userJax;
	}

	private String generateDefaultPassword() {
		PasswordGenerator gen = new PasswordGenerator();
		CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
		CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
		lowerCaseRule.setNumberOfCharacters(2);

		CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
		CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
		upperCaseRule.setNumberOfCharacters(2);

		CharacterData digitChars = EnglishCharacterData.Digit;
		CharacterRule digitRule = new CharacterRule(digitChars);
		digitRule.setNumberOfCharacters(2);

		String password = gen.generatePassword(8, lowerCaseRule,
			upperCaseRule, digitRule);
		return password;
	}

	public void joinGroup(@NonNull @NotNull String id, @NonNull @NotNull String groupName) {

		UsersResource usersRessource = realmResource.users();
		UserResource userResource = usersRessource.get(id);

		// Add to Group
		final GroupRepresentation groupRepresentation =
			findGroup(groupName).orElseThrow(
				() -> AppValidationMessage.KC_GROUP_DOES_NOT_EXIST.create(groupName)
			);

		userResource.joinGroup(groupRepresentation.getId());

		LOG.info("User with userId {} added to group {}", id, groupName);
	}

	public void leaveGroup(@NonNull @NotNull String id, @NonNull @NotNull String groupName) {

		UsersResource usersRessource = realmResource.users();
		UserResource userResource = usersRessource.get(id);

		// Add to Group
		final GroupRepresentation groupRepresentation =
			findGroup(groupName).orElseThrow(
				() -> AppValidationMessage.KC_GROUP_DOES_NOT_EXIST.create(groupName)
			);

		userResource.leaveGroup(groupRepresentation.getId());

		LOG.info("User with userId {} removed from group {}", id, groupName);
	}

	public UserRepresentation toggleEnabled(@NonNull @NotNull String id) {
		UsersResource usersRessource = realmResource.users();
		UserResource userResource = usersRessource.get(id);

		final UserRepresentation userRepresentation = userResource.toRepresentation();
		final boolean inverse = !userRepresentation.isEnabled();
		userRepresentation.setEnabled(inverse);
		userResource.update(userRepresentation);

		LOG.info("Set enabled to {} for user with userId {} ", inverse, userRepresentation.getId());
		return userRepresentation;
	}

	public void removeUser(@NonNull @NotNull String id) {
		UsersResource usersRessource = realmResource.users();
		UserResource userResource = usersRessource.get(id);
		userResource.remove();
		LOG.info("Removed user with userId {}", id);
	}

	public void joinRole(@NonNull @NotNull String id, @NonNull @NotNull String roleName) {

		UsersResource usersRessource = realmResource.users();
		UserResource userResource = usersRessource.get(id);

		// Add to Role
		RoleRepresentation roleRepresentation = realmResource.roles().get(roleName).toRepresentation();

		// Assign realm role tester to user
		userResource.roles().realmLevel() //
			.add(Arrays.asList(roleRepresentation));

		LOG.info("User with userId {} added to role {}", id, roleName);
	}

	public void leaveRole(@NonNull @NotNull String id, @NonNull @NotNull String roleName) {

		UsersResource usersRessource = realmResource.users();
		UserResource userResource = usersRessource.get(id);

		// Add to Role
		RoleRepresentation roleRepresentation = realmResource.roles().get(roleName).toRepresentation();

		// Assign realm role tester to user
		userResource.roles().realmLevel() //
			.remove(Arrays.asList(roleRepresentation));

		LOG.info("User with userId {} removed from role {}", id, roleName);
	}

	public List<OdiUserJax> getUsersInGroup(@NonNull @NotNull String groupeName, boolean includeDisabled, int first, int max) {

		UsersResource usersRessource = realmResource.users();

		final GroupRepresentation groupRepresentation =
			realmResource.groups().groups().stream()
				.filter(currGrp -> currGrp.getName().equals(groupeName)).findFirst()
				.orElseThrow(() -> AppValidationMessage.KC_GROUP_DOES_NOT_EXIST.create(groupeName));

		final List<OdiUserJax> userInGroup =
			realmResource.groups().group(groupRepresentation.getId()).members(first, max, true).stream()
				.filter(user -> includeDisabled || user.isEnabled())
				.map(p -> mapKeycloakUserToJax(p, usersRessource))
				.collect(Collectors.toList());

		LOG.debug("Found Users {} from group {}",
			userInGroup.stream().map(OdiUserJax::getUsername).collect(Collectors.joining(",")), groupeName);

		return userInGroup;
	}

	@NotNull
	private OdiUserJax mapKeycloakUserToJax(UserRepresentation p, UsersResource usersRessource) {

		FachRolle fachRolleOfOdiBenutzer = null;

		final UserResource userResourceToUpdate = usersRessource.get(p.getId());
		final List<RoleRepresentation> realmMappings = userResourceToUpdate.roles().getAll().getRealmMappings();
		if (realmMappings != null) {
			final List<FachRolle> fachRolles = realmMappings.stream()
				.filter(roleRepresentation -> Arrays.stream(FachRolle.values()).anyMatch(fachRolle -> fachRolle.getKeyCloakRoleName().equals(roleRepresentation.getName())))
				.map(roleRepresentation -> FachRolle.fromKeyCloakRoleName(roleRepresentation.getName()))
				.collect(Collectors.toList());
			if (fachRolles.size() == 1) {
				fachRolleOfOdiBenutzer = (fachRolles.get(0));
			} else {
				LOG.error("Ungueltige Anzahl fachliche Rollen {}, for user: {}", fachRolles.size(), p.getUsername());
			}
		}

		final OdiUserJax odiUserJax = new OdiUserJax(p.getId(), p.getUsername(), p.isEnabled(), p.getFirstName(), p.getLastName(), p.getEmail(),
			getAttribute(p, MOBILENUMMER), getAttribute(p, GLNNUMMER), fachRolleOfOdiBenutzer);

		return odiUserJax;
	}

	public Optional<String> getGlnNummerOfUser(@NonNull String userName) {
		UsersResource usersRessource = realmResource.users();
		UserRepresentation userRep = usersRessource.search(userName, true).stream()
			.findFirst().orElseThrow(() -> AppValidationMessage.KC_USER_DOES_NOT_EXIST.create(userName));
		String attribute = getAttribute(userRep, GLNNUMMER);
		return Optional.ofNullable(attribute);
	}

	public Optional<String> getMobileNumberOfUser(UserRepresentation p) {
		final String attribute = getAttribute(p, MOBILENUMMER);
		return Optional.ofNullable(attribute);
	}

	private String getAttribute(UserRepresentation p, String attrName) {
		final Map<String, List<String>> pAttributes = p.getAttributes();
		if (pAttributes == null) {
			LOG.debug("No attribute found for {}", attrName);
			return "";
		}
		final List<String> strings = pAttributes.get(attrName);
		if (strings == null || strings.isEmpty()) {
			LOG.debug("No attribute found for {}", attrName);
			return "";
		}
		if (strings.size() > 1) {
			LOG.warn("More than one attribute found for {}", attrName);
		}
		return strings.get(0);

	}

	/**
	 * reads the user from the fixed vacme-web realm by the username.
	 * The user must also be in the given group
	 *
	 * @param username the username to search for
	 * @param groupName the groupname
	 * @return the dto containing the user or an exception if the user does not exist
	 */
	public OdiUserJax getUserByGroupAndUsername(@NonNull String username, @NonNull String groupName) {

		UsersResource usersRessource = realmResource.users();

		UserRepresentation userRep = findUserByUsername(username)
			.orElseThrow(() -> AppValidationMessage.KC_USER_DOES_NOT_EXIST.create(username));

		boolean isInSearchedGroup =
			realmResource.users().get(userRep.getId()).groups(groupName, 0, 20).stream()
				.anyMatch(groupRepresentation -> groupRepresentation.getName().equals(groupName));

		if (!isInSearchedGroup) {
			throw AppValidationMessage.KC_USER_DOES_NOT_EXIST.create(username);
		}
		OdiUserJax keyCloakUserJax;
		keyCloakUserJax = mapKeycloakUserToJax(userRep, usersRessource);

		return keyCloakUserJax;
	}

	public UserResource getUser(@NonNull String userId) {
		return realmResource.users().get(userId);
	}

	public boolean checkGroupExists(String ortDerImpfungCode) {
		return findGroup(ortDerImpfungCode).isPresent();
	}

	@NonNull
	public Optional<GroupRepresentation> findGroup(@NonNull @NotNull String groupName) {
		return realmResource.groups().groups(0, Constants.MAX_KEYCLOAK_RESULTS).stream()
			.filter(currGroup -> currGroup.getName().equals(groupName)).findFirst();
	}

	public List<OdiUserDisplayNameJax> getUserInRole(FachRolle fachRolle) {

		final RolesResource roles = realmResource.roles();

		return roles.get(fachRolle.getKeyCloakRoleName()).getRoleUserMembers(0, Constants.MAX_KEYCLOAK_RESULTS).stream()
			.filter(UserRepresentation::isEnabled)
			.map(p -> new OdiUserDisplayNameJax(p.getId(), p.getUsername(), p.getFirstName(), p.getLastName(), p.getEmail()))
			.sorted(Comparator.comparing(OdiUserDisplayNameJax::getFirstName, String.CASE_INSENSITIVE_ORDER).thenComparing(OdiUserDisplayNameJax::getLastName, String.CASE_INSENSITIVE_ORDER))
			.collect(Collectors.toList());
	}

	public List<OdiUserDisplayNameJax> getUsersInRoleAndGroup(@NonNull FachRolle fachRolle, @NonNull String groupeName) {

		final RolesResource roles = realmResource.roles();

		final List<OdiUserJax> userFromGroup = getUsersInGroup(groupeName, false, 0, Constants.MAX_KEYCLOAK_RESULTS);

		// User in Role must be in KeyCloak Group
		return roles.get(fachRolle.getKeyCloakRoleName()).getRoleUserMembers(0, Constants.MAX_KEYCLOAK_RESULTS).stream()
			.filter(userRepresentation -> userRepresentation.isEnabled() && userFromGroup.stream().anyMatch(odiUserJax -> {
				assert odiUserJax.getId() != null;
				return odiUserJax.getId().equals(userRepresentation.getId());
			}))
			.map(p -> new OdiUserDisplayNameJax(p.getId(), p.getUsername(), p.getFirstName(), p.getLastName(), p.getEmail()))
			.sorted(Comparator.comparing(OdiUserDisplayNameJax::getFirstName, String.CASE_INSENSITIVE_ORDER).thenComparing(OdiUserDisplayNameJax::getLastName, String.CASE_INSENSITIVE_ORDER))
			.collect(Collectors.toList());
	}

	public void disableUser(@NonNull String userId) {
		UsersResource usersRessource = realmResource.users();
		UserRepresentation userRep = usersRessource.get(userId).toRepresentation();
		userRep.setEnabled(false); // disable user
		// for audit purposes we store the autodisable ts as an attribute
		List<String> listOfTimestamps = new ArrayList<>();
		if (userRep.getAttributes() != null) {
			listOfTimestamps = userRep.getAttributes().get(DISABLED_DUE_TO_INACTIVITY_TS) == null ?
				new ArrayList<>() :
				userRep.getAttributes().get(DISABLED_DUE_TO_INACTIVITY_TS);
		} else {
			userRep.setAttributes(new HashMap<>());
		}
		listOfTimestamps.add(DateUtil.FILENAME_DATETIME_PATTERN.apply(Locale.GERMAN).format(LocalDateTime.now()));
		userRep.getAttributes().put(DISABLED_DUE_TO_INACTIVITY_TS, listOfTimestamps);
		usersRessource.get(userId).update(userRep);
	}

}
