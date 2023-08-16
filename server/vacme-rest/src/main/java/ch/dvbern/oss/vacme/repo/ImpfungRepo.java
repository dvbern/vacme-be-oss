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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.QExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.QImpfstoff;
import ch.dvbern.oss.vacme.entities.impfen.QImpfung;
import ch.dvbern.oss.vacme.entities.impfen.QKrankheit;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.QFragebogen;
import ch.dvbern.oss.vacme.entities.registration.QRegistrierung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.QOrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.Kundengruppe;
import ch.dvbern.oss.vacme.jax.QZweitBoosterMailDataRow;
import ch.dvbern.oss.vacme.jax.ZweitBoosterMailDataRow;
import ch.dvbern.oss.vacme.smartdb.Db;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationDtoRecreator;
import ch.dvbern.oss.vacme.util.QImpfinformationDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import io.opentelemetry.extension.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.entities.registration.QRegistrierung.registrierung;

@RequestScoped
@Transactional
@Slf4j
public class ImpfungRepo {

	public static final int SLOW_THRESHOLD_MS =  15  * 1000;
	private final Db db;

	@Inject
	public ImpfungRepo(Db db) {
		this.db = db;
	}

	public void create(Impfung impfung) {
		db.persist(impfung);
		db.flush();
	}

	public void update(@NonNull Impfung impfung) {
		db.merge(impfung);
		db.flush();
	}

	public void delete(@NonNull ID<Impfung> id) {
		db.remove(id);
		db.flush();
	}

	public Optional<Impfung> getById(ID<Impfung> id) {
		return db.get(id);
	}

	@WithSpan
	@NonNull
	public Optional<Impfung> getByImpftermin(@NonNull Impftermin termin) {
		final Optional<Impfung> registrierungOptional = db.select(QImpfung.impfung)
			.from(QImpfung.impfung)
			.where(QImpfung.impfung.termin.eq(termin)).fetchOne();
		return registrierungOptional;
	}

	private void logIfSlow(StopWatch stopwatch, int resultCnt, String queryname) {
		stopwatch.stop();
		if (stopwatch.getTime(TimeUnit.MILLISECONDS) > SLOW_THRESHOLD_MS) {
			LOG.warn(
				"VACME-VMDL: Querytime for query '{}' with resultcount {} was {}ms",
				queryname,
				resultCnt,
				stopwatch.getTime(TimeUnit.MILLISECONDS));

		}
	}

	@NonNull
	public Optional<Impfung> getOneByKrankheitAndKantonaleBerechtigung(
		@NonNull KrankheitIdentifier krankheit,
		@NonNull KantonaleBerechtigung kantonaleBerechtigung
	) {
		final Optional<Impfung> impfungOptional =
			db.select(QImpfung.impfung)
				.from(QImpfung.impfung)
				.innerJoin(QImpfstoff.impfstoff).on(QImpfung.impfung.impfstoff.eq(QImpfstoff.impfstoff))
				.innerJoin(QKrankheit.krankheit).on(QImpfstoff.impfstoff.krankheiten.contains(QKrankheit.krankheit))
				.where(QKrankheit.krankheit.identifier.eq(krankheit)
					.and(QImpfung.impfung.kantonaleBerechtigung.eq(kantonaleBerechtigung)))
				.limit(1)
				.fetchOne();
		return impfungOptional;
	}

