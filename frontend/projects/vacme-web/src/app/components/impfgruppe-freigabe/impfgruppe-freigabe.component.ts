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
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {ApplicationPropertyJaxTS, ApplicationPropertyKeyTS, PrioritaetTS, PropertiesService} from 'vacme-web-generated';

@Component({
    selector: 'app-impfgruppe-freigabe',
    templateUrl: './impfgruppe-freigabe.component.html',
    styleUrls: ['./impfgruppe-freigabe.component.scss'],
})
export class ImpfgruppeFreigabeComponent implements OnInit {

    formGroup!: FormGroup;
    private readonly LAST_VAC_PRIORITY = PrioritaetTS.T;
    private readonly AD_HOC_PRIORITIES = [PrioritaetTS.X, PrioritaetTS.Y, PrioritaetTS.Z];
    public impfgruppeFreigabe?: PrioritaetTS[];
    public impfgruppeOptions?: any = [];

    constructor(
        private fb: FormBuilder,
        private propertiesService: PropertiesService,
    ) {
        this.initForm();
        this.initFreigabeOptions();
    }

    ngOnInit(): void {
        this.propertiesService.applicationPropertyResourceGetApplicationProperty(ApplicationPropertyKeyTS.PRIO_FREIGEGEBEN_BIS)
            .pipe()
            .subscribe((result: ApplicationPropertyJaxTS) => {
                    this.impfgruppeFreigabe = ((result.value as string).split('-') as PrioritaetTS[]);
                    this.initFreigabeOptions();
                    this.formGroup.get('impfgruppe')?.setValue(this.impfgruppeFreigabe);
                },
                error => {
                    console.error('Die aktuell freigegebene Impfgruppe kann nicht ermittelt werden', error);
                });
    }

    private initForm(): void {
        this.formGroup = this.fb.group({
            impfgruppe: this.fb.control(this.impfgruppeFreigabe,
                [
                    Validators.minLength(1),
                    Validators.required,
                ]),
        });
    }

    private initFreigabeOptions(): void {
        const FIRST_VAC_PRIORITY =  PrioritaetTS.A;
        this.impfgruppeOptions = Object.values(PrioritaetTS)
            .filter(p => (p >= FIRST_VAC_PRIORITY))
            .filter(p => (p <= this.LAST_VAC_PRIORITY))
            .concat(...this.AD_HOC_PRIORITIES)
            .map(p => {
                return {label: p, value: p};
            });
    }

    public onImpfgruppeFreigeben(): void {
        this.impfgruppeFreigabe = this.formGroup.get('impfgruppe')?.value;
        this.propertiesService.applicationPropertyResourceImpfgruppeFreigeben(this.impfgruppeFreigabe)
            .subscribe(res => {
                Swal.fire({
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false,
                });
                this.initFreigabeOptions();
            }, error => console.error('Die Impfgruppe konnte nicht freigegeben werden', error));
    }

}
