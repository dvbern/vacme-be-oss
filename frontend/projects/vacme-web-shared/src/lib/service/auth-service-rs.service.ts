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

import {HttpClient} from '@angular/common/http';
import {Inject, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {KeycloakService} from 'keycloak-angular';
import {CookieService} from 'ngx-cookie-service';

// import * as Raven from 'raven-js';
import {from, Observable, of} from 'rxjs';
import {concatMap, filter, finalize, first, map} from 'rxjs/operators';
import {KrankheitIdentifierTS, OrtDerImpfungJaxTS, OrtderimpfungService} from 'vacme-web-generated';
import {BenutzerService} from '../../../../vacme-web-generated/src/lib/api/benutzer.service';
import {BaseDestroyableComponent} from '../components/base-destroyable/base-destroyable.component';
import {API_URL} from '../constants';

import {LogFactory} from '../logging';
import {AppEvent, TSAppEventTyp, TSRole} from '../model';
import TSUser from '../model/TSUser';
import {APP_CONFIG, AppConfig} from '../types';
import {extractKrankheitenForOdis} from '../util/krankheit-utils';
import {ApplicationEventService} from './application-event.service';
import {KeycloakProfileWithMobileNumber} from './KeycloakProfileWithMobileNumber';
import {LoginEventService} from './login-event.service';

const LOG = LogFactory.createLog('AuthServiceRsService');

@Injectable({
    providedIn: 'root',
})
export class AuthServiceRsService extends BaseDestroyableComponent{

    private serviceURL: string;
    private principal?: TSUser;

    private readonly delay: number = 300;

    constructor(private http: HttpClient,
                private cookieService: CookieService,
                private readonly loginEventService: LoginEventService,
                private keycloakService: KeycloakService,
                private benutzerService: BenutzerService,
                private translateService: TranslateService,
                private ortderimpfungService: OrtderimpfungService,
                private applicationEventService: ApplicationEventService,
                @Inject(APP_CONFIG) private appConfig: AppConfig,
    ) {
        super();
        this.serviceURL = this.isInitialReg() ? `${API_URL}/reg/auth` : `${API_URL}/web/auth`;
        this.applicationEventService.appEventStream$
            .pipe(
                filter<AppEvent>(value => value.appEventTyp === TSAppEventTyp.ODI_MODIFIED),
                concatMap(value => this.loadOdisForCurrentUserAndStoreInPrincipal$(true)),
                this.takeUntilDestroyed(),
            ).subscribe(() => {LOG.info('received ODI_MODIFIED event ');}, error => LOG.error(error));
    }

    public triggerKeycloakLogin(): Promise<void> {
        return this.keycloakService.login();
    }

    public triggerKeycloakLogout(redirectUri: string): Promise<void> {
        return this.keycloakService.logout(redirectUri);
    }

    public triggerKeycloakRegister(redirectUri: string): Promise<void> {
        return this.keycloakService.register({
            redirectUri
        });
    }

    public logoutRequest$(newPath?: string): Observable<object> {
        return this.http.post(this.serviceURL + '/logout', null).pipe(map(res => {
            this.principal = undefined;
            // include.base will redirect 301 from / to the right place
            const newURL = `${window.location.protocol}//${window.location.host}/${newPath}`;

            this.keycloakService.logout(newURL); // single logout - redirect to start

            // Raven.setUserContext();
            this.loginEventService.broadcastEvent(TSAppEventTyp.LOGGED_OUT);

            return res;
        }));
    }

    public logout(router: Router, navigateToUrl: string): void {
        this.logoutRequest$(navigateToUrl)
            .pipe(
                first(),
                finalize(() => {
                    router.navigate([navigateToUrl]);
                }))
            .subscribe(() => {
                LOG.debug('logout was successful');
            }, () => {
                LOG.error('could not logout');
            });
    }

    public initWithCookie(): TSUser | undefined {
        this.principal = undefined;
        const authIdbase64 = this.cookieService.get('authId');
        if (authIdbase64) {
            try {
                // zuerst  uri decodieren und dann erst base 64
                const authDataJson = JSON.parse(window.atob(decodeURIComponent(authIdbase64)));
                this.principal = new TSUser(authDataJson.vorname, authDataJson.name, '', authDataJson.email, authDataJson.roles);
                this.principal.id = authDataJson.id;
                // Raven.setUserContext({
                //     email: this.principal.email,
                //     id: this.principal.id,
                //     maxRole: this.getHighestRoleOfPrincipal(),
                // });
                this.loginEventService.broadcastEvent(TSAppEventTyp.LOGGED_IN);
            } catch (e) {
                console.log('cookie decoding failed', e);
            }
        }

        return this.principal;
    }

    public initPrincipalFromKeycloak$(): Observable<void> {
        // todo homa keycloak maybe do with events, see:
        // https://github.com/mauriciovigolo/keycloak-angular/blob/master/examples/keycloak-events/src/app/app.component.ts
        // or this.keycloakService.keycloakEvents$

        const promise = this.keycloakService.loadUserProfile(false).then((profile: KeycloakProfileWithMobileNumber) => {
            LOG.info('loaded user profile');
            const keyClRoles = this.keycloakService.getUserRoles(true);
            const odis: OrtDerImpfungJaxTS[] | undefined = []; // odis are loaded later
            if (profile.attributes !== undefined && profile.attributes.MobileNummer !== undefined) {
                this.principal = new TSUser(profile.username, profile.firstName, profile.lastName, '', profile.email,
                    this.mapRoles(keyClRoles), odis, profile.attributes.MobileNummer[0]);
            } else {
                LOG.info('Fachapplikationsbenutzer hat keine MobileNummer');
                this.principal = new TSUser(profile.username, profile.firstName, profile.lastName, '', profile.email,
                    this.mapRoles(keyClRoles), odis);
            }

            const keyInst = this.keycloakService.getKeycloakInstance();

            this.principal.id = keyInst && keyInst.tokenParsed ? keyInst.tokenParsed.sub : '';

            // Raven.setUserContext({
            //     email: this.principal!.email,
            //     id: this.principal!.id,
            //     maxRole: this.getHighestRoleOfPrincipal(),
            // });

            this.translateService.use(profile.attributes?.locale || 'de');

            LOG.info('logged in user is now ', this.principal);
            this.loginEventService.broadcastEvent(TSAppEventTyp.LOGGED_IN);

        }).catch(reason => {
            LOG.warn('could not load user profile', reason);

        });

        // concat: the principal must be loaded before we can load additional Benutzer data into it
        return from(promise);

    }

    public loadOdisForCurrentUserAndStoreInPrincipal$(forceReload: boolean): Observable<Array<OrtDerImpfungJaxTS>> {
        const loadedOdis = this.principal?.orteDerImpfung;
        // load from server if forced or if list is somehow empty
        if(forceReload || loadedOdis === undefined || loadedOdis.length === 0) {
            return this.ortderimpfungService.ortDerImpfungResourceGetAllOrtDerImpfungJax()
                .pipe(
                    map(odis => {
                        if (this.principal) {
                            this.principal.orteDerImpfung = odis;
                            LOG.info('loaded odis for logged in user', this.principal);
                        }
                        return odis;
                    }));
        }
        return of(loadedOdis);
    }

    private mapRoles(keyClRoles: string[]): Array<TSRole> {
        const resultRoles: Array<TSRole> = [];
        for (const keyClRole of keyClRoles) {
            const maybeRole: TSRole | undefined = (TSRole as any)[keyClRole];
            if (maybeRole !== undefined) {
                resultRoles.push(maybeRole);
            }
        }

        // at least we do have user role
        if (resultRoles.length === 0) {
            resultRoles.push(TSRole.UNASSIGNED_ROLE);
        }

        return resultRoles;
    }

    public hasRoleCallCenter(): boolean {

        const roles = this.getPrincipalRoles();
        if (roles === undefined) {
            return false;
        }

        for (const currRole of roles) {
            switch (currRole) {
                case TSRole.CC_AGENT:
                case TSRole.CC_BENUTZER_VERWALTER:
                    return true;
            }

        }
        return false;
    }

    public hasAnyKantonRole(): boolean {
        return this.isOneOfRoles([
            TSRole.KT_IMPFDOKUMENTATION,
            TSRole.KT_BENUTZER_VERWALTER,
            TSRole.KT_BENUTZER_REPORTER,
            TSRole.KT_LOGISTIK_REPORTER,
            TSRole.KT_MEDIZINISCHER_REPORTER,
            TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION,
            TSRole.KT_ZERTIFIKAT_AUSSTELLER,
            TSRole.KT_NACHDOKUMENTATION,
            TSRole.KT_IMPFVERANTWORTUNG
        ]);
    }

    public getHighestRoleOfPrincipal(): TSRole | undefined {
        if (this.getPrincipalRoles()) {
            return this.getHighestRoleOfBenutzer(this.getPrincipalRoles());
        }

        return undefined;
    }

// noinspection TsLint
    /* eslint-disable */
    /**
     * Gibt die am privilegierteste Rolle des  Benutzers zurueck.
     */
    public getHighestRoleOfBenutzer(roles: Array<TSRole> | undefined): TSRole {
        let hasSuperAdmin = false;
        let hasUserRole = false;
        let hasAdminRole = false;
        let hasUnassigned = false;

        if (roles == undefined) {
            return TSRole.UNASSIGNED_ROLE;
        }

        for (const currRole of roles) {
            switch (currRole) {

                case TSRole.UNASSIGNED_ROLE:
                default:
                    hasUnassigned = true;
                    break;
            }
        }


        if (hasUnassigned) {
            return TSRole.UNASSIGNED_ROLE;
        }

        return TSRole.UNASSIGNED_ROLE;
    }

    /* eslint-enable */

    public getPrincipal(): TSUser | undefined {
        return this.principal;
    }

    /**
     * Gibt true zurueck, wenn der eingelogte Benutzer die gegebene Role hat. Fuer undefined Werte wird immer false
     * zurueckgegeben.
     */
    public hasRole(role: TSRole): boolean {
        if (role && this.principal) {
            return this.principal.roles.find(currRole => currRole === role) !== undefined;
        }
        return false;
    }

    /**
     * gibt true zurueck wenn der aktuelle Benutzer eine der uebergebenen Rollen innehat
     */
    public isOneOfRoles(roles: Array<TSRole> | undefined | null): boolean {
        if (roles !== undefined && roles !== null && this.principal) {
            // eslint-disable-next-line @typescript-eslint/prefer-for-of
            for (let i = 0; i < roles.length; i++) {
                const role = roles[i];
                if (this.hasRole(role)) {
                    return true;
                }
            }
        }

        return false;
    }

    public getPrincipalRoles(): Array<TSRole> | undefined {
        if (this.principal) {
            return this.principal.roles;
        }

        return undefined;
    }

    public refreshXSRFToken$(): Observable<undefined> {
        return this.http.get<undefined>(this.serviceURL + '/refreshXSRFToken');

    }

    public getProfileUrl(): string {
        if (this.keycloakService && this.keycloakService.getKeycloakInstance()) {
            return this.keycloakService.getKeycloakInstance().createAccountUrl();
        }

        return '#';
    }

    public getKeycloakPasswordUrl(): string {
        if (this.keycloakService && this.keycloakService.getKeycloakInstance()) {
            const accountUrl = this.keycloakService.getKeycloakInstance().createAccountUrl();
            const path = accountUrl.split('?')[0];
            const params = accountUrl.split('?')[1];
            return path + '/password?' + params;
        }

        return '#';
    }

    public ensureUserUpdatedInBackend$(): Observable<undefined> {
        // this is fire and forget
        if (this.isInitialReg()) {
            return this.benutzerService.benutzerResourceRegEnsureBenutzer(undefined).pipe(first());

        } else {

            return this.benutzerService.benutzerResourceEnsureBenutzer(undefined).pipe(first());
        }
    }

    public ensureUserActive$(): Observable<boolean> {
        if (!this.isInitialReg()) {
            return this.benutzerService.benutzerResourceEnsureUserActive().pipe(first());
        }
        return of(true);
    }

    private isInitialReg(): boolean {
        return this.appConfig.appName === 'vacme-initialreg';
    }

    public getKrankheitenForUser(): Set<KrankheitIdentifierTS> {
        const odisOfUser: OrtDerImpfungJaxTS[] | undefined = this.getPrincipal()?.orteDerImpfung;
        return extractKrankheitenForOdis(odisOfUser);
    }

    public getKrankheitForUserIfOnlyAllowedForExactlyOne(): KrankheitIdentifierTS | undefined{
        const krankheiten = this.getKrankheitenForUser();
        LOG.info('Krankheiten that user has an odi for', krankheiten);
        if (krankheiten.size === 1) {
            const [onlyKrankheit] = krankheiten;
            return onlyKrankheit;
        }
        return undefined;
    }
}
