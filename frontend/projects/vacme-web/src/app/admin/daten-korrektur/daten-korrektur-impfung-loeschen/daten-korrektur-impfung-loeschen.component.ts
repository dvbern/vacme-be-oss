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
import {UntypedFormBuilder, UntypedFormGroup, ValidatorFn, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {KorrekturDashboardJaxTS, KorrekturService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {ImpfungLoeschenJaxTS} from 'vacme-web-generated';
import {Option} from '../../../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {isAtLeastOnceGeimpft} from '../../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';
import DatenKorrekturService from '../daten-korrektur.service';
import {ImpfungListItem} from '../ImpfungListItem';
import {getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturImpfungLoeschenComponent');

@Component({
    selector: 'app-daten-korrektur-impfung-loeschen',
    templateUrl: './daten-korrektur-impfung-loeschen.component.html',
    styleUrls: ['./daten-korrektur-impfung-loeschen.component.scss'],
})
export class DatenKorrekturImpfungLoeschenComponent implements OnInit {

    @Input()
    korrekturDashboard?: KorrekturDashboardJaxTS;

    @Output()
    public finished = new EventEmitter<boolean>();

    formGroup!: UntypedFormGroup;

    public impfungenListOptions: Option[] = [];

    constructor(
        private authService: AuthServiceRsService,
        private fb: UntypedFormBuilder,
        private korrekturService: KorrekturService,
        private translateService: TranslateService,
        private datenKorrekturUtil: DatenKorrekturService,
    ) {
    }

    ngOnInit(): void {
        this.formGroup = this.fb.group({
            impfung: this.fb.control(null,
                [Validators.required, this.nurLetzteImpfungValidator()]),
        });
    }

    private nurLetzteImpfungValidator(): ValidatorFn {
        return (control): null | {must_be_last_impfung: true} => {
            if (this.impfungenListOptions.length > 0) {
                if (this.impfungenListOptions[this.impfungenListOptions.length - 1].value !== control.value) {
                    return {must_be_last_impfung: true};
                }
            }
            return null;
        };
    }

    availableImpfungAndImpffolgeOptions(): Option[] {
        if (!this.impfungenListOptions?.length) {
            this.impfungenListOptions = this.datenKorrekturUtil.availableImpfungAndImpffolgeOptions(this.korrekturDashboard);
        }
        return this.impfungenListOptions;
    }

    public hasRequiredRole(): boolean {
        return this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.DELETE_IMPFUNG));
    }

    public enabled(): boolean {
        if (this.korrekturDashboard?.registrierungsnummer) {
            return isAtLeastOnceGeimpft(this.korrekturDashboard.status);
        }
        return false;
    }

    public correctIfValid(): void {
        if (this.hasRequiredRole()) {
            FormUtil.doIfValid(this.formGroup, () => {
                this.correctData();
            });
        }
    }

    private correctData(): void {
        const regNummer = this.korrekturDashboard?.registrierungsnummer;
        const impfung: ImpfungListItem = this.formGroup.get('impfung')?.value;
        if (!regNummer || !impfung) {
            return;
        }
        Swal.fire({
            icon: 'question',
            title: this.translateService.instant('FACH-ADMIN.DATEN_KORREKTUR.DELETE_IMPFUNG_WARNING_DIALOG.TITLE'),
            text: this.translateService.instant('FACH-ADMIN.DATEN_KORREKTUR.DELETE_IMPFUNG_WARNING_DIALOG.LABEL'),
            showCancelButton: true,
            cancelButtonText: this.translateService.instant('FACH-ADMIN.DATEN_KORREKTUR.DELETE_IMPFUNG_WARNING_DIALOG.CANCEL'),
            confirmButtonText: this.translateService.instant('FACH-ADMIN.DATEN_KORREKTUR.DELETE_IMPFUNG_WARNING_DIALOG.CONFIRM'),
        }).then((r1) => {
            if (r1.isConfirmed) {
                this.deleteImpfung(regNummer, impfung);
            }
        });
    }

    private deleteImpfung(regNummer: string, impfung: ImpfungListItem): void {
        const jax: ImpfungLoeschenJaxTS = {
            impffolge: impfung.impffolge,
            impffolgeNr: impfung.impffolgeNr,
            krankheitIdentifier: this.korrekturDashboard?.krankheitIdentifier,
        };
        this.korrekturService.korrekturResourceImpfungLoeschen(regNummer, jax).subscribe(() => {
            this.showConfirmationBoxWithQuestion();
        }, err => {
            LOG.error('Could not delete Impfung', err);
        });
    }

    private showConfirmationBoxWithQuestion(): void {
        Swal.fire({
            icon: 'success',
            text: this.translateService.instant('FACH-ADMIN.DATEN_KORREKTUR.SUCCESS'),
            showConfirmButton: true,
        }).then(() => {
            this.korrekturDashboard = undefined;
            this.formGroup.reset();
            this.finished.emit(true);
        });
    }

    public reset(): void {
        this.korrekturDashboard = undefined;
        this.formGroup.reset();
        this.finished.emit(false);
    }
}
