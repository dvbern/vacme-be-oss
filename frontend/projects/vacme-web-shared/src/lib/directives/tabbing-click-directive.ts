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

/**
 * Diese Direktive macht Anchors mit (click) oder (routerLink) accessible. Sie ergaenzt das Element mit
 * tabindex=0 und role=button und faengt keydown.enter und keydown.space ab und fuehrt (click) aus.
 *
 * Kopiert und selber erweitert durch DV Bern von der Website:
 * https://www.bennadel.com/blog/3633-giving-click-anchor-links-tab-access-using-a-directive-in-angular-7-2-15.htm
 *
 */

// Import the core angular services.
import {Directive} from '@angular/core';

// ----------------------------------------------------------------------------------- //
// ----------------------------------------------------------------------------------- //

@Directive({
    // eslint-disable-next-line @angular-eslint/directive-selector
    selector: '(a[click]):not([href]):not([role]):not([tabindex]):not([x-no-tabbing]),div[click]',
    // eslint-disable-next-line @angular-eslint/no-host-metadata-property
    host: {
        // Adding [tabindex] allows tab-based access to this element. The "0" indicates
        // that the tabbing order should follow the native DOM element ordering.
        tabindex: '0',
        // Adding [role] tells screen readers that this "link" is really a "button",
        // in that it triggers an action, but doesn't navigate to a new resource.
        role: 'button',
        // Adding (keydown) allows us to translate the Enter and Spacebar keys into a
        // "click" event. This is the native behavior of a Button; so, we are trying to
        // mimic that behavior for our "link button".
        // --
        // NOTE: This is perhaps a good "code smell" that we should be using a Button
        // instead of a link for this host element.
        '(keydown.enter)': '$event.preventDefault() ; $event.target.click() ;',
        '(keydown.space)': '$event.preventDefault() ; $event.target.click() ;'
    }
})
export class TabbingClickDirective {
    //
}
