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
import {ValidationErrors} from '@angular/forms';
import {
    DATE_PATTERN, DATE_TIME_PATTERN
} from '../../../constants';

@Component({
    selector: 'lib-validationmessages',
    templateUrl: './validationmessages.component.html',
    styleUrls: ['./validationmessages.component.scss'],
})
export class ValidationmessagesComponent implements OnInit {

    @Input()
    public errors!: ValidationErrors | null;
    @Input()
    public myid!: string;

    constructor() {
    }

    ngOnInit(): void {
    }

    public getErrorKeys(): string[] {
        if (!this.errors) {
            return [];
        }
        if (this.errors && this.errors.pattern && this.errors.pattern.requiredPattern === DATE_PATTERN) {
            return [ 'DATE' ];
        }
        if (this.errors && this.errors.pattern && this.errors.pattern.requiredPattern === DATE_TIME_PATTERN) {
            return [ 'DATETIME' ];
        }
        return Object.keys(this.errors);
    }
}
