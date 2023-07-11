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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import ch.dvbern.oss.vacme.entities.UserPrincipal;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.onboarding.Onboarding;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.jax.base.AdresseJax;
import ch.dvbern.oss.vacme.jax.korrektur.PersonendatenKorrekturJax;
import ch.dvbern.oss.vacme.jax.registration.SelfserviceEditJax;
import ch.dvbern.oss.vacme.repo.AudHelperRepo;
import ch.dvbern.oss.vacme.repo.ExternesZertifikatRepo;
import ch.dvbern.oss.vacme.repo.FragebogenRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierFileRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.OnboardingRepo;
import ch.dvbern.oss.vacme.repo.PersonenkontrolleRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungFileRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.scheduler.SystemAdminRunnerService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.exception.ConstraintViolationException;

import static ch.dvbern.oss.vacme.entities.types.BenutzerRolle.IMPFWILLIGER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.KONTROLLIERT_BOOSTER;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RegistrierungService {

	private final UserPrincipal userPrincipal;
	private final RegistrierungRepo registrierungRepo;
	private final PersonenkontrolleRepo personenkontrolleRepo;
	private final FragebogenRepo fragebogenRepo;
	private final ImpfterminRepo impfterminRepo;
	private final StammdatenService stammdatenService;
	private final ConfirmationService confirmationService;
	private final KeyCloakRegService keyCloakRegService;
	private final BenutzerService benutzerService;
	private final RegistrierungFileRepo registrierungFileRepo;
	private final ImpfdossierFileRepo impfdossierFileRepo;
	private final SmsService smsService;
	private final SystemAdminRunnerService systemAdminRunnerService;
	private final KorrekturService korrekturService;
	private final ImpfinformationenService impfinformationenService;
	private final ImpfdossierRepo impfdossierRepo;
	private final AudHelperRepo audHelperRepo;
	private final ExternesZertifikatRepo externesZertifikatRepo;
	private final ZertifikatService zertifikatService;
	private final OnboardingRepo onboardingRepo;
	private final ImpfdossierService impfdossierService;


	@NonNull
	public Impfdossier createImpfdossierCovid(@NonNull Fragebogen fragebogen) {
		Registrierung registrierung = fragebogen.getRegistrierung();
		if (!registrierung.isNew()) {
			throw AppValidationMessage.EXISTING_REGISTRIERUNG.create(registrierung.getRegistrierungsnummer());
		}
		// Berechnete Attribute setzen
		final Benutzer currentBenutzer = userPrincipal.getBenutzerOrThrowException();
		if (userPrincipal.isCallerInRole(IMPFWILLIGER)) {
			registrierung.setRegistrierungsEingang(RegistrierungsEingang.ONLINE_REGISTRATION);
			registrierung.setBenutzerId(currentBenutzer.getId());
		} else {
			// Alle anderen sind Callcenter. Fuer die Registrierung vor Ort (am ODI) gibt es einen separaten Service
			registrierung.setRegistrierungsEingang(RegistrierungsEingang.CALLCENTER_REGISTRATION);
			registrierung.setBenutzerId(null);
		}
		registrierung.setRegistrierungsnummer(stammdatenService.createUniqueRegistrierungsnummer());
		registrierung.setPrioritaet(stammdatenService.calculatePrioritaet(fragebogen));
		registrierung.setRegistrationTimestamp(LocalDateTime.now());

		// Speichern
		try {
			fragebogenRepo.create(fragebogen);
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ConstraintViolationException) {
				LOG.warn("VACME-WARNING: Es wurde versucht, eine Registrierung zweimal zu speichern von Benutzer: {}", registrierung.getBenutzerId());
				// we catch this exception because it happens frequently in production
				throw AppValidationMessage.REGISTRIERUNG_DOES_ALREADY_EXIST.create();
			}
			throw e;
		}
		// TODO Affenpocken: Aktuell wird das Covid-Dossier immer sofort erstellt
		Impfdossier impfdossier = impfdossierService.createImpfdossier(registrierung, KrankheitIdentifier.COVID); // covid Dossier gibts immer
		impfdossierService.setStatusAccordingToPrioritaetFreischaltung(impfdossier);

		// Registrierung per SMS oder Post senden
		confirmationService.sendRegistrierungsbestaetigung(registrierung);
		return impfdossier;
	}

	public void registrierungErneutSenden(@NonNull Registrierung registrierung) {
		confirmationService.resendRegistrierungsbestaetigung(registrierung);
	}

	@NonNull
	public Registrierung findRegistrierung(@NonNull String registrierungsnummer) {
		Registrierung registrierung = registrierungRepo.getByRegistrierungnummer(registrierungsnummer).orElseThrow(
			() -> AppValidationMessage.UNKNOWN_REGISTRIERUNGSNUMMER.create(registrierungsnummer));
		return registrierung;
	}

	public void acceptElektronischerImpfausweis(@NonNull Registrierung registrierung) {
		registrierung.setAbgleichElektronischerImpfausweis(true);
		registrierungRepo.update(registrierung);
	}

	@Nullable
	public Registrierung findRegistrierungByUser(@NonNull UUID userId) {
		Registrierung registrierung = registrierungRepo.getByUserId(userId).orElse(null);
		return registrierung;
	}

	@NonNull
	public Optional<Registrierung> findRegistrierungById(@NonNull UUID registrierungId) {
		return registrierungRepo.getById(Registrierung.toId(registrierungId));
	}

	public void updatePersonalien(@NonNull Fragebogen fragebogen, @NonNull AdresseJax updateJax) {
		Registrierung registrierung = fragebogen.getRegistrierung();

		Adresse update = updateJax.toEntity();
		if (!Objects.equals(registrierung.getAdresse(), update)) {
			PersonendatenKorrekturJax korrekturJax = PersonendatenKorrekturJax.from(fragebogen);
			korrekturJax.setAdresse(updateJax);
			korrekturService.personendatenKorrigieren(fragebogen, korrekturJax);
		}
	}

	public void updateSelfserviceData(@NonNull Fragebogen fragebogen, @NonNull Registrierung registrierung, @NonNull SelfserviceEditJax updateJax) {
		// Adressaenderung ohne Konsequenzen
		Adresse adresse = registrierung.getAdresse();
		adresse.setAdresse1(updateJax.getAdresse().getAdresse1());
		adresse.setAdresse2(updateJax.getAdresse().getAdresse2());
		adresse.setPlz(updateJax.getAdresse().getPlz());
		adresse.setOrt(updateJax.getAdresse().getOrt());

		// Fragebogen Update ohne Konsequenzen
		assert updateJax.getChronischeKrankheiten() != null;
		fragebogen.setChronischeKrankheiten(updateJax.getChronischeKrankheiten());
		assert updateJax.getLebensumstaende() != null;
		fragebogen.setLebensumstaende(updateJax.getLebensumstaende());
		assert updateJax.getBeruflicheTaetigkeit() != null;
		fragebogen.setBeruflicheTaetigkeit(updateJax.getBeruflicheTaetigkeit());

		registrierung.setBemerkung(updateJax.getBemerkung());

		// Krankenkassennummer Update mit Archivierung der Nummer
		registrierung.setKrankenkasse(updateJax.getKrankenkasse());
		registrierung.setKrankenkasseKartenNrAndArchive(updateJax.getKrankenkasseKartenNr()); // setter macht auch archivierung
		registrierung.setAuslandArt(updateJax.getAuslandArt());
		registrierung.setKeinKontakt(updateJax.getKeinKontakt());

		registrierung.setTimestampInfoUpdate(updateJax.getTimestampInfoUpdate());
	}

	public void impfungVerweigert(
		@NonNull ImpfinformationDto infos
	) {
		Registrierung registrierung = infos.getRegistrierung();
		Impfdossier impfdossier = infos.getImpfdossier();
		switch (impfdossier.getDossierStatus()) {
		case IMPFUNG_1_KONTROLLIERT:
			ImpfdossierStatus lastStatus = impfdossierService.ermittleLetztenDossierStatusVorKontrolle1(impfdossier);
			LOG.info(
				"VACME-INFO: Impfwillige Person '{}' konnte nicht geimpft werden. Setze Status zurueck auf {}",
				registrierung.getRegistrierungsnummer(),
				lastStatus);
			impfdossier.setDossierStatus(lastStatus);
			break;
		case IMPFUNG_2_KONTROLLIERT:
			impfdossier.setDossierStatus(IMPFUNG_1_DURCHGEFUEHRT);
			break;
		case KONTROLLIERT_BOOSTER:
			var eintrag = ImpfinformationenService.getImpfdossierEintragForKontrolle(infos).orElseThrow();
			ImpfdossierStatus lastStatusBooster = impfdossierService.ermittleLetztenDossierStatusVorKontrolleBooster(
				infos.getImpfdossier(),
				eintrag);
			LOG.info(
				"VACME-INFO: Impfwillige Person '{}' konnte nicht geimpft werden. Setze Status von KONTROLLIERT_BOOSTER zurueck auf {}",
				registrierung.getRegistrierungsnummer(),
				lastStatusBooster);
			impfdossier.setDossierStatus(lastStatusBooster);
			break;

		default:
			ValidationUtil.validateStatusOneOf(impfdossier, IMPFUNG_1_KONTROLLIERT, IMPFUNG_2_KONTROLLIERT, KONTROLLIERT_BOOSTER);
		}
		impfdossierRepo.update(impfdossier);
		registrierungRepo.update(registrierung); // todo affenpocken; brachts das?
	}

	@NonNull
	public List<Registrierung> searchRegistrierungByKvKNummer(@NonNull String kvkNummer) {
		return registrierungRepo.searchRegistrierungByKvKNummer(kvkNummer);
	}

	public void deleteRegistrierung(@NonNull Registrierung registrierung, @NonNull Fragebogen fragebogen) {
		// Darf nur VOR der ersten Impfung gemacht werden
		if (impfinformationenService.hasAnyVacmeImpfungen(registrierung)) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Registrierung " + registrierung.getRegistrierungsnummer() + " hat schon  VACME-Imfpungen");
		}
		final List<Zertifikat> allZertifikate = zertifikatService.getAllZertifikateRegardlessOfRevocation(registrierung);
		if (!allZertifikate.isEmpty()) {
			throw AppValidationMessage.DELETE_NOT_POSSIBLE_BECAUSE_ZERTIFIKAT.create(registrierung.getRegistrierungsnummer());
		}
		// Onboarding
		final List<Onboarding> onboardingList = onboardingRepo.findByRegistrierung(registrierung);
		for (Onboarding onboarding : onboardingList) {
			onboardingRepo.delete(onboarding);
			audHelperRepo.deleteOnboardingDataInAuditTables(onboarding);
		}
		// Loeschungen immer loggen
		LOG.info("VACME-INFO: Fragebogen {} geloescht durch {}. RegistrierungsID {}",
			registrierung.getRegistrierungsnummer(),
			userPrincipal.getBenutzerOrThrowException().getBenutzername(),
			registrierung.getId());

		// Falls es einen Benutzer gibt (Online Anmeldung) diesen auch loeschen
		final UUID idOfOnlineBenutzer = registrierung.getBenutzerId();
		if (idOfOnlineBenutzer != null) {
			final Optional<Benutzer> benutzerOptional = benutzerService.getById(Benutzer.toId(idOfOnlineBenutzer));
			benutzerOptional.ifPresent(this::deleteBenutzerIfExists);
		}
		// Alle Dokumente loeschen, die zu dieser Reg gehoeren
		registrierungFileRepo.deleteAllRegistrierungFilesForReg(registrierung);
		// Die Audittabellen loeschen
		audHelperRepo.deleteFragebogenDataInAuditTables(fragebogen);
		// Fragebogen inkl. aller angehaengten Objekte loeschen (e.g. Impfungkontrolle fuer 1/2)
		fragebogenRepo.delete(fragebogen.toId());
		// Alle Impfdossiers aller Krankheiten loeschen
		for (KrankheitIdentifier krankheitIdentifier : KrankheitIdentifier.values()) {
			impfdossierRepo.findImpfdossierForReg(registrierung, krankheitIdentifier)
				.ifPresent(impfdossier -> {
					// Evtl. vorhandene Termine freigeben
					impfterminRepo.termine1Und2Freigeben(impfdossier);
					// da wir nur loeschen, wenn wir noch keine Impfung haben koennen wir nur einen 1. Boostertermin haben (ie. ext. Zertifikat erfasst oder Krankheit ohne Termin 1/2)
					firstAndOnlyBoosterterminFreigebenIfExists(impfdossier);
					if (impfdossier.getPersonenkontrolle() != null) {
						personenkontrolleRepo.delete(impfdossier.getPersonenkontrolle().toId());
					}
					// DossierFiles loeschen
					impfdossierFileRepo.deleteAllImpfdossierFilesForDossier(impfdossier);
					// externes Zert loeschen wenn vorhanden
					externesZertifikatRepo.findExternesZertifikatForDossier(impfdossier)
						.ifPresent(externesZertifikat -> {
							audHelperRepo.deleteExternesZertifikatInAuditTables(externesZertifikat);
							externesZertifikatRepo.remove(externesZertifikat);
							LOG.info("... externes Zertifikat {} geloescht ({})", krankheitIdentifier, registrierung.getRegistrierungsnummer());
						});

					audHelperRepo.deleteImpfdossierDataInAuditTables(impfdossier);
					impfdossierRepo.delete(impfdossier.toId());
					LOG.info("... Impfdossier {} geloescht ({})", krankheitIdentifier, registrierung.getRegistrierungsnummer());
				});
		}

		registrierungRepo.delete(registrierung.toId());
	}

	private void firstAndOnlyBoosterterminFreigebenIfExists(@Nullable Impfdossier impfdossier) {
		if (impfdossier != null) {
			validateOnlyOneEintragwithTermin(impfdossier);

			impfdossier.getOrderedEintraege().stream().
				findFirst()
				.ifPresent(impfterminRepo::boosterTerminFreigeben);
		}
	}

	private void validateOnlyOneEintragwithTermin(@NonNull Impfdossier impfdossier) {
		long eintraegeWithTermin = impfdossier.getOrderedEintraege().stream()
			.filter(impfdossiereintrag -> impfdossiereintrag.getImpftermin() != null)
			.count();
		if (eintraegeWithTermin > 1) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Dossier " + impfdossier.getRegistrierung().getRegistrierungsnummer()
				+ " hatte mehr als einen Dossiereintrag mit Impftermin. Dies ist bei der Accountloeschung nicht "
				+ "erwartet");
		}
	}

	public void deleteBenutzer(@NonNull Benutzer benutzer) {
		deleteBenutzerInVacmeAndAudit(benutzer);
		// Die ID in KeyCloak entspricht der ID des Vacme-Benutzers!
		keyCloakRegService.removeUser(benutzer.getId().toString());
	}

	public void deleteBenutzerIfExists(@NonNull Benutzer benutzer) {
		deleteBenutzerInVacmeAndAudit(benutzer);
		// Die ID in KeyCloak entspricht der ID des Vacme-Benutzers!
		keyCloakRegService.removeUserIfExists(benutzer.getId().toString());
	}

	private void deleteBenutzerInVacmeAndAudit(@NonNull Benutzer benutzer) {
		// Die Audittabellen loeschen
		audHelperRepo.deleteBenutzerDataInAuditTables(benutzer);
		// Den Benutzer in VacMe ebenfalls loeschen
		benutzerService.delete(benutzer.toId());
	}

	public void sendBenutzernameForRegistrierung(@NonNull String registrierungsnummer) {
		// Wir geben explizit keine Fehlermeldungen zurueck, wenn z.B. keine Registrierung gefunden wird,
		// diese eine falsche Eingangsart hat, oder der Benutzer nicht gefunden wird, um keine Informationen
		// nach aussen preiszugeben (Dieser Service ist public aufrufbar!)
		final Optional<Registrierung> registrierungOptional = registrierungRepo.getByRegistrierungnummer(registrierungsnummer);
		if (registrierungOptional.isPresent()) {
			final Registrierung registrierung = registrierungOptional.get();
			if (RegistrierungsEingang.ONLINE_REGISTRATION == registrierung.getRegistrierungsEingang()) {
				if (registrierung.getBenutzerId() != null) {
					final Optional<Benutzer> benutzerOptional = benutzerService.getById(Benutzer.toId(registrierung.getBenutzerId()));
					if (benutzerOptional.isPresent()) {
						Benutzer benutzer = benutzerOptional.get();
						if (benutzer.getBenutzernameGesendetTimestamp() != null) {
							// Pruefen, ob er schon wieder darf
							long between = DateUtil.getMinutesBetween(benutzer.getBenutzernameGesendetTimestamp(), LocalDateTime.now());
							if (between < Constants.MIN_ABSTAND_ZWISCHEN_BENUTZERNAME_ABFRAGE_IN_MINUTEN) {
								LOG.info("VACME-INFO: Es wurde versucht, den Benutzernamen fuer {} neu anzufordern, obwohl die letzte Abfrage erst "
									+ "{} Minuten her war", registrierungsnummer, between);
								return;
							}
						}
						systemAdminRunnerService.setBenutzernameGesendetTimestamp(benutzer);
						smsService.sendBenutzername(benutzer, registrierung.getLocale());
					}
				}
			}
		}
	}

	public void runPriorityUpdateForGrowingChildren() {
		runPriorityUpgradeToGroupForAge(12, Prioritaet.Q, Prioritaet.P);
		runPriorityUpgradeToGroupForAge(12, Prioritaet.T, Prioritaet.S);

	}

	private void runPriorityUpgradeToGroupForAge(int age, Prioritaet prioritaetFrom, Prioritaet prioritaetTo){
		List<String> foundRegs = registrierungRepo.getRegnumsOfGroupWithAgeGreaterOrEq(prioritaetFrom, age);

		for (String regnum : foundRegs) {
			Registrierung registrierung =
				registrierungRepo.getByRegistrierungnummer(regnum).orElseThrow(() -> AppFailureException.entityNotFound(Registrierung.class, "regnum:" + regnum));
			registrierung.setPrioritaet(prioritaetTo);
		}
		LOG.info("VACME-PRIORITY-UPDATE updated {} Regs that turned older than {} to Prio {} from {}", foundRegs.size(), age, prioritaetTo, prioritaetFrom);
	}
}
