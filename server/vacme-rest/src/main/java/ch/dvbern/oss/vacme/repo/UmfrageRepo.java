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
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.umfrage.QUmfrageDTO;
import ch.dvbern.oss.vacme.entities.umfrage.Umfrage;
import ch.dvbern.oss.vacme.entities.umfrage.UmfrageDTO;
import ch.dvbern.oss.vacme.entities.umfrage.UmfrageGruppe;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.service.HashIdService;
import ch.dvbern.oss.vacme.smartdb.Db;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.vacme.entities.benutzer.QBenutzer.benutzer;
import static ch.dvbern.oss.vacme.entities.registration.QRegistrierung.registrierung;
import static ch.dvbern.oss.vacme.entities.umfrage.QUmfrage.umfrage;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ABGESCHLOSSEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMMUNISIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_DURCHGEFUEHRT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.NEU;

@RequestScoped
@Transactional
public class UmfrageRepo {

	private final Db db;
	private final EntityManager em;
	private final HashIdService hashIdService;

	@Inject
	public UmfrageRepo(Db db, EntityManager em, HashIdService hashIdService) {
		this.db = db;
		this.em = em;
		this.hashIdService = hashIdService;
	}

	public void create(@NonNull Umfrage umfrage) {
		db.persist(umfrage);
		db.flush();
	}

	@NonNull
	@Transactional(TxType.SUPPORTS)
	/* use transaction if there is one in the context (keep in mind sequences can't be rolled back)
	otherwise run without */
	public String getNextUmfrageCode() {
		BigInteger nextValue =
			(BigInteger) em.createNativeQuery("SELECT NEXT VALUE FOR umfrage_sequence;").getSingleResult();
		return hashIdService.getUmfrageCodeHash(nextValue.longValue());
	}

	public Umfrage getUmfrageByCode(@NonNull String code) {
		return db.select(umfrage).from(umfrage).where(umfrage.umfrageCode.eq(code)).fetchFirst();
	}

	@NonNull
	public List<Umfrage> getUmfrageByRegistrierung(@NonNull Registrierung registrierung) {
		return db
			.select(umfrage)
			.from(umfrage)
			.where(umfrage.registrierung.eq(registrierung))
			.fetch();
	}

	public void update(@NonNull Umfrage umfrage) {
		db.merge(umfrage);
		db.flush();
	}

	@NonNull
	public List<UmfrageDTO> getKandidatenUmfrage3(int limit) {
		final Set<ImpfdossierStatus> statusList = Set.of(
			NEU,
			FREIGEGEBEN);
		return getKandidatenErwachsene(statusList, true, limit);
	}

	@NonNull
	public List<UmfrageDTO> getKandidatenUmfrage4(int limit) {
		final Set<ImpfdossierStatus> statusList = Set.of(
			IMPFUNG_1_DURCHGEFUEHRT,
			IMPFUNG_2_KONTROLLIERT,
			IMPFUNG_2_DURCHGEFUEHRT,
			ABGESCHLOSSEN,
			IMMUNISIERT,
			ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG);
		return getKandidatenErwachsene(statusList, true, limit);
	}

	@NonNull
	public List<UmfrageDTO> getKandidatenUmfrage5(int limit) {
		final Set<ImpfdossierStatus> statusList = Set.of(
			NEU,
			FREIGEGEBEN);
		return getKandidatenErwachsene(statusList, false, limit);
	}

	@NonNull
	public List<UmfrageDTO> getKandidatenUmfrage6(int limit) {
		final Set<ImpfdossierStatus> statusList = Set.of(
			IMPFUNG_1_DURCHGEFUEHRT,
			IMPFUNG_2_KONTROLLIERT,
			IMPFUNG_2_DURCHGEFUEHRT,
			ABGESCHLOSSEN,
			IMMUNISIERT,
			ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG);
		return getKandidatenErwachsene(statusList, false, limit);
	}

