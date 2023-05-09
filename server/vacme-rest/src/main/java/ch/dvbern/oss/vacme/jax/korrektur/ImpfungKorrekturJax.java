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

package ch.dvbern.oss.vacme.jax.korrektur;

import java.math.BigDecimal;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.impfen.Impfdossier;
import ch.dvbern.oss.vacme.entities.impfen.Impffolge;
import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import ch.dvbern.oss.vacme.util.ImpfinformationenUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ImpfungKorrekturJax {

	@NonNull
	@Schema(required = true)
	private Impffolge impffolge;

	@NonNull
	@Schema(required = true)
	private UUID impfstoff;

	@NonNull
	@Schema(required = true)
	private String lot;

	@NonNull
	@Schema(required = true)
	private BigDecimal menge;

	@Nullable
	private Integer impffolgeNr;

	@NonNull
	@Schema(required = true)
	private KrankheitIdentifier krankheitIdentifier;


	public static boolean needsNewZertifikat(
		@NonNull Impfdossier impfdossier,
		@NonNull Impffolge impffolge,
		@NonNull Impfstoff impfstoffFalschErfasst,
		@NonNull Impfstoff impfstoffKorrigiert,
		@Nullable ExternesZertifikat externesZertifikat
	) {
		if (!impfdossier.getKrankheitIdentifier().isSupportsZertifikat()) {
			return false;	// We don't need a new one, if we don't support it.
		}
		// COVID-Zertifikat beinhaltet immer nur die letzte Impfung und wir muessen nur anpassen wenn impfstoff gewechselt hat
		boolean impfstoffHatGewechselt = impfstoffHatGewechselt(impfstoffFalschErfasst, impfstoffKorrigiert);
		if (!impfstoffHatGewechselt) {
			return false;
		}

		// Das Zertifikat muss neu erstellt werden, wenn der Impfschutz vollstaendig war, und der
		// Impfstoff geaendert wurde (Lot und Menge sind nicht im Zertifikat)
		if (impfdossier.abgeschlossenMitVollstaendigemImpfschutz()) {
			// Wenn es die erste Impfung ist, und diese relevant ist, braucht es ein neues Zertifikat
			if (isErsteImpfung(impffolge)) {
				if (impfdossier.abgeschlossenMitCorona()) {
					return true;
				}
				// Bei mehr als einer Impfdosis  ist die Erste Impfung egal
				boolean needsZweitimpfung = !ImpfinformationenUtil.willBeGrundimmunisiertAfterErstimpfungImpfstoff(impfstoffKorrigiert, externesZertifikat);
				if (needsZweitimpfung) {
					return false;
				}
			}

			// Booster braucht immer ein neues Zertifikat
			if (impffolge == Impffolge.BOOSTER_IMPFUNG) {
				return true;
			}

			// Normalfall: Er hatte vollstaendigen Impfschutz, und der neue Impfstoff braucht nicht mehr Dosen als der
			// vorherige
			boolean neuerImpfstoffBrauchtWenigerOderGleichVieleDosen =
				impfstoffKorrigiert.getAnzahlDosenBenoetigt() <= impfstoffFalschErfasst.getAnzahlDosenBenoetigt();
			return neuerImpfstoffBrauchtWenigerOderGleichVieleDosen;
		}

		// es kann sein dass vorher kein vollstaendiger Impfschutz vorhanden war, jetzt aber einen Impfstoff hat wo er ihn erreicht
		if (impfstoffKorrigiert.getAnzahlDosenBenoetigt() == 1 && impffolge == Impffolge.ERSTE_IMPFUNG) {
			return true;
		}
		if (impfstoffKorrigiert.getAnzahlDosenBenoetigt() == 2 && impffolge == Impffolge.ZWEITE_IMPFUNG) {
			return true;
		}

		return false;
	}

	public static boolean needToRevoke(
		@NonNull Impfdossier impfdossier,
		@NonNull Impffolge impffolge,
		@NonNull Impfstoff impfstoffFalschErfasst,
		@NonNull Impfstoff impfstoffKorrigiert
	) {
		if (!impfdossier.getKrankheitIdentifier().isSupportsZertifikat()) {
			return false;	// We don't need to revoke, if we don't support it.
		}
		boolean impfstoffHatGewechselt = impfstoffHatGewechselt(impfstoffFalschErfasst, impfstoffKorrigiert);
		if (!impfstoffHatGewechselt) {
			return false;
		}
		// COVID-Zertifikat beinhaltet immer nur die letzte Impfung
		if (impfdossier.abgeschlossenMitVollstaendigemImpfschutz()) {
			// Wenn es die erste Impfung ist, und diese relevant ist, muss dieses revoziert werden
			if(isErsteImpfung(impffolge)) {
				if (impfdossier.abgeschlossenMitCorona()) {
					return true;
				}
				// wir wechseln von einem 2 Dosen Impfstoff auf einen anderen 2 Dosen Impfstoff und die erste Impfung ist daher nicht relevant
				if (impfstoffKorrigiert.getAnzahlDosenBenoetigt() > 1 && impfstoffFalschErfasst.getAnzahlDosenBenoetigt() > 1) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private static boolean impfstoffHatGewechselt(
		@NonNull Impfstoff impfstoffFalschErfasst,
		@NonNull Impfstoff impfstoffKorrigiert
	) {
		return !impfstoffFalschErfasst.getId().equals(impfstoffKorrigiert.getId());
	}

	private static boolean isErsteImpfung(@NonNull Impffolge folge) {
		return Impffolge.ERSTE_IMPFUNG == folge;
	}
}
