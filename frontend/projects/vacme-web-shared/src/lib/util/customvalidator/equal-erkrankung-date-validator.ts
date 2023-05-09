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
import moment from 'moment/moment';
import {ErkrankungJaxTS} from 'vacme-web-generated';
import {DATE_FORMAT} from '../../constants';

export function equalErkrankungDateValidator(index: number, getErkrankungen: () => ErkrankungJaxTS[]): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
        const erkrankungen = getErkrankungen();

        for (let i = 0; i < erkrankungen.length; i++) {
            if (i !== index && erkrankungen[i].date && moment(erkrankungen[i].date, DATE_FORMAT)
                .isSame(moment(control.value, DATE_FORMAT))) {
                return {equalErkrankungDate: true};
            }
        }
        return null;
    };
}
