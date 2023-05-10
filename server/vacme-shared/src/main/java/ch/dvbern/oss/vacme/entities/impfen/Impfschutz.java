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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
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



@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(indexes =
	@Index(name = "IX_Impfschutz_freigegebenNaechsteImpfungAb", columnList = "freigegebenNaechsteImpfungAb, id")
)
public class Impfschutz extends AbstractUUIDEntity<Impfschutz> {

	private static final long serialVersionUID = -2351206172682178319L;

	public static final String DELIMITER = ",";

	@Nullable
	@Column(nullable = true)
	private LocalDateTime immunisiertBis;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime freigegebenNaechsteImpfungAb;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime freigegebenAbSelbstzahler;

	@Nullable
	@Column(nullable = true)
	private String erlaubteImpfstoffe;

	@NotNull
	@Column(nullable = false)
	private boolean benachrichtigungBeiFreigabe = false;



	public Impfschutz(
		@Nullable LocalDateTime immunisiertBis,
		@Nullable LocalDateTime freigegebenAb,
		@Nullable LocalDateTime freigegebenAbSelbstzahler,
		@NonNull Set<UUID> erlaubteImpfstoffe,
		boolean benachrichtigungBeiFreigabe
	) {
		super();
		this.immunisiertBis = immunisiertBis;
		this.freigegebenNaechsteImpfungAb = freigegebenAb;
		this.freigegebenAbSelbstzahler = freigegebenAbSelbstzahler;
		this.setErlaubteImpfstoffeFromCollection(erlaubteImpfstoffe);
		this.benachrichtigungBeiFreigabe = benachrichtigungBeiFreigabe;
	}

	public void apply(@NonNull Impfschutz impfschutz) {
		this.setFreigegebenNaechsteImpfungAb(impfschutz.getFreigegebenNaechsteImpfungAb());
		this.setFreigegebenAbSelbstzahler(impfschutz.getFreigegebenAbSelbstzahler());
		this.setImmunisiertBis(impfschutz.getImmunisiertBis());
		this.setErlaubteImpfstoffe(impfschutz.getErlaubteImpfstoffe());
		this.setBenachrichtigungBeiFreigabe(impfschutz.isBenachrichtigungBeiFreigabe());
	}

	@NonNull
	public Set<UUID> getErlaubteImpfstoffeCollection() {
		return erlaubteImpfstoffe == null || erlaubteImpfstoffe.isEmpty()
			? new HashSet<>()
			: Arrays.stream(erlaubteImpfstoffe.split(DELIMITER)).map(UUID::fromString).collect(Collectors.toSet());
	}

	private void setErlaubteImpfstoffeFromCollection(@NonNull Set<UUID> erlaubteImpfstoffeCollection) {
		if (erlaubteImpfstoffeCollection.isEmpty()) {
			erlaubteImpfstoffe = null;
		} else {
			erlaubteImpfstoffe = String.join(DELIMITER, new ArrayList<>(
				erlaubteImpfstoffeCollection.stream()
					.map(UUID::toString)
					.collect(Collectors.toList())));
		}
	}

	public static ID<Impfschutz> toId(UUID id) {
		return new ID<>(id, Impfschutz.class);
	}
}
