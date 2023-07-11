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

import {DOCUMENT} from '@angular/common';
import {Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {Observable, Subject, Subscription} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    BenutzerDisplayNameJaxTS,
    DashboardJaxTS,
    GeschlechtTS,
    ImpfdokumentationJaxTS,
    ImpfdokumentationService,
    ImpffolgeTS,
    ImpfstoffJaxTS,
    KantonaleBerechtigungTS,
    KontrolleService,
    KrankheitIdentifierTS,
    OrtDerImpfungJaxTS,
    VerarbreichungsartTS,
    VerarbreichungsortTS,
    VerarbreichungsseiteTS,
    ZulassungsStatusTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {Option} from '../../../../../../vacme-web-shared';
import {
    DATE_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    DB_VMDL_SCHNITTSTELLE_LENGTH,
    MAX_LENGTH_TEXTAREA,
    REGEX_IMPFMENGE,
} from '../../../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {TerminUtilService} from '../../../../../../vacme-web-shared/src/lib/service/termin-util.service';
import {TerminfindungService} from '../../../../../../vacme-web-shared/src/lib/service/terminfindung.service';
import {VacmeSettingsService} from '../../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {
    datumInPastValidator,
} from '../../../../../../vacme-web-shared/src/lib/util/customvalidator/datum-in-past-validator';
import {minDateValidator} from '../../../../../../vacme-web-shared/src/lib/util/customvalidator/min-date-validator';
import {
    parsableDateValidator,
} from '../../../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';
import {
    requiredIfValidator,
} from '../../../../../../vacme-web-shared/src/lib/util/customvalidator/required-if-validator';
import DateUtil from '../../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {ExternGeimpftUtil} from '../../../../../../vacme-web-shared/src/lib/util/externgeimpft-util';
import FormUtil from '../../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {ImpfstoffUtil} from '../../../../../../vacme-web-shared/src/lib/util/impfstoff-util';
import {SelectOptionsUtil} from '../../../../../../vacme-web-shared/src/lib/util/select-options-util';
import TenantUtil from '../../../../../../vacme-web-shared/src/lib/util/TenantUtil';
import ValidationUtil from '../../../../../../vacme-web-shared/src/lib/util/ValidationUtil';
import {ImpfdokumentationCache, LotAndMenge} from '../../../model/impfdokumentation.cache';
import {ImpfdokumentationCacheService} from '../../../service/impfdokumentation.cache.service';
import {RegistrierungValidationService} from '../../../service/registrierung-validation.service';
import {TerminmanagementKontrolleImpfdokService} from '../../../service/terminmanagement-kontrolle-impfdok.service';

const LOG = LogFactory.createLog('ImpfdokumentationFormComponent');
const NACHTRAG = 'nachtrag';
const NO_NACHTRAG = 'no_nachtrag';
const KEINE_ANGABE = 'keine_angabe';

export interface ImpfdokumentationFormBaseData {
    krankheitIdentifier: KrankheitIdentifierTS;
    modif: boolean;
    accessOk?: boolean;
    impffolgeNr: number;
    impffolge: ImpffolgeTS;
    validate: (dossier: DashboardJaxTS | undefined, impffolge: ImpffolgeTS, odiId: string | null,
               impfstoff: ImpfstoffJaxTS | undefined, odiList: OrtDerImpfungJaxTS[],
    ) => void;
    disable$: Observable<boolean>;
    saved$: Observable<boolean>;
    validateTrigger$: Observable<void>;
    dashboardJax?: DashboardJaxTS;
    impfstoffe?: ImpfstoffJaxTS[];
    odiList: OrtDerImpfungJaxTS[];
    canSelectGrundimmunisierung: boolean;
    defaultGrundimmunisierung: boolean;
    selbstzahlende?: boolean;
}

export interface ImpfdokumentationFormSubmission {
    impfdokumentation: ImpfdokumentationJaxTS;
    odiId: string;
}

export interface ImpfstoffOption {
    label: string | undefined;
    value?: string | null;
    color: string | undefined;
    disabled: boolean;
    url: string | undefined;
    eingestellt: boolean;
}

@Component({
    selector: 'app-impfdokumentation-form',
    templateUrl: './impfdokumentation-form.component.html',
    styleUrls: ['./impfdokumentation-form.component.scss'],
})
export class ImpfdokumentationFormComponent implements OnInit, OnDestroy {
    @Input()
    public set baseData(baseData: ImpfdokumentationFormBaseData | undefined) {
        this.currentBaseData = baseData;
        this.refresh();
    }

    public get baseData(): ImpfdokumentationFormBaseData | undefined {
        return this.currentBaseData;
    }

    private currentBaseData?: ImpfdokumentationFormBaseData;

    @Output() submited: EventEmitter<ImpfdokumentationFormSubmission> = new EventEmitter<ImpfdokumentationFormSubmission>();
    @Output() selectOdi: EventEmitter<string | null> = new EventEmitter<string | null>();
    @Output() back: EventEmitter<void> = new EventEmitter<void>();
    @Output() notVaccinated: EventEmitter<void> = new EventEmitter<void>();

    public formGroup!: UntypedFormGroup;

