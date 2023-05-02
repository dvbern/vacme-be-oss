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

import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    BenutzerBasicinfoJaxTS,
    DossierService,
    EmailTelephoneKorrekturJaxTS,
    KorrekturDashboardJaxTS,
    KorrekturService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BenutzerService} from '../../../../../../vacme-web-generated/src/lib/api/benutzer.service';
import {
    DB_DEFAULT_MAX_LENGTH,
    EMAIL_PATTERN,
    TEL_REGEX_NUMBER_INT,
} from '../../../../../../vacme-web-shared/src/lib/constants';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../../../vacme-web-shared/src/lib/util/FormUtil';
import ValidationUtil from '../../../../../../vacme-web-shared/src/lib/util/ValidationUtil';
import {getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturEmailComponent');

@Component({
    selector: 'app-daten-korrektur-email',
    templateUrl: './daten-korrektur-email.component.html',
    styleUrls: ['./daten-korrektur-email.component.scss']
})
export class DatenKorrekturEmailComponent implements OnInit, OnDestroy {

    private ngUnsubscribe$ = new Subject();
    public basicUserInfo?: BenutzerBasicinfoJaxTS = undefined;

    @Input()
    set korrekturDashboardJax(value: KorrekturDashboardJaxTS | undefined) {
        this.korrekturDashboard = value;
        if (this.hasRequiredRole() && this.korrekturDashboard?.registrierungsnummer) {
            this.dossierService.dossierResourceGetUsername(this.korrekturDashboard.registrierungsnummer)
                .subscribe(response => {
                    this.formGroup.get('username')?.setValue(response);
                    this.benutzerService.benutzerResourceBasicBenutzerinfo(response).subscribe(
                        basicInfo => {
                            this.basicUserInfo = basicInfo;
                            this.formGroup.get('telefon')?.setValue(basicInfo.mobileNummer);
                            this.formGroup.get('mail')?.setValue(basicInfo.email);
                        }, err => {
                            LOG.error('Could not get infos', err);
                        }
                    );
                }, err => {
                    LOG.error('Could not get username', err);
                });
        }
    }

    korrekturDashboard: KorrekturDashboardJaxTS | undefined;

    @Output()
    public finished = new EventEmitter<boolean>();

    formGroup!: FormGroup;

    public hasSwissMobileVorwahl = true;


    constructor(
        private fb: FormBuilder,
        private authServiceRsService: AuthServiceRsService,
        private benutzerService: BenutzerService,
        private translationService: TranslateService,
        private korrekturService: KorrekturService,
        private dossierService: DossierService
    ) { }

    ngOnInit(): void {
        const minLengthPhone = 4;
        const minLength = 2;
        this.formGroup = this.fb.group({
            username: this.fb.control(undefined),
            mail: this.fb.control(undefined,
                [
                    Validators.minLength(minLength),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(EMAIL_PATTERN)
                ]),
            telefon: this.fb.control(undefined,
                [Validators.minLength(minLength),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.minLength(minLengthPhone),
                    Validators.pattern(TEL_REGEX_NUMBER_INT)
                    ]),
        });
        this.formGroup.get('username')?.disable();

        // validate mobile nummer
        this.formGroup.get('telefon')?.valueChanges
            .pipe(takeUntil(this.ngUnsubscribe$))
            .subscribe(value => {
               this.updateHasSwissMobileVorwahl(value);
            }, err => LOG.error(err));

    }

    ngOnDestroy(): void {
        this.ngUnsubscribe$.next();
        this.ngUnsubscribe$.complete();
    }

    public hasRequiredRole(): boolean {
        return this.authServiceRsService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.EMAIL_TELEPHONE));
    }

    public updateHasSwissMobileVorwahl(telefon: string): void{
        if (telefon) {
            this.hasSwissMobileVorwahl =  ValidationUtil.hasSwissMobileVorwahl(telefon);
        }
    }


    public correctIfValid(): void {
        if (this.hasRequiredRole()) {
            Swal.fire({
                icon: 'info',
                text: this.translationService.instant('FACH-ADMIN.DATEN_KORREKTUR.EMAIL_TELEPHONE_INFO'),
                showConfirmButton: true,
                showCancelButton: true,
                confirmButtonText: this.translationService.instant('CONFIRMATION.YES'),
                cancelButtonText: this.translationService.instant('CONFIRMATION.NO'),
            }).then((r) => {
                if (r.isConfirmed) {
                    FormUtil.doIfValid(this.formGroup, () => {
                        this.correctData();
                    });
                }
            });
        }
    }

    private correctData(): void {
        const data: EmailTelephoneKorrekturJaxTS = {
            telefon: this.formGroup.get('telefon')?.value,
            mail: this.formGroup.get('mail')?.value,
        };
        const regNummer = this.korrekturDashboard?.registrierungsnummer;
        if (!regNummer) {
            return;
        }
        this.korrekturService.korrekturResourceEmailTelephoneKorrigieren(regNummer, data).subscribe(res => {
            Swal.fire({
                icon: 'success',
                text: this.translationService.instant('FACH-ADMIN.DATEN_KORREKTUR.SUCCESS_EMAIL_TELEPHONE'),
                showConfirmButton: true,
            }).then(() => {
                this.korrekturDashboard = undefined;
                this.formGroup.reset();
                this.finished.emit(true);
            });
        }, err => {
            LOG.error('Could not update mail and telephone', err);
        });
    }

    public reset(): void {
        this.korrekturDashboard = undefined;
        this.formGroup.reset();
        this.finished.emit(false);
    }

}
