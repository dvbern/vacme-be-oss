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
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {KorrekturDashboardJaxTS, KorrekturService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturZertifikatComponent');

@Component({
  selector: 'app-daten-korrektur-zertifikat',
  templateUrl: './daten-korrektur-zertifikat.component.html',
  styleUrls: ['./daten-korrektur-zertifikat.component.scss']
})
export class DatenKorrekturZertifikatComponent implements OnInit {

    @Input()
    korrekturDashboard!: KorrekturDashboardJaxTS | undefined;

    @Output()
    public finished = new EventEmitter<boolean>();

    formGroup!: UntypedFormGroup;

    constructor(
        private fb: UntypedFormBuilder,
        private authService: AuthServiceRsService,
        private korrekturService: KorrekturService,
    ) { }

    ngOnInit(): void {
        this.formGroup = this.fb.group({
            forcePostzustellung: this.fb.control(null, [Validators.required])
        });
    }

    public hasRequiredRole(): boolean {
        return this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.ZERTIFIKAT));
    }

    public enabled(): boolean {
        if (this.korrekturDashboard?.vollstaendigerImpfschutz && this.korrekturDashboard.elektronischerImpfausweis) {
            return this.korrekturDashboard?.vollstaendigerImpfschutz  && this.korrekturDashboard.elektronischerImpfausweis;
        }
        return false;
    }

    public zertifikatNeuErstellen(): void {
        const regNummer = this.korrekturDashboard?.registrierungsnummer;
        if (!regNummer) {
            return;
        }
        const forcePost = !!this.formGroup.get('forcePostzustellung')?.value;
        this.korrekturService.korrekturResourceRegenerateZertifikate(regNummer, forcePost)
            .subscribe(_ => {
                void Swal.fire({
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false,
                });
            }, error => {
                LOG.error(error);
                Swal.hideLoading();
            });
    }

    public zertifikatZurueckziehen(): void {
        const regNummer = this.korrekturDashboard?.registrierungsnummer;
        if (!regNummer) {
            return;
        }
        const forcePost = !!this.formGroup.get('forcePostzustellung')?.value;
        this.korrekturService.korrekturResourceRevokeZertifikate(regNummer, forcePost)
            .subscribe(_ => {
                void Swal.fire({
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false,
                });
            }, error => {
                LOG.error(error);
                Swal.hideLoading();
            });
    }

    public reset(): void {
        this.korrekturDashboard = undefined;
        this.finished.emit(false);
    }

}
