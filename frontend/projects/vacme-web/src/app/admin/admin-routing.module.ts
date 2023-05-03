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

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AdminPageComponent} from './admin-page/admin-page.component';
import {DatenKorrekturPageComponent} from './daten-korrektur/daten-korrektur-page/daten-korrektur-page.component';
import {
    DatenKorrekturPersonalienSuchePageComponent,
} from './daten-korrektur/daten-korrektur-personalien-suche/daten-korrektur-personalien-suche-page.component';
import {StyleguidePageComponent} from './styleguide/styleguide-page.component';
import {DemoFormPageComponent} from './vacme-styleguide/demo-form-page.component';

const routes: Routes = [

    {
        path: '',
        component: AdminPageComponent
    },
    {
        path: 'datenkorrektur',
        component: DatenKorrekturPageComponent,
    },
    {
        path: 'datenkorrektur/suche-registrierung',
        component: DatenKorrekturPersonalienSuchePageComponent
    },
    {
        path: 'styleguide',
        component: StyleguidePageComponent
    },
    {
        path: 'demo-form',
        component: DemoFormPageComponent
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AdminRoutingModule {
}
