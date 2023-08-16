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

package ch.dvbern.oss.vacme.jax.base;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static ch.dvbern.oss.vacme.shared.util.OpenApiConst.Format.DATE;

@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class DateRangeJax {

	@NonNull @NotNull
	@Schema(format = DATE)
	private LocalDate von;

	@NonNull @NotNull
	@Schema(format = DATE)
	private LocalDate bis;

	public DateRangeJax(@NonNull @NotNull LocalDate von, @NonNull @NotNull LocalDate bis) {
		this.von = von;
		this.bis = bis;
	}
}
