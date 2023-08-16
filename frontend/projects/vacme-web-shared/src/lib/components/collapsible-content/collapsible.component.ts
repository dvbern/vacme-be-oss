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

import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, Input} from '@angular/core';

@Component({
    selector: 'lib-collapsible-content',
    templateUrl: './collapsible.component.html',
    styleUrls: ['./collapsible.component.scss'],
    animations: [
        trigger('contentExpansion', [
            state('expanded', style({height: '*', opacity: 1, visibility: 'visible'})),
            state('collapsed', style({height: '0px', opacity: 0, visibility: 'hidden'})),
            transition('expanded <=> collapsed',
                animate('300ms cubic-bezier(.37,1.04,.68,.98)')),
        ])
    ]
})
export class CollapsibleComponent {

    @Input()
    public opened = false;

}
