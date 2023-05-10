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

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.entities.types.VollstaendigerImpfschutzTyp;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class VacmeCovidDecorator implements VacmeKrankheitDecorator {

	@Override
	public @NonNull ImpfdossierStatus getStartStatusImpfdossier() {
		return ImpfdossierStatus.NEU;
	}

	@Override
	public @NonNull KrankheitIdentifier getKrankheitIdentifier() {
		return KrankheitIdentifier.COVID;
	}

	@Override
	public void setStatusToImmunisiertAfterBooster(
		@NonNull ImpfinformationDto infos,
		@NonNull Impfung relevanteImpfung,
		@Nullable Boolean immunsupprimiert
	) {
		Registrierung reg = infos.getRegistrierung();
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.IMMUNISIERT);
		infos.getImpfdossier().setTimestampZuletztAbgeschlossen(LocalDateTime.now());
		// Wenn beim Boostern der vollstaendigeImpfschutz fehlt, ist etwas schiefgelaufen!
		Validate.notNull(infos.getImpfdossier().getVollstaendigerImpfschutzTyp());
		// Erst ganz am Schluss koennen wir ermitteln, ob ein Zertifikat erlaubt ist, sonst berechnen wir aufgrund
		// falscher Daten denn manche fuer die Zertifikatsentscheidung relevanten Felder werden erst gerade gesetzt!
		reg.setGenerateZertifikatTrueIfAllowed(infos, relevanteImpfung);
	}

	@Override
	public void setStatusToImmunisiertWithExternZertifikat(
		@NonNull ImpfinformationDto infos,
		@NonNull ExternesZertifikat externesZertifikat,
		@Nullable Boolean immunsupprimiert
	) {
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.IMMUNISIERT);
		infos.getImpfdossier().setVollstaendigerImpfschutzTyp(externesZertifikat.isGenesen()
			? VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXTERNESZERTIFIKAT_GENESEN
			: VollstaendigerImpfschutzTyp.VOLLSTAENDIG_EXTERNESZERTIFIKAT);
		if (externesZertifikat.getLetzteImpfungDate() != null) {
			infos.getImpfdossier().setTimestampZuletztAbgeschlossen(externesZertifikat.getLetzteImpfungDate().atStartOfDay());
		}
	}

	@Override
	public void recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(
		@NonNull ImpfinformationDto infos,
		@Nullable Boolean immunsupprimiert
	) {
		// Immunsupprimiert hat bei COVID keinen Einfluss auf die Grundimmunisierung
	}
}
