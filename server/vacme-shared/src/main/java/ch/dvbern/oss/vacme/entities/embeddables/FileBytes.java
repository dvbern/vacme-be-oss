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

import java.time.LocalDateTime;

import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileBytes extends FileInfo {
	private static final long serialVersionUID = -3530272490906016494L;

	FileBytes(
		CleanFileName fileName,
		long fileSize,
		MimeType mimeType,
		byte[] data,
		@Nullable LocalDateTime mutiertAm
	) {
		super(fileName, fileSize, mimeType);
		this.data = data;
		this.mutiertAm = mutiertAm;
	}

	private byte[] data;

	private @Nullable LocalDateTime mutiertAm;

	public static FileBytes of(
		CleanFileName fileName,
		MimeType mimeType,
		byte[] data,
		@Nullable LocalDateTime mutiertAm
	) {
		return new FileBytes(
			fileName,
			data.length,
			mimeType,
			data,
			mutiertAm
		);
	}
}
