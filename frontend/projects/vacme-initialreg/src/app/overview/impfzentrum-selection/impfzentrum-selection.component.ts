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
import {DashboardJaxTS, OrtDerImpfungDisplayNameJaxTS} from 'vacme-web-generated';

@Component({
    selector: 'app-impfzentrum-selection',
    templateUrl: './impfzentrum-selection.component.html',
    styleUrls: ['./impfzentrum-selection.component.scss']
})
export class ImpfzentrumSelectionComponent implements OnInit {

    @Input() public dashboard!: DashboardJaxTS;

    @Input() public ortDerImpfungList?: OrtDerImpfungDisplayNameJaxTS[];

    @Input() public ortDerImfpung?: OrtDerImpfungDisplayNameJaxTS;
    @Output() public ortDerImfpungOutput = new EventEmitter<OrtDerImpfungDisplayNameJaxTS>();

    constructor() {
    }

    ngOnInit(): void {
    }

    public chooseItem(event: any): void {
        const item = event.target.value;
        const value = item ? (this.ortDerImpfungList?.find(each => each.name === item)) : null;
        this.chooseOrtDerImpfung(value as OrtDerImpfungDisplayNameJaxTS);
    }

    public chooseOrtDerImpfung(ort: OrtDerImpfungDisplayNameJaxTS): void {
        this.ortDerImfpung = ort;
        this.ortDerImfpungOutput.emit(ort);
    }

}
