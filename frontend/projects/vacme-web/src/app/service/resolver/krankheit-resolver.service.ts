/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, ResolveFn, Router, RouterStateSnapshot} from '@angular/router';
import {Observable, of} from 'rxjs';
import {LogFactory} from 'vacme-web-shared';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {extractKrankheitFromRoute} from '../../../../../vacme-web-shared/src/lib/util/krankheit-utils';

const LOG = LogFactory.createLog('resolveKrankheit');

export const resolveKrankheit: ResolveFn<Observable<any>> =
    (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
        const errorMessageService = inject(ErrorMessageService);
        const router = inject(Router);

        const krankheitFromRoute = extractKrankheitFromRoute(route);
        if (krankheitFromRoute !== undefined) {
            return of(krankheitFromRoute);
        }

        LOG.warn('URL contains unsupported Krankheit ' + route.url);
        errorMessageService.addMesageAsError('ERROR_UNKOWN_KRANKHEIT');
        router.navigate(['start']);
        return of(null);
    };
