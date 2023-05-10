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

import {ChangeDetectionStrategy, Component, forwardRef, Input, Optional} from '@angular/core';
import {FormGroupDirective, FormGroupName, NG_VALUE_ACCESSOR} from '@angular/forms';
import {FormControlBase} from '../abstract-form-control';

@Component({
    selector: 'lib-input-textarea',
    templateUrl: './input-textarea.component.html',
    styleUrls: ['./input-textarea.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            multi: true,
            useExisting: forwardRef(() => InputTextareaComponent),
        },
    ],
    changeDetection: ChangeDetectionStrategy.Default, // OnPush cannot detect "touched" programmatically
})
export class InputTextareaComponent extends FormControlBase {

    @Input()
    key = '';
    @Input()
    required = false;
    @Input()
    noTranslate = false;
    @Input()
    maxlength: null | number | string = null;

    constructor(
        fg: FormGroupDirective,
        @Optional() fgn?: FormGroupName,
    ) {
        super(() => {
            return this.required;
        }, fg, fgn);
    }
}
