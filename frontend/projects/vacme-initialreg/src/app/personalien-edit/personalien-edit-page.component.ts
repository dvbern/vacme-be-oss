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
import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {FileSaverService} from 'ngx-filesaver';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {DossierService, PersonalienJaxTS, RegistrierungService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../vacme-web-shared';
import {DB_DEFAULT_MAX_LENGTH} from '../../../../vacme-web-shared/src/lib/constants';
import FormUtil from '../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('PersonalienEditComponent');

@Component({
    selector: 'app-personalien-edit-page',
    templateUrl: './personalien-edit-page.component.html',
    styleUrls: ['./personalien-edit-page.component.scss'],
})
export class PersonalienEditPageComponent extends BaseDestroyableComponent implements OnInit {

    public personalien!: PersonalienJaxTS;
    public formGroup!: FormGroup;
    public elektronischerAusweisGroup!: FormGroup;
    public elektronischerImpfausweis = false;
    public hasZertifikat = false;
    public isZertifikatEnabled = false;

    constructor(
        private activeRoute: ActivatedRoute,
        private datePipe: DatePipe,
        private registrierungService: RegistrierungService,
        private dossierService: DossierService,
        private translationService: TranslateService,
        private filesaver: FileSaverService,
        private fb: FormBuilder,
    ) {
        super();
    }

    ngOnInit(): void {
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(next => {
                this.personalien = next.personalien;
                const minLength = 2;
                this.formGroup = this.fb.group({
                    name: this.fb.control(this.personalien.name),
                    vorname: this.fb.control(this.personalien.vorname),
                    geburtsdatum: this.fb.control(
                        this.datePipe.transform(this.personalien.geburtsdatum?.setHours(12), 'dd.MM.yyyy')),
                    registrierungId: this.fb.control(this.personalien.registrierungId),
                    strasse: this.fb.control(this.personalien.adresse?.adresse1, [
                        Validators.minLength(minLength),
                        Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                        Validators.required,
                    ]),
                    plz: this.fb.control(this.personalien.adresse?.plz, [
                        Validators.minLength(minLength),
                        Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                        Validators.required,
                    ]),
                    ort: this.fb.control(this.personalien.adresse?.ort, [Validators.required]),
                });
                this.formGroup.get('name')?.disable();
                this.formGroup.get('vorname')?.disable();
                this.formGroup.get('geburtsdatum')?.disable();

                // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                this.hasZertifikat = !!this.personalien.currentZertifikatInfo!.hasCovidZertifikat;
                this.elektronischerImpfausweis = !!this.personalien.abgleichElektronischerImpfausweis;
                this.elektronischerAusweisGroup = this.fb.group({
                    elektronischerImpfausweis: this.fb.control(
                        this.personalien.abgleichElektronischerImpfausweis, Validators.requiredTrue),
                });
                if (this.personalien.vollstaendigerImpfschutz) {
                    this.dossierService.dossierResourceRegIsZertifikatEnabled().subscribe(
                        response => {
                            this.isZertifikatEnabled = response;
                        },
                        error => LOG.error(error));
                }
            }, error => {
                LOG.error(error);
            });
    }

    speichern(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            if (this.personalien.registrierungId) {
                const updateJax = {
                    registrierungId: this.personalien.registrierungId,
                    name: this.personalien.name,
                    vorname: this.personalien.vorname,
                    geburtsdatum: this.personalien.geburtsdatum,
                    adresse: {
                        adresse1: this.formGroup.get('strasse')?.value,
                        adresse2: this.personalien.adresse?.adresse2,
                        plz: this.formGroup.get('plz')?.value,
                        ort: this.formGroup.get('ort')?.value,
                    },
                };
                this.registrierungService.registrierungResourceUpdatePersonalien(this.personalien.registrierungId,
                    updateJax)
                    .subscribe(response => {
                        Swal.fire({
                            icon: 'success',
                            showCancelButton: false,
                            showConfirmButton: false,
                            timer: 1500,
                        });
                    }, error => LOG.error(error));
            }
        });
    }

    public saveImpfausweis(): void {
        FormUtil.doIfValid(this.elektronischerAusweisGroup, () => {
            if (this.personalien.registrierungId) {
                this.registrierungService.registrierungResourceAcceptElektronischerImpfausweisWithId(
                    this.personalien.registrierungId).subscribe(response => {
                    this.elektronischerImpfausweis = true;
                    Swal.fire({
                        icon: 'success',
                        showCancelButton: false,
                        showConfirmButton: false,
                        timer: 1500,
                    });
                }, error => LOG.error(error));
            }
        });
    }

    public showZertifikatBox(): boolean {
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        return !!this.personalien.currentZertifikatInfo!.deservesZertifikat &&
            !(this.personalien.abgleichElektronischerImpfausweis && !this.isZertifikatEnabled);
    }

    resendZertifikatPerPost(): void {
        if (this.personalien.registrierungId) {
            this.dossierService.dossierResourceRegRecreatePerPost(this.personalien.registrierungId)
                .subscribe(value => {
                    Swal.fire({
                        icon: 'success',
                        timer: 1500,
                        showConfirmButton: false,
                    });
                }, error => LOG.error(error));
        }
    }
}
