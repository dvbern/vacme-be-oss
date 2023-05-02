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
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.registration.Sprache;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolleName;
import ch.dvbern.oss.vacme.enums.FileNameEnum;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.reports.abrechnung.ReportAbrechnungServiceBean;
import ch.dvbern.oss.vacme.reports.abrechnungZH.ReportAbrechnungZHServiceBean;
import ch.dvbern.oss.vacme.reports.reportingImpfungen.ReportingImpfungenReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingKantonKantonsarzt.ReportingKantonReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingKantonKantonsarzt.ReportingKantonsarztReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingOdiImpfungenTerminbuchungen.ReportingOdiImpfungenReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingOdiImpfungenTerminbuchungen.ReportingOdiTerminbuchungenReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingOdis.ReportingOdisReportServiceBean;
import ch.dvbern.oss.vacme.reports.reportingTerminslots.ReportingTerminslotsReportServiceBean;
import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import ch.dvbern.oss.vacme.util.RestUtil;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_REPORTS_SYNC))
@Path(VACME_WEB + "/reports/sync")
public class ReportResourceSync {

	private final ReportingKantonReportServiceBean reportingKantonServiceBean;
	private final ReportingTerminslotsReportServiceBean reportingTerminslotsService;
	private final ReportingOdisReportServiceBean reportingOdisService;
	private final ReportAbrechnungServiceBean abrechnungServiceBean;
	private final ReportAbrechnungZHServiceBean abrechnungZHServiceBean;
	private final ReportingKantonsarztReportServiceBean reportingKantonsarztReportServiceBean;
	private final ReportingImpfungenReportServiceBean reportingImpfungenReportServiceBean;
	private final ReportingOdiImpfungenReportServiceBean reportingOdiImpfungenReportServiceBean;
	private final ReportingOdiTerminbuchungenReportServiceBean reportingOdiTerminbuchungenReportServiceBean;
	private final UserPrincipal userPrincipal;

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("kanton/statistik")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateKantonCSV")
	public Response generateKantonCSVSync() {
		try {
			StreamingOutput streamingOutput = reportingKantonServiceBean.generateStatisticsExport();
			return Response.ok(streamingOutput).build();
		} catch (Exception e) {
			LOG.error("Could not generate Excel 'Reporting Kanton Statistik CSV'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("terminslots/statistik")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateReportingTerminslotsCSV")
	public Response generateReportingTerminslotsCSVSync() {
		try {
			StreamingOutput streamingOutput = reportingTerminslotsService.generateStatisticsExport();
			return Response.ok(streamingOutput).build();
		} catch (Exception e) {
			LOG.error("Could not generate CSV 'Reporting Terminslot Statistik CSV'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("odis/statistik")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateReportingOdisCSV")
	public Response generateReportingOdisCSVSync() {
		try {
			LOG.info("Starting export of ODI CSV");

			final byte[] content = reportingOdisService.generateStatisticsExport();
			final Response response = createDownloadResponse(FileNameEnum.ODI_REPORT_CSV, content);
			LOG.info("... Reporting ODI CSV beendet");
			return response;
		} catch (Exception e) {
			LOG.error("Could not generate CSV 'Reporting ODI CSV'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("kantonsarzt/statistik")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateReportingKantonsarztCSV")
	public Response generateReportingKantonsarztCSVSync() {
		try {
			StreamingOutput streamingOutput = reportingKantonsarztReportServiceBean.generateStatisticsExport();
			return Response.ok(streamingOutput).build();
		} catch (Exception e) {
			LOG.error("Could not generate Excel 'Reporting Kantonsarzt Statistik CSV'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("impfungen/statistik")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateReportingImpfungenCSV")
	public Response generateReportingImpfungenCSVSync() {
		try {
			StreamingOutput streamingOutput = reportingImpfungenReportServiceBean.generateStatisticsExport();
			return Response.ok(streamingOutput).build();
		} catch (Exception e) {
			LOG.error("Could not generate Excel 'Reporting Impfungen Statistik CSV'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("odiimpfungen/statistik/{language}")
	@RolesAllowed(BenutzerRolleName.OI_IMPFVERANTWORTUNG)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateReportingOdiImpfungenCSV")
	public Response generateReportingOdiImpfungenCSVSync(
		@NonNull @NotNull @PathParam("language") String language
	) {
		try {
			final Locale locale = Sprache.valueOf(language.toUpperCase(Locale.ROOT)).getLocale();
			final byte[] content = reportingOdiImpfungenReportServiceBean.generateExcelReportOdiImpfungen(
				locale,
				userPrincipal.getBenutzerOrThrowException().toId()
			);
			return createDownloadResponse(FileNameEnum.ODI_IMPFUNGEN, content);
		} catch (Exception e) {
			if (e instanceof AppValidationException) {
				throw e;
			}
			LOG.error("Could not generate Excel 'Reporting ODI-Impfungen Statistik CSV'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("oditerminbuchungen/statistik/{language}")
	@RolesAllowed(BenutzerRolleName.OI_LOGISTIK_REPORTER)
	@APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
	@Operation(description = "Sync version of generateReportingOdiTerminbuchungenCSV")
	public Response generateReportingOdiTerminbuchungenCSVSync(
		@NonNull @NotNull @PathParam("language") String language
	) {
		try {
			final Locale locale = Sprache.valueOf(language.toUpperCase(Locale.ROOT)).getLocale();
			Benutzer requestingUser = this.userPrincipal.getBenutzerOrThrowException();
			final byte[] content = reportingOdiTerminbuchungenReportServiceBean.generateExcelReportOdiTerminbuchungen(locale,
				requestingUser.toId());
			return createDownloadResponse(FileNameEnum.ODI_TERMINBUCHUNGEN, content);
		} catch (Exception e) {
			if (e instanceof AppValidationException) {
				throw e;
			}
			LOG.error("Could not generate Excel 'Reporting ODI-Terminbuchungen Statistik CSV'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("abrechnung/{language}")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateExcelReportAbrechnungSync")
	public Response generateExcelReportAbrechnungSync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		try {
			final Locale locale = Sprache.valueOf(language.toUpperCase(Locale.ROOT)).getLocale();
			final byte[] content = abrechnungServiceBean.generateExcelReportAbrechnung(locale, dateVon, dateBis);
			return createDownloadResponse(FileNameEnum.ABRECHNUNG, content);
		} catch (Exception e) {
			LOG.error("Could not generate Excel 'Abrechnung'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("abrechnung-erwachsen/{language}")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateExcelReportAbrechnungErwachsen")
	public Response generateExcelReportAbrechnungErwachsenSync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		try {
			final Locale locale = Sprache.valueOf(language.toUpperCase(Locale.ROOT)).getLocale();
			final byte[] content =
				abrechnungServiceBean.generateExcelReportAbrechnungErwachsen(locale, dateVon, dateBis);
			return createDownloadResponse(FileNameEnum.ABRECHNUNG_ERWACHSEN, content);
		} catch (Exception e) {
			LOG.error("Could not generate Excel 'AbrechnungErwachsen'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("abrechnung-kind/{language}")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateExcelReportAbrechnungKind")
	public Response generateExcelReportAbrechnungKindSync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		try {
			final Locale locale = Sprache.valueOf(language.toUpperCase(Locale.ROOT)).getLocale();
			final byte[] content = abrechnungServiceBean.generateExcelReportAbrechnungKind(locale, dateVon, dateBis);
			return createDownloadResponse(FileNameEnum.ABRECHNUNG_KIND, content);
		} catch (Exception e) {
			LOG.error("Could not generate Excel 'AbrechnungKind'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("abrechnung-zh/{language}")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateExcelReportAbrechnungZH")
	public Response generateExcelReportAbrechnungZHSync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		try {
			final Locale locale = Sprache.valueOf(language.toUpperCase(Locale.ROOT)).getLocale();
			final byte[] content = abrechnungZHServiceBean.generateExcelReportAbrechnung(locale, dateVon, dateBis);
			return createDownloadResponse(FileNameEnum.ABRECHNUNG_ZH, content);
		} catch (Exception e) {
			LOG.error("Could not generate Excel 'AbrechnungZH'", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = Constants.LOADBALANCER_TIMEOUT_SECONDS)
	@POST
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("abrechnung-zh-kind/{language}")
	@RolesAllowed(BenutzerRolleName.KT_MEDIZINISCHER_REPORTER)
	@Schema(type = SchemaType.STRING, format = "binary")
	@Operation(hidden = true, description = "Sync version of generateExcelReportAbrechnungZHKind")
	public Response generateExcelReportAbrechnungZHKindSync(
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateVon")
			LocalDate dateVon,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("dateBis")
			LocalDate dateBis,
		@NonNull @NotNull @PathParam("language") String language
	) {
		try {
			final Locale locale = Sprache.valueOf(language.toUpperCase(Locale.ROOT)).getLocale();
			final byte[] content = abrechnungZHServiceBean.generateExcelReportAbrechnungKind(locale, dateVon, dateBis);
			return createDownloadResponse(FileNameEnum.ABRECHNUNG_ZH_KIND, content);
		} catch (Exception e) {
			LOG.error("Could not generate Excel 'AbrechnungZHKind'", e);
			return Response.serverError().build();
		}
	}

	@NonNull
	private Response createDownloadResponse(
		@NonNull FileNameEnum reportFileName,
		byte[] content
	) {
		return RestUtil.createDownloadResponse(ServerMessageUtil.translateEnumValue(reportFileName, Locale.GERMAN),
			content, MimeType.APPLICATION_OCTET_STREAM);

	}
}

