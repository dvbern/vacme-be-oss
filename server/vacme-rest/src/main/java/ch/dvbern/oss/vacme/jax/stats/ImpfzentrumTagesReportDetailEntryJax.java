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

package ch.dvbern.oss.vacme.jax.stats;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.jax.impfslot.DateTimeRangeJax;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Tagesstatistik Detail Entry:
 * Ein Entry entspricht einem Zeitfenster (Slot), also einer Zeile in der Detailstatistik.
 * Sie enthaelt die Zahlen zu geplanten Terminen und durchgefuehrten Impfungen pro Impfstoff und Total.
 */
@AllArgsConstructor
@Getter
@Setter
@Slf4j
public class ImpfzentrumTagesReportDetailEntryJax {

	@JsonIgnore
	private Map<Impffolge, Map<String, Long>> impfstoffFreq = new EnumMap<>(Impffolge.class); // fuer alle Impffolgen: sammle Anzahl pro Impfstoff

	@NonNull
	private UUID slotId;

	@NonNull
	private DateTimeRangeJax zeitfenster;

	private long totalGeplanteImpfungen1;
	@JsonIgnore
	private long totalAusstehendeT1Impfungen;

	private long totalGeplanteImpfungen2;
	@JsonIgnore
	private long totalAusstehendeT2Impfungen;

	private long totalGeplanteImpfungenN;
	@JsonIgnore
	private long totalAusstehendeTNImpfungen;

	private long geplanteImpfungen1UnbekannterImpfstoff;
	private long geplanteImpfungen2UnbekannterImpfstoff;
	private long geplanteImpfungenNUnbekannterImpfstoff;

	private boolean hasAusstehendeImpfungen;

	@NonNull
	private Set<ImpfzentrumTagesReportDetailEntryImpfstoffJax> planungPerImpfstoff;

	public ImpfzentrumTagesReportDetailEntryJax() {
		impfstoffFreq.put(Impffolge.ERSTE_IMPFUNG, new HashMap<>());
		impfstoffFreq.put(Impffolge.ZWEITE_IMPFUNG, new HashMap<>());
		impfstoffFreq.put(Impffolge.BOOSTER_IMPFUNG, new HashMap<>());
	}

	@JsonIgnore
	public Long getCount(Impffolge impffolge, Impfstoff impfstoff) {
		Map<String, Long> countMap = this.impfstoffFreq.get(impffolge);
		return countMap.get(impfstoff.getName()) == null ? 0 : countMap.get(impfstoff.getName());
	}

	@JsonIgnore
	public void incrementForImpfstoff(Impffolge impffolge, Impfstoff stoff) {
		Map<String, Long> countMap = this.impfstoffFreq.get(impffolge);

		String name = stoff.getName();
		Long count = countMap.get(name);
		if (count == null) {
			countMap.put(name, 1L);
		} else {
			countMap.put(name, count + 1L);
		}
	}

	@JsonIgnore
	public void incrementT1ForImpfstoff(Impfstoff stoff) {
		incrementForImpfstoff(Impffolge.ERSTE_IMPFUNG, stoff);
	}

	/**
	 * inkrementiert den counter fuer  geplanten 2. Impfungen wo wir aufgrund der ersten Impfung wissen womit geimpft wird
	 *
	 * @param stoff imfstoff fuer den inkrementiert wird
	 */
	@JsonIgnore
	public void incrementT2ForImpfstoff(Impfstoff stoff) {
		incrementForImpfstoff(Impffolge.ZWEITE_IMPFUNG, stoff);
	}

	@JsonIgnore
	public void incrementTNForImpfstoff(Impfstoff stoff) {
		incrementForImpfstoff(Impffolge.BOOSTER_IMPFUNG, stoff);
	}

	@JsonIgnore
	public void incrementT1ForUnbekannterImpfstoff() {
		geplanteImpfungen1UnbekannterImpfstoff++;

	}

	/**
	 * inkrementiert den counter fuer  geplanten 2. Impfungen wo wir noch nicht wissen welcher Impfstoff gebraucht werden wird
	 */
	@JsonIgnore
	public void incrementT2ForUnbekannterImpfstoff() {
		geplanteImpfungen2UnbekannterImpfstoff++;

	}

