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

package ch.dvbern.oss.vacme.jax.registration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Erkrankung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImpfdossierJaxTest {

	private ImpfinformationDto impfinformationen;

	@BeforeEach
	void setup() {
		impfinformationen = TestdataCreationUtil.createImpfinformationen(
				KrankheitIdentifier.COVID,
				LocalDate.now().minusMonths(4), null);
		impfinformationen = TestdataCreationUtil.addBoosterImpfung(impfinformationen, LocalDate.now());

	}

	@Test
	void getErkrankungenEmpty() {
		Assertions.assertNotNull(impfinformationen.getImpfdossier());
		ImpfdossierJax jax = new ImpfdossierJax(impfinformationen.getImpfdossier(), impfinformationen.getBoosterImpfungen(), null);
		List<ErkrankungJax> erkrankungen = jax.getErkrankungen();
		Assert.assertTrue(erkrankungen.isEmpty());
	}

	@Test
	void getErkrankungFromGrundimmunisierung() {
		Assertions.assertNotNull(impfinformationen.getImpfdossier());
		impfinformationen.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setGenesen(true);
		impfinformationen.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setPositivGetestetDatum(LocalDate.of(2022, 1, 1));
		ImpfdossierJax jax = new ImpfdossierJax(impfinformationen.getImpfdossier(), impfinformationen.getBoosterImpfungen(), null);
		List<ErkrankungJax> erkrankungen = jax.getErkrankungen();
		assertEquals(1, erkrankungen.size());
		Assertions.assertEquals(LocalDate.of(2022, 1, 1), erkrankungen.get(0).getDate());
	}

	@Test
	void getErkrankungFromGrundimmunisierungAndManual() {
		Assertions.assertNotNull(impfinformationen.getImpfdossier());
		impfinformationen.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setGenesen(true);
		impfinformationen.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setPositivGetestetDatum(LocalDate.of(2022, 1, 1));
		Erkrankung e1 = new Erkrankung();
		e1.setDate(LocalDate.now());
		e1.setImpfdossier(impfinformationen.getImpfdossier());
		impfinformationen.getImpfdossier().setErkrankungen(new ArrayList<>(List.of(e1)));
		ImpfdossierJax jax = new ImpfdossierJax(impfinformationen.getImpfdossier(), impfinformationen.getBoosterImpfungen(), null);
		List<ErkrankungJax> erkrankungen = jax.getErkrankungen();
		assertEquals(2, erkrankungen.size());
		Assertions.assertEquals(LocalDate.of(2022, 1, 1), erkrankungen.get(0).getDate());
		Assertions.assertEquals(LocalDate.now(), erkrankungen.get(1).getDate());
	}

	@Test
	void getErkrankungFromExternesZertifikat() {

		LocalDate externGeimpftDate = LocalDate.now().minusMonths(9);
		LocalDate externGeimpftGenesenDate = LocalDate.now().minusMonths(10);
		LocalDate manuellErkrankungDate = LocalDate.now().minusMonths(3);

		// externes Zertifikat
		ExternesZertifikat externesZertifikat = new ExternesZertifikat(
			impfinformationen.getImpfdossier(),
			externGeimpftDate,
			false,
			TestdataCreationUtil.createImpfstoffModerna(),
			1,
			true,
			externGeimpftGenesenDate,
			null,
			null,
			null);
		impfinformationen = TestdataCreationUtil.addExternesZertifikat(impfinformationen, externesZertifikat);

		// manuelle Erkrankung
		Assertions.assertNotNull(impfinformationen.getImpfdossier());
		Erkrankung e1 = new Erkrankung();
		e1.setDate(manuellErkrankungDate);
		e1.setImpfdossier(impfinformationen.getImpfdossier());
		impfinformationen.getImpfdossier().setErkrankungen(new ArrayList<>(List.of(e1)));

		// Liste der Erkrankungen lesen
		ImpfdossierJax jax = new ImpfdossierJax(impfinformationen.getImpfdossier(), impfinformationen.getBoosterImpfungen(), externesZertifikat);
		List<ErkrankungJax> erkrankungen = jax.getErkrankungen();
		assertEquals(2, erkrankungen.size());
		Assertions.assertEquals(externGeimpftGenesenDate, erkrankungen.get(0).getDate());
		Assertions.assertEquals(manuellErkrankungDate, erkrankungen.get(1).getDate());
	}
}
