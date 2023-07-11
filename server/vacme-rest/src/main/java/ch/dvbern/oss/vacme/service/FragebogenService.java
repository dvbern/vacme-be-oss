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

import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.repo.FragebogenRepo;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@ApplicationScoped
@Slf4j
public class FragebogenService {

	private final FragebogenRepo fragebogenRepo;
	private final RegistrierungService registrierungService;

	@Inject
	public FragebogenService(
		@NonNull FragebogenRepo fragebogenRepo,
		@NonNull RegistrierungService registrierungService
	) {
		this.fragebogenRepo = fragebogenRepo;
		this.registrierungService = registrierungService;
	}

	@NonNull
	public Fragebogen findFragebogenByRegistrierungsnummer(
		@NonNull String registrierungsnummer
	) {
		Registrierung registrierung = registrierungService.findRegistrierung(registrierungsnummer);
		// Falls unterdessen meine Prioritaet freigeschaltet wurde, muss mein Status angepasst werden
		return fragebogenRepo.getByRegistrierung(registrierung).orElseThrow(
			() -> AppValidationMessage.UNKNOWN_REGISTRIERUNGSNUMMER.create(registrierungsnummer));
	}

	public void setAndUpdateImmunsupprimiert(String registrierungsnummer, @Nullable Boolean immunsupprimiert) {
		Fragebogen fragebogen = findFragebogenByRegistrierungsnummer(registrierungsnummer);
		fragebogen.setImmunsupprimiert(immunsupprimiert);
		fragebogenRepo.update(fragebogen);
	}
}
