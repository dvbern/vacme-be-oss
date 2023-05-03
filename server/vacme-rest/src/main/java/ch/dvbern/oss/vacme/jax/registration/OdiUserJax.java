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

package ch.dvbern.oss.vacme.jax.registration;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.types.FachRolle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OdiUserJax {

	public OdiUserJax() {
		this.username = "";
		this.firstName = "";
		this.lastName = "";
		this.email = "";
		this.phone = "";
		this.glnNummer = "";
	}

	@Nullable
	private String id;

	@NotNull
	private String username;

	@Nullable
	private Boolean enabled;

	@NotNull
	private String firstName;

	@NotNull
	private String lastName;

	@NotNull
	private String email;

	@NotNull
	private String phone;

	private String glnNummer;

	private FachRolle fachRolle;

}
