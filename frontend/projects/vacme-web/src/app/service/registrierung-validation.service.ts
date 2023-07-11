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
import {TranslateService} from '@ngx-translate/core';
import {ReplaySubject, Subject} from 'rxjs';
import {SweetAlertResult} from 'sweetalert2';
import Swal from 'sweetalert2/dist/sweetalert2.js'; // nur das JS importieren
import {
    DashboardJaxTS,
    ImpffolgeTS,
    ImpfstoffJaxTS,
    ImpfterminJaxTS,
    KrankheitIdentifierTS,
    OrtDerImpfungDisplayNameJaxTS,
    PrioritaetTS,
    RegistrierungStatusTS,
} from 'vacme-web-generated';
import {IMPFSTOFF_ID_KINDERIMPFUNG} from '../../../../vacme-web-shared/src/lib/constants';
import {LogFactory} from '../../../../vacme-web-shared/src/lib/logging';
import {TSErrorLevel} from '../../../../vacme-web-shared/src/lib/model/TSErrorLevel';
import TSRegistrierungValidation from '../../../../vacme-web-shared/src/lib/model/TSRegistrierungValidation';
import {TSRegistrierungViolationType} from '../../../../vacme-web-shared/src/lib/model/TSRegistrierungViolationType';
import {TerminUtilService} from '../../../../vacme-web-shared/src/lib/service/termin-util.service';
import {VacmeSettingsService} from '../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';
import {isSpecialPrio} from '../../../../vacme-web-shared/src/lib/util/parsePrio';
import {
    isAtLeastFreigegeben,
    isAtLeastFreigegebenBooster,
    isCurrentlyAbgeschlossen,
} from '../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';

const LOG = LogFactory.createLog('RegistrierungValidationService');

// noinspection FunctionWithMoreThanThreeNegationsJS
@Injectable({
    providedIn: 'root'
})
export class RegistrierungValidationService {

    // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
    private _registrierungValidationEventStream$: Subject<Array<TSRegistrierungValidation>>;
    private freigegebenePrio: PrioritaetTS[] = [PrioritaetTS.A];

    constructor(
        protected translateService: TranslateService,
        private terminUtilService: TerminUtilService,
        private vacmeSettingsService: VacmeSettingsService,
    ) {
        // emmited die letzte gemachte validierung die innert 5 sec passiert ist
        this._registrierungValidationEventStream$ = new ReplaySubject<Array<TSRegistrierungValidation>>(1, 5000);
    }

    private getTerminForImpffolge(
        impftermin: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS
    ): ImpfterminJaxTS | undefined {
        switch (impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return impftermin?.termin1;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return impftermin?.termin2;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return impftermin?.terminNPending;
        }
    }

    public validateTermineAndStatusAndUserodi(
        krankheit: KrankheitIdentifierTS,
        impftermin: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[],
    ): void {
        this.validateTermineAndStatusAndOdiAndImpfstoffAndUserodi(
            krankheit, impftermin, impffolge, null, null, odisOfCurrUser);
    }

    public validateTermineAndStatusAndOdiAndImpfstoffAndUserodi(
        krankheit: KrankheitIdentifierTS,
        impftermin: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        ortDerImpfungId: string | null,
        impfstoff: ImpfstoffJaxTS | null | undefined,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[],
    ): void {
        this.doValidate(krankheit, impftermin, impffolge, ortDerImpfungId, impfstoff, odisOfCurrUser);
    }

