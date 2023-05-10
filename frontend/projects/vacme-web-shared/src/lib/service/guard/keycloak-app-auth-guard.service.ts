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
import {ActivatedRouteSnapshot, Router, RouterStateSnapshot} from '@angular/router';
import {KeycloakAuthGuard, KeycloakService} from 'keycloak-angular';

import {LogFactory} from '../../logging';

const LOG = LogFactory.createLog('KeycloakAppAuthGuard');

@Injectable({
    providedIn: 'root'
})
export class KeycloakAppAuthGuard extends KeycloakAuthGuard {


    constructor(protected router: Router, protected keycloakAngular: KeycloakService) {
        super(router, keycloakAngular);
    }

    public isAccessAllowed(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {

        return new Promise<boolean>((resolve, reject): void => {
            if (!this.authenticated) {
                LOG.warn('user is not authenticated, redirecting to keycloak ');
                this.forceLogin(route, state);

                resolve(false);
            }
            resolve(true);
        });
    }

    public isLoggedIn(): Promise<boolean> {
        return this.keycloakAngular.isLoggedIn();
    }

    public forceLogin(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<void> {
        return this.keycloakAngular.login({
            redirectUri: window.location.origin + state.url,
        });
    }

    // example on how to check for roles if we want that
    // private hasAllowedRoles(route: ActivatedRouteSnapshot, resolve): void {
    //     const requiredRoles = route.data.roles;
    //     if (!requiredRoles || requiredRoles.length === 0) {
    //         resolve(true);
    //     } else {
    //         if (!this.roles || this.roles.length === 0) {
    //             resolve(false);
    //         }
    //         let granted: boolean = false;
    //         for (const requiredRole of requiredRoles) {
    //             if (this.roles.indexOf(requiredRole) > -1) {
    //                 granted = true;
    //                 break;
    //             }
    //         }
    //         resolve(granted);
    //     }
    // }
}
