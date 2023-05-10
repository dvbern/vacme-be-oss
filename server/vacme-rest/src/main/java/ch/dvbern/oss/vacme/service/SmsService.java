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

import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.enums.Mandant;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.service.boosterprioritaet.BoosterPrioUtil;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertBatchType;
import ch.dvbern.oss.vacme.service.sms.AbstractSmsProvider;
import ch.dvbern.oss.vacme.service.sms.ECallSmsProvider;
import ch.dvbern.oss.vacme.service.sms.SwissphoneSmsProvider;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.Gsm0338;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.PhoneNumberUtil;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jetbrains.annotations.NotNull;

import static ch.dvbern.oss.vacme.shared.util.Constants.SMS_CONNECTION_TIMEOUT;

@ApplicationScoped
@Transactional
@Slf4j
public class SmsService {

	@ConfigProperty(name = "vacme.stufe", defaultValue = "LOCAL")
	String stufe;

	@ConfigProperty(name = "vacme.sms.disabled", defaultValue = "false")
	boolean sendingOfSmsDisabled;

	@ConfigProperty(name = "vacme.sms.mobile-only", defaultValue = "true")
	boolean mobileOnly;

	@ConfigProperty(name = "vacme.sms.async", defaultValue = "true")
	boolean async;

	@ConfigProperty(name = "vacme.sms.provider", defaultValue = "ecall")
	String provider;

	@ConfigProperty(name = "vacme.sms.ecall.url")
	String urlECall;

	@ConfigProperty(name = "vacme.sms.ecall.username")
	String usernameECall;

	@ConfigProperty(name = "vacme.sms.ecall.password")
	String passwordECall;

	@ConfigProperty(name = "vacme.sms.ecall.jobid")
	String jobIdECall;

	@ConfigProperty(name = "vacme.sms.ecall.callback")
	String callbackECall;

	@ConfigProperty(name = "vacme.sms.ecall.callback.ext")
	String callbackExtECall;

	@ConfigProperty(name = "vacme.sms.swissphone.url")
	String urlSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.username")
	String usernameSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.password")
	String passwordSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.jobid")
	String jobIdSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.callback")
	String callbackSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.callback.ext")
	String callbackExtSwissphone;

	@ConfigProperty(name = "vacme.sms.link", defaultValue = "https://be.vacme.ch")
	String link;

	@ConfigProperty(name = "vacme.sms.link.booster.zh", defaultValue = "https://zh.ch/booster")
	String linkBoosterZh;

	@ConfigProperty(name = "vacme.sms.connection.timeout", defaultValue = SMS_CONNECTION_TIMEOUT)
	int connectionTimeout;

	@ConfigProperty(name = "vacme.sms.detail.debug.enabled", defaultValue = "true" )
	boolean smsDetailDebugEnabled;

	public void sendOnlineRegistrierungsSMS(@NonNull Registrierung registrierung, @NonNull String empfaenger, @NonNull Benutzer benutzer) {
		String key = "sms_registrierung_online_text";
		String vorname = StringUtils.abbreviate(registrierung.getVorname(), Constants.MAX_NAME_LENGTH_COVIDCERT);
		String name = StringUtils.abbreviate(registrierung.getName(), Constants.MAX_NAME_LENGTH_COVIDCERT);
		final var message = ServerMessageUtil.getMessage(
			key,
			registrierung.getLocale(),
			vorname,
			name,
			link,
			registrierung.getRegistrierungsnummer(),
			benutzer.getBenutzername());
		sendSMSToRegistrierung(empfaenger, message, registrierung);
	}

	public void sendOdiRegistrierungsSMS(@NonNull Registrierung registrierung, @NonNull String empfaenger) {
		final var message = ServerMessageUtil.getMessage(
			"sms_registrierung_odi_text",
			registrierung.getLocale(),
			registrierung.getRegistrierungsnummer());
		sendSMSToRegistrierung(empfaenger, message, registrierung);
	}

