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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Erkrankung;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.impfen.QImpfdossier;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.QRegistrierung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.smartdb.Db;
import ch.dvbern.oss.vacme.wrapper.VacmeDecoratorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.entities.impfen.QImpfdossier.impfdossier;
import static ch.dvbern.oss.vacme.entities.impfen.QImpfdossiereintrag.impfdossiereintrag;
import static ch.dvbern.oss.vacme.entities.registration.QRegistrierung.registrierung;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.KONTROLLIERT_BOOSTER;

@RequestScoped
@Transactional
@Slf4j
public class ImpfdossierRepo {

	private final Db db;

	@Inject
	public ImpfdossierRepo(Db db) {
		this.db = db;
	}

	public void create(@NonNull Impfdossier impfdossier) {
		db.persist(impfdossier);
		db.flush();
	}

	public void delete(ID<Impfdossier> impfdossierID) {
		db.remove(impfdossierID);
	}

	public void createEintrag(@NonNull Impfdossiereintrag eintrag) {
		db.persist(eintrag);
		db.flush();
	}

	private void deleteEintrag(ID<Impfdossiereintrag> eintragID) {
		db.remove(eintragID);
		db.flush();
	}

	@NonNull
	public Impfdossiereintrag addEintrag(Integer impffolgeNr, Impfdossier impfdossier) {
		Impfdossiereintrag newEintrag = new Impfdossiereintrag();
		newEintrag.setImpffolgeNr(impffolgeNr);
		newEintrag.setImpfdossier(impfdossier);
		impfdossier.getOrderedEintraege().add(newEintrag);
		createEintrag(newEintrag);
		return newEintrag;
	}

	public void deleteEintrag(@NonNull Impfdossiereintrag eintrag, @NonNull Impfdossier dossier) {
		boolean remove = dossier.getOrderedEintraege().remove(eintrag);
		if (!remove) {
			LOG.warn(
				"deleteEintrag was called on Dossier {} for Eintrag {} but it was not in list",
				dossier.getId(),
				eintrag.getId());
		}

		deleteEintrag(eintrag.toId());
	}

	@NonNull
	public Impfdossier getImpfdossier(ID<Impfdossier> id) {
		return db.get(id)
			.orElseThrow(() -> AppFailureException.entityNotFound(Impfdossier.class, id));
	}

	@NonNull
	public Collection<Impfdossier> findImpfdossiersForReg(
		@NonNull ID<Registrierung> registrierungID
	) {
		var result = db.select(QImpfdossier.impfdossier)
			.from(QImpfdossier.impfdossier)
			.where(impfdossier.registrierung.id.eq(registrierungID.getId()))
			.fetch();
		return result;
	}

	@NonNull
	public Optional<Impfdossier> findImpfdossierForReg(
		@NonNull Registrierung registrierung,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		return findImpfdossierForRegnumAndKrankheit(registrierung.getRegistrierungsnummer(), krankheitIdentifier);
	}

	@NonNull
	public Optional<Impfdossier> findImpfdossierForRegnumAndKrankheit(
		@NonNull String regNum,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		var result = db.select(QImpfdossier.impfdossier)
			.from(QImpfdossier.impfdossier)
			.innerJoin(QRegistrierung.registrierung)
			.on(QImpfdossier.impfdossier.registrierung.eq(QRegistrierung.registrierung))
			.where(QRegistrierung.registrierung.registrierungsnummer.eq(regNum)
				.and(QImpfdossier.impfdossier.krankheitIdentifier.eq(krankheitIdentifier))
			)
			.fetchOne();
		return result;
	}

	@NonNull
	public Optional<UUID> findIdOfImpfdossiereintragForImpftermin(@NonNull Impftermin termin) {
		return db.select(impfdossiereintrag.id)
			.from(impfdossiereintrag)
			.where(impfdossiereintrag.impftermin.eq(termin)).fetchOne();
	}

	@NonNull
	public Optional<Impfdossiereintrag> findImpfdossiereintragForImpftermin(@NonNull Impftermin termin) {
		return db.select(impfdossiereintrag)
			.from(impfdossiereintrag)
			.where(impfdossiereintrag.impftermin.eq(termin)).fetchOne();
	}

	public Impfdossier update(@NonNull Impfdossier impfdossier) {
		return db.merge(impfdossier);
	}