    ortDerImpfungId: string | null = null;
    impffolge!: ImpffolgeTS;
    private isInitialized = false;
    public odiOptions: Option[] = [];
    public krankheiten = KrankheitIdentifierTS;
    public verantwortlichenOptions: BenutzerDisplayNameJaxTS[] = [];
    public alleVerantwortlichenOptions: BenutzerDisplayNameJaxTS[] = [];
    public durchfuehrendenOptions: BenutzerDisplayNameJaxTS[] = [];
    public alleDurchfuehrendenOptions: BenutzerDisplayNameJaxTS[] = [];
    public impfstoffOptions: ImpfstoffOption[] = [];
    public impfstoffBackgroundColor = '#FFFFFF';
    public impfstoffInformationsLink: string | undefined;
    public verabreichungArtOptions: Option[] = Object.values(VerarbreichungsartTS).map(t => {
        return {label: t, value: t};
    });
    public verabreichungOrtOptions = Object.values(VerarbreichungsortTS).map(t => {
        return {label: t, value: t};
    });
    public verabreichungOrtLROptions: Option[] = Object.values(VerarbreichungsseiteTS).map(t => {
        return {label: t, value: t};
    });
    public selbstzahlendeOptions: Option[] = [];
    public verarbreichungsSeiteSelectedVal?: string;
    public saved = false;
    allergieInfoExpanded = false;
    nachtragOptions?: { label: string; value: any }[] = [];
    impffolgeNr = 0;
    private ngUnsubscribe$ = new Subject<void>();
    private parentSubscriptions: Subscription[] = [];
    private lastSubmission?: ImpfdokumentationFormSubmission;

    public impfstoffNeedsOnlyOneDose = false;

    public optionalTrueFalseOptions = [
        {label: 'true', value: true},
        {label: 'false', value: false},
        {label: 'keine_angabe', value: KEINE_ANGABE},
    ];

    constructor(
        private fb: UntypedFormBuilder,
        private kontrolleService: KontrolleService,
        private impfdokumentationService: ImpfdokumentationService,
        private translationService: TranslateService,
        private impfdokumentationCacheService: ImpfdokumentationCacheService,
        private registrierungValidationService: RegistrierungValidationService,
        private terminfindungService: TerminfindungService,
        private terminUtilService: TerminUtilService,
        private authService: AuthServiceRsService,
        private terminmanagementKontrolleImpfdokService: TerminmanagementKontrolleImpfdokService,
        private vacmeSettingsService: VacmeSettingsService,
        @Inject(DOCUMENT) private document: Document,

    ) {
    }

    ngOnInit(): void {
        this.initUI();
        this.isInitialized = true;
        this.refresh();
    }

    ngOnDestroy(): void {
        this.ngUnsubscribe$.next(undefined);
        this.ngUnsubscribe$.complete();
        this.parentSubscriptions.forEach(subscription => subscription.unsubscribe());
    }

    private refresh(): void {
        this.parentSubscriptions.forEach(subscription => subscription.unsubscribe());
        this.parentSubscriptions = [];
        if (this.currentBaseData && this.isInitialized) {
            this.impffolgeNr = this.currentBaseData.impffolgeNr;
            this.impffolge = this.currentBaseData.impffolge;
            this.fillFormWithCachedValuesAndValuesFromModel();
            this.setImpfstoffOptions(this.currentBaseData?.impfstoffe);
            this.setOdis(this.currentBaseData.odiList);
            this.refreshNachtragOptions();
            this.parentSubscriptions.push(this.currentBaseData.disable$.subscribe(
                disable => disable ? this.formGroup.disable({emitEvent: false}) : this.formGroup.enable(),
                error => LOG.error(error),
            ));
            this.parentSubscriptions.push(this.currentBaseData.validateTrigger$.subscribe(
                () => this.validate(),
                error => LOG.error(error),
            ));
            this.parentSubscriptions.push(this.currentBaseData.saved$.subscribe(
                (saved) => this.saved = saved,
                error => LOG.error(error),
            ));

            // Warnungen ausgeben, wenn Prio oder Termin nicht korrekt sind
            if (!this.currentBaseData.modif) {
                this.validate();
            }

            const impfstoff = this.getImpfstoff(this.getFormControlValue('impfstoff'));
            this.impfstoffNeedsOnlyOneDose = impfstoff?.anzahlDosenBenoetigt === 1;
            this.updateImpfstoffColorStyle(impfstoff?.hexFarbe);
            this.updateImpfstoffLink(impfstoff?.informationsLink);
        }
    }

    private refreshNachtragOptions(): void {
        this.nachtragOptions = [
            {label: 'IMPFUNG_HEUTE', value: NO_NACHTRAG},
            {label: 'IMPFUNG_NACHTRAG', value: NACHTRAG},
        ];
    }

    odiSelectChange(odiId: string | null): void {
        const odi = this.odiOptions.find((k: any) => k.value === odiId);
        this.ortDerImpfungId = null;
        this.verantwortlichenOptions = [];
        this.alleVerantwortlichenOptions = [];
        this.durchfuehrendenOptions = [];
        this.alleDurchfuehrendenOptions = [];
        this.formGroup.get('verantwortlich')?.setValue(null);
        this.formGroup.get('durchfuehrend')?.setValue(null);
        if (odi) {
            this.ortDerImpfungId = odi.value;
            this.getVerantwortlichePerson();
            this.getDurchfuehrendePerson();
            this.updateOdiInLocalStorage(odi);
            // Neues ODi gewaehlt, pruefen, ob der Impfling hier den Termin hat
            if (this.currentBaseData && !this.currentBaseData.modif) {
                this.validate();
            }
        }
        this.selectOdi.emit(this.ortDerImpfungId);
    }

