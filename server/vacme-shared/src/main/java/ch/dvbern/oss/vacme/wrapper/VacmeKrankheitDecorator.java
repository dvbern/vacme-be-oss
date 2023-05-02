/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of
 *  the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.wrapper;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.MissingForGrundimmunisiert;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.impfen.Impfung;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.enums.ImpfdossierStatus;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface VacmeKrankheitDecorator {

	@NonNull
	ImpfdossierStatus getStartStatusImpfdossier();

	@NonNull
	KrankheitIdentifier getKrankheitIdentifier();

	default void setStatusToImmunisiertAfterBooster(
		@NonNull ImpfinformationDto infos,
		@NonNull Impfung relevanteImpfung,
		@Nullable Boolean immunsupprimiert){
		Validate.isTrue(infos.getKrankheitIdentifier() == getKrankheitIdentifier(),
			"KrankheitIdentifier must be "+ getKrankheitIdentifier());
		recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(infos, immunsupprimiert);
		infos.getImpfdossier().setDossierStatus(ImpfdossierStatus.IMMUNISIERT);
		// Auf zweite Impfung verzichten gibts es in dieser Krankheit nicht
		infos.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetZeit(null);
		infos.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setZweiteImpfungVerzichtetGrund(null);
		infos.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setGenesen(false);
		infos.getImpfdossier().getZweiteGrundimmunisierungVerzichtet().setPositivGetestetDatum(null);
		// Zertifikat ist nicht unterstuetzt
		relevanteImpfung.setGenerateZertifikat(false);
	}

	void setStatusToImmunisiertWithExternZertifikat(
		@NonNull ImpfinformationDto infos,
		@NonNull ExternesZertifikat externesZertifikat,
		@Nullable Boolean immunsupprimiert
	);

	void recalculateVollstaendigerImpfschutzTypBasedOnImmunsupprimiert(
		@NonNull ImpfinformationDto infos,
		@Nullable Boolean immunsupprimiert
	);

	@NonNull
	default MissingForGrundimmunisiert calculateMissingFuerGrundimmunisierung(
		@NonNull Impfstoff impfstoff,
		@Nullable Integer anzahlImpfungenImEZ,
		@Nullable Boolean genesen) {
		var impfempfehlungOptional = impfstoff.findImpfempfehlung(anzahlImpfungenImEZ, genesen);
		if (impfempfehlungOptional.isPresent()) {
			var impfempfehlung = impfempfehlungOptional.get();
			var anzahlMissing = impfempfehlung.getNotwendigFuerChGrundimmunisierung();

			if (anzahlMissing == impfstoff.getAnzahlDosenBenoetigt()) {
				return MissingForGrundimmunisiert.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG;
			}
			if (anzahlMissing == 0) {
				return MissingForGrundimmunisiert.BRAUCHT_0_IMPFUNGEN;
			}
			if (anzahlMissing == 1) {
				return MissingForGrundimmunisiert.BRAUCHT_1_IMPFUNG;
			}
		}
		return MissingForGrundimmunisiert.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG;
	}
}
