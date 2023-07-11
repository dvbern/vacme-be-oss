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

package ch.dvbern.oss.vacme.entities.statistik;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.oss.vacme.entities.impfen.Impfstoff;
import ch.dvbern.oss.vacme.enums.ImpfstoffNamesJax;
import ch.dvbern.oss.vacme.shared.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.vacme.shared.util.Constants.MODERNA_ID_STRING;

@Slf4j
public final class StatistikUtil {

	private StatistikUtil() {
	}

	@Nullable
	public static String mapToImpfstoffString(@Nullable String commaSeparatedString) {
		if (StringUtils.isEmpty(commaSeparatedString)) {
			return null;
		}

		String impfstoffeAsString = Arrays.stream(commaSeparatedString.split(",")).map(s -> {
			switch (s) {
			case MODERNA_ID_STRING:
				return ImpfstoffNamesJax.MODERNA.name();
			case Constants.PFIZER_BIONTECH_ID_STRING:
				return ImpfstoffNamesJax.PFIZER_BIONTECH.name();
			case Constants.PFIZER_BIONTECH_BIVALENT_ID_STRING:
				return ImpfstoffNamesJax.PFIZER_BIONTECH_BIVALENT.name();
			case Constants.PFIZER_BIONTECH_KINDER_ID_STRING:
				return ImpfstoffNamesJax.PFIZER_BIONTECH_KINDER.name();
			case Constants.JANSSEN_ID_STRING:
				return ImpfstoffNamesJax.JANSSEN.name();
			case Constants.ASTRA_ZENECA_ID_STRING:
				return ImpfstoffNamesJax.ASTRAZENECA.name();
			case Constants.SINOVAC_ID_STRING:
				return ImpfstoffNamesJax.SINOVAC.name();
			case Constants.SINOPHARM_ID_STRING:
				return ImpfstoffNamesJax.SINOPHARM.name();
			case Constants.COVAXIN_ID_STRING:
				return ImpfstoffNamesJax.COVAXIN.name();
			case Constants.NOVAVAX_ID_STRING:
				return ImpfstoffNamesJax.NOVAVAX.name();
			case Constants.MODERNA_BIVALENT_ID_STRING:
				return ImpfstoffNamesJax.MODERNA_BIVALENT.name();
			default:
				LOG.warn("Mapping for Impfstoff with id '{}' not yet implemented!", s);
				return s;
			}
		}).collect(Collectors.joining("/"));
		return impfstoffeAsString;
	}

	@Nullable
	public static String mapToImpfstoffString(@Nullable String commaSeparatedString, @NonNull Set<Impfstoff> impfstoffe) {
		if (StringUtils.isEmpty(commaSeparatedString)) {
			return null;
		}
		Map<String, String> impfstoffIdToName = impfstoffSetToMap(impfstoffe);

		String impfstoffeAsString = Arrays.stream(commaSeparatedString.split(",")).map(s -> {
			String readableName = impfstoffIdToName.get(s);
			if (readableName == null) {
				LOG.warn("Mapping for Impfstoff with id '{}' not yet implemented!", s);
				return s;
			}
			return readableName;

		}).collect(Collectors.joining("/"));

		return impfstoffeAsString;
	}

	private static Map<String, String> impfstoffSetToMap(@NonNull Set<Impfstoff> impfstoffe) {
		return impfstoffe.stream().collect(Collectors.toMap(impfstoff -> impfstoff.getId().toString(), Impfstoff::getName));
	}
}
