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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.migration.DataMigrationPatchGLNJax;
import ch.dvbern.oss.vacme.jax.migration.DataMigrationPatchJax;
import ch.dvbern.oss.vacme.jax.migration.DataMigrationRequestJax;
import ch.dvbern.oss.vacme.jax.migration.DataMigrationResponseJax;
import ch.dvbern.oss.vacme.jax.migration.RegistrierungResultatJax;
import ch.dvbern.oss.vacme.jax.migration.StatusMigrationJax;
import ch.dvbern.oss.vacme.service.DataMigrationService;
import ch.dvbern.oss.vacme.shared.errors.AppException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import ch.dvbern.oss.vacme.shared.errors.mappers.AppValidationExceptionMapper;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import io.quarkus.runtime.util.ExceptionUtil;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.IMPFWILLIGER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.MIG_MIGRATION_ADM;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.SYSTEM_INTERNAL_ADMIN;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_INITIALREG;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Tags(@Tag(name = OpenApiConst.TAG_DATA_MIGRATION))
@Path("/")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Transactional(TxType.NOT_SUPPORTED)
public class DataMigrationResource {

	public static final String DATA_MIGRATION_PATH = "/data_migration";
	@ConfigProperty(name = "migration.rate.limit.enabled", defaultValue = "false")
	boolean enabled;

	@ConfigProperty(name = "migration.rate.limit.interval.seconds", defaultValue = "30")
	int interval;

	@ConfigProperty(name = "migration.test_data.enabled", defaultValue = "false")
	boolean testDataEnabled;

	@ConfigProperty(name = "vacme.stufe", defaultValue = "LOCAL")
	String stufe;

