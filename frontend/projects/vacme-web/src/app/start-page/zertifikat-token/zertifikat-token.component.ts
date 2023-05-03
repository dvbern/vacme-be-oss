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
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SwalPortalTargets} from '@sweetalert2/ngx-sweetalert2';
import Swal from 'sweetalert2/dist/sweetalert2.js'; // nur das JS importieren
import {LogFactory} from 'vacme-web-shared';
import {ZertifikatService} from '../../../../../vacme-web-generated/src/lib/api/zertifikat.service';
import {API_TOKEN_MAX_LENGTH} from '../../../../../vacme-web-shared/src/lib/constants';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('ZertifikatTokenComponent');

/**
 * This component is to paste the token to generate certificates for storage on the server
 */
@Component({
  selector: 'app-zertifikat-token',
  templateUrl: './zertifikat-token.component.html',
  styleUrls: ['./zertifikat-token.component.scss']
})
export class ZertifikatTokenComponent implements OnInit {

    public formGroup!: FormGroup;
    public hasValidToken = false;

    constructor(
      public readonly swalTargets: SwalPortalTargets,
      private fb: FormBuilder,
      private zertifikatService: ZertifikatService,
    ) { }

    ngOnInit(): void {
        this.updateHasValidToken();
        this.formGroup = this.fb.group({
            token: this.fb.control(undefined, [
                Validators.required, Validators.maxLength(API_TOKEN_MAX_LENGTH)]),
        });
    }

    public saveToken(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.zertifikatService.zertifikatResourceTokenErfassen({
                token: this.formGroup.get('token')?.value
            }).subscribe(response => {
                this.updateHasValidToken();
                Swal.fire({
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false,
                });
            }, error => {
                LOG.error(error);
            });
        });
    }

    public deleteAllTokens(): void {
        this.zertifikatService.zertifikatResourceClearAllCovidCertTokens().subscribe(response => {
            this.updateHasValidToken();
            Swal.fire({
                icon: 'success',
                timer: 1500,
                showConfirmButton: false,
            });
        }, error => {
            LOG.error(error);
        });

    }

    private updateHasValidToken(): void {
        this.zertifikatService.zertifikatResourceHasValidToken().subscribe(
            response => {
                this.hasValidToken = response;
                this.formGroup.reset();
            }, error => {
                LOG.error(error);
            }
        );
    }

}
