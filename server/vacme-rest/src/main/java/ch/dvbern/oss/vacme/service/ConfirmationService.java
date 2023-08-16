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

import java.time.LocalDate;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFile;
import ch.dvbern.oss.vacme.entities.dossier.ImpfdossierFileTyp;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFile;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFileTyp;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.repo.BenutzerRepo;
import ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioUtil;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertBatchType;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;


@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@ApplicationScoped
public class ConfirmationService {

	private final BenutzerRepo benutzerRepo;
	private final SmsService smsService;
	private final PdfService pdfService;
	private final RegistrierungFileService registrierungFileService;
	private final ImpfdossierFileService impfdossierFileService;
	private final ImpfinformationenService impfinformationenService;
	private final VacmeSettingsService vacmeSettingsService;
	private final ImpfdossierService impfdossierService;
	private final BenutzerService benutzerService;


	public void sendRegistrierungsbestaetigung(@NonNull Registrierung registrierung) {
		if (registrierung.getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
			sendRegistrierungsbestaetigungSMS(registrierung);
		} else { // bei callcenter und allen anderen machen wir das file neu so dass es neu gesendet wird
			sendRegistrierungsbestaetigungBrief(registrierung);
		}
	}

	public void sendTerminbestaetigung(@NonNull Impfdossier impfdossier, @Nullable Impftermin boosterTerminOrNull) {
		if (impfdossier.getRegistrierung().getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
			sendTerminbestaetigungSMS(impfdossier, boosterTerminOrNull);
		} else { // bei callcenter und allen anderen machen wir das file neu so dass es neu gesendet wird
			sendTerminbestaetigungBrief(impfdossier, boosterTerminOrNull);
		}
	}

	public void sendBoosterFreigabeInfo(@NonNull ImpfinformationDto info) {
		if (info.getRegistrierung().getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
			sendBoosterFreigabeInfoSMS(info);
		} else { // bei callcenter und allen anderen machen wir das file neu so dass es neu gesendet wird
			sendFreigabeBoosterInfoBrief(info);
		}
	}

	public void resendRegistrierungsbestaetigung(@NonNull Registrierung registrierung) {
		registrierungFileService.deleteRegistrierungbestaetigung(registrierung);
		sendRegistrierungsbestaetigung(registrierung);
	}

	public void resendTerminbestaetigung(@NonNull Impfdossier impfdossier, @Nullable Impftermin boosterTerminOrNull) {
		impfdossierFileService.deleteTerminbestaetigung(impfdossier);
		sendTerminbestaetigung(impfdossier, boosterTerminOrNull);
	}

	private void sendRegistrierungsbestaetigungSMS(@NonNull Registrierung registrierung) {
		final Benutzer targetBenutzer = benutzerService.getBenutzerOfOnlineRegistrierung(registrierung);

		if (targetBenutzer.hasNonEmptyAndValidatedMobileNumber()) {
			String number = targetBenutzer.getMobiltelefon();
			Objects.requireNonNull(number);
			smsService.sendOnlineRegistrierungsSMS(registrierung, number, targetBenutzer);
		}
	}

	private void sendTerminbestaetigungSMS(@NonNull Impfdossier impfdossier, @Nullable Impftermin boosterTerminOrNull) {
		final Registrierung registrierung = impfdossier.getRegistrierung();
		final Benutzer targetBenutzer = benutzerService.getBenutzerOfOnlineRegistrierung(registrierung);

		if (targetBenutzer.hasNonEmptyAndValidatedMobileNumber()) {
			String number = targetBenutzer.getMobiltelefon();
			Objects.requireNonNull(number);
			smsService.sendTerminbestaetigungSMS(impfdossier, boosterTerminOrNull, targetBenutzer);
		}
	}

	private void sendBoosterFreigabeInfoSMS(@NonNull ImpfinformationDto infos) {
		final Registrierung registrierung = infos.getRegistrierung();
		final Benutzer targetBenutzer = benutzerService.getBenutzerOfOnlineRegistrierung(registrierung);

		if (targetBenutzer.hasNonEmptyAndValidatedMobileNumber()) {

			String number = targetBenutzer.getMobiltelefon();
			Objects.requireNonNull(number);

			if (doNotSendFreigabeBoosterInfo(infos)) {
				// Es soll keine Benachrichtigung geschickt werden. Wir brechen hier ab
				return;
			}

			final Impfdossier impfdossier = infos.getImpfdossier();
			Objects.requireNonNull(
				impfdossier,
				() -> "Kein Impfdossier gefunden fuer " + infos.getRegistrierung().getRegistrierungsnummer());
			Objects.requireNonNull(
				impfdossier.getImpfschutz(),
				() -> "Kein Impfschutz gefunden " + infos.getRegistrierung().getRegistrierungsnummer());
			if (!impfdossier.getImpfschutz().isBenachrichtigungBeiFreigabe()) {
				// Es soll keine Benachrichtigung geschickt werden. Wir brechen hier ab
				return;
			}

			Objects.requireNonNull(infos.getRegistrierung().getBenutzerId());
			String benutzerName =
				benutzerRepo.getById(Benutzer.toId(infos.getRegistrierung().getBenutzerId()))
					.map(Benutzer::getBenutzername)
					.orElse(null);
			smsService.sendFreigabeBoosterSMS(infos, benutzerName, targetBenutzer);
		}
	}

