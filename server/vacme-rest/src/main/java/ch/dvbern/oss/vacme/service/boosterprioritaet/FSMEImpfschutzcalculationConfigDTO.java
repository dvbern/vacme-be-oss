/*
 * Copyright (C) 2022 DV Bern AG, Switzerland This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service.boosterprioritaet;

import java.time.temporal.ChronoUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FSMEImpfschutzcalculationConfigDTO {

	private int minAge;

	private int freigabeOffsetKonventionellWithImpfschutz;
	private ChronoUnit freigabeOffsetKonventionellWithImpfschutzUnit;

	private int freigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutz;
	private ChronoUnit freigabeOffsetFirstEncepurBoosterSchnellschemaWithImpfschutzUnit;

	private int freigabeOffsetKonventionellWithoutImpfschutz;
	private ChronoUnit freigabeOffsetKonventionellWithoutImpfschutzUnit;

	private int freigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutz;
	private ChronoUnit freigabeOffsetFirstFSMEImmuneImpfungSchnellschemaWithoutImpfschutzUnit;

	private int freigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutz;
	private ChronoUnit freigabeOffsetFirstEncepurImpfungSchnellschemaWithoutImpfschutzUnit;

	private int freigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutz;
	private ChronoUnit freigabeOffsetSecondFSMEImmuneImpfungWithoutImpfschutzUnit;

	private int freigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutz;
	private ChronoUnit freigabeOffsetSecondEncepurImpfungKonventionellWithoutImpfschutzUnit;

	private int freigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutz;
	private ChronoUnit freigabeOffsetSecondEncepurImpfungSchnellschemaWithoutImpfschutzUnit;

}
