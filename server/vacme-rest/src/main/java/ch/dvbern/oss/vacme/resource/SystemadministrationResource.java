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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.jax.ForceTerminJax;
import ch.dvbern.oss.vacme.jax.base.DateRangeJax;
import ch.dvbern.oss.vacme.jax.registration.DashboardJax;
import ch.dvbern.oss.vacme.scheduler.SystemAdminRunnerService;
import ch.dvbern.oss.vacme.service.SystemAdministrationService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_REGISTRATION_OI;

/**
 * Resource fuer die System Administration
 */
@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_SYSTEM_ADMINISTRATION))
@Path("/systemadminstration")
public class SystemadministrationResource {

	private final SystemAdministrationService systemAdministrationService;
	private final SystemAdminRunnerService systemAdminRunnerService;

	@Inject
	public SystemadministrationResource(
		@NonNull SystemAdministrationService systemAdministrationService,
		@NonNull SystemAdminRunnerService systemAdminRunnerService
	) {
		this.systemAdministrationService = systemAdministrationService;
		this.systemAdminRunnerService = systemAdminRunnerService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("terminbuchung")
	@RolesAllowed({ AS_REGISTRATION_OI })
	public DashboardJax forceTerminbuchung(
		@NonNull @NotNull
		@Parameter(description = "Object welches die noetigen Angaben zur Terminbuchung enthaelt")
			@Valid ForceTerminJax forceTerminJax
	) {
		return systemAdministrationService.forceTerminbuchung(forceTerminJax);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("importplz")
	@RolesAllowed({ AS_REGISTRATION_OI })
	public String importPlzFromCsv() {
		return systemAdministrationService.importPlzKantonIntoDatabase();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("importmedstat")
	@RolesAllowed({ AS_REGISTRATION_OI })
	public String importPlzMedstatFromCsv() {
		return systemAdministrationService.importPlzMedstatIntoDatabase();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("updateGlnFromKC")
	@RolesAllowed({ AS_REGISTRATION_OI })
	public void importMissingGln() {
		systemAdministrationService.importMissingGln();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/jobStatistikErstimpfungen")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	public Response startJobStatistikErstimpfungen(
		@NonNull @NotNull DateRangeJax range
	) {
		systemAdministrationService.runServiceReportingAnzahlErstimpfungenMailTask(range.getVon(), range.getBis());
		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/run-application-health")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	public Response runApplicationHealthBatchJob() {
		systemAdminRunnerService.runDbValidationTask();
		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/zertifikate/pendingrevocation/")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	public Response findCertsToRevoke(
		@NonNull @NotNull String regnums
	) {
		return manualCertRevocation(regnums, false);
	}

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/zertifikate/pendingrevocation/fix")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	public Response findAndFixCertsToRevoke(
		@NonNull @NotNull String regnums
	) {
		return manualCertRevocation(regnums, true);
	}

	@NonNull
	private Response manualCertRevocation(
		@NonNull String regnums,
		@NonNull Boolean triggerRevocation
	) {
		String[] split = regnums.split("\n");
		Map<String, List<Zertifikat>> certsToRevoke =
			systemAdministrationService.findCertsToRevoke(Arrays.asList(split));
		if (triggerRevocation) {
			systemAdministrationService.queueRevocationsForZertifikate(certsToRevoke);
		}

		Map<String, List<String>> outputMap = new LinkedHashMap<>();
		certsToRevoke.forEach((key, value) -> outputMap.put(key,
			value.stream().map(this::getZertifikatRevocationInfo).collect(Collectors.toList())));

		return Response.ok(outputMap).build();
	}

	@NonNull
	private String getZertifikatRevocationInfo(@NonNull Zertifikat zertifikat) {
		return zertifikat.getUvci() + ", " +
			zertifikat.getRegistrierung().getRegistrierungsEingang().name() + ", " +
			DateUtil.formatDateTime(zertifikat.getTimestampErstellt());
	}
}
