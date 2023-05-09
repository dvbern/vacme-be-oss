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

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.embeddables.FileBlob;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = {
	@UniqueConstraint(name = "UC_ImpfungArchive_registrierung_id", columnNames = "registrierung_id")
})
public class ImpfungArchive extends AbstractUUIDEntity<ImpfungArchive> {
	private static final long serialVersionUID = 8115792032619370146L;

	@Valid
	@NotNull
	@NonNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ImpfungArchive_registrierung_id"), nullable = false)
	private Registrierung registrierung;

	@Valid
	@NotNull
	@NonNull
	@Embedded
	private FileBlob fileBlob;
}
