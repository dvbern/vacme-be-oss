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

package ch.dvbern.oss.vacme.jax.impfslot;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = Visibility.ANY,
	getterVisibility = Visibility.NONE,
	setterVisibility = Visibility.NONE)
public class ImpfslotValidationJax {
	@Schema(required = true, description = "Tag der ersten Impfung als String")
	private String datum1Display;

	@NotNull
	@NonNull
	private LocalDate date1;

	@Schema(required = true, description = "Tag der zweiten Impfung als String")
	private String datum2Display;

	@NotNull
	@NonNull
	private LocalDate date2;

	@Schema(required = true, description = "Tageskapazitaet der ersten Impfung")
	private int kap1;

	@Schema(required = true, description = "Tageskapazitaet der zweiten Impfung")
	private int kap2;

}
