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
import {TranslateModule} from '@ngx-translate/core';
import {VacmeWebSharedModule} from 'vacme-web-shared';
import {BenutzerErstellenPageComponent} from './Benutzerverwaltung/benutzer-erstellen-page.component';
import {BenutzerlisteComponent} from './Benutzerverwaltung/benutzerliste.component';
import {SortableHeaderDirective} from './Benutzerverwaltung/sortable.directive';
import {OrtderimpfungRoutingModule} from './ortderimpfung-routing.module';
import {StammdatenPageComponent} from './stammdaten-page/stammdaten-page.component';
import {TerminverwaltungPageComponent} from './terminverwaltung-page/terminverwaltung-page.component';
import { OdiFilterPageComponent } from './odi-filter-page/odi-filter-page.component';

@NgModule({
    declarations: [StammdatenPageComponent,
        TerminverwaltungPageComponent,
        BenutzerlisteComponent,
        BenutzerErstellenPageComponent,
        SortableHeaderDirective,
        OdiFilterPageComponent],
    exports: [],
    imports: [
        CommonModule,
        VacmeWebSharedModule,
        FormsModule,
        ReactiveFormsModule,
        OrtderimpfungRoutingModule,
        TranslateModule.forChild()
    ]
})
export class OrtderimpfungModule {
}
