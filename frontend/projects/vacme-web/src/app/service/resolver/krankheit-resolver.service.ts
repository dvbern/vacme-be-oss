/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router} from '@angular/router';
import {Observable, of} from 'rxjs';
import {LogFactory} from 'vacme-web-shared';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {extractKrankheitFromRoute} from '../../../../../vacme-web-shared/src/lib/util/krankheit-utils';

const LOG = LogFactory.createLog('KrankheitResolverService');

@Injectable({
    providedIn: 'root',
})
export class KrankheitResolverService implements Resolve<any> {

    constructor(
        private errorMessageService: ErrorMessageService,
        private router: Router,
    ) {
    }

    public resolve(route: ActivatedRouteSnapshot): Observable<any> {

        const krankheitFromRoute = extractKrankheitFromRoute(route);
        if (krankheitFromRoute !== undefined) {
            return of(krankheitFromRoute);
        }

        LOG.warn('URL contains unsupported Krankheit ' + route.url);
        this.errorMessageService.addMesageAsError('ERROR_UNKOWN_KRANKHEIT');
        this.router.navigate(['start']);
        return of(null);
    }
}
