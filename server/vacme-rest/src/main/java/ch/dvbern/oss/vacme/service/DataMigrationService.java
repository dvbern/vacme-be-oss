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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.embeddables.Buchung;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.migration.Migration;
import ch.dvbern.oss.vacme.entities.migration.MigrationRateLimit;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Personenkontrolle;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.enums.ImpfstoffNamesJax;
import ch.dvbern.oss.vacme.jax.korrektur.ImpfungOdiKorrekturJax;
import ch.dvbern.oss.vacme.jax.migration.AdresseMigrationJax;
import ch.dvbern.oss.vacme.jax.migration.ImpfungGLNPatchMigrationJax;
import ch.dvbern.oss.vacme.jax.migration.ImpfungMigrationJax;
import ch.dvbern.oss.vacme.jax.migration.RegistrierungGLNPatchMigrationJax;
import ch.dvbern.oss.vacme.jax.migration.RegistrierungMigrationJax;
import ch.dvbern.oss.vacme.jax.migration.RegistrierungPLZPatchMigrationJax;
import ch.dvbern.oss.vacme.jax.migration.validation.ValidateCheckIdOrGlnPresent;
import ch.dvbern.oss.vacme.repo.FragebogenRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import ch.dvbern.oss.vacme.repo.MigrationRateLimitRepo;
import ch.dvbern.oss.vacme.repo.MigrationRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppException;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import static ch.dvbern.oss.vacme.shared.util.Constants.COVOVAX_UUID;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_BENUTZER;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_ALTERSHEIM;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_ANDERE;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_APOTHEKE;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_HAUSARZT;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_IMPFTENTRUM;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_MOBIL;
import static ch.dvbern.oss.vacme.shared.util.Constants.DUMMY_MIGRATION_ODI_SPITAL;
import static ch.dvbern.oss.vacme.shared.util.Constants.SINOPHARM_UUID;
import static ch.dvbern.oss.vacme.shared.util.Constants.SINOVAC_UUID;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DataMigrationService {

	private static final LocalDateTime DEFAULT_MIGRATION_REGISTRATION_DATE = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
	private static final Comparator<Impffolge> IMPFFOLGE_COMPARATOR = new Comparator<Impffolge>() {
		@Override
		public int compare(Impffolge o1, Impffolge o2) {
			return Integer.compare(getAssignedValue(o1), getAssignedValue(o2));
		}

		int getAssignedValue(Impffolge impffolge) {
			switch (impffolge) {
			case ERSTE_IMPFUNG:
				return 0;
			case ZWEITE_IMPFUNG:
				return 1;
			default:
				return Integer.MAX_VALUE;
			}
		}
	};

	private final RegistrierungRepo registrierungRepo;
	private final ImpfungRepo impfungRepo;
	private final MigrationRateLimitRepo migrationRateLimitRepo;
	private final OrtDerImpfungService ortDerImpfungService;
	private final ImpfstoffService impfstoffService;
	private final KorrekturService korrekturService;
	private final ImpfinformationenService impfinformationenService;
	private final StammdatenService stammdatenService;
	private final FragebogenRepo fragebogenRepo;
	private final TerminbuchungService terminbuchungService;
	private final BenutzerService benutzerService;
	private final ImpfterminRepo impfterminRepo;
	private final MigrationRepo migrationRepo;
	private final ImpfdossierService impfdossierService;

	private void validateUniqueImpffolge(Stream<?> stream) {
		if (!stream.allMatch(new HashSet<>()::add)) {
			throw new AppFailureException("The impfungen array can't contain duplicate Impffolge keys");
		}
	}

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public String migrateData(RegistrierungMigrationJax request) {
		// validate Impffolge is unique in list
		validateUniqueImpffolge(request.getImpfungen().stream().map(ImpfungMigrationJax::getImpffolge));

		Impfdossier impfdossier = getOrCreateRegistrierung(request);
		Registrierung registrierung = impfdossier.getRegistrierung();
		Fragebogen fragebogen = deleteAndCreateFragebogen(request, registrierung);
		Personenkontrolle personenkontrolle = impfdossier.getOrCreatePersonenkontrolle();
		final Buchung buchung = impfdossier.getBuchung();

		// Liste umsortieren, da fuer die 2 Impfung mehrere Validierungen gibt die sich auf die 1 Impfung beziehen
		List<ImpfungMigrationJax> sortedList = request.getImpfungen()
			.stream()
			.sorted((o1, o2) -> IMPFFOLGE_COMPARATOR.compare(o1.getImpffolge(), o2.getImpffolge()))
			.collect(Collectors.toList());
		sortedList.forEach(impfRequest -> {
			ValidationUtil.validateFlags(impfRequest);
			if (Impffolge.ERSTE_IMPFUNG == impfRequest.getImpffolge()) {
				Impftermin oldTermin1 = buchung.getImpftermin1();
				Impftermin termin1 = getOrCreateImpfterminForMigration(impfdossier, impfRequest);
				if (buchung.getImpftermin2() == null) {
					impfdossier.setDossierStatus(ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT);
				}
				ValidationUtil.validateImpfungMinDate(termin1);
				updateOrCreateImpfung(impfRequest, oldTermin1, termin1);

				ImpfungkontrolleTermin impfungkontrolleTermin1 = updateOrCreateImpfungkontrolleTermin(impfRequest, personenkontrolle.getKontrolleTermin1());
				personenkontrolle.setKontrolleTermin1(impfungkontrolleTermin1);
			} else {
				Impftermin oldTermin2 = buchung.getImpftermin2();
				Impftermin termin2 = getOrCreateImpfterminForMigration(impfdossier, impfRequest);

				ValidationUtil.validateImpfungMinDate(termin2);
				updateOrCreateImpfung(impfRequest, oldTermin2, termin2);
				Impfung impfung2 = impfungRepo.getByImpftermin(termin2).get();
				if (buchung.getImpftermin1() != null) {
					final ImpfinformationDto infos =
						impfinformationenService.getImpfinformationen(
							registrierung.getRegistrierungsnummer(),
							KrankheitIdentifier.COVID);
					impfdossier.setStatusToAbgeschlossen(infos, impfung2);
				}

				Optional.ofNullable(buchung.getImpftermin1()).orElseThrow(() -> new AppFailureException("Bei der zweiten Impfung muss zwingend ein "
					+ "Termin 1 vorhanden sein"));
				final Impfung impfung1 = impfungRepo.getByImpftermin(buchung.getImpftermin1())
					.orElseThrow(() -> AppFailureException.entityNotFound(Impfung.class, buchung.getImpftermin1()));
				if (!impfung1.getImpfstoff().equals(impfung2.getImpfstoff())) {
					LOG.warn("VACME-MIGRATION not same Impfstoff {} {}", impfung1.getId(), impfung2.getId());
				}
				ImpfungkontrolleTermin impfungkontrolleTermin2 = updateOrCreateImpfungkontrolleTermin(impfRequest, personenkontrolle.getKontrolleTermin2());
				personenkontrolle.setKontrolleTermin2(impfungkontrolleTermin2);
			}
		});
		// Nur wenn in dem Request explizit das Flag auf TRUE gesetzt wird, hat es mehr prio. und ueberschreibt den durch die Logik kalkulierten Wert.
		if (request.getVollstaendigerImpfschutz() != null && request.getVollstaendigerImpfschutz()) {
			if (buchung.getImpftermin2() != null) {
				Impfung impfung2 = impfungRepo.getByImpftermin(buchung.getImpftermin2()).get();
				final ImpfinformationDto infos =
					impfinformationenService.getImpfinformationen(
						registrierung.getRegistrierungsnummer(),
						KrankheitIdentifier.COVID);
				impfdossier.setStatusToAbgeschlossen(infos, impfung2); // wenn 2 impfungen vorhanden sind ignorieren wir das flag
			} else {
				throw new IllegalStateException("PCR-Testdatum fehlt in Migration, aber die Migration ist schliesslich deprecated");
			}
		}
		fragebogenRepo.update(fragebogen);
		// check order after updating both termine
		if (buchung.getImpftermin1() != null && buchung.getImpftermin2() != null) {
			ValidationUtil.validateFirstTerminBeforeSecond(buchung.getImpftermin1(), buchung.getImpftermin2());
		}

		return registrierung.getRegistrierungsnummer();
	}

	@NotNull
	private ImpfungkontrolleTermin updateOrCreateImpfungkontrolleTermin(@NotNull ImpfungMigrationJax impfRequest,
		@Nullable ImpfungkontrolleTermin impfungkontrolleTerminInput) {
		ImpfungkontrolleTermin impfungkontrolleTermin = impfungkontrolleTerminInput == null ? new ImpfungkontrolleTermin() : impfungkontrolleTerminInput;
		impfungkontrolleTermin.setTimestampKontrolle(impfRequest.getImpfdatum().atTime(LocalTime.NOON));
		impfungkontrolleTermin.setIdentitaetGeprueft(impfRequest.isIdentitaetGeprueft());
		return impfungkontrolleTermin;
	}

	@NotNull
	private Impfdossier getOrCreateRegistrierung(@NotNull RegistrierungMigrationJax request) {
		Registrierung registrierung = registrierungRepo.getByExternalId(request.getExternalId()).orElseGet(Registrierung::new);
		registrierung.setExternalId(request.getExternalId());
		registrierung.setAnonymisiert(request.isAnonymisiert());
		registrierung.setGeschlecht(request.getGeschlecht());
		registrierung.setName(request.getName());
		registrierung.setVorname(request.getVorname());
		registrierung.setGeburtsdatum(request.getGeburtsdatum());
		registrierung.setAdresse(mapAdresse(request.getAdresse()));
		registrierung.setMail(request.getEmail());
		registrierung.setTelefon(request.getTelefon());
		registrierung.setKrankenkasse(request.getKrankenkasse());
		registrierung.setKrankenkasseKartenNrAndArchive(request.getKrankenkassennummer());
		registrierung.setAuslandArt(request.getAuslandArt());
		registrierung.setAbgleichElektronischerImpfausweis(request.isAbgleichElektronischerImpfausweis());
		registrierung.setRegistrierungsEingang(RegistrierungsEingang.DATA_MIGRATION);
		registrierung.setPrioritaet(Prioritaet.W);
		registrierung.setRegistrationTimestamp(DEFAULT_MIGRATION_REGISTRATION_DATE);
		if (registrierung.getRegistrierungsnummer() == null) {
			registrierung.setRegistrierungsnummer(stammdatenService.createUniqueRegistrierungsnummer());
		}
		Registrierung reg =  registrierungRepo.update(registrierung);

		return impfdossierService.getOrCreateImpfdossier(reg, KrankheitIdentifier.COVID);
	}

	@NotNull
	private Fragebogen deleteAndCreateFragebogen(@NotNull RegistrierungMigrationJax request, @NotNull Registrierung registrierung) {
		// delete existing Fragebogen and create a new Instance
		Optional<Fragebogen> fragebogenDB = fragebogenRepo.getByRegistrierung(registrierung);
		fragebogenDB.ifPresent(fragebogen -> fragebogenRepo.delete(fragebogen.toId()));
		Fragebogen fragebogen = new Fragebogen();
		fragebogen.setAmpel(request.getAmpel());
		fragebogen.setBeruflicheTaetigkeit(request.getBeruf());
		fragebogen.setLebensumstaende(request.getLebensumstaende());
		fragebogen.setChronischeKrankheiten(request.getChronischeKrankheiten());

		fragebogen.setRegistrierung(registrierung);
		fragebogenRepo.update(fragebogen);
		return fragebogenRepo.getByRegistrierung(registrierung).get();
	}

	@NotNull
	private Adresse mapAdresse(@NotNull AdresseMigrationJax adresseJax) {
		Adresse adresse = new Adresse();
		adresse.setAdresse1(adresseJax.getAdresse1());
		adresse.setAdresse2(adresseJax.getAdresse2());
		adresse.setOrt(adresseJax.getOrt());
		adresse.setPlz(adresseJax.getPlz());
		return adresse;
	}

	@NotNull
	private Impftermin getOrCreateImpfterminForMigration(@NotNull Impfdossier impfdossier, @NotNull ImpfungMigrationJax impfRequest) {
		final OrtDerImpfung odi = ortDerImpfungService.getByOdiIdentifier(mapDummyODITyp(impfRequest.getOrtDerImpfungTyp()));
		Impftermin oldTermin = Impffolge.ERSTE_IMPFUNG == impfRequest.getImpffolge()
			? impfdossier.getBuchung().getImpftermin1()
			: impfdossier.getBuchung().getImpftermin2();
		if (oldTermin == null ||
			!oldTermin.getImpfslot().getZeitfenster().isInRange(impfRequest.getImpfdatum().atTime(LocalTime.NOON)) ||
			!oldTermin.getImpfslot().getOrtDerImpfung().equals(odi)
		) {
			Impftermin termin = terminbuchungService.createOnDemandImpftermin(
				KrankheitIdentifier.COVID,
				impfRequest.getImpffolge(),
				odi,
				impfRequest.getImpfdatum().atTime(LocalTime.NOON));
			replaceTermin(impfdossier, impfRequest, oldTermin, termin);
		}
		return Objects.requireNonNull(Impffolge.ERSTE_IMPFUNG == impfRequest.getImpffolge()
			? impfdossier.getBuchung().getImpftermin1()
			: impfdossier.getBuchung().getImpftermin2());
	}

	private void updateOrCreateImpfung(@NotNull ImpfungMigrationJax impfRequest, @Nullable Impftermin oldTermin, @NotNull Impftermin termin) {
		Impfung impfung;
		if (oldTermin != null) {
			impfung = impfungRepo.getByImpftermin(oldTermin).orElseGet(Impfung::new);
		} else {
			impfung = new Impfung();
		}
		impfung.setTimestampImpfung(impfRequest.getImpfdatum().atTime(LocalTime.NOON));
		impfung.setImpfstoff(mapImpfstoff(impfRequest.getImpfstoff()));
		impfung.setBenutzerDurchfuehrend(mapBenutzer(impfRequest.getDurchfuehrendPersonGLN(), BenutzerRolle.OI_DOKUMENTATION));
		impfung.setBenutzerVerantwortlicher(mapBenutzer(impfRequest.getVerantwortlicherPersonGLN(), BenutzerRolle.OI_IMPFVERANTWORTUNG));
		impfung.setEinwilligung(impfRequest.isEinwilligungImpfung());
		impfung.setFieber(impfRequest.isFieber());
		impfung.setKeineBesonderenUmstaende(null); // dieses flag kann nicht ueber die API gesetzt werden
		impfung.setNeueKrankheit(impfRequest.isNeueKrankheit());
		impfung.setLot(impfRequest.getLot());
		impfung.setMenge(impfRequest.getMenge());
		impfung.setVerarbreichungsart(impfRequest.getVerarbreichungsart());
		impfung.setVerarbreichungsort(impfRequest.getVerarbreichungsort());
		impfung.setVerarbreichungsseite(impfRequest.getVerarbreichungsseite());
		impfung.setExtern(impfRequest.isExtern());
		impfung.setTimestampVMDL(null);
		impfung.setTermin(termin);
		impfung.setGrundimmunisierung(true);
		ValidationUtil.validateImpfstoffZulassung(impfung.getImpfstoff(), impfung.isExtern());
		impfungRepo.update(impfung);
		Impfung impfungUpdate = impfungRepo.getByImpftermin(termin).get();
		updateOrCreateMigration(impfRequest, impfungUpdate);
	}

	private void replaceTermin(@NotNull Impfdossier impfdossier, @NotNull ImpfungMigrationJax impfRequest, @Nullable Impftermin oldTermin,
		@NotNull Impftermin termin) {
		switch (impfRequest.getImpffolge()) {
		case ERSTE_IMPFUNG:
			terminFreigeben(oldTermin);
			impfdossier.getBuchung().setImpftermin1FromImpfterminRepo(null);
			impfterminRepo.termin1Speichern(impfdossier, termin);
			break;
		case ZWEITE_IMPFUNG:
			terminFreigeben(oldTermin);
			impfdossier.getBuchung().setImpftermin2FromImpfterminRepo(null);
			impfterminRepo.termin2Speichern(impfdossier, termin);
			break;
		}
		ValidationUtil.validateNotAfterTomorrow(termin);
	}

	private void terminFreigeben(@Nullable Impftermin impftermin) {
		if (impftermin != null) {
			// Wir rufen hier die Methode direkt auf, um die Validierung zu umgehen! (im Repo wird validiert dass keine Impfung am Termin haengt)
			impftermin.setGebuchtFromImpfterminRepo(false);
			impftermin.setRegistrierungsnummerReserviert(null);
			impftermin.setTimestampReserviert(null);
			impftermin.setOffsetInMinutes(0);
		}
	}

	@NotNull
	private String mapDummyODITyp(@NotNull OrtDerImpfungTyp typ) {
		switch (typ) {
		case SPITAL:
			return DUMMY_MIGRATION_ODI_SPITAL;
		case ALTERSHEIM:
			return DUMMY_MIGRATION_ODI_ALTERSHEIM;
		case IMPFZENTRUM:
		case KINDER_IMPFZENTRUM:
			return DUMMY_MIGRATION_ODI_IMPFTENTRUM;
		case HAUSARZT:
			return DUMMY_MIGRATION_ODI_HAUSARZT;
		case APOTHEKE:
			return DUMMY_MIGRATION_ODI_APOTHEKE;
		case MOBIL:
			return DUMMY_MIGRATION_ODI_MOBIL;
		case ANDERE:
			return DUMMY_MIGRATION_ODI_ANDERE;
		default:
			throw new AppFailureException("Unknown mapping for typ '" + typ + "'");
		}
	}

	@NotNull
	private Benutzer mapBenutzer(@Nullable String glnNummer, @NotNull BenutzerRolle rolle) {
		if (StringUtils.isNotBlank(glnNummer)) {
			List<Benutzer> benutzerList = benutzerService.getByGLNAndRolle(glnNummer, rolle);
			if (benutzerList.size() == 1) {
				return benutzerList.get(0);
			} else if (benutzerList.size() > 1) {
				LOG.warn("VACME-MIGRATION: On 'Benutzer' table the 'glnNummer' '{}' is duplicated for 'rolle' '{}'", glnNummer, rolle);
			}
		}
		return benutzerService.getById(Benutzer.toId(DUMMY_MIGRATION_BENUTZER))
			.orElseThrow(() -> new IllegalStateException("Unknown Benutzer"));
	}

	private void updateOrCreateMigration(@NotNull ImpfungMigrationJax impfRequest, @NotNull Impfung impfung) {
		Migration migration = migrationRepo.getByImpfung(impfung).orElseGet(Migration::new);
		migration.setImpfung(impfung);
		migration.setImpfortGLN(impfRequest.getImpfortGLN());
		migration.setVerantwortlicherPersonGLN(impfRequest.getVerantwortlicherPersonGLN());
		migration.setDurchfuehrendPersonGLN(impfRequest.getDurchfuehrendPersonGLN());
		migrationRepo.update(migration);
	}

	@NotNull
	public Impfstoff mapImpfstoff(@NotNull ImpfstoffNamesJax impfstoff) {
		switch (impfstoff) {
		case PFIZER_BIONTECH:
			return impfstoffService.findById(Impfstoff.toId(Constants.PFIZER_BIONTECH_UUID));
		case PFIZER_BIONTECH_KINDER:
			return impfstoffService.findById(Impfstoff.toId(Constants.PFIZER_BIONTECH_KINDER_UUID));
		case MODERNA:
			return impfstoffService.findById(Impfstoff.toId(Constants.MODERNA_UUID));
		case ASTRAZENECA:
			return impfstoffService.findById(Impfstoff.toId(Constants.ASTRA_ZENECA_UUID));
		case JANSSEN:
			return impfstoffService.findById(Impfstoff.toId(Constants.JANSSEN_UUID));
		case SINOVAC:
			return impfstoffService.findById(Impfstoff.toId(SINOVAC_UUID));
		case SINOPHARM:
			return impfstoffService.findById(Impfstoff.toId(SINOPHARM_UUID));
		case COVAXIN:
			return impfstoffService.findById(Impfstoff.toId(Constants.COVAXIN_UUID));
		case NOVAVAX:
			return impfstoffService.findById(Impfstoff.toId(Constants.NOVAVAX_UUID));
		case COVISHIELD:
			return impfstoffService.findById(Impfstoff.toId(Constants.COVISHIELD_UUID));
		case COVOVAX:
			return impfstoffService.findById(Impfstoff.toId(COVOVAX_UUID));
		default:
			throw new AppFailureException("Mapping for Impfstoff '" + impfstoff + "' not yet implemented!");
		}
	}

	public Optional<LocalDateTime> getLastRequestTimestamp() {
		return migrationRateLimitRepo.get().map(MigrationRateLimit::getTimestampLastRequest);
	}

	public void updateRequestTimestamp() {
		Optional<MigrationRateLimit> migrationRateLimitDB = migrationRateLimitRepo.get();
		if (migrationRateLimitDB.isPresent()) {
			MigrationRateLimit migrationRateLimit = migrationRateLimitDB.get();
			migrationRateLimit.setTimestampLastRequest(LocalDateTime.now());
			migrationRateLimitRepo.update(migrationRateLimit);
		} else {
			migrationRateLimitRepo.update(new MigrationRateLimit());
		}
	}

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void patchPLZ(RegistrierungPLZPatchMigrationJax request) {
		// search the Registrierung by externalId
		Registrierung registrierung = registrierungRepo
			.getByExternalId(request.getExternalId())
			.orElseThrow(() -> AppFailureException.entityNotFound(Registrierung.class, request.getExternalId(), "Unknown externalID"));

		// TODO Affenpocken: VACME-2405 DataMigration nur fuer COVID unterstuetzt
		final Impfdossier impfdossier =
			impfdossierService.findImpfdossierForRegnumAndKrankheit(registrierung.getRegistrierungsnummer(), KrankheitIdentifier.COVID);

		// update the plz
		registrierung.getAdresse().setPlz(request.getPlz());
		registrierungRepo.update(registrierung);

		// search for Impfungen and set the timestampVMDL to null to be resent to VMDL with the new PLZ
		resetVMDLFlag(impfdossier.getBuchung().getImpftermin1());
		resetVMDLFlag(impfdossier.getBuchung().getImpftermin2());
	}

	private void resetVMDLFlag(@Nullable Impftermin impftermin) {
		if (impftermin == null) {
			return;
		}
		impfungRepo.getByImpftermin(impftermin).ifPresent(impfung -> {
			impfung.setTimestampVMDL(null);
			impfungRepo.update(impfung);
		});
	}

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public String patchGLN(RegistrierungGLNPatchMigrationJax request) {
		// validate Impffolge is unique in list
		validateUniqueImpffolge(request.getImpfungen().stream().map(ImpfungGLNPatchMigrationJax::getImpffolge));
		// validate that we got a gln or an odi id
		request.getImpfungen().forEach(impfungGLNPatchMigrationJax -> ValidateCheckIdOrGlnPresent.checkIdOrGln(impfungGLNPatchMigrationJax,
			request.getExternalId()));

		// search the Registrierung by externalId
		Registrierung registrierung = registrierungRepo
			.getByExternalId(request.getExternalId())
			.orElseThrow(() -> AppFailureException.entityNotFound(Registrierung.class, request.getExternalId(), "Unknown externalID"));

		// Impfinformationen vorbereiten
		final ImpfinformationDto impfinformationen = impfinformationenService.getImpfinformationen(
			registrierung.getRegistrierungsnummer(),
			KrankheitIdentifier.COVID);

		// loop ueber die Impfungen
		request.getImpfungen().forEach(impfungRequest -> {
			// Suche Odi per GLN Nummer
			OrtDerImpfung odi = getOdi(impfungRequest);
			// Korrektur Daten vorbereiten
			final ImpfungOdiKorrekturJax korrekturJax = new ImpfungOdiKorrekturJax(
				impfungRequest.getImpffolge(),
				odi.getId(),
				null,
				impfinformationen.getKrankheitIdentifier());
			// Impfung suchen
			Impfung impfung = null;
			final Buchung buchung = impfinformationen.getImpfdossier().getBuchung();
			if (impfungRequest.getImpffolge() == Impffolge.ERSTE_IMPFUNG) {
				if (buchung.getImpftermin1() != null) {
					impfung = korrekturService.getImpfungByTermin(buchung.getImpftermin1());
				}
			} else if (impfungRequest.getImpffolge() == Impffolge.ZWEITE_IMPFUNG) {
				if (buchung.getImpftermin2() != null) {
					impfung = korrekturService.getImpfungByTermin(buchung.getImpftermin2());
				}
			} else {
				//Booster Impfung werden nicht unterstuetzt
				throw new AppFailureException("Mapping for Impffolge '" + impfungRequest.getImpffolge() + "' not allowed!");
			}
			if (impfung == null) {
				// Impfung anhand der Impffolge nicht gefunden
				throw new AppFailureException("Impfung for Impffolge '" + impfungRequest.getImpffolge() + "' not found");
			}
			korrekturService.impfungOdiKorrigieren(korrekturJax, impfinformationen, impfung);
		});

		return registrierung.getRegistrierungsnummer();
	}

	private OrtDerImpfung getOdi(ImpfungGLNPatchMigrationJax impfungRequest) {

		if (impfungRequest.getImpfortId() != null) {
			return getOdiByOdiID(Objects.requireNonNull(impfungRequest.getImpfortId()));
		}
		return getOdiByGLN(Objects.requireNonNull(impfungRequest.getImpfortGLN()));

	}

	@NotNull
	private OrtDerImpfung getOdiByGLN(@NotNull String glnNummer) {
		List<OrtDerImpfung> ortDerImpfungList = ortDerImpfungService.getByGLN(glnNummer);
		if (ortDerImpfungList.size() == 1) {
			return ortDerImpfungList.get(0);
		} else if (ortDerImpfungList.size() > 1) {
			String duplicateGlnOdis =
				ortDerImpfungList.stream().map(OrtDerImpfung::getIdentifier).collect(Collectors.joining(","));
			LOG.warn("VACME-MIGRATION: On 'OrtDerImpfung' table the 'glnNummer' '{}' is duplicated."
				+ " using the first one ({}) from list ({})", glnNummer, ortDerImpfungList.get(0).getIdentifier(), duplicateGlnOdis);
			return ortDerImpfungList.get(0);
		}
		throw new AppFailureException("Odi with GLN '" + glnNummer + "' not found");
	}

	@NotNull
	private OrtDerImpfung getOdiByOdiID(@NotNull UUID odiId) {
		ID<OrtDerImpfung> ortDerImpfungID = OrtDerImpfung.toId(odiId);
		try {
			return ortDerImpfungService.getById(ortDerImpfungID);
		} catch (AppException ex) {
			throw new AppFailureException("Odi with ID '" + odiId + "' not found");
		}
	}

	public void claimRegistration(String regNum, Benutzer currentBenutzer, @NonNull KrankheitIdentifier krankheitIdentifier) {
		final Impfdossier dossier =
			impfdossierService.findImpfdossierForRegnumAndKrankheit(regNum, krankheitIdentifier);
		Registrierung registrierung = dossier.getRegistrierung();

		registrierung.setBenutzerId(currentBenutzer.getId());
		registrierung.setMail(currentBenutzer.getEmail());
		registrierung.setRegistrierungsEingang(RegistrierungsEingang.ONLINE_REGISTRATION);
		dossier.setDossierStatus(ImpfdossierStatus.FREIGEGEBEN_BOOSTER);

		registrierungRepo.update(registrierung);
	}
}
