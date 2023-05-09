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

package ch.dvbern.oss.vacme.entities.registration;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = {
	@UniqueConstraint(name = "UC_Fragebogen_registrierung", columnNames = "registrierung_id")
})
public class Fragebogen extends AbstractUUIDEntity<Fragebogen> {

	private static final long serialVersionUID = -355110587188501409L;

	@Valid
	@NotNull @NonNull
	@OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}) // NOT CascadeType.REMOVE!!!
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_fragebogen_registrierung_id"), nullable = false)
	private Registrierung registrierung;

	@NotNull @NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = true, length = DBConst.DB_ENUM_LENGTH)
	private ChronischeKrankheiten chronischeKrankheiten;

	@NotNull @NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = true, length = DBConst.DB_ENUM_LENGTH)
	private BeruflicheTaetigkeit beruflicheTaetigkeit;

	@NotNull @NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = true, length = DBConst.DB_ENUM_LENGTH)
	private Lebensumstaende lebensumstaende;

	@NotNull @NonNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, length = DBConst.DB_ENUM_LENGTH)
	private AmpelColor ampel;

	@NonNull
	public static ID<Fragebogen> toId(@NonNull UUID id) {
		return new ID<>(id, Fragebogen.class);
	}

	@Nullable
	@Column(nullable = true)
	private Boolean immunsupprimiert;
}
