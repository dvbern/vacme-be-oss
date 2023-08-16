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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.benutzer.QBenutzer;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.QImpfung;
import ch.dvbern.oss.vacme.entities.registration.QRegistrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.QOrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.enums.FileNameEnum;
import ch.dvbern.oss.vacme.jax.AbrechnungDTO;
import ch.dvbern.oss.vacme.jax.AbrechnungZHDTO;
import ch.dvbern.oss.vacme.jax.QAbrechnungDTO;
import ch.dvbern.oss.vacme.jax.QAbrechnungZHDTO;
import ch.dvbern.oss.vacme.reports.abrechnung.AbrechnungDataRow;
import ch.dvbern.oss.vacme.reports.abrechnungZH.AbrechnungZHDataRow;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.smartdb.Db;
import ch.dvbern.oss.vacme.smartdb.SmartJPAQuery;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.entities.impfen.QImpfdossier.impfdossier;
import static ch.dvbern.oss.vacme.entities.registration.QRegistrierung.registrierung;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_ALTERSHEIM;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_ANDERE;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_APOTHEKE;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_HAUSARZT;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_IMPFTENTRUM;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_MOBIL;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_SPITAL;
import static ch.dvbern.oss.vacme.shared.util.Constants.MODERNA_BIVALENT_UUID;
import static ch.dvbern.oss.vacme.shared.util.Constants.MODERNA_UUID;

@RequestScoped
@Transactional
@Slf4j
public class AbrechnungRepo {

	private final Db db;

	@Inject
	public AbrechnungRepo(Db db) {
		this.db = db;
	}

	@NonNull
	@TransactionConfiguration(timeout = Constants.DAY_IN_SECONDS)
	public List<AbrechnungDataRow> findOdiAbrechnung(
		@NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis,
		FileNameEnum fileName) {
		QImpfung impfung = new QImpfung("impfung");
		String booleanTemplateStr;

		LOG.info("VACME-INFO: ABRECHNUNG gestartet {}", DateUtil.formatLocalTime(LocalDateTime.now()));

		switch (fileName) {
		case ABRECHNUNG_ERWACHSEN:
			booleanTemplateStr = "((datediff({0}, {1})/365.2425) >= 12)";
			break;
		case ABRECHNUNG_KIND:
			booleanTemplateStr = "((datediff({0}, {1})/365.2425) < 12)";
			break;
		default:
			booleanTemplateStr = "1 = 1";
		}

		final BooleanTemplate booleanTemplate = Expressions.booleanTemplate(
			booleanTemplateStr,
			impfung.timestampImpfung,
			registrierung.geburtsdatum);

		List<AbrechnungDTO> result;

		//Anzahl Impfungen pro ODI und pro Krankenkasse
		result = runOdiAbrechnungRegistrierungsimpfungenQuery(dateVon,
			dateBis,
			impfung,
			booleanTemplate,
			getBooleanExpImpftermin1(),
			"Impfungen 1");
		result.addAll(runOdiAbrechnungRegistrierungsimpfungenQuery(
			dateVon,
			dateBis,
			impfung,
			booleanTemplate,
			getBooleanExpImpftermin2(),
			"Impfungen 2"));
		result.addAll(runOdiAbrechnungDossierimpfungenQuery(dateVon, dateBis, impfung, booleanTemplate));

		StopWatch stopWatch = StopWatch.createStarted();
		// Die Totals sind jetzt pro Krankenkasse, sie muessen noch addiert werden
		List<AbrechnungDataRow> resultKrankenkassen = assembleKrankenkasseNormal(result);
		logAbrechnungKrankenkasseBeendetAndRestartWatch(stopWatch, resultKrankenkassen.size());

		// Anzahl selbstbezahlte Impfungen werden pro ODI gezählt
		resultKrankenkassen.addAll(readSelbstzahlendeToResult(resultKrankenkassen, dateVon, dateBis, booleanTemplate));

		// Um OuterJoins zu den Benutzern zu vermeiden, wurde im obigen Query der Fachverantwortliche und der Organisationsverantwortliche
		// ausgeklammert. Diese muessen jetzt noch gelesen werden
		readBenutzerToResult(resultKrankenkassen);
		logAbrechungBenutzerBeendetAndRestartWatch(stopWatch);

		stopWatch.stop();
		return resultKrankenkassen;
	}

