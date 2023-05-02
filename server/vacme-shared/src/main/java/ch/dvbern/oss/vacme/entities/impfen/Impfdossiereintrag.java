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

import java.util.Comparator;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

/**
 * This class is an entry that binds Kontrolle and Impfung together
 */
@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(
	indexes = {
		@Index(name = "IX_Impfdossiereintrag_impfdossier_impftermin", columnList = "impfdossier_id, impftermin_id, id"),
		@Index(name = "IX_Impfdossiereintrag_kontrolle", columnList = "impfungkontrolleTermin_id, id"),
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_Impfdossiereintrag_impftermin", columnNames = "impftermin_id"),
		@UniqueConstraint(name = "UC_Impfdossiereintrag_kontrolle", columnNames = "impfungkontrolleTermin_id"),
	}
)
public class Impfdossiereintrag extends AbstractUUIDEntity<Impfdossiereintrag>  implements Comparator<Impfdossiereintrag>, Comparable<Impfdossiereintrag> {

	private static final long serialVersionUID = 2255721458819528453L;


	@NotNull
	@NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Impfdossiereintrag_impfdossier"), nullable = false, updatable = false)
	private Impfdossier impfdossier;


	@Nullable
	@OneToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Impfdossiereintrag_impftermin_id"), nullable = true)
	private Impftermin impftermin;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Impfdossiereintrag_impfungkontrolleTermin_id"), nullable = true)
	private ImpfungkontrolleTermin impfungkontrolleTermin;

	@NotNull
	@NonNull
	@Column(nullable = false)
	private Integer impffolgeNr; // z.B. Impfung Nr. 3


	public static ID<Impfdossiereintrag> toId(UUID id) {
		return new ID<>(id, Impfdossiereintrag.class);
	}

	@Override
	public int compare(Impfdossiereintrag eintrag1, Impfdossiereintrag eintrag2) {
		return eintrag1.getImpffolgeNr().compareTo(eintrag2.getImpffolgeNr());
	}

	@Override
	public int compareTo(@NonNull Impfdossiereintrag o) {
		return this.compare(this, o);
	}


	public void setImpftermin(@Nullable Impftermin impftermin) throws IllegalAccessException {
		throw new IllegalAccessException("Achtung, Impftermin muss ueber ImpfterminRepo gesetzt werden!");
	}

	public void setImpfterminFromImpfterminRepo(@Nullable Impftermin impftermin){
		this.impftermin = impftermin;
	}
}
