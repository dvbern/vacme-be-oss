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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {
    DashboardJaxTS,
    ImpffolgeTS,
    KrankheitIdentifierTS,
    NextFreierTerminJaxTS,
    OrtDerImpfungBuchungJaxTS,
    RegistrierungStatusTS,
} from 'vacme-web-generated';
import {
    ImpfdokumentationCacheService,
} from '../../../../../../vacme-web/src/app/service/impfdokumentation.cache.service';
import {ErrorMessageService} from '../../../service/error-message.service';
import {TerminUtilService} from '../../../service/termin-util.service';
import {TerminfindungService} from '../../../service/terminfindung.service';
import {VacmeSettingsService} from '../../../service/vacme-settings.service';
import DateUtil from '../../../util/DateUtil';
import {
    isAtLeastGebuchtOrOdiGewaehlt,
    isAtLeastOnceGeimpft,
    isErsteImpfungDoneAndZweitePending,
} from '../../../util/registrierung-status-utils';

interface OdiOption {
    value?: string;
    label?: string;
    disabled: boolean;
}

export interface NextFreierTerminSearch {
    impffolge: ImpffolgeTS;
    ortDerImpfungId: string;
    nextDateParam: NextFreierTerminJaxTS | undefined;
    freigegebenAb: Date | undefined;
    registrierungsnummer: string;
    otherTerminDate: Date | undefined;
}

@Component({
    selector: 'lib-termine-bearbeiten',
    templateUrl: './termine-bearbeiten.component.html',
    styleUrls: ['./termine-bearbeiten.component.scss']
})
export class TermineBearbeitenComponent implements OnInit, OnChanges {

    @Input()
    public dashboardJax!: DashboardJaxTS;

    @Input()
    public set ortDerImpfungList(odiList: OrtDerImpfungBuchungJaxTS[]) {
        this._ortDerImpfungList = odiList;
        this.odiOptions = this.getOdiOptions();
    }

    public get ortDerImpfungList(): OrtDerImpfungBuchungJaxTS[] {
        return this._ortDerImpfungList;
    }

    private _ortDerImpfungList!: OrtDerImpfungBuchungJaxTS[];

    @Input()
    public selectedOdiId: string | undefined;

    @Input()
    public mussAtLeastGebuchtSein = true; // wird in Fachapp auf false gesetzt, aber in Portal bleibt es true

    @Input()
    public mussAtLeastFreigegebenSein = true; // wird in Fachapp auf false gesetzt, aber in Portal bleibt es true

    @Input()
    public erstTerminAdHoc = false;

    @Output()
    public goBack = new EventEmitter<string>();

    @Output()
    public cancelAppointment = new EventEmitter<string>();

    @Output()
    public updateAppointment = new EventEmitter<string>();

    @Output()
    public nextFreieTermin = new EventEmitter<NextFreierTerminSearch>();

    public odiOptions: Array<OdiOption> = [];

    private initialSelectedSlot1: string | undefined;
    private initialSelectedSlot2: string | undefined;
    private initialSelectedSlotN: string | undefined;

    constructor(
        public terminfindungService: TerminfindungService,
        private terminUtilService: TerminUtilService,
        public impfdokumentationCacheService: ImpfdokumentationCacheService,
        private translationService: TranslateService,
        private vacmeSettings: VacmeSettingsService,
        private errorMessageService: ErrorMessageService
    ) {
    }

    ngOnInit(): void {
        if (this.hasInvalidStatus()) {
            this.errorMessageService.addMesageAsError('ERROR_NON_BOOKABLE_STATUS');
            this.doGoBack();
        }
    }

    private hasInvalidStatus(): boolean {
        // mussAtLeastGebuchtSein? Portal: Ja (neu buchen geht nicht ueber diese Component). FachApp: Nein.
        if (this.mussAtLeastGebuchtSein) {
            if (!isAtLeastGebuchtOrOdiGewaehlt(this.dashboardJax.status)) {
                return true;
            }
        }

        // mussAtLeastFreigegebenSein? Portal: Ja, Fachapp: Nein
        // in der Fachapp kann man NEU auch buchen/umbuchen, wenn man noch nicht freigegeben/freigegeben_booster ist
        // Grund: im bisherigen Code war "Termin heute erstellen" moeglich und anschliessen konnte man umbuchen.
        if (this.mussAtLeastFreigegebenSein) {
            if (!!this.dashboardJax.status
                && [RegistrierungStatusTS.REGISTRIERT, RegistrierungStatusTS.IMMUNISIERT]
                    .includes(this.dashboardJax.status)) {
                return true;
            }
        }

        // abgeschlossen (und noch nicht mal immunisiert)
        const abgeschlossen = !!this.dashboardJax.status
            && [RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT, RegistrierungStatusTS.ABGESCHLOSSEN]
                .includes(this.dashboardJax.status);
        if (abgeschlossen) {
            return true;
        }

        return false;
    }

