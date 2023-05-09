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

import ch.dvbern.oss.vacme.entities.registration.BeruflicheTaetigkeit;
import ch.dvbern.oss.vacme.entities.registration.ChronischeKrankheiten;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Lebensumstaende;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.repo.UmfrageRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class StammdatenServiceZHTest {

	private StammdatenService stammdatenService;

	@BeforeAll
	static void setUp() {
		System.setProperty("vacme.mandant", "ZH");
	}

	@BeforeEach
	public void before() {
		stammdatenService = new StammdatenService(
			Mockito.mock(RegistrierungRepo.class),
			Mockito.mock(ApplicationPropertyService.class),
			Mockito.mock(UmfrageRepo.class));
	}

	@Test
	public void testCalculatePrioritaetA() {
		Fragebogen fragebogenA =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withAlter(75)
				.build();
		Assertions.assertEquals(Prioritaet.A, stammdatenService.calculatePrioritaet(fragebogenA));
	}

	@Test
	public void testCalculatePrioritaetB() {
		Fragebogen fragebogenB =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.SCHWERE_KRANKHEITSVERLAEUFE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withAlter(17)
				.build();
		Assertions.assertEquals(Prioritaet.B, stammdatenService.calculatePrioritaet(fragebogenB));
	}

	@Test
	public void testCalculatePrioritaetC() {
		Fragebogen fragebogenC =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.KEINE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withAlter(65)
				.build();
		Assertions.assertEquals(Prioritaet.C, stammdatenService.calculatePrioritaet(fragebogenC));
	}

	@Test
	public void testCalculatePrioritaetD() {
		Fragebogen fragebogenD =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.KRANKHEIT)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.EINZELHAUSHALT)
				.withAlter(64)
				.build();
		Assertions.assertEquals(Prioritaet.D, stammdatenService.calculatePrioritaet(fragebogenD));
	}

	@Test
	public void testCalculatePrioritaetE() {
		Fragebogen fragebogenE =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.KRANKHEIT)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withAlter(18)
				.build();
		Assertions.assertEquals(Prioritaet.E, stammdatenService.calculatePrioritaet(fragebogenE));
	}

	@Test
	public void testCalculatePrioritaetF() {
		Fragebogen fragebogenF =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.GES_PERSONAL_MIT_PAT_KONTAKT_INTENSIV)
				.withLebensumstaende(Lebensumstaende.EINZELHAUSHALT)
				.withAlter(30)
				.build();
		Assertions.assertEquals(Prioritaet.F, stammdatenService.calculatePrioritaet(fragebogenF));
	}

	@Test
	public void testCalculatePrioritaetG() {
		Fragebogen fragebogenG =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.GES_PERSONAL_MIT_PAT_KONTAKT)
				.withLebensumstaende(Lebensumstaende.EINZELHAUSHALT)
				.withAlter(30)
				.build();
		Assertions.assertEquals(Prioritaet.G, stammdatenService.calculatePrioritaet(fragebogenG));
	}

	@Test
	public void testCalculatePrioritaetH() {
		Fragebogen fragebogenH =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.BERUF_MIT_HOMEOFFICE_MOEGLICH)
				.withLebensumstaende(Lebensumstaende.MIT_BESONDERS_GEFAEHRDETEN_PERSON)
				.withAlter(55)
				.build();
		Assertions.assertEquals(Prioritaet.H, stammdatenService.calculatePrioritaet(fragebogenH));
	}

	@Test
	public void testCalculatePrioritaetI() {
		Fragebogen fragebogenI =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.MIT_BESONDERS_GEFAEHRDETEN_PERSON)
				.withAlter(25)
				.build();
		Assertions.assertEquals(Prioritaet.I, stammdatenService.calculatePrioritaet(fragebogenI));
	}

	@Test
	public void testCalculatePrioritaetK() {
		Fragebogen fragebogenK =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.GEMEINSCHAFTEN)
				.withAlter(60)
				.build();
		Assertions.assertEquals(Prioritaet.K, stammdatenService.calculatePrioritaet(fragebogenK));
	}

	@Test
	public void testCalculatePrioritaetL() {
		Fragebogen fragebogenL =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.MASSENUNTERKUENFTEN)
				.withAlter(20)
				.build();
		Assertions.assertEquals(Prioritaet.L, stammdatenService.calculatePrioritaet(fragebogenL));
	}

	@Test
	public void testCalculatePrioritaetM() {
		Fragebogen fragebogenM =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withAlter(55)
				.build();
		Assertions.assertEquals(Prioritaet.M, stammdatenService.calculatePrioritaet(fragebogenM));
	}

	@Test
	public void testCalculatePrioritaetN() {
		Fragebogen fragebogenN =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withAlter(20)
				.build();
		Assertions.assertEquals(Prioritaet.N, stammdatenService.calculatePrioritaet(fragebogenN));
	}

	@Test
	public void testCalculatePrioritaetO() {
		Fragebogen fragebogenO =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.KRANKHEIT)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.FAMILIENHAUSHALT)
				.withAlter(16)
				.build();
		Assertions.assertEquals(Prioritaet.O, stammdatenService.calculatePrioritaet(fragebogenO));
	}

	@Test
	public void testCalculatePrioritaetP() {
		Fragebogen fragebogenP =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.KRANKHEIT)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.FAMILIENHAUSHALT)
				.withAlter(13)
				.build();
		Assertions.assertEquals(Prioritaet.P, stammdatenService.calculatePrioritaet(fragebogenP));
	}

	@Test
	public void testCalculatePrioritaetQ() {
		Fragebogen fragebogenQ =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.KRANKHEIT)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.FAMILIENHAUSHALT)
				.withAlter(2)
				.build();
		Assertions.assertEquals(Prioritaet.Q, stammdatenService.calculatePrioritaet(fragebogenQ));
	}

	@Test
	public void testCalculatePrioritaetR() {
		Fragebogen fragebogenR =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withAlter(16)
				.build();
		Assertions.assertEquals(Prioritaet.R, stammdatenService.calculatePrioritaet(fragebogenR));
	}

	@Test
	public void testCalculatePrioritaetS() {
		Fragebogen fragebogenS =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withAlter(12)
				.build();
		Assertions.assertEquals(Prioritaet.S, stammdatenService.calculatePrioritaet(fragebogenS));
	}

	@Test
	public void testCalculatePrioritaetT() {
		Fragebogen fragebogenT =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withAlter(0)
				.build();
		Assertions.assertEquals(Prioritaet.T, stammdatenService.calculatePrioritaet(fragebogenT));
	}

	@Test
	public void testCalculatePrioritaetZ() {
		Fragebogen fragebogenZ =
			new FragebogenBuilder()
				.withChronischeKrankheiten(ChronischeKrankheiten.ANDERE)
				.withBeruflicheTaetigkeit(BeruflicheTaetigkeit.ANDERE)
				.withLebensumstaende(Lebensumstaende.ANDERE)
				.withRegistrierungsEingang(RegistrierungsEingang.ORT_DER_IMPFUNG)
				.build();
		Assertions.assertEquals(Prioritaet.Z, stammdatenService.calculatePrioritaet(fragebogenZ));
	}

	static class FragebogenBuilder {
		private ChronischeKrankheiten chronischeKrankheiten;
		private BeruflicheTaetigkeit beruflicheTaetigkeit;
		private Lebensumstaende lebensumstaende;
		private int alter;
		private RegistrierungsEingang registrierungsEingang = RegistrierungsEingang.ONLINE_REGISTRATION;

		public FragebogenBuilder withChronischeKrankheiten(ChronischeKrankheiten chronischeKrankheiten) {
			this.chronischeKrankheiten = chronischeKrankheiten;
			return this;
		}

		public FragebogenBuilder withBeruflicheTaetigkeit(BeruflicheTaetigkeit beruflicheTaetigkeit) {
			this.beruflicheTaetigkeit = beruflicheTaetigkeit;
			return this;
		}

		public FragebogenBuilder withLebensumstaende(Lebensumstaende lebensumstaende) {
			this.lebensumstaende = lebensumstaende;
			return this;
		}

		public FragebogenBuilder withAlter(int alter) {
			this.alter = alter;
			return this;
		}

		public FragebogenBuilder withRegistrierungsEingang(RegistrierungsEingang registrierungsEingang) {
			this.registrierungsEingang = registrierungsEingang;
			return this;
		}

		public Fragebogen build() {
			Fragebogen fragebogen = new Fragebogen();
			fragebogen.setChronischeKrankheiten(this.chronischeKrankheiten);
			fragebogen.setBeruflicheTaetigkeit(this.beruflicheTaetigkeit);
			fragebogen.setLebensumstaende(this.lebensumstaende);
			Registrierung registrierung = new Registrierung();
			registrierung.setRegistrierungsEingang(this.registrierungsEingang);
			registrierung.setGeburtsdatum(LocalDate.now().minusYears(this.alter));
			fragebogen.setRegistrierung(registrierung);
			return fragebogen;
		}
	}
}
