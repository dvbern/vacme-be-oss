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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.terminbuchung.Impfslot;
import ch.dvbern.oss.vacme.entities.types.KrankheitIdentifier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtil.DEFAULT_DATE_FORMAT;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class ImpfslotDisplayDayJax {

	@NotNull
	@NonNull
	private LocalDate day;

	private String dayDisplay;

	@NonNull
	@Schema(required = false, readOnly = true, description = "Liste aller Impfslots des Tages")
	private Map<KrankheitIdentifier, List<ImpfslotDisplayJax>> krankheitImpfslotDisplayJaxMap;

	public static ImpfslotDisplayDayJax of(@NonNull Entry<LocalDate, List<Impfslot>> localDateListEntry) {
		LocalDate dayDate = localDateListEntry.getKey();
		String displayDate = DEFAULT_DATE_FORMAT.apply(Locale.getDefault()).format(dayDate);

		// Beautiful *.*
		// Takes an Entry<LocalDate, List<Impfslot> and converts it into a Map<KrankheitsIndentifier, List<ImpfImpfslotDisplayJax>>
		// does so by first grouping the List<Impfslot> by KrankheitIdentifier into a Map<KrankheitsIdentifier, List<Impfslot>>
		// then converts the List<Impfslot> into List<ImpfImpfslotDisplayJax> and finally sorts this new List<ImpfImpfslotDisplayJax> by zeitfenster.von.
		Map<KrankheitIdentifier, List<ImpfslotDisplayJax>> krankheitImpfslotJaxMap =
			localDateListEntry.getValue()
				.stream()
				.collect(Collectors.groupingBy(Impfslot::getKrankheitIdentifier, Collectors.mapping(
					Function.identity(),
					Collectors.collectingAndThen(
						Collectors.toList(),
						impfslots -> impfslots.stream()
							.map(ImpfslotDisplayJax::of)
							.sorted(Comparator.comparing(impfslotDisplayJax -> impfslotDisplayJax.getZeitfenster().getVon()))
							.collect(
								Collectors.toList())))));

		return new ImpfslotDisplayDayJax(dayDate, displayDate, krankheitImpfslotJaxMap);
	}
}
