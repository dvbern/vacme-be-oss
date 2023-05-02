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

import {Component, Input, OnInit} from '@angular/core';
import * as moment from 'moment';
import {OrtDerImpfungDisplayNameJaxTS, RegistrierungTermineImpfungJaxTS} from 'vacme-web-generated';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';

@Component({
    selector: 'app-registrierung-termine-impfungen',
    templateUrl: './registrierung-termine-impfungen.component.html',
    styleUrls: ['./registrierung-termine-impfungen.component.scss']
})
export class RegistrierungTermineImpfungenComponent implements OnInit {

    @Input()
    itemList: Array<RegistrierungTermineImpfungJaxTS> | undefined;

    @Input()
    showImpfungen = true;

    @Input()
    showInfo = false;


    constructor() {
    }

    ngOnInit(): void {

    }

    public getItemListSorted(): Array<RegistrierungTermineImpfungJaxTS> {
        if (!this.itemList) {
            return [];
        }
        return this.itemList?.sort((a: RegistrierungTermineImpfungJaxTS, b: RegistrierungTermineImpfungJaxTS) => {
            // @ts-ignore
            return (moment(a.impfung1Datum)) - (moment(b.impfung1Datum));
        });
    }

    public toDate(aDate: Date | undefined): string {
        if (aDate) {
            const asString = DateUtil.momentToLocalDate(moment(aDate));
            return asString ? asString : '-';
        }
        return '-';
    }

    public toDateTime(aDate: Date | undefined): string {
        if (aDate) {
            const asString = DateUtil.momentToLocalDateTime(moment(aDate));
            return asString ? asString : '-';
        }
        return '-';
    }

    public getOdiName(odi: OrtDerImpfungDisplayNameJaxTS | undefined): string {
        if (odi && odi.name) {
            return odi.name;
        }
        return '-';
    }
}
