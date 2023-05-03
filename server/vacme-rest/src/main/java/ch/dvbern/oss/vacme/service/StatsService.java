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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.statistik.StatistikKennzahl;
import ch.dvbern.oss.vacme.entities.statistik.StatistikKennzahlEintrag;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.jax.impfslot.DateTimeRangeJax;
import ch.dvbern.oss.vacme.jax.registration.ImpfstoffJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfstoffTagesReportJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumDayStatJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumDayStatTermin1DataRow;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumDayStatTermin2DataRow;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumDayStatTerminNDataRow;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumStatJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumTagesReportDetailAusstehendEntryJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumTagesReportDetailAusstehendJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumTagesReportDetailEntryJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumTagesReportDetailJax;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumTagesReportJax;
import ch.dvbern.oss.vacme.jax.StatsTerminAndImpfungJax;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.repo.StatistikKennzahlEintragRepo;
import ch.dvbern.oss.vacme.repo.StatsRepo;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Transactional
public class StatsService {

	private final StatsRepo statsRepo;
	private final StatistikKennzahlEintragRepo statistikKennzahlEintragRepo;
	private final RegistrierungRepo registrierungRepo;
	private final ImpfslotService impfslotService;

	@Inject
	public StatsService(
		@NonNull StatsRepo statsRepo,
		@NonNull StatistikKennzahlEintragRepo statistikKennzahlEintragRepo,
		@NonNull RegistrierungRepo registrierungRepo,
		@NonNull ImpfslotService impfslotService
	) {
		this.statsRepo = statsRepo;
		this.statistikKennzahlEintragRepo = statistikKennzahlEintragRepo;
		this.registrierungRepo = registrierungRepo;
		this.impfslotService = impfslotService;
	}

	public long getAnzahlRegistrierungen() {
		return registrierungRepo.getAnzahlRegistrierungen();
	}

	private long getAnzahlImpfung1Durchgefuehrt() {
		return statsRepo.getAnzahlImpfung1Durchgefuehrt();
	}

	private long getAnzahlImpfung2Durchgefuehrt() {
		return statsRepo.getAnzahlImpfung2Durchgefuehrt();
	}

	private long getAnzahlCovidImpfungNDurchgefueht() {
		return statsRepo.getAnzahlCovidImpfungNDurchgefuehrt();
	}

	private long getAnzahlRegistrierungenCallcenter() {
		return this.statsRepo.getAnzahlRegistrierungenCallcenter();
	}

	private long getAnzahlRegistrierungenCallcenterWithTermin() {
		return this.statsRepo.getAnzahlRegistrierungenCallcenterWithTermin();
	}


	@NonNull
	public ImpfzentrumStatJax getImpfzentrumStatistics(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate vonDate,
		@NonNull LocalDate bisDate
	) {
		final List<StatsTerminAndImpfungJax> entries = statsRepo.getImpfTermineAndImpfungenForOdi(ortDerImpfung, vonDate, bisDate);

		final ImpfzentrumStatJax impfzentrumStatJax = new ImpfzentrumStatJax();
		impfzentrumStatJax.setImpfzentrumCode(ortDerImpfung.getIdentifier());
		impfzentrumStatJax.setList(convertToDayStat(entries));
		return impfzentrumStatJax;
	}

