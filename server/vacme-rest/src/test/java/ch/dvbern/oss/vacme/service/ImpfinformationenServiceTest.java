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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.embeddables.DateTimeRange;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Personenkontrolle;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.helper.TestImpfstoff;
import ch.dvbern.oss.vacme.jax.registration.ExternGeimpftJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfkontrolleJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfstoffJax;
import ch.dvbern.oss.vacme.repo.ExternesZertifikatRepo;
import ch.dvbern.oss.vacme.repo.FragebogenRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfempfehlungChGrundimmunisierungRepo;
import ch.dvbern.oss.vacme.repo.ImpfstoffRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.KrankheitRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.rest.auth.UserPrincipalImpl;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.mappers.TestUtil;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationDtoRecreator;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXTERNESZERTIFIKAT;
import static ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_GENESEN_PLUS_VACME;
import static ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_PLUS_VACME;
import static ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ABGESCHLOSSEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMMUNISIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.KONTROLLIERT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.NEU;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;

public class ImpfinformationenServiceTest {

	private ImpfinformationenService impfinformationenService;
	private ImpfstoffService impfstoffService;
	private ImpfungRepo impfungRepo;
	private ImpfdossierRepo impfdossierRepo;
	private KrankheitRepo krankheitRepo;
	private ExternesZertifikatService externesZertifikatService;
	private ExternesZertifikatRepo externesZertifikatRepo;
	private ImpfdokumentationService impfdokumentationService;
	private ImpfkontrolleService impfkontrolleService;
	private VacmeSettingsService vacmeSettingsService;

	@Nullable
	private Fragebogen dbFragebogen;
	@Nullable
	private Registrierung dbRegistrierung;
	@Nullable
	private Impfung dbImpfung1;
	@Nullable
	private Impfung dbImpfung2;
	@Nullable
	private Impfdossier dbImpfdossier;
	@Nullable
	private List<Impfung> dbBoosterImpfungen = new LinkedList<>();
	@Nullable
	private ExternesZertifikat dbExternesZertifikat;
	// end

	@BeforeAll
	static void setUp() {
		System.setProperty("vacme.mandant", "BE");
	}

	@BeforeEach
	public void before() {
		resetData();
		setupObjects();
		mockServices();
	}

	private void resetData() {
		dbImpfung1 = null;
		dbImpfung2 = null;
		dbBoosterImpfungen = new ArrayList<>();
		dbImpfdossier = null;
		dbFragebogen = null;
		dbRegistrierung = null;
		dbExternesZertifikat = null;
	}

	public void setupObjects() {
		dbRegistrierung = new Registrierung();
		Impfdossier dossier = new Impfdossier();
		dossier.setDossierStatus(NEU);
		dossier.setKrankheitIdentifier(KrankheitIdentifier.COVID);
		dossier.setRegistrierung(dbRegistrierung);
		dossier.setPersonenkontrolle(new Personenkontrolle());
		dbImpfdossier = dossier;
		dbFragebogen = new Fragebogen();
		dbFragebogen.setRegistrierung(dbRegistrierung);
	}

