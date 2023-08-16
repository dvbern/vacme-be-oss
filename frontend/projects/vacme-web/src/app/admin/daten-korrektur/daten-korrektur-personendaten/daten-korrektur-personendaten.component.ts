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
import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {first} from 'rxjs/operators';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    DossierService,
    GeschlechtTS,
    KorrekturDashboardJaxTS,
    KorrekturService,
    PersonendatenKorrekturJaxTS,
    RegistrierungsEingangTS,
    StammdatenService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {Option} from '../../../../../../vacme-web-shared';
import {DATE_PATTERN, DB_DEFAULT_MAX_LENGTH, EMAIL_PATTERN} from '../../../../../../vacme-web-shared/src/lib/constants';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {ConfirmUtil} from '../../../../../../vacme-web-shared/src/lib/util/confirm-util';
import {
    certifiableNamestringValidator,
} from '../../../../../../vacme-web-shared/src/lib/util/customvalidator/certifiable-namestring-validator';
import {
    datumInPastValidator,
} from '../../../../../../vacme-web-shared/src/lib/util/customvalidator/datum-in-past-validator';
import {
    parsableDateValidator,
} from '../../../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';
import DateUtil from '../../../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {
    KrankenkasseEnumInterface,
    KrankenkasseUtil,
} from '../../../../../../vacme-web-shared/src/lib/util/krankenkasse-util';
import TenantUtil from '../../../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {atLeastOneImpfung, getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturPersonendatenComponent');
const KEINE_ANGABE = 'keine_angabe';

@Component({
    selector: 'app-daten-korrektur-personendaten',
    templateUrl: './daten-korrektur-personendaten.component.html',
    styleUrls: ['./daten-korrektur-personendaten.component.scss'],
})
export class DatenKorrekturPersonendatenComponent implements OnInit, OnChanges {

    @Input()
    korrekturDashboard: KorrekturDashboardJaxTS | undefined;

    @Output()
    public finished = new EventEmitter<boolean>();

    @Input() public showTelefonHinweis = false;

    formGroup!: UntypedFormGroup;

    public krankenkassen!: KrankenkasseEnumInterface[];
    public krankenkassenSelected = false;
    public bezugOptions: Option[] = [];

    public geschlechtOptions: Option[] = Object.values(GeschlechtTS).map(t => {
        return {label: t, value: t};
    });

    public optionalTrueFalseOptions: Option[] = [
        {label: 'true', value: true},
        {label: 'false', value: false},
        {label: 'keine_angabe', value: KEINE_ANGABE},
    ];

    public kkUtil = KrankenkasseUtil;
    public verstorben?: boolean;

    constructor(
        private authService: AuthServiceRsService,
        private fb: UntypedFormBuilder,
        private translationService: TranslateService,
        private korrekturService: KorrekturService,
        private stammdatenService: StammdatenService,
        private datePipe: DatePipe,
        private dossierService: DossierService,
    ) {
    }

    ngOnInit(): void {
        const minLength = 2;
        this.formGroup = this.fb.group({
            geschlecht: this.fb.control(undefined,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            name: this.fb.control(undefined, [
                Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required,
                certifiableNamestringValidator(),
            ]),
            vorname: this.fb.control(undefined, [
                Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required,
                certifiableNamestringValidator(),
            ]),
            geburtsdatum: this.fb.control(undefined, [
                Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.pattern(DATE_PATTERN), Validators.required, parsableDateValidator(), datumInPastValidator(),
            ]),
            verstorben: this.fb.control(undefined),
            adresse1: this.fb.control(undefined, [
                Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required,
            ]),
            plz: this.fb.control(undefined, [
                Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required,
            ]),
            ort: this.fb.control(undefined, [Validators.required]),
            mail: this.fb.control(undefined, [
                Validators.minLength(minLength),
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.pattern(EMAIL_PATTERN),
            ]),
            telefon: this.fb.control(undefined, [
                Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required,
            ]),
            identifikationsnummer: this.fb.control(undefined,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH)]),
            abgleichElektronischerImpfausweis: this.fb.control(false, []),
            contactTracing: this.fb.control(false, []),
            krankenkasse: this.fb.control(undefined, [
                Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required,
            ]),
            krankenkasseKartenNr: this.fb.control(undefined, [
                Validators.minLength(20), Validators.maxLength(20), Validators.required,
            ]),
            auslandArt: this.fb.control(undefined,
                [KrankenkasseUtil.createAuslandArtValidator(this, 'krankenkasse')]),
            keinKontakt: this.fb.control(undefined),
            immunsupprimiert: this.fb.control(undefined),
        });

        if (!TenantUtil.hasFluechtlingUeberKrankenkasse()) {
            this.formGroup.addControl('schutzstatus', this.fb.control(undefined));
        }

