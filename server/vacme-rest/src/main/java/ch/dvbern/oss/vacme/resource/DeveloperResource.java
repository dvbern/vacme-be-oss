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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.reports.festnetznummerBenutzer.FestnetznummerBenutzerReportServiceBean;
import ch.dvbern.oss.vacme.scheduler.SystemAdminRunnerService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.massenmutation.MassenverarbeitungService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.SYSTEM_INTERNAL_ADMIN;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Tags(@Tag(name = OpenApiConst.TAG_DEVELOP))
@Path(VACME_WEB + "/develop")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DeveloperResource {

	private final CurrentIdentityAssociation association;
	private final SystemAdminRunnerService systemAdminRunnerService;
	private final BoosterService boosterService;
	private final VacmeSettingsService vacmeSettingsService;
	private final MassenverarbeitungService massenmutationService;
	private final FestnetznummerBenutzerReportServiceBean festnetznummerBenutzerReportServiceBean;
	private final OrtDerImpfungRepo ortDerImpfungRepo;
	private final ImpfdossierService impfdossierCreationService;

	@POST
	@Path("festnetznummerBenutzer")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response getFestnetznummerBenutzer() {
		List<String> festnetznummerBenutzer =
			festnetznummerBenutzerReportServiceBean.fetchAllValidFestnetznummerBenutzer();
		return Response.ok(festnetznummerBenutzer).build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("callVMDLUploadBatchJob")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response callVMDLUploadBatchJob(
	) {
		loginAsSystemInternalAdmin();
		Long numberOfRunsToTrigger = vacmeSettingsService.getVmdlManualTriggerNumberOfRunsToTrigger();

		for (int i = 0; i < numberOfRunsToTrigger; i++) {
			systemAdminRunnerService.runVMDLUploadTaskForAllKrankheiten();
		}
		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("callBoosterImmunisiertStatusImpfschutzService")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response callBoosterImmunisiertStatusImpfschutzService() {
		systemAdminRunnerService.runBoosterImmunisiertStatusImpfschutzService(true);
		return Response.ok().build();
	}

	@POST
	@Path("callBoosterFreigabeStatusService")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response callBoosterFreigabeStatusService() {
		systemAdminRunnerService.runBoosterFreigabeStatusService(true);
		return Response.ok().build();
	}

	@POST
	@Path("callPriorityUpdateForGrowingChildren")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response callPriorityUpdateForGrowingChildren() {
		systemAdminRunnerService.runPriorityUpdateForGrowingChildren();
		return Response.ok().build();
	}

	@POST
	@Path("booster/recalculate/all")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response callBoosterRecalculation() {
		try {
			int numCreated = boosterService.queueAllRegsForImpfschutzRecalculation();
			return Response.ok(numCreated).build();
		} catch (Exception e) {
			LOG.error("VACME-BOOSTER-RULE_ENGINE: Error while queueing Regs for recalculation", e);
			return Response.serverError().build();
		}
	}

	@POST
	@Path("booster/recalculate/krankheit/{krankheit}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response callBoosterRecalculationForKrankheit(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit
	) {
		try {
			int numCreated = boosterService.queueAllRegsForImpfschutzRecalculation(krankheit);
			return Response.ok(numCreated).build();
		} catch (Exception e) {
			LOG.error("VACME-BOOSTER-RULE_ENGINE: Error while queueing Regs for recalculation", e);
			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("callArchivierung")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response callArchivierung() {
		systemAdminRunnerService.runArchivierungD3();
		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("callUserDeactivation")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response callUserDeactivation() {
		systemAdminRunnerService.runInactiveOdiUserDisableTask();
		return Response.ok().build();
	}

	private void loginAsSystemInternalAdmin() {
		QuarkusSecurityIdentity.Builder builder = new QuarkusSecurityIdentity.Builder();
		QuarkusSecurityIdentity internalAdmin =
			builder.addRole(SYSTEM_INTERNAL_ADMIN).setPrincipal(() -> SYSTEM_INTERNAL_ADMIN).build();
		association.setIdentity(internalAdmin);
	}

	@POST
	@Path("/impfungen/externalize")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response externalizeImpfungen(@NotNull @NonNull String csvWithImpfids) {
		List<UUID> impfungenIds = massenmutationService.parseAsImpfungenUUIDs(csvWithImpfids);
		massenmutationService.addImpfungenToExternalizeQueue(toImpfungIds(impfungenIds));
		return Response.ok(impfungenIds.size()).build();
	}

	@NonNull
	private List<ID<Impfung>> toImpfungIds(@NonNull List<UUID> impfungenIds) {
		return impfungenIds.stream().map(Impfung::toId).collect(Collectors.toList());
	}

	@POST
	@Path("/impfungen/odimigration")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response moveImpfungenToOdi(@NotNull @NonNull String csvWithImpfids) {
		List<Pair<UUID, UUID>> impfungenAndOdiUUIDs =
			massenmutationService.parseAsImpfungenAndOdiUUIDs(csvWithImpfids);
		List<Pair<ID<Impfung>, ID<OrtDerImpfung>>> listWithIds = toImpfungAndOdiId(impfungenAndOdiUUIDs);
		massenmutationService.addImpfungenToMoveToOdiQueue(listWithIds);

		return Response.ok(impfungenAndOdiUUIDs.size()).build();
	}

	@POST
	@Path("/deleteregistrierungen")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteRegistrierungen(@NotNull @NonNull String csvWithRegistrierungsnummern) {
		List<String> registrierungsnummern =
			massenmutationService.parseAsRegistrierungsnummern(csvWithRegistrierungsnummern);
		massenmutationService.addRegistrierungenToDeleteQueue(registrierungsnummern);
		return Response.ok(registrierungsnummern.size()).build();
	}

	@POST
	@Path("/impfungen/loeschen")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response impfungenLoeschen(@NotNull @NonNull String csvWithImpfids) {
		List<UUID> impfungenIds = massenmutationService.parseAsImpfungenUUIDs(csvWithImpfids);
		massenmutationService.addImpfungenToLoeschenQueue(toImpfungIds(impfungenIds));
		return Response.ok(impfungenIds.size()).build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("covid/impfdossier/ensure")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response addAllToImpfdossierEnsureQueue() {
		List<String> regsWithoutImpfdossier = impfdossierCreationService.findRegsWithoutImpfdossierCovid();
		massenmutationService.addRegistrierungenToImpfdossierCreateQueue(regsWithoutImpfdossier);
		return Response.ok(regsWithoutImpfdossier.size()).build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("calculateOdiLatLng")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	@Transactional(TxType.NOT_SUPPORTED)
	public Response calculateOdiLatLn() {
		List<ID<OrtDerImpfung>> odiIds = ortDerImpfungRepo.findAll()
			.stream()
			.map(ortDerImpfung -> OrtDerImpfung.toId(ortDerImpfung.getId()))
			.collect(Collectors.toList());
		massenmutationService.addOdiToLatLngQueue(odiIds);
		return Response.ok().build();
	}

	@NonNull
	private List<Pair<ID<Impfung>, ID<OrtDerImpfung>>> toImpfungAndOdiId(List<Pair<UUID, UUID>> impfungenAndOdiUUIDs) {
		return impfungenAndOdiUUIDs.stream()
			.map(uuiduuidPair -> Pair.of(
				Impfung.toId(uuiduuidPair.getLeft()),
				OrtDerImpfung.toId(uuiduuidPair.getRight())))
			.collect(Collectors.toList());
	}
}
