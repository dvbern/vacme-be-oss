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

package ch.dvbern.oss.vacme.shared.errors.mappers;

import java.util.List;

import ch.dvbern.oss.vacme.shared.errors.FailureType;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AppFailureErrorResponse extends AppErrorResponse {

	@Getter
	private final String summary;
	@Getter
	private final @Nullable String detail;

	public AppFailureErrorResponse(AppFailureMessage appFailureMessage) {
		super(FailureType.FAILURE);
		summary = appFailureMessage.getMessage();
		detail = null;
	}

	public AppFailureErrorResponse(AppFailureMessage appFailureMessage, @Nullable String detail) {
		super(FailureType.FAILURE);
		summary = appFailureMessage.getMessage();
		this.detail = detail;
	}

	public AppFailureErrorResponse(AppFailureMessage appFailureMessage, List<Reference> referenceList) {
		this(appFailureMessage, buildPath(referenceList));
	}

}