	private void sendRegistrierungsbestaetigungBrief(@NonNull Registrierung registrierung) {
		if (isKeinKontaktAndLog(registrierung)) {
			return;
		}
		final byte[] fileContent = pdfService.createRegistrationsbestaetigung(registrierung);
		saveAndSendLetter(registrierung, RegistrierungFileTyp.REGISTRIERUNG_BESTAETIGUNG, fileContent);
	}

	private void sendTerminbestaetigungBrief(@NonNull Impfdossier impfdossier, @Nullable Impftermin boosterTerminOrNull) {
		if (!impfdossier.getKrankheitIdentifier().isSupportsBenachrichtigungenPerBrief()) {
			return;
		}
		final byte[] fileContent = pdfService.createTerminbestaetigung(impfdossier, boosterTerminOrNull);
		saveAndSendLetter(impfdossier, ImpfdossierFileTyp.TERMIN_BESTAETIGUNG, fileContent);
	}

	private void sendFreigabeBoosterInfoBrief(@NonNull ImpfinformationDto infos) {
		if (!infos.getKrankheitIdentifier().isSupportsBenachrichtigungenPerBrief()) {
			return;
		}
		if (doNotSendFreigabeBoosterInfo(infos)) {
			// Es soll keine Benachrichtigung geschickt werden. Wir brechen hier ab
			return;
		}

		Objects.requireNonNull(infos.getImpfdossier(), "Kein Impfdossier gefunden");
		Objects.requireNonNull(infos.getImpfdossier().getImpfschutz(), "Kein Impfschutz gefunden");
		if (!infos.getImpfdossier().getImpfschutz().isBenachrichtigungBeiFreigabe()) {
			// Es soll keine Benachrichtigung geschickt werden. Wir brechen hier ab
			return;
		}

		LocalDate latestImpfung = BoosterPrioUtil.getDateOfNewestImpfung(infos);
		if (latestImpfung == null) {
			throw AppValidationMessage.IMPFUNG_DATE_NOT_AVAILABLE.create(infos.getRegistrierung().getRegistrierungsnummer());
		}

		final byte[] fileContent = pdfService.createFreigabeBoosterBrief(infos.getImpfdossier(), latestImpfung);
		saveAndSendLetter(infos.getImpfdossier(), ImpfdossierFileTyp.FREIGABE_BOOSTER_INFO, fileContent);
	}

	public void sendTerminabsage(
		@NonNull Impfdossier impfdossier,
		@NonNull Impftermin termin,
		@NonNull String terminEffectiveStartBeforeOffsetReset
	) {
		Registrierung registrierung = impfdossier.getRegistrierung();
		if (isVerstorbenAndLog(registrierung)) {
			return;
		}
		if (registrierung.getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
			final Benutzer targetBenutzer = benutzerService.getBenutzerOfOnlineRegistrierung(registrierung);
			String number = targetBenutzer.getMobiltelefon();
			Objects.requireNonNull(number);
			smsService.sendTerminabsage(impfdossier, termin, targetBenutzer, terminEffectiveStartBeforeOffsetReset);
		} else { // bei callcenter und allen anderen generieren wir das file neu damit es neu gesendet wird
			byte[] fileContent = pdfService.createTerminabsage(registrierung, termin, terminEffectiveStartBeforeOffsetReset);
			saveAndSendLetter(impfdossier, ImpfdossierFileTyp.TERMIN_ABSAGE, fileContent);
		}
	}

	public void sendTerminabsageBeideTermine(@NonNull Impfdossier impfdossier, @NonNull Impftermin termin1, @NonNull Impftermin termin2,
		@NonNull String terminEffectiveStart, @NonNull String termin2EffectiveStart) {
		Registrierung registrierung = impfdossier.getRegistrierung();
		if (isVerstorbenAndLog(registrierung)) {
			return;
		}
		if (registrierung.getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
			final Benutzer targetBenutzer = benutzerService.getBenutzerOfOnlineRegistrierung(registrierung);
			String number = targetBenutzer.getMobiltelefon();
			Objects.requireNonNull(number);
			smsService.sendTerminabsageBeideTermine(registrierung, number, terminEffectiveStart, termin2EffectiveStart);
		} else { // bei callcenter und allen anderen generieren wir das file neu damit es neu gesendet wird
			byte[] fileContent = pdfService.sendTerminabsageBeideTermine(impfdossier.getRegistrierung(), termin1, termin2, terminEffectiveStart, termin2EffectiveStart);
			saveAndSendLetter(impfdossier, ImpfdossierFileTyp.TERMIN_ABSAGE, fileContent);
		}
	}

