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
import java.util.Locale;

import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.documentqueue.IDocumentQueueJobFinishedBenachrichtigungService;
import ch.dvbern.oss.vacme.entities.documentqueue.IDocumentQueueService;
import ch.dvbern.oss.vacme.entities.documentqueue.SpracheParamJax;
import ch.dvbern.oss.vacme.entities.registration.Sprache;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public abstract class SpracheabhDocQueue extends DocumentQueue{

	private static final long serialVersionUID = -6804325187852515969L;

	protected void setSpracheParam(@Nullable SpracheParamJax param, @NonNull ObjectMapper mapper) {
		this.jobParameters = mapParameterObjectToString(param, mapper);
	}

	public SpracheParamJax getSpracheParam(@NonNull ObjectMapper mapper) {
		try {
			return mapper.readValue(this.jobParameters, SpracheParamJax.class);
		} catch (JsonProcessingException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("could not convert json jobParameter string to object. " + e.getMessage());
		}
	}

	@Nullable
	protected static String mapParameterObjectToString(@Nullable SpracheParamJax param, @NonNull ObjectMapper mapper) {
		if (param == null) {
			return null;
		}
		try {
			return mapper.writeValueAsString(param);
		} catch (JsonProcessingException e) {
			throw AppValidationMessage.ILLEGAL_STATE.create("could not convert json jobParameter object to string. " + e.getMessage());
		}
	}

	@Override
	public void sendFinishedDocumentQueueJobSuccessMail(
		@NonNull IDocumentQueueJobFinishedBenachrichtigungService mailService,
		@NonNull ObjectMapper mapper
	) {
		LOG.warn("todo send finish mail");
		Sprache sprache = Sprache.DE;
		try {
			sprache = getSpracheParam(mapper).getSprache();
		} catch (Exception e) {
			LOG.warn("Could not parse Sprache from Param for DocumentQueue. Using default " + sprache, e);
		}
		String processingTimeSeconds = new DecimalFormat("#.##").format(this.calculateProcessingTimeMs() / 1000f);
		mailService.sendFinishedDocumentQueueSuccessJobMail(sprache, this, processingTimeSeconds);
	}

	@Override
	public void sendFinishedDocumentQueueJobFailureMail(
		@NonNull IDocumentQueueJobFinishedBenachrichtigungService mailService,
		@NonNull ObjectMapper mapper,
		@NonNull String errormessage) {
		Sprache sprache = Sprache.DE;
		try {
			sprache = getSpracheParam(mapper).getSprache();
		} catch (Exception e) {
			LOG.warn("Could not parse Sprache from Param for DocumentQueue. Using default " + sprache, e);
		}
		mailService.sendFinishedDocumentQueueJobFailureMail(sprache,getJobParameters(), this,errormessage );
	}

	@Override
	public String calculateFilename(@NonNull ObjectMapper mapper) {
		Locale locale = getSpracheParam(mapper).getSprache().getLocale();
		return ServerMessageUtil.translateEnumValue(this.getTyp().getFileNameEnum(), locale);
	}

	public void init(@Nullable SpracheParamJax params, @NonNull Benutzer benutzer, @NonNull ObjectMapper objectMapper) {
		Validate.notNull(objectMapper, "objectMapper muss gesetzt sein");
		Validate.notNull(benutzer, "Benutzer muss gesetzt sein");
		this.setSpracheParam(params, objectMapper);
		this.setBenutzer(benutzer);
	}

	public void addJobToQueue(@NonNull  IDocumentQueueService documentQueueService) {
		documentQueueService.addJobToQueueIfAllowedForBenutzer(this);
	}
}
