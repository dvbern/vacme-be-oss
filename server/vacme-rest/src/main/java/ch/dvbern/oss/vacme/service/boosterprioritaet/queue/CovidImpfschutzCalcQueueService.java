/*
 *
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

package ch.dvbern.oss.vacme.service.boosterprioritaet.queue;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.repo.BoosterQueueRepo;
import ch.dvbern.oss.vacme.service.impfschutz.ImpfschutzCalculationQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CovidImpfschutzCalcQueueService implements ImpfschutzCalculationQueueService {

	private final BoosterQueueRepo boosterQueueRepo;

	@Override
	public long removeAllSuccessfullQueueEntries() {
		return boosterQueueRepo.removeAllSuccessfullEntries(KrankheitIdentifier.COVID);
	}

	@Override
	public int queueRelevantRegsForImpfschutzRecalculation() {
		return boosterQueueRepo.queueRelevantRegsForImpfschutzRecalculationCovid();
	}
}
