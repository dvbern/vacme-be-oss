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

package ch.dvbern.oss.vacme.init;

import javax.inject.Singleton;

import ch.dvbern.oss.vacme.entities.types.CleanFileNameConverter;
import ch.dvbern.oss.vacme.entities.types.DurationConverter;
import ch.dvbern.oss.vacme.entities.types.EmailAddressConverter;
import ch.dvbern.oss.vacme.init.jackson.StringNormalizerModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;

/**
 * See <a href="https://quarkus.io/guides/rest-json#jackson">Quarkus Jackson Customization</a>
 */
@Singleton
@SuppressWarnings("unused")
public class JacksonCustomizer implements ObjectMapperCustomizer {

	@Override
	public void customize(ObjectMapper mapper) {
		// Default is: write timestamps as array... which sucks
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		SimpleModule module = new SimpleModule("ABC"); // TODO wozu brauchen wir das?

		EmailAddressConverter.registerJackson(module);
		CleanFileNameConverter.registerJackson(module);
		DurationConverter.registerJackson(module);

		mapper.registerModule(module);
		mapper.registerModule(new StringNormalizerModule());
	}

}
