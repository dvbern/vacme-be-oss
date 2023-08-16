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

import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {UntypedFormArray, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {Observable, of} from 'rxjs';
import {SweetAlertOptions} from 'sweetalert2';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    ImpffolgeTS,
    ImpfslotDisplayDayJaxTS,
    ImpfslotDisplayJaxTS,
    ImpfslotService,
    KrankheitIdentifierTS,
    KrankheitJaxTS,
    TerminbuchungService,
    TermineAbsagenJaxTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {
    BaseDestroyableComponent,
} from '../../../../../vacme-web-shared';
import {
    NBR_IMPFSLOT_PER_DAY,
    REGEX_NUMBER_INT,
    TERMINSLOTS_MAX_PER_DAY,
} from '../../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';
import LayoutUtil from '../../../../../vacme-web-shared/src/lib/util/LayoutUtil';
import TenantUtil from '../../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {CanComponentDeactivate} from '../../service/termin-guard.service';

const LOG = LogFactory.createLog('TerminverwaltungPageComponent');

@Component({
    selector: 'app-terminverwaltung-page',
    templateUrl: './terminverwaltung-page.component.html',
    styleUrls: ['./terminverwaltung-page.component.scss'],
})
export class TerminverwaltungPageComponent extends BaseDestroyableComponent implements OnInit, CanComponentDeactivate {

    constructor(
        private fb: UntypedFormBuilder,
        private router: Router,
        private activeRoute: ActivatedRoute,
        private impfslotsService: ImpfslotService,
        private translate: TranslateService,
        private changeDetector: ChangeDetectorRef,
        private terminbuchungService: TerminbuchungService,
        private authService: AuthServiceRsService,
        private vacmeSettingsService: VacmeSettingsService,
        private impfslotService: ImpfslotService
    ) {
        super();
    }

    formGroup!: UntypedFormGroup;
    rows: ImpfslotDisplayDayJaxTS[] = [];
    selectedODIId: string | null = null;
    selectedJahr = 0;
    selectedMonat = 0;
    loaded = false;
    monthYearCombination: Array<{ month: number; year: number }> = [];
    impffolge = ImpffolgeTS;
    maxRowLength = 0; // some rows are longer than others! We need this number for the tabindex and arrow navigation
    hasMonatEveryImpfslot = true;

    verfuegbareKrankheiten: Array<KrankheitIdentifierTS> = [];
    COVID: KrankheitIdentifierTS = KrankheitIdentifierTS.COVID;
    AFFENPOCKEN: KrankheitIdentifierTS = KrankheitIdentifierTS.AFFENPOCKEN;

    routeId?: string;
    routeJahr?: number;
    routeMonat?: number;

    ERSTEIMPFUNG: ImpffolgeTS = ImpffolgeTS.ERSTE_IMPFUNG;
    ZWEITEIMPFUNG: ImpffolgeTS = ImpffolgeTS.ZWEITE_IMPFUNG;
    BOOSTERIMPFUNG: ImpffolgeTS = ImpffolgeTS.BOOSTER_IMPFUNG;

    // Total rows a day can contain with all Krankheit specific row numbers from verfuegbareKrankheiten
    totalRowsPerDayCache = 0;
    // Holds the number of rows above the given krankheit in a day.
    previousKrankheitRowCountCache: { [key: string]: number } = {};
    // Holds the number of rows of the given Krankheit
    krankheitRowCountCache: { [key: string]: number } = {};


    ngOnInit(): void {
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(data => {
                this.routeId = data.data.id;
                this.routeJahr = data.data.jahr;
                this.routeMonat = data.data.monat;
                this.verfuegbareKrankheiten = data.odi.krankheiten.map((krankheit: KrankheitJaxTS) => krankheit.identifier);
                this.calculateKrankheitRowCaches();
                this.reload();
            }, error => {
                LOG.error(error);
            });
    }

    private calculateKrankheitRowCaches(): void {
        // Reset variables as this caused problems when switching between Odis with different Krankheiten
        this.totalRowsPerDayCache = 0;
        this.previousKrankheitRowCountCache = {};
        this.krankheitRowCountCache = {};
        // We cache these settings in hope of making the getTabIndex method more performant.
        for (const krankheit of this.verfuegbareKrankheiten) {
            this.previousKrankheitRowCountCache[krankheit] = this.totalRowsPerDayCache;
            // For a ImpffolgeEinsUndZwei krankheit there are 3 rows added each day, otherwise just 1.
            const krankheitRowCount = this.vacmeSettingsService.supportsImpffolgenEinsUndZwei(krankheit) ? 3 : 1;
            this.krankheitRowCountCache[krankheit] = krankheitRowCount;
            this.totalRowsPerDayCache += krankheitRowCount;
        }
    }

    private reload(): void {
        if (!this.routeId || !this.routeJahr || !this.routeMonat) {
            return;
        }

        this.loaded = false;
        this.loadImpfslots$(this.routeId, this.routeJahr, this.routeMonat).pipe().subscribe(impfslots => {

            this.rows = impfslots;
            if (this.rows) {
                this.createForm();
                this.selectedODIId = this.activeRoute.snapshot.paramMap.get('ortDerImpfungId');
                this.selectedJahr = +(this.activeRoute.snapshot.paramMap.get('jahr') as string);
                this.selectedMonat = +(this.activeRoute.snapshot.paramMap.get('monat') as string);
                this.updateMonthYearCombinations();
                this.loaded = true;
            }
            this.hasMonatEveryImpfslot = this.calculateHasMonatEveryImpfslot();
        }, error => {
            LOG.error(error);
            this.loaded = true;
        });
    }

    private createForm(): void {
        this.formGroup = this.fb.group(
            {tableRowArray: this.fb.array(this.createTableRow())},
        );
        this.changeDetector.detectChanges();
    }

    private createTableRow(): UntypedFormGroup[] {
        const result = [];
        this.maxRowLength = 0;
        for (const row of this.rows) {
            const rowGroup = this.fb.group({
                day: this.fb.control(row.day),
                krankheitImpfslotDisplayJaxMap: this.fb.group({}),
            });

            for (const krankheit of this.verfuegbareKrankheiten) {
                (rowGroup.get('krankheitImpfslotDisplayJaxMap') as UntypedFormGroup).addControl(krankheit,
                    this.fb.array(this.createRowCell(row, krankheit)));
            }

            result.push(rowGroup);
        }
        return result;
    }

    private createRowCell(row: ImpfslotDisplayDayJaxTS, krankheit: KrankheitIdentifierTS): UntypedFormGroup[] {
        const result = [];
        if (row.krankheitImpfslotDisplayJaxMap && row.krankheitImpfslotDisplayJaxMap[krankheit]) {
            const impfslots = row.krankheitImpfslotDisplayJaxMap[krankheit];
            this.maxRowLength = Math.max(this.maxRowLength, impfslots.length);
            const rowIsDisabled = !DateUtil.isAfterToday(row.day);
            for (const cell of impfslots) {
                const cellGroup = this.fb.group({
                    id: this.fb.control(cell.id),
                    zeitfenster: this.fb.group({
                        von: this.fb.control(cell.zeitfenster?.von),
                        bis: this.fb.control(cell.zeitfenster?.bis),
                    }),
                    kapazitaetBoosterImpfung: this.fb.control(
                        {value: cell.kapazitaetBoosterImpfung, disabled: rowIsDisabled},
                        [Validators.pattern(REGEX_NUMBER_INT), Validators.max(TERMINSLOTS_MAX_PER_DAY)]),
                });

                if (this.vacmeSettingsService.supportsImpffolgenEinsUndZwei(krankheit)) {
                    cellGroup.addControl('kapazitaetErsteImpfung', this.fb.control(
                        {value: cell.kapazitaetErsteImpfung, disabled: rowIsDisabled},
                        [Validators.pattern(REGEX_NUMBER_INT), Validators.max(TERMINSLOTS_MAX_PER_DAY)]));
                    cellGroup.addControl('kapazitaetZweiteImpfung', this.fb.control(
                        {value: cell.kapazitaetZweiteImpfung, disabled: rowIsDisabled},
                        [Validators.pattern(REGEX_NUMBER_INT), Validators.max(TERMINSLOTS_MAX_PER_DAY)]));
                }

                result.push(cellGroup);
            }
        }

        return result;
    }

    public getTabIndex(rowIndex: number, krankheit: KrankheitIdentifierTS, timeIndex: number, impffolge: ImpffolgeTS): number {
        // We start at 1
        let index = 1;

        // Move to the current day by multiplying all previoius days by the max width of a day and the number of
        // rows in a day
        index += rowIndex * this.maxRowLength * this.totalRowsPerDayCache;

        // Within the day, we need to move down a few more rows depending on how many rows the previous krankheiten occupy
        index += this.maxRowLength * this.previousKrankheitRowCountCache[krankheit];


        // Within the krankheit we potentially need to move down some more depending on the Impffolge
        const krankheitSupportsEinsUndZwei = this.vacmeSettingsService.supportsImpffolgenEinsUndZwei(krankheit);
        const rowsToSkipByImpffolge = impffolge === ImpffolgeTS.BOOSTER_IMPFUNG ?
            2 : impffolge === ImpffolgeTS.ZWEITE_IMPFUNG ? 1 : 0;
        const rowsToSkipByKrankheitAndImpffolge = krankheitSupportsEinsUndZwei ? rowsToSkipByImpffolge : 0;

        index += this.maxRowLength * rowsToSkipByKrankheitAndImpffolge;

        // Finally move right to the correct cell by adding the timeIndex
        index += timeIndex;
        return index;
    }

    public supportsImpffolgeEinsUndZwei(krankheit: KrankheitIdentifierTS): boolean {
        return this.vacmeSettingsService.supportsImpffolgenEinsUndZwei(krankheit);
    }

    public isOfftime(row: ImpfslotDisplayDayJaxTS, vonDisplay: string): boolean {
        // weekends
        if (this.isWeekend(row.day)) {
            return true;
        }
        // night & lunchtime
        const column = this.getColumnAt(vonDisplay);
        return column ? column.offtime : true; // columns not found -> offtime as well
    }

    getColumnAt(vonDisplay: string): any {
        return this.getColumns().find(value => vonDisplay === value.von);
    }

    getColumns(): any[] {

        return [
            {von: '06:00', bis: '06:30', offtime: true},
            {von: '06:30', bis: '07:00', offtime: true},
            {von: '07:00', bis: '07:30', offtime: true},
            {von: '07:30', bis: '08:00', offtime: true},
            {von: '08:00', bis: '08:30', offtime: false},
            {von: '08:30', bis: '09:00', offtime: false},
            {von: '09:00', bis: '09:30', offtime: false},
            {von: '09:30', bis: '10:00', offtime: false},
            {von: '10:00', bis: '10:30', offtime: false},
            {von: '10:30', bis: '11:00', offtime: false},
            {von: '11:00', bis: '11:30', offtime: false},
            {von: '11:30', bis: '12:00', offtime: false},
            {von: '12:00', bis: '12:30', offtime: true},
            {von: '12:30', bis: '13:00', offtime: true},
            {von: '13:00', bis: '13:30', offtime: true},
            {von: '13:30', bis: '14:00', offtime: true},
            {von: '14:00', bis: '14:30', offtime: false},
            {von: '14:30', bis: '15:00', offtime: false},
            {von: '15:00', bis: '15:30', offtime: false},
            {von: '15:30', bis: '16:00', offtime: false},
            {von: '16:00', bis: '16:30', offtime: false},
            {von: '16:30', bis: '17:00', offtime: false},
            {von: '17:00', bis: '17:30', offtime: false},
            {von: '17:30', bis: '18:00', offtime: false},
            {von: '18:00', bis: '18:30', offtime: true},
            {von: '18:30', bis: '19:00', offtime: true},
            {von: '19:00', bis: '19:30', offtime: true},
            {von: '19:30', bis: '20:00', offtime: true},
            {von: '20:00', bis: '20:30', offtime: true},
            {von: '20:30', bis: '21:00', offtime: true},
            {von: '21:00', bis: '21:30', offtime: true},
            {von: '21:30', bis: '22:00', offtime: true},
        ];

    }

    getRows(): Array<ImpfslotDisplayDayJaxTS> {
        return this.rows;
    }

    public getImpfslotArrayOfDayAndKrankheit(rowIndex: number, krankheit: KrankheitIdentifierTS): Array<ImpfslotDisplayJaxTS> | undefined {
        return this.rows[rowIndex].krankheitImpfslotDisplayJaxMap?.[krankheit];
    }

    isWeekend(date: Date | undefined): boolean {
        if (!date) {
            return false;
        }
        switch (date.getDay()) {
            case 0:
            case 6:
                return true;
        }
        return false;
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.save();
        });
    }

    save(): void {
        this.loaded = false;
        const parameters: Array<ImpfslotDisplayJaxTS> = [];
        for (const row of this.formGroup.value.tableRowArray as Array<ImpfslotDisplayDayJaxTS>) {
            const rowIsDisabled = !DateUtil.isAfterToday(row.day);
            if (rowIsDisabled) {
                continue;
            }
            for (const krankheit of this.verfuegbareKrankheiten) {
                if (!row.krankheitImpfslotDisplayJaxMap?.[krankheit]) {
                    continue;
                }
                // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain, @typescript-eslint/no-non-null-assertion
                for (const slot of row.krankheitImpfslotDisplayJaxMap?.[krankheit]!) {
                    // Wir moechten keine leeren Zellen haben, also schreiben wir 0 hin, wenn die Zelle leer ist.
                    slot.kapazitaetErsteImpfung = slot.kapazitaetErsteImpfung || 0;
                    slot.kapazitaetZweiteImpfung = slot.kapazitaetZweiteImpfung || 0;
                    slot.kapazitaetBoosterImpfung = slot.kapazitaetBoosterImpfung || 0;
                    parameters.push(slot);
                }
            }
        }
        this.impfslotsService.impfslotResourceUpdateImpfslot(parameters).subscribe(
            () => {
                this.reload();
                this.validateEnoughZweittermineOrOk();
            },
            error => {
                this.loaded = true;
                LOG.error(error);
            },
        );
    }

    private validateEnoughZweittermineOrOk(): void {
        if (!this.routeId || !this.routeJahr || !this.routeMonat) {
            return;
        }
        // at midday to avoid timezone issues
        const mStart = DateUtil.firstDayOfMonth(this.routeMonat).year(this.routeJahr).hour(12);
        const mEnd = DateUtil.lastDayOfMonth(this.routeMonat).year(this.routeJahr).hour(12);

        this.impfslotsService.impfslotResourceValidateImpfslotsByOdiBetween(this.routeId, mEnd.toDate(), mStart.toDate())
            .pipe().subscribe(results => {
            let swalOptions: SweetAlertOptions;
            if (results.length > 0) {
                // Zweittermin-Validierungs-Warnungen
                // noinspection MagicNumberJS
                const maxWarnings = 15;
                const message = results.slice(0, maxWarnings).reduce((text, result) =>
                    text +
                    this.translate.instant('FACH-APP.ODI.TERMINVERWALTUNG.VALIDIERUNG_ZEILE', result) +
                    '<br>', '');
                swalOptions = {
                    icon: 'warning',
                    title: this.translate.instant('FACH-APP.ODI.TERMINVERWALTUNG.VALIDIERUNG_TITEL',
                        {desiredDays: this.vacmeSettingsService.distanceImpfungenDesired}),
                    html: message,
                    showConfirmButton: true,
                };
            } else {
                // Kein Problem, alles gut
                swalOptions = {
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false,
                };
            }

            // Nur ein Swal oeffnen, sonst hatten wir ploetzlich race condition und dann wurde das falsche gezeigt
            void Swal.fire(swalOptions);

        }, error => {
            LOG.error(error);
        });
    }

    navigate(monthYear: { month: number; year: number }): void {
        this.loaded = false;
        void this.router.navigate(
            ['/ortderimpfung/terminverwaltung', this.selectedODIId, monthYear.year, monthYear.month]);
        LayoutUtil.makePageBig();
    }

    updateMonthYearCombinations(): void {
        this.monthYearCombination.splice(0, this.monthYearCombination.length);
        const m: moment.Moment = DateUtil.ofMonthYear(this.selectedMonat, this.selectedJahr);
        const mm1: moment.Moment = DateUtil.substractMonths(m, 1);
        const mm2: moment.Moment = DateUtil.substractMonths(m, 2);
        const mm3: moment.Moment = DateUtil.substractMonths(m, 3);
        const mp1: moment.Moment = DateUtil.addMonths(m, 1);
        const mp2: moment.Moment = DateUtil.addMonths(m, 2);
        const mp3: moment.Moment = DateUtil.addMonths(m, 3);
        this.monthYearCombination.push({month: mm3.month(), year: mm3.year()});
        this.monthYearCombination.push({month: mm2.month(), year: mm2.year()});
        this.monthYearCombination.push({month: mm1.month(), year: mm1.year()});
        this.monthYearCombination.push({month: m.month(), year: m.year()});
        this.monthYearCombination.push({month: mp1.month(), year: mp1.year()});
        this.monthYearCombination.push({month: mp2.month(), year: mp2.year()});
        this.monthYearCombination.push({month: mp3.month(), year: mp3.year()});
    }

    backToStammdaten(): void {
        void this.router.navigate(['/ortderimpfung/stammdaten/' + this.selectedODIId]);
    }

    public canDeactivate(): Observable<boolean> | Promise<boolean> | boolean {
        if (!this.formGroup.dirty) {
            LayoutUtil.makePageNormal();
            return true;
        }

        const translatedWarningText = this.translate.instant('WARN.WARN_UNSAVED_CHANGES');
        return Swal.fire({
            icon: 'warning',
            text: translatedWarningText,
            showCancelButton: true,
            cancelButtonText: this.translate.instant('WARN.WARN_UNSAVED_CHANGES_CANCEL'),
            confirmButtonText: this.translate.instant('WARN.WARN_UNSAVED_CHANGES_OK'),
        }).then(value => {
            if (value.isConfirmed) {
                LayoutUtil.makePageNormal();
            }

            return value.isConfirmed;
        });
    }

    public preventDrag(_: any): boolean {
        return false;
    }

    public onKeydown($event: KeyboardEvent): void {
        switch ($event.code) {
            case 'ArrowLeft':
                this.moveFocus(-1, $event.target as Element);
                return;
            case 'ArrowRight':
                this.moveFocus(1, $event.target as Element);
                return;
            case 'ArrowUp':
                this.moveFocus(-this.maxRowLength, $event.target as Element);
                return;
            case 'ArrowDown':
                this.moveFocus(this.maxRowLength, $event.target as Element);
                return;
        }

    }

    private moveFocus(move: number, currentElement: Element): void {
        if (!currentElement) {
            return;
        }

        const attr = currentElement.getAttribute('tabindex');
        if (!attr) {
            return;
        }
        const currentIndex = parseInt(attr, 10);
        const newIndex = move + currentIndex;

        const newElement = document.querySelector('input[tabindex="' + newIndex + '"]');
        if (newElement) {
            const newInput = newElement as HTMLInputElement;
            newInput.focus();
            // ohne das timeout wird die selection von irgendwoher wieder abgewahlt
            setTimeout(() => {
                newInput.select();
            }, 1);

        }
    }

    public getDayFormgroup(row: number): UntypedFormGroup {
        const rowArray = this.formGroup.controls.tableRowArray as UntypedFormArray;
        return rowArray.controls[row] as UntypedFormGroup;
    }

    public getDayTimeslotFormgroup(dayFormGroup: UntypedFormGroup, col: number, krankheit: KrankheitIdentifierTS): UntypedFormGroup {
        const colArray = dayFormGroup.controls.krankheitImpfslotDisplayJaxMap.get(krankheit) as UntypedFormArray;
        return colArray.controls[col] as UntypedFormGroup;
    }

    public getControl(row: number, col: number, controlName: string, krankheit: KrankheitIdentifierTS): UntypedFormControl {
        return this.getDayTimeslotFormgroup(this.getDayFormgroup(row), col, krankheit).controls[controlName] as UntypedFormControl;
    }

    public getSum(rowIndex: number, impffolge: ImpffolgeTS, krankheit: KrankheitIdentifierTS): number | undefined {
        let sum = 0;

        const dayFormGroup = this.getDayFormgroup(rowIndex);

        const dayKrankheitFormgroupArray = dayFormGroup.controls.krankheitImpfslotDisplayJaxMap.get(krankheit) as UntypedFormArray;
        for (const item of dayKrankheitFormgroupArray.controls) {
            let val = 0;
            const hourFormgroup = item as UntypedFormGroup;
            let controlName: string;
            switch (impffolge) {
                case ImpffolgeTS.ERSTE_IMPFUNG:
                    controlName = 'kapazitaetErsteImpfung';
                    break;
                case ImpffolgeTS.ZWEITE_IMPFUNG:
                    controlName = 'kapazitaetZweiteImpfung';
                    break;
                case ImpffolgeTS.BOOSTER_IMPFUNG:
                    controlName = 'kapazitaetBoosterImpfung';
                    break;
            }
            const control = hourFormgroup.controls[controlName];
            try {
                val = control.value ? parseInt(control.value, 10) : 0;
            } finally {
                sum += val;
            }
        }

        return sum;
    }

    private loadImpfslots$(id: string, jahr: number, monat: number): Observable<Array<ImpfslotDisplayDayJaxTS>> {
        const mStart = DateUtil.firstDayOfMonth(monat).year(jahr).hour(12); // at midday to avoid timezone issues
        const mEnd = DateUtil.lastDayOfMonth(monat).year(jahr).hour(12); // at midday to avoid timezone issues
        if (id) {
            LayoutUtil.makePageBig();
            return this.impfslotsService.impfslotResourceGetImpfslotByODIBetween(id,
                mEnd.toDate(),
                mStart.toDate());
        }

        return of([]);
    }

    public showButtonAbsagen(row: ImpfslotDisplayDayJaxTS, krankheit: KrankheitIdentifierTS): boolean {
        // TODO Affenpocken: VACME-2340
        if (!TenantUtil.hasMassenTermineAbsagen() || krankheit === this.AFFENPOCKEN) {
            return false;
        }
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI)
            && DateUtil.isAfterToday(row.day);
    }

    public termineAbsagen(impffolge: ImpffolgeTS, row: ImpfslotDisplayDayJaxTS): void {
        let key;
        switch (impffolge) {
            case this.ERSTEIMPFUNG:
                key =  'FACH-APP.ODI.STAMMDATEN.TERMINE_ABSAGEN_ERSTTERMIN_CONFIRM';
                break;
            case this.ZWEITEIMPFUNG:
                key = 'FACH-APP.ODI.STAMMDATEN.TERMINE_ABSAGEN_ZWEITTERMIN_CONFIRM';
                break;
            case this.BOOSTERIMPFUNG:
                key = 'FACH-APP.ODI.STAMMDATEN.TERMINE_ABSAGEN_BOOSTERTERMIN_CONFIRM';
                break;
            default:
                throw new Error('Nicht unterstuetze Impffolge ' + impffolge);
        }
        void Swal.fire({
            icon: 'warning',
            text: this.translate.instant(key),
            showConfirmButton: true,
            showCancelButton: true,
        }).then(r => {
            if (r.isConfirmed && this.selectedODIId && row.day) {
                const model: TermineAbsagenJaxTS = {
                    odiId: this.selectedODIId,
                    impffolge,
                    datum: row.day
                };
                this.terminbuchungService.terminbuchungResourceTermineAbsagenForOdiAndDatum(model).subscribe(_ => {
                    this.reload();
                    void Swal.fire({
                        icon: 'success',
                        showCancelButton: false,
                        showConfirmButton: false,
                        timer: 1500,
                    });
                }, error => LOG.error(error));
            }
        });
    }

    public generateImpfslots(): void {
        if (this.selectedODIId) {
            // Months are starting with 0 in typescript
            const monat = this.selectedMonat + 1;
            this.impfslotService.impfslotResourceGenerateImpfslot(
                monat, this.selectedODIId, this.selectedJahr
            ).subscribe(() => {
                this.reload();
            }, error => LOG.error(error));
        }
    }

    private calculateHasMonatEveryImpfslot(): boolean {
        if (!this.rows) {
            return false;
        }
        const numberOfDays = DateUtil.daysInMonth(this.selectedMonat, this.selectedJahr);
        if (this.rows.length < numberOfDays) {
            return false;
        }

        for (const krankheiten of this.verfuegbareKrankheiten) {
            if (this.rows.find(row =>
                !row.krankheitImpfslotDisplayJaxMap?.[krankheiten] ||
                row.krankheitImpfslotDisplayJaxMap?.[krankheiten]?.length < NBR_IMPFSLOT_PER_DAY)) {
                return false;
            }
        }

        return true;
    }
}
