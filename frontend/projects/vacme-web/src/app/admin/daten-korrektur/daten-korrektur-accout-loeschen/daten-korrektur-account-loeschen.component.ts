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
import {UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {DossierService, KorrekturDashboardJaxTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturAccountLoeschenComponent');

@Component({
    selector: 'app-daten-korrektur-account-loeschen',
    templateUrl: './daten-korrektur-account-loeschen.component.html',
    styleUrls: ['./daten-korrektur-account-loeschen.component.scss']
})
export class DatenKorrekturAccountLoeschenComponent implements OnInit {

    @Input()
    korrekturDashboard: KorrekturDashboardJaxTS | undefined;

    @Output()
    public finished = new EventEmitter<boolean>();

    formGroup!: UntypedFormGroup;

    constructor(
        private authService: AuthServiceRsService,
        private fb: UntypedFormBuilder,
        private dossierService: DossierService,
        private translationService: TranslateService,
    ) {
    }

    ngOnInit(): void {
        this.formGroup = this.fb.group({
        });
    }

    public hasRequiredRole(): boolean {
        return this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.DELETE_ACCOUNT));
    }

    public enabled(): boolean {
        if (this.korrekturDashboard?.registrierungsnummer) {
            // Die Zulaessigkeit wird serverseitig geprueft
            return true;
        }
        return false;
    }

    public correctIfValid(): void {
        if (this.hasRequiredRole()) {
            FormUtil.doIfValid(this.formGroup, () => {
                this.deleteAccount();
            });
        }
    }

    public deleteAccount(): void {
        const registrierungsNummer = this.korrekturDashboard?.registrierungsnummer;
        if (registrierungsNummer) {
            Swal.fire({
                icon: 'question',
                text: this.translationService.instant('FACH-ADMIN.DATEN_KORREKTUR.DELETE_ACCOUNT_QUESTION'),
                showCancelButton: true,
                confirmButtonText: this.translationService.instant('OVERVIEW.DELETE_ACCOUNT_QUESTION_CONFIRM'),
                cancelButtonText: this.translationService.instant('OVERVIEW.DELETE_ACCOUNT_QUESTION_CANCEL')
            }).then(r => {
                if (r.isConfirmed) {
                    this.dossierService.dossierResourceDeleteRegistrierung(registrierungsNummer)
                        .subscribe(() => {
                            Swal.fire({
                                icon: 'success',
                                showCancelButton: false,
                                showConfirmButton: false,
                                timer: 1500,
                            });
                            this.finished.emit(false);
                        }, error => {
                            LOG.error('Could not delete Account', error);
                        });
                }
            });
        }
    }

    public reset(): void {
        this.korrekturDashboard = undefined;
        this.finished.emit(false);
    }
}
