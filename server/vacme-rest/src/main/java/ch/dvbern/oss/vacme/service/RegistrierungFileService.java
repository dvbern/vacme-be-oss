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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFile;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFileTyp;
import ch.dvbern.oss.vacme.repo.RegistrierungFileRepo;
import ch.dvbern.oss.vacme.util.VacmeFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RegistrierungFileService {

	private final RegistrierungFileRepo registrierungFileRepo;

	public RegistrierungFile createAndSave(
		byte[] content,
		@NonNull RegistrierungFileTyp typ,
		@NonNull Registrierung registrierung
	) {
		final RegistrierungFile registrierungFile = VacmeFileUtil.createRegistrierungFile(typ, registrierung, content);
		registrierungFileRepo.createRegistrierungFile(registrierungFile);
		return registrierungFile;
	}

	public void deleteRegistrierungbestaetigung(@NonNull Registrierung registrierung) {
		final RegistrierungFile registrierungFile =
			registrierungFileRepo.getRegistrierungFile(registrierung, RegistrierungFileTyp.REGISTRIERUNG_BESTAETIGUNG);
		if (registrierungFile != null) {
			registrierungFileRepo.deleteRegistrierungFile(registrierungFile.toId());
		}
	}
}
