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
import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {Subscription} from 'rxjs';
import {
    DossierService,
    ImpfdossierSummaryJaxTS,
    KrankheitIdentifierTS,
    PropertiesService,
    RegistrierungsCodeJaxTS,
    RegistrierungService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {NavigationService} from '../service/navigation.service';

const LOG = LogFactory.createLog('LandingpageComponent');

@Component({
    selector: 'app-landing-page',
    templateUrl: './landing-page.component.html',
    styleUrls: ['./landing-page.component.scss']
})
export class LandingPageComponent extends BaseDestroyableComponent implements OnInit, OnDestroy {

    public generalInfoMessage: string | undefined;

    private langChangeSubscription: Subscription | undefined;

    public fsmeInfoExpanded = false;

    constructor(
        protected authServiceRsService: AuthServiceRsService,
        protected registrationService: RegistrierungService,
        protected propertiesService: PropertiesService,
        protected translateService: TranslateService,
        protected vacmeSettingsService: VacmeSettingsService,
        protected router: Router,
        protected navigationService: NavigationService,
        protected dossierService: DossierService,
        @Inject(DOCUMENT) protected document: Document,
    ) {
        super();
    }

    ngOnInit(): void {

        // check if the user is logged in
        if (this.authServiceRsService.getPrincipal()) {

            if (this.authServiceRsService.hasRoleCallCenter()) {
                this.router.navigate(['callcenter']);
            } else {


                this.registrationService.registrierungResourceMy().subscribe(
                    (res: RegistrierungsCodeJaxTS) => {
                        // if the user has a registration we go to overview
                        // AND If the user has not logged in for a year, then go to the page with the survey about the current data
                        if (res !== null && res.registrierungsnummer !== undefined) {
                            this.navigateToDefaultStartpage(res.registrierungsnummer);
                        } else {
                            this.navigationService.navigatePostKeycloakRegistration();
                        }
                    },
                    (err: any) => LOG.error('HTTP Error', err));
            }
        }

        this.generalInfoMessage = this.getInfomessageForCurrentLanguage();
        this.langChangeSubscription = this.translateService.onLangChange
            .subscribe((event: LangChangeEvent) => {
                this.generalInfoMessage = this.getInfomessageForCurrentLanguage();
            }, () => {
                LOG.error('switich lang err');
            });
    }

    triggerKeycloakLogin(): Promise<void> {
        return this.authServiceRsService.triggerKeycloakLogin();
    }

    public isAlreadyLoggedIn(): boolean {
        return !!this.authServiceRsService.getPrincipal();
    }

    public showOnboardingWelcomeText(): boolean {
        return this.vacmeSettingsService.showOnboardingWelcomeText;
    }
    public showGeneralInfomessage(): boolean {
        return this.vacmeSettingsService.showGeneralInfomessage;
    }

    private getInfomessageForCurrentLanguage(): string | undefined{
        return this.vacmeSettingsService.getInfomessageForcurrentLanguage();

    }

    public showAngeboteneImpfungen(): boolean {
        return TenantUtil.isMultiKrankheitGUI();
    }

    public showFSMESidebar(): boolean {
        return TenantUtil.isMultiKrankheitGUI();
    }

    ngOnDestroy(): void {
        this.langChangeSubscription?.unsubscribe();
    }

    public goToRegistration(): void {
        this.navigationService.navigatePreRegistration( () => this.authServiceRsService
            .triggerKeycloakRegister(window.location.origin + '/personendaten'));
    }

    public goToPostRegistration(): void {
        this.navigationService.navigatePostKeycloakRegistration();
    }

    public navigateToDefaultStartpage(registrierungsnummer: string): void {
        this.navigationService.navigateToStartpage(registrierungsnummer, KrankheitIdentifierTS.COVID);
    }
}
