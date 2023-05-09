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
import {PersonalienJaxTS, RegistrierungService} from 'vacme-web-generated';

@Injectable({
    providedIn: 'root',
})
export class RegistrierungIdResolverService implements Resolve<PersonalienJaxTS> {

    constructor(private registrierungService: RegistrierungService) {
    }

    public resolve(route: ActivatedRouteSnapshot): Observable<PersonalienJaxTS> {

        const registrierungId = route.params.registrierungId as string;

        if (registrierungId) {
            return this.loadPersonalien$(registrierungId);
        }
        return of();

    }

    private loadPersonalien$(registrierungId: string): Observable<PersonalienJaxTS> {
        if (registrierungId) {
            return this.registrierungService.registrierungResourceGetPersonalien(registrierungId);
        }

        return of();
    }
}
