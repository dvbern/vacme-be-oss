/* eslint-disable @typescript-eslint/restrict-template-expressions */
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
import moment from 'moment';
import {
    DashboardJaxTS,
    ImpffolgeTS,
    ImpfslotJaxTS,
    OrtDerImpfungBuchungJaxTS,
    RegistrierungStatusTS,
} from 'vacme-web-generated';
import {
    OrtDerImpfungDisplayNameExtendedJaxTS,
} from '../../../../vacme-web-generated/src/lib/model/ort-der-impfung-display-name-extended-jax';
import {MAX_ODI_DISTANCE} from '../constants';
import DateUtil from '../util/DateUtil';
import {
    isAnyStatusOfBooster,
    isAtLeastGebucht,
    isAtLeastOnceGeimpft,
    isErsteImpfungDoneAndZweitePending,
} from '../util/registrierung-status-utils';
import {TerminUtilService} from './termin-util.service';

/**
 * Service, der den State und ein bisschen Logik besitzt fuer die Buchung von 1 oder 2 Terminen.
 * Beim Oeffnen einer neuen Person muss resetData() aufgerufen werden.
 *
 * Er wird fuer initialreg und Fachapplikation gleichermassen verwendet.
 *
 * GEBUCHTE TERMINE ANZEIGEN UND BUTTON ZUM BUCHEN/UMBUCHEN/:
 * - [initialReg] Overview
 * - [Fachapp] Kontrolle
 * - [Fachapp] ImpfdokBooster
 * - [Fachapp] ImpfdokGrundimmunisierung
 *
 * ODI UND TERMINE ANZEIGEN UND BEARBEITEN UND BUCHEN ODER ABSAGEN:
 * - - TermineBearbeitenComponent mit den gewaehlten/gebuchten Terminen (aus Kontrolle, Impfdok und Overview)
 *
 * KALENDER FUER TERMINFUNDUNG:
 * - [initialReg] TerminfindungPage (initialReg terminfindung/:ortDerImpfungId/:impffolge/:date)
 * - [Fachapp] TerminfindungWebPage (web)
 * ENTHALTEN BEIDE DEN:
 * - - - TerminfindungComponent: Kalender, 1 Termin suchen
 *
 */
@Injectable({
    providedIn: 'root',
})
export class TerminfindungService {

    private _ortDerImpfung: OrtDerImpfungBuchungJaxTS | undefined;
    private _dashboard: DashboardJaxTS = {};
    private _selectedSlot1: ImpfslotJaxTS | undefined;
    private _selectedSlot2: ImpfslotJaxTS | undefined;
    private _selectedSlotN: ImpfslotJaxTS | undefined;
    private _confirmedErkrankungen = false;

    constructor(
        private terminUtilService: TerminUtilService,
        private translationService: TranslateService,
    ) {

    }

    get ortDerImpfung(): OrtDerImpfungBuchungJaxTS | undefined {
        return this._ortDerImpfung;
    }

    set ortDerImpfung(value: OrtDerImpfungBuchungJaxTS | undefined) {
        this._ortDerImpfung = value;
    }

    get dashboard(): DashboardJaxTS {
        return this._dashboard;
    }

    get selectedSlot1(): ImpfslotJaxTS | undefined {
        return this._selectedSlot1;
    }

    set selectedSlot1(value: ImpfslotJaxTS | undefined) {
        this._selectedSlot1 = value;
    }

    get selectedSlot2(): ImpfslotJaxTS | undefined {
        return this._selectedSlot2;
    }

    set selectedSlot2(value: ImpfslotJaxTS | undefined) {
        this._selectedSlot2 = value;
    }

    get selectedSlotN(): ImpfslotJaxTS | undefined {
        return this._selectedSlotN;
    }

    set selectedSlotN(value: ImpfslotJaxTS | undefined) {
        this._selectedSlotN = value;
    }

    get confirmedErkrankungen(): boolean {
        return this._confirmedErkrankungen;
    }

    set confirmedErkrankungen(value: boolean) {
        this._confirmedErkrankungen = value;
    }

