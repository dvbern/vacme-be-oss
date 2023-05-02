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

package ch.dvbern.oss.vacme.jax.registration;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
/**
 * DTO mit minimalinformationen zu durchgefuehrten Impfungen
 */
public class ImpfungJax {

	@NonNull
	@Schema(required = true)
	private Impffolge impffolge;

	@NotNull
	private LocalDateTime timestampImpfung;

	@NotNull
	private String ortDerImpfung;

	@NonNull
	@Schema(required = true)
	private ImpfstoffJax impfstoff;

	@Nullable
	private String bemerkung;


	@NotNull
	private Integer impfungNr;

	@NotNull
	private boolean grundimmunisierung;

	@Nullable
	private Boolean schwanger;

	@NotNull
	private String durchfuehrendePerson;

	@NonNull
	@Schema(required = true)
	private boolean schnellschemaGesetztFuerImpfung;

	@NonNull
	public static ImpfungJax from(@NonNull Impfung impfung, @NonNull Integer nr) {
		return new ImpfungJax(
			impfung.getTermin().getImpffolge(),
			impfung.getTimestampImpfung(),
			impfung.getTermin().getImpfslot().getOrtDerImpfung().getName(),
			ImpfstoffJax.from(impfung.getImpfstoff()),
			impfung.getBemerkung(),
			nr,
			impfung.isGrundimmunisierung(),
			impfung.getSchwanger(),
			impfung.getBenutzerDurchfuehrend().getDisplayName(),
			impfung.isSchnellschemaGesetztFuerImpfung()
		);
	}
}
