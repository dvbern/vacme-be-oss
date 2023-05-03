/*
 *
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

import ch.dvbern.oss.vacme.entities.terminbuchung.OdiNoFreieTermine;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.jax.OrtDerImpfungDisplayNameJax;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
public class OrtDerImpfungDisplayNameExtendedJax extends OrtDerImpfungDisplayNameJax {

	private OrtDerImpfungTyp typ;

	private boolean noFreieTermine1;
	private boolean noFreieTermine2;
	private boolean noFreieTermineN;

	@Nullable
	private LocalDateTime nextTermin1Date;

	@Nullable
	private LocalDateTime nextTermin2Date;

	@Nullable
	private LocalDateTime nextTerminNDate;

	@Nullable
	private LatLngJax latLng;

	@Nullable
	private Double distanceToReg; // not used on server but calculated on client

	private boolean impfungGegenBezahlung;

	public OrtDerImpfungDisplayNameExtendedJax(
		@NonNull OrtDerImpfung ortDerImpfungEntity,
		@NonNull OdiNoFreieTermine odiFreieTermine
	) {
		super(ortDerImpfungEntity);
		this.typ = ortDerImpfungEntity.getTyp();
		this.latLng = new LatLngJax(ortDerImpfungEntity.getLat(), ortDerImpfungEntity.getLng());
		this.impfungGegenBezahlung = ortDerImpfungEntity.isImpfungGegenBezahlung();
		this.noFreieTermine1 = odiFreieTermine.isNoFreieTermine1();
		this.noFreieTermine2 = odiFreieTermine.isNoFreieTermine2();
		this.noFreieTermineN = odiFreieTermine.isNoFreieTermineN();
	}
}
