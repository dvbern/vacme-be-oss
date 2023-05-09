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

import ch.dvbern.oss.vacme.entities.externeimpfinfos.ExternesZertifikat;
import ch.dvbern.oss.vacme.entities.externeimpfinfos.MissingForGrundimmunisiert;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternGeimpftJax {

	private boolean externGeimpft;

	private boolean letzteImpfungDateUnknown;

	@Nullable
	private LocalDate letzteImpfungDate;

	@Nullable
	private ImpfstoffJax impfstoff;

	@Nullable
	private Integer anzahlImpfungen;

	@Nullable
	private Boolean genesen;

	@Nullable
	private LocalDate positivGetestetDatum;

	@Nullable
	private Boolean grundimmunisiert;

	@Nullable
	private Boolean kontrolliert;

	@Nullable
	private MissingForGrundimmunisiert missingForGrundimmunisiertBeforeDecision;

	@Nullable
	private Boolean trotzdemVollstaendigGrundimmunisieren;

	@Nullable
	private MissingForGrundimmunisiert missingForGrundimmunisiertAfterDecision;

	@Schema(required = true)
	private boolean schnellschema;

	@Nullable
	public static ExternGeimpftJax from(@Nullable ExternesZertifikat externeImpfinfo) {
		if (externeImpfinfo == null) {
			return null;
		}
		ExternGeimpftJax jax = new ExternGeimpftJax();
		jax.externGeimpft = true;
		jax.letzteImpfungDate = externeImpfinfo.getLetzteImpfungDate();
		jax.letzteImpfungDateUnknown = externeImpfinfo.isLetzteImpfungDateUnknown();
		jax.impfstoff = ImpfstoffJax.from(externeImpfinfo.getImpfstoff());
		jax.genesen = externeImpfinfo.isGenesen();
		jax.positivGetestetDatum = externeImpfinfo.getPositivGetestetDatum();
		jax.anzahlImpfungen = externeImpfinfo.getAnzahlImpfungen();
		KrankheitIdentifier krankheitIdentifier = externeImpfinfo.getImpfdossier().getKrankheitIdentifier();
		jax.grundimmunisiert = externeImpfinfo.isGrundimmunisiert(krankheitIdentifier);
		jax.kontrolliert = externeImpfinfo.isKontrolliert();
		jax.missingForGrundimmunisiertBeforeDecision = externeImpfinfo.getMissingForGrundimmunisiertBeforeDecision(
			krankheitIdentifier);
		jax.trotzdemVollstaendigGrundimmunisieren = externeImpfinfo.getTrotzdemVollstaendigGrundimmunisieren();
		jax.missingForGrundimmunisiertAfterDecision =
			externeImpfinfo.getMissingForGrundimmunisiert(krankheitIdentifier);
		jax.schnellschema = externeImpfinfo.getImpfdossier().isSchnellschema();
		return jax;
	}
}
