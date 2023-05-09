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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadBaseJax;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This is a factory to create the appropriate VMDLService for a given Krankheit
 */
@Slf4j
@ApplicationScoped
public class VMDLServiceFactory {

	@Inject
	Instance<VMDLServiceCovid> vmdlServiceCovidInstance;

	@Inject
	Instance<VMDLServiceAffenpocken> vmdlServiceAffenpockenInstance;


	@NonNull
	public VMDLServiceAbstract<? extends VMDLUploadBaseJax> createVMDLService(@NonNull KrankheitIdentifier krankheit) {
		switch (krankheit) {
			case COVID:
				return vmdlServiceCovidInstance.get();
			case AFFENPOCKEN:
				return vmdlServiceAffenpockenInstance.get();
			default:
				throw new IllegalArgumentException("Krankheit not supported for VMDL Service: " + krankheit);
		}
	}
}
