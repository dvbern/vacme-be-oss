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

package ch.dvbern.oss.vacme.shared.errors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AppException extends RuntimeException {
	private static final long serialVersionUID = -30027164914702837L;

	@Getter
	private final Serializable[] args;

	protected AppException(
			String message,
			@Nullable Throwable cause,
			Serializable... args) {
		super(buildMessage(message, args), cause);
		this.args = requireNonNull(args);
	}

	private static String buildMessage(String message, Serializable[] args) {
		return new StringJoiner(", args: ")
				.add(message)
				.add(Arrays.toString(args))
				.toString();
	}

}
