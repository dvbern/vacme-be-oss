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
import {ApplicationMessageResolverService} from '../service/resolver/application-message-resolver.service';
import {ApplicationMessageBearbeitenPageComponent} from './application-message-bearbeiten/application-message-bearbeiten-page.component';
import {ApplicationMessagePageComponent} from './application-message-page/application-message-page.component';

const routes: Routes = [
    {
        path: '',
        component: ApplicationMessagePageComponent
    },
    {
        path: 'bearbeiten',
        component: ApplicationMessageBearbeitenPageComponent
    },
    {
        path: 'bearbeiten/:messageId',
        component: ApplicationMessageBearbeitenPageComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            message: ApplicationMessageResolverService,
        },
    },
    {
        path: '**',
        redirectTo: ''
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class ApplicationMessageRoutingModule {
}
