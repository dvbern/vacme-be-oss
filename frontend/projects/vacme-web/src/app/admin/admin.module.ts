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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgbDatepickerModule, NgbTimepickerModule} from '@ng-bootstrap/ng-bootstrap';
import {VacmeWebSharedModule} from 'vacme-web-shared';
import {AdminPageComponent} from './admin-page/admin-page.component';

import {AdminRoutingModule} from './admin-routing.module';
import {DatenKorrekturAccountLoeschenComponent} from './daten-korrektur/daten-korrektur-accout-loeschen/daten-korrektur-account-loeschen.component';
import {DatenKorrekturImpfungDatumComponent} from './daten-korrektur/daten-korrektur-impfung-datum/daten-korrektur-impfung-datum.component';
import {DatenKorrekturImpfungLoeschenComponent} from './daten-korrektur/daten-korrektur-impfung-loeschen/daten-korrektur-impfung-loeschen.component';
import {DatenKorrekturImpfungVerabreichungComponent} from './daten-korrektur/daten-korrektur-impfung-verabreichung/daten-korrektur-impfung-verabreichung.component';
import {DatenKorrekturImpfungDatenComponent} from './daten-korrektur/daten-korrektur-impfung/daten-korrektur-impfung-daten.component';
import {DatenKorrekturOdiComponent} from './daten-korrektur/daten-korrektur-odi/daten-korrektur-odi.component';
import {
    DatenKorrekturOnboardingComponent
} from './daten-korrektur/daten-korrektur-onboarding/daten-korrektur-onboarding.component';
import {DatenKorrekturPageComponent} from './daten-korrektur/daten-korrektur-page/daten-korrektur-page.component';
/* eslint-disable max-len */
import {DatenKorrekturPersonendatenComponent} from './daten-korrektur/daten-korrektur-personendaten/daten-korrektur-personendaten.component';
import {DatenKorrekturZertifikatRevokeAndRecreateComponent} from './daten-korrektur/daten-korrektur-zertifikat-revokeandrecreate/daten-korrektur-zertifikat-revokeandrecreate.component';
import {StyleguidePageComponent} from './styleguide/styleguide-page.component';
import {DemoFormPageComponent} from './vacme-styleguide/demo-form-page.component';
import { DatenKorrekturZertifikatComponent } from './daten-korrektur/daten-korrektur-zertifikat/daten-korrektur-zertifikat.component';
import { DatenKorrekturPersonalienSuchePageComponent } from './daten-korrektur/daten-korrektur-personalien-suche/daten-korrektur-personalien-suche-page.component';
import { DatenKorrekturEmailComponent } from './daten-korrektur/daten-korrektur-email/daten-korrektur-email.component';
import { DatenKorrekturSelbszahlendeComponent } from './daten-korrektur/daten-korrektur-selbszahlende/daten-korrektur-selbszahlende.component';

@NgModule({
    declarations: [
        AdminPageComponent,
        StyleguidePageComponent,
        DemoFormPageComponent,
        DatenKorrekturPageComponent,
        DatenKorrekturImpfungDatenComponent,
        DatenKorrekturAccountLoeschenComponent,
        DatenKorrekturOdiComponent,
        DatenKorrekturImpfungLoeschenComponent,
        DatenKorrekturImpfungVerabreichungComponent,
        DatenKorrekturImpfungDatumComponent,
        DatenKorrekturPersonendatenComponent,
        DatenKorrekturZertifikatComponent,
        DatenKorrekturZertifikatRevokeAndRecreateComponent,
        DatenKorrekturPersonalienSuchePageComponent,
        DatenKorrekturEmailComponent,
        DatenKorrekturSelbszahlendeComponent,
        DatenKorrekturOnboardingComponent
    ],
    imports: [
        CommonModule,
        AdminRoutingModule,
        VacmeWebSharedModule,
        FormsModule,
        ReactiveFormsModule,
        NgbDatepickerModule,
        NgbTimepickerModule,
    ]
})
export class AdminModule {
}
