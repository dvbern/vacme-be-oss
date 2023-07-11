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

package ch.dvbern.oss.vacme.service.covidcertificate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.oss.vacme.entities.covidcertificate.CovidCertErrorcode;
import ch.dvbern.oss.vacme.entities.covidcertificate.CovidCertLang;
import ch.dvbern.oss.vacme.entities.covidcertificate.CovidCertificateAdresseDto;
import ch.dvbern.oss.vacme.entities.covidcertificate.CovidCertificateCreateResponseDto;
import ch.dvbern.oss.vacme.entities.covidcertificate.CovidCertificatePersonNameDto;
import ch.dvbern.oss.vacme.entities.covidcertificate.RevocationDto;
import ch.dvbern.oss.vacme.entities.covidcertificate.VaccinationCertificateCreateDto;
import ch.dvbern.oss.vacme.entities.covidcertificate.VaccinationCertificateDataDto;
import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import ch.dvbern.oss.vacme.entities.zertifikat.ZertifizierungsToken;
import ch.dvbern.oss.vacme.i18n.MandantUtil;
import ch.dvbern.oss.vacme.service.ZertifikatService;
import ch.dvbern.oss.vacme.shared.util.InputValidationUtil;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.service.impfinformationen.ImpfinformationenService;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.errors.CovidCertApiFailureException;
import ch.dvbern.oss.vacme.shared.errors.NoTokenFailureException;
import ch.dvbern.oss.vacme.shared.errors.PlzMappingException;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.ValidationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jetbrains.annotations.NotNull;

import static ch.dvbern.oss.vacme.service.covidcertificate.CovidCertUtils.normalizeJson;

/**
 * This is the service class that manages communication with the Certification aPI
 */
@ApplicationScoped
@Slf4j
public class CovidCertApiService {

	public static final int MAX_WIDTH_CITY = 128;
	public static final int MAX_WITH_ADDR1 = 128;
	private final SignatureHeaderBean signatureHeaderBean;
	private final CovidCertSignatureService covidCertSignatureService;
	private final ZertifikatService zertifikatService;
	private final ObjectMapper jackonObjectMapper;

	@Inject
	@RestClient
	CovidCertRestApiClientService covidCertRestApiClientService;

	private @Nullable ZertifizierungsToken zertifizierungstoken;
	private ImpfinformationenService impfinformationenService;

	@Inject
	public CovidCertApiService(
		SignatureHeaderBean signatureHeaderBean,
		CovidCertSignatureService covidCertSignatureService,
		ZertifikatService zertifikatService,
		ObjectMapper jackonObjectMapper,
		ImpfinformationenService impfinformationenService
	) {
		this.signatureHeaderBean = signatureHeaderBean;
		this.covidCertSignatureService = covidCertSignatureService;
		this.zertifikatService = zertifikatService;
		this.jackonObjectMapper = jackonObjectMapper;
		this.impfinformationenService = impfinformationenService;
	}

	public ZertifizierungsToken readCurrentOtp() {

		if (zertifizierungstoken == null || zertifizierungstoken.getGueltigkeit().isBefore(LocalDateTime.now())) {
			zertifizierungstoken =
				this.zertifikatService.getZertifizierungstoken().orElseThrow(() -> {
					LOG.warn("VACME-ZERTIFIKAT there is currenty no active token to use for certification API");
					return new NoTokenFailureException("Could not load"
						+ " an active OTP Token to transmit Certinfo");
				});
		}
		return zertifizierungstoken;

	}

	@NonNull
	public Zertifikat createVaccinationCert(
		@NonNull ImpfinformationDto infos,
		@NonNull Impfung impfung,
		@NonNull CovidCertBatchType batchType
	) {
		VaccinationCertificateCreateDto vaccCertCreateDto = mapRegForCovidCertDto(infos, impfung, batchType);

		Zertifikat zertifikat = new Zertifikat(infos.getRegistrierung());
		// Ab Booster-Impfung koennen mehrere Zertifikate gleichzeitig valid sein. Wir muessen beim eventuellen
		// Loeschen einer Impfung wissen, welches Zertifikat dazugehoert und revoziert werden muss

		zertifikat.setImpfung(impfung);
		// read current otp and set it into the payload
		vaccCertCreateDto.setOtp(readCurrentOtp().getToken());
		calculateSignatureForHeader(vaccCertCreateDto, zertifikat);

		try {
			CovidCertificateCreateResponseDto vaccinationCert = this.covidCertRestApiClientService.createVaccinationCert(vaccCertCreateDto);
			LOG.info("VACME-ZERTIFIKAT: created Certificate with uvci {}", vaccinationCert.getUvci());
			zertifikat.setCovidCertificateCreateResponseDto(vaccinationCert);
			return zertifikat;

		} catch (CovidCertApiFailureException e) {
			if (e.getErrorFromApi() != null) {
				LOG.error("VACME-ZERTIFIKAT: Covic-Cert Error Status: '{}', message: '{}'", e.getErrorFromApi().getErrorCode(),
					e.getErrorFromApi().getErrorMessage());
			} else {
				LOG.error("VACME-ZERTIFIKAT: Unknown Error from CovidCert Api ", e);
			}
			LOG.error("VACME-ZERTIFIKAT: there was an error when trying to create a certificate. Detailinformation: \n'{}'",
				printCertDetail(vaccCertCreateDto));
			this.zertifizierungstoken = null; // clear out token because the problem might be the token
			throw new AppFailureException("Got error-response from covid-cert api: " + e.getMessage(), e);
		}
	}

