/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.jax;

import java.util.UUID;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Result of PersonalienSuche
 */
@Getter
@Setter
@NoArgsConstructor
public class PersonalienSucheJax {

	@NonNull
	private UUID impfdossierId;

	@NonNull
	private String name;

	@NonNull
	private String vorname;


	@QueryProjection
	public PersonalienSucheJax(
		@NonNull UUID impfdossierId,
		@NonNull String name,
		@NonNull String vorname
	) {
		this.impfdossierId = impfdossierId;
		this.name = name;
		this.vorname = vorname;
	}
}
