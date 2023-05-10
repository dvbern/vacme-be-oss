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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.jax.registration.RegistrierungSearchResponseWithRegnummerJax;
import ch.dvbern.oss.vacme.service.PersonalienSucheService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_BENUTZER_VERWALTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_DOKUMENTATION;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_IMPFVERANTWORTUNG;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.OI_KONTROLLE;
import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_PERSONALIEN_SUCHE))
@Path(VACME_WEB + "/personalien-suche")
@AllArgsConstructor(onConstructor_ = @Inject)
public class PersonalienSucheResource {

	private final PersonalienSucheService sucheService;
	private final UserPrincipal userPrincipal;


	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("suchen/{vorname}/{name}/{geburtsdatum}")
	@RolesAllowed({ OI_KONTROLLE, OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, AS_BENUTZER_VERWALTER })
	/**
	 * Diese Suche ist eingeschraenkt auf Personen, die in meinen ODIs einen Termin hat
	 */
	public List<RegistrierungSearchResponseWithRegnummerJax> suchen(
		@NonNull @NotNull @PathParam("vorname") String vorname,
		@NonNull @NotNull @PathParam("name") String name,
		@NonNull @NotNull @PathParam("geburtsdatum") Date geburtsdatum
	) {
		Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();

		return sucheService.suchenFuerODI(vorname, name, geburtsdatum, currentBenutzer)
			.stream()
			.map(RegistrierungSearchResponseWithRegnummerJax::new)
			.collect(Collectors.toList());
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("suchen/{vorname}/{name}/{geburtsdatum}/{uvci}")
	@RolesAllowed({ OI_KONTROLLE, OI_DOKUMENTATION, OI_IMPFVERANTWORTUNG, AS_BENUTZER_VERWALTER })
	/**
	 * Mit dieser Suche konnen beliebige Personen gefunden werden, man braucht aber die uvci Nummer
	 */
	public List<RegistrierungSearchResponseWithRegnummerJax> suchenUvci(
		@NonNull @NotNull @PathParam("vorname") String vorname,
		@NonNull @NotNull @PathParam("name") String name,
		@NonNull @NotNull @PathParam("geburtsdatum") Date geburtsdatum,
		@NonNull @NotNull @PathParam("uvci") String uvci
	) {

		return sucheService.suchen(vorname, name, geburtsdatum, uvci)
			.stream()
			.map(RegistrierungSearchResponseWithRegnummerJax::new)
			.collect(Collectors.toList());
	}
}
