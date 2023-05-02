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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.plz.PLZData;
import ch.dvbern.oss.vacme.service.plz.PLZImportService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PLZImportServiceTest {

	public static final String PATH_TO_TESTFILE = "/plz/plz_verzeichnis_v2_test.csv";



	@Test
	void testCsvParsing() throws IOException {
		PLZImportService plzImportService = new PLZImportService();

		List<PLZData> importedData;
		try (InputStream inputStreamOfCsv = PLZImportServiceTest.class.getResourceAsStream(PATH_TO_TESTFILE)) {
			importedData = plzImportService.impportPlzDataDtoFromCSV(inputStreamOfCsv);
		}
		Assertions.assertNotNull(importedData);
		Assertions.assertEquals(29, importedData.size());

	}

	@Nested
	class ImportedNameCorrectTest {

		private List<PLZData> importedData;
		public ImportedNameCorrectTest() throws IOException {
			PLZImportService plzImportService = new PLZImportService();
			try (InputStream inputStreamOfCsv = PLZImportServiceTest.class.getResourceAsStream(PATH_TO_TESTFILE)) {
				importedData = plzImportService.impportPlzDataDtoFromCSV(inputStreamOfCsv);
			}
		}

		@ParameterizedTest
		@CsvSource({
			"Lausanne",
			"Valendas",
			"Castrisch",
			"Ilanz",
			"Ladir",
			"Falera",
			"Morissen",
			"Degen",
			"Genève",
			"Bulle Jardins P.L.",
			"Zürich 27 Zust",
			"Daillens SPS",
			"Martigny rue Avou.",
			"Cadenazzo ZF",
			"Urdorf PZ",
			"Pratteln Sperrgut",
			"Grabs Staatsstr",
			"Zofingen U. Graben",
			"Malters Luzernstr",
			"Lausen Grammontstr",
			"Bern Weltpoststr",
			"Oftringen Tychb.",
			"Wimmis Herrenmatte",
			"Cevio Dist",
			"Glis Gliserallee",
			"Luzern Hirschengr",
			"Beride d. Bedigli.",
			"Chapella",
			"Urtenen-S. Z-Platz"
		})
		public void testImportedName(String expectedImportedName) {
			Assertions.assertTrue(importedData.stream().map(PLZData::getOrtsbez).anyMatch(s -> s.equals(expectedImportedName)),
				"Expected to find " + expectedImportedName + " in list of names " + importedData.stream().map(PLZData::getOrtsbez).collect(Collectors.joining(";")));

		}
	}
}
