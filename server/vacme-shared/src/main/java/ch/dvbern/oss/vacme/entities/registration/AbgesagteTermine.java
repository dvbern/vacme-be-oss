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

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Entity used to store information about AbgesagteTermine. This is only used if the
 * OdI decides to cancel all Termine e.g. for logistical reasons
 */
@Entity
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Builder(builderMethodName = "hiddenBuilder")
public class AbgesagteTermine extends AbstractUUIDEntity<AbgesagteTermine> {

	private static final long serialVersionUID = -2854345385817970097L;

	// Wir haben hier nur einen ODI, weil bei Absage eines Termins 1, welcher die Absage des
	// dazugehoerenden Termins 2 nach sich zieht, der ODI noch nicht unterschiedlich sein kann.
	// Dies ist erst NACH der ersten Impfung moeglich, und dann kann der Termin 1 ja nicht mehr
	// abgesagt werden.
	@NotNull
	@NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_abgesagtetermine_impfzentrum_id"))
	private OrtDerImpfung ortDerImpfung;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime termin1;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime termin2;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime terminN;

	public void setOdiAndImpftermine(@NonNull final Impftermin termin1, @NonNull final Impftermin termin2) {
		this.setOrtDerImpfung(termin1.getImpfslot().getOrtDerImpfung());
		this.setTermin1(termin1.getImpfslot().getZeitfenster().getVon());
		this.setTermin2(termin2.getImpfslot().getZeitfenster().getVon());
	}

	public void setOdiAndImpftermin2(@NonNull final Impftermin termin2) {
		this.setOrtDerImpfung(termin2.getImpfslot().getOrtDerImpfung());
		this.setTermin2(termin2.getImpfslot().getZeitfenster().getVon());
	}

	public void setOdiAndImpftermin1(@NonNull final Impftermin termin1) {
		this.setOrtDerImpfung(termin1.getImpfslot().getOrtDerImpfung());
		this.setTermin1(termin1.getImpfslot().getZeitfenster().getVon());
	}

	public void setOdiAndImpfterminN(@NonNull final Impftermin terminN) {
		this.setOrtDerImpfung(terminN.getImpfslot().getOrtDerImpfung());
		this.setTerminN(terminN.getImpfslot().getZeitfenster().getVon());
	}
}
