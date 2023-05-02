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
    selector: 'lib-input-text',
    templateUrl: 'input-text.component.html',
    styleUrls: ['input-text.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            multi: true,
            useExisting: forwardRef(() => InputTextComponent),
        },
    ],
    changeDetection: ChangeDetectionStrategy.Default, // OnPush cannot detect "touched" programmatically
})
export class InputTextComponent extends FormControlBase {

    @Input()
    key = '';
    @Input()
    translateParams = {};
    @Input()
    type = 'text';
    @Input()
    required = false;
    @Input()
    placeholder?: string;
    @Input()
    autocomplete ? = true;
    @Input()
    trim ? = false; // trim: trims the text field before submitting

    @Input()
    noticeText?: string;

    constructor(
        fg: FormGroupDirective,
        @Optional() fgn?: FormGroupName,
    ) {
        super(() => {
            return this.required;
        }, fg, fgn);
    }
}
