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

package ch.dvbern.oss.vacme.util;

import java.util.Locale;

import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.enums.FileNameEnum;
import ch.dvbern.oss.vacme.shared.util.MimeType;

public class ZertifikatDownloadUtil {

	private ZertifikatDownloadUtil() {
		// util, do not instantiate
	}

	public static Response zertifikatBlobToDownloadResponse( byte[] zertifikatContent) {

		return RestUtil.createDownloadResponse(
			ServerMessageUtil.translateEnumValue(FileNameEnum.COVID_IMPFZERTIFIKAT, Locale.GERMAN),
			zertifikatContent, MimeType.APPLICATION_PDF);
	}
}
