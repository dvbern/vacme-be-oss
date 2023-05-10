/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.wrapper;

import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import org.apache.commons.lang3.NotImplementedException;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class VacmeDecoratorFactory {

	private VacmeDecoratorFactory() {
	}

	public static VacmeKrankheitDecorator getDecorator(
		@NonNull KrankheitIdentifier krankheitIdentifier
	) {
		switch (krankheitIdentifier) {
		case COVID:
			return new VacmeCovidDecorator();
		case AFFENPOCKEN:
			return new VacmeAffenpockenDecorator();
		case FSME:
			return new VacmeFSMEDecorator();
		default:
			throw new NotImplementedException("Kein VacmeDecorator gefunden fuer Krankheit "
				+ krankheitIdentifier);
		}
	}
}