    ngOnChanges(): void {
        this.initialSelectedSlot1 = this.dashboardJax.termin1?.impfslot?.id;
        this.initialSelectedSlot2 = this.dashboardJax.termin2?.impfslot?.id;
        this.initialSelectedSlotN = this.dashboardJax.terminNPending?.impfslot?.id;
    }

    public isImpffolgeBooster(): boolean {
        return !!this.dashboardJax.vollstaendigerImpfschutz;
    }

    public getOrtDerImpfung(): OrtDerImpfungBuchungJaxTS | undefined {
        return this.terminfindungService.ortDerImpfung;
    }

    public getOrtDerImpfungId(): string {
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        return this.getOrtDerImpfung() ? this.getOrtDerImpfung()!.id! : '';
    }

    public setOrtDerImpfung(ortDerImpfung: OrtDerImpfungBuchungJaxTS | undefined): void {
        this.terminfindungService.ortDerImpfung = ortDerImpfung;
    }

    public canSeeTermine(): boolean {
        return isAtLeastGebuchtOrOdiGewaehlt(this.terminfindungService.dashboard?.status)
            && !!this.getOrtDerImpfung();
    }

    public warnDates(): boolean {
        return !this.isAlreadyGrundimmunisiert() && this.invalidDateDiff();
    }

    public invalidDateDiff(): boolean {
        if (this.isAlreadyGrundimmunisiert()) {
            // Fuer Booster gibt es keinen Abstand
            return false;
        }
        if (this.canSeeTermine()
            && this.hasAllRequiredSlotsChosen()) {
            const dateDff = this.getDaysDiff();
            return !!dateDff
                && !(dateDff <= this.terminUtilService.getMaxDiff()
                    && dateDff >= this.terminUtilService.getMinDiff());
        }
        return false;
    }

    public getDaysDiffAbs(): number | undefined {
        const diff = this.getDaysDiff();
        if (diff !== undefined) {
            return Math.abs(diff);
        } else {
            return undefined;
        }
    }

