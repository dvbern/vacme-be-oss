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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.impfen.Krankheit;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Geschlecht;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OdiFilter;
import ch.dvbern.oss.vacme.entities.terminbuchung.OdiFilterTyp;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.enums.Kundengruppe;
import ch.dvbern.oss.vacme.repo.BenutzerRepo;
import ch.dvbern.oss.vacme.repo.ImpfstoffRepo;
import ch.dvbern.oss.vacme.repo.KrankheitRepo;
import ch.dvbern.oss.vacme.repo.OdiFilterRepo;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.rest.auth.BenutzerSyncFilter;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.testing.MariaDBProfile;
import ch.dvbern.oss.vacme.testing.MariaDBTestResource;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.vacme.entities.terminbuchung.OdiFilterTyp.AGE_FILTER;
import static ch.dvbern.oss.vacme.entities.terminbuchung.OdiFilterTyp.GESCHLECHT_FILTER;
import static ch.dvbern.oss.vacme.entities.terminbuchung.OdiFilterTyp.PRIORITAET_FILTER;
import static ch.dvbern.oss.vacme.util.TestdataCreationUtil.createImpfstoffEncepur;
import static ch.dvbern.oss.vacme.util.TestdataCreationUtil.createImpfstoffJanssen;
import static ch.dvbern.oss.vacme.util.TestdataCreationUtil.createImpfstoffModerna;
import static ch.dvbern.oss.vacme.util.TestdataCreationUtil.createImpfstoffPfizer;
import static ch.dvbern.oss.vacme.util.TestdataCreationUtil.createImpfstoffPfizerKinder;
import static ch.dvbern.oss.vacme.util.TestdataCreationUtil.createOrtDerImpfung;

@QuarkusTestResource(MariaDBTestResource.class)
@TestProfile(MariaDBProfile.class)
@QuarkusTest
class OdiFilterServiceIT {

	private static final Benutzer BENUTZER = TestdataCreationUtil.createBenutzer("Tester", "Fritz", "123456");
	private static final Impfstoff IMPFSTOFF_MODERNA = createImpfstoffModerna();
	private static final Impfstoff IMPFSTOFF_JANSSEN = createImpfstoffJanssen();
	private static final Impfstoff IMPFSTOFF_PFIZER = createImpfstoffPfizer();
	private static final Impfstoff IMPFSTOFF_PFIZER_KINDER = createImpfstoffPfizerKinder();
	private static final Impfstoff IMPFSTOFF_ENCEPUR = createImpfstoffEncepur();

	@Inject
	BenutzerSyncFilter benutzerSyncFilter;

	@Inject
	BenutzerRepo benutzerRepo;

	@Inject
	OdiFilterService odiFilterService;

	@Inject
	OdiFilterRepo odiFilterRepo;

	@Inject
	OrtDerImpfungRepo ortDerImpfungRepo;

	@Inject
	ImpfstoffRepo impfstoffRepo;

	@Inject
	KrankheitRepo krankheitRepo;

	@Inject
	Flyway flyway;

	@BeforeEach
	void setUp() {
		benutzerSyncFilter.switchToAdmin();
		flyway.clean();
		flyway.migrate();

		benutzerRepo.create(BENUTZER);

		final Krankheit krankheitCovid = krankheitRepo.getOrCreateByIdentifier(
			KrankheitIdentifier.COVID,
			KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG);
		Set<Krankheit> krankheitenListCovid = new HashSet<>();
		krankheitenListCovid.add(krankheitCovid);
		IMPFSTOFF_MODERNA.setKrankheiten(krankheitenListCovid);
		IMPFSTOFF_JANSSEN.setKrankheiten(krankheitenListCovid);
		IMPFSTOFF_PFIZER.setKrankheiten(krankheitenListCovid);
		IMPFSTOFF_PFIZER_KINDER.setKrankheiten(krankheitenListCovid);
		impfstoffRepo.create(IMPFSTOFF_MODERNA);
		impfstoffRepo.create(IMPFSTOFF_JANSSEN);
		impfstoffRepo.create(IMPFSTOFF_PFIZER);
		impfstoffRepo.create(IMPFSTOFF_PFIZER_KINDER);

		final Krankheit krankheitFSME = krankheitRepo.getOrCreateByIdentifier(
			KrankheitIdentifier.FSME,
			KantonaleBerechtigung.LEISTUNGSERBRINGER);
		Set<Krankheit> krankheitenListFsme = new HashSet<>();
		krankheitenListFsme.add(krankheitFSME);
		IMPFSTOFF_ENCEPUR.setKrankheiten(krankheitenListFsme);
		impfstoffRepo.create(IMPFSTOFF_ENCEPUR);

		// Alle bereis in anderen Tests erstellten ODIs wieder loeschen
		final List<OrtDerImpfung> allOdis = ortDerImpfungRepo.findAll();
		for (OrtDerImpfung odi : allOdis) {
			ortDerImpfungRepo.delete(odi.toId());
		}
	}

