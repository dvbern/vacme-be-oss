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
import {Router} from '@angular/router';
import {DashboardJaxTS, DossierService} from 'vacme-web-generated';
import {
    RegistrierungBasicInfoJaxTS
} from '../../../../../vacme-web-generated/src/lib/model/registrierung-basic-info-jax';
import {krankenkassenkartennummerValidator} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/krankenkassenkartennummer-validator';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {NavigationService} from '../../service/navigation.service';
import {PersonSuchenKvkNummerService} from '../../service/person-suchen-kvk-nummer.service';
import {TranslateService} from '@ngx-translate/core';

@Component({
    selector: 'app-person-suchen-kvk-nummer',
    templateUrl: './person-suchen-kvk-nummer.component.html',
    styleUrls: ['./person-suchen-kvk-nummer.component.scss']
})
export class PersonSuchenKvkNummerComponent implements OnInit {

    public formGroup!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private dossierService: DossierService,
        private navigationService: NavigationService,
        public kvkSucheService: PersonSuchenKvkNummerService,
        public translateService: TranslateService,
    ) {
    }

    ngOnInit(): void {
        const minLength = 20;
        const maxLength = 20;
        this.formGroup = this.fb.group({
            kvk: this.fb.control(this.kvkSucheService.kvkSucheText,
                [
                    Validators.minLength(minLength),
                    Validators.maxLength(maxLength),
                    Validators.required,
                    krankenkassenkartennummerValidator()]),
        });
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.suchen();
        });
    }

    private suchen(): void {
        const kvk = this.formGroup.get('kvk')?.value;
        this.kvkSucheService.kvkSucheText = kvk;
        this.dossierService.dossierResourceSearchDashboardRegistrierung(kvk).subscribe(

            (res: RegistrierungBasicInfoJaxTS[]) => {
                this.kvkSucheService.kvkSucheResult = res;
                if (res.length === 0) {
                    this.navigationService.notFoundResult();
                } else if (res.length === 1) {
                    this.openDossier(res[0]);
                }
            },
            () => {
                this.kvkSucheService.kvkSucheResult = [];
                this.navigationService.notFoundResult();
            }
        );
    }

    openDossier(reg: RegistrierungBasicInfoJaxTS): void {
        this.dossierService.dossierResourceGetDashboardRegistrierung(reg.registrierungsnummer as string).subscribe(
            (res: DashboardJaxTS) => {
                this.navigationService.navigate(res);
            },
            () => {
                this.navigationService.notFoundResult();
            }
        );
    }

    toggleKvKSuche(): void {
        if (this.kvkSucheService.displayKvKSuche) {
            this.clearKvkSuche();
        }
        this.kvkSucheService.displayKvKSuche = !this.kvkSucheService.displayKvKSuche;
    }

    clearKvkSuche(): void {
        this.formGroup.reset();
        this.kvkSucheService.kvkSucheText = undefined;
        this.kvkSucheService.kvkSucheResult = [];
    }
}
