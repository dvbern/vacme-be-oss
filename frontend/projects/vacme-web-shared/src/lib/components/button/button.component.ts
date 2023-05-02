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

@Component({
    selector: 'lib-button',
    templateUrl: './button.component.html',
    styleUrls: ['./button.component.css']
})
export class ButtonComponent implements OnInit {

    @Input()
    disabled?: boolean;

    @Input()
    buttonKind?: 'submit' | 'primary' | 'secondary' | 'go-back' | 'go-next' | string;

    @Output()
    clickIfEnabled = new EventEmitter();
    /*
    Important: we don't use (click) itself because that will be added to the parent (<lib-button>) directly and
     would be executed even when the button is disabled. (clickIfEnabled) is only executed if the button is not
      disabled.
      We tried using (click) itself but when the button is disabled, we cannot catch the click and the parent click
       will be executed anyway, which is dangerous.
     */

    @Input()
    translationKey?: string | undefined;

    @Input()
    customClass = '';

    @Input()
    noMargin = false;

    constructor() {
    }

    ngOnInit(): void {
    }

    getButtonKindClass(): string {
        switch (this.buttonKind) {
            case 'submit':
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
            case 'submit':
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

    public onClick(event: Event): any {
        if (!this.disabled) {
            // wenn der Button enabled ist, fuehren wir die Aktion aus
            this.clickIfEnabled.emit(event);
        }
        // Wenn der Button disabled ist, kommen wir gar nicht hierhin. Aber (click) auf dem <lib-button> wuerde
        // trotzdem ausgefuehrt, also egal ob der <button> disabled oder enabled ist.
    }

}

