/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.prozess;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.KantonaleBerechtigung;
import ch.dvbern.oss.vacme.entities.impfen.Krankheit;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.repo.BenutzerRepo;
import ch.dvbern.oss.vacme.repo.FragebogenRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfslotRepo;
import ch.dvbern.oss.vacme.repo.ImpfstoffRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.KrankheitRepo;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.rest.auth.BenutzerSyncFilter;
import ch.dvbern.oss.vacme.service.HashIdService;
import ch.dvbern.oss.vacme.service.KrankheitService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfdossierBuilder;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.TestdataCreationUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractProzessIT {

	private static int regIndex = 1;
	protected static final Benutzer TESTBENUTZER = TestdataCreationUtil.createBenutzer("Tester", "Fritz", "123456");
	protected static final Impfstoff IMPFSTOFF_MODERNA = TestdataCreationUtil.createImpfstoffModerna();
	protected static final Impfstoff IMPFSTOFF_AFFENPOCKEN = TestdataCreationUtil.createImpfstoffAffenpocken();

	@Inject
	protected ImpfungRepo impfungRepo;

	@Inject
	protected RegistrierungRepo registrierungRepo;

	@Inject
	BenutzerSyncFilter benutzerSyncFilter;

	@Inject
	BenutzerRepo benutzerRepo;

	@Inject
	HashIdService hashIdService;

	@Inject
	OrtDerImpfungRepo ortDerImpfungRepo;

	@Inject
	ImpfstoffRepo impfstoffRepo;

	@Inject
	FragebogenRepo fragebogenRepo;

	@Inject
	ImpfdossierRepo impfdossierRepo;

	@Inject
	ImpfterminRepo impfterminRepo;

	@Inject
	ImpfslotRepo impfslotRepo;

	@Inject
	KrankheitRepo krankheitRepo;

	@Inject
	KrankheitService krankheitService;

	@Inject
	Flyway flyway;


	@BeforeEach
	void setUp(){
		benutzerSyncFilter.switchToAdmin();

		flyway.clean();
		flyway.migrate();

		final Krankheit krankheitCovid = krankheitRepo.getOrCreateByIdentifier(KrankheitIdentifier.COVID, KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE_MIT_BEARBEITUNG);
		final Krankheit krankheitAffenpocken = krankheitRepo.getOrCreateByIdentifier(KrankheitIdentifier.AFFENPOCKEN, KantonaleBerechtigung.KANTONALE_IMPFKAMPAGNE);
		IMPFSTOFF_MODERNA.setKrankheiten(Set.of(krankheitCovid));
		IMPFSTOFF_AFFENPOCKEN.setKrankheiten(Set.of(krankheitAffenpocken));
		benutzerRepo.create(TESTBENUTZER);
		impfstoffRepo.create(IMPFSTOFF_MODERNA);
		impfstoffRepo.create(IMPFSTOFF_AFFENPOCKEN);
	}

	@NonNull
	protected Impfdossier createImpfdossierAffenpockenWithImpfungenAm(@NonNull LocalDate impfung1, @NonNull LocalDate impfung2) {
		ImpfdossierBuilder builder = ImpfdossierBuilder
			.create()
			.forKrankheit(KrankheitIdentifier.AFFENPOCKEN)
			.withRegistrierungsnummer(hashIdService.getHashFromNumber(regIndex++));
		saveBase(builder.getFragebogen(), builder.getDossier());
		builder
			.withImpfung(impfung1, 1, IMPFSTOFF_AFFENPOCKEN, KrankheitIdentifier.AFFENPOCKEN)
			.withImpfung(impfung2, 2, IMPFSTOFF_AFFENPOCKEN, KrankheitIdentifier.AFFENPOCKEN);

		saveEintraege(builder.getDossier(), builder.getImpfungen());
		saveDossierAsAbgeschlossen(builder.getDossier());
		return builder.getDossier();
	}

	@NonNull
	protected Impfdossier createImpfdossierCovidWithBoosteram(@NonNull LocalDate impfung1, @NonNull LocalDate impfung2) {
		ImpfdossierBuilder builderCovid = ImpfdossierBuilder
			.create()
			.forKrankheit(KrankheitIdentifier.COVID)
			.withRegistrierungsnummer(hashIdService.getHashFromNumber(regIndex++));
		saveBase(builderCovid.getFragebogen(), builderCovid.getDossier());

		builderCovid
			.withImpfung(impfung1, 3, IMPFSTOFF_MODERNA, KrankheitIdentifier.COVID)
			.withImpfung(impfung2, 4, IMPFSTOFF_MODERNA, KrankheitIdentifier.COVID);

		saveEintraege(builderCovid.getDossier(), builderCovid.getImpfungen());
		saveDossierAsAbgeschlossen(builderCovid.getDossier());
		return builderCovid.getDossier();
	}

	protected ImpfinformationDto createImpfdossierCovidWithBoosterAm(@NonNull LocalDate impfung1) {
		ImpfdossierBuilder builderCovid = ImpfdossierBuilder
			.create()
			.forKrankheit(KrankheitIdentifier.COVID)
			.withRegistrierungsnummer(hashIdService.getHashFromNumber(regIndex++));
		saveBase(builderCovid.getFragebogen(), builderCovid.getDossier());

		builderCovid
			.withImpfung(impfung1, 3, IMPFSTOFF_MODERNA, KrankheitIdentifier.COVID);

		saveEintraege(builderCovid.getDossier(), builderCovid.getImpfungen());
		return new ImpfinformationDto(
			KrankheitIdentifier.COVID,
			builderCovid.getDossier().getRegistrierung(),
			null,
			null,
			builderCovid.getDossier(),
			builderCovid.getImpfungen(),
			null);
	}

	private void saveBase(@NonNull Fragebogen fragebogen, @NonNull Impfdossier impfdossier) {
		fragebogenRepo.create(fragebogen);
		impfdossierRepo.create(impfdossier);
	}

	private void saveEintraege(
		@Nullable Impfdossier dossier,
		@Nullable List<Impfung> impfungen
	) {
		if (dossier == null) {
			return;
		}
		registrierungRepo.update(dossier.getRegistrierung());
		final List<Impfdossiereintrag> orderedEintraege = dossier.getImpfdossierEintraege();
		for (Impfdossiereintrag impfdossiereintrag : orderedEintraege) {
			final Impftermin termin = impfdossiereintrag.getImpftermin();
			Assertions.assertNotNull(termin);
			saveTermin(termin);
			impfdossierRepo.createEintrag(impfdossiereintrag);
		}

		Optional<Benutzer> benutzerOpt = benutzerRepo.getById(TESTBENUTZER.toId());
		Benutzer benutzer = benutzerOpt.get();

		if (impfungen != null) {
			for (Impfung impfung : impfungen) {
				// Replace static Impfstoff with managed entity Impfstoff
				Optional<Impfstoff> impfstoffOpt = impfstoffRepo.getById(impfung.getImpfstoff().toId());
				impfung.setImpfstoff(impfstoffOpt.get());
				impfung.setBenutzerDurchfuehrend(benutzer);
				impfung.setBenutzerVerantwortlicher(benutzer);
				impfung.setKantonaleBerechtigung(krankheitService.getKantonaleBerechtigung(dossier.getKrankheitIdentifier()));
				impfungRepo.create(impfung);
			}
		}
	}

	private void saveTermin(@NonNull Impftermin termin) {
		final OrtDerImpfung ortDerImpfung = termin.getImpfslot().getOrtDerImpfung();
		if (ortDerImpfungRepo.getById(ortDerImpfung.toId()).isEmpty()) {
			ortDerImpfungRepo.create(ortDerImpfung);
		}
		impfslotRepo.create(termin.getImpfslot());
		impfterminRepo.create(termin);
	}

	private void saveDossierAsAbgeschlossen(@NonNull Impfdossier dossier) {
		final Impfdossier impfdossier = impfdossierRepo.getImpfdossier(dossier.getImpfdossierId());
		impfdossier.setTimestampZuletztAbgeschlossen(dossier.getTimestampZuletztAbgeschlossen());
		impfdossier.setVollstaendigerImpfschutzTyp(VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME);
		impfdossierRepo.update(impfdossier);
	}

	@Nullable
	protected Impfung getNewestImpfung(
		@NonNull Registrierung registrierung,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		final Optional<ImpfinformationDto> infos = impfungRepo.getImpfinformationenOptional(
			registrierung.getRegistrierungsnummer(),
			krankheitIdentifier);
		Assertions.assertTrue(infos.isPresent());
		return ImpfinformationenService.getNewestVacmeImpfung(infos.get());
	}
}
