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
import {TranslateService} from '@ngx-translate/core';
import {ExternGeimpftJaxTS} from 'vacme-web-generated';
import DateUtil from '../../../util/DateUtil';

@Component({
    selector: 'lib-extern-geimpft-info-component',
    templateUrl: './extern-geimpft-info-component.component.html',
    styleUrls: ['./extern-geimpft-info-component.component.scss']
})
export class ExternGeimpftInfoComponentComponent implements OnInit {

    @Input() externGeimpft!: ExternGeimpftJaxTS;

    public dateUtil = DateUtil;

    constructor(public translateService: TranslateService) {
    }

    ngOnInit(): void {
    }

}
