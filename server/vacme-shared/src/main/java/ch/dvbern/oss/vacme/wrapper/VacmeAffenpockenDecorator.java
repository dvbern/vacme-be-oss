/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.wrapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import ch.dvbern.oss.vacme.entities.base.ImpfInfo;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.shared.util.Constants;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class VacmeAffenpockenDecorator implements VacmeKrankheitDecorator {

	@Override
	public @NonNull ImpfdossierStatus getStartStatusImpfdossier() {
		return ImpfdossierStatus.IMMUNISIERT;
	}

	@Override
	public @NonNull KrankheitIdentifier getKrankheitIdentifier() {
		return KrankheitIdentifier.AFFENPOCKEN;
	}

	@Nullable
	private VollstaendigerImpfschutzTyp calculateVollstaendigerImpfschutzTyp(
		@NonNull ImpfinformationDto infos,
		@Nullable Boolean immunsupprimiert
	) {
		ExternesZertifikat ez = infos.getExternesZertifikat();
		if (ez != null) {
			switch (ez.getMissingForGrundimmunisiert(infos.getKrankheitIdentifier())) {
			case BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG:
				if (hasMinCntOfVacmeImpfungen(infos, 2)) {
					return VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_PLUS_VACME;
				}
				return null;
			case BRAUCHT_1_IMPFUNG:
				if (Boolean.TRUE.equals(immunsupprimiert) && isUnbekanntePockenImpfungInKindheit(ez)) {
					if (hasMinCntOfVacmeImpfungen(infos, 2)) {
						return VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_PLUS_VACME;
					}
					return null;
				} else {
					if (hasMinCntOfVacmeImpfungen(infos, 1)) {
						return VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_PLUS_VACME;
					}
					return null;
				}
			case BRAUCHT_0_IMPFUNGEN: // Ist gelogen fuer immunsupprimierte...
				if (Boolean.TRUE.equals(immunsupprimiert) && isUnbekanntePockenImpfungInKindheit(ez)) {
					if (hasMinCntOfVacmeImpfungen(infos, 1)) {
						return VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXT_PLUS_VACME;
					}
					return null;
				}
				return VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXTERNESZERTIFIKAT;
			}
		}
		// ohne EZ hat man sicher keinen unbekannten Impfstoff in Kindheit, -> 2 mal impfen reicht immer
		if (hasMinCntOfVacmeImpfungen(infos, 2)) {
			return VollstaendigerImpfschutzTyp.VOLLSTAENDIG_VACME;
		}
		return null;
	}

	/**
	 * Checks that at les the minCount of Impfungen exis in vacme itself (not ExternesZertifikat)
	 */
	private boolean hasMinCntOfVacmeImpfungen(@NonNull ImpfinformationDto impfinformationDto, int minCount) {
		List<Impfung> impfungenInVacme = impfinformationDto.getBoosterImpfungen();
		impfungenInVacme = impfungenInVacme == null ? Collections.emptyList() : impfungenInVacme;
		return impfungenInVacme.size() >= minCount;
	}

	private boolean isUnbekanntePockenImpfungInKindheit(@NonNull ImpfInfo impfInfo) {
		return impfInfo.getImpfstoff()
			.getId()
			.equals(Constants.UNBEKANNTE_POCKENIMPFUNG_IN_KINDHEIT_UUID);
	}


	@Override
	public void setStatusToImmunisiertWithExternZertifikat(
		@NonNull ImpfinformationDto infos,
		@NonNull ExternesZertifikat externesZertifikat,
		@Nullable Boolean immunsupprimiert
	) {
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.IMMUNISIERT);
		recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(infos, immunsupprimiert);
		if (externesZertifikat.getLetzteImpfungDate() != null) {
			infos.getImpfdossier().setTimestampZuletztAbgeschlossen(externesZertifikat.getLetzteImpfungDate().atStartOfDay());
		}
	}

	@Override
	public void recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(
		@NonNull ImpfinformationDto infos,
		@Nullable Boolean immunsupprimiert
	) {
		final VollstaendigerImpfschutzTyp vollstaendigerImpfschutzTyp =
			calculateVollstaendigerImpfschutzTyp(infos, immunsupprimiert);
		infos.getImpfdossier().setVollstaendigerImpfschutzTyp(vollstaendigerImpfschutzTyp);

		if (vollstaendigerImpfschutzTyp != null) {
			infos.getImpfdossier().setTimestampZuletztAbgeschlossen(LocalDateTime.now());
		} else {
			infos.getImpfdossier().setTimestampZuletztAbgeschlossen(null);
		}
	}
}
