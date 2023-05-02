/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.jax.registration;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
public class ImpfdossiersOverviewJax {

	@NonNull
	@Schema(required = true)
	private String registrierungsnummer;

	@NonNull
	@Schema(required = true)
	private String name;

	@NonNull
	@Schema(required = true)
	private String vorname;

	@NonNull
	@Schema(required = true)
	private LocalDate geburtsdatum;

	@NonNull
	@Schema
	private Prioritaet prioritaet;

	@Nullable
	@Schema(required = false)
	private List<ImpfdossierSummaryJax> impfdossierSummaryList;

	public static ImpfdossiersOverviewJax of(
		@NonNull Registrierung registrierung,
		List<ImpfdossierSummaryJax> impfdossierSummaryList) {
		return new ImpfdossiersOverviewJax(
			registrierung.getRegistrierungsnummer(),
			registrierung.getName(),
			registrierung.getVorname(),
			registrierung.getGeburtsdatum(),
			registrierung.getPrioritaet(),
			impfdossierSummaryList
		);
	}

}
