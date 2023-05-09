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

import {AbstractControl, ValidatorFn} from '@angular/forms';
import {LogFactory} from '../../logging';

const LOG = LogFactory.createLog('certifiableNamestringValidator');

export function certifiableNamestringValidator(): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} | null => {
        const name = control.value;
        if (!name) {
            return null;
        }

        // eslint-disable-next-line @typescript-eslint/no-inferrable-types
        const nameRegexp: RegExp = /[!@#\r\n$%¶*\\()_:+=\[\]{}|<>\/?~]/;
        if (control.value && nameRegexp.test(control.value)) {
            return { nonCertifiableName: true };
        }
        return null;
    };
}
