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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.shared.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class CheckFreieZweittermineService {

	@ConfigProperty(name = "vacme.cache.nextfrei.maxrange.months", defaultValue = Constants.DEFAULT_CUTOFF_TIME_MONTHS_FOR_FREE_TERMINE)
	String maxrangeToLookForFreieTermineInMonths;

	private final OrtDerImpfungRepo ortDerImpfungRepo;
	private final ImpfterminRepo impfterminRepo;
	private final BenutzerService benutzerService;
	private final MailService mailService;
	private final SettingsService settingsService;

	public CheckFreieZweittermineService(
		@NonNull OrtDerImpfungRepo ortDerImpfungRepo,
		@NonNull ImpfterminRepo impfterminRepo,
		@NonNull BenutzerService benutzerService,
		@NonNull MailService mailService,
		@NonNull SettingsService settingsService
	) {
		this.ortDerImpfungRepo = ortDerImpfungRepo;
		this.impfterminRepo = impfterminRepo;
		this.benutzerService = benutzerService;
		this.mailService = mailService;
		this.settingsService = settingsService;
	}

	public void analyseFreieZweittermine() {
		LOG.info("VACME-ZWEITTERMINE: Analysiere freie Zweittermine...");
		final List<OrtDerImpfung> odiList = ortDerImpfungRepo.findAllActiveOeffentlich();
		List<OrtDerImpfung> odisMitTerminen =
			odiList.stream().filter(OrtDerImpfung::isTerminverwaltung).collect(Collectors.toList());
		LOG.debug("VACME-ZWEITTERMINE: folgende OdI werden analysiert {}",
			odiList.stream().map(OrtDerImpfung::getName).collect(Collectors.joining(",")));
		for (OrtDerImpfung odi : odisMitTerminen) {
			analyseFreieZweittermine(odi);
		}
		LOG.info("VACME-ZWEITTERMINE: Analyse freie Zweittermine beendet.");
	}

	private void analyseFreieZweittermine(@NonNull OrtDerImpfung odi) {
		// Naechsten freien Termin suchen
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusMonths(getTerminsucheMaxRangeMonths());
		final LocalDateTime firstFreierTermin = impfterminRepo
			.findNextFreierImpftermin(odi, Impffolge.ERSTE_IMPFUNG, tomorrow, endDate, KrankheitIdentifier.COVID);
		if (firstFreierTermin == null) {
			LOG.debug("VACME-ZWEITTERMINE: Es gibt keinen freien Ersttermin im OdI {} im Range von {} bis {}. Wir koennen abbrechen ",
				odi.getName(),
				DateUtil.formatDate(tomorrow, Locale.GERMAN),
				DateUtil.formatDate(endDate, Locale.GERMAN));
			return;
		}
		// Ab dem ersten Freien Termin wird eine Woche analysiert
		Set<LocalDate> listOfProblematicDates = new LinkedHashSet<>();
		LocalDate datum = firstFreierTermin.toLocalDate();
		for (int i = 0; i < Constants.TOLERANZ_SPAN_DAYS; i++) {
			LocalDate analyziert = datum.plusDays(i);
			if (!hasEnoughZweittermine(odi, analyziert)) {
				LOG.warn("VACME-ZWEITTERMINE: ODI {}: Für die freien Erstermine am Tag {} hat es keine passenden Zweittermine",
					odi.getName(), analyziert);
				listOfProblematicDates.add(analyziert);
			}
		}
		sendMail(odi, listOfProblematicDates);
	}

	private boolean hasEnoughZweittermine(@NonNull OrtDerImpfung odi, @NonNull LocalDate datum) {
		// Wir vergleichen die Anzahl freie Termine 1 mit den Anzahl freien Terminen 2 in 4-5 Wochen
		// Dies bedeutet auch, dass ein Zeittermin fuer mehrere Ersttermine als potenzieller Zweittermin zaehlt - solange,
		// bis jemand den ersten gebucht hat
		final int desiredDays = this.settingsService.getSettings().getDistanceImpfungenDesired();
		final int maxDays = this.settingsService.getSettings().getDistanceImpfungenMaximal();

		final long freiImpfung1 = impfterminRepo.getAnzahlFreieTermine(odi, Impffolge.ERSTE_IMPFUNG, datum, datum, KrankheitIdentifier.COVID);
		if (freiImpfung1 > 0) {
			final long freiImpfung2 = impfterminRepo.getAnzahlFreieTermine(odi, Impffolge.ZWEITE_IMPFUNG,
				datum.plusDays(desiredDays),
				datum.plusDays(maxDays),
				KrankheitIdentifier.COVID);
			return freiImpfung1 <= freiImpfung2;
		}
		return true;
	}

	private void sendMail(@NonNull OrtDerImpfung odi, @NonNull Set<LocalDate> problemDates) {
		if (!problemDates.isEmpty()) {
		final Set<String> mailEmpfaenger = getMailEmpfaengerOfOdi(odi);
			mailService.sendCheckFreieZweittermineMail(mailEmpfaenger, odi, problemDates);
		} else{
			LOG.debug("VACME-ZWEITTERMINE: Kein Problem mit zuwenigen Zweitterminen im OdI {}", odi.getName());
		}
	}

	@NonNull
	private Set<String> getMailEmpfaengerOfOdi(@NonNull OrtDerImpfung odi) {
		HashSet<String> mailEmpfaenger = new HashSet<>();
		final List<Benutzer> benutzerOrganisationsverantwortung =
			benutzerService.getBenutzerByRolleAndOrtDerImpfung(odi, BenutzerRolle.OI_BENUTZER_VERWALTER);
		for (Benutzer benutzer : benutzerOrganisationsverantwortung) {
			mailEmpfaenger.add(benutzer.getEmail());
		}
		final List<Benutzer> benutzerFachverantwortungBAB =
			benutzerService.getBenutzerByRolleAndOrtDerImpfung(odi, BenutzerRolle.OI_IMPFVERANTWORTUNG);
		for (Benutzer benutzer : benutzerFachverantwortungBAB) {
			mailEmpfaenger.add(benutzer.getEmail());
		}
		return mailEmpfaenger;
	}

	private long getTerminsucheMaxRangeMonths() {
		try {
			String maxMonthsFuture = maxrangeToLookForFreieTermineInMonths;
			if (maxMonthsFuture == null) {
				final Config config = ConfigProvider.getConfig(); // read from static config if not set otherwise, makes testing easier
				maxMonthsFuture = config.getValue("vacme.cache.nextfrei.maxrange.months", String.class);
			}
			return Long.parseLong(maxMonthsFuture);
		} catch (NumberFormatException exception) {
			LOG.error("Missconfiguration: vacme.cache.nextfrei.maxrange.months must be numeric, using default value " + Constants.DEFAULT_CUTOFF_TIME_MONTHS_FOR_FREE_TERMINE );
			return Long.parseLong(Constants.DEFAULT_CUTOFF_TIME_MONTHS_FOR_FREE_TERMINE);
		}
	}
}
