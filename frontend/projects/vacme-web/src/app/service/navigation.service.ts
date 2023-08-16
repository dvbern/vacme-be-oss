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

import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    DashboardJaxTS,
    ImpfdossiersOverviewJaxTS,
    ImpfdossierStatusTS,
    KrankheitIdentifierTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {TSRole} from '../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';

const LOG = LogFactory.createLog('NavigationService');

@Injectable({
    providedIn: 'root',
})
export class NavigationService {

    constructor(
        private router: Router,
        private authServiceRS: AuthServiceRsService,
        private translationService: TranslateService,
    ) {
    }

    public navigate(dossiersOverview: ImpfdossiersOverviewJaxTS): void {
        if (!dossiersOverview) {
            this.notFoundResult();
        }
        this.toGeimpft(dossiersOverview.registrierungsnummer);
    }

    private navigateForCovid(
        impfdossiersOverviewJaxTS: ImpfdossiersOverviewJaxTS,
        krankheit: KrankheitIdentifierTS.COVID,
    ): void {
        const dossier = impfdossiersOverviewJaxTS.impfdossierSummaryList?.find(summary => summary.krankheit.identifier === krankheit);
        if (!dossier) {
            LOG.error('Kein Covid Dossier gefunden!');
            return;
        }
        switch (dossier.status) {
            case ImpfdossierStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG:
            case ImpfdossierStatusTS.AUTOMATISCH_ABGESCHLOSSEN:
            case ImpfdossierStatusTS.ABGESCHLOSSEN:
            case ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT:
            case ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT:
            case ImpfdossierStatusTS.IMMUNISIERT:
                this.toGeimpft(dossier.registrierungsnummer);
                break;
            case ImpfdossierStatusTS.IMPFUNG_1_KONTROLLIERT:
                if (!this.isBerechtigtForImpfdokumentation()) {
                    this.showInfoBereitsKontrolliert(impfdossiersOverviewJaxTS);
                } else {
                    this.toImpfung1(dossier.registrierungsnummer, krankheit);
                }
                break;
            case ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT:
                if (this.isNachdokumentationWithoutImpfungRole()) {
                    this.toGeimpft(dossier.registrierungsnummer);
                    break;
                }
                if (!this.isBerechtigtForImpfdokumentation()) {
                    this.showInfoBereitsKontrolliert(impfdossiersOverviewJaxTS);
                } else {
                    this.toImpfung2(dossier.registrierungsnummer, krankheit);
                }
                break;
            case ImpfdossierStatusTS.NEU:
            case ImpfdossierStatusTS.ODI_GEWAEHLT:
            case ImpfdossierStatusTS.FREIGEGEBEN:
            case ImpfdossierStatusTS.GEBUCHT:
                if (this.isNachdokumentationWithoutImpfungRole()) {
                    this.toGeimpft(dossier.registrierungsnummer);
                    break;
                }
                this.toKontrolle1(dossier.registrierungsnummer, krankheit);
                break;
            case ImpfdossierStatusTS.GEBUCHT_BOOSTER:
            case ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER:
                if (this.isNachdokumentationWithoutImpfungRole()) {
                    this.toGeimpft(dossier.registrierungsnummer);
                    break;
                }
                this.toKontrolleBooster(dossier.registrierungsnummer, krankheit);
                break;
            case ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER:
                this.toGeimpft(dossier.registrierungsnummer);
                break;
            case ImpfdossierStatusTS.KONTROLLIERT_BOOSTER:

                if (this.isNachdokumentationWithoutImpfungRole()) {
                    this.toGeimpft(dossier.registrierungsnummer);
                    break;
                }
                if (!this.isBerechtigtForImpfdokumentation()) {
                    this.showInfoBereitsKontrolliert(impfdossiersOverviewJaxTS);
                } else {
                    this.toImpfungBooster(dossier.registrierungsnummer, krankheit);
                }
                break;
            default:
                throw Error(`Nicht behandelter Status: ${dossier.status as string}`);
        }
    }

    private isNachdokumentationWithoutImpfungRole(): boolean {
        return this.authServiceRS.isOneOfRoles([
                TSRole.KT_NACHDOKUMENTATION,
                TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION,
            ]) &&
            !this.authServiceRS.isOneOfRoles([
                TSRole.OI_IMPFVERANTWORTUNG,
                TSRole.OI_DOKUMENTATION,
                TSRole.OI_KONTROLLE,
                TSRole.KT_IMPFDOKUMENTATION,
            ]);
    }

    public notFoundResult(): void {
        void this.router.navigate(['startseite'], {queryParams: {err: 'ERR_NO_IMPFABLE_PERS'}});
    }

    private toImpfung1(registrierungsnummer: string, krankheitsIdentifier: KrankheitIdentifierTS): void {
        this.redirectToImpfdokumentation(registrierungsnummer, krankheitsIdentifier);
    }

