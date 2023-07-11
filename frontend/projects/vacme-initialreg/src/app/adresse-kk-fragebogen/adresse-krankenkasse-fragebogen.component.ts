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

import {Component, Input, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {first} from 'rxjs/operators';

// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    ChronischeKrankheitenTS,
    RegistrierungService,
    SelfserviceEditJaxTS,
    StammdatenService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {Option} from '../../../../vacme-web-shared';
import {DB_DEFAULT_MAX_LENGTH, MAX_LENGTH_TEXTAREA} from '../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../vacme-web-shared/src/lib/util/FormUtil';
import {FragebogenUtil} from '../../../../vacme-web-shared/src/lib/util/fragebogen-util';
import {KrankenkasseEnumInterface, KrankenkasseUtil} from '../../../../vacme-web-shared/src/lib/util/krankenkasse-util';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {OdiDistanceCacheService} from '../service/odi-distance.cache.service';

const LOG = LogFactory.createLog('UserprofileComponent');

@Component({
    selector: 'app-adresse-kk-fragebogen',
    templateUrl: './adresse-krankenkasse-fragebogen.component.html',
    styleUrls: ['./adresse-krankenkasse-fragebogen.component.scss'],
})
export class AdresseKrankenkasseFragebogenComponent implements OnInit {

    @Input()
    public registrierungNummer?: string;

    @Input()
    public isPopup = false;

    public formGroup!: UntypedFormGroup;
    public formVisible = false;

    public saveRequestPending = false;
    public kkUtil = KrankenkasseUtil;
    public fragebogenUtil = FragebogenUtil;
    public krankenkassen!: KrankenkasseEnumInterface[];

    public krankenkassenSelected = false;
    public bezugOptions: Option[] = [];
    public beruflicheTaetigkeit = TenantUtil.getBeruflicheTaetigkeit().map(t => {
        return {label: t, value: t};
    });
    public krankheitenOptions = Object.values(ChronischeKrankheitenTS).map(t => {
        return {label: t, value: t};
    });

    constructor(
        private registrierungService: RegistrierungService,
        private fb: UntypedFormBuilder,
        private translateService: TranslateService,
        private stammdatenService: StammdatenService,
        private odiDistanceCache: OdiDistanceCacheService,
        public authServiceRsService: AuthServiceRsService,
        private router: Router
    ) {
    }

    ngOnInit(): void {

        // KRANKENKASSEN
        this.stammdatenService.stammdatenResourceRegGetKrankenkassen().pipe(first()).subscribe(
            (list: any) => {
                this.krankenkassen = list;

                // FORMGROUP
                const minLength = 2;
                this.formGroup = this.fb.group({
                    // Adresse
                    adresse: this.fb.group({
                            adresse1: this.fb.control(undefined,
                                [
                                    Validators.minLength(minLength),
                                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                                    Validators.required,
                                ]),
                            plz: this.fb.control(undefined, Validators.required),
                            ort: this.fb.control(undefined,
                                [
                                    Validators.minLength(minLength),
                                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                                    Validators.required,
                                ]),
                        },
                    ),

                    // KrankenkassenkrankenkasseKartenNr
                    krankenkasse: this.fb.control(undefined,
                        [
                            Validators.minLength(minLength),
                            Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                            Validators.required,
                        ]),
                    krankenkasseKartenNr: this.fb.control(undefined,
                        [Validators.minLength(20), Validators.maxLength(20), Validators.required]),
                    auslandArt: this.fb.control(undefined,
                        [KrankenkasseUtil.createAuslandArtValidator(this, 'krankenkasse')]),

                    // Fragebogen
                    bemerkung: this.fb.control(undefined, Validators.maxLength(MAX_LENGTH_TEXTAREA)),
                    chronischeKrankheiten: this.fb.control(undefined, Validators.required),
                    lebensumstaende: this.fb.control(undefined, Validators.required),
                    beruflicheTaetigkeit: this.fb.control(undefined, Validators.required),
                    keinKontakt: this.fb.control(undefined)
                });

                // LOAD MODEL
                this.loadModel();
            },
            (error: any) => {
                LOG.error(error);
            },
        );
    }

    public adresseFormGroup(): UntypedFormGroup {
        return this.formGroup.get('adresse') as UntypedFormGroup;
    }

    private loadModel(): void {
        const loadModelRequest$ = this.registrierungNummer
            // mit Nummer fuer CC -> lade Daten einer Person
            ? this.registrierungService.registrierungResourceGetSelfserviceEditJaxCC(this.registrierungNummer)
            // ohne Nummer fuer Impfling -> lade eigene Daten
            : this.registrierungService.registrierungResourceGetSelfserviceEditJax();
        loadModelRequest$.pipe(first()).subscribe(
            (selfserviceEditJax: SelfserviceEditJaxTS) => {
                // only show form if there is a Reg present (there might only be an account but not Registrierung)
                this.formVisible = !!selfserviceEditJax;
                this.modelToForm(selfserviceEditJax);
            }, error => LOG.error(error),
        );
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.save();
        });
    }

    private save(): void {
        if (this.saveRequestPending) {
            return;
        }
        this.saveRequestPending = true; // can only run one save request at a time
        const value: SelfserviceEditJaxTS = this.formToModel();
        const saveRequest$ = this.registrierungNummer
            // mit Nummer fuer CC -> speichere Daten einer Person
            ? this.registrierungService
                .registrierungResourceUpdateSelfserviceEditDataCC(this.registrierungNummer, value)
            // ohne Nummer fuer Impfling -> speichere eigene Daten
            : this.registrierungService.registrierungResourceUpdateSelfserviceEditData(value);
        saveRequest$.subscribe(
            (res: any) => {
                Swal.fire({
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false,
                }).then(() => {
                    if (this.isPopup) {
                        // If we are a popup, continue automatically.
                        this.router.navigateByUrl('/start');
                    }
                });
                this.saveRequestPending = false;
                this.odiDistanceCache.clear();
                this.loadModel();
            },
            (err: any) => {
                LOG.error('HTTP Error', err);
                this.saveRequestPending = false;
            },
        );
    }

    private modelToForm(model: SelfserviceEditJaxTS): void {
        const formGroup = this.formGroup;
        formGroup.reset();
        if (!model) {
            return;
        }

        // Das Jax entspricht 1:1 dem Form
        formGroup.patchValue(model);

        // Die Adresse entspricht 1:1 der Adresse-FormGroup
        formGroup.get('adresse')?.patchValue(model.adresse);

        // Ausnahme Krankenkassennummer (Enum-Problem)
        // @ts-ignore weil die Krankenkasse ein Enum ist, aber von openapi als objekt erstellt wurde...
        formGroup.get('krankenkasse')?.patchValue(model.krankenkasse?.name);

        // KrankenkasseDisable
        this.krankenkasseDisableIfOhnePrefix();
    }

    private formToModel(): SelfserviceEditJaxTS {
        const modelToSave: SelfserviceEditJaxTS = this.formGroup.value as SelfserviceEditJaxTS;
        modelToSave.krankenkasseKartenNr = this.formGroup.get('krankenkasseKartenNr')?.value; // sonst fehlt die Nummer
                                                                                              // falls disabled
        modelToSave.timestampInfoUpdate = new Date();
        return modelToSave;
    }

    krankenkasseSelectChange($event: any): void {
        KrankenkasseUtil.onKrankenkasseChange(this, 'krankenkasse', 'krankenkasseKartenNr', 'auslandArt');
    }

    krankenkasseDisableIfOhnePrefix(): void {
        KrankenkasseUtil.krankenkasseDisableIfOhnePrefix(this, 'krankenkasse', 'krankenkasseKartenNr');
    }

    // Nur für CC-Agent und Zürich aktiviert
    hasKeinKontaktEnabled(): boolean {
        return this.authServiceRsService.hasRole(TSRole.CC_AGENT)
            && TenantUtil.hasKeinKontakt();
    }
}
