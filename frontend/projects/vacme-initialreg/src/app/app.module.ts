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

import {registerLocaleData} from '@angular/common';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import localeDeCH from '@angular/common/locales/de-CH';
import localeEnCH from '@angular/common/locales/en-CH';
import localeDeCHExtra from '@angular/common/locales/extra/de-CH';
import localeEnCHExtra from '@angular/common/locales/extra/en-CH';
import localeFrCHExtra from '@angular/common/locales/extra/fr-CH';
import localeFrCH from '@angular/common/locales/fr-CH';
import {APP_INITIALIZER, LOCALE_ID, NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {SweetAlert2Module} from '@sweetalert2/ngx-sweetalert2';
import {KeycloakAngularModule, KeycloakService} from 'keycloak-angular';

import * as moment from 'moment';
import {NgProgressModule} from 'ngx-progressbar';
import {NgProgressHttpModule} from 'ngx-progressbar/http';
import {MultiTranslateHttpLoader} from 'ngx-translate-multi-http-loader';
// @ts-ignore
import {ApiModule, Configuration} from 'vacme-web-generated';

import {APP_CONFIG, canton, VacmeWebSharedModule} from 'vacme-web-shared';
import {COLOR_PROGRESSBAR} from '../../../vacme-web-shared/src/lib/constants';
import {HttpAuthInterceptorService} from '../../../vacme-web-shared/src/lib/interceptors/http-auth-interceptor.service';
import {HttpErrorInterceptorService} from '../../../vacme-web-shared/src/lib/interceptors/http-error-interceptor.service';

import {AppLoadService} from '../../../vacme-web-shared/src/lib/service';
import TenantUtil from '../../../vacme-web-shared/src/lib/util/TenantUtil';
import {AccountPageComponent} from './account/account-page.component';
import {AdresseKrankenkasseFragebogenComponent} from './adresse-kk-fragebogen/adresse-krankenkasse-fragebogen.component';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BenutzernameVergessenPageComponent} from './benutzername-vergessen/benutzername-vergessen-page.component';
import {CcAdresseKkFragebogenPageComponent} from './cc-adresse-kk-fragebogen-page/cc-adresse-kk-fragebogen-page.component';
import {CoreModule} from './core/core.module';
import {ExternGeimpftPageComponent} from './extern-geimpft/extern-geimpft-page.component';
import {ImpfdossiersAuswahlComponent} from './impfdossiers-auswahl/impfdossiers-auswahl.component';
import {ImpfdossiersOverviewComponent} from './impfdossiers-overview/impfdossiers-overview.component';
import {ImpffaehigkeitPageComponent} from './impffaehigkeit/impffaehigkeit-page.component';
import {InitalregConfig} from './initalreg.app.config';
import {LandingPageComponent} from './landingpage/landing-page.component';
import {WellLandingPageComponent} from './landingpage/well-landing-page.component';
import {LeistungserbringerAgbPageComponent} from './leistungserbringer-agb/leistungserbringer-agb-page.component';
import {OnboardingProcessPageComponent} from './onboarding-process/onboarding-process-page.component';
import {OnboardingStartPageComponent} from './onboarding-start/onboarding-start-page.component';
import {CodeComponent} from './overview/code/code.component';
import {ImpfzentrumSelectionComponent} from './overview/impfzentrum-selection/impfzentrum-selection.component';
import {OverviewPageComponent} from './overview/overview-page/overview-page.component';
import {OverviewOdiFilterComponent} from './overview/overview/overview-odi-filter/overview-odi-filter.component';
import {OverviewComponent} from './overview/overview/overview.component';
import {PriorityComponent} from './overview/priority/priority.component';
import {PersonalienEditPageComponent} from './personalien-edit/personalien-edit-page.component';
import {PersonendatenPageComponent} from './personendaten/personendaten-page.component';
import {UmfrageAktuellDatenComponent} from './umfrage-aktuell-daten/umfrage-aktuell-daten.component';

moment.locale(['de-ch', 'fr-ch', 'en-ch']);

registerLocaleData(localeDeCH, 'de-CH', localeDeCHExtra);
registerLocaleData(localeDeCH, 'de', localeDeCHExtra);
registerLocaleData(localeFrCH, 'fr-CH', localeFrCHExtra);
registerLocaleData(localeFrCH, 'fr', localeFrCHExtra);
registerLocaleData(localeEnCH, 'en-CH', localeEnCHExtra);
registerLocaleData(localeEnCH, 'en', localeEnCHExtra);

export function onAppInit(appLoadService: AppLoadService): () => Promise<any> {
    return (): Promise<any> => appLoadService.initializeApp();
}

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader | MultiTranslateHttpLoader {

    const i18nPath = './assets/i18n/';
    const jsonSuffix = `.json?cache=${__VERSION__}`;

    if (TenantUtil.hasAdditionalTranslations()) {
        return new MultiTranslateHttpLoader(http, [
            {prefix: i18nPath, suffix: jsonSuffix},
            {prefix: i18nPath, suffix: `.${canton.name}` + jsonSuffix}
        ]);
    }

    return new TranslateHttpLoader(http, i18nPath, jsonSuffix);
}

// Sweetalert
// eslint-disable-next-line
export function provideSwal() {
    return import('sweetalert2/src/sweetalert2.js'); // instead of import('sweetalert2')
}

@NgModule({
    declarations: [
        AppComponent,
        PersonendatenPageComponent,
        ExternGeimpftPageComponent,
        LeistungserbringerAgbPageComponent,
        OverviewComponent,
        CodeComponent,
        PriorityComponent,
        ImpfzentrumSelectionComponent,
        ImpffaehigkeitPageComponent,
        LandingPageComponent,
        WellLandingPageComponent,
        OverviewPageComponent,
        BenutzernameVergessenPageComponent,
        PersonalienEditPageComponent,
        OnboardingStartPageComponent,
        OnboardingProcessPageComponent,
        AdresseKrankenkasseFragebogenComponent,
        AccountPageComponent,
        CcAdresseKkFragebogenPageComponent,
        OverviewOdiFilterComponent,
        UmfrageAktuellDatenComponent,
        ImpfdossiersOverviewComponent,
        ImpfdossiersAuswahlComponent,
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        AppRoutingModule,
        HttpClientModule,
        CoreModule,
        VacmeWebSharedModule,
        FormsModule,
        ReactiveFormsModule,
        ApiModule,
        KeycloakAngularModule,
        TranslateModule.forRoot({
            defaultLanguage: 'de',
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient]
            }
        }),

        NgProgressModule.withConfig({
            trickleSpeed: 200,
            debounceTime: 10,
            color: COLOR_PROGRESSBAR,
            spinner: false,
            // plus we added styles for .ng-bar-placeholder
        }),
        NgProgressHttpModule,
        SweetAlert2Module.forRoot({provideSwal}),
    ],
    providers: [
        {provide: APP_INITIALIZER, useFactory: onAppInit, multi: true, deps: [AppLoadService, KeycloakService]},
        {
            provide: Configuration,
            useFactory: (): Configuration => new Configuration(
                {
                    basePath: '',
                },
            ),
            multi: false
        },
        {provide: APP_CONFIG, useValue: InitalregConfig},
        {provide: LOCALE_ID, useValue: 'de-CH'},
        {provide: HTTP_INTERCEPTORS, useClass: HttpErrorInterceptorService, multi: true},
        {provide: HTTP_INTERCEPTORS, useClass: HttpAuthInterceptorService, multi: true},
    ],
    bootstrap: [AppComponent],
})
export class AppModule {
}
