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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.dto.GuiSettingsDTO;
import ch.dvbern.oss.vacme.dto.ImpfslotCreationSettingsDTO;
import ch.dvbern.oss.vacme.dto.SmsSettingsDTO;
import ch.dvbern.oss.vacme.entities.base.ApplicationProperty;
import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.service.boosterprioritaet.AffenpockenImpfschutzcalculationConfigDTO;
import ch.dvbern.oss.vacme.service.boosterprioritaet.CovidImpfschutzcalculationConfigDTO;
import ch.dvbern.oss.vacme.service.boosterprioritaet.FSMEImpfschutzcalculationConfigDTO;
import ch.dvbern.oss.vacme.service.settings.FTPClientConfigDTO;
import ch.dvbern.oss.vacme.service.sms.AbstractSmsProvider;
import ch.dvbern.oss.vacme.service.sms.ECallSmsProvider;
import ch.dvbern.oss.vacme.service.sms.SwissphoneSmsProvider;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.COVID_ZERTIFIKAT_ENABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.DISTANCE_BETWEEN_IMPFUNGEN_DISIRED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_AFTER;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_BEFORE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.KORREKTUR_EMAIL_TELEPHONE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.MINUTES_BETWEEN_INFO_UPDATES;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.MINUTES_BETWEEN_PHONENUMBER_UPDATE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.SELBSTZAHLER_FACHAPPLIKATION_ENABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.SELBSTZAHLER_PORTAL_ENABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.SHOW_ONBOARDING_WELCOME_TEXT;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_FREIGABE_JOB_BATCH_SIZE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_FREIGABE_NOTIFICATION_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_FREIGABE_NOTIFICATION_TERMIN_N_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_FREIGABE_SMS_SLEEP_TIME_MS;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_RULE_ENGINE_JOB_BATCH_SIZE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_RULE_ENGINE_JOB_PARTITIONS;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_BOOSTER_STATUSMOVER_JOB_BATCH_SIZE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_COVIDAPI_PS_BATCHSIZE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_CRON_GLOBAL_FREI_TERMINE_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_CRON_ODI_TERMINE_FREI_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_DEACTIVATE_UNUSED_USERACCOUNTS_AFTER_MINUTES;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_GEOCODING_ENABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_BATCH_SIZE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_PARTITIONS;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VACME_ONBOARDING_BRIEF_BATCHSIZE;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VMDL_CRON_3QUERIES;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VMDL_CRON_DISABLED;
import static ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey.VMDL_CRON_MANUAL_TRIGGER_MULTIPLICATOR;
import static ch.dvbern.oss.vacme.service.onboarding.OnboardingHashIdService.onboardingIDAlphabet;
import static ch.dvbern.oss.vacme.shared.util.Constants.DB_QUERY_SLOW_THRESHOLD;
import static ch.dvbern.oss.vacme.shared.util.Constants.GEOLOCATION_CONNECTION_TIMEOUT;
import static ch.dvbern.oss.vacme.shared.util.Constants.SMS_CONNECTION_TIMEOUT;