	@NonNull
	private List<ImpfzentrumDayStatJax> convertToDayStat(
		@NonNull List<StatsTerminAndImpfungJax> impfterminList
	) {
		// Map als zwischenspeicher...
		Map<String, ImpfzentrumDayStatJax> map = new HashMap<>();
		Set<UUID> beruecksichtigteSlots = new HashSet<>();

		// moegliche Termine zusammenzaehlen
		for (StatsTerminAndImpfungJax entry : impfterminList) {

			final Impftermin termin = entry.getTermin();
			final Impfung impfung = entry.getImpfung();
			final Impfslot impfslot = termin.getImpfslot();

			final LocalDate datum = impfslot.getZeitfenster().getVon().toLocalDate();
			String mapKey = DateUtil.formatDate(datum, Locale.GERMAN);
			ImpfzentrumDayStatJax dayStats = map.get(mapKey);
			if (dayStats == null) {
				dayStats = new ImpfzentrumDayStatJax(datum);
				map.put(mapKey, dayStats);
			}

			// Kapazitaet darf nur einmal pro Slot beruecksichtigt werden, muss aber fuer den ganzen Tag zusammengezaehlt werden
			if (!beruecksichtigteSlots.contains(impfslot.getId())) {
				dayStats.increaseKapazitaetTermin1(impfslot.getKapazitaetErsteImpfung());
				dayStats.increaseKapazitaetTermin2(impfslot.getKapazitaetZweiteImpfung());
				dayStats.increaseKapazitaetTerminN(impfslot.getKapazitaetBoosterImpfung());
				beruecksichtigteSlots.add(impfslot.getId());
			}

			if (termin.isGebucht()) {
				if (Impffolge.ERSTE_IMPFUNG == termin.getImpffolge()) {
					dayStats.increaseNumberTermin1();
					if (impfung != null) {
						dayStats.increaseNumberImpfung1();
					}
				} else if(Impffolge.ZWEITE_IMPFUNG == termin.getImpffolge()) {
					dayStats.increaseNumberTermin2();
					if (impfung != null) {
						dayStats.increaseNumberImpfung2();
					}
				} else if(Impffolge.BOOSTER_IMPFUNG == termin.getImpffolge()) {
					dayStats.increaseNumberTerminN();
					if (impfung != null) {
						dayStats.increaseNumberImpfungN();
					}
				}
			}
		}

		// Liste aus Map extrahieren und sortieren
		List<ImpfzentrumDayStatJax> list = new ArrayList<>(map.values());
		list.sort(Comparator.comparing(ImpfzentrumDayStatJax::getDatum));

		return list;
	}

	public void takeKennzahlenSnapshot() {
		long anzahlImpfung1Durchgefueht = this.getAnzahlImpfung1Durchgefuehrt();
		long anzahlImpfung2Durchgefueht = this.getAnzahlImpfung2Durchgefuehrt();
		long anzahlCovidImpfungNDurchgefuehrt = this.getAnzahlCovidImpfungNDurchgefueht();
		long anzahlCallcenter = this.getAnzahlRegistrierungenCallcenter();
		long anzahlCallcenterWithTermin = this.getAnzahlRegistrierungenCallcenterWithTermin();

		this.statistikKennzahlEintragRepo.create(StatistikKennzahlEintrag.create(StatistikKennzahl.TOTAL_DURCHGEFUEHRTE_IMPFUNG1, anzahlImpfung1Durchgefueht));
		this.statistikKennzahlEintragRepo.create(StatistikKennzahlEintrag.create(StatistikKennzahl.TOTAL_DURCHGEFUEHRTE_IMPFUNG2, anzahlImpfung2Durchgefueht));
		this.statistikKennzahlEintragRepo.create(StatistikKennzahlEintrag.create(StatistikKennzahl.TOTAL_DURCHGEFUEHRTE_IMPFUNGN, anzahlCovidImpfungNDurchgefuehrt));
		this.statistikKennzahlEintragRepo.create(StatistikKennzahlEintrag.create(StatistikKennzahl.TOTAL_CALLCENTER_REGISTRIERUNGEN, anzahlCallcenter));
		this.statistikKennzahlEintragRepo.create(StatistikKennzahlEintrag.create(StatistikKennzahl.TOTAL_CALLCENTER_REGISTRIERUNGE_MIT_TERMIN, anzahlCallcenterWithTermin));
	}


