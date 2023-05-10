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

import {AfterViewInit, Directive, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ControlValueAccessor, FormControl, FormGroupDirective, FormGroupName} from '@angular/forms';
import {Empty} from '../../types';
import FormUtil from '../../util/FormUtil';

/**
 * Allow standard behavior for custom components that wrap standard inputs with formControl/formControlName attributes.
 * These might be HTML <input> elements or primeNG controls.
 *
 * To have custom controls use formControl/formControlName attributes, the ControlValueAccessor needs to be implemented.
 * BUT: since we're just wrapping other normal-behavior controls, our implementation is not really relevant as handling
 * is already implementd in that wrapped control.
 * This means that meaningful implementation of ControlValueAccessor is not necessary
 * since this is - like mentioned before - correctly done in the wrapped control.
 */
@Directive()
// eslint-disable-next-line @angular-eslint/directive-class-suffix
export abstract class FormControlBase implements OnInit, AfterViewInit, ControlValueAccessor {
    // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
    private _control!: FormControl;
    private readonly container: FormGroupDirective | FormGroupName | Empty;
    protected controlName: string | undefined = undefined;

    @Input() focusOnShow = false;

    @Output() blurred = new EventEmitter<FocusEvent>();

    /**
     * Please note: PrimeNG falsely handles boolean 'required' parameter: it just passes the param-value as a string
     * to the rendered input element.
     * This results int: <input required="false">
     *   But: the value "false" also marks the input as required since it is a true-if-present attribute as per HTML
     * spec!
     * => Pass the canonical 'required' attribute-value or undefined (which does not render the "required" attribute).
     */
    public requiredAttr: 'required' | undefined = undefined;

    protected constructor(
        private readonly requiredSupplier: () => boolean | Empty,
        fg: FormGroupDirective | Empty,
        fgn: FormGroupName | Empty
    ) {
        this.container = fgn || fg;
    }

    @Input()
    set formControl(value: FormControl) {
        this._control = value;

        // make debugging easier
        this.controlName = findControlName(this._control);
    }

    get formControl(): FormControl {
        return this._control;
    }

    @Input()
    set formControlName(name: string) {
        this.controlName = name;
    }

    public ngOnInit(): void {
        if (this.controlName && this.container) {
            this._control = this.container.control.controls[this.controlName] as FormControl;
        }
    }

    public ngAfterViewInit(): void {
        this.doAutofocus();
    }

    public doAutofocus(): void {
        if (this.focusOnShow) {
            window.setTimeout(() => {
                FormUtil.autofocusField();
            });
        }
    }

    /**
     * Intended to be overridden by subclasses since setting focus is highly control-specific.
     */
    setFocus(): void {
        throw new Error('Not yet implemented. Implement the setFocus() method in ' + (this as any).constructor.name);
    }

    public onBlur(blur: FocusEvent): void {
        // force update so the error display gets triggered
        if (this.formControl?.enabled) {
            this.formControl.setValue(this.formControl.value);
        }
        this.blurred.emit(blur);
    }

    /**
     * DO NOT USE, See class comment!
     */
    public registerOnChange(fn: any): void {
        // nop, see class comment!
    }

    /**
     * DO NOT USE, See class comment!
     */
    public registerOnTouched(fn: any): void {
        // nop, see class comment!
    }

    /**
     * DO NOT USE, See class comment!
     */
    public setDisabledState(isDisabled: boolean): void {
        // nop, see class comment!
    }

    /**
     * DO NOT USE, See class comment!
     */
    public writeValue(obj: any): void {
        // nop, see class comment!
    }

    public getValidationId(): string {
        return 'error.' + this.controlName;
    }

    public getLabelId(): string {
        return 'label.' + this.controlName;
    }

}

function findControlName(control: FormControl | Empty): string | undefined {
    if (!control) {
        return undefined;
    }

    const parent = control.parent;
    if (!parent) {
        return '';
    }

    for (const [name, ctl] of Object.entries(parent.controls)) {
        if (ctl === control) {
            return name;
        }
    }

    throw new Error('Should not get here: unknown control name?');
}

