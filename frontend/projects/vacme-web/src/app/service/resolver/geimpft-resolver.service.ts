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
import {forkJoin, Observable, of} from 'rxjs';
import {concatMap} from 'rxjs/operators';
import {DossierService, FileInfoJaxTS, KrankheitIdentifierTS, OrtDerImpfungJaxTS} from 'vacme-web-generated';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {extractKrankheitenForOdis} from '../../../../../vacme-web-shared/src/lib/util/krankheit-utils';

@Injectable({
    providedIn: 'root',
})
export class GeimpftResolverService implements Resolve<any> {

    constructor(
        private dossierService: DossierService,
        private authService: AuthServiceRsService,
    ) {
    }

    public resolve(route: ActivatedRouteSnapshot): Observable<any> {
        const regNummer = route.params.registrierungsnummer;
        const odiListRequest$ = this.authService.loadOdisForCurrentUserAndStoreInPrincipal$(false);
        // welche files geladen werden ist abhaengig von der (einzigen) Krankheiten fuer die der User berechtigt ist
        const fileInfoRequest$ = this.getFileInfoRequest$(odiListRequest$, regNummer);
        const dashboardRequest$ = this.dossierService.dossierResourceGetDashboardRegistrierung(regNummer);
        const dossiersOverviewRequest$ = this.dossierService.dossierResourceGetImpfdossiersOverview(regNummer);
        return forkJoin({
            dashboard$: dashboardRequest$,
            fileInfo$: fileInfoRequest$,
            odiList$: odiListRequest$,
            dossiersOverview$: dossiersOverviewRequest$
        });
    }

    private getFileInfoRequest$(odiListRequest$: Observable<Array<OrtDerImpfungJaxTS>>, regNummer: any): Observable<Array<FileInfoJaxTS>> {
        return odiListRequest$.pipe(
            concatMap(odiList => {
                    const krankheitenList = extractKrankheitenForOdis(odiList);
                    const hasBerechtigungForMoreThanOneKrankheit = krankheitenList.size !== 1;
                    if (hasBerechtigungForMoreThanOneKrankheit) {
                        return of([] as Array<FileInfoJaxTS>);
                    } else {
                        return this.dossierService.dossierResourceGetFileInfo([...krankheitenList][0], regNummer);
                    }
                },
            ),
        );
    }
}
