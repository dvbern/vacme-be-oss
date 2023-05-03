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

package ch.dvbern.oss.vacme.helper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.impfen.ImpfempfehlungChGrundimmunisierung;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum TestImpfstoff {
	TEST_MODERNA, 		// 2 Dosen (1->1, 2->0)
	TEST_PFIZER, 		// 2 Dosen (1->1, 2->0)
	TEST_JOHNSON, 		// nach 1 Dosis grundimmunisiert (1->0)
	TEST_ASTRA,

	TEST_KAZAKH, 		// braucht 2 Dosen, aber ist nicht als grundimmunisierung akzeptiert (1->2, 2->1)
	TEST_ABDALA, 		// braucht 3 Dosen (1->2, 2->1, 3->0)
	TEST_CONVIDECIA, 	// gilt nicht als grundimmunisiert (1->2)

	TEST_COVAXIN,
	TEST_SPUTN_V,
	TEST_SINOPH,
	TEST_ZIFIVAX,
	TEST_AFFENPOCKEN;


	@Nullable
	public static Impfstoff createImpfstoffForTest(@Nullable TestImpfstoff stoff){
		if (stoff == null) {
			return null;
		}
		Impfstoff impfstoff = new Impfstoff();
		List<ImpfempfehlungChGrundimmunisierung> impfempfehlungen = new LinkedList<>();
		impfstoff.setImpfempfehlungenChGrundimmunisierung(impfempfehlungen);
		switch (stoff) {
		case TEST_MODERNA:
			return TestdataCreationUtil.createImpfstoffModerna();
		case TEST_PFIZER:
			return TestdataCreationUtil.createImpfstoffPfizer();
		case TEST_JOHNSON:
			return TestdataCreationUtil.createImpfstoffJanssen();
		case TEST_ASTRA:
			return TestdataCreationUtil.createImpfstoffAstraZeneca();
		case TEST_KAZAKH:
			return TestdataCreationUtil.createImpfstoffKazakhstan();
		case TEST_ABDALA:
			return TestdataCreationUtil.createImpfstoffAbdala();
		case TEST_CONVIDECIA:
			return TestdataCreationUtil.createImpfstoffConvidecia();
		case TEST_COVAXIN:
			return TestdataCreationUtil.createImpfstoffCovaxin();
		case TEST_SINOPH:
			return TestdataCreationUtil.createImpfstoffSinopharm();
		case TEST_SPUTN_V:
			return TestdataCreationUtil.createImpfstoffSputnikV();
		case TEST_ZIFIVAX:
			return TestdataCreationUtil.createImpfstoffZifivax();
		case TEST_AFFENPOCKEN:
			return TestdataCreationUtil.createImpfstoffAffenpocken();
		default:
			throw new IllegalArgumentException("No Valid Testimpfstoff name provided");
		}
	}

	@Nullable
	public static Impfstoff getImpfstoffById(UUID id) {
		return Arrays.stream(values())
			.map(TestImpfstoff::createImpfstoffForTest)
			.filter(impfstoff -> impfstoff != null && impfstoff.getId().equals(id))
			.findFirst().orElse(null);
	}
}
