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

package ch.dvbern.oss.vacme.entities.documentqueue;


import ch.dvbern.oss.vacme.enums.FileNameEnum;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum DocumentQueueType {

	ABRECHNUNG(FileNameEnum.ABRECHNUNG),
	ABRECHNUNG_ERWACHSEN(FileNameEnum.ABRECHNUNG_ERWACHSEN),
	ABRECHNUNG_KIND(FileNameEnum.ABRECHNUNG_KIND),
	ABRECHNUNG_ZH(FileNameEnum.ABRECHNUNG_ZH),
	ABRECHNUNG_ZH_KIND(FileNameEnum.ABRECHNUNG_ZH_KIND),

	IMPFUNGEN_REPORT_CSV(FileNameEnum.IMPFUNGEN_REPORT_CSV),

	IMPFSLOTS_REPORT_CSV(FileNameEnum.IMPFSLOTS_REPORT_CSV),
	REGISTRIERUNGEN_KANTON_CSV(FileNameEnum.REGISTRIERUNGEN_KANTON_CSV),
	REGISTRIERUNGEN_KANTONSARZT_CSV(FileNameEnum.REGISTRIERUNGEN_KANTONSARZT_CSV),
	ODI_REPORT_CSV(FileNameEnum.ODI_REPORT_CSV),
	ODI_IMPFUNGEN(FileNameEnum.ODI_IMPFUNGEN),
	ODI_TERMINBUCHUNGEN(FileNameEnum.ODI_TERMINBUCHUNGEN);

	@NonNull
	private final FileNameEnum fileNameEnum;

	DocumentQueueType(@NonNull FileNameEnum fileNameEnum) {
		this.fileNameEnum = fileNameEnum;
	}

	@NonNull
	public FileNameEnum getFileNameEnum() {
		return fileNameEnum;
	}
}
