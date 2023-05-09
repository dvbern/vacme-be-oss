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

import java.util.Arrays;

import ch.dvbern.oss.vacme.shared.errors.AppValidationException;
import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.opentest4j.AssertionFailedError;

public class TestUtil {

	// Methode abgeschrieben von Assertions.assertThrows
	// aber mit der zusaetzlichen Validierung der Message (weil bei uns etwa alle Exceptions die gleiche Klasse haben)
	public static AppValidationException assertThrowsAppvalidation(AppValidationMessage validationMessage, @Nullable String argsPrefix,
		Executable executable) {
		try {
			executable.execute();
		} catch (Throwable expectedException) {
			if (AppValidationException.class.isInstance(expectedException)) {
				AppValidationException appEx = (AppValidationException) expectedException;
				boolean matchingValidationMessage = validationMessage == null
					|| validationMessage.equals(appEx.getValidationMessage());
				boolean matchingArgs = argsPrefix == null
					|| Arrays.stream(appEx.getArgs()).anyMatch(serializable -> serializable.toString().startsWith(argsPrefix));
				if (matchingValidationMessage && matchingArgs) {
					return (AppValidationException) expectedException;
				} else {
					throw new AssertionFailedError("expected exception was thrown, but with the wrong message oder args");
				}
			}

			UnrecoverableExceptions.rethrowIfUnrecoverable(expectedException);
			throw new AssertionFailedError("another exception was thrown: ", expectedException);
		}

		throw new AssertionFailedError("expected exception was not thrown");
	}
}