	public ImpfzentrumTagesReportJax getOdiTagesReport(@NonNull OrtDerImpfung ortDerImpfung, @NonNull LocalDate datum) {
		List<ImpfstoffTagesReportJax> list = new ArrayList<>();

		// ***** Pro Impfstoff ein ImpfstoffTagesReportJax
		//todo Affenpocken: wir lesen hier zwar nur die Impfstoffe fuer deren Krankheit die Tagesstatistik unterstuetzt
		// im eigentlichen Query lesen wir aber immer covid
		List<Impfstoff> impfstoffList = statsRepo.getAllZugelasseneImpfstoffeThatSupportTagesstatistik();
		for (Impfstoff impfstoff : impfstoffList) {

			if (impfstoff.getKrankheiten()
				.stream()
				.anyMatch(krankheit -> krankheit.getIdentifier() != KrankheitIdentifier.COVID)) {
				throw AppValidationMessage.ILLEGAL_STATE.create("getOdiTagesReport unteststuetzt aktuell nur COVID");
			}

			ImpfstoffTagesReportJax jaxImpfstoff = new ImpfstoffTagesReportJax();

			// Impfstoff
			jaxImpfstoff.setImpfstoffName(impfstoff.getName());
			jaxImpfstoff.setImpfstoffDisplayName(impfstoff.getDisplayName());

			jaxImpfstoff.setDurchgefuehrtImpfung1(statsRepo.getDurchgefuerteImpfung1(impfstoff, ortDerImpfung, datum));
			jaxImpfstoff.setDurchgefuehrtImpfung2(statsRepo.getDurchgefuerteImpfung2(impfstoff, ortDerImpfung, datum));
			jaxImpfstoff.setDurchgefuehrtImpfungN(statsRepo.getDurchgefuerteImpfungN(impfstoff, ortDerImpfung, datum));

			jaxImpfstoff.setPendentTermin1(0); // bei offenen Erstterminen kennt man den Impfstoff nicht
			jaxImpfstoff.setPendentTermin2(statsRepo.getPendentImpfung2ImpfstoffEmpfohlen(
				KrankheitIdentifier.COVID, impfstoff, ortDerImpfung, datum));
			jaxImpfstoff.setPendentTerminN(statsRepo.getPendentImpfungNImpfstoffEmpfohlen(
				KrankheitIdentifier.COVID, impfstoff, ortDerImpfung, datum));

			list.add(jaxImpfstoff);
		}

		// ***** Impfstoff unbekannt (fuer pendente Termine)
		ImpfstoffTagesReportJax jaxImpfstoffUnbekannt = new ImpfstoffTagesReportJax();
		jaxImpfstoffUnbekannt.setDurchgefuehrtImpfung1(0);
		jaxImpfstoffUnbekannt.setDurchgefuehrtImpfung2(0);
		jaxImpfstoffUnbekannt.setDurchgefuehrtImpfungN(0);
		jaxImpfstoffUnbekannt.setPendentTermin1(statsRepo.getPendentImpfung1(
			KrankheitIdentifier.COVID, ortDerImpfung, datum));
		jaxImpfstoffUnbekannt.setPendentTermin2(statsRepo.getPendentImpfung2ImpfstoffUnbekannt(
			KrankheitIdentifier.COVID, ortDerImpfung, datum));
		jaxImpfstoffUnbekannt.setPendentTerminN(statsRepo.getPendentImpfungNImpfstoffUnbekannt(
			KrankheitIdentifier.COVID, ortDerImpfung, datum));
		list.add(jaxImpfstoffUnbekannt);

		// ***** Liste zusammenstellen
		ImpfzentrumTagesReportJax impfzentrumTagesReportJax = new ImpfzentrumTagesReportJax();
		impfzentrumTagesReportJax.setImpfstoffTagesReportJaxMap(list);
		return impfzentrumTagesReportJax;
	}

