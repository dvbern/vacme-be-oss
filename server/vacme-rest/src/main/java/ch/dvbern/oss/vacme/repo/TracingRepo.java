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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.registration.QRegistrierung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.zertifikat.QZertifikat;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.smartdb.Db;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@RequestScoped
@Transactional
public class TracingRepo {

	private static final String FORBIDDEN_KKK = "00000000000000000000"; // Filter out EDA und Ausland

	@ConfigProperty(name = "tracing.respect.choice", defaultValue = "true")
	boolean respectTracingChoice;

	private final Db db;

	@Inject
	public TracingRepo(Db db) {
		this.db = db;
	}

	@NonNull
	public Optional<Registrierung> getByRegistrierungnummerAndStatus(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull String registrierungsnummer,
		@NonNull Set<ImpfdossierStatus> statusList
	) {
		var result = db.selectFrom(QRegistrierung.registrierung)
			.innerJoin(QImpfdossier.impfdossier).on(QRegistrierung.registrierung.eq(QImpfdossier.impfdossier.registrierung))
			.where(QRegistrierung.registrierung.registrierungsnummer.eq(registrierungsnummer)
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier))
				.and(QImpfdossier.impfdossier.dossierStatus.in(statusList))
				.and(predicateTracingChoice()))
			.fetchOne();
		return result;
	}

	@NonNull
	public Optional<Registrierung> getByZertifikatUVCIAndStatus(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull String uvci,
		@NonNull Set<ImpfdossierStatus> statusList
	) {
		var result = db.selectFrom(QRegistrierung.registrierung)
			.innerJoin(QZertifikat.zertifikat).on(QRegistrierung.registrierung.eq(QZertifikat.zertifikat.registrierung))
			.innerJoin(QImpfdossier.impfdossier).on(QRegistrierung.registrierung.eq(QImpfdossier.impfdossier.registrierung))
			.where(QZertifikat.zertifikat.uvci.eq(uvci)
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier))
				.and(QImpfdossier.impfdossier.dossierStatus.in(statusList))
				.and(predicateTracingChoice()))
			.fetchOne();
		return result;
	}

	public List<Registrierung> getByKrankenkassennummerAndStatus(
		@NonNull KrankheitIdentifier krankheitIdentifier,
		@NonNull String krankenkassennummer,
		@NonNull Set<ImpfdossierStatus> statusList
	) {
		var result = db.selectFrom(QRegistrierung.registrierung)
			.innerJoin(QImpfdossier.impfdossier).on(QRegistrierung.registrierung.eq(QImpfdossier.impfdossier.registrierung))
			.where(QRegistrierung.registrierung.krankenkasseKartenNr.eq(krankenkassennummer)
				.and(QRegistrierung.registrierung.krankenkasseKartenNr.ne(FORBIDDEN_KKK))
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier))
				.and(QImpfdossier.impfdossier.dossierStatus.in(statusList))
				.and(predicateTracingChoice()))
			.fetch();
		return result;
	}

	private Predicate predicateTracingChoice() {
		if (respectTracingChoice) {
			return QRegistrierung.registrierung.contactTracing.eq(Boolean.TRUE);
		}
		// Expression, die immer TRUE ist
		return Expressions.asBoolean(Expressions.constant(true)).isTrue();
	}
}
