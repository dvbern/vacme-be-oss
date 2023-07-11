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
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.service.TestdataCreationService;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_TESTDATA_CREATION))
@Path(VACME_WEB + "/testdata")
public class TestdataCreationResource {

	private final TestdataCreationService testdataCreationService;
	private final VacmeSettingsService vacmeSettingsService;

	@TransactionConfiguration(timeout = 6000000)
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("createRegistrierungen/{anzahl}")
	@PermitAll // Fuer jede Funktion erlaubt, jedoch nicht auf Stufe Prod
	@Operation(hidden = true)
	public Response createRegistrierungen(
		@PathParam("anzahl") int anzahl
	) {
		try {
			isTestdataCreationAllowed();
			LOG.info("Creating Testdata...");
			final List<String> result = testdataCreationService.createRegistrierungen(anzahl);
			LOG.info("...Testdata beendet");
			return Response.ok(result).build();
		} catch (Exception e) {
			LOG.error("Testdaten konnten nicht erstellt werden", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = 6000000)
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("createRegistrierungenMitTermin/{anzahl}/{odiname}/{datumTermin1}/{datumTermin2}")
	@PermitAll // Fuer jede Funktion erlaubt, jedoch nicht auf Stufe Prod
	@Operation(hidden = true)
	public Response createRegistrierungenMitTermin(
		@PathParam("anzahl") int anzahl,
		@PathParam("odiname") String odiname,
		@PathParam("datumTermin1") LocalDate datumTermin1,
		@PathParam("datumTermin2") LocalDate datumTermin2
	) {
		try {
			isTestdataCreationAllowed();
			LOG.info("Creating Testdata...");
			final List<String> result =
				testdataCreationService.createRegistrierungenMitTermin(anzahl, odiname, datumTermin1, datumTermin2);
			LOG.info("...Testdata beendet");
			return Response.ok(result).build();
		} catch (Exception e) {
			LOG.error("Testdaten konnten nicht erstellt werden", e);
			return Response.serverError().build();
		}
	}

	@TransactionConfiguration(timeout = 6000000)
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("createRegistrierungenForOnboarding/{anzahl}/{odiname}/{datumTermin1}/{datumTermin2}")
	@PermitAll // Fuer jede Funktion erlaubt, jedoch nicht auf Stufe Prod
	@Operation(hidden = true)
	public Response createRegistrierungenForOnboarding(
		@PathParam("anzahl") int anzahl,
		@PathParam("odiname") String odiname,
		@PathParam("datumTermin1") LocalDate datumTermin1,
		@PathParam("datumTermin2") LocalDate datumTermin2
	) {
		try {
			isTestdataCreationAllowed();
			LOG.info("Creating Testdata...");
			final List<String> result =
				testdataCreationService.createRegistrierungenForOnboarding(anzahl, odiname, datumTermin1,
					datumTermin2);
			LOG.info("...Testdata beendet");
			return Response.ok(result).build();
		} catch (Exception e) {
			LOG.error("Testdaten konnten nicht erstellt werden", e);
			return Response.serverError().build();
		}
	}

	private void isTestdataCreationAllowed() throws IllegalAccessException {
		if (!vacmeSettingsService.isMigrationTestDataEnabled() || vacmeSettingsService.isStufeProd()) {
			throw new IllegalAccessException("Testdaten nicht verfuegbar in Produktion");
		}
	}

	@POST
	@Operation(
		summary = "Generiert termine for all odis fuer den gegebenen  monat und das gegebene jahr",
		hidden = true
	)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("createTermineForAllOdis/{year}/{month}")
	@PermitAll()
	@Transactional(TxType.NOT_SUPPORTED)
	public Response createTermineForAllOdis(
		@Min(2022) @NotNull @PathParam("year") int year,
		@Min(1) @Max(12) @NotNull @PathParam("month") int month

	) throws IllegalAccessException {
		isTestdataCreationAllowed();
		// TODO Affenpocken currently only generates COVID Slots
		testdataCreationService.generateImpfslotsForAllOdi(year, month);
		return Response.ok().build();
	}
}