	private List<AbrechnungDTO> runOdiAbrechnungRegistrierungsimpfungenQuery(
		@NonNull LocalDate dateVon, @NonNull LocalDate dateBis,
		QImpfung impfung, BooleanTemplate booleanTemplate, Predicate terminToDossierJoinExpression, String currentJoinName) {
		StopWatch stopWatch = StopWatch.createStarted();

		QOrtDerImpfung odi = QOrtDerImpfung.ortDerImpfung;
		QImpftermin termin = QImpftermin.impftermin;
		QImpfslot slot = new QImpfslot("slot");

		List<AbrechnungDTO> result = getAbrechnungRootQuery(dateVon, dateBis, odi, termin, slot, impfung)
			.innerJoin(impfdossier).on(terminToDossierJoinExpression)
			.innerJoin(registrierung).on(registrierung.eq(impfdossier.registrierung))
			.where(booleanTemplate
				.and(impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.groupBy(odi, registrierung.krankenkasse)
			.fetch();

		logAbrechnungScriptBeendetAndRestartWatch(currentJoinName, stopWatch, result.size());

		stopWatch.stop();
		return result;
	}

	private List<AbrechnungDTO> runOdiAbrechnungDossierimpfungenQuery(
		@NonNull LocalDate dateVon, @NonNull LocalDate dateBis,
		QImpfung impfung, BooleanTemplate booleanTemplate) {
		StopWatch stopWatch = StopWatch.createStarted();
		QOrtDerImpfung odi = QOrtDerImpfung.ortDerImpfung;
		QImpftermin termin = QImpftermin.impftermin;
		QImpfslot slot = new QImpfslot("slot");
		QImpfdossiereintrag eintrag = new QImpfdossiereintrag("eintrag");
		QImpfdossier dossier = new QImpfdossier("dossier");

		List<AbrechnungDTO> result = getAbrechnungRootQuery(dateVon, dateBis, odi, termin, slot, impfung)
			.leftJoin(eintrag).on(termin.eq(eintrag.impftermin))
			.leftJoin(dossier).on(eintrag.impfdossier.eq(dossier))
			.innerJoin(registrierung).on(registrierung.eq(dossier.registrierung))
			.where(booleanTemplate
				.and(dossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.groupBy(odi, registrierung.krankenkasse)
			.fetch();

		logAbrechnungScriptBeendetAndRestartWatch("Impfungen N", stopWatch, result.size());

		stopWatch.stop();
		return result;
	}

	@NonNull
	private SmartJPAQuery<AbrechnungDTO> getAbrechnungRootQuery(
		@NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis,
		@NonNull QOrtDerImpfung odi,
		@NonNull QImpftermin termin,
		@NonNull QImpfslot slot,
		@NonNull QImpfung impfung
	) {
		SmartJPAQuery<AbrechnungDTO> abrechnungRootQuery = db
			.select(new QAbrechnungDTO(
				QOrtDerImpfung.ortDerImpfung,
				registrierung.krankenkasse,
				registrierung.id.count()))
			.from(odi)
			.innerJoin(slot).on(slot.ortDerImpfung.eq(odi))
			.innerJoin(termin).on(termin.impfslot.eq(slot))
			.innerJoin(impfung).on(impfung.termin.eq(termin)
				.and(impfung.extern.isFalse())
				.and(impfung.selbstzahlende.isFalse())
				.and(impfung.timestampImpfung.between(dateVon.atTime(LocalTime.MIN), dateBis.atTime(LocalTime.MAX)))
				.and(impfung.kantonaleBerechtigung.in(KantonaleBerechtigung.editableForKanton())));

		return abrechnungRootQuery;
	}

	@NonNull
	private List<AbrechnungDataRow> assembleKrankenkasseNormal(@NonNull List<AbrechnungDTO> fetch) {
		Map<OrtDerImpfung, AbrechnungDataRow> myMap = new HashMap<>();
		for (AbrechnungDTO dto : fetch) {
			if (!myMap.containsKey(dto.getOrtDerImpfung())) {
				myMap.put(dto.getOrtDerImpfung(), toDataRow(dto));
			} else {
				final AbrechnungDataRow found = myMap.get(dto.getOrtDerImpfung());
				addToDataRow(dto, found);

			}
		}
		return new ArrayList<>(myMap.values());
	}

	@NonNull
	private AbrechnungDataRow toDataRow(@NonNull AbrechnungDTO dto) {
		AbrechnungDataRow row = new AbrechnungDataRow();
		row.setOrtDerImpfung(dto.getOrtDerImpfung());
		if (dto.getKrankenkasse() != null && dto.getKrankenkassenCount() != null) {
			switch (dto.getKrankenkasse()) {
			case ANDERE:
				row.setKrankenkasseAndereCount(dto.getKrankenkassenCount());
				break;
			case AUSLAND:
				row.setKrankenkasseAuslandCount(dto.getKrankenkassenCount());
				break;
			case EDA:
				row.setKrankenkasseEdaCount(dto.getKrankenkassenCount());
				break;
			default:
				row.setKrankenkasseOKPCount(dto.getKrankenkassenCount());
				break;
			}
		}
		return row;
	}

	private void addToDataRow(@NonNull AbrechnungDTO dto, @NonNull AbrechnungDataRow row) {
		if (dto.getKrankenkasse() != null && dto.getKrankenkassenCount() != null) {
			switch (dto.getKrankenkasse()) {
			case ANDERE:
				row.setKrankenkasseAndereCount(row.getKrankenkasseAndereCount() + dto.getKrankenkassenCount());
				break;
			case AUSLAND:
				row.setKrankenkasseAuslandCount(row.getKrankenkasseAuslandCount() + dto.getKrankenkassenCount());
				break;
			case EDA:
				row.setKrankenkasseEdaCount(row.getKrankenkasseEdaCount() + dto.getKrankenkassenCount());
				break;
			default:
				row.setKrankenkasseOKPCount(row.getKrankenkasseOKPCount() + dto.getKrankenkassenCount());
				break;
			}
		}
	}

	private List<AbrechnungDataRow> readSelbstzahlendeToResult(
		@NonNull List<? extends AbrechnungDataRow> result,
		@NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis,
		BooleanTemplate booleanTemplate) {
		QImpfung qImpfung = new QImpfung("impfung");
		QOrtDerImpfung odi = QOrtDerImpfung.ortDerImpfung;
		QImpftermin termin = QImpftermin.impftermin;
		QImpfslot slot = QImpfslot.impfslot;
		List<AbrechnungDataRow> newDataRows = new ArrayList<>();
		Map<OrtDerImpfung, Long> odiCountSelbstzahlendeMap = new HashMap<>();

		// Erhalte selbstbezahlte 1. Impfungen
		odiCountSelbstzahlendeMap.putAll(
			getSelbstzahlendeRegistrierungsimpfungen(
				dateVon,
				dateBis,
				odi,
				termin,
				slot,
				qImpfung,
				getBooleanExpImpftermin1(),
				booleanTemplate));

		// Erhalte selbstbezahlte 2. Impfungen
		Map<OrtDerImpfung, Long> odiCountSelbstzahlendeZweitImpfung =
			getSelbstzahlendeRegistrierungsimpfungen(
				dateVon,
				dateBis,
				odi,
				termin,
				slot,
				qImpfung,
				getBooleanExpImpftermin2(),
				booleanTemplate);
		// Merge Anzahl selbstbezahlte bei gleichem ODI
		odiCountSelbstzahlendeZweitImpfung.forEach((k, v) -> odiCountSelbstzahlendeMap.merge(
			k,
			v,
			(v1, v2) -> v1 + v2));

		// Erhalte selbstbezahlte N-Impfungen
		Map<OrtDerImpfung, Long> odiCountSelbstzahlendeNImpfung =
			getSelbstzahlendeDossierimpfungen(dateVon, dateBis, odi, termin, slot, qImpfung, booleanTemplate);
		// Merge Anzahl selbstbezahlte bei gleichem ODI
		odiCountSelbstzahlendeNImpfung.forEach((k, v) -> odiCountSelbstzahlendeMap.merge(k, v, (v1, v2) -> v1 + v2));

		for (OrtDerImpfung odiSelbstzahlende : odiCountSelbstzahlendeMap.keySet()) {
			Long countSelbstzahlende = odiCountSelbstzahlendeMap.get(odiSelbstzahlende);

			AbrechnungDataRow existingRow = result.stream()
				.filter(r -> r.getOrtDerImpfung() != null && odiSelbstzahlende.getId().equals(r.getOrtDerImpfung().getId()))
				.findAny()
				.orElse(null);

			if (existingRow != null) { // Row mit ODI existiert bereits
				existingRow.setSelbstzahlendeCount(countSelbstzahlende);
			} else { //Neues ODI mit nur selbstzahlende Impfungen
				AbrechnungDataRow newRow = new AbrechnungDataRow();
				newRow.setOrtDerImpfung(odiSelbstzahlende);
				newRow.setSelbstzahlendeCount(countSelbstzahlende);
				newDataRows.add(newRow);
			}
		}
		return newDataRows;
	}

	private Map<OrtDerImpfung, Long> getSelbstzahlendeRegistrierungsimpfungen(
		@NonNull LocalDate dateVon, @NonNull LocalDate dateBis,
		@NonNull QOrtDerImpfung odi,
		@NonNull QImpftermin termin,
		@NonNull QImpfslot slot,
		@NonNull QImpfung qImpfung,
		BooleanExpression dossierJoinExpression, BooleanTemplate booleanTemplate) {

		List<Tuple> countSelbstzahlendeOdiTupleList =
			getSelbstzahlendeRootQuery(dateVon, dateBis, odi, termin, slot, qImpfung)
				.innerJoin(impfdossier).on(dossierJoinExpression)
				.innerJoin(registrierung).on(registrierung.eq(impfdossier.registrierung))
				.where(booleanTemplate)
				.groupBy(odi)
				.fetch();

		return mapTupleListToMap(countSelbstzahlendeOdiTupleList);
	}

	private Map<OrtDerImpfung, Long> getSelbstzahlendeDossierimpfungen(
		@NonNull LocalDate dateVon, @NonNull LocalDate dateBis,
		@NonNull QOrtDerImpfung odi,
		@NonNull QImpftermin termin,
		@NonNull QImpfslot slot,
		@NonNull QImpfung qImpfung,
		BooleanTemplate booleanTemplate) {
		QImpfdossiereintrag eintrag = new QImpfdossiereintrag("eintrag");
		QImpfdossier dossier = new QImpfdossier("dossier");

		List<Tuple> countSelbstzahlendeOdiTupleList =
			getSelbstzahlendeRootQuery(dateVon, dateBis, odi, termin, slot, qImpfung)
				.leftJoin(eintrag).on(termin.eq(eintrag.impftermin))
				.leftJoin(dossier).on(eintrag.impfdossier.eq(dossier))
				.innerJoin(registrierung).on(registrierung.eq(dossier.registrierung))
				.where(booleanTemplate
					.and(dossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
				.groupBy(odi)
				.fetch();

		return mapTupleListToMap(countSelbstzahlendeOdiTupleList);
	}

	private Map<OrtDerImpfung, Long> mapTupleListToMap(List<Tuple> countSelbstzahlendeOdiTupleList) {
		return countSelbstzahlendeOdiTupleList.stream()
			.collect(Collectors.toMap(
				t -> t.get(1, OrtDerImpfung.class),
				t -> {
					Long count = t.get(0, Long.class);
					return count != null ? count : 0L;
				}));
	}

	@NonNull
	private SmartJPAQuery<Tuple> getSelbstzahlendeRootQuery(
		@NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis,
		@NonNull QOrtDerImpfung odi,
		@NonNull QImpftermin termin,
		@NonNull QImpfslot slot,
		@NonNull QImpfung qImpfung
	) {
		return db
			.select(qImpfung.count(), odi)
			.from(qImpfung)
			.innerJoin(termin).on(qImpfung.termin.eq(termin))
			.innerJoin(slot).on(termin.impfslot.eq(slot))
			.innerJoin(odi).on(slot.ortDerImpfung.eq(odi)
				.and(qImpfung.selbstzahlende.eq(Boolean.TRUE))
				.and(qImpfung.timestampImpfung.between(dateVon.atTime(LocalTime.MIN), dateBis.atTime(LocalTime.MAX))));

	}

	private void readBenutzerToResult(@NonNull List<? extends AbrechnungDataRow> result) {
		// Um OuterJoins zu den Benutzern zu vermeiden, wurde im obigen Query der Fachverantwortliche und der Organisationsverantwortliche
		// ausgeklammert. Diese muessen jetzt noch gelesen werden
		for (AbrechnungDataRow abrechnungDataRow : result) {
			if (abrechnungDataRow.getOrtDerImpfung() != null
				&& abrechnungDataRow.getOrtDerImpfung().getFachverantwortungbabKeyCloakId() != null) {
				QBenutzer qBenutzer = new QBenutzer("benutzer");
				Tuple fachverantw = db
					.select(qBenutzer.name, qBenutzer.vorname, qBenutzer.email, qBenutzer.glnNummer)
					.from(qBenutzer)
					.where(qBenutzer.id.eq(UUID.fromString(abrechnungDataRow.getOrtDerImpfung()
						.getFachverantwortungbabKeyCloakId())))
					.fetchFirst();
				if (fachverantw != null) {
					abrechnungDataRow.setFvName(fachverantw.get(0, String.class));
					abrechnungDataRow.setFvVorname(fachverantw.get(1, String.class));
					abrechnungDataRow.setFvMail(fachverantw.get(2, String.class));
					abrechnungDataRow.setFvGlnNummer(fachverantw.get(3, String.class));
				}
			}
			if (abrechnungDataRow.getOrtDerImpfung() != null
				&& abrechnungDataRow.getOrtDerImpfung().getOrganisationsverantwortungKeyCloakId() != null) {
				QBenutzer qBenutzer = new QBenutzer("benutzer");
				Tuple orgVerantw = db
					.select(qBenutzer.name, qBenutzer.vorname, qBenutzer.email)
					.from(qBenutzer)
					.where(qBenutzer.id.eq(UUID.fromString(abrechnungDataRow.getOrtDerImpfung()
						.getOrganisationsverantwortungKeyCloakId())))
					.fetchFirst();
				if (orgVerantw != null) {
					abrechnungDataRow.setOvName(orgVerantw.get(0, String.class));
					abrechnungDataRow.setOvVorname(orgVerantw.get(1, String.class));
					abrechnungDataRow.setOvMail(orgVerantw.get(2, String.class));
				}
			}
		}
	}

	@NonNull
	@TransactionConfiguration(timeout = Constants.DAY_IN_SECONDS)
	public List<AbrechnungZHDataRow> findOdiAbrechnungZH(
		@NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis,
		boolean abrechnungZHKind) {
		QOrtDerImpfung odi = QOrtDerImpfung.ortDerImpfung;
		QImpftermin termin = new QImpftermin("termin");
		QImpfslot slot = new QImpfslot("slot");
		QImpfung impfung = new QImpfung("impfung");
		QImpfdossier impfdossier = QImpfdossier.impfdossier;

		LOG.info("VACME-INFO: ABRECHNUNG-ZH gestartet {}", DateUtil.formatLocalTime(LocalDateTime.now()));
		String booleanTemplateStr = abrechnungZHKind ? "((datediff({0}, {1})/365.2425) >= 11)"
			: "((datediff({0}, {1})/365.2425) >= 65)";

		final BooleanTemplate booleanTemplate = Expressions.booleanTemplate(
			booleanTemplateStr,
			impfung.timestampImpfung,
			registrierung.geburtsdatum);

		//Erhalte Impfungen (nicht gruppiert)
		BooleanExpression impfdossierJoinExporessionT1 = impfdossier.buchung.impftermin1.eq(termin);
		List<AbrechnungZHDTO> result =
			runOdiAbrechnungZHRegistrierungsimpfungenQuery(
				dateVon, dateBis,
				termin, impfung, odi, slot,
				booleanTemplate, impfdossierJoinExporessionT1,
				"Impfungen 1");

		BooleanExpression impfdossierJoinExporessionT2 = impfdossier.buchung.impftermin2.eq(termin);
		result.addAll(runOdiAbrechnungZHRegistrierungsimpfungenQuery(
			dateVon, dateBis,
			termin, impfung, odi, slot,
			booleanTemplate, impfdossierJoinExporessionT2,
			"Impfungen 2"));

		result.addAll(runOdiAbrechnungZHDossierimpfungenQuery(
			dateVon, dateBis,
			termin, impfung, odi, slot,
			booleanTemplate, registrierung));

		StopWatch stopWatch = StopWatch.createStarted();
		// Die Totals sind jetzt pro Krankenkasse, sie muessen noch addiert werden
		List<AbrechnungZHDataRow> resultKrankenkassen = assembleKrankenkasseNormalZH(result);
		logAbrechnungKrankenkasseBeendetAndRestartWatch(stopWatch, resultKrankenkassen.size());

		// Um OuterJoins zu den Benutzern zu vermeiden, wurde im obigen Query der Fachverantwortliche und der Organisationsverantwortliche
		// ausgeklammert. Diese muessen jetzt noch gelesen werden
		readBenutzerToResult(resultKrankenkassen);
		logAbrechungBenutzerBeendetAndRestartWatch(stopWatch);

		stopWatch.stop();
		return resultKrankenkassen;
	}

	private List<AbrechnungZHDTO> runOdiAbrechnungZHRegistrierungsimpfungenQuery(
		@NonNull LocalDate dateVon, @NonNull LocalDate dateBis,
		QImpftermin qTermin, QImpfung qImpfung,
		QOrtDerImpfung qOdi, QImpfslot qSlot,
		BooleanTemplate booleanTemplate,
		Predicate impfdossierJoinExpression,
		String currentJoinName
	) {
		StopWatch stopWatch = StopWatch.createStarted();

		List<AbrechnungZHDTO> result =
			getAbrechnungZHRootQuery(dateVon, dateBis, qOdi, qTermin, qSlot, qImpfung, booleanTemplate)
				.innerJoin(impfdossier).on(impfdossierJoinExpression)
				.innerJoin(registrierung).on(impfdossier.registrierung.eq(registrierung))
				.fetch();

		logAbrechnungScriptBeendetAndRestartWatch(currentJoinName, stopWatch, result.size());
		stopWatch.stop();

		return result;
	}

	private List<AbrechnungZHDTO> runOdiAbrechnungZHDossierimpfungenQuery(
		@NonNull LocalDate dateVon, @NonNull LocalDate dateBis,
		QImpftermin qTermin, QImpfung qImpfung,
		QOrtDerImpfung qOdi, QImpfslot qSlot,
		BooleanTemplate booleanTemplate, QRegistrierung registrierung) {
		StopWatch stopWatch = StopWatch.createStarted();

		QImpfdossiereintrag eintrag = new QImpfdossiereintrag("eintrag");
		QImpfdossier dossier = new QImpfdossier("dossier");

		List<AbrechnungZHDTO> result =
			getAbrechnungZHRootQuery(dateVon, dateBis, qOdi, qTermin, qSlot, qImpfung, booleanTemplate)
				.leftJoin(eintrag).on(qTermin.eq(eintrag.impftermin))
				.leftJoin(dossier).on(eintrag.impfdossier.eq(dossier))
				.innerJoin(registrierung).on(registrierung.eq(dossier.registrierung))
				.where(dossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
				.fetch();

		logAbrechnungScriptBeendetAndRestartWatch("Impfungen N", stopWatch, result.size());

		stopWatch.stop();
		return result;
	}

	@NonNull
	private SmartJPAQuery<AbrechnungZHDTO> getAbrechnungZHRootQuery(
		@NonNull LocalDate dateVon,
		@NonNull LocalDate dateBis,
		@NonNull QOrtDerImpfung odi,
		@NonNull QImpftermin termin,
		@NonNull QImpfslot slot,
		@NonNull QImpfung impfung,
		@NonNull BooleanTemplate booleanTemplate
	) {
		final NumberTemplate<Long> eins = Expressions.numberTemplate(Long.class, "1");

		SmartJPAQuery<AbrechnungZHDTO> abrechnungZHRootQuery = db
			.select(new QAbrechnungZHDTO(
				booleanTemplate,
				QOrtDerImpfung.ortDerImpfung,
				registrierung.krankenkasse,
				eins, //KrankenkasseCount=1, Impfungen werden einzeln ausgegeben und nicht gruppiert
				impfung.impfstoff.id,
				impfung.grundimmunisierung,
				impfung.menge,
				impfung.selbstzahlende
			))
			.from(odi)
			.innerJoin(slot).on(slot.ortDerImpfung.eq(odi))
			.innerJoin(termin).on(termin.impfslot.eq(slot))
			.innerJoin(impfung).on(impfung.termin.eq(termin)
				.and(impfung.extern.isFalse())
				.and(impfung.timestampImpfung.between(dateVon.atTime(LocalTime.MIN), dateBis.atTime(LocalTime.MAX)))
				.and(impfung.kantonaleBerechtigung.in(KantonaleBerechtigung.editableForKanton())));
		return abrechnungZHRootQuery;
	}

	@NonNull
	private List<AbrechnungZHDataRow> assembleKrankenkasseNormalZH(@NonNull List<AbrechnungZHDTO> fetch) {
		Map<UUID, AbrechnungZHDataRow> myMap = new HashMap<>();
		for (AbrechnungZHDTO dto : fetch) {
			Objects.requireNonNull(dto.getOrtDerImpfung());
			final UUID key = dto.getOrtDerImpfung().getId();
			Objects.requireNonNull(key);
			if (!myMap.containsKey(key)) {
				myMap.put(key, new AbrechnungZHDataRow());
				myMap.get(key).setOrtDerImpfung(dto.getOrtDerImpfung());
			}
			final AbrechnungZHDataRow found = myMap.get(key);
			addToDataRowZH(dto, found);
		}
		return new ArrayList<>(myMap.values());
	}

	private void addToDataRowZH(@NonNull AbrechnungZHDTO dto, @NonNull AbrechnungZHDataRow row) {
		if (dto.getKrankenkasse() != null && dto.getKrankenkassenCount() != null) {

			//Anzahl aller Impfungen für ODI
			row.setTotalImpfungenCount(row.getTotalImpfungenCount() + dto.getKrankenkassenCount());

			//Gewichtete Dosis aller Impfungen für ODI
			double gewichtung = getGewichtungDosis(dto.getImpfstoffId(), dto.getGrundimmunisierung(), dto.getMenge());
			BigDecimal gewichteteDosis = new BigDecimal(dto.getKrankenkassenCount() * gewichtung);
			row.setTotalGewichtetDosen(row.getTotalGewichtetDosen().add(gewichteteDosis));

			//Impfung auf Selbstkosten
			if (dto.getSelbstzahlende() != null && dto.getSelbstzahlende()) {
				if (dto.isYounger()) {
					row.setSelbstzahlendeJuengerCount(row.getSelbstzahlendeJuengerCount()
						+ dto.getKrankenkassenCount());
				} else {
					row.setSelbstzahlendeCount(row.getSelbstzahlendeCount() + dto.getKrankenkassenCount());
				}
				//Falls Impfung auf Selbstkosten, wird sie bei Krankenkasse nicht mehr mitgezählt
				return;
			}

			switch (dto.getKrankenkasse()) {
			case ANDERE:
				if (dto.isYounger()) {
					row.setKrankenkasseAndereJuengerCount(row.getKrankenkasseAndereJuengerCount()
						+ dto.getKrankenkassenCount());
				} else {
					row.setKrankenkasseAndereCount(row.getKrankenkasseAndereCount() + dto.getKrankenkassenCount());
				}
				break;
			case AUSLAND:
				if (dto.isYounger()) {
					row.setKrankenkasseAuslandJuengerCount(row.getKrankenkasseAuslandJuengerCount()
						+ dto.getKrankenkassenCount());
				} else {
					row.setKrankenkasseAuslandCount(row.getKrankenkasseAuslandCount() + dto.getKrankenkassenCount());
				}
				break;
			case EDA:
				if (dto.isYounger()) {
					row.setKrankenkasseEdaJuengerCount(row.getKrankenkasseEdaJuengerCount()
						+ dto.getKrankenkassenCount());
				} else {
					row.setKrankenkasseEdaCount(row.getKrankenkasseEdaCount() + dto.getKrankenkassenCount());
				}
				break;
			default:
				if (dto.isYounger()) {
					row.setKrankenkasseOKPJuengerCount(row.getKrankenkasseOKPJuengerCount()
						+ dto.getKrankenkassenCount());
				} else {
					row.setKrankenkasseOKPCount(row.getKrankenkasseOKPCount() + dto.getKrankenkassenCount());
				}
				break;
			}
		}
	}

	private double getGewichtungDosis(
		@Nullable UUID impfstoffId,
		@Nullable Boolean grundimmunisierung,
		@Nullable Double menge
	) {
		double gewichtungNormal = 1;
		double gewichtungHalb = 0.5;

		if (!ObjectUtils.allNotNull(impfstoffId, grundimmunisierung, menge)) {
			return gewichtungNormal;
		}

		// Gewichtung 0.5 nur für Impfstoff Moderna,
		// welches nicht zur Grundimmunisierung gehört und Menge kleiner als 0.3
		@SuppressWarnings("ConstantConditions")
		boolean isModernaBooster =
			(MODERNA_UUID.equals(impfstoffId) || MODERNA_BIVALENT_UUID.equals(impfstoffId))
				&& Boolean.FALSE.equals(grundimmunisierung)
				&& menge < 0.3;

		return isModernaBooster ? gewichtungHalb : gewichtungNormal;
	}

	private BooleanExpression getDummyOdiNameMatchExpression(QOrtDerImpfung odi) {
		return odi.name.in(DUMMY_MIGRATION_ODI_ALTERSHEIM, DUMMY_MIGRATION_ODI_ANDERE, DUMMY_MIGRATION_ODI_APOTHEKE,
			DUMMY_MIGRATION_ODI_HAUSARZT, DUMMY_MIGRATION_ODI_MOBIL, DUMMY_MIGRATION_ODI_IMPFTENTRUM,
			DUMMY_MIGRATION_ODI_SPITAL);
	}

	private void restartStopwatch(StopWatch stopWatch) {
		stopWatch.reset();
		stopWatch.start();
	}

	private BooleanExpression getBooleanExpImpftermin1() {
		return impfdossier.buchung.impftermin1.eq(QImpftermin.impftermin);
	}

	private BooleanExpression getBooleanExpImpftermin2() {
		return impfdossier.buchung.impftermin2.eq(QImpftermin.impftermin);
	}

	private void logAbrechnungScriptBeendetAndRestartWatch(
		String currentJoinName,
		StopWatch stopWatch,
		int resultSize) {
		LOG.info(
			"VACME-INFO: ABRECHNUNG DB Skript beendet fuer {} {}, {} resultate (total)",
			currentJoinName,
			DateUtil.formatLocalTime(LocalDateTime.now()),
			resultSize);
		LOG.info("VACME-INFO: ABRECHNUNG DB Skript beendet fuer {} in {}ms", currentJoinName, stopWatch.getTime());
		restartStopwatch(stopWatch);
	}

	private void logAbrechnungKrankenkasseBeendetAndRestartWatch(StopWatch stopWatch, int resultSize) {
		LOG.info(
			"VACME-INFO: ABRECHNUNG Krankenkassen dazuaddiert {}, {} resultate",
			DateUtil.formatLocalTime(LocalDateTime.now()),
			resultSize);
		LOG.info("VACME-INFO: ABRECHNUNG Krankenkassen dazuaddiert in {}ms", stopWatch.getTime());
		restartStopwatch(stopWatch);
	}

	private void logAbrechungBenutzerBeendetAndRestartWatch(StopWatch stopWatch) {
		LOG.info("VACME-INFO: ABRECHNUNG Benutzer gelesen {}", DateUtil.formatLocalTime(LocalDateTime.now()));
		LOG.info("VACME-INFO: ABRECHNUNG Benutzer gelesen in {}", stopWatch.getTime());
		LOG.info("VACME-INFO: ABRECHNUNG beendet.");
		restartStopwatch(stopWatch);
	}
}
