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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import moment from 'moment';
import {ImpffolgeTS, ImpfslotJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import {LogFactory} from '../../../logging';
import DateUtil from '../../../util/DateUtil';

const LOG = LogFactory.createLog('TerminfindungComponent');

@Component({
    selector: 'lib-terminfindung',
    templateUrl: './terminfindung.component.html',
    styleUrls: ['./terminfindung.component.scss'],
})
export class TerminfindungComponent implements OnInit {

    @Input() public freieSlots!: Array<ImpfslotJaxTS>;
    @Input() public impffolge!: ImpffolgeTS;
    @Input() public selectedImpfslot: ImpfslotJaxTS | undefined;
    @Input() public datum!: Date;
    @Input() public minDateRestriction: Date | undefined;
    @Input() public maxDateRestriction: Date | undefined;
    @Input() public krankheitIdentifier: KrankheitIdentifierTS | undefined;

    @Output() public changedImpfslot = new EventEmitter<ImpfslotJaxTS>();
    @Output() public changedDatum = new EventEmitter<Date>();


    constructor(
        public translateService: TranslateService
    ) {
    }

    ngOnInit(): void {
        if (!this.freieSlots) {
            LOG.error('Keine freien Slots uebergeben. Setze Attribut [freieSlots]');
        }
        if (!this.impffolge) {
            LOG.error('Keine Impffolge uebergeben. Setze Attribut [impffolge]');
        }
        if (!this.datum) {
            LOG.error('Kein Startdatum uebergeben. Setze Attribut [datum]');
        }
    }

    public chooseSlot(slot: ImpfslotJaxTS): void {
        this.changedImpfslot.emit(slot);
    }

    public getSelectedId(): string | undefined {
        return this.selectedImpfslot?.id;
    }

    public spinToDate(newDatum: Date): void {
        this.datum = newDatum;
        this.changedDatum.emit(this.datum);
    }

    public spinBackHidden(): boolean {
        const isPast = moment(this.datum).startOf('day')
            .isSameOrBefore(DateUtil.today().add( 1, 'day'));
        let isAtTerminMinBasedOnOther = false;
        const terminLimitMinBasedOnOtherTermin = this.minDateRestriction;

        if (terminLimitMinBasedOnOtherTermin ) {
            isAtTerminMinBasedOnOther = moment(this.datum).startOf('day').isSameOrBefore(terminLimitMinBasedOnOtherTermin);
        }
        return isPast || isAtTerminMinBasedOnOther;
    }

    public spinFwdHidden(): boolean {
        const terminLimitMaxBasedOnOtherTermin = this.maxDateRestriction;

        let isAtTerminMax = false;
        if (terminLimitMaxBasedOnOtherTermin ) {
            isAtTerminMax = moment(this.datum).startOf('day').isSameOrAfter(terminLimitMaxBasedOnOtherTermin);
        }
        return isAtTerminMax;
    }
}
