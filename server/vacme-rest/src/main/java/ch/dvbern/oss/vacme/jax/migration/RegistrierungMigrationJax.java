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

package ch.dvbern.oss.vacme.jax.migration;

import ch.dvbern.oss.vacme.entities.impfen.AuslandArt;
import ch.dvbern.oss.vacme.entities.impfen.Krankenkasse;
import ch.dvbern.oss.vacme.entities.registration.*;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistrierungMigrationJax {

	@NonNull @NotNull @NotEmpty
	@JsonProperty
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(required = true, maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "ExternalID aus dem Client System. Wird benutzt um Aenderungen in bereits gesendete Datensaetze vornehmen zu können")
	private String externalId;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_ENUM_LENGTH, description = "Ampel aus der Selbsteinschaetzung. <br/> * Gruen ist die Person berechtigt zur Impfung.<br/> * Orange is die Berechtigung mit Vorweisung eines Ärzte-Attests.<br/> * Rot ist die Impfung nicht erlaubt.")
	private AmpelColor ampel;

	@JsonProperty
	@Schema(required = true, description = "Sind die gesendeten Daten anonymisiert")
	private boolean anonymisiert;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true,  maxLength = DBConst.DB_ENUM_LENGTH, description = "Bei unbekanntem Geschlecht ist der Wert UNBEKANNT zu waehlen")
	private Geschlecht geschlecht;

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

	@JsonProperty
	@Schema(description = "Ist die Person verstorben?")
	private Boolean verstorben;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, description = "Adresse der Person")
	@Valid
	private AdresseMigrationJax adresse;

	@NonNull @NotNull
	@JsonProperty
	@Size(max = DBConst.DB_PHONE_LENGTH)
	@Schema(required = true, maxLength = DBConst.DB_PHONE_LENGTH, description = "Telefon / Mobil-Nummer der Person")
	private String telefon;

	@Nullable
	@JsonProperty
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "E-Mail Adresse der Person")
	private String email;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true,  maxLength = DBConst.DB_ENUM_LENGTH, description = "Krankenkasse der Person. <br/>Liste aus: https://www.bag.admin.ch/bag/de/home/versicherungen/krankenversicherung/krankenversicherung-versicherer-aufsicht/verzeichnisse-krankenundrueckversicherer.html <br/>Plus Ausland und EDA")
	private Krankenkasse krankenkasse;

	@NonNull @NotNull @NotEmpty
	@JsonProperty
	@Size(min = 20, max = 20)
	@Schema(required = true, minLength = 20, maxLength = 20, description = "Krankenkassen Kartennummer im Format: 80756<5 stellige BAG Nummer><10 stellige Laufnummer>. Fuer auslaendische Krankenkassen und EDA ist die Kartennummer 00000000000000000000")
	private String krankenkassennummer;

	@Nullable
	@JsonProperty
	@Schema(description = "Für Personen ohne Krankenkasse: Auslandschweizer/Grenzgänger oder andere")
	private AuslandArt auslandArt;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_ENUM_LENGTH, description = "Einschaetzung des chronischen Krankheitszustandes der Person")
	private ChronischeKrankheiten chronischeKrankheiten;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_ENUM_LENGTH, description = "Berufliche Taetigkeit der Person")
	private BeruflicheTaetigkeit beruf;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_ENUM_LENGTH, description = "Lebensumstaende der Person")
	private Lebensumstaende lebensumstaende;

	@JsonProperty
	@Schema(required = true, description = "Erlaubt die Person den Ableich seiner Daten mit der MyCOVIDvac Schnittstelle")
	private boolean abgleichElektronischerImpfausweis;

	@JsonProperty
	@Schema(description = "Erlaubt es die Registrierung abzuschliessen und das Flag 'vollstaendigerImpfschutz' auf true zu setzen")
	private Boolean vollstaendigerImpfschutz;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, minItems = 1, maxItems = 2, description = "Liste der getaetigten Impfungen. Diese kann 1 oder 2 beinhalten")
	@Size(min = 1, max = 2)
	@Valid
	private List<ImpfungMigrationJax> impfungen;
}
