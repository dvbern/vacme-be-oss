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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.embeddables.Adresse;
import ch.dvbern.oss.vacme.entities.impfen.AuslandArt;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.Krankenkasse;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import ch.dvbern.oss.vacme.util.DeservesZertifikatValidator;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import ch.dvbern.oss.vacme.validators.CheckRegistrierungKontaktdaten;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

/**
 * Die Mobilenummer darf aber mehrfach verwendet werden, damit ich z.B. meine
 * Grosseltern anmelden kann.
 */
@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@CheckRegistrierungKontaktdaten
@Table(
	indexes = {
		@Index(name = "IX_registrierungsnummer_id", columnList = "registrierungsnummer, id"),
		@Index(name = "IX_Registrierung_externalId", columnList = "externalId, id"),
		@Index(name = "IX_Registrierung_geburtsdatum", columnList = "geburtsdatum, id"),
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_Registrierung_registrierungsnummer", columnNames = "registrierungsnummer"),
		@UniqueConstraint(name = "UC_Registrierung_benutzer", columnNames = "benutzerId"),
		@UniqueConstraint(name = "UC_Registrierung_externalId", columnNames = "externalId"),
	}
)
@Slf4j
public class Registrierung extends AbstractUUIDEntity<Registrierung> {

	private static final long serialVersionUID = 1471567013714242919L;

	@Nullable
	@Column(nullable = true, updatable = true, unique = true, length = DBConst.DB_UUID_LENGTH)
	@Type(type = "org.hibernate.type.UUIDCharType")
	private UUID benutzerId;

	@Nullable // Wir wissen die Sprache nur bei Online-Registrierungen
	@Column(nullable = true, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Sprache sprache;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Geschlecht geschlecht;

	@NotEmpty
	@NonNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String name;

	@NotEmpty
	@NonNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String vorname;

	@NotNull
	@NonNull
	@Column(nullable = false)
	private LocalDate geburtsdatum;

	@Valid
	@Embedded
	@NotNull
	@NonNull
	private Adresse adresse = new Adresse();

	@NotNull
	@Column(nullable = false)
	private boolean immobil = false;
		// benutzer kann nicht ins impfzentrum kommen sondern muss mobile Impfzentren wahlen

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private RegistrierungsEingang registrierungsEingang;

	@NotNull
	@NonNull
	@Column(nullable = false, updatable = false)
	private LocalDateTime registrationTimestamp;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String mail;

	@NotNull
	@Column(nullable = false)
	private boolean mailValidiert = false;            // Benutzer hat auf Link im Mail geklickt

	@Nullable
	@Column(nullable = true, length = DBConst.DB_PHONE_LENGTH)
	@Size(max = DBConst.DB_PHONE_LENGTH)
	private String telefon;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	@Size(max = DBConst.DB_BEMERKUNGEN_MAX_LENGTH)
	private String bemerkung;

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Prioritaet prioritaet;

	@NotEmpty
	@NonNull
	@Column(nullable = false, updatable = false, length = 8)
	@Size(max = 8)
	private String registrierungsnummer; // 5-stellig, Gross-Buchstaben+Zahlen, unique

	@NotNull
	@NonNull
	@Column(nullable = false, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private Krankenkasse krankenkasse; // Enum oder Entity oder String?

	@NotEmpty
	@NonNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String krankenkasseKartenNr;

	@NonNull
	@OneToMany(mappedBy = "registrierung", fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, orphanRemoval = true)
	private Set<KkkNummerAlt> kkkNummerAltSet = new HashSet<>(); // damit man auch nach vorherigen Nummern suchen kann

	@Nullable
	@Column(nullable = true, length = DBConst.DB_ENUM_LENGTH)
	@Enumerated(EnumType.STRING)
	private AuslandArt auslandArt;

	@NotNull
	@Column(nullable = false)
	private boolean abgleichElektronischerImpfausweis = false;

	/**
	 * Null fuer alte Registrierungen und default auf false fuer neue
	 */
	@Nullable
	@Column(nullable = true)
	private Boolean contactTracing = false;

	@Nullable
	@Column(nullable = true, updatable = false, unique = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String externalId;

	@NotNull
	@Column(nullable = false)
	private boolean anonymisiert = false;

	@NotNull
	@Column(nullable = false)
	private boolean generateOnboardingLetter = false;

	@Nullable
	@Column(nullable = true)
	private Boolean verstorben;

	/**
	 * Null fuer alte Registrierungen und default auf false fuer neue
	 */
	@Nullable
	@Column(nullable = true)
	private Boolean schutzstatus;

	/**
	 * Null für alte Registrierungen und default auf false für neue
	 */
	@Nullable
	@Column(nullable = true)
	private Boolean keinKontakt;

	@Nullable
	@Column(nullable = true)
	// Wird verwendet um die darstellung des umfragen-aktuell-daten.component zu bestimmen.
	private LocalDateTime timestampInfoUpdate;

	@Nullable
	@Column(nullable = true)
	// Wird verwendet um die darstellung das phonenumber-update popup zu bestimmen.
	private LocalDateTime timestampPhonenumberUpdate;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String identifikationsnummer;

	@JsonIgnore
	@NonNull
	public String getNameVorname() {
		return getName() + ' ' + getVorname();
	}

	@JsonIgnore
	@NonNull
	public Locale getLocale() {
		if (sprache != null) {
			return sprache.getLocale();
		}
		return Locale.GERMAN;
	}

	@NonNull
	public static ID<Registrierung> toId(@NonNull UUID id) {
		return new ID<>(id, Registrierung.class);
	}

	public void setBenutzerId(@Nullable UUID benutzerId) {
		if (this.benutzerId != null && !this.benutzerId.equals(benutzerId)) {
			throw new AppFailureException("Not allowed to change the benutzerId from "
				+ this.benutzerId
				+ " to "
				+ benutzerId);
		}
		this.benutzerId = benutzerId;
	}

	public void setGenerateZertifikatTrueIfAllowed(
		@NonNull ImpfinformationDto infos,
		@Nullable Impfung relevanteImpfung
	) {
		if (relevanteImpfung == null) {
			return;
		}
		final boolean deservesZertifikat = DeservesZertifikatValidator.deservesZertifikat(infos, relevanteImpfung);
		relevanteImpfung.setGenerateZertifikat(deservesZertifikat);
	}

	// ueberschreibt den default Setter, weil wir die alten Nummern archivieren wollen
	public void setKrankenkasseKartenNrAndArchive(@NotNull @NonNull String kkkNummerNeu) {
		archiviereKkkNummer(this.krankenkasseKartenNr, kkkNummerNeu);
		this.krankenkasseKartenNr = kkkNummerNeu;
	}

	private void archiviereKkkNummer(@Nullable String kkkNummerBisher, @NotNull @NonNull String kkkNummerNeu) {
		if (kkkNummerBisher != null && !kkkNummerBisher.equals(kkkNummerNeu)) {
			KkkNummerAlt kkkNummerAlt = new KkkNummerAlt();
			kkkNummerAlt.setRegistrierung(this);
			kkkNummerAlt.setNummer(kkkNummerBisher);
			kkkNummerAlt.setAktivBis(LocalDateTime.now());
			this.getKkkNummerAltSet().add(kkkNummerAlt);
		}
	}
}
