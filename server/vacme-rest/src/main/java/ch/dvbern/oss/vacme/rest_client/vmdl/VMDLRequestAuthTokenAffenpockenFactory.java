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

package ch.dvbern.oss.vacme.rest_client.vmdl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

/**
 * Da es fuer Affenpocken eine eigenen client-id gibt muss die config entsprechend angepasst werden koennen
 */
@ApplicationScoped
public class VMDLRequestAuthTokenAffenpockenFactory extends VMDLRequestAuthTokenFactory implements ClientHeadersFactory {

	@Inject
	VacmeSettingsService vacmeSettingsService;

	@Override
	protected String getClientID() {
		return vacmeSettingsService.getVmdlClientIdAffenpocken();
	}
}
