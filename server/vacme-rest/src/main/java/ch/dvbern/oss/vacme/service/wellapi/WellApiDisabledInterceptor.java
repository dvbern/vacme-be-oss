/*
 *
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.oss.vacme.service.wellapi;

import javax.annotation.Nullable;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Interceptor
@CheckIfWellDisabledInterceptor
class WellApiDisabledInterceptor {

	@ConfigProperty(name = "vacme.well.api.disabled", defaultValue = "false")
	boolean wellApiDisabled;

	@AroundInvoke
	@Nullable
	public Object checkifEnabled(InvocationContext context) throws Exception {

		if (wellApiDisabled) {
			return null;
		}
		return context.proceed();
	}
}
