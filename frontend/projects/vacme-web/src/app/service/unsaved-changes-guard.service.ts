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

import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanDeactivate, RouterStateSnapshot, UrlTree} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';

export interface CanComponentDeactivateSimple {
    canDeactivate: () => boolean;
}



@Injectable({
    providedIn: 'root'
})
export class UnsavedChangesGuard implements CanDeactivate<CanComponentDeactivateSimple> {


    constructor(private translateService: TranslateService) {
    }

    public canDeactivate(component: CanComponentDeactivateSimple, currentRoute: ActivatedRouteSnapshot,
                         currentState: RouterStateSnapshot,
                         nextState?: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

        const componentOkWithDeactivation = component.canDeactivate();
        if (componentOkWithDeactivation) {
            return true;
        }


        const translatedWarningText = this.translateService.instant('WARN.WARN_UNSAVED_CHANGES');
        return Swal.fire({
            icon: 'warning',
            text: translatedWarningText,
            showCancelButton: true,
            cancelButtonText: this.translateService.instant('WARN.WARN_UNSAVED_CHANGES_CANCEL'),
            confirmButtonText: this.translateService.instant('WARN.WARN_UNSAVED_CHANGES_OK'),
        }).then(value => {

            return value.isConfirmed;
        });

    }

}
