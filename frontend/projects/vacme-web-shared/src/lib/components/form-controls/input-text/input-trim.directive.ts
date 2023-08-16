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

import {Directive, Input} from '@angular/core';
import {ControlValueAccessor, NgControl} from '@angular/forms';

@Directive({
    selector: '[libInputTrim]'
})
export class InputTrimDirective {

    @Input() trim?: boolean;

    constructor(private ngControl: NgControl) {
        trimValueAccessor(ngControl.valueAccessor, this);
    }

}

function trimValueAccessor(valueAccessor: ControlValueAccessor | null, trimDirective: InputTrimDirective): void {
    if (!valueAccessor) {
        return;
    }

    // eslint-disable-next-line @typescript-eslint/unbound-method
    const original = valueAccessor.registerOnChange;

    valueAccessor.registerOnChange = (fn: (_: unknown) => void): void => {
        return original.call(valueAccessor, (value: unknown) => {
            const betterValue = (typeof value === 'string' && trimDirective.trim)
                ? value.trim()
                : value;
            return fn(betterValue);
        });
    };
}
