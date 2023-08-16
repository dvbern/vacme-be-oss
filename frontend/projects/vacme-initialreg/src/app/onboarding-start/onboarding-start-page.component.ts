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
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {OnboardingService, StartOnboardingJaxTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../vacme-web-shared';
import {
    DATE_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    JUMP_TO_KC_REGISTRAION,
    JUMP_TO_KC_REGISTRAION_TTL,
    ONBOARDING_ACTIVE_KEY,
    ONBOARDING_TOKEN_TTL,
} from '../../../../vacme-web-shared/src/lib/constants';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {ErrorMessageService} from '../../../../vacme-web-shared/src/lib/service/error-message.service';
import {datumInPastValidator} from '../../../../vacme-web-shared/src/lib/util/customvalidator/datum-in-past-validator';
import {parsableDateValidator} from '../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../vacme-web-shared/src/lib/util/FormUtil';
import LocalstoreUtil from '../../../../vacme-web-shared/src/lib/util/LocalstoreUtil';

const LOG = LogFactory.createLog('OnboardingStartComponent');

/**
 * This is the landingpage for the onboarding process. The user has to enter its onboardincode and birthday
 * to trigger the process
 */
@Component({
    selector: 'app-onboarding-start-page',
    templateUrl: './onboarding-start-page.component.html',
    styleUrls: ['./onboarding-start-page.component.scss'],
})
export class OnboardingStartPageComponent extends BaseDestroyableComponent implements OnInit {

    public formGroup!: UntypedFormGroup;

    private saveRequestPending = false;

    constructor(
        private authServiceRsService: AuthServiceRsService,
        private onboardingService: OnboardingService,
        private errorMessageService: ErrorMessageService,
        private translateService: TranslateService,
        private fb: UntypedFormBuilder,
        public router: Router,
    ) {
        super();
    }

    ngOnInit(): void {
        // jump to keycloak registration:
        if (LocalstoreUtil.getWithExpiry(JUMP_TO_KC_REGISTRAION)) {
            LocalstoreUtil.delete(JUMP_TO_KC_REGISTRAION);
            void this.triggerKeycloakRegistration();
        }

        // form
        this.setupForm();
    }

    private setupForm(): void {
        this.formGroup = this.fb.group({
            onboardingcode: this.fb.control(undefined,
                [Validators.minLength(10), Validators.maxLength(11), Validators.required]),
            geburtsdatum: this.fb.control(undefined,
                [
                    Validators.minLength(5),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN),
                    Validators.required,
                    parsableDateValidator(),
                    datumInPastValidator(),
                ]),
        });
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.save();
        });
    }

    private save(): void {
        if (this.saveRequestPending) {
            return;
        }

        this.submitBasic();

    }

    private submitBasic(): void {

        this.saveRequestPending = true; // can only run one save request at a time
        const value: StartOnboardingJaxTS = {
            language: this.translateService.currentLang,
            onboardingcode: this.formGroup.get('onboardingcode')?.value,
            geburtsdatum: DateUtil.parseDateAsMidday(this.formGroup.get('geburtsdatum')?.value),
        } as StartOnboardingJaxTS;
        this.onboardingService.onboardingRegResourceStartOnboardingBeforeKeycloakRegistration(value).subscribe(
            () => {
                this.saveRequestPending = false;
                this.saveOnboardingTokenTimeToLive();
                this.registerOrUseUser();
            },
            (err: any) => {
                this.saveRequestPending = false;
                // Fehlermeldung wird wegen dem Interceptor sowieso schon angezeigt
                LOG.error('HTTP Error', err);
            },
        );
    }

    private saveOnboardingTokenTimeToLive(): void {
        // saves a key that will expire in a certain timeframe so we know if a cookie should still be present on the
        // client
        LocalstoreUtil.setWithExpiry(ONBOARDING_ACTIVE_KEY, 'true', ONBOARDING_TOKEN_TTL);
    }

    private registerOrUseUser(): void {

        if (this.isAlreadyLoggedIn()) {
            void Swal.fire({
                icon: 'question',
                text: this.translateService.instant('ONBOARDING.START.IS_LOGGED_IN'),
                showCancelButton: true,
                cancelButtonText: this.translateService.instant('ONBOARDING.START.LOGOUT'),
                confirmButtonText: this.translateService.instant('ONBOARDING.START.KEEP_USER'),
            }).then(r => {
                if (r.isConfirmed) {
                    // keep user
                    void this.router.navigate(['/onboarding-process']);
                } else {
                    // logout and then create new user
                    this.triggerKeycloakLogoutAndRegistration();
                }
            });
        } else {
            // create new user
            void this.triggerKeycloakRegistration();
        }
    }

    private triggerKeycloakLogoutAndRegistration(): void {
        // trigger the registration directly afterwards
        LocalstoreUtil.setWithExpiry(JUMP_TO_KC_REGISTRAION, 'true', JUMP_TO_KC_REGISTRAION_TTL);
        // explanation: if we trigger the registration directly, it  does not work! after reg form Keycloak says
        // "You are already authenticated as different user 'lore8' in this session. Please log out first."

        // logout and come back here
        this.authServiceRsService.logout(this.router, '/onboarding');

    }

    private triggerKeycloakRegistration(): Promise<void | boolean> {
        return this.authServiceRsService
            .triggerKeycloakRegister(window.location.origin + '/onboarding-process');
    }

    public isAlreadyLoggedIn(): boolean {
        return !!this.authServiceRsService.getPrincipal();
    }

}