	/**
	 * inkrementiert den counter fuer  geplanten Booster Impfungen wo wir noch nicht wissen welcher Impfstoff gebraucht werden wird
	 */
	@JsonIgnore
	public void incrementTNForUnbekannterImpfstoff() {
		geplanteImpfungenNUnbekannterImpfstoff++;

	}

	@JsonIgnore
	public void incrementTotalGeplanteImpfungen2() {
		totalGeplanteImpfungen2++;
	}

	@JsonIgnore
	public void incrementTotalGeplanteImpfungenN() {
		totalGeplanteImpfungenN++;
	}

	@JsonIgnore
	public void incrementTotalGeplanteImpfungen1() {
		totalGeplanteImpfungen1++;
	}

	@JsonIgnore
	public void incrementAusstehendeT1Impfungen() {
		totalAusstehendeT1Impfungen++;
	}

	@JsonIgnore
	public void incrementAusstehendeT2Impfungen() {
		totalAusstehendeT2Impfungen++;
	}

	@JsonIgnore
	public void incrementAusstehendeTNImpfungen() {
		totalAusstehendeTNImpfungen++;
	}

	/**
	 * nachdem alle Slots gezaehlt und alle increments gemacht hat koennen wir noch die fuer den client benotigte Liste
	 * mit ImpfzentrumTagesReportDetailEntryImpfstoffJax aus unseren count-HashMaps erzeugen.
	 * Damit wir sicher alle Impfstoffe drin haben geben wir noch die Liste aller Impfstoffe mit
	 */
	@JsonIgnore
	public void finalizeCount(List<Impfstoff> impfstoffList) {

		planungPerImpfstoff = new LinkedHashSet<>();
		long recheckedGeplanteImpfungen2Count = 0;
		for (Impfstoff impfstoff : impfstoffList) {
			long countTermin1PerStoff = getCount(Impffolge.ERSTE_IMPFUNG, impfstoff);
			long countTermin2PerStoff = getCount(Impffolge.ZWEITE_IMPFUNG, impfstoff);
			long countTerminNPerStoff = getCount(Impffolge.BOOSTER_IMPFUNG, impfstoff);

			ImpfzentrumTagesReportDetailEntryImpfstoffJax impfstoffCountEntry
				= new ImpfzentrumTagesReportDetailEntryImpfstoffJax(impfstoff.getName(), countTermin1PerStoff, countTermin2PerStoff, countTerminNPerStoff);
			planungPerImpfstoff.add(impfstoffCountEntry);
			recheckedGeplanteImpfungen2Count += countTermin2PerStoff;

		}
		recheckedGeplanteImpfungen2Count += geplanteImpfungen2UnbekannterImpfstoff;

		// validierung
		if (totalGeplanteImpfungen2 != recheckedGeplanteImpfungen2Count) {
			String slotInErrorInfo = String.format("Slot: %s-%s %s", zeitfenster.getVon(), zeitfenster.getBisDisplay(),
				planungPerImpfstoff.stream().map(e -> e.getImpfstoffName() + "=" + e.getGeplanteOderDurchgefuehrteImpfungen2()).collect(Collectors.joining(",")));
			String format = String.format("Fehler: Die Summe totalGeplanteImpfungen2 %s muss der Summe aller geplanten "
					+ "Impfungen mit bekannten und unbekanntenImpfstoff ensprechen %s."
				, totalGeplanteImpfungen2, recheckedGeplanteImpfungen2Count);
			format = format + "\n" + slotInErrorInfo;
			LOG.warn("VACME-TAGESSTATISTIK: " + format);
			Validate.validState(totalGeplanteImpfungen2 == recheckedGeplanteImpfungen2Count, format);
		}

		// hasAusstehendeImpfungen
		this.hasAusstehendeImpfungen = totalAusstehendeT1Impfungen > 0
			|| totalAusstehendeT2Impfungen > 0
			|| totalAusstehendeTNImpfungen > 0;

	}
}
