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

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumStatJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumTagesReportDetailAusstehendJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumTagesReportDetailJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumTagesReportJax;
import ch.dvbern.oss.vacme.service.ImpfslotService;
import ch.dvbern.oss.vacme.service.OrtDerImpfungService;
import ch.dvbern.oss.vacme.service.StatsService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_REGISTRATION_OI;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.KT_MEDIZINISCHER_REPORTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_LOGISTIK_REPORTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_ORT_VERWALTER;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@Path(VACME_WEB + "/stat/")
@RolesAllowed({ AS_REGISTRATION_OI, OI_ORT_VERWALTER, OI_IMPFVERANTWORTUNG })
@Tags(@Tag(name = OpenApiConst.TAG_STAT))
public class StatsResource {

	private final OrtDerImpfungService ortDerImpfungService;
	private final StatsService statsService;
	private final ImpfslotService impfslotService;

	@Inject
	public StatsResource(
		@NonNull OrtDerImpfungService ortDerImpfungService,
		@NonNull StatsService statsService,
		@NonNull ImpfslotService impfslotService
	) {
		this.ortDerImpfungService = ortDerImpfungService;
		this.statsService = statsService;
		this.impfslotService = impfslotService;
	}

	@GET()
	@Produces(MediaType.APPLICATION_JSON)
	@Path("impfzentrum/{odiId}")
	@RolesAllowed({ AS_REGISTRATION_OI, OI_ORT_VERWALTER, OI_IMPFVERANTWORTUNG })
	public ImpfzentrumStatJax getImpfzentrumStatistics(
		@NonNull @PathParam("odiId") UUID odiId,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("vonDate") LocalDate vonDate,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("bisDate") LocalDate bisDate
	) {
		final OrtDerImpfung ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(odiId));
		return statsService.getImpfzentrumStatistics(ortDerImpfung, vonDate, bisDate);
	}

	@GET()
	@Produces(MediaType.APPLICATION_JSON)
	@Path("anzahl-registrierungen")
	@RolesAllowed({ AS_REGISTRATION_OI, OI_ORT_VERWALTER, OI_IMPFVERANTWORTUNG })
	public long getAnzahlRegistrierungen(
	) {
		return statsService.getAnzahlRegistrierungen();
	}

	@GET()
	@Produces(MediaType.APPLICATION_JSON)
	@Path("tages-report/{odiId}")
	@RolesAllowed({ OI_LOGISTIK_REPORTER, KT_MEDIZINISCHER_REPORTER })
	public ImpfzentrumTagesReportJax getOdiTagesReport(
		@NonNull @PathParam("odiId") UUID odiId,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("datum") LocalDate datum
	) {
		final OrtDerImpfung ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(odiId));
		return statsService.getOdiTagesReport(ortDerImpfung, datum);
	}

	@GET()
	@Produces(MediaType.APPLICATION_JSON)
	@Path("tages-report-detail/{odiId}")
	@RolesAllowed({ OI_LOGISTIK_REPORTER, KT_MEDIZINISCHER_REPORTER })
	@Timed(name = "getOdiTagesReportDetail_Timer", description = "A measure of how long it takes to perform the primality test.", unit = MetricUnits.MILLISECONDS)
	public ImpfzentrumTagesReportDetailJax getOdiTagesReportDetail(
		@NonNull @PathParam("odiId") UUID odiId,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("datum") LocalDate datum
	) {
		StopWatch started = StopWatch.createStarted();
		final OrtDerImpfung ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(odiId));
		ImpfzentrumTagesReportDetailJax odiTagesReportDetail = statsService.getOdiTagesReportDetailFast(ortDerImpfung,
			datum);

		LOG.debug("Got Tagesstatistik, took {}ms", started.getTime(TimeUnit.MILLISECONDS));

		return odiTagesReportDetail;
	}

	@GET()
	@Produces(MediaType.APPLICATION_JSON)
	@Path("tages-report-detail-ausstehend/{slotId}")
	@RolesAllowed({ OI_LOGISTIK_REPORTER, KT_MEDIZINISCHER_REPORTER })
	public ImpfzentrumTagesReportDetailAusstehendJax getOdiTagesReportDetailAusstehendeCodes(
		@NonNull @PathParam("slotId") UUID slotId
	) {
		final Impfslot slot = impfslotService.getById(Impfslot.toId(slotId));
		return statsService.getOdiTagesReportDetailAusstehendeCodes(slot);
	}
}
