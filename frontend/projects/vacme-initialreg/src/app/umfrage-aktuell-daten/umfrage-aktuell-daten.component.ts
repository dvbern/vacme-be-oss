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
import {FormBuilder, FormGroup} from '@angular/forms';
import {LogFactory} from 'vacme-web-shared';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {
    BaseDestroyableComponent
} from '../../../../vacme-web-shared';


const LOG = LogFactory.createLog('AccountPageComponent');


@Component({
    selector: 'app-umfrage-aktuell-daten',
    templateUrl: './umfrage-aktuell-daten.component.html',
    styleUrls: ['./umfrage-aktuell-daten.component.scss'],
})
export class UmfrageAktuellDatenComponent extends BaseDestroyableComponent implements OnInit {

    public formGroup!: FormGroup;

    constructor(
        public authServiceRsService: AuthServiceRsService
    ) {
        super();
    }


    ngOnInit(): void {
    }
}
