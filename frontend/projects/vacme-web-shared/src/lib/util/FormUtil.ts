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

import {FormGroup} from '@angular/forms';
import {LogFactory} from '../logging';

const LOG = LogFactory.createLog('FormUtil');

export default class FormUtil {

    /* 1. .ng-invalid: find all elements with the class .ng-invalid
     2. [formcontrolname]: from those, take the ones that are actually on a control, not on a form/formgroup/formgroupname
     3. .focusable-field: inside those, find the element with the class focusable-field */
    public static readonly INVALID_INPUT_FIELDS_CSS_SELECTOR = '.ng-invalid[formcontrolname] .focusable-field';

    public static selectFirstInvalid(): void {
        // focusable-field innerhalb eines invaliden Feldes. Aber nicht einfach das erste Element im invaliden Form.

        FormUtil.focus(FormUtil.INVALID_INPUT_FIELDS_CSS_SELECTOR);
    }

    public static autofocusField(): void {
        FormUtil.focus('.autofocus .focusable-field');
    }
    public static focus(cssSelector: string): void {
        const invalidFields = document.querySelectorAll(cssSelector);
        const tmp: any = invalidFields[0];
        if (tmp) {
            tmp.focus();
        }
    }

    /**
     * Touches all fields of the formGroup in order to show the validation of each field, especially
     * untouched ones.
     * If the form is invalid, jump to the first invalid field.
     * Otherwise, continue with the specified method.
     * ifNotValidBlock is a callback function
     *
     * FormGroup has one of 4 possible status: valid | invalid | disabled | pending
     */
    public static doIfValid(formGroup: FormGroup, submitBlock: () => any, ifNotValidBlock?: () => any): void {
        formGroup.markAllAsTouched();
        if (formGroup.valid) {
            submitBlock();
        } else {
            LOG.info('Formular war nicht valid', formGroup);
            FormUtil.selectFirstInvalid();
            if (ifNotValidBlock) {
                ifNotValidBlock();
            }
        }
    }
}