	private final DataMigrationService dataMigrationService;
	private final SecurityIdentity securityIdentity;
	private final CurrentIdentityAssociation association;
	private final AppValidationExceptionMapper exceptionMapper;
	private final UserPrincipal userPrincipal;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(VACME_WEB + "/test_data")
	@PermitAll // acl through config flag migration.test_data.enabled
	@Operation(hidden = true, description = "Hidden because it is only used for testing")
	public Response testData(@Nonnull @Valid DataMigrationRequestJax dataMigrationRequestJax) {
		if (!testDataEnabled || stufe.equalsIgnoreCase("PROD")) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		return migrateData(dataMigrationRequestJax);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(VACME_INITIALREG + "/test_data/claim/{regNum}")
	@RolesAllowed(IMPFWILLIGER)
	@Operation(hidden = true, description = "Hidden because it is only used for testing")
	public Response claimTestData(@PathParam("regNum") String regNum) {
		if (!testDataEnabled || stufe.equalsIgnoreCase("PROD")) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		// todo Affenpocken: VACME-2405 low prio: DataMigration ist aktuell nur fuer Covid unterstuetzt
		Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();
		dataMigrationService.claimRegistration(regNum, currentBenutzer, KrankheitIdentifier.COVID);

		return Response.ok().build();
	}

	private Response migrateData(@Nonnull DataMigrationRequestJax dataMigrationRequestJax) {
		LOG.info("VACME-MIGRATION: Service 'migrateData' was called by {} with roles {} ", securityIdentity.getPrincipal().getName(), securityIdentity.getRoles().stream().collect(Collectors.joining()));
		runAsSystemAdmin();

		if (exceededRateLimit()) {
			LOG.error("VACME-MIGRATION rate limit - More than one request received in interval of {} seconds", interval);
			return Response.status(Response.Status.TOO_MANY_REQUESTS).header("X-RateLimit-Limit", calculateHourlyRate()).build();
		}
		dataMigrationService.updateRequestTimestamp();

		DataMigrationResponseJax dataMigrationResponseJax = new DataMigrationResponseJax();
		List<RegistrierungResultatJax> resultatJaxList = new ArrayList<>();
		dataMigrationRequestJax.getRegistrierungen().forEach(request -> {
			RegistrierungResultatJax resultat = new RegistrierungResultatJax();
			resultat.setExternalId(request.getExternalId());
			try {
				String registationNum = dataMigrationService.migrateData(request);
				resultat.setRegistrierungsnummer(registationNum);
				resultat.setStatus(StatusMigrationJax.SUCCESS);
				resultat.setStatusMeldung("Success");
				LOG.info("VACME-MIGRATION success - External_ID: {} ", request.getExternalId());
			} catch (AppException e) {
				LOG.error(String.format("VACME-MIGRATION validation - External_ID: %s", request.getExternalId()), e);
				resultat.setStatus(StatusMigrationJax.ERROR);
				resultat.setStatusMeldung("Validation: " + e.getMessage());
				if (e instanceof AppValidationException) {
					resultat.setStatusMeldung("Validation: " + exceptionMapper.toMessage((AppValidationException) e));
				} else {
					resultat.setStatusMeldung("Validation: " + e.getMessage());
				}
			} catch (Exception e) {
				LOG.error(String.format("VACME-MIGRATION failure - External_ID: %s", request.getExternalId()), e);
				resultat.setStatus(StatusMigrationJax.ERROR);
				resultat.setStatusMeldung("Failure: " + (ExceptionUtil.getRootCause(e) != null ? ExceptionUtil.getRootCause(e).getMessage() : e.getMessage()));
			}
			resultatJaxList.add(resultat);
		});
		dataMigrationResponseJax.setRegistrierungStatusList(resultatJaxList);
		return Response.ok(dataMigrationResponseJax).build();
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(MIG_MIGRATION_ADM)
	@Path(VACME_WEB + DATA_MIGRATION_PATH)
	@Operation(hidden = true, description = "Hidden because it is only used for testing")
	public Response patchPLZ(@Nonnull @Valid DataMigrationPatchJax dataMigrationPatchJax) {
		if (!testDataEnabled || stufe.equalsIgnoreCase("PROD")) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		LOG.info("VACME-MIGRATION: Service 'patchPLZ' was called by {} with roles {} ", securityIdentity.getPrincipal().getName(), securityIdentity.getRoles().stream().collect(Collectors.joining()));
		runAsSystemAdmin();

		if (exceededRateLimit()) {
			LOG.error("VACME-MIGRATION rate limit - More than one request received in interval of {} seconds", interval);
			return Response.status(Response.Status.TOO_MANY_REQUESTS).header("X-RateLimit-Limit", calculateHourlyRate()).build();
		}
		dataMigrationService.updateRequestTimestamp();

		DataMigrationResponseJax dataMigrationResponseJax = new DataMigrationResponseJax();
		List<RegistrierungResultatJax> resultatJaxList = new ArrayList<>();
		dataMigrationPatchJax.getRegistrierungen().forEach(request -> {
			RegistrierungResultatJax resultat = new RegistrierungResultatJax();
			resultat.setExternalId(request.getExternalId());
			try {
				dataMigrationService.patchPLZ(request);
				resultat.setStatus(StatusMigrationJax.SUCCESS);
				resultat.setStatusMeldung("Success");
				LOG.info("VACME-MIGRATION (patch) success - External_ID: {} ", request.getExternalId());
			} catch (AppException e) {
				LOG.error(String.format("VACME-MIGRATION (patch) validation - External_ID: %s", request.getExternalId()), e);
				resultat.setStatus(StatusMigrationJax.ERROR);
				resultat.setStatusMeldung("Validation: " + e.getMessage());
			} catch (Exception e) {
				LOG.error(String.format("VACME-MIGRATION (patch) failure - External_ID: %s", request.getExternalId()), e);
				resultat.setStatus(StatusMigrationJax.ERROR);
				resultat.setStatusMeldung("Failure: " + (ExceptionUtil.getRootCause(e) != null ? ExceptionUtil.getRootCause(e).getMessage() : e.getMessage()));
			}
			resultatJaxList.add(resultat);
		});
		dataMigrationResponseJax.setRegistrierungStatusList(resultatJaxList);
		return Response.ok(dataMigrationResponseJax).build();
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(MIG_MIGRATION_ADM)
	@Path(VACME_WEB + "/patch_odi")
	@Operation(hidden = true, description = "Hidden because it is only used for testing")
	public Response patchGLN(@Nonnull @Valid DataMigrationPatchGLNJax dataMigrationPatchGlnJax) {
		if (!testDataEnabled || stufe.equalsIgnoreCase("PROD")) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		LOG.info("VACME-MIGRATION: Service 'patchGLN' was called by {} with roles {} ", securityIdentity.getPrincipal().getName(), securityIdentity.getRoles().stream().collect(Collectors.joining()));
		runAsSystemAdmin();

		if (exceededRateLimit()) {
			LOG.error("VACME-MIGRATION rate limit - More than one request received in interval of {} seconds", interval);
			return Response.status(Response.Status.TOO_MANY_REQUESTS).header("X-RateLimit-Limit", calculateHourlyRate()).build();
		}
		dataMigrationService.updateRequestTimestamp();

		DataMigrationResponseJax dataMigrationResponseJax = new DataMigrationResponseJax();
		List<RegistrierungResultatJax> resultatJaxList = new ArrayList<>();
		dataMigrationPatchGlnJax.getRegistrierungen().forEach(request -> {
			RegistrierungResultatJax resultat = new RegistrierungResultatJax();
			resultat.setExternalId(request.getExternalId());
			try {
				String changedRegnum = dataMigrationService.patchGLN(request);
				resultat.setRegistrierungsnummer(changedRegnum);
				resultat.setStatus(StatusMigrationJax.SUCCESS);
				resultat.setStatusMeldung("Success");
				LOG.info("VACME-MIGRATION (patchGLN) success - External_ID: {} ", request.getExternalId());
			} catch (AppException e) {
				LOG.error(String.format("VACME-MIGRATION (patchGLN) validation - External_ID: %s", request.getExternalId()), e);
				resultat.setStatus(StatusMigrationJax.ERROR);
				resultat.setStatusMeldung("Validation: " + e.getMessage());
			} catch (Exception e) {
				LOG.error(String.format("VACME-MIGRATION (patchGLN) failure - External_ID: %s", request.getExternalId()), e);
				resultat.setStatus(StatusMigrationJax.ERROR);
				resultat.setStatusMeldung("Failure: " + (ExceptionUtil.getRootCause(e) != null ? ExceptionUtil.getRootCause(e).getMessage() : e.getMessage()));
			}
			resultatJaxList.add(resultat);
		});
		dataMigrationResponseJax.setRegistrierungStatusList(resultatJaxList);
		return Response.ok(dataMigrationResponseJax).build();
	}

	private void runAsSystemAdmin() {
		QuarkusSecurityIdentity.Builder builder = new QuarkusSecurityIdentity.Builder();
		QuarkusSecurityIdentity internalAdmin =
			builder.addRole(SYSTEM_INTERNAL_ADMIN).setPrincipal(() -> SYSTEM_INTERNAL_ADMIN).build();
		association.setIdentity(internalAdmin);
	}

	private int calculateHourlyRate() {
		return 3600 / interval;
	}

	private boolean exceededRateLimit() {
		if (!enabled) {
			return false;
		}
		Optional<LocalDateTime> lastRequestTimestamp = dataMigrationService.getLastRequestTimestamp();
		return lastRequestTimestamp.filter(localDateTime -> Duration.between(localDateTime, LocalDateTime.now()).getSeconds() < interval).isPresent();
	}

}
