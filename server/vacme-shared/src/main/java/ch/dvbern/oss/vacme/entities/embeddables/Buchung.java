/*
 *
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

package ch.dvbern.oss.vacme.entities.embeddables;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.registration.AbgesagteTermine;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Getter
@Setter
@Embeddable
public class Buchung implements Serializable {

	private static final long serialVersionUID = -7105529119759021491L;

	@NotNull
	@Column(nullable = false)
	private boolean nichtVerwalteterOdiSelected = false;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impfdossier_impfzentrum_id"), nullable = true)
	private OrtDerImpfung gewuenschterOdi;

	@Nullable
	@OneToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impfdossier_impftermin1_id"), nullable = true)
	private Impftermin impftermin1;

	@Nullable
	@OneToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impfdossier_impftermin2_id"), nullable = true)
	private Impftermin impftermin2;

	@Valid
	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_impfdossier_abgesagtetermine_id"), nullable = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private AbgesagteTermine abgesagteTermine;

	@NotNull
	@Column(nullable = false)
	private boolean selbstzahler = false;

	@NotNull
	@Column(nullable = false)
	private boolean externGeimpftConfirmationNeeded = false;


	@SuppressWarnings("unused")
	public void setImpftermin1(@Nullable Impftermin impftermin1) throws IllegalAccessException {
		throw new IllegalAccessException("Achtung, Impftermin muss ueber ImpfterminRepo gesetzt werden!");
	}

	public void setImpftermin1FromImpfterminRepo(@Nullable Impftermin impftermin1) {
		this.impftermin1 = impftermin1;
	}

	@SuppressWarnings("unused")
	public void setImpftermin2(@Nullable Impftermin impftermin2) throws IllegalAccessException {
		throw new IllegalAccessException("Achtung, Impftermin muss ueber ImpfterminRepo gesetzt werden!");
	}

	public void setImpftermin2FromImpfterminRepo(@Nullable Impftermin impftermin2) {
		this.impftermin2 = impftermin2;
	}
}
