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

import {DatePipe, registerLocaleData} from '@angular/common';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import localeDeCH from '@angular/common/locales/de-CH';
import localeDeCHExtra from '@angular/common/locales/extra/de-CH';
import localeFrCHExtra from '@angular/common/locales/extra/fr-CH';
import localeFrCH from '@angular/common/locales/fr-CH';
import {APP_INITIALIZER, LOCALE_ID, NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgbPaginationModule} from '@ng-bootstrap/ng-bootstrap';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {SweetAlert2Module} from '@sweetalert2/ngx-sweetalert2';
import {KeycloakAngularModule, KeycloakService} from 'keycloak-angular';
import * as moment from 'moment';
import {NgxFileDropModule} from 'ngx-file-drop';
import {NgProgressModule} from 'ngx-progressbar';
import {NgProgressHttpModule} from 'ngx-progressbar/http';
import {MultiTranslateHttpLoader} from 'ngx-translate-multi-http-loader';
import {ApiModule, Configuration} from 'vacme-web-generated';
import {APP_CONFIG, canton, VacmeWebSharedModule} from 'vacme-web-shared';
import {COLOR_PROGRESSBAR} from '../../../vacme-web-shared/src/lib/constants';
import {HttpAuthInterceptorService} from '../../../vacme-web-shared/src/lib/interceptors/http-auth-interceptor.service';
import {
    HttpErrorInterceptorService,
} from '../../../vacme-web-shared/src/lib/interceptors/http-error-interceptor.service';
import {AppLoadService} from '../../../vacme-web-shared/src/lib/service';
import TenantUtil from '../../../vacme-web-shared/src/lib/util/TenantUtil';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {ImpfgruppeFreigabeComponent} from './components/impfgruppe-freigabe/impfgruppe-freigabe.component';
import {
    JobStatistikErstimpfungenComponent,
} from './components/job-statistik-erstimpfungen/job-statistik-erstimpfungen.component';
import {KontrolleFormComponent} from './kontrolle/kontrolle/kontrolle-form/kontrolle-form.component';
import {KontrollePageComponent} from './kontrolle/kontrolle/page/kontrolle-page.component';
import {KontrolleUploadComponent} from './kontrolle/kontrolle/upload/kontrolle-upload.component';
import {
    OdiTagesstatistikDetailPageComponent,
} from './odi-tagesstatistik-detail-page/odi-tagesstatistik-detail-page.component';
import {OdiTagesstatistikComponent} from './odi-tagesstatistik/odi-tagesstatistik.component';
import {OdiTagesstatistikenComponent} from './odi-tagesstatistiken/odi-tagesstatistiken.component';
import {ErkrankungenListComponent} from './person/erkrankungen-list/erkrankungen-list.component';
import {GeimpftImpfungenListComponent} from './person/geimpft-impfungen-list/geimpft-impfungen-list.component';
import {
    GeimpftTermineBearbeitenPageComponent
} from './person/geimpft-termine-bearbeiten/geimpft-termine-bearbeiten-page.component';
import {GeimpftPageComponent} from './person/geimpft/geimpft-page.component';
import {
    ImpfdokumentationBoosterPageComponent,
} from './person/impfdokumentation/impfdokumentation-booster/impfdokumentation-booster-page.component';
import {
    ImpfdokumentationFormComponent,
} from './person/impfdokumentation/impfdokumentation-form/impfdokumentation-form.component';
import {
    ImpfdokumentationGrundimunisierungPageComponent,
} from './person/impfdokumentation/impfdokumentation-grundimunisierung/impfdokumentation-grundimunisierung-page.component';
import {PersonDocumentsComponent} from './person/person-documents/person-documents.component';
import {PersonFileuploadComponent} from './person/person-fileupload/person-fileupload.component';
import {PersonFolgeterminComponent} from './person/person-folgetermin/person-folgetermin.component';
import {PersonImpfungenComponent} from './person/person-impfungen/person-impfungen.component';
import {PersonInfosComponent} from './person/person-infos/person-infos.component';
import {PersonSuchenKvkNummerComponent} from './person/person-suchen-kvk-nummer/person-suchen-kvk-nummer.component';
import {PersonSuchenComponent} from './person/person-suchen/person-suchen.component';
import {PersonTerminComponent} from './person/person-termin/person-termin.component';
import {PersonZertifikatComponent} from './person/person-zertifikat/person-zertifikat.component';
import {TerminfindungWebPageComponent} from './person/terminfindung-page/terminfindung-web-page.component';
import {ZweiteImpfungVerzichtenComponent} from './person/zweite-impfung-verzichten/zweite-impfung-verzichten.component';
import {ReportsPageComponent} from './reports/reports-page.component';
import {CreateRequestGuardService} from './service/termin-guard.service';
import {
    OdiPersonalienSuchePageComponent,
} from './start-page/odi-personalien-suche/odi-personalien-suche-page.component';
import {
    OdiPersonalienUVCISuchePageComponent
} from './start-page/odi-personalien-uvci-suche/odi-personalien-uvci-suche-page.component';
import {StartPageComponent} from './start-page/start-page.component';
import {ZertifikatTokenComponent} from './start-page/zertifikat-token/zertifikat-token.component';
import {ApplicationHealthPageComponent} from './systemadmin/application-health/application-health-page.component';
import {
    RegistrierungTermineImpfungenComponent,
} from './systemadmin/registrierung-termine-impfungen/registrierung-termine-impfungen.component';
import {SystemPageComponent} from './systemadmin/systemadministration/system-page.component';
import {AccountFachappPageComponent} from './userprofile/account-fachapp-page.component';
import {WebConfig} from './vacme-web.app.config';