	@Test
	void passesAgeFilterTest() {
		OdiFilter filter = createOdiFilter(AGE_FILTER, null, new BigDecimal(16), new BigDecimal(18));

		@NonNull Fragebogen fragebogen = new Fragebogen();
		Registrierung reg = new Registrierung();

		reg.setGeburtsdatum(LocalDate.now().minusYears(17));
		fragebogen.setRegistrierung(reg);
		boolean passes;
		passes = odiFilterService.passesAgeFilter(filter, fragebogen);
		Assertions.assertTrue(passes, "should be between");

		reg.setGeburtsdatum(LocalDate.now().minusYears(13));

		passes = odiFilterService.passesAgeFilter(filter, fragebogen);
		Assertions.assertFalse(passes, "should be too young");

		reg.setGeburtsdatum(LocalDate.now().minusYears(20));

		passes = odiFilterService.passesAgeFilter(filter, fragebogen);
		Assertions.assertFalse(passes, "should be too old");

		reg.setGeburtsdatum(LocalDate.now().minusYears(18));

		passes = odiFilterService.passesAgeFilter(filter, fragebogen);
		Assertions.assertFalse(passes, "should be exatly at max age");

		reg.setGeburtsdatum(LocalDate.now().minusYears(18).plusDays(1));

		passes = odiFilterService.passesAgeFilter(filter, fragebogen);
		Assertions.assertTrue(passes, "should be exatly at max age (one day before should be ok)");

		reg.setGeburtsdatum(LocalDate.now().minusYears(16));

		passes = odiFilterService.passesAgeFilter(filter, fragebogen);
		Assertions.assertFalse(passes, "should be exactly at min age");

		reg.setGeburtsdatum(LocalDate.now().minusYears(16).minusDays(1));

		passes = odiFilterService.passesAgeFilter(filter, fragebogen);
		Assertions.assertTrue(passes, "should be exactly at min age (one day after should be ok)");
	}

	@Test
	void testFilterForRegistrationByAge() {
		int threshholdAge = 16;

		// Odis vorbereiten fuer Personen ab 16 mit Moderna und Jansson
		final int anzahlOdisAb16 = 10;
		OdiFilter ageFilter = createOdiFilter(AGE_FILTER, null, BigDecimal.valueOf(threshholdAge), null);
		for (int i = 0; i < anzahlOdisAb16; i++) {
			createOdiHavingImpfstoffAndFilters(Set.of(IMPFSTOFF_MODERNA, IMPFSTOFF_JANSSEN), Set.of(ageFilter), Kundengruppe.UNBEKANNT);
		}

		// Grundimmunisierung soll keine finden wenn zu jung
		ImpfinformationBuilder builder = new ImpfinformationBuilder()
			.create(KrankheitIdentifier.COVID)
			.withAge(12);
		Set<UUID> usedImpfstoffe = new HashSet<>();

		Assertions.assertTrue(DateUtil.getAge(builder.getInfos().getRegistrierung().getGeburtsdatum()) < threshholdAge);
		Assertions.assertFalse(ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(builder.getInfos().getImpfdossier().getDossierStatus()));
		List<OrtDerImpfung> ortDerImpfungs = searchOdisAndFilter(builder.getInfos(), builder.getFragebogen(), false, usedImpfstoffe);
		Assertions.assertEquals(0, ortDerImpfungs.size(), "Reg is too young so should find no odi");

		// Boosterimpfung soll immer noch keine finden wenn zu jung
		builder = builder
			.withImpfung1(LocalDate.now().minusMonths(11), IMPFSTOFF_MODERNA)
			.withImpfung2(LocalDate.now().minusMonths(7), IMPFSTOFF_MODERNA);
		usedImpfstoffe.add(IMPFSTOFF_MODERNA.getId());

		Assertions.assertEquals(ImpfdossierStatus.ABGESCHLOSSEN, builder.getInfos().getImpfdossier().getDossierStatus());
		List<OrtDerImpfung> tooYoungOidsBstr = searchOdisAndFilter(builder.getInfos(), builder.getFragebogen(), true, usedImpfstoffe);
		Assertions.assertEquals(0, tooYoungOidsBstr.size(), "Reg is too young so should find no odi");

		// Grundimmunisierung soll alle finden wenn alt genug
		builder = new ImpfinformationBuilder()
			.create(KrankheitIdentifier.COVID)
			.withAge(21);
		usedImpfstoffe.clear();

		Assertions.assertTrue(DateUtil.getAge(builder.getInfos().getRegistrierung().getGeburtsdatum()) > threshholdAge);
		Assertions.assertFalse(ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(builder.getInfos().getImpfdossier().getDossierStatus()));
		List<OrtDerImpfung> odisForOldEnough = searchOdisAndFilter(builder.getInfos(), builder.getFragebogen(), false, usedImpfstoffe);
		Assertions.assertEquals(anzahlOdisAb16, odisForOldEnough.size(), "Reg is old ehough so should find all odis");

		// Boosterimpfung soll immer noch alle finden wenn Impfstoff vorhanden und Alter erfuellt
		builder = builder
			.withImpfung1(LocalDate.now().minusMonths(11), IMPFSTOFF_MODERNA)
			.withImpfung2(LocalDate.now().minusMonths(7), IMPFSTOFF_MODERNA);
		usedImpfstoffe.add(IMPFSTOFF_MODERNA.getId());

		List<OrtDerImpfung> odisForOldEnoughBstr = searchOdisAndFilter(builder.getInfos(), builder.getFragebogen(), true, usedImpfstoffe);
		Assertions.assertEquals(anzahlOdisAb16, odisForOldEnoughBstr.size(), "Reg is old ehough so should find all odis");
	}

