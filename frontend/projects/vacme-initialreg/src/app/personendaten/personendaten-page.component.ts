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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {Subject} from 'rxjs';
import {first, takeUntil} from 'rxjs/operators';
import Swal from 'sweetalert2/dist/sweetalert2.js';

import {
    AmpelColorTS,
    ChronischeKrankheitenTS,
    CreateRegistrierungJaxTS,
    GeschlechtTS,
    KrankheitIdentifierTS,
    RegistrierungsCodeJaxTS,
    RegistrierungService,
    StammdatenService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, Option} from '../../../../vacme-web-shared';

import {
    DATE_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    EMAIL_PATTERN,
    MAX_LENGTH_TEXTAREA,
} from '../../../../vacme-web-shared/src/lib/constants';
import {AuthServiceRsService} from '../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {TerminfindungResetService} from '../../../../vacme-web-shared/src/lib/service/terminfindung-reset.service';
import {BerufUtil} from '../../../../vacme-web-shared/src/lib/util/beruf-util';
import {
    certifiableNamestringValidator,
} from '../../../../vacme-web-shared/src/lib/util/customvalidator/certifiable-namestring-validator';
import {datumInPastValidator} from '../../../../vacme-web-shared/src/lib/util/customvalidator/datum-in-past-validator';
import {parsableDateValidator} from '../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';
import {
    isMobileNumberValidator,
    validPhoneNumberValidator,
} from '../../../../vacme-web-shared/src/lib/util/customvalidator/phone-number-validator';
import DateUtil from '../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../vacme-web-shared/src/lib/util/FormUtil';
import {FragebogenUtil} from '../../../../vacme-web-shared/src/lib/util/fragebogen-util';
import {KrankenkasseEnumInterface, KrankenkasseUtil} from '../../../../vacme-web-shared/src/lib/util/krankenkasse-util';
import TenantUtil from '../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {NavigationService} from '../service/navigation.service';

const LOG = LogFactory.createLog('PersonendatenComponent');

@Component({
    selector: 'app-personendaten-page',
    templateUrl: './personendaten-page.component.html',
    styleUrls: ['./personendaten-page.component.scss'],
})
export class PersonendatenPageComponent extends BaseDestroyableComponent implements OnInit, OnDestroy {

    private ngUnsubscribe$ = new Subject();

    public formGroup!: FormGroup;
    public ampelColor!: FormControl;
    public krankenkassen!: KrankenkasseEnumInterface[];
    public krankenkassenSelected = false;
    public bezugOptions: Option[] = [];
    public beruflicheTaetigkeit = TenantUtil.getBeruflicheTaetigkeit().map(t => {
        return {label: t, value: t};
    });
    public krankheitenOptions = Object.values(ChronischeKrankheitenTS).map(t => {
        return {label: t, value: t};
    });
    public geschlechtOptions = Object.values(GeschlechtTS).map(t => {
        return {label: t, value: t};
    });
    public localStoreAmpelColor!: AmpelColorTS;

    private saveRequestPending = false;

    public kkUtil = KrankenkasseUtil;

