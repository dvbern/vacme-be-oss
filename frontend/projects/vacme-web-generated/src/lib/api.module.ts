import { NgModule, ModuleWithProviders, SkipSelf, Optional } from '@angular/core';
import { Configuration } from './configuration';
import { HttpClient } from '@angular/common/http';

import { ApplicationhealthService } from './api/applicationhealth.service';
import { BenutzerService } from './api/benutzer.service';
import { DevelopService } from './api/develop.service';
import { DossierService } from './api/dossier.service';
import { DownloadService } from './api/download.service';
import { GeimpftService } from './api/geimpft.service';
import { ImpfdokumentationService } from './api/impfdokumentation.service';
import { ImpfslotService } from './api/impfslot.service';
import { ImpfstoffService } from './api/impfstoff.service';
import { KontrolleService } from './api/kontrolle.service';
import { KorrekturService } from './api/korrektur.service';
import { MessagesService } from './api/messages.service';
import { OdibenutzerService } from './api/odibenutzer.service';
import { OnboardingService } from './api/onboarding.service';
import { OrtderimpfungService } from './api/ortderimpfung.service';
import { PersonalienSucheService } from './api/personalien-suche.service';
import { PropertiesService } from './api/properties.service';
import { PublicService } from './api/public.service';
import { RegistrierungService } from './api/registrierung.service';
import { ReportsService } from './api/reports.service';
import { ReportsSyncService } from './api/reports-sync.service';
import { SettingsService } from './api/settings.service';
import { StammdatenService } from './api/stammdaten.service';
import { StatService } from './api/stat.service';
import { SystemadministrationService } from './api/systemadministration.service';
import { TerminbuchungService } from './api/terminbuchung.service';
import { ZertifikatService } from './api/zertifikat.service';

@NgModule({
  imports:      [],
  declarations: [],
  exports:      [],
  providers: []
})
export class ApiModule {
    public static forRoot(configurationFactory: () => Configuration): ModuleWithProviders<ApiModule> {
        return {
            ngModule: ApiModule,
            providers: [ { provide: Configuration, useFactory: configurationFactory } ]
        };
    }

    constructor( @Optional() @SkipSelf() parentModule: ApiModule,
                 @Optional() http: HttpClient) {
        if (parentModule) {
            throw new Error('ApiModule is already loaded. Import in your base AppModule only.');
        }
        if (!http) {
            throw new Error('You need to import the HttpClientModule in your AppModule! \n' +
            'See also https://github.com/angular/angular/issues/20575');
        }
    }
}
