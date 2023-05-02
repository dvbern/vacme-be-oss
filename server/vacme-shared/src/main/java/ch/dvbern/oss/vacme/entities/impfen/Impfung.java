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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.benutzer.Benutzer;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.ZertifikatCreationDTO;
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
		@UniqueConstraint(name = "UC_Impfung_termin", columnNames = "termin_id")
	},
	indexes = {
	@Index(name = "IX_Impfung_timestamp_impfung", columnList = "timestampImpfung"),
	@Index(name = "IX_Impfung_timestampvmdl_extern", columnList = "timestampVMDL, extern, id"),
	@Index(name = "IX_Impfung_generateZertifikat", columnList = "generateZertifikat, id"),
}
)
@SqlResultSetMapping(
	name = Constants.REGISTTIERUNGSNUMMER_IMPFUNGID_DTO_MAPPING,
	classes = @ConstructorResult(
		targetClass = ZertifikatCreationDTO.class,
		columns = {
			@ColumnResult(name = "registrierungsnummer", type = String.class),
			@ColumnResult(name = "impfungUUID", type = String.class), // UUID-Mapping scheint problematisch zu sein!
		}
	)
)
public class Impfung extends AbstractUUIDEntity<Impfung> implements ImpfInfo {

	private static final long serialVersionUID = -2854345385817970097L;

	@NotNull @NonNull
	@Column(nullable = false)
	private LocalDateTime timestampImpfung;

	@NotNull @NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impfung_impfstoff_id"), nullable = false)
	private Impfstoff impfstoff;

	@NotNull @NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Verarbreichungsart verarbreichungsart;

	@NotNull @NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Verarbreichungsort verarbreichungsort;

	@NotNull @NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Verarbreichungsseite verarbreichungsseite;

	@NotEmpty @NonNull
	@Column(nullable = false, length = DBConst.DB_VMDL_SCHNITTSTELLE_LENGTH)
	@Size(max = DBConst.DB_VMDL_SCHNITTSTELLE_LENGTH)
	private String lot; // Min 10 (eher 100) haben dieselbe Charge

	@NotNull @NonNull
	@Column(nullable = false)
	private BigDecimal menge;

	/**
	 * Dieses Feld muss immer false sein und belegt dass keine Kontraindikationen zum Zeitpunkt der Impfung bestanden
	 * Wird immer zusammen mit neueKrankheit gesetzt
	 * @since 5.0.4
	 */
	@NotNull
	@Column(nullable = false)
	private boolean fieber = false;


	/**
	 * Dieses Feld muss immer false sein und belegt dass keine Kontraindikationen zum Zeitpunkt der Impfung bestanden
	 * Wird immer zusammen mit fieber gesetzt
	 * @since 5.0.4
	 */
	@NotNull
	@Column(nullable = false)
	private boolean neueKrankheit = false;

	@Nullable
	@Column()
	private Boolean keineBesonderenUmstaende = false;

	@Nullable
	@Column()
	private Boolean schwanger;

	@NotNull
	@Column(nullable = false)
	private boolean einwilligung = false;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	@Size(max = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	private String bemerkung;


	@NotNull @NonNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impfung_termin_id"), nullable = false)
	private Impftermin termin;

	@NotNull @NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impfung_verantwortlicher_id"), nullable = false)
	private Benutzer benutzerVerantwortlicher;

	@NotNull @NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impfung_durchfuehrender_id"), nullable = false)
	private Benutzer benutzerDurchfuehrend;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime timestampVMDL;


	public static ID<Impfung> toId(UUID id) {
		return new ID<>(id, Impfung.class);
	}

	@Column(nullable = false)
	private boolean extern = false;

	/**
	 * Dieses Flag ist normalerweise true fuer Impfung 1+2 und kann fuer nachfolgende Impfungen auch true sein, z.B. wenn eine Person
	 * mit Immunschwaeche eine dritte oder vierte Impfung braucht, um die Grundimmunisierung abzuschliessen.
	 * Spaetere Boosterimpfungen haben grundimmunisierung=false.
	 * Bei jemandem, der nur 1 Impfung brauchte, weil er Covid hatte, ist auch die zweite Impfung schon grundimmunisierung=false.
	 */
	@NotNull
	@Column(nullable = false)
	private boolean grundimmunisierung;

	@NotNull
	@Column(nullable = false)
	private boolean generateZertifikat = false; // Zertifikat generieren (wichtig: alles vorher validieren, sonst fuellt es den Batchjob mit nicht erstellbaren!)

	@NotNull
	@Column(nullable = false)
	private boolean selbstzahlende;

	@Nullable
	@Column()
	private Boolean risikoreichesSexualleben;

	@Nullable
	@Column()
	private Boolean impfungAusBeruflichenGruenden;

	@Nullable
	@Column()
	private Boolean kontaktMitPersonAusRisikogruppe;

	@NotNull
	@Column(nullable = false)
	private boolean schnellschemaGesetztFuerImpfung = false;

	@NotNull
	@NonNull
	@Column(nullable = false, updatable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private KantonaleBerechtigung kantonaleBerechtigung;

	@Nullable
	@Column()
	private Boolean zeckenstich;

	@NonNull
	@Override
	public LocalDateTime getTimestampImpfung() {
		return timestampImpfung;
	}

	@Override
	public boolean gehoertZuGrundimmunisierung() {
		return isGrundimmunisierung();
	}

	@Override
	public int getAnzahlImpfungen() {
		return 1;	// A single Impfung is always just 1
	}

	@Override
	public boolean isNextImpfungPossiblySelbstzahler(@NonNull KrankheitIdentifier krankheitIdentifier) {
		return !isGrundimmunisierung();
	}
}
