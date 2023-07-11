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

import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Krankheit;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.enums.Kundengruppe;
import ch.dvbern.oss.vacme.enums.Mandant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

import static ch.dvbern.oss.vacme.entities.util.DBConst.DB_TEXT_HTML_MAX_LENGTH;

/**
 * Anzahl Lanes, pro Lane alle 5 Minuten eine Person
 * oder
 * Jedes Zentrum muss pro 5 Minuten sagen, wieviele sie nehmen koennen
 * Jedes Zentrum muss seine Termine festlegen koennen! z.B. auch Apotheken
 * Altersheime vergeben keine Termine in VacMe
 *
 * Oeffnungszeiten muessen angegeben werden, damit wird das Raster vorgegeben
 */
@Entity
@Audited
@Table(uniqueConstraints = {
	@UniqueConstraint(name = "UC_OrtDerImpfung_name", columnNames = "name"),
	@UniqueConstraint(name = "UC_OrtDerImpfung_identifier", columnNames = "identifier")
})
@Getter
@Setter
@NoArgsConstructor
public class OrtDerImpfung extends AbstractUUIDEntity<OrtDerImpfung> {

	private static final long serialVersionUID = 7667206727153841194L;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Kundengruppe kundengruppe;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Mandant mandant;

	@NotEmpty
	@NonNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String name;

	@Valid
	@Embedded
	@NotNull
	@NonNull
	private Adresse adresse;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private OrtDerImpfungTyp typ;

	@JsonIgnore
	public OrtDerImpfungTyp getTypForAbrechnungExcel() {
		// KINDER_IMPFZENTRUM soll als IMPFZENTRUM ausgewiesen werden
		OrtDerImpfungTyp odiTyp = typ;
		if (odiTyp == OrtDerImpfungTyp.KINDER_IMPFZENTRUM) {
			odiTyp = OrtDerImpfungTyp.IMPFZENTRUM;
		}
		return odiTyp;
	}

	@NotNull
	@Column(nullable = false)
	private boolean mobilerOrtDerImpfung;

	@NotNull
	@Column(nullable = false)
	private boolean oeffentlich;

	@NotNull
	@Column(nullable = false)
	private boolean terminverwaltung;

	@Nullable
	@Column(nullable = true, length = DB_TEXT_HTML_MAX_LENGTH)
	@Size(max = DB_TEXT_HTML_MAX_LENGTH)
	private String externerBuchungslink;

	@NotNull
	@Column(nullable = false)
	private boolean booster;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String zsrNummer; // Fuer Krankenkassen

	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String glnNummer; // Fuer Krankenkassen

	@Nullable
	@Column(nullable = true, length = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	@Size(max = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	private String kommentar;

	@NotNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String identifier;
		// dies ist ein unique string der den ort der Impfung identifiziert der dann auch Keycloak kommt

	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String organisationsverantwortungKeyCloakId;

	@NotNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String fachverantwortungbabKeyCloakId;

	@Nullable
	@Column(nullable = true)
	private Double lat;

	@Nullable
	@Column(nullable = true)
	private Double lng;

	@ManyToMany()
	@JoinTable(name = "OdiFilter_OrtDerImpfung",
		joinColumns = @JoinColumn(
			name = "ortDerImpfung_id",
			foreignKey = @ForeignKey(name = "odifilter_ortderimpfung_ortderimpfung_fk")),
		inverseJoinColumns = @JoinColumn(
			name = "odifilter_id",
			foreignKey = @ForeignKey(name = "odifilter_ortderimpfung_odifilter_fk")),
		indexes = {
			@Index(name = "odifilter_ortderimpfung_odifilter_fk_ix",
				columnList = "odifilter_id"),
			@Index(name = "odifilter_ortderimpfung_ortderimpfung_fk_ix",
				columnList = "ortDerImpfung_id"),
		}
	)
	private Set<OdiFilter> filters;

	@ManyToMany()
	@JoinTable(name = "OrtDerImpfung_Impfstoff",
		joinColumns = @JoinColumn(
			name = "ortDerImpfung_id",
			foreignKey = @ForeignKey(name = "ortderimpfung_impfstoff_ortderimpfung_fk")),
		inverseJoinColumns = @JoinColumn(
			name = "impfstoff_id",
			foreignKey = @ForeignKey(name = "ortderimpfung_impfstoff_impfstoff_fk")),
		indexes = {
			@Index(name = "ortderimpfung_impfstoff_impfstoff_fk_ix",
				columnList = "impfstoff_id"),
			@Index(name = "ortderimpfung_impfstoff_ortderimpfung_fk_ix",
				columnList = "ortDerImpfung_id"),
		}
	)
	private Set<Impfstoff> impfstoffs;

	@ManyToMany
	@JoinTable(name = "OrtDerImpfung_Krankheit",
		joinColumns = @JoinColumn(
			name = "ortDerImpfung_id",
			foreignKey = @ForeignKey(name = "ortderimpfung_krankheit_ortderimpfung_fk")),
		inverseJoinColumns = @JoinColumn(
			name = "krankheit_id",
			foreignKey = @ForeignKey(name = "ortderimpfung_krankheit_krankheit_fk")),
		indexes = {
			@Index(name = "ortderimpfung_krankheit_ortderimpfung_fk_ix",
				columnList = "ortDerImpfung_id"),
			@Index(name = "ortderimpfung_krankheit_krankheit_fk_ix",
				columnList = "krankheit_id"),
		}

	)
	private Set<Krankheit> krankheiten;

	@NotNull
	@Column(nullable = false)
	private boolean personalisierterImpfReport;

	@NotNull
	@Column(nullable = false)
	private boolean deaktiviert;

	@NotNull
	@Column(nullable = false)
	private boolean impfungGegenBezahlung;

	public static ID<OrtDerImpfung> toId(UUID id) {
		return new ID<>(id, OrtDerImpfung.class);
	}
}