	public void updateImpfschutz(@NonNull Impfdossier impfdossier, @Nullable Impfschutz impfschutz) {
		if (impfdossier.getImpfschutz() != null) {
			if (impfschutz == null) {
				LOG.warn(
					"Es existierte fuer das Dossier {} bereits ein Impfschutz welcher nun weggefallen ist",
					impfdossier.getId().toString());
				impfdossier.setImpfschutz(null);
			} else {
				impfdossier.getImpfschutz().apply(impfschutz);
			}
		} else {
			impfdossier.setImpfschutz(impfschutz);
		}
		this.update(impfdossier);
	}

	@NonNull
	public Impfdossier createImpfdossier(
		@NonNull Registrierung registrierung,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		LOG.info("Creating Impfdossier {} for Reg {}", krankheitIdentifier, registrierung);
		Impfdossier dossier = new Impfdossier();
		final ImpfdossierStatus startStatusImpfdossier =
			VacmeDecoratorFactory.getDecorator(krankheitIdentifier).getStartStatusImpfdossier();
		dossier.setDossierStatus(startStatusImpfdossier);
		dossier.setRegistrierung(registrierung);
		dossier.setKrankheitIdentifier(krankheitIdentifier);
		dossier.getBuchung().setExternGeimpftConfirmationNeeded(krankheitIdentifier.isSupportsExternesZertifikat());
		create(dossier);
		return dossier;
	}

	@NonNull
	public Impfdossier getOrCreateImpfdossier(
		@NonNull Registrierung registrierung,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		Impfdossier impfdossier = this.findImpfdossierForReg(registrierung, krankheitIdentifier)
			.orElseGet(() -> createImpfdossier(registrierung, krankheitIdentifier));
		return impfdossier;
	}

	public void updateErkrankungen(@NonNull Impfdossier impfdossier, @NonNull List<Erkrankung> erkrankungen) {
		// mit clear und addAll die bestehende Collection abaendern, damit Hibernate alles automatisch updatet
		impfdossier.getErkrankungen().clear();
		impfdossier.getErkrankungen().addAll(erkrankungen);
		for (Erkrankung erkrankung : erkrankungen) {
			erkrankung.setImpfdossier(impfdossier);
		}
		this.update(impfdossier);
	}

	@NonNull
	public Optional<Impfdossier> getImpfdossierForGrundimpftermin(@NonNull Impftermin termin) {
		Validate.isTrue(
			!Impffolge.BOOSTER_IMPFUNG.equals(termin.getImpffolge()),
			"Die Funktion getImpfdossierForGrundimpftermin darf nur fuer Impffolge1/2 verwendet werden. "
				+ "Fuer Boostertermine getImpfdossierForBoosterImpftermin verwenden");
		final Optional<Impfdossier> registrierungOptional = db
			.select(impfdossier)
			.from(impfdossier)
			.where(impfdossier.buchung.impftermin1.eq(termin)
				.or(impfdossier.buchung.impftermin2.eq(termin))).fetchOne();
		return registrierungOptional;
	}

	@NonNull
	public Optional<Impfdossier> getImpfdossierForBoosterImpftermin(@NonNull Impftermin termin) {
		Validate.isTrue(
			Impffolge.BOOSTER_IMPFUNG.equals(termin.getImpffolge()),
			"Die Funktion getImpfdossierForBoosterImpftermin darf nur fuer Boostertermine verwendet werden. "
				+ "Fuer Impfolge 1/2 getImpfdossierForGrundimpftermin verwenden");
		final Optional<Impfdossier> registrierungOptional = db
			.select(impfdossier)
			.from(impfdossier)
			.innerJoin(impfdossiereintrag).on(impfdossier.eq(impfdossiereintrag.impfdossier))
			.where(impfdossiereintrag.impftermin.eq(termin))
			.fetchOne();
		return registrierungOptional;
	}

	@NonNull
	public List<UUID> findImpfdossiersInStatusKontrolliert(int limit) {
		return db
			.select(impfdossier.id)
			.from(impfdossier)
			.where(impfdossier.dossierStatus.in(IMPFUNG_1_KONTROLLIERT, IMPFUNG_2_KONTROLLIERT, KONTROLLIERT_BOOSTER))
			.limit(limit)
			.fetch();
	}

	@NonNull
	public List<UUID> findImpfdossiersOfPrioritaetAndStatus(
		@NonNull Prioritaet prioritaet,
		@NonNull ImpfdossierStatus... statusList) {
		List<UUID> foundRegs = db
			.select(impfdossier.id)
			.from(impfdossier)
			.innerJoin(QRegistrierung.registrierung)
			.on(QRegistrierung.registrierung.eq(impfdossier.registrierung))
			.where(registrierung.prioritaet.eq(prioritaet)
				.and(impfdossier.dossierStatus.in(statusList))
			)
			.fetch();
		return foundRegs;
	}
}