    public getDaysDiff(): number | undefined {
        if (this.terminfindungService.selectedSlot1 && this.terminfindungService.selectedSlot2) {
            return this.terminUtilService.getDaysDiff(
                // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                this.terminfindungService.selectedSlot2,
                // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                this.terminfindungService.selectedSlot1);
        } else {
            return undefined;
        }
    }

    public getImpffolgen(): ImpffolgeTS[] {
        return this.terminfindungService.getImpffolgenToBook();
    }

    public getOdiOptions(): Array<OdiOption> {
        const freieTermineOptions: Array<OdiOption> = [];
        const noFreieTermineOptions: Array<OdiOption> = [];
        this.getOrtDerImpfungListOptions().forEach(odi => {
            let odiLabel = odi.name;
            if (odi.terminverwaltung && this.terminUtilService.hasOdiNoFreieTermineForMe(this.dashboardJax, odi)) {
                odiLabel += ' ' + this.translationService.instant('OVERVIEW.NO_TERMIN');
                noFreieTermineOptions.push(
                    {label: odiLabel, value: odi.id, disabled: true}
                );
            } else if (odi.deaktiviert) {
                odiLabel += ' ' + this.translationService.instant('OVERVIEW.ODI_INAKTIV');
                noFreieTermineOptions.push(
                    {label: odiLabel, value: odi.id, disabled: true}
                );
            } else {
                const terminLabel = this.terminfindungService.getNextTerminLabel(odi, this.dashboardJax.status);
                odiLabel += terminLabel;
                freieTermineOptions.push(
                    {label: odiLabel, value: odi.id, disabled: false}
                );
            }
        });
        const options: Array<OdiOption> = [
            {label: this.translationService.instant('OVERVIEW.IMPFORT_LABEL'), value: undefined, disabled: false}
        ];
        options.push(...freieTermineOptions);
        options.push(...noFreieTermineOptions);
        return options;
    }

    public selectOrtDerImpfung(): void {
        // Wenn die Impfung schon durch ist, darf der Termin nicht deselektiert werden
        if (!isErsteImpfungDoneAndZweitePending(this.terminfindungService.dashboard?.status)) {
            this.terminfindungService.selectedSlot1 = undefined;
        }
        this.terminfindungService.selectedSlot2 = undefined;
        this.terminfindungService.selectedSlotN = undefined;
        const selectedOrt = this.findOrtById(this.selectedOdiId);
        this.setOrtDerImpfung(selectedOrt);
        // TODO Affenpocken for now Affenpocken can't do termine.
        this.impfdokumentationCacheService.cacheSelectedOdi(this.selectedOdiId, KrankheitIdentifierTS.COVID);
    }

    public canSave(): boolean {
        return this.hasAllRequiredSlotsChosen() && !!this.selectedOdiId;
    }

    public hasAllRequiredSlotsChosen(): boolean {
        if (this.getOrtDerImpfung()?.terminverwaltung === false) {
            return true; // Termin muss haendisch abgemacht werden, nicht ueber VacMe
        }
        return this.terminfindungService.hasAllRequiredSlotsChosen();
    }

    private findOrtById(ortId?: string): OrtDerImpfungBuchungJaxTS | undefined {
        if (!ortId) {
            return undefined;
        }
        for (const ort of this.ortDerImpfungList) {
            if (ort.id === ortId) {
                return ort;
            }

        }
        return undefined;
    }

    public getOrtDerImpfungListOptions(): OrtDerImpfungBuchungJaxTS[] {
        if (this.ortDerImpfungList) {
            const filtered = this.ortDerImpfungList.filter(value => value.terminverwaltung);

            filtered.sort((a, b) => {
                if (a.name && b.name) {
                    return a.name.toLocaleLowerCase().localeCompare(b.name.toLocaleLowerCase());
                }
                return 0;
            });
            return filtered;
        }
        return [];
    }

    getRegistrierungsnummer(): string {
        return this.dashboardJax.registrierungsnummer as string;
    }

    public isAlreadyGrundimmunisiert(): boolean {
        return this.terminfindungService.isAlreadyGrundimmunisiert();
    }

    getInfoTextDistanceBetweenImpfungen(): string {
        return this.vacmeSettings.getInfoTextDistanceBetweenImpfungen();
    }

    public doCancelAppointments(): void {
        this.cancelAppointment.emit(this.dashboardJax.registrierungsnummer);
    }

    isTerminReadOnly(impffolge: ImpffolgeTS): boolean {
        switch (impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return isErsteImpfungDoneAndZweitePending(this.terminfindungService.dashboard?.status);
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return this.isZweiteImpfungDurch()
                    || this.terminfindungService.selectedSlot1 === undefined
                    || this.isErstterminVerpasst();
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return false; // man kann neuerdings in der Fachapp immer buchen/umbuchen
        }
    }

    isZweiteImpfungDurch(): boolean {
        return !!this.terminfindungService.dashboard?.impfung2;
    }

    isErstterminVerpasst(): boolean {
        // wenn die erste Impfung noch nicht erfolgt ist und der 1. Termin in der Vergangenheit ist, hat man den 1.
        // Termin verpasst
        return !isErsteImpfungDoneAndZweitePending(this.terminfindungService.dashboard?.status)
            && !DateUtil.isSameOrAfterToday(this.terminfindungService.selectedSlot1?.zeitfenster?.von);

    }

    doUpdateAppointments(): void {
        this.updateAppointment.emit(this.dashboardJax.registrierungsnummer);
    }

    doGoBack(): void {
        this.goBack.emit(this.dashboardJax.registrierungsnummer);
    }

    noChanges(): boolean {
        const firstSlotNotChanged = this.erstTerminAdHoc
            ? false
            : this.initialSelectedSlot1 === this.terminfindungService.selectedSlot1?.id;
        const secondSlotNotChanged = this.initialSelectedSlot2 === this.terminfindungService.selectedSlot2?.id;
        const boosterSlotNotChanged = this.initialSelectedSlotN === this.terminfindungService.selectedSlotN?.id;
        return firstSlotNotChanged && secondSlotNotChanged && boosterSlotNotChanged;
    }

    hasAnyAppointments(): boolean {
        return !!this.getImpffolgen().find(impffolge => {
            switch (impffolge) {
                case ImpffolgeTS.ERSTE_IMPFUNG:
                    return !!this.initialSelectedSlot1;
                case ImpffolgeTS.ZWEITE_IMPFUNG:
                    return !!this.initialSelectedSlot2;
                case ImpffolgeTS.BOOSTER_IMPFUNG:
                    return !!this.initialSelectedSlotN;
                default:
                    throw new Error('invalid impffolge ' + impffolge);
            }
        });
    }

    showAnnullieren(): boolean {
        return this.hasAnyAppointments() && !this.erstTerminAdHoc;
    }

    isPendingSecondImpfung(): boolean {
        if (this.terminfindungService.isAlreadyGrundimmunisiert()) {
            return false;
        }
        return isAtLeastOnceGeimpft(this.terminfindungService.dashboard?.status);
    }

    isDelta(impffolge: ImpffolgeTS): boolean {
        switch (impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return !!this.terminfindungService.selectedSlot1?.id &&
                    this.initialSelectedSlot1 !== this.terminfindungService.selectedSlot1?.id;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return !!this.terminfindungService.selectedSlot2?.id &&
                    this.initialSelectedSlot2 !== this.terminfindungService.selectedSlot2?.id;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return !!this.terminfindungService.selectedSlotN?.id &&
                    this.initialSelectedSlotN !== this.terminfindungService.selectedSlotN?.id;
        }
    }

    doNextFreieTermin($event: any): void {
        this.nextFreieTermin.emit($event);
    }
}
