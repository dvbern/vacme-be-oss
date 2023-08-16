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

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.QImpfschutz;
import ch.dvbern.oss.vacme.entities.impfen.QImpfung;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.QKkkNummerAlt;
import ch.dvbern.oss.vacme.entities.registration.QRegistrierung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.jax.PersonalienSucheJax;
import ch.dvbern.oss.vacme.jax.QPersonalienSucheJax;
import ch.dvbern.oss.vacme.service.HashIdService;
import ch.dvbern.oss.vacme.smartdb.Db;
import ch.dvbern.oss.vacme.smartdb.SmartJPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import static ch.dvbern.oss.vacme.entities.impfen.QImpfdossier.impfdossier;
import static ch.dvbern.oss.vacme.entities.impfen.QImpfdossiereintrag.impfdossiereintrag;
import static ch.dvbern.oss.vacme.entities.impfen.QImpfung.impfung;
import static ch.dvbern.oss.vacme.entities.registration.QRegistrierung.registrierung;
import static ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang.DATA_MIGRATION;
import static ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang.MASSENUPLOAD;
import static ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang.ORT_DER_IMPFUNG;
import static ch.dvbern.oss.vacme.entities.terminbuchung.QImpfslot.impfslot;
import static ch.dvbern.oss.vacme.entities.terminbuchung.QImpftermin.impftermin;
import static ch.dvbern.oss.vacme.entities.terminbuchung.QOrtDerImpfung.ortDerImpfung;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ABGESCHLOSSEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.AUTOMATISCH_ABGESCHLOSSEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.GEBUCHT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.GEBUCHT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMMUNISIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.KONTROLLIERT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ODI_GEWAEHLT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ODI_GEWAEHLT_BOOSTER;

@RequestScoped
@Transactional
@Slf4j
public class RegistrierungRepo {

	private final Db db;
	private final EntityManager em;
	private final HashIdService hashIdService;

	@Inject
	public RegistrierungRepo(Db db, EntityManager em, HashIdService hashIdService) {
		this.db = db;
		this.em = em;
		this.hashIdService = hashIdService;
	}

	public void create(@NonNull Registrierung registrierung) {
		db.persist(registrierung);
		db.flush();
	}

	@NonNull
	public Optional<Registrierung> getByRegistrierungnummer(@NonNull String registrierungsnummer) {
		var result = db.selectFrom(QRegistrierung.registrierung)
			.where(QRegistrierung.registrierung.registrierungsnummer.eq(registrierungsnummer))
			.fetchOne();
		return result;
	}

	@NonNull
	public Optional<Registrierung> getByUserId(UUID userid) {
		var result = db.selectFrom(QRegistrierung.registrierung)
			.where(QRegistrierung.registrierung.benutzerId.eq(userid))
			.fetchOne();
		return result;
	}

	@NonNull
	public List<Registrierung> getPendenteByImpfslot(@NonNull Impfslot slot) {
		List<Registrierung> list = new LinkedList<>();
		list.addAll(this.getPendente1ByImpfslot(slot));
		list.addAll(this.getPendente2ByImpfslot(slot));
		list.addAll(this.getPendenteNByImpfslot(slot));
		return list;
	}

	@NonNull
	public List<Registrierung> getPendente1ByImpfslot(@NonNull Impfslot slot) {
		QImpftermin aliasTermin1 = new QImpftermin("termin1");
		QImpfslot aliasSlot1 = new QImpfslot("slot1");
		QImpfung aliasImpfung1 = new QImpfung("impfung1");
		return db
			.select(registrierung)
			.from(registrierung)
			.innerJoin(impfdossier).on(registrierung.eq(impfdossier.registrierung))
			.innerJoin(impfdossier.buchung.impftermin1, aliasTermin1)
			.innerJoin(aliasTermin1.impfslot, aliasSlot1)
			.leftJoin(aliasImpfung1).on(aliasImpfung1.termin.eq(aliasTermin1))
			.where(aliasSlot1.eq(slot).and(aliasImpfung1.isNull()))
			.fetch();
	}

