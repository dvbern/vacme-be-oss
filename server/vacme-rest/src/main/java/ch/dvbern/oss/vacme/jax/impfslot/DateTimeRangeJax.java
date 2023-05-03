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

package ch.dvbern.oss.vacme.jax.impfslot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.shared.util.OpenApiConst;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class DateTimeRangeJax {

	@JsonIgnore
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

	@NonNull @NotNull
	@Schema(format = OpenApiConst.Format.DATE_TIME)
	private LocalDateTime von;

	@NonNull @NotNull
	private String vonDisplay;

	@NonNull @NotNull
	@Schema(format = OpenApiConst.Format.DATE_TIME)
	private LocalDateTime bis;

	@NonNull @NotNull
	private String bisDisplay;

	@Nullable
	private String exactDisplay;

	public DateTimeRangeJax(@NonNull @NotNull LocalDateTime von, @NonNull @NotNull LocalDateTime bis) {
		this.von = von;
		this.bis = bis;
		this.vonDisplay = formatter.format(von);
		this.bisDisplay = formatter.format(bis);
	}

	public void setExactDisplay(@NonNull String exactDisplay) {
		this.exactDisplay = exactDisplay;
	}
}
