/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.jax;

import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.jax.base.AbstractUUIDEntityJax;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
public class OrtDerImpfungDisplayNameJax extends AbstractUUIDEntityJax {

	private String name;
	private boolean terminverwaltung;
	private boolean mobilerOrtDerImpfung;
	private boolean booster;
	private boolean deaktiviert;
	private boolean oeffentlich;


	@Nullable
	private String glnNummer;

	public OrtDerImpfungDisplayNameJax(@NonNull OrtDerImpfung ortDerImpfungEntity) {
		super(ortDerImpfungEntity);
		this.name = ortDerImpfungEntity.getName();
		this.terminverwaltung = ortDerImpfungEntity.isTerminverwaltung();
		this.mobilerOrtDerImpfung = ortDerImpfungEntity.isMobilerOrtDerImpfung();
		this.glnNummer = ortDerImpfungEntity.getGlnNummer();
		this.booster = ortDerImpfungEntity.isBooster();
		this.deaktiviert = ortDerImpfungEntity.isDeaktiviert();
		this.oeffentlich = ortDerImpfungEntity.isOeffentlich();
	}
}
