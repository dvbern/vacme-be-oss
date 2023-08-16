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
import {forkJoin, Observable, of} from 'rxjs';
import {DossierService, KrankheitIdentifierTS, StammdatenService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {extractKrankheitFromRoute} from '../../../../../vacme-web-shared/src/lib/util/krankheit-utils';

const LOG = LogFactory.createLog('resolveImpfdokumentation');

export const resolveImpfdokumentation: ResolveFn<Observable<any>> =
    (route: ActivatedRouteSnapshot, _: RouterStateSnapshot) => {
        const stammdatenService = inject(StammdatenService);
        const dossierService = inject(DossierService);
        const authService = inject(AuthServiceRsService);

        const registrierungsnummer = route.params.registrierungsnummer as string;
        if (registrierungsnummer) {
            const krankheitFromRoute = extractKrankheitFromRoute(route);
            if (krankheitFromRoute === undefined) {
                LOG.warn('Impfdokumentation called without krankheit in URL, this is unexpected, defaulting to COVID');
            }
            const krankheitToUse = (krankheitFromRoute === undefined)
                ? KrankheitIdentifierTS.COVID
                : krankheitFromRoute;
            const dossierRequest$ = dossierService.dossierResourceGetDashboardImpfdossier(
                krankheitToUse, registrierungsnummer);
            const impfstoffeRequest$ = stammdatenService.stammdatenResourceGetZugelasseneAndExternZugelasseneImpfstoffeList(
                krankheitToUse);
            const odiRequest$ = authService.loadOdisForCurrentUserAndStoreInPrincipal$(true); // forced reload in case of f5
            return forkJoin({
                dossier$: dossierRequest$,
                impfstoffe$: impfstoffeRequest$,
                odi$: odiRequest$
            });
        }
        return of([]);
    };