	@NonNull
	public List<Registrierung> getPendente2ByImpfslot(@NonNull Impfslot slot) {
		QImpftermin aliasTermin2 = new QImpftermin("termin2");
		QImpfslot aliasSlot2 = new QImpfslot("slot2");
		QImpfung aliasImpfung2 = new QImpfung("impfung2");
		return db
			.select(registrierung)
			.from(registrierung)
			.innerJoin(impfdossier).on(registrierung.eq(impfdossier.registrierung))
			.innerJoin(impfdossier.buchung.impftermin2, aliasTermin2)
			.innerJoin(aliasTermin2.impfslot, aliasSlot2)
			.leftJoin(aliasImpfung2).on(aliasImpfung2.termin.eq(aliasTermin2))
			.where(aliasSlot2.eq(slot).and(aliasImpfung2.isNull()))
			.fetch();
	}

	@NonNull
	public List<Registrierung> getPendenteNByImpfslot(@NonNull Impfslot slot) {
		QImpftermin termin = new QImpftermin("termin");
		QImpfslot impfslot = new QImpfslot("impfslot");
		QImpfung impfung = new QImpfung("impfung");
		return db
			.select(registrierung)
			.from(registrierung)
			.innerJoin(QImpfdossier.impfdossier)
			.on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag)
			.on(QImpfdossiereintrag.impfdossiereintrag.impfdossier.eq(QImpfdossier.impfdossier))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag.impftermin, termin)
			.innerJoin(impfslot)
			.on(impfslot.id.eq(termin.impfslot.id))
			.leftJoin(impfung)
			.on(impfung.termin.id.eq(termin.id)) // Impfung nur falls vorhanden
			.where(impfslot.eq(slot)
				.and(impfung.isNull())
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.fetch();
	}

	@NonNull
	@Transactional(Transactional.TxType.SUPPORTS)
	/* use transaction if there is one in the context (keep in mind sequences can't be rolled back)
	otherwise run without */
	public String getNextRegistrierungnummer() {
		BigInteger nextValue =
			(BigInteger) em.createNativeQuery("SELECT NEXT VALUE FOR register_sequence;").getSingleResult();
		return hashIdService.getHashFromNumber(nextValue.longValue());
	}

	public Registrierung update(@NonNull Registrierung registrierung) {
		Registrierung merge = db.merge(registrierung);
		db.flush();
		return merge;
	}

	// TODO Performance testen. Falls es haeufig zu langsam geht, sollte man vielleicht die beiden Varianten separat machen
	// (aktuelle Nummer versus archivierte)
	@NonNull
	public List<Registrierung> searchRegistrierungByKvKNummer(@NonNull String kvkNummer) {
		var kkkNummerAltAlias = QKkkNummerAlt.kkkNummerAlt;
		return db.selectFrom(registrierung)
			.groupBy(registrierung.id)
			.leftJoin(kkkNummerAltAlias).on(kkkNummerAltAlias.registrierung.eq(registrierung))
			.where(registrierung.krankenkasseKartenNr.eq(kvkNummer)
				.or(kkkNummerAltAlias.nummer.eq(kvkNummer)))
			.fetch();
	}

	@NonNull
	public List<PersonalienSucheJax> findRegistrierungByGeburtsdatum(@NonNull LocalDate geburtsdatum) {
		return db
			.select(new QPersonalienSucheJax(
				impfdossier.id,
				registrierung.name,
				registrierung.vorname))
			.from(registrierung)
			.distinct()
			.innerJoin(impfdossier).on(registrierung.eq(impfdossier.registrierung))
			.where(registrierung.geburtsdatum.eq(geburtsdatum)
				.and(impfdossier.dossierStatus.in(
					ODI_GEWAEHLT, GEBUCHT,
					ABGESCHLOSSEN, ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG, AUTOMATISCH_ABGESCHLOSSEN,
					IMPFUNG_2_DURCHGEFUEHRT, IMPFUNG_2_KONTROLLIERT,
					IMPFUNG_1_DURCHGEFUEHRT, IMMUNISIERT,
					FREIGEGEBEN_BOOSTER,
					ODI_GEWAEHLT_BOOSTER,
					GEBUCHT_BOOSTER,
					KONTROLLIERT_BOOSTER)))
			.fetch();
	}

	@NonNull
	public List<PersonalienSucheJax> findRegistrierungByGeburtsdatumGeimpft(@NonNull LocalDate geburtsdatum) {
		return db.select(new QPersonalienSucheJax(
				impfdossier.id,
				registrierung.name,
				registrierung.vorname))
			.from(registrierung)
			.distinct()
			.innerJoin(impfdossier).on(registrierung.eq(impfdossier.registrierung))
			.where(registrierung.geburtsdatum.eq(geburtsdatum)
				.and(impfdossier.dossierStatus.in(
					ABGESCHLOSSEN, ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG, AUTOMATISCH_ABGESCHLOSSEN,
					IMPFUNG_2_DURCHGEFUEHRT, IMPFUNG_2_KONTROLLIERT,
					IMPFUNG_1_DURCHGEFUEHRT, IMMUNISIERT,
					FREIGEGEBEN_BOOSTER,
					ODI_GEWAEHLT_BOOSTER,
					GEBUCHT_BOOSTER,
					KONTROLLIERT_BOOSTER)))

			.fetch();
	}

	public long getAnzahlRegistrierungen() {
		return db.selectFrom(QRegistrierung.registrierung).fetchCount();
	}

	@NonNull
	public Optional<Registrierung> getById(@NonNull ID<Registrierung> id) {
		return db.get(id);
	}

	public long getAnzahlErstimpfungen(
		@NonNull Mandant mandant,
		@NonNull OrtDerImpfungTyp typ,
		@NonNull LocalDate von,
		@NonNull LocalDate bis
	) {
		var anzahl = db
			.selectFrom(registrierung)
			.innerJoin(impfdossier).on(registrierung.eq(impfdossier.registrierung))
			.innerJoin(impfdossier.buchung.impftermin1, impftermin)
			.innerJoin(impftermin.impfslot, impfslot)
			.innerJoin(impfslot.ortDerImpfung, ortDerImpfung)
			.innerJoin(impfung).on(impftermin.eq(impfung.termin))
			.where(ortDerImpfung.typ.eq(typ)
				.and(registrierung.registrierungsEingang.eq(DATA_MIGRATION).not()) // Migration wird nicht mitgezaehlt
				.and(impfung.timestampErstellt.between(von.atStartOfDay(), bis.plusDays(1).atStartOfDay()))
				.and(impftermin.impffolge.eq(Impffolge.ERSTE_IMPFUNG))
				.and(ortDerImpfung.mandant.eq(mandant)))
			.fetchCount();
		return anzahl;
	}

	public long getAnzahlBoosterOrGrundimunisierungGT3Covid(
		@NonNull Mandant mandant,
		@NonNull OrtDerImpfungTyp typ,
		@NonNull LocalDate von,
		@NonNull LocalDate bis
	) {
		var anzahl = db
			.selectFrom(impfung)
			.innerJoin(impfung.termin, impftermin)
			.innerJoin(impftermin.impfslot, impfslot)
			.innerJoin(impfslot.ortDerImpfung, ortDerImpfung)
			.innerJoin(impfdossiereintrag).on(impfdossiereintrag.impftermin.eq(impftermin))
			.innerJoin(impfdossier).on(impfdossiereintrag.impfdossier.eq(impfdossier))
			.where(ortDerImpfung.typ.eq(typ)
				.and(impfung.timestampErstellt.between(von.atStartOfDay(), bis.plusDays(1).atStartOfDay()))
				.and(impfung.grundimmunisierung.isFalse().or(impfdossiereintrag.impffolgeNr.gt(3)))
				.and(impftermin.impffolge.eq(Impffolge.BOOSTER_IMPFUNG))
				.and(impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
				.and(ortDerImpfung.mandant.eq(mandant)))
			.fetchCount();

		return anzahl;
	}

	public long getAnzahlCovidBoosterOhneErstimpfungOderBoosterImKalenderjahr(
		@NonNull Mandant mandant,
		@NonNull OrtDerImpfungTyp typ,
		@NonNull LocalDate von,
		@NonNull LocalDate bis
	) {
		// Das "aktuelle Kalenderjahr" muss dasjenige des Reportzeitraums sein
		LocalDate firstDayOfYear = von.with(TemporalAdjusters.firstDayOfYear());

		return db
			.selectFrom(impfung)
			.innerJoin(impfung.termin, impftermin)
			.innerJoin(impftermin.impfslot, impfslot)
			.innerJoin(impfslot.ortDerImpfung, ortDerImpfung)
			.innerJoin(impfdossiereintrag).on(impfdossiereintrag.impftermin.eq(impftermin))
			.innerJoin(impfdossiereintrag.impfdossier, impfdossier)
			.innerJoin(impfdossier.registrierung, registrierung)
			.where(ortDerImpfung.typ.eq(typ)
				.and(impfung.timestampErstellt.between(von.atStartOfDay(), bis.plusDays(1).atStartOfDay()))
				.and(impfung.grundimmunisierung.isFalse().or(impfdossiereintrag.impffolgeNr.gt(3)))
				.and(impftermin.impffolge.eq(Impffolge.BOOSTER_IMPFUNG))
				.and(registrierung.notIn(getErstimpfungen(firstDayOfYear, bis).asSubQuery()))
				.and(impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
				.and(impfdossier.notIn(getImpfdossierMitMehrerenBooster(
					firstDayOfYear,
					bis,
					KrankheitIdentifier.COVID).asSubQuery()))
				.and(ortDerImpfung.mandant.eq(mandant)))
			.fetchCount();
	}

	private SmartJPAQuery<Registrierung> getErstimpfungen(
		@NotNull LocalDate von,
		@NotNull LocalDate bis) {
		return db
			.selectFrom(registrierung)
			.innerJoin(impfdossier).on(registrierung.eq(impfdossier.registrierung))
			.innerJoin(impfdossier.buchung.impftermin1, impftermin)
			.innerJoin(impftermin.impfslot, impfslot)
			.innerJoin(impfung).on(impftermin.eq(impfung.termin))
			.where(registrierung.registrierungsEingang.eq(DATA_MIGRATION).not() // Migration wird nicht mitgezaehlt
				.and(impfung.timestampErstellt.between(von.atStartOfDay(), bis.plusDays(1).atStartOfDay()))
				.and(impftermin.impffolge.eq(Impffolge.ERSTE_IMPFUNG)));
	}

	private SmartJPAQuery<Impfdossier> getImpfdossierMitMehrerenBooster(
		LocalDate von, LocalDate bis,
		@NonNull KrankheitIdentifier krankheitIdentifier) {
		return db
			.select(impfdossier)
			.from(impfung)
			.innerJoin(impfung.termin, impftermin)
			.innerJoin(impftermin.impfslot, impfslot)
			.innerJoin(impfdossiereintrag).on(impfdossiereintrag.impftermin.eq(impftermin))
			.innerJoin(impfdossiereintrag.impfdossier, impfdossier)
			.where(impfung.timestampErstellt.between(von.atStartOfDay(), bis.plusDays(1).atStartOfDay())
				.and(impfung.grundimmunisierung.isFalse().or(impfdossiereintrag.impffolgeNr.gt(3)))
				.and(impftermin.impffolge.eq(Impffolge.BOOSTER_IMPFUNG))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier)))
			.groupBy(impfdossier)
			.having(impfdossier.count().gt(1));
	}

	public Optional<Registrierung> getByExternalId(String externalId) {
		return db.selectFrom(registrierung)
			.where(registrierung.externalId.eq(externalId))
			.fetchOne();
	}

	public void delete(ID<Registrierung> registrierungId) {
		db.remove(registrierungId);
		db.flush();
	}

	/**
	 * Liest alle Registrierungen aus der Datenbank die im Status IMPFUNG_1_DURCHGEFUEHRT sind
	 * und deren erster Termin schon weiter in der Vergangenheit liegt als pastDate
	 * und die entweder gar keinen 2.Termin haben oder deren 2.Termin in der Vergangenheit war (und also wohl nicht
	 * wahrgenommen wurde)
	 *
	 * @param pastDate cutoff date vor dem gesucht wird
	 * @return Liste von Registrierungen
	 */
	public List<Registrierung> getErsteImpfungNoZweiteSince(LocalDateTime pastDate) {
		QImpftermin impftermin1 = new QImpftermin("impftermin1");
		QImpftermin impftermin2 = new QImpftermin("impftermin2");
		QImpfslot impfslot1 = new QImpfslot("impfslot1");
		QImpfslot impfslot2 = new QImpfslot("impfslot2");
		return db.selectFrom(registrierung)
			.innerJoin(impfdossier).on(impfdossier.registrierung.eq(registrierung))
			.innerJoin(impfdossier.buchung.impftermin1, impftermin1)
			.innerJoin(impftermin1.impfslot, impfslot1)
			.leftJoin(impfdossier.buchung.impftermin2, impftermin2)
			.leftJoin(impftermin2.impfslot, impfslot2)
			.where(impfslot1.zeitfenster.bis.lt(pastDate)
				.and(impfdossier.dossierStatus.eq(IMPFUNG_1_DURCHGEFUEHRT))
				.and(impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
				.and(impftermin2.isNull().or(impfslot2.zeitfenster.bis.lt(LocalDateTime.now()))))
			.fetch();
	}

	public List<String> findRegistrierungenForOnboarding(long batchSize) {
		// TODO Booster??

		QImpftermin impftermin1 = new QImpftermin("impftermin1");
		QImpfslot impfslot1 = new QImpfslot("impfslot1");
		return db.select(QRegistrierung.registrierung.registrierungsnummer)
			.from(QRegistrierung.registrierung)
			.distinct()
			.innerJoin(impfdossier).on(registrierung.eq(impfdossier.registrierung))
			.innerJoin(impfdossier.buchung.impftermin1, impftermin1)
			.innerJoin(impftermin1.impfslot, impfslot1)
			.innerJoin(QImpfung.impfung).on(QImpfung.impfung.termin.eq(impftermin1))
			.where(registrierung.registrierungsEingang.in(List.of(ORT_DER_IMPFUNG, MASSENUPLOAD, DATA_MIGRATION))
				.and(registrierung.generateOnboardingLetter.isTrue())
				.and(registrierung.anonymisiert.isFalse())
			)
			.orderBy(impfung.timestampImpfung.asc())
			.limit(batchSize)
			.fetch();
	}

	/**
	 * Gibt Registrierungsnummern von lebenen Personen zurueck welche vollstaendigen Impfschutz haben
	 *
	 * @param limit maximale Anzahl zurueckgegebener
	 * @return Liste von Registrierungsnummern
	 */
	@NonNull
	public List<String> findRegsWithVollstImpfschutzToMoveToImmunisiert(long limit) {
		return db.select(QRegistrierung.registrierung.registrierungsnummer)
			.from(QRegistrierung.registrierung)
			.innerJoin(impfdossier).on(impfdossier.registrierung.eq(registrierung))
			.where(
				impfdossier.vollstaendigerImpfschutzTyp.isNotNull()
					.and(impfdossier.dossierStatus.in(ABGESCHLOSSEN, ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG))
					.and(impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
					.and(registrierung.verstorben.isFalse().or(registrierung.verstorben.isNull()))) // Feld kann null
			.orderBy(impfdossier.timestampZuletztAbgeschlossen.asc())
			.limit(limit)
			.fetch();
	}

	@NonNull
	public List<UUID> findDossiersToMoveToFreigegebenBooster(long limit) {
		return db.select(impfdossier.id)
			.from(QImpfschutz.impfschutz)
			.innerJoin(impfdossier).on(impfdossier.impfschutz.eq(QImpfschutz.impfschutz))
			.innerJoin(registrierung).on(impfdossier.registrierung.eq(registrierung))
			.where(impfdossier.dossierStatus.in(IMMUNISIERT)
				.and(QImpfschutz.impfschutz.freigegebenNaechsteImpfungAb.lt(LocalDateTime.now()))
				.and(registrierung.verstorben.isFalse().or(registrierung.verstorben.isNull())) // Feld kann null
			)
			.orderBy(QImpfschutz.impfschutz.freigegebenNaechsteImpfungAb.asc())
			.limit(limit)
			.fetch();
	}

	public List<String> getRegnumsOfGroupWithAgeGreaterOrEq(Prioritaet prioritaetFrom, int age) {
		LocalDate ageBorderDate = LocalDate.now().minusYears(age);
		List<String> foundRegs = db.select(registrierung.registrierungsnummer)
			.from(registrierung)
			.where(registrierung.prioritaet.eq(prioritaetFrom)
				.and(registrierung.geburtsdatum.loe(ageBorderDate))).fetch();
		return foundRegs;
	}
}