    private doValidate(
        krankheit: KrankheitIdentifierTS,
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        ortDerImpfungId: string | null,
        impfstoff: ImpfstoffJaxTS | null | undefined,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[]
    ): void {
        // Validierungen, die sowohl fuer Grundimmunisierung wie fuer Booster gelten:
        const nichtFreigegbenEvent = this.checkNichtFreigegeben(krankheit, impfterminInfos, impffolge);
        const keinTerminEvent = this.checkKeinenTermin(krankheit, impfterminInfos, impffolge, odisOfCurrUser);
        const falscherTerminEvent = this.checkFalscherTermin(krankheit, impfterminInfos, impffolge);
        const kontrolle2SameDayEvent = this.checkImpfungSameDay(impfterminInfos, impffolge);
        const falscherOdiEvent = this.checkFalscherOdiForTermin(impfterminInfos, impffolge, ortDerImpfungId);
        const odiInaktivEvent = this.checkOdiInaktiv(impffolge, ortDerImpfungId, odisOfCurrUser);
        const ageWarningChildEvent = this.checkAgeWarningChildNeeded(krankheit, impfterminInfos, impfstoff);
        const ageWarningAdultEvent = this.checkAgeWarningAdultNeeded(krankheit, impfterminInfos, impfstoff);
        const userOdiNoMatchEvent = this.checkUserodiNotMatching(impfterminInfos, impffolge, odisOfCurrUser);
        const impfstoffEingestellt = this.checkImpfstoffNotEingestellt(impfstoff);
        // Alle Fehler aufs Mal anzeigen
        let regValidations: Array<TSRegistrierungValidation | undefined> = [];
        if (impffolge === ImpffolgeTS.BOOSTER_IMPFUNG) {
            regValidations =
                [nichtFreigegbenEvent, keinTerminEvent, falscherTerminEvent, kontrolle2SameDayEvent,
                    falscherOdiEvent, odiInaktivEvent, ageWarningChildEvent, ageWarningAdultEvent,
                    userOdiNoMatchEvent, impfstoffEingestellt];
        } else {
            const andererImpfstoffEvent = this.checkAndererImpfstoff(impfterminInfos, impffolge, impfstoff);
            const nichtFreigegebenePrioEvent = this.checkImpfgruppeNichtFrei(impfterminInfos?.prioritaet);
            const minDaysBetweenImpfungEvents = this.checkMinDaysBetweenImpfungen(krankheit, impfterminInfos);
            const maxDaysBetweenImpfungEvents = this.checkMaxDaysBetweenImpfungen(krankheit, impfterminInfos);
            regValidations =
                [nichtFreigegbenEvent, nichtFreigegebenePrioEvent, keinTerminEvent, falscherTerminEvent,
                    falscherOdiEvent, odiInaktivEvent, andererImpfstoffEvent, kontrolle2SameDayEvent,
                    ageWarningChildEvent, ageWarningAdultEvent, userOdiNoMatchEvent,
                    minDaysBetweenImpfungEvents, maxDaysBetweenImpfungEvents, impfstoffEingestellt];
        }
        this.displayRegistrierungValidations(regValidations, impfterminInfos?.status);
    }

    private displayRegistrierungValidations(
        regValidations: Array<TSRegistrierungValidation | undefined>,
        status: RegistrierungStatusTS | undefined
    ): void {
        //  undefineds rauswerfen und dann casten damit TS happy ist
        const nonNullValidations = regValidations
            .filter(value => value !== undefined)
            .map(value => value as TSRegistrierungValidation);
        this.addValidationEvents(nonNullValidations);

        // als Popup kommen nur events mit dem Level Severe. Zudem kommen sie nur auf der Kontrolle. Daher noch den
        // Status validieren
        const eventsForPopup = this.filterOutNonPopupEvents(nonNullValidations, status);

        if (eventsForPopup && eventsForPopup.length !== 0) {
            const joinedMsg = eventsForPopup.map(value => value.message).join('<br/>');
            Swal.fire({
                titleText: this.translateService.instant('REG_VALIDATION.TITLE'),
                html: '<br/>' + joinedMsg,
                icon: 'warning',
                showConfirmButton: true
            });
        }
    }

