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

package ch.dvbern.oss.vacme.entities.plz;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;

@Entity
@Getter
@Setter
@ToString
@Table(
	indexes = {
		@Index(name = "IX_plz_medstat", columnList = "plz"),
	}
)
public class PLZMedstat extends AbstractUUIDEntity<PLZMedstat> {

	private static final long serialVersionUID = 3189985148329318330L;

	@NotNull
	@NonNull
	@Column(nullable = false, length = 4)
	@Size(max = 4)
	private String plz;

	@NotNull
	@NonNull
	@Column(nullable = false, length = 2)
	@Size(max = 2)
	private String kanton;

	@NotNull
	@NonNull
	@Column(nullable = false, length = 4)
	@Size(max = 4)
	private String medstat;

	public PLZMedstat() {

	}

	public PLZMedstat(@NotNull String plz, @NotNull String kanton, @NotNull String medstat) {
		super();
		this.plz = plz;
		this.kanton = kanton;
		this.medstat = medstat;
	}

}