    public impfstoffSelectChange(): void {
        const impfstoffId = this.getFormControlValue('impfstoff');
        const impfstoffOpt = this.impfstoffOptions.find((k: any) => k.value === impfstoffId);
        if (this.currentBaseData && impfstoffOpt) {
            this.updateLotAndMenge(impfstoffOpt.value);
            // Neuer Impfstoff gewaehlt. Pruefen, dass ggf. bei Impfung 1 derselbe Impfstoff verwendet wurde!
            this.validate();
        } else {
            this.clearLotAndMenge();
        }
        this.impfstoffNeedsOnlyOneDose =
            this.getImpfstoff(this.getFormControlValue('impfstoff'))?.anzahlDosenBenoetigt === 1;
        this.updateImpfstoffColorStyle(impfstoffOpt?.color);
        this.updateImpfstoffLink(impfstoffOpt?.url);
    }

    private updateLotAndMenge(impfstoff?: string | null): void {
        const savedLotAndMenge: LotAndMenge | undefined =
            this.impfdokumentationCacheService.getLotAndMengeForImpfstoff(this.getKrankheit(), impfstoff);
        if (savedLotAndMenge) {
            this.formGroup.get('lot')?.setValue(savedLotAndMenge?.lot);
            this.formGroup.get('menge')?.setValue(savedLotAndMenge?.menge);
        } else {
            this.clearLotAndMenge();
        }
    }