    private filterOutNonPopupEvents(
        nonNullValidations: TSRegistrierungValidation[],
        status: RegistrierungStatusTS | undefined
    ): Array<TSRegistrierungValidation> {
        let eventsForPopup = nonNullValidations
            .filter(value => !value.clearEvent)
            .filter(value => value.severity === TSErrorLevel.SEVERE || value.severity === TSErrorLevel.ERROR);
        if (eventsForPopup && eventsForPopup.length !== 0) {
            // Kontrolle oder Impfung bereits durchgefuehrt -> einige Validierungen unterdruecken
            if (this.isStatusThatSuppressesSomeValidations(status)) {
                const suppressed = [];
                const eventsForImpfdoku = [];
                for (const valEvent of eventsForPopup) {
                    if (valEvent.severity === TSErrorLevel.ERROR) {
                        // ERROR wird in diesen Stati nicht mehr angezeigt, nur SEVERE
                        suppressed.push(valEvent);
                    } else if (valEvent.type === TSRegistrierungViolationType.DIFFERENT_IMPFSTOFF
                        && isCurrentlyAbgeschlossen(status)) {
                        // Impfstoffwarnung soll nach erfolgter Impfung nicht mehr kommen
                        suppressed.push(valEvent);
                    } else {
                        // andere Warnungen werden angezeigt
                        eventsForImpfdoku.push(valEvent);
                    }
                }
                LOG.info('Supressed ValidtionPopup because Status is ', status, suppressed);
                eventsForPopup = eventsForImpfdoku;
            }
        }
        return eventsForPopup;
    }

    private isStatusThatSuppressesSomeValidations(status?: RegistrierungStatusTS): boolean {
        return !!status && this.statusesThatSuppressSomeValidations().includes(status);
    }

    private statusesThatSuppressSomeValidations(): Array<RegistrierungStatusTS> {
        return [
            // Kontrolle durchgefuehrt (Kontrollformular grau / Impfdok)
            RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT,
            RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT,
            RegistrierungStatusTS.KONTROLLIERT_BOOSTER, // neu
            // Impfung durchgefuehrt (Impfdok grau)
            RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
            RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
            RegistrierungStatusTS.ABGESCHLOSSEN,
            RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT];
            // Ohne IMMUNISIERT: koennte bereits in der Kontrolle des naechsten Boosters sein (nach der Impfung ist vor der Impfung!)
    }

    private isNichtFreigegeben(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS
    ): boolean {
        if (!!impfterminInfos) {
            if (impffolge === ImpffolgeTS.BOOSTER_IMPFUNG) {
                return !isAtLeastFreigegebenBooster(impfterminInfos?.status);
            } else {
                return !isAtLeastFreigegeben(impfterminInfos.status);
            }
        }
        return false;
    }

    private isNichtFreigegebenSelbstzahler(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS
    ): boolean {
        if (!!impfterminInfos) {
            if (impffolge === ImpffolgeTS.BOOSTER_IMPFUNG) {
                return !this.isFreigegebenFor(impfterminInfos?.impfdossier?.impfschutzJax?.freigegebenAbSelbstzahler);
            }
        }
        return false;
    }

    private isFreigegebenFor(datum: Date | undefined | null): boolean {
        return datum == null ? false : !DateUtil.isAfterToday(datum);
    }

    private checkImpfstoffNotEingestellt(impfstoff: ImpfstoffJaxTS | null | undefined): TSRegistrierungValidation {
        if (impfstoff?.eingestellt) {
            const msg = this.translateService.instant('REG_VALIDATION.IMPFSTOFF_EINGESTELLT',
                {impfstoff: impfstoff.displayName});
            const tsRegistrierungValidation = new TSRegistrierungValidation(TSRegistrierungViolationType.IMPFSTOFF_EINGESTELLT, msg);
            tsRegistrierungValidation.severity = TSErrorLevel.SEVERE; // SEVERE damit Popup beim impfen angezeigt wird
            return tsRegistrierungValidation;
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.IMPFSTOFF_EINGESTELLT);
    }

