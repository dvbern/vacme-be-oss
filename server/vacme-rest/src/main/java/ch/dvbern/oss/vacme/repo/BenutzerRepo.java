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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.benutzer.QBenutzer;
import ch.dvbern.oss.vacme.entities.benutzer.QBenutzerBerechtigung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.logging.LoggedIfSlow;
import ch.dvbern.oss.vacme.smartdb.Db;
import ch.dvbern.oss.vacme.smartdb.SmartJPAQuery;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.entities.benutzer.QBenutzer.benutzer;
import static ch.dvbern.oss.vacme.entities.terminbuchung.QOrtDerImpfung.ortDerImpfung;

@RequestScoped
@Transactional
@Slf4j
public class BenutzerRepo {


	@ConfigProperty(name = "vacme.oidc.reg.auth.server.url", defaultValue = "http://localhost:8180/auth/realms/vacme") // configured as VACME_OIDC_REG_AUTH_SERVER_URL
	protected String keycloakRegIssuerUrl; // as opposed to value configured for Fachapplikation VACME_OIDC_WEB_AUTH_SERVER_URL

	private final Db db;

	@Inject
	public BenutzerRepo(Db db) {
		this.db = db;
	}

	@NonNull
	public Benutzer superAdminRef() {
		return db.getReference(Benutzer.toId(DBConst.SYSTEM_ADMIN_ID));
	}

	public void create(@NonNull Benutzer user) {
		db.persist(user);
		db.flush();
	}

	public Benutzer merge(@NonNull Benutzer user) {
		return db.merge(user);
	}

	@NonNull
	public Optional<Benutzer> getById(@NonNull ID<Benutzer> id) {
		return db.get(id);
	}

	public Set<OrtDerImpfung> getOdisOfBenutzer(ID<Benutzer> id) {
		return new HashSet<>( db.select(ortDerImpfung)
			.from(benutzer)
			.innerJoin(benutzer.ortDerImpfung, ortDerImpfung)
			.where(benutzer.id.eq(id.getId()))
			.fetch());
	}

	public void delete(@NonNull ID<Benutzer> benutzerId) {
		db.remove(benutzerId);
	}

	@NonNull
	public List<Benutzer> findAll() {
		return db.findAll(benutzer);
	}

	@NonNull
	public List<Benutzer> findByRolleAndOrtDerImpfung(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull BenutzerRolle rolle
	) {
		return db.selectFrom(benutzer)
			.distinct()
			.innerJoin(benutzer.berechtigungen, QBenutzerBerechtigung.benutzerBerechtigung)
			.on(QBenutzerBerechtigung.benutzerBerechtigung.rolle.eq(rolle))
			.where(benutzer.ortDerImpfung.contains(ortDerImpfung))
			.fetch();
	}

	@NonNull
	public List<Benutzer> getByGLNAndRolle(@NonNull String glnNummer, @NonNull BenutzerRolle rolle) {
		return db.selectFrom(benutzer)
			.distinct()
			.innerJoin(benutzer.berechtigungen, QBenutzerBerechtigung.benutzerBerechtigung)
			.on(QBenutzerBerechtigung.benutzerBerechtigung.rolle.eq(rolle))
			.where(benutzer.glnNummer.eq(glnNummer))
			.fetch();
	}


	public Optional<Benutzer> getByBenutzernameFromRegapp(@NonNull String benutzername) {
		return this.getByBenutzername(benutzername, this.keycloakRegIssuerUrl);
	}

	public Optional<Benutzer> getByBenutzername(@NonNull String benutzername, @NonNull String keycloakIssuer) {
		List<Benutzer> fetchedUsers = db.selectFrom(benutzer)
			.distinct()
			.where(benutzer.benutzername.eq(benutzername))
			.fetch();

		List<Benutzer> collect =
			fetchedUsers.stream().filter(benutzer1 -> benutzer1.getIssuer().endsWith(keycloakIssuer)).collect(Collectors.toList());

		if (collect.size() > 1) {
			throw new NonUniqueResultException("Found more than one Benutzer issued from " + keycloakIssuer + " for username " + benutzername);
		}

		return Optional.of(collect.get(0));
	}

	@NonNull
	@LoggedIfSlow
	public List<ID<Benutzer>> getAllActiveSubordinateBenutzerWhereLastSessionAndLastUnlockedIsOlderThanMinutesOrNull(
		int minutes,
		@Nullable String issuerToLookFor,
		@Nullable Integer limit
	) {
		LocalDateTime cutoffDateTimeForOldSessions = LocalDateTime.now().minusMinutes(minutes);

		QBenutzer aliasBenutzer = new QBenutzer("B1");
		QBenutzerBerechtigung aliasBenutzerBer = new QBenutzerBerechtigung("B1Ber");
		QBenutzer otherBenutzer = new QBenutzer("B2");
		SmartJPAQuery<UUID> query =
			db.select(aliasBenutzer.id)
				.from(aliasBenutzer)
				.innerJoin(aliasBenutzerBer)
				.on(aliasBenutzerBer.benutzer.eq(aliasBenutzer))
				.distinct()
				.where(
					(aliasBenutzer.timestampLastSessionId.lt(cutoffDateTimeForOldSessions).or(aliasBenutzer.timestampLastSessionId.isNull()))
						.and(getConditionLastUnlocked(cutoffDateTimeForOldSessions, aliasBenutzer))
						.and(aliasBenutzer.issuer.eq(issuerToLookFor))
						.and(aliasBenutzer.deaktiviert.isFalse())
						.and(aliasBenutzerBer.rolle.in(BenutzerRolle.OI_KONTROLLE))
						.and(aliasBenutzer.id.notIn(db.select(otherBenutzer.id)
							.from(otherBenutzer)
							.innerJoin(QBenutzerBerechtigung.benutzerBerechtigung)
							.on(QBenutzerBerechtigung.benutzerBerechtigung.benutzer.eq(otherBenutzer))
								.where(QBenutzerBerechtigung.benutzerBerechtigung.rolle.in(BenutzerRolle.OI_IMPFVERANTWORTUNG, BenutzerRolle.OI_ORT_VERWALTER))
							.asSubQuery()))

				);

		List<UUID> resultList;
		if (limit != null) {
			resultList = query.limit(limit).fetch();
		} else {
			resultList = query.fetch();
		}
		return resultList.stream().map(Benutzer::toId).collect(Collectors.toList());
	}

	private Predicate getConditionLastUnlocked(LocalDateTime cutoffDateTimeForOldSessions, QBenutzer aliasBenutzer) {
		if(MandantUtil.getMandant().equals(Mandant.BE)) {
			return Expressions.asBoolean(true).isTrue();
		} else {
			return aliasBenutzer.timestampLastUnlocked.lt(cutoffDateTimeForOldSessions).or(aliasBenutzer.timestampLastUnlocked.isNull());
		}
	}
}
