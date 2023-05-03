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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackendTranslationTest {

	static final String BACKEND_I18N_PATH = "../vacme-shared/src/main/resources/ch/dvbern/oss/vacme/i18n";
	static final String I18N_EXTENSION = ".properties";

	@Test
	public void noStraightApostrophs() throws IOException {
		var fileMap = findI18nFiles();
		var kantone = getKantone(fileMap);
		for (String k : kantone) {
			Map<String, Path> filesToCompare = getFilesToCompare(fileMap, k);
			Set<String> sprachen = filesToCompare.keySet();

			// then check no apostrophs are used
			checkNoStraightApostrophes(filesToCompare, sprachen);
		}
	}

	private void checkNoStraightApostrophes(Map<String, Path> filesToCompare, Set<String> sprachen) throws IOException {
		for (String sprache : sprachen) {
			var propFile = filesToCompare.get(sprache);
			var keyValueMap = propertiesToMap(propFile);

			// Es ist ueblich, dass der nicht uebersetzte Wert, den Sprach-Praefix der Uebersetzung (z.B. FR_),
			// gefolgt vom Text der primaeren Sprache enthaelt.
			// Beispiel: "FR_Dieser Text ist noch nicht uebersetzt"
			keyValueMap.forEach((k, v) -> {
				int singleQuotes = StringUtils.countMatches(v, "'");
				int escapedSingleQuotes = StringUtils.countMatches(v, "''");
				if (singleQuotes  != escapedSingleQuotes * 2 ) {
					Assertions.fail(MessageFormat.format(
						"VACME-I18N: Falscher Apostroph in {0} ist fuer den Schlüssel {1}  im Text '{2}'  Es wurde  ' "
							+ "statt zb ’ geschrieben",
						propFile.getFileName(),
						k,
						StringUtils.abbreviate(v, 20)));
				}
			});
		}
	}

	@Test
	public void allKeysAreMatching() throws IOException {

		var fileMap = findI18nFiles();
		var kantone = getKantone(fileMap);

		// first compare keys and produce corresponding warnings
		boolean structureOk = true;
		for (String k : kantone) {
			var filesToCompare = getFilesToCompare(fileMap, k);
			assertTrue(filesToCompare.size() >= 2, "At least two I18N files required to compare!");

			var sprachen = filesToCompare.keySet();
			assertThat(sprachen, hasSize(greaterThanOrEqualTo(2)));

			structureOk = structureOk && compareKeys(filesToCompare, sprachen);

			// then check for not translated values and produce corresponding warnings
			checkTranslation(filesToCompare, sprachen);
		}

		// finally fail on missing key(s)
		assertTrue(structureOk, "The structure of two or more property files is different!");
	}

	private boolean compareKeys(Map<String, Path> fileCompareMap, Set<String> sprachen) throws IOException {
		String s1 = null;
		String s2 = null;
		boolean structureOk = true;
		Iterator<String> spi = sprachen.iterator();
		while (spi.hasNext()) {
			if (null == s2) {
				s1 = spi.next();
			} else {
				s1 = s2;
			}
			s2 = spi.next();
			structureOk = structureOk && compareStructure(fileCompareMap.get(s1), fileCompareMap.get(s2));
		}
		return structureOk;
	}

	private boolean compareStructure(Path prop1, Path prop2) throws IOException {
		var ks1 = propertiesToKeySet(prop1);
		var ks2 = propertiesToKeySet(prop2);
		AtomicBoolean t1ok = new AtomicBoolean(true);
		AtomicBoolean t2ok = new AtomicBoolean(true);

		// Hier pruefen wir, ob beide Dateien dieselben Schluessel enthalten
		ks2.forEach(k2 -> {
			if (!ks1.contains(k2)) {
				System.out.println(MessageFormat.format(
					"VACME-I18N: In {0} fehlt der Schlüssel {1}",
					prop1.getFileName(),
					k2));
				t1ok.set(false);
			}
		});
		ks1.forEach(k1 -> {
			if (!ks2.contains(k1)) {
				System.out.println(MessageFormat.format(
					"VACME-I18N: In {0} fehlt der Schlüssel {1}",
					prop2.getFileName(),
					k1));
				t2ok.set(false);
			}
		});
		return (t1ok.get() && t2ok.get());
	}

	private void checkTranslation(Map<String, Path> fileCompareMap, Set<String> sprachen) throws IOException {
		for (String sprache : sprachen) {
			var propFile = fileCompareMap.get(sprache);
			var keyValueMap = propertiesToMap(propFile);

			// Es ist ueblich, dass der nicht uebersetzte Wert, den Sprach-Praefix der Uebersetzung (z.B. FR_),
			// gefolgt vom Text der primaeren Sprache enthaelt.
			// Beispiel: "FR_Dieser Text ist noch nicht uebersetzt"
			keyValueMap.forEach((k, v) -> {
				if (null != v && StringUtils.startsWithIgnoreCase(v, sprache + '_')) {
					System.out.println(MessageFormat.format(
						"VACME-I18N: In {0} ist der Schlüssel {1} (Text {2}) nicht übersetzt",
						propFile.getFileName(),
						k,
						StringUtils.abbreviate(v, 20)));
				}
			});
		}
	}

	private Map<String, String> propertiesToMap(Path propFile) throws IOException {
		Map<String, String> map = new HashMap<>();
		try (InputStream input = Files.newInputStream(propFile)) {
			Properties prop = new Properties();
			prop.load(input); // load the properties file
			map = prop.entrySet().stream()
				.collect(Collectors.toMap(
					e -> String.valueOf(e.getKey()),
					e -> String.valueOf(e.getValue())));
		}
		return map;
	}

	private Set<String> propertiesToKeySet(Path propFile) throws IOException {
		var map = propertiesToMap(propFile);
		var ks = map.keySet();
		return ks;
	}

	private Map<String, Path> getFilesToCompare(Map<String, Path> fileMap, String kanton) {
		return fileMap.entrySet().stream()
			.filter(e -> kanton.equals(extractKanton(e.getKey())))
			.collect(Collectors.toMap(e -> extractSprache(e.getKey()), Entry::getValue));
	}

	private Map<String, Path> findI18nFiles() throws IOException {
		if (!Files.isDirectory(Paths.get(BACKEND_I18N_PATH))) {
			throw new IllegalArgumentException("Path must be a directory!");
		}
		try (var stream = Files.walk(Paths.get(BACKEND_I18N_PATH))) {
			return stream
				.filter(Files::isRegularFile)
				.filter(p -> p.getFileName().toString().endsWith(I18N_EXTENSION))
				.collect(Collectors.toMap(path -> path.getFileName().toString(), path -> path));
		}
	}

	private Set<String> getKantone(Map<String, Path> fileMap) {
		return fileMap.keySet().stream()
			.map(BackendTranslationTest::extractKanton)
			.collect(Collectors.toSet());
	}

	private static String extractKanton(String name) {
		return name.substring(13, 15);
	}

	private static String extractSprache(String name) {
		if (name.length() == 26) {
			return "de";
		}
		return name.substring(16, 18);
	}

}
