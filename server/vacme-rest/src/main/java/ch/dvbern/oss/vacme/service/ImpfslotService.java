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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoField;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.embeddables.DateTimeRange;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.jax.impfslot.ImpfslotValidationJax;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfslotRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
@Transactional
public class ImpfslotService {

	// FIXME: Slots nicht fuer ein Kalender Jahr, sondern einfach fuer +12 Monate. Mit Xaver absprechen
	/*@ConfigProperty(name = "impfslot.year", defaultValue = "2021")
	protected Integer year;*/

	@ConfigProperty(name = "impfslot.duration", defaultValue = "30")
	protected Integer duration;

	@ConfigProperty(name = "impfslot.work.start", defaultValue = "6")
	protected Integer start;

	@ConfigProperty(name = "impfslot.work.end", defaultValue = "22")
	protected Integer end;

	private final ImpfslotRepo impfslotRepo;
	private final ImpfterminRepo impfterminRepo;
	private final SettingsService settingsService;
	private final ImpfdossierRepo impfdossierRepo;

	@Inject
	public ImpfslotService(
		@NonNull ImpfslotRepo impfslotRepo,
		@NonNull ImpfterminRepo impfterminRepo,
		@NonNull SettingsService settingsService,
		@NonNull ImpfdossierRepo impfdossierRepo
	) {
		this.impfslotRepo = impfslotRepo;
		this.impfterminRepo = impfterminRepo;
		this.settingsService = settingsService;
		this.impfdossierRepo = impfdossierRepo;
	}

	@NonNull
	public Impfslot create(@NonNull Impfslot impfslot) {
		impfslotRepo.create(impfslot);
		return impfslot;
	}

	public void createEmptyImpfslots(@NonNull OrtDerImpfung ortDerImpfung, @NonNull LocalDateTime startDay, int monthsAmount) {
		Duration slotDuration = Duration.ofMinutes(duration);
		Predicate<LocalDateTime> filter = getDailyFilter();
		AtomicInteger generatedCount = new AtomicInteger();
		if (ortDerImpfung.getKrankheiten() == null) {
			LOG.error("Tried adding Impfslots without any Krankheiten assigned to Odi");
			return;
		}
		ortDerImpfung.getKrankheiten().forEach(krankheit -> {
			Set<LocalDateTime> alreadyExistsslots = impfslotRepo
				.find(
					ortDerImpfung,
					startDay.toLocalDate(),
					startDay.plusMonths(monthsAmount).toLocalDate(),
					krankheit.getKrankheitIdentifier())
				.stream()
				.map(Impfslot::getZeitfenster)
				.map(DateTimeRange::getVon)
				.collect(Collectors.toSet());
			LOG.debug(String.format("Bestehende Impfslot gefunden fuer %s (total):(%d)", krankheit.getKrankheitIdentifier(), alreadyExistsslots.size()));

			getSlotStream(startDay, startDay.plus(Period.ofMonths(monthsAmount)), slotDuration, filter)
				.filter(d -> !alreadyExistsslots.contains(d))
				.forEach(d -> {
					LOG.trace(String.format("Erstelle Impfslot fuer Krankheit %s und Zeitraum (%2$td-%2$tm-%2$tY %2$tT - %3$td-%3$tm-%3$tY "
						+ "%3$tT)", krankheit.getKrankheitIdentifier(), d, d.plus(slotDuration)));
					Impfslot impfslot = Impfslot.of(krankheit.getKrankheitIdentifier(), ortDerImpfung, DateTimeRange.of(d, d.plus(slotDuration)));
					impfslotRepo.create(impfslot);
					generatedCount.getAndIncrement();
				});
		});
		LOG.debug("Created {} new Impfslots", generatedCount);
	}

	protected Stream<LocalDateTime> getSlotStream(
		LocalDateTime startDay, LocalDateTime endDay, Duration slotDuration,
		Predicate<LocalDateTime> filter) {
		return Stream.iterate(startDay, d -> d.isBefore(endDay), d -> d.plus(slotDuration))
			.filter(filter);
	}

	protected Predicate<LocalDateTime> getDailyFilter() {
		ValueRange zeitraum = ValueRange.of(
			LocalDate.now().atTime(start, 0).get(ChronoField.MINUTE_OF_DAY),
			LocalDate.now().atTime(end, 0).minusMinutes(1).get(ChronoField.MINUTE_OF_DAY));
		return localDateTime -> zeitraum.isValidValue(localDateTime.get(ChronoField.MINUTE_OF_DAY));
	}

