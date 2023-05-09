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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.oss.vacme.entities.impfen.Krankheit;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.base.SettingsJax;
import ch.dvbern.oss.vacme.jax.registration.KrankheitSettingsJax;
import ch.dvbern.oss.vacme.service.KrankheitService;
import ch.dvbern.oss.vacme.service.SettingsService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_WEB;

/**
 * Resource fuer Settings aus Web
 */
@ApplicationScoped
@Transactional
@Slf4j
@Tags(@Tag(name = OpenApiConst.TAG_SETTINGS))
@Path(VACME_WEB + "/settings")
public class SettingsResource {

	private final SettingsService settingsService;
	private final KrankheitService krankheitService;

	@Inject
	public SettingsResource(
		@NonNull SettingsService settingsService,
		@NonNull KrankheitService krankheitService
	) {
		this.settingsService = settingsService;
		this.krankheitService = krankheitService;
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("all")
	@PermitAll
	public SettingsJax getSettings() {
		return settingsService.getSettings();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/krankheiten")
	@PermitAll
	public List<KrankheitSettingsJax> getKrankheitSettings() {
		return Arrays.stream(KrankheitIdentifier.values())
			.map(krankheitIdentifier -> {
				Krankheit krankheit = krankheitService.getByIdentifier(krankheitIdentifier);
				return KrankheitSettingsJax.from(krankheitIdentifier, krankheit);
			})
			.collect(Collectors.toList());
	}
}
