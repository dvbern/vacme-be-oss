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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.types.ZulassungsStatus;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.shared.util.Constants;
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
@Table(
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_Impfstoff_name", columnNames = "name"),
		@UniqueConstraint(name = "UC_Impfstoff_code", columnNames = "code")
	}
)
public class Impfstoff extends AbstractUUIDEntity<Impfstoff> {

	private static final long serialVersionUID = 1471567013714242919L;

	@NotEmpty
	@NonNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String hersteller;

	@NotEmpty
	@NonNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String name;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String wHoch2Code;

	@NotEmpty
	@NonNull
	@Column(nullable = false, length = DBConst.DB_VMDL_SCHNITTSTELLE_LENGTH)
	@Size(max = DBConst.DB_VMDL_SCHNITTSTELLE_LENGTH)
	private String code;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_HEX_FARBE_LENGTH)
	@Size(max = DBConst.DB_HEX_FARBE_LENGTH)
	private String hexFarbe;

	@NotNull
	@Column(nullable = false)
	private int anzahlDosenBenoetigt;

	@NotEmpty
	@NonNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String covidCertProdCode;

	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private ZulassungsStatus zulassungsStatus;

	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private ZulassungsStatus zulassungsStatusBooster;

	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Impfstofftyp impfstofftyp;

	@Nullable
	@Column(nullable = true)
	private String informationsLink;

	@NonNull
	@OneToMany(mappedBy = "impfstoff", fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, orphanRemoval = true)
	private List<ImpfempfehlungChGrundimmunisierung> impfempfehlungenChGrundimmunisierung = new ArrayList<>();

	@NotNull
	@Column(nullable = false)
	private boolean eingestellt = false;

	@NonNull
	@ManyToMany
	@JoinTable(name = "Impfstoff_Krankheit",
		joinColumns = @JoinColumn(
			name = "impfstoff_id",
			foreignKey = @ForeignKey(name = "impfstoff_krankheit_impfstoff_fk")),
		inverseJoinColumns = @JoinColumn(
			name = "krankheit_id",
			foreignKey = @ForeignKey(name = "impfstoff_krankheit_krankheit_fk"))
	)
	private Set<Krankheit> krankheiten;

	@NonNull
	public static ID<Impfstoff> toId(@NonNull UUID id) {
		return new ID<>(id, Impfstoff.class);
	}

	@NonNull
	public String getDisplayName() {
		if (Constants.UNBEKANNTE_POCKENIMPFUNG_IN_KINDHEIT_UUID.equals(getId())) {
			return getName(); // Name wird Clientseitig uebersetzt
		}
		return this.getHersteller() + " - " + this.getName();
	}

	@NonNull
	public Optional<ImpfempfehlungChGrundimmunisierung> findImpfempfehlung(@Nullable Integer anzahlImpfungen, @Nullable Boolean genesen) {
		if (anzahlImpfungen == null) {
			return Optional.empty();
		}

		var countGenesen = (anzahlImpfungen > 0 && Boolean.TRUE.equals(genesen)) ? 1 : 0; // genesen zaehlt als 1 Impfung, aber nur, wenn man auch mind. 1 Impfung hat.
		var impfungenPlusGenesen = anzahlImpfungen + countGenesen;
		return impfempfehlungenChGrundimmunisierung.stream()
			.filter(impfempfehlung -> impfempfehlung.getAnzahlVerabreicht() <= impfungenPlusGenesen)
			.min(Comparator.comparingInt(ImpfempfehlungChGrundimmunisierung::getNotwendigFuerChGrundimmunisierung));
	}
}
