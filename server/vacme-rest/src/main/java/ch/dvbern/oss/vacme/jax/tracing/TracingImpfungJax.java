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

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.entities.util.DBConst;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TracingImpfungJax {

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

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_DEFAULT_MAX_LENGTH, description = "Name des Impfortes")
	private String impfortName;

	@NonNull @NotNull
	@JsonProperty
	@Schema(required = true, maxLength = DBConst.DB_DEFAULT_MAX_LENGTH,  description = "Auflistung der moeglichen verabreichten Impfstoffe")
	private String impfstoff;

	@JsonIgnore
	public static TracingImpfungJax from(Impfung impfung) {
		TracingImpfungJax impfungJax = new TracingImpfungJax();
		impfungJax.setImpfdatum(impfung.getTimestampImpfung().toLocalDate());
		impfungJax.setImpffolge(impfung.getTermin().getImpffolge());
		impfungJax.setOrtDerImpfungTyp(impfung.getTermin().getImpfslot().getOrtDerImpfung().getTyp());
		impfungJax.setImpfortGLN(impfung.getTermin().getImpfslot().getOrtDerImpfung().getGlnNummer());
		impfungJax.setImpfortName(impfung.getTermin().getImpfslot().getOrtDerImpfung().getName());
		impfungJax.setImpfstoff(impfung.getImpfstoff().getName());
		return impfungJax;
	}
}
