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

import {ViewportScroller} from '@angular/common';
import {AfterViewChecked, Component} from '@angular/core';
import {ActivationEnd, Router} from '@angular/router';
import {filter} from 'rxjs/operators';
import {canton} from 'vacme-web-shared';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements AfterViewChecked {
    title = 'vacme-initialreg';
    helpUrl = canton.publicHelpURL;
    fragment: string | undefined;
    changed = false;
    viewportScroller: ViewportScroller;

    constructor(router: Router, viewportScroller: ViewportScroller) {
        this.viewportScroller = viewportScroller;
        router.events.pipe(
                filter((e): e is ActivationEnd => e instanceof ActivationEnd)
            ).subscribe((event: ActivationEnd) => {
                this.changed = (event as ActivationEnd).snapshot.fragment !== this.fragment;
                // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                this.fragment = (event as ActivationEnd).snapshot.fragment!;
            },
            () => {
            });
    }

    ngAfterViewChecked(): void {
        if (!!this.fragment && this.changed) {
            setTimeout(() => {
                const element = document.getElementById(this.fragment as string);
                if (!!element) {
                    // Der ViewportScroller hat ein scrollOffset, siehe RouterModule.forRoot
                    this.viewportScroller.scrollToAnchor(this.fragment as string);
                    this.fragment = undefined;
                }
            }, 250);
        }

    }
}
