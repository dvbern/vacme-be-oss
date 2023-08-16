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

package ch.dvbern.oss.vacme.entities.impfen;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(
	indexes = {
		@Index(name = "IX_Erkrankung_impfdossier", columnList = "impfdossier_id, id")
	}
)
@EqualsAndHashCode(callSuper = true)
public class Erkrankung extends AbstractUUIDEntity<Erkrankung> implements Comparator<Erkrankung>, Comparable<Erkrankung> {

	private static final long serialVersionUID = 3236609534170556332L;
	@NotNull
	@NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Erkrankung_impfdossier"), nullable = false, updatable = false)
	private Impfdossier impfdossier;

	@NotNull
	@NonNull
	@Column(nullable = false)
	private LocalDate date;

	public static ID<Erkrankung> toId(UUID id) {
		return new ID<>(id, Erkrankung.class);
	}

	@Override
	public int compare(Erkrankung eintrag1, Erkrankung eintrag2) {
		return eintrag1.getDate().compareTo(eintrag2.getDate());
	}

	@Override
	public int compareTo(@NonNull Erkrankung o) {
		return this.compare(this, o);
	}

}