        this.stammdatenService.stammdatenResourceGetKrankenkassen().pipe(first()).subscribe(
            (list: any) => {
                this.krankenkassen = list;
            },
            (error: any) => {
                LOG.error(error);
            },
        );
        ConfirmUtil.addCheckboxAreYouSureWarning(this.formGroup,
            'verstorben',
            this.translationService,
            'FACH-APP.KONTROLLE.VERSTORBEN');
    }

    ngOnChanges(_: SimpleChanges): void {
        if (this.korrekturDashboard && this.korrekturDashboard.registrierungsnummer) {
            this.korrekturService.korrekturResourceGetPersonendaten(
                this.korrekturDashboard?.registrierungsnummer).pipe().subscribe(
                (result: PersonendatenKorrekturJaxTS) => {
                    this.modelToForm(result);
                },
                (error: any) => {
                    LOG.error(error);
                },
            );
        }
    }

    private modelToForm(data: PersonendatenKorrekturJaxTS): void {
        if (data) {
            this.verstorben = data.verstorben;
            this.formGroup.patchValue(data);
            if (data.krankenkasse) {
                // Vom Server kommt ein Enum mit name und bagNummer. In Typescipr Enum haben wir dies nicht,
                // die Werte werden aber reingemappt
                // @ts-ignore
                this.formGroup.get('krankenkasse')?.setValue(data.krankenkasse.name);
                this.krankenkassenSelected = true;
                // @ts-ignore
                this.bezugOptions = KrankenkasseUtil.auslandArtOptions();
            }
            if (data.adresse) {
                this.formGroup.patchValue(data.adresse);
            }
            // at midday to avoid timezone issues
            this.formGroup.get('geburtsdatum')?.setValue(
                this.datePipe.transform(data.geburtsdatum?.setHours(12), 'dd.MM.yyyy'),
            );
            // Bei Eingang ONLINE kann Mail und Telefon nicht gesetzt werden
            if (this.korrekturDashboard?.eingang === RegistrierungsEingangTS.ONLINE_REGISTRATION) {
                this.formGroup.get('mail')?.disable();
                this.formGroup.get('telefon')?.disable();
                this.showTelefonHinweis = true;
            } else {
                this.formGroup.get('mail')?.enable();
                this.formGroup.get('telefon')?.enable();
            }
        }
    }

    private formToModel(): PersonendatenKorrekturJaxTS {
        const model: PersonendatenKorrekturJaxTS = {
            registrierungsnummer: this.korrekturDashboard?.registrierungsnummer,
            geschlecht: this.getFormControlValue('geschlecht'),
            name: this.getFormControlValue('name'),
            vorname: this.getFormControlValue('vorname'),
            geburtsdatum: DateUtil.parseDateAsMidday(this.getFormControlValue('geburtsdatum')),
            verstorben: this.getFormControlValue('verstorben'),
            adresse: {
                adresse1: this.getFormControlValue('adresse1'),
                plz: this.getFormControlValue('plz'),
                ort: this.getFormControlValue('ort'),
            },
            mail: this.getFormControlValue('mail'),
            telefon: this.getFormControlValue('telefon'),
            abgleichElektronischerImpfausweis: this.getFormControlValue('abgleichElektronischerImpfausweis'),
            identifikationsnummer: this.getFormControlValue('identifikationsnummer'),
            contactTracing: this.getFormControlValue('contactTracing'),
            krankenkasse: this.getFormControlValue('krankenkasse'),
            krankenkasseKartenNr: this.getFormControlValue('krankenkasseKartenNr'),
            auslandArt: this.getFormControlValue('auslandArt'),
            schutzstatus: this.getSchutzstatus(),
            keinKontakt: this.getFormControlValue('keinKontakt'),
            immunsupprimiert: this.getFormControlValue('immunsupprimiert') === KEINE_ANGABE ?
                null :
                this.getFormControlValue('immunsupprimiert'),

        };
        return model;
    }

    private getSchutzstatus(): boolean {
        if (TenantUtil.hasFluechtlingUeberKrankenkasse()) {
            return this.getFormControlValue('auslandArt') === 'FLUECHTLING';
        } else {
            return this.getFormControlValue('schutzstatus');
        }
    }

    public hasRequiredRole(): boolean {
        return this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.PERSONENDATEN));
    }

    public enabled(): boolean {
        return atLeastOneImpfung(this.korrekturDashboard);
    }

    public correctIfValid(): void {
        if (this.hasRequiredRole()) {
            FormUtil.doIfValid(this.formGroup, () => {
                this.correctData();
            });
        }
    }

    private correctData(): void {
        const data: PersonendatenKorrekturJaxTS = this.formToModel();
        const regNummer = this.korrekturDashboard?.registrierungsnummer;
        if (!regNummer || !data) {
            return;
        }
        this.korrekturService.korrekturResourcePersonendatenKorrigieren(regNummer, data).subscribe(_ => {
            void Swal.fire({
                icon: 'success',
                text: this.translationService.instant('FACH-ADMIN.DATEN_KORREKTUR.SUCCESS_PERSONENDATEN'),
                showConfirmButton: true,
            }).then(_ => {
                this.korrekturDashboard = undefined;
                this.formGroup.reset();
                this.finished.emit(true);
            });
        }, err => {
            LOG.error('Could not update Personendaten', err);
        });
    }

    public reset(): void {
        this.korrekturDashboard = undefined;
        this.formGroup.reset();
        this.finished.emit(false);
    }

    krankenkasseSelectChange(_: any): void {
        KrankenkasseUtil.onKrankenkasseChange(this, 'krankenkasse', 'krankenkasseKartenNr', 'auslandArt');
    }

    private getFormControlValue(field: string): any {
        return this.formGroup.get(field)?.value;
    }

    hasKeinKontaktEnabled(): boolean {
        return TenantUtil.hasKeinKontakt();
    }

    hasContactTracingEnabled(): boolean {
        return TenantUtil.hasContactTracing();
    }
}
