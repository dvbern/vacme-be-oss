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
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable, of} from 'rxjs';
import {KrankheitIdentifierTS} from 'vacme-web-generated';
import {ImpfdokumentationService} from '../../../../../vacme-web-generated/src/lib/api/impfdokumentation.service';
import {extractKrankheitFromRoute} from '../../../../../vacme-web-shared/src/lib/util/krankheit-utils';

@Injectable({
    providedIn: 'root',
})
export class CanBeGrundimmunisierungResolverService implements Resolve<any> {

    constructor(
        private impfdokumentationService: ImpfdokumentationService
    ) {
    }

    public resolve(route: ActivatedRouteSnapshot): Observable<boolean> {

        const registrierungsnummer = route.params.registrierungsnummer as string;
        if (registrierungsnummer) {
            let krankheitFromRoute = extractKrankheitFromRoute(route);
            // TODO Affenpocken: wenn VACME-2162 umgesetzt kann krankheit nie mehr undefined sein
            if (krankheitFromRoute === undefined) {
                krankheitFromRoute = KrankheitIdentifierTS.COVID;
            }
            const canBeGrundimmunisierung$ =
                this.impfdokumentationService.impfdokumentationResourceCanBeGrundimmunisierung(
                    krankheitFromRoute, registrierungsnummer);
            return canBeGrundimmunisierung$;
        }
        return of(false);
    }
}
