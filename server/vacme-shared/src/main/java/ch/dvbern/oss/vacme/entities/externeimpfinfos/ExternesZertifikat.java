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

package ch.dvbern.oss.vacme.entities.externeimpfinfos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.entities.util.validators.CheckExternesZertifikatDatumLetzteImpfung;
import ch.dvbern.oss.vacme.wrapper.VacmeDecoratorFactory;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Table(indexes = {
	@Index(name = "IX_ExternesZertifikat_impfdossier", columnList = "impfdossier_id, id"),
},
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_ExternesZertifikat_impfdossier", columnNames = "impfdossier_id"),
	})
@CheckExternesZertifikatDatumLetzteImpfung
public class ExternesZertifikat extends AbstractUUIDEntity<ExternesZertifikat> implements ImpfInfo {

	private static final long serialVersionUID = -4023431963287495361L;

	@NotNull
	@NonNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ExternesZertifikat_impfdossier"), nullable = false, updatable = false)
	private Impfdossier impfdossier;

	@Nullable
	@Column(nullable = true)
	private LocalDate letzteImpfungDate;

	@Column(nullable = false)
	private boolean letzteImpfungDateUnknown;

	@NotNull
	@NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ExternesZertifikat_impfstoff_id"), nullable = false)
	private Impfstoff impfstoff;

	@NonNull
	@NotNull
	@Min(1)
	private Integer anzahlImpfungen;

	@NotNull
	private boolean genesen = false;

	@Nullable
	@Column(nullable = true)
	private LocalDate positivGetestetDatum;

	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true, length = DBConst.DB_UUID_LENGTH)
	private String kontrollePersonUUID;

	@Column(nullable = true)
	private LocalDateTime kontrolliertTimestamp = null;

	@Nullable
	@Column(nullable = true)
	private Boolean trotzdemVollstaendigGrundimmunisieren;

	public boolean isGrundimmunisiert(@NotNull @NonNull KrankheitIdentifier krankheit) {
		return isGrundimmunisiert(krankheit, impfstoff, anzahlImpfungen, genesen, trotzdemVollstaendigGrundimmunisieren);
	}

	@Override
	public boolean gehoertZuGrundimmunisierung() {
		return true; // Externe Zertifikate sind IMMER Teil der Grundimmunisierung, nicht Booster
	}

	@Override
	public boolean isNextImpfungPossiblySelbstzahler(@NonNull KrankheitIdentifier krankheitIdentifier) {
		return isGrundimmunisiert(krankheitIdentifier);
	}

	@NonNull
	public MissingForGrundimmunisiert getMissingForGrundimmunisiertBeforeDecision(@NotNull @NonNull KrankheitIdentifier krankheit) {
		return calculateAnzahlMissingImpfungen(krankheit, impfstoff, anzahlImpfungen, genesen, null);
	}

	@NonNull
	public MissingForGrundimmunisiert getMissingForGrundimmunisiert(@NotNull @NonNull KrankheitIdentifier krankheit) {
		return calculateAnzahlMissingImpfungen(krankheit, impfstoff, anzahlImpfungen, genesen, trotzdemVollstaendigGrundimmunisieren);
	}

	public static boolean isGrundimmunisiert(
		@NonNull KrankheitIdentifier krankheit,
		@Nullable Impfstoff impfstoff,
		@Nullable Integer anzahlImpfungen,
		@Nullable Boolean genesen,
		@Nullable Boolean trotzdemVollstaendigGrundimmunisieren
	) {
		MissingForGrundimmunisiert missingForGrundimmunisiert = calculateAnzahlMissingImpfungen(
			krankheit,
			impfstoff,
			anzahlImpfungen,
			genesen,
			trotzdemVollstaendigGrundimmunisieren);
		return missingForGrundimmunisiert == MissingForGrundimmunisiert.BRAUCHT_0_IMPFUNGEN;
	}

	@NonNull
	public static MissingForGrundimmunisiert calculateAnzahlMissingImpfungen(
		@NonNull KrankheitIdentifier krankheit,
		@Nullable Impfstoff impfstoff,
		@Nullable Integer anzahlImpfungen,
		@Nullable Boolean genesen,
		@Nullable Boolean trotzdemVollstaendigGrundimmunisieren
	) {
		if (impfstoff == null || Boolean.TRUE.equals(trotzdemVollstaendigGrundimmunisieren)) {
			return MissingForGrundimmunisiert.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG;
		}
		return VacmeDecoratorFactory.getDecorator(krankheit).calculateMissingFuerGrundimmunisierung(impfstoff, anzahlImpfungen, genesen);
	}

	public boolean isKontrolliert() {
		return this.kontrolliertTimestamp != null;
	}

	@Override
	@Nullable
	public LocalDateTime getTimestampImpfung() {
		if (letzteImpfungDate != null) {
			return letzteImpfungDate.plusDays(1).atStartOfDay().minusSeconds(1); // last possible
		}
		return null;
	}

	@Override
	public int getAnzahlImpfungen() {
		return anzahlImpfungen;
	}

}

