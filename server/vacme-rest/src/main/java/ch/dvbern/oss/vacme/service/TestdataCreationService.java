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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.security.RunAs;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsart;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsort;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsseite;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Personenkontrolle;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.impfslot.DateTimeRangeJax;
import ch.dvbern.oss.vacme.jax.impfslot.ImpfslotDisplayJax;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import com.github.javafaker.Faker;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity.Builder;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolleName.SYSTEM_INTERNAL_ADMIN;

@RunAs(SYSTEM_INTERNAL_ADMIN)
@ApplicationScoped
@Slf4j
public class TestdataCreationService {

	private final CurrentIdentityAssociation association;
	private final RegistrierungService registrierungService;
	private final TerminbuchungService terminbuchungService;
	private final OrtDerImpfungRepo ortDerImpfungRepo;
	private final ImpfterminRepo impfterminRepo;
	private final ImpfkontrolleService impfkontrolleService;
	private final ImpfdokumentationService impfdokumentationService;
	private final UserPrincipal userPrincipal;
	private final ImpfslotService impfslotService;

	@Inject
	public TestdataCreationService(
		@NonNull CurrentIdentityAssociation association,
		@NonNull RegistrierungService registrierungService,
		@NonNull TerminbuchungService terminbuchungService,
		@NonNull OrtDerImpfungRepo ortDerImpfungRepo,
		@NonNull ImpfterminRepo impfterminRepo,
		@NonNull ImpfkontrolleService impfkontrolleService,
		@NonNull ImpfdokumentationService impfdokumentationService,
		@NonNull UserPrincipal userPrincipal,
		@NonNull ImpfslotService impfslotService
	) {
		this.association = association;
		this.registrierungService = registrierungService;
		this.terminbuchungService = terminbuchungService;
		this.ortDerImpfungRepo = ortDerImpfungRepo;
		this.impfterminRepo = impfterminRepo;
		this.impfkontrolleService = impfkontrolleService;
		this.impfdokumentationService = impfdokumentationService;
		this.userPrincipal = userPrincipal;
		this.impfslotService = impfslotService;
	}

	@NonNull
	public  List<String> createRegistrierungen(int anzahl) {
		runAsInternalSystemAdmin();
		List<String> result = new ArrayList<>();
		for (int i = 0; i < anzahl; i++) {
			final Fragebogen fragebogen = TestdataCreationUtil.createFragebogen();
			final Impfdossier impfdossier = registrierungService.createImpfdossierCovid(fragebogen);
			result.add(impfdossier.getRegistrierung().getRegistrierungsnummer());
		}
		return result;
	}

	@NonNull
	public List<String> createRegistrierungenMitTermin(
		int anzahl,
		@NonNull String odiname,
		@NonNull LocalDate datumTermin1,
		@NonNull LocalDate datumTermin2
	) {
		runAsInternalSystemAdmin();

		final OrtDerImpfung odi = ortDerImpfungRepo
			.getByName(odiname)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_ODI.create(odiname));

