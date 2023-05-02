/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.terminbuchung.OdiNoFreieTermine;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.repo.OdiNoFreieTermineRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OdiNoFreieTermineService {

	private final OdiNoFreieTermineRepo odiNoFreieTermineRepo;

	public OdiNoFreieTermine getOrCreateByOdiAndKrankheit(@NonNull OrtDerImpfung odi, @NonNull KrankheitIdentifier krankheitIdentifier) {
		final Optional<OdiNoFreieTermine> nextFreiOptional =
			odiNoFreieTermineRepo.getByOdiAndKrankheit(odi, krankheitIdentifier);
		if (nextFreiOptional.isEmpty()) {
			OdiNoFreieTermine nextFreieTermine = new OdiNoFreieTermine(krankheitIdentifier, odi);
			odiNoFreieTermineRepo.create(nextFreieTermine);
			return nextFreieTermine;
		}
		return nextFreiOptional.get();
	}

	@NonNull
	public OdiNoFreieTermine update(@NonNull OdiNoFreieTermine odiNextFreieTermine) {
		return odiNoFreieTermineRepo.update(odiNextFreieTermine);
	}
}