@SuppressWarnings("PackageVisibleField")
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class VacmeSettingsService {

	private final ApplicationPropertyService applicationPropertyService;

	// default is 30 days in minutes (60 * 24 * 30)
	@ConfigProperty(name = "vacme.deactivate.unused.useraccounts.after.minutes", defaultValue = "43200")
	int vacmeDeactivateUnusedUseraccountsAfterMinutes;

	@ConfigProperty(name = "vacme.deactivate.unused.useraccounts.batchsize", defaultValue = "200")
	int deactivateUnusedUseraccountsBatchsize;

	@ConfigProperty(name = "vacme.booster.rule.engine.job.batchsize", defaultValue = "1")
	long vacmeBoosterRuleEngineJobBatchSize;

	@ConfigProperty(name = "vacme.booster.move.immunisiert.batchsize", defaultValue = "1")
	long moveVollstGeimpfteToImmunisiertBatchSize;

	@ConfigProperty(name = "vacme.booster.move.freigegeben.batchsize", defaultValue = "1")
	long moveImmunisiertToFreigegebenBatchSize;

	@ConfigProperty(name = "vacme.booster.rule.engine.job.partitions", defaultValue = "3")
	long numberOfPartitions;

	@ConfigProperty(name = "vacme.freigabe.sms.sleeptime.ms", defaultValue = "50")
	long freigabeJobSMSSleepTime;

	@ConfigProperty(name = "vacme.massenverarbeitung.queue.job.batchsize", defaultValue = "200")
	long vacmeMassenverarbeitungQueueProcessingBatchSize;

	@ConfigProperty(name = "vacme.covidapi.ps.batchsize")
	long covidapiBatchSize;

	@ConfigProperty(name = "vacme.covidapi.ps.sleeptime.ms")
	long covidapiSleeptime;

	@ConfigProperty(name = "boosterrule.enable.pfizer.only.for.u30", defaultValue = "false")
	boolean calcCovidEnablePfizerOnlyForU30;

	@ConfigProperty(name = "boosterrule.zh.minage", defaultValue = "16")
	int calcCovidMinAgeZh;

	@ConfigProperty(name = "boosterrule.be.minage", defaultValue = "16")
	int calcCovidMinAgeBe;

	@ConfigProperty(name = "boosterrule.freigabeoffset.impfung.monate", defaultValue = "4")
	int calcCovidFreigabeOffsetImpfungMonate;

	@ConfigProperty(name = "boosterrule.freigabeoffset.impfung.tage", defaultValue = "0")
	int calcCovidFreigabeOffsetImpfungTage;

	@ConfigProperty(name = "boosterrule.freigabeoffset.krankheit.monate")
	Optional<Integer> calcCovidFreigabeOffsetKrankheitMonate = Optional.empty();

	@ConfigProperty(name = "boosterrule.freigabeoffset.krankheit.tage")
	Optional<Integer> calcCovidFreigabeOffsetKrankheitTage = Optional.empty();

	@ConfigProperty(name = "boosterrule.selbstzahler.cutoff")
	public Optional<LocalDate> calcCovidFreigabeCutoffDateSelbstzahler = Optional.empty();

	@ConfigProperty(name = "boosterrule.affenpocken.minage", defaultValue = "18")
	int calcAffenpockenMinAge;

	@ConfigProperty(name = "boosterrule.affenpocken.freigabeoffset.withimpfschutz.amount", defaultValue = "2")
	int calcAffenpockenFreigabeOffsetImpfungWithImpfschutz;

	@ConfigProperty(name = "boosterrule.affenpocken.freigabeoffset.withimpfschutz.unit", defaultValue = "YEARS")
	ChronoUnit calcAffenpockenFreigabeoffsetLetzteImpfungWithImpfschutzUnit = ChronoUnit.YEARS;

	@ConfigProperty(name = "boosterrule.affenpocken.freigabeoffset.noimpfschutz.amount", defaultValue = "4")
	int calcAffenpockenFreigabeOffsetImpfungWithoutImpfschutz;

	@ConfigProperty(name = "boosterrule.affenpocken.freigabeoffset.noimpfschutz.unit", defaultValue = "WEEKS")
	ChronoUnit calcAffenpockenFreigabeoffsetLetzteImpfungWithoutImpfschutzUnit = ChronoUnit.WEEKS;

	@ConfigProperty(name = "impfslot.duration", defaultValue = "30")
	Integer impfslotDuration;

	@ConfigProperty(name = "impfslot.work.start", defaultValue = "6")
	Integer impfslotStart;

	@ConfigProperty(name = "impfslot.work.end", defaultValue = "22")
	Integer impfslotEnd;

	@ConfigProperty(name = "vacme.admin.mail")
	String mailAdmin;

	@ConfigProperty(name = "vacme.mail.reporting.anzahl.impfungen.disabled", defaultValue = "false")
	boolean reportingAnzahlImpfungenDisabled;

	@ConfigProperty(name = "vacme.mail.reporting.anzahl.zweitbooster.disabled", defaultValue = "false")
	boolean reportingAnzahlZweitBoosterDisabled;

	@ConfigProperty(name = "geocode.api.key")
	Optional<String> geocodeApiKeyOptional;

	@ConfigProperty(name = "vmdl.cron.disabled", defaultValue = "true")
	boolean vmdlCronDisabled;

	@ConfigProperty(name = "vacme.service.automatisch.abschliessen.zeit.days", defaultValue = "100")
	int automatischAbschliessenZeitDays;

	@ConfigProperty(name = "vacme.service.archivierung.days", defaultValue = "400")
	int archivierungZeitDays;

	@ConfigProperty(name = "vacme.cron.archivierung.disabled")
	boolean archivierungD3Disabled;

	@ConfigProperty(name = "vacme.cron.global.freie.termine.disabled", defaultValue = "false")
	boolean cronUpdateNoFreieTermineProKrankheitDisabled;

	@ConfigProperty(name = "vacme.min.impftermin.for.meldung")
	Integer cronUpdateNoFreieTermineProKrankheitMinTermine;

	@ConfigProperty(name = "vacme.cron.odi.termine.frei.disabled", defaultValue = "false")
	boolean cronOdiNoTermineFreiDisabled;

	@ConfigProperty(name = "vacme.cron.stat.dbvalidation.disabled", defaultValue = "false")
	boolean cronDbValidationJobDisabled;

	@ConfigProperty(name = "vacme.cron.handle.kontrolle.abgelaufen.batchsize", defaultValue = "100")
	Integer cronKontrolleAbgelaufenBatchsize;

	@ConfigProperty(name = "vacme.stufe", defaultValue = "LOCAL")
	String stufe;

	//@Devs: If you need to run the download of fhir xml in quarkus:dev mode please change this property in your .env file
	// according to the .env-template (e.g. VACME_FHIR_PATH_CUSTOMNARRATIVES=file:src/main/resources/fhir/customnarratives-local.properties)
	@ConfigProperty(name = "vacme.fhir.path.customnarratives",
		defaultValue = "classpath:/fhir/customnarratives.properties")
	String fhirCustomNarrativePropertyFile;

	@ConfigProperty(name = "vacme.terminslot.offset.groups", defaultValue = "3")
	int terminslotOffsetGroups;

	@ConfigProperty(name = "vacme.terminslot.offset.max.termine.to.divide", defaultValue = "20")
	protected int slotOffsetMaxTermineToDivide;

	@ConfigProperty(name = "vacme.terminslot.offset.deterministic.when.low.capacity", defaultValue = "false")
	protected boolean slotOffsetDeterministicWhenLowCapacity;

	@ConfigProperty(name = "vacme.terminslot.offset.min.termine.per.slot", defaultValue = "5")
	protected int terminslotOffsetMinTerminePerSlot;

	@ConfigProperty(name = "vacme.terminreservation.enabled", defaultValue = "false") // mehrfach
	boolean terminReservationEnabled;

	@ConfigProperty(name = "vacme.terminreservation.dauer.in.min", defaultValue = "10")
	int terminReservationDauerInMinutes;

	@ConfigProperty(name = "vacme.terminvergabe.random.enabled", defaultValue = "false")
	boolean terminVergabeRandomEnabled;

	@ConfigProperty(name = "vacme.terminvergabe.lock.enabled", defaultValue = "false")
	boolean terminVergabeLockEnabled;

	@ConfigProperty(name = "vacme.sms.disabled", defaultValue = "false")
	boolean smsSendingDisabled;

	@ConfigProperty(name = "vacme.sms.mobile-only", defaultValue = "true")
	boolean smsMobileOnly;

	@ConfigProperty(name = "vacme.sms.async", defaultValue = "true")
	boolean smsAsync;

	@ConfigProperty(name = "vacme.sms.provider", defaultValue = "ecall")
	String smsProvider;

	@ConfigProperty(name = "vacme.sms.ecall.url")
	String smsUrlECall;

	@ConfigProperty(name = "vacme.sms.ecall.username")
	String smsUsernameECall;

	@ConfigProperty(name = "vacme.sms.ecall.password")
	String smsPasswordECall;

	@ConfigProperty(name = "vacme.sms.ecall.jobid")
	String smsJobIdECall;

	@ConfigProperty(name = "vacme.sms.ecall.callback")
	String smsCallbackECall;

	@ConfigProperty(name = "vacme.sms.ecall.callback.ext")
	String smsCallbackExtECall;

	@ConfigProperty(name = "vacme.sms.swissphone.url")
	String smsUrlSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.username")
	String smsUsernameSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.password")
	String smsPasswordSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.jobid")
	String smsJobIdSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.callback")
	String smsCallbackSwissphone;

	@ConfigProperty(name = "vacme.sms.swissphone.callback.ext")
	String smsCallbackExtSwissphone;

	@ConfigProperty(name = "vacme.sms.link", defaultValue = "https://be.vacme.ch")
	String applicationLink;

	@ConfigProperty(name = "vacme.sms.link.booster.zh", defaultValue = "https://zh.ch/booster")
	String applicationLinkBoosterZh;

	@ConfigProperty(name = "vacme.sms.connection.timeout", defaultValue = SMS_CONNECTION_TIMEOUT)
	int smsConnectionTimeout;

	@ConfigProperty(name = "vacme.sms.detail.debug.enabled", defaultValue = "true")
	boolean smsDetailDebugEnabled;

	@ConfigProperty(name = "vmdl.tenant_id")
	String vmdlTenantID;

	@ConfigProperty(name = "vmdl.username")
	String vmdlUsername;

	@ConfigProperty(name = "vmdl.password")
	String vmdlPassword;

	@ConfigProperty(name = "vmdl.client.logging.filter.disabled", defaultValue = "false")
	boolean vmdlLoggingFilterDisabled;

	@ConfigProperty(name = "vmdl.upload.chunk.limit", defaultValue = "100")
	int vmdlUploadChunkLimit;

	@ConfigProperty(name = "vmdl.reporting_unit_id")
	String vmdlReportingUnitID;

	@ConfigProperty(name = "vmdl.client_id")
	String vmdlClientIdCovid;

	@ConfigProperty(name = "vmdl.client_id.affenpocken")
	String vmdlClientIdAffenpocken;

	@ConfigProperty(name = "vacme.healthcheck.query.timeout", defaultValue = "290")
	int dbQueryTimeoutInSeconds;

	@ConfigProperty(name = "vacme.keycloak.serverUrl", defaultValue = "http://localhost:8180/")
	String keycloakServerUrl;
	@ConfigProperty(name = "vacme.keycloak.clientId", defaultValue = "")
	String keycloakClientId;
	@ConfigProperty(name = "vacme.keycloak.username", defaultValue = "")
	String keycloakUsername;
	@ConfigProperty(name = "vacme.keycloak.password", defaultValue = "")
	String keycloakPassword;

	// Web-Realm
	@ConfigProperty(name = "vacme.keycloak.realm", defaultValue = "vacme-web")
	String keycloakWebRealm;
	@ConfigProperty(name = "vacme.keycloak.clientSecret", defaultValue = "")
	String keycloakWebClientSecret;
	@ConfigProperty(name = "vacme.oidc.web.auth.server.url") // configured as VACME_OIDC_WEB_AUTH_SERVER_URL
	String keycloakWebIssuerUrl;
	@ConfigProperty(name = "vacme.keycloak.config.web")
	String keyCloakConfigWeb;

	// Reg-Realm
	@ConfigProperty(name = "vacme.keycloak.reg.realm", defaultValue = "vacme")
	String keycloakRegRealm;
	@ConfigProperty(name = "vacme.keycloak.reg.clientSecret", defaultValue = "")
	String keycloakRegClientSecret;
	@ConfigProperty(name = "vacme.oidc.reg.auth.server.url", defaultValue = "http://localhost:8180/auth/realms/vacme")
	// configured as VACME_OIDC_REG_AUTH_SERVER_URL
	String keycloakRegIssuerUrl; // as opposed to value configured for Fachapplikation VACME_OIDC_WEB_AUTH_SERVER_URL

	@ConfigProperty(name = "vacme.loggingintercepot.slowthreshold.ms", defaultValue = DB_QUERY_SLOW_THRESHOLD)
	long loggingSlowThresholdMs;

	@ConfigProperty(name = "covid-cert-api/mp-rest/keyStore")
	String covidCertApiPrivateKeyPath;

	@ConfigProperty(name = "covid-cert-api/mp-rest/keyStorePassword")
	String covidCertApiPassword;

	@ConfigProperty(name = "vacme.covidapi.ps.key.alias", defaultValue = "1")
	String covidCertApiKeyAlias;

	@ConfigProperty(name = "vacme.covidcert.postable.eingang")
	protected List<RegistrierungsEingang> covidCertPostableEingang;

	@ConfigProperty(name = "vacme.zertifikat.minimumwait.min", defaultValue = "10")
	int covidCertMinimumWaittimeMinutes;

	@ConfigProperty(name = "vacme.zertifikat.minimumwait.min.revocation.post.hours", defaultValue = "48")
	int covidCertMinimumWaittimeRevocationPostHours;

	@ConfigProperty(name = "vacme.cache.zertifikat.enabled.ttl.sconds", defaultValue = CacheUtil.DEFAULT_CACHE_TIME)
	String zertifikatEnabledCacheTimeToLive;

	@ConfigProperty(name = "vacme.kontrolle.gueltigkeit.hours", defaultValue = "4")
	Double kontrolleGueltigkeitHours; // Double zum Testen -> 0.1h moeglich

	@ConfigProperty(name = "vacme.minalter.impfung", defaultValue = "5")
	int minAlterCovidImpfung;

	@ConfigProperty(name = "vacme.validation.kontrolle.disallow.sameday", defaultValue = "true")
	Boolean validateSameDayKontrolle;

	@ConfigProperty(name = "vacme.validation.impfung.disallow.sameday", defaultValue = "true")
	Boolean validateSameDayImpfungen;

	@ConfigProperty(name = "vacme.onboarding.batchsize", defaultValue = "1")
	long onboardingBatchSize;

	@ConfigProperty(name = "vacme.onboarding.code.maxtries", defaultValue = "5")
	Integer onboardingMaxTries;

	@ConfigProperty(name = "vacme.onboarding.code.ttl.days", defaultValue = "40")
	Integer onboardingCodeTTLDays;

	@ConfigProperty(name = "vacme.onboarding.token.ttl.minutes", defaultValue = "15")
	Integer onboardingTokenTTLMinutes;

	@ConfigProperty(name = "vacme.onboarding.hashids.alphabet", defaultValue = onboardingIDAlphabet)
	String onboardingHashidAlphabet;

	@ConfigProperty(name = "vacme.onboarding.hashids.minLength", defaultValue = "5")
	int onboardingHashidMinLength;

	@ConfigProperty(name = "vacme.onboarding.hashids.salt")
	String onboardingHashidSalt;

	@ConfigProperty(name = "hashids.alphabet")
	String hashidAlphabet;

	@ConfigProperty(name = "hashids.minLength")
	int hashidMinLength;

	@ConfigProperty(name = "hashids.salt")
	String hashidSalt;

	@ConfigProperty(name = "vacme.service.reporting.anzahlzweitbooster.mail")
	String serviceReportingAnzahlZweitBoosterMail;

	@ConfigProperty(name = "vacme.service.reporting.anzahl.impfungen.fsme.galenica.recipients")
	String serviceReportingAnzahlImpfungenFsmeGalenicaRecipients;

	@ConfigProperty(name = "vacme.cron.doccreation.maxjobs", defaultValue = "2")
	int docCreationMaxNumOfJobsPerType;

	@ConfigProperty(name = "vacme.cron.doccreation.cleanup.maxage.seconds", defaultValue = "14400")
	long maxDocumentqueueResultAgeSeconds; //60 * 60 * 4 = 4 hours

	@ConfigProperty(name = "well.url")
	String wellUrl;
	@ConfigProperty(name = "well.api.client_id")
	String wellClientId;
	@ConfigProperty(name = "well.api.client_secret")
	String wellClientSecret;
	@ConfigProperty(name = "vacme.well.api.disabled", defaultValue = "false")
	boolean wellApiDisabled;

	@ConfigProperty(name = "vacme.cache.nextfrei.maxrange.months",
		defaultValue = Constants.DEFAULT_CUTOFF_TIME_MONTHS_FOR_FREE_TERMINE)
	String maxrangeToLookForFreieTermineInMonths;

	@ConfigProperty(name = "vacme.authorization.disable", defaultValue = "false")
	boolean authorizationDisabled;

	@ConfigProperty(name = "vacme.kantonsreport.map.impfstoff.names.from.db", defaultValue = "false")
	boolean kantonsreportMapImpfstoffNamesFromDB;

	@ConfigProperty(name = "tracing.respect.choice", defaultValue = "true")
	boolean tracingRespectChoice;

	@ConfigProperty(name = "migration.rate.limit.enabled", defaultValue = "false")
	boolean migrationEnabled;

	@ConfigProperty(name = "migration.rate.limit.interval.seconds", defaultValue = "30")
	int migrationInterval;

	@ConfigProperty(name = "migration.test_data.enabled", defaultValue = "false")
	boolean migrationTestDataEnabled;

	@ConfigProperty(name = "vacme.force.cookie.secure.flag", defaultValue = "false")
	boolean cookieSecure;

	@ConfigProperty(name = "vacme.upload.whitelist",
		defaultValue = "application/pdf,image/png,image/jpg,image/jpeg,image/svg,image/gif,image/bmp")
	List<String> whitelistedMimeTypesForUpload;

	@ConfigProperty(name = "vacme.cache.no.freietermine.ttl.sconds", defaultValue = CacheUtil.DEFAULT_CACHE_TIME)
	String noFreieTermineCacheTimeToLive;

	@ConfigProperty(name = "vacme.cache.nextfrei.ttl.sconds", defaultValue = CacheUtil.DEFAULT_CACHE_TIME)
	String nextfreierTerminCacheTimeToLive;

	@ConfigProperty(name = "vacme.ftp.disabled", defaultValue = "false")
	boolean ftpDisabled;

	@ConfigProperty(name = "vacme.ftp.server")
	String ftpServer;

	@ConfigProperty(name = "vacme.ftp.port", defaultValue = "21")
	int ftpPort;

	@ConfigProperty(name = "vacme.ftp.username")
	String ftpUsername;

	@ConfigProperty(name = "vacme.ftp.password")
	String ftpPassword;

	@ConfigProperty(name = "vacme.geocode.connection.timeout", defaultValue = GEOLOCATION_CONNECTION_TIMEOUT)
	int geocodeApiTimeout;

	@ConfigProperty(name = "vacme.mandant", defaultValue = "BE")
	String mandant;

	@ConfigProperty(name = "vacme.fachapplikation.url", defaultValue = "https://impfen-be.vacme.ch")
	String urlFachapplikation;

	@ConfigProperty(name = "vacme.fachapplikation.blog.url", defaultValue = "https://blog-impfen.vacme.ch/be/")
	String urlBlogFachapplikation;

	@ConfigProperty(name = "vacme.cc.zuwenige.zweittermine.mail")
	String ccMailZuwenigeZweittermine;

	@ConfigProperty(name = "boosterrule.fsme.minage", defaultValue = "6")
	int calcFsmeFreigabeMinAge;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.konventionell.withimpfschutz.amount", defaultValue = "10")
	int calcFsmeFreigabeOffsetKonventionellWithImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.konventionell.withimpfschutz.unit", defaultValue = "YEARS")
	ChronoUnit calcFsmeFreigabeOffsetKonventionellWithImpfschutzUnit;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.firstbooster.schnellschema.withimpfschutz.amount",
		defaultValue = "12")
	int calcFsmeFreigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.firstbooster.schnellschema.withimpfschutz.unit",
		defaultValue = "MONTHS")
	ChronoUnit calcFsmeFreigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutzUnit;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.firstimpfung.konventionell.noimpfschutz.amount",
		defaultValue = "4")
	int calcFsmeFreigabeOffsetKonventionellWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.firstimpfung.konventionell.noimpfschutz.unit",
		defaultValue = "WEEKS")
	ChronoUnit calcFsmeFreigabeOffsetKonventionellWithoutImpfschutzUnit;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.fsmeimmune.firstimpfung.schnellschema.noimpfschutz.amount",
		defaultValue = "14")
	int calcFsmeFreigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.fsmeimmune.firstimpfung.schnellschema.noimpfschutz.unit",
		defaultValue = "DAYS")
	ChronoUnit calcFsmeFreigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutzUnit;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.firstimpfung.schnellschema.noimpfschutz.amount",
		defaultValue = "7")
	int calcFsmeFreigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.firstimpfung.schnellschema.noimpfschutz.unit",
		defaultValue = "DAYS")
	ChronoUnit calcFsmeFreigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutzUnit;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.fsmeimmune.secondimpfung.noimpfschutz.amount",
		defaultValue = "5")
	int calcFsmeFreigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.fsmeimmune.secondimpfung.noimpfschutz.unit",
		defaultValue = "MONTHS")
	ChronoUnit calcFsmeFreigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutzUnit;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.secondimpfung.konventionell.noimpfschutz.amount",
		defaultValue = "9")
	int calcFsmeFreigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.secondimpfung.konventionell.noimpfschutz.unit",
		defaultValue = "MONTHS")
	ChronoUnit calcFsmeFreigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutzUnit;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.secondimpfung.schnellschema.noimpfschutz.amount",
		defaultValue = "14")
	int calcFsmeFreigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutz;
	@ConfigProperty(name = "boosterrule.fsme.freigabeoffset.encepur.secondimpfung.schnellschema.noimpfschutz.unit",
		defaultValue = "DAYS")
	ChronoUnit calcFsmeFreigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutzUnit;

	public boolean isVmdlCronDisabled() {
		return getBoolean(vmdlCronDisabled, VMDL_CRON_DISABLED);
	}

	@NonNull
	public Long getVmdlManualTriggerNumberOfRunsToTrigger() {
		return getLong(
			1L,
			VMDL_CRON_MANUAL_TRIGGER_MULTIPLICATOR);
	}

	public boolean getVmdlCovidRun3QueriesSettingEnabled() {
		return getBoolean(
			false,
			VMDL_CRON_3QUERIES);
	}

	public int getBenutzerDeactivateAfterInactiveTimeMinutes() {
		return getInt(
			vacmeDeactivateUnusedUseraccountsAfterMinutes,
			VACME_DEACTIVATE_UNUSED_USERACCOUNTS_AFTER_MINUTES);
	}

	public long getRuleEngineJobBatchSize() {
		return getLong(
			vacmeBoosterRuleEngineJobBatchSize,
			VACME_BOOSTER_RULE_ENGINE_JOB_BATCH_SIZE);
	}

	public long getMoveVollstGeimpfteToImmunisiertBatchSize() {
		return getLong(
			moveVollstGeimpfteToImmunisiertBatchSize,
			VACME_BOOSTER_STATUSMOVER_JOB_BATCH_SIZE);
	}

	public long getMoveImmunisierteToFreigegebeneBoosterBatchSize() {
		return getLong(
			moveImmunisiertToFreigegebenBatchSize,
			VACME_BOOSTER_FREIGABE_JOB_BATCH_SIZE);
	}

	public long getFreigabeSMSSleepTime() {
		return getLong(
			freigabeJobSMSSleepTime,
			VACME_BOOSTER_FREIGABE_SMS_SLEEP_TIME_MS);
	}

	public long getNumberOfPartionsForRuleRecalculation() {
		return getLong(
			numberOfPartitions,
			VACME_BOOSTER_RULE_ENGINE_JOB_PARTITIONS);
	}

	public long getNumberOfPartitionsForMassenverarbeitung() {
		return getLong(
			numberOfPartitions,
			VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_PARTITIONS);
	}

	public long getMassenverarbeitungQueueProcessingJobBatchSize() {
		return getLong(
			vacmeMassenverarbeitungQueueProcessingBatchSize,
			VACME_MASSENVERARBEITUNGQUEUE_PROCESSING_JOB_BATCH_SIZE);
	}

	public long getOnboardingLettersBatchSize() {
		return getLong(onboardingBatchSize, VACME_ONBOARDING_BRIEF_BATCHSIZE);
	}

	public long getCovidApiBatchSize() {
		return getLong(covidapiBatchSize, VACME_COVIDAPI_PS_BATCHSIZE);
	}

	public long getCovidApiSleeptime() {
		return covidapiSleeptime;
	}

	public boolean isZertifikatEnabled() {
		return getBoolean(false, COVID_ZERTIFIKAT_ENABLED);
	}

	public boolean isEmailKorrekturEnabled() {
		return getBoolean(false, KORREKTUR_EMAIL_TELEPHONE);
	}

	public boolean isBoosterFreigabeNotificationDisabled() {
		return getBoolean(false, VACME_BOOSTER_FREIGABE_NOTIFICATION_DISABLED);
	}

	public boolean isBoosterFreigabeNotificationTerminNDisabled() {
		return getBoolean(false, VACME_BOOSTER_FREIGABE_NOTIFICATION_TERMIN_N_DISABLED);
	}

	@NonNull
	public CovidImpfschutzcalculationConfigDTO getCovidImpfschutzcalculationConfigDTO() {
		return new CovidImpfschutzcalculationConfigDTO(
			calcCovidEnablePfizerOnlyForU30,
			calcCovidMinAgeZh,
			calcCovidMinAgeBe,
			calcCovidFreigabeOffsetImpfungMonate,
			calcCovidFreigabeOffsetImpfungTage,
			calcCovidFreigabeOffsetKrankheitMonate.orElse(null),
			calcCovidFreigabeOffsetKrankheitTage.orElse(null),
			calcCovidFreigabeCutoffDateSelbstzahler.orElse(null)
		);
	}

	@NonNull
	public AffenpockenImpfschutzcalculationConfigDTO getAffenpockenImpfschutzcalculationConfigDTO() {
		return new AffenpockenImpfschutzcalculationConfigDTO(
			calcAffenpockenMinAge,
			calcAffenpockenFreigabeOffsetImpfungWithImpfschutz,
			calcAffenpockenFreigabeoffsetLetzteImpfungWithImpfschutzUnit,
			calcAffenpockenFreigabeOffsetImpfungWithoutImpfschutz,
			calcAffenpockenFreigabeoffsetLetzteImpfungWithoutImpfschutzUnit
		);
	}

	@NonNull
	public GuiSettingsDTO getGuiSettingsDTO() {
		return new GuiSettingsDTO(
			getInt(DISTANCE_BETWEEN_IMPFUNGEN_DISIRED),
			getInt(DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_BEFORE),
			getInt(DISTANCE_BETWEEN_IMPFUNGEN_TOLERANCE_AFTER),
			getBoolean(SHOW_ONBOARDING_WELCOME_TEXT),
			getInt(MINUTES_BETWEEN_INFO_UPDATES),
			getInt(MINUTES_BETWEEN_PHONENUMBER_UPDATE),
			getBoolean(SELBSTZAHLER_FACHAPPLIKATION_ENABLED),
			getBoolean(SELBSTZAHLER_PORTAL_ENABLED)
		);
	}

	@NonNull
	public ImpfslotCreationSettingsDTO getImpfslotCreationSettingsDTO() {
		return new ImpfslotCreationSettingsDTO(
			impfslotDuration,
			impfslotStart,
			impfslotEnd
		);
	}

	@Nullable
	public String getMailAdmin() {
		return mailAdmin;
	}

	public boolean isReportingAnzahlImpfungenDisabled() {
		return reportingAnzahlImpfungenDisabled;
	}

	public boolean isReportingAnzahlZweitBoosterDisabled() {
		return reportingAnzahlZweitBoosterDisabled;
	}

	public boolean isGeocodingEnabled() {
		return getBoolean(VACME_GEOCODING_ENABLED);
	}

	@NonNull
	public Optional<String> getGeocodeApiKeyOptional() {
		return geocodeApiKeyOptional;
	}

	public int getAutomatischAbschliessenZeitDays() {
		return automatischAbschliessenZeitDays;
	}

	public int getArchivierungZeitDays() {
		return archivierungZeitDays;
	}

	public boolean isArchivierungD3Disabled() {
		return archivierungD3Disabled;
	}

	public boolean isCronUpdateNoFreieTermineProKranhkeitDisabled() {
		return getBoolean(cronUpdateNoFreieTermineProKrankheitDisabled, VACME_CRON_GLOBAL_FREI_TERMINE_DISABLED);
	}

	public int getCronUpdateNoFreieTermineProKrankheitMinTermine() {
		return cronUpdateNoFreieTermineProKrankheitMinTermine;
	}

	public boolean isCronDbValidationJobDisabled() {
		return cronDbValidationJobDisabled;
	}

	public boolean isCronUpdateNoFreieTermineProOdiDisabled() {
		return getBoolean(cronOdiNoTermineFreiDisabled, VACME_CRON_ODI_TERMINE_FREI_DISABLED);
	}

	public int getCronKontrolleAbgelaufenBatchsize() {
		return cronKontrolleAbgelaufenBatchsize;
	}

	@NonNull
	public String getStufe() {
		return stufe;
	}

	public boolean isStufeProd() {
		return "PROD".equals(getStufe());
	}

	@NonNull
	public String getFhirCustomNarrativePropertyFile() {
		return fhirCustomNarrativePropertyFile;
	}

	public int getTerminslotOffsetGroups() {
		return terminslotOffsetGroups;
	}

	public int getSlotOffsetMaxTermineToDivide() {
		return slotOffsetMaxTermineToDivide;
	}

	public boolean isSlotOffsetDeterministicWhenLowCapacity() {
		return slotOffsetDeterministicWhenLowCapacity;
	}

	public int getTerminslotOffsetMinTerminePerSlot() {
		return terminslotOffsetMinTerminePerSlot;
	}

	public boolean isTerminReservationEnabled() {
		return terminReservationEnabled;
	}

	public int getTerminReservationDauerInMinutes() {
		return terminReservationDauerInMinutes;
	}

	public boolean isTerminVergabeRandomEnabled() {
		return terminVergabeRandomEnabled;
	}

	public boolean isTerminVergabeLockEnabled() {
		return terminVergabeLockEnabled;
	}

	@NonNull
	public String getApplicationLink() {
		return applicationLink;
	}

	@NonNull
	private AbstractSmsProvider getSmsProvider() {
		if (AbstractSmsProvider.ECALL.equalsIgnoreCase(smsProvider)) {
			return new ECallSmsProvider(smsUrlECall,
				smsUsernameECall, smsPasswordECall, smsJobIdECall,
				smsCallbackECall, smsCallbackExtECall);
		}
		return new SwissphoneSmsProvider(smsUrlSwissphone,
			smsUsernameSwissphone, smsPasswordSwissphone, smsJobIdSwissphone,
			smsCallbackSwissphone, smsCallbackExtSwissphone);
	}

	@NonNull
	public SmsSettingsDTO getSmsSettingsDTO() {
		return new SmsSettingsDTO(
			smsSendingDisabled,
			smsMobileOnly,
			smsAsync,
			getSmsProvider(),
			getApplicationLink(),
			applicationLinkBoosterZh,
			smsConnectionTimeout,
			smsDetailDebugEnabled
		);
	}

	@NonNull
	public String getVmdlTenantID() {
		return vmdlTenantID;
	}

	@NonNull
	public String getVmdlUsername() {
		return vmdlUsername;
	}

	@NonNull
	public String getVmdlPassword() {
		return vmdlPassword;
	}

	public boolean isVmdlLoggingFilterDisabled() {
		return vmdlLoggingFilterDisabled;
	}

	public int getVmdlUploadChunkLimit() {
		return vmdlUploadChunkLimit;
	}

	@NonNull
	public String getVmdlReportingUnitID() {
		return vmdlReportingUnitID;
	}

	@NonNull
	public String getVmdlClientIdCovid() {
		return vmdlClientIdCovid;
	}

	@NonNull
	public String getVmdlClientIdAffenpocken() {
		return vmdlClientIdAffenpocken;
	}

	public int getDbQueryTimeoutInSeconds() {
		return dbQueryTimeoutInSeconds;
	}

	public String getKeycloakServerUrl() {
		return keycloakServerUrl;
	}

	public String getKeycloakClientId() {
		return keycloakClientId;
	}

	public String getKeycloakUsername() {
		return keycloakUsername;
	}

	public String getKeycloakPassword() {
		return keycloakPassword;
	}

	public String getKeycloakWebRealm() {
		return keycloakWebRealm;
	}

	public String getKeycloakWebClientSecret() {
		return keycloakWebClientSecret;
	}

	public String getKeycloakRegRealm() {
		return keycloakRegRealm;
	}

	public String getKeycloakRegClientSecret() {
		return keycloakRegClientSecret;
	}

	public String getKeycloakRegIssuerUrl() {
		return keycloakRegIssuerUrl;
	}

	public String getKeycloakWebIssuerUrl() {
		return keycloakWebIssuerUrl;
	}

	public String getKeyCloakConfigWeb() {
		return keyCloakConfigWeb;
	}

	public long getLoggingSlowThresholdMs() {
		return loggingSlowThresholdMs;
	}

	public String getCovidCertApiPrivateKeyPath() {
		return covidCertApiPrivateKeyPath;
	}

	public String getCovidCertApiPassword() {
		return covidCertApiPassword;
	}

	public String getCovidCertApiKeyAlias() {
		return covidCertApiKeyAlias;
	}

	public List<RegistrierungsEingang> getCovidCertPostableEingang() {
		return covidCertPostableEingang;
	}

	public int getCovidCertMinimumWaittimeMinutes() {
		return covidCertMinimumWaittimeMinutes;
	}

	public int getCovidCertMinimumWaittimeRevocationPostHours() {
		return covidCertMinimumWaittimeRevocationPostHours;
	}

	public String getZertifikatEnabledCacheTimeToLive() {
		return zertifikatEnabledCacheTimeToLive;
	}

	public Double getKontrolleGueltigkeitHours() {
		return kontrolleGueltigkeitHours;
	}

	public int getMinAlterCovidImpfung() {
		return minAlterCovidImpfung;
	}

	public Boolean getValidateSameDayKontrolle() {
		return validateSameDayKontrolle;
	}

	public Boolean getValidateSameDayImpfungen() {
		return validateSameDayImpfungen;
	}

	public Integer getOnboardingMaxTries() {
		return onboardingMaxTries;
	}

	public Integer getOnboardingCodeTTLDays() {
		return onboardingCodeTTLDays;
	}

	public Integer getOnboardingTokenTTLMinutes() {
		return onboardingTokenTTLMinutes;
	}

	public String getOnboardingHashidsAlphabet() {
		return onboardingHashidAlphabet;
	}

	public int getOnboardingHashidsMinLength() {
		return onboardingHashidMinLength;
	}

	public String getOnboardingHashidsSalt() {
		return onboardingHashidSalt;
	}

	public String getHashidAlphabet() {
		return hashidAlphabet;
	}

	public int getHashidMinLength() {
		return hashidMinLength;
	}

	public String getHashidSalt() {
		return hashidSalt;
	}

	public String getServiceReportingAnzahlZweitBoosterMail() {
		return serviceReportingAnzahlZweitBoosterMail;
	}

	public String getServiceReportingAnzahlImpfungenFsmeGalenicaRecipients() {
		return serviceReportingAnzahlImpfungenFsmeGalenicaRecipients;
	}

	public int getDocCreationMaxNumOfJobsPerType() {
		return docCreationMaxNumOfJobsPerType;
	}

	public long getMaxDocumentqueueResultAgeSeconds() {
		return maxDocumentqueueResultAgeSeconds;
	}

	public String getWellUrl() {
		return wellUrl;
	}

	public String getWellClientId() {
		return wellClientId;
	}

	public String getWellClientSecret() {
		return wellClientSecret;
	}

	public boolean isWellApiDisabled() {
		return wellApiDisabled;
	}

	public String getMaxrangeToLookForFreieTermineInMonths() {
		return maxrangeToLookForFreieTermineInMonths;
	}

	public boolean isAuthorizationDisabled() {
		return authorizationDisabled;
	}

	public boolean isKantonsreportMapImpfstoffNamesFromDB() {
		return kantonsreportMapImpfstoffNamesFromDB;
	}

	public boolean isTracingRespectChoice() {
		return tracingRespectChoice;
	}

	public boolean isMigrationEnabled() {
		return migrationEnabled;
	}

	public int getMigrationInterval() {
		return migrationInterval;
	}

	public boolean isMigrationTestDataEnabled() {
		return migrationTestDataEnabled;
	}

	public boolean isCookieSecure() {
		return cookieSecure;
	}

	public List<String> getWhitelistedMimeTypesForUpload() {
		return whitelistedMimeTypesForUpload;
	}

	public String getNoFreieTermineCacheTimeToLive() {
		return noFreieTermineCacheTimeToLive;
	}

	public String getNextfreierTerminCacheTimeToLive() {
		return nextfreierTerminCacheTimeToLive;
	}

	public FTPClientConfigDTO getFtpClientConfigDTO() {
		return new FTPClientConfigDTO(
			ftpDisabled,
			ftpServer,
			ftpPort,
			ftpUsername,
			ftpPassword
		);
	}

	public int getGeocodeApiTimeout() {
		return geocodeApiTimeout;
	}

	public String getMandant() {
		return mandant;
	}

	public String getUrlFachapplikation() {
		return urlFachapplikation;
	}

	public String getUrlBlogFachapplikation() {
		return urlBlogFachapplikation;
	}

	public String getCcMailZuwenigeZweittermine() {
		return ccMailZuwenigeZweittermine;
	}

	public int getDeactivateUnusedUseraccountsBatchsize() {
		return deactivateUnusedUseraccountsBatchsize;
	}

	public FSMEImpfschutzcalculationConfigDTO getFsmeImpfschutzcalculationConfigDTO() {
		return new FSMEImpfschutzcalculationConfigDTO(
			calcFsmeFreigabeMinAge,
			calcFsmeFreigabeOffsetKonventionellWithImpfschutz,
			calcFsmeFreigabeOffsetKonventionellWithImpfschutzUnit,
			calcFsmeFreigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutz,
			calcFsmeFreigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutzUnit,
			calcFsmeFreigabeOffsetKonventionellWithoutImpfschutz,
			calcFsmeFreigabeOffsetKonventionellWithoutImpfschutzUnit,
			calcFsmeFreigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutz,
			calcFsmeFreigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutzUnit,
			calcFsmeFreigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutz,
			calcFsmeFreigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutzUnit,
			calcFsmeFreigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutz,
			calcFsmeFreigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutzUnit,
			calcFsmeFreigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutz,
			calcFsmeFreigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutzUnit,
			calcFsmeFreigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutz,
			calcFsmeFreigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutzUnit
		);
	}

	private long getLong(long defaultValue, @NonNull ApplicationPropertyKey overridingApplicationPropertyKey) {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(overridingApplicationPropertyKey);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Long.parseLong(applicationProperty.getValue()))
			.orElse(defaultValue);
	}

	private int getInt(int defaultValue, @NonNull ApplicationPropertyKey overridingApplicationPropertyKey) {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(overridingApplicationPropertyKey);
		return byKeyOptional
			.filter(applicationProperty -> StringUtils.isNumeric(applicationProperty.getValue()))
			.map(applicationProperty -> Integer.parseInt(applicationProperty.getValue()))
			.orElse(defaultValue);
	}

	private int getInt(@NonNull ApplicationPropertyKey key) {
		ApplicationProperty applicationProperty =
			this.applicationPropertyService.getByKey(key);
		return applicationProperty.getValueAsInteger();
	}

	private boolean getBoolean(
		boolean defaultValue,
		@NonNull ApplicationPropertyKey overridingApplicationPropertyKey) {
		Optional<ApplicationProperty> byKeyOptional =
			this.applicationPropertyService.getByKeyOptional(overridingApplicationPropertyKey);
		return byKeyOptional
			.map(applicationProperty -> Boolean.parseBoolean(applicationProperty.getValue()))
			.orElse(defaultValue);
	}

	private boolean getBoolean(@NonNull ApplicationPropertyKey key) {
		ApplicationProperty applicationProperty =
			this.applicationPropertyService.getByKey(key);
		return applicationProperty.getValueAsBoolean();
	}

}
