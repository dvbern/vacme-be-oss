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
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js'; // nur das JS importieren
import {
    AmpelColorTS,
    OrtDerImpfungDisplayNameJaxTS,
    PublicService,
    VerarbreichungsartTS,
    VerarbreichungsseiteTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, Option} from '../../../../../vacme-web-shared';
import {
    DATE_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    MAX_LENGTH_TEXTAREA,
} from '../../../../../vacme-web-shared/src/lib/constants';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {BlobUtil} from '../../../../../vacme-web-shared/src/lib/util/BlobUtil';
import {minDateValidator} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/min-date-validator';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('DemoFormComponent');

@Component({
    selector: 'app-demo-form-page',
    templateUrl: './demo-form-page.component.html',
    styleUrls: ['./demo-form-page.component.scss'],
})
export class DemoFormPageComponent extends BaseDestroyableComponent implements OnInit {

    // --------------- NGMODEL -----------------------------------

    public ampelColor!: AmpelColorTS;
    public consent = false;
    public ortDerImpfungList = [
        {id: 'aaa', name: 'Impfzentrum AAA, mit Terminverwaltung'},
        {id: 'bbb', name: 'Arzt BBB, ohne Terminverwaltung'},
        {id: 'ccc', name: 'Mobiles Zentrum CCC, ohne Terminverwaltung'},
    ];

    public ortDerImfpungId?: string;
    public ortDerImfpung?: OrtDerImpfungDisplayNameJaxTS;

    // --------------- REACTIVE -----------------------------------
    public formGroup!: FormGroup;

    public verabreichungOrtLROptions: Option[] = Object.values(VerarbreichungsseiteTS).map(t => {
        return {label: t, value: t};
    });
    public verabreichungArtOptions: Option[] = Object.values(VerarbreichungsartTS).map(t => {
        return {label: t, value: t};
    });

    public odiOptions: any[] = [
        {label: '1 Impfzentrum Von der Heide, Schönried', value: '1'},
        {label: '2 Dr. med. Denise Vauffelinier, La Neuveville', value: '2'},
        {label: 'Andere', value: 'ANDERE'}, // test reordering
        {label: '3 Ärztezentrum Heinzelmann, Hinterfingen', value: '3'},
        {label: '4 Es war einmal, End der Welt', value: '4', disabled: true},
        {label: '5 Wurzelapotheke, Wald', value: '5', disabled: true},
    ];
    public onOdiChangeOutput?: string;

    // ----------- components
    public collapsibleComponentOpen = false;

    constructor(
        // --------------- REACTIVE -----------------------------------
        private fb: FormBuilder,
        private translationService: TranslateService,
        private publicService: PublicService,
        private errorMessageService: ErrorMessageService,
    ) {
        super();
    }