	public void revokeVaccinationCert(
		@NonNull Zertifikat zertifikat
	) {
		RevocationDto revocationDto = new RevocationDto();
		revocationDto.setUvci(zertifikat.getUvci());
		revocationDto.setOtp(readCurrentOtp().getToken());

		try {
			calculateSignatureForHeader(revocationDto, zertifikat);
			this.covidCertRestApiClientService.revokeVaccinationCert(revocationDto);
			LOG.info("VACME-ZERTIFIKAT: Certificate with uvci {} for Registration {} has been revoked",
				revocationDto.getUvci(),
				zertifikat.getRegistrierung().getRegistrierungsnummer());
		} catch (CovidCertApiFailureException e) {
			if (e.getErrorFromApi() != null) {
				final Integer errorCode = e.getErrorFromApi().getErrorCode();
				LOG.error("VACME-ZERTIFIKAT: Covic-Cert Revocation Error Status: '{}', message: '{}'",
					errorCode,
					e.getErrorFromApi().getErrorMessage());
				if (CovidCertErrorcode.CC_ERROR_DUPLICATE_UVCI.getErrorCode() == errorCode) {
					// Zertifikat scheint bereits revoziert zu sein. Wir setzen es auf revoziert (bzw. werfen keine Exception)
					LOG.info("VACME-ZERTIFIKAT: Received CC_ERROR_DUPLICATE_UVCI for Revocation uvci {} and Registration {}. Setting to revoked.",
						revocationDto.getUvci(), zertifikat.getRegistrierung().getRegistrierungsnummer());
					return;
				}
			} else {
				LOG.error("VACME-ZERTIFIKAT: Unknown Error from CovidCert Revocation Api ", e);
			}
			LOG.error("VACME-ZERTIFIKAT: there was an error when trying to revoke a certificate. Detailinformation: \n'{}'",
				printCertDetail(revocationDto));
			this.zertifizierungstoken = null; // clear out token because the problem might be the token
			throw new AppFailureException("Got error-response from covid-cert api (revocation): " + e.getMessage(), e);
		}
	}

	private String printCertDetail(@NonNull Object vaccCertCreateDto) {
		try {
			return jackonObjectMapper.writeValueAsString(vaccCertCreateDto);
		} catch (Exception e) {
			LOG.error("VACME-ZERTIFIKAT: Could not print content for debugging ", e);
			return "ERROR-COULD-NOT-LOG";
		}
	}

	private void calculateSignatureForHeader(@NonNull Object dto, @NonNull Zertifikat zertifikat) {
		try {
			String jsonString = jackonObjectMapper.writeValueAsString(dto);
			jsonString = normalizeJson(jsonString);
			LOG.debug("DTO to create Signature for \n{}", dto);
			String signatureString = this.covidCertSignatureService.sign(jsonString);
			LOG.debug("Calculated Signature \n{}", signatureString);
			this.signatureHeaderBean.setSignature(signatureString);
			zertifikat.setPayload(jsonString);
			zertifikat.setSignature(signatureString);
		} catch (JsonProcessingException e) {
			throw new AppFailureException("Could not create JSON String form Covid Cert Vaccination DTO", e);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new AppFailureException("Could not create Signature for CovidCert API", e);
		}
	}

