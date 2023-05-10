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

import {AbstractControl, ValidatorFn, Validators} from '@angular/forms';

/**
 * this validator checks that a required field is set. But it only does it if the predicate returns true
 */
export function requiredIfValidator(predicate: () => boolean): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} | null => {
        if (predicate()) {
            return Validators.required(control);
        }
        return null;
    };
}

/**
 * this validator checks that a required field is set. But it only does it if the predicate returns true
 */
export function requiredTrueIfValidator(predicate: () => boolean): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} | null => {
        if (predicate()) {
            return Validators.requiredTrue(control);
        }
        return null;
    };
}