	@NonNull
	public Optional<ImpfinformationDto> getImpfinformationenOptional(
		@NonNull String registrierungsNummer,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		QImpftermin aliasTermin1 = new QImpftermin("termin1");
		QImpftermin aliasTermin2 = new QImpftermin("termin2");
		QImpfung aliasImpfung1 = new QImpfung("impfung1");
		QImpfung aliasImpfung2 = new QImpfung("impfung2");
		QImpfdossier dossier = QImpfdossier.impfdossier;
		QExternesZertifikat externesZertifikat = new QExternesZertifikat("externesZertifikat");
		final ConstructorExpression<ImpfinformationDto> constructor;
		if (KrankheitIdentifier.COVID == krankheitIdentifier) {
			constructor = new QImpfinformationDto(
				Expressions.constant(KrankheitIdentifier.COVID),
				registrierung,
				aliasImpfung1,
				aliasImpfung2,
				dossier,
				externesZertifikat);
		} else {
			constructor = new QImpfinformationDto(
				Expressions.constant(krankheitIdentifier),
				registrierung,
				dossier,
				externesZertifikat);
		}
		Optional<ImpfinformationDto> optional = db
			.select(constructor)
			.from(registrierung)
			.innerJoin(dossier).on(dossier.registrierung.eq(registrierung))
			.leftJoin(QImpfdossier.impfdossier.buchung.impftermin1, aliasTermin1)
			.leftJoin(QImpfdossier.impfdossier.buchung.impftermin2, aliasTermin2)
			.leftJoin(aliasImpfung1).on(aliasImpfung1.termin.eq(aliasTermin1))
			.leftJoin(aliasImpfung2).on(aliasImpfung2.termin.eq(aliasTermin2))
			.leftJoin(externesZertifikat).on(externesZertifikat.impfdossier.eq(dossier))
			.where(registrierung.registrierungsnummer.eq(registrierungsNummer)
				.and(dossier.krankheitIdentifier.eq(krankheitIdentifier)))
			.fetchOne();

		return optional.map(impfinformationen -> {
			List<Impfung> boosterImpfungen = getBoosterImpfungen(registrierungsNummer, krankheitIdentifier);
			return ImpfinformationDtoRecreator.from(impfinformationen).withBoosterImpfungen(boosterImpfungen).build();
		});
	}

