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

package ch.dvbern.oss.vacme.entities.types;

import java.time.LocalDateTime;
import java.util.UUID;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
public class ZertifikatCreationDTO {

	@NonNull
	private String registrierungsnummer;

	@NonNull
	private UUID impfungId;

	@Nullable
	private LocalDateTime timestampZuletztAbgeschlossen;

	/**
	 * Fuer native Queries: UUID-Mapping scheint nicht zu funktionnieren.
	 */
	public ZertifikatCreationDTO(@NonNull String registrierungsnummer, @NonNull String impfungId) {
		this.registrierungsnummer = registrierungsnummer;
		this.impfungId = UUID.fromString(impfungId);
	}

	/**
	 * Fuer "normale" Queries
	 */
	@QueryProjection
	public ZertifikatCreationDTO(@NonNull String registrierungsnummer, @NonNull UUID impfungId, @Nullable LocalDateTime zuletztAbgeschlossen) {
		this.registrierungsnummer = registrierungsnummer;
		this.impfungId = impfungId;
		this.timestampZuletztAbgeschlossen = zuletztAbgeschlossen;
	}
}
