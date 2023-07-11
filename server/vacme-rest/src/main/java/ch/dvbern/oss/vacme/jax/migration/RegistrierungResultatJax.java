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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class RegistrierungResultatJax {

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, description = "ExternalID aus dem Client System. Als Referenz aus der Request zurückgegeben")
	private String externalId;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, description = "Registrierungsnummer im Vacme")
	private String registrierungsnummer;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, description = "Auflistung der Verarbeitung der Daten")
	private StatusMigrationJax status;

	@Nullable
	@JsonProperty
	@Schema(description = "Meldung mit Hinweise zur Grund des Fehlers")
	private String statusMeldung;
}
