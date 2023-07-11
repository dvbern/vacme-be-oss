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

import java.math.BigInteger;
import java.time.Duration;

import javax.persistence.Converter;

import com.fasterxml.jackson.databind.module.SimpleModule;

@Converter(autoApply = true)
public class DurationConverter extends GenericBigIntegerConverter<Duration> {

	public DurationConverter() {
		super(DurationConverter::toExternalForm, DurationConverter::fromBigInteger);
	}

	public static void registerJackson(
			SimpleModule module
	) {
		GenericBigIntegerConverter.registerJackson(
				module,
				Duration.class,
				DurationConverter::toExternalForm,
				DurationConverter::fromBigInteger
		);
	}

	private static BigInteger toExternalForm(Duration duration) {
		var seconds = duration.getSeconds();
		return BigInteger.valueOf(seconds);
	}

	private static Duration fromBigInteger(BigInteger bigInteger) {
		var seconds = bigInteger.longValue();
		return Duration.ofSeconds(seconds);
	}

}
