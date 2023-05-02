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
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.oss.vacme.entities.impfen.Krankenkasse;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import ch.dvbern.oss.vacme.jax.registration.ImpfstoffJax;
import ch.dvbern.oss.vacme.service.ImpfstoffService;
import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import static ch.dvbern.oss.vacme.rest.auth.ClientApplications.VACME_INITIALREG;

@ApplicationScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Tags(@Tag(name = OpenApiConst.TAG_STAMMDATEN))
@Path(VACME_INITIALREG + "/stammdaten/")
public class StammdatenResourceReg {

	private final ImpfstoffService impfstoffService;


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/krankenkassen")
	@PermitAll
	public Set<Krankenkasse> getKrankenkassen() {
		return Sets.newHashSet(Krankenkasse.values());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/impfstoffeForExternGeimpft/{krankheit}")
	@PermitAll
	public List<ImpfstoffJax> getAlleImpfstoffeForExternGeimpft(
		@NonNull @NotNull @PathParam("krankheit") KrankheitIdentifier krankheit
	) {
		return impfstoffService.getImpfstoffeForKrankheitAndStatus(Set.of(
			ZulassungsStatus.EMPFOHLEN,
			ZulassungsStatus.ZUGELASSEN,
			ZulassungsStatus.EXTERN_ZUGELASSEN,
			ZulassungsStatus.NICHT_WHO_ZUGELASSEN),
			krankheit
		);
	}
}
