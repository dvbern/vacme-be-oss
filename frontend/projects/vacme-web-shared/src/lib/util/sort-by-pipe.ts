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

import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'sortBy'})
export class SortByPipe implements PipeTransform {

    private compareFn = (a: any, b: any, field: any): -1 | 1 | 0 => {
        if (a[field].toLowerCase() < b[field].toLowerCase()) {
            return -1;
        } else if (a[field].toLowerCase() > b[field].toLowerCase()) {
            return 1;
        } else {
            return 0;
        }
    };

    transform(value: any[], order = '', column: string = ''): any[] {
        if (!value || order === '' || !order) {
            return value;
        } // no array
        if (value.length <= 1) {
            return value;
        } // array with only one item
        if (!column || column === '') {
            if (order === 'asc') {
                return value.sort();
            } else {
                return value.sort().reverse();
            }
        } // sort 1d array
        else {
            if (order === 'asc') {
                return value.sort((a, b) => this.compareFn(a, b, column));
            } else {
                return value.sort((a, b) => this.compareFn(a, b, column)).reverse();
            }
        }
    }
}
