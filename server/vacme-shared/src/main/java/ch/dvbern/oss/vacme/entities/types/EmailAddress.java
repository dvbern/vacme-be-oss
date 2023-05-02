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

package ch.dvbern.oss.vacme.entities.types;

import java.io.Serializable;

import ch.dvbern.oss.vacme.shared.errors.AppValidationMessage;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

@Getter
public class EmailAddress implements Serializable {

	private static final long serialVersionUID = -2872872456003785391L;

	private final String address;

	protected EmailAddress(String address) {
		this.address = address;
	}

	public static EmailAddress fromString(String s) {
		requireNonNull(s);

		return parse(s);
	}

	static EmailAddress parse(String input) {
		String normalized = StringUtils.normalizeSpace(input);
		if (!EmailValidator.getInstance(true).isValid(normalized)) {
			throw AppValidationMessage.INVALID_EMAIL.create(input);
		}

		return new EmailAddress(normalized);
	}

	@Override
	public String toString() {
		return address;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof EmailAddress)) {
			return false;
		}

		EmailAddress that = (EmailAddress) o;

		return address.equals(that.address);
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}
}