	@Test
	void passesGeschlechtFilterTest() {
		final OdiFilter filter = createOdiFilter(GESCHLECHT_FILTER, "WEIBLICH", null, null);

		@NonNull Fragebogen fragebogen = new Fragebogen();
		Registrierung reg = new Registrierung();

		reg.setGeburtsdatum(LocalDate.now().minusYears(17));
		fragebogen.setRegistrierung(reg);
		reg.setGeschlecht(Geschlecht.WEIBLICH);
		boolean passes;
		passes = odiFilterService.passesGeschlechtFilter(filter, fragebogen);
		Assertions.assertTrue(passes, "should ok");

		reg.setGeschlecht(Geschlecht.MAENNLICH);

		passes = odiFilterService.passesGeschlechtFilter(filter, fragebogen);
		Assertions.assertFalse(passes, "maennlich should not pass");

		reg.setGeschlecht(Geschlecht.ANDERE);

		passes = odiFilterService.passesGeschlechtFilter(filter, fragebogen);
		Assertions.assertFalse(passes, "andere should not pass");
	}

	@Test
	void passesPrioritaetFilterTest() {
		final OdiFilter filter = createOdiFilter(PRIORITAET_FILTER, "A;B;D", null, null);
		@NonNull Fragebogen fragebogen = new Fragebogen();
		Registrierung reg = new Registrierung();

		reg.setGeburtsdatum(LocalDate.now().minusYears(17));
		fragebogen.setRegistrierung(reg);
		reg.setPrioritaet(Prioritaet.A);
		boolean passes;
		passes = odiFilterService.passesPrioritaetFilter(filter, fragebogen);
		Assertions.assertTrue(passes, "Prio A should be ok");

		reg.setPrioritaet(Prioritaet.B);

		passes = odiFilterService.passesPrioritaetFilter(filter, fragebogen);
		Assertions.assertTrue(passes, "Prio B should be ok");
		reg.setPrioritaet(Prioritaet.C);

		passes = odiFilterService.passesPrioritaetFilter(filter, fragebogen);
		Assertions.assertFalse(passes, "Prio C should fail");

		reg.setPrioritaet(Prioritaet.D);

		passes = odiFilterService.passesPrioritaetFilter(filter, fragebogen);
		Assertions.assertTrue(passes, "Prio D should be ok");
	}

	@Test
	void passesImpfstoffFilter() {
		OrtDerImpfung ortDerImpfung = new OrtDerImpfung();
		ortDerImpfung.setImpfstoffs(Set.of(IMPFSTOFF_MODERNA, IMPFSTOFF_PFIZER));
		boolean available = odiFilterService.isOdiAvailableForOneOfImpfstoff(ortDerImpfung, Collections.emptyList());
		Assertions.assertFalse(available);

		ortDerImpfung.setImpfstoffs(Set.of(IMPFSTOFF_MODERNA));
		Assertions.assertTrue(odiFilterService.isOdiAvailableForOneOfImpfstoff(ortDerImpfung,
			List.of(IMPFSTOFF_MODERNA.getId())));

		Assertions.assertFalse(odiFilterService.isOdiAvailableForOneOfImpfstoff(ortDerImpfung,
			List.of(IMPFSTOFF_PFIZER.getId())));

		ortDerImpfung.setImpfstoffs(Collections.emptySet());
		Assertions.assertFalse(odiFilterService.isOdiAvailableForOneOfImpfstoff(ortDerImpfung,
			List.of(IMPFSTOFF_PFIZER.getId())));
		Assertions.assertFalse(odiFilterService.isOdiAvailableForOneOfImpfstoff(ortDerImpfung,
			List.of(IMPFSTOFF_MODERNA.getId())));
	}

