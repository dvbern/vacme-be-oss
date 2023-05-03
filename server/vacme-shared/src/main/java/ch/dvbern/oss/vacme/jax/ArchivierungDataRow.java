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

package ch.dvbern.oss.vacme.jax;

import ch.dvbern.oss.vacme.entities.registration.Fragebogen;
import ch.dvbern.oss.vacme.util.ImpfinformationDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * DTO fuer die Daten die in das zu archivierende PDF sollen
 */
@Getter
@Setter
public class ArchivierungDataRow {

	@NonNull
	private Fragebogen fragebogen;

	@NonNull
	private ImpfinformationDto impfinformationDto;

	/**
	 * Wird aus DB Query gebraucht
	 */
	@QueryProjection
	public ArchivierungDataRow(
		@NonNull Fragebogen fragebogen
	) {
		this.fragebogen = fragebogen;
	}

	public ArchivierungDataRow(
		@NonNull Fragebogen fragebogen,
		@NonNull ImpfinformationDto impfinformationDto
	) {
		this.fragebogen = fragebogen;
		this.impfinformationDto = impfinformationDto;
	}
}
