/*
 *
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

package ch.dvbern.oss.vacme.repo.vmdl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.QImpfung;
import ch.dvbern.oss.vacme.entities.registration.QFragebogen;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.jax.vmdl.QVMDLUploadAffenpockenJax;
import ch.dvbern.oss.vacme.jax.vmdl.QVMDLUploadCovidJax;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadAffenpockenJax;
import ch.dvbern.oss.vacme.jax.vmdl.VMDLUploadCovidJax;
import ch.dvbern.oss.vacme.smartdb.Db;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.entities.registration.QRegistrierung.registrierung;

@RequestScoped
@Transactional
@Slf4j
public class VMDLRepo {

	public static final int SLOW_THRESHOLD_MS =  15  * 1000;
	private final Db db;

	@Inject
	public VMDLRepo(Db db) {
		this.db = db;
	}

	@NonNull
	public List<VMDLUploadCovidJax> getVMDLPendenteImpfungen3QueriesCovid(int uploadChunkLimit, @NonNull String reportingUnitID) {
		final Expression<String> reportingUnitIDExpression = Expressions.constant(reportingUnitID);
		BooleanExpression joinExpressionT1 = QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin1);
		List<VMDLUploadCovidJax> impfungen1 = runVMDLQueryRegistrierungstermineCovid(uploadChunkLimit, reportingUnitIDExpression, joinExpressionT1, "vmdl_t1_impfungen");
		List<VMDLUploadCovidJax> result = new ArrayList<>(impfungen1);

		int remainingChunkLimit = uploadChunkLimit - impfungen1.size();

		if (remainingChunkLimit > 0) {
			BooleanExpression joinExpressionT2 = QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin2);
			List<VMDLUploadCovidJax> impfungen2 = runVMDLQueryRegistrierungstermineCovid(remainingChunkLimit, reportingUnitIDExpression, joinExpressionT2, "vmdl_t2_impfungen");
			result.addAll(impfungen2);
			remainingChunkLimit = remainingChunkLimit - impfungen2.size();
		}

		if (remainingChunkLimit > 0) {
			List<VMDLUploadCovidJax> impfungenN = runDossierImpfungenVMDLQueryForCovid(reportingUnitIDExpression, remainingChunkLimit);
			result.addAll(impfungenN);
		}
		return result;
	}

	@NonNull
	public List<VMDLUploadCovidJax> getVMDLPendenteImpfungen2QueriesCovid(int uploadChunkLimit, @NonNull String reportingUnitID) {
		final Expression<String> reportingUnitIDExpression = Expressions.constant(reportingUnitID);
		BooleanExpression joinExpressionT1T2 = QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin1)
			.or(QImpftermin.impftermin.eq(QImpfdossier.impfdossier.buchung.impftermin2));

		List<VMDLUploadCovidJax> impfungen1Or2 = runVMDLQueryRegistrierungstermineCovid(uploadChunkLimit, reportingUnitIDExpression, joinExpressionT1T2, "vmdl_t1_t2_impfungen");

		List<VMDLUploadCovidJax> result = new ArrayList<>(impfungen1Or2);
		long remainingChunkLimit = uploadChunkLimit - result.size();

		if (remainingChunkLimit > 0) {
			List<VMDLUploadCovidJax> impfungenN = runDossierImpfungenVMDLQueryForCovid(reportingUnitIDExpression, remainingChunkLimit);
			result.addAll(impfungenN);
		}
		return result;
	}

	@NonNull
	public List<VMDLUploadAffenpockenJax> getVMDLPendenteAffenpockenImpfungen(int uploadChunkLimit, @NonNull String reportingUnitID) {
		if (uploadChunkLimit > 0) {
			final Expression<String> reportingUnitIDExpression = Expressions.constant(reportingUnitID);
			List<VMDLUploadAffenpockenJax> impfungenN = runDossierImpfungenVMDLQueryForAffenpocken(reportingUnitIDExpression, uploadChunkLimit);
			return impfungenN;
		}
		return Collections.emptyList();
	}

	@NonNull
	private List<VMDLUploadCovidJax> runVMDLQueryRegistrierungstermineCovid(
		int limit,
		@NonNull Expression<String> reportingUnitIDExpression,
		@NonNull Predicate impfdossierJoinfExpression,
		@NonNull String queryName
	) {
		StopWatch stopwatch = StopWatch.createStarted();

		List<VMDLUploadCovidJax> impfungen =
			db.select(new QVMDLUploadCovidJax(
					QImpfung.impfung,
					QImpfdossier.impfdossier,
					QFragebogen.fragebogen,
					reportingUnitIDExpression)
				)
				.from(QImpfung.impfung)
				.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.eq(QImpfung.impfung.termin))
				.innerJoin(QImpfslot.impfslot).on(QImpftermin.impftermin.impfslot.eq(QImpfslot.impfslot))
				.innerJoin(QImpfdossier.impfdossier).on(impfdossierJoinfExpression)
				.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
				.leftJoin(QFragebogen.fragebogen).on(registrierung.eq(QFragebogen.fragebogen.registrierung))
				.where(QImpfung.impfung.timestampVMDL.isNull()
					.and(QImpfung.impfung.extern.isFalse())
					.and(QImpfslot.impfslot.krankheitIdentifier.in(KrankheitIdentifier.getVMDLSupportedKrankheiten()))
				)
				.limit(limit)
				.fetch();

		logIfSlow(stopwatch, impfungen.size(), queryName);
		return impfungen;
	}

	@NonNull
	private List<VMDLUploadCovidJax> runDossierImpfungenVMDLQueryForCovid(
		@NonNull Expression<String> reportingUnitIDExpression,
		long remainingChunkLimit
	) {
		StopWatch stopwatchQ2 = StopWatch.createStarted();
		List<VMDLUploadCovidJax> impfungenN = db.select(new QVMDLUploadCovidJax(
				QImpfung.impfung,
				QImpfdossier.impfdossier,
				QFragebogen.fragebogen,
				QImpfdossiereintrag.impfdossiereintrag,
				reportingUnitIDExpression))
			.from(QImpfung.impfung)
			.limit(remainingChunkLimit)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.eq(QImpfung.impfung.termin))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpfdossiereintrag.impfdossiereintrag.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.eq(QImpfdossiereintrag.impfdossiereintrag.impfdossier))
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.innerJoin(QFragebogen.fragebogen).on(registrierung.eq(QFragebogen.fragebogen.registrierung))
			.where(QImpfung.impfung.timestampVMDL.isNull()
				.and(QImpfung.impfung.extern.isFalse())
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
			)
			.fetch();
		logIfSlow(stopwatchQ2, impfungenN.size(), "vmdl_dossiereintragimpfungen_covid");
		return impfungenN;
	}

	@NonNull
	private List<VMDLUploadAffenpockenJax> runDossierImpfungenVMDLQueryForAffenpocken(
		@NonNull Expression<String> reportingUnitIDExpression,
		long remainingChunkLimit
	) {
		StopWatch stopwatchQ2 = StopWatch.createStarted();
		List<VMDLUploadAffenpockenJax> impfungenN = db.select(new QVMDLUploadAffenpockenJax(
				QImpfung.impfung,
				QImpfdossier.impfdossier,
				QFragebogen.fragebogen,
				QImpfdossiereintrag.impfdossiereintrag,
				reportingUnitIDExpression))
			.from(QImpfung.impfung)
			.limit(remainingChunkLimit)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.eq(QImpfung.impfung.termin))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpfdossiereintrag.impfdossiereintrag.impftermin.eq(QImpftermin.impftermin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.eq(QImpfdossiereintrag.impfdossiereintrag.impfdossier))
			.innerJoin(registrierung).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
			.innerJoin(QFragebogen.fragebogen).on(registrierung.eq(QFragebogen.fragebogen.registrierung))
			.where(QImpfung.impfung.timestampVMDL.isNull()
				.and(QImpfung.impfung.extern.isFalse())
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.AFFENPOCKEN))
			)
			.fetch();
		logIfSlow(stopwatchQ2, impfungenN.size(), "vmdl_dossiereintragimpfungen_affenpocken");
		return impfungenN;
	}

	private void logIfSlow(@NonNull StopWatch stopwatch, int resultCnt, @NonNull String queryname) {
		stopwatch.stop();
		if (stopwatch.getTime(TimeUnit.MILLISECONDS) > SLOW_THRESHOLD_MS) {
			LOG.warn("VACME-VMDL: Querytime for query '{}' with resultcount {} was {}ms", queryname, resultCnt, stopwatch.getTime(TimeUnit.MILLISECONDS));

		}
	}

	/**
	 * Prueft ob diese Impfung jemals an VMDL gesendet wurde. Dabei wird auch die Autit Tabelle durchsucht,
	 * weil z.B. bei einer Korrektur der VMDL-Timestamp geloescht wird, damit die Impfung neu geschickt wird.
	 *
	 * @return true wenn timestempVMDL dieser Impfung mal gesetzt war
	 */
	public boolean wasSentToVMDL(@NonNull Impfung impfung) {
		// current timestampVMDL is not null
		if (impfung.getTimestampVMDL() != null) {
			return true;
		}
		// Otherwise search in the audit table
		String query = "SELECT REV FROM Impfung_AUD WHERE id = ?1 and timestampVMDL is not null;";
		Query nativeQuery = db.getEntityManager().createNativeQuery(query);
		nativeQuery.setParameter(1, impfung.getId().toString());
		return nativeQuery.getResultList().size() > 0;
	}
}
