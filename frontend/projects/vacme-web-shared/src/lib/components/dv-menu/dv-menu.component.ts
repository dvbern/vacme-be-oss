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

import {DOCUMENT} from '@angular/common';
import {ChangeDetectorRef, Component, Inject, Input, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {canton} from 'projects/vacme-web-shared/src/cantons/canton';
import {Subscription} from 'rxjs';
import {filter} from 'rxjs/operators';
import {libExpansionAnimations} from '../../animations/expansion-animations';

import {LogFactory} from '../../logging';
import {TSAppEventTyp} from '../../model';
import {LoginEventService} from '../../service';
import {DVWindowService} from '../../service/app-window-service';
import {AuthServiceRsService} from '../../service/auth-service-rs.service';
import {TerminfindungResetService} from '../../service/terminfindung-reset.service';
import {APP_CONFIG, AppConfig} from '../../types';

const LOG = LogFactory.createLog('DvMenuComponent');

export type ElementExpansionState = 'expanded' | 'collapsed';

@Component({
    selector: 'lib-menu',
    templateUrl: './dv-menu.component.html',
    styleUrls: ['./dv-menu.component.scss'],
    animations: [libExpansionAnimations.elementExpansion, libExpansionAnimations.elementFadeIn],
})
export class DvMenuComponent implements OnInit, OnDestroy {

    private msgSub?: Subscription;

    public appName = '';

    public profileName = 'Profile';
    public profileUsername ? = '';

    @Input()
    public helpURL = '';

    constructor(private authServiceRsService: AuthServiceRsService,
                private router: Router,
                @Inject(APP_CONFIG) private appConfig: AppConfig,
                @Inject(DOCUMENT) private document: any,
                private loginEventService: LoginEventService, private cdRef: ChangeDetectorRef,
                public translate: TranslateService,
                public windowService: DVWindowService,
                public terminfindungResetService: TerminfindungResetService,
    ) {
        // this language will be used as a fallback when a translation isn't found in the current language
        translate.setDefaultLang('de');
        // the lang to use, if the lang isn't available, it will use the current loader to get them

        const browserLang: any = translate.getBrowserLang();
        if (browserLang !== undefined && browserLang.startsWith('fr')) {
            this.translate.use('fr');
        } else if (browserLang !== undefined && browserLang.startsWith('en') && this.hasEnglishTranslationEnabled()) {
            this.translate.use('en');
        } else {
            this.translate.use('de');
        }
        // wenn die gewahelte sprache wechselt dann wollen wir auch dass das root html tag sein lang attribut wechselt
        this.translate.onLangChange
            .subscribe((event: LangChangeEvent) => {
                this.document.documentElement.lang = event.lang;
            }, () => {
                LOG.error('switich lang err');
            });

    }

    public getCurrentLang(): string {
        return this.translate.currentLang;
    }

    public getEnvironmentClass(): string {
        const hostname = this.windowService.getHostname();

        if (hostname.includes('uat')) {
            return 'dv-menu-container-uat';
        }
        if (hostname.includes('localhost')) {
            return 'dv-menu-container-local';
        }
        if (hostname.includes('dev')) {
            return 'dv-menu-container-dev';
        }
        if (hostname.includes('demo')) {
            return 'dv-menu-container-demo';
        }

        return '';
    }

    public hasEnglishTranslationEnabled(): boolean {
        return this.appConfig.appName === 'vacme-initialreg' && canton.name === 'zh';
    }

    public ngOnInit(): void {

        this.appName = this.appConfig.appName === 'vacme-web' ? 'VACME.TITLEIMPFEN' : 'VACME.TITLEPORTAL';

        this.msgSub = this.loginEventService.appEventStream$.pipe(
            filter(event => event === TSAppEventTyp.LOGGED_OUT || event === TSAppEventTyp.LOGGED_IN),
        ).subscribe(() => {
            this.recalculateProfileName();
            this.cdRef.markForCheck();
        }, error => LOG.error(error));

    }

    public triggerKeycloakLogin(): Promise<void> {
        return this.authServiceRsService.triggerKeycloakLogin();
    }

    public getCurrentProfileName(): string | undefined {
        const principal = this.authServiceRsService.getPrincipal();
        if (principal) {
            return principal.getFullName();
        }

        return undefined;
    }

    private getCurrentProfileUsername(): string | undefined {
        const principal = this.authServiceRsService.getPrincipal();
        if (principal) {
            return principal.username;
        }

        return undefined;

    }

    private recalculateProfileName(): void {
        if (this.getCurrentProfileName()) {
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            this.profileName = this.getCurrentProfileName()!;
        }
        this.profileUsername = this.getCurrentProfileUsername();

    }

    public getCurrentRoute(): string {
        return this.router.url;
    }

    public showBackLink(): boolean {
        return !this.getCurrentRoute().includes('start');
    }

    public logout(): void {
        this.authServiceRsService.logout(this.router, '/start');
    }

    public isLoggedIn(): boolean {
        return !!this.authServiceRsService.getPrincipal();
    }

    public ngOnDestroy(): void {
        if (this.msgSub) {
            this.msgSub.unsubscribe();
        }
    }

    getLogoUrl(): string {
        return canton.logo;
    }

    public resetDataAndReturnToStart(): void {
        this.terminfindungResetService.resetData();
        this.router.navigate(['/']);
    }
}
