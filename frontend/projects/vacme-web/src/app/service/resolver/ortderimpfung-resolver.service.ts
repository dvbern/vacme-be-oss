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
import {OrtDerImpfungJaxTS, OrtderimpfungService} from 'vacme-web-generated';

@Injectable({
    providedIn: 'root',
})
export class OrtDerImpfungResolverService implements Resolve<OrtDerImpfungJaxTS | undefined> {

    constructor(protected ortderimpfungService: OrtderimpfungService) {
    }

    public resolve(route: ActivatedRouteSnapshot): Observable<OrtDerImpfungJaxTS | undefined> {

        const id = route.params.ortDerImpfungId;

        if (id) {
            return this.loadOrtDerImpfung$(id);
        }
        return of(undefined);

    }

    private loadOrtDerImpfung$(id: string): Observable<OrtDerImpfungJaxTS | undefined> {
        if (id) {
            return this.ortderimpfungService.ortDerImpfungResourceGetOrtDerImpfung(id);
        }

        return of(undefined);
    }
}
