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

package ch.dvbern.oss.vacme.jax.stats;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueStatus;
import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueType;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocumentQueueJax implements  Serializable {

	@Schema(required = true)
	@NonNull
	private DocumentQueueType typ;

	@NotNull
	private int errorCount = 0;

	@Schema(required = true)
	@NonNull
	private DocumentQueueStatus status = DocumentQueueStatus.NEW;

	@NotNull
	private LocalDateTime timestampErstellt;

	@Nullable
	private LocalDateTime resultTimestamp;

	@Nullable
	private String lastError;

	@Nullable
	private UUID docQueueResultId;

	@Nullable
	private String docQueueResultFilename;

	public DocumentQueueJax(DocumentQueue documentQueue) {
		this(
			documentQueue.getTyp(),
			documentQueue.getErrorCount(),
			documentQueue.getStatus(),
			documentQueue.getTimestampErstellt(),
			documentQueue.getResultTimestamp(),
			documentQueue.getLastError(),
			documentQueue.getDocumentQueueResult() != null ? documentQueue.getDocumentQueueResult().getId(): null,
			documentQueue.getDocumentQueueResult() != null ? documentQueue.getDocumentQueueResult().getFileBlob().getFileName().getFileName() : null
		);
	}


	@Nullable
	public static DocumentQueueJax from(@Nullable DocumentQueue documentQueue) {
		if (documentQueue == null) {
			return null;
		}
		return new DocumentQueueJax(documentQueue);
	}
}
