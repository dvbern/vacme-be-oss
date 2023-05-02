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

import java.util.UUID;

public final class DBConst {

	public static final byte[] EMPTY_BYTES = {};

	public static final int DB_DEFAULT_MAX_LENGTH = 255;
	public static final int DB_VMDL_SCHNITTSTELLE_LENGTH = 32;
	public static final int DB_UUID_LENGTH = 36;
	public static final int DB_ENUM_LENGTH = 50;
	public static final int DB_BEMERKUNGEN_MAX_LENGTH = 2000;
	public static final int DB_TEXT_HTML_MAX_LENGTH = 2000;
	public static final int DB_ZERTIFIZIERUNGS_TOKEN_MAX_LENGTH = 2000;
	public static final int DB_LENGTH_ZERTIFIKAT_PAYLOAD = 4096;
	public static final int DB_HEX_FARBE_LENGTH = 8;
	public static final int DB_PHONE_LENGTH = 30;

	public static final UUID SYSTEM_ADMIN_ID = UUID.fromString("f7c43312-7245-429e-96b9-a615b989522a");

	private DBConst() {
	}


}
