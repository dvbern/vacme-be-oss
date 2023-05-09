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

package ch.dvbern.oss.vacme.shared.util;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import static ch.dvbern.oss.vacme.shared.util.InputValidationUtil.removeAllControlCharacters;

@Getter
@EqualsAndHashCode
public class CleanFileName implements Serializable {
	private static final long serialVersionUID = -4274317691559857419L;

	@NotEmpty
	private final String fileName;

	public CleanFileName(String fileName) {
		this.fileName = clean(fileName);
	}

	@Override
	public String toString() {
		return fileName;
	}

	public static CleanFileName parse(String filePath) {
		return new CleanFileName(filePath);
	}

	public static String clean(String filePath) {
		String filename = FilenameUtils.getName(filePath);

		String noControls = removeAllControlCharacters(filename);

		String result = StringUtils.trimToEmpty(noControls);

		return result;
	}

}
