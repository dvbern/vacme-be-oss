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

package ch.dvbern.oss.vacme.betrug;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Splitter;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class BetrugTest {

	static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static void main(String[] args) {
		BetrugTest test = new BetrugTest();
		test.pruefeZertifikate();
	}

	public void pruefeZertifikate() {
		Map<String, List<BetrugTestPayload>> zertifikatMap = readZertifikate("usercert_zh.csv");
		int count = 0;
		for (Map.Entry<String, List<BetrugTestPayload>> zertifikate : zertifikatMap.entrySet()) {
			Set<BetrugTestPayload> set = new HashSet<BetrugTestPayload>(zertifikate.getValue());
			BetrugTestPayload last = null;
			for(int i = 0; i < zertifikate.getValue().size(); i++) {
				BetrugTestPayload current = zertifikate.getValue().get(i);
				if (last != null && !current.geburtsdatum.equals(last.geburtsdatum) && !current.name.equals(last.name)) {
					if (set.contains(current)) {
						System.out.println(count++ + " " + current.id + " " + current + ", " + last);
					}
				}
				set.add(current);
				last = current;
			}
		}
	}

	@SneakyThrows
	private Map<String, List<BetrugTestPayload>> readZertifikate(@NonNull String fileName) {
		Map<String, List<BetrugTestPayload>> zertifikatMap = new HashMap<>();
		final InputStream resourceAsStream = BetrugTest.class.getResourceAsStream(fileName);
		Objects.requireNonNull(resourceAsStream);
		BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			List<String> elements = Splitter.on(',').trimResults().splitToList(line);
			String id = removeQuotes(elements.get(0));
			List<BetrugTestPayload> zertifikate = zertifikatMap.get(id);
			if (zertifikate == null) {
				zertifikate = new ArrayList<BetrugTestPayload>();
				zertifikatMap.put(id, zertifikate);
			}
			BetrugTestPayload BetrugTestPayload = new BetrugTestPayload();
			BetrugTestPayload.id = id;
			BetrugTestPayload.date = format.parse(removeQuotes(elements.get(1))) ;
			BetrugTestPayload.name = getSubstring(elements.get(2));
			BetrugTestPayload.vorname = getSubstring(elements.get(3));
			BetrugTestPayload.geburtsdatum = getSubstring(elements.get(4));
			zertifikate.add(BetrugTestPayload);
		}
		return zertifikatMap;
	}

	@NotNull
	private String getSubstring(String csvContent) {
		try {
			return csvContent.substring(3, csvContent.length() - 3);
		} catch (Exception e) {
			return "";
		}
	}

	@NotNull
	private String removeQuotes(String csvContent) {
		try {
			return csvContent.substring(1, csvContent.length() - 1);
		} catch (Exception e) {
			return "";
		}
	}
}
