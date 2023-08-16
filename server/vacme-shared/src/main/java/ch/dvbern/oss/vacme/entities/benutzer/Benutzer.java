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
package ch.dvbern.oss.vacme.entities.benutzer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.base.DisplayName;
import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.base.LoginName;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.types.BenutzerRolle;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.entities.util.OneToManyHelper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.envers.Audited;

import static javax.persistence.CascadeType.ALL;

@Entity
@Audited
@Table(
	indexes = {
		@Index(name = "IX_Benutzer_email", columnList = "email, id"),
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_Benutzer_benutzername", columnNames = {"benutzername", "issuer"}),
		@UniqueConstraint(name = "UC_Benutzer_wellId", columnNames = {"wellId"})
	}
)
@Getter
@Setter
public class Benutzer extends AbstractUUIDEntity<Benutzer> implements DisplayName, LoginName {

	private static final long serialVersionUID = -1331860802838451212L;

	@Valid
	@NotEmpty
	@Column(nullable = false)
	private String email;

	@Valid
	@NotEmpty
	@Column(nullable = false)
	private String benutzername;

	@NotEmpty
	@Column(nullable = false)
	@Schema(required = true)
	private String name = "";

	@NotEmpty
	@Column(nullable = false)
	@Schema(required = true)
	private String vorname = "";

	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String glnNummer;

	// hier speichern wir die session id des letzten gueltigen tokens das wir bekommen haben
	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String lastUsedSessionId;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime timestampLastSessionId; // timestamp an dem wir die letzte Session gesehen haben

	@Nullable
	@Column(nullable = true)
	private LocalDateTime timestampLastUnlocked;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String lastSeenIp;

	/**
	 * Well ID is used to denote which users are imported from WEll
	 */
	@Nullable
	@Column(nullable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String wellId;

	// es ist moeglich dass ein arzt fuer mehrere orte der Impfung verantwortlich ist
	@ManyToMany()
	@JoinTable(name = "Benutzer_OrtDerImpfung",
		joinColumns = @JoinColumn(
			name = "benutzer_id",
			foreignKey = @ForeignKey(name = "benutzer_fk")),
		inverseJoinColumns = @JoinColumn(
			name = "ortDerImpfung_id",
			foreignKey = @ForeignKey(name = "ortderimpfung_fk")),
		indexes = {
			@Index(name = "benutzer_fk_ix", columnList = "benutzer_id"),
			@Index(name = "ortderimpfung_fk_ix", columnList = "ortDerImpfung_id"),
		}
	)
	private Set<OrtDerImpfung> ortDerImpfung = new HashSet<>();

	/**
	 * The actual deactivation happens in the login system (i.e. Keycloak) but this serves as a flag, so we know
	 * which users where last thought of by vacme as deaktiviert
	 */
	@Column(nullable = false)
	private boolean deaktiviert = false;

	@Column(nullable = false)
	private boolean geloescht = false;

	@Nullable
	private LocalDateTime geloeschtAm = null;

	@Nullable
	@Column(nullable = true, length = DBConst.DB_PHONE_LENGTH)
	@Size(max = DBConst.DB_PHONE_LENGTH)
	private String mobiltelefon;

	@NotNull
	@Column(nullable = false)
	private boolean mobiltelefonValidiert = false;

	@OneToMany(mappedBy = "benutzer", cascade = ALL, orphanRemoval = true)
	@Valid
	private List<BenutzerBerechtigung> berechtigungen = new ArrayList<>();


	@NotNull
	@Column(nullable = false, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	private String issuer;

	@Nullable
	private LocalDateTime benutzernameGesendetTimestamp = null;

	/**
	 * JPA only
	 */
	// @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "JPA instantiation only")
	@SuppressWarnings("assignment.type.incompatible")
	protected Benutzer() {
		//noinspection ConstantConditions
		email = null;
	}

	/**
	 * Used for JaxRS-Lookup.
	 */
	@SuppressWarnings("unused")
	public Benutzer(String id) {
		setId(UUID.fromString(id));
	}


	public Benutzer(UUID id) {
		setId(id);
	}

	@NonNull
	public static Benutzer fromEmail(@NonNull String email) {
		final Benutzer benutzer = new Benutzer();
		benutzer.setEmail(email);
		return benutzer;
	}

	@Override
	public String getDisplayName() {
		return name + ", " + vorname;
	}

	@Override
	public String getLoginName() {
		return getBenutzername();
	}

	public OneToManyHelper<Benutzer, BenutzerBerechtigung> berechtigungenHelper() {
		return new OneToManyHelper<>(
			() -> this,
			this::getBerechtigungen,
			BenutzerBerechtigung::new,
			BenutzerBerechtigung::setBenutzer
		);
	}

	@Override
	public String toString() {
		return super.toStringHelper()
			.add("email", email)
			.toString();
	}

	public static ID<Benutzer> toId(UUID id) {
		return new ID<>(id, Benutzer.class);
	}

	public boolean hasNonEmptyAndValidatedMobileNumber() {
		return StringUtils.isNotEmpty(mobiltelefon) && mobiltelefonValidiert;
	}

	public List<BenutzerRolle> getRoles() {
		return getBerechtigungen().stream()
			.map(BenutzerBerechtigung::getRolle)
			.collect(Collectors.toList());
	}
}
