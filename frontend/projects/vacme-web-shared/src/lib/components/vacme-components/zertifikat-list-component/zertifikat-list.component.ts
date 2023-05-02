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
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import {ZertifikatJaxTS} from 'vacme-web-generated';
import DateUtil from '../../../util/DateUtil';

@Component({
    selector: 'lib-zertifikat-list',
    templateUrl: './zertifikat-list.component.html',
    styleUrls: ['./zertifikat-list.component.scss']
})
export class ZertifikatListComponent implements OnInit {

    @Input() zertifikatList?: ZertifikatJaxTS[];


    @Output() public triggerDownload = new EventEmitter<ZertifikatJaxTS>();

    constructor(
        private translationService: TranslateService) {
    }

    ngOnInit(): void {
    }

    public printDate(termin?: Date): string {
        if (!termin) {
            return '';
        }
        return formatDate(termin, DateUtil.dateFormatMedium(this.translationService.currentLang), this.translationService.currentLang);
    }

    public downloadSpecificZertifikat(zertifikat: ZertifikatJaxTS): void {
        this.triggerDownload.emit(zertifikat);
    }

}
