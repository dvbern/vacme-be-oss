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

package ch.dvbern.oss.vacme.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.ApplicationProperty;
import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.repo.UmfrageRepo;
import ch.dvbern.oss.vacme.util.PrioritaetUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class StammdatenService {

	private final ApplicationPropertyService applicationPropertyService;
	private final RegistrierungRepo registrierungRepo;
	private final UmfrageRepo umfrageRepo;

	@Inject
	public StammdatenService(
		@NonNull RegistrierungRepo registrierungRepo,
		@NonNull ApplicationPropertyService applicationPropertyService,
		@NonNull UmfrageRepo umfrageRepo
	) {
		this.registrierungRepo = registrierungRepo;
		this.applicationPropertyService = applicationPropertyService;
		this.umfrageRepo = umfrageRepo;
	}

	@NonNull
	public String createUniqueRegistrierungsnummer() {
		return registrierungRepo.getNextRegistrierungnummer();
	}

	@NonNull
	public String createUniqueUmfrageCode() {
		return umfrageRepo.getNextUmfrageCode();
	}

	@NonNull
	public Prioritaet calculatePrioritaet(@NonNull Fragebogen fragebogen) {
		return PrioritaetUtil.calculatePrioritaet(fragebogen);
	}

	public boolean istPrioritaetFreigeschaltet(@NonNull Prioritaet prioritaet) {
		final Optional<ApplicationProperty> property = applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.PRIO_FREIGEGEBEN_BIS);
		if (property.isEmpty()) {
			return false;
		}
		final List<Prioritaet> prioritaetFreigeschaltetBis = Arrays.stream(property.get().getValue().split("-")).map(Prioritaet::valueOf).collect(Collectors.toList());
		return prioritaetFreigeschaltetBis.contains(prioritaet);
	}
}
