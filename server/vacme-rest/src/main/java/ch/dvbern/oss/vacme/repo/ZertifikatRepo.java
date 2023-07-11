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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.QImpfung;
import ch.dvbern.oss.vacme.entities.registration.QRegistrierung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.QZertifikatCreationDTO;
import ch.dvbern.oss.vacme.entities.types.ZertifikatCreationDTO;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.entities.zertifikat.QZertifikat;
import ch.dvbern.oss.vacme.entities.zertifikat.QZertifikatFile;
import ch.dvbern.oss.vacme.entities.zertifikat.QZertifikatQueue;
import ch.dvbern.oss.vacme.entities.zertifikat.QZertifizierungsToken;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifikatFile;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifikatQueue;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifikatQueueStatus;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifikatQueueTyp;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifizierungsToken;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.smartdb.Db;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.quarkus.logging.Log;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import static ch.dvbern.oss.vacme.entities.registration.QRegistrierung.registrierung;

@RequestScoped
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ZertifikatRepo {

	private final Db db;
	private final ObjectMapper mapper;
	private final VacmeSettingsService vacmeSettingsService;


	/**
	 * Speichert ein Zertifikat in der DB
	 */
	public void create(@NonNull Zertifikat zertifikat) {
		db.persist(zertifikat);
	}

	@Transactional(TxType.REQUIRES_NEW) // token ist sonst nicht verfuegbar in anderer transaktion
	public void createToken(@NonNull ZertifizierungsToken token) {
		extractAndStoreGueltigkeitForToken(token);
		// Sicherstellen, dass nicht bereits ein laenger gueltiges Token vorhanden ist
		if (getLongestGueltigkeitOfAllTokens().isPresent()) {
			LocalDateTime latestExpiry = getLongestGueltigkeitOfAllTokens().get();
			if (latestExpiry.isAfter(token.getGueltigkeit())) {
				// Wir brechen einfach ab
				String msg = String.format("Token with later expiry (%s) already present",
					DateUtil.formatDate(latestExpiry));
				Log.warn(msg);
				throw AppValidationMessage.ILLEGAL_STATE.create(msg);
			}
		}
		db.persist(token);
		db.flush();
	}

	@NonNull
	private Optional<LocalDateTime> getLongestGueltigkeitOfAllTokens() {
		return db
			.select(QZertifizierungsToken.zertifizierungsToken.gueltigkeit.max())
			.from(QZertifizierungsToken.zertifizierungsToken)
			.fetchOne();
	}

	public void deleteToken(@NonNull ID<ZertifizierungsToken> tokenId) {
		db.remove(tokenId);
	}

	/**
	 * Speichert ein ZertifikatQueue in der DB
	 */
	public void createZertifikatQueue(@NonNull ZertifikatQueue zertifikatQueue) {
		db.persist(zertifikatQueue);
	}

	void extractAndStoreGueltigkeitForToken(@NotNull ZertifizierungsToken token) {
		try {
			String rawToken = token.getToken();
			String[] chunks = rawToken.split("\\.");
			Base64.Decoder decoder = Base64.getDecoder();

			String payload = new String(decoder.decode(chunks[1]));
			JsonNode jsonNodeRoot = mapper.readTree(payload);
			JsonNode jsonNodeExp = jsonNodeRoot.get("exp");
			long expEpoch = jsonNodeExp.asLong();

			Instant instant = Instant.ofEpochSecond(expEpoch);
			ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
			ZoneId zoneId = ZoneId.of( "Europe/Zurich" );
			ZonedDateTime zonedInstant = ZonedDateTime.ofInstant(zonedDateTime.toInstant(), zoneId);
			LocalDateTime localDateTime = zonedInstant.toLocalDateTime();
			token.setGueltigkeit(localDateTime);

		} catch (RuntimeException | JsonProcessingException e) {
			throw new AppFailureException("Could not extract gueltigkeit from Token", e);
		}
	}

	/**
	 * Aktualisiert ein Zertifikat in der DB. Achtung: Nur das Feld "revoked" kann aktualisiert werden
	 */
	public Zertifikat update(@NonNull Zertifikat zertifikat) {
		return db.merge(zertifikat);
	}

	@NonNull
	public Optional<Zertifikat> getZertifikatById(@NonNull ID<Zertifikat> id) {
		return db.get(id);
	}

	@NonNull
	public Optional<ZertifikatQueue> getZertifikaQueueById(@NonNull ID<ZertifikatQueue> id) {
		return db.get(id);
	}

	/**
	 * Sucht das neueste Zertifikat fuer eine Registrierung. Dieses kann auch revoked sein
	 */
	@NonNull
	public Optional<Zertifikat> getNewestZertifikatRegardlessOfRevocation(@NonNull Registrierung registrierung) {
		var result = db.selectFrom(QZertifikat.zertifikat)
			.where(QZertifikat.zertifikat.registrierung.eq(registrierung))
			.orderBy(QZertifikat.zertifikat.timestampErstellt.desc())
			.fetchFirst();
		return Optional.ofNullable(result);
	}

	@NonNull
	public List<Zertifikat> getAllZertifikateRegardlessOfRevocation(@NonNull Registrierung registrierung) {
		var result = db.selectFrom(QZertifikat.zertifikat)
			.where(QZertifikat.zertifikat.registrierung.eq(registrierung))
			.orderBy(QZertifikat.zertifikat.timestampErstellt.desc());
		return result.fetch();
	}

	/**
	 * Sucht das neueste Zertifikat fuer eine Registrierung, welches nicht revoked ist
	 */
	@NonNull
	public Optional<Zertifikat> getNewestNonRevokedZertifikat(@NonNull Registrierung registrierung) {
		var result =
			db.selectFrom(QZertifikat.zertifikat)
				.leftJoin(QImpfung.impfung).on(QZertifikat.zertifikat.impfung.eq(QImpfung.impfung))
			.where(QZertifikat.zertifikat.registrierung.eq(registrierung)
			.and(QZertifikat.zertifikat.revoked.isFalse()))
			.orderBy(QImpfung.impfung.timestampImpfung.desc(), QZertifikat.zertifikat.timestampErstellt.desc())
			.fetchFirst();
		return Optional.ofNullable(result);
	}

	/**
	 * Gibt alle nicht revoked Zertifikate fuer eine Registrierung zurueck.
	 * Es wird nicht mit der Queue abgeglichen!
	 */
	@NonNull
	public List<Zertifikat> getAllNonRevokedZertifikate(@NonNull Registrierung registrierung) {
		return db.selectFrom(QZertifikat.zertifikat)
			.where(QZertifikat.zertifikat.registrierung.eq(registrierung)
				.and(QZertifikat.zertifikat.revoked.isFalse()))
			.fetch();
	}

	/**
	 * Sucht das neueste Zertifikat-PDF fuer eine Registrierung. Dieses kann auch revoked sein
	 */
	@NonNull
	public Optional<ZertifikatFile> getZertifikatPdf(@NonNull Zertifikat zertifikat) {
		QZertifikatFile aliasZertifikatFile = new QZertifikatFile("zertifikatFile");
		var result = db
			.select(QZertifikatFile.zertifikatFile)
			.from(QZertifikat.zertifikat)
			.innerJoin(QZertifikat.zertifikat.zertifikatPdf, aliasZertifikatFile)
			.where(QZertifikat.zertifikat.eq(zertifikat))
			.orderBy(QZertifikatFile.zertifikatFile.timestampErstellt.desc())
			.fetchFirst();
		return Optional.ofNullable(result);
	}

	/**
	 * Sucht das neueste Zertifikat-PDF fuer eine Registrierung. Dieses darf nicht revoked sein
	 */
	@NonNull
	public Optional<String> findZertifikatUCVI(@NonNull String registrierungsNummer) {
		var result = db.select(QZertifikat.zertifikat.uvci)
			.from(QZertifikat.zertifikat)
			.where(
				QZertifikat.zertifikat.registrierung.registrierungsnummer.eq(registrierungsNummer)
					.and(QZertifikat.zertifikat.revoked.eq(Boolean.FALSE))
			)
			.orderBy(QZertifikat.zertifikat.timestampErstellt.desc())
			.fetchFirst();
		return Optional.ofNullable(result);
	}

	/**
	 * Sucht den neuesten Zertifikat-QR-Code fuer eine Registrierung. Dieses kann auch revoked sein
	 */
	@NonNull
	public Optional<ZertifikatFile> getZertifikatQrCode(@NonNull Zertifikat zertifikat) {
		QZertifikatFile aliasZertifikatFile = new QZertifikatFile("zertifikatFile");
		var result = db
			.select(QZertifikatFile.zertifikatFile)
			.from(QZertifikat.zertifikat)
			.innerJoin(QZertifikat.zertifikat.zertifikatQrCode, aliasZertifikatFile)
			.where(QZertifikat.zertifikat.eq(zertifikat))
			.orderBy(QZertifikatFile.zertifikatFile.timestampErstellt.desc())
			.fetchFirst();
		return Optional.ofNullable(result);
	}

	/**
	 * Gibt das neueste, noch gueltige Zertifizierungstoken zurueck.
	 */
	@NonNull
	public Optional<ZertifizierungsToken> getZertifizierungstoken() {
		var result = db.select(QZertifizierungsToken.zertifizierungsToken)
			.from(QZertifizierungsToken.zertifizierungsToken)
			.where(QZertifizierungsToken.zertifizierungsToken.gueltigkeit.after(LocalDateTime.now()))
			.orderBy(QZertifizierungsToken.zertifizierungsToken.timestampErstellt.desc())
			.fetchFirst();
		return Optional.ofNullable(result);
	}

	public boolean hasValidToken() {
		return getZertifizierungstoken().isPresent();
	}


	/**
	 * @return Gibt eine Liste von ZertifikatCreationDTO zurueck fuer die folgendes gilt:
	 * - generateZertifikat ist true
	 * - Eingangsart war war in der Liste der uebergebenen arten
	 * - nicht anonymisiert
	 * - abgleichElektronischer Impfausweis ist true
	 *
	 */
	@NonNull
	public List<ZertifikatCreationDTO> findImpfungenForZertifikatsGeneration(List<RegistrierungsEingang> eingangs, long covidapiBatchSize) {
		LocalDateTime minimumWaitTime = LocalDateTime.now().minusMinutes(vacmeSettingsService.getCovidCertMinimumWaittimeMinutes());

		long remainingBatchSize = covidapiBatchSize;
		// Grundimpfungen
		final List<ZertifikatCreationDTO> impfungen1 = baseImpfungen1ForZertifikatsGeneration(eingangs, minimumWaitTime, remainingBatchSize);
		remainingBatchSize = remainingBatchSize - impfungen1.size();

		List<ZertifikatCreationDTO> impfungen2 = Collections.emptyList();
		if (remainingBatchSize > 0) { // haben wir noch Platz im batchjob?
			impfungen2 = baseImpfungen2ForZertifikatsGeneration(eingangs, minimumWaitTime, remainingBatchSize);
			remainingBatchSize = remainingBatchSize - impfungen2.size();
		}

		List<ZertifikatCreationDTO> impfungenBooster = Collections.emptyList();
		if (remainingBatchSize > 0) {
			impfungenBooster = baseImpfungenBoosterForZertifikatsGeneration(eingangs, minimumWaitTime, remainingBatchSize);
		}

		List<ZertifikatCreationDTO> result = new ArrayList<>();
		result.addAll(impfungen1);
		result.addAll(impfungen2);
		result.addAll(impfungenBooster);
		return result;
	}

	@NonNull
	public List<ZertifikatCreationDTO> findAllMigrationImpfungenForZertifikatsGenerationPostRegexbased(String migrationRegex, long availableMigrationBatchSize) {
		LocalDateTime minimumWaitTime = LocalDateTime.now().minusMinutes(vacmeSettingsService.getCovidCertMinimumWaittimeMinutes());
		long remainingBatchSize = availableMigrationBatchSize;

		String queryImpfung1 =
			"select R.registrierungsnummer, I.id as impfungUUID "
				+ "from Impfung I "
				+ "inner join Impftermin T ON I.termin_id = T.id "
				+ "inner join Impfdossier D ON T.id = D.impftermin1_id "
				+ "inner join Registrierung R on D.registrierung_id = R.id "
				+ "where I.generateZertifikat = true "
				+ "and R.registrierungsEingang in ('DATA_MIGRATION') "
				+ "and R.anonymisiert = false "
				+ "and R.abgleichElektronischerImpfausweis = true "
				+ "and I.timestampMutiert < ?1 "
				+ "and R.timestampMutiert < ?2 "
				+ "and R.externalId REGEXP (?3) "
				+ "ORDER BY D.timestampZuletztAbgeschlossen ASC "
				+ "LIMIT ?4 ;";
		final List<ZertifikatCreationDTO> impfungen1 =
			findAllMigrationImpfungenForZertifikatsGenerationPostRegexbased(queryImpfung1, migrationRegex, minimumWaitTime, remainingBatchSize);
		remainingBatchSize = availableMigrationBatchSize - impfungen1.size();

		List<ZertifikatCreationDTO> impfungen2 = Collections.emptyList();
		if(remainingBatchSize > 0) {
			String queryImpfung2 =
				"select R.registrierungsnummer, I.id as impfungUUID "
					+ "from Impfung I "
					+ "inner join Impftermin T ON I.termin_id = T.id "
					+ "inner join Impfdossier D ON T.id = D.impftermin2_id "
					+ "inner join Registrierung R on D.registrierung_id = R.id "
					+ "where I.generateZertifikat = true "
					+ "and R.registrierungsEingang in ('DATA_MIGRATION') "
					+ "and R.anonymisiert = false "
					+ "and R.abgleichElektronischerImpfausweis = true "
					+ "and I.timestampMutiert < ?1 "
					+ "and R.timestampMutiert < ?2 "
					+ "and R.externalId REGEXP (?3) "
					+ "ORDER BY D.timestampZuletztAbgeschlossen ASC "
					+ "LIMIT ?4 ;";

			impfungen2 = findAllMigrationImpfungenForZertifikatsGenerationPostRegexbased(queryImpfung2, migrationRegex, minimumWaitTime, remainingBatchSize);
			remainingBatchSize = availableMigrationBatchSize - impfungen2.size();
		}

		List<ZertifikatCreationDTO> impfungenBooster = Collections.emptyList();
		if(remainingBatchSize > 0) {
			String queryBooster =
				"select R.registrierungsnummer, I.id as impfungUUID "
					+ "from Impfung I "
					+ "inner join Impftermin T ON I.termin_id = T.id "
					+ "inner join Impfdossiereintrag E ON T.id = E.impftermin_id "
					+ "inner join Impfdossier D on E.impfdossier_id = D.id "
					+ "inner join Registrierung R ON D.registrierung_id = R.id "
					+ "where I.generateZertifikat = true "
					+ "and R.registrierungsEingang in ('DATA_MIGRATION') "
					+ "and R.anonymisiert = false "
					+ "and R.abgleichElektronischerImpfausweis = true "
					+ "and I.timestampMutiert < ?1 "
					+ "and R.timestampMutiert < ?2 "
					+ "and R.externalId REGEXP (?3) "
					+ "and D.krankheitIdentifier = 'COVID' "
					+ "ORDER BY D.timestampZuletztAbgeschlossen ASC "
					+ "LIMIT ?4 ;";

			impfungenBooster = findAllMigrationImpfungenForZertifikatsGenerationPostRegexbased(queryBooster, migrationRegex, minimumWaitTime, remainingBatchSize);
		}
		List<ZertifikatCreationDTO> result = new ArrayList<>();
		result.addAll(impfungen1);
		result.addAll(impfungen2);
		result.addAll(impfungenBooster);
		return result;
	}

	@NonNull
	private List<ZertifikatCreationDTO> findAllMigrationImpfungenForZertifikatsGenerationPostRegexbased(
		@NonNull String query,
		@NonNull String migrationRegex,
		@NonNull LocalDateTime minimumWaitTime,
		long availableMigrationBatchSize
	) {
		Query nativeQuery = db.getEntityManager().createNativeQuery(query, Constants.REGISTTIERUNGSNUMMER_IMPFUNGID_DTO_MAPPING);
		nativeQuery.setParameter(1, minimumWaitTime);
		nativeQuery.setParameter(2, minimumWaitTime);
		nativeQuery.setParameter(3, migrationRegex);
		nativeQuery.setParameter(4, availableMigrationBatchSize);
		return (List<ZertifikatCreationDTO>) nativeQuery.getResultList();
	}

	@NonNull
	private List<ZertifikatCreationDTO> baseImpfungen1ForZertifikatsGeneration(
		@NonNull List<RegistrierungsEingang> eingangstypen,
		@NonNull LocalDateTime minimumWaitTime,
		long covidapiBatchSize
	) {
		return db
			.select(new QZertifikatCreationDTO(
				registrierung.registrierungsnummer,
				QImpfung.impfung.id,
				QImpfdossier.impfdossier.timestampZuletztAbgeschlossen))
			.from(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.eq(QImpfung.impfung.termin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin1))
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.distinct()
			.where(predicateImpfungenForZertifikatGeneration(eingangstypen, minimumWaitTime))
			.orderBy(QImpfdossier.impfdossier.timestampZuletztAbgeschlossen.asc()) // Die aeltesten zuerst
			.limit(covidapiBatchSize)
			.fetch();
	}

	@NonNull
	private List<ZertifikatCreationDTO> baseImpfungen2ForZertifikatsGeneration(
		@NonNull List<RegistrierungsEingang> eingangstypen,
		@NonNull LocalDateTime minimumWaitTime,
		long covidapiBatchSize
	) {
		return db
			.select(new QZertifikatCreationDTO(
				registrierung.registrierungsnummer,
				QImpfung.impfung.id,
				QImpfdossier.impfdossier.timestampZuletztAbgeschlossen))
			.from(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.eq(QImpfung.impfung.termin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin2))
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.distinct()
			.where(predicateImpfungenForZertifikatGeneration(eingangstypen, minimumWaitTime))
			.orderBy(QImpfdossier.impfdossier.timestampZuletztAbgeschlossen.asc()) // Die aeltesten zuerst
			.limit(covidapiBatchSize)
			.fetch();
	}

	@NonNull
	private List<ZertifikatCreationDTO> baseImpfungenBoosterForZertifikatsGeneration(
		@NonNull List<RegistrierungsEingang> eingangstypen,
		@NonNull LocalDateTime minimumWaitTime,
		long covidapiBatchSize
	) {
		return db
			.select(new QZertifikatCreationDTO(
				registrierung.registrierungsnummer,
				QImpfung.impfung.id,
				QImpfdossier.impfdossier.timestampZuletztAbgeschlossen))
			.from(QImpfung.impfung)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.eq(QImpfung.impfung.termin))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpfdossiereintrag.impfdossiereintrag.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.eq(QImpfdossiereintrag.impfdossiereintrag.impfdossier))
			.innerJoin(QRegistrierung.registrierung).on(registrierung.eq(QImpfdossier.impfdossier.registrierung))
			.distinct()
			.where(predicateImpfungenForZertifikatGeneration(eingangstypen, minimumWaitTime)
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID)))
			.orderBy(QImpfdossier.impfdossier.timestampZuletztAbgeschlossen.asc())  // Die aeltesten zuerst
			.limit(covidapiBatchSize)
			.fetch();
	}

	@NonNull
	private BooleanExpression predicateImpfungenForZertifikatGeneration(
		@NonNull List<RegistrierungsEingang> eingangstypen,
		@NonNull LocalDateTime minimumWaitTime
	) {
		BooleanExpression predicate = QImpfung.impfung.generateZertifikat.isTrue()
			.and(registrierung.anonymisiert.eq(Boolean.FALSE))
			.and(registrierung.abgleichElektronischerImpfausweis.eq(Boolean.TRUE))
			.and(registrierung.timestampMutiert.before(minimumWaitTime))
			.and(QImpfung.impfung.timestampMutiert.before(minimumWaitTime));
		if (eingangstypen.size() == 1) {
			predicate = predicate.and(registrierung.registrierungsEingang.eq(eingangstypen.get(0)));
		} else{
			predicate = predicate.and(registrierung.registrierungsEingang.in(eingangstypen));
		}
		return predicate;
	}

	@NonNull
	public List<UUID> findZertifikateForZertifikatsRevocationOnline(long batchsize) {
		return findZertifikateForZertifikatsRevocation(batchsize, 0, ZertifikatQueueTyp.REVOCATION_ONLINE);
	}

	@NonNull
	public List<UUID> findZertifikateForZertifikatsRevocationPost(long batchsize) {
		return findZertifikateForZertifikatsRevocation(batchsize,
			vacmeSettingsService.getCovidCertMinimumWaittimeRevocationPostHours(), ZertifikatQueueTyp.REVOCATION_POST);
	}

	@NonNull
	private List<UUID> findZertifikateForZertifikatsRevocation(long batchsize, int minimumWaittimeRevocationHours, @NonNull ZertifikatQueueTyp typ) {

		LocalDateTime minimumWaitTime = LocalDateTime.now().minusHours(minimumWaittimeRevocationHours);
		// Zuerst werden alle Revozierungen mit Prio-Flag abgehandelt, OHNE waitTime:
		final List<UUID> revozierungen = db.select(QZertifikatQueue.zertifikatQueue.id)
			.from(QZertifikatQueue.zertifikatQueue)
			.where(QZertifikatQueue.zertifikatQueue.typ.eq(typ)
				.and(QZertifikatQueue.zertifikatQueue.zertifikatToRevoke.isNotNull())
				.and(QZertifikatQueue.zertifikatQueue.status.in(ZertifikatQueueStatus.NEW,
					ZertifikatQueueStatus.FAILED_RETRY))
				.and(QZertifikatQueue.zertifikatQueue.prioritaet.isTrue().or(QZertifikatQueue.zertifikatQueue.timestampErstellt.before(minimumWaitTime))))

			.orderBy(QZertifikatQueue.zertifikatQueue.prioritaet.desc(),
				QZertifikatQueue.zertifikatQueue.timestampErstellt.asc()) // Die aeltesten zuerst
			.limit(batchsize)
			.fetch();

		return revozierungen;
	}

	public void updateZertifikatQueue(@NonNull ZertifikatQueue queueItem) {
		db.merge(queueItem);
	}

	/**
	 * Gibt alle abgelaufenen Token zurueck
	 */
	@NonNull
	public List<ZertifizierungsToken> getAbgelaufeneZertifizierungstokens() {
		return db.select(QZertifizierungsToken.zertifizierungsToken)
			.from(QZertifizierungsToken.zertifizierungsToken)
			.where(QZertifizierungsToken.zertifizierungsToken.gueltigkeit.before(LocalDateTime.now()))
			.fetch();
	}

	/**
	 * Gibt alle Token zurueck
	 */
	@NonNull
	public List<ZertifizierungsToken> getAllZertifizierungstokens() {
		return db.findAll(QZertifizierungsToken.zertifizierungsToken);
	}

	@NonNull
	public List<Zertifikat> findNonRevokedZertifikatForImpfung(@NonNull Impfung impfung) {
		return db
			.selectFrom(QZertifikat.zertifikat)
			.where(QZertifikat.zertifikat.impfung.eq(impfung).and(QZertifikat.zertifikat.revoked.isFalse()))
			.fetch();
	}

	@NonNull
	public List<Zertifikat> findZertifikatForImpfung(@NonNull Impfung impfung) {
		return db
			.selectFrom(QZertifikat.zertifikat)
			.where(QZertifikat.zertifikat.impfung.eq(impfung))
			.fetch();
	}
}
