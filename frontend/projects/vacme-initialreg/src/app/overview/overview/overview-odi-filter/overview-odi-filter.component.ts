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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {UntypedFormGroup} from '@angular/forms';

@Component({
    selector: 'app-overview-odi-filter',
    templateUrl: './overview-odi-filter.component.html',
    styleUrls: ['./overview-odi-filter.component.scss'],
})
export class OverviewOdiFilterComponent implements OnInit, OnChanges {

    @Input() hideOdiFilters!: boolean;
    @Input() odiFilterFormGroup!: UntypedFormGroup;
    @Input() showDistanceFilter = false;

    @Output() resetOdiFilter: EventEmitter<void> = new EventEmitter<void>();
    @Output() filterOdis: EventEmitter<void> = new EventEmitter<void>();

    public options: { label: string; value: any }[] = [];


    createSortOptions(): void {
        this.options = [
            {label: 'IMPFTERMIN', value: 'TERMIN'},
            {label: 'IMPFORT', value: 'ALPHABETISCH'},
        ];
        // Distanz ist nur eine Option wenn enabled
        if (this.showDistanceFilter) {
            this.options.push({label: 'ENTFERNUNG', value: 'DISTANZ'});
        }
    }

    ngOnInit(): void {
        this.createSortOptions();
    }

    public ngOnChanges(_: SimpleChanges): void {
        // weil showDistanceFilter aendert, sobald alle ODIS und ihre Distanzen
        // geladen/berechnet sind
        this.createSortOptions();
    }
}
