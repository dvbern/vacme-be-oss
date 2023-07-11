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

package ch.dvbern.oss.vacme.service.onboarding;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.onboarding.Onboarding;
import ch.dvbern.oss.vacme.entities.registration.*;
import ch.dvbern.oss.vacme.repo.OnboardingRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.service.ConfirmationService;
import ch.dvbern.oss.vacme.service.PdfService;
import ch.dvbern.oss.vacme.service.RegistrierungService;
import ch.dvbern.oss.vacme.service.VacmeSettingsService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequestScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OnboardingService {

	private final RegistrierungRepo registrierungRepo;
	private final OnboardingRepo onboardingRepo;
	private final PdfService pdfService;
	private final RegistrierungService registrierungService;
	private final ConfirmationService confirmationService;
	private final VacmeSettingsService vacmeSettingsService;

	@Transactional(TxType.REQUIRES_NEW)
	public void triggerLetterGeneration(@NonNull ID<Registrierung> registrierungsId, @NonNull OnboardingBatchType batchType) {
		Registrierung reg = registrierungService.findRegistrierungById(registrierungsId.getId()).orElseThrow();

		// create onboarding code & DB entry
		Onboarding onboarding = createAndStoreNewOnboardingCode(reg, batchType);
		LOG.info("VACME-ONBOARDING: creating onboarding {} for reg {}", onboarding.getCode(), registrierungsId);

		// Do not generate a letter anymore
		onboarding.getRegistrierung().setGenerateOnboardingLetter(false);
	}

	public Onboarding createAndStoreNewOnboardingCode(Registrierung registrierung, OnboardingBatchType batchType) {
		// setup the onboarding
		Onboarding onboarding = new Onboarding(registrierung);
		onboarding.setCode(onboardingRepo.getNextOnboardingCode());
		LOG.info("Generated onboarding code for registration: {} {}", onboarding.getCode(), registrierung.getId());

		// send letter
		sendOnboarding(onboarding, batchType);

		// save after sending the letter (because we want to save the onboardingPdf_id in the onboarding)
		onboardingRepo.create(onboarding);

		return onboarding;
	}

	private void sendOnboarding(@NonNull Onboarding onboarding, @NonNull OnboardingBatchType batchType) {
		// generate and send letter
		if (batchType == OnboardingBatchType.POST) {
			sendLetter(onboarding);
		} else {
			throw new AppFailureException("unkown onboarding batch type: " + batchType);
		}
	}

	private void sendLetter(@NonNull Onboarding onboarding) {
		Registrierung registrierung = onboarding.getRegistrierung();

		// Generate and save the letter Pdf and upload to FTP
		LOG.info("VACME-ONBOARDING: creating letter for {} {}", onboarding.getCode(), registrierung.getId());
		final byte[] content = pdfService.createOnboardingLetter(onboarding);
		RegistrierungFile pdf = confirmationService.saveAndSendLetter(registrierung, RegistrierungFileTyp.ONBOARDING_LETTER, content);
		LOG.info("VACME-ONBOARDING: uploaded letter for {} {}", onboarding.getCode(), registrierung.getId());

		// save the pdf id to the onboarding
		onboarding.setOnboardingPdf(pdf);
		LOG.info("VACME-ONBOARDING: created letter for {} {}", onboarding.getCode(), registrierung.getId());

	}

	@NonNull
	public List<String> findRegistrierungenForOnboarding(long batchSize) {
		return this.registrierungRepo.findRegistrierungenForOnboarding(batchSize);
	}

	@NonNull
	public Optional<Onboarding> findOnboardingByCode(@NonNull String onboardingCode) {
		return onboardingRepo.findOnboardingObjectByCode(onboardingCode);
	}

	@Transactional
	public Onboarding startOnboardingValidateAndIncreaseNumOfTries(String code, LocalDate geburtsdatum) {

		// find onboarding with this code
		var onboarding = onboardingRepo.findOnboardingObjectByCode(code)
			.orElseThrow(() -> {
					LOG.info("VACME-ONBOARDING Validation error during onboarding: invalid code. {}", code);
					return AppValidationMessage.ONBOARDING_INVALID_CODE.create(code);
				}
			);

		// limit tries to avoid brute force attack
		if (onboarding.getNumOfTries() > vacmeSettingsService.getOnboardingMaxTries()) {
			throw AppValidationMessage.ONBOARDING_CODE_LOCKED.create(onboarding.getCode());
		}

		// increase numOfTries (before checking geburtsdatum!) and replace the onboarding object because of the transaction
		onboarding = onboardingRepo.increaseNumOfTries(onboarding);

		// check used
		validateOnboardingCodeNotUsed(onboarding, "VACME-ONBOARDING-START");

		//validateRegistrierung not yet linked
		validateRegistrierungNotYetLinked(onboarding, "VACME-ONBOARDING-START");

		// validate onboarding age: The onboarding entry was created and then sent as a letter -> it should be used within maybe 30 days?
		validateOnboardingCodeAge(onboarding);

		// validate geburtsdatum
		Registrierung registrierung = onboarding.getRegistrierung();
		if (!registrierung.getGeburtsdatum().isEqual(geburtsdatum)) {
			LOG.info("VACME-ONBOARDING Invalid onboarding birthday");
			throw AppValidationMessage.ONBOARDING_INVALID_CODE.create(onboarding.getCode());
		}

		return onboarding; // return the onboarding that has an incremented numOfTries (new transaction)

	}

	private void validateOnboardingCodeNotUsed(Onboarding onboarding, String prefix) {
		if (onboarding.getUsed()) {
			LOG.info("{} Code was already used {}", prefix, onboarding.getCode());
			throw AppValidationMessage.ONBOARDING_CODE_LOCKED.create(onboarding.getCode());
		}
	}

	@Transactional
	@Nullable
	public String startOnboarding(Onboarding onboarding, @Nullable String language) {
		Registrierung registrierung = onboarding.getRegistrierung();

		// save browser language from onboarding page to registration
		if (StringUtils.isNotEmpty(language)) {
			registrierung.setSprache(Sprache.from(language));
		}

		// save
		registrierungRepo.update(registrierung);

		// generate a token for localstorage and save this in onboarding
		return onboardingRepo.generateOnboardingTempToken(onboarding);

	}

	public void finishOnboardingAfterKeycloakRegistration(String onboardingToken, Benutzer benutzer) {

		// find onboarding with this token and validate
		var onboarding = findAndValidateOnboarding(onboardingToken, benutzer);

		// connect Benutzer with Registrierung
		connectBenutzerWithRegistrierung(benutzer, onboarding);
	}

	public Onboarding findAndValidateOnboarding(String onboardingToken, Benutzer benutzer) {

		// find onboarding with this token
		Onboarding onboarding = onboardingRepo.findOnboardingObjectByToken(onboardingToken).orElseThrow(() ->
			{
				LOG.info("VACME-ONBOARDING Validation error while finishing onboarding: invalid onboarding token. {}", onboardingToken);
				return AppValidationMessage.ONBOARDING_FINISH_FAILED.create("invalid token");
			}
		);

		// validate
		validateToCompleteOnboarding(onboardingToken, onboarding, benutzer);

		return onboarding;
	}

	private void connectBenutzerWithRegistrierung(Benutzer benutzer, Onboarding onboarding) {
		// update Registrierung: add Benutzer and change type to online
		Registrierung registrierung = onboarding.getRegistrierung();
		validateRegistrierungNotYetLinked(onboarding, "VACME-ONBOARDING-COMPLETION");
		LOG.info("VACME-ONBOARDING: connecting Benutzer {} with Registrierung {} because of onboarding {}",
			benutzer.getId(),
			registrierung.getId(),
			onboarding.getCode());
		registrierung.setBenutzerId(benutzer.getId());
		registrierung.setMail(benutzer.getEmail());
		registrierung.setRegistrierungsEingang(RegistrierungsEingang.ONLINE_REGISTRATION);
		registrierungRepo.update(registrierung);

		// update Onboarding: set used
		onboarding.setUsed(true);
		onboardingRepo.update(onboarding);
	}

	private void validateRegistrierungNotYetLinked(@NonNull Onboarding onboarding, @NonNull String prefix) {
		Registrierung registrierung = onboarding.getRegistrierung();
		if (registrierung.getBenutzerId() != null) {
			LOG.info("{} Validation error during onboarding: registration already belongs to a user. {}, {}", prefix, onboarding.getCode(),
				registrierung.getRegistrierungsnummer());
			throw AppValidationMessage.ONBOARDING_FINISH_FAILED.create(onboarding.getCode());
		}
	}

	private void validateToCompleteOnboarding(String onboardingToken, Onboarding onboarding, Benutzer benutzer) {
		// validate token
		if (onboarding.getOnboardingTempToken() == null
			|| onboarding.getOnboardingTempToken().isEmpty()
			|| !onboarding.getOnboardingTempToken().equals(onboardingToken)) {
			LOG.info("VACME-ONBOARDING Validation error during onboarding: Invalid onboarding token. {}, {}", onboarding.getCode(), onboardingToken);
			throw AppValidationMessage.ONBOARDING_FINISH_FAILED.create(onboarding.getCode());
		}

		// validate token age: The onboarding token was generated when the user started onboarding -> lifespan of 15 minutes
		int tokenTtlMinutes = vacmeSettingsService.getOnboardingTokenTTLMinutes();
		if (onboarding.getOnboardingTempTokenCreationTime() == null
			|| (onboarding.getOnboardingTempTokenCreationTime()
			.plusMinutes(tokenTtlMinutes)).isBefore(LocalDateTime.now())) {
			LOG.info("VACME-ONBOARDING Validation error during onboarding: Expired onboarding token. {}, {}", onboarding.getCode(), onboardingToken);
			throw AppValidationMessage.ONBOARDING_FINISH_FAILED.create(onboarding.getCode());
		}

		// validate onboarding age: The onboarding entry was created and then sent as a letter -> it should be used within maybe 30 days?
		validateOnboardingCodeAge(onboarding);

		// validate used flag: it can only be used once
		validateOnboardingCodeNotUsed(onboarding, "VACME-ONBOARDING-COMPLETION");

		// validate Benutzer has no Registrierung yet
		Registrierung registrierung = registrierungService.findRegistrierungByUser(benutzer.getId());
		if (registrierung != null) {
			LOG.info("VACME-ONBOARDING Validation error during onboarding: user has a registration already. {}, {}, {}",
				onboarding.getCode(), benutzer.getId(), registrierung.getId());
			throw AppValidationMessage.ONBOARDING_FINISH_FAILED.create(onboarding.getCode());
		}
	}

	private void validateOnboardingCodeAge(Onboarding onboarding) {
		int onboardingTtlDays = vacmeSettingsService.getOnboardingCodeTTLDays();
		if (onboarding.getTimestampErstellt() == null
			|| (onboarding.getTimestampErstellt()
			.plusDays(onboardingTtlDays)).isBefore(LocalDateTime.now())) {
			LOG.info("VACME-ONBOARDING Validation error during onboarding: Expired onboarding entry. {}", onboarding.getCode());
			throw AppValidationMessage.ONBOARDING_CODE_LOCKED.create(onboarding.getCode());
		}
	}

	public boolean isValidOnboardingCode(String onboardingCode) {
		return this.onboardingRepo.isValidOnboardingCode(onboardingCode);
	}
}
