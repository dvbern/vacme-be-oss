/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service.d3api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.base.ApplicationProperty;
import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.service.ApplicationPropertyService;
import ch.dvbern.oss.vacme.shared.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import static ch.dvbern.oss.vacme.util.TimingUtil.calculateGenerationSpeed;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ArchivierungService {

	private final ApplicationPropertyService applicationPropertyService;
	private final ImpfungRepo impfungRepo;
	private final D3ApiService d3ApiService;

	public void archive() {
		StopWatch stopWatch = StopWatch.createStarted();
		int successCounter = 0;
		int totalCounter = 0;
		List<UUID> impfungIdsToArchive = new ArrayList<>();

		try {
			impfungIdsToArchive = impfungRepo.getImpfungenZuArchivieren(getArchivierungBatchSize());
			LOG.info("VACME-ARCHIVIERUNG: Starting to archive a batch of up to {} Impfungen. Found {}",
				getArchivierungBatchSize(), impfungIdsToArchive.size());

			for (UUID impfungId : impfungIdsToArchive) {
				try {
					LOG.debug("VACME-ARCHIVIERUNG: Archiviere Impfung {}",  impfungId);
					boolean success = d3ApiService.saveInD3(Impfung.toId(impfungId));
					if (success) {
						successCounter++;
					}
				} finally {
					totalCounter++;
				}
			}

		} finally {
			stopWatch.stop();
			if (!impfungIdsToArchive.isEmpty() || stopWatch.getTime(TimeUnit.MILLISECONDS) > Long.parseLong(Constants.DB_QUERY_SLOW_THRESHOLD)) {
				LOG.info(
					"VACME-ARCHIVIERUNG: Archivierung beendet. Es wurden {} Impfungen von total {} "
						+ "in {}ms archiviert. {} ms/stk",
					successCounter, impfungIdsToArchive.size(), stopWatch.getTime(TimeUnit.MILLISECONDS),
					calculateGenerationSpeed(totalCounter,
						stopWatch.getTime(TimeUnit.MILLISECONDS)));
			}
		}
	}

	private long getArchivierungBatchSize() {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(ApplicationPropertyKey.ARCHIVIERUNG_JOB_BATCH_SIZE);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Long.parseLong(applicationProperty.getValue()))
			.orElseGet(() -> 0L);
	}
}