	public void sendPasswordSMS(@NonNull String password, @NonNull Locale locale, @NonNull String empfaenger) {
		final var message = ServerMessageUtil.getMessage(
			"sms_password_text",
			locale,
			password);
		sendSMS(empfaenger, message, locale);
	}


	public void sendTerminbestaetigungSMS(
		@NonNull Impfdossier impfdossier,
		@Nullable Impftermin boosterTerminOrNull,
		@NonNull Benutzer empfaengerWithValidMobileNumber
	) {
		// Fuer FSME-Impfungen von Well-Benutzern schicken wir keine SMS
		if (checkWellEnabledAndWellUserOtherwiseLog(impfdossier.getKrankheitIdentifier(), empfaengerWithValidMobileNumber)) {
			return;
		}
		String message = null;
		Registrierung registrierung = impfdossier.getRegistrierung();
		final boolean isAlreadyGrundimmunisiert =
			ImpfdossierStatus.getAnyStatusOfGrundimmunisiert().contains(impfdossier.getDossierStatus());
		if (isAlreadyGrundimmunisiert && boosterTerminOrNull != null) {
			message = messageTerminbestaetigungBoosterTermin(impfdossier, boosterTerminOrNull);
		} else if (!isAlreadyGrundimmunisiert && impfdossier.getBuchung().getImpftermin1() != null) {
			message = messageTerminbestaetigungBasisTermine(impfdossier);
		} else if (impfdossier.getBuchung().getGewuenschterOdi() != null) {
			message = messageTerminbestaetigungOdiOhneTerminverwaltungSelected(impfdossier);
		} else if (impfdossier.getBuchung().isNichtVerwalteterOdiSelected()) {
			message = messageTerminbestaetigungNichtVerwalteterOdiSelected(impfdossier);
		}
		if (message != null) {
			Objects.requireNonNull(empfaengerWithValidMobileNumber.getMobiltelefon());
			sendSMSToRegistrierung(empfaengerWithValidMobileNumber.getMobiltelefon(), message, registrierung);
		}
	}

	@NonNull
	private String messageTerminbestaetigungBasisTermine(@NonNull Impfdossier impfdossier) {
		Registrierung registrierung = impfdossier.getRegistrierung();
		final Impftermin impfterminErsteImpffolge = impfdossier.getBuchung().getImpftermin1();
		Objects.requireNonNull(impfterminErsteImpffolge);
		String termin1 = "-";
		String impfzentrum = "";
		// Infos von T1 werden nur angezeigt wenn T1 noch nicht wahrgenommen
		if (!ImpfdossierStatus.isErsteImpfungDoneAndZweitePending().contains(impfdossier.getDossierStatus())) {
			impfzentrum  = getOdiAdresseShort(impfterminErsteImpffolge.getImpfslot().getOrtDerImpfung());
			termin1 = impfterminErsteImpffolge.getTerminZeitfensterStartDateAndTimeString();
		}

		String termin2 = "-";
		final Impftermin impfterminZweiteImpffolge = impfdossier.getBuchung().getImpftermin2();
		if (impfterminZweiteImpffolge != null) {
			impfzentrum = getOdiAdresseShort(impfterminZweiteImpffolge.getImpfslot().getOrtDerImpfung());
			termin2 = impfterminZweiteImpffolge.getTerminZeitfensterStartDateAndTimeString();
		}
		return ServerMessageUtil.getMessage(
			"sms_terminbestaetigung_text",
			registrierung.getLocale(),
			termin1,
			termin2,
			impfzentrum,
			registrierung.getRegistrierungsnummer(),
			link);
	}

	@NonNull
	private String messageTerminbestaetigungBoosterTermin(@NonNull Impfdossier impfdossier, @NonNull Impftermin boosterTermin) {
		String terminN = boosterTermin.getTerminZeitfensterStartDateAndTimeString();
		String impfzentrum = getOdiAdresseShort(boosterTermin.getImpfslot().getOrtDerImpfung());
		Registrierung registrierung = impfdossier.getRegistrierung();
		return ServerMessageUtil.getMessage(
			"sms_terminbestaetigung_booster_text",
			registrierung.getLocale(),
			getKrankheitString(impfdossier.getKrankheitIdentifier(), registrierung.getLocale()),
			terminN,
			impfzentrum,
			registrierung.getRegistrierungsnummer(),
			link);
	}

