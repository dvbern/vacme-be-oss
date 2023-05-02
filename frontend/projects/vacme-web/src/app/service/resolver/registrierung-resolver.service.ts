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
import {ImpfkontrolleJaxTS, KontrolleService, KrankheitIdentifierTS} from 'vacme-web-generated';
import {extractKrankheitFromRoute} from '../../../../../vacme-web-shared/src/lib/util/krankheit-utils';

@Injectable({
    providedIn: 'root',
})
export class RegistrierungResolverService implements Resolve<ImpfkontrolleJaxTS | undefined> {

    constructor(protected kontrolleService: KontrolleService) {
    }

    public resolve(route: ActivatedRouteSnapshot): Observable<ImpfkontrolleJaxTS | undefined> {
        const nr = route.params.registrierungsnummer;

        if (nr !== 'new') {
            // TODO Affenpocken: warum haben wir diesen resolver separat?
            const krankheitFromRoute = extractKrankheitFromRoute(route);
            if (krankheitFromRoute !== undefined) {
                return this.load$(nr,krankheitFromRoute);
            }
            return this.load$(nr,KrankheitIdentifierTS.COVID);
        }
        return of(undefined);

    }

    private load$(nr: string, krankheit: KrankheitIdentifierTS): Observable<ImpfkontrolleJaxTS | undefined> {
        if (nr) {
            return this.kontrolleService.impfkontrolleResourceFind(krankheit, nr);
        }

        return of(undefined);
    }
}
