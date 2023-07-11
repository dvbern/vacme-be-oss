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

package ch.dvbern.oss.vacme.dbschema;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlywaySchemaTest {

	@Test
	public void flywayMigrationNumbersCorrect() {
		Set<String> usedNumbers = new HashSet<>();
		URL scriptFolder = FlywaySchemaTest.class.getResource("/db/migration");
		if (scriptFolder != null) {
			File folder = new File(scriptFolder.getFile());
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					Assertions.assertTrue(file.isFile());
					if (file.isFile()) {
						String revNbr = getRevisionNumberFromFilename(file.getName());
						if (usedNumbers.contains(revNbr)) {
							Assertions.fail("Fehler in Flyway-Skripts: Die Nummer " + revNbr + " ist mehrmals vergeben!");
						}
						usedNumbers.add(revNbr);
					}
				}
			}
		}
		Assertions.assertTrue(true, "All Files checked!");
	}

	private String getRevisionNumberFromFilename(String filename) {
		// Der Text zwischen V und dem _ ist die Nummer
		return StringUtils.substringBefore(StringUtils.substring(filename, 1), "_");
	}
}
