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
import {DOCUMENT} from '@angular/common';
import {Component, Inject} from '@angular/core';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {
    DossierService,
    ImpfdossierSummaryJaxTS,
    KrankheitIdentifierTS,
    PropertiesService,
    RegistrierungService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {
    KEY_NEXT_FORCED_WELL_LOGOUT_TIMESTAMP, KEY_NEXT_FORCED_WELL_LOGOUT_TIMESTAMP_GRACE_SECONDS,
} from '../../../../vacme-web-shared/src/lib/constants';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {NavigationService} from '../service/navigation.service';
import {LandingPageComponent} from './landing-page.component';

const LOG = LogFactory.createLog('LandingpageComponent');

@Component({
    selector: 'app-well-landing-page',
    templateUrl: './landing-page.component.html',
    styleUrls: ['./landing-page.component.scss']
})
export class WellLandingPageComponent extends LandingPageComponent {

    constructor(
        protected authServiceRsService: AuthServiceRsService,
        protected registrationService: RegistrierungService,
        protected propertiesService: PropertiesService,
        protected translateService: TranslateService,
        protected vacmeSettingsService: VacmeSettingsService,
        protected router: Router,
        protected navigationService: NavigationService,
        protected dossierService: DossierService,
                @Inject(DOCUMENT) protected document: Document,
    ) {
        super(
            authServiceRsService,
            registrationService,
            propertiesService,
            translateService,
            vacmeSettingsService,
            router,
            navigationService,
            dossierService,
            document
        );
    }

    public navigateToDefaultStartpage(registrierungsnummer: string): void {
        // if we are in a well context we navigate to the FSME dossier by default. Since this could be the first
        // login the dossier may not exist yet.
        if (TenantUtil.isWellPartnerDomain(document.location.hostname)) {
            // this.checkAndPerformLogoutIfNecessary(); well logout should be done in well
            // this is always triggered when landing on the well startpage
            this.dossierService.dossierResourceRegGetOrCreateImpfdossier(KrankheitIdentifierTS.FSME, registrierungsnummer)
                .subscribe((updatedDossierSummary: ImpfdossierSummaryJaxTS) => {
                    this.navigationService.navigateToDossierDetailCheckingPreConditions(
                        registrierungsnummer,
                        KrankheitIdentifierTS.FSME,
                        updatedDossierSummary.leistungerbringerAgbConfirmationNeeded,
                        updatedDossierSummary.externGeimpftConfirmationNeeded);
                }, error => LOG.error(error));

        } else {
            // otherwise we use the default mechanism
            this.navigationService.navigateToStartpage(registrierungsnummer, KrankheitIdentifierTS.COVID);
        }
    }

    /**
     * since well can currently not trigger a logout in our keycloak we manually ensure that users landing on
     * /wellstart are logged off from all previous sessions in keycloak. This is to ensure a user using a family
     * tablet does not see the vaccinations of the previous well-user.
     */
    private checkAndPerformLogoutIfNecessary(): void {
        const nextForcedLogout: string | null = localStorage.getItem(KEY_NEXT_FORCED_WELL_LOGOUT_TIMESTAMP);
        if (nextForcedLogout) {
            const now = DateUtil.now().toDate();
            const nextForcedLogoutTimestamp = DateUtil.localDateTimeToMoment(nextForcedLogout)?.toDate();

            // If grace period is over then trigger logout
            if (nextForcedLogoutTimestamp && nextForcedLogoutTimestamp < now) {
                this.triggerLogout();
            }
        } else {
            // if the popup has never been shown (no timstamp saved)
            this.triggerLogout();
        }
    }

    private triggerLogout(): void {
        localStorage.setItem(KEY_NEXT_FORCED_WELL_LOGOUT_TIMESTAMP,
            DateUtil.momentToLocalDateTime(DateUtil.now().add(KEY_NEXT_FORCED_WELL_LOGOUT_TIMESTAMP_GRACE_SECONDS, 'seconds')) as string);
        this.authServiceRsService.logout(this.router, '/wellstart');
    }
}