		List<String> result = new ArrayList<>();
		for (int i = 0; i < anzahl; i++) {
			final String registrierungsnummer = createRegistrierungMitTermin(odi, datumTermin1, datumTermin2);
			result.add(registrierungsnummer);
		}
		return result;
	}

	@Transactional(TxType.REQUIRES_NEW)
	private String createRegistrierungMitTermin(
		@NonNull OrtDerImpfung odi,
		@NonNull LocalDate datumTermin1,
		@NonNull LocalDate datumTermin2
	) {
		final Fragebogen fragebogen = TestdataCreationUtil.createFragebogen();
		final Impfdossier impfdossier = registrierungService.createImpfdossierCovid(fragebogen);

		// Der Einfachheit halber werden immer neue ad-hoc Termine erstellt
		final Impftermin termin1 = terminbuchungService.createOnDemandImpftermin(
			KrankheitIdentifier.COVID, Impffolge.ERSTE_IMPFUNG, odi, datumTermin1.atTime(LocalTime.now()));
		final Impftermin termin2 = terminbuchungService.createOnDemandImpftermin(
			KrankheitIdentifier.COVID, Impffolge.ZWEITE_IMPFUNG, odi, datumTermin2.atTime(LocalTime.now()));

		impfterminRepo.termineSpeichern(impfdossier, termin1, termin2);

		return impfdossier.getRegistrierung().getRegistrierungsnummer();
	}


	@NonNull
	private Impfung createImpfung(Registrierung registrierung) {

		Impfstoff impfstoff = new Impfstoff();
		impfstoff.setId(Constants.PFIZER_BIONTECH_UUID);
		impfstoff.setAnzahlDosenBenoetigt(3);

		Impfung impfung = new Impfung();
		impfung.setTimestampImpfung(LocalDate.of(2021, 5, 15).atStartOfDay());
		impfung.setImpfstoff(impfstoff);

		impfung.setVerarbreichungsseite(Verarbreichungsseite.LINKS);
		impfung.setVerarbreichungsart(Verarbreichungsart.SUBKUTAN);
		impfung.setVerarbreichungsort(Verarbreichungsort.OBERARM);

		Benutzer benutzer = userPrincipal.getBenutzerOrThrowException();

		impfung.setBenutzerDurchfuehrend(benutzer);
		impfung.setLot("AA");
		impfung.setBenutzerVerantwortlicher(benutzer);
		impfung.setMenge(BigDecimal.valueOf(5.5));

		return impfung;
	}

	private void addKontrolle(Impfdossier impfdossier, Impffolge impffolge) {

		impfdossier.setPersonenkontrolle(new Personenkontrolle());
		final ImpfungkontrolleTermin impfungkontrolleTermin1 = new ImpfungkontrolleTermin();
		impfungkontrolleTermin1.setIdentitaetGeprueft(true);
		Objects.requireNonNull(impfdossier.getPersonenkontrolle());
		if (impffolge == Impffolge.ERSTE_IMPFUNG) {
			impfdossier.getPersonenkontrolle().setKontrolleTermin1(impfungkontrolleTermin1);
		} else {
			impfdossier.getPersonenkontrolle().setKontrolleTermin2(impfungkontrolleTermin1);
		}
	}

	private void runAsInternalSystemAdmin() {
		// Task soll asl Systembenutzer laufen
		Builder builder = new Builder();
		QuarkusSecurityIdentity internalAdmin =
			builder.addRole(SYSTEM_INTERNAL_ADMIN).setPrincipal(() -> SYSTEM_INTERNAL_ADMIN).build();
		association.setIdentity(internalAdmin);
	}

	@NonNull
	public List<String> createRegistrierungenForOnboarding(
		int anzahl,
		@NonNull String odiname,
		@NonNull LocalDate datumTermin1,
		@NonNull LocalDate datumTermin2
	) {
		runAsInternalSystemAdmin();

		final OrtDerImpfung odi = ortDerImpfungRepo
			.getByName(odiname)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_ODI.create(odiname));

		List<String> result = new ArrayList<>();
		for (int i = 0; i < anzahl; i++) {
			final String registrierungsnummer = createRegistrierungForOnboarding(odi, datumTermin1, datumTermin2);
			result.add(registrierungsnummer);
		}
		return result;
	}

	@Transactional(TxType.REQUIRES_NEW)
	private String createRegistrierungForOnboarding(
		@NonNull OrtDerImpfung odi,
		@NonNull LocalDate datumTermin1,
		@NonNull LocalDate datumTermin2
	) {
		final Fragebogen fragebogen = TestdataCreationUtil.createFragebogen();
		final Impfdossier impfdossier = registrierungService.createImpfdossierCovid(fragebogen);

		// Der Einfachheit halber werden immer neue ad-hoc Termine erstellt
		final Impftermin termin1 = terminbuchungService.createOnDemandImpftermin(
			KrankheitIdentifier.COVID, Impffolge.ERSTE_IMPFUNG, odi, datumTermin1.atTime(LocalTime.now()));
		final Impftermin termin2 = terminbuchungService.createOnDemandImpftermin(
			KrankheitIdentifier.COVID, Impffolge.ZWEITE_IMPFUNG, odi, datumTermin2.atTime(LocalTime.now()));

		impfterminRepo.termineSpeichern(impfdossier, termin1, termin2);

		// Kontrolle 1
		addKontrolle(impfdossier, Impffolge.ERSTE_IMPFUNG);
		impfkontrolleService.kontrolleOkForOnboarding(fragebogen, Impffolge.ERSTE_IMPFUNG, null);

		ImpfinformationDto infos =
			new ImpfinformationDto(KrankheitIdentifier.COVID, impfdossier.getRegistrierung(), null, null, impfdossier, null);

		// Impfung 1
		Impfung impfung = createImpfung(impfdossier.getRegistrierung());
		impfdokumentationService.createImpfung(
			infos, odi, Impffolge.ERSTE_IMPFUNG, impfung, false, impfung.getTimestampImpfung(), false);

		// Manchmal die zweite Impfung hinzufuegen
		if (Faker.instance().bool().bool()) {
			// Kontrolle 2
			addKontrolle(impfdossier, Impffolge.ZWEITE_IMPFUNG);
			impfkontrolleService.kontrolleOkForOnboarding(fragebogen, Impffolge.ZWEITE_IMPFUNG, null);

			infos =
				new ImpfinformationDto(KrankheitIdentifier.COVID, impfdossier.getRegistrierung(), impfung, null, impfdossier, null);

			// Impfung 2
			Impfung impfung2 = createImpfung(impfdossier.getRegistrierung());
			impfdokumentationService.createImpfung(
				infos, odi, Impffolge.ZWEITE_IMPFUNG, impfung2, false, impfung2.getTimestampImpfung(), false);
		}

		// Massenupload
		impfdossier.getRegistrierung().setRegistrierungsEingang(RegistrierungsEingang.MASSENUPLOAD);

		// Onboarding erwuenscht
		impfdossier.getRegistrierung().setGenerateOnboardingLetter(true);

		return impfdossier.getRegistrierung().getRegistrierungsnummer();
	}

	@Transactional(TxType.NOT_SUPPORTED)
	public void generateImpfslotsForAllOdi(int year, int month) {
		runAsInternalSystemAdmin();
		List<UUID> odiIdsToGenerateSlotsFor = ortDerImpfungRepo.findIdsOfAllAktivOeffentlichWithTerminVerwaltung();
		LOG.info("VACME-TESTDATA: Starting to create Testtermine for {} OdIs", odiIdsToGenerateSlotsFor.size());

		LocalDateTime startDay = LocalDateTime.of(year, month, 1, 0, 0);
		for (UUID ortDerImpfungId : odiIdsToGenerateSlotsFor) {
			OrtDerImpfung ortDerImpfung = ortDerImpfungRepo.getById(OrtDerImpfung.toId(ortDerImpfungId))
				.orElseThrow(() -> AppFailureException.entityNotFound(OrtDerImpfung.class, ortDerImpfungId, "Ort der Impfung not found"));
			generateImpfslotsAndTermineForOdi(startDay, ortDerImpfung);
		}
		LOG.info("VACME-TESTDATA: Finished creating Testtermine for {} OdIs", odiIdsToGenerateSlotsFor.size());
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void generateImpfslotsAndTermineForOdi(@NonNull LocalDateTime startDay, @NonNull OrtDerImpfung ortDerImpfung) { // muss public sein damit @Transactional verwendet wird
		impfslotService.createEmptyImpfslots(ortDerImpfung, startDay, 1);
		var findImpfslots = impfslotService.find(ortDerImpfung, startDay.toLocalDate(), startDay.plusMonths(1L).toLocalDate());

		for (Impfslot impfslot : findImpfslots) {

			DateTimeRangeJax zeitfenster = new DateTimeRangeJax(impfslot.getZeitfenster().getVon(),impfslot.getZeitfenster().getBis());
			ImpfslotDisplayJax impfslotDisplayJax = new ImpfslotDisplayJax(impfslot.getId(), zeitfenster, 10, 10, 10);
			impfslotService.updateImpfslot(impfslot, impfslotDisplayJax.getUpdateEntityConsumer());
		}
		LOG.info("VACME-TESTDATA: Created Impfslots and Termine for OdI '{}' and month {}", ortDerImpfung.getName(), startDay.getMonth());
	}

}