    ngOnInit(): void {

        // --------------- REACTIVE -----------------------------------

        const minLength = 2;

        this.formGroup = this.fb.group({
            ampelColorControl: this.fb.control(undefined,
                [Validators.required]),

            name: this.fb.control(undefined, [Validators.maxLength(DB_DEFAULT_MAX_LENGTH)]),
            vorname: this.fb.control(undefined,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            immobil: this.fb.control(false),
            geburtsdatum: this.fb.control(undefined,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN), Validators.required,
                    minDateValidator(moment('01.01.1910', 'DD.MM.YYYY').toDate()),
                ]),
            odi: this.fb.control(undefined, [Validators.required]),
            odi2: this.fb.control(undefined, [Validators.required]),
            verabreichung_art: this.fb.control(undefined, [Validators.required]),
            bemerkungen: this.fb.control(undefined, [Validators.maxLength(MAX_LENGTH_TEXTAREA), Validators.required]),
            agb: this.fb.control(undefined, [Validators.required]),
            einwilligung: this.fb.control(undefined, [Validators.required]),
            abgleichElektronischerImpfausweis: this.fb.control(undefined),
            verabreichung_ort_lr: this.fb.control(undefined, [Validators.required]),
            radioVertical: this.fb.control(undefined, [Validators.required]),
            grundimmunisierung: this.fb.control(undefined, [Validators.required]),
            externGeimpft: this.fb.control(undefined, Validators.required),
        });
    }

    // --------------- NGMODEL -----------------------------------

    public chooseItem(): void {
        this.ortDerImfpung = this.ortDerImfpungId
            ? (this.ortDerImpfungList?.find(each => each.id === this.ortDerImfpungId)) as OrtDerImpfungDisplayNameJaxTS
            : undefined;
    }

    isPass(): boolean {
        const pass = this.ampelColor !== undefined && this.ampelColor !== AmpelColorTS.RED;

        // if not passing anymore, reset the consent
        if (!pass) {
            this.consent = false;
        }

        return pass;
    }

    private handleRequestError(err: any,
                               handleStrategy: 'none' | 'manualsimple' | 'manualblob',
                               handleTypes: string[],
    ): void {
        LOG.error(err);
        switch (handleStrategy) {
            case 'none':
                return;
            case 'manualsimple':
                this.errorMessageService.addMesageAsError('** manuelles Errorhandling **');
                return;
            case 'manualblob':
                BlobUtil.parseErrorMessage(err.error, handleTypes)
                    .then(() => this.errorMessageService
                        // wird angezeigt, weil diese manuelle Message erst nach der Interceptor-Message erzeugt wird.
                        .addMesageAsError('** manuelles Errorhandling **  ' +
                            this.translationService.instant('OVERVIEW.DOWNLOAD_IlLEGAL_STATE')));
        }
    }

    // wie bei nextFreieTermin in OverviewPage
    public triggerBackendExceptionNormal(exc: boolean, handleStrategy: 'none' | 'manualsimple'): void {
        this.publicService.publicResourceThrowDemoException(exc)
            .subscribe(value => alert(value), err => {
                this.handleRequestError(err, handleStrategy, []);
            });
    }

    // wie bei Download, z.B. Terminbestaetigung
    public triggerBackendExceptionDownload(exc: boolean, handleStrategy: 'none' | 'manualsimple' | 'manualblob'): void {
        this.publicService.publicResourceThrowDemoBlobException(exc)
            .subscribe(value => alert(value), err => {
                this.handleRequestError(err,
                    handleStrategy,
                    ['AppValidationMessage.TERMINBESTAETIGUNG_KEIN_OFFENER_TERMIN']);
            });
    }

    // wie Massenimport
    public triggerBackendExceptionUpload(exc: boolean, handleStrategy: 'none' | 'manualsimple' | 'manualblob'): void {
        this.publicService.publicResourceThrowDemoUploadException(exc)
            .subscribe(value => alert(value), err => {
                this.handleRequestError(err, handleStrategy, ['AppValidationMessage.ILLEGAL_STATE']);
            });
    }

    public submitNgForm(): void {
        alert('Weiter zur Registrierung');
    }

    // --------------- NgModel mit ngSubmit ----------------------
    public submitNgSubmitForm(): void {
        alert('Weiter zur Registrierung');
    }

    // --------------- REACTIVE -----------------------------------

    public onOdiChange($event: any): void {
        this.onOdiChangeOutput = 'gewählter Impfort: ' + $event.target.value;
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.save();
        });
    }

    private save(): void {
        // blabla, do anything you want
        this.formGroup.disable();

        Swal.fire({
            icon: 'success',
            showCancelButton: false,
            showConfirmButton: false,
            timer: 1500,
        });
    }

    setRandomValues(): void {
        this.formGroup.controls.odi.setValue(this.odiOptions[1].value);
        this.formGroup.controls.ampelColorControl.setValue(AmpelColorTS.ORANGE);
        this.formGroup.controls.name.setValue('Wiesenmeier');
        this.formGroup.controls.vorname.setValue('Johanna');
        this.formGroup.controls.immobil.setValue(false);
        this.formGroup.controls.geburtsdatum.setValue(new Date().toDateString());
        this.formGroup.controls.verabreichung_art.setValue(this.verabreichungArtOptions[1].value);
        this.formGroup.controls.bemerkungen.setValue('Impfen ist super.');
        this.formGroup.controls.agb.setValue(false);
        this.formGroup.controls.einwilligung.setValue(true);
        this.formGroup.controls.abgleichElektronischerImpfausweis.setValue(false);
        this.formGroup.controls.verabreichung_ort_lr.setValue(this.verabreichungOrtLROptions[0].value);
    }

    setNullValues(): void {
        this.formGroup.reset();
    }

    log(text: string): void {
        console.log(text);
    }

    showConfirmDialog(): void {

        Swal.fire({
            icon: 'question',
            text: this.translationService.instant('OVERVIEW.CANCEL_TERMIN.QUESTION'),
            showCancelButton: true,
            cancelButtonText: this.translationService.instant('OVERVIEW.CANCEL_TERMIN.CANCEL'),
            confirmButtonText: this.translationService.instant('OVERVIEW.CANCEL_TERMIN.CONFIRM'),
        }).then(r => {
                if (r.isConfirmed) {
                    Swal.fire({
                        icon: 'success',
                        timer: 1500,
                        showConfirmButton: false,
                    });
                }
            },
        );
    }

    showErrorDialog(): void {
        Swal.fire({
            icon: 'warning',
            text: this.translationService.instant('REG_VALIDATION.MIN_DAYS_NOT_VALID'),
            showCancelButton: false,
            showConfirmButton: false,
        });
    }

    showInfoDialog(): void {
        Swal.fire({
            icon: 'info',
            text: this.translationService.instant('REG_VALIDATION.NO_TERMIN'),
            showCancelButton: false,
            showConfirmButton: false,
        });
    }

    showSuccessDialog(): void {
        Swal.fire({
            icon: 'success',
            showCancelButton: false,
            showConfirmButton: false,
        });
    }

    showValidation(): void {
        this.formGroup.markAllAsTouched();
    }
}
