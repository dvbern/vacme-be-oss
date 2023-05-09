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

package ch.dvbern.oss.vacme.i18n;

import ch.dvbern.oss.vacme.enums.Mandant;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public class MandantUtil {

	private MandantUtil() {
		//util
	}

	@NonNull
	public static Mandant getMandant() {
		return Mandant.valueOf(getMandantProperty());
	}

	@NonNull
	public static String getMandantProperty() {
		final Config config = ConfigProvider.getConfig();
		return config.getValue("vacme.mandant", String.class);
	}
}
