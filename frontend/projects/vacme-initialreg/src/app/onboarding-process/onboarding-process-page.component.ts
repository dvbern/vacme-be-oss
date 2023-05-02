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
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
	KrankheitIdentifierTS,
	OnboardingService,
	RegistrierungsCodeJaxTS,
	RegistrierungService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
// eslint-disable-next-line max-len
import {
	BaseDestroyableComponent,
} from '../../../../vacme-web-shared'; // nur das JS
// importieren
import {ONBOARDING_ACTIVE_KEY} from '../../../../vacme-web-shared/src/lib/constants';
import LocalstoreUtil from '../../../../vacme-web-shared/src/lib/util/LocalstoreUtil';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {NavigationService} from '../service/navigation.service';

const LOG = LogFactory.createLog('OnboardingProcessComponent');

/**
 * This component is used to "finish" the onboarding process once the user has created an account
 */
@Component({
    selector: 'app-onboarding-process-page',
    templateUrl: './onboarding-process-page.component.html',
    styleUrls: ['./onboarding-process-page.component.scss'],
})
export class OnboardingProcessPageComponent extends BaseDestroyableComponent implements OnInit {

    private saveRequestPending = false;

    constructor(
        private router: Router,
        private registrierungService: RegistrierungService,
        private onboardingService: OnboardingService,
        private translateService: TranslateService,
		private navigationService: NavigationService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.processOnboardingToken();
    }

    private processOnboardingToken(): void {
        const activeOnboarding = LocalstoreUtil.getWithExpiry(ONBOARDING_ACTIVE_KEY);
        if (activeOnboarding) {
            // Onboarding noch nicht abgelaufen: Onboarding abschliessen
            LocalstoreUtil.delete(ONBOARDING_ACTIVE_KEY);
            this.finishOnboarding();
        } else {
            // Onboarding und daher Cookie abgelaufen: weg von hier.
            Swal.fire({
                icon: 'warning',
                text: this.translateService.instant('ONBOARDING.ERROR_TIMEOUT'),
                showCancelButton: false,
            }).then(() => this.router.navigate(['/onboarding']));
        }
    }

    /**
     * The onboarding token is saved in a cookie that can only be accessed from the server, not in javascript
     */
    public finishOnboarding(): void {
        this.saveRequestPending = true; // can only run one save request at a time
        this.onboardingService.onboardingRegResourceFinishOnboardingAfterKeycloakRegistration().subscribe(
            (res: string) => {
                this.saveRequestPending = false;
                const message = '<p>' + this.translateService.instant('ONBOARDING.SUCCESS.MESSAGE_1') + '</p>';

                Swal.fire({
                    icon: 'success',
                    title: this.translateService.instant('ONBOARDING.SUCCESS.TITLE'),
                    html: message,
                    showConfirmButton: true,
                });
                LocalstoreUtil.delete(ONBOARDING_ACTIVE_KEY);
                this.redirectToNormal();

            },
            (err: any) => {
                LOG.error('HTTP Error', err);
                this.saveRequestPending = false;
                setTimeout(() => {
                    this.redirectToNormal();
                }, 6000);
            },
        );

    }

    private redirectToNormal(): void {
        this.registrierungService.registrierungResourceMy().subscribe(
            (res: RegistrierungsCodeJaxTS) => {
                if (res !== null) {
                    // if the user has a registration we go to his overview
					this.navigationService.navigateToStartpage(res.registrierungsnummer, KrankheitIdentifierTS.COVID);
                } else {
                    // no registration? back to start
                    this.router.navigate(['/start']);
                }
            },
            (err: any) =>
                // no registration? back to start
                this.router.navigate(['/start']));
    }

}
