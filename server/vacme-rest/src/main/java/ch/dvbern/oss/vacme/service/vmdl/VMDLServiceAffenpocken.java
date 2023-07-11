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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLDeleteJax;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadAffenpockenJax;
import ch.dvbern.oss.vacme.rest_client.vmdl.VMDLRestClientServiceAffenpocken;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Slf4j
public class VMDLServiceAffenpocken extends VMDLServiceAbstract<VMDLUploadAffenpockenJax> {

	@Inject
	@RestClient
	VMDLRestClientServiceAffenpocken vmdlRestClientServiceAffenpocken;

	@Override
	protected void performAdditionalMappings(@NonNull List<VMDLUploadAffenpockenJax> vmdlUploadJax) {
		// no additional post-create mappings needed for Affenpocken
	}

	@Override
	public void uploadData(@NonNull List<VMDLUploadAffenpockenJax> vmdlUploadJax) {
		this.vmdlRestClientServiceAffenpocken.uploadData(vmdlUploadJax);
	}

	@Override
	@NonNull
	protected List<VMDLUploadAffenpockenJax> findVMDLPendenteImpfungen() {
		return vmdlRepo.getVMDLPendenteAffenpockenImpfungen(getUploadChunkLimit(), getReportingUnitID());
	}

	@Transactional(TxType.REQUIRED)
	@Override
	public void deleteImpfung(@NonNull Impfung impfung) {
		LOG.info("VACME-VMDL: START Delete Affenpocken Impfung. ID: {}", impfung.getId());
		VMDLDeleteJax deleteJax = new VMDLDeleteJax(impfung, getReportingUnitID());
		vmdlRestClientServiceAffenpocken.deleteData(Collections.singletonList(deleteJax));
		LOG.info("VACME-VMDL: END Delete Affenpocken Impfung. ID: {}", impfung.getId());
	}
}