    private clearLotAndMenge(): void {
        this.formGroup.get('lot')?.reset();
        this.formGroup.get('menge')?.reset();
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            const submission = {
                impfdokumentation: this.toImpfdokumentationJax(),
                odiId: this.getFormControlValue('odi')?.value,
            };
            this.lastSubmission = submission;
            this.submited.emit(submission);
        });
    }

    public canSelectGrundimmunisierung(): boolean {
        return !!this.currentBaseData?.canSelectGrundimmunisierung;
    }

    public canSelectSchwanger(): boolean {
        return this.currentBaseData?.dashboardJax?.geschlecht !== GeschlechtTS.MAENNLICH;
    }

    public canSelectAttributeFuerAffenpocken(): boolean{
        return this.getKrankheit() === KrankheitIdentifierTS.AFFENPOCKEN;
    }

    public canSelectAttributeFuerFSME(): boolean{
        return this.getKrankheit() === KrankheitIdentifierTS.FSME;
    }

    public canSelectImpfschema(): boolean {
        return ExternGeimpftUtil.showSchnellschema(this.getKrankheit());
    }

    public showWarningIfSchwanger(): boolean {
        return TenantUtil.showSchwangerWarnung()
            && this.canSelectSchwanger()
            && this.getFormControlValue('schwanger') === true;
    }

    private toImpfdokumentationJax(): ImpfdokumentationJaxTS {
        let nachtraeglicheErfassung: true | undefined; // todo homa das wird immer nur true oder leer sein?
        const nachtragErsteImpfungValue = this.getFormControlValue('impfungNachtragen');
        if (nachtragErsteImpfungValue && nachtragErsteImpfungValue === NACHTRAG) {
            nachtraeglicheErfassung = true;
        }
        // todo Affenpocken ich glaube das ist falsch weil wir brauchen ein boolean setzen aber einen string
        let schwangerschaft;
        if (this.canSelectSchwanger() && this.getFormControlValue('schwanger') !== KEINE_ANGABE) {
            schwangerschaft =  this.getFormControlValue('schwanger');
        }

        let immunsupprimiert: boolean | undefined;
        if (this.getFormControlValue('immunsupprimiert') !== KEINE_ANGABE) {
            immunsupprimiert = this.getFormControlValue('immunsupprimiert'); // todo homa wie wird das zu einem boolean?
        }
        const parsedImpfstoff = this.getImpfstoff(this.getFormControlValue('impfstoff'));

        if (parsedImpfstoff === undefined) {
            throw Error('Impfstoff nicht gesetzt');
        }

        let risikoreichesSexualleben: boolean | undefined;
        if(this.canSelectAttributeFuerAffenpocken() && this.getFormControlValue('risikoreichesSexualleben') !== KEINE_ANGABE){
            risikoreichesSexualleben = this.getFormControlValue('risikoreichesSexualleben');
        }

        let impfungAusBeruflichenGruenden: boolean | undefined;
        if(this.canSelectAttributeFuerAffenpocken() && this.getFormControlValue('impfungAusBeruflichenGruenden') !== KEINE_ANGABE){
            impfungAusBeruflichenGruenden = this.getFormControlValue('impfungAusBeruflichenGruenden');
        }

        let kontaktMitPersonAusRisikogruppe: boolean | undefined;
        if(this.canSelectAttributeFuerAffenpocken() && this.getFormControlValue('kontaktMitPersonAusRisikogruppe') !== KEINE_ANGABE){
            kontaktMitPersonAusRisikogruppe = this.getFormControlValue('kontaktMitPersonAusRisikogruppe');
        }

        let zeckenstich: boolean | undefined;
        if(this.canSelectAttributeFuerFSME() && this.getFormControlValue('zeckenstich') !== KEINE_ANGABE){
            zeckenstich = this.getFormControlValue('zeckenstich');
        }

        const impfdokumentation: ImpfdokumentationJaxTS  = {
            krankheitIdentifier: this.getFormControlValue('krankheitIdentifier'),
            nachtraeglicheErfassung,
            datumFallsNachtraeglich: DateUtil.parseDateAsMidday(this.getFormControlValue('datumFallsNachtrag')),
            extern: this.getFormControlValue('extern'),
            grundimmunisierung: this.canSelectGrundimmunisierung() ? this.getFormControlValue('grundimmunisierung')
                : this.currentBaseData?.defaultGrundimmunisierung,
            verantwortlicherBenutzerId: this.getFormControlValue('verantwortlich').benutzerId,
            durchfuehrenderBenutzerId: this.getFormControlValue('durchfuehrend').benutzerId,
            impfstoff: parsedImpfstoff,
            lot: this.getFormControlValue('lot'),
            fieber: !this.getFormControlValue('kein_fieber_keine_kontraindikation'),
            neueKrankheit: !this.getFormControlValue('kein_fieber_keine_kontraindikation'),
            keineBesonderenUmstaende: this.getFormControlValue('keine_besonderen_umstaende'),
            schwanger: schwangerschaft,
            einwilligung: this.getFormControlValue('einwilligung'),
            verarbreichungsart: this.getFormControlValue('verabreichung_art') as VerarbreichungsartTS,
            verarbreichungsort: this.getFormControlValue('verabreichung_ort') as VerarbreichungsortTS,
            verarbreichungsseite: this.getFormControlValue('verabreichung_ort_lr') as VerarbreichungsseiteTS,
            menge: this.getFormControlValue('menge'),
            bemerkung: this.getFormControlValue('bemerkung'),
            registrierungsnummer: this.currentBaseData?.dashboardJax?.registrierungsnummer as string,
            immunsupprimiert,
            selbstzahlende: this.getFormControlValue('selbstzahlende'),
            risikoreichesSexualleben,
            impfungAusBeruflichenGruenden,
            kontaktMitPersonAusRisikogruppe,
            schnellschema: this.getFormControlValue('schnellschema'),
            zeckenstich
        };

        return impfdokumentation;
    }

    public onBack(): void {
        this.back.emit();
    }

    private initUI(): void {

        this.formGroup = this.fb.group({
            krankheitIdentifier: this.fb.control(undefined),
            impfungNachtragen: this.fb.control(undefined, [Validators.required]),
            datumFallsNachtrag: this.fb.control(undefined, []),
            extern: this.fb.control(false, []),
            selbstzahlende: this.fb.control(undefined, Validators.required),
            grundimmunisierung: this.fb.control(undefined, [
                requiredIfValidator(() => this.canSelectGrundimmunisierung()),
            ]),
            odi: this.fb.control(undefined, [Validators.required]),
            verantwortlich: this.fb.control(undefined, [Validators.required]),
            durchfuehrend: this.fb.control(undefined, [Validators.required]),
            impfstoff: this.fb.control(undefined,
                [Validators.required, this.createImpfstoffZulassungValidator(this, 'extern')]),
            lot: this.fb.control(undefined, [
                Validators.required,
                Validators.maxLength(DB_VMDL_SCHNITTSTELLE_LENGTH),
            ]),
            kein_fieber_keine_kontraindikation: this.fb.control(false, [Validators.required, Validators.requiredTrue]),
            keine_besonderen_umstaende: this.fb.control(false, [Validators.required, Validators.requiredTrue]),
            schwanger: this.fb.control(undefined, [requiredIfValidator(() => this.canSelectSchwanger())]),
            einwilligung: this.fb.control(false, [Validators.required, Validators.requiredTrue]),
            verabreichung_art: this.fb.control(undefined, [Validators.required]),
            verabreichung_ort: this.fb.control(undefined, [Validators.required]),
            verabreichung_ort_lr: this.fb.control(undefined, [Validators.required]),
            menge: this.fb.control(undefined, [
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.required, Validators.pattern(REGEX_IMPFMENGE),
            ]),
            bemerkung: this.fb.control(undefined, Validators.maxLength(MAX_LENGTH_TEXTAREA)),
            immunsupprimiert: this.fb.control(undefined, Validators.required),
            risikoreichesSexualleben: this.fb.control(undefined,
                [requiredIfValidator(() => this.canSelectAttributeFuerAffenpocken())]),
            impfungAusBeruflichenGruenden: this.fb.control(undefined,
                [requiredIfValidator(
                () => this.canSelectAttributeFuerAffenpocken())]),
            kontaktMitPersonAusRisikogruppe: this.fb.control(undefined,
                [requiredIfValidator(
                () => this.canSelectAttributeFuerAffenpocken())]),
            schnellschema: this.fb.control(undefined,
                requiredIfValidator(() => this.canSelectImpfschema())),
            zeckenstich: this.fb.control(undefined,
                [requiredIfValidator(() => this.canSelectAttributeFuerFSME())])
        });
        this.addContitionalValidatorForNachtragsdatum();
        this.subscribeNachtrag();
        this.subscribeExtern();
        this.subscribeSelbstzahlend();
        this.subscribeImmunsupprimiert();
        this.formGroup.controls.datumFallsNachtrag.updateValueAndValidity();
        this.selbstzahlendeOptions = SelectOptionsUtil.zahlungstypOptions(this.currentBaseData?.krankheitIdentifier);
        this.verabreichungArtOptions = SelectOptionsUtil.verabreichungsartOptions(
            this.vacmeSettingsService.getSupportedVerabreichungsarten(this.getKrankheit())
        );
    }

    public createImpfstoffZulassungValidator(
        component: {
            formGroup: UntypedFormGroup;
            getImpfstoff: (id: string | undefined) => ImpfstoffJaxTS | undefined;
        },
        externName: string,
    ): ValidatorFn {
        return (control): ValidationErrors | null => {
            const externCtrl = component.formGroup?.get(externName);
            const impfstoff = component.getImpfstoff(control.value);

            const isExtern = externCtrl?.value;

            return this.validateZulassung(isExtern, impfstoff);
        };
    }

    private validateZulassung(
        extern: boolean,
        impfstoff: ImpfstoffJaxTS | undefined,
    ): ValidationErrors | null {

        if (impfstoff) {
            switch (impfstoff.zulassungsStatus) {
                case ZulassungsStatusTS.ZUGELASSEN:
                case ZulassungsStatusTS.EMPFOHLEN:
                    return null;
                case ZulassungsStatusTS.EXTERN_ZUGELASSEN:
                    return extern ? null : {zulassung: 'nicht_zugelassen'};
                default:
                    return {zulassung: 'nicht_zugelassen'};
            }
        }

        return null;

    }

    // Die Validierung des Impfstoffs ist abhaengig vom nachtragen-Control, deshalb muss sie aufgefrischt werden
    private subscribeExtern(): void {
        const externCtrl = this.formGroup.get('extern');
        const impfstoffCtrl = this.formGroup.get('impfstoff');

        externCtrl?.valueChanges
            .pipe(takeUntil(this.ngUnsubscribe$))
            .subscribe(() => {
                impfstoffCtrl?.updateValueAndValidity();
            }, err => LOG.error(err));
    }

    // Bei der Auswahl von Nachtrag muss das Datum und das Extern-Flag zurueckgesetzt werden
    private subscribeNachtrag(): void {
        const nachtragCtrl = this.formGroup.get('impfungNachtragen');
        const datumErsteImpfungCtrl = this.formGroup.get('datumFallsNachtrag');
        const externCtrl = this.formGroup.get('extern');

        nachtragCtrl?.valueChanges
            .pipe(takeUntil(this.ngUnsubscribe$))
            .subscribe((value) => {
                datumErsteImpfungCtrl?.setValue(undefined);
                externCtrl?.setValue(false);
                this.setDurchfuehrendenOptionen(value);
                this.setVerantwortlichenOptionen(value);
                this.clearSelectedPersonsNoLongerInOptions();
            }, err => LOG.error(err));
    }

    private clearSelectedPersonsNoLongerInOptions(): void {
        const durchfuerendeCtrl = this.formGroup.get('durchfuehrend');
        const verantwortlicheCtrl = this.formGroup.get('verantwortlich');
        if (!this.durchfuehrendenOptions.some(option => option.benutzerId === durchfuerendeCtrl?.value?.benutzerId)) {
            durchfuerendeCtrl?.setValue(null);
        }
        if (!this.verantwortlichenOptions.some(option => option.benutzerId === verantwortlicheCtrl?.value?.benutzerId)) {
            verantwortlicheCtrl?.setValue(null);
        }
    }

    private addContitionalValidatorForNachtragsdatum(): void {
        // Der Validator fuer datumFallsNachtrag ist abhaengig vom Feld impfungNachtragen und
        // kann daher erst nach dem Erstellen der FormGroup hinzugefuegt werden
        const nachtragCheckboxCtrl = this.formGroup.get('impfungNachtragen');
        const datumErsteImpfungCtrl = this.formGroup.get('datumFallsNachtrag');

        if (nachtragCheckboxCtrl && datumErsteImpfungCtrl) {
            this.formGroup.controls.datumFallsNachtrag.setValidators([
                requiredIfValidator(() => {
                    return nachtragCheckboxCtrl.value
                        && nachtragCheckboxCtrl.value === NACHTRAG;
                }),
                Validators.minLength(4), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.pattern(DATE_PATTERN), parsableDateValidator(), datumInPastValidator(),
                minDateValidator(ValidationUtil.getMinDateForImpfung(this.getKrankheit())),
            ]);

            nachtragCheckboxCtrl.valueChanges
                .pipe(takeUntil(this.ngUnsubscribe$))
                .subscribe(() => {
                        if (nachtragCheckboxCtrl.value === NO_NACHTRAG) {
                            datumErsteImpfungCtrl.setValue(null);
                        }
                        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                        datumErsteImpfungCtrl.updateValueAndValidity();
                    },
                    error => {
                        LOG.error(error);
                    });
        }
    }

    public showFrageImpfungNachtragen(): boolean {
        return !!this.formGroup.get('impfungNachtragen');
    }

    public showNachtragen(): boolean {
        const nachtragen = this.getFormControlValue('impfungNachtragen');
        return nachtragen === NACHTRAG;
    }

    public getTranslatedKrankheit(): string {
        return this.translationService.instant(this.getKrankheit());
    }

    private getDurchfuehrendePerson(): void {
        // Durchfuehrenden Personen laden
        if (this.ortDerImpfungId !== null) {
            this.impfdokumentationService.impfdokumentationResourceGetDurchfuehrendeList(this.ortDerImpfungId)
                .subscribe(
                    durchfuehrendenPersonen => {
                        this.alleDurchfuehrendenOptions = durchfuehrendenPersonen;
                        this.setDurchfuehrendenOptionen(this.formGroup.get('impfungNachtragen')?.value);
                        const storedData = this.impfdokumentationCacheService.getImpfdokumentation(this.getKrankheit());
                        if (this.ortDerImpfungId === storedData?.ortDerImpfungId) {
                            const durchfuehrendeId = storedData.durchfuehrenderBenutzerId;
                            const durchfuehrendePerson = this.durchfuehrendenOptions.find(p => p.benutzerId === durchfuehrendeId);
                            if(durchfuehrendePerson) {
                                this.formGroup.get('durchfuehrend')?.setValue(durchfuehrendePerson);
                            }
                        }
                    },
                    error => {
                        LOG.error(error);
                    },
                );
        }
    }

    private setDurchfuehrendenOptionen(typ: string): void {
        if (typ === NO_NACHTRAG) {
            //Nur aktive Durchführende Benutzer
            this.durchfuehrendenOptions = this.alleDurchfuehrendenOptions.filter(p => p.deaktiviert === false);
        } else {
            //alle Durchführende Benutzer
            this.durchfuehrendenOptions = this.alleDurchfuehrendenOptions;
        }
    }

    private getVerantwortlichePerson(): void {
        // Verantwortlichen Personen laden
        if (this.ortDerImpfungId !== null) {
            this.impfdokumentationService.impfdokumentationResourceGetVerantwortlicheList(this.ortDerImpfungId)
                .subscribe(
                    verantwortlichenPersonen => {
                        this.alleVerantwortlichenOptions = verantwortlichenPersonen;
                        this.setVerantwortlichenOptionen(this.formGroup.get('impfungNachtragen')?.value);
                        const storedData = this.impfdokumentationCacheService.getImpfdokumentation(this.getKrankheit());
                        if (this.ortDerImpfungId === storedData?.ortDerImpfungId) {
                            const verantwortlicheId = storedData.verantwortlicherBenutzerId;
                            const verantwortlichePerson = this.verantwortlichenOptions.find(p => p.benutzerId === verantwortlicheId);
                            if(verantwortlichePerson){
                                this.formGroup.get('verantwortlich')?.setValue(verantwortlichePerson);
                            }
                        }
                    },
                    error => {
                        LOG.error(error);
                    },
                );
        }
    }

    private setVerantwortlichenOptionen(typ: string): void {
        if (typ === NO_NACHTRAG) {
            //Nur aktive Verantwortliche Benutzer
            this.verantwortlichenOptions = this.alleVerantwortlichenOptions.filter(p => p.deaktiviert === false);
        } else {
            //alle Verantwortliche Benutzer
            this.verantwortlichenOptions = this.alleVerantwortlichenOptions;
        }
    }

    private setOdis(data: OrtDerImpfungJaxTS[]): void {
        this.odiOptions = data.map((odi) => {
            let odiLabel = odi.name;
            let odiDisabled = false;
            if (odi.deaktiviert) {
                // Wenn der ODI nicht mehr aktiv ist: Im Label vermerken und fuer
                // nicht-FachBABs disablen
                const isFachBAB = this.authService.isOneOfRoles([TSRole.OI_IMPFVERANTWORTUNG]);
                if (!isFachBAB) {
                    odiDisabled = true;
                }
                odiLabel += ' ' + this.translationService.instant('OVERVIEW.ODI_INAKTIV');
            }
            return {label: odiLabel, value: odi.id, disabled: odiDisabled};
        });
        const odiIdToSelect = this.terminmanagementKontrolleImpfdokService.identifyOdiIdToSelect(data, this.getKrankheit());
        this.formGroup.get('odi')?.setValue(this.odiOptions.find(o => o.value === odiIdToSelect));
        this.odiSelectChange(odiIdToSelect);
    }

    private setImpfstoffOptions(data: ImpfstoffJaxTS[] | undefined): void {
        if (data) {
            this.impfstoffOptions = data.map((impfstoff: ImpfstoffJaxTS) => {
                return {
                    label: ImpfstoffUtil.createImpfstoffLabel(impfstoff, this.translationService),
                    value: impfstoff.id,
                    color: impfstoff.hexFarbe,
                    disabled: ImpfstoffUtil.isDisabledFuerImpfdok(impfstoff),
                    url: impfstoff.informationsLink,
                    eingestellt: impfstoff.eingestellt,
                };
            });
        } else {
            this.impfstoffOptions = [];
        }
    }

    public getImpfstoff(idStr: string | undefined): ImpfstoffJaxTS | undefined {
        if (this.currentBaseData?.impfstoffe) {
            for (const item of this.currentBaseData?.impfstoffe) {
                if (item.id === idStr) {
                    return item;
                }
            }
        }
        return undefined;
    }

    public getFormControlValue(field: string): any {
        return this.formGroup.get(field)?.value;
    }

    onNotVaccinated(): void {
        this.notVaccinated.emit();
    }

    showAufZweiteImpfungVerzichten(): boolean {
        if (!this.baseData?.accessOk) {
            return false;
        }
        if (this.canReset()) {
            return false; // erst nach dem Speichern koennen wir auf die 2. Impfung verzichten
        }

        const externGeimpft = this.currentBaseData?.dashboardJax?.externGeimpft;
        const impfstoff1 = this.getImpfstoff(this.getFormControlValue('impfstoff'));

        return this.impffolge === ImpffolgeTS.ERSTE_IMPFUNG
            && !!impfstoff1
            && ExternGeimpftUtil.needsZweitimpfung(impfstoff1, externGeimpft);
    }

    showEsKannNichtGeimpftWerden(): boolean {
        if (!this.baseData?.accessOk) {
            return false;
        }
        return this.impffolge === ImpffolgeTS.ZWEITE_IMPFUNG;
    }

    canReset(): boolean {
        return !this.saved;
    }

    isNotSameODI(): boolean {
        return this.terminUtilService.isNotSameODI(
            this.impffolge,
            this.getFormControlValue('odi')?.value,
            this.currentBaseData?.dashboardJax);
    }

    isNotSameODIWarnText(): string {
        switch (this.impffolge) { // haengt von der Impffolge welchen Termin wichtig ist
            case ImpffolgeTS.ERSTE_IMPFUNG:
                if (this.currentBaseData?.dashboardJax?.termin1?.impfslot?.ortDerImpfung?.id) { // Termin 1 hat ein ODI, das zurueckgeben
                    return this.translationService.instant('IMPFDOK.NOT_SAME_ODI.TERMIN1',
                        {i: this.currentBaseData?.dashboardJax?.termin1?.impfslot?.ortDerImpfung?.name});
                }
                // Kein Termin 1 definiert, dann gewuenschten ODI zurueckgeben
                return this.translationService.instant('IMPFDOK.NOT_SAME_ODI.GEWUENSCHTER_ODI',
                    {i: this.currentBaseData?.dashboardJax?.gewuenschterOrtDerImpfung?.name});
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                if (this.currentBaseData?.dashboardJax?.termin2?.impfslot?.ortDerImpfung?.id) { // Termin 2 hat ein ODI, das zurueckgeben
                    return this.translationService.instant('IMPFDOK.NOT_SAME_ODI.TERMIN2',
                        {i: this.currentBaseData?.dashboardJax?.termin2?.impfslot?.ortDerImpfung?.name});
                }
                // Kein Termin 2 definiert, dann gewuenschten ODI zurueckgeben
                return this.translationService.instant('IMPFDOK.NOT_SAME_ODI.GEWUENSCHTER_ODI',
                    {i: this.currentBaseData?.dashboardJax?.gewuenschterOrtDerImpfung?.name});
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                // Termin N hat ein ODI, das zurueckgeben
                if (this.currentBaseData?.dashboardJax?.terminNPending?.impfslot?.ortDerImpfung?.id) {
                    return this.translationService.instant('IMPFDOK.NOT_SAME_ODI.TERMIN',
                        {i: this.currentBaseData?.dashboardJax?.terminNPending?.impfslot?.ortDerImpfung?.name});
                }
                // Kein Termin 2 definiert, dann gewuenschten ODI zurueckgeben
                return this.translationService.instant('IMPFDOK.NOT_SAME_ODI.GEWUENSCHTER_ODI',
                    {i: this.currentBaseData?.dashboardJax?.gewuenschterOrtDerImpfung?.name});
        }
        return '';
    }

    private updateOdiInLocalStorage(odiOption: Option): void {
        if (odiOption.disabled) {
            // Wenn der ODI schon disabled war (d.h. fuer den aktuellen Benutzer nicht mehr auswaehlbar)
            // wird er auch nicht mehr im LocalStorage gespeichert
            this.impfdokumentationCacheService.removeOdiFromCache(this.getKrankheit());
        } else {
            this.impfdokumentationCacheService.cacheSelectedOdi(this.ortDerImpfungId as string,
                this.getKrankheit());
        }
    }

    toggleInfo($event: MouseEvent): void {
        $event.preventDefault();
        this.allergieInfoExpanded = !this.allergieInfoExpanded;
    }

    private updateImpfstoffColorStyle(color: string | null | undefined): void {
        const defaultCol = '#FFFFFF';
        if (color) {
            this.impfstoffBackgroundColor = color ? color : defaultCol;
        } else {
            this.impfstoffBackgroundColor = defaultCol;
        }
    }

    private updateImpfstoffLink(link?: string): void {
        this.impfstoffInformationsLink = link;
    }

    private validate(): void {
        if (this.currentBaseData) {
            this.currentBaseData.validate(
                this.currentBaseData?.dashboardJax,
                this.impffolge,
                this.ortDerImpfungId,
                this.getImpfstoff(this.getFormControlValue('impfstoff')),
                this.currentBaseData ? this.currentBaseData.odiList : [],
            );
        }
    }

    public openImpfstoffInformation(): void {
        window.open(this.impfstoffInformationsLink, '_blank');
    }

    private getImmunsupprimiertVorauswahl(): boolean | undefined {
        return this.currentBaseData?.dashboardJax?.immunsupprimiert;
    }

    private getSelbstzahlendeVorauswahl(): boolean | undefined {
        // Falls wir vorher auf der Kontrolle waren, ist das Feld schon gesetzt
        if (this.currentBaseData?.selbstzahlende !== undefined) {
            return this.currentBaseData?.selbstzahlende;
        }
        // Impfungen von Leistungserbringern sind immer selbst zu zahlen
        if (this.vacmeSettingsService.getKantonaleBerechtigung(this.getKrankheit())
            === KantonaleBerechtigungTS.LEISTUNGSERBRINGER) {
            return true;
        }
        // Falls die vorherige Impfung ein Nachtrag war, muessen wir aufgrund der Impffolge berechnen.
        // Den Impfschutz haben wir hier nicht. Sollte aber für diesen Spezialfall kein Problem sein.
        if (this.impffolge !== ImpffolgeTS.BOOSTER_IMPFUNG) {
            return false;
        }

        if (TenantUtil.BERN) {
            // Hier sind wir sowieso KONTROLLIERT_BOOSTER und koennen nicht mehr aufgrund des Status sagen,
            // ob wir nach EKIF freigegeben waren oder nicht
            // Wir koennen auch nicht auf das Flag auf der Registrierung schauen, da die Regeln unterdessen
            // geaendert haben koennten -> neu im Impfschutz nachschauen
            if (!!this.currentBaseData?.dashboardJax?.geburtsdatum
                && DateUtil.age(this.currentBaseData.dashboardJax.geburtsdatum)
                < 16) {
                return true;
            } else {
                return false;
            }
        }
        const freigegebenNaechsteImpfungAb: any =
            this.currentBaseData?.dashboardJax?.impfdossier?.impfschutzJax?.freigegebenNaechsteImpfungAb;
        if (freigegebenNaechsteImpfungAb && !DateUtil.isAfterToday(freigegebenNaechsteImpfungAb)) {
            return false;
        }

        // Default is now always selbstzahler
        return true;
    }

    private getGrundimmunisierungVorauswahl(): boolean | undefined {
        if (this.currentBaseData?.impffolge !== ImpffolgeTS.BOOSTER_IMPFUNG) {
            return true;
        }
        if (this.currentBaseData?.impffolgeNr === 1) {
            return true;
        }
        return undefined; // note that we always will send this.currentBaseData?.defaultGrundimmunisierung
        // to the server  if canSelectGrundimmunisierung is false (e.g. radiobutton for selection is not shown)
    }

    private subscribeSelbstzahlend(): void {
        this.formGroup.get('selbstzahlende')?.valueChanges.pipe(takeUntil(this.ngUnsubscribe$)).subscribe(value => {
            this.immunsupprimiertSelbstzahlendeValidierung(this.formGroup.get('immunsupprimiert')?.value, value);
        }, err => LOG.error(err));
    }

    private subscribeImmunsupprimiert(): void {
        this.formGroup.get('immunsupprimiert')?.valueChanges.pipe(takeUntil(this.ngUnsubscribe$)).subscribe(value => {
            if (value !== KEINE_ANGABE) {
                this.immunsupprimiertSelbstzahlendeValidierung(value, this.formGroup.get('selbstzahlende')?.value);
            }
        }, err => LOG.error(err));
    }

    private immunsupprimiertSelbstzahlendeValidierung(
        immunsupprimiert?: boolean | undefined,
        selbstzahlend?: boolean | undefined,
    ): void {
        // TODO Affenpocken: Only validate selbstzahlende fuer COVID, what are the Rules for Affenpocken?
        // Or could they be generalized?
        if (this.currentBaseData?.krankheitIdentifier !== KrankheitIdentifierTS.COVID) {
            return;
        }

        if (this.impffolge !== ImpffolgeTS.BOOSTER_IMPFUNG) {
            return;
        }

        if (immunsupprimiert === true && selbstzahlend) {
            this.showImmunsupprimiertSelbstzahlendeWarnung('IMMUNSUPPRIMIERT');
        }
    }

    private showImmunsupprimiertSelbstzahlendeWarnung(key: string): void {
        Swal.fire({
            icon: 'warning',
            text: this.translationService.instant('IMPFDOK.SELBSTZAHLENDE_WARNUNG.' + key,
                {vorname: this.baseData?.dashboardJax?.vorname, name: this.baseData?.dashboardJax?.name}),
            showConfirmButton: true,
        });
    }

    public getKrankheit(): KrankheitIdentifierTS {
        if (this.currentBaseData === undefined || this.currentBaseData.krankheitIdentifier === undefined) {
            throw new Error('Krankheit nicht gesetzt');
        }
        return this.currentBaseData.krankheitIdentifier;
    }

    private fillFormWithCachedValuesAndValuesFromModel(): void {
        const impfdokCacheModel: ImpfdokumentationCache | undefined = this.impfdokumentationCacheService.getImpfdokumentation(
            this.getKrankheit());
        const odiId = impfdokCacheModel?.ortDerImpfungId;
        const durchfuehrendeId = impfdokCacheModel?.durchfuehrenderBenutzerId;
        const durchfuehrendePerson = this.durchfuehrendenOptions.find(p => p.benutzerId === durchfuehrendeId);
        const verantwortlicheId = impfdokCacheModel?.verantwortlicherBenutzerId;
        const verantwortlichePerson = this.verantwortlichenOptions.find(p => p.benutzerId === verantwortlicheId);
        const odi = this.odiOptions.find(o => o.value === odiId);
        const lotAndMenge = impfdokCacheModel?.impfstoff ?
            this.impfdokumentationCacheService.getLotAndMengeForImpfstoff(this.getKrankheit(),
                impfdokCacheModel?.impfstoff) :
            undefined;

        const impfstoffId = impfdokCacheModel?.impfstoff ?? null;
        this.setFormControlValue('krankheitIdentifier', this.currentBaseData?.krankheitIdentifier);
        this.setFormControlValue('selbstzahlende', this.getSelbstzahlendeVorauswahl());
        this.setFormControlValue('odi', odi);
        this.setFormControlValue('verantwortlich', verantwortlichePerson);
        this.setFormControlValue('durchfuehrend', durchfuehrendePerson);
        this.setFormControlValue('impfstoff', impfstoffId);
        this.setFormControlValue('lot', lotAndMenge?.lot);
        this.setFormControlValue('verabreichung_art', this.readValidVerabreichunsartFromCacheModel(impfdokCacheModel));
        this.setFormControlValue('verabreichung_ort', impfdokCacheModel?.verarbreichungsort ?? null);
        this.setFormControlValue('verabreichung_ort_lr', impfdokCacheModel?.verarbreichungsseite ?? null);
        this.setFormControlValue('menge', lotAndMenge?.menge);
        this.setFormControlValue('immunsupprimiert', this.getImmunsupprimiertVorauswahl());
        this.setFormControlValue('grundimmunisierung', this.getGrundimmunisierungVorauswahl());
        this.verarbreichungsSeiteSelectedVal = impfdokCacheModel?.verarbreichungsseite;
    }

    // this code was added to make sure that cached values are still valid, there could have been changes in the meantime
    private readValidVerabreichunsartFromCacheModel(impfdokCacheModel: ImpfdokumentationCache | undefined): string | null {
        const allowedArtInCache = this.vacmeSettingsService.getSupportedVerabreichungsarten(this.getKrankheit()).some(v => {
            return v === impfdokCacheModel?.verarbreichungsart;
        });
        if (allowedArtInCache) {
            return impfdokCacheModel?.verarbreichungsart ?? null;
        }
        return null; // it not valid, return no cached value
    }

    private setFormControlValue(field: string, value: any): void {
        this.formGroup.get(field)?.setValue(value);
    }

    public showSchnellschema(): boolean {
        return ExternGeimpftUtil.showSchnellschema(this.getKrankheit());
    }
}
