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

import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';
import {PersonalienJaxTS, RegistrierungService} from 'vacme-web-generated';

export const resolveRegistrierungId: ResolveFn<Observable<PersonalienJaxTS>> =
    (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
        const registrierungService = inject(RegistrierungService);
        const registrierungId = route.params.registrierungId as string;

        if (registrierungId) {
            if (registrierungId) {
                return registrierungService.registrierungResourceGetPersonalien(registrierungId);
            }
        }
        throw Error('Missing route-param registrierungId ');
    };
