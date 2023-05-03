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
import {AmpelColorTS} from 'vacme-web-generated';

@Component({
    selector: 'lib-ampel',
    templateUrl: './ampel.component.html',
    styleUrls: ['./ampel.component.scss']
})
export class AmpelComponent implements OnInit {

    @Input() color!: AmpelColorTS;
    @Output() colorChange = new EventEmitter<AmpelColorTS>();

    options = [
        {name: 'ROT', value: AmpelColorTS.RED},
        {name: 'ORANGE', value: AmpelColorTS.ORANGE},
        {name: 'GRUEN', value: AmpelColorTS.GREEN},
    ];

    constructor() {
    }

    ngOnInit(): void {
    }

    changeColor(newColor: string): void {
        this.color = newColor as AmpelColorTS;
        this.colorChange.emit(this.color);
    }
}
