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
import {
    SysImpfstoffePageComponent,
} from './sys-impfstoffe-page/sys-impfstoffe-page.component';
import {MassenverarbeitungPageComponent} from './massenverarbeitung/massenverarbeitung-page.component';
import {SysTerminPageComponent} from './sys-termin-page/sys-termin-page.component';

import {SystemadminRoutingModule} from './systemadmin-routing.module';

@NgModule({
    declarations: [SysTerminPageComponent, MassenverarbeitungPageComponent, SysImpfstoffePageComponent],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        NgbDatepickerModule,
        NgbTimepickerModule,
        VacmeWebSharedModule,
        SystemadminRoutingModule,
    ],
})
export class SystemadminModule {
}
