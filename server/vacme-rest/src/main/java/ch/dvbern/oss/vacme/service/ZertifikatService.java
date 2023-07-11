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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.entities.base.ApplicationProperty;
import ch.dvbern.oss.vacme.entities.base.ApplicationPropertyKey;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.base.ZertifikatInfo;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.ZertifikatCreationDTO;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifikatFile;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifikatQueue;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifizierungsToken;
import ch.dvbern.oss.vacme.jax.registration.ImpfstoffJax;
import ch.dvbern.oss.vacme.jax.registration.ZertifikatJax;
import ch.dvbern.oss.vacme.jax.registration.ZertifizierungsTokenJax;
import ch.dvbern.oss.vacme.repo.ApplicationPropertyRepo;
import ch.dvbern.oss.vacme.repo.RegistrierungRepo;
import ch.dvbern.oss.vacme.repo.ZertifikatRepo;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertApiService;
import ch.dvbern.oss.vacme.service.covidcertificate.CovidCertBatchType;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.util.DeservesZertifikatValidator;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.util.ImpfinformationenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Collections.reverseOrder;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ZertifikatService {

	private final ZertifikatRepo zertifikatRepo;
	private final CovidCertApiService covidCertApiService;
	private final ApplicationPropertyRepo applicationPropertyRepo;
	private final RegistrierungRepo registrierungRepo;
	private final ImpfinformationenService impfinformationenService;
	private final ObjectMapper mapper;
	private final ImpfstoffService impfstoffService;
	private final VacmeSettingsService vacmeSettingsService;

	@NonNull
	private Zertifikat createZertifikat(@NonNull Zertifikat zertifikat) {
		zertifikatRepo.create(zertifikat);
		return zertifikat;
	}

	private void revokeZertifikat(@NonNull Zertifikat zertifikat) {
		zertifikat.setRevoked(true);
		zertifikatRepo.update(zertifikat);
	}

	@NonNull
	public Optional<Zertifikat> getNewestZertifikatRegardlessOfRevocation(@NonNull Registrierung registrierung) {
		return zertifikatRepo.getNewestZertifikatRegardlessOfRevocation(registrierung);
	}

	@NonNull
	public Optional<Zertifikat> getNewestNonRevokedZertifikat(@NonNull Registrierung registrierung) {
		return zertifikatRepo.getNewestNonRevokedZertifikat(registrierung);
	}

	@NonNull
	public Optional<Zertifikat> getBestMatchingZertifikat(@NonNull Registrierung registrierung) {

		// Falls vorhanden, geben wir ein gueltiges zurueck:
		final Optional<Zertifikat> newestNonRevokedZertifikat = getNewestNonRevokedZertifikat(registrierung);
		if (newestNonRevokedZertifikat.isPresent()) {
			return newestNonRevokedZertifikat;
		}
		// Ansonsten geben wir halt das juengste revozierte zurueck
		return getNewestZertifikatRegardlessOfRevocation(registrierung);
	}

	@NonNull
	public List<Zertifikat> getAllZertifikateRegardlessOfRevocation(@NonNull Registrierung registrierung) {
		return zertifikatRepo.getAllZertifikateRegardlessOfRevocation(registrierung);
	}

	@NonNull
	public ZertifikatQueue getZertifikatQueueItem(@NonNull ID<ZertifikatQueue> id) {
		return zertifikatRepo.getZertifikaQueueById(id).orElseThrow(
			() -> AppValidationMessage.UNKNOWN_ZERTIFIKAT.create(id));
	}

	@NonNull
	public Zertifikat getZertifikatById(@NonNull ID<Zertifikat> id) {
		return zertifikatRepo.getZertifikatById(id).orElseThrow(
			() -> AppValidationMessage.UNKNOWN_ZERTIFIKAT.create(id));
	}

	@NonNull
	public byte[] getZertifikatPdf(@NonNull Zertifikat zertifikat) {
		final Optional<ZertifikatFile> zertifikatPdf = zertifikatRepo.getZertifikatPdf(zertifikat);
		if (zertifikatPdf.isPresent()) {
			return zertifikatPdf.get().getContent();
		}
		throw AppValidationMessage.NO_ZERTIFIKAT_PDF.create(zertifikat.getRegistrierung().getRegistrierungsnummer());
	}

	public boolean hasCovidZertifikat(@NonNull String registrierungsNummer) {
		return zertifikatRepo.findZertifikatUCVI(registrierungsNummer).isPresent();
	}

	public byte[] getZertifikatQrCode(@NonNull Zertifikat zertifikat) {
		final Optional<ZertifikatFile> zertifikatQrCode = zertifikatRepo.getZertifikatQrCode(zertifikat);
		if (zertifikatQrCode.isPresent()) {
			return zertifikatQrCode.get().getContent();
		}
		throw AppValidationMessage.NO_ZERTIFIKAT_QRCODE.create(zertifikat.getRegistrierung().getRegistrierungsnummer());
	}

	@NonNull
	public Optional<ZertifizierungsToken> getZertifizierungstoken() {
		return zertifikatRepo.getZertifizierungstoken();
	}

	/**
	 * speichert ein neues otp token in der Datenbank
	 */
	public void createToken(@NonNull ZertifizierungsTokenJax tokenJax) {
		zertifikatRepo.createToken(tokenJax.toEntity());
		LOG.info("VACME-ZERTIFIKAT: neues OTP Token wurde hochgeladen");
	}

	public boolean hasValidToken() {
		return zertifikatRepo.hasValidToken();
	}

	@Transactional(TxType.REQUIRES_NEW)
	@NonNull
	public Zertifikat validateAndTriggerCertificateCreation(
		@NonNull String registrierungsNummer,
		@NonNull ID<Impfung> impfungId,
		@NonNull CovidCertBatchType batchType
	) {
		ImpfinformationDto infos = impfinformationenService.getImpfinformationen(
			registrierungsNummer,
			KrankheitIdentifier.COVID);
		final Impfung impfung = impfinformationenService.getImpfungById(impfungId);

		// Wir haben jetzt (unabhaenige) Regs und Impfungen gelesen. Ueberpruefen, dass dies zusammengehoeren!
		final List<Impfung> orderedImpftermine = ImpfinformationenUtil.getImpfungenOrderedByImpffolgeNr(infos);
		if (!orderedImpftermine.contains(impfung)) {
			// Die uebergebene ImpfungID gehoert nicht zur uebergebenen Registrierungsnummer!!!
			final String errorMessage = String.format(
				"VACME-ZERTIFIKAT: ImpfungId %s passt nicht zu Registrierungsnummer %s",
				impfungId,
				registrierungsNummer);
			LOG.error(errorMessage);
			throw AppValidationMessage.ILLEGAL_STATE.create(errorMessage);
		}

		// validate
		DeservesZertifikatValidator.deservesZertifikatOrThrowException(infos, impfung);
		// create Zertifikat
		final Zertifikat zertifikat = covidCertApiService.createVaccinationCert(infos, impfung, batchType);
		// Zertifikat in Vacme speichern
		createZertifikat(zertifikat);
		impfung.setGenerateZertifikat(false);
		return zertifikat;
	}

	@NonNull
	public List<ZertifikatCreationDTO> findImpfungenForZertifikatsGeneration(@NonNull CovidCertBatchType batchType, long covidapiBatchSize) {
		if (batchType == CovidCertBatchType.POST) {
			// Beim Postversand kommen prinzipiell alle Eingangsarten ausser ONLINE in Frage. Je nach Mandant sollen jedoch die
			// migrierten Daten ueber deren ExternalId aufgeschluesselt und einzeln verarbeitet werden
			// Sobald im ApplicationPropertyKey.VACME_MIGRATION_ZERTIFIKAT_REGEX etwas steht, werden nur diejenigen migrierten
			// Daten verarbeitet, deren externalID dieser Regex entspricht.
			// Wenn jedoch im ApplicationPropertyKey.VACME_MIGRATION_ZERTIFIKAT_REGEX nichts steht, werden die Migrierten Daten
			// normal zusammen mit den anderen Eingangstypen fuer Postversand abgearbeitet.

			String migrationRegex = this.applicationPropertyRepo.getByKey(ApplicationPropertyKey.VACME_MIGRATION_ZERTIFIKAT_REGEX)
				.map(ApplicationProperty::getValue)
				.orElse(null);
			List<RegistrierungsEingang> konfigurierteEingangstypenFuerPost = Lists.newArrayList(retrievePostableEingang());
			boolean doHandleMigratedDataSeparatly = false;
			if (migrationRegex != null) {
				// Es ist eine Regex fuer migrierte Daten definiert: Wir muessen die migrierten Daten aus der normalen Verarbeitung entfernen
				doHandleMigratedDataSeparatly = konfigurierteEingangstypenFuerPost.removeIf(eingang -> eingang == RegistrierungsEingang.DATA_MIGRATION);
			}
			// Wir lesen die zu verarbeitenden Regs (d.h. alle, falls Migrierte nicht separat, ansonsten alle ausser den Migrierten)
			List<ZertifikatCreationDTO> zertifikatCreationDTOs = this.zertifikatRepo.findImpfungenForZertifikatsGeneration(konfigurierteEingangstypenFuerPost,
				covidapiBatchSize);
			if (!doHandleMigratedDataSeparatly) {
				// Falls die migrierten nicht separat abgehandelt werden, sind wir hier fertig
				return zertifikatCreationDTOs;
			}
			// Pruefen, ob wir noch Kapazitaet fuer migrierte Daten haben
			long availableMigrationBatchSize = covidapiBatchSize - zertifikatCreationDTOs.size();
			LOG.debug("VACME-ZERTIFIKAT: (POST) Space for DATA_MIGRATION reggs transmission {}", availableMigrationBatchSize);
			if (availableMigrationBatchSize == 0) {
				return zertifikatCreationDTOs;
			}
			// Jetzt werden noch die Migrierten speziell abgehandelt. Es werden zuerst ALLE in Frage kommenden gelesen, danach wird deren
			// ExternalID mit der konfiguerieten Regex verglichen. Erst dann kommt das Limit zum Zug
			@NonNull List<ZertifikatCreationDTO> zertifikatCreationDTOS =
				this.zertifikatRepo.findAllMigrationImpfungenForZertifikatsGenerationPostRegexbased(migrationRegex, availableMigrationBatchSize);
			zertifikatCreationDTOs.addAll(zertifikatCreationDTOS);

			if (LOG.isDebugEnabled()) {
				LOG.debug("VACME-ZERTIFIKAT: (POST) Found {} matches for regex {}", zertifikatCreationDTOs.size(), migrationRegex);
			}
			return zertifikatCreationDTOs;
		}

		return this.zertifikatRepo.findImpfungenForZertifikatsGeneration(List.of(RegistrierungsEingang.ONLINE_REGISTRATION), covidapiBatchSize);
	}

	@NonNull
	public List<Zertifikat> getAllNonRevokedZertifikate(@NonNull String registrierungsnummer) {
		final Optional<Registrierung> optional = registrierungRepo.getByRegistrierungnummer(registrierungsnummer);
		if (optional.isPresent()) {
			return this.zertifikatRepo.getAllNonRevokedZertifikate(optional.get());
		}
		return Collections.emptyList();
	}

	public void clearExpiredCovidCertTokens() {
		final List<ZertifizierungsToken> tokens = zertifikatRepo.getAbgelaufeneZertifizierungstokens();
		LOG.info("VACME-INFO: Deleting {} Token that are no longer valid", tokens.size());
		for (ZertifizierungsToken token : tokens) {
			zertifikatRepo.deleteToken(token.toId());
		}
	}

	public void clearAllCovidCertTokens() {
		final List<ZertifizierungsToken> tokens = zertifikatRepo.getAllZertifizierungstokens();
		LOG.info("VACME-INFO: Deleting all {} Tokens ", tokens.size());
		for (ZertifizierungsToken token : tokens) {
			zertifikatRepo.deleteToken(token.toId());
		}
	}

	private List<RegistrierungsEingang> retrievePostableEingang() {
		return vacmeSettingsService.getCovidCertPostableEingang();
	}

	@NonNull
	public List<UUID> findZertifikateForZertifikatsRevocation(@NonNull CovidCertBatchType batchType, long covidapiBatchSize) {
		if (batchType.equals(CovidCertBatchType.REVOCATION_POST)) {
			return this.zertifikatRepo.findZertifikateForZertifikatsRevocationPost(covidapiBatchSize);
		}
		return this.zertifikatRepo.findZertifikateForZertifikatsRevocationOnline(covidapiBatchSize);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public boolean triggerCertificateRevocation(@NonNull ID<Zertifikat> zertifikatId) {
		final Optional<Zertifikat> zertifikatOptional = zertifikatRepo.getZertifikatById(zertifikatId);
		if (zertifikatOptional.isPresent()) {
			Zertifikat zertifikatToRevoke = zertifikatOptional.get();
			if (zertifikatToRevoke.getRevoked()) {
				LOG.info("VACME-ZERTIFIKAT: Zertifikat {} for Registration {} is already revoked",
					zertifikatToRevoke.getUvci(),
					zertifikatToRevoke.getRegistrierung().getRegistrierungsnummer());
				return true;
			}
			LOG.info("VACME-ZERTIFIKAT: Revoking zertifikat {} for Registration {}",
				zertifikatToRevoke.getUvci(),
				zertifikatToRevoke.getRegistrierung().getRegistrierungsnummer());

			covidCertApiService.revokeVaccinationCert(zertifikatToRevoke);
			// Zertifikat auch in VacMe als revoked markieren
			revokeZertifikat(zertifikatToRevoke);
			return true;
		}
		return false;
	}

	public void queueForRevocation(@NonNull Registrierung registrierung) {
		final List<Zertifikat> zertifikateToRevoke = getAllNonRevokedZertifikate(registrierung.getRegistrierungsnummer());
		for (Zertifikat zertifikat : zertifikateToRevoke) {
			queueForRevocation(zertifikat);
		}
	}

	public void queueForRevocation(@NonNull Impfung impfung) {
		final List<Zertifikat> zertifikateToRevoke = findNonRevokedZertifikatForImpfung(impfung);
		for (Zertifikat zertifikat : zertifikateToRevoke) {
			queueForRevocation(zertifikat);
		}
	}

	public void queueForRevocationPrioritaerUndOhneWaittime(@NonNull Zertifikat zertifikat) {
		ZertifikatQueue queue = ZertifikatQueue.forRevocation(zertifikat, true);
		zertifikatRepo.createZertifikatQueue(queue);
	}

	public void queueForRevocation(@NonNull Zertifikat zertifikat) {
		ZertifikatQueue queue = ZertifikatQueue.forRevocation(zertifikat, false);
		zertifikatRepo.createZertifikatQueue(queue);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void markForRegenerationNewTransaction(@NonNull ID<Impfung> impfungId) {
		final Impfung loaded = impfinformationenService.getImpfungById(impfungId);
		loaded.setGenerateZertifikat(true);
	}

	public void updateZertifikatQueue(@NonNull ZertifikatQueue queueItem) {
		zertifikatRepo.updateZertifikatQueue(queueItem);
	}

	@NonNull
	public List<Zertifikat> findNonRevokedZertifikatForImpfung(@NonNull Impfung impfung) {
		return zertifikatRepo.findNonRevokedZertifikatForImpfung(impfung);
	}

	@NonNull
	public List<Zertifikat> findZertifikatForImpfung(@NonNull Impfung impfung) {
		return zertifikatRepo.findZertifikatForImpfung(impfung);
	}

	@NonNull
	public Zertifikat unlinkFromImpfung(@NonNull Zertifikat zertifikat) {
		zertifikat.setImpfung(null);
		return zertifikatRepo.update(zertifikat);
	}

	@NonNull
	public ZertifikatJax getZertifikatJaxFrom(Zertifikat zertifikat) {

		ZertifikatJax zertifikatJax = new ZertifikatJax(zertifikat);

		// payload parsen:
		try {
			JsonNode jsonNode = mapper.readTree(zertifikat.getPayload()); // VaccinationCertificateCreateDto
			final JsonNode vaccinationInfo = jsonNode.get("vaccinationInfo"); //List<VaccinationCertificateDataDto> vaccinationInfo
			if (vaccinationInfo != null) {
				final JsonNode vaccinationInfo1 = vaccinationInfo.get(0);
				final JsonNode vaccinationDate = vaccinationInfo1.get("vaccinationDate");
				if (vaccinationDate != null) {
					zertifikatJax.setVaccinationDate(LocalDate.parse(vaccinationDate.asText()));
				}
				final JsonNode totalNumberOfDoses = vaccinationInfo1.get("totalNumberOfDoses");
				if (totalNumberOfDoses != null) {
					zertifikatJax.setTotalNumberOfDoses(totalNumberOfDoses.asInt());
				}
				final JsonNode numberOfDoses = vaccinationInfo1.get("numberOfDoses");
				if (numberOfDoses != null) {
					zertifikatJax.setNumberOfDoses(numberOfDoses.asInt());
				}
				final JsonNode medicinalProductCode = vaccinationInfo1.get("medicinalProductCode");
				if (zertifikat.getImpfung() != null) {
					zertifikatJax.setImpfstoffJax(ImpfstoffJax.from(zertifikat.getImpfung().getImpfstoff()));
				}else if (medicinalProductCode != null) {
					Impfstoff impfstoff = impfstoffService.findByCovidCertProdCode(medicinalProductCode.asText());
					zertifikatJax.setImpfstoffJax(ImpfstoffJax.from(impfstoff));
				}
			}
		} catch (JsonProcessingException e) {
			// egal, wir lesen, was wir lesen koennen.
		}

		return zertifikatJax;
	}

	@NonNull
	public List<ZertifikatJax> mapToZertifikatJax(@NonNull List<Zertifikat> zertifikatList) {
		return zertifikatList.stream()
			.map(this::getZertifikatJaxFrom)
			.sorted(
				comparatorSortingByHighestDoseNr()
			)
			.collect(Collectors.toList());
	}


	@NonNull
	public List<ZertifikatInfo> mapToZertifikatInfo(@NonNull List<Zertifikat> zertifikatList) {
		return new ArrayList<>(mapToZertifikatJax(zertifikatList));
	}

	Comparator<ZertifikatJax> comparatorSortingByHighestDoseNr() {
		return Comparator.comparing(
			ZertifikatJax::getNumberOfDoses, reverseOrder())
			.thenComparing(ZertifikatJax::getVaccinationDate, reverseOrder())
			.thenComparing(ZertifikatJax::getTimestampZertifikatErstellt, reverseOrder());
	}
}
