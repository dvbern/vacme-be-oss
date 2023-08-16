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
    resolveFreieTermine,
} from '../../../../vacme-web-shared/src/lib/service/resolver/freie-termine-resolver.service';
import {resolveImpffolge} from '../../../../vacme-web-shared/src/lib/service/resolver/impffolge-resolver.service';
import {
    resolveOrtDerImpfungId,
} from '../../../../vacme-web-shared/src/lib/service/resolver/ort-der-impfung-id-resolver.service';
// eslint-disable-next-line max-len
import {
    resolveRegistrierungsnummer,
} from '../../../../vacme-web-shared/src/lib/service/resolver/registrierungsnummer-resolver.service';
// eslint-disable-next-line max-len
import {
    resolveRegistrierungsStatus,
} from '../../../../vacme-web-shared/src/lib/service/resolver/registrierungsstatus-resolver.service';
import {resolveTermine1} from '../../../../vacme-web-shared/src/lib/service/resolver/termin-1-resolver.service';
import {resolveTermine2} from '../../../../vacme-web-shared/src/lib/service/resolver/termin-2-resolver.service';
import {
    resolveTermineBearbeiten,
} from '../../../../vacme-web-shared/src/lib/service/resolver/termin-bearbeiten-resolver.service';
import {resolveTermineN} from '../../../../vacme-web-shared/src/lib/service/resolver/termin-N-resolver.service';
import {resolveKrankheit} from '../../../../vacme-web/src/app/service/resolver/krankheit-resolver.service';
import {UnsavedChangesGuard} from '../../../../vacme-web/src/app/service/unsaved-changes-guard.service';
import {resolveDossier} from '../service/resolver/dossier-resolver.service';
import {resolveFreieImpfslots} from '../service/resolver/freie-impfslots-resolver.service';
import {ErkrankungPageComponent} from './erkrankung-page/erkrankung-page.component';
import {TerminfindungPageComponent} from './terminfindung-page/terminfindung-page.component';

const routes: Routes = [

    {
        path: 'terminfindung/krankheit/:krankheit/:ortDerImpfungId/:impffolge/:date',
        component: TerminfindungPageComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            freieSlots: resolveFreieImpfslots,
            impffolge: resolveImpffolge,
            datum: resolveFreieTermine,
            odi: resolveOrtDerImpfungId,
            registrierungsnummer: resolveRegistrierungsnummer,
            modif: resolveTermineBearbeiten,
            termin1: resolveTermine1,
            termin2: resolveTermine2,
            terminN: resolveTermineN,
            status: resolveRegistrierungsStatus,
            krankheit: resolveKrankheit
        },
    },
    {
        path: 'erkrankungen/krankheit/:krankheit',
        component: ErkrankungPageComponent,
        canDeactivate: [UnsavedChangesGuard],
        resolve: {
            dossier: resolveDossier
        }
    },
    {
        path: '',
        redirectTo: 'start',
        pathMatch: 'full',
    },
    {
        path: '**',
        redirectTo: '/start',
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
})
export class RegistrierungRoutingModule {
}
