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

import ch.dvbern.oss.vacme.entities.base.AbstractStringEntity;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;

@Entity
@Getter
@Setter
@ToString
@Table(indexes = @Index(name = "IX_plz", columnList = "plz"))
public class PLZData extends AbstractStringEntity<PLZData> {

	private static final long serialVersionUID = -4622340660235789864L;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_UUID_LENGTH)
	private String plz;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	private String ortsbez;

	@NotNull
	@NonNull
	@Column(nullable = false, length = 2)
	@Size(max = 2)
	private String kanton;

	public PLZData() {

	}

	public PLZData(@NonNull String id, @NonNull String plz, @NonNull String ortsbez, @NonNull String kanton) {
		super();
		setId(id);
		this.plz = plz;
		this.ortsbez = ortsbez;
		this.kanton = kanton;
	}
}
