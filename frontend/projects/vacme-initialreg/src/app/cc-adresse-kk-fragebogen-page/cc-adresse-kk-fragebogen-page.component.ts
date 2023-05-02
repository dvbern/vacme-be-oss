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
import {TSRole} from '../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {
    BaseDestroyableComponent
} from '../../../../vacme-web-shared';

const LOG = LogFactory.createLog('CcAdresseKkFragebogenPageComponent');

@Component({
    templateUrl: './cc-adresse-kk-fragebogen-page.component.html',
    styleUrls: ['./cc-adresse-kk-fragebogen-page.component.scss']
})
export class CcAdresseKkFragebogenPageComponent extends BaseDestroyableComponent implements OnInit {

    public registrierungNummer?: string;

    constructor(
        private route: ActivatedRoute,
        public authServiceRsService: AuthServiceRsService) {
        super();
    }

    ngOnInit(): void {
        this.route.params
            .pipe(this.takeUntilDestroyed())
            .subscribe(params => {
            this.registrierungNummer = params.registrierungsnummer || undefined;
        }, error => LOG.error(error));
    }

    showAdresseKkFragebogen(): boolean {
        return this.authServiceRsService.hasRole(TSRole.CC_AGENT);
    }

}
