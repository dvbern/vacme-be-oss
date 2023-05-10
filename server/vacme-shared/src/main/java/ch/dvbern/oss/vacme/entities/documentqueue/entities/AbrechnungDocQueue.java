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

package ch.dvbern.oss.vacme.entities.documentqueue.entities;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueType;
import ch.dvbern.oss.vacme.entities.documentqueue.IDocumentQueueJobFinishedBenachrichtigungService;
import ch.dvbern.oss.vacme.entities.documentqueue.IDocumentQueueService;
import ch.dvbern.oss.vacme.entities.documentqueue.VonBisSpracheParamJax;
import ch.dvbern.oss.vacme.entities.registration.Sprache;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("JpaMissingIdInspection")
@Entity
@Getter
@Setter
@Slf4j
public class AbrechnungDocQueue extends DocumentQueue {

	private static final long serialVersionUID = 2974109055005584915L;

	public AbrechnungDocQueue() {
		super();
		this.setTyp(DocumentQueueType.ABRECHNUNG);
	}

	@Nullable
	protected static String mapParameterObjectToString(@Nullable VonBisSpracheParamJax vonBisSpracheParamJax, @NonNull ObjectMapper mapper) {
		if (vonBisSpracheParamJax == null) {
			return null;
		}
		try {
			return mapper.writeValueAsString(vonBisSpracheParamJax);
		} catch (JsonProcessingException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("could not convert json jobParameter object to string. " + e.getMessage());
		}
	}

	protected void setVonBisParam(@Nullable VonBisSpracheParamJax vonBisParamJax, @NonNull ObjectMapper mapper) {
		this.jobParameters = mapParameterObjectToString(vonBisParamJax, mapper);
	}

	public VonBisSpracheParamJax getVonBisSpracheParam(@NonNull ObjectMapper mapper) {
		try {
			return mapper.readValue(this.jobParameters, VonBisSpracheParamJax.class);
		} catch (JsonProcessingException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("could not convert json jobParameter string to object. " + e.getMessage());
		}
	}

	public void init(@Nullable VonBisSpracheParamJax params, @NonNull Benutzer benutzer, @NonNull ObjectMapper objectMapper) {
		Validate.notNull(objectMapper, "objectMapper muss gesetzt sein");
		Validate.notNull(benutzer, "Benutzer muss gesetzt sein");
		this.setVonBisParam(params, objectMapper);
		this.setBenutzer(benutzer);
	}

	public void addJobToQueue(@NonNull IDocumentQueueService documentQueueService) {
		documentQueueService.addJobToQueueIfAllowedForBenutzer(this);
	}

	@Override
	public void sendFinishedDocumentQueueJobSuccessMail(
		@NonNull IDocumentQueueJobFinishedBenachrichtigungService mailService,
		@NonNull ObjectMapper mapper
	) {
		VonBisSpracheParamJax vonBisParam = getVonBisSpracheParam(mapper);
		String processingTimeSeconds = new DecimalFormat("#.##").format(this.calculateProcessingTimeMs() / 1000f);
		mailService.sendFinishedDocumentQueueSuccessJobMail(vonBisParam, this, processingTimeSeconds);
	}

	@Override
	public void sendFinishedDocumentQueueJobFailureMail(
		@NonNull IDocumentQueueJobFinishedBenachrichtigungService mailService,
		@NonNull ObjectMapper mapper,
		@NonNull String errorMessage
	) {
		Sprache sprache = Sprache.DE;
		try {
			sprache = getVonBisSpracheParam(mapper).getSprache();
		} catch (Exception e) {
			LOG.warn("Could not parse Sprache from Param for DocumentQueue. Using default {}", sprache, e);
		}
		mailService.sendFinishedDocumentQueueJobFailureMail(sprache, getJobParameters(), this, errorMessage);
	}

	@Override
	public String calculateFilename(@NonNull ObjectMapper mapper) {
		VonBisSpracheParamJax vonBisSpracheParam = getVonBisSpracheParam(mapper);
		Locale locale = vonBisSpracheParam.getSprache().getLocale();

		DateTimeFormatter dd_mmm = DateTimeFormatter.ofPattern("dd_MM", locale);
		String vontext = dd_mmm.format(vonBisSpracheParam.getVon());
		String bistext = dd_mmm.format(vonBisSpracheParam.getBis());
		String vonbis = "_" + vontext + "-" + bistext;


		String filename = ServerMessageUtil.translateEnumValue(this.getTyp().getFileNameEnum(),
			locale);
		String[] split = filename.split("\\.");
		return split[0] + vonbis +  "." +  Arrays.stream(split).skip(1).collect(Collectors.joining(".")) ;
	}
}
