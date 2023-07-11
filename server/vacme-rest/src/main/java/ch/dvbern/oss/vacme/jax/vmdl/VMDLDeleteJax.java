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

package ch.dvbern.oss.vacme.jax.vmdl;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO fuer die VMDL Schnittstelle des Bundes welches im Falle einer Loeschung meldet fuer welche reportingUnit welche
 * Impfung geloescht werden soll
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VMDLDeleteJax {

	@JsonProperty("reporting_unit_id")
	@Schema(required = true, maxLength = 8)
	private String reportingUnitID;

	@JsonProperty("vacc_event_id")
	@Schema(required = true, maxLength = 256)
	private String vaccEventID;

	public VMDLDeleteJax(@NonNull Impfung impfung, @NonNull String reportingUnitID) {
		this.reportingUnitID = reportingUnitID;
		this.vaccEventID = reportingUnitID + "-" + impfung.getId().toString();
	}
}