    private checkNichtFreigegeben(
        krankheit: KrankheitIdentifierTS,
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS
    ): TSRegistrierungValidation | undefined {
        // TODO Affenpocken: Selbstzahler aktuell nur fuer COVID möglich
        if (KrankheitIdentifierTS.COVID === krankheit) {
            if (this.isNichtFreigegebenSelbstzahler(impfterminInfos, impffolge)) {
                const msg = this.translateService.instant('REG_VALIDATION.NICHT_FREIGEGEBEN_SELBSTZAHLER');
                return new TSRegistrierungValidation(TSRegistrierungViolationType.NICHT_FREIGEGEBEN_SELBSTZAHLER, msg);
            }
        }
        else if (this.isNichtFreigegeben(impfterminInfos, impffolge)) {
            const msg = this.translateService.instant('REG_VALIDATION.WRONG_STATUS');
            return new TSRegistrierungValidation(TSRegistrierungViolationType.WRONG_STATUS, msg);
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.WRONG_STATUS);
    }

    private checkImpfgruppeNichtFrei(prio?: PrioritaetTS): TSRegistrierungValidation | undefined {
        // TODO Affenpocken: Abklaeren, ist die prio fuer Affenpocken relevant?
        if (prio !== undefined && !isSpecialPrio(prio) && !this.freigegebenePrio.includes(prio)) {
            const msg = this.translateService.instant('REG_VALIDATION.WRONG_IMPFGRUPPE', {gruppe: prio});
            const tsRegistrierungValidation = new TSRegistrierungValidation(TSRegistrierungViolationType.WRONG_IMPFGRUPPE, msg);
            tsRegistrierungValidation.severity = TSErrorLevel.WARNING;
            return tsRegistrierungValidation;
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.WRONG_IMPFGRUPPE);
    }

