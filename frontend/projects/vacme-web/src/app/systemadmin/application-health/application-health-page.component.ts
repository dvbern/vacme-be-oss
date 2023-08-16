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
import {UntypedFormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ApplicationhealthService, RegistrierungTermineImpfungJaxTS} from 'vacme-web-generated';

// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';

const LOG = LogFactory.createLog('ApplicationHealthPageComponent');

@Component({
    selector: 'app-application-health-page',
    templateUrl: './application-health-page.component.html',
    styleUrls: ['./application-health-page.component.scss'],
})
export class ApplicationHealthPageComponent extends BaseDestroyableComponent {

    public autokorrektur = false;
    public resultInkonsistenzenTermine: Array<RegistrierungTermineImpfungJaxTS> | undefined;

    constructor(
        private fb: UntypedFormBuilder,
        private router: Router,
        private activeRoute: ActivatedRoute,
        private authService: AuthServiceRsService,
        private applicationhealthService: ApplicationhealthService,
    ) {
        super();
    }

    public isUserInroleAsRegistrationOi(): boolean {
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI);
    }

    public getInkonsistenzenTermine(): void {
        this.applicationhealthService.applicationHealthResourceGetInkonsistenzenTermine(this.autokorrektur)
            .subscribe((res: RegistrierungTermineImpfungJaxTS[]) => {
                this.autokorrektur = false;
                this.resultInkonsistenzenTermine = res;
            }, (error: any) => LOG.error('ERROR getInkonsistenzenTermine', error));
    }
}

