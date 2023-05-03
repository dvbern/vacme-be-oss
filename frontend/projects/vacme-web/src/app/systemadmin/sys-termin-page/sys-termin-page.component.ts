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
import {ActivatedRoute} from '@angular/router';
import {NgbTimeStruct} from '@ng-bootstrap/ng-bootstrap';
import * as moment from 'moment';
import {map} from 'rxjs/operators';
import {DashboardJaxTS, ForceTerminJaxTS, OrtDerImpfungJaxTS, SystemadministrationService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {
    DATE_FORMAT,
    DATE_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    REGISTRIERUNGSNUMMER_LENGTH,
} from '../../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {
    parsableDateValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('SysTerminPageComponent');

@Component({
    selector: 'app-sys-termin-page',
    templateUrl: './sys-termin-page.component.html',
    styleUrls: ['./sys-termin-page.component.scss'],
})
export class SysTerminPageComponent extends BaseDestroyableComponent implements OnInit {

    public formGroup!: FormGroup;

    hourStep = 1;
    minuteStep = 30;
    public odiSelectValues: { label: string | undefined; value: string | undefined }[] = [];
    public createdTermin: DashboardJaxTS | undefined;

    constructor(
        private fb: FormBuilder,
        private activatedRoute: ActivatedRoute,
        private errorMessageService: ErrorMessageService,
        private systemAdminService: SystemadministrationService,
        private authService: AuthServiceRsService,
    ) {
        super();
    }

    ngOnInit(): void {

        this.activatedRoute.data.pipe(
            this.takeUntilDestroyed(),
            map(next => next.odis$),
            map((odiArr: Array<OrtDerImpfungJaxTS>) => {
                return odiArr.map(odi => {
                        return {label: odi.name, value: odi.id};
                    },
                );
            }),
        ).subscribe(value => this.odiSelectValues = value, error => LOG.error(error));

        this.createFormGroup();
    }

    private createFormGroup(): void {
        this.formGroup = this.fb.group({

            odiId: this.fb.control(null, [Validators.required]),
            registrierungsNummer: this.fb.control(null, [
                Validators.minLength(REGISTRIERUNGSNUMMER_LENGTH),
                Validators.maxLength(REGISTRIERUNGSNUMMER_LENGTH),
                Validators.required,
            ]),
            datum1: this.fb.control(null,
                [
                    Validators.minLength(5), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN), Validators.required, parsableDateValidator(),
                ]),
            zeit1: this.fb.control(null, Validators.required),
            datum2: this.fb.control(null,
                [
                    Validators.minLength(5),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN),
                    Validators.required,
                    parsableDateValidator(),
                ]),
            zeit2: this.fb.control(null, Validators.required),
        });
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.submit();
        });
    }

    private submit(): void {

        const forceTerminJax: ForceTerminJaxTS = this.formToModel();
        const valid = this.validateTermin2AfterTermin1(forceTerminJax);
        console.log(forceTerminJax);
        if (valid) {
            this.systemAdminService.systemadministrationResourceForceTerminbuchung(forceTerminJax)
                .subscribe(value => {
                    LOG.info(value);
                    this.createdTermin = value;
                }, error => LOG.error(error));
        }

    }

    private validateTermin2AfterTermin1(forceTerminJax: ForceTerminJaxTS): boolean {
        if (moment(forceTerminJax.termin1Time).startOf('day')
            .isSameOrAfter(moment(forceTerminJax.termin2Time).startOf('day'))) {
            this.errorMessageService.addMesageAsError('Termin2 muss nach Termin1 sein');
            return false;
        }
        if (!this.validMinutes(moment(forceTerminJax.termin1Time).minutes()) ||
            !this.validMinutes(moment(forceTerminJax.termin2Time).minutes())) {
            this.errorMessageService.addMesageAsError('Minuten muessen ganze oder halbe Stunde sein');
            return false;
        }
        return true;
    }

    private validMinutes(mins: number): boolean {
        return mins === 0 || mins === 30;
    }

    private formToModel(): ForceTerminJaxTS {
        const model: ForceTerminJaxTS = {
            ortDerImpfungId: this.getFormControlValue('odiId'),
            registrierungsNummer: this.getFormControlValue('registrierungsNummer'),

            termin1Time: this.getFormControlTermin(1),
            termin2Time: this.getFormControlTermin(2),

        };

        return model;
    }

    private getFormControlValue(field: string): any {
        return this.formGroup.get(field)?.value;
    }

    private getFormControlTermin(termNum: number): Date {
        const datekey = `datum${termNum}`;
        const timekey = `zeit${termNum}`;
        const time: NgbTimeStruct = this.getFormControlValue(timekey);
        const moment1 = moment(this.getFormControlValue(datekey), DATE_FORMAT);
        moment1.hour(time.hour).minute(time.minute);
        return moment1.toDate();
    }

    public isInRoleAsRegistrationOi(): boolean {
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI);
    }
}

