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

package ch.dvbern.oss.vacme.entities.zertifikat;

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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.base.AbstractUUIDEntity;
import ch.dvbern.oss.vacme.entities.covidcertificate.CovidCertificateCreateResponseDto;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.shared.util.MimeType;
import ch.dvbern.oss.vacme.util.VacmeFileUtil;
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
@NoArgsConstructor
@Table(
	uniqueConstraints = {
		@UniqueConstraint(name = "UC_Zertifikat_uvci", columnNames = "uvci"),
		@UniqueConstraint(name = "UC_Zertifikat_zertifikatPdf", columnNames = "zertifikatPdf_id"),
		@UniqueConstraint(name = "UC_Zertifikat_zertifikatQrCode", columnNames = "zertifikatQrCode_id"),
	}
)
public class Zertifikat extends AbstractUUIDEntity<Zertifikat> {

	private static final long serialVersionUID = -2854345385817970097L;

	@NotNull
	@NonNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Zertifikat_registrierung"), nullable = false, updatable = false)
	private Registrierung registrierung;

	// Ab Booster muessen wir wissen, zu welcher Impfung ein Zertifikat gehoert, da es evtl. mehrere
	// gleichzeitig gueltige gibt und wir beim Loeschen sonst nicht wissen, welches revoziert werden muss.
	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Zertifikat_impfung"), nullable = true, updatable = true)
	private Impfung impfung;

	@NotEmpty
	@NonNull
	@Column(nullable = false, updatable = false, length = DBConst.DB_LENGTH_ZERTIFIKAT_PAYLOAD)
	@Size(max = DBConst.DB_LENGTH_ZERTIFIKAT_PAYLOAD)
	private String payload;

	@NotEmpty
	@NonNull
	@Column(nullable = false, updatable = false, length = DBConst.DB_ZERTIFIZIERUNGS_TOKEN_MAX_LENGTH)
	@Size(max = DBConst.DB_ZERTIFIZIERUNGS_TOKEN_MAX_LENGTH)
	private String signature;

	@NotNull
	@NonNull
	@Column(updatable = true) // Dies ist das einzige Feld, welches updated werden darf
	private Boolean revoked = false;

	@NotNull
	@NonNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Zertifikat_zertifikatPdf"), nullable = false, updatable = false)
	private ZertifikatFile zertifikatPdf;

	@NotNull
	@NonNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Zertifikat_zertifikatQrCode"), nullable = false, updatable = false)
	private ZertifikatFile zertifikatQrCode;

	@NotEmpty
	@NonNull
	@Column(nullable = false, updatable = false, length = DBConst.DB_ENUM_LENGTH)
	@Size(max = DBConst.DB_ENUM_LENGTH)
	private String uvci; // Unique Vaccination Certificate Identifier

	public Zertifikat(@NonNull Registrierung registrierung) {
		this.registrierung = registrierung;
	}

	public void setCovidCertificateCreateResponseDto(@NonNull CovidCertificateCreateResponseDto vaccinationCert) {
		this.zertifikatPdf = VacmeFileUtil.createZertifikatFile(registrierung, MimeType.APPLICATION_PDF,  vaccinationCert.getPdf());
		this.zertifikatQrCode = VacmeFileUtil.createZertifikatFile(registrierung, MimeType.IMAGE_PNG, vaccinationCert.getQrCode());
		this.revoked = false;
		this.uvci = vaccinationCert.getUvci();
	}
}