    /**
     * gibt den jeweils gegensaetzlichen Impfslot zurueck
     *
     * @param impffolge die Impffolge deren Gegentermin gesucht wird
     */
    public getOtherSelected(impffolge: ImpffolgeTS): ImpfslotJaxTS | undefined {
        switch (impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return this._selectedSlot2;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return this._selectedSlot1;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return undefined;
        }
    }

    /**
     * gibt das Datum am unteren Limit des zulaessigen Datumsbereichs zurueck basierend auf dem anderen gewahlten
     * Impftermin sofern dieser schon gewahlt wurde. Wenn noch nicht gewahlt kommt undefined zurueck
     *
     * @param impffolge die Impffolge fuer die wir einen Termin wahlen
     */
    public getMinDateRestriction(impffolge: ImpffolgeTS): Date | undefined {
        const otherSelected = this.getOtherSelected(impffolge);
        if (otherSelected) {

            switch (impffolge) {
                case ImpffolgeTS.ERSTE_IMPFUNG:
                    return DateUtil.substractDays(moment(this.getDateOfSlot(otherSelected)),
                        this.terminUtilService.getMaxDiff()).toDate();

                case ImpffolgeTS.ZWEITE_IMPFUNG:
                    return DateUtil.addDaysDate(this.getDateOfSlot(otherSelected), this.terminUtilService.getMinDiff());

            }

        }
        return undefined;
    }

    /**
     * gibt das Datum am oberen Limit des zulaessigen Datumsbereichs zurueck basierend auf dem anderen gewahlten
     * Impftermin sofern dieser schon gewahlt wurde. Wenn noch nicht gewahlt kommt undefined zurueck
     *
     * @param impffolge die Impffolge fuer die wir einen Termin wahlen
     */
    public getMaxDateRestriction(impffolge: ImpffolgeTS): Date | undefined {
        // Falls die Termine bereits gebucht, aber noch keine Impfung vorgenommen,
        // dann gilt die Intervallsetzung fuer Termin 1 nicht mehr, weil sonst durch Termin 2 geblockt
        if (impffolge === ImpffolgeTS.ERSTE_IMPFUNG &&
            isAtLeastGebucht(this._dashboard.status) &&
            !isAtLeastOnceGeimpft(this._dashboard.status)) {
            return undefined;
        }
        // Falls bereits die erste Impfung durch und der Kunde versucht eine neues 2.
        // Termin zu finden, dann gilt die Regel Max = 28 + 5 nicht mehr
        if (impffolge === ImpffolgeTS.ZWEITE_IMPFUNG &&
            isAtLeastOnceGeimpft(this._dashboard.status)) {
            return undefined;
        }
        const otherSelected = this.getOtherSelected(impffolge);
        if (otherSelected) {

            switch (impffolge) {
                case ImpffolgeTS.ERSTE_IMPFUNG:
                    return DateUtil.substractDays(moment(this.getDateOfSlot(otherSelected)),
                        this.terminUtilService.getMinDiff()).toDate();

                case ImpffolgeTS.ZWEITE_IMPFUNG:
                    return DateUtil.addDaysDate(this.getDateOfSlot(otherSelected), this.terminUtilService.getMaxDiff());

            }

        }
        return undefined;
    }

    /**
     * gibt das Tagesdatum mit Zeit 00:00 des anderen Termins zurueck
     *
     * @param otherSelected der Termin dessen startdatum ermittelt wird
     */
    private getDateOfSlot(otherSelected: ImpfslotJaxTS): Date {
        return moment(otherSelected.zeitfenster?.von).startOf('day').toDate();
    }

    /**
     * prueft ob es sich beim Fehler um den IMPFTERMIN_BESETZT validation err handelt
     */
    public isErrorTerminGebuchtVallidation(error: any): boolean {
        if (error && error.error && error.error.violations && Array.isArray(error.error.violations)
            && error.error.violations.length !== 0
            && error.error.violations[0].key === 'AppValidationMessage.IMPFTERMIN_BESETZT') {
            return true;
        }
        return false;
    }

    public resetData(): void {
        this._dashboard = {};
        this._ortDerImpfung = undefined;
        this._selectedSlot1 = undefined;
        this._selectedSlot2 = undefined;
        this._selectedSlotN = undefined;
    }

