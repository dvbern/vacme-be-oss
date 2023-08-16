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

import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';
import {PhoneNumberType, PhoneNumberUtil} from 'google-libphonenumber';
import {LogFactory} from 'vacme-web-shared';

const LOG = LogFactory.createLog('PhoneNumberValidator');
const phoneNumberUtil = PhoneNumberUtil.getInstance();

export function validPhoneNumberValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        let validNumber = false;
        if (!control.value) {
            return null;
        }
        try {
            const phoneNumber = phoneNumberUtil.parse(control.value, 'CH');
            validNumber = phoneNumberUtil.isValidNumber(phoneNumber);
        } catch (e) {
            LOG.debug('Could not parse phone number', e);
        }
        return validNumber ? null : {numberIncorrect: true};
    };
}

export function isMobileNumberValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        let validNumber = false;
        if (!control.value) {
            return null;
        }
        try {
            validNumber = isMobileNumber(control.value);
        } catch (e) {
            LOG.debug('Could not parse phone number', e);
        }
        return validNumber ? null : {festnetznummer: true};
    };
}

export function isMobileNumber(phoneNumber: string): boolean {
    const parsedNumber = phoneNumberUtil.parse(phoneNumber, 'CH');
    const type = phoneNumberUtil.getNumberType(parsedNumber);
    return type === PhoneNumberType.MOBILE || type === PhoneNumberType.FIXED_LINE_OR_MOBILE;
}
