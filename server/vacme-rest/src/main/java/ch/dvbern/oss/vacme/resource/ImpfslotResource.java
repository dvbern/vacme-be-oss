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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.jax.impfslot.ImpfslotDisplayDayJax;
import ch.dvbern.oss.vacme.jax.impfslot.ImpfslotDisplayJax;
import ch.dvbern.oss.vacme.jax.impfslot.ImpfslotValidationJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfslotJax;
import ch.dvbern.oss.vacme.rest.auth.Authorizer;
import ch.dvbern.oss.vacme.service.ImpfslotService;
import ch.dvbern.oss.vacme.service.OrtDerImpfungService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_REGISTRATION_OI;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_ORT_VERWALTER;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_IMPFSLOT))
@Path(VACME_WEB + "/impfslot")
public class ImpfslotResource {

	private final ImpfslotService impfslotService;
	private final OrtDerImpfungService ortDerImpfungService;
	private Authorizer authorizer;

	@Inject
	public ImpfslotResource(
		@NonNull ImpfslotService impfslotService,
		@NonNull OrtDerImpfungService ortDerImpfungService,
		@NonNull Authorizer authorizer
	) {
		this.impfslotService = impfslotService;
		this.ortDerImpfungService = ortDerImpfungService;
		this.authorizer = authorizer;
	}

	@GET
	@Operation(summary = "Liefert Impfslot anhand der ID")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("findByID/{impfslotId}")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	public ImpfslotJax getImpfslotByID(
		@NonNull @NotNull @PathParam("impfslotId") UUID impfslotId
	) {
		var impfslot = impfslotService.getById(Impfslot.toId(impfslotId));
		authorizer.checkReadAuthorization(impfslot.getOrtDerImpfung());
		return new ImpfslotJax(impfslot);
	}

	@PUT
	@Operation(summary = "Aktualisiert die Kapazitaeten von den Impfslots")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("/aktualisieren")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	public Response updateImpfslot(
		@NonNull @NotNull @Valid List<ImpfslotDisplayJax> impfslotDisplayJaxList
	) {
		for (ImpfslotDisplayJax impfslotDisplayJax : impfslotDisplayJaxList) {
			ID<Impfslot> impfslotID = Impfslot.toId(impfslotDisplayJax.getId());
			Impfslot akImpfslot = impfslotService.getById(impfslotID);
			authorizer.checkUpdateAuthorization(akImpfslot.getOrtDerImpfung());
			impfslotService.updateImpfslot(akImpfslot, impfslotDisplayJax.getUpdateEntityConsumer());
		}
		return Response.ok().build();
	}

	@GET
	@Operation(summary =
		"Validierung genuegend Impfslots fuer zweite Impfung eines ODI im Zeitraum von/bis "
			+ "(kapazitaetErsteImpfung <= kapazitaetZweiteImpfung 28 Tage spaeter")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/validate/{ortDerImpfungId}")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	public List<ImpfslotValidationJax> validateImpfslotsByOdiBetween(
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("vonDate")
			LocalDate vonDate,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("bisDate")
			LocalDate bisDate
	) {
		var result = impfslotService.validateImpfslotsByOdi(ortDerImpfungId, vonDate, bisDate);
		return result;
	}

	@GET
	@Operation(summary = "Gibt Liste der Impfslot's eines ODI und Zeitraum von/bis")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/findByInterval/{ortDerImpfungId}")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	public List<ImpfslotDisplayDayJax> getImpfslotByODIBetween(
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("vonDate")
			LocalDate vonDate,
		@NonNull @NotNull @Parameter(schema = @Schema(format = OpenApiConst.Format.DATE)) @QueryParam("bisDate")
			LocalDate bisDate
	) {
		var ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(ortDerImpfungId));
		authorizer.checkReadAuthorization(ortDerImpfung);
		var impfslotList = impfslotService.findAllImpfslots(ortDerImpfung, vonDate, bisDate);
		return createImpfslotDisplayDayJaxList(impfslotList);
	}

	@POST
	@Operation(summary = "Generiert alle Impfslot fuer den Monat")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/generate/{ortDerImpfungId}/{month}/{year}")
	@RolesAllowed({ OI_ORT_VERWALTER, AS_REGISTRATION_OI })
	public Response generateImpfslot(
		@NonNull @NotNull @PathParam("ortDerImpfungId") UUID ortDerImpfungId,
		@NonNull @NotNull @PathParam("month") Integer month,
		@NonNull @NotNull @PathParam("year") Integer year
	) {
		OrtDerImpfung ortDerImpfung = ortDerImpfungService.getById(OrtDerImpfung.toId(ortDerImpfungId));
		authorizer.checkReadAuthorization(ortDerImpfung);
		LocalDateTime startDay = LocalDateTime.of(year, month, 1, 0, 0);
		impfslotService.createEmptyImpfslots(ortDerImpfung, startDay, 1);
		return Response.ok().build();
	}

	private List<ImpfslotDisplayDayJax> createImpfslotDisplayDayJaxList(List<Impfslot> impfslots) {
		var localDateListMap = impfslots.stream()
			.collect(Collectors.groupingBy(impfslot -> impfslot.getZeitfenster().getVon().toLocalDate()));
		return localDateListMap.entrySet()
			.stream()
			.map(ImpfslotDisplayDayJax::of)
			.sorted(Comparator.comparing(ImpfslotDisplayDayJax::getDay))
			.collect(Collectors.toList());
	}
}
