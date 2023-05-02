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

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadBaseJax;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.vmdl.VMDLRepo;
import ch.dvbern.oss.vacme.service.plz.PLZCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadCovidJax.UNKOWN_KANTON;

@Slf4j
public abstract class VMDLServiceAbstract<T extends VMDLUploadBaseJax> {

	private static final String BUESINGEN_KANTON = "DE";
	private static final String CAMPIONE_ITALIA_KANTON = "IT";
	private static final String VMDL_KRZL_D = "D";
	private static final String VMDL_KRZL_I = "I";

	@Inject
	ImpfungRepo impfungRepo;

	@Inject
	VMDLRepo vmdlRepo;

	@Inject
	PLZCacheService plzCacheService;

	@ConfigProperty(name = "vmdl.upload.chunk.limit", defaultValue = "100")
	int uploadChunkLimit;

	@ConfigProperty(name = "vmdl.reporting_unit_id")
	String reportingUnitID;


	@Transactional(TxType.REQUIRES_NEW)
	public void doUploadVMDLDataForGenericBatch() {
		StopWatch stopWatchQuery = StopWatch.createStarted();

		List<T> vmdlUploadJax = findVMDLPendenteImpfungen();

		LOG.info("VACME-VMDL: Total Query Time: {}ms for {} elements", stopWatchQuery.getTime(TimeUnit.MILLISECONDS), vmdlUploadJax.size());
		if (vmdlUploadJax.isEmpty()) {
			return;
		}

		StopWatch stopwWatchMapping = StopWatch.createStarted();
		this.setKantonBasedOnPlzForAllEntries(vmdlUploadJax);
		LOG.info("VACME-VMDL: Mapping Time PLZ Time: {}ms for {}", stopwWatchMapping.getTime(TimeUnit.MILLISECONDS), vmdlUploadJax.size());
		performAdditionalMappings(vmdlUploadJax);


		StopWatch stopWatch = StopWatch.createStarted();
		LOG.info("VACME-VMDL: START Send next chunk to VMDL upload. Size: {}", vmdlUploadJax.size());
		this.uploadData(vmdlUploadJax);
		stopWatch.stop();
		LOG.info("VACME-VMDL: END Sending chunk to VMDL (took {}ms)", stopWatch.getTime(TimeUnit.MILLISECONDS));

		final LocalDateTime now = LocalDateTime.now();
		for (VMDLUploadBaseJax impfungVmdl : vmdlUploadJax) {
			Impfung impfung = impfungVmdl.getImpfung();
			impfung.setTimestampVMDL(now);
			impfungRepo.update(impfung);
		}
	}

	protected abstract void performAdditionalMappings(@NonNull List<T> vmdlUploadJax);

	public abstract void  uploadData(List<T> vmdlUploadJax);

	@NonNull
	protected abstract List<T> findVMDLPendenteImpfungen();

	private void  setKantonBasedOnPlzForAllEntries(@NonNull List< ? extends VMDLUploadBaseJax> vmdlUploadBaseJaxes) {
		vmdlUploadBaseJaxes.forEach(vmdlUploadJax -> {
			String kantonsKrzl = plzCacheService.findBestMatchingKantonFor(vmdlUploadJax.getPlz()).orElse(UNKOWN_KANTON);
			if (BUESINGEN_KANTON.equals(kantonsKrzl)) {
				kantonsKrzl = VMDL_KRZL_D;
			} else if (CAMPIONE_ITALIA_KANTON.equals(kantonsKrzl)) {
				kantonsKrzl = VMDL_KRZL_I;
			}
			vmdlUploadJax.setPersonResidenceCtn(kantonsKrzl);
		});
	}

	public abstract void deleteImpfung(@NonNull Impfung impfung);

	public boolean wasSentToVMDL(@NonNull Impfung impfung) {
		return this.vmdlRepo.wasSentToVMDL(impfung);
	}
}
