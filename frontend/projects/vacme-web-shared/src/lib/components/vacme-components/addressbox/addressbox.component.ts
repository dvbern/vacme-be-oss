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
import {AdresseJaxTS} from 'vacme-web-generated';

@Component({
    selector: 'lib-addressbox',
    templateUrl: './addressbox.component.html',
    styleUrls: ['./addressbox.component.scss'],
})
export class AddressboxComponent implements OnInit {

    @Input()
    adresse?: AdresseJaxTS;

    @Input()
    anschrift?: string;

    constructor() {
    }

    ngOnInit(): void {
    }

}
