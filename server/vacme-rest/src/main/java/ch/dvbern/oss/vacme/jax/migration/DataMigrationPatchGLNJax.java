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

package ch.dvbern.oss.vacme.jax.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * DTO fuer die Migrationssschnittstelle welches eine Liste von Registrierungs DTOs beinhaltet wo das ODI geaendert werden kann
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DataMigrationPatchGLNJax {

	@JsonProperty
	@Schema(required = true, maxItems = 200, description = "Request zum Patch der ODIs von migrierten Impfdaten. Diese kann max. 200 Datensaetze beinhalten.")
	@Size(max = 200)
	@Valid
	private List<RegistrierungGLNPatchMigrationJax> registrierungen;
}
