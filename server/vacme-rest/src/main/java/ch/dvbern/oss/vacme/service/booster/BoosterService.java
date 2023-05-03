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

package ch.dvbern.oss.vacme.service.booster;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.booster.RegistrierungQueue;
import ch.dvbern.oss.vacme.entities.booster.RegistrierungQueueTyp;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfschutz;
import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.repo.BoosterQueueRepo;
import ch.dvbern.oss.vacme.repo.ImpfdossierRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.service.ConfirmationService;
import ch.dvbern.oss.vacme.service.FragebogenService;
import ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioUtil;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.service.impfschutz.ImpfschutzCalculationQueueService;
import ch.dvbern.oss.vacme.service.impfschutz.ImpfschutzCalculationQueueServiceFactory;
import ch.dvbern.oss.vacme.service.impfschutz.ImpfschutzCalculationService;
import ch.dvbern.oss.vacme.service.impfschutz.ImpfschutzCalculationServiceFactory;
import ch.dvbern.oss.vacme.service.wellapi.WellApiService;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoosterService {

	private final ImpfschutzCalculationServiceFactory impfschutzCalculationServiceFactory;
	private final ImpfschutzCalculationQueueServiceFactory impfschutzCalculationQueueServiceFactory;
	private final RegistrierungRepo registrierungRepo;
	private final ConfirmationService confirmationService;
	private final BoosterQueueRepo boosterQueueRepo;
	private final FragebogenService fragebogenService;
	private final ImpfinformationenService impfinformationenService;
	private final ImpfdossierRepo impfdossierRepo;
	private final WellApiService wellApiService;


	@NonNull
	public List<String> findRegsToMoveToImmunisiert(long limit) {
		return this.registrierungRepo.findRegsWithVollstImpfschutzToMoveToImmunisiert(limit);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean moveStatusToImmunisiertForRegistrierung(@NonNull String regNum) {
		try {
			// todo Affenpocken VACME-2402 berechnung und move fuer Affenpocken? oder braucht es das gar nie wahrscheinlich weil direkt im "booster" begonnen wird
			ImpfinformationDto infos =
				impfinformationenService.getImpfinformationen(regNum, KrankheitIdentifier.COVID);

			moveStatusToImmunisiertForRegistrierung(infos);
			return true;

		} catch (Exception exception) {
			LOG.error("VACME-BOOSTER-IMMUNISIERT: Error during statuschange  for {} ", regNum, exception);
			return false;
		}
	}

	private void moveStatusToImmunisiertForRegistrierung(ImpfinformationDto infos) {
		// explizit nicht ueber registrierung#setStatusToImmunisiert();
		// da wir davon ausgehen dass das Zertifikat schon generiert wurde weil wir nur abgeschlossene laden
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.IMMUNISIERT);
		infos.getImpfdossier().getBuchung().setGewuenschterOdi(null);
		impfdossierRepo.update(infos.getImpfdossier());
		registrierungRepo.update(infos.getRegistrierung());
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean calculateImpfschutz(
		@NonNull RegistrierungQueue queueItem,
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		String regNum = queueItem.getRegistrierungNummer();
		try {
			if (!queueItem.needsToRecalculate()) {
				LOG.warn("VACME-BOOSTER-RULE_ENGINE: queueItem {} is not ready to be recalculated. Maybe it was already processed?", queueItem.getId());
				return true;
			}

			ImpfinformationDto infos = impfinformationenService.getImpfinformationen(regNum,krankheitIdentifier);
			Optional<Impfschutz> impfschutzOpt = calculateAndStoreImpfschutz(infos);
			impfschutzOpt.ifPresent(impfschutz -> wellApiService.sendApprovalPeriod(infos, impfschutz));

			queueItem.markSuccessful();
			return true;

		} catch (Exception exception) {
			LOG.error("VACME-BOOSTER-RULE_ENGINE: Error during Impfschutzcalculation  for {} ", regNum, exception);
			queueItem.markFailed(exception.getMessage());
			return false;
		} finally {
			if (queueItem.getTyp() == RegistrierungQueueTyp.BOOSTER_RULE_RECALCULATION) {
				boosterQueueRepo.updateQueueItem(queueItem);
			}
		}
	}

	@NonNull
	private Optional<Impfschutz> calculateAndStoreImpfschutz(
		@NonNull ImpfinformationDto impfinformationDto
	) {
		Fragebogen fragebogen = fragebogenService.findFragebogenByRegistrierungsnummer(impfinformationDto.getRegistrierung().getRegistrierungsnummer());
		ImpfschutzCalculationService impfschutzCalcService = getServiceForImpfschutzCalculation(impfinformationDto.getKrankheitIdentifier());
		Optional<Impfschutz> impfschutzOpt = impfschutzCalcService.calculateImpfschutz(fragebogen, impfinformationDto);
		if (impfschutzOpt.isEmpty()) {
			LOG.trace("VACME-BOOSTER-RULE_ENGINE: Die Registrierung {} erfuellt nicht die Kriterien um einen Impfschutz zu erhalten gemeass den aktuellen Regeln ", impfinformationDto.getRegistrierung().getRegistrierungsnummer());
		}
		Impfdossier impfdossierToUpdt = impfdossierRepo.getOrCreateImpfdossier(
			impfinformationDto.getRegistrierung(),
			impfinformationDto.getKrankheitIdentifier());
		impfdossierRepo.updateImpfschutz(impfdossierToUpdt, impfschutzOpt.orElse(null));

		// to ensure we return the impfschutz that may alrady have existed in the db we return it from the impfdossier
		return Optional.ofNullable(impfdossierToUpdt.getImpfschutz());
	}

	@NonNull
	private ImpfschutzCalculationService getServiceForImpfschutzCalculation(@NonNull KrankheitIdentifier krankheitIdentifier) {
		return this.impfschutzCalculationServiceFactory.createImpfschutzCalculationService(krankheitIdentifier);
	}

	public void recalculateImpfschutzAndStatusmovesForSingleRegWithReload(@NonNull Registrierung registrierung) {
		Collection<Impfdossier> dossiers = impfdossierRepo.findImpfdossiersForReg(Registrierung.toId(registrierung.getId()));
		for (Impfdossier dossier : dossiers) {
			recalculateImpfschutzAndStatusmovesForSingleRegWithReload(registrierung, dossier.getKrankheitIdentifier());
		}
	}

	public void recalculateImpfschutzAndStatusmovesForSingleRegWithReload(@NonNull Registrierung registrierung, KrankheitIdentifier krankheitIdentifier) {
		ImpfinformationDto infos =
			impfinformationenService.getImpfinformationen(registrierung.getRegistrierungsnummer(), krankheitIdentifier);
		recalculateImpfschutzAndStatusmovesForSingleReg(infos);
	}

	public void recalculateImpfschutzAndStatusmovesForSingleReg(
		@NonNull ImpfinformationDto infos
	) {
		recalculateImpfschutzAndStatusmovesForSingleReg(infos, false);
	}

	public void recalculateImpfschutzAndStatusmovesForSingleReg(
		@NonNull ImpfinformationDto infos,
		boolean suppressBenachrichtigung
	) {
		// Wenn noetig verschieben nach immunisiert
		if (needsToBeMovedFromAbgeschlToImmunisiert(infos)) {
			moveStatusToImmunisiertForRegistrierung(infos);
		} else {
			LOG.debug("Registrierung {} hat Kriterien zum verschieben nach Immunisiert nicht erfuellt, status"
				+ " {}", infos.getRegistrierung().getRegistrierungsnummer(), infos.getImpfdossier().getDossierStatus());
		}
		// Impschutz immer neu berechnen, sogar wenn wir keinen vollst. Impfschutz haben weil wir ihn dann evtl loeschen mussen
		Optional<Impfschutz> impfschutzOpt = calculateAndStoreImpfschutz(infos);
		if (!suppressBenachrichtigung) {
			impfschutzOpt.ifPresent(impfschutz -> wellApiService.sendApprovalPeriod(infos, impfschutz));
		}
		// update nicht noetig da attached todo affenpocken, ich glaube das fuehrt zu konflikten wenn man das einkommentiert, sollten wir nochmal testen
//		infos.getImpfdossier().setImpfschutz(impfschutzOpt.orElse(null));
		// verschieben nach Freigegeben
		if (BoosterPrioUtil.meetsCriteriaForFreigabeBooster(infos.getImpfdossier(), impfschutzOpt.orElse(null))) {
			moveStatusToFreigegebenAndSendBenachrichtigung(infos, suppressBenachrichtigung);
		} else {
			LOG.debug("Registrierung {} hat Kriterien zum verschieben nach Freigegeben_Booster nicht erfuellt, status"
					+ " {}, freigabe ab {}", infos.getRegistrierung().getRegistrierungsnummer(),
				infos.getImpfdossier().getDossierStatus(),
				impfschutzOpt.orElse(new Impfschutz()).getFreigegebenNaechsteImpfungAb());
		}
	}

	/**
	 * prueft ob die Registrierung die Bedingungen erfuellt um nach Immunisiert verschoben zu werden. Beim batchjob wird dies durch das Query sichergestellt
	 *
	 */
	private boolean needsToBeMovedFromAbgeschlToImmunisiert(@NonNull ImpfinformationDto infos) {
		Impfdossier dossier = infos.getImpfdossier();

		boolean vollstImpfschutz = dossier.getVollstaendigerImpfschutzTyp() != null;
		boolean isMoveableStatus =  dossier.getDossierStatus() == ImpfdossierStatus.ABGESCHLOSSEN || dossier.getDossierStatus() == ImpfdossierStatus.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG;
		boolean nichtVerstorben = !Boolean.TRUE.equals(infos.getRegistrierung().getVerstorben());

		return vollstImpfschutz && isMoveableStatus && nichtVerstorben;
	}

	@NonNull
	public List<UUID> findDossiersToMoveToFreigegebenBooster(long limit) {
		return this.registrierungRepo.findDossiersToMoveToFreigegebenBooster(limit);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean moveStatusToFreigegebenAndSendBenachrichtigung(@NonNull Impfdossier impfdossier) {
		try {
			ImpfinformationDto infos =
				impfinformationenService.getImpfinformationen(
					impfdossier.getRegistrierung().getRegistrierungsnummer(),
					impfdossier.getKrankheitIdentifier());

			moveStatusToFreigegebenAndSendBenachrichtigung(infos, false);
			return true;

		} catch (Exception exception) {
			LOG.error(
				"VACME-BOOSTER-FREIGABEMOVE: Error during statuschange  for {} ",
				impfdossier.getRegistrierung().getRegistrierungsnummer(),
				exception);
			return false;
		}
	}

	private void moveStatusToFreigegebenAndSendBenachrichtigung(@NonNull ImpfinformationDto infos, boolean suppressBenachrichtigung) {
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.FREIGEGEBEN_BOOSTER);
		// regulaere Freigabe besteht -> Selbstzahler-Flag entfernen
		infos.getImpfdossier().getBuchung().setSelbstzahler(false);
		if (!suppressBenachrichtigung) {
			confirmationService.sendBoosterFreigabeInfo(infos);
		}
	}

	public int queueAllRegsForImpfschutzRecalculation() {
		int totalRecalcNum = 0;
		for (KrankheitIdentifier krankheitIdentifier : KrankheitIdentifier.values()) {
			try {
				totalRecalcNum += queueAllRegsForImpfschutzRecalculation(krankheitIdentifier);
			} catch (Exception e) {
				LOG.error("Error during queueAllRegsForBoosterRuleengine for Krankheit={}", krankheitIdentifier, e);
			}
		}
		return totalRecalcNum;
	}

	public int queueAllRegsForImpfschutzRecalculation(@NonNull KrankheitIdentifier krankheitIdentifier) {
		ImpfschutzCalculationQueueService impfschutzQueueService = impfschutzCalculationQueueServiceFactory.createImpfschutzCalculationService(krankheitIdentifier);
		LOG.info("VACME-BOOSTER-RULE_ENGINE: Loesche alle erfolgreich durchgefuehrten Queue Items fuer Krankheit={} ", krankheitIdentifier);
		long deletedNum = impfschutzQueueService.removeAllSuccessfullQueueEntries();
		LOG.info("VACME-BOOSTER-RULE_ENGINE: Loeschen von {} items fuer Krankheit={} beendet", deletedNum, krankheitIdentifier);
		LOG.info("VACME-BOOSTER-RULE_ENGINE: Ermittle zu berechnende Registierungen fuer Krankheit={} ...", krankheitIdentifier);
		int num =  impfschutzQueueService.queueRelevantRegsForImpfschutzRecalculation();
		LOG.info("VACME-BOOSTER-RULE_ENGINE: Einfuegen von {} QueueItems zur Impfschutzneuberechnung beendet fuer Krankheit={}", num, krankheitIdentifier);

		return num;
	}
}
