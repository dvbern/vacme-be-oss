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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

import javax.ws.rs.core.Response;

import ch.dvbern.oss.vacme.entities.embeddables.FileBytes;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.shared.util.CleanFileName;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import ch.dvbern.oss.vacme.shared.util.Util;
import com.github.HonoluluHenk.httpcontentdisposition.Disposition;
import com.github.HonoluluHenk.httpcontentdisposition.HttpContentDisposition;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

public final class RestUtil {

	private RestUtil() {
	}

	static HttpContentDisposition buildContentDisposition(CleanFileName fileName, Disposition disposition) {
		return HttpContentDisposition.builder()
				.disposition(disposition)
				.filename(fileName.getFileName())
				.build();
	}

	public static Response buildFileResponse(
			Disposition disposition,
			CleanFileName fileName,
			byte[] content,
			MimeType mimeType,
			@Nullable LocalDateTime lastModified
	) {
		HttpContentDisposition contentDisposition = buildContentDisposition(fileName, disposition);
		@Nullable Date actualLastModified = Util.ifValue(lastModified, DateUtil::getDate);

		var result = Response
				.ok(content, mimeType.getMimeType())
				.lastModified(actualLastModified)
				.header(contentDisposition.headerName(), contentDisposition.headerValue())
				// note: content-length is not allowed if transfer is chunked
				// ... which is used for most downloads by Jackson
				//.header(HttpHeaders.CONTENT_LENGTH, content.length)
				.build();

		return result;
	}

	public static Response buildFileResponse(
			Disposition disposition,
			FileBytes fileBytes
	) {
		var result = buildFileResponse(
				disposition,
				fileBytes.getFileName(),
				fileBytes.getData(),
				fileBytes.getMimeType(),
				fileBytes.getMutiertAm());

		return result;
	}

	@NonNull
	public static Response createDownloadResponse(
		@NonNull String proposedFileName,
		byte[] content,
		MimeType type) {
		CleanFileName fileName = new CleanFileName(proposedFileName);
		final FileBytes downloadFile = FileBytes.of(
			fileName,
			type,
			content,
			LocalDateTime.now()
		);
		return RestUtil.buildFileResponse(Disposition.ATTACHMENT, downloadFile);
	}

	/**
	 * extract file name from multipart form data part,
	 * with the name "filename".
	 * If none is supplied, "unkown.bin" is returned
	 **/
	public static String getFileName(MultipartFormDataInput input) {
		try {
			@Nullable String filename = input.getFormDataPart("filename", String.class, null);
			return filename == null
					? "unkown.bin"
					: filename;
		} catch (IOException e) {
			throw new AppFailureException("Could not read filetype", e);
		}
	}
}
