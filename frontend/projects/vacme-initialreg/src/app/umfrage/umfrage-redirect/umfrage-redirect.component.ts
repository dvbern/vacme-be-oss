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

import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {LogFactory} from 'vacme-web-shared';

const LOG = LogFactory.createLog('UmfrageRedirectComponent');

@Component({
    selector: 'app-umfrage-redirect',
    templateUrl: './umfrage-redirect.component.html',
    styleUrls: ['./umfrage-redirect.component.scss']
})
export class UmfrageRedirectComponent implements OnInit {

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            window.location.href = `https://survey.w-hoch2.ch/index.php/321279?token=${params.code}&newtest=Y`;
        }, err => {
            LOG.error(err);
        });
    }
}
