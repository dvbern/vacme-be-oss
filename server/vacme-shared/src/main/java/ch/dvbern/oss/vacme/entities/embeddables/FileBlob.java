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

package ch.dvbern.oss.vacme.entities.embeddables;

import java.sql.Blob;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.util.BlobUtil;
import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import static ch.dvbern.oss.vacme.entities.util.BlobUtil.emptyBlob;
import static javax.persistence.FetchType.LAZY;

@Setter
@Getter
@Embeddable
@MappedSuperclass
public class FileBlob extends FileInfo {
	private static final long serialVersionUID = -661141665899629276L;

	protected FileBlob() {
		super();
	}

	private FileBlob(
		CleanFileName fileName,
		long fileSize,
		MimeType mimeType,
		Blob data
	) {
		super(fileName, fileSize, mimeType);
		this.data = data;
	}

	@Lob
	@NotNull
	@Basic(fetch = LAZY)
	@Column(nullable = false)
	@JsonIgnore
	private Blob data = emptyBlob();

	public static FileBlob of(CleanFileName fileName, MimeType mimeType, byte[] data) {
		return new FileBlob(
			fileName,
			data.length,
			mimeType,
			BlobUtil.createBlob(data)
		);
	}

}
