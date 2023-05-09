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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import ch.dvbern.oss.vacme.jax.stats.ImpfstoffTagesReportJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumTagesReportJax;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.repo.StatistikKennzahlEintragRepo;
import ch.dvbern.oss.vacme.repo.StatsRepo;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

// TODO Test entweder erweitern oder loeschen, denn jetzt ist er ziemlich nutzlos...
@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class StatsServiceTest {

	private StatsService statsService;

	private final OrtDerImpfung odi1 = mockOrtDerImpfung("10");
	private final Impfstoff moderna = TestdataCreationUtil.createImpfstoffModerna();
	private final Impfstoff pfizer = TestdataCreationUtil.createImpfstoffPfizer();
	private final Impfstoff janssen = TestdataCreationUtil.createImpfstoffJanssen();

	private final LocalDate date = LocalDate.now();

	// durchgefuehrt
	private final HashMap<Impfstoff, Long> durchgefuerteImpfung1 = new HashMap<>();
	private final HashMap<Impfstoff, Long> durchgefuerteImpfung2 = new HashMap<>();
	private final HashMap<Impfstoff, Long> durchgefuerteImpfungN = new HashMap<>();

	// pendent mit Impfstoff
	private final HashMap<Impfstoff, Long> pendent2 = new HashMap<>();
	private final HashMap<Impfstoff, Long> pendentN = new HashMap<>();

	// pendent Impfstoff unbekannt
	private long pendent1U = 0;
	private long pendent2U = 0;
	private long pendentNU = 0;

	@BeforeAll
	static void setUp() {
		System.setProperty("vacme.mandant", "BE");
	}

	@BeforeEach
	public void before() {

		StatsRepo statsRepoMock = mock(StatsRepo.class);
		StatistikKennzahlEintragRepo statistikKennzahlEintragRepoMock = mock(StatistikKennzahlEintragRepo.class);
		RegistrierungRepo registrierungRepo = mock(RegistrierungRepo.class);
		ImpfslotService impfslotService = mock(ImpfslotService.class);

		durchgefuerteImpfung1.put(moderna, 0L);
		durchgefuerteImpfung1.put(pfizer, 0L);
		durchgefuerteImpfung1.put(janssen, 0L);
		durchgefuerteImpfung2.put(moderna, 0L);
		durchgefuerteImpfung2.put(pfizer, 0L);
		durchgefuerteImpfung2.put(janssen, 0L);
		durchgefuerteImpfungN.put(moderna, 0L);
		durchgefuerteImpfungN.put(pfizer, 0L);
		durchgefuerteImpfungN.put(janssen, 0L);
		durchgefuerteImpfungN.put(janssen, 0L);

		pendent2.put(moderna, 0L);
		pendent2.put(pfizer, 0L);
		pendent2.put(janssen, 0L);
		pendentN.put(moderna, 0L);
		pendentN.put(pfizer, 0L);
		pendentN.put(janssen, 0L);

		Mockito.when(statsRepoMock.getAllZugelasseneImpfstoffeThatSupportTagesstatistik()).thenReturn(List.of(moderna, pfizer, janssen));

		Mockito.doAnswer(invocation -> pendent1U).when(statsRepoMock).getPendentImpfung1(
				KrankheitIdentifier.COVID, odi1, date);
		Mockito.doAnswer(invocation -> pendent2U).when(statsRepoMock).getPendentImpfung2ImpfstoffUnbekannt(
			KrankheitIdentifier.COVID, odi1, date);
		Mockito.doAnswer(invocation -> pendentNU).when(statsRepoMock).getPendentImpfungNImpfstoffUnbekannt(
			KrankheitIdentifier.COVID, odi1, date);

		Mockito.doAnswer(invocation -> pendent2.get(moderna)).when(statsRepoMock).getPendentImpfung2ImpfstoffEmpfohlen(
			KrankheitIdentifier.COVID, moderna, odi1, date);
		Mockito.doAnswer(invocation -> pendent2.get(pfizer)).when(statsRepoMock).getPendentImpfung2ImpfstoffEmpfohlen(
			KrankheitIdentifier.COVID, pfizer, odi1, date);
		Mockito.doAnswer(invocation -> pendent2.get(janssen)).when(statsRepoMock).getPendentImpfung2ImpfstoffEmpfohlen(
			KrankheitIdentifier.COVID, janssen, odi1, date);
		Mockito.doAnswer(invocation -> pendentN.get(moderna)).when(statsRepoMock).getPendentImpfungNImpfstoffEmpfohlen(
				KrankheitIdentifier.COVID, moderna, odi1, date);
		Mockito.doAnswer(invocation -> pendentN.get(pfizer)).when(statsRepoMock).getPendentImpfungNImpfstoffEmpfohlen(
				KrankheitIdentifier.COVID, pfizer, odi1, date);
		Mockito.doAnswer(invocation -> pendentN.get(janssen)).when(statsRepoMock).getPendentImpfungNImpfstoffEmpfohlen(
				KrankheitIdentifier.COVID, janssen, odi1, date);

		Mockito.doAnswer(invocation -> durchgefuerteImpfung1.get(moderna)).when(statsRepoMock).getDurchgefuerteImpfung1(moderna, odi1, date);
		Mockito.doAnswer(invocation -> durchgefuerteImpfung1.get(pfizer)).when(statsRepoMock).getDurchgefuerteImpfung1(pfizer, odi1, date);
		Mockito.doAnswer(invocation -> durchgefuerteImpfung1.get(janssen)).when(statsRepoMock).getDurchgefuerteImpfung1(janssen, odi1, date);
		Mockito.doAnswer(invocation -> durchgefuerteImpfung2.get(moderna)).when(statsRepoMock).getDurchgefuerteImpfung2(moderna, odi1, date);
		Mockito.doAnswer(invocation -> durchgefuerteImpfung2.get(pfizer)).when(statsRepoMock).getDurchgefuerteImpfung2(pfizer, odi1, date);
		Mockito.doAnswer(invocation -> durchgefuerteImpfung2.get(janssen)).when(statsRepoMock).getDurchgefuerteImpfung2(janssen, odi1, date);
		Mockito.doAnswer(invocation -> durchgefuerteImpfungN.get(moderna)).when(statsRepoMock).getDurchgefuerteImpfungN(moderna, odi1, date);
		Mockito.doAnswer(invocation -> durchgefuerteImpfungN.get(pfizer)).when(statsRepoMock).getDurchgefuerteImpfungN(pfizer, odi1, date);
		Mockito.doAnswer(invocation -> durchgefuerteImpfungN.get(janssen)).when(statsRepoMock).getDurchgefuerteImpfungN(janssen, odi1, date);

		statsService = new StatsService(statsRepoMock, statistikKennzahlEintragRepoMock, registrierungRepo, impfslotService);
	}

	private Impfstoff createImpfstoff(@NonNull String name, @NonNull ZulassungsStatus status, int anzahlDosenBenoetigt) {
		Impfstoff impfstoff = new Impfstoff();
		impfstoff.setHersteller(name);
		impfstoff.setName(name);
		impfstoff.setZulassungsStatus(status);
		impfstoff.setAnzahlDosenBenoetigt(anzahlDosenBenoetigt);
		return impfstoff;

	}

	@CsvSource({
		// DURCHGEFUEHRT                          PENDENT
		// 1         2           N                1             2             N
		// M P J      M  p  J     M  P  J         M  P  J  ?    M  P  J  ?    M  P  J  ?
		" 0, 0, 0,    0, 0, 0,    0, 0, 0,        0, 0, 0, 0,   0, 0, 0, 0,   0, 0, 0, 0", //
		" 9, 7, 3,    1, 2, 3,    7, 7, 7,        0, 0, 0, 8,   5, 6, 7, 9,   0, 0, 0,13", //
		"21,55,88,   11,22,33,   37,27,87,        0, 0, 0,45,  25,76, 1, 2,   0, 0, 0,30", //
		"21,55,88,   11,22,33,   37,27,87,        0, 0, 0,45,  25,76, 1, 2,   4, 5, 6,30", //
	})
	@ParameterizedTest
	public void testOdiTagesReport(
		// durchgefuehrt
		int durchg1M, int durchg1P, int durchg1J,
		int durchg2M, int durchg2P, int durchg2J,
		int durchgNM, int durchgNP, int durchgNJ,

		// pendent
		int pendent1M, int pendent1P, int pendent1J, int pendent1U,
		int pendent2M, int pendent2P, int pendent2J, int pendent2U,
		int pendentNM, int pendentNP, int pendentNJ, int pendentNU) {

		// "DB abfuellen" (also: Mock abfuellen)
		setValues(pendent1U, pendent2U, pendentNU, durchg1M, durchg1P, durchg1J, durchg2M, durchg2P, durchg2J, durchgNM, durchgNP, durchgNJ,
			pendent2M, pendent2P, pendent2J, pendentNM, pendentNP, pendentNJ);

		// Report zusammenstellen (also: Mock abfragen - die echte Logik befindet sich in den StatsRepo-Queries und bleibt leider ungetestet)
		ImpfzentrumTagesReportJax reportJax = statsService.getOdiTagesReport(odi1, date);

		ImpfstoffTagesReportJax modernaJax = reportJax.getImpfstoffTagesReportJaxMap().get(0);
		ImpfstoffTagesReportJax pfizerJax = reportJax.getImpfstoffTagesReportJaxMap().get(1);
		ImpfstoffTagesReportJax janssenJax = reportJax.getImpfstoffTagesReportJaxMap().get(2);
		ImpfstoffTagesReportJax unbekanntJax = reportJax.getImpfstoffTagesReportJaxMap().get(3);

		// Werte ueberpruefen
		assertValues(modernaJax, "Spikevax",
			durchg1M, durchg2M, durchgNM, // durchgefuehrt
			pendent1M, pendent2M, pendentNM // pendent
		);
		assertValues(pfizerJax, "Comirnaty",
			durchg1P, durchg2P, durchgNP,
			pendent1P, pendent2P, pendentNP // pendent
		);
		assertValues(janssenJax, "COVID-19 vaccine",
			durchg1J, durchg2J, durchgNJ,
			pendent1J, pendent2J, pendentNJ // pendent
		);
		assertValues(unbekanntJax, null,
			0, 0, 0,
			pendent1U, pendent2U, pendentNU // pendent
		);

	}

	private OrtDerImpfung mockOrtDerImpfung(String gln) {
		OrtDerImpfung ortDerImpfung = new OrtDerImpfung();
		ortDerImpfung.setGlnNummer(gln);
		return ortDerImpfung;
	}

	private void assertValues(ImpfstoffTagesReportJax impfstoffJax, @Nullable String impfstoffName,
		int durchgefuertImpfung1,
		int durchgefuertImpfung2,
		int durchgefuertImpfungN,
		int pendent1,
		int pendent2,
		int pendentN
	) {
		assertEquals(impfstoffName, impfstoffJax.getImpfstoffName());

		assertEquals((long) durchgefuertImpfung1, impfstoffJax.getDurchgefuehrtImpfung1());
		assertEquals((long) durchgefuertImpfung2, impfstoffJax.getDurchgefuehrtImpfung2());
		assertEquals((long) durchgefuertImpfungN, impfstoffJax.getDurchgefuehrtImpfungN());
		assertEquals((long) pendent1, impfstoffJax.getPendentTermin1());
		assertEquals((long) pendent2, impfstoffJax.getPendentTermin2());
		assertEquals((long) pendentN, impfstoffJax.getPendentTerminN());
	}

	private void setValues(int unbekannt1, int unbekannt2, int unbekannt3,
		int d1Mod, int d1Pf, int d1Jan, int d2Mod, int d2Pf, int d2Jan, int dnMod, int dnPf, int dnJan,
		int geplant2Mod, int geplant2Pf, int geplant2Jan,	int geplantNMod, int geplantNPf, int geplantNJan) {

		pendent1U = unbekannt1;
		pendent2U = unbekannt2;
		pendentNU = unbekannt3;
		durchgefuerteImpfung1.put(moderna, (long) d1Mod);
		durchgefuerteImpfung1.put(pfizer, (long) d1Pf);
		durchgefuerteImpfung1.put(janssen, (long) d1Jan);
		durchgefuerteImpfung2.put(moderna, (long) d2Mod);
		durchgefuerteImpfung2.put(pfizer, (long) d2Pf);
		durchgefuerteImpfung2.put(janssen, (long) d2Jan);
		durchgefuerteImpfungN.put(moderna, (long) dnMod);
		durchgefuerteImpfungN.put(pfizer, (long) dnPf);
		durchgefuerteImpfungN.put(janssen, (long) dnJan);
		pendent2.put(moderna, (long) geplant2Mod);
		pendent2.put(pfizer, (long) geplant2Pf);
		pendent2.put(janssen, (long) geplant2Jan);
		pendentN.put(moderna, (long) geplantNMod);
		pendentN.put(pfizer, (long) geplantNPf);
		pendentN.put(janssen, (long) geplantNJan);

	}

}
