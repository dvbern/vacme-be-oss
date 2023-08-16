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
import {InfoPageComponent} from '../../../vacme-web-shared/src/lib/info-page-component/info-page.component';
import {KeycloakAppAuthGuard} from '../../../vacme-web-shared/src/lib/service/guard/keycloak-app-auth-guard.service';
import {
    resolveErstTerminAdHoc,
} from '../../../vacme-web-shared/src/lib/service/resolver/erst-termin-ad-hoc-resolver.service';
import {resolveFreieTermine} from '../../../vacme-web-shared/src/lib/service/resolver/freie-termine-resolver.service';
import {resolveImpffolge} from '../../../vacme-web-shared/src/lib/service/resolver/impffolge-resolver.service';
import {
    resolveOrtDerImpfungId,
} from '../../../vacme-web-shared/src/lib/service/resolver/ort-der-impfung-id-resolver.service';
import {resolveReferrerPart} from '../../../vacme-web-shared/src/lib/service/resolver/referrer-part-resolver.service';
/* eslint-disable max-len, , , , , , , ,  */
import {
    resolveRegistrierungsnummer,
} from '../../../vacme-web-shared/src/lib/service/resolver/registrierungsnummer-resolver.service';
/* eslint-disable max-len */
import {
    resolveRegistrierungsStatus,
} from '../../../vacme-web-shared/src/lib/service/resolver/registrierungsstatus-resolver.service';
import {resolveTermine1} from '../../../vacme-web-shared/src/lib/service/resolver/termin-1-resolver.service';
import {resolveTermine2} from '../../../vacme-web-shared/src/lib/service/resolver/termin-2-resolver.service';
import {
    resolveTermineBearbeiten,
} from '../../../vacme-web-shared/src/lib/service/resolver/termin-bearbeiten-resolver.service';
import {resolveTermineN} from '../../../vacme-web-shared/src/lib/service/resolver/termin-N-resolver.service';
import {KontrollePageComponent} from './kontrolle/kontrolle/page/kontrolle-page.component';
import {
    OdiTagesstatistikDetailPageComponent,
} from './odi-tagesstatistik-detail-page/odi-tagesstatistik-detail-page.component';
import {
    GeimpftTermineBearbeitenPageComponent,
} from './person/geimpft-termine-bearbeiten/geimpft-termine-bearbeiten-page.component';
import {GeimpftPageComponent} from './person/geimpft/geimpft-page.component';
import {
    ImpfdokumentationBoosterPageComponent,
} from './person/impfdokumentation/impfdokumentation-booster/impfdokumentation-booster-page.component';
import {
    ImpfdokumentationGrundimunisierungPageComponent,
} from './person/impfdokumentation/impfdokumentation-grundimunisierung/impfdokumentation-grundimunisierung-page.component';
import {TerminfindungWebPageComponent} from './person/terminfindung-page/terminfindung-web-page.component';
import {ReportsPageComponent} from './reports/reports-page.component';
import {QRCodeGuardService} from './service/qrcode-guard.service';
import {resolveCanBeGrundimmunisierung} from './service/resolver/can-be-grundimmunisierung-resolver.service';
import {resolveFreieImpfslotsWeb} from './service/resolver/freie-impfslots-web-resolver.service';
import {resolveGeimpft} from './service/resolver/geimpft-resolver.service';
import {resolveGeimpftTermineBearbeiten} from './service/resolver/geimpft-termine-bearbeiten-resolver.service';
import {resolveImpfdokumentation} from './service/resolver/impfdokumentation-resolver.service';
import {
    resolveImpfstoffZugelassenTagesstatistik,
} from './service/resolver/impfstoff-zugelassen-tagesstatistik-resolver.service';
import {resolveKontrolle} from './service/resolver/kontrolle-resolver.service';
import {resolveKrankheit} from './service/resolver/krankheit-resolver.service';
import {resolveOdiTagesstatistikDetailPage} from './service/resolver/odi-tagesstatistik-detail-page-resolver.service';
import {resolveOrtDerImpfungAssigned} from './service/resolver/ort-der-impfung-assigned-resolver.service';
import {resolveRegistrierung} from './service/resolver/registrierung-resolver.service';
import {resolveSelbstzahlende} from './service/resolver/selbstzahlende-resolver.service';
import {
    OdiPersonalienSuchePageComponent,
} from './start-page/odi-personalien-suche/odi-personalien-suche-page.component';
import {
    OdiPersonalienUVCISuchePageComponent,
} from './start-page/odi-personalien-uvci-suche/odi-personalien-uvci-suche-page.component';
import {StartPageComponent} from './start-page/start-page.component';
import {AccountFachappPageComponent} from './userprofile/account-fachapp-page.component';

