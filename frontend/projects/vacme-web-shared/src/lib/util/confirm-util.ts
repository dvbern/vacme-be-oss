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
import {TranslateService} from '@ngx-translate/core';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {LogFactory} from '../logging';

const LOG = LogFactory.createLog('ConfirmUtil');

export class ConfirmUtil {

    public static swalAsObservable$(confirmDialog: Promise<{ isConfirmed: boolean }>): Observable<boolean> {
        return from(confirmDialog).pipe(map(r => r.isConfirmed));
    }

    public static addCheckboxAreYouSureWarning(
        formGroup: UntypedFormGroup, key: string, translate: TranslateService, prefix: string
    ): void {
        formGroup.get(key)?.valueChanges.subscribe((value) => {
            const previousValue = formGroup.value[key]; // nicht get verwenden, sonst gibt es schon den neuen Wert
            if (value && !previousValue && !formGroup.get(key)?.pristine) {
                Swal.fire({
                    icon: 'question',
                    text: translate.instant(prefix +'.CONFIRM_QUESTION'),
                    showCancelButton: true,
                    cancelButtonText: translate.instant(prefix + '.CANCEL'),
                    confirmButtonText: translate.instant(prefix + '.CONFIRM'),
                }).then(r => {
                    if (!r.isConfirmed) {
                        formGroup.get(key)?.setValue(false);
                    }
                });
            }
        }, error => LOG.error(error));
    }

}