	@NonNull
	private String messageTerminbestaetigungOdiOhneTerminverwaltungSelected(@NonNull Impfdossier impfdossier) {
		Objects.requireNonNull(impfdossier.getBuchung().getGewuenschterOdi());
		String impfzentrum = getOdiAdresseShort(impfdossier.getBuchung().getGewuenschterOdi());
		if (impfdossier.getBuchung().getGewuenschterOdi().isMobilerOrtDerImpfung()) {
			return ServerMessageUtil.getMessage(
				"sms_terminbestaetigung_text_mobiler_odi",
				impfdossier.getRegistrierung().getLocale(),
				getKrankheitString(impfdossier.getKrankheitIdentifier(), impfdossier.getRegistrierung().getLocale()),
				impfzentrum,
				impfdossier.getRegistrierung().getRegistrierungsnummer(),
				link);
		}
		return ServerMessageUtil.getMessage(
			"sms_terminbestaetigung_text_ohne_termin",
			impfdossier.getRegistrierung().getLocale(),
			impfzentrum,
			getKrankheitString(impfdossier.getKrankheitIdentifier(), impfdossier.getRegistrierung().getLocale()),
			impfdossier.getRegistrierung().getRegistrierungsnummer(),
			link);
	}

	@NonNull
	private String messageTerminbestaetigungNichtVerwalteterOdiSelected(@NonNull Impfdossier impfdossier) {
		Registrierung registrierung = impfdossier.getRegistrierung();
		return ServerMessageUtil.getMessage(
			"sms_terminbestaetigung_text_nicht_verwalteter_odi",
			registrierung.getLocale(),
			getKrankheitString(impfdossier.getKrankheitIdentifier(), registrierung.getLocale()),
			registrierung.getRegistrierungsnummer(),
			link);
	}

	public void sendFreigabeBoosterSMS(
		@NonNull ImpfinformationDto infos,
		@Nullable String benutzername,
		@NonNull Benutzer empfaengerWithValidMobileNumber
	) {
		// Fuer FSME-Impfungen von Well-Benutzern schicken wir keine SMS
		if (checkWellEnabledAndWellUserOtherwiseLog(infos.getKrankheitIdentifier(), empfaengerWithValidMobileNumber)) {
			return;
		}
		LocalDate latestImpfung = BoosterPrioUtil.getDateOfNewestImpfung(infos);
		Registrierung registrierung = infos.getRegistrierung();

		String vorname = registrierung.getVorname();
		String name = registrierung.getName();
		String buchungslink = Mandant.ZH == MandantUtil.getMandant() ? linkBoosterZh : link;
		String krankheit = getKrankheitString(infos.getKrankheitIdentifier(), registrierung.getLocale());
		String finalMsg;

		if (latestImpfung == null) {
			finalMsg = ServerMessageUtil.getMessage(
				"sms_booster_freigabe_text",
				registrierung.getLocale(),
				vorname,
				name,
				krankheit,
				buchungslink,
				registrierung.getRegistrierungsnummer());
		} else {
			finalMsg = ServerMessageUtil.getMessage(
				"sms_booster_freigabe_with_previous_impfung_text",
				registrierung.getLocale(),
				vorname,
				name,
				DateUtil.formatDate(latestImpfung, registrierung.getLocale()),
				krankheit,
				buchungslink,
				registrierung.getRegistrierungsnummer());
		}

		if (benutzername != null) {
			final var messageBenutzername = ServerMessageUtil.getMessage(
				"sms_booster_freigabe_text_benutzername",
				registrierung.getLocale(),
				benutzername);

			finalMsg += "\n" + messageBenutzername;
		}
		Objects.requireNonNull(empfaengerWithValidMobileNumber.getMobiltelefon());
		sendSMSToRegistrierung(empfaengerWithValidMobileNumber.getMobiltelefon(), finalMsg, registrierung);
	}

