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

import lombok.RequiredArgsConstructor;
import org.hashids.Hashids;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class HashIdService {

	private final VacmeSettingsService vacmeSettingsService;

	public String getHashFromNumber(long number) {
		Hashids hashids = new Hashids(vacmeSettingsService.getHashidSalt(), vacmeSettingsService.getHashidMinLength(), vacmeSettingsService.getHashidAlphabet());
		return hashids.encode(number);
	}
}
