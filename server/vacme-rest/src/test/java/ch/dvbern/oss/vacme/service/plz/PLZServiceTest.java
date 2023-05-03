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

package ch.dvbern.oss.vacme.service.plz;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.plz.PLZData;
import ch.dvbern.oss.vacme.repo.PLZDataRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class PLZServiceTest {


	PLZService plzService;

	@BeforeEach
	void init() {

		PLZDataRepo plzDataRepo = mock(PLZDataRepo.class);
		List<PLZData> mockData = new ArrayList<>();
		mockData.add(new PLZData("1", "3000", "OrtA", "BE"));
		mockData.add(new PLZData("2", "3000", "OrtA", "JU"));
		mockData.add(new PLZData("3", "3000", "OrtA", "BE"));
		mockData.add(new PLZData("4", "4000", "OrtB", "FR"));
		mockData.add(new PLZData("5", "4000", "OrtB", "VD"));
		mockData.add(new PLZData("6", "5000", "OrtC", "GR"));
		mockData.add(new PLZData("7", "3014", "OrtX", "ZH"));
		mockData.add(new PLZData("8", "3014", "OrtX", "BE"));

		Mockito
			.when(plzDataRepo.findOrteForPLZ(any(String.class)))
			.thenAnswer(invocationOnMock -> {
				String plz = (String) invocationOnMock.getArguments()[0];
				return mockData.stream().filter(plzData -> plzData.getPlz().equals(plz)).collect(Collectors.toList());

			});

		this.plzService =  new PLZService(
			Mockito.mock(PLZImportService.class),
			Mockito.mock(PLZMedstatImportService.class),
			plzDataRepo);
	}

	@Test
	void findOrteForPLZ() {
		List<PLZData> orteForPLZ = this.plzService.findOrteForPLZ("4000");
		Assertions.assertEquals(2, orteForPLZ.size());

		List<PLZData> orteForPLZ2 = this.plzService.findOrteForPLZ("3000");
		Assertions.assertEquals(3, orteForPLZ2.size());

		List<PLZData> empty = this.plzService.findOrteForPLZ("9999");
		Assertions.assertEquals(0, empty.size());

		List<PLZData> one = this.plzService.findOrteForPLZ("5000");
		Assertions.assertEquals(1, one.size());
		Assertions.assertEquals("OrtC", one.stream().findFirst().orElseThrow().getOrtsbez());
	}

	@Test
	void findKantoneForPLZ() {

		Set<String> kantoneForPLZ = plzService.findKantoneForPLZ("3000");
		Assertions.assertEquals(2, kantoneForPLZ.size());
		Assertions.assertLinesMatch(List.of("BE", "JU"), new ArrayList<>(kantoneForPLZ));
		Set<String> kantoneForPLZ1 = plzService.findKantoneForPLZ("4000");
		Assertions.assertEquals(2, kantoneForPLZ1.size());
		Assertions.assertLinesMatch(List.of("FR", "VD"), new ArrayList<>(kantoneForPLZ1));
		Set<String> kantoneForPLZ2 = plzService.findKantoneForPLZ("5000");
		Assertions.assertEquals(1, kantoneForPLZ2.size());
		Assertions.assertEquals("GR", kantoneForPLZ2.stream().findFirst().orElseThrow());
		Set<String> kantoneForPLZ3 = plzService.findKantoneForPLZ("9000");
		Assertions.assertEquals(0, kantoneForPLZ3.size());
	}

	@Test
	void findBestMatchingKantonFor() {
		System.setProperty("vacme.mandant", "BE");
		Optional<String> kantOpt = plzService.findBestMatchingKantonFor("3000");
		Assertions.assertEquals("BE", kantOpt.orElseThrow());

		Optional<String> kantOpt2 = plzService.findBestMatchingKantonFor("4000");
		Assertions.assertEquals("FR", kantOpt2.orElseThrow());

		Optional<String> emptyOpt = plzService.findBestMatchingKantonFor("9000");
		Assertions.assertTrue(emptyOpt.isEmpty());

		Optional<String> conflictOpt = plzService.findBestMatchingKantonFor("3014");
		Assertions.assertEquals("BE", conflictOpt.orElseThrow());

		System.setProperty("vacme.mandant", "ZH");
		Optional<String> conflictOptOther = plzService.findBestMatchingKantonFor("3014");
		Assertions.assertEquals("ZH", conflictOptOther.orElseThrow());
	}
}
