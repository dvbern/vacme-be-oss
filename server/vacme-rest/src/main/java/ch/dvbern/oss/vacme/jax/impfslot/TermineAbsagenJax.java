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
import java.util.UUID;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class TermineAbsagenJax {

	@NonNull
	@Schema(required = true)
	private UUID odiId;

	@NonNull
	@NotNull
	@Future
	private LocalDate datum;

	@NonNull
	@Schema(required = true)
	private Impffolge impffolge;

}
