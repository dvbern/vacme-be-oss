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

package ch.dvbern.oss.vacme.jax.tracing;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Geschlecht;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TracingRegistrierungJax {

	@NonNull @NotNull @NotEmpty
	@JsonProperty
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(required = true, maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "Name der Parson")
	private String name;

	@NonNull @NotNull @NotEmpty
	@JsonProperty
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(required = true, maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "Vorname der Person")
	private String vorname;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, implementation = LocalDate.class, description = "Geburtsdatum im Format: YYYY-MM-DD")
	private LocalDate geburtsdatum;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true,  maxLength = DBConst.DB_ENUM_LENGTH, description = "Bei unbekanntem Geschlecht ist der Wert UNBEKANNT zu waehlen")
	private Geschlecht geschlecht;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, description = "Adresse der Person")
	@Valid
	private TracingAdresseJax adresse;

	@Nullable
	@JsonProperty
	@Size(max = DBConst.DB_PHONE_LENGTH)
	@Schema(maxLength = DBConst.DB_PHONE_LENGTH, description = "Telefon / Mobil-Nummer der Person")
	private String telefon;

	@Nullable
	@JsonProperty
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "E-Mail Adresse der Person")
	private String email;

	@JsonProperty
	@Schema(required = true,  description = "Vollstaendiger Impfschutz gewaehrleistet")
	private boolean vollstaendigerImpfschutz;

	@NonNull @NotNull @NotEmpty
	@JsonProperty
	@Size(max = 6)
	@Schema(required = true, maxLength = 6, description = "VacMe Impf-Code der Person")
	private String registrierungsnummer;

	@NonNull @NotNull @NotEmpty
	@JsonProperty
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(required = true, maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "Krankenkassen Kartennummer")
	private String krankenkassennummer;

	@Nullable
	@JsonProperty
	@Size(max = DBConst.DB_ENUM_LENGTH)
	@Schema(maxLength = DBConst.DB_ENUM_LENGTH, description = "Certificat UVCI")
	private String certificatUVCI;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, minItems = 1, maxItems = 2, description = "Liste der getaetigten Impfungen. Diese kann 1 oder 2 beinhalten")
	@Size(min = 1, max = 2)
	@Valid
	private List<TracingImpfungJax> impfungen;

	@JsonIgnore
	public static TracingRegistrierungJax from(
		@NonNull Registrierung registrierung,
		@Nullable Impfung impfung1,
		@Nullable Impfung impfung2,
		@Nullable List<Impfung> boosterImpfungen,
		@Nullable Zertifikat zertifikat,
		@Nullable VollstaendigerImpfschutzTyp vollstaendigerImpfschutzTyp
	) {
		TracingRegistrierungJax registrierungJax = new TracingRegistrierungJax();
		registrierungJax.setName(registrierung.getName());
		registrierungJax.setVorname(registrierung.getVorname());
		registrierungJax.setGeburtsdatum(registrierung.getGeburtsdatum());
		registrierungJax.setGeschlecht(registrierung.getGeschlecht());
		registrierungJax.setAdresse(TracingAdresseJax.from(registrierung.getAdresse()));
		registrierungJax.setTelefon(registrierung.getTelefon());
		registrierungJax.setEmail(registrierung.getMail());
		registrierungJax.setVollstaendigerImpfschutz(vollstaendigerImpfschutzTyp != null);
		registrierungJax.setRegistrierungsnummer(registrierung.getRegistrierungsnummer());
		registrierungJax.setKrankenkassennummer(registrierung.getKrankenkasseKartenNr());
		registrierungJax.setCertificatUVCI(zertifikat != null ? zertifikat.getUvci() : null);
		List<TracingImpfungJax> impfungJaxList = new ArrayList<>();
		if (impfung1 != null) {
			impfungJaxList.add(TracingImpfungJax.from(impfung1));
		}
		if (impfung2 != null) {
			impfungJaxList.add(TracingImpfungJax.from(impfung2));
		}
		if (CollectionUtils.isNotEmpty(boosterImpfungen)) {
			List<TracingImpfungJax> mappedImpfungen =
				boosterImpfungen.stream().map(TracingImpfungJax::from).collect(Collectors.toList());
			impfungJaxList.addAll(mappedImpfungen);
		}
		registrierungJax.setImpfungen(impfungJaxList);
		return registrierungJax;
	}
}
