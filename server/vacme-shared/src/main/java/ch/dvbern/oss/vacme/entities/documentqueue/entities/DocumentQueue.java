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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractLongEntity;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueStatus;
import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueType;
import ch.dvbern.oss.vacme.entities.documentqueue.IDocumentQueueJobFinishedBenachrichtigungService;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Queue in welche man die Regs schreibt wenn man ihren Impschutz neu berechnen will
 */
@SuppressWarnings("JpaMissingIdInspection")
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType. SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
@Table(
	indexes = {
		@Index(name = "IX_DocumentQueue_DTYPE", columnList = "DTYPE"),
		@Index(name = "IX_DocumentQueue_DTYPE_benutzer", columnList = "DTYPE,  benutzer_id, id")
	}
)
public abstract class DocumentQueue extends AbstractLongEntity<DocumentQueue> {

	private static final long serialVersionUID = -8267577170954277144L;
	public static final int MAX_TRIES = 3;


	@NotNull @NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH, updatable = false)
	private DocumentQueueType typ;

	@NotNull
	@Column(nullable = false)
	private int errorCount = 0;

	@NotNull @NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	private DocumentQueueStatus status = DocumentQueueStatus.NEW;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_LENGTH_ZERTIFIKAT_PAYLOAD, updatable = false)
	@Size(max = DBConst.DB_LENGTH_ZERTIFIKAT_PAYLOAD)
	protected String jobParameters;

	@Column(nullable = true)
	private LocalDateTime resultTimestamp;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	@Size(max = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	private String lastError;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_DocumentQueue_documentQueueResult_id"), nullable = true)
	private DocumentQueueResult documentQueueResult;

	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_DocumentQueue_benutzer"), updatable = false)
	@NotNull
	private Benutzer benutzer;


	protected DocumentQueue(@NonNull DocumentQueueType typ, @Nullable String jobParameters, @NonNull Benutzer benutzer) {
		this.typ = typ;
		this.jobParameters = jobParameters;
		this.benutzer = benutzer;
	}

	protected DocumentQueue() {
	}

	@Nullable
	protected String getJobParameters() {
		return jobParameters;
	}

	protected void setJobParameters(@Nullable String jobParameters) {
		this.jobParameters = jobParameters;
	}

	public void markSuccessful() {
		setStatus(DocumentQueueStatus.SUCCESS);
		setResultTimestamp(LocalDateTime.now());
	}

	public void markFailed(@Nullable  String error) {
		if (getErrorCount() >= MAX_TRIES) {
			setStatus(DocumentQueueStatus.FAILED);
		} else {
			setErrorCount(getErrorCount() + 1);
			setStatus(DocumentQueueStatus.FAILED_RETRY);
		}
		setLastError(error);
		setResultTimestamp(LocalDateTime.now());
	}

	@NonNull
	public Long calculateProcessingTimeMs() {
		if (status == DocumentQueueStatus.SUCCESS && getResultTimestamp() != null) {
			return ChronoUnit.MILLIS.between(this.getTimestampErstellt(), getResultTimestamp());
		}
		return 0L;
	}

	public abstract void sendFinishedDocumentQueueJobSuccessMail(
		@NonNull IDocumentQueueJobFinishedBenachrichtigungService mailService,
		@NonNull ObjectMapper mapper);

	public abstract void sendFinishedDocumentQueueJobFailureMail(
		@NonNull IDocumentQueueJobFinishedBenachrichtigungService mailService,
		@NonNull ObjectMapper mapper,
		@NonNull String errorMessage);

	public abstract String calculateFilename(@NonNull ObjectMapper mapper);
}
