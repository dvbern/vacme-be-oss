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
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {CreateUmfrageJaxTS, SendUmfrageJaxTS, UmfrageGruppeTS, UmfrageService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {Option} from '../../../../../vacme-web-shared';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';

const LOG = LogFactory.createLog('UmfrageComponent');

@Component({
    selector: 'app-umfrage',
    templateUrl: './umfrage.component.html',
    styleUrls: ['./umfrage.component.scss']
})
export class UmfrageComponent implements OnInit {
    umfrageFormGroup!: FormGroup;
    smsFormGroup!: FormGroup;
    gruppen: Option[] = Object.values(UmfrageGruppeTS).map(t => {
        return {label: t, value: t};
    });

    constructor(private fb: FormBuilder,
                private authService: AuthServiceRsService,
                private umfrageService: UmfrageService,
                private translationService: TranslateService) {
    }

    ngOnInit(): void {
        this.umfrageFormGroup = this.fb.group({
            gruppe: this.fb.control(undefined, [Validators.required]),
            limit: this.fb.control(undefined, [Validators.required, Validators.min(0)])
        });

        this.smsFormGroup = this.fb.group({
            gruppe: this.fb.control(undefined, [Validators.required])
        });
    }

    enabled(): boolean {
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI);
    }

    downloadStichprobe(): void {
        if (this.umfrageFormGroup.valid) {
            const data = this.umfrageFormGroup.value as CreateUmfrageJaxTS;
            this.umfrageService.umfrageResourceCreateUmfrage(data).subscribe(csv => {
                saveAs(csv, `umfrage_${data.gruppe}_${data.limit}.csv`);
            }, err => {
                LOG.error(err);
            });
        }
    }


    sendSms(): void {
        if (this.smsFormGroup.valid) {
            const data = this.smsFormGroup.value as SendUmfrageJaxTS;
            this.umfrageService.umfrageResourceSendUmfrage(data).subscribe(_ => {
                Swal.fire({
                    icon: 'success',
                    text: this.translationService.instant('UMFRAGE.SMS_SUCCESS'),
                    showConfirmButton: true,
                }).then(() => {
                    this.smsFormGroup.reset();
                });
            }, err => {
                LOG.error(err);
            });
        }
    }

    sendReminder(): void {
        if (this.smsFormGroup.valid) {
            const data = this.smsFormGroup.value as SendUmfrageJaxTS;
            this.umfrageService.umfrageResourceSendReminder(data).subscribe(_ => {
                Swal.fire({
                    icon: 'success',
                    text: this.translationService.instant('UMFRAGE.SMS_SUCCESS'),
                    showConfirmButton: true,
                }).then(() => {
                    this.smsFormGroup.reset();
                });
            }, err => {
                LOG.error(err);
            });
        }
    }
}