const routes: Routes = [
    {
        path: 'startseite',
        component: StartPageComponent
    },
    {
        path: '',
        redirectTo: 'startseite',
        pathMatch: 'full',
    },
    {
        path: 'account',
        canActivate: [KeycloakAppAuthGuard],
        component: AccountFachappPageComponent,
    },
    {
        path: 'ortderimpfung',
        canActivate: [KeycloakAppAuthGuard],
        loadChildren: () => import('./ortderimpfung/ortderimpfung.module').then(m => m.OrtderimpfungModule)
    },
    {
        path: 'odistats/ortderimpfung/:odiId/date/:date',
        component: OdiTagesstatistikDetailPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: resolveOdiTagesstatistikDetailPage,
            impfstoffList: resolveImpfstoffZugelassenTagesstatistik
        },
    },
    {
        path: 'person/:registrierungsnummer/impfdokumentation/:krankheit',
        component: ImpfdokumentationGrundimunisierungPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: resolveImpfdokumentation,
            modif: resolveTermineBearbeiten,
            erstTerminAdHoc: resolveErstTerminAdHoc,
            selbstzahlende: resolveSelbstzahlende,
            krankheit: resolveKrankheit
        },
    },
    {
        path: 'odi-registrierung-suchen',
        component: OdiPersonalienSuchePageComponent,
        canActivate: [KeycloakAppAuthGuard],
    },
    {
        path: 'odi-registrierung-uvci-suchen',
        component: OdiPersonalienUVCISuchePageComponent,
        canActivate: [KeycloakAppAuthGuard],
    },
    {
        path: 'person/:registrierungsnummer/impfdokumentation/booster/:krankheit',
        component: ImpfdokumentationBoosterPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: resolveImpfdokumentation,
            modif: resolveTermineBearbeiten,
            canBeGrundimmunisierung: resolveCanBeGrundimmunisierung,
            selbstzahlende: resolveSelbstzahlende,
            krankheit: resolveKrankheit
        },
    },
    {
        pathMatch: 'full',
        path: 'person/:registrierungsnummer/impfdokumentation/booster',
        redirectTo: '/person/:registrierungsnummer/impfdokumentation/booster/UNKNOWN_KRANKHEIT',
    },
    {
        pathMatch: 'full',
        path: 'person/:registrierungsnummer/impfdokumentation',
        redirectTo: '/person/:registrierungsnummer/impfdokumentation/UNKNOWN_KRANKHEIT',
    },
    {
        path: 'person/:registrierungsnummer/geimpft',
        component: GeimpftPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: resolveGeimpft
        },
    },
    {
        path: 'person/:registrierungsnummer/geimpftterminbearbeitung/:krankheit',
        component: GeimpftTermineBearbeitenPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: resolveGeimpftTermineBearbeiten,
        },
    },
    {
        path: 'person/:registrierungsnummer/kontrolle/booster/:krankheit',
        component: KontrollePageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: resolveKontrolle,
            impfkontrolle: resolveRegistrierung,
            modif: resolveTermineBearbeiten,
            ortDerImpfungList: resolveOrtDerImpfungAssigned,
            krankheit: resolveKrankheit
        },
    },
    {
        path: 'person/:registrierungsnummer/kontrolle/booster',
        pathMatch: 'full',
        redirectTo: '/person/:registrierungsnummer/kontrolle/booster/UNKNOWN_KRANKHEIT',
    },
    {
        path: 'person/:registrierungsnummer/kontrolle',
        pathMatch: 'full',
        redirectTo: '/person/:registrierungsnummer/kontrolle/UNKNOWN_KRANKHEIT',
    },
    {
        path: 'person/:registrierungsnummer/kontrolle/:krankheit',
        component: KontrollePageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: resolveKontrolle,
            impfkontrolle: resolveRegistrierung,
            modif: resolveTermineBearbeiten,
            erstTerminAdHoc: resolveErstTerminAdHoc,
            ortDerImpfungList: resolveOrtDerImpfungAssigned,
            krankheit: resolveKrankheit
        },
    },
    {
        path: 'person/:registrierungsnummer/terminfindung/krankheit/:krankheit/:ortDerImpfungId/:impffolge/:date',
        component: TerminfindungWebPageComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            freieSlots: resolveFreieImpfslotsWeb,
            impffolge: resolveImpffolge,
            datum: resolveFreieTermine,
            odi: resolveOrtDerImpfungId,
            registrierungsnummer: resolveRegistrierungsnummer,
            modif: resolveTermineBearbeiten,
            erstTerminAdHoc: resolveErstTerminAdHoc,
            referrerPart: resolveReferrerPart,
            termin1: resolveTermine1,
            termin2: resolveTermine2,
            terminN: resolveTermineN,
            status: resolveRegistrierungsStatus,
            krankheit: resolveKrankheit,
        },
    },
    {
        path: 'person/:registrierungsnummer/terminfindung/:ortDerImpfungId/:impffolge/:date',
        pathMatch: 'full',
        redirectTo: 'person/:registrierungsnummer/terminfindung/krankheit/UNKNOWN_KRANKHEIT/:ortDerImpfungId/:impffolge/:date',
    },
    {
        path: 'dossier/:registrierungsnummer',
        component: InfoPageComponent,
        canActivate: [KeycloakAppAuthGuard, QRCodeGuardService],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
    },
    {
        path: 'dossier/:registrierungsnummer/krankheit/:krankheit',
        component: InfoPageComponent,
        canActivate: [KeycloakAppAuthGuard, QRCodeGuardService],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
    },
    {
        path: 'reports',
        canActivate: [KeycloakAppAuthGuard],
        component: ReportsPageComponent
    },
    {
        path: 'appmessage',
        canActivate: [KeycloakAppAuthGuard],
        loadChildren: () => import('./application-message/application-message.module').then(m => m.ApplicationMessageModule)
    },
    {
        path: 'admin',
        canActivate: [KeycloakAppAuthGuard],
        loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule),
    },
    {
        path: 'sysadmin',
        canActivate: [KeycloakAppAuthGuard],
        loadChildren: () => import('./systemadmin/systemadmin.module').then(m => m.SystemadminModule),
    },
    {
        path: 'infopage',
        component: InfoPageComponent,
    },
    {
        path: '**',
        redirectTo: '/startseite',
    },
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {
        scrollPositionRestoration: 'enabled',
        onSameUrlNavigation: 'reload',
        anchorScrolling: 'enabled',
        // ScrollOffset fuer ViewportScroller. Siehe this.viewportScroller.scrollToAnchor in ngAfterViewChecked
        scrollOffset: [0, 200]
    })],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
