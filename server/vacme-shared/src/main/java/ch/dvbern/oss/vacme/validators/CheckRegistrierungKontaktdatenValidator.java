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

package ch.dvbern.oss.vacme.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.oss.vacme.entities.registration.Registrierung;
import ch.dvbern.oss.vacme.entities.registration.RegistrierungsEingang;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Prueft ob die Kontaktangaben der Registrierung gueltig sind
 */
public class CheckRegistrierungKontaktdatenValidator implements ConstraintValidator<CheckRegistrierungKontaktdaten, Registrierung> {

	@Override
	public void initialize(CheckRegistrierungKontaktdaten constraintAnnotation) {
		//nop
	}

	@Override
	public boolean isValid(Registrierung registrierung, @Nullable ConstraintValidatorContext context) {
		if (registrierung.getRegistrierungsEingang() == RegistrierungsEingang.ONLINE_REGISTRATION) {
			// Bei Online Registrierung muss entweder Mobile oder Mail erfasst sein
			return !StringUtils.isEmpty(registrierung.getMail());
		}
		if (registrierung.getRegistrierungsEingang() == RegistrierungsEingang.CALLCENTER_REGISTRATION) {
			// Bei Callcenter Registrierung muss eine Mail oder Telefonnummer angegeben werden
			return !StringUtils.isEmpty(registrierung.getTelefon()) || !StringUtils.isEmpty(registrierung.getMail());
		}
		return true;
	}
}
