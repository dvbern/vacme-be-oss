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

package ch.dvbern.oss.vacme.entities.umfrage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Umfrage extends AbstractUUIDEntity<Umfrage> {

	private static final long serialVersionUID = 2084889512677044121L;

	@NotNull
	@NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Umfrage_registrierung"), nullable = false)
	private Registrierung registrierung;

	@NotEmpty
	@NonNull
	@Column(nullable = false, updatable = false, length = 8)
	@Size(max = 8)
	private String umfrageCode; // 6-stellig, Kleinbuchstaben-Buchstaben+Zahlen, unique

	@NotNull @NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private UmfrageGruppe umfrageGruppe;

	@NotNull
	@Column(nullable = false)
	private boolean teilgenommen = false;

	@NotNull
	@Column(nullable = false)
	private boolean valid = false; // gueltige CH-Natelnummer, damit nicht immer wieder dieselben gesucht werden

	@NotNull
	@NonNull
	@Column(nullable = true, length = DBConst.DB_PHONE_LENGTH)
	@Size(max = DBConst.DB_PHONE_LENGTH)
	private String mobiltelefon; // Wir merken uns die Tel, damit wir nicht wieder joinen muessen. Koennte theoretisch unterdessen geaendert haben

}
