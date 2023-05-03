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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.QImpfschutz;
import ch.dvbern.oss.vacme.entities.impfen.QImpfstoff;
import ch.dvbern.oss.vacme.entities.impfen.QImpfung;
import ch.dvbern.oss.vacme.entities.impfen.QKrankheit;
import ch.dvbern.oss.vacme.entities.registration.QRegistrierung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.QOrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumDayStatTermin1DataRow;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumDayStatTermin2DataRow;
import ch.dvbern.oss.vacme.jax.stats.ImpfzentrumDayStatTerminNDataRow;
import ch.dvbern.oss.vacme.jax.QStatsTerminAndImpfungJax;
import ch.dvbern.oss.vacme.jax.StatsTerminAndImpfungJax;
import ch.dvbern.oss.vacme.jax.stats.QImpfzentrumDayStatTermin1DataRow;
import ch.dvbern.oss.vacme.jax.stats.QImpfzentrumDayStatTermin2DataRow;
import ch.dvbern.oss.vacme.jax.stats.QImpfzentrumDayStatTerminNDataRow;
import ch.dvbern.oss.vacme.smartdb.Db;
import ch.dvbern.oss.vacme.smartdb.SmartJPAQuery;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.entities.registration.QRegistrierung.registrierung;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.GEBUCHT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.NEU;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ODI_GEWAEHLT;

@RequestScoped
@Transactional
public class StatsRepo {

	private final Db db;

	@Inject
	public StatsRepo(Db db) {
		this.db = db;
	}

	@NonNull
	public List<StatsTerminAndImpfungJax> getImpfTermineAndImpfungenForOdi(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis
	) {
		return db.selectFrom(QImpftermin.impftermin)
			.innerJoin(QImpftermin.impftermin.impfslot)
			.innerJoin(QImpftermin.impftermin.impfslot.ortDerImpfung)
			.leftJoin(QImpfung.impfung).on(QImpfung.impfung.termin.eq(QImpftermin.impftermin))
			.select(new QStatsTerminAndImpfungJax(
				QImpftermin.impftermin,
				QImpfung.impfung)
			)
			.where(QImpftermin.impftermin.impfslot.ortDerImpfung.eq(ortDerImpfung)
				.and(QImpftermin.impftermin.impfslot.zeitfenster.bis.between(dateVon.atTime(LocalTime.MIN), dateBis.atTime(LocalTime.MAX)))
			)
			.fetch();
	}

