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

package ch.dvbern.oss.vacme.entities.onboarding;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungFile;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

/**
 * Entity used to store information about the generated Onboarding process where an "Analog" Registration is
 * linked to a Benutzer
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited
@Table(
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_Onboarding_code", columnNames = "code"),
		//		@UniqueConstraint(name = "UC_Onboarding_onboardingPDF", columnNames = "onboardingPdf_id"),
	}
)
public class Onboarding extends AbstractUUIDEntity<Onboarding> {

	private static final long serialVersionUID = -8720585555347040138L;

	@NotNull
	@NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Onboarding_registrierung"), nullable = false, updatable = false)
	private Registrierung registrierung;

	@NotNull
	@NonNull
	@Column(updatable = true)
	private Boolean used = false;

		@Nullable
		@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Onboarding_onboardingPdf"), updatable = false)
	@Audited(targetAuditMode = NOT_AUDITED)
	private RegistrierungFile onboardingPdf;
	@NotEmpty
	@NonNull
	@Column(nullable = false, updatable = false, length = DBConst.DB_ENUM_LENGTH)
	@Size(max = DBConst.DB_ENUM_LENGTH)
	private String code; // Unique code

	@Nullable
	@Column(nullable = true, updatable = true, length = DBConst.DB_ENUM_LENGTH)
	@Size(max = DBConst.DB_UUID_LENGTH)
	private String onboardingTempToken; // Unique code to reidentify browser session after registration

	@Column(nullable = true, updatable = true, length = DBConst.DB_DEFAULT_MAX_LENGTH)
	private LocalDateTime onboardingTempTokenCreationTime;

	@Column(nullable = true, updatable = true)
	@Min(0)
	private long numOfTries; // counts how often someone tried to use this code, used for brute force check

	public Onboarding(@NonNull Registrierung registrierung) {
		this.registrierung = registrierung;
		this.setNumOfTries(0);
		this.setUsed(false);
	}

	/*
	the temporary token can be generated more than once. If a user starts the onboarding and does not finish, he can try again later.
	 */
	public void generateOnboardingTempToken() {
		this.onboardingTempTokenCreationTime = LocalDateTime.now();
		this.onboardingTempToken = UUID.randomUUID().toString();

	}

	public void increaseNumOfTries() {
		numOfTries += 1;
	}
}