	@NonNull
	public List<UmfrageDTO> getKandidatenErwachsene(@NonNull Set<ImpfdossierStatus> statusList, boolean vorStichtag, int limit) {
		if (vorStichtag) {
			// vorStichtag heisst eigentlich: Registrierungstimestamp egal
			// todo Affenpocken: VACME-2404 low prio: Umfrage funktinoiert nur fuer Covid
			return db
				.select(new QUmfrageDTO(
					registrierung,
					benutzer.mobiltelefon))
				.from(registrierung)
				.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
				.leftJoin(benutzer).on(registrierung.benutzerId.eq(benutzer.id))
				.where(registrierung.id.in(getUniqueOnlineErwachsene())
					.and(QImpfdossier.impfdossier.dossierStatus.in(statusList))
					.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
				)
				.orderBy(registrierung.registrierungsnummer.asc())
				.limit(limit)
				.fetch();
		} else {
			LocalDateTime stichtag = LocalDateTime.of(2021, Month.SEPTEMBER, 8, 14, 30, 0);
			return db
				.select(new QUmfrageDTO(
					registrierung,
					benutzer.mobiltelefon))
				.from(registrierung)
				.innerJoin(QImpfdossier.impfdossier).on(QImpfdossier.impfdossier.registrierung.eq(registrierung))
				.leftJoin(benutzer).on(registrierung.benutzerId.eq(benutzer.id))
				.where(registrierung.id.in(getUniqueOnlineErwachsene())
					.and(QImpfdossier.impfdossier.dossierStatus.in(statusList))
					.and(registrierung.registrationTimestamp.after(stichtag))
					.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(KrankheitIdentifier.COVID))
				)
				.orderBy(registrierung.registrierungsnummer.asc())
				.limit(limit)
				.fetch();
		}
	}

	/**
	 * Gibt die IDs aller Personen zurueck, welche:
	 * - Zwischen 25 und 49 Jahre alt sind
	 * - Keine Duplikate gemaess Name/Vorname/Geburtsdatum haben
	 * - Online eingegangen sind
	 * - Nicht bereits fuer eine Umfrage ausgewaehlt sind
	 */
	@NonNull
	private List<UUID> getUniqueOnlineErwachsene() {
		String query =
			"SELECT R.id "
				+ "FROM Registrierung R "
				+ "LEFT JOIN Umfrage U on R.id = U.registrierung_id "
				+ "WHERE R.registrierungsEingang = 'ONLINE_REGISTRATION' "
				+ "AND U.id IS NULL "
				+ "AND ((DATEDIFF(NOW(), R.geburtsdatum) / 365.2425)) < 50 "
				+ "AND ((DATEDIFF(NOW(), R.geburtsdatum) / 365.2425)) >= 25 "
				+ "GROUP BY R.name, R.vorname, R.geburtsdatum "
				+ "HAVING COUNT(*) <= 1";
		final Query nativeQuery = db.getEntityManager().createNativeQuery(query);
		List<String> result = nativeQuery.getResultList();

		return result.stream().map(UUID::fromString).collect(Collectors.toList());
	}

	@NonNull
	public List<Umfrage> getUmfrage(@NonNull UmfrageGruppe gruppe) {
		return db
			.select(umfrage)
			.from(umfrage)
			.where(umfrage.umfrageGruppe.eq(gruppe)
				.and(umfrage.valid.isTrue())
			)
			.fetch();
	}

	@NonNull
	public List<Umfrage> getUmfrageNichtTeilgenommen(@NonNull UmfrageGruppe gruppe) {
		return db
			.select(umfrage)
			.from(umfrage)
			.where(umfrage.umfrageGruppe.eq(gruppe)
				.and(umfrage.teilgenommen.isFalse())
				.and(umfrage.valid.isTrue())
			)
			.fetch();
	}

	public void delete(@NonNull ID<Umfrage> umfrageId) {
		db.remove(umfrageId);
	}
}