    public fragebogenUtil = FragebogenUtil;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private route: ActivatedRoute,
        private stammdatenService: StammdatenService,
        private registrierungsService: RegistrierungService,
        private authService: AuthServiceRsService,
        public translate: TranslateService,
        public terminfindungResetService: TerminfindungResetService,
        private navigationService: NavigationService,
    ) {
        super();
    }

    ngOnInit(): void {
        let parameters;
        if (this.showAmpel()) {
            // ampel color muss vom call center im form gesetzt werden
            this.ampelColor = this.fb.control(undefined, [Validators.required]);
        } else {
            this.localStoreAmpelColor = this.getAmpelColorFromState();

            // TODO Affenpocken VACME-2365 with multiple Krankheiten, the Ampel should be removed.
            //  Zurich and until the relevant question in VACME-2365 has been answered, require the Ampel to be set/not
            // red. Hence we overwrite it here to reduce its impact.
            if (TenantUtil.isMultiKrankheitGUI()) {
                this.localStoreAmpelColor = AmpelColorTS.UNBEKANNT;
            }

            // if the ampel color isn't set, then the user skipped this step so we redirect the user
            // back to the impfaehigkeit page
            if (!Object.values(AmpelColorTS).includes(this.localStoreAmpelColor)) {
                this.router.navigate(['impffaehigkeit']);
            }
            const principal = this.authService.getPrincipal();
            parameters = {
                name: principal?.nachname,
                vorname: principal?.vorname,
                mail: principal?.email,
                telefon: principal?.telefon,
            };
        }
        const minLength = 2;
        const mailValidators = this.hasRoleCallCenter() ?
            [
                Validators.minLength(minLength),
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.pattern(EMAIL_PATTERN),
            ] :
            [
                Validators.minLength(minLength),
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.pattern(EMAIL_PATTERN),
                Validators.required,
            ];
        this.formGroup = this.fb.group({
            geschlecht: this.fb.control(undefined,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            name: this.fb.control(parameters?.name,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required,
                    certifiableNamestringValidator(),
                ]),
            vorname: this.fb.control(parameters?.vorname,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required,
                    certifiableNamestringValidator(),
                ]),
            strasse: this.fb.control(undefined,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            plz: this.fb.control(undefined, Validators.required),
            ort: this.fb.control(undefined,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            immobil: this.fb.control(false),
            mail: this.fb.control(parameters?.mail, mailValidators),
            telefon: this.fb.control(parameters?.telefon,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required,
                    TenantUtil.BERN ? isMobileNumberValidator() : validPhoneNumberValidator(),
                ]),
            krankenkasse: this.fb.control(undefined,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            kartennummer: this.fb.control(undefined,
                [Validators.minLength(20), Validators.maxLength(20), Validators.required]),
            auslandArt: this.fb.control(undefined,
                [KrankenkasseUtil.createAuslandArtValidator(this, 'krankenkasse')]),
            geburtsdatum: this.fb.control(undefined,
                [
                    Validators.minLength(minLength),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN),
                    Validators.required,
                    parsableDateValidator(),
                    datumInPastValidator(),
                ]),
            bemerkungen: this.fb.control(undefined, Validators.maxLength(MAX_LENGTH_TEXTAREA)),
            krankheiten: this.fb.control(undefined, Validators.required),
            lebensumstaende: this.fb.control(undefined, Validators.required),
            beruflicheTaetigkeit: this.fb.control(undefined, Validators.required),
        });

        if (!TenantUtil.isMultiKrankheitGUI()) {
            this.formGroup.addControl('abgleichElektronischerImpfausweis', this.fb.control(false));
            this.formGroup.addControl('contactTracing', this.fb.control(false));
        }

        if (!TenantUtil.hasFluechtlingUeberKrankenkasse()) {
            this.formGroup.addControl('schutzstatus', this.fb.control(false));
        }

        BerufUtil.addBerufAutoselectForChildren(this.formGroup, 'geburtsdatum', 'beruflicheTaetigkeit');

        if (this.showAmpel()) {
            this.formGroup.addControl('ampelColor', this.ampelColor);
            this.formGroup.get('ampelColor')?.valueChanges
                .pipe(takeUntil(this.ngUnsubscribe$))
                .subscribe(selectedColor => {
                    if (selectedColor === 'RED') {
                        this.formGroup.get('beruflicheTaetigkeit')?.disable();
                        this.formGroup.get('lebensumstaende')?.disable();
                        this.formGroup.get('krankheiten')?.disable();
                        this.formGroup.get('strasse')?.disable();
                        this.formGroup.get('plz')?.disable();
                        this.formGroup.get('ort')?.disable();
                        this.formGroup.get('bemerkungen')?.disable();
                        this.formGroup.get('geburtsdatum')?.disable();
                        this.formGroup.get('geschlecht')?.disable();
                        this.formGroup.get('immobil')?.disable();
                        this.formGroup.get('krankenkasse')?.disable();
                        this.formGroup.get('kartennummer')?.disable();
                        this.formGroup.get('auslandArt')?.disable();
                        this.formGroup.get('schutzstatus')?.disable();
                        this.formGroup.get('mail')?.disable();
                        this.formGroup.get('name')?.disable();
                        this.formGroup.get('telefon')?.disable();
                        this.formGroup.get('vorname')?.disable();
                        this.formGroup.get('abgleichElektronischerImpfausweis')?.disable();
                        this.formGroup.get('contactTracing')?.disable();
                    } else {
                        this.formGroup.get('beruflicheTaetigkeit')?.enable();
                        this.formGroup.get('lebensumstaende')?.enable();
                        this.formGroup.get('krankheiten')?.enable();
                        this.formGroup.get('strasse')?.enable();
                        this.formGroup.get('plz')?.enable();
                        this.formGroup.get('ort')?.enable();
                        this.formGroup.get('bemerkungen')?.enable();
                        this.formGroup.get('geburtsdatum')?.enable();
                        this.formGroup.get('geschlecht')?.enable();
                        this.formGroup.get('immobil')?.enable();
                        this.formGroup.get('krankenkasse')?.enable();
                        this.formGroup.get('kartennummer')?.enable();
                        this.formGroup.get('auslandArt')?.enable();
                        this.formGroup.get('schutzstatus')?.enable();
                        this.formGroup.get('mail')?.enable();
                        this.formGroup.get('name')?.enable();
                        this.formGroup.get('telefon')?.enable();
                        this.formGroup.get('vorname')?.enable();
                        this.formGroup.get('abgleichElektronischerImpfausweis')?.enable();
                        this.formGroup.get('contactTracing')?.enable();
                    }
                }, err => LOG.error(err));
        }
        if (!this.hasRoleCallCenter()) {
            this.formGroup.addControl('agb', this.fb.control(false, [Validators.required, Validators.requiredTrue]));
            this.formGroup.addControl('einwilligung',
                this.fb.control(false, [Validators.required, Validators.requiredTrue]));
        }

        this.stammdatenService.stammdatenResourceRegGetKrankenkassen().pipe(first()).subscribe(
            (list: any) => {
                this.krankenkassen = list;
            },
            (error: any) => {
                LOG.error(error);
            },
        );
    }

    krankenkasseSelectChange($event: any): void {
        KrankenkasseUtil.onKrankenkasseChange(this, 'krankenkasse', 'kartennummer', 'auslandArt');
    }

    public isSaveEnabled(): boolean {
        if (this.saveRequestPending) {
            return false;
        }
        if (this.showAmpel()) {
            return this.getAmpelColor() !== AmpelColorTS.RED;
        }
        return true;
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
        const value: CreateRegistrierungJaxTS = this.formToModel();
        this.registrierungsService.registrierungResourceRegistrieren(value).subscribe(
            (res: RegistrierungsCodeJaxTS) => {
                this.removeAmpelColorFromState();
                this.resetTermineInTerminfindung(); // reset wegen callcenter
                this.forwardToExternGeimpftOrImpfdossiersOverview(res);
            },
            (err: any) => {
                LOG.error('HTTP Error', err);
                this.saveRequestPending = false;
            },
        );
    }

    private forwardToExternGeimpftOrImpfdossiersOverview(code: RegistrierungsCodeJaxTS): void {
        if (TenantUtil.ZURICH) {
            this.navigationService.navigateToExternGeimpftPage(code.registrierungsnummer, KrankheitIdentifierTS.COVID);
        } else {
            this.navigationService.navigateToStartpage(code.registrierungsnummer, KrankheitIdentifierTS.COVID);
        }
    }

    private formToModel(): CreateRegistrierungJaxTS {
        const model: CreateRegistrierungJaxTS = {
            beruflicheTaetigkeit: this.getFormControlValue('beruflicheTaetigkeit'),
            lebensumstaende: this.getFormControlValue('lebensumstaende'),
            chronischeKrankheiten: this.getFormControlValue('krankheiten'),
            adresse: {
                adresse1: this.getFormControlValue('strasse'),
                plz: this.getFormControlValue('plz'),
                ort: this.getFormControlValue('ort'),
            },
            language: this.translate.currentLang,
            bemerkung: this.getFormControlValue('bemerkungen'),
            geburtsdatum: DateUtil.parseDateAsMidday(this.getFormControlValue('geburtsdatum')),
            geschlecht: this.getFormControlValue('geschlecht'),
            immobil: this.getFormControlValue('immobil'),
            krankenkasse: this.getFormControlValue('krankenkasse'),
            krankenkasseKartenNr: this.getFormControlValue('kartennummer'),
            auslandArt: this.getFormControlValue('auslandArt'),
            mail: this.getFormControlValue('mail'),
            name: this.getFormControlValue('name'),
            telefon: this.getFormControlValue('telefon'),
            vorname: this.getFormControlValue('vorname'),
            ampelColor: this.getAmpelColor(),
            schutzstatus: this.getSchutzstatus(),
        };

        if (!TenantUtil.isMultiKrankheitGUI()) {
            model.abgleichElektronischerImpfausweis = this.getFormControlValue('abgleichElektronischerImpfausweis');
            model.contactTracing = this.getFormControlValue('contactTracing');
        }

        return model;
    }

    private getSchutzstatus(): boolean {
        if (TenantUtil.hasFluechtlingUeberKrankenkasse()) {
            return this.getFormControlValue('auslandArt') === 'FLUECHTLING';
        } else {
            return this.getFormControlValue('schutzstatus');
        }
    }

    private getAmpelColor(): any {
        if (this.showAmpel()) {
            return this.getFormControlValue('ampelColor');
        }
        return this.localStoreAmpelColor;
    }

    private getFormControlValue(field: string): any {
        return this.formGroup.get(field)?.value;
    }

    private getAmpelColorFromState(): AmpelColorTS {
        return localStorage.getItem('ampel') as AmpelColorTS;
    }

    private removeAmpelColorFromState(): void {
        localStorage.removeItem('ampel');
    }

    private resetTermineInTerminfindung(): void {
        this.terminfindungResetService.resetData();
    }

    hasRoleCallCenter(): boolean {
        return this.authService.hasRoleCallCenter();
    }

    public showAmpel(): boolean {
        return this.hasRoleCallCenter() && !TenantUtil.isMultiKrankheitGUI();
    }

    showImmobilConfirmation(): void {
        const immobilControl = this.formGroup.controls.immobil;

        if (immobilControl.value) {
            Swal.fire({
                icon: 'question',
                text: this.translate.instant('REGISTRIERUNG.IMMOBIL_CONFIRMATION'),
                showCancelButton: true,
                confirmButtonText: this.translate.instant('CONFIRMATION.YES'),
                cancelButtonText: this.translate.instant('CONFIRMATION.NO'),
            }).then(r => {
                if (!r.isConfirmed) {
                    immobilControl.setValue(false);
                }
            });
        }
    }

    hasMobileOrtDerImpfung(): boolean {
        return TenantUtil.hasMobileOrtDerImpfung();
    }

    ngOnDestroy(): void {
        this.ngUnsubscribe$.next();
        this.ngUnsubscribe$.complete();
    }

    hasContactTracingEnabled(): boolean {
        return TenantUtil.hasContactTracing();
    }
}
