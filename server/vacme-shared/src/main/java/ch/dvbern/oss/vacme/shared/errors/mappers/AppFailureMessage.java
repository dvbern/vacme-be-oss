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

import lombok.Getter;

public enum AppFailureMessage {
	INTERNAL_ERROR("Internal Server error"),
	UNRECOGNIZED_PROPERTY("Unrecognized property"),
	JSON_PARSING("JSON parsing failed"),
	JSON_MAPPING("JSON mapping failed");

	@Getter
	private final String message;

	AppFailureMessage(String message) {
		this.message = message;
	}
}