	private boolean boosterNotificationDisabled(@NonNull ImpfinformationDto infos) {
		final boolean notificationDisabled = vacmeSettingsService.isBoosterFreigabeNotificationDisabled();
		final boolean notificationDisabledTerminN = vacmeSettingsService.isBoosterFreigabeNotificationTerminNDisabled();
		return notificationDisabled
			|| (notificationDisabledTerminN
			&& infos.getBoosterImpfungen() != null
			&& !infos.getBoosterImpfungen().isEmpty());
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void sendZertifikatsbenachrichtigung(
		@NonNull Registrierung registrierung,
		@NonNull Zertifikat zertifikat,
		@NonNull CovidCertBatchType batchType
	) {
		if (isVerstorbenAndLog(registrierung)) {
			return;
		}
		if (registrierung.getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
			final Benutzer targetBenutzer = benutzerService.getBenutzerOfOnlineRegistrierung(registrierung);
			String number = targetBenutzer.getMobiltelefon();
			Objects.requireNonNull(number);
			smsService.sendZertifikatsbenachrichtigung(registrierung, number, batchType);
		} else {
			LOG.warn("VACME-ZERTIFIKAT: Es wird keine Benachrichtigung generiert fuer das generierte Zertifikat"
					+ " {} der  Registrierung {} weil "
					+ "es sich nicht um eine Online Registrierung handelt ",
				zertifikat.getUvci(),
				registrierung.getRegistrierungsnummer()
			);
		}
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void sendZertifikatRevocationBenachrichtigung(
		@NonNull Zertifikat zertifikat,
		@NonNull CovidCertBatchType batchType
	) {
		Validate.isTrue(
			CovidCertBatchType.REVOCATION_ONLINE == batchType
				|| CovidCertBatchType.REVOCATION_POST == batchType, "Batch Typ muss REVOCATION_ONLINE oder REVOCATION_POST sein");
		Registrierung registrierung = zertifikat.getRegistrierung();
		if (isVerstorbenAndLog(registrierung)) {
			return;
		}
		if (batchType == CovidCertBatchType.REVOCATION_ONLINE) {
			final Benutzer targetBenutzer = benutzerService.getBenutzerOfOnlineRegistrierung(registrierung);
			String number = targetBenutzer.getMobiltelefon();
			Objects.requireNonNull(number);
			smsService.sendZertifikatRevocationBenachrichtigung(zertifikat, number);
		} else {
			// TODO Affenpocken. VACME-2403 Wenn dann das Zertifikat mit Dossier statt Reg verlinkt ist, kann hier die Krankheit vom Zertifikat genommen werden
			final Impfdossier dossier =
				impfdossierService.findImpfdossierForRegnumAndKrankheitOptional(registrierung.getRegistrierungsnummer(), KrankheitIdentifier.COVID)
					.orElseThrow(() -> AppValidationMessage.UNKNOWN_REGISTRIERUNGSNUMMER_KRANKHEIT.create(registrierung.getRegistrierungsnummer(), KrankheitIdentifier.COVID));
			byte[] fileContent = pdfService.createZertifikatStornierung(registrierung, zertifikat);
			saveAndSendLetter(dossier, ImpfdossierFileTyp.TERMIN_ZERTIFIKAT_STORNIERUNG, fileContent);
		}
	}

	private boolean isVerstorbenAndLog(@NonNull Registrierung registrierung) {
		if (Boolean.TRUE.equals(registrierung.getVerstorben())) {
			LOG.warn("Versand abgebrochen, Registrierung ist als verstorben markiert");
			return true;
		}
		return false;
	}

	private boolean isKeinKontaktAndLog(@NonNull Registrierung registrierung) {
		if (Boolean.TRUE.equals(registrierung.getKeinKontakt())) {
			LOG.warn("Versand abgebrochen, Registrierung ist als keinKontakt markiert");
			return true;
		}
		return false;
	}

	public RegistrierungFile saveAndSendLetter(@NotNull Registrierung registrierung, @NonNull RegistrierungFileTyp type, byte[] fileContent) {
		RegistrierungFile file = registrierungFileService.createAndSave(fileContent, type, registrierung);
		return file;
	}

	public ImpfdossierFile saveAndSendLetter(@NotNull Impfdossier impfdossier, @NonNull ImpfdossierFileTyp type, byte[] fileContent) {
		ImpfdossierFile file = impfdossierFileService.createAndSave(fileContent, type, impfdossier);
		return file;
	}

	private boolean doNotSendFreigabeBoosterInfo(@NonNull ImpfinformationDto infos) {
		// Wir schicken NIE ein SMS, wenn noch gar keine Impfung dokumentiert (in Vacme oder ExternesZertifikat) ist
		boolean isGrundimmunisiert =
			infos.getExternesZertifikat() != null && infos.getExternesZertifikat().isGrundimmunisiert(infos.getKrankheitIdentifier());
		boolean hasVacmeImpfungen = impfinformationenService.hasVacmeImpfungen(infos);
		if (!(isGrundimmunisiert || hasVacmeImpfungen)) {
			return true;
		}
		return boosterNotificationDisabled(infos)
			|| isKeinKontaktAndLog(infos.getRegistrierung())
			|| infos.getRegistrierung().isImmobil()
			|| !infos.getKrankheitIdentifier().isSupportsFreigabeSMS();
	}
}
