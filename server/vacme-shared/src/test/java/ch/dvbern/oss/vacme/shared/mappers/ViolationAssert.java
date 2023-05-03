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

package ch.dvbern.oss.vacme.shared.mappers;

import java.util.Objects;

import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import ch.dvbern.oss.vacme.shared.errors.mappers.AppValidationErrorResponse;
import ch.dvbern.oss.vacme.shared.errors.mappers.AppValidationErrorResponse.Violation;
import org.assertj.core.api.AbstractAssert;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ViolationAssert extends AbstractAssert<ViolationAssert, Violation> {

	public ViolationAssert(AppValidationErrorResponse.Violation violation) {
		super(violation, ViolationAssert.class);
	}

	public static ViolationAssert assertThat(@Nullable Violation violation) {
		return new ViolationAssert(violation);
	}

	public ViolationAssert hasKey(String key) {
		isNotNull();

		if (!Objects.equals(key, actual.getKey())) {
			failWithMessage("Expected keys name to be <%s> but was <%s>", key, actual.getKey());
		}

		return this;
	}

	public ViolationAssert hasKey(Class<?> annotationClass) {
		return hasKey(annotationClass.getSimpleName());
	}

	public ViolationAssert hasKey(AppValidationMessage key) {
		return hasKey(key.name());
	}

	public ViolationAssert hasPath(String path) {
		isNotNull();

		if (!Objects.equals(path, actual.getPath())) {
			failWithMessage("Expected path be <%s> but was <%s>", path, actual.getPath());
		}

		return this;
	}

	public ViolationAssert hasMessage(String message) {
		isNotNull();

		if (!Objects.equals(message, actual.getMessage())) {
			failWithMessage("Expected message be <%s> but was <%s>", message, actual.getMessage());
		}

		return this;
	}
}
