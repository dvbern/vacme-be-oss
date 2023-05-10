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
import {PublicService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';

const LOG = LogFactory.createLog('UmfrageCompletionComponent');

@Component({
    selector: 'app-umfrage-completion',
    templateUrl: './umfrage-completion.component.html',
    styleUrls: ['./umfrage-completion.component.scss']
})
export class UmfrageCompletionComponent implements OnInit {
    code!: string;

    constructor(private route: ActivatedRoute, private publicService: PublicService) {
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.code = params.code;
            this.publicService.publicResourceCompleteUmfrage(this.code).subscribe(_ => {
            }, err => {
                LOG.error(err);
            });
        }, err => {
            LOG.error(err);
        });
    }
}
