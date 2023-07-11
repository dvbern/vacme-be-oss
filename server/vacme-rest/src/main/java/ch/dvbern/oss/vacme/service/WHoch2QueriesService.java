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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.smartdb.Db;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Transactional
@ApplicationScoped
@Slf4j
public class WHoch2QueriesService {

	private static final Pattern OUTFILE_MATCH_PATTERN = Pattern.compile("into outfile.* from",
		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern OUTFILE_MATCH_PATTERN_ALTERNATIVE = Pattern.compile("into outfile.* escaped by ''",
		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	public final String wHoch2QueryRegistrierungen = "/db/queries/WHoch2QueryRegistrierungen.sql";
	public final String wHoch2QueryImpfungen = "/db/queries/WHoch2QueryImpfungen.sql";
	public final String wHoch2QueryImpfslots = "/db/queries/WHoch2QueryImpfslots.sql";
	public final String wHoch2EdorexQueryRegistrierungen = "/db/queries/WHoch2EdorexQueryRegistrierungen.sql";
	public final String wHoch2EdorexQueryImpfung = "/db/queries/WHoch2EdorexQueryImpfungen.sql";
	public final String wHoch2EdorexQueryImpfslots = "/db/queries/WHoch2EdorexQueryImpfslots.sql";

	private final Db db;

	public static final String DB_QUERY_TIMEOUT_HINT = "org.hibernate.timeout";

	@Inject
	VacmeSettingsService vacmeSettingsService;

	@Inject
	public WHoch2QueriesService(@NonNull Db db) {
		this.db = db;
	}


	/**
	 * @return query aus Filesystem lesen
	 */

	public String loadWHoch2Query(String queryFilename) {
		InputStream inputStream = WHoch2QueriesService.class.getResourceAsStream(queryFilename);
		Objects.requireNonNull(inputStream);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			var sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}

			String s = sb.toString();
			s = OUTFILE_MATCH_PATTERN.matcher(s).replaceAll("from");
			s = OUTFILE_MATCH_PATTERN_ALTERNATIVE.matcher(s).replaceAll("");
			s = s.replace("${TOMORROW}", LocalDate.now().plusDays(1).toString());
			return s;
		} catch (IOException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query"
				+ queryFilename);
		}
	}


	@NonNull

	@Transactional(TxType.REQUIRES_NEW)
	public List getWHoch2QueryRegistrierungen() {

		try {
			final Query nativeQueryRegistrierungen = this.db.getEntityManager().createNativeQuery(loadWHoch2Query(
				wHoch2QueryRegistrierungen));
			nativeQueryRegistrierungen.setHint(DB_QUERY_TIMEOUT_HINT, vacmeSettingsService.getDbQueryTimeoutInSeconds());
			return nativeQueryRegistrierungen.getResultList();
		} catch(QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getWHoch2QueryImpfungen() {

		try {
			final Query nativeQueryImpfungen = this.db.getEntityManager().createNativeQuery(loadWHoch2Query(wHoch2QueryImpfungen));
			nativeQueryImpfungen.setHint(DB_QUERY_TIMEOUT_HINT, vacmeSettingsService.getDbQueryTimeoutInSeconds());
			return nativeQueryImpfungen.getResultList();

		} catch(QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getWHoch2QueryImpfslots() {

		try {
			final Query nativeQueryImpfslots = this.db.getEntityManager().createNativeQuery(loadWHoch2Query(wHoch2QueryImpfslots));
			nativeQueryImpfslots.setHint(DB_QUERY_TIMEOUT_HINT, vacmeSettingsService.getDbQueryTimeoutInSeconds());
			return nativeQueryImpfslots.getResultList();

		} catch(QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getWHoch2EdorexQueryRegistrierungen() {
		try {
			final Query nativeQueryEdorexRegistrierungen = this.db.getEntityManager().createNativeQuery(loadWHoch2Query(wHoch2EdorexQueryRegistrierungen));
			nativeQueryEdorexRegistrierungen.setHint(DB_QUERY_TIMEOUT_HINT, vacmeSettingsService.getDbQueryTimeoutInSeconds());
			return nativeQueryEdorexRegistrierungen.getResultList();

		} catch(QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getWHoch2EdorexQueryImpfungen() {
		try {
			final Query nativeQueryEdorexImpfungen = this.db.getEntityManager().createNativeQuery(loadWHoch2Query(wHoch2EdorexQueryImpfung));
			nativeQueryEdorexImpfungen.setHint(DB_QUERY_TIMEOUT_HINT, vacmeSettingsService.getDbQueryTimeoutInSeconds());
			return nativeQueryEdorexImpfungen.getResultList();

		} catch(QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getWHoch2EdorexQueryImpfslots() {
		try {
			final Query nativeQueryEdorexImpfslots = this.db.getEntityManager().createNativeQuery(loadWHoch2Query(wHoch2EdorexQueryImpfslots));
			nativeQueryEdorexImpfslots.setHint(DB_QUERY_TIMEOUT_HINT, vacmeSettingsService.getDbQueryTimeoutInSeconds());
			return nativeQueryEdorexImpfslots.getResultList();

		} catch(QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}
}
