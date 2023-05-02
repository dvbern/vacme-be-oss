/*
 * Copyright (C) 2023 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service.wellapi;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonValue;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.Kundengruppe;
import ch.dvbern.oss.vacme.repo.BenutzerRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.rest_client.well.api.WellRestClientService;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeAppointmentRequestDto;
import ch.dvbern.oss.vacme.rest_client.well.model.VacMeApprovalPeriodRequestDto;
import ch.dvbern.oss.vacme.service.ApplicationPropertyService;
import ch.dvbern.oss.vacme.service.BenutzerService;
import ch.dvbern.oss.vacme.service.EnsureBenutzerService;
import ch.dvbern.oss.vacme.service.ImpfdossierService;
import ch.dvbern.oss.vacme.service.KeyCloakService;
import ch.dvbern.oss.vacme.service.OrtDerImpfungService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationBuilder;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.smartdb.Db;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import io.quarkus.security.identity.SecurityIdentity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class WellApiServiceTest {

	private final WellApiService wellApiService = Mockito.spy(WellApiService.class);
	private final WellApiInitalDataSenderService wellApiInitalDataSenderService = new WellApiInitalDataSenderService(
		wellApiService,
		Mockito.mock(RegistrierungService.class),
		Mockito.mock(ImpfinformationenService.class)
	);
	private final UserPrincipal userPrincipalMock = Mockito.mock(UserPrincipal.class);
	private final JsonWebToken jsonWebToken = Mockito.mock(JsonWebToken.class);
	private final BenutzerService benutzerServiceMock = Mockito.mock(BenutzerService.class);

	private final ImpfterminRepo impfterminRepo = new ImpfterminRepo(
		Mockito.mock(Db.class),
		Mockito.mock(ImpfungRepo.class),
		wellApiService);

	private final EnsureBenutzerService ensureBenutzerService = new EnsureBenutzerService(
		jsonWebToken,
		Mockito.mock(SecurityIdentity.class),
		benutzerServiceMock,
		Mockito.mock(BenutzerRepo.class),
		Mockito.mock(OrtDerImpfungService.class),
		Mockito.mock(KeyCloakService.class),
		Mockito.mock(ApplicationPropertyService.class),
		Mockito.mock(RegistrierungRepo.class),
		wellApiInitalDataSenderService);

	private final ImpfinformationBuilder builder = new ImpfinformationBuilder();
	private final Impftermin pendingGalenicaTermin = TestdataCreationUtil.createImpftermin(
		TestdataCreationUtil.createOrtDerImpfung(Kundengruppe.GALENICA),
		LocalDate.now().plusDays(7));

	@BeforeEach
	void setUp() {
		System.setProperty("vacme.mandant", "BE");
		wellApiService.wellRestClientService = Mockito.mock(WellRestClientService.class);
		wellApiService.benutzerService = benutzerServiceMock;
		wellApiService.impfdossierService = Mockito.mock(ImpfdossierService.class);
	}

	@Test
	void sendAccountLink_notCalledForVacmeUser() {
		// Testdaten vorbereiten
		prepareTestdataAccountLink(false);
		// Aufruf
		ensureBenutzerService.ensureBenutzer();
		// Assertions
		Mockito.verify(wellApiService, Mockito.never()).sendAccountLink(any());
	}

	@Test
	void sendAccountLink_calledForWellUser() {
		// Testdaten vorbereiten
		prepareTestdataAccountLink(true);
		// Aufruf
		ensureBenutzerService.ensureBenutzer();
		// Assertions
		Mockito.verify(wellApiService, Mockito.times(1)).sendAccountLink(any());
		Mockito.verify(wellApiService, Mockito.timeout(500).times(1)).sendAccountLinkAsync(any());
	}

	@Test
	void given_covid_impfung1_when_buchung_then_createAppointmentInfo_must_not_be_sent() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.COVID, true);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isEmpty());
		// Aufruf
		impfterminRepo.termin1Speichern(builder.getInfos().getImpfdossier(), pendingGalenicaTermin);
		// Assertions
		// the actual async call should never be trigger for Impfung1
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWell(null);
		Mockito.verify(wellApiService, Mockito.never()).sendAppointmentInfoToWellAsync(any());
	}

	@Test
	void given_covid_impfungN_when_buchung_then_createAppointmentInfo_must_not_be_sent() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.COVID, true);
		builder.withImpfung1(LocalDate.now().minusMonths(6), TestdataCreationUtil.createImpfstoffModerna());
		builder.withImpfung2(LocalDate.now().minusMonths(5), TestdataCreationUtil.createImpfstoffModerna());
		builder.withDossiereintrag(3);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());
		// Aufruf
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), pendingGalenicaTermin);
		// Assertions
		// the actual async call should never be trigger for covid
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.never()).sendAppointmentInfoToWellAsync(any());
	}

	@Test
	void given_first_FSME_impftermin_when_termin_inpast_then_createAppointmentInfo_must_not_sent_to_well() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.FSME, true);
		builder.withDossiereintrag(1);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());

		Impftermin terminInPast = TestdataCreationUtil.createImpftermin(
			TestdataCreationUtil.createOrtDerImpfung(),
			LocalDate.now().minusMonths(2));

		// Aufruf
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), terminInPast);
		// Assertions
		// the actual async call should never be trigger for termine that are in the past
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.never()).sendAppointmentInfoToWellAsync(any());
	}

	@Test
	void given_first_FSME_impftermin_when_termin_in_non_galenica_odi_then_createAppointmentInfo_must_not_sent_to_well() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.FSME, true);
		builder.withDossiereintrag(1);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());
		Impftermin terminInNonGalenica = TestdataCreationUtil.createImpftermin(
			TestdataCreationUtil.createOrtDerImpfung(Kundengruppe.UNBEKANNT),
			LocalDate.now().minusMonths(2));

		// Aufruf
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), terminInNonGalenica);
		// Assertions
		// the actual async call should never be trigger for termine that are not in galenica odi
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.never()).sendAppointmentInfoToWellAsync(any());
	}

	@Test
	void given_first_FSME_impftermin_when_buchung_then_createAppointmentInfo_must_be_sent_to_well() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.FSME, true);
		builder.withDossiereintrag(1);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());
		// Aufruf
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), pendingGalenicaTermin);
		// Assertions
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWellAsync(any());
	}

	@Test
	void given_third_FSME_impftermin_of_non_well_benutzer_when_buchung_then_createAppointmentInfo_must_not_be_sent_to_well_user() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.FSME, false);
		builder.withExternesZertifikat(
			TestdataCreationUtil.createImpfstoffEncepur(),
			2,
			LocalDate.now().minusMonths(6),
			null);
		builder.withDossiereintrag(3);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());
		// Aufruf
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), pendingGalenicaTermin);
		// Assertions
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.never()).sendAppointmentInfoToWellAsync(any());
	}

	@Test
	void given_third_FSME_impftermin_of_non_online_benutzer_when_buchung_then_createAppointmentInfo_must_not_be_sent_to_well_user() {
		// Testdaten vorbereiten
		//non-online Benutzer will never have a well id, this should be purely theoretical
		prepareTestdataAppointment(KrankheitIdentifier.FSME, true);
		builder.withRegistrierungsEingang(RegistrierungsEingang.DATA_MIGRATION);

		builder.withExternesZertifikat(
			TestdataCreationUtil.createImpfstoffEncepur(),
			2,
			LocalDate.now().minusMonths(6),
			null);
		builder.withDossiereintrag(3);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());
		// Aufruf
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), pendingGalenicaTermin);
		// Assertions
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.never()).sendAppointmentInfoToWellAsync(any());
	}

	@Test
	void given_third_FSME_impftermin_when_buchung_then_createAppointmentInfo_must_be_sent_to_well_user() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.FSME, true);
		builder.withExternesZertifikat(
			TestdataCreationUtil.createImpfstoffEncepur(),
			2,
			LocalDate.now().minusMonths(6),
			null);
		builder.withDossiereintrag(3);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());
		// Aufruf
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), pendingGalenicaTermin);
		// Assertions
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.times(1)).sendAppointmentInfoToWellAsync(any());
	}

	@Test
	void given_covid_impfung1_when_termin1freigabe_must_not_be_sent_to_well() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.COVID, true);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isEmpty());
		impfterminRepo.termin1Speichern(builder.getInfos().getImpfdossier(), pendingGalenicaTermin);
		// Aufruf
		impfterminRepo.termin1Freigeben(builder.getInfos().getImpfdossier());
		// Assertions
		// Assertions actual async call should never be trigger for covid
		Mockito.verify(wellApiService, Mockito.times(1)).deleteAppointmentInfoInWell(null);
		Mockito.verify(wellApiService, Mockito.never()).deleteAppointmentInfoInWellAsync(any());
	}

	@Test
	void given_covid_impfungN_deleteAppointmentInfo_must_not_be_sent_to_well() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.COVID, true);
		builder.withImpfung1(LocalDate.now().minusMonths(6), TestdataCreationUtil.createImpfstoffModerna());
		builder.withImpfung2(LocalDate.now().minusMonths(5), TestdataCreationUtil.createImpfstoffModerna());
		builder.withDossiereintrag(3);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), pendingGalenicaTermin);
		// Aufruf
		impfterminRepo.boosterTerminFreigeben(eintrag.get());
		// Assertions actual async call should never be trigger for covid
		Mockito.verify(wellApiService, Mockito.times(1)).deleteAppointmentInfoInWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.never()).deleteAppointmentInfoInWellAsync(any());
	}

	@Test
	void given_first_fsme_impfung_of_nonwelluser_when_terminfreigabe_deleteAppointmentInfo_must_not_be_sent_to_well() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.FSME, false);
		builder.withDossiereintrag(1);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), pendingGalenicaTermin);
		// Aufruf
		impfterminRepo.boosterTerminFreigeben(eintrag.get());
		// Assertions actual async call should never be trigger for non well user
		Mockito.verify(wellApiService, Mockito.times(1)).deleteAppointmentInfoInWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.never()).deleteAppointmentInfoInWellAsync(any());
	}

	@Test
	void given_first_fsme_impfung_when_terminfreigabe_deleteAppointmentInfo_must_be_sent_to_well() {
		// Testdaten vorbereiten
		prepareTestdataAppointment(KrankheitIdentifier.FSME, true);
		builder.withDossiereintrag(1);
		// Dossiereintrag ermitteln
		final Optional<Impfdossiereintrag> eintrag =
			ImpfinformationenService.getPendingDossiereintrag(builder.getInfos());
		Assertions.assertTrue(eintrag.isPresent());
		impfterminRepo.boosterTerminSpeichern(eintrag.get(), pendingGalenicaTermin);
		// Aufruf
		impfterminRepo.boosterTerminFreigeben(eintrag.get());
		// Assertions
		Mockito.verify(wellApiService, Mockito.times(1)).deleteAppointmentInfoInWell(eintrag.get());
		Mockito.verify(wellApiService, Mockito.times(1)).deleteAppointmentInfoInWellAsync(any());
	}

	private void prepareTestdataAccountLink(boolean isWellUser) {
		Mockito.when(jsonWebToken.getClaim(anyString())).thenReturn("notImportant");
		Mockito.when(jsonWebToken.getClaim(Constants.MEMBER_OF_CLAIM)).thenReturn(JsonValue.EMPTY_JSON_ARRAY);
		Mockito.when(jsonWebToken.getSubject()).thenReturn(UUID.randomUUID().toString());
		if (isWellUser) {
			Mockito.when(benutzerServiceMock.save(any(), any(), any(), any()))
				.thenReturn(TestdataCreationUtil.createBenutzerWell());
			Mockito.when(jsonWebToken.claim(Constants.WELL_ID_CLAIM))
				.thenReturn(Optional.of(UUID.randomUUID().toString()));
		} else {
			Mockito.when(benutzerServiceMock.save(any(), any(), any(), any()))
				.thenReturn(TestdataCreationUtil.createBenutzer("Tester", "Tim", null));
		}
	}

	@Nested
	class WellApiUnavailableTest {

		@BeforeEach
		void setUp() {
			System.setProperty("vacme.mandant", "BE");
			WellRestClientService alwaysThorApiMock = Mockito.mock(WellRestClientService.class, invocation -> {
				throw new RuntimeException("This Mock Dummy always throws an exception");
			});

			wellApiService.wellRestClientService = alwaysThorApiMock;
			wellApiService.benutzerService = benutzerServiceMock;
		}

		@Test
		void given_well_api_down_when_well_triggerd_in_vacme_exceptions_are_ignored() {
			// since the call is asyncronous, we can not really verify that exceptions are catched
			// this test at least excercies the scenario, it should produce no stacktrace (just a log entry)

			Assertions.assertDoesNotThrow(() -> {
				wellApiService.sendAccountLinkAsync(UUID.randomUUID());
				wellApiService.deleteAccountLinkAsync(UUID.randomUUID());
				wellApiService.sendAppointmentInfoToWellAsync(new VacMeAppointmentRequestDto());
				wellApiService.deleteAppointmentInfoInWellAsync(Impftermin.toId(UUID.randomUUID()));
				wellApiService.sendApprovalPeriodAsync(new VacMeApprovalPeriodRequestDto());
				wellApiService.deleteApprovalperiodAsync(Impfschutz.toId(UUID.randomUUID()));

			});

		}
	}


	private void prepareTestdataAppointment(@NonNull KrankheitIdentifier krankheitIdentifier, boolean isWellUser) {
		// Krankheit
		builder.create(krankheitIdentifier)
			.withRegistrierungsEingang(RegistrierungsEingang.ONLINE_REGISTRATION);
		pendingGalenicaTermin.getImpfslot().setKrankheitIdentifier(krankheitIdentifier);
		Mockito.when(userPrincipalMock.isCallerInRole(BenutzerRolle.IMPFWILLIGER)).thenReturn(Boolean.TRUE);
		Benutzer userToWorkAs;
		if (isWellUser) {
			userToWorkAs = TestdataCreationUtil.createBenutzerWell();
		} else {
			userToWorkAs = TestdataCreationUtil.createBenutzer("Tester", "Tim", null);
		}
		Mockito.when(userPrincipalMock.getBenutzerOrThrowException())
			.thenReturn(userToWorkAs);

		Mockito.when(benutzerServiceMock.getBenutzerOfOnlineRegistrierung(any()))
			.thenReturn(userToWorkAs);
	}
}
