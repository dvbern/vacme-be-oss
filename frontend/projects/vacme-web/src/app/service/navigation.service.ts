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
import {DashboardJaxTS, KrankheitIdentifierTS, RegistrierungStatusTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {NBR_HOURS_TO_SHOW_IMPFDETAILS} from '../../../../vacme-web-shared/src/lib/constants';
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

    public navigate(dossier: DashboardJaxTS): void {
        if (!dossier) {
            this.notFoundResult();
        }

        if (!this.isCurrentBenutzerAllowedForExactlyOneKrankheit()) {
            this.toGeimpft(dossier.registrierungsnummer as string);
            return;
        }

        // TODO Affenpocken: Ich bin zwar nur fuer 1 Krankheit berechtigt, aber diese ist
        //  AFFENPOCKEN, das DashboardJaxTS ist hier aber (noch) immer COVID. Als Workaround
        //  gehen wir hier immer auf die Geimpft-Seite
        let krankheit = this.authServiceRS.getKrankheitForUserIfOnlyAllowedForExactlyOne();
        if (KrankheitIdentifierTS.COVID !== krankheit) {
            this.toGeimpft(dossier.registrierungsnummer as string);
            return;
        }
        // Wenn wir hier hin kommen, ist die (einzige) Krankheit COVID
        krankheit = KrankheitIdentifierTS.COVID;
        this.navigateForCovid(dossier, krankheit);
    }

    private navigateForCovid(dossier: DashboardJaxTS, krankheit: KrankheitIdentifierTS.COVID): void {
        switch (dossier.status) {
            case RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG:
            case RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN:
            case RegistrierungStatusTS.ABGESCHLOSSEN:
            case RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT:
            case RegistrierungStatusTS.IMMUNISIERT:
                this.toGeimpft(dossier.registrierungsnummer as string);
                break;
            case RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT:
                if (this.isNachdokumentationWithoutImpfungRole()) {
                    this.toGeimpft(dossier.registrierungsnummer as string);
                    break;
                }
                if (dossier.impfung1?.timestampImpfung) {
                    // Wenn die Impfung weniger als 24h her ist: Impfdetails anzeigen
                    const hoursDiff = DateUtil.getHoursDiff(moment(dossier.impfung1?.timestampImpfung), DateUtil.now());
                    if (hoursDiff < NBR_HOURS_TO_SHOW_IMPFDETAILS) {
                        this.toGeimpft(dossier.registrierungsnummer as string);
                        break;
                    }
                }
                // Sonst normal auf die Kontrolle
                this.toKontrolle2(dossier.registrierungsnummer as string, krankheit);
                break;
            case RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT:
                if (!this.isBerechtigtForImpfdokumentation()) {
                    this.showInfoBereitsKontrolliert(dossier);
                } else {
                    this.toImpfung1(dossier.registrierungsnummer as string, krankheit);
                }
                break;
            case RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT:
                if (this.isNachdokumentationWithoutImpfungRole()) {
                    this.toGeimpft(dossier.registrierungsnummer as string);
                    break;
                }
                if (!this.isBerechtigtForImpfdokumentation()) {
                    this.showInfoBereitsKontrolliert(dossier);
                } else {
                    this.toImpfung2(dossier.registrierungsnummer as string, krankheit);
                }
                break;
            case RegistrierungStatusTS.ODI_GEWAEHLT:
            case RegistrierungStatusTS.REGISTRIERT:
            case RegistrierungStatusTS.FREIGEGEBEN:
            case RegistrierungStatusTS.GEBUCHT:
                if (this.isNachdokumentationWithoutImpfungRole()) {
                    this.toGeimpft(dossier.registrierungsnummer as string);
                    break;
                }
                this.toKontrolle1(dossier.registrierungsnummer as string, krankheit);
                break;
            case RegistrierungStatusTS.GEBUCHT_BOOSTER:
            case RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER:
                if (this.isNachdokumentationWithoutImpfungRole()) {
                    this.toGeimpft(dossier.registrierungsnummer as string);
                    break;
                }
                this.toKontrolleBooster(dossier.registrierungsnummer as string, krankheit);
                break;
            case RegistrierungStatusTS.FREIGEGEBEN_BOOSTER:
                this.toGeimpft(dossier.registrierungsnummer as string);
                break;
            case RegistrierungStatusTS.KONTROLLIERT_BOOSTER:

                if (this.isNachdokumentationWithoutImpfungRole()) {
                    this.toGeimpft(dossier.registrierungsnummer as string);
                    break;
                }
                if (!this.isBerechtigtForImpfdokumentation()) {
                    this.showInfoBereitsKontrolliert(dossier);
                } else {
                    this.toImpfungBooster(dossier.registrierungsnummer as string, krankheit);
                }
                break;
            default:
                throw Error('Nicht behandelter Status: ' + dossier.status);
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
        this.router.navigate(['startseite'], {queryParams: {err: 'ERR_NO_IMPFABLE_PERS'}});
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
        booster?: boolean
    ): void {
        if (this.router.url.includes('/impfdokumentation')) {
            this.router.navigateByUrl('/', {skipLocationChange: true}).then(() => {
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
        booster?: boolean
    ): void {
        if (booster) {
            this.router.navigate(['person', registrierungsnummer, 'impfdokumentation', 'booster', krankheitsIdentifier]);
        } else {
            this.router.navigate(['person', registrierungsnummer, 'impfdokumentation', krankheitsIdentifier]);
        }
    }

    private toKontrolle1(registrierungsnummer: string, krankheitsIdentifier: KrankheitIdentifierTS): void {
        this.router.navigate(['person', registrierungsnummer, 'kontrolle', krankheitsIdentifier]);
    }

    private toKontrolle2(registrierungsnummer: string, krankheitsIdentifier: KrankheitIdentifierTS): void {
        this.router.navigate(['person', registrierungsnummer, 'kontrolle', krankheitsIdentifier]);
    }

    private toKontrolleBooster(registrierungsnummer: string, krankheitsIdentifier: KrankheitIdentifierTS): void {
        this.router.navigate(['person', registrierungsnummer, 'kontrolle', krankheitsIdentifier]);
    }

    private toGeimpft(registrierungsnummer: string): void {
        if (this.router.url.includes('/geimpft')) {
            this.router.navigateByUrl('/infopage', {skipLocationChange: true}).then(() => {
                LOG.info('forced reload of component during navigation');
                this.router.navigate(['person', registrierungsnummer, 'geimpft']);
            });
        } else {
            this.router.navigate(['person', registrierungsnummer, 'geimpft']);
        }
    }

    private showInfoBereitsKontrolliert(dossier: DashboardJaxTS): void {
        Swal.fire({
            icon: 'info',
            text: this.translationService.instant('NAVIGATION_SERVICE.PERSON_BEREITS_KONTROLLIERT',
                {person: this.getPersonInfos(dossier)}),
            showConfirmButton: true,
        });
    }

    private getPersonInfos(dashboardJax: DashboardJaxTS): string {
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

    private isCurrentBenutzerAllowedForExactlyOneKrankheit(): boolean {
        return this.authServiceRS.getKrankheitForUserIfOnlyAllowedForExactlyOne() !== undefined;
    }

    public reloadCurrentPage(): void {
        const currentUrl = this.router.url;
        // forces a component reinit by navigating to an empty infopage inbetween
        this.router.navigateByUrl('/infopage?htmlstr=', {skipLocationChange: true}).then(() => {
            this.router.navigate([currentUrl]);
        });
    }

    public navigateForKrankheit(
        dossier: DashboardJaxTS,
        krankheitFromRoute: KrankheitIdentifierTS,
    ): void {
        if (krankheitFromRoute === KrankheitIdentifierTS.COVID) {
            this.navigateForCovid(dossier, krankheitFromRoute);
        } else {
            this.navigate(dossier);
        }
    }
}
