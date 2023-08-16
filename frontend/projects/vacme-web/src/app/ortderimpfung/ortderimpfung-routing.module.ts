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
import {resolveImpfslotParams} from '../service/resolver/impfslot-params-resolver.service';
import {resolveImpfstoffZugelassene} from '../service/resolver/impfstoff-zugelassene-resolver.service';
import {resolveOdiBenutzer} from '../service/resolver/oidbenutzer-resolver.service';
import {resolveOrtDerImpfung} from '../service/resolver/ortderimpfung-resolver.service';
import {CreateRequestGuardService} from '../service/termin-guard.service';
import {BenutzerErstellenPageComponent} from './Benutzerverwaltung/benutzer-erstellen-page.component';
import {OdiFilterPageComponent} from './odi-filter-page/odi-filter-page.component';
import {StammdatenPageComponent} from './stammdaten-page/stammdaten-page.component';
import {TerminverwaltungPageComponent} from './terminverwaltung-page/terminverwaltung-page.component';

const routes: Routes = [

    {
        path: 'stammdaten',
        component: StammdatenPageComponent,
        resolve: {
            impfstoffList: resolveImpfstoffZugelassene
        }
    },
    {
        path: 'stammdaten/:ortDerImpfungId',
        component: StammdatenPageComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            ortDerImpfung: resolveOrtDerImpfung,
            impfstoffList: resolveImpfstoffZugelassene
        },
    },
    {
        path: 'odifilter/:ortDerImpfungId',
        component: OdiFilterPageComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            ortDerImpfung: resolveOrtDerImpfung,
        },
    },
    {
        path: 'oidbenutzer/:ortDerImpfungIdentifier/:ortDerImpfungId',
        component: BenutzerErstellenPageComponent
    },
    {
        path: 'oidbenutzer/:ortDerImpfungIdentifier/:ortDerImpfungId/:username',
        component: BenutzerErstellenPageComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            user: resolveOdiBenutzer,
        },
    },
    {
        path: 'terminverwaltung/:ortDerImpfungId/:jahr/:monat',
        component: TerminverwaltungPageComponent,
        canDeactivate: [CreateRequestGuardService],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: resolveImpfslotParams,
            odi: resolveOrtDerImpfung
        },
    },
    {
        path: '',
        redirectTo: 'stammdaten',
        pathMatch: 'full',
    },
    {
        path: '**',
        redirectTo: '/stammdaten',
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class OrtderimpfungRoutingModule {
}