	@Test
	void testFilterForRegistration() {
		// Odis vorbereiten mit Moderna und Janssen
		final int anzahlOdisWithModernaAndJanssen = 500;
		for (int i = 0; i < anzahlOdisWithModernaAndJanssen; i++) {
			createOdiHavingImpfstoff(Set.of(IMPFSTOFF_MODERNA, IMPFSTOFF_JANSSEN));
		}

		// Vor der ersten Impfung
		ImpfinformationBuilder builder = new ImpfinformationBuilder().create(KrankheitIdentifier.COVID);
		Set<UUID> usedImpfstoffe = new HashSet<>();

		List<OrtDerImpfung> ortDerImpfungs = ortDerImpfungRepo.findOdisAvailableForRegistrierung(
			builder.getInfos(), false, usedImpfstoffe, null);
		Assertions.assertEquals(anzahlOdisWithModernaAndJanssen, ortDerImpfungs.size());

		// Grundimmunisierung mit Moderna: Wir wollen alle ODIs finden
		builder = builder
			.withImpfung1(LocalDate.now().minusMonths(11), IMPFSTOFF_MODERNA)
			.withImpfung2(LocalDate.now().minusMonths(7), IMPFSTOFF_MODERNA);
		usedImpfstoffe.add(IMPFSTOFF_MODERNA.getId());

		Assertions.assertEquals(anzahlOdisWithModernaAndJanssen, ortDerImpfungRepo.findOdisAvailableForRegistrierung(
			builder.getInfos(), true, usedImpfstoffe, null).size());

		// Zusaetzliches ODI mit anderem Impfstoff: Dieses soll nicht (zusaetzlich) gefunden werden
		createOdiHavingImpfstoff(Set.of(IMPFSTOFF_PFIZER_KINDER));

		Assertions.assertEquals(anzahlOdisWithModernaAndJanssen, ortDerImpfungRepo.findOdisAvailableForRegistrierung(
			builder.getInfos(), true, usedImpfstoffe, null).size());
	}

	@Test
	void testFilterImpfstoffBetweenImpfungen() {
		int anzahlOdisWithModernaAndJansson = 2000;
		int anzahlOdisOnlyJanssen = 20;
		Set<UUID> usedImpfstoffe = new HashSet<>();

		ImpfinformationBuilder builder = new ImpfinformationBuilder().create(KrankheitIdentifier.COVID)
			.withImpfung1(LocalDate.now().minusMonths(7), IMPFSTOFF_MODERNA);
		usedImpfstoffe.add(IMPFSTOFF_MODERNA.getId());

		Assertions.assertNotNull(builder.getInfos().getImpfung1());
		Assertions.assertEquals(ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT, builder.getInfos().getImpfdossier().getDossierStatus());

		for (int i = 0; i < anzahlOdisWithModernaAndJansson; i++) {
			createOdiHavingImpfstoff(Set.of(IMPFSTOFF_MODERNA, IMPFSTOFF_JANSSEN));
		}
		List<OrtDerImpfung> ortDerImpfungs = ortDerImpfungRepo.findOdisAvailableForRegistrierung(
			builder.getInfos(), false, usedImpfstoffe, null);
		Assertions.assertEquals(anzahlOdisWithModernaAndJansson, ortDerImpfungs.size());

		for (int i = 0; i < anzahlOdisOnlyJanssen; i++) {
			createOdiHavingImpfstoff(Set.of(IMPFSTOFF_JANSSEN));
		}
		List<OrtDerImpfung> notModernaOdisFiltered = ortDerImpfungRepo.findOdisAvailableForRegistrierung(
			builder.getInfos(), false, usedImpfstoffe, null);
		Assertions.assertEquals(anzahlOdisWithModernaAndJansson, notModernaOdisFiltered.size(),
			"Odis ohne Moderna sollen nicht (zusaetzlich) gefunden werden");
	}

