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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.types.CleanFileNameConverter;
import ch.dvbern.oss.vacme.entities.types.MimeTypeConverter;
import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import com.google.common.base.MoreObjects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@Embeddable
public abstract class FileInfo implements Serializable {
	private static final long serialVersionUID = -1805105243056027213L;

	@Valid
	@Column(nullable = false)
	@Convert(converter = CleanFileNameConverter.class)
	private CleanFileName fileName = CleanFileName.parse("filename-not-set.bin");

	private long fileSize = 0;

	@NotNull
	@Column(nullable = false)
	@Convert(converter = MimeTypeConverter.class)
	@Valid
	private MimeType mimeType = MimeType.APPLICATION_OCTET_STREAM;

	public boolean isPresent() {
		return fileSize > 0;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(fileName)
				.addValue(mimeType)
				.addValue(fileSize)
				.toString();
	}

}
