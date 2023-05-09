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

import {ChangeDetectionStrategy, Component, EventEmitter, forwardRef, Input, Optional, Output} from '@angular/core';
import {FormGroupDirective, FormGroupName, NG_VALUE_ACCESSOR} from '@angular/forms';

import {FormControlBase} from '../abstract-form-control';
import {Option} from '../input-select/option';

@Component({
    selector: 'lib-input-radio',
    templateUrl: './input-radio.component.html',
    styleUrls: ['./input-radio.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            multi: true,
            useExisting: forwardRef(() => InputRadioComponent),
        },
    ],
    changeDetection: ChangeDetectionStrategy.Default, // OnPush cannot detect "touched" programmatically
})
export class InputRadioComponent extends FormControlBase {

    @Input()
    required = false;

    @Input()
    key = '';

    @Input()
    showGroupLabel = true;

    @Input()
    options!: Option[];

    @Output()
    selected: EventEmitter<any> = new EventEmitter();

    @Input()
    translationPrefix?: string;
    @Input()
    noTranslate = false;

    @Input()
    selectedValue: any;

    @Input()
    columnDisplay = false;

    @Input()
    public translateParams: any = {};

    constructor(
        fg: FormGroupDirective,
        @Optional() fgn?: FormGroupName,
    ) {
        super(() => {
            return this.required;
        }, fg, fgn);
    }

    public select(option: any): void {
        this.formControl.setValue(option.value);
        this.selected.emit(new Event('change'));
    }

    blur(): void {
        this.formControl.markAsTouched();
    }
}
