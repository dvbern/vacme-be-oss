/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.jax;

import java.time.LocalDateTime;
import java.util.Objects;

import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.terminbuchung.Impftermin;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * DTO which is mainly used by the ApplicationHealthResource to transfer data that is used in the health check
 */
@Getter
@Setter
public class RegistrierungTermineImpfungJax {

	private @NonNull String registrierungsnummer;

	private @Nullable LocalDateTime termin1Datum;
	private @Nullable OrtDerImpfungDisplayNameJax termin1Odi;
	private @Nullable Boolean termin1Gebucht;

	private @Nullable LocalDateTime termin2Datum;
	private @Nullable OrtDerImpfungDisplayNameJax termin2Odi;
	private @Nullable Boolean termin2Gebucht;

	private @Nullable LocalDateTime impfung1Datum;
	private @Nullable String impfung1Impfstoff;

	private @Nullable LocalDateTime impfung2Datum;
	private @Nullable String impfung2Impfstoff;

	private @Nullable String info;


	@QueryProjection
	public RegistrierungTermineImpfungJax(
		@NonNull Registrierung registrierung,
		@Nullable Impftermin termin1,
		@Nullable Impftermin termin2,
		@Nullable Impfung impfung1,
		@Nullable Impfung impfung2
	) {
		this.readAttributesOfRegistration(registrierung);
		this.readAttributesOfTermin1(termin1);
		this.readAttributesOfTermin2(termin2);
		this.readAttributesOfImpfung1(impfung1);
		this.readAttributesOfImpfung2(impfung2);
	}

	@QueryProjection
	public RegistrierungTermineImpfungJax(
		@NonNull Registrierung registrierung,
		@Nullable Impftermin termin1,
		@Nullable Impftermin termin2
	) {
		this(registrierung, termin1, termin2, null, null);
	}

	@QueryProjection
	public RegistrierungTermineImpfungJax(
		@NonNull Registrierung registrierung,
		@NonNull Impftermin termin
	) {
		this.readAttributesOfRegistration(registrierung);
		if (termin.getImpffolge() == Impffolge.ERSTE_IMPFUNG) {
			this.readAttributesOfTermin1(termin);
		} else {
			this.readAttributesOfTermin2(termin);
		}
	}

	@QueryProjection
	public RegistrierungTermineImpfungJax(@NonNull Impftermin termin) {
		if (termin.getImpffolge() == Impffolge.ERSTE_IMPFUNG) {
			this.readAttributesOfTermin1(termin);
		} else {
			this.readAttributesOfTermin2(termin);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RegistrierungTermineImpfungJax)) {
			return false;
		}
		RegistrierungTermineImpfungJax that = (RegistrierungTermineImpfungJax) o;
		return registrierungsnummer.equals(that.registrierungsnummer);
	}

	@Override
	public int hashCode() {
		return Objects.hash(registrierungsnummer);
	}

	private void readAttributesOfRegistration(@NonNull Registrierung registrierung) {
		this.registrierungsnummer = registrierung.getRegistrierungsnummer();
	}

	private void readAttributesOfTermin1(@Nullable Impftermin termin1) {
		if (termin1 != null) {
			this.termin1Datum = termin1.getImpfslot().getZeitfenster().getVon();
			this.termin1Odi = new OrtDerImpfungDisplayNameJax(termin1.getImpfslot().getOrtDerImpfung());
			this.termin1Gebucht = termin1.isGebucht();
		}
	}

	private void readAttributesOfTermin2(@Nullable Impftermin termin2) {
		if (termin2 != null) {
			this.termin2Datum = termin2.getImpfslot().getZeitfenster().getVon();
			this.termin2Odi = new OrtDerImpfungDisplayNameJax(termin2.getImpfslot().getOrtDerImpfung());
			this.termin2Gebucht = termin2.isGebucht();
		}
	}

	private void readAttributesOfImpfung1(@Nullable Impfung impfung1) {
		if (impfung1 != null) {
			this.impfung1Datum = impfung1.getTimestampImpfung();
			this.impfung1Impfstoff = impfung1.getImpfstoff().getDisplayName();
		}
	}

	private void readAttributesOfImpfung2(@Nullable Impfung impfung2) {
		if (impfung2 != null) {
			this.impfung2Datum = impfung2.getTimestampImpfung();
			this.impfung2Impfstoff = impfung2.getImpfstoff().getDisplayName();
		}
	}
}
