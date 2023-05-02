/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.entities.terminbuchung;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Entity
@Table(uniqueConstraints = {
	@UniqueConstraint(name = "UC_OdiNoFreieTermine_odi_krankheit", columnNames = {"ortDerImpfung_id", "krankheitIdentifier"})
})
@Getter
@Setter
@NoArgsConstructor
public class OdiNoFreieTermine extends AbstractUUIDEntity<OdiNoFreieTermine> {

	private static final long serialVersionUID = -5658479277961443850L;

	@Column(nullable = false)
	private boolean noFreieTermine1;

	@Column(nullable = false)
	private boolean noFreieTermine2;

	@Column(nullable = false)
	private boolean noFreieTermineN;

	@NonNull
	@NotNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private KrankheitIdentifier krankheitIdentifier;

	@NonNull
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_odiNoFreieTermine_ortDerImpfung_id"), nullable = false)
	private OrtDerImpfung ortDerImpfung;

	public OdiNoFreieTermine(@NonNull KrankheitIdentifier krankheitIdentifier, @NonNull OrtDerImpfung ortDerImpfung) {
		this.krankheitIdentifier = krankheitIdentifier;
		this.ortDerImpfung = ortDerImpfung;
		this.noFreieTermine1 = true;
		this.noFreieTermine2 = true;
		this.noFreieTermineN = true;
	}

	public static OdiNoFreieTermine createDummy(
		@NonNull @NotNull KrankheitIdentifier krankheit,
		@NonNull @NotNull OrtDerImpfung ortDerImpfung
	) {
		return new OdiNoFreieTermine(krankheit, ortDerImpfung);
	}
}
