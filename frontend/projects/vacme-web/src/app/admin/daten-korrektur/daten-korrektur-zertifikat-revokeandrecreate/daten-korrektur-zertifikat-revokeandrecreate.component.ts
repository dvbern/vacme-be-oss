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

import {DatePipe} from '@angular/common';
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {KorrekturDashboardJaxTS, KorrekturService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturZertifikatRevokeAndRecreateComponent');

@Component({
  selector: 'app-daten-korrektur-zertifikat-revokeandrecreate',
  templateUrl: './daten-korrektur-zertifikat-revokeandrecreate.component.html',
  styleUrls: ['./daten-korrektur-zertifikat-revokeandrecreate.component.scss']
})
export class DatenKorrekturZertifikatRevokeAndRecreateComponent implements OnInit {

    @Input()
    korrekturDashboard!: KorrekturDashboardJaxTS | undefined;

    @Output()
    public finished = new EventEmitter<boolean>();

    formGroup!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private authService: AuthServiceRsService,
        private korrekturService: KorrekturService,
        private translationService: TranslateService,
        private datePipe: DatePipe
    ) { }

    ngOnInit(): void {
        this.formGroup = this.fb.group({
            forcePostzustellung: this.fb.control(true, [Validators.required])
        });
    }

    public hasRequiredRole(): boolean {
        return this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.ZERTIFIKAT_REVOKE_AND_RECREATE));
    }

    public enabled(): boolean {
        if (this.korrekturDashboard?.vollstaendigerImpfschutz && this.korrekturDashboard.elektronischerImpfausweis) {
            return this.korrekturDashboard?.vollstaendigerImpfschutz  && this.korrekturDashboard.elektronischerImpfausweis;
        }
        return false;
    }

    public getTextLastSentZertifikatText(): string | null {
        if (this.korrekturDashboard?.timestampLetzterPostversand) {
            return this.translationService.instant('FACH-ADMIN.DATEN_KORREKTUR.ZERTIFIKAT_BEREITS_PER_POST_GESCHICKT', {
                timestampLetzterPostversand: this.datePipe.transform(
                    this.korrekturDashboard?.timestampLetzterPostversand, 'dd.MM.yyyy HH:mm')
            });
        }
        return null;
    }

    public zertifikatRevokeAndRecreate(): void {
        const regNummer = this.korrekturDashboard?.registrierungsnummer;
        if (!regNummer) {
            return;
        }
        const forcePost = !!this.formGroup.get('forcePostzustellung')?.value;
        this.korrekturService.korrekturResourceRecreateZertifikat(regNummer, forcePost)
            .subscribe(response => {
                Swal.fire({
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false,
                }).then(() => {
                    this.korrekturDashboard = undefined;
                    this.formGroup.reset();
                    this.finished.emit(true);
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

    public isGueltigeSchweizerAdresse(): boolean {
        return !!this.korrekturDashboard?.gueltigeSchweizerAdresse;
    }

    public postChecked(): boolean {
        return !!this.formGroup.get('forcePostzustellung')?.value;
    }
}