moment.locale(['de-ch', 'fr-ch']);

registerLocaleData(localeDeCH, 'de-CH', localeDeCHExtra);
registerLocaleData(localeDeCH, 'de', localeDeCHExtra);
registerLocaleData(localeFrCH, 'fr-CH', localeFrCHExtra);
registerLocaleData(localeFrCH, 'fr', localeFrCHExtra);

export function onAppInit(appLoadService: AppLoadService): () => Promise<any> {
    return (): Promise<any> => appLoadService.initializeApp();
}

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader | MultiTranslateHttpLoader {

    const i18nPath = './assets/i18n/';
    const jsonBaseSuffix = `.json?cache=${__VERSION__}`;
    let suffixes = ['', '-web'];

    if (TenantUtil.hasAdditionalTranslations()) {
        const additionalSuffixes: string[] = [];
        suffixes.forEach(suffix => {
            additionalSuffixes.push(suffix);
            additionalSuffixes.push(suffix + `.${canton.name}`);
        });
        suffixes = additionalSuffixes;
    }

    return new MultiTranslateHttpLoader(http, suffixes.map((suffix) => {
        return {prefix: i18nPath, suffix: suffix + jsonBaseSuffix};
    }));
}

// Sweetalert
// eslint-disable-next-line
export function provideSwal() {
    return import('sweetalert2/src/sweetalert2.js'); // instead of import('sweetalert2')
}

@NgModule({
    declarations: [
        AppComponent,
        StartPageComponent,
        ImpfdokumentationGrundimunisierungPageComponent,
        PersonSuchenComponent,
        KontrollePageComponent,
        PersonInfosComponent,
        PersonTerminComponent,
        PersonFolgeterminComponent,
        PersonImpfungenComponent,
        ReportsPageComponent,
        PersonSuchenKvkNummerComponent,
        SystemPageComponent,
        ApplicationHealthPageComponent,
        TerminfindungWebPageComponent,
        PersonDocumentsComponent,
        OdiPersonalienSuchePageComponent,
        OdiPersonalienUVCISuchePageComponent,
        OdiTagesstatistikComponent,
        OdiTagesstatistikenComponent,
        OdiTagesstatistikDetailPageComponent,
        RegistrierungTermineImpfungenComponent,
        GeimpftPageComponent,
        GeimpftTermineBearbeitenPageComponent,
        ImpfgruppeFreigabeComponent,
        ZweiteImpfungVerzichtenComponent,
        JobStatistikErstimpfungenComponent,
        ZertifikatTokenComponent,
        PersonZertifikatComponent,
        PersonFileuploadComponent,
        ImpfdokumentationFormComponent,
        ImpfdokumentationBoosterPageComponent,
        GeimpftImpfungenListComponent,
        AccountFachappPageComponent,
        KontrolleFormComponent,
        KontrolleUploadComponent,
        ErkrankungenListComponent
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        AppRoutingModule,
        HttpClientModule,
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
                deps: [HttpClient],
            },
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
        NgxFileDropModule,
        NgbPaginationModule,
    ],
    providers: [
        CreateRequestGuardService,
        DatePipe,
        {provide: APP_INITIALIZER, useFactory: onAppInit, multi: true, deps: [AppLoadService, KeycloakService]},
        {
            provide: Configuration,
            useFactory: (): Configuration => new Configuration(
                {
                    basePath: '',
                },
            ),
            multi: false,
        },
        {provide: APP_CONFIG, useValue: WebConfig},
        {provide: LOCALE_ID, useValue: 'de-CH'},
        {provide: HTTP_INTERCEPTORS, useClass: HttpErrorInterceptorService, multi: true},
        {provide: HTTP_INTERCEPTORS, useClass: HttpAuthInterceptorService, multi: true},
    ],
    bootstrap: [AppComponent],
})
export class AppModule {
}
