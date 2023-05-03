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

@Component({
    selector: 'lib-warningbox',
    templateUrl: './warningbox.component.html',
    styleUrls: ['./warningbox.component.scss']
})
export class WarningboxComponent implements OnInit {

    @Input() critical = false;
    @Input() success = false;

    constructor() {
    }

    ngOnInit(): void {
    }

    getIconSrc(): string {
        if (this.success) {
            return 'img/Success Icon.svg';
        }
        if (this.critical) {
            return 'img/Explanation Mark white.svg';
        }
        return 'img/Explanation Mark white.svg';
    }

    showStyleguideAlert(): boolean {
        // see styleguide: http://design.onda01.snowflakehosting.ch/AtomicKitchen/styleguides/1/Kanton-Bern/#M39
        return !this.success;
    }

}