	public void mockServices() {

		// VacmeSettingsService
		vacmeSettingsService = Mockito.mock(VacmeSettingsService.class);

		// ImpfstoffService
		ImpfstoffRepo impfstoffRepo = Mockito.mock(ImpfstoffRepo.class);
		ImpfempfehlungChGrundimmunisierungRepo empfehlungRepo = Mockito.mock(ImpfempfehlungChGrundimmunisierungRepo.class);
		KrankheitRepo krankheitRepo = Mockito.mock(KrankheitRepo.class);
		impfstoffService = new ImpfstoffService(impfstoffRepo, empfehlungRepo, krankheitRepo);
		// -- get
		Mockito.when(impfstoffRepo.getById(any())).thenAnswer(invocation -> {
			ID<Impfstoff> argument = invocation.getArgument(0, ID.class);
			return Optional.ofNullable(TestImpfstoff.getImpfstoffById(argument.getId()));
		});

		// ImpfungRepo
		impfungRepo = Mockito.mock(ImpfungRepo.class);
		// -- get infos
		Mockito.doAnswer(invocation -> Optional.of(
			new ImpfinformationDto(KrankheitIdentifier.COVID, dbRegistrierung, dbImpfung1, dbImpfung2, dbImpfdossier, dbBoosterImpfungen, dbExternesZertifikat)
			)).when(impfungRepo)
			.getImpfinformationenOptional(nullable(String.class), any());
		// -- get boosterimpfungen
		Mockito.when(impfungRepo.getBoosterImpfungen(any(), any()))
			.thenReturn(dbBoosterImpfungen);
		Mockito.doAnswer(invocation -> {
			Impftermin termin = invocation.getArgument(0, Impftermin.class);
			if (dbImpfung1 != null && termin.equals(dbImpfung1.getTermin())) {
				return Optional.of(dbImpfung1);
			}
			if (dbImpfung2 != null && termin.equals(dbImpfung2.getTermin())) {
				return Optional.of(dbImpfung2);
			}
			if (dbBoosterImpfungen != null) {
				return Optional.ofNullable(dbBoosterImpfungen.stream().filter(impfung -> termin.equals(impfung.getTermin())).findFirst().orElse(null));
			}
			return Optional.empty();
		})
			.when(impfungRepo).getByImpftermin(any());
		// TODO create/update/delete Impfung

		// ImpfdossierRepo
		impfdossierRepo = Mockito.mock(ImpfdossierRepo.class);
		// -- find
		Mockito.doAnswer(invocation -> Optional.ofNullable(dbImpfdossier)).when(impfdossierRepo)
			.findImpfdossierForReg(any(), any());
		// -- create
		Mockito.doAnswer(invocation -> {
			dbImpfdossier = invocation.getArgument(0, Impfdossier.class);
			return null;
		})
			.when(impfdossierRepo).create(any());
		// -- createEintrag
		Mockito.doAnswer(invocation -> {
			var eintrag = invocation.getArgument(0, Impfdossiereintrag.class);
			assert dbImpfdossier != null;
			// dbImpfdossier.getImpfdossierEintraege().add(eintrag);
			return null;
		})
			.when(impfdossierRepo).createEintrag(any());
		// -- addEintrag
		Mockito.doCallRealMethod()
			.when(impfdossierRepo).addEintrag(any(), any());
		// -- getOrCreateImpfdossier
		Mockito.doCallRealMethod()
			.when(impfdossierRepo).getOrCreateImpfdossier(any(), any());
		Mockito.doCallRealMethod()
			.when(impfdossierRepo).createImpfdossier(any(), any());

		// ImpfdossierService
		final ImpfdossierService impfdossierServiceMock = Mockito.mock(ImpfdossierService.class);
		// -- ermittleLetztenStatusVorKontrolle1 (freigegeben/registriert/gebucht..)
		Mockito.doAnswer(invocation -> FREIGEGEBEN)
			.when(impfdossierServiceMock).ermittleLetztenDossierStatusVorKontrolle1(any());

		// RegistrierungService
		RegistrierungService registrierungService = Mockito.mock(RegistrierungService.class);

		// KrankheitService
		KrankheitService krankheitService = Mockito.mock(KrankheitService.class);
		Mockito.when(krankheitService.getKantonaleBerechtigung(any()))
			.thenReturn(KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG);

		// ImpfinformationenService
		impfinformationenService = new ImpfinformationenService(
			impfungRepo,
			impfdossierRepo);

		// ExternesZertifikatRepo
		externesZertifikatRepo = Mockito.mock(ExternesZertifikatRepo.class);
		// -- find
		Mockito.doAnswer(invocation -> Optional.ofNullable(dbExternesZertifikat))
			.when(externesZertifikatRepo).findExternesZertifikatForDossier(any());
		// -- create
		Mockito.doAnswer(invocation -> {
			dbExternesZertifikat = invocation.getArgument(0, ExternesZertifikat.class);
			return dbExternesZertifikat;
		})
			.when(externesZertifikatRepo).create(any(ExternesZertifikat.class));
		// -- remove
		Mockito.doAnswer(invocation -> dbExternesZertifikat = null).when(externesZertifikatRepo).remove(any());
		// -- update
		Mockito.doAnswer(invocation -> {
			dbExternesZertifikat = invocation.getArgument(0, ExternesZertifikat.class);
			return dbExternesZertifikat;
		})
			.when(externesZertifikatRepo).update(any(ExternesZertifikat.class));

		// UserPrincipal
		UserPrincipal userPrincipal = Mockito.mock(UserPrincipalImpl.class);
		Benutzer benutzer = mockBenutzer("1", mockOrtDerImpfung("1"));
		Mockito.when(userPrincipal.getBenutzerOrThrowException()).thenReturn(benutzer);

		// ExternesZertifikatService
		externesZertifikatService = new ExternesZertifikatService(
			externesZertifikatRepo,
			impfstoffService,
			userPrincipal,
			impfinformationenService,
			Mockito.mock(BoosterService.class),
			Mockito.mock(DossierService.class),
			impfdossierServiceMock);

		// TerminbuchungService
		TerminbuchungService terminbuchungService = Mockito.mock(TerminbuchungService.class);
		Mockito.doAnswer(invocation -> {
			Impftermin termin = new Impftermin();
			Impfslot slot = new Impfslot();
			slot.setZeitfenster(DateTimeRange.of(LocalDateTime.now(), LocalDateTime.now()));
			termin.setImpfslot(slot);
			return termin;
		}).when(terminbuchungService).createOnDemandImpftermin(any(), any(), any(), any());

		// TerminRepo
		ImpfterminRepo impfterminRepo = Mockito.mock(ImpfterminRepo.class);
		Mockito.doAnswer(invocation -> {
			Impftermin termin1 = invocation.getArgument(1, Impftermin.class);
			Assertions.assertNotNull(dbImpfdossier);
			dbImpfdossier.getBuchung().setImpftermin1FromImpfterminRepo(termin1);
			return null;
		}).when(impfterminRepo).termin1Speichern(any(), any());
		Mockito.doAnswer(invocation -> {
			Impftermin termin2 = invocation.getArgument(1, Impftermin.class);
			Assertions.assertNotNull(dbImpfdossier);
			dbImpfdossier.getBuchung().setImpftermin2FromImpfterminRepo(termin2);
			return null;
		}).when(impfterminRepo).termin2Speichern(any(), any());
		Mockito.doAnswer(invocation -> {
			Impfdossiereintrag eintrag = invocation.getArgument(0, Impfdossiereintrag.class);
			Impftermin termin = invocation.getArgument(1, Impftermin.class);
			eintrag.setImpfterminFromImpfterminRepo(termin);
			return null;
		}).when(impfterminRepo).boosterTerminSpeichern(any(), any());

		// ImpfdokumentationService
		impfdokumentationService = new ImpfdokumentationService(
			impfungRepo,
			impfterminRepo,
			terminbuchungService,
			impfinformationenService,
			Mockito.mock(UserPrincipal.class),
			Mockito.mock(BoosterService.class),
			krankheitService,
			vacmeSettingsService);
		Mockito.when(vacmeSettingsService.getValidateSameDayImpfungen()).thenReturn(Boolean.FALSE);

		// ImpfkontrolleService
		impfkontrolleService = new ImpfkontrolleService(
			Mockito.mock(FragebogenRepo.class),
			Mockito.mock(RegistrierungRepo.class),
			Mockito.mock(StammdatenService.class),
			impfungRepo,
			Mockito.mock(SmsService.class),
			impfterminRepo,
			Mockito.mock(ImpfdossierRepo.class),
			Mockito.mock(ZertifikatService.class),
			impfinformationenService,
			externesZertifikatService,
			Mockito.mock(BoosterService.class),
			impfdossierServiceMock,
			vacmeSettingsService
		);
		Mockito.when(vacmeSettingsService.getValidateSameDayKontrolle()).thenReturn(Boolean.FALSE);
	}