    private checkKeinenTermin(
        krankheit: KrankheitIdentifierTS,
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[]
    ): TSRegistrierungValidation | undefined {
        // Wenn keine Terminbuchung aktiv ist, wollen wir auch nicht warnen, dass kein Termin gebucht wurde.
        if (this.vacmeSettingsService.supportsTerminbuchung(krankheit)) {

            // Die Warnung soll NICHT kommen, wenn der Benutzer berechtigt ist fuer ein ODI, und dieses keine Termine hat
            // ODER wenn "Impfung beim Hausarzt" ausgewaehlt wurde
            if (impfterminInfos) {
                const berechtigtFuerOdiOhneTermine = impfterminInfos.gewuenschterOrtDerImpfung
                    && this.isUserBerechtigtForOdiAndOdiWithoutTermin(impfterminInfos.gewuenschterOrtDerImpfung,
                        odisOfCurrUser);
                const nichtVerwalteterOdi = impfterminInfos.nichtVerwalteterOdiSelected
                    && this.hasUserOnlyOdisWithoutTermin(odisOfCurrUser);

                if (berechtigtFuerOdiOhneTermine || nichtVerwalteterOdi) {
                    return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.NO_TERMIN,
                        impffolge);
                }
            }
            if (!this.getTerminForImpffolge(impfterminInfos, impffolge)) {
                const msg = this.translateService.instant('REG_VALIDATION.NO_TERMIN');
                return new TSRegistrierungValidation(TSRegistrierungViolationType.NO_TERMIN, msg, impffolge);
            }
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.NO_TERMIN, impffolge);
    }

    private isUserBerechtigtForOdiAndOdiWithoutTermin(
        gewuenschterOrtDerImpfung: OrtDerImpfungDisplayNameJaxTS,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[]
    ): boolean {
        const allowedForOd = !!this.getOdiById(gewuenschterOrtDerImpfung.id, odisOfCurrUser);
        return allowedForOd && gewuenschterOrtDerImpfung.terminverwaltung !== true;
    }

    private hasUserOnlyOdisWithoutTermin(odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[]): boolean {
        if (odisOfCurrUser) {
            return odisOfCurrUser.every(value => value.terminverwaltung !== true);
        }
        return false;
    }

    private isFalscherTermin(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS
    ): boolean {
        const impftermin = this.getTerminForImpffolge(impfterminInfos, impffolge);
        return !!impfterminInfos && !!impftermin && !DateUtil.isToday(impftermin?.impfslot?.zeitfenster?.von);
    }

    private checkFalscherTermin(
        krankheit: KrankheitIdentifierTS,
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS
    ): TSRegistrierungValidation | undefined {
        // Wenn keine Terminbuchung aktiv ist, wollen wir auch nicht warnen, wenn es nicht der richtige ist
        if (this.vacmeSettingsService.supportsTerminbuchung(krankheit)) {
            if (this.isFalscherTermin(impfterminInfos, impffolge)) {
                const msg = this.translateService.instant('REG_VALIDATION.WRONG_TERMIN');
                return new TSRegistrierungValidation(TSRegistrierungViolationType.WRONG_TERMIN, msg, impffolge);
            }
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.WRONG_TERMIN, impffolge);
    }

    private isImpfungSameDay(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS
    ): boolean {
        if (!!impfterminInfos) {
            if (ImpffolgeTS.ZWEITE_IMPFUNG === impffolge) {
                return !!impfterminInfos?.termin1
                    && DateUtil.isToday(impfterminInfos?.termin1?.impfslot?.zeitfenster?.von);
            } else if (ImpffolgeTS.BOOSTER_IMPFUNG === impffolge) {
                // TODO Booster: Folgebooster muessen mit vorherigem Booster verglichen werden, nicht mit Impfung 2
                return !!impfterminInfos?.termin2
                    && DateUtil.isToday(impfterminInfos?.termin2?.impfslot?.zeitfenster?.von);
            }
        }
        return false;
    }

    private checkImpfungSameDay(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS
    ): TSRegistrierungValidation | undefined {
        if (this.isImpfungSameDay(impfterminInfos, impffolge)) {
            const msg = this.translateService.instant('REG_VALIDATION.IMPFUNG_WAS_SAME_DAY');
            return new TSRegistrierungValidation(TSRegistrierungViolationType.IMPFUNG_WAS_SAME_DAY, msg, impffolge);
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.IMPFUNG_WAS_SAME_DAY, impffolge);
    }

    private isFalscherOdiForTermin(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        ortDerImpfungId: string | null
    ): boolean {
        const impftermin = this.getTerminForImpffolge(impfterminInfos, impffolge);
        return !!ortDerImpfungId
            && !!impftermin
            && !!impftermin?.impfslot?.ortDerImpfung
            && ortDerImpfungId !== impftermin?.impfslot?.ortDerImpfung?.id;
    }

    private checkFalscherOdiForTermin(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        ortDerImpfungId: string | null
    ): TSRegistrierungValidation | undefined {
        if (this.isFalscherOdiForTermin(impfterminInfos, impffolge, ortDerImpfungId)) {
            const msg = this.translateService.instant('REG_VALIDATION.WRONG_ODI');
            return new TSRegistrierungValidation(TSRegistrierungViolationType.WRONG_ODI, msg, impffolge);
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.WRONG_ODI, impffolge);
    }

    private isOdiInaktiv(
        ortDerImpfungId: string | null,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[]
    ): boolean {
        // prueft ob das ausgewaehlte ODI noch aktiv ist
        const find: OrtDerImpfungDisplayNameJaxTS | undefined = odisOfCurrUser.find(value => value.id === ortDerImpfungId);
        return !!find && !!find.deaktiviert;
    }

    private checkOdiInaktiv(
        impffolge: ImpffolgeTS,
        ortDerImpfungId: string | null,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[]
    ): TSRegistrierungValidation | undefined {
        if (this.isOdiInaktiv(ortDerImpfungId, odisOfCurrUser)) {
            const msg = this.translateService.instant('REG_VALIDATION.ODI_INAKTIV');
            const validation = new TSRegistrierungValidation(TSRegistrierungViolationType.ODI_INAKTIV, msg, impffolge);
            validation.severity = TSErrorLevel.SEVERE; // SEVERE damit Popup beim impfen angezeigt wird
            return validation;
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.ODI_INAKTIV, impffolge);
    }

    private isUserodiNotMatching(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[]
    ): boolean {
        const impftermin = this.getTerminForImpffolge(impfterminInfos, impffolge);
        if (!!impftermin
            && !!impftermin?.impfslot?.ortDerImpfung) {

            // prueft ob eines der odis den aktuellen users dem gesuchten odi entspricht
            const ortDerImpfung = impftermin.impfslot.ortDerImpfung;
            const isAllowedOdi = odisOfCurrUser.map(currOdi => currOdi.id)
                .filter(value => value !== undefined)
                .some(odiId => odiId === ortDerImpfung.id);

            return !isAllowedOdi; // wir wollen wissen ob der user den ort nicht hat

        }
        return false;
    }

    private getOdiById(
        odiId: string | null | undefined,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[]
    ): OrtDerImpfungDisplayNameJaxTS | undefined {
        if (!odiId) {
            return undefined;
        }
        return odisOfCurrUser.find(value => value.id === odiId);
    }

    private checkUserodiNotMatching(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        odisOfCurrUser: OrtDerImpfungDisplayNameJaxTS[]
    ): TSRegistrierungValidation | undefined {
        if (this.isUserodiNotMatching(impfterminInfos, impffolge, odisOfCurrUser)) {
            let num: string;
            if (impffolge === ImpffolgeTS.BOOSTER_IMPFUNG) {
                num = '';
            } else {
                num = (impffolge === ImpffolgeTS.ERSTE_IMPFUNG) ? '1' : '2';
            }
            const msg = this.translateService.instant('REG_VALIDATION.USER_NOT_FOR_ODI', {impffolge: num});
            return new TSRegistrierungValidation(TSRegistrierungViolationType.USER_NOT_FOR_ODI, msg, impffolge);
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.USER_NOT_FOR_ODI, impffolge);
    }

    private isMinDaysBetweenImpfungInvalid(
        impfterminInfos: DashboardJaxTS | undefined
    ): boolean {
        const termin1 = impfterminInfos?.termin1;
        const termin2 = impfterminInfos?.termin2;
        if (!!termin1 && !!termin2 && !!termin1.impfslot && !!termin2.impfslot) {
            const daysDiff = this.terminUtilService.getDaysDiff(termin2.impfslot, termin1.impfslot);
            return daysDiff < this.terminUtilService.getMinDiff();
        }
        return false;
    }

    private isMaxDaysBetweenImpfungInvalid(
        impfterminInfos: DashboardJaxTS | undefined
    ): boolean {
        const termin1 = impfterminInfos?.termin1;
        const termin2 = impfterminInfos?.termin2;
        if (!!termin1 && !!termin2 && !!termin1.impfslot && !!termin2.impfslot) {
            const daysDiff = this.terminUtilService.getDaysDiff(termin2.impfslot, termin1.impfslot);
            return daysDiff > this.terminUtilService.getMaxDiff();
        }
        return false;
    }

    private checkMinDaysBetweenImpfungen(
        krankheit: KrankheitIdentifierTS,
        impfterminInfos: DashboardJaxTS | undefined,
    ): TSRegistrierungValidation | undefined {
        // TODO Affenpocken: checkMinDaysBetweenImpfungen aktuell nur fuer COVID
        if (KrankheitIdentifierTS.COVID === krankheit) {
            if (this.isMinDaysBetweenImpfungInvalid(impfterminInfos)) {
                const msg = this.translateService.instant('REG_VALIDATION.MIN_DAYS_NOT_VALID');
                // wir senden diese validierung immer als zur 2. impfung gehoerend
                return new TSRegistrierungValidation(TSRegistrierungViolationType.MIN_DAYS_NOT_VALID,
                    msg,
                    ImpffolgeTS.ZWEITE_IMPFUNG);
                }
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.MIN_DAYS_NOT_VALID, ImpffolgeTS.ZWEITE_IMPFUNG);
    }

    private checkMaxDaysBetweenImpfungen(
        krankheit: KrankheitIdentifierTS,
        impfterminInfos: DashboardJaxTS | undefined,
    ): TSRegistrierungValidation | undefined {
        // TODO Affenpocken: checkMaxDaysBetweenImpfungen aktuell nur fuer COVID
        if (KrankheitIdentifierTS.COVID === krankheit) {
            if (this.isMaxDaysBetweenImpfungInvalid(impfterminInfos)) {
                const msg = this.translateService.instant('REG_VALIDATION.MAX_DAYS_NOT_VALID');
                // wir senden diese validierung immer als zur 2. impfung gehoerend
                return new TSRegistrierungValidation(TSRegistrierungViolationType.MAX_DAYS_NOT_VALID,
                    msg,
                    ImpffolgeTS.ZWEITE_IMPFUNG);
            }
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.MAX_DAYS_NOT_VALID, ImpffolgeTS.ZWEITE_IMPFUNG);
    }

    public isAndererImpfstoff(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        impfstoff: ImpfstoffJaxTS | null | undefined
    ): boolean {
        if (!!impfstoff && !!impfterminInfos) {
            if (ImpffolgeTS.ZWEITE_IMPFUNG === impffolge) {
                // wenn wir keine impfung1 haben nicht validieren, kommt temporaer vor bei kontrolle wegen async reload
                return !!impfterminInfos.impfung1 && impfstoff.id !== impfterminInfos?.impfung1?.impfstoff?.id;
            }
            // Booster: keine Impfstoff-Validierung noetig, da viele Varianten empfohlen und weil wir impfung_n-1 nicht
            // kennen
        }
        return false;
    }

    private checkAndererImpfstoff(
        impfterminInfos: DashboardJaxTS | undefined,
        impffolge: ImpffolgeTS,
        impfstoff: ImpfstoffJaxTS | null | undefined
    ): TSRegistrierungValidation | undefined {
        if (this.isAndererImpfstoff(impfterminInfos, impffolge, impfstoff)) {
            const msg = this.translateService.instant('REG_VALIDATION.DIFFERENT_IMPFSTOFF');
            const validation = new TSRegistrierungValidation(TSRegistrierungViolationType.DIFFERENT_IMPFSTOFF, msg, impffolge);
            validation.severity = TSErrorLevel.SEVERE;
            return validation;
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.DIFFERENT_IMPFSTOFF, impffolge);
    }

    private isAgeWarningChildNeededCovid(
        dossier: DashboardJaxTS | undefined,
        impfstoff: ImpfstoffJaxTS | null | undefined,
    ): boolean {
        if (dossier && dossier.geburtsdatum) {
            const geburtsdatum = dossier.geburtsdatum;
            const age = DateUtil.age(geburtsdatum);
            const warnMinAge = this.vacmeSettingsService.getWarnMinAgeImpfling(KrankheitIdentifierTS.COVID);
            if (warnMinAge !== undefined && age < warnMinAge && impfstoff && impfstoff?.id !== IMPFSTOFF_ID_KINDERIMPFUNG) {
                // Kind ohne Kinderimpfstoff
                return true;
            }
        }
        return false;
    }

    private isAgeWarningChildNeeded(
        dossier: DashboardJaxTS | undefined,
        krankheit: KrankheitIdentifierTS,
    ): boolean {
        if (dossier && dossier.geburtsdatum) {
            const warnAlterImpflingForKrankheit = this.vacmeSettingsService.getWarnMinAgeImpfling(krankheit);
            const geburtsdatum = dossier.geburtsdatum;
            const age = DateUtil.age(geburtsdatum);
            if (warnAlterImpflingForKrankheit !== undefined && age < warnAlterImpflingForKrankheit) {
                return true;
            }
        }
        return false;
    }

    private isAgeWarningAdultNeeded(
        krankheit: KrankheitIdentifierTS,
        dossier: DashboardJaxTS | undefined,
        impfstoff: ImpfstoffJaxTS | null | undefined,
    ): boolean {
        if (dossier && dossier.geburtsdatum) {
            const geburtsdatum = dossier.geburtsdatum;
            const age = DateUtil.age(geburtsdatum);
            const warnMinAge = this.vacmeSettingsService.getWarnMinAgeImpfling(krankheit);
            if (warnMinAge !== undefined && age > warnMinAge && impfstoff && impfstoff?.id === IMPFSTOFF_ID_KINDERIMPFUNG) {
                // Erwachsener mit Kinderimpfstoff
                return true;
            }
        }
        return false;
    }

    private checkAgeWarningChildNeeded(
        krankheit: KrankheitIdentifierTS,
        impfterminInfos: DashboardJaxTS | undefined,
        impfstoff: ImpfstoffJaxTS | null | undefined,
    ): TSRegistrierungValidation | undefined {
        // die Warnung soll nur auf der impfdokumentation page kommen, daher status checken
        if (this.isInStateForImpfdokumentation(impfterminInfos?.status)) {
            if (KrankheitIdentifierTS.COVID === krankheit
                && this.isAgeWarningChildNeededCovid(impfterminInfos, impfstoff)
            ) {
                const msg = this.translateService.instant('REG_VALIDATION.AGE_WARNING_CHILD_COVID');
                const validation = new TSRegistrierungValidation(TSRegistrierungViolationType.AGE_WARNING_CHILD, msg);
                validation.severity = TSErrorLevel.SEVERE;
                return validation;
            }
            if (KrankheitIdentifierTS.COVID !== krankheit
                && this.isAgeWarningChildNeeded(impfterminInfos, krankheit)
            ){
                const warnMinAge = this.vacmeSettingsService.getWarnMinAgeImpfling(krankheit);
                const msg = this.translateService.instant('REG_VALIDATION.AGE_WARNING_CHILD',{warnage: warnMinAge} );
                const validation = new TSRegistrierungValidation(TSRegistrierungViolationType.AGE_WARNING_CHILD, msg);
                validation.severity = TSErrorLevel.SEVERE;
                return validation;
            }
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.AGE_WARNING_CHILD);
    }

    private checkAgeWarningAdultNeeded(
        krankheit: KrankheitIdentifierTS,
        impfterminInfos: DashboardJaxTS | undefined,
        impfstoff: ImpfstoffJaxTS | null | undefined,
    ): TSRegistrierungValidation | undefined {
        // die Warnung soll nur auf der impfdokumentation page kommen, daher status checken
        if (KrankheitIdentifierTS.COVID === krankheit
            && this.isAgeWarningAdultNeeded(krankheit, impfterminInfos, impfstoff)
            && this.isInStateForImpfdokumentation(impfterminInfos?.status)) {
            const msg = this.translateService.instant('REG_VALIDATION.AGE_WARNING_ADULT_COVID');
            const validation = new TSRegistrierungValidation(TSRegistrierungViolationType.AGE_WARNING_ADULT, msg);
            validation.severity = TSErrorLevel.SEVERE;
            return validation;
        }
        return TSRegistrierungValidation.createClearEvent(TSRegistrierungViolationType.AGE_WARNING_ADULT);
    }

    private isInStateForImpfdokumentation(status?: RegistrierungStatusTS): boolean {
        return status === RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT
            || status === RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT
            || status === RegistrierungStatusTS.KONTROLLIERT_BOOSTER;
    }

    public showConfirmPopupAndererImpfstoff(): Promise<SweetAlertResult> {
        return Swal.fire({
            titleText: this.translateService.instant('REG_VALIDATION.TITLE'),
            html: this.translateService.instant('REG_VALIDATION.DIFFERENT_IMPFSTOFF'),
            icon: 'warning',
            showConfirmButton: true,
            confirmButtonText: this.translateService.instant('REG_VALIDATION.BUTTON_SAVE_ANYWAY'),
            showCancelButton: true,
            cancelButtonText: this.translateService.instant('REG_VALIDATION.BUTTON_CANCEL'),
        });
    }

    private addValidationEvents(validations: Array<TSRegistrierungValidation>): void {
        if (validations !== undefined && Array.isArray(validations)) {
            // console.log('emitted', validations);
            this._registrierungValidationEventStream$.next(validations);
        }
    }

    public get registrierungValidationEventStrdeam$(): Subject<Array<TSRegistrierungValidation>> {
        return this._registrierungValidationEventStream$;
    }

    public setFreigegebenePrio(value: PrioritaetTS[]): void {
        this.freigegebenePrio = value;
    }
}
