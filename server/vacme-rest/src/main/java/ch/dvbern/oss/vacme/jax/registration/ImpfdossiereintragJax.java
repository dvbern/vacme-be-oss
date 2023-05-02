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

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.impfen.Impfdossiereintrag;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.impfen.ImpfungkontrolleTermin;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
public class ImpfdossiereintragJax {

	@NotNull
	@NonNull
	private Integer impffolgeNr;
	private ImpfterminJax impftermin;
	private ImpfkontrolleTerminJax impfkontrolleTermin;
	private ImpfungJax impfung;

	public ImpfdossiereintragJax(@NonNull Impfdossiereintrag eintrag, @Nullable Impfung impfung) {
		this(eintrag);
		if (impfung != null) {
			this.impfung = ImpfungJax.from(impfung, eintrag.getImpffolgeNr());
		}
	}

	public ImpfdossiereintragJax(@NonNull Impfdossiereintrag eintrag) {
		this.impffolgeNr = eintrag.getImpffolgeNr();
		this.impftermin = eintrag.getImpftermin() != null ? new ImpfterminJax(eintrag.getImpftermin()) : null;
		ImpfungkontrolleTermin impfungkontrolleTermin = eintrag.getImpfungkontrolleTermin();
		if (impfungkontrolleTermin != null) {
			this.impfkontrolleTermin = new ImpfkontrolleTerminJax(
				impfungkontrolleTermin.getBemerkung(),
				impfungkontrolleTermin.isIdentitaetGeprueft(),
				impfungkontrolleTermin.getSelbstzahlende()
			);
		}
	}
}
