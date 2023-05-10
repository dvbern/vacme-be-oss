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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.entities.umfrage.Umfrage;
import ch.dvbern.oss.vacme.entities.umfrage.UmfrageDTO;
import ch.dvbern.oss.vacme.entities.umfrage.UmfrageGruppe;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.UmfrageRepo;
import ch.dvbern.oss.vacme.scheduler.SystemAdminRunnerService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.EnumUtil;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ABGESCHLOSSEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMMUNISIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.NEU;
import static java.util.Locale.GERMAN;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UmfrageService {

	@ConfigProperty(name = "vacme.stufe", defaultValue = "LOCAL")
	String stufe;

	@ConfigProperty(name = "vacme.umfrage.link.gruppe1", defaultValue = "www.dvbern.ch/{}")
	String umfrageLinkGruppe1;

	@ConfigProperty(name = "vacme.umfrage.link.gruppe2", defaultValue = "www.dvbern.ch/{}")
	String umfrageLinkGruppe2;

	@ConfigProperty(name = "vacme.umfrage.link.gruppe3", defaultValue = "www.dvbern.ch/{}")
	String umfrageLinkGruppe3;

	@ConfigProperty(name = "vacme.umfrage.link.gruppe4", defaultValue = "www.dvbern.ch/{}")
	String umfrageLinkGruppe4;

	@ConfigProperty(name = "vacme.umfrage.test.empfaenger", defaultValue = "1234")
	String umfrageTestEmpfaenger;

	private final ImpfinformationenService impfinformationenService;
	private final StammdatenService stammdatenService;
	private final UmfrageRepo umfrageRepo;
	private final FragebogenService fragebogenService;
	private final ImpfungRepo impfungRepo;
	private final SmsService smsService;
	private final SystemAdminRunnerService systemAdminRunnerService;
	private final ImpfdossierService impfdossierService;


	@NonNull
	public StreamingOutput createUmfrageAndDownloadCsv(@NonNull UmfrageGruppe gruppe, int limit) {
		createUmfrage(gruppe, limit);
		return generateCsv(gruppe);
	}

	public void sendUmfrage(@NonNull UmfrageGruppe gruppe) {
		List<Umfrage> kandidaten = umfrageRepo.getUmfrage(gruppe);
		LOG.info("VACME-UMFRAGE: Starte Versenden der Umfrage der Gruppe {}. Anzahl Kandidaten: {}", gruppe, kandidaten.size());
		for (Umfrage umfrage : kandidaten) {
			if (checkIfStatusStillOkay(umfrage)) {
				final String message = getUmfrageMessage(umfrage);
				String empfaenger = umfrage.getMobiltelefon();
				if (!"PROD".equalsIgnoreCase(stufe)) {
					// Auf allen Teststufen wollen wir die SMS an eine definierte Nummer senden, damit nicht aus Versehen SMS
					// an echte Impflinge geschickt werden
					empfaenger = umfrageTestEmpfaenger;
				}

				if (Boolean.TRUE.equals(umfrage.getRegistrierung().getVerstorben())) {
					LOG.warn("Versand abgebrochen, Registrierung ist als verstorben markiert");
				} else if (Boolean.TRUE.equals(umfrage.getRegistrierung().getKeinKontakt())) {
					LOG.warn("Versand abgebrochen, Registrierung ist als keinKontakt markiert");
				} else {
					smsService.sendSMSToRegistrierung(
						empfaenger,
						message,
						umfrage.getRegistrierung());
					LOG.info("VACME-UMFRAGE: Sende Umfrage an Umfragecode {}", umfrage.getUmfrageCode());
				}

				try {
					// Damit nicht 5000 SMS aufs Mal geschickt werden, warten wir nach jedem Senden kurz
					Thread.sleep(150);
				} catch (InterruptedException ex) {
					LOG.error("VACME-UMFRAGE: Sleep intrrupted", ex);
				}
			} else {
				LOG.info("VACME-UMFRAGE: Kandidat entspricht nicht mehr den Kriterien fuer die Umfrage. {}", umfrage.getUmfrageCode());
			}
		}
		LOG.info("VACME-UMFRAGE: Versenden der Umfrage der Gruppe {} beendet", gruppe);
	}

	public void sendUmfrageReminder(@NonNull UmfrageGruppe gruppe) {
		List<Umfrage> kandidaten = umfrageRepo.getUmfrageNichtTeilgenommen(gruppe);
		LOG.info("VACME-UMFRAGE: Starte Reminder der Umfrage der Gruppe {}. Anzahl Kandidaten: {}", gruppe, kandidaten.size());
		for (Umfrage umfrage : kandidaten) {
			if (checkIfStatusStillOkay(umfrage)) {
				final String message = getUmfrageReminderMessage(umfrage);
				String empfaenger = umfrage.getMobiltelefon();
				if (!"PROD".equalsIgnoreCase(stufe)) {
					// Auf allen Teststufen wollen wir die SMS an eine definierte Nummer senden, damit nicht aus Versehen SMS
					// an echte Impflinge geschickt werden
					empfaenger = umfrageTestEmpfaenger;
				}
				if (Boolean.TRUE.equals(umfrage.getRegistrierung().getVerstorben())) {
					LOG.warn("Versand abgebrochen, Registrierung ist als verstorben markiert");
				} else if (Boolean.TRUE.equals(umfrage.getRegistrierung().getKeinKontakt())) {
					LOG.warn("Versand abgebrochen, Registrierung ist als keinKontakt markiert");
				} else {
					smsService.sendSMSToRegistrierung(
						empfaenger,
						message,
						umfrage.getRegistrierung());
					LOG.info("VACME-UMFRAGE: Sende Reminder an Umfragecode {}", umfrage.getUmfrageCode());
				}

				try {
					// Damit nicht 5000 SMS aufs Mal geschickt werden, warten wir nach jedem Senden kurz
					Thread.sleep(150);
				} catch (InterruptedException ex) {
					LOG.error("VACME-UMFRAGE: Sleep intrrupted", ex);
				}
			} else {
				LOG.info("VACME-UMFRAGE: Reminder, Kandidat entspricht nicht mehr den Kriterien fuer die Umfrage. {}", umfrage.getUmfrageCode());
			}
		}
		LOG.info("VACME-UMFRAGE: Versenden des Reminder der Gruppe {} beendet", gruppe);
	}

	private boolean checkIfStatusStillOkay(@NonNull Umfrage umfrage) {
		// todo Affenpocken: VACME-2404 low prio: Umfrage ist nur fuer Covid aktuell
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			umfrage.getRegistrierung().getRegistrierungsnummer(),
			KrankheitIdentifier.COVID);
		// Sicherstellen, dass zum Zeitpunkt des SMS Versands der Status noch dem Zielstatus dieser Umfrage entspricht
		final UmfrageGruppe gruppe = umfrage.getUmfrageGruppe();
		if (UmfrageGruppe.GRUPPE_1 == gruppe || UmfrageGruppe.GRUPPE_3 == gruppe || UmfrageGruppe.GRUPPE_5 == gruppe) {
			return !EnumUtil.isNoneOf(
				infos.getImpfdossier().getDossierStatus(),
				NEU, FREIGEGEBEN);
		}
		return !EnumUtil.isNoneOf(
			infos.getImpfdossier().getDossierStatus(),
			IMPFUNG_1_DURCHGEFUEHRT, IMPFUNG_2_KONTROLLIERT, IMPFUNG_2_DURCHGEFUEHRT, ABGESCHLOSSEN, IMMUNISIERT, ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG);
	}

	@NonNull
	private String getUmfrageMessage(@NonNull Umfrage umfrage) {
		String message = null;
		switch (umfrage.getUmfrageGruppe()) {
		case GRUPPE_1:
			String link1 = umfrageLinkGruppe1.replace("{}", umfrage.getUmfrageCode());
			message = ServerMessageUtil.getMessage("sms_umfrage_gruppe_1", umfrage.getRegistrierung().getLocale(), link1);
			break;
		case GRUPPE_2:
			String link2 = umfrageLinkGruppe2.replace("{}", umfrage.getUmfrageCode());
			message = ServerMessageUtil.getMessage("sms_umfrage_gruppe_2", umfrage.getRegistrierung().getLocale(), link2);
			break;
		case GRUPPE_3:
		case GRUPPE_5:
			String link3 = umfrageLinkGruppe3.replace("{}", umfrage.getUmfrageCode());
			message = ServerMessageUtil.getMessage("sms_umfrage_gruppe_3", umfrage.getRegistrierung().getLocale(), link3);
			break;
		case GRUPPE_4:
		case GRUPPE_6:
			String link4 = umfrageLinkGruppe4.replace("{}", umfrage.getUmfrageCode());
			message = ServerMessageUtil.getMessage("sms_umfrage_gruppe_4", umfrage.getRegistrierung().getLocale(), link4);
			break;
		}
		return message;
	}

	@NonNull
	private String getUmfrageReminderMessage(@NonNull Umfrage umfrage) {
		String message = null;
		switch (umfrage.getUmfrageGruppe()) {
		case GRUPPE_1:
			String link1 = umfrageLinkGruppe1.replace("{}", umfrage.getUmfrageCode());
			message = ServerMessageUtil.getMessage("sms_umfrage_reminder", umfrage.getRegistrierung().getLocale(), link1);
			break;
		case GRUPPE_2:
			String link2 = umfrageLinkGruppe2.replace("{}", umfrage.getUmfrageCode());
			message = ServerMessageUtil.getMessage("sms_umfrage_reminder", umfrage.getRegistrierung().getLocale(), link2);
			break;
		case GRUPPE_3:
		case GRUPPE_5:
			String link3 = umfrageLinkGruppe3.replace("{}", umfrage.getUmfrageCode());
			message = ServerMessageUtil.getMessage("sms_umfrage_reminder", umfrage.getRegistrierung().getLocale(), link3);
			break;
		case GRUPPE_4:
		case GRUPPE_6:
			String link4 = umfrageLinkGruppe4.replace("{}", umfrage.getUmfrageCode());
			message = ServerMessageUtil.getMessage("sms_umfrage_reminder", umfrage.getRegistrierung().getLocale(), link4);
			break;
		}
		return message;
	}

	private void persistUmfrage(
		@NonNull Registrierung registrierung,
		@NonNull UmfrageGruppe gruppe,
		@NonNull String mobiltelefon,
		boolean validPhoneNumber
	) {
		Umfrage umfrage = new Umfrage();
		umfrage.setRegistrierung(registrierung);
		umfrage.setUmfrageGruppe(gruppe);
		umfrage.setUmfrageCode(stammdatenService.createUniqueUmfrageCode());
		umfrage.setMobiltelefon(mobiltelefon);
		umfrage.setValid(validPhoneNumber);
		umfrageRepo.create(umfrage);
	}

	private void createUmfrage(@NonNull UmfrageGruppe gruppe, int limit) {
		LOG.info("VACME-UMFRAGE: Erstelle Umfrage fuer {}, gesuchte Anzahl {}", gruppe, limit);
		int anzahlValid = 0;
		// Wir suchen so lange nach Kandidaten, bis wir die gewuenschte Anzahl gueltige gefunden haben
		while (anzahlValid < limit) {
			int newLimit = limit - anzahlValid;
			int newAnzahlValid = 0;
			switch (gruppe) {
			case GRUPPE_3:
				newAnzahlValid = createUmfrage3AndReturnAnzahlValid(newLimit);
				break;
			case GRUPPE_4:
				newAnzahlValid = createUmfrage4AndReturnAnzahlValid(newLimit);
				break;
			case GRUPPE_5:
				newAnzahlValid = createUmfrage5AndReturnAnzahlValid(newLimit);
				break;
			case GRUPPE_6:
				newAnzahlValid = createUmfrage6AndReturnAnzahlValid(newLimit);
				break;
			}
			// Falls in diesem Durchgang keine neuen gueltigen mehr dazugekommen sind, muessen wir hier wohl
			// unverrichteter Dinge abbrechen
			if (newAnzahlValid == 0) {
				LOG.error("VACME-UMFRAGE: Es gibt insgesamt nicht genuegend Kandidaten fuer Umfrage {}. Gefragt: {}, Gefunden: {}. Breche ab.",
					gruppe, limit, anzahlValid);
				return;
			}
			anzahlValid = anzahlValid + newAnzahlValid;
		}
	}

	private int createUmfrage3AndReturnAnzahlValid(int limit) {
		// Da wohl einige Natelnummer nicht gueltige CH-Nummern sind, lesen wir vorsichtshalber lieber
		// zuviele Kandidaten: 20% mehr als angefordert
		int limitWithBuffer = Math.toIntExact(Math.round(limit * 1.2));
		LOG.info("VACME-UMFRAGE: Umfrage {}, Gesucht sind {} Kandidaten, wir lesen vorsichtshalber {}",
			UmfrageGruppe.GRUPPE_3, limit, limitWithBuffer);
		final List<UmfrageDTO> kandidatenUmfrage3 = umfrageRepo.getKandidatenUmfrage3(limitWithBuffer);
		return checkValidSaveUmfragenAndReturnAnzahlValid(limit, UmfrageGruppe.GRUPPE_3, kandidatenUmfrage3);
	}

	private int createUmfrage4AndReturnAnzahlValid(int limit) {
		// Da wohl einige Natelnummer nicht gueltige CH-Nummern sind, lesen wir vorsichtshalber lieber
		// zuviele Kandidaten: 20% mehr als angefordert
		int limitWithBuffer = Math.toIntExact(Math.round(limit * 1.2));
		LOG.info("VACME-UMFRAGE: Umfrage {}, Gesucht sind {} Kandidaten, wir lesen vorsichtshalber {}",
			UmfrageGruppe.GRUPPE_4, limit, limitWithBuffer);
		final List<UmfrageDTO> kandidatenUmfrage4 = umfrageRepo.getKandidatenUmfrage4(limitWithBuffer);
		return checkValidSaveUmfragenAndReturnAnzahlValid(limit, UmfrageGruppe.GRUPPE_4, kandidatenUmfrage4);
	}

	private int createUmfrage5AndReturnAnzahlValid(int limit) {
		// Da wohl einige Natelnummer nicht gueltige CH-Nummern sind, lesen wir vorsichtshalber lieber
		// zuviele Kandidaten: 20% mehr als angefordert
		int limitWithBuffer = Math.toIntExact(Math.round(limit * 1.2));
		LOG.info("VACME-UMFRAGE: Umfrage {}, Gesucht sind {} Kandidaten, wir lesen vorsichtshalber {}",
			UmfrageGruppe.GRUPPE_5, limit, limitWithBuffer);
		final List<UmfrageDTO> kandidatenUmfrage5 = umfrageRepo.getKandidatenUmfrage5(limitWithBuffer);
		return checkValidSaveUmfragenAndReturnAnzahlValid(limit, UmfrageGruppe.GRUPPE_5, kandidatenUmfrage5);
	}

	private int createUmfrage6AndReturnAnzahlValid(int limit) {
		// Da wohl einige Natelnummer nicht gueltige CH-Nummern sind, lesen wir vorsichtshalber lieber
		// zuviele Kandidaten: 20% mehr als angefordert
		int limitWithBuffer = Math.toIntExact(Math.round(limit * 1.2));
		LOG.info("VACME-UMFRAGE: Umfrage {}, Gesucht sind {} Kandidaten, wir lesen vorsichtshalber {}",
			UmfrageGruppe.GRUPPE_6, limit, limitWithBuffer);
		final List<UmfrageDTO> kandidatenUmfrage6 = umfrageRepo.getKandidatenUmfrage6(limitWithBuffer);
		return checkValidSaveUmfragenAndReturnAnzahlValid(limit, UmfrageGruppe.GRUPPE_6, kandidatenUmfrage6);
	}

	private int checkValidSaveUmfragenAndReturnAnzahlValid(
		int limit,
		@NonNull UmfrageGruppe gruppe,
		@NonNull List<UmfrageDTO> kandidaten
	) {
		int nbrValid = 0; // Wir zaehlen die erfolgreichen
		for (UmfrageDTO umfrageDTO : kandidaten) {
			final boolean validPhone = PhoneNumberUtil.isMobileNumber(umfrageDTO.getMobile());
			final long age = DateUtil.getAge(umfrageDTO.getRegistrierung().getGeburtsdatum());
			final boolean validAge = age >= 25 && age <=49;
			final boolean valid = validPhone && validAge;
			// Wir speichern auch die ungueltigen, damit bei der naechsten Umfrage
			// diese nicht mehr als Kandidaten gewaehlt werden
			persistUmfrage(umfrageDTO.getRegistrierung(), gruppe, umfrageDTO.getMobile(), valid);
			if (valid) {
				nbrValid++;
				if (nbrValid >= limit) {
					// Wir haben genug gueltige gefunden und koennen abbrechen
					LOG.info("VACME-UMFRAGE: Genuegend Kandidaten gefunden fuer Umfrage {}: {}", gruppe, limit);
					return nbrValid;
				}
			}
		}
		// Wenn wir hierhin kommen, hatten wir vermutlich keine Kandidaten uebrig
		if (nbrValid < limit) {
			LOG.warn("VACME-UMFRAGE: Es gab nicht genuegend Kandidaten fuer Umfrage {}. Gefragt: {}, Gefunden: {}",
				gruppe, limit, nbrValid);
		}
		return nbrValid;
	}

	@NonNull
	public StreamingOutput generateCsv(@NonNull UmfrageGruppe gruppe) {

		List<Umfrage> kandidaten = umfrageRepo.getUmfrage(gruppe);
		return output -> {
			LOG.info("VACME-UMFRAGE: Starte die Erstellung des CSV fuer Gruppe {}", gruppe);
			try (
				Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
				CSVPrinter printer = CSVFormat.DEFAULT.withHeader(csvTitleRow()).print(writer);
			) {
				kandidaten.forEach(kand -> {
					// Fragebogen und Impfung lesen
					final Registrierung registrierung = kand.getRegistrierung();
					Fragebogen fragebogen =
						fragebogenService.findFragebogenByRegistrierungsnummer(registrierung.getRegistrierungsnummer());
					// TODO Affenpocken: VACME-2404 Umfrage aktuell nur fuer COVID
					final Impfdossier impfdossier = impfdossierService
						.findImpfdossierForRegnumAndKrankheitOptional(registrierung.getRegistrierungsnummer(), KrankheitIdentifier.COVID)
						.orElseThrow();
					Impfung impfung1 = null;
					if (UmfrageGruppe.GRUPPE_2 == gruppe || UmfrageGruppe.GRUPPE_4 == gruppe || UmfrageGruppe.GRUPPE_6 == gruppe) {
						if (impfdossier.getBuchung().getImpftermin1() != null) {
							impfung1 = impfungRepo.getByImpftermin(impfdossier.getBuchung().getImpftermin1()).orElse(null);
						}
					}
					try {
						printer.printRecord((Object[]) toCsvRecord(kand, fragebogen, impfung1));
						printer.flush();
					} catch (IOException e) {
						LOG.error("VACME-UMFRAGE: CSV konnte nicht erstellt werden fuer Gruppe {}", gruppe, e);
						throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate umfrage");
					}
				});
			} catch (IOException e) {
				LOG.error("VACME-UMFRAGE: CSV konnte nicht erstellt werden fuer Gruppe {}", gruppe, e);
				throw AppValidationMessage.ILLEGAL_STATE.create("Could not generate umfrage");
			}
		};
	}

	@NonNull
	private String[] csvTitleRow() {
		String[] header = {
			"Gruppe",
			"PLZ",
			"Alter",
			"Geschlecht",
			"Registrierungsdatum",
			"Datum Impfung 1",
			"Sprache",
			"Lebensumstaende",
			"Berufliche Taetigkeit",
			"Umfragecode"
		};
		return header;
	}

	@NonNull
	private String[] toCsvRecord(@NonNull Umfrage umfrage, @NonNull Fragebogen fragebogen, @Nullable Impfung impfung1) {
		final Registrierung registrierung = umfrage.getRegistrierung();
		final DateTimeFormatter formatter = DateUtil.DEFAULT_DATE_TIME_FORMAT.apply(GERMAN);
		String[] values = {
			umfrage.getUmfrageGruppe().name(),
			registrierung.getAdresse().getPlz(),
			String.valueOf(DateUtil.getAge(registrierung.getGeburtsdatum())),
			ServerMessageUtil.translateEnumValue(registrierung.getGeschlecht(), Locale.GERMAN),
			formatter.format(registrierung.getRegistrationTimestamp()),
			impfung1 != null ? formatter.format(impfung1.getTimestampImpfung()) : "",
			ServerMessageUtil.translateEnumValue(registrierung.getSprache(), Locale.GERMAN),
			ServerMessageUtil.translateEnumValue(fragebogen.getLebensumstaende(), Locale.GERMAN),
			ServerMessageUtil.translateEnumValue(fragebogen.getBeruflicheTaetigkeit(), Locale.GERMAN),
			umfrage.getUmfrageCode()
		};
		return values;
	}

	public void completeUmfrage(@NonNull String code) {
		Umfrage umfrage = umfrageRepo.getUmfrageByCode(code);
		if (umfrage != null) {
			systemAdminRunnerService.completeUmfrage(umfrage);
			LOG.info("VACME-UMFRAGE: Umfrage abgeschlossen fuer Code {}", code);
		}
	}
}
