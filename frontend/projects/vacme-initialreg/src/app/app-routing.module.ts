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
import {KeycloakAppAuthGuard} from '../../../vacme-web-shared/src/lib/service/guard/keycloak-app-auth-guard.service';
import {
    TermineBearbeitenResolverService,
} from '../../../vacme-web-shared/src/lib/service/resolver/termin-bearbeiten-resolver.service';
import {AccountPageComponent} from './account/account-page.component';
import {BenutzernameVergessenPageComponent} from './benutzername-vergessen/benutzername-vergessen-page.component';
import {CallcenterModule} from './callcenter/callcenter.module';
import {
    CcAdresseKkFragebogenPageComponent,
} from './cc-adresse-kk-fragebogen-page/cc-adresse-kk-fragebogen-page.component';
import {ExternGeimpftPageComponent} from './extern-geimpft/extern-geimpft-page.component';
import {ImpfdossiersAuswahlComponent} from './impfdossiers-auswahl/impfdossiers-auswahl.component';
import {ImpfdossiersOverviewComponent} from './impfdossiers-overview/impfdossiers-overview.component';
import {ImpffaehigkeitPageComponent} from './impffaehigkeit/impffaehigkeit-page.component';
import {LandingPageComponent} from './landingpage/landing-page.component';
import {WellLandingPageComponent} from './landingpage/well-landing-page.component';
import {LeistungserbringerAgbPageComponent} from './leistungserbringer-agb/leistungserbringer-agb-page.component';
import {OnboardingProcessPageComponent} from './onboarding-process/onboarding-process-page.component';
import {OnboardingStartPageComponent} from './onboarding-start/onboarding-start-page.component';
import {OverviewPageComponent} from './overview/overview-page/overview-page.component';
import {PersonalienEditPageComponent} from './personalien-edit/personalien-edit-page.component';
import {PersonendatenPageComponent} from './personendaten/personendaten-page.component';
import {RegistrierungModule} from './registrierung/registrierung.module';
import {InfoUpdateGuard} from './service/info-update-guard.service';
import {PhonenumberUpdateGuard} from './service/phonenumber-update-guard.service';
import {DossierResolverService} from './service/resolver/dossier-resolver.service';
import {DossiersOverviewResolverService} from './service/resolver/dossiers-overview-resolver.service';
import {EzImpfstoffResolverService} from './service/resolver/ez-impfstoff-resolver.service';
import {RegistrierungIdResolverService} from './service/resolver/registrierung-id-resolver.service';
import {UmfrageAktuellDatenComponent} from './umfrage-aktuell-daten/umfrage-aktuell-daten.component';
import {UmfrageModule} from './umfrage/umfrage.module';

const routes: Routes = [
    {
        path: 'start',
        component: LandingPageComponent
    },
    {
        path: 'personendaten',
        canActivate: [KeycloakAppAuthGuard],
        component: PersonendatenPageComponent,
    },
    {
        path: 'impffaehigkeit',
        component: ImpffaehigkeitPageComponent,
    },
    {
        path: 'personalien-edit/:registrierungId',
        canActivate: [KeycloakAppAuthGuard],
        component: PersonalienEditPageComponent,
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            personalien: RegistrierungIdResolverService
        }
    },
    {
        path: 'registrierung/:registrierungsnummer',
        canActivate: [KeycloakAppAuthGuard],
        loadChildren: (): Promise<RegistrierungModule> => import('./registrierung/registrierung.module').then(m => m.RegistrierungModule),
    },
    {
        path: 'externgeimpft/:registrierungsnummer/krankheit/:krankheit',
        canActivate: [KeycloakAppAuthGuard],
        component: ExternGeimpftPageComponent,
        resolve: {
            impfstoffe: EzImpfstoffResolverService,
            dossier: DossierResolverService,
        }
    },
    {
        path: 'leistungserbringer-agb/:registrierungsnummer/krankheit/:krankheit',
        canActivate: [KeycloakAppAuthGuard],
        component: LeistungserbringerAgbPageComponent,
        resolve: {
            dossiersOverview: DossiersOverviewResolverService,
        }
    },
    {
        path: 'impfdossiers-overview/:registrierungsnummer',
        canActivate: [KeycloakAppAuthGuard, InfoUpdateGuard, PhonenumberUpdateGuard],
        component: ImpfdossiersOverviewComponent,
        resolve: {
            dossiersOverview: DossiersOverviewResolverService,
        }
    },
    {
        path: 'impfdossiers-auswahl/:registrierungsnummer',
        canActivate: [KeycloakAppAuthGuard],
        component: ImpfdossiersAuswahlComponent,
        resolve: {
            dossiersOverview: DossiersOverviewResolverService,
        }
    },
    {
        path: 'overview/:registrierungsnummer/krankheit/:krankheit',
        component: OverviewPageComponent,
        canActivate: [KeycloakAppAuthGuard, InfoUpdateGuard, PhonenumberUpdateGuard],
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        resolve: {
            dossier: DossierResolverService,
            modif: TermineBearbeitenResolverService,
        },
    },
    {
        pathMatch: 'full',
        path: 'overview/:registrierungsnummer',
        redirectTo: 'overview/:registrierungsnummer/krankheit/UNKNOWN_KRANKHEIT'
    },
    {
        path: 'cc-adresse-edit/:registrierungsnummer',
        canActivate: [KeycloakAppAuthGuard],
        component: CcAdresseKkFragebogenPageComponent
    },
    {
        path: 'callcenter',
        canActivate: [KeycloakAppAuthGuard],
        loadChildren: (): Promise<CallcenterModule> => import('./callcenter/callcenter.module').then(m => m.CallcenterModule),
    },
    {
        path: 'benutzername-vergessen',
        component: BenutzernameVergessenPageComponent,
    },
    {
        path: 'account',
        component: AccountPageComponent,
        canActivate: [KeycloakAppAuthGuard]
    },
    {
        path: 'umfrage',
        loadChildren: (): Promise<UmfrageModule> => import('./umfrage/umfrage.module').then(m => m.UmfrageModule)
    },
    {
        path: 'u/:code',
        redirectTo: '/umfrage/:code',
    },
    {
        path: 'onboarding',
        component: OnboardingStartPageComponent,
    },
    {
        path: 'onboarding-process',
        canActivate: [KeycloakAppAuthGuard],
        component: OnboardingProcessPageComponent,
    },
    {
        path:'umfrage-aktuell-daten',
        canActivate: [KeycloakAppAuthGuard],
        component: UmfrageAktuellDatenComponent,
    },
    {
        path:'wellstart',
        component: WellLandingPageComponent,
        canActivate: [KeycloakAppAuthGuard],
        pathMatch: 'full',
    },
    {
        path: '',
        redirectTo: '/start',
        pathMatch: 'full',
    },
    {
        path: '**',
        redirectTo: '/start',
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {
        scrollPositionRestoration: 'enabled',
        onSameUrlNavigation: 'reload',
        anchorScrolling: 'enabled', // geht leider nicht
        // ScrollOffset fuer ViewportScroller. Siehe this.viewportScroller.scrollToAnchor in ngAfterViewChecked
        scrollOffset: [0, 200]
    })],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
