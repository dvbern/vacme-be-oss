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

package ch.dvbern.oss.vacme.entities.terminbuchung;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

import static java.util.Locale.GERMAN;

/**
 * Impftermin, welcher in einem Impfslots eines OrtDerImpfung angeboten wird.
 * Sobald eine Person gesetzt ist, ist der Termin besetzt.
 */
@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@Table(
	indexes = {
		@Index(name = "IX_Impftermin_impfslot", columnList = "impfslot_id"),
		@Index(name = "IX_Impftermin_impffolge", columnList = "impffolge"),
		@Index(name = "IX_Impftermin_gebucht", columnList = "gebucht"),
		@Index(name = "IX_Impftermin_Impffolge_Gebucht", columnList = "impffolge, gebucht"),
		@Index(name = "IX_Impftermin_gebucht_folge", columnList = "impfslot_id, gebucht, impffolge"),
	}
)
public class Impftermin extends AbstractUUIDEntity<Impftermin> {

	private static final long serialVersionUID = -2854345385817970097L;

	@NotNull @NonNull
	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private Impffolge impffolge;

	@NotNull @NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impftermin_impfslot_id"), nullable = false)
	private Impfslot impfslot;

	@NotNull
	@Column(nullable = false)
	private boolean gebucht;

	@NotNull
	@Column(nullable = false)
	private int offsetInMinutes = 0;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime timestampReserviert;

	@Nullable
	@Column(nullable = true, length = 8)
	@Size(max = 8)
	private String registrierungsnummerReserviert;


	public static ID<Impftermin> toId(UUID id) {
		return new ID<>(id, Impftermin.class);
	}

	@SuppressWarnings("unused")
	public void setGebucht(boolean gebucht) throws IllegalAccessException {
		throw new IllegalAccessException("Achtung, gebucht-Flag muss ueber ImpfterminRepo gesetzt werden!");
	}

	public void setGebuchtFromImpfterminRepo(boolean gebucht) {
		this.gebucht = gebucht;
	}

	@NonNull
	private LocalDateTime getEffectiveTerminStart() {
		return impfslot.getZeitfenster().getVon().plusMinutes(offsetInMinutes);
	}

	@NonNull
	public String getTerminZeitfensterStartTimeString() {
		return DateUtil.formatLocalTime(getEffectiveTerminStart());
	}

	@NonNull
	public String getTerminZeitfensterStartDateAndTimeString() {
		return DateUtil.DEFAULT_DATE_TIME_FORMAT.apply(GERMAN).format(getEffectiveTerminStart());
	}
}
