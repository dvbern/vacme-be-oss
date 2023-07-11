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

package ch.dvbern.oss.vacme.repo;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpfslot;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.impfslot.ImpfslotValidationJax;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtil.DEFAULT_DATE_FORMAT;

@Slf4j
@RequestScoped
@Transactional
public class ImpfslotRepo {

	private final Db db;

	@Inject
	public ImpfslotRepo(Db db) {
		this.db = db;
	}

	public void create(Impfslot impfslot) {

		db.persist(impfslot);
	}

	@NonNull
	public Optional<Impfslot> getById(@NonNull ID<Impfslot> id) {
		return db.get(id);
	}

	@NonNull
	public Impfslot update(@NonNull Impfslot impfslot) {
		return db.merge(impfslot);
	}

	@NonNull
	public List<Impfslot> find(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		LOG.debug(String.format("Alle Impfslot fuer OrtDerImpfung ID:(%s)", ortDerImpfung.getId().toString()));
		var result = db.selectFrom(QImpfslot.impfslot)
			.where(QImpfslot.impfslot.ortDerImpfung.eq(ortDerImpfung)
				.and(QImpfslot.impfslot.krankheitIdentifier.eq(krankheitIdentifier)))
			.fetch();
		LOG.debug(String.format("Anz. Impfslot gefunden:(%d)", result.size()));
		return result;
	}

	@NonNull
	public List<Impfslot> find(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate von,
		@NonNull LocalDate bis,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		LOG.debug(String.format(
			"Alle Impfslot fuer OrtDerImpfung ID:(%s), Zwischen Datum (%2$td-%2$tm-%2$tY - %3$td-%3$tm-%3$tY)",
			ortDerImpfung.getId().toString(),
			von,
			bis));
		var result = db.selectFrom(QImpfslot.impfslot)
			.where(QImpfslot.impfslot.ortDerImpfung.eq(ortDerImpfung)
				.and(QImpfslot.impfslot.zeitfenster.bis.between(von.atTime(LocalTime.MIN), bis.atTime(LocalTime.MAX)))
				.and(QImpfslot.impfslot.krankheitIdentifier.eq(krankheitIdentifier))
			)
			.fetch();
		LOG.debug(String.format("Anz. Impfslot in Zeitraum gefunden:(%d)", result.size()));
		return result;
	}

	@Nullable
	public Impfslot find(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDateTime stichzeit,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		LOG.debug(String.format("Alle Impfslot fuer OrtDerImpfung ID:(%s), zum Zeitpunkt (%2$td-%2$tm-%2$tY %2tT)",
			ortDerImpfung.getId().toString(), stichzeit));
		return db.selectFrom(QImpfslot.impfslot)
			.where(QImpfslot.impfslot.ortDerImpfung.eq(ortDerImpfung)
				.and(QImpfslot.impfslot.zeitfenster.von.loe(stichzeit))
				.and(QImpfslot.impfslot.zeitfenster.bis.gt(stichzeit))
				.and(QImpfslot.impfslot.krankheitIdentifier.eq(krankheitIdentifier))
			)
			.fetchFirst();
	}

