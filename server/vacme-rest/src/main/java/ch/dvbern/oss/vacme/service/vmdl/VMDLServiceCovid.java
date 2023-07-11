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

package ch.dvbern.oss.vacme.service.vmdl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLDeleteJax;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadCovidJax;
import ch.dvbern.oss.vacme.rest_client.vmdl.VMDLRestClientServiceCovid;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadBaseJax.MEDSTAT_AUSLAND;
import static ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadBaseJax.MEDSTAT_UNKNOWN;

@ApplicationScoped
@Slf4j
public class VMDLServiceCovid extends VMDLServiceAbstract<VMDLUploadCovidJax> {

	@Inject
	@RestClient
	VMDLRestClientServiceCovid vmdlRestClientServiceCovid;

	@Inject
	VacmeSettingsService vacmeSettingsService;

	@Override
	protected void performAdditionalMappings(@NonNull List<VMDLUploadCovidJax> vmdlUploadJax) {
		StopWatch stopwWatchMapping = StopWatch.createStarted();
		this.setMedStatBasedOnPlzForAllEntries(vmdlUploadJax);
		LOG.info("VACME-VMDL: Mapping Time Medstat Time: {}ms for {}", stopwWatchMapping.getTime(TimeUnit.MILLISECONDS), vmdlUploadJax.size());
	}

	/**
	 * - People living in CH = Medstat Code
	 * - People living in Liechtenstein = LIE
	 * - For people living outside CH and LIE = XX99
	 * - Unknown = 0000
	 */
	private void setMedStatBasedOnPlzForAllEntries(@NonNull List<VMDLUploadCovidJax> vmdlUploadCovidJaxList) {
		vmdlUploadCovidJaxList.forEach(vmdlUploadJax -> {
			Optional<String> medStatOptional = plzCacheService.findBestMatchingMedStatFor(vmdlUploadJax.getPlz());
			if (medStatOptional.isPresent()) {
				// Schweiz und Liechtenstein, PLZ bekannt (FL hat den Code LIE drin)
				vmdlUploadJax.setMedstat(medStatOptional.get());
			} else if (vmdlUploadJax.isAusland()) {
				// Ausland
				vmdlUploadJax.setMedstat(MEDSTAT_AUSLAND);
			} else {
				// unbekannt
				vmdlUploadJax.setMedstat(MEDSTAT_UNKNOWN);
			}
		});
	}

	@Override
	public void uploadData(@NonNull List<VMDLUploadCovidJax> vmdlUploadJax) {
		vmdlRestClientServiceCovid.uploadData(vmdlUploadJax);
	}

	@Override
	@NonNull
	protected List<VMDLUploadCovidJax> findVMDLPendenteImpfungen() {
		return findVMDLPendenteCovidImpfungen();
	}

	@NonNull
	private List<VMDLUploadCovidJax> findVMDLPendenteCovidImpfungen() {
		if (vacmeSettingsService.getVmdlCovidRun3QueriesSettingEnabled()) {
			return vmdlRepo.getVMDLPendenteImpfungen3QueriesCovid(getUploadChunkLimit(), getReportingUnitID());
		}
		return vmdlRepo.getVMDLPendenteImpfungen2QueriesCovid(getUploadChunkLimit(), getReportingUnitID());
	}

	@Transactional(TxType.REQUIRED)
	@Override
	public void deleteImpfung(@NonNull Impfung impfung) {
		LOG.info("VACME-VMDL: START Delete COVID Impfung. ID: {}", impfung.getId());
		VMDLDeleteJax deleteJax = new VMDLDeleteJax(impfung, getReportingUnitID());
		vmdlRestClientServiceCovid.deleteData(Collections.singletonList(deleteJax));
		LOG.info("VACME-VMDL: END Delete COVID Impfung. ID: {}", impfung.getId());
	}
}
