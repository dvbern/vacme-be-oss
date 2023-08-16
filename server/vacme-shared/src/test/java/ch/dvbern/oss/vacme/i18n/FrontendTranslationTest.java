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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendTranslationTest {

	static final String FRONTEND_I18N_PATH = "../../frontend/projects/vacme-web-shared/src/assets/i18n/";
	static final String I18N_EXTENSION = ".json";
	private Map<String, JsonNode> flatJson = null;

	@Test
	public void allKeysAreMatching() throws IOException {
		var fileMap = findI18nFiles();
		var kantone = getKantone(fileMap);

		// first compare keys and produce corresponding warnings
		boolean structureOk = true;
		for (String k : kantone) {
			Map<Pair<String, String>, Path> filesToCompare = getFilesToCompare(fileMap, k);
			assertTrue(filesToCompare.size() >= 2, "At least two I18N files required to compare!");

			Set<Pair<String, String>> sprachenAndSuffix = filesToCompare.keySet();
			assertThat(sprachenAndSuffix, hasSize(greaterThanOrEqualTo(2)));

			structureOk = structureOk && compareKeys(filesToCompare, sprachenAndSuffix);

			// then check for not translated values and produce corresponding warnings
			checkTranslation(filesToCompare, sprachenAndSuffix);
		}

		// finally fail on missing key(s)
		assertTrue(structureOk, "The structure of two or more property files is different!");
	}

	private boolean compareKeys(Map<Pair<String, String>, Path> fileCompareMap, Set<Pair<String, String>> sprachenAndSuffix) throws IOException {
		boolean structureOk = true;
		Set<String> suffixes = sprachenAndSuffix.stream()
			.map(Pair::getRight)
			.collect(Collectors.toSet());

		for (String suffix: suffixes) {
			String s1 = null;
			String s2 = null;
			Set<String> sprachenForSuffix = sprachenAndSuffix.stream()
				.filter(pair -> pair.getRight().equals(suffix))
				.map(Pair::getLeft)
				.collect(Collectors.toSet());

			Iterator<String> itr = sprachenForSuffix.iterator();
			while (itr.hasNext()) {
				if (null == s2) {
					s1 = itr.next();
				} else {
					s1 = s2;
				}
				s2 = itr.next();
				structureOk = structureOk && compareStructure(
					fileCompareMap.get(ImmutablePair.of(s1, suffix)),
					fileCompareMap.get(ImmutablePair.of(s2, suffix)));
			}
		}

		return structureOk;
	}

	private boolean compareStructure(Path json1, Path json2) throws IOException {
		var ks1 = jsonToKeySet(json1);
		var ks2 = jsonToKeySet(json2);
		AtomicBoolean t1ok = new AtomicBoolean(true);
		AtomicBoolean t2ok = new AtomicBoolean(true);

		// Hier pruefen wir, ob beide Dateien dieselben Schluessel enthalten
		ks2.forEach(k2 -> {
			if (!ks1.contains(k2)) {
				System.out.println(MessageFormat.format(
					"VACME-I18N: In {0} fehlt der Schlüssel {1}",
					json1.getFileName(),
					k2));
				t1ok.set(false);
			}
		});
		ks1.forEach(k1 -> {
			if (!ks2.contains(k1)) {
				System.out.println(MessageFormat.format(
					"VACME-I18N: In {0} fehlt der Schlüssel {1}",
					json2.getFileName(),
					k1));
				t2ok.set(false);
			}
		});
		return (t1ok.get() && t2ok.get());
	}

	private void checkTranslation(Map<Pair<String, String>, Path> fileCompareMap, Set<Pair<String, String>> sprachenAndSuffix) throws IOException {
		for (Pair<String, String> spracheAndSuffix : sprachenAndSuffix) {
			var jsonFile = fileCompareMap.get(spracheAndSuffix);
			var keyNodeMap = jsonToKeyNodeMap(jsonFile);

			// Es ist ueblich, dass der nicht uebersetzte Wert, den Sprach-Praefix der Uebersetzung (z.B. FR_),
			// gefolgt vom Text der primaeren Sprache enthaelt.
			// Beispiel: "FR_Dieser Text ist noch nicht uebersetzt"
			keyNodeMap.forEach((k, v) -> {
				if (null != v && StringUtils.startsWithIgnoreCase(v.asText(), spracheAndSuffix.getLeft() + '_')) {
					System.out.println(MessageFormat.format(
						"VACME-I18N: In {0} ist der Schlüssel {1} (Text {2}) nicht übersetzt",
						jsonFile.getFileName(),
						k,
						StringUtils.abbreviate(v.asText(), 20)));
				}
			});
		}
	}

	private Set<String> jsonToKeySet(Path json) throws IOException {
		Map<String, JsonNode> fj = jsonToKeyNodeMap(json);
		var ks = fj.keySet();
		return ks;
	}

	private Map<String, JsonNode> jsonToKeyNodeMap(Path json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
			var jn = mapper.readTree(json.toFile());
			var fj = flattenJson(jn);
			return fj;
	}

	private Map<String, JsonNode> flattenJson(JsonNode root) {
		flatJson = new LinkedHashMap<>(); // reset recurs collection
		processFlatten(root, "");
		return flatJson;
	}

	private void processFlatten(JsonNode node, String prefix) {
		if (node.isObject()) {
			ObjectNode object = (ObjectNode) node;
			object
				.fields()
				.forEachRemaining(
					entry -> processFlatten(entry.getValue(), prefix + '/' + entry.getKey()));
		} else if (node.isArray()) {
			ArrayNode array = (ArrayNode) node;
			AtomicInteger counter = new AtomicInteger();
			array
				.elements()
				.forEachRemaining(
					item -> processFlatten(item, prefix + '/' + counter.getAndIncrement()));
		} else {
			flatJson.put(prefix, node);
		}
	}

	private Map<Pair<String, String>, Path> getFilesToCompare(Map<String, Path> fileMap, String kanton) {
		return fileMap.entrySet().stream()
			.filter(e -> kanton.equals(extractKanton(e.getKey())))
			.collect(Collectors.toMap(e -> extractSpracheAndSuffix(e.getKey()), Entry::getValue));
	}

	private Map<String, Path> findI18nFiles() throws IOException {
		if (!Files.isDirectory(Paths.get(FRONTEND_I18N_PATH))) {
			throw new IllegalArgumentException("Path must be a directory!");
		}
		try (var stream = Files.walk(Paths.get(FRONTEND_I18N_PATH))) {
			return stream
				.filter(Files::isRegularFile)
				.filter(p -> p.getFileName().toString().endsWith(I18N_EXTENSION))
				.collect(Collectors.toMap(path -> path.getFileName().toString(), path -> path));
		}
	}

	private Set<String> getKantone(Map<String, Path> fileMap) {
		return fileMap.keySet().stream()
			.map(FrontendTranslationTest::extractKanton)
			.collect(Collectors.toSet());
	}

	private static String extractKanton(String name) {
		// split names like en.zh.json into parts and return the middle part if present or 'base' otherwise
		String[] splitName = name.split("\\.");
		if (splitName.length == 2) {
			return "base";
		}
		return splitName[1];
	}

	private static Pair<String, String> extractSpracheAndSuffix(String name) {
		String[] splitName = name.split("\\.");
		String sprachePart = splitName[0];
		return ImmutablePair.of(sprachePart.substring(0, 2), sprachePart.substring(2));
	}

}
