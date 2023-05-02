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
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {KeycloakService} from 'keycloak-angular';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {PublicService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {
    BaseDestroyableComponent,
} from '../../../../vacme-web-shared';
import {REGISTRIERUNGSNUMMER_LENGTH} from '../../../../vacme-web-shared/src/lib/constants';
import FormUtil from '../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('LandingpageComponent');

@Component({
    selector: 'app-benutzername-vergessen-page',
    templateUrl: './benutzername-vergessen-page.component.html',
    styleUrls: ['./benutzername-vergessen-page.component.scss'],
})
export class BenutzernameVergessenPageComponent extends BaseDestroyableComponent implements OnInit {

    public formGroup!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private publicService: PublicService,
        private translateService: TranslateService,
        protected keycloakAngular: KeycloakService
    ) {
        super();
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.abschicken();
        });
    }

    ngOnInit(): void {
        this.formGroup = this.fb.group({
            code: this.fb.control(undefined, [
                Validators.minLength(REGISTRIERUNGSNUMMER_LENGTH),
                Validators.maxLength(REGISTRIERUNGSNUMMER_LENGTH),
                Validators.required]),
        });
    }

    private abschicken(): void {
        const registrierungsnummer = this.formGroup.controls.code.value;
        this.publicService.publicResourceSendBenutzername(registrierungsnummer)
            .subscribe(() => {
                Swal.fire({
                    icon: 'info',
                    text: this.translateService.instant('BENUTZERNAME-VERGESSEN.SUCCESS'),
                    showConfirmButton: false,
                    showCancelButton: false,
                    timer: 1500,
                }).then(() => {
                        this.triggerLogin();
                });
            }, error => {
                LOG.error(error);
            });
    }

    private triggerLogin(): Promise<void> {
        return this.keycloakAngular.login({
            redirectUri: window.location.origin,
            locale: this.translateService.currentLang
        });
    }
}
