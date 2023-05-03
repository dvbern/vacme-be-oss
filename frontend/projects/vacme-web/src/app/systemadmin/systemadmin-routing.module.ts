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
import {
    SysImpfstoffePageComponent
} from './sys-impfstoffe-page/sys-impfstoffe-page.component';
import {KeycloakAppAuthGuard} from '../../../../vacme-web-shared/src/lib/service/guard/keycloak-app-auth-guard.service';
import {ApplicationHealthPageComponent} from './application-health/application-health-page.component';
import {MassenverarbeitungPageComponent} from './massenverarbeitung/massenverarbeitung-page.component';
import {SysTerminPageResolverResolver} from './sys-termin-page/sys-termin-page-resolver.resolver';
import {SysTerminPageComponent} from './sys-termin-page/sys-termin-page.component';
import {SystemPageComponent} from './systemadministration/system-page.component';

const routes: Routes = [


    {
        path: 'termin',
        component: SysTerminPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        resolve: {
            odis$: SysTerminPageResolverResolver
        }
    },
    {
        path: 'masseverarbeitung',
        component: MassenverarbeitungPageComponent,
        canActivate: [KeycloakAppAuthGuard]
    },
    {
        path: 'system',
        canActivate: [KeycloakAppAuthGuard],
        component: SystemPageComponent
    },
    {
        path: 'impfstoff',
        canActivate: [KeycloakAppAuthGuard],
        component: SysImpfstoffePageComponent
    },
    {
        path: 'application-health',
        canActivate: [KeycloakAppAuthGuard],
        component: ApplicationHealthPageComponent
    },
    {
        path: '',
        redirectTo: 'termin'
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class SystemadminRoutingModule {
}
