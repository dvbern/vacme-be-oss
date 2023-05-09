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
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Transactional
@ApplicationScoped
@Slf4j
public class PostversandQueriesService {

	private static final Pattern OUTFILE_MATCH_PATTERN = Pattern.compile(
		"into dumpfile.*from",
		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern OUTFILE_MATCH_PATTERN_ALTERNATIVE = Pattern.compile(
		"into dumpfile.* escaped by ''",
		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final String postversandQueryTerminBestaetigung_regnum =
		"/db/postverdandqueries/fileTypIsTerminBestaetigung_regnum.sql";
	private static final String postversandQueryTerminBestaetigung_dumpfile =
		"/db/postverdandqueries/fileTypIsTerminBestaetigung_dumpfile.sql";
	private static final String postversandQueryTerminBestaetigung_regID =
		"/db/postverdandqueries/fileTypIsTerminBestaetigung_impfdossierID.sql";
	private static final String postversandQueryTerminBestaetigung_update =
		"/db/postverdandqueries/fileTypIsTerminBestaetigung_update.sql";
	private static final String postversandQueryTerminAbsage_regnum = "/db/postverdandqueries/fileTypIsTerminAbsage_regnum"
		+ ".sql";
	private static final String postversandQueryTerminAbsage_dumpfile =
		"/db/postverdandqueries/fileTypIsTerminAbsage_dumpfile.sql";
	private static final String postversandQueryTerminAbsage_regID =
		"/db/postverdandqueries/fileTypIsTerminAbsage_impfdossierID.sql";
	private static final String postversandQueryTerminAbsage_update = "/db/postverdandqueries/fileTypIsTerminAbsage_update"
		+ ".sql";
	private static final String postversandQueryTerminZertifikatStornierung_regnum =
		"/db/postverdandqueries/fileTypIsTerminZertifikatStornierung_regnum.sql";
	private static final String postversandQueryTerminZertifikatStornierung_regID =
		"/db/postverdandqueries/fileTypIsTerminZertifikatStornierung_impfdossierID.sql";
	private static final String postversandQueryTerminZertifikatStornierung_dumpfile =
		"/db/postverdandqueries/fileTypIsTerminZertifikatStornierung_dumpfile.sql";
	private static final String postversandQueryTerminZertifikatStornierung_update =
		"/db/postverdandqueries/fileTypIsTerminZertifikatStornierung_update.sql";
	private static final String postversandQueryRegistrierungBestaetigung_regnum =
		"/db/postverdandqueries/fileTypIsRegistrierungBestaetigung_regnum.sql";
	private static final String postversandQueryRegistrierungBestaetigung_dumpfile =
		"/db/postverdandqueries/fileTypIsRegistrierungBestaetigung_dumpfile.sql";
	private static final String postversandQueryRegistrierungBestaetigung_regID =
		"/db/postverdandqueries/fileTypIsRegistrierungBestaetigung_regID.sql";
	private static final String postversandQueryRegistrierungBestaetigung_update =
		"/db/postverdandqueries/fileTypIsRegistrierungBestaetigung_update.sql";
	private static final String postversandQueryFreigabeBoosterInfo_regnum =
		"/db/postverdandqueries/fileTypIsFreigabeBoosterInfo_regnum.sql";
	private static final String postversandQueryFreigabeBoosterInfo_regID =
		"/db/postverdandqueries/fileTypIsFreigabeBoosterInfo_impfdossierID.sql";
	private static final String postversandQueryFreigabeBoosterInfo_dumpfile =
		"/db/postverdandqueries/fileTypIsFreigabeBoosterInfo_dumpfile.sql";
	private static final String postversandQueryFreigabeBoosterInfo_update =
		"/db/postverdandqueries/fileTypIsFreigabeBoosterInfo_update.sql";
	private static final String postversandQueryOnboardingLetter_regnum =
		"/db/postverdandqueries/fileTypIsOnboardingLetter_regnum.sql";
	private static final String postversandQueryOnboardingLetter_regID =
		"/db/postverdandqueries/fileTypIsOnboardingLetter_regID.sql";
	private static final String postversandQueryOnboardingLetter_dumpfile =
		"/db/postverdandqueries/fileTypIsOnboardingLetter_dumpfile.sql";
	private static final String postversandQueryOnboardingLetter_update =
		"/db/postverdandqueries/fileTypIsOnboardingLetter_update.sql";

	private final Db db;

	public static final String DB_QUERY_TIMEOUT_HINT = "org.hibernate.timeout";

	@ConfigProperty(name = "vacme.healthcheck.query.timeout", defaultValue = "290")
	int dbQueryTimeoutInSeconds;

	@Inject
	public PostversandQueriesService(@NonNull Db db) {
		this.db = db;
	}

	/**
	 * @return query aus Filesystem lesen
	 */

	public String loadPostversandQuery(String queryFilename) {
		InputStream inputStream = PostversandQueriesService.class.getResourceAsStream(queryFilename);
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
			return s;
		} catch (IOException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query"
				+ queryFilename);
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminBestaetigung_regnum() {

		try {
			final Query nativeQueryTerminBestaetigung_regnum =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminBestaetigung_regnum));
			nativeQueryTerminBestaetigung_regnum.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryTerminBestaetigung_regnum.getResultList();
		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminBestaetigung_dumpfile() {

		try {
			final Query nativeQueryTerminBestaetigung_dumpfile =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminBestaetigung_dumpfile));
			nativeQueryTerminBestaetigung_dumpfile.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryTerminBestaetigung_dumpfile.getResultList();
		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminBestaetigung_regID() {

		try {
			final Query nativeQueryTerminBestaetigung_regID =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminBestaetigung_regID));
			nativeQueryTerminBestaetigung_regID.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryTerminBestaetigung_regID.getResultList();
		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminBestaetigung_update() {

		try {
			final Query nativeQueryTerminBestaetigung_update =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminBestaetigung_update));
			nativeQueryTerminBestaetigung_update.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryTerminBestaetigung_update.getResultList();
		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminAbsage_regnum() {

		try {
			final Query nativeQueryTerminAbsage_regnum =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminAbsage_regnum));
			nativeQueryTerminAbsage_regnum.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryTerminAbsage_regnum.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminAbsage_dumpfile() {

		try {
			final Query nativeQueryTerminAbsage_dumpfile =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminAbsage_dumpfile));
			nativeQueryTerminAbsage_dumpfile.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryTerminAbsage_dumpfile.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminAbsage_regID() {

		try {
			final Query nativeQueryTerminAbsage_regID =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminAbsage_regID));
			nativeQueryTerminAbsage_regID.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryTerminAbsage_regID.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminAbsage_update() {

		try {
			final Query nativeQueryTerminAbsage_update =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminAbsage_update));
			nativeQueryTerminAbsage_update.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryTerminAbsage_update.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryRegistrierungBestaetigung_regnum() {

		try {
			final Query nativeQueryPostversandQueryRegistrierungBestaetigung_regnum =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryRegistrierungBestaetigung_regnum));
			nativeQueryPostversandQueryRegistrierungBestaetigung_regnum.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryRegistrierungBestaetigung_regnum.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryRegistrierungBestaetigung_dumpfile() {

		try {
			final Query nativeQueryPostversandQueryRegistrierungBestaetigung_dumpfile =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryRegistrierungBestaetigung_dumpfile));
			nativeQueryPostversandQueryRegistrierungBestaetigung_dumpfile.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryRegistrierungBestaetigung_dumpfile.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryRegistrierungBestaetigung_regID() {

		try {
			final Query nativeQueryPostversandQueryRegistrierungBestaetigung_regID =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryRegistrierungBestaetigung_regID));
			nativeQueryPostversandQueryRegistrierungBestaetigung_regID.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryRegistrierungBestaetigung_regID.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryRegistrierungBestaetigung_update() {

		try {
			final Query nativeQueryPostversandQueryRegistrierungBestaetigung_update =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryRegistrierungBestaetigung_update));
			nativeQueryPostversandQueryRegistrierungBestaetigung_update.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryRegistrierungBestaetigung_update.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryFreigabeBoosterInfo_regnum() {

		try {
			final Query nativeQueryPostversandQueryFreigabeBoosterInfo_regnum =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryFreigabeBoosterInfo_regnum));
			nativeQueryPostversandQueryFreigabeBoosterInfo_regnum.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryFreigabeBoosterInfo_regnum.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryFreigabeBoosterInfo_regID() {

		try {
			final Query nativeQueryPostversandQueryFreigabeBoosterInfo_regID =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryFreigabeBoosterInfo_regID));
			nativeQueryPostversandQueryFreigabeBoosterInfo_regID.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryFreigabeBoosterInfo_regID.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryFreigabeBoosterInfo_dumpfile() {

		try {
			final Query nativeQueryPostversandQueryFreigabeBoosterInfo_dumpfile =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryFreigabeBoosterInfo_dumpfile));
			nativeQueryPostversandQueryFreigabeBoosterInfo_dumpfile.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryFreigabeBoosterInfo_dumpfile.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryFreigabeBoosterInfo_update() {

		try {
			final Query nativeQueryPostversandQueryFreigabeBoosterInfo_update =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryFreigabeBoosterInfo_update));
			nativeQueryPostversandQueryFreigabeBoosterInfo_update.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryFreigabeBoosterInfo_update.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminZertifikatStornierung_regnum() {

		try {
			final Query nativeQueryPostversandQueryTerminZertifikatStornierung_regnum =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminZertifikatStornierung_regnum));
			nativeQueryPostversandQueryTerminZertifikatStornierung_regnum.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryTerminZertifikatStornierung_regnum.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminZertifikatStornierung_regID() {

		try {
			final Query nativeQueryPostversandQueryTerminZertifikatStornierung_regID =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminZertifikatStornierung_regID));
			nativeQueryPostversandQueryTerminZertifikatStornierung_regID.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryTerminZertifikatStornierung_regID.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminZertifikatStornierung_dumpfile() {

		try {
			final Query nativeQueryPostversandQueryTerminZertifikatStornierung_dumpfile =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminZertifikatStornierung_dumpfile));
			nativeQueryPostversandQueryTerminZertifikatStornierung_dumpfile.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryTerminZertifikatStornierung_dumpfile.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryTerminZertifikatStornierung_update() {

		try {
			final Query nativeQueryPostversandQueryTerminZertifikatStornierung_update =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryTerminZertifikatStornierung_update));
			nativeQueryPostversandQueryTerminZertifikatStornierung_update.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryTerminZertifikatStornierung_update.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryOnboardingLetter_regnum() {

		try {
			final Query nativeQueryPostversandQueryOnboardingLetter_regnum =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryOnboardingLetter_regnum));
			nativeQueryPostversandQueryOnboardingLetter_regnum.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryOnboardingLetter_regnum.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryOnboardingLetter_regID() {

		try {
			final Query nativeQueryPostversandQueryOnboardingLetter_regID =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryOnboardingLetter_regID));
			nativeQueryPostversandQueryOnboardingLetter_regID.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryOnboardingLetter_regID.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryOnboardingLetter_dumpfile() {

		try {
			final Query nativeQueryPostversandQueryOnboardingLetter_dumpfile =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryOnboardingLetter_dumpfile));
			nativeQueryPostversandQueryOnboardingLetter_dumpfile.setHint(
				DB_QUERY_TIMEOUT_HINT,
				dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryOnboardingLetter_dumpfile.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}

	@NonNull
	@Transactional(TxType.REQUIRES_NEW)
	public List getPostversandQueryOnboardingLetter_update() {

		try {
			final Query nativeQueryPostversandQueryOnboardingLetter_update =
				this.db.getEntityManager().createNativeQuery(loadPostversandQuery(
					postversandQueryOnboardingLetter_update));
			nativeQueryPostversandQueryOnboardingLetter_update.setHint(DB_QUERY_TIMEOUT_HINT, dbQueryTimeoutInSeconds);
			return nativeQueryPostversandQueryOnboardingLetter_update.getResultList();

		} catch (QueryTimeoutException ex) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query");
		}
	}
}
