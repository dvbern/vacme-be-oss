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
import {ActivatedRoute, Params, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {Observable, of} from 'rxjs';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    DashboardJaxTS,
    ImpffolgeTS,
    ImpfterminJaxTS,
    KrankheitIdentifierTS,
    NextFreierTerminJaxTS,
    OrtDerImpfungDisplayNameJaxTS,
    OrtDerImpfungJaxTS,
    TerminbuchungService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {NextFreierTerminSearch} from '../../../../vacme-web-shared';
import {ErrorMessageService} from '../../../../vacme-web-shared/src/lib/service/error-message.service';
import {TerminUtilService} from '../../../../vacme-web-shared/src/lib/service/termin-util.service';
import {TerminfindungService} from '../../../../vacme-web-shared/src/lib/service/terminfindung.service';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {ConfirmUtil} from '../../../../vacme-web-shared/src/lib/util/confirm-util';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';
import {ImpfdokumentationCacheService} from './impfdokumentation.cache.service';

const LOG = LogFactory.createLog('TerminmanagementKontrolleImpfdokService');

@Injectable({
    providedIn: 'root',
})
export class TerminmanagementKontrolleImpfdokService {

    constructor(
        private terminfindungService: TerminfindungService,
        private terminbuchungService: TerminbuchungService,
        private terminUtilService: TerminUtilService,
        private translationService: TranslateService,
        private router: Router,
        private activeRoute: ActivatedRoute,
        private errorService: ErrorMessageService,
        private vacmeSettingsService: VacmeSettingsService,
        private impfdokumentationCacheService: ImpfdokumentationCacheService,
    ) {
    }

    public setupAdhocSlot1Unsaved(): void {
        // ad hoc Termin neu machen (nur Frontend) und als Slot 1 waehlen
        this.terminfindungService.selectedSlot1 = this.terminUtilService.createAdHocTermin(
            this.translationService.instant('TERMINE.JETZT'),
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            this.terminfindungService.ortDerImpfung!);
    }

    public canProceedTermineUmbuchen$(isUserBerechtigtForOdiOfTermin: boolean): Observable<boolean> {
        let canProceed$: Observable<boolean>;
        if (!isUserBerechtigtForOdiOfTermin) {
            // Nachfragen, falls Termine bei einem ODI, in dem ich nicht berechtigt bin
            canProceed$ = ConfirmUtil.swalAsObservable$(Swal.fire({
                icon: 'question',
                text: this.translationService.instant('TERMINFINDUNG.TERMIN_ANDERER_ODI'),
                showCancelButton: true,
                cancelButtonText: this.translationService.instant('TERMINFINDUNG.ABBRECHEN'),
                confirmButtonText: this.translationService.instant('TERMINFINDUNG.UEBERSCHREIBEN'),
                customClass: {
                    confirmButton: 'primary',
                    cancelButton: 'secondary',
                },
            }));
        } else {
            // Ich bin sowieso berechtigt, also muss ich nicht bestaetigen
            canProceed$ = of(true);
        }
        return canProceed$;
    }

    public umbuchungRequestPut$(
        impffolge: ImpffolgeTS,
        registrierungsNummer: string,
        krankheit: KrankheitIdentifierTS,
    ): Observable<DashboardJaxTS> {
        const impffolge1oder2 = impffolge !== ImpffolgeTS.BOOSTER_IMPFUNG;
        const umbuchungRequest$ = impffolge1oder2
            ? this.terminbuchungService
                .terminbuchungResourceUmbuchenGrundimmunisierung(
                    this.terminfindungService.selectedSlot1?.id as string,
                    this.terminfindungService.selectedSlot2?.id as string,
                    KrankheitIdentifierTS.COVID,
                    registrierungsNummer)
            : this.terminbuchungService
                .terminbuchungResourceUmbuchenBooster(
                    this.terminfindungService.selectedSlotN?.id as string,
                    krankheit,
                    registrierungsNummer);
        return umbuchungRequest$;
    }

    public createAdhocTermin1RequestPut$(registrierungsnummer: string): Observable<DashboardJaxTS> {
        return this.terminbuchungService
            .terminbuchungResourceCreateAdHocTermin1AndBucheTermin2(
                this.terminfindungService.selectedSlot2?.id as string,
                KrankheitIdentifierTS.COVID,
                this.terminfindungService.ortDerImpfung?.id as string,
                registrierungsnummer);
    }

    public confirmTerminAbsagen(impffolge: ImpffolgeTS): Promise<{ isConfirmed: boolean }> {
        const prefix = impffolge === ImpffolgeTS.BOOSTER_IMPFUNG
            ? 'OVERVIEW.CANCEL_TERMIN_BOOSTER.'
            : 'OVERVIEW.CANCEL_TERMIN.';
        return Swal.fire({
            icon: 'question',
            text: this.translationService.instant(prefix + 'QUESTION'),
            showCancelButton: true,
            cancelButtonText: this.translationService.instant(prefix + 'CANCEL'),
            confirmButtonText: this.translationService.instant(prefix + 'CONFIRM'),
            customClass: {
                confirmButton: 'primary',
                cancelButton: 'secondary',
            },
        });
    }

    public reinitializeFromDashboard(dashboardJax: DashboardJaxTS, forceReload: boolean,
                                     impffolge: ImpffolgeTS,
                                     kontrolleOrImpfdok: { isUserBerechtigtForOdiOfTermin: () => boolean },
    ): void {
        const dossierChange = this.terminfindungService.hasChangedDossiernummer(dashboardJax);
        // alle daten bis auf die termine koennen aus dem dossier gelesen werden
        const hasKrankheitChanged = this.terminfindungService.hasChangedKrankheit(dashboardJax);
        this.terminfindungService.setDashboardAndResetDataIfKrankheitChanged(dashboardJax);

        const needsReload = dossierChange || hasKrankheitChanged;
        // den impfort muessen wir auch wiederherstellen  wenn er im current dossier noch nicht gespeichert wurde.
        // i.e. wenn wir von der terminverwaltung zurueck kommen
        if (needsReload || forceReload) {
            this.setOdiInTerminfindung(dashboardJax, impffolge);
        }

        // Wenn ich fuer den ODI des bestehenden Termins nicht berechtigt bin, soll dieser Termin auch nicht angezeigt
        // werden
        if (kontrolleOrImpfdok.isUserBerechtigtForOdiOfTermin()) {
            if (needsReload || forceReload) {
                this.setSlotsInTerminfindung(dashboardJax);
            }
        } else {
            // Wenn ich fuer den ODI des bestehenden Termins nicht berechtigt bin darf ich nur vergangene termine sehen
            // die ich nicht umbuchen kann (e.g wo die Impfung schon erfolgt ist)

            // wenn termin1 schon geimpft wurde dann kann man den sicher setzen auch wenn ich nicht berechtigt waere
            if (this.terminfindungService.dashboard?.impfung1) {
                this.terminfindungService.selectedSlot1 = dashboardJax.termin1?.impfslot;
            }
            if (needsReload || forceReload) {
                this.resetSlotsInTerminfindungIfNichtBerechtigt(impffolge);
            }
        }
    }

    private resetSlotsInTerminfindungIfNichtBerechtigt(
        impffolge: ImpffolgeTS | ImpffolgeTS.ZWEITE_IMPFUNG | ImpffolgeTS.BOOSTER_IMPFUNG): void {

        // ich bin fuer das odi des termins nicht berechtigt: mein odi setzen, aber alle noch nciht
        // wahrgenommenen termien aus dem terminfindungsservice entfernen
        if (impffolge === ImpffolgeTS.ERSTE_IMPFUNG) {
            this.terminfindungService.selectedSlot1 = undefined;
            this.terminfindungService.selectedSlot2 = undefined;
        } else if (impffolge === ImpffolgeTS.ZWEITE_IMPFUNG) {
            this.terminfindungService.selectedSlot2 = undefined;
        } else {
            this.terminfindungService.selectedSlotN = undefined;
        }

    }

    private setOdiInTerminfindung(dashboardJax: DashboardJaxTS, impffolge: ImpffolgeTS): void {
        const dashboardTermin = this.getTerminOfImpffolge(dashboardJax, impffolge);
        this.terminfindungService.ortDerImpfung =
            dashboardTermin?.impfslot?.ortDerImpfung || dashboardJax.gewuenschterOrtDerImpfung;
    }

    private getTerminOfImpffolge(dashboardJax: DashboardJaxTS, impffolge: ImpffolgeTS): ImpfterminJaxTS | undefined {
        switch (impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return dashboardJax.termin1;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return dashboardJax.termin2;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return dashboardJax.terminNPending;
        }
    }

    private setSlotsInTerminfindung(dashboardJax: DashboardJaxTS): void {
        this.terminfindungService.selectedSlot1 = dashboardJax.termin1?.impfslot;
        this.terminfindungService.selectedSlot2 = dashboardJax.termin2?.impfslot;
        this.terminfindungService.selectedSlotN = dashboardJax.terminNPending?.impfslot;
    }

    public gotoNextFreienTermin(
        referrer: string,
        $event: NextFreierTerminSearch,
        krankheit: KrankheitIdentifierTS,
    ): void {
        // Wir muessen den naechsten freien Termin suchen, der fuer mich infrage kommt, d.h.
        // das spaetere von $event.nextDateParam und $event.freigegebenAb
        let laterDate = DateUtil.getLaterDate($event.nextDateParam?.nextDate, $event.freigegebenAb);
        if (!laterDate) {
            laterDate = DateUtil.today().toDate();
        }
        const nextFreiNeu: NextFreierTerminJaxTS = {...$event.nextDateParam, nextDate: laterDate};

        this.terminbuchungService
            .terminbuchungResourceGetNextFreierImpftermin(
                $event.impffolge, krankheit, $event.ortDerImpfungId, nextFreiNeu)
            .subscribe(
                nextReturned => {
                    if (nextReturned) { // Termin gefunden -> navigieren
                        const modifFlag = this.activeRoute.snapshot.queryParamMap.get('modif');
                        const erstTerminAdHocFlag = this.activeRoute.snapshot.queryParamMap.get('erstTerminAdHoc');
                        const modifQueryParams: Params = {
                            modif: modifFlag,
                            erstTerminAdHoc: erstTerminAdHocFlag,
                            referrerPart: referrer,
                            t1: this.terminfindungService.selectedSlot1 ?
                                JSON.stringify(this.terminfindungService.selectedSlot1) : undefined,
                            t2: this.terminfindungService.selectedSlot2 ?
                                JSON.stringify(this.terminfindungService.selectedSlot2) : undefined,
                            tN: this.terminfindungService.selectedSlotN ?
                                JSON.stringify(this.terminfindungService.selectedSlotN) : undefined,
                            status: this.terminfindungService.dashboard.status ?
                                JSON.stringify(this.terminfindungService.dashboard.status) : undefined,
                        };
                        void this.router.navigate([
                                'person',
                                $event.registrierungsnummer,
                                'terminfindung',
                                'krankheit',
                                krankheit,
                                $event.ortDerImpfungId,
                                $event.impffolge,
                                DateUtil.momentToLocalDateTime(moment(nextReturned.nextDate)),
                            ],
                            {
                                queryParams: modifQueryParams,
                            });
                    } else {
                        if ($event.otherTerminDate) { // Warscheinlich ist die Regel +-28 Tage nicht erfuellt
                            const distText = this.vacmeSettingsService.getDistanceDesiredDistanceWithUnitText();
                            const errMsg = this.translationService.instant('ERROR_NO_TERMINE_IN_RANGE',
                                {desiredDistance: distText});
                            this.errorService.addMesageAsError(errMsg);
                        } else { // Warscheinlich hat der Slot keine Termine frei
                            this.errorService.addMesageAsError('ERROR_NO_TERMINE');
                        }
                    }
                },
                error => {
                    LOG.error(error);
                    this.errorService.addMesageAsError('ERROR_NO_TERMINE');
                },
            );
    }

    public identifyOdiIdToSelect(odiList: OrtDerImpfungJaxTS[], krankheit: KrankheitIdentifierTS): string | null {
        if (odiList.length === 1) { // wenn nur 1 direkt selecten
            const odiId: string | null = odiList[0].id ? odiList[0].id : null;
            // Falls wir nur fuer 1 ODI berechtigbt sind: Nur setzen, falls dieses noch aktiv
            return this.odiIdIfActiveOrNull(odiId, odiList, krankheit);
        } else {
            const storedData = this.impfdokumentationCacheService.getImpfdokumentation(krankheit);
            const odiId: string | null = storedData?.ortDerImpfungId ? storedData?.ortDerImpfungId : null;
            // Falls wir ein ODI gefunden haben im LocalCache: Wir setzen es nur, wenn es noch aktiv ist
            return this.odiIdIfActiveOrNull(odiId, odiList, krankheit);
        }
    }

    private odiIdIfActiveOrNull(
        odiId: string | null,
        odiList: OrtDerImpfungJaxTS[],
        krankheit: KrankheitIdentifierTS,
    ): string | null {
        if (odiId) {
            const odiFromList = this.findOdiByIdFromList(odiList, odiId);
            if (odiFromList?.deaktiviert) {
                this.impfdokumentationCacheService.removeOdiFromCache(krankheit);
                return null;
            }
            return odiId;
        }
        return null;
    }

    private findOdiByIdFromList(odiList: OrtDerImpfungJaxTS[], odiIdToLookFor: string | undefined,
    ): OrtDerImpfungDisplayNameJaxTS | undefined {
        return odiList?.find(value => value.id === odiIdToLookFor);
    }
}
