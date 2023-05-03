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
import * as moment from 'moment';
import DateUtil from '../../../util/DateUtil';

@Component({
    selector: 'lib-date-spinner',
    templateUrl: './date-spinner.component.html',
    styleUrls: ['./date-spinner.component.scss']
})
export class DateSpinnerComponent implements OnInit {

    @Input() public datum!: Date;
    @Input() public showBack!: boolean;
    @Input() public showForward!: boolean;

    @Output() public changedDatum = new EventEmitter<Date>();

    constructor(
        public translateService: TranslateService) {
    }

    ngOnInit(): void {
    }

    public spin(days: number): void {
        this.datum = this.spinDate(days);
        this.changedDatum.emit(this.datum);
    }

    public spinDate(days: number): Date {
        return DateUtil.addDays(moment(this.datum), days).toDate();
    }

}
