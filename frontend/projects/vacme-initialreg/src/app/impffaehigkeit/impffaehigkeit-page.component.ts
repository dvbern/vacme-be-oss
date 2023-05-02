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

import {Component, OnInit} from '@angular/core';
import {Router, UrlSerializer} from '@angular/router';
import {AmpelColorTS, KrankheitIdentifierTS, RegistrierungsCodeJaxTS, RegistrierungService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {
    BaseDestroyableComponent,
} from '../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {NavigationService} from '../service/navigation.service';

const LOG = LogFactory.createLog('ImpffaehigkeitComponent');

@Component({
    selector: 'app-impffaehigkeit-page',
    templateUrl: './impffaehigkeit-page.component.html',
    styleUrls: ['./impffaehigkeit-page.component.scss'],
})
export class ImpffaehigkeitPageComponent extends BaseDestroyableComponent implements OnInit {

    constructor(
        private authServiceRsService: AuthServiceRsService,
        private router: Router,
        private serializer: UrlSerializer,
        private registrationService: RegistrierungService,
        private navigationSerivce: NavigationService,
    ) {
        super();
    }

    public ampelColor!: AmpelColorTS;
    public consent = false;
    private needsUserRegistration = true;

    ngOnInit(): void {

        // check if the user is logged in
        if (this.authServiceRsService.getPrincipal()) {
            this.registrationService.registrierungResourceMy().subscribe(
                (res: RegistrierungsCodeJaxTS) => {
                    // if the user has no Registration yet
                    if (res === null) {
                        this.needsUserRegistration = false;
                    } else { // if the user has a registration, then forward him to the dashboard
                        this.navigationSerivce.navigateToStartpage(res.registrierungsnummer, KrankheitIdentifierTS.COVID);
                    }

                },
                (err: any) => LOG.error('HTTP Error', err));
        }
    }

    isPass(): boolean {
        const pass = this.ampelColor !== undefined && this.ampelColor !== AmpelColorTS.RED;

        // if not passing anymore, reset the consent
        if (!pass) {
            this.consent = false;
        }

        return pass;
    }

    nextStep(): Promise<void | boolean> {
        this.saveAmpelState();
        if (this.needsUserRegistration) {
            return this.triggerKeycloakRegistration();
        } else {
            return this.forwardToPersonendaten();
        }
    }

    private saveAmpelState(): void {
        localStorage.setItem('ampel', this.ampelColor);
    }

    private forwardToPersonendaten(): Promise<boolean> {
        return this.navigationSerivce.navigateToPersonendaten();
    }

    private triggerKeycloakRegistration(): Promise<void | boolean> {
        return this.authServiceRsService
            .triggerKeycloakRegister(window.location.origin + '/personendaten');
    }

    private triggerKeycloakLogout(): Promise<void> {
        return this.authServiceRsService
            .triggerKeycloakLogout(window.location.origin + '/impffaehigkeit');
    }

    public showHinweis(): boolean {
        return TenantUtil.ZURICH;
    }
}
