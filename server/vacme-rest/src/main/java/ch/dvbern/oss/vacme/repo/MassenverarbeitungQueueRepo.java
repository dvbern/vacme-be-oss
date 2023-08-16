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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueue;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueueStatus;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.MassenverarbeitungQueueTyp;
import ch.dvbern.oss.vacme.entities.massenverarbeitung.QMassenverarbeitungQueue;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.smartdb.Db;
import io.agroal.api.AgroalDataSource;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@RequestScoped
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MassenverarbeitungQueueRepo {

	@Inject
	AgroalDataSource defaultDataSource;

	private final Db db;

	@Transactional(TxType.REQUIRES_NEW)
	@TransactionConfiguration(timeout = Constants.DAY_IN_SECONDS)
	public void addImpfungenToExternalizeQueue(@NonNull List<ID<Impfung>> impfungIdsToExternalize) {
		StopWatch started = StopWatch.createStarted();
		List<MassenverarbeitungQueue> itemsToInsert =
			transformImpfungIdstoQueueItemsForExternalization(impfungIdsToExternalize);
		LOG.info(
			"VACME-MASSENVERARBEITUNG: Starting to insert {} MassenverarbeitungQueue Items for Externalization ...",
			impfungIdsToExternalize.size());
		//		int i = createQueueItemsJPA(impfungIdsToExternalize);
		int i = createQueueItemsJDBC(itemsToInsert);
		LOG.info("VACME-MASSENVERARBEITUNG: Added {} queue items in  {} ms", i,
			started.getTime(TimeUnit.MILLISECONDS));
	}

	private int createQueueItemsJPA(@NonNull List<ID<Impfung>> impfungIdsToExternalize) {
		List<MassenverarbeitungQueue> itemsToStore =
			transformImpfungIdstoQueueItemsForExternalization(impfungIdsToExternalize);
		for (MassenverarbeitungQueue item : itemsToStore) {
			db.persist(item);
		}
		db.flush();
		return itemsToStore.size();
	}

	@NonNull
	private List<MassenverarbeitungQueue> transformImpfungIdstoQueueItemsForExternalization(@NonNull List<ID<Impfung>> impfungIdsToExternalize) {
		List<MassenverarbeitungQueue> itemsToStore =
			impfungIdsToExternalize.stream()
				.map(MassenverarbeitungQueue::forExternalization)
				.collect(Collectors.toList());
		return itemsToStore;
	}

	private int createQueueItemsJDBC(@NonNull List<MassenverarbeitungQueue> itemsToInsert) {
		int batchSize = 500;

		String query = "INSERT INTO %s (id, timestampErstellt, timestampMutiert, userErstellt, userMutiert, version, "
			+ "errorCount, lastError, impfungId, odiId, registrierungNummer, status, typ, impfdossierId) "
			+ "VALUES (NEXTVAL(hibernate_sequence), now(), now(), '%s', '%s', 1, 0, NULL, ?, ?, ?, ?, ?, ?);";
		String sql = String.format(query,
			MassenverarbeitungQueue.class.getSimpleName(), DBConst.SYSTEM_ADMIN_ID, DBConst.SYSTEM_ADMIN_ID);

		int counter = 0;
		try (Connection connection = defaultDataSource.getConnection();
			PreparedStatement statement = connection.prepareStatement(sql)
		) {

			for (MassenverarbeitungQueue item : itemsToInsert) {
				statement.clearParameters();
				statement.setString(1, item.getImpfungId());
				statement.setString(2, item.getOdiId());
				statement.setString(3, item.getRegistrierungNummer());
				statement.setString(4, item.getStatus().name());
				statement.setString(5, item.getTyp().name());
				statement.setString(6, item.getImpfdossierId() == null ? null : item.getImpfdossierId().toString());
				statement.addBatch();
				if ((counter + 1) % batchSize == 0 || (counter + 1) == itemsToInsert.size()) {
					statement.executeBatch();
					statement.clearBatch();
					LOG.info(
						"VACME-MASSENVERARBEITUNG: Triggered batch-insert after processing {}/{}",
						counter,
						itemsToInsert.size());
				}
				counter++;
			}
		} catch (SQLException e) {
			String msg = "VACME-MASSENVERARBEITUNG: Fehler beim Inserten der MasesnverarbeitungQueueItems";
			LOG.error(msg);
			throw new AppFailureException(msg, e);
		}
		return counter;
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void updateQueueItemNewTransaction(@NonNull MassenverarbeitungQueue queueItem) {
		db.merge(queueItem);
	}

	public void addImpfungenToMoveToOdiQueue(@NonNull List<Pair<ID<Impfung>, ID<OrtDerImpfung>>> impfungenToMoveToOdiList) {
		StopWatch started = StopWatch.createStarted();
		LOG.info(
			"VACME-MASSENVERARBEITUNG: Starting to insert {} MassenverarbeitungQueue Items for OdiMove ...",
			impfungenToMoveToOdiList.size());
		//		int i = createQueueItemsJPA(impfungIdsToExternalize);
		int i = createQueueItemsJDBC(transformImpfungIdstoQueueItemsForOdiMove(impfungenToMoveToOdiList));
		LOG.info("VACME-MASSENVERARBEITUNG: Added {} queue items in  {} ms", i,
			started.getTime(TimeUnit.MILLISECONDS));
	}

	public void addRegistrierungenToDeleteQueue(List<String> registrierungsnummern) {
		StopWatch started = StopWatch.createStarted();
		LOG.info(
			"VACME-MASSENVERARBEITUNG: Starting to insert {} MassenverarbeitungQueue Items for RegistrationDelete ...",
			registrierungsnummern.size());
		int i =
			createQueueItemsJDBC(transformRegistrierungsnummernToQueueItemsForRegistrierungDelete(registrierungsnummern));
		LOG.info("VACME-MASSENVERARBEITUNG: Added {} queue items in  {} ms", i,
			started.getTime(TimeUnit.MILLISECONDS));
	}

	public void addOdiToLatLngQueue(List<ID<OrtDerImpfung>> odiIds) {
		StopWatch started = StopWatch.createStarted();
		LOG.info("VACME-MASSENVERARBEITUNG: Starting to insert {} MassenverarbeitungQueue Items for OdiLatLng Calculation ...",
			odiIds.size());
		int i = createQueueItemsJDBC(transformOdiIdsToQueueItemsForLatLngCalculation(odiIds));
		LOG.info("VACME-MASSENVERARBEITUNG: Added {} queue items in  {} ms", i,
			started.getTime(TimeUnit.MILLISECONDS));
	}

	public void addImpfungenToLoeschenQueue(@NonNull List<ID<Impfung>> impfungIdsToLoeschen) {
		StopWatch started = StopWatch.createStarted();
		List<MassenverarbeitungQueue> itemsToDelete =
			transformImpfungIdsToQueueItemsForImpfungenLoeschen(impfungIdsToLoeschen);
		LOG.info(
			"VACME-MASSENVERARBEITUNG: Starting to insert {} MassenverarbeitungQueue Items for ImpfungenLoeschen ...",
			impfungIdsToLoeschen.size());
		int i = createQueueItemsJDBC(itemsToDelete);
		LOG.info("VACME-MASSENVERARBEITUNG: Added {} queue items in  {} ms", i,
			started.getTime(TimeUnit.MILLISECONDS));
	}

	public void addImpfgruppeToFreigebenQueue(List<UUID> impfdossiersIds) {
		StopWatch started = StopWatch.createStarted();
		LOG.info("VACME-MASSENVERARBEITUNG: Starting to insert {} MassenverarbeitungQueue Items for ImpfgruppeFreigeben ...",
			impfdossiersIds.size());
		int i = createQueueItemsJDBC(transformImpfgruppeIdsToQueueItemsForImpfgruppeFreigegeben(impfdossiersIds));
		LOG.info("VACME-MASSENVERARBEITUNG: Added {} queue items in  {} ms", i,
			started.getTime(TimeUnit.MILLISECONDS));
	}

	@NonNull
	private List<MassenverarbeitungQueue> transformOdiIdsToQueueItemsForLatLngCalculation(@NonNull List<ID<OrtDerImpfung>> odiIds) {
		return odiIds.stream().map(MassenverarbeitungQueue::forOdiLatLngCalculation).collect(Collectors.toList());
	}

	@NonNull
	private List<MassenverarbeitungQueue> transformRegistrierungsnummernToQueueItemsForRegistrierungDelete(@NonNull List<String> registrierungsnummern) {
		return registrierungsnummern.stream()
			.map(MassenverarbeitungQueue::forRegistrierungDelete)
			.collect(Collectors.toList());
	}

	@NonNull
	private List<MassenverarbeitungQueue> transformImpfungIdsToQueueItemsForImpfungenLoeschen(@NonNull List<ID<Impfung>> impfungIdsToLoeschen) {
		List<MassenverarbeitungQueue> itemsToStore =
			impfungIdsToLoeschen.stream()
				.map(MassenverarbeitungQueue::forImpfungenLoeschen)
				.collect(Collectors.toList());
		return itemsToStore;
	}

	@NonNull
	private List<MassenverarbeitungQueue> transformImpfungIdstoQueueItemsForOdiMove(
		@NonNull List<Pair<ID<Impfung>,
			ID<OrtDerImpfung>>> impfungenToMoveToOdiList) {
		return impfungenToMoveToOdiList.stream()
			.map(impfungToNewOdiPair -> MassenverarbeitungQueue.forImpfungOdiMove(
				impfungToNewOdiPair.getLeft(),
				impfungToNewOdiPair.getRight()))
			.collect(Collectors.toList());
	}

	@NonNull
	public List<MassenverarbeitungQueue> transformImpfgruppeIdsToQueueItemsForImpfgruppeFreigegeben(@NonNull List<UUID> impfdossiersIds) {
		return impfdossiersIds.stream()
			.map(MassenverarbeitungQueue::forImpfgruppeFreigegeben)
			.collect(Collectors.toList());
	}

	@NonNull
	public List<MassenverarbeitungQueue> findMassenverarbeitungQueueItemsToProcess(
		long massenverarbeitungQueueProcessingJobBatchSize,
		@NonNull MassenverarbeitungQueueTyp typ
	) {
		return db.select(QMassenverarbeitungQueue.massenverarbeitungQueue)
			.from(QMassenverarbeitungQueue.massenverarbeitungQueue)
			.where(QMassenverarbeitungQueue.massenverarbeitungQueue.typ.eq(typ)
				.and(QMassenverarbeitungQueue.massenverarbeitungQueue.status.in(
					MassenverarbeitungQueueStatus.NEW,
					MassenverarbeitungQueueStatus.FAILED_RETRY)))
			.orderBy(QMassenverarbeitungQueue.massenverarbeitungQueue.timestampErstellt.asc()) // Die aeltesten zuerst
			.limit(massenverarbeitungQueueProcessingJobBatchSize)
			.fetch();
	}
}
