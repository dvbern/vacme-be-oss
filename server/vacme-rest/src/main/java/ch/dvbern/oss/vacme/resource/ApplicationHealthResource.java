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

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.jax.RegistrierungTermineImpfungJax;
import ch.dvbern.oss.vacme.service.ApplicationHealthCorrectionService;
import ch.dvbern.oss.vacme.service.ApplicationHealthService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

/**
 * Resource fuer ApplicationProperties
 */
@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_APPLICATION_HEALTH))
@Path(VACME_WEB +"/applicationhealth/")
public class ApplicationHealthResource {

	private final ApplicationHealthService applicationHealthService;
	private final ApplicationHealthCorrectionService applicationHealthCorrectionService;

	@Inject
	public ApplicationHealthResource(
		@NonNull ApplicationHealthService applicationHealthService,
		@NonNull ApplicationHealthCorrectionService applicationHealthCorrectionService
	) {
		this.applicationHealthService = applicationHealthService;
		this.applicationHealthCorrectionService = applicationHealthCorrectionService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("inkonsistenzenTermine/{autoKorrektur}")
	@RolesAllowed(BenutzerRolleName.AS_REGISTRATION_OI)
	public List<RegistrierungTermineImpfungJax> getInkonsistenzenTermine(
		@PathParam("autoKorrektur") boolean autoKorrektur
	) {
		final List<RegistrierungTermineImpfungJax> resultList =	applicationHealthService.getInkonsistenzenTermine();
		if (!autoKorrektur) {
			return resultList;
		}
		LOG.info("VACME-HEALTH: Starte Korrektur InkonsistenzenTermine. Anzahl {}", resultList.size());
		applicationHealthCorrectionService.handleInkonsistenzenTermine();
		final List<RegistrierungTermineImpfungJax> resultKorrigiert = applicationHealthService.getInkonsistenzenTermine();
		LOG.info("VACME-HEALTH: ... Korrektur InkonsistenzenTermine beendet. {} konnten nicht automatisch korrigiert werden", resultList.size());
		return resultKorrigiert;
	}
}
