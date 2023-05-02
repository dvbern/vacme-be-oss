/*
 *
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

package ch.dvbern.oss.vacme.repo;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.enums.FileNameEnum;
import ch.dvbern.oss.vacme.prozess.AbstractProzessIT;
import ch.dvbern.oss.vacme.reports.abrechnung.AbrechnungDataRow;
import ch.dvbern.oss.vacme.reports.abrechnungZH.AbrechnungZHDataRow;
import ch.dvbern.oss.vacme.testing.MariaDBProfile;
import ch.dvbern.oss.vacme.testing.MariaDBTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(MariaDBTestResource.class)
@TestProfile(MariaDBProfile.class)
@QuarkusTest
public class AbrechnungRepoIT extends AbstractProzessIT {

	@Inject
	AbrechnungRepo abrechnungRepo;

	@Test
	void testAbrechnungQuery() {

		createImpfdossierAffenpockenWithImpfungenAm(LocalDate.now().minusMonths(5), LocalDate.now().minusMonths(1));

		// Abrechnung is only for Covid so no result if only Affenpockenimpfungen
		List<AbrechnungDataRow> odiAbrechnung =
			abrechnungRepo.findOdiAbrechnung(LocalDate.now().minusYears(1), LocalDate.now(), FileNameEnum.ABRECHNUNG_ERWACHSEN);
		Assertions.assertNotNull(odiAbrechnung);
		Assertions.assertEquals(0,odiAbrechnung.size());

		// add a Covid Impfdossier
		createImpfdossierCovidWithBoosteram(LocalDate.now().minusMonths(5), LocalDate.now().minusMonths(1));
		odiAbrechnung =
			abrechnungRepo.findOdiAbrechnung(LocalDate.now().minusYears(1), LocalDate.now(), FileNameEnum.ABRECHNUNG_ERWACHSEN);
		Assertions.assertNotNull(odiAbrechnung);
		Assertions.assertEquals(2,odiAbrechnung.size(), "Should find 2 Covid Impfungen");


		List<AbrechnungZHDataRow> odiAbrechnungZH =
			abrechnungRepo.findOdiAbrechnungZH(LocalDate.now().minusYears(1), LocalDate.now(), false);
		Assertions.assertEquals(2,odiAbrechnungZH.size());

		List<AbrechnungZHDataRow> odiAbrechnungZHKind =
			abrechnungRepo.findOdiAbrechnungZH(LocalDate.now().minusYears(1), LocalDate.now(), true);

		Assertions.assertNotNull(odiAbrechnungZH);
		Assertions.assertEquals(2,odiAbrechnungZHKind.size());
	}
}
