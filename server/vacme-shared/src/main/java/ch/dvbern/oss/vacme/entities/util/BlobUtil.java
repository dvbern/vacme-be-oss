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

package ch.dvbern.oss.vacme.entities.util;

import java.sql.Blob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;

import static ch.dvbern.oss.vacme.entities.util.DBConst.EMPTY_BYTES;
import static java.util.Objects.requireNonNull;

public final class BlobUtil {

	private BlobUtil() {
		// utility class
	}

	public static SerialBlob createBlob(byte[] bytes) {
		try {
			return new SerialBlob(requireNonNull(bytes));
		} catch (SQLException e) {
			throw new IllegalArgumentException("Could not serialize", e);
		}
	}

	public static SerialBlob emptyBlob() {
		return createBlob(EMPTY_BYTES);
	}

	public static byte[] getBlobBytes(Blob blob) {
		try {
			int length = Math.toIntExact(blob.length());
			byte[] bytes = blob.getBytes(1, length);
			return bytes;
		} catch (SQLException e) {
			throw new IllegalStateException("Illegal blob operation", e);
		}
	}
}
