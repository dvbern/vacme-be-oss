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

import {formatDate} from '@angular/common';
import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {ImpffolgeTS, ImpfslotJaxTS, NextFreierTerminJaxTS, RegistrierungStatusTS} from 'vacme-web-generated';
import {TerminUtilService} from '../../../service/termin-util.service';
import {TerminfindungService} from '../../../service/terminfindung.service';
import DateUtil from '../../../util/DateUtil';
import {isAtLeastOnceGeimpft} from '../../../util/registrierung-status-utils';
import {NextFreierTerminSearch} from '../termine-bearbeiten/termine-bearbeiten.component';

@Component({
    selector: 'lib-termin-overview-item',
    templateUrl: './termin-overview-item.component.html',
    styleUrls: ['./termin-overview-item.component.scss'],
})
export class TerminOverviewItemComponent implements OnInit, OnChanges {

    @Input() public impffolge!: ImpffolgeTS;
    @Input() public ortDerImpfungId!: string;
    @Input() public registrierungsnummer!: string;
    @Input() public readonly = false;
    @Input() public disableIntervallCheck = false;
    @Input() public hasDeltaChanges = false;
    @Input() public erstTerminAdHoc = false;
    @Input() public freigegebenAb: Date | undefined;
    @Output() public nextFreieTermin = new EventEmitter<NextFreierTerminSearch>();

    constructor(
        public translateService: TranslateService,
        public terminfindungService: TerminfindungService,
        private terminUtilService: TerminUtilService
    ) {
    }

    ngOnInit(): void {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.impffolge === ImpffolgeTS.ERSTE_IMPFUNG && this.erstTerminAdHoc) {
            // Falls wir vorher noch kein ODI hatten, muss der AdHoc Termin jetzt noch erstellt werden
            if (this.terminfindungService.ortDerImpfung) {
                this.terminfindungService.selectedSlot1 = this.terminUtilService.createAdHocTermin(
                    this.translateService.instant('TERMINE.JETZT'),
                    this.terminfindungService.ortDerImpfung);
            }
        }
    }

    public getTermin(): ImpfslotJaxTS | undefined {
        switch (this.impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return this.terminfindungService.selectedSlot1;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return this.terminfindungService.selectedSlot2;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return this.terminfindungService.selectedSlotN;
        }
        return undefined;
    }

    public printDate(termin?: ImpfslotJaxTS): string {
        if (!termin) {
            return '';
        }
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        return formatDate(termin.zeitfenster!.von,
            DateUtil.dateFormatVeryLong(this.translateService.currentLang), this.translateService.currentLang);
    }

    /* eslint-disable @typescript-eslint/no-non-null-assertion */
    public printTime(termin?: ImpfslotJaxTS): string {
        if (!termin) {
            return '';
        }
        // Sobald wir einen exakten Termin haben, zeigen wir diesen an (inkl. Offset)
        if (!!termin.zeitfenster?.exactDisplay) {
            return termin.zeitfenster.exactDisplay;
        }
        // Ansonsten einfach den Zeitraum des Slots
        return termin.zeitfenster!.vonDisplay + ' - ' + termin.zeitfenster!.bisDisplay;
    }

    selectDate(): void {
        let otherTerminDate: Date | undefined;
        switch (this.impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                otherTerminDate = this.terminfindungService.selectedSlot2?.zeitfenster?.von;
                break;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                otherTerminDate = this.terminfindungService.selectedSlot1?.zeitfenster?.von;
                break;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                otherTerminDate = undefined;
                break;
        }
        let nextDateParam: NextFreierTerminJaxTS | undefined;
        // Falls die Termine bereits gebucht,
        // dann gilt die Intervallsetzung fuer Termin 1 nicht mehr, weil sonst durch Termin 2 geblockt
        if (this.disableIntervallCheck && this.impffolge === ImpffolgeTS.ERSTE_IMPFUNG) {
            otherTerminDate = undefined;
        }
        if (otherTerminDate !== undefined) {
            nextDateParam = {nextDate: otherTerminDate};
        }

        const selectedDate: NextFreierTerminSearch = {
            impffolge: this.impffolge,
            ortDerImpfungId: this.ortDerImpfungId,
            nextDateParam,
            freigegebenAb: this.freigegebenAb,
            registrierungsnummer: this.registrierungsnummer,
            otherTerminDate
        };
        this.nextFreieTermin.emit(selectedDate);
    }

    getDeltaText(): string {
        return `${this.translateService.instant('OVERVIEW.TERMIN_NOCH_NICHT_GEBUCHT')}`;
    }

    public isTerminForImpffolgeAdHoc(): boolean {
        return this.impffolge === ImpffolgeTS.ERSTE_IMPFUNG && this.erstTerminAdHoc;
    }

    alreadyGeimpftForImpffolge(): boolean {
        switch (this.impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return isAtLeastOnceGeimpft(this.terminfindungService.dashboard?.status);
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return !!this.terminfindungService.dashboard?.impfung2;
            case ImpffolgeTS.BOOSTER_IMPFUNG :
                // Im Fall von Booster nehmen wir an, dass er im Status IMMUNISIERT die n-te Impfung bekommen hat
                // aber die n+1 noch nicht gebucht hat
                return RegistrierungStatusTS.IMMUNISIERT === this.terminfindungService.dashboard?.status;
        }
    }

    inFuture(): boolean {
        switch (this.impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return DateUtil.isSameOrAfterToday(this.terminfindungService.selectedSlot1?.zeitfenster?.von);
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return DateUtil.isSameOrAfterToday(this.terminfindungService.selectedSlot2?.zeitfenster?.von);
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return DateUtil.isSameOrAfterToday(this.terminfindungService.selectedSlotN?.zeitfenster?.von);
        }
    }

    getStatusIcon(): string {

        if (this.alreadyGeimpftForImpffolge()) {
            return 'check-icon';
        } else {

            if (this.inFuture()) {
                return 'calender-icon';
            } else {
                return 'missed-icon';
            }
        }
    }

    getStatusText(): string {
        if (this.alreadyGeimpftForImpffolge()) {
            return 'OVERVIEW.DONE_TERMIN';
        } else {

            if (this.inFuture()) {
                return 'OVERVIEW.PLANNED_TERMIN';
            } else {
                return 'OVERVIEW.MISSED_TERMIN';
            }
        }
    }
}