	public long getAnzahlImpfung1Durchgefuehrt() {
		QImpftermin termin = new QImpftermin("termin");
		SmartJPAQuery<Impfdossier> query = db
			.selectFrom(QImpfdossier.impfdossier)
			.distinct()
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin1, termin)
			.innerJoin(QImpfung.impfung).on(QImpfung.impfung.termin.id.eq(termin.id))
			.where(QImpfung.impfung.timestampImpfung.isNotNull());
		return query.fetchCount();
	}

	public long getAnzahlImpfung2Durchgefuehrt() {
		QImpftermin termin = new QImpftermin("termin");
		SmartJPAQuery<Impfdossier> query = db
			.selectFrom(QImpfdossier.impfdossier)
			.distinct()
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin2, termin)
			.innerJoin(QImpfung.impfung).on(QImpfung.impfung.termin.id.eq(termin.id))
			.where(QImpfung.impfung.timestampImpfung.isNotNull());
		return query.fetchCount();
	}

	public long getAnzahlCovidImpfungNDurchgefuehrt() {
		return db.select(QImpfung.impfung)
			.from(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpfung.impfung.termin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpftermin.impftermin.eq(QImpfdossiereintrag.impfdossiereintrag.impftermin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossiereintrag.impfdossiereintrag.impfdossier.eq(QImpfdossier.impfdossier))
			.where(QImpfung.impfung.timestampImpfung.isNotNull()
				.and(QImpftermin.impftermin.impffolge.eq(Impffolge.BOOSTER_IMPFUNG))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.fetchCount();
	}

	public long getAnzahlRegistrierungenCallcenter() {

		SmartJPAQuery<Registrierung> query = db.selectFrom(QRegistrierung.registrierung)
			.where(QRegistrierung.registrierung.registrierungsEingang.eq(RegistrierungsEingang.CALLCENTER_REGISTRATION));
		return query.fetchCount();
	}

	public long getAnzahlRegistrierungenCallcenterWithTermin() {
		SmartJPAQuery<Impfdossier> query = db
			.selectFrom(QImpfdossier.impfdossier)
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.where(registrierung.registrierungsEingang.eq(RegistrierungsEingang.CALLCENTER_REGISTRATION)
				.and(QImpfdossier.impfdossier.buchung.impftermin1.isNotNull().or(QImpfdossier.impfdossier.buchung.impftermin2.isNotNull())));
		return query.fetchCount();
	}

	public List<Impfstoff> getAllZugelasseneImpfstoffeThatSupportTagesstatistik() {
		return db.select(QImpfstoff.impfstoff)
			.from(QImpfstoff.impfstoff)
			.innerJoin(QImpfstoff.impfstoff.krankheiten, QKrankheit.krankheit)
			.where(QImpfstoff.impfstoff.zulassungsStatus.in(ZulassungsStatus.EMPFOHLEN, ZulassungsStatus.ZUGELASSEN)
				.and(QKrankheit.krankheit.identifier.in(KrankheitIdentifier.getTagesStatistikSupportedKrankheiten())))
			.fetch();
	}

	/**
	 * Bei Impfung 1 und N ist der Impfstoff immer erst nach der Durchfuehrung bekannt
	 */
	public long getPendentImpfung1(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate datum
	) {
		QImpftermin termin = new QImpftermin("termin");
		SmartJPAQuery<Impfdossier> query = db.selectFrom(QImpfdossier.impfdossier)
			.distinct()
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin1, termin)
			.innerJoin(QImpfslot.impfslot).on(QImpfslot.impfslot.id.eq(termin.impfslot.id))
			.innerJoin(QOrtDerImpfung.ortDerImpfung).on(QOrtDerImpfung.ortDerImpfung.id.eq(termin.impfslot.ortDerImpfung.id))
			.where(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier)
				.and(termin.impfslot.ortDerImpfung.id.eq(ortDerImpfung.getId())
				.and(termin.impfslot.zeitfenster.bis.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX)))
				// Der Impfstoff ist solange unbekannt, wie die Impfung nicht durchgefuehrt wurde
				.and(QImpfdossier.impfdossier.dossierStatus.in(NEU, FREIGEGEBEN, ODI_GEWAEHLT, GEBUCHT, IMPFUNG_1_KONTROLLIERT))));
		return query.fetchCount();
	}

	/**
	 * Bei Impfung 2 gilt:
	 * -- Vor der ersten Impfung: Impfstoff unbekannt
	 * -- Nach der ersten Impfung aber vor der zweiten Impfung: Geplant ist Impfstoff der Impfung 1
	 * -- Nach der zweiten Impfung; "Geplant" ist jetzt der effektive Impfstoff der Impfung 2
	 */
	public long getPendentImpfung2ImpfstoffEmpfohlen(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull Impfstoff impfstoff,
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate datum
	) {
		QImpftermin termin1 = new QImpftermin("termin1");
		QImpftermin termin2 = new QImpftermin("termin2");
		SmartJPAQuery<Impfdossier> query = db.selectFrom(QImpfdossier.impfdossier)
			.distinct()
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin1, termin1)
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin2, termin2)
			.innerJoin(QImpfslot.impfslot).on(QImpfslot.impfslot.id.eq(termin2.impfslot.id))
			.innerJoin(QOrtDerImpfung.ortDerImpfung).on(QOrtDerImpfung.ortDerImpfung.id.eq(termin2.impfslot.ortDerImpfung.id))
			.innerJoin(QImpfung.impfung).on(QImpfung.impfung.termin.id.eq(termin1.id))
			.innerJoin(QImpfstoff.impfstoff).on(QImpfstoff.impfstoff.id.eq(QImpfung.impfung.impfstoff.id))
			.where(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier)
				.and(termin2.impfslot.ortDerImpfung.id.eq(ortDerImpfung.getId())
				.and(termin2.impfslot.zeitfenster.bis.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX)))
				.and(QImpfstoff.impfstoff.id.eq(impfstoff.getId()))
				.and(QImpfdossier.impfdossier.dossierStatus.in(
					NEU, FREIGEGEBEN, ODI_GEWAEHLT, GEBUCHT,
					IMPFUNG_1_KONTROLLIERT, IMPFUNG_1_DURCHGEFUEHRT, IMPFUNG_2_KONTROLLIERT))));
		return query.fetchCount();
	}

	public long getPendentImpfung2ImpfstoffUnbekannt(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate datum
	) {
		QImpftermin termin1 = new QImpftermin("termin1");
		QImpftermin termin2 = new QImpftermin("termin2");
		SmartJPAQuery<Impfdossier> query = db.selectFrom(QImpfdossier.impfdossier)
			.distinct()
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin1, termin1)
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin2, termin2)
			.innerJoin(QImpfslot.impfslot).on(QImpfslot.impfslot.id.eq(termin2.impfslot.id))
			.innerJoin(QOrtDerImpfung.ortDerImpfung).on(QOrtDerImpfung.ortDerImpfung.id.eq(termin2.impfslot.ortDerImpfung.id))
			.where(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier)
				.and(termin2.impfslot.ortDerImpfung.id.eq(ortDerImpfung.getId())
				.and(termin2.impfslot.zeitfenster.bis.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX)))
				// Unbekannter Impfstoff ist solange, bis die Impfung 1 durchgefuehrt ist, ab dann wissen wir welchen Impfstoff
				.and(QImpfdossier.impfdossier.dossierStatus.in(NEU, FREIGEGEBEN, ODI_GEWAEHLT, GEBUCHT, IMPFUNG_1_KONTROLLIERT))));
		return query.fetchCount();
	}

	// Alle pendenten Booster, bei denen genau dieser Impfstoff empfohlen wird (laut Impfschutz)
	public long getPendentImpfungNImpfstoffEmpfohlen(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull Impfstoff impfstoff,
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate datum
	) {
		BooleanExpression impfstoffFilter = QImpfschutz.impfschutz.isNotNull()
			.and(QImpfschutz.impfschutz.erlaubteImpfstoffe.eq(impfstoff.getId().toString()));
		return getPendentImpfungN(krankheitIdentifier, ortDerImpfung, datum, impfstoffFilter);
	}

	// Alle pendenten Booster, bei denen keiner oder mehrere Impfstoffe empfohlen sind (laut Impfschutz)
	public long getPendentImpfungNImpfstoffUnbekannt(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate datum) {
		// "Geplante" Impfungen sind das Total der durchgefuehrten und noch geplanten. Bei bereits durchgefuehrten ist der Impfstoff bekannt!
		// D.h. hier kommen nur die pendenten Booster-Impfungen:
		// und Impfschutz fehlt oder hat keine oder mehrere Impfstoff-Empfehlungen
		BooleanExpression impfstoffFilter = QImpfschutz.impfschutz.isNull()
			.or(QImpfschutz.impfschutz.erlaubteImpfstoffe.isEmpty())
			.or(QImpfschutz.impfschutz.erlaubteImpfstoffe.contains(Impfschutz.DELIMITER));
		return getPendentImpfungN(krankheitIdentifier, ortDerImpfung, datum, impfstoffFilter);
	}

	public long getPendentImpfungN(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate datum,
		@NonNull BooleanExpression impfstoffFilter
	) {
		// "Geplante" Impfungen sind das Total der durchgefuehrten und noch geplanten. Bei bereits durchgefuehrten ist der Impfstoff bekannt!
		// D.h. hier kommen nur die pendenten Booster-Impfungen:
		QImpftermin termin = new QImpftermin("termin");
		SmartJPAQuery<Registrierung> query = db
			.selectFrom(QRegistrierung.registrierung)
			.distinct()
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpfdossiereintrag.impfdossiereintrag.impfdossier.eq(QImpfdossier.impfdossier))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag.impftermin, termin)
			.innerJoin(QImpfslot.impfslot).on(QImpfslot.impfslot.id.eq(termin.impfslot.id))
			.leftJoin(QImpfung.impfung).on(QImpfung.impfung.termin.id.eq(termin.id))
			.leftJoin(QImpfschutz.impfschutz).on(QImpfschutz.impfschutz.eq(QImpfdossier.impfdossier.impfschutz))
			.where(termin.impfslot.ortDerImpfung.id.eq(ortDerImpfung.getId())
				// noch pendent
				.and(QImpfung.impfung.isNull())

				// Impfstoff filtern
				.and(impfstoffFilter)

				.and(QImpfslot.impfslot.zeitfenster.von.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX)))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier)));
		return query.fetchCount();
	}

	public long getDurchgefuerteImpfung1(Impfstoff impfstoff, OrtDerImpfung ortDerImpfung, LocalDate datum) {
		QImpftermin termin = new QImpftermin("termin");
		SmartJPAQuery<Impfdossier> query = db
			.selectFrom(QImpfdossier.impfdossier)
			.distinct()
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin1, termin)
			.innerJoin(QImpfslot.impfslot).on(QImpfslot.impfslot.id.eq(termin.impfslot.id))
			.innerJoin(QOrtDerImpfung.ortDerImpfung).on(QOrtDerImpfung.ortDerImpfung.id.eq(termin.impfslot.ortDerImpfung.id))
			.innerJoin(QImpfung.impfung).on(QImpfung.impfung.termin.id.eq(termin.id))
			.innerJoin(QImpfstoff.impfstoff).on(QImpfstoff.impfstoff.id.eq(QImpfung.impfung.impfstoff.id))
			.where(termin.impfslot.ortDerImpfung.id.eq(ortDerImpfung.getId())
				.and(QImpfstoff.impfstoff.id.eq(impfstoff.getId()))
				.and(QImpfung.impfung.timestampImpfung.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX))));
		return query.fetchCount();
	}

	public long getDurchgefuerteImpfung2(Impfstoff impfstoff, OrtDerImpfung ortDerImpfung, LocalDate datum) {
		QImpftermin termin = new QImpftermin("termin");
		SmartJPAQuery<Impfdossier> query = db.selectFrom(QImpfdossier.impfdossier)
			.distinct()
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin2, termin)
			.innerJoin(QImpfslot.impfslot).on(QImpfslot.impfslot.id.eq(termin.impfslot.id))
			.innerJoin(QOrtDerImpfung.ortDerImpfung).on(QOrtDerImpfung.ortDerImpfung.id.eq(termin.impfslot.ortDerImpfung.id))
			.innerJoin(QImpfung.impfung).on(QImpfung.impfung.termin.id.eq(termin.id))
			.innerJoin(QImpfstoff.impfstoff).on(QImpfstoff.impfstoff.id.eq(QImpfung.impfung.impfstoff.id))
			.where(termin.impfslot.ortDerImpfung.id.eq(ortDerImpfung.getId())
				.and(QImpfstoff.impfstoff.id.eq(impfstoff.getId()))
				.and(QImpfung.impfung.timestampImpfung.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX))));
		return query.fetchCount();
	}

	public long getDurchgefuerteImpfungN(
		@NonNull Impfstoff impfstoff,
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull LocalDate datum
	) {
		QImpftermin termin = new QImpftermin("termin");
		SmartJPAQuery<Registrierung> query = db.selectFrom(QRegistrierung.registrierung)
			.distinct()
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpfdossiereintrag.impfdossiereintrag.impfdossier.eq(QImpfdossier.impfdossier))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag.impftermin, termin)
			.innerJoin(QImpfslot.impfslot).on(QImpfslot.impfslot.id.eq(termin.impfslot.id))
			.innerJoin(QImpfung.impfung).on(QImpfung.impfung.termin.id.eq(termin.id))
			.innerJoin(QImpfstoff.impfstoff).on(QImpfstoff.impfstoff.id.eq(QImpfung.impfung.impfstoff.id))
			.where(termin.impfslot.ortDerImpfung.id.eq(ortDerImpfung.getId())
				.and(QImpfstoff.impfstoff.id.eq(impfstoff.getId()))
				.and(QImpfung.impfung.timestampImpfung.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX)))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)));
		return query.fetchCount();
	}

	/**
	 * Gibt die 1.Termine am relevanten OdI von einem bestimmten Datum zurueck zusammen mit dem Impfstoff der bei
	 * der ersten Impfung eingesetzt wurde
	 */
	public List<ImpfzentrumDayStatTermin1DataRow> getTagesstatistikDatenTermin1(OrtDerImpfung ortDerImpfung, LocalDate datum) {

		QImpftermin aliasTermin1 = new QImpftermin("termin1");
		QImpfslot aliasSlot1 = new QImpfslot("slot1");
		QImpfung aliasImpfung1 = new QImpfung("impfung1");

		List<ImpfzentrumDayStatTermin1DataRow> termin1AtOdi = db
			.select(new QImpfzentrumDayStatTermin1DataRow(
				QImpfdossier.impfdossier,
				aliasTermin1,
				aliasSlot1,
				aliasImpfung1
			))
			.from(QImpfdossier.impfdossier)
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin1, aliasTermin1)
			.innerJoin(aliasTermin1.impfslot, aliasSlot1)
			.leftJoin(aliasImpfung1).on(aliasImpfung1.termin.eq(aliasTermin1))
			.where(aliasTermin1.impfslot.zeitfenster.von
				.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX))
				.and(aliasSlot1.ortDerImpfung.id.eq(ortDerImpfung.getId()))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.fetch();
		return termin1AtOdi;
	}

	/**
	 * Gibt die 2.Termine am relevanten OdI von einem bestimmten Datum zurueck zusammen mit dem Impfstoff der bei
	 * der ersten Impfung eingesetzt wurde
	 *
	 * @return transformiertes
	 */
	public List<ImpfzentrumDayStatTermin2DataRow> getTagesstatistikDatenTermin2(OrtDerImpfung ortDerImpfung, LocalDate datum) {

		QImpftermin aliasTermin1 = new QImpftermin("termin1");
		QImpftermin aliasTermin2 = new QImpftermin("termin2");
		QImpfslot aliasSlot1 = new QImpfslot("slot1");
		QImpfslot aliasSlot2 = new QImpfslot("slot2");
		QImpfung aliasImpfung1 = new QImpfung("impfung1");
		QImpfung aliasImpfung2 = new QImpfung("impfung2");

		List<ImpfzentrumDayStatTermin2DataRow> termine2AtOdI = db
			.select(new QImpfzentrumDayStatTermin2DataRow(
				QImpfdossier.impfdossier,
				aliasTermin1,
				aliasTermin2,
				aliasSlot2,
				aliasImpfung1,
				aliasImpfung2))
			.from(QImpfdossier.impfdossier)
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin1, aliasTermin1)
			.innerJoin(aliasTermin1.impfslot, aliasSlot1)
			.leftJoin(aliasImpfung1).on(aliasImpfung1.termin.eq(aliasTermin1))
			.innerJoin(QImpfdossier.impfdossier.buchung.impftermin2, aliasTermin2)
			.innerJoin(aliasTermin2.impfslot, aliasSlot2)
			.leftJoin(aliasImpfung2).on(aliasImpfung2.termin.eq(aliasTermin2))
			.where(aliasTermin2.impfslot.zeitfenster.von.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX))
				.and(aliasSlot2.ortDerImpfung.id.eq(ortDerImpfung.getId()))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.fetch();

		return termine2AtOdI;
	}

	/**
	 * Gibt die N.Termine am relevanten OdI von einem bestimmten Datum zurueck.
	 * Abgekupfert von getTagesstatistikDatenTermin2 und getDurchgefuerteImpfungN.
	 *
	 * @return transformiertes
	 */
	public List<ImpfzentrumDayStatTerminNDataRow> getTagesstatistikDatenTerminN(OrtDerImpfung ortDerImpfung, LocalDate datum) {

		QImpftermin termin = new QImpftermin("termin");
		QImpfslot impfslot = new QImpfslot("impfslot");
		QImpfung impfung = new QImpfung("impfung");

		List<ImpfzentrumDayStatTerminNDataRow> termineNAtOdI = db
			.select(new QImpfzentrumDayStatTerminNDataRow(
				QImpfdossier.impfdossier,
				termin,
				impfslot,
				impfung))
			.from(QImpfdossier.impfdossier)
			.distinct()
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpfdossiereintrag.impfdossiereintrag.impfdossier.eq(QImpfdossier.impfdossier))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag.impftermin, termin)
			.innerJoin(impfslot).on(impfslot.id.eq(termin.impfslot.id))
			.leftJoin(impfung).on(impfung.termin.id.eq(termin.id)) // Impfung nur falls vorhanden
			.where(termin.impfslot.ortDerImpfung.id.eq(ortDerImpfung.getId())
				.and(termin.impfslot.zeitfenster.von.between(datum.atTime(LocalTime.MIN), datum.atTime(LocalTime.MAX)))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.fetch();
		return termineNAtOdI;
	}
}