	@NonNull
	public List<Impfung> getBoosterImpfungen(
		@NonNull String registrierungsNummer,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		return db.selectFrom(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpfung.impfung.termin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpfdossiereintrag.impfdossiereintrag.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossiereintrag.impfdossiereintrag.impfdossier.eq(QImpfdossier.impfdossier))
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.where(registrierung.registrierungsnummer.eq(registrierungsNummer)
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier)))
			.orderBy(QImpfdossiereintrag.impfdossiereintrag.impffolgeNr.asc())
			.fetch();
	}

	@NonNull
	public List<Impfung> getAllImpfungen(
		@NonNull String registrierungsNummer
	) {
		List<Impfung> result = new ArrayList<>();
		// Impfung 1 (Covid)
		final Optional<Impfung> optionalImpfung1 = db.selectFrom(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpfung.impfung.termin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin1))
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.where(registrierung.registrierungsnummer.eq(registrierungsNummer))
			.fetchOne();
		optionalImpfung1.ifPresent(result::add);
		// Impfung 2 (Covid)
		final Optional<Impfung> optionalImpfung2 = db.selectFrom(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpfung.impfung.termin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin2))
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.where(registrierung.registrierungsnummer.eq(registrierungsNummer))
			.fetchOne();
		optionalImpfung2.ifPresent(result::add);
		// Alle anderen Impfungen aller Krankheiten
		final List<Impfung> impfungenAusDossiers = db.selectFrom(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin)
			.on(QImpfung.impfung.termin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag)
			.on(QImpfdossiereintrag.impfdossiereintrag.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossier.impfdossier)
			.on(QImpfdossiereintrag.impfdossiereintrag.impfdossier.eq(QImpfdossier.impfdossier))
			.innerJoin(registrierung)
			.on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.where(registrierung.registrierungsnummer.eq(registrierungsNummer))
			.orderBy(QImpfdossiereintrag.impfdossiereintrag.impffolgeNr.asc())
			.fetch();
		result.addAll(impfungenAusDossiers);
		return result;
	}

	/**
	 * List die zu einer Impfung gehoerenden Impfinformationen aus. Dabei spielt es keine Rolle ob
	 * es sich um eine Impfung 1 /2 oder N handelt
	 */
	@NonNull
	public Optional<ImpfinformationDto> getImpfinformationenOptional(@NonNull ID<Impfung> impfungId, @NonNull KrankheitIdentifier krankheitIdentifier) {
		Optional<Registrierung> registrierungForImpfung = getRegistrierungForImpfung(impfungId);
		return registrierungForImpfung.flatMap(value -> getImpfinformationenOptional(value.getRegistrierungsnummer(), krankheitIdentifier));
	}

	@NonNull
	public Optional<Registrierung> getRegistrierungForImpfung(@NonNull ID<Impfung> impfungId) {
		BooleanExpression joinExpressionT1 = QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin1);
		Optional<Registrierung> regOpt = runQueryReadRegistrierungForGrundImpfung(impfungId,  joinExpressionT1, "reg_t1_impfungen");

		Optional<Registrierung> result =
			regOpt.or(() -> {
				BooleanExpression joinExpressionT2 = QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin2);
				return runQueryReadRegistrierungForGrundImpfung(impfungId, joinExpressionT2, "reg_t2_impfungen");
			}).or(() -> runQueryReadRegistrierungForNImpfung(impfungId));
		return result;
	}

	@NonNull
	private Optional<Registrierung> runQueryReadRegistrierungForGrundImpfung(@NonNull ID<Impfung> impfungId, @NonNull Predicate impfdossierJoinfExpression, @NonNull String queryName) {
		StopWatch stopwatch = StopWatch.createStarted();
		Optional<Registrierung> registrierung = db.select(QRegistrierung.registrierung)
			.from(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.eq(QImpfung.impfung.termin))
			.innerJoin(QImpfdossier.impfdossier).on(impfdossierJoinfExpression)
			.innerJoin(QRegistrierung.registrierung).on(QRegistrierung.registrierung.eq(QImpfdossier.impfdossier.registrierung))
			.where(QImpfung.impfung.id.eq(impfungId.getId()))
			.fetchOne();

		logIfSlow(stopwatch, registrierung.isPresent() ? 1 : 0 , queryName);
		return registrierung;
	}

	@NonNull
	private Optional<Registrierung> runQueryReadRegistrierungForNImpfung(@NonNull ID<Impfung> impfungId) {
		StopWatch stopwatch = StopWatch.createStarted();
		Optional<Registrierung> regOpt =
			db.select(registrierung)
			.from(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.eq(QImpfung.impfung.termin))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpfdossiereintrag.impfdossiereintrag.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.eq(QImpfdossiereintrag.impfdossiereintrag.impfdossier))
			.innerJoin(registrierung)
			.on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.where(QImpfung.impfung.id.eq(impfungId.getId()))
			.fetchOne();
		logIfSlow(stopwatch, regOpt.isPresent() ? 1 : 0 , "reg_N_impfungen");
		return regOpt;
	}

	@NonNull
	public Map<Prioritaet, Long> getCountAllErstImpfungenPerPrioritaet() {
		StopWatch stopWatch = StopWatch.createStarted();
		Map<Prioritaet, Long> erstImpfungen = new EnumMap<>(Prioritaet.class);
		final List<Tuple> tuples = db
			.select(registrierung.prioritaet, QImpfung.impfung.count())
			.from(QImpfung.impfung)
			.innerJoin(QImpfung.impfung.termin, QImpftermin.impftermin)
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.buchung.impftermin1.eq(QImpftermin.impftermin))
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.groupBy(registrierung.prioritaet)
			.fetch();
		for (Tuple tuple : tuples) {
			erstImpfungen.put(tuple.get(0, Prioritaet.class), tuple.get(1, Long.class));
		}
		logIfSlow(stopWatch, erstImpfungen.size(), "getCountAllErstImpfungenPerPrioritaet");
		return erstImpfungen;
	}

	@NonNull
	public Map<Prioritaet, Long> getCountAllZweitImpfungenPerPrioritaet() {
		StopWatch stopWatch = StopWatch.createStarted();
		Map<Prioritaet, Long> zweitImpfungen = new EnumMap<>(Prioritaet.class);
		final List<Tuple> tuples = db
			.select(registrierung.prioritaet, QImpfung.impfung.count())
			.from(QImpfung.impfung)
			.innerJoin(QImpfung.impfung.termin, QImpftermin.impftermin)
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.buchung.impftermin2.eq(QImpftermin.impftermin))
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.groupBy(registrierung.prioritaet)
			.fetch();
		for (Tuple tuple : tuples) {
			zweitImpfungen.put(tuple.get(0, Prioritaet.class), tuple.get(1, Long.class));
		}
		logIfSlow(stopWatch, zweitImpfungen.size(), "getCountAllZweitImpfungenPerPrioritaet");
		return zweitImpfungen;
	}

	@NonNull
	public Map<Prioritaet, Long> getCountAllErstBoosterPerPrioritaet() {
		StopWatch stopWatch = StopWatch.createStarted();
		Map<Prioritaet, Long> erstBooster = new EnumMap<>(Prioritaet.class);
		final List<Tuple> tuples = db
			.select(registrierung.prioritaet, QImpfdossier.impfdossier.countDistinct())
			.from(QImpfung.impfung)
			.innerJoin(QImpfung.impfung.termin, QImpftermin.impftermin)
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag)
			.on(QImpfdossiereintrag.impfdossiereintrag.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossier.impfdossier)
			.on(QImpfdossiereintrag.impfdossiereintrag.impfdossier.eq(QImpfdossier.impfdossier))
			.innerJoin(registrierung)
			.on(registrierung.eq(QImpfdossier.impfdossier.registrierung))
			.where((QImpfung.impfung.grundimmunisierung.isFalse()
				.or(QImpfdossiereintrag.impfdossiereintrag.impffolgeNr.gt(3)))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.groupBy(registrierung.prioritaet)
			.fetch();
		for (Tuple tuple : tuples) {
			erstBooster.put(tuple.get(0, Prioritaet.class), tuple.get(1, Long.class));
		}
		logIfSlow(stopWatch, erstBooster.size(), "getCountAllErstBoosterPerPrioritaet");
		return erstBooster;
	}

	/**
	 * Mit Abfrage der min. impffolgeNr. Ist aber (zumindest momentan noch) weniger performant
	 */
	public Map<Prioritaet, Long> getCountAllErstBoosterPerPrioritaetV2() {
		QImpfdossiereintrag aliasDossiereintragMain = new QImpfdossiereintrag("eintrag_main");
		QImpfdossier aliasDossierMain = new QImpfdossier("dossier_main");
		StopWatch stopWatch = StopWatch.createStarted();
		Map<Prioritaet, Long> erstBooster = new EnumMap<>(Prioritaet.class);
		final List<Tuple> tuples = db
			.select(registrierung.prioritaet, QImpfung.impfung.count())
			.from(QImpfung.impfung)
			.innerJoin(QImpfung.impfung.termin, QImpftermin.impftermin)
			.innerJoin(aliasDossiereintragMain).on(aliasDossiereintragMain.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(aliasDossierMain).on(aliasDossiereintragMain.impfdossier.eq(aliasDossierMain))
			.innerJoin(registrierung).on(registrierung.eq(aliasDossierMain.registrierung))
			.where(aliasDossiereintragMain.impffolgeNr.eq(getMinimalBoosterImpffolgeNrQuery(aliasDossierMain))
				.and(QImpfung.impfung.grundimmunisierung.isFalse()
					.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			)
			.groupBy(registrierung.prioritaet)
			.fetch();
		for (Tuple tuple : tuples) {
			erstBooster.put(tuple.get(0, Prioritaet.class), tuple.get(1, Long.class));
		}
		logIfSlow(stopWatch, erstBooster.size(), "getCountAllErstBoosterPerPrioritaet");
		return erstBooster;
	}

	private SubQueryExpression<Integer> getMinimalBoosterImpffolgeNrQuery(Expression<Impfdossier> outerDossier) {
		QImpfdossiereintrag aliasDossiereintraqSub = new QImpfdossiereintrag("eintrag_subquery");
		QImpfdossier aliasDossierSub = new QImpfdossier("dossier_subquery");
		return db.select(aliasDossiereintraqSub.impffolgeNr.min())
			.from(aliasDossiereintraqSub)
			.innerJoin(aliasDossierSub).on(aliasDossiereintraqSub.impfdossier.eq(aliasDossierSub))
			.innerJoin(QImpftermin.impftermin).on(aliasDossiereintraqSub.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfung.impfung).on(QImpftermin.impftermin.eq(QImpfung.impfung.termin))
			.where(aliasDossierSub.eq(outerDossier)
				.and(QImpfung.impfung.grundimmunisierung.isFalse())
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.asSubQuery();
	}

	@NonNull
	public List<ZweitBoosterMailDataRow> getAllZweitOderMehrBooster() {
		StopWatch stopWatch = StopWatch.createStarted();

		final int maxImpffolgeNr = getMaxImpffolgeNr();

		final List<ZweitBoosterMailDataRow> allImpfungen = new ArrayList<>();
		for (int i = 2; i < maxImpffolgeNr; i++) {
			StopWatch stopWatch2 = StopWatch.createStarted();
			List<ZweitBoosterMailDataRow> impfungen = getAllNBooster(i);
			logIfSlow(stopWatch2, impfungen.size(), "zweit_booster_mit_impffolgeNr " + (i + 1));
			allImpfungen.addAll(impfungen);
		}

		logIfSlow(stopWatch, allImpfungen.size(), "all_zweit_oder_mehr_booster");

		return allImpfungen;
	}

	public long getAnzahlImpfungen(@NonNull KrankheitIdentifier krankheitIdentifier) {
		return db
			.select(QImpfung.impfung)
			.from(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpfung.impfung.termin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfslot.impfslot).on(QImpftermin.impftermin.impfslot.eq(QImpfslot.impfslot))
			.where(QImpfslot.impfslot.krankheitIdentifier.eq(krankheitIdentifier))
			.fetchCount();
	}

	public long getAnzahlImpfungen(@NonNull KrankheitIdentifier krankheitIdentifier, @NonNull Kundengruppe kundengruppe) {
		return db
			.select(QImpfung.impfung)
			.from(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpfung.impfung.termin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfslot.impfslot).on(QImpftermin.impftermin.impfslot.eq(QImpfslot.impfslot))
			.innerJoin(QOrtDerImpfung.ortDerImpfung).on(QImpfslot.impfslot.ortDerImpfung.eq(QOrtDerImpfung.ortDerImpfung))
			.where(QImpfslot.impfslot.krankheitIdentifier.eq(krankheitIdentifier)
					.and(QOrtDerImpfung.ortDerImpfung.kundengruppe.eq(kundengruppe)))
			.fetchCount();
	}

	/**
	 * Mit Abfrage der min. impffolgeNr. Ist aber (zumindest momentan noch) weniger performant
	 * Aendert sich evtl. wenn bei der anderen Variante noch mehr Iterationen faellig werden
	 */
	@NonNull
	public List<ZweitBoosterMailDataRow> getAllZweitOderMehrBoosterV2() {
		QImpfdossiereintrag aliasDossiereintragMain = new QImpfdossiereintrag("eintrag_main");
		QImpfdossier aliasDossierMain = new QImpfdossier("dossier_main");
		StopWatch stopWatch = StopWatch.createStarted();
		final List<ZweitBoosterMailDataRow> dataRows = db.select(new QZweitBoosterMailDataRow(
				QImpfung.impfung.selbstzahlende,
				registrierung.geburtsdatum,
				QFragebogen.fragebogen.immunsupprimiert,
				registrierung.prioritaet
			))
			.from(QImpfung.impfung)
			.innerJoin(QImpfung.impfung.termin, QImpftermin.impftermin)
			.innerJoin(aliasDossiereintragMain).on(aliasDossiereintragMain.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(aliasDossierMain).on(aliasDossiereintragMain.impfdossier.eq(aliasDossierMain))
			.innerJoin(registrierung).on(registrierung.eq(aliasDossierMain.registrierung))
			.innerJoin(QFragebogen.fragebogen).on(registrierung.eq(QFragebogen.fragebogen.registrierung))
			.where(aliasDossiereintragMain.impffolgeNr.ne(getMinimalBoosterImpffolgeNrQuery(aliasDossierMain))
				.and(QImpfung.impfung.grundimmunisierung.isFalse()
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			)
			.fetch();
		logIfSlow(stopWatch, dataRows.size(), "getAllZweitOderMehrBooster");
		return dataRows;
	}

	private int getMaxImpffolgeNr() {
		return db.select(QImpfdossiereintrag.impfdossiereintrag.impffolgeNr.max())
			.from(QImpfdossiereintrag.impfdossiereintrag)
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.eq(QImpfdossiereintrag.impfdossiereintrag.impfdossier))
			.where(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
			.fetchFirst();
	}

	private List<ZweitBoosterMailDataRow> getAllNBooster(int impffolgeNr) {
		QImpfdossiereintrag aliasImpfdossiereintrag1 = new QImpfdossiereintrag("impfdossiereintrag1");
		QImpfdossiereintrag aliasImpfdossiereintrag2 = new QImpfdossiereintrag("impfdossiereintrag2");
		QImpfung aliasImpfung1 = new QImpfung("impfung1");
		QImpfung aliasImpfung2 = new QImpfung("impfung2");
		QImpftermin aliasImpftermin1 = new QImpftermin("impftermin1");
		QImpftermin aliasImpftermin2 = new QImpftermin("impftermin2");

		return db.select(new QZweitBoosterMailDataRow(
				aliasImpfung2.selbstzahlende,
				registrierung.geburtsdatum,
				QFragebogen.fragebogen.immunsupprimiert,
				registrierung.prioritaet
			))
			.from(registrierung)
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.innerJoin(QFragebogen.fragebogen).on(QFragebogen.fragebogen.registrierung.eq(registrierung))

			.innerJoin(aliasImpfdossiereintrag1).on(aliasImpfdossiereintrag1.impfdossier.eq(QImpfdossier.impfdossier)
				.and(aliasImpfdossiereintrag1.impffolgeNr.eq(impffolgeNr)))
			.innerJoin(aliasImpfdossiereintrag1.impftermin, aliasImpftermin1)
			.innerJoin(aliasImpfung1).on(aliasImpfung1.termin.eq(aliasImpftermin1))

			.innerJoin(aliasImpfdossiereintrag2).on(aliasImpfdossiereintrag2.impfdossier.eq(QImpfdossier.impfdossier)
				.and(aliasImpfdossiereintrag2.impffolgeNr.eq(impffolgeNr + 1)))
			.innerJoin(aliasImpfdossiereintrag2.impftermin, aliasImpftermin2)
			.innerJoin(aliasImpfung2).on(aliasImpfung2.termin.eq(aliasImpftermin2))

			.where(aliasImpfung1.grundimmunisierung.isFalse()
				.and(aliasImpfung2.grundimmunisierung.isFalse())
				.and(aliasImpftermin1.impffolge.eq(Impffolge.BOOSTER_IMPFUNG))
				.and(aliasImpftermin2.impffolge.eq(Impffolge.BOOSTER_IMPFUNG))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
			)
			.fetch();
	}

	@NonNull
	public List<UUID> getImpfungenZuArchivieren(long batchsize) {
		return db.select(QImpfung.impfung.id)
			.from(QImpfung.impfung)
			.where(QImpfung.impfung.sollArchiviertWerden.isTrue()
				.and(QImpfung.impfung.archiviertAm.isNull()))
			.limit(batchsize)
			.fetch();
	}
}
