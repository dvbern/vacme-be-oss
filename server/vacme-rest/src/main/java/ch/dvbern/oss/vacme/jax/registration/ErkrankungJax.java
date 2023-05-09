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

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.impfen.Erkrankung;
import ch.dvbern.oss.vacme.entities.registration.ErkrankungDatumHerkunft;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErkrankungJax {

	@NotNull
	@NonNull
	private LocalDate date;

	private ErkrankungDatumHerkunft erkrankungdatumHerkunft;

	public ErkrankungJax(@NonNull Erkrankung eintrag) {
		this.setDate(eintrag.getDate());
		this.erkrankungdatumHerkunft = ErkrankungDatumHerkunft.ERFASST_IMPFLING;
	}

	public Erkrankung toEntity() {
		if (erkrankungdatumHerkunft != ErkrankungDatumHerkunft.ERFASST_IMPFLING) {
			throw new IllegalArgumentException("Can not create Erkrankung Entity for ErkrankungJax with type" + erkrankungdatumHerkunft);
		}
		Erkrankung erkrankung = new Erkrankung();
		erkrankung.setDate(date);
		return erkrankung;
	}

}