	@NonNull
	private static String getKrankheitString(@NonNull KrankheitIdentifier krankheitIdentifier, @NonNull Locale locale) {
		return ServerMessageUtil.getMessage(
			"KrankheitIdentifier_" + krankheitIdentifier.name(),
			locale);
	}

	public void sendBenutzername(@NonNull Benutzer benutzer, @NonNull Locale locale) {
		final String empfaenger = benutzer.getMobiltelefon();
		if (empfaenger == null) {
			return;
		}
		final var message = ServerMessageUtil.getMessage(
			"sms_benutzername_text",
			locale,
			benutzer.getBenutzername());

		sendSMS(empfaenger, message, locale);
	}

	public void sendTerminabsage(
		@NonNull Impfdossier impfdossier,
		@NonNull Impftermin termin,
		@NonNull Benutzer empfaengerWithValidMobileNumber,
		@NonNull String terminEffectiveStartBeforeOffsetReset
	) {
		// Fuer FSME-Impfungen von Well-Benutzern schicken wir keine SMS
		if (checkWellEnabledAndWellUserOtherwiseLog(impfdossier.getKrankheitIdentifier(), empfaengerWithValidMobileNumber)) {
			return;
		}
		String message = null;
		switch (termin.getImpffolge()) {

		case ERSTE_IMPFUNG:
			message = ServerMessageUtil.getMessage(
				"sms_terminabsage_text",
				impfdossier.getRegistrierung().getLocale(),
				"1",
				terminEffectiveStartBeforeOffsetReset,
				impfdossier.getRegistrierung().getRegistrierungsnummer(),
				link);
			break;
		case ZWEITE_IMPFUNG:
			message = ServerMessageUtil.getMessage(
				"sms_terminabsage_text",
				impfdossier.getRegistrierung().getLocale(),
				"2",
				terminEffectiveStartBeforeOffsetReset,
				impfdossier.getRegistrierung().getRegistrierungsnummer(),
				link);
			break;
		case BOOSTER_IMPFUNG:
			message = ServerMessageUtil.getMessage(
				"sms_terminabsage_booster_text",
				impfdossier.getRegistrierung().getLocale(),
				terminEffectiveStartBeforeOffsetReset,
				impfdossier.getRegistrierung().getRegistrierungsnummer(),
				link);
			break;
		}
		Objects.requireNonNull(empfaengerWithValidMobileNumber.getMobiltelefon());
		// Die Terminabsagen senden wir immer synchron
		sendSmsImplWithOneRetryForReg(empfaengerWithValidMobileNumber.getMobiltelefon(), message, impfdossier.getRegistrierung());
	}

	public void sendTerminabsageBeideTermine(
		@Nonnull Registrierung registrierung,
		@NonNull String empfaenger,
		@NonNull String terminEffectiveStart,
		@NonNull String termin2EffectiveStart
	) {
		final var message = ServerMessageUtil.getMessage(
			"sms_terminabsage_beide_termine_text",
			registrierung.getLocale(),
			terminEffectiveStart,
			termin2EffectiveStart,
			registrierung.getRegistrierungsnummer(),
			link);
		// Die Terminabsagen senden wir immer synchron
		sendSmsImplWithOneRetryForReg(empfaenger, message, registrierung);
	}

	public void sendZertifikatsbenachrichtigung(@NonNull Registrierung registrierung, @NonNull String empfaenger, @NonNull CovidCertBatchType batchType) {
		// Es gibt ein SMS bei OnlineVersand, aber auch bei Postversand einer OnlineReg
		String messageKey = batchType == CovidCertBatchType.ONLINE ? "sms_covidcertificate_text" : "sms_covidcertificate_post_text";
		String vorname = StringUtils.abbreviate(registrierung.getVorname(), Constants.MAX_NAME_LENGTH_COVIDCERT);
		String name = StringUtils.abbreviate(registrierung.getName(), Constants.MAX_NAME_LENGTH_COVIDCERT);
		final var message = ServerMessageUtil.getMessage(
			messageKey,
			registrierung.getLocale(),
			vorname, name, registrierung.getRegistrierungsnummer(), link);
		// Die Terminabsagen senden wir immer synchron
		sendSmsImplWithOneRetryForReg(empfaenger, message, registrierung);
	}

