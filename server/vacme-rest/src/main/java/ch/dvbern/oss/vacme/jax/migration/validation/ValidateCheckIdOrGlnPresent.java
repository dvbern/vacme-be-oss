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

package ch.dvbern.oss.vacme.jax.migration.validation;

import ch.dvbern.oss.vacme.jax.migration.ImpfungGLNPatchMigrationJax;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import org.apache.commons.lang3.StringUtils;

public class ValidateCheckIdOrGlnPresent {

	public static void checkIdOrGln(ImpfungGLNPatchMigrationJax impfungGLNPatchMigrationJax,
		String externalId) {

		if (StringUtils.isNotBlank(impfungGLNPatchMigrationJax.getImpfortGLN()) && impfungGLNPatchMigrationJax.getImpfortId() != null) {
			throw new AppFailureException(String.format("Es darf nur entweder impfortGln oder impfortId gesetzt sein "
				+ "(%s, %s)", externalId, impfungGLNPatchMigrationJax.getImpffolge()));
		}


		if (impfungGLNPatchMigrationJax.getImpfortGLN() == null && impfungGLNPatchMigrationJax.getImpfortId() == null) {
			throw new AppFailureException(String.format("Es darf muss entweder impfortGln oder impfortId gesetzt sein "
				+ "(%s, %s)", externalId, impfungGLNPatchMigrationJax.getImpffolge()));
		}

	}
}
