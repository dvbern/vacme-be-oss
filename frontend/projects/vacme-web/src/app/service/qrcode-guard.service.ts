/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

import {Injectable, Input} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {DashboardJaxTS, DossierService, KrankheitIdentifierTS, SelfserviceEditJaxTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {extractKrankheitFromRoute} from '../../../../vacme-web-shared/src/lib/util/krankheit-utils';
import {NavigationService} from './navigation.service';

const LOG = LogFactory.createLog('GesuchInfoUpdateGuard');

@Injectable({
    providedIn: 'root',
})
export class QRCodeGuardService implements CanActivate {

    @Input()
    public selfserviceEditJax?: SelfserviceEditJaxTS;

    constructor(
        protected dossierService: DossierService,
        private router: Router,
        private navigationService: NavigationService,
    ) {
    }

    public canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot,
    ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        const nr = route.params.registrierungsnummer;
        const krankheitFromRoute = extractKrankheitFromRoute(route);

        if (nr !== 'new') {
            return this.load$(nr, krankheitFromRoute).pipe(
                map(dossier => {
                    if (dossier !== undefined) {
                        if (krankheitFromRoute) {
                            this.navigationService.navigateForKrankheit(dossier, krankheitFromRoute);
                            return false;
                        }
                        this.navigationService.navigate(dossier);
                        return false;
                    }
                    this.navigationService.notFoundResult();
                    return false; // we never navigate to the page
                }),
                catchError(error => {
                    LOG.error(error);
                    this.navigationService.notFoundResult();
                    return of(false);
                }),
            );
        }
        LOG.warn('Fuer die registrierungsnummer kann nicht mittels qr code navigiert werden da nichts gefunden wurde');
        return of(false);

    }

    private load$(nr: string, krankheit: KrankheitIdentifierTS | undefined ): Observable<DashboardJaxTS | undefined> {
        if (nr) {
            if (krankheit) {
                return this.dossierService.dossierResourceGetDashboardImpfdossier(krankheit, nr);
            }
            return this.dossierService.dossierResourceGetDashboardRegistrierung(nr);
        }
        return of(undefined);
    }

}
