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

package ch.dvbern.oss.vacme.service.plz;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.plz.PLZData;
import ch.dvbern.oss.vacme.entities.plz.PLZMedstat;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.repo.PLZDataRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
public class PLZService {

	private PLZImportService plzImportService;
	private PLZMedstatImportService plzMedstatImportService;
	private PLZDataRepo plzDataRepo;

	@Inject
	public PLZService(
		@NonNull PLZImportService plzImportService,
		@NonNull PLZMedstatImportService plzMedstatImportService,
		@NonNull PLZDataRepo plzDataRepo
	) {
		this.plzImportService = plzImportService;
		this.plzMedstatImportService = plzMedstatImportService;
		this.plzDataRepo = plzDataRepo;
	}

	@NonNull
	public String importIntoDatabase() {
		LOG.info("VACME-INFO: START Impofrt of PLZs was triggered");
		StopWatch stopwatch = StopWatch.createStarted();
		this.plzDataRepo.dropAll();
		List<PLZData> plzData = this.plzImportService.impportDefaultPlzDataDtoFromCSV();
		Set<PLZData> uniquePLZs = new HashSet<>(plzData);
		Validate.isTrue(uniquePLZs.size() == plzData.size(), "Duplicate values in csv");
		plzDataRepo.createAll(plzData);

		LOG.info("VACME-INFO: END PLZ Import done, took {}s", stopwatch.getTime(TimeUnit.SECONDS));
		return plzData.stream()
			.map(plzData1 -> plzData1.getPlz() + " " + plzData1.getOrtsbez())
			.collect(Collectors.joining(","));
	}

	@NonNull
	public String importMedstatIntoDatabase() {
		LOG.info("VACME-INFO: START Import of PLZs Medstat was triggered");
		StopWatch stopwatch = StopWatch.createStarted();
		this.plzDataRepo.dropAllMedstat();
		List<PLZMedstat> plzData = this.plzMedstatImportService.impportDefaultPlzDataDtoFromCSV();
		plzDataRepo.createAllMedstat(plzData);

		LOG.info("VACME-INFO: END PLZ Medstat Import done, took {}s", stopwatch.getTime(TimeUnit.SECONDS));
		return plzData.stream()
			.map(plzData1 -> plzData1.getId() + " " + plzData1.getMedstat())
			.collect(Collectors.joining(","));
	}

	@NonNull
	public List<PLZData> findOrteForPLZ(@NonNull String plz) {
		return plzDataRepo.findOrteForPLZ(plz);
	}

	@NonNull
	public Set<String> findKantoneForPLZ(@NonNull String plz) {
		List<PLZData> orteForPLZ = this.findOrteForPLZ(plz);
		return orteForPLZ.stream().map(PLZData::getKanton).collect(Collectors.toSet());
	}

	@NonNull
	public Optional<String> findMedstatForPLZ(@NonNull String plz) {
		return plzDataRepo.findMedstatForPLZ(plz).map(PLZMedstat::getMedstat);
	}

	/**
	 * prueft zu welchem Kanton die PLZ am besten zugeordnet wird. Einige PLZ koennten in mehreren Kantonen sein,
	 * Wenn dies der Fall ist wird mit Vorrang das Kantonskuerzel des aktuellen Mandanten verwendet. Sollte
	 * dies nicht vorhanden sein wird das erstbeste Kantonskuerzel aus dem Set verwendet
	 *
	 * @param plz Postlietzahl deren Kantonszugehoerigkeit ermittelt wird
	 * @return Optional mit einem String des Kantonskuerzel
	 */
	@NonNull
	public Optional<String> findBestMatchingKantonFor(@NonNull String plz) {
		Set<String> kantoneForPLZ = findKantoneForPLZ(plz);
		if (kantoneForPLZ.contains(MandantUtil.getMandant().name())) {
			return Optional.of(MandantUtil.getMandant().name());
		}
		return kantoneForPLZ.stream().findFirst();
	}
}
