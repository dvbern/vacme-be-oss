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

import {Component, Input} from '@angular/core';
import moment from 'moment';
import {KrankheitIdentifierTS, PrioritaetTS} from 'vacme-web-generated';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';

@Component({
    selector: 'app-code',
    templateUrl: './code.component.html',
    styleUrls: ['./code.component.scss'],
})
export class CodeComponent {

    @Input() public registrierungsnummer: string | undefined;
    @Input() public name: string | undefined;
    @Input() public vorname: string | undefined;
    @Input() public geburtsdatum: Date | undefined;
    @Input() public prioritaet: PrioritaetTS | undefined;
    @Input() public krankheit: KrankheitIdentifierTS | undefined;
    @Input() public hideQrCode!: boolean;


    getGeburtsdatum(): string {
        return moment(this.geburtsdatum).format(DateUtil.dateFormatShort());
    }

    public allPropertiesLoaded(): boolean {
        return !!this.registrierungsnummer && !!this.name && !!this.vorname && !!this.geburtsdatum && !!this.prioritaet;
    }
}