	private Benutzer mockBenutzer(String gln, OrtDerImpfung ortDerImpfung) {
		Benutzer benutzer = Benutzer.fromEmail("benutzer" + gln + "@test.ch");
		benutzer.setGlnNummer(gln);
		benutzer.setOrtDerImpfung(Set.of(ortDerImpfung));
		return benutzer;
	}

	private OrtDerImpfung mockOrtDerImpfung(String gln) {
		OrtDerImpfung ortDerImpfung = new OrtDerImpfung();
		ortDerImpfung.setGlnNummer(gln);
		return ortDerImpfung;
	}

	// Eines von mehreren Szenarien zum Testen der currentKontrolle etc.
	// Weitere Szenarien: Zweitimpfung, die schon Booster ist; mit/ohne externem Zertifikat; Impfungen loeschen, Impfstoff wechseln...
	@Test
	public void testCurrentKontrolle() {
		Assertions.assertNotNull(dbFragebogen);
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen("1", KrankheitIdentifier.COVID);
		Personenkontrolle pk = infos.getImpfdossier().getOrCreatePersonenkontrolle();
		runAssertions(NEU, 0, 1, false, null);
		assertNumbers(false, false, null, null);

		// Erstimpfung Moderna
		// -- vorher
		assertCurrentKontrolleUndImpfung(1, null, pk.getKontrolleTermin1(), null, dbImpfung1);
		addKontrolle(Impffolge.ERSTE_IMPFUNG, createExternGeimpftJaxEmpty());
		assertCurrentKontrolleUndImpfung(1, pk.getKontrolleTermin1(), pk.getKontrolleTermin1(), null, dbImpfung1,
			Impffolge.ERSTE_IMPFUNG, 1);
		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ERSTE_IMPFUNG, true);
		assertCurrentKontrolleUndImpfung(2, null, pk.getKontrolleTermin2(), dbImpfung1, dbImpfung1);

