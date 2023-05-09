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

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import moment from 'moment/moment';
import {Observable, of} from 'rxjs';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    DashboardJaxTS,
    DossierService,
    ErkrankungDatumHerkunftTS,
    ErkrankungJaxTS,
    KrankheitIdentifierTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {
    DATE_FORMAT,
    DATE_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    MIN_DATE_FOR_POSITIV_GETESTET,
} from '../../../../../vacme-web-shared/src/lib/constants';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {TerminfindungResetService} from '../../../../../vacme-web-shared/src/lib/service/terminfindung-reset.service';
import {VacmeSettingsService} from '../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {ConfirmUtil} from '../../../../../vacme-web-shared/src/lib/util/confirm-util';
import {
    datumInPastValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/datum-in-past-validator';
import {
    equalErkrankungDateValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/equal-erkrankung-date-validator';
import {minDateValidator} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/min-date-validator';
import {
    parsableDateValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {ErkrankungUtil} from '../../../../../vacme-web-shared/src/lib/util/erkrankung-util';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {FreigabeEntzugUtil} from '../../../../../vacme-web-shared/src/lib/util/freigabe-entzug-util';
import {CanComponentDeactivateSimple} from '../../../../../vacme-web/src/app/service/unsaved-changes-guard.service';
import {NavigationService} from '../../service/navigation.service';

const LOG = LogFactory.createLog('ErkrankungPageComponent');

@Component({
    templateUrl: './erkrankung-page.component.html',
    styleUrls: ['./erkrankung-page.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ErkrankungPageComponent extends BaseDestroyableComponent implements OnInit, CanComponentDeactivateSimple {

    public dashboard!: DashboardJaxTS;
    public formGroup!: FormGroup;
    private saveRequestPending = false;

    constructor(
        public route: ActivatedRoute,
        private router: Router,
        private dossierService: DossierService,
        private fb: FormBuilder,
        private translateService: TranslateService,
        private terminfindungResetService: TerminfindungResetService,
        private errorMessageService: ErrorMessageService,
        private navigationService: NavigationService,
        private vacmeSettingsService: VacmeSettingsService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.route.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(next => {
                this.dashboard = next.dossier;
                const erkrankungen = this.dashboard.impfdossier?.erkrankungen || [];
                this.formGroup = this.fb.group({
                    erkrankungen: this.fb.array(erkrankungen.map((
                        erkrankung: ErkrankungJaxTS,
                        index: number,
                    ) => this.createErkrankungFormgroup(
                        erkrankung,
                        index))),
                });
                if (!this.canEditErkrankungen()) {
                    this.formGroup.disable();
                }
            }, error => {
                LOG.error(error);
            });
    }

    canEditErkrankungen(): boolean {
        return ErkrankungUtil.canEditErkrankungen(this.dashboard?.status);
    }

    createErkrankungFormgroup(erkrankung: ErkrankungJaxTS, index: number): FormGroup {
        const erkFormGrp = this.fb.group({
            date: this.fb.control(DateUtil.dateAsLocalDateString(erkrankung.date), [
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.pattern(DATE_PATTERN),
                Validators.required,
                parsableDateValidator(),
                datumInPastValidator(),
                minDateValidator(moment(MIN_DATE_FOR_POSITIV_GETESTET, 'DD.MM.YYYY').toDate()),
                equalErkrankungDateValidator(index, () => this.getErkrankungen()),
            ]),
            erkrankungdatumHerkunft: this.fb.control(erkrankung.erkrankungdatumHerkunft),
        });
        if (!ErkrankungUtil.isEditableErkrankungByImpfling(erkrankung)) {
            erkFormGrp.disable();
        }
        this.addValueChangeListenersForDependentValidation(erkFormGrp);
        return erkFormGrp;
    }

    private getKrankheit(): KrankheitIdentifierTS {
        if (this.dashboard.krankheitIdentifier === undefined || this.dashboard.krankheitIdentifier === null) {
            this.errorMessageService.addMesageAsError('KRANKHEIT NICHT GESETZT');
            throw new Error('Krankheit nicht gesetzt ' + this.dashboard.registrierungsnummer);
        }
        return this.dashboard.krankheitIdentifier;
    }

    /**
     * adds a listener that checks if the input is a valid date and if it is trigger a validation against all other
     * fields
     */
    private addValueChangeListenersForDependentValidation(erkFormGrp: FormGroup): void {
        erkFormGrp?.valueChanges
            .pipe(this.takeUntilDestroyed())
            .subscribe(value => {
                const parsedDate = moment(value.date, DATE_FORMAT, true);
                if (parsedDate.isValid()) {
                    this.reValidateAllControlsExceptPassed(erkFormGrp);
                }
            }, err => LOG.error(err));
    }

    private getErkrankungen(): Array<ErkrankungJaxTS> {
        if (this.formGroup) {
            const erkrankungenFormArray = this.getErkrankungenFormArray();
            if (erkrankungenFormArray) {
                return erkrankungenFormArray.getRawValue();
            }
        }
        return this.dashboard.impfdossier?.erkrankungen || [];

    }

    getErkrankungenFormArray(): FormArray | undefined {
        return this.formGroup.get('erkrankungen') as FormArray;
    }

    getErkrankungFormGroup(erkrankungFormGroup: any): FormGroup {
        return erkrankungFormGroup as FormGroup;
    }

    canAdd(): boolean {
        return this.canEditErkrankungen()
            && !!this.getErkrankungenFormArray()
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            && this.getErkrankungenFormArray()!.controls.length < 10; // limit the list
    }

    canDelete(erkrankungFormGroup: FormGroup): boolean {
        return this.canEditErkrankungen() && this.isEditableErkrankung(erkrankungFormGroup.value);
    }

    add(): void {
        const newErk = {erkrankungdatumHerkunft: ErkrankungDatumHerkunftTS.ERFASST_IMPFLING} as ErkrankungJaxTS;
        this.addBasic(newErk);
    }

    private addBasic(erkrankung: ErkrankungJaxTS): void {
        const erkrankungArray = this.formGroup.get('erkrankungen') as FormArray;
        const erkrankungFormgroup = this.createErkrankungFormgroup(erkrankung, erkrankungArray.length);
        erkrankungArray.push(erkrankungFormgroup);
        erkrankungArray.markAsDirty();
        erkrankungArray.markAsTouched();
    }

    public deleteErkrankung(i: number): void {
        const erkrankungArray = this.formGroup.get('erkrankungen') as FormArray;
        erkrankungArray.removeAt(i);
        erkrankungArray.markAsDirty();
        erkrankungArray.markAsTouched();
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.save();
        }, () => {
            // Wenn die Liste leer ist ausser einer disabled Erkrankung-FormGroup, ist die FormGroup disabled statt
            // valid
            if (this.formGroup.disabled) {
                this.save();
            }
            // Sonst ist sie invalid oder pending, dann speichern wir nicht, sondern zeigen die Warnung an
        });
    }

    public cancel(): void {
        this.navigationService.navigateToDossierDetail(
            this.dashboard.registrierungsnummer,
            this.getKrankheit());
    }

    private save(): void {
        if (this.saveRequestPending) {
            LOG.warn('Saverequest ist already pending. Not doing anything');
            return;
        }
        const dashboardVorher = this.dashboard;
        const load = this.prepareLoadToSave();

        this.saveRequestPending = true;

        // Testweise ausfueehren und schauen, was passieren wuerde
        this.dossierService.dossierResourceRegUpdateErkrankungen(load.regNr, true, load.data)
            .subscribe(
                dashboardReloadedTest => {

                    // vergleichen, ob beim Speichern der Termin oder die Odiwahl geloescht werden wuerde
                    const canProceed$ = this.warnBeforeFinalSaveIfNeeded$(dashboardVorher, dashboardReloadedTest);
                    canProceed$.subscribe(canProceed => {
                        if (canProceed) {
                            // Wirklich speichern
                            this.dossierService.dossierResourceRegUpdateErkrankungen(load.regNr,
                                false, load.data).subscribe(
                                dashboardReloaded => {
                                    this.doAfterSaved(dashboardVorher, dashboardReloaded, load);
                                }, error => this.onSaveError(error));
                        } else {
                            this.saveRequestPending = false;
                        }
                    }, error => this.onSaveError(error));
                }, error => this.onSaveError(error));
    }

    private prepareLoadToSave(): { data: ErkrankungJaxTS[] | undefined; regNr: string } {
        const data = this.getErkrankungenFormArray()?.controls
            .filter(control => control.get('erkrankungdatumHerkunft')?.value
                === ErkrankungDatumHerkunftTS.ERFASST_IMPFLING)
            .map(erkrankungFormGroup => {
                const enteredDate = DateUtil.parseDateAsMidday(erkrankungFormGroup.get('date')?.value);
                const mappedErkrankungJaxTs: ErkrankungJaxTS = {
                    date: enteredDate,
                    erkrankungdatumHerkunft: erkrankungFormGroup.get('erkrankungdatumHerkunft')?.value,
                };
                return mappedErkrankungJaxTs;
            });

        console.assert(!!this.dashboard && !!this.dashboard.registrierungsnummer);
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        const regNr: string = this.dashboard.registrierungsnummer!;

        return {data, regNr};
    }

    private warnBeforeFinalSaveIfNeeded$(
        dashboardVorher: DashboardJaxTS,
        dashboardReloadedTest: DashboardJaxTS,
    ): Observable<any> {
        // vergleichen, ob beim Speichern der Termin oder die Odiwahl geloescht werden wuerde
        const willLoseOdi = FreigabeEntzugUtil.lostOdi(dashboardVorher, dashboardReloadedTest);
        const willLoseNichtVerwalteterOdi = FreigabeEntzugUtil.lostNichtVerwalteterOdi(dashboardVorher,
            dashboardReloadedTest);
        const willLoseTermine = FreigabeEntzugUtil.lostTermin(dashboardVorher, dashboardReloadedTest);

        if ((willLoseOdi || willLoseNichtVerwalteterOdi || willLoseTermine)) {
            // Bestaetigungsdialog oeffnen, wenn Termine/Odiwahl geloescht werden
            let message = '';
            if (willLoseOdi) {
                message = 'WILL_LOSE_ODI';
            } else if (willLoseTermine) {
                message = 'WILL_LOSE_TERMINE';
            } else if (willLoseNichtVerwalteterOdi) {
                message = 'WILL_LOSE_ODI_NICHT_VERWALTET';
            }
            const msgPrefix = 'ERKRANKUNGEN.' + message + '.';
            return ConfirmUtil.swalAsObservable$(
                Swal.fire({
                    icon: 'warning',
                    text: this.translateService.instant(msgPrefix + 'CONFIRM_QUESTION', {
                        odi: dashboardVorher.gewuenschterOrtDerImpfung?.name,
                    }),
                    showCancelButton: true,
                    cancelButtonText: this.translateService.instant(msgPrefix + 'CANCEL'),
                    confirmButtonText: this.translateService.instant(msgPrefix + 'CONFIRM'),
                }));
        } else {
            // es passiert nichts Schlimmes, kein Dialog noetig
            return of(true);
        }
    }

    private doAfterSaved(
        dashboardVorher: DashboardJaxTS,
        dashboardReloaded: DashboardJaxTS,
        load: { data: ErkrankungJaxTS[] | undefined; regNr: string },
    ): void {
        // Abschlussdialog, wenn noetig
        this.informUserAboutLostFreigabe(dashboardVorher, dashboardReloaded);
        // aufraeumen
        this.saveRequestPending = false;
        this.formGroup = this.fb.group({}); // damit man keine unsaved changes warnung bekommt
        this.terminfindungResetService.resetData();

        // zurueck zum Start
        this.navigationService.navigateToDossierDetail(
            dashboardReloaded.registrierungsnummer,
            this.getKrankheit());
    }

    private onSaveError(err: any): void {
        LOG.error('HTTP Error', err);
        this.saveRequestPending = false;
    }

    canDeactivate(): boolean {
        return !this.formGroup.dirty;
    }

    private informUserAboutLostFreigabe(dashboardVorher: DashboardJaxTS, dashboardReloaded: DashboardJaxTS): void {

        // Termin wurde annulliert
        if (FreigabeEntzugUtil.lostTermin(dashboardVorher, dashboardReloaded)) {
            this.informUser('ERKRANKUNGEN.TERMIN_ENTFERNT', undefined);
            return;
        }
        // Odiwahl wurde annulliert
        if (FreigabeEntzugUtil.lostOdi(dashboardVorher, dashboardReloaded)) {
            this.informUser('ERKRANKUNGEN.ODIWAHL_ENTFERNT', {
                odi: dashboardVorher.gewuenschterOrtDerImpfung?.name,
            });
            return;
        }
        // Nicht-verwalteter ODI wurde annuliert
        if (FreigabeEntzugUtil.lostNichtVerwalteterOdi(dashboardVorher, dashboardReloaded)) {
            this.informUser('ERKRANKUNGEN.ODIWAHL_ENTFERNT_NICHT_VERWALTET', undefined);
            return;
        }

        // Freigabe wurde entfernt, ohne dass bereits Termin oder Odi gewaehlt war
        if (FreigabeEntzugUtil.lostFreigabe(dashboardVorher, dashboardReloaded)) {
            if (this.vacmeSettingsService.supportsTerminFreigabeSMS(this.getKrankheit())) {
                this.informUser('ERKRANKUNGEN.FREIGABE_ENTFERNT_WITH_BENACHRICHTIGUNG', undefined);
            } else {
                this.informUser('ERKRANKUNGEN.FREIGABE_ENTFERNT', undefined);
            }
            return;
        }
    }

    private informUser(messageKey: string, translateParams: object | undefined): void {
        const message = this.translateService.instant(messageKey, translateParams);

        Swal.fire({
            icon: 'info',
            text: message,
            showConfirmButton: true,
            showCancelButton: false,
        });
        return;
    }

    public isEditableErkrankung(value: { erkrankungdatumHerkunft: ErkrankungDatumHerkunftTS }): boolean {
        return ErkrankungUtil.isEditableErkrankungHerkunftByImpfling(value.erkrankungdatumHerkunft);
    }

    public getErkrankungHintKey(erkrankungFormGroup: FormGroup): string {
        return 'ERKRANKUNGEN.DATE_HINT.' + (erkrankungFormGroup.value as ErkrankungJaxTS).erkrankungdatumHerkunft;
    }

    public reValidateAllControlsExceptPassed(erkFormGrp: FormGroup): void {
        this.getErkrankungenFormArray()?.controls.forEach(control => {
            if (erkFormGrp === control) {
                return;
            }
            // Nur vorher Fehlerhafte felder re-validieren. Somit verhindern wir, den Fehler bei beiden Felder mit dem
            // gleichen Datum anzuzeigen.
            if (control.get('date')?.hasError('equalErkrankungDate')) {
                control.get('date')?.updateValueAndValidity();
            }
        });
    }
}