	@NonNull
	VaccinationCertificateCreateDto mapRegForCovidCertDto(
		@NonNull ImpfinformationDto infos,
		@NonNull Impfung impfung,
		@NonNull CovidCertBatchType batchType
	) {
		Registrierung registrierung = infos.getRegistrierung();
		Validate.isTrue(infos.getImpfdossier().abgeschlossenMitVollstaendigemImpfschutz(),
			"Vollstaendiger Impfschutz muss erreicht sein " + registrierung.getRegistrierungsnummer());
		Objects.requireNonNull(impfung, "Eine VacMe-Impfung muss existieren " + registrierung.getRegistrierungsnummer());

		VaccinationCertificateCreateDto dto = new VaccinationCertificateCreateDto();
		CovidCertificatePersonNameDto nameDto = mapName(registrierung);
		dto.setName(nameDto);
		dto.setLanguage(CovidCertLang.fromSprache(registrierung.getSprache()).getLangCode());
		dto.setDateOfBirth(registrierung.getGeburtsdatum());

		List<VaccinationCertificateDataDto> impfungen = new ArrayList<>();

		var zahlVorUndNachSchraegstrich = CovidCertUtils.calculateZahlVorUndNachSchraegstrich(
			infos,
			registrierung,
			impfung);

		VaccinationCertificateDataDto impfungDto = mapImpfungForCovidCertDTO(
			impfung,
			zahlVorUndNachSchraegstrich.getLeft(),
			zahlVorUndNachSchraegstrich.getRight());
		impfungen.add(impfungDto);
		dto.setVaccinationInfo(impfungen);

		if (batchType == CovidCertBatchType.POST) {
			try {
				CovidCertificateAdresseDto adresseDto = mapPostadresseForCovidCertDTO(registrierung.getAdresse());
				dto.setAddress(adresseDto);
			} catch (PlzMappingException ex) {
				LOG.info("VACME-ZERTIFIKAT: Could not map '{}' as a valid siwss-plz for registrierung {}. Letter will not be sent",
					registrierung.getAdresse().getPlz(),
					registrierung.getRegistrierungsnummer());
				dto.setAddress(null);
			}
		}
		return dto;
	}

	@NonNull
	private CovidCertificateAdresseDto mapPostadresseForCovidCertDTO(@NonNull Adresse adresse) {

		CovidCertificateAdresseDto adresseDto = new CovidCertificateAdresseDto();

		adresseDto.setCity(StringUtils.truncate(adresse.getOrt(), MAX_WIDTH_CITY));
		adresseDto.setStreetAndNr(StringUtils.truncate(adresse.getAdresse1(), MAX_WITH_ADDR1));
		// note adresse2 is not used
		adresseDto.setCantonCodeSender(MandantUtil.getMandant().name());
		String normalizedPlz = ValidationUtil.validateAndNormalizePlz(adresse.getPlz());
		adresseDto.setZipCode(normalizedPlz);
		return adresseDto;
	}

	@NotNull
	private CovidCertificatePersonNameDto mapName(@NotNull Registrierung reg) {
		CovidCertificatePersonNameDto nameDto = new CovidCertificatePersonNameDto();
		String mappedVorname = reg.getVorname();
		if (reg.getVorname().length() > Constants.MAX_NAME_LENGTH_COVIDCERT) {
			LOG.warn("VACME-ZERTIFIKAT-KUERZUNG Vorname musste abgekuerzt werden fuer {}", reg.getRegistrierungsnummer());
			mappedVorname = StringUtils.abbreviate(reg.getVorname(), Constants.MAX_NAME_LENGTH_COVIDCERT);
		}
		nameDto.setGivenName(InputValidationUtil.clean(mappedVorname));

		String mappedName = reg.getName();
		if (reg.getName().length() > Constants.MAX_NAME_LENGTH_COVIDCERT) {
			LOG.warn("VACME-ZERTIFIKAT-KUERZUNG Familienname musste abgekuerzt werden fuer {}", reg.getRegistrierungsnummer());
			mappedName = StringUtils.abbreviate(reg.getName(), Constants.MAX_NAME_LENGTH_COVIDCERT);
		}
		nameDto.setFamilyName(InputValidationUtil.clean(mappedName));
		return nameDto;
	}

	private VaccinationCertificateDataDto mapImpfungForCovidCertDTO(
		@NotNull Impfung impfung,
		int zahlVorSchraegstrich,
		int zahlNachSchraegstrich
	) {
		VaccinationCertificateDataDto dataDto = new VaccinationCertificateDataDto();
		String covidCertProdCode = impfung.getImpfstoff().getCovidCertProdCode();
		if (StringUtils.isBlank(covidCertProdCode) || covidCertProdCode.equals("xxx")) {
			throw new AppFailureException("Covidcert Mapping for Impfstoff '" + impfung.getImpfstoff() + "' not yet implemented!");
		}
		dataDto.setMedicinalProductCode(covidCertProdCode);
		dataDto.setCountryOfVaccination("CH");
		dataDto.setNumberOfDoses(zahlVorSchraegstrich);
		dataDto.setTotalNumberOfDoses(zahlNachSchraegstrich);
		dataDto.setVaccinationDate(impfung.getTimestampImpfung().toLocalDate());
		return dataDto;
	}
}
