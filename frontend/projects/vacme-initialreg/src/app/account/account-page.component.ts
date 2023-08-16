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

import {Component} from '@angular/core';
import {Observable, of} from 'rxjs';
import {BenutzerService} from 'vacme-web-generated';

// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../vacme-web-shared';
import {TSRole} from '../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';

const LOG = LogFactory.createLog('AccountPageComponent');

@Component({
    templateUrl: './account-page.component.html',
    styleUrls: ['./account-page.component.scss'],
})
export class AccountPageComponent extends BaseDestroyableComponent {

    federationLink$: Observable<Array<string>>;

    constructor(
        public authServiceRsService: AuthServiceRsService,
        public benutzerservice: BenutzerService,
    ) {
        super();
        this.federationLink$ = this.benutzerservice.benutzerResourceRegReadFederationLinks();
    }

    showAdresseKkFragebogen(): boolean {
        return this.authServiceRsService.hasRole(TSRole.IMPFWILLIGER);
    }

    public removeFederation(fedLink: string): void {
        this.benutzerservice.benutzerResourceRegRemoveFederationLink(fedLink).subscribe(fedLinks => {
            this.federationLink$ = of(fedLinks);
        }, error => {
            LOG.error(error);
        });
    }

    public getMappedIDPName(fedLink: string): any {
        if (fedLink && fedLink.includes('well')) {
            return 'WELL';
        }
    }
}
