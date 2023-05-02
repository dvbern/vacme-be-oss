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

package ch.dvbern.oss.vacme.entities.util.validators;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

public class IsInFutureValidator implements ConstraintValidator<IsInFuture, Temporal> {

	private @Nullable IsInFuture annotation = null;

	@Override
	public void initialize(IsInFuture annotation) {
		this.annotation = requireNonNull(annotation);
	}

	@Override
	public boolean isValid(@Nullable Temporal value, ConstraintValidatorContext ctx) {
		if (value == null) {
			return true;
		}

		boolean ok = validate(value);

		return ok;
	}

	@SuppressWarnings("dereference.of.nullable")
	boolean validate(Temporal value) {
		Objects.requireNonNull(annotation);

		TemporalUnit timeUnit = findSuportedUnit(value);

		long duration = value.until(now(), timeUnit);

		if (annotation.allowNow()) {
			return duration <= 0;
		}

		return duration < 0;
	}

	private TemporalUnit findSuportedUnit(Temporal value) {
		for (ChronoUnit chronoUnit : ChronoUnit.values()) {
			if (value.isSupported(chronoUnit)) {
				return chronoUnit;
			}
		}

		throw new IllegalStateException(String.format("No supported chronounit found for value: %s, class: %s",
				value, value.getClass().getName()));
	}

	LocalDateTime now() {
		return LocalDateTime.now();
	}
}
