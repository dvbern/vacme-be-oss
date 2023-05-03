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
import {ActivatedRoute, Router} from '@angular/router';
import {KeycloakAppAuthGuard} from './guard/keycloak-app-auth-guard.service';
import {LogFactory} from '../logging';
import {HTTP_UNAUTHORIZED} from '../constants';

const LOG = LogFactory.createLog('ErrorHandlerService');

@Injectable({
    providedIn: 'root',
})
export class ErrorHandlerService {
    constructor(
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private keycloakAuthGuard: KeycloakAppAuthGuard
    ) {
    }

    public handleIf401(error: any): void {
        if (!error?.status) {
            return;
        }
        if (error.status === HTTP_UNAUTHORIZED) {
            this.keycloakAuthGuard.isLoggedIn() // could also use keycloak service
                .then(loggedIn => {
                    if (!loggedIn) {
                        LOG.info('User is  not logged in in KC, redirecting to Loginpage');
                        this.keycloakAuthGuard.forceLogin(
                            this.activatedRoute.snapshot, this.router.routerState.snapshot
                        );
                    } else {
                        LOG.info('User is logged in in KC, will not redirect to login');
                    }
                });
        }
    }
}
