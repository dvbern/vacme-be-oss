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

import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'lib-button-navigate',
    templateUrl: './button-navigate.component.html',
    styleUrls: ['./button-navigate.component.css']
})
export class ButtonNavigateComponent implements OnInit {

    @Input()
    disabled?: boolean;

    @Input()
    buttonKind?: 'primary' | 'secondary' | 'go-back' | 'go-next' | string;

    @Input()
    goto!: string;
    /* It needs to have a different name than "routerLink" because otherwise, <lib-button-navigate> and <button> have a
     routerLink and then both elements are focusable (and receive an ugly outline).
     */

    @Input()
    translationKey?: string | undefined;

    constructor() {
    }

    ngOnInit(): void {
        if (!this.goto) {
            throw Error('A navigation button needs to have a "goto". Avoid using (click) here!');
        }
    }

    getButtonKindClass(): string {
        switch (this.buttonKind) {
            case 'primary':
            case 'secondary':
            case 'go-back':
            case 'go-next':
                return this.buttonKind;

            default:
                return 'secondary';
        }
    }

    getArrowSrc(): string | undefined {
        switch (this.buttonKind) {
            case 'go-next':
                return 'img/right-arrow-white.svg';
            case 'go-back':
                return this.disabled ? 'img/go-back-deactive.svg' : 'img/go-back.svg';
            case 'primary':
            case 'secondary':
            default:
                return undefined;
        }
    }

}

