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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.base.ZertifikatInfo;
import ch.dvbern.oss.vacme.entities.zertifikat.Zertifikat;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Getter
@Setter
public class ZertifikatJax implements ZertifikatInfo {

	private UUID id;

	// ImpfungId: damit man im GUI die Zertifikate direkt bei der Impfung anzeigen koennte.
	// Die Impfung selber anhaengen nuetzt nichts, weil dort drin nicht das steht, das im Zertifikat steht, falls sie korrigiert wurde
	private UUID impfungId;

	// Zertifikatinfos
	private boolean revoked;
	private LocalDateTime timestampZertifikatErstellt;
	private LocalDateTime timestampZertifikatRevoked;

	// Infos aus Payload
	private LocalDate vaccinationDate;
	private Integer totalNumberOfDoses;
	private Integer numberOfDoses;
	private ImpfstoffJax impfstoffJax;
	private String uvci;

	public ZertifikatJax(@NonNull Zertifikat zertifikat) {
		this.id = zertifikat.getId();
		this.revoked = zertifikat.getRevoked();
		this.uvci = zertifikat.getUvci();
		this.timestampZertifikatErstellt = zertifikat.getTimestampErstellt();
		if (Boolean.TRUE.equals(revoked)) {
			this.timestampZertifikatRevoked = zertifikat.getTimestampMutiert();
		}
		if (zertifikat.getImpfung() != null) {
			this.impfungId = zertifikat.getImpfung().getId();
		}
	}

	@Override
	public String toString() {
		return "ZertifikatJax{" +
			"impfungId=" + impfungId +
			", revoked=" + revoked +
			", timestampZertifikatErstellt=" + timestampZertifikatErstellt +
			", timestampZertifikatRevoked=" + timestampZertifikatRevoked +
			", vaccinationDate=" + vaccinationDate +
			", totalNumberOfDoses=" + totalNumberOfDoses +
			", numberOfDoses=" + numberOfDoses +
			", impfstoffJax=" + impfstoffJax +
			'}';
	}
}
