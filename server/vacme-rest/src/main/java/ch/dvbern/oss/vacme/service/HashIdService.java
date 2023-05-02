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

package ch.dvbern.oss.vacme.service;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hashids.Hashids;

@ApplicationScoped
public class HashIdService {

	@ConfigProperty(name = "hashids.alphabet")
	String alphabet;

	@ConfigProperty(name = "hashids.minLength")
	int minLength;

	@ConfigProperty(name = "hashids.salt")
	String salt;

	private static final String umfrageAlphabet = "123456789abcdfghiklmnpqrstuvwxyz";


	public String getHashFromNumber(long number) {
		Hashids hashids = new Hashids(this.salt, this.minLength, this.alphabet);
		return hashids.encode(number);
	}

	public String getUmfrageCodeHash(long number) {
		Hashids hashids = new Hashids(this.salt, this.minLength, umfrageAlphabet);
		return hashids.encode(number);
	}
}
