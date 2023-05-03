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
import {KeycloakAppAuthGuard} from '../../../../vacme-web-shared/src/lib/service/guard/keycloak-app-auth-guard.service';
import {OnboardingSuchePageComponent} from './onboarding-suche-page/onboarding-suche-page.component';
import {PersonalienSuchePageComponent} from './personalien-suche-page/personalien-suche-page.component';
import {RegistrierungUvciSuchePageComponent} from './registrierung-suche-page/registrierung-uvci-suche-page.component';
import {RegistrierungSuchenPageComponent} from './registrierung-suchen-page/registrierung-suchen-page.component';

const routes: Routes = [
    {
        // Startseite Callcenter
        path: 'suchen',
        canActivate: [KeycloakAppAuthGuard],
        component: RegistrierungSuchenPageComponent,
    },
    {
        // einfache Suche (Name/Vorname/Geb), aber man kann nur Adresse editieren
        path: 'personalien-suchen',
        canActivate: [KeycloakAppAuthGuard],
        component: PersonalienSuchePageComponent,
    },
    {
        // neu: Suche inkl. UVCI, dafuer kann man die Registrierung oeffnen, ohne dass man die Reg-Nr kennt
        path: 'registrierung-suchen',
        canActivate: [KeycloakAppAuthGuard],
        component: RegistrierungUvciSuchePageComponent,
    },
    {
        path: 'onboardingcode-suchen',
        canActivate: [KeycloakAppAuthGuard],
        component: OnboardingSuchePageComponent,
    },
    {
        path: '',
        redirectTo: 'suchen',
        pathMatch: 'full',
    },
    {
        path: '**',
        redirectTo: '/suchen',
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
})
export class CallcenterRoutingModule {
}