	@Test
	void filterWellForGalenica() {
		// Odis vorbereiten fuer alle Kundengruppen
		createOdiHavingImpfstoffAndKundengruppe(Set.of(IMPFSTOFF_ENCEPUR), Kundengruppe.GALENICA);
		createOdiHavingImpfstoffAndKundengruppe(Set.of(IMPFSTOFF_ENCEPUR), Kundengruppe.GALENICA);
		createOdiHavingImpfstoffAndKundengruppe(Set.of(IMPFSTOFF_ENCEPUR), Kundengruppe.DROPA);
		createOdiHavingImpfstoffAndKundengruppe(Set.of(IMPFSTOFF_ENCEPUR), Kundengruppe.UNBEKANNT);

		ImpfinformationBuilder builder = new ImpfinformationBuilder().create(KrankheitIdentifier.FSME);

		List<OrtDerImpfung> alleOdis = ortDerImpfungRepo.findOdisAvailableForRegistrierung(
			builder.getInfos(), true, Set.of(IMPFSTOFF_ENCEPUR.getId()), null);
		Assertions.assertEquals(4, alleOdis.size());

		List<OrtDerImpfung> galenicaOdis = ortDerImpfungRepo.findOdisAvailableForRegistrierung(
			builder.getInfos(), true, Set.of(IMPFSTOFF_ENCEPUR.getId()), Kundengruppe.GALENICA);
		Assertions.assertEquals(2, galenicaOdis.size());
	}

	private OdiFilter createOdiFilter(
		@NonNull OdiFilterTyp typ,
		@Nullable String stringArgument,
		@Nullable BigDecimal minimalWert,
		@Nullable BigDecimal maximalWert
	) {
		OdiFilter filter = new OdiFilter();
		filter.setTyp(typ);
		filter.setStringArgument(stringArgument);
		filter.setMinimalWert(minimalWert);
		filter.setMaximalWert(maximalWert);
		odiFilterRepo.create(filter);
		return filter;
	}

	@NonNull
	private OrtDerImpfung createOdiHavingImpfstoff(
		@NonNull Set<Impfstoff> availableImpfstoffe
	) {
		return createOdiHavingImpfstoffAndKundengruppe(availableImpfstoffe, Kundengruppe.UNBEKANNT);
	}

	@NonNull
	private OrtDerImpfung createOdiHavingImpfstoffAndKundengruppe(
		@NonNull Set<Impfstoff> availableImpfstoffe,
		@NonNull Kundengruppe kundengruppe
	) {
		final OdiFilter filter = createOdiFilter(PRIORITAET_FILTER, "A;B;D", null, null);
		return createOdiHavingImpfstoffAndFilters(availableImpfstoffe, Set.of(filter), kundengruppe);
	}

	@NonNull
	private OrtDerImpfung createOdi() {
		OrtDerImpfung ortDerImpfung = createOrtDerImpfung();
		ortDerImpfung.setKundengruppe(Kundengruppe.UNBEKANNT);
		ortDerImpfung.setBooster(true);
		ortDerImpfung.setAdresse(TestdataCreationUtil.createAdresse());
		ortDerImpfung.setIdentifier(UUID.randomUUID().toString());
		ortDerImpfung.setFachverantwortungbabKeyCloakId(BENUTZER.getId().toString());
		ortDerImpfung.setOeffentlich(true);
		ortDerImpfung.setDeaktiviert(false);
		ortDerImpfungRepo.create(ortDerImpfung);
		return ortDerImpfung;
	}

	@NonNull
	private OrtDerImpfung createOdiHavingImpfstoffAndFilters(
		@NonNull Set<Impfstoff> availableImpfstoffe,
		@NonNull Set<OdiFilter> filters,
		@NonNull Kundengruppe kundengruppe
	) {
		OrtDerImpfung ortDerImpfung = createOdi();
		ortDerImpfung.setImpfstoffs(availableImpfstoffe);
		Set<Krankheit> krankheitenList = availableImpfstoffe
			.stream()
			.flatMap(impfstoff -> impfstoff.getKrankheiten().stream())
			.collect(Collectors.toSet());
		ortDerImpfung.setKrankheiten(krankheitenList);
		ortDerImpfung.setFilters(filters);
		ortDerImpfung.setKundengruppe(kundengruppe);
		ortDerImpfungRepo.update(ortDerImpfung);
		return ortDerImpfung;
	}

	private List<OrtDerImpfung> searchOdisAndFilter(
		@NonNull ImpfinformationDto infos,
		@NonNull Fragebogen fragebogen,
		boolean isBooster,
		@NonNull Set<UUID> usedImpfstoffe) {
		return
			ortDerImpfungRepo.findOdisAvailableForRegistrierung(infos, isBooster, usedImpfstoffe, null)
				.stream()
				.filter(odi -> odiFilterService.registrierungPassesAllOdiFilters(odi, fragebogen))
				.collect(Collectors.toList());
	}
}