	public void sendZertifikatCounterNeuerstelltBenachrichtigung(@NonNull Registrierung registrierung, String empfaenger) {
		final var message = ServerMessageUtil.getMessage(
			"sms_covidcertificate_counter_neuerstellt_text",
			registrierung.getLocale(),
			registrierung.getNameVorname(),
			registrierung.getRegistrierungsnummer());
		sendSmsImplWithOneRetryForReg(empfaenger, message, registrierung); // auch sync, analog zu sendZertifikatsbenachrichtigung
	}

	public void sendZertifikatRevocationBenachrichtigung(@NonNull Zertifikat zertifikat, @NonNull String empfaenger) {
		Registrierung registrierung = zertifikat.getRegistrierung();
		final var message = ServerMessageUtil.getMessage(
			"sms_covidcertificate_revocation_text",
			registrierung.getLocale(),
			registrierung.getRegistrierungsnummer(),
			zertifikat.getUvci());
		// Die Terminabsagen senden wir immer synchron
		sendSmsImplWithOneRetryForReg(empfaenger, message, registrierung);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void sendSMS(@NonNull String empfaenger, @NonNull String message, @NonNull Locale locale) {
		if (async) {
			sendSMSAsync(empfaenger, message, locale);
		} else {
			sendSmsImplWithOneRetry(empfaenger, message, locale);
		}

	}

	@Transactional(TxType.REQUIRES_NEW)
	public void sendSMSToRegistrierung(@NonNull String empfaenger, @NonNull String message, @NonNull Registrierung reg) {
		if (async) {
			sendSMSAsyncForReg(empfaenger, message, reg);
		} else {
			sendSmsImplWithOneRetryForReg(empfaenger, message, reg);
		}
	}

	private void sendSMSAsyncForReg(@NonNull String empfaenger, @NonNull String message, @NonNull Registrierung reg) {
		if (isVerstorbenAndLog(reg)) {
			return;
		}
		sendSMSAsync(empfaenger, message, reg.getLocale());
	}

	private void sendSMSAsync(@NonNull String empfaenger, @NonNull String message, @NonNull Locale locale) {
		Uni.createFrom().item(message)
			.emitOn(Infrastructure.getDefaultWorkerPool())
			.subscribe().with(
			item -> {
				sendSmsImplWithOneRetry(empfaenger, message, locale);
			}, throwable -> {
				LOG.error(String.format("Sending SMS Failed. Was going to send message '%s' to '%s'", message,
					empfaenger), throwable);
			}
		);
	}

	private void sendSmsImplWithOneRetryForReg(@NonNull String empfaenger, @NonNull String message, @NonNull Registrierung reg) {
		if (isVerstorbenAndLog(reg)) {
			return;
		}
		sendSmsImplWithOneRetry(empfaenger, message, reg.getLocale());
	}

	private void sendSmsImplWithOneRetry(@NonNull String empfaenger, @NonNull String message, @NonNull Locale locale) {
		try {
			sendSmsImpl(empfaenger, message, locale);
		} catch (Exception ex) {
			// retry once
			LOG.warn("VACME-SMS: Could not send SMS, will retry one more time ", ex);
			sendSmsImpl(empfaenger, message, locale);
		}
	}

	private boolean isVerstorbenAndLog(@NonNull Registrierung registrierung) {
		if (Boolean.TRUE.equals(registrierung.getVerstorben())) {
			LOG.warn("VACME-SMS: Versand abgebrochen, Registrierung ist als verstorben markiert {}", registrierung.getRegistrierungsnummer());
			return true;
		}
		return false;
	}

	private boolean checkWellEnabledAndWellUserOtherwiseLog(@NonNull KrankheitIdentifier krankheitIdentifier, @NonNull Benutzer benutzer) {
		if (krankheitIdentifier.isWellEnabled() && benutzer.getWellId() != null) {
			LOG.warn("VACME-SMS: Versand abgebrochen, Benachrichtigung fuer {} fuer den Well-Benutzer {}",
				krankheitIdentifier,
				benutzer.getWellId());
			return true;
		}
		return false;
	}

	private void sendSmsImpl(@NotNull String empfaenger, @NotNull String message, @NotNull Locale locale) {

		// Leerzeichen aus Mobilenummern entfernen, evtl. Prefix setzen
		var trimmedMobileNumber = PhoneNumberUtil.processMobileNumber(empfaenger);

		if (!PhoneNumberUtil.isMobileNumber(trimmedMobileNumber) && mobileOnly) {
			LOG.warn("VACME-SMS: Meldung verhindert, Empfaenger ist keine Mobile-Nummer: {}", empfaenger);
			return;
		}

		// Falls das SMS aus einer Testumgebung verschickt wurde, ergaenzen wir die Message mit einem
		// entsprechenden Hinweis
		if (!"PROD".equalsIgnoreCase(stufe)) {
			String hinweisTestumgebungStart = ServerMessageUtil.getMessage("sms_hinweis_testumgebung_start", locale);
			String hinweisTestumgebungEnd = ServerMessageUtil.getMessage("sms_hinweis_testumgebung_end", locale);
			message = hinweisTestumgebungStart + '\n' + message + '\n' + hinweisTestumgebungEnd;
		}

		message = removeUnsupportedCharacters(message);

		if (sendingOfSmsDisabled) {
			pretendToSendSMS(empfaenger, message);
			return;
		}

		final AbstractSmsProvider smsProvider = getSmsProvider();

		long requestStart = System.currentTimeMillis();
		try {
			final URI uri = smsProvider.getUriWithParams(
				trimmedMobileNumber,
				message);
			var get = new HttpGet(uri);

			//specify timeouts

			RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(this.connectionTimeout * 1000)
				.setConnectionRequestTimeout(this.connectionTimeout * 1000)
				.setSocketTimeout(this.connectionTimeout * 1000).build();

			try (
				CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
				CloseableHttpResponse response = client.execute(get)
			) {
				LOG.debug(get.getURI().toASCIIString());
				if (response.getStatusLine().getStatusCode() != 200) {
					LOG.info("Status Code: " + response.getStatusLine().getStatusCode());
					LOG.info("Reason Phrase: " + response.getStatusLine().getReasonPhrase());
				}
			}
			if (smsDetailDebugEnabled) {
				LOG.info("Sending SMS to {}", StringUtils.substring(empfaenger, 0, 7));
			}
		} catch (Exception e) {
			LOG.error("Could not send SMS to {}", trimmedMobileNumber, e);
		} finally {
			long duration = System.currentTimeMillis() - requestStart;
			if (duration > 4000) {
				LOG.info("Sending SMS took longer than 4s:  {}ms", duration);
			}
		}
	}

	@NonNull
	String removeUnsupportedCharacters(@NonNull String message) {
		// In mobile telephony GSM 03.38 or 3GPP 23.038 is a character encoding used in GSM networks for SMS (Short Message Service)
		// If we send non-GSM_03.38 characters, the SMS is not delivered in full length!
		return Gsm0338.transformToGsm0338String(message);
	}

	private AbstractSmsProvider getSmsProvider() {
		if (AbstractSmsProvider.ECALL.equalsIgnoreCase(provider)) {
			return new ECallSmsProvider(urlECall, usernameECall, passwordECall, jobIdECall, callbackECall, callbackExtECall);
		}
		return new SwissphoneSmsProvider(urlSwissphone, usernameSwissphone, passwordSwissphone, jobIdSwissphone, callbackSwissphone, callbackExtSwissphone);
	}

	private void pretendToSendSMS(@NonNull String empfaenger, @NonNull String message) {
		LOG.info("Sending of SMS disabled. Would send to: {}, Message: {}", empfaenger, message);
	}

	@NonNull
	private String getOdiAdresseShort(@NonNull OrtDerImpfung odi) {
		return odi.getName() + ", " + odi.getAdresse().getAdresse1() + ", " + odi.getAdresse().getOrt();
	}
}
