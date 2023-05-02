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

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsart;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsort;
import ch.dvbern.oss.vacme.entities.impfen.Verarbreichungsseite;
import ch.dvbern.oss.vacme.enums.ImpfstoffNamesJax;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImpfungMigrationJax {

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, implementation = LocalDate.class, description = "Datum an dem die Person geimpft wurde")
	private LocalDate impfdatum;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = 20,  description = "Impffolge ERSTE_IMPFUNG oder ZWEITE_IMPFUNG")
	private Impffolge impffolge;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_ENUM_LENGTH, description = "Auflistung von Typen des Impfortes")
	private OrtDerImpfungTyp ortDerImpfungTyp;

	@Nullable
	@JsonProperty
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "GLN-Nummer des Impfortes")
	private String impfortGLN;

	@Nullable
	@JsonProperty
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "GLN-Nummer der verantwortlichen Person")
	private String verantwortlicherPersonGLN;

	@Nullable
	@JsonProperty
	@Size(max = DBConst.DB_DEFAULT_MAX_LENGTH)
	@Schema(maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "GLN-Nummer der durchfuehrende Person")
	private String durchfuehrendPersonGLN;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_ENUM_LENGTH,  description = "Auflistung der moeglichen verabreichten Impfstoffe")
	private ImpfstoffNamesJax impfstoff;

	@NonNull @NotNull @NotEmpty
	@JsonProperty
	@Size(max = DBConst.DB_VMDL_SCHNITTSTELLE_LENGTH)
	@Schema(required = true, maxLength = DBConst.DB_VMDL_SCHNITTSTELLE_LENGTH, description = "Lot-Nummer des verabreichten Impfstoffes")
	private String lot;

	@NonNull @NotNull
	@JsonProperty
	@Min(0)
	@Max(3)
	@Schema(required = true, minimum = "0", maximum = "3", description = "Menge des verabreichten Impfstoffes in Milliliter(ml)")
	private BigDecimal menge;

	@JsonProperty
	@Schema(required = true, description = "Hatte die Person Fieber")
	private boolean fieber;

	@JsonProperty
	@Schema(required = true, description = "Sind bei der Person neue Krankheiten erschienen")
	private boolean neueKrankheit;

	@JsonProperty
	@Schema(required = true, description = "Hat die Person ihre Einwilligung fuer die Impfung gegeben")
	private boolean einwilligungImpfung;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_ENUM_LENGTH, description = "Auflistung der Arten der Impfung")
	private Verarbreichungsart verarbreichungsart;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_ENUM_LENGTH, description = "Auflistung der Orte der Impfung")
	private Verarbreichungsort verarbreichungsort;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_ENUM_LENGTH,  description = "Auflistung der Seiten der Impfung (LINKS, RECHTS)")
	private Verarbreichungsseite verarbreichungsseite;

	@JsonProperty
	@Schema(required = true, description = "Wurde die Identitaet der Person geprueft")
	private boolean identitaetGeprueft;

	@JsonProperty
	@Schema(description = "Hat diese Impfung Systemextern stattgefunden (eg. erfolgte im Ausland und wird in Vacme nur nacherfasst)")
	private boolean extern;
}
