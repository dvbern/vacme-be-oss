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
    ErstTerminAdHocResolverService,
} from '../../../vacme-web-shared/src/lib/service/resolver/erst-termin-ad-hoc-resolver.service';
import {
    FreieTermineResolverService,
} from '../../../vacme-web-shared/src/lib/service/resolver/freie-termine-resolver.service';
import {ImpffolgeResolverService} from '../../../vacme-web-shared/src/lib/service/resolver/impffolge-resolver.service';
import {
    OrtDerImpfungIdResolverService,
} from '../../../vacme-web-shared/src/lib/service/resolver/ort-der-impfung-id-resolver.service';
import {
    ReferrerPartResolverService,
} from '../../../vacme-web-shared/src/lib/service/resolver/referrer-part-resolver.service';
/* eslint-disable max-len, , , , , , , ,  */
import {
    RegistrierungsnummerResolverService,
} from '../../../vacme-web-shared/src/lib/service/resolver/registrierungsnummer-resolver.service';
/* eslint-disable max-len */
import {
    RegistrierungsStatusResolverService,
} from '../../../vacme-web-shared/src/lib/service/resolver/registrierungsstatus-resolver.service';
import {Termine1ResolverService} from '../../../vacme-web-shared/src/lib/service/resolver/termin-1-resolver.service';
import {Termine2ResolverService} from '../../../vacme-web-shared/src/lib/service/resolver/termin-2-resolver.service';
import {
    TermineBearbeitenResolverService,
} from '../../../vacme-web-shared/src/lib/service/resolver/termin-bearbeiten-resolver.service';
import {TermineNResolverService} from '../../../vacme-web-shared/src/lib/service/resolver/termin-N-resolver.service';
import {AdminModule} from './admin/admin.module';
import {ApplicationMessageModule} from './application-message/application-message.module';
import {KontrollePageComponent} from './kontrolle/kontrolle/page/kontrolle-page.component';
import {
    OdiTagesstatistikDetailPageComponent,
} from './odi-tagesstatistik-detail-page/odi-tagesstatistik-detail-page.component';
import {OrtderimpfungModule} from './ortderimpfung/ortderimpfung.module';
import {
    GeimpftTermineBearbeitenPageComponent
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
import {CanBeGrundimmunisierungResolverService} from './service/resolver/can-be-grundimmunisierung-resolver.service';
import {FreieImpfslotsWebResolverService} from './service/resolver/freie-impfslots-web-resolver.service';
import {GeimpftResolverService} from './service/resolver/geimpft-resolver.service';
import {GeimpftTermineBearbeitenResolverService} from './service/resolver/geimpft-termine-bearbeiten-resolver.service';
import {ImpfdokumentationResolverService} from './service/resolver/impfdokumentation-resolver.service';
import {
    ImpfstoffZugelassenTagesstatistikResolverService,
} from './service/resolver/impfstoff-zugelassen-tagesstatistik-resolver.service';
import {KontrolleResolverService} from './service/resolver/kontrolle-resolver.service';
import {KrankheitResolverService} from './service/resolver/krankheit-resolver.service';
import {
    OdiTagesstatistikDetailPageResolverService,
} from './service/resolver/odi-tagesstatistik-detail-page-resolver.service';
import {OrtDerImpfungAssignedResolverService} from './service/resolver/ort-der-impfung-assigned-resolver.service';
import {RegistrierungResolverService} from './service/resolver/registrierung-resolver.service';
import {SelbstzahlendeResolverService} from './service/resolver/selbstzahlende-resolver.service';
import {
    OdiPersonalienSuchePageComponent,
} from './start-page/odi-personalien-suche/odi-personalien-suche-page.component';
import {
    OdiPersonalienUVCISuchePageComponent
} from './start-page/odi-personalien-uvci-suche/odi-personalien-uvci-suche-page.component';
import {StartPageComponent} from './start-page/start-page.component';
import {SystemadminModule} from './systemadmin/systemadmin.module';
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
        loadChildren: (): Promise<OrtderimpfungModule> => import('./ortderimpfung/ortderimpfung.module').then(m => m.OrtderimpfungModule)
    },
    {
        path: 'odistats/ortderimpfung/:odiId/date/:date',
        component: OdiTagesstatistikDetailPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: OdiTagesstatistikDetailPageResolverService,
            impfstoffList: ImpfstoffZugelassenTagesstatistikResolverService
        },
    },
    {
        path: 'person/:registrierungsnummer/impfdokumentation/:krankheit',
        component: ImpfdokumentationGrundimunisierungPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: ImpfdokumentationResolverService,
            modif: TermineBearbeitenResolverService,
            erstTerminAdHoc: ErstTerminAdHocResolverService,
            selbstzahlende: SelbstzahlendeResolverService,
            krankheit: KrankheitResolverService
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
            data: ImpfdokumentationResolverService,
            modif: TermineBearbeitenResolverService,
            canBeGrundimmunisierung: CanBeGrundimmunisierungResolverService,
            selbstzahlende: SelbstzahlendeResolverService,
            krankheit: KrankheitResolverService
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
            data: GeimpftResolverService
        },
    },
    {
        path: 'person/:registrierungsnummer/geimpftterminbearbeitung/:krankheit',
        component: GeimpftTermineBearbeitenPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: GeimpftTermineBearbeitenResolverService,
        },
    },
    {
        path: 'person/:registrierungsnummer/kontrolle/booster/:krankheit',
        component: KontrollePageComponent,
        canActivate: [KeycloakAppAuthGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            data: KontrolleResolverService,
            impfkontrolle: RegistrierungResolverService,
            modif: TermineBearbeitenResolverService,
            ortDerImpfungList: OrtDerImpfungAssignedResolverService,
            krankheit: KrankheitResolverService
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
            data: KontrolleResolverService,
            impfkontrolle: RegistrierungResolverService,
            modif: TermineBearbeitenResolverService,
            erstTerminAdHoc: ErstTerminAdHocResolverService,
            ortDerImpfungList: OrtDerImpfungAssignedResolverService,
            krankheit: KrankheitResolverService
        },
    },
    {
        path: 'person/:registrierungsnummer/terminfindung/krankheit/:krankheit/:ortDerImpfungId/:impffolge/:date',
        component: TerminfindungWebPageComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            freieSlots: FreieImpfslotsWebResolverService,
            impffolge: ImpffolgeResolverService,
            datum: FreieTermineResolverService,
            odi: OrtDerImpfungIdResolverService,
            registrierungsnummer: RegistrierungsnummerResolverService,
            modif: TermineBearbeitenResolverService,
            erstTerminAdHoc: ErstTerminAdHocResolverService,
            referrerPart: ReferrerPartResolverService,
            termin1: Termine1ResolverService,
            termin2: Termine2ResolverService,
            terminN: TermineNResolverService,
            status: RegistrierungsStatusResolverService,
            krankheit: KrankheitResolverService,
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
        loadChildren: (): Promise<ApplicationMessageModule> => import('./application-message/application-message.module').then(m => m.ApplicationMessageModule)
    },
    {
        path: 'admin',
        canActivate: [KeycloakAppAuthGuard],
        loadChildren: (): Promise<AdminModule> => import('./admin/admin.module').then(m => m.AdminModule),
    },
    {
        path: 'sysadmin',
        canActivate: [KeycloakAppAuthGuard],
        loadChildren: (): Promise<SystemadminModule> => import('./systemadmin/systemadmin.module').then(m => m.SystemadminModule),
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
