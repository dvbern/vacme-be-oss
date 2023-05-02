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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Erkrankung;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.jax.registration.ErkrankungJax;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.ImpfslotRepo;
import ch.dvbern.oss.vacme.repo.ImpfterminRepo;
import ch.dvbern.oss.vacme.repo.OrtDerImpfungRepo;
import ch.dvbern.oss.vacme.service.booster.BoosterService;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.EnumUtil;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationDtoRecreator;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.FREIGEGEBEN_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.GEBUCHT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.GEBUCHT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMMUNISIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_1_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.IMPFUNG_2_KONTROLLIERT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.KONTROLLIERT_BOOSTER;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ODI_GEWAEHLT;
import static ch.dvbern.oss.vacme.enums.ImpfdossierStatus.ODI_GEWAEHLT_BOOSTER;

@ApplicationScoped
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DossierService {

	private final ImpfterminRepo impfterminRepo;
	private final ImpfslotRepo impfslotRepo;
	private final OrtDerImpfungRepo ortDerImpfungRepo;
	private final ImpfdossierFileService impfdossierFileService;
	private final ConfirmationService confirmationService;
	private final SettingsService settingsService;
	private final ImpfinformationenService impfinformationenService;
	private final ImpfdossierRepo impfdossierRepo;
	private final BoosterService boosterService;
	private final TerminbuchungService terminbuchungService;
	private final ImpfdossierService impfdossierService;

	public void termineBuchenGrundimmunisierung(
		@NonNull ImpfinformationDto infos,
		@NonNull ID<Impfslot> slotId1,
		@NonNull ID<Impfslot> slotId2
	) {
		Impfdossier impfdossier = infos.getImpfdossier();
		Registrierung registrierung = impfdossier.getRegistrierung();

		final Impfslot slot1 = impfslotRepo
			.getById(slotId1)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_IMPFTERMIN.create(slotId1));
		final Impfslot slot2 = impfslotRepo
			.getById(slotId2)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_IMPFTERMIN.create(slotId2));

		// Status muss FREIGEGEBEN sein
		ValidationUtil.validateStatusOneOf(impfdossier, FREIGEGEBEN);
		// Es ist nicht mehr noetig, dass beide Termine zum gleichen Odi gehoeren
		// Sicherstellen, dass genuegend Abstand zwischen den beiden Terminen
		final int minimumDaysBetweenImpfungen = this.settingsService.getSettings().getDistanceImpfungenMinimal();
		final int maximumDaysBetweenImpfungen = this.settingsService.getSettings().getDistanceImpfungenMaximal();
		ValidationUtil.validateDaysBetweenImpfungen(
			slot1.getZeitfenster(),
			slot2.getZeitfenster(),
			minimumDaysBetweenImpfungen,
			maximumDaysBetweenImpfungen);
		// Freien Termin fuer diesen Slot suchen
		final Impftermin termin1 =
			impfterminRepo.findMeinenReserviertenOrFreienImpftermin(registrierung, slot1, Impffolge.ERSTE_IMPFUNG);
		final Impftermin termin2 =
			impfterminRepo.findMeinenReserviertenOrFreienImpftermin(registrierung, slot2, Impffolge.ZWEITE_IMPFUNG);
		// locking sollte nicht noetig sein wegen unique constraint
		if (termin1 == null) {
			throw AppValidationMessage.IMPFTERMIN_BESETZT.create(slot1.toDateMessage());
		}
		if (termin2 == null) {
			throw AppValidationMessage.IMPFTERMIN_BESETZT.create(slot2.toDateMessage());
		}

		// Status setzen & speichern
		impfdossier.setDossierStatus(GEBUCHT);
		//Wir setzen den gewunschten OdI aus dem slot 1 oder 2 je nach dem ob die 1. Impfung schon durch ist
		if (!ImpfdossierStatus.isErsteImpfungDoneAndZweitePending().contains(impfdossier.getDossierStatus())) {
			impfdossier.getBuchung().setGewuenschterOdi(slot1.getOrtDerImpfung());
		} else {
			impfdossier.getBuchung().setGewuenschterOdi(slot2.getOrtDerImpfung());
		}
		impfdossier.getBuchung().setNichtVerwalteterOdiSelected(false);

		// Termine anhaengen und auf besetzt setzen
		impfterminRepo.termineSpeichern(impfdossier, termin1, termin2);

		ValidationUtil.validateSlotForKrankheit(impfdossier.getKrankheitIdentifier(), slot1);
		ValidationUtil.validateSlotForKrankheit(impfdossier.getKrankheitIdentifier(), slot2);
		final Impfdossier updatedImpfdossier = impfdossierRepo.update(impfdossier);

		// Terminbestaetigung per SMS senden
		confirmationService.sendTerminbestaetigung(updatedImpfdossier, null); // nur impfung 1/2
	}

	public void termineBuchenBooster(
		@NonNull ImpfinformationDto info,
		@NonNull ID<Impfslot> slotIdN
	) {
		Impfdossier impfdossier = info.getImpfdossier();
		Registrierung registrierung = impfdossier.getRegistrierung();

		final Impfslot slotN = impfslotRepo
			.getById(slotIdN)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_IMPFTERMIN.create(slotIdN));

		ValidationUtil.validateSlotForKrankheit(info.getKrankheitIdentifier(), slotN);

		if (!impfdossier.getBuchung().isSelbstzahler()) {
			// Status muss FREIGEGEBEN_BOOSTER sein
			ValidationUtil.validateStatusOneOf(impfdossier, FREIGEGEBEN_BOOSTER);
		} else {
			ValidationUtil.validateStatusOneOf(impfdossier, IMMUNISIERT);
		}

		// Freien Termin fuer diesen Slot suchen
		final Impftermin terminN =
			impfterminRepo.findMeinenReserviertenOrFreienImpftermin(registrierung, slotN, Impffolge.BOOSTER_IMPFUNG);
		// locking sollte nicht noetig sein wegen unique constraint
		if (terminN == null) {
			throw AppValidationMessage.IMPFTERMIN_BESETZT.create(slotN.toDateMessage());
		}

		// Status setzen & speichern
		impfdossier.setDossierStatus(GEBUCHT_BOOSTER);
		impfdossier.getBuchung().setGewuenschterOdi(slotN.getOrtDerImpfung());
		impfdossier.getBuchung().setNichtVerwalteterOdiSelected(false);
		info = ImpfinformationDtoRecreator.from(info).withDossier(impfdossierRepo.update(impfdossier)).build();

		// Termine anhaengen und auf besetzt setzen
		Integer impffolgeNr = ImpfinformationenService.getNumberOfImpfung(info) + 1;
		final Impfdossiereintrag dossierEintrag =
			impfinformationenService.getOrCreateLatestImpfdossierEintrag(info, impffolgeNr);
		impfterminRepo.boosterTerminSpeichern(dossierEintrag, terminN);

		// Dossier mit dem neuen Termin neu laden
		impfdossier = impfdossierService.getImpfdossierById(impfdossier.toId());

		// Terminbestaetigung per SMS senden
		confirmationService.sendTerminbestaetigung(impfdossier, terminN);
	}

	/**
	 * Resets the {@link Registrierung#gewuenschterOdi} and cancels booked termine if there are any
	 *
	 * @param registrierung no-doc
	 */
	public void odiAndTermineAbsagen(@NonNull ImpfinformationDto infos) {
		Impfdossier impfdossier = infos.getImpfdossier();
		/*
			Cancel ODI and or all appointments if an ODI has been selected, the appointments have been
			booked or the first control has been made.
			After these states at least one vaccination has been made and therefore we can no longer
			cancel all appointments.
		 */
		ValidationUtil.validateStatusOneOf(impfdossier, FREIGEGEBEN, ODI_GEWAEHLT, GEBUCHT, IMPFUNG_1_KONTROLLIERT,
			IMMUNISIERT, FREIGEGEBEN_BOOSTER, ODI_GEWAEHLT_BOOSTER, GEBUCHT_BOOSTER, KONTROLLIERT_BOOSTER);

		// Flags zuerst zuruecksetzen, da aufgrund dessen der letzte Stand vor Kontrolle ermittelt wird
		impfdossier.getBuchung().setGewuenschterOdi(null);
		impfdossier.getBuchung().setNichtVerwalteterOdiSelected(false);

		if (ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(impfdossier.getDossierStatus())) {
			// Unabhaengig davon, ob das ODI aktuell ueber eine Terminverwaltung verfuegt: Falls Termine vorhanden
			// sind, diese loeschen, ansonsten nur gewuenschtesODI entfernen
			final Optional<Impfdossiereintrag> pendingDossiereintrag =
				ImpfinformationenService.getPendingDossiereintrag(infos);
			if (pendingDossiereintrag.isPresent()) {
				final Impfdossiereintrag eintrag = pendingDossiereintrag.get();
				impfterminRepo.boosterTerminFreigeben(eintrag);
				resetBoosterStatusToStatusBeforeBuchung(impfdossier, eintrag);
			} else {
				resetBoosterStatusToStatusBeforeBuchung(impfdossier, null);
			}
		} else {
			impfterminRepo.termine1Und2Freigeben(impfdossier);
			// Wenn nach der Kontrolle der Termin abgesagt wird, wollen wir im Status Kontrolliert bleiben, damit man
			// ad-hoc impfen kann!
			if (EnumUtil.isNoneOf(impfdossier.getDossierStatus(), IMPFUNG_1_KONTROLLIERT, IMPFUNG_2_KONTROLLIERT)) {
				impfdossier.setDossierStatus(impfdossierService.ermittleLetztenDossierStatusVorKontrolle1(impfdossier));
			}
		}

		impfdossierFileService.deleteTerminbestaetigung(impfdossier);
	}

	private void resetBoosterStatusToStatusBeforeBuchung(
		@NonNull Impfdossier dossier,
		@Nullable Impfdossiereintrag eintrag
	) {
		// Wenn nach der Kontrolle der Termin abgesagt wird, wollen wir im Status Kontrolliert bleiben, damit man
		// ad-hoc impfen kann!
		if (EnumUtil.isNoneOf(dossier.getDossierStatus(), KONTROLLIERT_BOOSTER)) {
			ImpfdossierStatus lastStatus = impfdossierService.ermittleLetztenDossierStatusVorKontrolleBooster(
				dossier, eintrag);
			dossier.setDossierStatus(lastStatus);
		}
	}

	public void selectOrtDerImpfung(
		@NonNull ImpfinformationDto infos,
		@NonNull ID<OrtDerImpfung> ortDerImpfungID
	) {
		Registrierung registrierung = infos.getRegistrierung();
		Impfdossier impfdossier = infos.getImpfdossier();

		if (!impfdossier.getBuchung().isSelbstzahler()) {
			// Status muss FREIGEGEBEN oder FREIGEGEBEN_BOOSTER sein
			ValidationUtil.validateStatusOneOf(impfdossier, FREIGEGEBEN, FREIGEGEBEN_BOOSTER);
		} else {
			ValidationUtil.validateStatusOneOf(impfdossier, IMMUNISIERT);
		}

		final OrtDerImpfung gewuenschterODI = ortDerImpfungRepo
			.getById(ortDerImpfungID)
			.orElseThrow(() -> AppValidationMessage.UNKNOWN_ODI.create(ortDerImpfungID));

		impfdossier.getBuchung().setGewuenschterOdi(gewuenschterODI);
		impfdossier.getBuchung().setNichtVerwalteterOdiSelected(false);

		// Falls dieser ODI kein Mobiler ODI ist, scheint der Impfling neu mobil zu sein
		if (!gewuenschterODI.isMobilerOrtDerImpfung()) {
			registrierung.setImmobil(false);
		}

		switch (impfdossier.getDossierStatus()) {
		case FREIGEGEBEN:
			impfdossier.setDossierStatus(ODI_GEWAEHLT);
			break;
		case FREIGEGEBEN_BOOSTER:
		case IMMUNISIERT:
			impfdossier.setDossierStatus(ODI_GEWAEHLT_BOOSTER);
			break;
		default:
			throw AppValidationMessage.REGISTRIERUNG_WRONG_STATUS.create(
				registrierung.getRegistrierungsnummer(),
				FREIGEGEBEN,
				FREIGEGEBEN_BOOSTER);
		}

		// Terminbestaetigung per SMS bzw. Post senden
		// Wir koennen sendConfirmationNoBoosterTermin verwenden, weil fuer ODI-GEWAEHLT kein Termin notwendig ist
		// sondern nur das gewuenschteOdi, welches fuer Normal und Booster das selbe Attribut ist
		confirmationService.sendTerminbestaetigung(impfdossier, null);
	}

	public void selectNichtVerwalteterOrtDerImpfung(
		@NonNull ImpfinformationDto infos
	) {
		Registrierung registrierung = infos.getRegistrierung();
		Impfdossier impfdossier = infos.getImpfdossier();
		if (!impfdossier.getBuchung().isSelbstzahler()) {
			// Status muss FREIGEGEBEN oder FREIGEGEBEN_BOOSTER sein
			ValidationUtil.validateStatusOneOf(impfdossier, FREIGEGEBEN, FREIGEGEBEN_BOOSTER);
		} else {
			ValidationUtil.validateStatusOneOf(impfdossier, IMMUNISIERT);
		}

		impfdossier.getBuchung().setNichtVerwalteterOdiSelected(true);
		impfdossier.getBuchung().setGewuenschterOdi(null);

		// Der Impfling hat eine nicht aufgefuehrte Arztpraxis gewaehlt, scheint also mobil zu sein
		registrierung.setImmobil(false);

		// Terminbestaetigung per SMS bzw. Post senden
		// Wir koennen sendConfirmationNoBoosterTermin verwenden, weil fuer ODI-GEWAEHLT kein Termin notwendig ist
		// sondern nur das gewuenschteOdi, welches fuer Normal und Booster das selbe Attribut ist
		confirmationService.sendTerminbestaetigung(impfdossier, null);
	}

	public void updateErkrankungen(
		@NonNull ImpfinformationDto infos,
		@NonNull List<ErkrankungJax> erkrankungJaxList,
		boolean suppressBenachrichtigung
	) {
		Impfdossier impfdossier = infos.getImpfdossier();

		List<Erkrankung> erkrankungen =
			erkrankungJaxList.stream().map(ErkrankungJax::toEntity).collect(Collectors.toList());
		impfdossierRepo.updateErkrankungen(impfdossier, erkrankungen);
		boosterService.recalculateImpfschutzAndStatusmovesForSingleReg(infos, suppressBenachrichtigung);
		freigabestatusUndTermineEntziehenFallsImpfschutzNochNichtFreigegeben(infos);
	}

	public void freigabestatusUndTermineEntziehenFallsImpfschutzNochNichtFreigegeben(@NonNull ImpfinformationDto infosNeu) {
		Impfschutz impfschutzOrNullNeu = infosNeu.getImpfdossier().getImpfschutz();
		Impfdossier impfdossier = infosNeu.getImpfdossier();

		// neu: nicht (mehr) freigegeben. Falls wir bisher freigegeben waren, muessen wir die Freigabe entziehen und
		// Termine absagen
		boolean neuNichtFreigegeben = impfschutzOrNullNeu == null
			|| impfschutzOrNullNeu.getFreigegebenNaechsteImpfungAb() == null
			|| impfschutzOrNullNeu.getFreigegebenNaechsteImpfungAb().isAfter(LocalDateTime.now());

		if (neuNichtFreigegeben) {
			// Termine freigeben / ODI-Wahl loeschen
			switch (impfdossier.getDossierStatus()) {
			case GEBUCHT_BOOSTER:
				terminbuchungService.boosterTerminAbsagen(infosNeu);
				impfdossier.getBuchung().setGewuenschterOdi(null);
				break;
			case ODI_GEWAEHLT_BOOSTER:
				impfdossier.getBuchung().setGewuenschterOdi(null);
				break;
			default:
				// nichts tun
			}

			// Nicht-verwalteter ODI zuruecksetzen
			impfdossier.getBuchung().setNichtVerwalteterOdiSelected(false);

			// Freigabe-Status entziehen
			switch (impfdossier.getDossierStatus()) {
			case FREIGEGEBEN_BOOSTER:
			case GEBUCHT_BOOSTER:
			case ODI_GEWAEHLT_BOOSTER:
				impfdossier.setDossierStatus(IMMUNISIERT);
				break;
			default:
				// nichts tun
			}
		}
	}
}
