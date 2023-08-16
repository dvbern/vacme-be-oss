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

import {UntypedFormGroup} from '@angular/forms';
import {BeruflicheTaetigkeitTS} from 'vacme-web-generated';
import {LogFactory} from '../logging';
import {MIN_ALTER_BERUF} from '../constants';
import DateUtil from './DateUtil';


const LOG = LogFactory.createLog('BerufUtil');

export class BerufUtil {

    public static addBerufAutoselectForChildren(formGroup: UntypedFormGroup, geburtstagKey: string, berufKey: string): void {
        formGroup.get(geburtstagKey)?.valueChanges.subscribe(() => {
            this.onGeburtsdatumChange(formGroup, geburtstagKey, berufKey);
        }, err => LOG.error(err));
    }

    private static onGeburtsdatumChange(formGroup: UntypedFormGroup,
                                        geburtstagKey: string,
                                        berufKey: string): void {

        const isTooYoungForWork = this.isTooYoungForWork(formGroup, geburtstagKey);
        const berufControl = formGroup.get(berufKey);

        if (isTooYoungForWork) { // Kind?
            if (!berufControl?.value) { // Beruf noch nicht gewaehlt?
                LOG.info('Setting Beruf to "nicht erwerbstaetig" because of young age');
                berufControl?.setValue(BeruflicheTaetigkeitTS.NICHT_ERWERBSTAETIG);
            }
        }

        berufControl?.updateValueAndValidity();
    }

    private static isTooYoungForWork(formGroup: UntypedFormGroup, geburtstagKey: string): boolean {
        const geburtsdatumControl = formGroup.get(geburtstagKey);

        // erst etwas machen, wenn das Geburtsdatum valid ist, sonst wird 02.02.20 bereits als Datum geparst
        const validGeburtsdatum = geburtsdatumControl?.valid;
        if (!validGeburtsdatum) {
            return false;
        }

        const geburtsdatum = DateUtil.parseDateAsMiddayOrUndefined(geburtsdatumControl?.value);
        return !!geburtsdatum && DateUtil.age(geburtsdatum) < MIN_ALTER_BERUF;
    }

}
