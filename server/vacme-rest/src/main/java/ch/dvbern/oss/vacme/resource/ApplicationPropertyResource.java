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
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ApplicationProperty;
import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.jax.base.ApplicationPropertyJax;
import ch.dvbern.oss.vacme.service.ApplicationPropertyService;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_BENUTZER_VERWALTER;
import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.AS_REGISTRATION_OI;

/**
 * Resource fuer ApplicationProperties
 */
@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_PROPERTIES))
@Path("/properties")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ApplicationPropertyResource {

	private final ApplicationPropertyService applicationPropertyService;
	private final VacmeSettingsService vacmeSettingsService;
	private final UserPrincipal userPrincipal;


	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("find/{key}")
	@PermitAll
	public ApplicationPropertyJax getApplicationProperty(
		@NonNull @NotNull @PathParam("key") String key
	) {
		final ApplicationPropertyKey applicationPropertyKey = ApplicationPropertyKey.valueOf(key);
		final ApplicationProperty applicationProperty = applicationPropertyService.getByKey(applicationPropertyKey);
		return ApplicationPropertyJax.from(applicationProperty);
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("all")
	@PermitAll
	public List<ApplicationPropertyJax> getAllApplicationProperties() {
		return applicationPropertyService.findAll()
			.stream()
			.map(ApplicationPropertyJax::from)
			.collect(Collectors.toList());
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("save")
	@RolesAllowed({ AS_BENUTZER_VERWALTER, AS_REGISTRATION_OI})
	public Response update(ApplicationPropertyJax applicationPropertyJax) {
		applicationPropertyService.save(applicationPropertyJax.getName(), applicationPropertyJax.getValue());
		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@Path("freigeben")
	@RolesAllowed({ AS_BENUTZER_VERWALTER, AS_REGISTRATION_OI})
	public Response impfgruppeFreigeben(List<String> impfgruppen) {
		var alteImpfgruppe = Arrays.asList(applicationPropertyService.getByKey(ApplicationPropertyKey.PRIO_FREIGEGEBEN_BIS).getValue().split("-"));
		applicationPropertyService.impfgruppeFreigeben(impfgruppen);
		LOG.info("VACME-INFO: Die Impfgruppe(n) {} wurde(n) freigegeben durch {}. Vorherige Gruppe(n) war(en) {}", impfgruppen,
			this.userPrincipal.getBenutzerOrThrowException().getBenutzername(), alteImpfgruppe);
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("isZertifikatEnabled")
	@RolesAllowed({ AS_BENUTZER_VERWALTER, AS_REGISTRATION_OI})
	public boolean isZertifikatEnabled() {
		// This does not use the cached value
		return vacmeSettingsService.isZertifikatEnabled();
	}
}
