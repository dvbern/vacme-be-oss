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
import {AmpelColorTS} from 'vacme-web-generated';
import {FormControlBase} from '../abstract-form-control';

/**
 * Ampel fuers callcenter
 */
@Component({
    selector: 'lib-form-control-ampel',
    templateUrl: './form-control-ampel.component.html',
    styleUrls: ['./form-control-ampel.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            multi: true,
            useExisting: forwardRef(() => FormControlAmpelComponent),
        },
    ],
    changeDetection: ChangeDetectionStrategy.Default, // OnPush cannot detect "touched" programmatically
})
export class FormControlAmpelComponent extends FormControlBase {

    @Input() required = false;

    color!: AmpelColorTS;

    @Input()
    options: { label: string; value: any }[] = [
        {label: 'ROT', value: AmpelColorTS.RED},
        {label: 'ORANGE', value: AmpelColorTS.ORANGE},
        {label: 'GRUEN', value: AmpelColorTS.GREEN},
    ];

    @Input()
    translationPrefix = 'AMPEL';

    @Input()
    noTranslate = false;

    @Input()
    noTranslateLabel = false;

    @Input()
    labelKey = '';

    @Input()
    showGroupLabel = false;

    constructor(
        fg: FormGroupDirective,
        @Optional() fgn?: FormGroupName,
    ) {
        super(() => {
            return this.required;
        }, fg, fgn);
    }
}
