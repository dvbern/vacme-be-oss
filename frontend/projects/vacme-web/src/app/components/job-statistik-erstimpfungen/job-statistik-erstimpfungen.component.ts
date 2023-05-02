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

import {Component} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {SystemadministrationService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {DateRangeJaxTS} from '../../../../../vacme-web-generated/src/lib/model';
import {DATE_PATTERN, DB_DEFAULT_MAX_LENGTH} from '../../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {
    parsableDateValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('JobStatistikErstimpfungenComponent');

@Component({
    selector: 'app-job-statistik-erstimpfungen',
    templateUrl: './job-statistik-erstimpfungen.component.html',
    styleUrls: ['./job-statistik-erstimpfungen.component.scss'],
})
export class JobStatistikErstimpfungenComponent {

    formGroup!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private authService: AuthServiceRsService,
        private sysadminService: SystemadministrationService,
        private translateService: TranslateService
    ) {
        this.initForm();
    }

    private initForm(): void {
        const minLength = 2;
        this.formGroup = this.fb.group({
            abrechnungFrom: this.fb.control(undefined,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN), Validators.required, parsableDateValidator()
                ]),
            abrechnungTo: this.fb.control(undefined,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN), Validators.required, parsableDateValidator()
                ]),
        });
    }

    public jobStatistikErstimpfungenEnabled(): boolean {
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI);
    }

    public runJobStatistikErstimpfungen(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            Swal.showLoading();
            // at midday to avoid timezone issues
            const model: DateRangeJaxTS = {
                von: DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungFrom')?.value),
                bis: DateUtil.parseDateAsMidday(this.formGroup.get('abrechnungTo')?.value),
            };
            this.sysadminService.systemadministrationResourceStartJobStatistikErstimpfungen(model)
                .subscribe(res => {
                    Swal.fire({
                        icon: 'success',
                        text: this.translateService.instant('SYSTEM_ADMINISTRATION.JOB_STATISTIK_ERSTIMPFUNGEN_SUCCESS'),
                        showCancelButton: false,
                        showConfirmButton: false,
                        timer: 3000,
                    });
                }, error => {
                    LOG.error('Statistik Erstimpfungen konnte nicht erstellt werden', error);
                });
        });
    }
}
