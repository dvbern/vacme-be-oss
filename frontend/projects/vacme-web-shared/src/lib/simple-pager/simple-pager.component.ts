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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PagerEvent, PagerState} from './pager-enum';

@Component({
  selector: 'lib-simple-pager',
  templateUrl: './simple-pager.component.html',
  styleUrls: ['./simple-pager.component.scss']
})
export class SimplePagerComponent implements OnInit {
    @Input()
    pagerState: PagerState = PagerState.NONE;
    state = PagerState;

    @Input()
    disabled = false;

    @Output()
    pageEvent: EventEmitter<PagerEvent> = new EventEmitter<PagerEvent>();
    event = PagerEvent;
    constructor() { }

    ngOnInit(): void {
    }

    emitEvent(event: PagerEvent): void {
        if (!this.disabled) {
            this.pageEvent.emit(event);
        }
    }
}