	@NonNull
	public Impfslot getById(@NonNull ID<Impfslot> impfslotID) {
		return impfslotRepo
			.getById(impfslotID)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_IMPFTERMIN.create(impfslotID));

	}

	@NonNull
	public Impfslot updateImpfslot(@NonNull Impfslot akImpfslot, @NonNull Consumer<Impfslot> impfslotConsumer) {

		// Ueberpruefen, ob die Kapazitaet veraendert wurde
		final int kapazitaetVorherImpfung1 = akImpfslot.getKapazitaetErsteImpfung();
		final int kapazitaetVorherImpfung2 = akImpfslot.getKapazitaetZweiteImpfung();
		final int kapazitaetVorherImpfungN = akImpfslot.getKapazitaetBoosterImpfung();
		impfslotConsumer.accept(akImpfslot);
		// Falls notwendig, Termine hinzufuegen oder entfernen
		updateImpftermineAccordingToNewKapazitaeten(kapazitaetVorherImpfung1, kapazitaetVorherImpfung2, kapazitaetVorherImpfungN, akImpfslot);
		return impfslotRepo.update(akImpfslot);
	}

	@NonNull
	public Impfslot updateImpfslot(@NonNull Impfslot akImpfslot) {
		return impfslotRepo.update(akImpfslot);
	}

	private void updateImpftermineAccordingToNewKapazitaeten(
		int kapazitaetVorherImpfung1,
		int kapazitaetVorherImpfung2,
		int kapazitaetVorherImpfungN,
		@NonNull Impfslot impfslot
	) {
		if (kapazitaetVorherImpfung1 != impfslot.getKapazitaetErsteImpfung()) {
			if (kapazitaetVorherImpfung1 < impfslot.getKapazitaetErsteImpfung()) {
				// Es kommen neue Termine dazu
				addImpftermineForChangedKapazitaet(
					impfslot,
					Impffolge.ERSTE_IMPFUNG,
					impfslot.getKapazitaetErsteImpfung() - kapazitaetVorherImpfung1);
			} else {
				// Der schwierigere Fall: Es sind Termine weggefallen! Ueberpruefen, dass sie noch nicht gebucht waren
				removeImpftermineForChangedKapazitaet(
					impfslot,
					Impffolge.ERSTE_IMPFUNG,
					kapazitaetVorherImpfung1 - impfslot.getKapazitaetErsteImpfung()
				);
			}
		}
		if (kapazitaetVorherImpfung2 != impfslot.getKapazitaetZweiteImpfung()) {
			if (kapazitaetVorherImpfung2 < impfslot.getKapazitaetZweiteImpfung()) {
				// Es kommen neue Termine dazu
				addImpftermineForChangedKapazitaet(
					impfslot,
					Impffolge.ZWEITE_IMPFUNG,
					impfslot.getKapazitaetZweiteImpfung() - kapazitaetVorherImpfung2);
			} else {
				// Der schwierigere Fall: Es sind Termine weggefallen! Ueberpruefen, dass sie noch nicht gebucht waren
				removeImpftermineForChangedKapazitaet(
					impfslot,
					Impffolge.ZWEITE_IMPFUNG,
					kapazitaetVorherImpfung2 - impfslot.getKapazitaetZweiteImpfung()
				);
			}
		}
		if (kapazitaetVorherImpfungN != impfslot.getKapazitaetBoosterImpfung()) {
			if (kapazitaetVorherImpfungN < impfslot.getKapazitaetBoosterImpfung()) {
				// Es kommen neue Termine dazu
				addImpftermineForChangedKapazitaet(
					impfslot,
					Impffolge.BOOSTER_IMPFUNG,
					impfslot.getKapazitaetBoosterImpfung() - kapazitaetVorherImpfungN);
			} else {
				// Der schwierigere Fall: Es sind Termine weggefallen! Ueberpruefen, dass sie noch nicht gebucht waren
				removeImpftermineForChangedKapazitaet(
					impfslot,
					Impffolge.BOOSTER_IMPFUNG,
					kapazitaetVorherImpfungN - impfslot.getKapazitaetBoosterImpfung()
				);
			}
		}
	}

	private void addImpftermineForChangedKapazitaet(@NonNull Impfslot impfslot, Impffolge impffolge, int anzahl) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(
				"Kapazitaet wurde erhoeht fuer Slot {} im ODI {}. Neue freie Termine werden erstellt",
				DateUtil.formatDateTimeRange(impfslot.getZeitfenster()),
				impfslot.getOrtDerImpfung().getName());
		}
		IntStream.range(0, anzahl).mapToObj(i -> new Impftermin()).forEach(termin -> {
			termin.setImpfslot(impfslot);
			termin.setImpffolge(impffolge);
			impfterminRepo.create(termin);
		});
	}

	private void removeImpftermineForChangedKapazitaet(@NonNull Impfslot impfslot, Impffolge impffolge, int anzahl) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(
				"Kapazitaet wurde vermindert fuer Slot {} im ODI {}. Noch nicht freigegbene Termine werden "
					+ "geloescht",
				DateUtil.formatDateTimeRange(impfslot.getZeitfenster()),
				impfslot.getOrtDerImpfung().getName());
		}

		int anzRemoved = 0;
		StopWatch findStopwatch = StopWatch.createStarted();
		final List<Impftermin> impftermine = impfterminRepo.findImpftermine(impfslot, impffolge);
		LOG.debug("VACME-REDUKTION: findImpftermine fuer slot dauerte " + findStopwatch.getTime(TimeUnit.MILLISECONDS));
		for (int i = 0, impftermineSize = impftermine.size(); i < impftermineSize; i++) {
			Impftermin impftermin = impftermine.get(i);
			StopWatch stopwatchGetByImpftermin = StopWatch.createStarted();
			boolean empty;
			if (Impffolge.BOOSTER_IMPFUNG.equals(impftermin.getImpffolge())) {
				empty = impfdossierRepo.findIdOfImpfdossiereintragForImpftermin(impftermin).isEmpty();
			} else {
				// impffolge 1 und 2 abhandeln
				empty = impfdossierRepo.getImpfdossierForGrundimpftermin(impftermin).isEmpty();
			}
			long time = stopwatchGetByImpftermin.getTime(TimeUnit.MILLISECONDS);
			LOG.debug("VACME-REDUKTION: finding out if Termin was booked took " + time);

			if (empty) {
				// Termin kann geloescht werden
				StopWatch stopwatchDeleted = StopWatch.createStarted();
				impfterminRepo.delete(Impftermin.toId(impftermin.getId()));
				long deleteTime = stopwatchDeleted.getTime(TimeUnit.MILLISECONDS);
				LOG.debug("VACME-REDUKTION: Deleting took " + deleteTime);

				anzRemoved++;
				if (anzRemoved == anzahl) {
					LOG.debug("VACME-REDUKTION: reduktionsloop  dauerte " + findStopwatch.getTime(TimeUnit.MILLISECONDS));
					return;
				}
			}
		}
		// Wenn wir bis hier hin kommen, waren zu wenige Termine frei, die geloescht werden konnten!
		LOG.warn(
			"Es waren nicht genug freie Termine verfueghar umd die Kapazitaet fuer den Slot {} im ODI {} zu "
				+ "senken",
			DateUtil.formatDateTimeRange(impfslot.getZeitfenster()),
			impfslot.getOrtDerImpfung().getName());
		throw AppValidationMessage.IMPFTERMINE_KAPAZITAET.create(DateUtil.formatDateTimeRange(impfslot.getZeitfenster()));
	}

	@NonNull
	public List<Impfslot> find(@NonNull OrtDerImpfung ortDerImpfung) {
		return impfslotRepo.find(ortDerImpfung, KrankheitIdentifier.COVID);
	}

	@NonNull
	public List<Impfslot> findAllImpfslots(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis
	) {
		return ortDerImpfung.getKrankheiten()
			.stream()
			.flatMap(krankheitEntity -> impfslotRepo.find(ortDerImpfung,
				dateVon,
				dateBis,
				krankheitEntity.getKrankheitIdentifier()).stream())
			.collect(Collectors.toList());
	}

	@NonNull
	public List<Impfslot> find(@NonNull OrtDerImpfung ortDerImpfung, @NonNull LocalDate date) {
		return find(ortDerImpfung, date, date);
	}

	public List<Impfslot> find(
		@NonNull OrtDerImpfung ortDerImpfung, @NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis) {
		// TODO Affenpocken used in loadtests and Tagesstatistik
		return impfslotRepo.find(ortDerImpfung, dateVon, dateBis, KrankheitIdentifier.COVID);
	}

	@Nullable
	public Impfslot findImpfslotForTime(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDateTime desiredTime
	) {
		return impfslotRepo.find(ortDerImpfung, desiredTime, krankheitIdentifier);
	}

	public boolean hasAtLeastFreieImpftermine(@NonNull Integer minTermin, @NonNull KrankheitIdentifier krankheit) {
		return impfterminRepo.hasAtLeastFreieImpfslots(minTermin, krankheit);
	}

	@NonNull
	public List<ImpfslotValidationJax> validateImpfslotsByOdi(
		@NonNull UUID ortDerImpfungId,
		@NonNull LocalDate vonDate,
		@NonNull LocalDate bisDate
	) {
		final int distanceImpfungenDesired = this.settingsService.getSettings().getDistanceImpfungenDesired();
		return impfslotRepo.validateImpfslotsByOdi(ortDerImpfungId, vonDate, bisDate, distanceImpfungenDesired);
	}
}
