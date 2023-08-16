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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.embeddables.Buchung;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossiereintrag;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.QImpftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.service.wellapi.WellApiService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.smartdb.Db;
import ch.dvbern.oss.vacme.smartdb.MySQLJPATemplates;
import ch.dvbern.oss.vacme.util.ImpfterminOffsetWuerfel;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import io.opentelemetry.extension.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Route;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

@RequestScoped
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ImpfterminRepo {

	private final Db db;
	private final ImpfungRepo impfungRepo;
	private final WellApiService wellApiService;
	private final VacmeSettingsService vacmeSettingsService;

	@Nullable
	private ImpfterminOffsetWuerfel wuerfel = null;


	@NonNull
	private ImpfterminOffsetWuerfel getWuerfel() {
		if (wuerfel == null) {
			wuerfel = new ImpfterminOffsetWuerfel(
				vacmeSettingsService.getImpfslotCreationSettingsDTO().getDuration(),
				vacmeSettingsService.getTerminslotOffsetGroups(),
				vacmeSettingsService.getSlotOffsetMaxTermineToDivide(),
				vacmeSettingsService.isSlotOffsetDeterministicWhenLowCapacity());
		}
		return wuerfel;
	}

	public void create(@NonNull Impftermin impftermin) {
		db.persist(impftermin);
	}

	@NonNull
	public Optional<Impftermin> getById(@NonNull ID<Impftermin> id) {
		return db.get(id);
	}

	@NonNull
	public List<Impftermin> findAll() {
		return db.findAll(QImpftermin.impftermin);
	}

	@NonNull
	public List<Impfslot> findFreieImpfslots(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull Impffolge impffolge,
		@NonNull LocalDate date,
		@NonNull KrankheitIdentifier krankheit,
		boolean isPortalUser
	) {
		if (isPortalUser) {
			date = dateMustBeAfterTomorrow(date);
		}
		final LocalDateTime minDateTime = date.atStartOfDay();
		final LocalDateTime maxDateTime = date.plusDays(1).atStartOfDay();

		// Achtung, diese Methode gibt meine eigenen reservierten Termine nicht zurueck!
		// Problem: Wir wissen auf dem Backend die Registrierungsnummer nicht
		return db
			.select(QImpfslot.impfslot)
			.distinct()
			.from(QImpfslot.impfslot)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.impfslot.eq(QImpfslot.impfslot))
			.where(QImpftermin.impftermin.gebucht.isFalse()
				.and(QImpfslot.impfslot.krankheitIdentifier.eq(krankheit))
				.and(QImpfslot.impfslot.ortDerImpfung.eq(ortDerImpfung))
				.and(QImpftermin.impftermin.impffolge.eq(impffolge))
				.and(QImpfslot.impfslot.zeitfenster.bis.between(minDateTime, maxDateTime)
					.and(isTerminNichtReserviert()))
			)
			.fetch();
	}

	@NotNull
	private LocalDate dateMustBeAfterTomorrow(@NotNull LocalDate date) {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		if (date.isBefore(tomorrow)) {
			date = tomorrow;  // we can only search for free slots beginning from tomorrow midnight
		}
		return date;
	}

	public boolean hasAtLeastFreieImpfslots(@NonNull Integer minTermin, @NonNull KrankheitIdentifier krankheit) {
		String queryFileName = "/db/queries/hasFreieTermin.sql";
		InputStream inputStream = ImpfterminRepo.class.getResourceAsStream(queryFileName);
		Objects.requireNonNull(inputStream);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			var sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}

			String query = sb.toString();

			Query nativeQuery = this.db.getEntityManager().createNativeQuery(query);
			nativeQuery.setParameter("bisDate", LocalDate.now().plusDays(1).atStartOfDay());
			nativeQuery.setParameter("minTermin", minTermin);
			nativeQuery.setParameter("krankheitIdentifier", krankheit.name());
			return !nativeQuery.getResultList().isEmpty();
		} catch (IOException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("Could not load sql query from file " + queryFileName);
		}
	}

	@WithSpan
	@Nullable
	public LocalDateTime findNextFreierImpftermin(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull Impffolge impffolge,
		@NonNull LocalDate minDate,
		@NonNull LocalDate maxDate,
		@NonNull KrankheitIdentifier krankheit,
		boolean includeSameDayTermine
	) {
		if (!includeSameDayTermine) {
			minDate = dateMustBeAfterTomorrow(minDate);
			maxDate = dateMustBeAfterTomorrow(maxDate);
		}
		final LocalDateTime minDateTime = minDate.atStartOfDay();
		final LocalDateTime maxDateTime = maxDate.plusDays(1).atStartOfDay();

		// Achtung, diese Methode gibt meine eigenen reservierten Termine nicht zurueck!
		// Problem: Wir wissen auf dem Backend die Registrierungsnummer nicht
		return db
			.select(QImpfslot.impfslot.zeitfenster.bis)
			.from(QImpfslot.impfslot)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.impfslot.eq(QImpfslot.impfslot))
			.where(
				QImpftermin.impftermin.gebucht.isFalse()
					.and(QImpfslot.impfslot.ortDerImpfung.eq(ortDerImpfung))
					.and(QImpfslot.impfslot.krankheitIdentifier.eq(krankheit))
					.and(QImpftermin.impftermin.impffolge.eq(impffolge))
					.and(QImpfslot.impfslot.zeitfenster.bis.between(minDateTime, maxDateTime))
					.and(isTerminNichtReserviert()))
			.orderBy(QImpfslot.impfslot.zeitfenster.bis.asc())
			.fetchFirst();
	}

	@NonNull
	public List<Impftermin> findImpftermine(@NonNull Impfslot slot, @NonNull Impffolge impffolge) {
		final List<Impftermin> freieTermine = db
			.select(QImpftermin.impftermin)
			.from(QImpftermin.impftermin)
			.where(QImpftermin.impftermin.impfslot.eq(slot)
				.and(QImpftermin.impftermin.impffolge.eq(impffolge)))
			.fetch();
		return freieTermine;
	}

	public List<ID<Impftermin>> findGebuchteTermine(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull Impffolge impffolge,
		@NonNull LocalDate minDate,
		@NonNull LocalDate maxDate
	) {
		final LocalDateTime minDateTime = minDate.atStartOfDay();
		final LocalDateTime maxDateTime = maxDate.plusDays(1).atStartOfDay();

		List<UUID> terminUuids = db
			.select(QImpftermin.impftermin.id)
			.distinct()
			.from(QImpfslot.impfslot)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.impfslot.eq(QImpfslot.impfslot))
			.where(QImpftermin.impftermin.gebucht.isTrue()
				.and(QImpfslot.impfslot.ortDerImpfung.eq(ortDerImpfung))
				.and(QImpftermin.impftermin.impffolge.eq(impffolge))
				.and(QImpfslot.impfslot.zeitfenster.bis.between(minDateTime, maxDateTime))
			)
			.fetch();
		return terminUuids.stream().map(Impftermin::toId).collect(Collectors.toList());
	}

	public List<Impftermin> findAlleTermine(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull Impffolge impffolge,
		@NonNull LocalDate minDate,
		@NonNull LocalDate maxDate
	) {
		final LocalDateTime minDateTime = minDate.atStartOfDay();
		final LocalDateTime maxDateTime = maxDate.plusDays(1).atStartOfDay();

		return db
			.select(QImpftermin.impftermin)
			.distinct()
			.from(QImpfslot.impfslot)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.impfslot.eq(QImpfslot.impfslot))
			.where(QImpfslot.impfslot.ortDerImpfung.eq(ortDerImpfung)
				.and(QImpftermin.impftermin.impffolge.eq(impffolge))
				.and(QImpfslot.impfslot.zeitfenster.bis.between(minDateTime, maxDateTime))
			)
			.fetch();
	}

	@Nullable
	public Impftermin findFreienImpftermin(@NonNull Impfslot slot, @NonNull Impffolge impffolge) {
		return db
			.select(QImpftermin.impftermin)
			.from(QImpftermin.impftermin)
			.innerJoin(QImpftermin.impftermin.impfslot)
			.where(QImpftermin.impftermin.gebucht.isFalse()
				.and(QImpftermin.impftermin.impfslot.eq(slot))
				.and(QImpftermin.impftermin.impffolge.eq(impffolge))
				.and(isTerminNichtReserviert()))
			.fetchFirst();
	}

	@Nullable
	public Impftermin findFreienImpfterminRandomForSlot(@NonNull Impfslot slot, @NonNull Impffolge impffolge) {
		JPAQuery<Route> query = new JPAQuery<>(db.getEntityManager().em(), MySQLJPATemplates.DEFAULT);
		return query
			.select(QImpftermin.impftermin)
			.from(QImpftermin.impftermin)
			.innerJoin(QImpftermin.impftermin.impfslot)
			.where(QImpftermin.impftermin.gebucht.isFalse()
				.and(QImpftermin.impftermin.impfslot.eq(slot))
				.and(QImpftermin.impftermin.impffolge.eq(impffolge))
				.and(isTerminNichtReserviert())
			)
			.orderBy(NumberExpression.random().asc())
			.fetchFirst();
	}

	@Nullable
	public Impftermin findFreienImpfterminWithLock(@NonNull Impfslot slot, @NonNull Impffolge impffolge) {
		return db
			.select(QImpftermin.impftermin)
			.from(QImpftermin.impftermin)
			.innerJoin(QImpftermin.impftermin.impfslot)
			.where(QImpftermin.impftermin.gebucht.isFalse()
				.and(QImpftermin.impftermin.impfslot.eq(slot))
				.and(QImpftermin.impftermin.impffolge.eq(impffolge))
				.and(isTerminNichtReserviert()))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchFirst();
	}

	@Nullable
	public Impftermin findMeinenReserviertenOrFreienImpftermin(
		@NonNull Registrierung registrierung, @NonNull Impfslot slot, @NonNull Impffolge impffolge
	) {
		// wenn Reservation ausgeschaltet dann direkt einen freiden Termin suchen und zurueckgeben
		if (!vacmeSettingsService.isTerminReservationEnabled()) {
			if (vacmeSettingsService.isTerminVergabeRandomEnabled()) {
				return findFreienImpfterminRandomForSlot(slot, impffolge);
			} else if (vacmeSettingsService.isTerminVergabeLockEnabled()) {
				return findFreienImpfterminWithLock(slot, impffolge);
			}
			return findFreienImpftermin(slot, impffolge);
		}

		final Impftermin impftermin = db
			.selectFrom(QImpftermin.impftermin)
			.innerJoin(QImpftermin.impftermin.impfslot)
			.where(QImpftermin.impftermin.gebucht.isFalse()
				.and(QImpftermin.impftermin.impfslot.eq(slot))
				.and(QImpftermin.impftermin.impffolge.eq(impffolge))
				.and(isFuerMichReservierterTermin(registrierung)))
			.fetchFirst();
		if (impftermin != null) {
			return impftermin;
		}
		LOG.warn("VACME-WARN: Kein reservierter Termin gefunden fuer Registrierung {}", registrierung.getRegistrierungsnummer());
		// Wir schauen, ob es zufaellig noch einen freien gibt
		return findFreienImpftermin(slot, impffolge);
	}

	public void delete(@NonNull ID<Impftermin> terminId) {
		db.remove(terminId);
	}

	public void terminReservieren(
		@NonNull Registrierung registrierung,
		@NonNull Impftermin termin
	) {
		if (!vacmeSettingsService.isTerminReservationEnabled()) {
			return;
		}
		if (termin.getTimestampReserviert() != null
				&& termin.getTimestampReserviert().isAfter(LocalDateTime.now().minusMinutes(vacmeSettingsService.getTerminReservationDauerInMinutes()))) {
			if (termin.getRegistrierungsnummerReserviert() != null && termin.getRegistrierungsnummerReserviert().equals(registrierung.getRegistrierungsnummer())) {
				// Der Termin ist schon fuer mich reserviert
				return;
			}
			// Der Termin ist fuer jemand anderes reserviert!
			throw AppValidationMessage.IMPFTERMIN_WITH_EXISTING_RESERVATION.create(termin.getId());
		}
		// Allfaellige bisherige Reservationen loeschen
		terminReservationAufheben(registrierung, termin.getImpffolge());
		// und den gewuenschten Termin reservieren
		termin.setTimestampReserviert(LocalDateTime.now());
		termin.setRegistrierungsnummerReserviert(registrierung.getRegistrierungsnummer());
		db.flush();
	}

	private void terminReservationAufheben(@NonNull Registrierung registrierung, @NonNull Impffolge impffolge) {
		if (!vacmeSettingsService.isTerminReservationEnabled()) {
			return;
		}
		db.update(QImpftermin.impftermin)
			.setNull(QImpftermin.impftermin.timestampReserviert)
			.setNull(QImpftermin.impftermin.registrierungsnummerReserviert)
			.where(QImpftermin.impftermin.impffolge.eq(impffolge)
				.and(isFuerMichReservierterTermin(registrierung)))
			.execute();
	}

	public void abgelaufeneTerminReservationenAufheben() {
		if (!vacmeSettingsService.isTerminReservationEnabled()) {
			return;
		}
		final long countDeleted = db.update(QImpftermin.impftermin)
			.setNull(QImpftermin.impftermin.timestampReserviert)
			.setNull(QImpftermin.impftermin.registrierungsnummerReserviert)
			.where(terminReservationAbgelaufen())
			.execute();
		LOG.debug("{} Reservationen sind abgelaufen und wurden entfernt.", countDeleted);
	}

	public long getAnzahlFreieTermine(
		@NonNull OrtDerImpfung ortDerImpfung,
		@NonNull Impffolge impffolge,
		@NonNull LocalDate start,
		@NonNull LocalDate end,
		@NonNull KrankheitIdentifier krankheit
	) {
		final LocalDateTime minDateTime = start.atStartOfDay();
		final LocalDateTime maxDateTime = end.plusDays(1).atStartOfDay();
		return db
			.select(QImpftermin.impftermin)
			.from(QImpfslot.impfslot)
			.innerJoin(QImpftermin.impftermin).on(QImpftermin.impftermin.impfslot.eq(QImpfslot.impfslot))
			.where(QImpftermin.impftermin.gebucht.isFalse()
				.and(QImpfslot.impfslot.ortDerImpfung.eq(ortDerImpfung))
				.and(QImpfslot.impfslot.krankheitIdentifier.eq(krankheit))
				.and(QImpftermin.impftermin.impffolge.eq(impffolge))
				.and(QImpfslot.impfslot.zeitfenster.bis.between(minDateTime, maxDateTime))
			)
			.fetchCount();
	}

	public long getAnzahlGebuchteTermine(
		@NonNull Impfslot impfslot
	) {
		return db
			.select(QImpftermin.impftermin.id)
			.from(QImpfslot.impfslot)
			.innerJoin(QImpftermin.impftermin).on(QImpfslot.impfslot.eq(QImpftermin.impftermin.impfslot))
			.where(QImpftermin.impftermin.gebucht.isTrue()
				.and(QImpfslot.impfslot.id.eq(impfslot.getId()))
			)
			.fetchCount();
	}

	public void termineSpeichern(
		@NonNull Impfdossier impfdossier,
		@Nullable Impftermin termin1,
		@Nullable Impftermin termin2
	) {
		termin1Speichern(impfdossier, termin1);
		termin2Speichern(impfdossier, termin2);
	}

	public void termin1Speichern(
		@NonNull Impfdossier impfdossier,
		@Nullable Impftermin termin1
	) {
		final Buchung buchung = impfdossier.getBuchung();
		final Registrierung registrierung = impfdossier.getRegistrierung();
		if (buchung.getImpftermin1() != null && buchung.getImpftermin1().equals(termin1)) {
			// Der gleiche Termin ist schon angehaengt, wir muessen nichts mehr machen
			return;
		}
		if (buchung.getImpftermin1() != null) {
			this.termin1Freigeben(impfdossier);
		}
		if (termin1 != null) {
			terminBuchen(registrierung, termin1, null);
			buchung.setImpftermin1FromImpfterminRepo(termin1);
			if (buchung.getAbgesagteTermine() != null) {
				// Ein allfaelliger vorher vom Odi abgesagter Termin darf nicht mehr gespeichert bleiben
				buchung.getAbgesagteTermine().setTermin1(null);
			}
		}
		db.persist(registrierung);
		db.flush();
	}

	public void termin2Speichern(
		@NonNull Impfdossier impfdossier,
		@Nullable Impftermin termin2
	) {
		final Buchung buchung = impfdossier.getBuchung();
		final Registrierung registrierung = impfdossier.getRegistrierung();
		if (buchung.getImpftermin2() != null && buchung.getImpftermin2().equals(termin2)) {
			// Der gleiche Termin ist schon angehaengt, wir muessen nichts mehr machen
			return;
		}
		if (buchung.getImpftermin2() != null) {
			this.termin2Freigeben(impfdossier);
		}
		if (termin2 != null) {
			terminBuchen(registrierung, termin2, null);
			buchung.setImpftermin2FromImpfterminRepo(termin2);
			if (buchung.getAbgesagteTermine() != null) {
				// Ein allfaelliger vorher vom Odi abgesagter Termin darf nicht mehr gespeichert bleiben
				buchung.getAbgesagteTermine().setTermin2(null);
			}
		}
		db.persist(registrierung);
		db.flush();
	}

	public void boosterTerminSpeichern(
		@NonNull Impfdossiereintrag impfdossiereintrag,
		@NonNull Impftermin termin
	) {
		if (impfdossiereintrag.getImpftermin() != null && impfdossiereintrag.getImpftermin().equals(termin)) {
			// Der gleiche Termin ist schon angehaengt, wir muessen nichts mehr machen
			return;
		}
		if (impfdossiereintrag.getImpftermin() != null) {
			this.boosterTerminFreigeben(impfdossiereintrag);
		}
		terminBuchen(impfdossiereintrag.getImpfdossier().getRegistrierung(), termin, impfdossiereintrag);

		if (impfdossiereintrag.getImpfdossier().getBuchung().getAbgesagteTermine() != null) {
			// Ein allfaelliger vorher vom Odi abgesagter Termin darf nicht mehr gespeichert bleiben
			impfdossiereintrag.getImpfdossier().getBuchung().getAbgesagteTermine().setTerminN(null);
		}
		db.persist(impfdossiereintrag);
		db.flush();
	}

	private void terminBuchen(@NonNull Registrierung registrierung, @NonNull Impftermin termin, @Nullable Impfdossiereintrag eintragIfBooster) {
		// Bevor wir den Termin speichern: Nochmals sicherstellen, dass keine Impfung an diesem Termin haengt
		assertTerminHasNoImpfung(termin);
		// Sicherstellen, dass der Termin entweder fuer mich reserviert, oder gar nicht reserviert ist
		assertTerminNotReserviertForAnotherRegistration(registrierung, termin);
		// Offset wuerfeln. Es ist egal ob evtl. schon ein Offset von einer frueheren Buchung drauf ist.
		getWuerfel().wuerfleOffset(
			termin,
			(currentTermin) -> this.getAnzahlGebuchteTermine(currentTermin.getImpfslot())
		);
		// und auf gebucht setzen
		termin.setGebuchtFromImpfterminRepo(true);
		// wenn dossiereintrag vorhanden ist (case N-Impfung), dann auch dort den Termin setzen
		if (eintragIfBooster != null) {
			eintragIfBooster.setImpfterminFromImpfterminRepo(termin);
		}
		// Well informieren
		wellApiService.sendAppointmentInfoToWell(eintragIfBooster);
	}

	public void termine1Und2Freigeben(@NonNull Impfdossier impfdossier) {
		termin1Freigeben(impfdossier);
		termin2Freigeben(impfdossier);
	}

	public void termin1Freigeben(@NonNull Impfdossier impfdossier) {
		final Impftermin impftermin1 = impfdossier.getBuchung().getImpftermin1();
		if (impftermin1 != null) {
			// Bevor wir den Termin freigeben: Nochmals sicherstellen, dass keine Impfung an diesem Termin haengt
			terminFreigeben(impftermin1, null);
			impfdossier.getBuchung().setImpftermin1FromImpfterminRepo(null);
			db.persist(impftermin1);
			db.flush();
		}
	}

	public void termin2Freigeben(@NonNull Impfdossier impfdossier) {
		final Impftermin impftermin2 = impfdossier.getBuchung().getImpftermin2();
		if (impftermin2 != null){
			// Bevor wir den Termin freigeben: Nochmals sicherstellen, dass keine Impfung an diesem Termin haengt
			terminFreigeben(impftermin2, null);
			impfdossier.getBuchung().setImpftermin2FromImpfterminRepo(null);
			db.persist(impftermin2);
			db.flush();
		}
	}

	public void boosterTerminFreigeben(@NonNull Impfdossiereintrag impfdossiereintrag) {
		Impftermin impftermin = impfdossiereintrag.getImpftermin();
		if (impftermin != null) {
			// Bevor wir den Termin freigeben: Nochmals sicherstellen, dass keine Impfung an diesem Termin haengt
			terminFreigeben(impftermin, impfdossiereintrag);
			impfdossiereintrag.setImpfterminFromImpfterminRepo(null);
			db.persist(impftermin);
			db.flush();
		}
	}

	public boolean hasAnyBoosterTerminInOdis(@NonNull Impfdossier impfdossier, @NonNull Set<OrtDerImpfung> odis) {
		final Impftermin impfterminOrNull = db.select(QImpftermin.impftermin)
			.from(QImpftermin.impftermin)
			.innerJoin(QImpfslot.impfslot).on(QImpftermin.impftermin.impfslot.eq(QImpfslot.impfslot))
			.innerJoin(QImpfdossiereintrag.impfdossiereintrag).on(QImpftermin.impftermin.eq(QImpfdossiereintrag.impfdossiereintrag.impftermin))
			.innerJoin(QImpfdossier.impfdossier).on(QImpfdossiereintrag.impfdossiereintrag.impfdossier.eq(QImpfdossier.impfdossier))
			.where(QImpfslot.impfslot.ortDerImpfung.in(odis)
				.and(QImpfdossier.impfdossier.eq(impfdossier)))
			.fetchFirst();
		return impfterminOrNull != null;
	}

	private void terminFreigeben(@NonNull Impftermin termin, @Nullable Impfdossiereintrag impfdossiereintragIfBooster) {
		assertTerminHasNoImpfung(termin);
		termin.setGebuchtFromImpfterminRepo(false);
		termin.setRegistrierungsnummerReserviert(null);
		termin.setTimestampReserviert(null);
		// Den Offset zuruecksetzen, falls die Kapazitaet spaeter vermindert wird und kein Offset mehr gebraucht wird
		termin.setOffsetInMinutes(0);
		// Well informieren
		wellApiService.deleteAppointmentInfoInWell(impfdossiereintragIfBooster);
	}

	private void assertTerminHasNoImpfung(@NonNull Impftermin termin) {
		final Optional<Impfung> existingImpfung = impfungRepo.getByImpftermin(termin);
		if (existingImpfung.isPresent()) {
			throw AppValidationMessage.IMPFTERMIN_WITH_EXISTING_IMPFUNG.create(termin.getId());
		}
	}

	private void assertTerminNotReserviertForAnotherRegistration(@NonNull Registrierung registrierung, @NonNull Impftermin termin) {

		if (!registrierung.getRegistrierungsnummer().equals(termin.getRegistrierungsnummerReserviert())
			&& termin.getTimestampReserviert() != null
			&& termin.getTimestampReserviert().isAfter(LocalDateTime.now().minusMinutes(vacmeSettingsService.getTerminReservationDauerInMinutes()))) {
				throw AppValidationMessage.IMPFTERMIN_WITH_EXISTING_RESERVATION.create(termin.getId());

		}
	}

	@NonNull
	private Predicate isTerminNichtReserviert() {
		if (vacmeSettingsService.isTerminReservationEnabled()) {
			return QImpftermin.impftermin.timestampReserviert.isNull()
				.or(terminReservationAbgelaufen());
		}
		// Expression, die immer TRUE ist
		return Expressions.asBoolean(Expressions.constant(true)).isTrue();
	}


	@NonNull
	private Predicate isFuerMichReservierterTermin(@NonNull Registrierung registrierung) {
		if (vacmeSettingsService.isTerminReservationEnabled()) {
			return QImpftermin.impftermin.registrierungsnummerReserviert.eq(registrierung.getRegistrierungsnummer())
				.and((terminReservationAbgelaufen().not()));
		}
		// Expression, die immer FALSE ist
		return Expressions.asBoolean(Expressions.constant(true)).isTrue();
	}

	@NonNull
	private Predicate terminReservationAbgelaufen() {
		return QImpftermin.impftermin.timestampReserviert.before(LocalDateTime.now().minusMinutes(vacmeSettingsService.getTerminReservationDauerInMinutes()));
	}
}
