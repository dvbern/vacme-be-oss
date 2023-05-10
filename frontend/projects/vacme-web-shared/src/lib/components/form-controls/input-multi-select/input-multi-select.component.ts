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

import {
    ChangeDetectionStrategy,
    Component,
    forwardRef,
    Input,
    OnChanges,
    Optional,
    SimpleChanges,
} from '@angular/core';
import {FormGroupDirective, FormGroupName, NG_VALUE_ACCESSOR} from '@angular/forms';
import {FormControlBase} from '../abstract-form-control';
import EnumUtil from '../../../util/EnumUtil';
import {Option} from '../input-select/option';


@Component({
    selector: 'lib-input-multi-select',
    templateUrl: './input-multi-select.component.html',
    styleUrls: ['./input-multi-select.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            multi: true,
            useExisting: forwardRef(() => InputMultiSelectComponent),
        },
    ],
    changeDetection: ChangeDetectionStrategy.Default, // OnPush cannot detect "touched" programmatically
})
export class InputMultiSelectComponent extends FormControlBase implements OnChanges {
    @Input()
    key!: string;

    @Input()
    options!: Array<Option>;

    @Input()
    doSortOptions = true;

    optionsSorted!: Array<Option>;

    @Input()
    translationPrefix?: string;

    @Input()
    noTranslate = false;

    @Input()
    undefinedLabelKey?: string;

    required = false;

    @Input()
    addOptgroup = false;

    constructor(
        fg: FormGroupDirective,
        @Optional() fgn?: FormGroupName,
    ) {
        super(() => {
            return this.required;
        }, fg, fgn);
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.reorderOptionList(changes);
    }

    /**
     * Reorder the options when changes occur
     *
     * @param changes no-doc
     */
    private reorderOptionList(changes: SimpleChanges): void {
        const optionChange = changes.options;

        if (optionChange === undefined) {
            return; // if this change is not the options list then do nothing
        }

        const optionList = optionChange.currentValue;

        if (optionList !== undefined && Array.isArray(optionList)) {
            if (this.doSortOptions) {
                this.optionsSorted = optionList.sort(EnumUtil.defaultEnumSorter);
            } else {
                this.optionsSorted = optionList;
            }
        }
    }

    public getSelectId(): string {
        return 'mul.sel.' + this.controlName;
    }
}