		// Zweitimpfung (grundimmun.)
		addKontrolle(Impffolge.ZWEITE_IMPFUNG, createExternGeimpftJaxEmpty());
		assertCurrentKontrolleUndImpfung(2, pk.getKontrolleTermin2(), pk.getKontrolleTermin2(), dbImpfung1, dbImpfung1,
			Impffolge.ZWEITE_IMPFUNG, 2);
		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ZWEITE_IMPFUNG, true);
		assertCurrentKontrolleUndImpfung(3, null, null, dbImpfung2, dbImpfung2);

		// Booster (3)
		addKontrolle(Impffolge.BOOSTER_IMPFUNG, createExternGeimpftJaxEmpty());
		infos = impfinformationenService.getImpfinformationen("1", KrankheitIdentifier.COVID);
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getImpfdossier().getImpfdossierEintraege());
		Impfdossiereintrag eintrag = infos.getImpfdossier().getImpfdossierEintraege().get(0);
		ImpfungkontrolleTermin kontrolleTermin = eintrag.getImpfungkontrolleTermin();
		assertCurrentKontrolleUndImpfung(3, kontrolleTermin, kontrolleTermin, dbImpfung2, dbImpfung2, Impffolge.BOOSTER_IMPFUNG,
			3, eintrag);
		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.BOOSTER_IMPFUNG, false);
		Assertions.assertNotNull(dbBoosterImpfungen);
		assertCurrentKontrolleUndImpfung(4, null, null, dbBoosterImpfungen.get(0), dbBoosterImpfungen.get(0));

		// Booster (4)
		addKontrolle(Impffolge.BOOSTER_IMPFUNG, createExternGeimpftJaxEmpty());
		infos = impfinformationenService.getImpfinformationen("1", KrankheitIdentifier.COVID);
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getImpfdossier().getImpfdossierEintraege());
		eintrag = infos.getImpfdossier().getImpfdossierEintraege().get(1);
		kontrolleTermin = eintrag.getImpfungkontrolleTermin();
		assertCurrentKontrolleUndImpfung(4, kontrolleTermin, kontrolleTermin, dbBoosterImpfungen.get(0), dbBoosterImpfungen.get(0), Impffolge.BOOSTER_IMPFUNG,
			4, eintrag);
		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.BOOSTER_IMPFUNG, false);
		assertCurrentKontrolleUndImpfung(5, null, null, dbBoosterImpfungen.get(1), dbBoosterImpfungen.get(1));
	}

	// Zweitimpfung, die schon Booster ist
	@Test
	public void testCurrentKontrolleSzenario2() {
		Assertions.assertNotNull(dbFragebogen);
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen("1", KrankheitIdentifier.COVID);
		Personenkontrolle pk = infos.getImpfdossier().getOrCreatePersonenkontrolle();
		runAssertions(NEU, 0, 1, false, null);
		assertNumbers(false, false, null, null);

		// Erstimpfung Janssen
		assertCurrentKontrolleUndImpfung(1, null, pk.getKontrolleTermin1(), null, dbImpfung1);
		addKontrolle(Impffolge.ERSTE_IMPFUNG, createExternGeimpftJaxEmpty());
		assertCurrentKontrolleUndImpfung(1, pk.getKontrolleTermin1(), pk.getKontrolleTermin1(), null, dbImpfung1,
			Impffolge.ERSTE_IMPFUNG, 1);
		addImpfung(TestImpfstoff.TEST_JOHNSON, Impffolge.ERSTE_IMPFUNG, true);
		assertCurrentKontrolleUndImpfung(2, null, pk.getKontrolleTermin2(), dbImpfung1, dbImpfung1);

		// Booster (2)
		addKontrolle(Impffolge.BOOSTER_IMPFUNG, createExternGeimpftJaxEmpty());
		infos = impfinformationenService.getImpfinformationen("1", KrankheitIdentifier.COVID);
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getImpfdossier().getImpfdossierEintraege());
		Impfdossiereintrag eintrag = infos.getImpfdossier().getImpfdossierEintraege().get(0);
		ImpfungkontrolleTermin kontrolleTermin = eintrag.getImpfungkontrolleTermin();
		assertCurrentKontrolleUndImpfung(2, kontrolleTermin, kontrolleTermin, dbImpfung1, dbImpfung1, Impffolge.BOOSTER_IMPFUNG, 2, eintrag);
		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.BOOSTER_IMPFUNG, false);
		Assertions.assertNotNull(dbBoosterImpfungen);
		assertCurrentKontrolleUndImpfung(3, null, null, dbBoosterImpfungen.get(0), dbBoosterImpfungen.get(0));

		// Booster (3)
		addKontrolle(Impffolge.BOOSTER_IMPFUNG, createExternGeimpftJaxEmpty());
		infos = impfinformationenService.getImpfinformationen("1", KrankheitIdentifier.COVID);
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getImpfdossier().getImpfdossierEintraege());
		eintrag = infos.getImpfdossier().getImpfdossierEintraege().get(1);
		kontrolleTermin = eintrag.getImpfungkontrolleTermin();
		assertCurrentKontrolleUndImpfung(3, kontrolleTermin, kontrolleTermin, dbBoosterImpfungen.get(0), dbBoosterImpfungen.get(0), Impffolge.BOOSTER_IMPFUNG,
			3, eintrag);
		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.BOOSTER_IMPFUNG, false);
		Assertions.assertNotNull(dbBoosterImpfungen);
		assertCurrentKontrolleUndImpfung(4, null, null, dbBoosterImpfungen.get(1), dbBoosterImpfungen.get(1));

		// Booster (4)
		addKontrolle(Impffolge.BOOSTER_IMPFUNG, createExternGeimpftJaxEmpty());
		infos = impfinformationenService.getImpfinformationen("1", KrankheitIdentifier.COVID);
		Assertions.assertNotNull(infos.getImpfdossier());
		Assertions.assertNotNull(infos.getImpfdossier().getImpfdossierEintraege());
		eintrag = infos.getImpfdossier().getImpfdossierEintraege().get(2);
		kontrolleTermin = eintrag.getImpfungkontrolleTermin();
		assertCurrentKontrolleUndImpfung(4, kontrolleTermin, kontrolleTermin, dbBoosterImpfungen.get(1), dbBoosterImpfungen.get(1), Impffolge.BOOSTER_IMPFUNG,
			4, eintrag);
		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.BOOSTER_IMPFUNG, false);
		assertCurrentKontrolleUndImpfung(5, null, null, dbBoosterImpfungen.get(2), dbBoosterImpfungen.get(2));
	}

	@Test
	public void testExternGeimpftSzenario1() {

		// Impfling behauptet, extern vollstaendig geimpft worden zu sein
		saveExternGeimpftImpfling(true, TestImpfstoff.TEST_MODERNA, 2, false, true, IMMUNISIERT);
		runAssertions(IMMUNISIERT, 2, 3, false, VOLLSTAENDIG_EXTERNESZERTIFIKAT);
		assertNumbers(false, false, null, 2);

		// 1x Booster mit Nummer 3 hinzufuegen -> [extern 2, 3]
		addKontrolle(Impffolge.BOOSTER_IMPFUNG, getChangedExternGeimpftJax(null, null, null));
		runAssertions(KONTROLLIERT_BOOSTER, 2, 3, false, VOLLSTAENDIG_EXTERNESZERTIFIKAT);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.BOOSTER_IMPFUNG, false);
		runAssertions(IMMUNISIERT, 3, 4, true, VOLLSTAENDIG_EXTERNESZERTIFIKAT);
		assertNumbers(false, false, List.of(3), 2);

		// externGeimpft um 1 erhoehen und wieder impfen -> [extern 3, 3, 5]
		addKontrolle(Impffolge.BOOSTER_IMPFUNG, getChangedExternGeimpftJax(3, null, null));
		runAssertions(KONTROLLIERT_BOOSTER, 4, 5, true, VOLLSTAENDIG_EXTERNESZERTIFIKAT);
		assertNumbers(false, false, List.of(3, 5), 3);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.BOOSTER_IMPFUNG, false);
		runAssertions(IMMUNISIERT, 5, 6, true, VOLLSTAENDIG_EXTERNESZERTIFIKAT);
		assertNumbers(false, false, List.of(3, 5), 3);

	}

	@Test
	public void testExternGeimpftBrauchtNoch1Impfung() {
		// Impfling behauptet, extern 2x Abdala erhalten zu haben -> braucht nur noch 1 VacMe-Impfung fuer Grundimmunisierung
		saveExternGeimpftImpfling(true, TestImpfstoff.TEST_ABDALA, 2, false, false, NEU);
		runAssertions(NEU, 2, 3, false, null);
		assertNumbers(false, false, null, 2);

		// 1x Erstimpfung hinzufuegen -> Grundimmunisierung fertig
		addKontrolle(Impffolge.ERSTE_IMPFUNG, getChangedExternGeimpftJax(null, null, null));
		runAssertions(IMPFUNG_1_KONTROLLIERT, 2, 3, false, null);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ERSTE_IMPFUNG, true);
		runAssertions(ABGESCHLOSSEN, 3, 4, true, VOLLSTAENDIG_EXT_PLUS_VACME);
		assertNumbers(true, false, null, 2);
	}

	@Test
	public void testExternGeimpftGenesenBrauchtNoch1Impfung() {
		// Impfling behauptet, extern 1x Abdala erhalten zu haben + genesen -> braucht nur noch 1 VacMe-Impfung fuer Grundimmunisierung
		saveExternGeimpftImpfling(true, TestImpfstoff.TEST_ABDALA, 1, true, false, NEU);
		runAssertions(NEU, 1, 2, false, null);
		assertNumbers(false, false, null, 1);

		// 1x Erstimpfung hinzufuegen -> Grundimmunisierung fertig
		addKontrolle(Impffolge.ERSTE_IMPFUNG, getChangedExternGeimpftJax(null, null, null));
		runAssertions(IMPFUNG_1_KONTROLLIERT, 1, 2, false, null);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ERSTE_IMPFUNG, true);
		runAssertions(ABGESCHLOSSEN, 2, 3, true, VOLLSTAENDIG_EXT_GENESEN_PLUS_VACME);
		assertNumbers(true, false, null, 1);
	}

	@Test
	public void testExternGeimpftBrauchtNoch2Impfungen() {
		// Impfling behauptet, extern 1x Abdala erhalten zu haben -> braucht noch 2 VacMe-Impfung fuer Grundimmunisierung
		saveExternGeimpftImpfling(true, TestImpfstoff.TEST_ABDALA, 1, false, false, NEU);
		runAssertions(NEU, 1, 2, false, null);
		assertNumbers(false, false, null, 1);

		// Erstimpfung hinzufuegen
		addKontrolle(Impffolge.ERSTE_IMPFUNG, getChangedExternGeimpftJax(null, null, null));
		runAssertions(IMPFUNG_1_KONTROLLIERT, 1, 2, false, null);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ERSTE_IMPFUNG, true);
		runAssertions(IMPFUNG_1_DURCHGEFUEHRT, 2, 3, true, null);
		assertNumbers(true, false, null, 1);

		// Zweitimpfung hinzufuegen -> Grundimmunisierung fertig
		addKontrolle(Impffolge.ZWEITE_IMPFUNG, getChangedExternGeimpftJax(null, null, null));
		runAssertions(IMPFUNG_2_KONTROLLIERT, 2, 3, true, null);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ZWEITE_IMPFUNG, true);
		runAssertions(ABGESCHLOSSEN, 3, 4, true, VOLLSTAENDIG_VACME);
		assertNumbers(true, true, null, 1);
	}

	@Test
	public void testExternesZertifikatAblehnen() {

		// Impfling behauptet, extern komplett geimpft worden zu sein (2xModerna)
		saveExternGeimpftImpfling(true, TestImpfstoff.TEST_MODERNA, 2, false, true, IMMUNISIERT);
		runAssertions(IMMUNISIERT, 2, 3, false, VOLLSTAENDIG_EXTERNESZERTIFIKAT);
		assertNumbers(false, false, null, 2);

		// ExternesZertifikat teilweise ablehnen (nur 1 Moderna) und Impfung 1 hinzufuegen -> [1]
		addKontrolle(Impffolge.BOOSTER_IMPFUNG, getChangedExternGeimpftJax(1, false, null));
		runAssertions(IMPFUNG_1_KONTROLLIERT, 1, 2, false, null);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ERSTE_IMPFUNG, true);
		runAssertions(ABGESCHLOSSEN, 2, 3, true, VOLLSTAENDIG_EXT_PLUS_VACME);
		assertNumbers(true, false, null, 1);

	}

	@Test
	public void testExternesZertifikatHinzufuegen() {

		// Impfling weiss von nichts
		runAssertions(NEU, 0, 1, false, null);
		assertNumbers(false, false, null, null);

		// ExternesZertifikat bei Kontrolle hinzufuegen und Impfung 1 hinzufuegen -> [extern 1, 2]
		addKontrolle(Impffolge.ERSTE_IMPFUNG, createExternGeimpftJax(true, TestImpfstoff.TEST_JOHNSON, 1, false, true));
		runAssertions(KONTROLLIERT_BOOSTER, 1, 2, false, VOLLSTAENDIG_EXTERNESZERTIFIKAT);

		addImpfung(TestImpfstoff.TEST_PFIZER, Impffolge.BOOSTER_IMPFUNG, false);
		runAssertions(IMMUNISIERT, 2, 3, true, VOLLSTAENDIG_EXTERNESZERTIFIKAT);
		assertNumbers(false, false, List.of(2), 1);

	}

	@Test
	public void testExternesZertifikatSpaeterHinzufuegen1() {

		// Impfling weiss von nichts
		runAssertions(NEU, 0, 1, false, null);
		assertNumbers(false, false, null, null);

		//  Impfung 1 hinzufuegen -> [1]
		addKontrolle(Impffolge.ERSTE_IMPFUNG, createExternGeimpftJax(false, null, 0, false, false));
		runAssertions(IMPFUNG_1_KONTROLLIERT, 0, 1, false, null);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ERSTE_IMPFUNG, true);
		runAssertions(IMPFUNG_1_DURCHGEFUEHRT, 1, 2, true, null);
		assertNumbers(true, false, null, null);

		//  Impfung 2 hinzufuegen, externes Zertifikat hinzugefuegt -> sollte Exception werfen
		TestUtil.assertThrowsAppvalidation(
			AppValidationMessage.EXISTING_VACME_IMPFUNGEN_CANNOT_ADD_EXTERN_GEIMPFT,
			null,
			() -> {
				addKontrolle(Impffolge.ZWEITE_IMPFUNG, createExternGeimpftJax(true, TestImpfstoff.TEST_MODERNA, 3, false, true));
			});

	}

	@Test
	public void testExternesZertifikatSpaeterHinzufuegen2() {

		// Impfling weiss von nichts
		runAssertions(NEU, 0, 1, false, null);
		assertNumbers(false, false, null, null);

		//  Impfung 1 hinzufuegen -> [1]
		addKontrolle(Impffolge.ERSTE_IMPFUNG, createExternGeimpftJax(false, null, 0, false, false));
		runAssertions(IMPFUNG_1_KONTROLLIERT, 0, 1, false, null);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ERSTE_IMPFUNG, true);
		runAssertions(IMPFUNG_1_DURCHGEFUEHRT, 1, 2, true, null);
		assertNumbers(true, false, null, null);

		//  Impfung 2 hinzufuegen -> [2]
		addKontrolle(Impffolge.ZWEITE_IMPFUNG, createExternGeimpftJax(false, null, 0, false, false));
		runAssertions(IMPFUNG_2_KONTROLLIERT, 1, 2, true, null);

		addImpfung(TestImpfstoff.TEST_MODERNA, Impffolge.ZWEITE_IMPFUNG, true);
		runAssertions(ABGESCHLOSSEN, 2, 3, true, VOLLSTAENDIG_VACME);
		assertNumbers(true, true, null, null);

		// Boosterkontrolle, externes Zertifikat hinzugefuegt -> sollte Exception werfen
		TestUtil.assertThrowsAppvalidation(
			AppValidationMessage.EXISTING_VACME_IMPFUNGEN_CANNOT_ADD_EXTERN_GEIMPFT,
			null,
			() -> {
				addKontrolle(Impffolge.BOOSTER_IMPFUNG, createExternGeimpftJax(true, TestImpfstoff.TEST_MODERNA, 3, false, true));
			});

	}

	private ExternGeimpftJax getChangedExternGeimpftJax(@Nullable Integer anzahlImpfungen, @Nullable Boolean genesen, @Nullable TestImpfstoff testImpfstoff) {
		ExternGeimpftJax externGeimpftJax1 = ExternGeimpftJax.from(dbExternesZertifikat); // unveraendert
		Assertions.assertNotNull(externGeimpftJax1);
		// update
		if (anzahlImpfungen != null) {
			externGeimpftJax1.setAnzahlImpfungen(anzahlImpfungen);
		}
		if (genesen != null) {
			externGeimpftJax1.setGenesen(genesen);
		}
		if (testImpfstoff != null) {
			final Impfstoff impfstoffForTest = TestImpfstoff.createImpfstoffForTest(testImpfstoff);
			Assertions.assertNotNull(impfstoffForTest);
			externGeimpftJax1.setImpfstoff(ImpfstoffJax.from(impfstoffForTest));
		}
		return externGeimpftJax1;

	}

	private void addImpfung(TestImpfstoff testImpfstoff, Impffolge impffolge, boolean grund) {
		Impfung impfung = new Impfung();
		final Impfstoff impfstoffForTest = TestImpfstoff.createImpfstoffForTest(testImpfstoff);
		Assertions.assertNotNull(impfstoffForTest);
		impfung.setImpfstoff(impfstoffForTest);
		impfung.setGrundimmunisierung(grund);
		OrtDerImpfung odi = TestdataCreationUtil.createOrtDerImpfung(KrankheitIdentifier.COVID);
		LocalDateTime time = LocalDateTime.now();
		Assertions.assertNotNull(dbRegistrierung);
		Assertions.assertNotNull(dbImpfdossier);
		ImpfinformationDto infos =
			new ImpfinformationDto(KrankheitIdentifier.COVID, dbRegistrierung, dbImpfung1, dbImpfung2, dbImpfdossier, dbExternesZertifikat);
		if (dbBoosterImpfungen != null) {
			infos = ImpfinformationDtoRecreator.from(infos).withBoosterImpfungen(dbBoosterImpfungen).build();
		}
		impfdokumentationService.createImpfung(infos, odi, impffolge, impfung, false, time, false);
		switch (impffolge) {
		case ERSTE_IMPFUNG:
			dbImpfung1 = impfung;
			break;
		case ZWEITE_IMPFUNG:
			dbImpfung2 = impfung;
			break;
		case BOOSTER_IMPFUNG:
			assert dbBoosterImpfungen != null;
			dbBoosterImpfungen.add(impfung);
			break;
		}
	}

	private void saveExternGeimpftImpfling(
		boolean geimpftNeu,
		@NonNull TestImpfstoff testImpfstoffNeu,
		int anzahlImpfungenNeu,
		boolean genesenNeu,
		boolean grundimmunisiertNeu,
		@NonNull ImpfdossierStatus status
	) {
		ExternGeimpftJax externGeimpftJax = createExternGeimpftJax(geimpftNeu, testImpfstoffNeu, anzahlImpfungenNeu, genesenNeu, grundimmunisiertNeu);
		// Impfling speichert ExternGeimpft --> IMMUNISIERT
		Assertions.assertNotNull(dbRegistrierung);
		Assertions.assertNotNull(dbImpfdossier);
		ImpfinformationDto infos = new ImpfinformationDto(KrankheitIdentifier.COVID, dbRegistrierung, null, null, dbImpfdossier, null);
		externesZertifikatService.saveExternGeimpftImpfling(infos, externGeimpftJax, false);
		infos.getImpfdossier().setDossierStatus(status);
	}

	private void addKontrolle(Impffolge impffolge, ExternGeimpftJax externGeimpftJax) {
		ImpfkontrolleJax dummyImpfkontrolleJax = TestdataCreationUtil.createImpfkontrolleJax();
		Assertions.assertNotNull(dbFragebogen);
		impfkontrolleService.kontrolleOk(
			dbFragebogen, impffolge, null, dummyImpfkontrolleJax, externGeimpftJax);
	}

	private void runAssertions(
		@NonNull ImpfdossierStatus status,
		int impfungCount,
		int currentKontrolle,
		boolean hasVacmeImpfungen,
		@Nullable VollstaendigerImpfschutzTyp expectedImpfschutzTyp
	) {
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen("1", KrankheitIdentifier.COVID);
		Assertions.assertEquals(status, infos.getImpfdossier().getDossierStatus());
		Assertions.assertEquals(impfungCount, ImpfinformationenService.getNumberOfImpfung(infos));
		Assertions.assertEquals(currentKontrolle, ImpfinformationenService.getCurrentKontrolleNr(infos));
		Assertions.assertEquals(hasVacmeImpfungen, impfinformationenService.hasVacmeImpfungen(infos));
		Assertions.assertNotNull(dbImpfdossier);
		Assertions.assertEquals(expectedImpfschutzTyp, dbImpfdossier.getVollstaendigerImpfschutzTyp());
		if (expectedImpfschutzTyp != null) {
			Assertions.assertNotNull(dbImpfdossier.getVollstaendigerImpfschutzTyp());
			Assertions.assertEquals(expectedImpfschutzTyp, dbImpfdossier.getVollstaendigerImpfschutzTyp());
		} else {
			Assertions.assertNull(dbImpfdossier.getVollstaendigerImpfschutzTyp());
		}

		// testing the test setup
		Assertions.assertEquals(dbImpfdossier, infos.getImpfdossier());
		Assertions.assertEquals(dbExternesZertifikat, infos.getExternesZertifikat());
	}

	// ImpffolgeNummern vergleichen und ein paar Hilfsmethoden aus dem ImpfinformationenService testen!
	private void assertNumbers(boolean hasImpfung1, boolean hasImpfung2, @Nullable List<Integer> boosterNumbers, @Nullable Integer externGeimpftCount) {
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen("1", KrankheitIdentifier.COVID);

		// Impfung 1 und 2
		Assertions.assertEquals(hasImpfung1, infos.getImpfung1() != null);
		Assertions.assertEquals(hasImpfung2, infos.getImpfung2() != null);

		// Externes Zertifikat
		if (externGeimpftCount != null) {
			Assertions.assertNotNull(infos.getExternesZertifikat());
			Assertions.assertEquals(externGeimpftCount, infos.getExternesZertifikat().getAnzahlImpfungen());
		} else {
			Assertions.assertNull(infos.getExternesZertifikat());
		}

		if (boosterNumbers != null) {
			Assertions.assertNotNull(infos.getImpfdossier());
			int numbersIndex = 0;
			Assertions.assertEquals(boosterNumbers.size(), infos.getImpfdossier().getImpfdossierEintraege().size());

			// getImpfdossierEintraege
			for (Impfdossiereintrag eintrag : infos.getImpfdossier().getImpfdossierEintraege()) {
				Integer expectedNumber = boosterNumbers.get(numbersIndex);
				Assertions.assertEquals(expectedNumber, eintrag.getImpffolgeNr());
				numbersIndex++;
			}

			// getDossiereintragForNr
			for (int i = 0; i < boosterNumbers.size(); i++) {
				Integer expectedNumber = boosterNumbers.get(i);
				Impfdossiereintrag eintrag = ImpfinformationenService.getDossiereintragForNr(infos, expectedNumber);
				assert eintrag != null;
				Assertions.assertEquals(expectedNumber, eintrag.getImpffolgeNr());
			}

		}

	}

	private void assertCurrentKontrolleUndImpfung(
		int currentKontrolleNr,
		@Nullable ImpfungkontrolleTermin currentKontrolleTerminExpected,
		@Nullable ImpfungkontrolleTermin currentKontrolleTerminDb,
		@Nullable Impfung newestImpfungExpected,
		@Nullable Impfung newestImpfungDb) {
		assertCurrentKontrolleUndImpfung(currentKontrolleNr, currentKontrolleTerminExpected, currentKontrolleTerminDb, newestImpfungExpected, newestImpfungDb,
			null, null);
	}

	private void assertCurrentKontrolleUndImpfung(
		int currentKontrolleNr,
		@Nullable ImpfungkontrolleTermin currentKontrolleTerminExpected,
		@Nullable ImpfungkontrolleTermin currentKontrolleTerminDb,
		@Nullable Impfung newestImpfungExpected,
		@Nullable Impfung newestImpfungDb,
		@Nullable Impffolge impffolge,
		@Nullable Integer impffolgeNr
	) {
		assertCurrentKontrolleUndImpfung(currentKontrolleNr, currentKontrolleTerminExpected, currentKontrolleTerminDb, newestImpfungExpected, newestImpfungDb,
			impffolge, impffolgeNr, null);
	}

	private void assertCurrentKontrolleUndImpfung(
		int currentKontrolleNr,
		@Nullable ImpfungkontrolleTermin currentKontrolleTerminExpected,
		@Nullable ImpfungkontrolleTermin currentKontrolleTerminDb,
		@Nullable Impfung newestImpfungExpected,
		@Nullable Impfung newestImpfungDb,
		@Nullable Impffolge impffolge,
		@Nullable Integer impffolgeNr,
		@Nullable Impfdossiereintrag impfdossiereintrag
	) {
		Assertions.assertNotNull(dbRegistrierung);
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			dbRegistrierung.getRegistrierungsnummer(),
			KrankheitIdentifier.COVID);

		Assertions.assertEquals(currentKontrolleNr, ImpfinformationenService.getCurrentKontrolleNr(infos));
		Assertions.assertEquals(currentKontrolleTerminExpected, currentKontrolleTerminDb);
		Assertions.assertNotNull(dbFragebogen);
		Assertions.assertEquals(currentKontrolleTerminDb, ImpfinformationenService.getCurrentKontrolleTerminOrNull(infos));
		Assertions.assertEquals(newestImpfungExpected, newestImpfungDb);
		Assertions.assertEquals(newestImpfungDb, ImpfinformationenService.getNewestVacmeImpfung(infos));

		if (currentKontrolleTerminExpected != null) {
			Assertions.assertNotNull(impffolge);
			Assertions.assertNotNull(impffolgeNr);
			UUID existingDossiereintragIdOrNull = impfdossiereintrag == null ? null : impfdossiereintrag.getId();

			Assertions.assertEquals(currentKontrolleTerminExpected,
				impfkontrolleService.getOrCreateImpfkontrolleTermin(infos, impffolge, impffolgeNr, existingDossiereintragIdOrNull));
		}
	}

	private ExternGeimpftJax createExternGeimpftJaxEmpty() {
		return new ExternGeimpftJax();
	}

	private ExternGeimpftJax createExternGeimpftJax(boolean geimpftNeu, @Nullable TestImpfstoff testImpfstoffNeu, int anzahlImpfungenNeu, boolean genesenNeu,
		boolean grundimmunisiertNeu) {
		ExternGeimpftJax externGeimpftJaxNeu = new ExternGeimpftJax();
		Impfstoff impfstoffNeu = geimpftNeu && testImpfstoffNeu != null ? TestImpfstoff.createImpfstoffForTest(testImpfstoffNeu) : null;
		ImpfstoffJax impfstoffNeuJax = impfstoffNeu != null ? ImpfstoffJax.from(impfstoffNeu) : null;
		externGeimpftJaxNeu.setImpfstoff(impfstoffNeuJax);
		externGeimpftJaxNeu.setAnzahlImpfungen(anzahlImpfungenNeu);
		externGeimpftJaxNeu.setGenesen(genesenNeu);
		externGeimpftJaxNeu.setExternGeimpft(geimpftNeu);
		externGeimpftJaxNeu.setLetzteImpfungDate(LocalDate.now().minusMonths(1));
		Assertions.assertEquals(grundimmunisiertNeu, ExternesZertifikat.isGrundimmunisiert(KrankheitIdentifier.COVID, impfstoffNeu, anzahlImpfungenNeu, genesenNeu, null));
		return externGeimpftJaxNeu;
	}

}
