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

package ch.dvbern.oss.vacme.entities.types;

import javax.persistence.Converter;

import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Converter(autoApply = true)
public class CleanFileNameConverter extends GenericStringConverter<CleanFileName> {

	public CleanFileNameConverter() {
		super(CleanFileNameConverter::toExternalForm, CleanFileNameConverter::fromString);
	}

	private static String toExternalForm(CleanFileName fileName) {
		return fileName.getFileName();
	}

	private static CleanFileName fromString(String s) {
		return CleanFileName.parse(s);
	}

	public static void registerJackson(
			SimpleModule module
	) {
		GenericStringConverter.registerJackson(
				module,
				CleanFileName.class,
				CleanFileNameConverter::toExternalForm,
				CleanFileNameConverter::fromString
		);
	}
}
