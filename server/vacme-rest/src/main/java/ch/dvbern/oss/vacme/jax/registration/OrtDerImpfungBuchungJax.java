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

import java.util.UUID;

import ch.dvbern.oss.vacme.entities.terminbuchung.OdiNoFreieTermine;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.jax.base.AdresseJax;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrtDerImpfungBuchungJax {

	@Nullable
	private UUID id;

	@NonNull
	private String name;

	@NonNull
	private AdresseJax adresse;

	private boolean terminverwaltung;

	@Nullable
	private String externerBuchungslink;

	@Nullable
	private String kommentar;

	private boolean mobilerOrtDerImpfung;

	private boolean noFreieTermine1;

	private boolean noFreieTermine2;

	private boolean noFreieTermineN;

	private boolean booster;

	private boolean deaktiviert;

	private boolean oeffentlich;

	private LatLngJax latLng;

	@Nullable
	private Double distanceToReg; // not used on server but calculated on client

	@NonNull
	private OrtDerImpfungTyp typ;

	@NonNull
	public static OrtDerImpfungBuchungJax from(@NonNull OrtDerImpfung ortDerImpfung, @NonNull OdiNoFreieTermine nextFreieTermine) {
		return new OrtDerImpfungBuchungJax(
			ortDerImpfung.getId(),
			ortDerImpfung.getName(),
			AdresseJax.from(ortDerImpfung.getAdresse()),
			ortDerImpfung.isTerminverwaltung(),
			ortDerImpfung.getExternerBuchungslink(),
			ortDerImpfung.getKommentar(),
			ortDerImpfung.isMobilerOrtDerImpfung(),
			nextFreieTermine.isNoFreieTermine1(),
			nextFreieTermine.isNoFreieTermine2(),
			nextFreieTermine.isNoFreieTermineN(),
			ortDerImpfung.isBooster(),
			ortDerImpfung.isDeaktiviert(),
			ortDerImpfung.isOeffentlich(),
			new LatLngJax(ortDerImpfung.getLat(), ortDerImpfung.getLng()),
			null,
			ortDerImpfung.getTyp()
		);
	}

}
