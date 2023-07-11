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

package ch.dvbern.oss.vacme.entities.registration;

import java.time.LocalDate;

import ch.dvbern.oss.vacme.entities.embeddables.ZweiteGrundimmunisierungVerzichtet;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrierungTest {

	private ImpfinformationDto infos;

	@BeforeEach
	void setUp() {
		ImpfinformationBuilder builder = new ImpfinformationBuilder();
		infos = builder
			.create(KrankheitIdentifier.COVID)
			.withImpfung1(LocalDate.now(), TestdataCreationUtil.createImpfstoffPfizer())
			.getInfos();
	}

	@Test
	void setStatusToAbgeschlossenOhneZweiteImpfung() {
		Impfdossier dossier = infos.getImpfdossier();
		final ZweiteGrundimmunisierungVerzichtet verzichtetData = dossier.getZweiteGrundimmunisierungVerzichtet();
		Impfung impfung = infos.getImpfung1();
		assertNotNull(impfung);
		// erkrankt
		dossier.setStatusToAbgeschlossenOhneZweiteImpfung(infos,  true, null, LocalDate.now().minusDays(3));
		assertNull(verzichtetData.getZweiteImpfungVerzichtetGrund());
		assertNotNull(dossier.getVollstaendigerImpfschutzTyp());
		assertTrue(verzichtetData.isGenesen());
		assertEquals(LocalDate.now().minusDays(3), verzichtetData.getPositivGetestetDatum());

		dossier.setStatusToAbgeschlossenOhneZweiteImpfung(infos, false, "hatte doch keine Lust", null);
		assertNull(dossier.getVollstaendigerImpfschutzTyp());
		assertFalse(verzichtetData.isGenesen());
		assertNull(verzichtetData.getPositivGetestetDatum());
		assertEquals("hatte doch keine Lust", verzichtetData.getZweiteImpfungVerzichtetGrund());
	}

	@Test
	void testSetStatusToNichtAbgeschlossenStatus() {
		Impfdossier dossier = infos.getImpfdossier();
		final ZweiteGrundimmunisierungVerzichtet verzichtetInfo = dossier.getZweiteGrundimmunisierungVerzichtet();
		Impfung impfung = infos.getImpfung1();
		assertNotNull(impfung);

		dossier.setStatusToAbgeschlossenOhneZweiteImpfung(infos, true, null, LocalDate.now().minusDays(3));
		assertNull(verzichtetInfo.getZweiteImpfungVerzichtetGrund());
		assertNotNull(dossier.getVollstaendigerImpfschutzTyp());
		assertTrue(verzichtetInfo.isGenesen());

		infos.getImpfdossier().setStatusToNichtAbgeschlossenStatus(infos, ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT, impfung);
		assertNull(dossier.getVollstaendigerImpfschutzTyp());
		assertFalse(verzichtetInfo.isGenesen());
	}
}
