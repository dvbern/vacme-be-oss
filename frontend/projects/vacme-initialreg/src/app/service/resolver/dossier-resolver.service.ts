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
import {DashboardJaxTS, DossierService, KrankheitIdentifierTS} from 'vacme-web-generated';
import {extractKrankheitFromRoute} from '../../../../../vacme-web-shared/src/lib/util/krankheit-utils';

@Injectable({
    providedIn: 'root',
})
export class DossierResolverService implements Resolve<DashboardJaxTS> {

    constructor(private dossierService: DossierService) {
    }

    public resolve(route: ActivatedRouteSnapshot): Observable<DashboardJaxTS> {

        const registrierungsnummer = route.params.registrierungsnummer as string;
        const krankheitFromRoute = extractKrankheitFromRoute(route);
        if (registrierungsnummer && krankheitFromRoute) {
            return this.loadDossier$(krankheitFromRoute, registrierungsnummer);
        }
        return of();

    }

    private loadDossier$(krankheit: KrankheitIdentifierTS, registrierungsnummer: string): Observable<DashboardJaxTS> {
        if (registrierungsnummer) {
            // Achtung es ist wichtig dass dieser service aufgerufen wird weil damit der status wechselt wenn noetig
            return this.dossierService.dossierResourceRegGetDashboardRegistrierung(krankheit, registrierungsnummer);
        }

        return of();
    }
}