	@NonNull
	public ImpfzentrumTagesReportDetailJax getOdiTagesReportDetailFast(@NonNull OrtDerImpfung ortDerImpfung, @NonNull LocalDate datum) {

		List<Impfstoff> impfstoffList = statsRepo.getAllZugelasseneImpfstoffeThatSupportTagesstatistik();

		// Map mit allen Slots dieses Tages (Zeit -> SlotEntry)
		SortedMap<LocalDateTime, ImpfzentrumTagesReportDetailEntryJax> setOfSlots = new TreeMap<>();
		final List<Impfslot> impfslots = impfslotService.find(ortDerImpfung, datum);
		for (Impfslot impfslot : impfslots) {
			ImpfzentrumTagesReportDetailEntryJax slotEntry = new ImpfzentrumTagesReportDetailEntryJax();
			slotEntry.setSlotId(impfslot.getId());
			slotEntry.setZeitfenster(new DateTimeRangeJax(impfslot.getZeitfenster().getVon(), impfslot.getZeitfenster().getBis()));
			setOfSlots.put(slotEntry.getZeitfenster().getVon(), slotEntry);
		}

		// Suche alle Registrierungen, derer 1. Termin heute ist
		List<ImpfzentrumDayStatTermin1DataRow> tagesstatistikDatenTermin1 = statsRepo.getTagesstatistikDatenTermin1(ortDerImpfung, datum);

		// PRO TERMIN 1: SlotEntry hochzaehlen
		for (ImpfzentrumDayStatTermin1DataRow termin1Dto : tagesstatistikDatenTermin1) {

			// SlotEntry in der Map suchen
			LocalDateTime termin1Time = termin1Dto.getTermin1Datum();
			ImpfzentrumTagesReportDetailEntryJax slotEntry = setOfSlots.get(termin1Time);
			Objects.requireNonNull(slotEntry, "Slot Entry for Termin must exist");

			// SlotEntry hochzaehlen: bei Impfstoff oder unbekannt
			Impfstoff impfstoff1 = termin1Dto.getImpfung1Impfstoff();
			if (impfstoff1 != null && impfstoffList.contains(impfstoff1)) {
				// Impfung 1 durchgefuehrt, Impfstoff bekannt
				// -- Neu: Auch Impfung 1 kann man pro Impfstoff sammeln, nachdem sie durchgefuehrt wurde
				slotEntry.incrementT1ForImpfstoff(impfstoff1);
			} else {
				// Impfung 1 noch nicht durchgefuehrt -> Impfstoff unbekannt
				slotEntry.incrementT1ForUnbekannterImpfstoff();
			}

			// SlotEntry hochzaehlen: Total
			slotEntry.incrementTotalGeplanteImpfungen1();

			// SlotEntry hochzaehlen: Ausstehend (wenn Impfung noch nicht durchgefuehrt)
			if (termin1Dto.getImpfung1Datum() == null) {
				slotEntry.incrementAusstehendeT1Impfungen();
			}
		}


		// Suche alle Registrierungen derer 2. Termin heute ist und zaehle getrennt nach impfstoff
		List<ImpfzentrumDayStatTermin2DataRow> tagesstatistikDatenTermin2 =
			this.statsRepo.getTagesstatistikDatenTermin2(ortDerImpfung, datum);

		// PRO TERMIN 2: SlotEntry hochzaehlen
		for (ImpfzentrumDayStatTermin2DataRow termin2Dto : tagesstatistikDatenTermin2) {
			// SlotEntry in der Map suchen
			LocalDateTime termin2Time = termin2Dto.getTermin2Datum();
			ImpfzentrumTagesReportDetailEntryJax slotEntry = setOfSlots.get(termin2Time);
			Objects.requireNonNull(slotEntry, "Slot Entry for Termin must exist");

			// SlotEntry hochzaehlen: bei Impfstoff oder unbekannt
			Impfstoff impfung1Impfstoff = termin2Dto.getImpfung1Impfstoff();
			Impfstoff impfstoff2 = termin2Dto.getImpfung2Impfstoff();
			if (impfstoff2 != null && impfstoffList.contains(impfstoff2)) {
				// Impfung 2 durchgefuehrt, Impfstoff bekannt: VACME-1567
				slotEntry.incrementT2ForImpfstoff(impfstoff2);
			} else {
				// Impfstoff 2 noch nicht bekannt => Empfehlung basierend auf Impfstoff 1 machen
				if (impfung1Impfstoff == null || !impfstoffList.contains(impfung1Impfstoff)) {
					// Impfstoff 1 auch nicht bekannt -> unbekannt
					slotEntry.incrementT2ForUnbekannterImpfstoff();
				} else {
					// Impfstoff 1 bekannt -> wir nehmen an, dass Impfung 2 mit demselben Impfstoff passieren soll
					slotEntry.incrementT2ForImpfstoff(impfung1Impfstoff);
				}
			}
			// SlotEntry hochzaehlen: Total
			slotEntry.incrementTotalGeplanteImpfungen2();
			// SlotEntry hochzaehlen: Ausstehend (wenn Impfung noch nicht durchgefuehrt)
			if (termin2Dto.getImpfung2Datum() == null) {
				slotEntry.incrementAusstehendeT2Impfungen();
			}
		}

		// Suche alle Registrierungen, deren Boostertermin heute ist und zaehle getrennt nach impfstoff
		List<ImpfzentrumDayStatTerminNDataRow> tagesstatistikDatenTerminN =
			this.statsRepo.getTagesstatistikDatenTerminN(ortDerImpfung, datum);

		// PRO TERMIN N: SlotEntry hochzaehlen
		// -- Neu vacme-1545, vorher wurde Booster nicht gezaehlt
		for (ImpfzentrumDayStatTerminNDataRow terminNDto : tagesstatistikDatenTerminN) {
			// SlotEntry in der Map suchen
			LocalDateTime terminNTime = terminNDto.getTerminNDatum();
			ImpfzentrumTagesReportDetailEntryJax slotEntry = setOfSlots.get(terminNTime);
			Objects.requireNonNull(slotEntry, "Slot Entry for Termin must exist");

			// SlotEntry hochzaehlen: bei Impfstoff oder unbekannt
			Impfstoff impfstoffN = terminNDto.getImpfungNImpfstoff();
			if (impfstoffN != null && impfstoffList.contains(impfstoffN)) {
				// Booster schon durchgefuehrt -> Impfstoff klar
				slotEntry.incrementTNForImpfstoff(terminNDto.getImpfungNImpfstoff());
			} else {
				// Booster noch nicht durchgefuehrt -> Impfstoff unbekannt (TODO Booster: empfehlen basierend auf Impfschutz)
				slotEntry.incrementTNForUnbekannterImpfstoff();
			}

			// SlotEntry hochzaehlen: Total
			slotEntry.incrementTotalGeplanteImpfungenN();
			// SlotEntry hochzaehlen: Ausstehend (wenn Impfung noch nicht durchgefuehrt)
			if (terminNDto.getImpfungNDatum() == null) {
				slotEntry.incrementAusstehendeTNImpfungen();
			}
		}

		// Pro Slot: alles fertig zaehlen
		setOfSlots.values().forEach(slotEntry -> slotEntry.finalizeCount(impfstoffList));

		// Report zusammenbauen
		ImpfzentrumTagesReportDetailJax report = new ImpfzentrumTagesReportDetailJax();
		report.setStichtag(DateUtil.formatDate(datum, Locale.GERMAN));
		report.setOdiName(ortDerImpfung.getName());
		report.setImpfstoffTagesReportJaxMap(setOfSlots.values());
		return report;
	}

	@NonNull
	public ImpfzentrumTagesReportDetailAusstehendJax getOdiTagesReportDetailAusstehendeCodes(@NonNull Impfslot slot) {
		final List<Registrierung> registrierungList = registrierungRepo.getPendenteByImpfslot(slot);
		List<ImpfzentrumTagesReportDetailAusstehendEntryJax> entries = new ArrayList<>();
		for (Registrierung registrierung : registrierungList) {
			entries.add(ImpfzentrumTagesReportDetailAusstehendEntryJax.from(registrierung));
		}
		ImpfzentrumTagesReportDetailAusstehendJax report = new ImpfzentrumTagesReportDetailAusstehendJax();
		report.setEntryList(entries);
		return report;
	}

	public List<ImpfstoffJax> getAllZugelasseneImpfstoffeThatSupportTagesstatistik() {
		List<ImpfstoffJax> impfstoffe = this.statsRepo.getAllZugelasseneImpfstoffeThatSupportTagesstatistik()
			.stream()
			.map(ImpfstoffJax::from)
			.collect(Collectors.toList());
		return impfstoffe;
	}
}
