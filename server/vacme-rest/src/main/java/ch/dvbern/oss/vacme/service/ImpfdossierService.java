/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.embeddables.Buchung;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.repo.FragebogenRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.service.massenmutation.MassenverarbeitungService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.EnumUtil;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.KONTROLLIERT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.NEU;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ImpfdossierService {

	private final ImpfdossierRepo impfdossierRepo;
	private final FragebogenRepo fragebogenRepo;
	private final ImpfinformationenService impfinformationenService;
	private final StammdatenService stammdatenService;
	private final MassenverarbeitungService massenverarbeitungService;
	private final VacmeSettingsService vacmeSettingsService;

	public @NonNull Impfdossier createImpfdossier(
		@NonNull Registrierung registrierung,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		return impfdossierRepo.createImpfdossier(registrierung, krankheitIdentifier);
	}

	public @NonNull Impfdossier update(@NonNull Impfdossier impfdossier) {
		return impfdossierRepo.update(impfdossier);
	}

	@NonNull
	public Impfdossier getOrCreateImpfdossier(
		@NonNull Registrierung registrierung,
		@NonNull KrankheitIdentifier krankheitIdentifier) {
		return impfdossierRepo.getOrCreateImpfdossier(registrierung, krankheitIdentifier);
	}

	@NonNull
	public Impfdossier getImpfdossierById(@NonNull ID<Impfdossier> id) {
		return impfdossierRepo.getImpfdossier(id);
	}

	@NonNull
	public Impfdossier findImpfdossierForRegnumAndKrankheit(
		@NonNull String regNum,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		return findImpfdossierForRegnumAndKrankheitOptional(regNum, krankheitIdentifier).orElseThrow();
	}

	@NonNull
	public Optional<Impfdossier> findImpfdossierForRegnumAndKrankheitOptional(
		@NonNull String regNum,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		return impfdossierRepo.findImpfdossierForRegnumAndKrankheit(regNum, krankheitIdentifier);
	}

	@NonNull
	public List<UUID> findImpfdossiersInStatusKontrolliert(int limit) {
		return impfdossierRepo.findImpfdossiersInStatusKontrolliert(limit);
	}

	public void acceptLeistungserbringerAgb(
		@NonNull String registrierungsnummer,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		Impfdossier impfdossier = findImpfdossierForRegnumAndKrankheit(registrierungsnummer, krankheitIdentifier);

		impfdossier.setLeistungerbringerAgbConfirmationNeeded(false);

		impfdossierRepo.update(impfdossier);
	}

	public void updateSchnellschemaFlag(Impfdossier impfdossier, boolean schnellschema) {
		impfdossier.setSchnellschema(schnellschema);
		impfdossierRepo.update(impfdossier);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void handleGueltigkeitKontrolleAbgelaufen(
		@NonNull ID<Impfdossier> impfdossierId
	) {
		final Impfdossier impfdossier = impfdossierRepo.getImpfdossier(impfdossierId);
		Registrierung registrierung = impfdossier.getRegistrierung();
		if (EnumUtil.isNoneOf(
			impfdossier.getDossierStatus(),
			IMPFUNG_1_KONTROLLIERT,
			IMPFUNG_2_KONTROLLIERT,
			KONTROLLIERT_BOOSTER)) {
			return;
		}

		switch (impfdossier.getDossierStatus()) {
		case IMPFUNG_1_KONTROLLIERT:
			if (isKontrolleAbgelaufen(impfdossier.getImpfungkontrolleTermin1())) {
				final ImpfdossierStatus lastStatus = ermittleLetztenDossierStatusVorKontrolle1(impfdossier);
				impfdossier.setDossierStatus(lastStatus);
				LOG.info(
					"VACME-KONTROLLE-ABGELAUFEN: Kontrolle 1 abgelaufen. Setze Status zurueck auf {}, "
						+ "Registrierung {}",
					lastStatus,
					registrierung.getRegistrierungsnummer());
			}
			break;
		case IMPFUNG_2_KONTROLLIERT:
			if (isKontrolleAbgelaufen(impfdossier.getImpfungkontrolleTermin2())) {
				impfdossier.setDossierStatus(ImpfdossierStatus.IMPFUNG_1_DURCHGEFUEHRT);
				LOG.info(
					"VACME-KONTROLLE-ABGELAUFEN: Kontrolle 2 abgelaufen. Setze Status zurueck auf "
						+ "getPersonenkontrolle, Registrierung {}",
					registrierung.getRegistrierungsnummer());
			}
			break;
		case KONTROLLIERT_BOOSTER:
			final ImpfinformationDto infos =
				impfinformationenService.getImpfinformationen(impfdossier.getRegistrierung()
					.getRegistrierungsnummer(), impfdossier.getKrankheitIdentifier());
			final Optional<Impfdossiereintrag> eintragOptional =
				ImpfinformationenService.getImpfdossierEintragForKontrolle(infos);
			if (eintragOptional.isEmpty()) {
				int impffolgeNr = ImpfinformationenService.getCurrentKontrolleNr(infos);
				final ImpfdossierStatus lastStatus =
					ermittleLetztenDossierStatusVorKontrolleBooster(infos.getImpfdossier(), null);
				impfdossier.setDossierStatus(lastStatus);
				LOG.warn(
					"VACME-KONTROLLE-ABGELAUFEN: Kontrolle N abgelaufen fuer Registrierung={}, Krankheit={}, aber kein"
						+ " Dossiereintrag fuer KontrolleNr={} gefunden! "
						+ "Status wird resettet: Status={} ",
					registrierung.getRegistrierungsnummer(),
					infos.getKrankheitIdentifier(),
					impffolgeNr,
					lastStatus);
				return;

			}
			final Impfdossiereintrag eintrag = eintragOptional.get();
			if (isKontrolleAbgelaufen(eintrag.getImpfungkontrolleTermin())) {
				final ImpfdossierStatus lastStatus =
					ermittleLetztenDossierStatusVorKontrolleBooster(infos.getImpfdossier(), eintrag);
				impfdossier.setDossierStatus(lastStatus);
				LOG.info(
					"VACME-KONTROLLE-ABGELAUFEN: Kontrolle N abgelaufen. Setze Status zurueck auf {}, "
						+ "Registrierung {}",
					lastStatus,
					registrierung.getRegistrierungsnummer());
			}
			break;
		default:
			throw AppValidationMessage.REGISTRIERUNG_WRONG_STATUS.create(
				registrierung.getRegistrierungsnummer(),
				"IMPFUNG_1_KONTROLLIERT, IMPFUNG_2_KONTROLLIERT, KONTROLLIERT_BOOSTER");
		}
	}

	private Fragebogen readFragebogen(@NonNull Registrierung registrierung) {
		return fragebogenRepo.getByRegistrierung(registrierung).orElseThrow();
	}

	@NonNull
	public ImpfdossierStatus ermittleLetztenDossierStatusVorKontrolle1(
		@NonNull Impfdossier dossier
	) {
		Validate.isTrue(
			dossier.getKrankheitIdentifier().isSupportsImpffolgenEinsUndZwei(),
			"Krankheit %s unterstuetzt keine Impffolgen1/2. Rueckfall auf non-booster Status macht keinen Sinn",
			dossier.getKrankheitIdentifier());

		final Registrierung registrierung = dossier.getRegistrierung();
		final Buchung buchung = dossier.getBuchung();
		ImpfdossierStatus lastStatus = null;
		if (buchung.getImpftermin1() != null && buchung.getImpftermin2() != null) {
			// Beide Termine vorhanden -> Status gebucht
			lastStatus = ImpfdossierStatus.GEBUCHT;
		} else {
			// Keine Termine vorhanden. War die Prio ueberhaupt schon freigegeben?
			if (!stammdatenService.istPrioritaetFreigeschaltet(registrierung.getPrioritaet())) {
				lastStatus = ImpfdossierStatus.NEU;
			} else if (buchung.getGewuenschterOdi() != null
				&& (!buchung.getGewuenschterOdi().isTerminverwaltung()
				|| buchung.getGewuenschterOdi().isMobilerOrtDerImpfung())) {
				// ODI ausgewaehlt, aber keine Termine (moeglich)
				lastStatus = ImpfdossierStatus.ODI_GEWAEHLT;
			} else {
				lastStatus = ImpfdossierStatus.FREIGEGEBEN;
			}
		}
		return lastStatus;
	}

	@NonNull
	public ImpfdossierStatus ermittleLetztenDossierStatusVorKontrolleBooster(
		@NonNull Impfdossier dossier,
		@Nullable Impfdossiereintrag eintrag
	) {
		// Termin vorhanden -> gebucht
		if (eintrag != null && eintrag.getImpftermin() != null) {
			return ImpfdossierStatus.GEBUCHT_BOOSTER;
		}
		// es war eine ad hoc Kontrolle
		// war die Reg schon freigegen fuer booster?
		if (!ImpfinformationenService.hasFreigegebenenImpfschutz(dossier)) {
			return ImpfdossierStatus.IMMUNISIERT;
		}
		// ODI gewaehlt fuer Booster (derjenige von Impfung 1/2 wurde beim Schieben nach Immunisiert auf null gesetzt)
		final Buchung buchung = dossier.getBuchung();
		if (buchung.getGewuenschterOdi() != null
			&& (!buchung.getGewuenschterOdi().isTerminverwaltung()
			|| buchung.getGewuenschterOdi().isMobilerOrtDerImpfung())) {
			return ImpfdossierStatus.ODI_GEWAEHLT_BOOSTER;
		}
		return ImpfdossierStatus.FREIGEGEBEN_BOOSTER;
	}

	private boolean isKontrolleAbgelaufen(
		@Nullable ImpfungkontrolleTermin impfungkontrolleTermin
	) {
		if (impfungkontrolleTermin != null) {
			final LocalDateTime timestampKontrolle = impfungkontrolleTermin.getTimestampKontrolle();
			long kontrolleSince = DateUtil.getMinutesBetween(timestampKontrolle, LocalDateTime.now());
			// KontrolleGueltigkeit ist in Stunden
			final boolean kontrolleAbgelaufen =
				kontrolleSince > vacmeSettingsService.getKontrolleGueltigkeitHours() * 60;
			return kontrolleAbgelaufen;
		}
		// Wenn es keine Kontrolle-Objekt gibt, ist die Kontrolle ebenfalls abgelaufen
		return true;
	}

	public void saveImpfdossiersToChangePrioritaet(@NonNull Prioritaet prioritaet) {
		final List<UUID> dossiersToAnalyze =
			impfdossierRepo.findImpfdossiersOfPrioritaetAndStatus(prioritaet, NEU, FREIGEGEBEN);
		massenverarbeitungService.addImpfgruppeToFreigebenQueue(dossiersToAnalyze);
	}

	public void setStatusAccordingToPrioritaetFreischaltung(@NonNull @NotNull Impfdossier dossier) {
		Registrierung registrierung = dossier.getRegistrierung();
		int alter = (int) DateUtil.getAge(registrierung.getGeburtsdatum());
		moveFromNeuToFreigegebenIfMinAgeReachedAndPrioritaetIsFreigeschaltet(dossier, alter);
		moveFromFreigegebenToNeuIfMinAgeNotReached(dossier, alter);
	}

	private void moveFromFreigegebenToNeuIfMinAgeNotReached(@NonNull @NotNull Impfdossier impfdossier, int alter) {
		// Dies machen wir nur, weil Personen die das Mindestalter noch nicht erreicht haben, schon freigegeben wurden
		if (ImpfdossierStatus.FREIGEGEBEN == impfdossier.getDossierStatus()
			&& alter < vacmeSettingsService.getMinAlterCovidImpfung()) {
			impfdossier.setDossierStatus(ImpfdossierStatus.NEU);
			LOG.info(
				"Setting Registrierung to REGISTRIERT fuer Covid Dossier von Reg {} (min. Alter nicht erreicht)",
				impfdossier.getRegistrierung().getRegistrierungsnummer());
		}
	}

	private void moveFromNeuToFreigegebenIfMinAgeReachedAndPrioritaetIsFreigeschaltet(
		@NonNull @NotNull Impfdossier impfdossier,
		int alter) {
		Registrierung registrierung = impfdossier.getRegistrierung();
		if (ImpfdossierStatus.NEU == impfdossier.getDossierStatus()
			&& alter >= vacmeSettingsService.getMinAlterCovidImpfung()
			&& stammdatenService.istPrioritaetFreigeschaltet(registrierung.getPrioritaet())) {
			impfdossier.setDossierStatus(ImpfdossierStatus.FREIGEGEBEN);
			LOG.info("Setting Registrierung to FREIGEGEBEN {}", registrierung.getRegistrierungsnummer());
		}
	}
}