    public getImpffolgenToBook(): ImpffolgeTS[] {
        if (this.isAlreadyGrundimmunisiert()) {
            return [ImpffolgeTS.BOOSTER_IMPFUNG];
        }
        return [ImpffolgeTS.ERSTE_IMPFUNG, ImpffolgeTS.ZWEITE_IMPFUNG];
    }

    public isAlreadyGrundimmunisiert(): boolean {
        return isAnyStatusOfBooster(this.dashboard?.status);
    }

    public hasAllRequiredSlotsChosen(): boolean {
        if (this.isAlreadyGrundimmunisiert()) {
            // Nur der Booster-Termin ist zwingend
            return !!this.selectedSlotN;
        } else {
            // Fuer die Grundimmunisierung braucht es zwei Termine
            return !!this.selectedSlot1 && !!this.selectedSlot2;
        }
    }

    public hasSelectedSlot2WithCorrectAbstand(): boolean {
        return !!this.selectedSlot2 &&
            this.terminUtilService.isCorrectAbstandZweiterTerminMinMaxAndSameOdi(
                this.selectedSlot1,
                this.selectedSlot2,
                this.dashboard.status);
    }

    public getNextTerminLabel(
        odi: OrtDerImpfungDisplayNameExtendedJaxTS,
        status: RegistrierungStatusTS | undefined,
    ): string {
        let terminLabel = '';
        const terminPrefix = this.translationService.instant('OVERVIEW.NEXT_TERMIN_AM');
        // Grundimmunisiert -> booster termin.
        if (this.isAlreadyGrundimmunisiert()) {
            if (!odi.noFreieTermineN && odi.nextTerminNDate) {
                terminLabel +=
                    ` (${terminPrefix} ${moment(odi.nextTerminNDate).format(DateUtil.dateFormatShort())})`;
            }
        } // Erste impfung durch -> naechster termin2
        else if (isErsteImpfungDoneAndZweitePending(status)) {
            if (!odi.noFreieTermine2 && odi.nextTermin2Date) {
                terminLabel +=
                    ` (${terminPrefix} ${moment(odi.nextTermin2Date).format(DateUtil.dateFormatShort())})`;
            }
        } // Sonst -> naechster termin1
        else {
            if (!odi.noFreieTermine1 && odi.nextTermin1Date) {
                terminLabel +=
                    ` (${terminPrefix} ${moment(odi.nextTermin1Date).format(DateUtil.dateFormatShort())})`;
            }
        }

        terminLabel += ` ${this.getDistanceAsString(odi.distanceToReg)}`;

        return terminLabel;
    }

    private getDistanceAsString(distanceInM: number | undefined): string {
        if (distanceInM) {
            const distanceInKm = distanceInM / 1000;
            if (distanceInKm < MAX_ODI_DISTANCE) { // cutoff if outside of switzerland
                const fractionDigits = (distanceInKm > 1) ? 0 : 2;
                return `(${distanceInKm.toFixed(fractionDigits).toString()} km)`;
            }
        }
        return '';
    }

    public hasChangedKrankheit(dashboardJax: DashboardJaxTS): boolean {
        const oldKrankheit = this._dashboard?.krankheitIdentifier;
        const newKrankheit = dashboardJax.krankheitIdentifier;
        return oldKrankheit !== newKrankheit;
    }

    public hasChangedDossiernummer(dashboardJax: DashboardJaxTS): boolean {
        const oldDossierNr = this._dashboard?.registrierungsnummer;
        const newDossierNr = dashboardJax.registrierungsnummer;
        return oldDossierNr !== newDossierNr;
    }

    /**
     * Sets the dashboard into the service for easy access from different components
     * In case we already have a stored dashboard stored in the service and the the dashboardJax that we
     * want to set is for a different krankheit we need to reset the selected slots because the user probably changed
     * the dossier
     *
     * @param dashboardJax
     */
    public setDashboardAndResetDataIfKrankheitChanged(dashboardJax: DashboardJaxTS): void {
        if (this.hasChangedKrankheit(dashboardJax)) {
            this.resetData();
        }
        this._dashboard = dashboardJax;
    }
}