    private toImpfung2(registrierungsnummer: string, krankheitsIdentifier: KrankheitIdentifierTS): void {
        this.redirectToImpfdokumentation(registrierungsnummer, krankheitsIdentifier);
    }

    private toImpfungBooster(registrierungsnummer: string, krankheitsIdentifier: KrankheitIdentifierTS): void {
        this.redirectToImpfdokumentation(registrierungsnummer, krankheitsIdentifier, true);
    }

    /**
     * Diese Funktion forciert ein navigieren auf eine eige andere Seite wenn man von
     * impfdokumentation wieder auf impfdokumentation navigiert (e.g. wenn man von dort eine Person sucht
     * die ebenfalls grad ans impfen kommt wie bspw beim Massenupload). Grund dafuer ist, dass wir
     * die Page in diesem Fall neu aufbauen wollen (damit sie wieder enabled ist etc)
     *
     * Damit wir das aber nicht machen wenn nicht noetig pruefen wir wo wir hergekommen sind
     */
    private redirectToImpfdokumentation(
        registrierungsnummer: string,
        krankheitsIdentifier: KrankheitIdentifierTS,
        booster?: boolean,
    ): void {
        if (this.router.url.includes('/impfdokumentation')) {
            void this.router.navigateByUrl('/', {skipLocationChange: true}).then(() => {
                LOG.info('forced reload of component during navigation');
                this.redirectToImpfdokumentationBasic(registrierungsnummer, krankheitsIdentifier, booster);
            });
        } else {
            this.redirectToImpfdokumentationBasic(registrierungsnummer, krankheitsIdentifier, booster);
        }
    }

    private redirectToImpfdokumentationBasic(
        registrierungsnummer: string,
        krankheitsIdentifier: KrankheitIdentifierTS,
        booster?: boolean,
    ): void {
        if (booster) {
            void this.router.navigate([
                'person',
                registrierungsnummer,
                'impfdokumentation',
                'booster',
                krankheitsIdentifier,
            ]);
        } else {
            void this.router.navigate(['person', registrierungsnummer, 'impfdokumentation', krankheitsIdentifier]);
        }
    }

    private toKontrolle1(registrierungsnummer: string, krankheitsIdentifier: KrankheitIdentifierTS): void {
        void this.router.navigate(['person', registrierungsnummer, 'kontrolle', krankheitsIdentifier]);
    }

    private toKontrolle2(registrierungsnummer: string, krankheitsIdentifier: KrankheitIdentifierTS): void {
        void this.router.navigate(['person', registrierungsnummer, 'kontrolle', krankheitsIdentifier]);
    }

    private toKontrolleBooster(registrierungsnummer: string, krankheitsIdentifier: KrankheitIdentifierTS): void {
        void this.router.navigate(['person', registrierungsnummer, 'kontrolle', krankheitsIdentifier]);
    }

    private toGeimpft(registrierungsnummer: string): void {
        if (this.router.url.includes('/geimpft')) {
            void this.router.navigateByUrl('/infopage', {skipLocationChange: true}).then(() => {
                LOG.info('forced reload of component during navigation');
                void this.router.navigate(['person', registrierungsnummer, 'geimpft']);
            });
        } else {
            void this.router.navigate(['person', registrierungsnummer, 'geimpft']);
        }
    }

    private showInfoBereitsKontrolliert(dossier: ImpfdossiersOverviewJaxTS): void {
        void Swal.fire({
            icon: 'info',
            text: this.translationService.instant('NAVIGATION_SERVICE.PERSON_BEREITS_KONTROLLIERT',
                {person: this.getPersonInfos(dossier)}),
            showConfirmButton: true,
        });
    }

    private getPersonInfos(dashboardJax: ImpfdossiersOverviewJaxTS): string {
        const result = dashboardJax.registrierungsnummer
            + ' - ' + dashboardJax.name
            + ' ' + dashboardJax.vorname
            + ' (' + this.geburtsdatumAsString(dashboardJax) + ') ';

        return result;
    }

    private geburtsdatumAsString(dashboardJax: DashboardJaxTS): string {
        if (dashboardJax && dashboardJax.geburtsdatum) {
            const moment1 = moment(dashboardJax.geburtsdatum);
            if (moment1) {
                const gebDat = DateUtil.momentToLocalDateFormat(moment1, 'DD.MM.YYYY');
                if (gebDat) {
                    return gebDat;
                }
            }
        }
        return '';
    }

    private isBerechtigtForImpfdokumentation(): boolean {
        return this.authServiceRS.isOneOfRoles([TSRole.OI_DOKUMENTATION, TSRole.KT_IMPFDOKUMENTATION]);
    }

    public navigateForKrankheit(
        dossier: ImpfdossiersOverviewJaxTS,
        krankheitFromRoute: KrankheitIdentifierTS,
    ): void {
        if (krankheitFromRoute === KrankheitIdentifierTS.COVID) {
            this.navigateForCovid(dossier, krankheitFromRoute);
        } else {
            this.navigate(dossier);
        }
    }
}