	/**
	 * Service der ein Query macht welches die Kapazitaeten eines Slots und seines gegenstuecks 28 Tage spaeter zurueckgibt
	 */
	@NonNull
	public List<ImpfslotValidationJax> validateImpfslotsByOdi(
		UUID ortDerImpfungId,
		LocalDate vonDate,
		LocalDate bisDate,
		int desiredDaysBetweenImpfungen
	) {
		var queryErstslotsMitFehlenderZweitkapazitaet = ""
			+ "SELECT kap1, kap2, datum1, datum2 "
			+ "FROM "
			+ "		(SELECT ortDerImpfung_id, DATE(von) datum1, SUM(kapazitaetErsteImpfung) kap1 " // Erstkapazitaeten pro Tag
			+ "		FROM Impfslot "
			+ "  	GROUP BY ortDerImpfung_id, DATE(von)) i1 "
			+ "LEFT JOIN "
			+ "		(SELECT ortDerImpfung_id, DATE(von) datum2, SUM(kapazitaetZweiteImpfung) kap2 "  // Zweitkapazitaeten pro Tag
			+ "		FROM Impfslot "
			+ "  	GROUP BY ortDerImpfung_id, DATE(von)) i2 "
			+ "ON DATE_ADD(datum1, INTERVAL ?4 DAY) = datum2 " // nur Tage vergleichen, die genau 28 Tage auseinanderliegen
			+ "    AND i2.ortDerImpfung_id = i1.ortDerImpfung_id " // nur in diesem ODI
			+ "WHERE " // Falls: Erstkapazitaet > 0 und > Zweitkapazitaet und Erstdatum innerhalb Datumsrange und ODI stimmt
			+ "		kap1 > 0 "
			+ "		AND kap1 > kap2 "
			+ "		AND datum1 >= ?1 "
			+ "		AND datum1 <= ?2 "
			+ " 	AND i1.ortDerImpfung_id = ?3 "
			+ "ORDER BY datum1;";

		var queryErstslotsOhneZweitslot = ""
			+ "SELECT kap1, 0.0, datum1, DATE_ADD(datum1, INTERVAL ?4 DAY) "
			+ "FROM "
			+ "		(SELECT ortDerImpfung_id, DATE(von) datum1, SUM(kapazitaetErsteImpfung) kap1 " // Erstkapazitaeten pro Tag
			+ "		FROM Impfslot "
			+ "  	GROUP BY ortDerImpfung_id, DATE(von)) i1 "
			+ "WHERE kap1 > 0 " // Falls: Erstkapazitaet > 0 und Erstdatum innerhalb Datumsrange und ODI stimmt
			+ "AND datum1 >= ?1 "
			+ "AND datum1 <= ?2 "
			+ "AND i1.ortDerImpfung_id = ?3 "
			+ "AND NOT EXISTS" // wo 1 Monat spaeter gar kein Impslot existiert
			+ "		(SELECT * "
			+ "		FROM Impfslot i2"
			+ "		WHERE "
			+ "			i2.ortDerImpfung_id = i1.ortDerImpfung_id"  // nur in diesem ODI
			+ "			AND DATE_ADD(datum1, INTERVAL ?4 DAY) = DATE(i2.von)" // nur Tage vergleichen, die genau 28 Tage auseinanderliegen
			+ "		) "
			+ "ORDER BY datum1;";

		final List<ImpfslotValidationJax> erstslotsOhneZweitslot = getJaxList(queryErstslotsOhneZweitslot, ortDerImpfungId, vonDate, bisDate,
			desiredDaysBetweenImpfungen);
		final List<ImpfslotValidationJax> erstslotsFehlendeZweitkapazitaet = getJaxList(queryErstslotsMitFehlenderZweitkapazitaet, ortDerImpfungId, vonDate,
			bisDate, desiredDaysBetweenImpfungen);

		return Stream.concat(erstslotsOhneZweitslot.stream(), erstslotsFehlendeZweitkapazitaet.stream()).collect(Collectors.toList());
	}

	private List<ImpfslotValidationJax> getJaxList(String impfslotQuery,
		UUID ortDerImpfungId,
		LocalDate vonDate,
		LocalDate bisDate,
		int desiredDaysBetweenImpfungen) {
		final TypedQuery<Object[]> nativeQuery = (TypedQuery<Object[]>) db.getEntityManager().createNativeQuery(impfslotQuery);

		nativeQuery.setParameter(1, vonDate);
		nativeQuery.setParameter(2, bisDate);
		nativeQuery.setParameter(3, ortDerImpfungId.toString());
		nativeQuery.setParameter(4, desiredDaysBetweenImpfungen);

		List<Object[]> results = nativeQuery.getResultList();
		var jaxList = new ArrayList<ImpfslotValidationJax>();
		for (Object[] r : results) {
			var jax = new ImpfslotValidationJax(
				DEFAULT_DATE_FORMAT.apply(Locale.getDefault()).format(((Date) r[2]).toLocalDate()),
				((Date) r[2]).toLocalDate(),
				DEFAULT_DATE_FORMAT.apply(Locale.getDefault()).format(((Date) r[3]).toLocalDate()),
				((Date) r[3]).toLocalDate(),
				((BigDecimal) r[0]).intValue(),
				((BigDecimal) r[1]).intValue());
			jaxList.add(jax);
		}

		return jaxList;
	}

}
