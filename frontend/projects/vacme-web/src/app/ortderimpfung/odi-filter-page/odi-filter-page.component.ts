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
import {UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {OdiFilterJaxTS, OdiFilterTypTS, OrtderimpfungService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, Option} from '../../../../../vacme-web-shared';
import {
    BASE_DECIMAL_MAX_LENGTH,
    DB_DEFAULT_MAX_LENGTH,
    REGEX_NUMBER,
} from '../../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('OdiFilterPageComponent');

@Component({
    selector: 'app-odi-filter-page',
    templateUrl: './odi-filter-page.component.html',
    styleUrls: ['./odi-filter-page.component.scss'],
})
export class OdiFilterPageComponent extends BaseDestroyableComponent implements OnInit {

    public formGroup!: UntypedFormGroup;
    public filterTypOptions: Option[] = Object.values(OdiFilterTypTS).map(f => {
        return {label: f, value: f};
    });
    private odiId: string | undefined;

    constructor(
        private fb: UntypedFormBuilder,
        private activeRoute: ActivatedRoute,
        private authService: AuthServiceRsService,
        private ortderimpfungService: OrtderimpfungService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.formGroup = this.fb.group({
            filters: this.fb.array([]),
        });
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(data => {
                this.odiId = data.ortDerImpfung?.id;
                this.refreshWithRemoteFilter();
            }, error => LOG.error(error));
    }

    private refreshWithRemoteFilter(): void {
        if (!!this.odiId) {
            this.ortderimpfungService.ortDerImpfungResourceGetFilters(this.odiId)
                .subscribe(filters => {
                    if (!!filters) {
                        this.updateFormWithRemoteFilter(filters);
                    }
                }, error => LOG.error(error));
        }
    }

    public getFilterGroups(): UntypedFormGroup[] {
        return this.getFiltersArray().controls as UntypedFormGroup[];
    }

    public getFiltersArray(): UntypedFormArray {
        return this.formGroup.get('filters') as UntypedFormArray;
    }

    public addFilter(): void {
        this.getFiltersArray().push(this.createFilterGroup());
    }

    public removeFilter(filter: UntypedFormGroup): void {
        const filterArray = this.getFiltersArray();
        const index = filterArray.controls.indexOf(filter);
        filterArray.removeAt(index);
    }

    private createFilterGroup(filter?: OdiFilterJaxTS): UntypedFormGroup {
        return this.fb.group({
            typ: this.fb.control(filter?.typ,
                [Validators.required]),
            minimalWert: this.fb.control(filter?.minimalWert,
                [Validators.pattern(REGEX_NUMBER), Validators.maxLength(BASE_DECIMAL_MAX_LENGTH)]),
            maximalWert: this.fb.control(filter?.maximalWert,
                [Validators.pattern(REGEX_NUMBER), Validators.maxLength(BASE_DECIMAL_MAX_LENGTH)]),
            stringArgument: this.fb.control(filter?.stringArgument, Validators.maxLength(DB_DEFAULT_MAX_LENGTH)),
        });
    }

    private updateFormWithRemoteFilter(filters: OdiFilterJaxTS[]): void {
        const filterArray = this.getFiltersArray();
        filterArray.clear();
        for (const filter of filters) {
            filterArray.push(this.createFilterGroup(filter));
        }
    }

    public save(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            if (this.odiId) {
                console.log(this.extractJax());
                this.ortderimpfungService.ortDerImpfungResourceUpdateFilters(
                    this.odiId, this.extractJax())
                    .subscribe(next => {
                        Swal.fire({
                            icon: 'success',
                            timer: 1500,
                            showConfirmButton: false,
                        });
                        this.refreshWithRemoteFilter();
                    }, error => {
                        LOG.error(error);
                    });
            }
        });
    }

    private extractJax(): OdiFilterJaxTS[] {
        const filterGroups = this.getFilterGroups();
        return filterGroups.map(group => {
            const stringArgument = group.get('stringArgument')?.value;
            return {
                maximalWert: group.get('maximalWert')?.value,
                minimalWert: group.get('minimalWert')?.value,
                stringArgument: !!stringArgument ? stringArgument : null,
                typ: group.get('typ')?.value,
            };
        });
    }

    public isAsRegistrationOi(): boolean {
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI);
    }
}
