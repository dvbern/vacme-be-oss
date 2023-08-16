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

import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {SwalPortalTargets} from '@sweetalert2/ngx-sweetalert2';
import moment from 'moment/moment';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {ImpffolgeTS, KontrolleService, KrankheitIdentifierTS, ZweiteImpfungVerzichtenJaxTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {ImpfdokumentationService} from '../../../../../vacme-web-generated/src/lib/api/impfdokumentation.service';
import {
    DATE_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    MIN_DATE_FOR_POSITIV_GETESTET,
} from '../../../../../vacme-web-shared/src/lib/constants';
import {
    datumInPastValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/datum-in-past-validator';
import {minDateValidator} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/min-date-validator';
import {
    parsableDateValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';
import {requiredIfValidator} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/required-if-validator';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {NavigationService} from '../../service/navigation.service';

const LOG = LogFactory.createLog('ZweiteImpfungVerzichtenComponent');

@Component({
    selector: 'app-zweite-impfung-verzichten',
    templateUrl: './zweite-impfung-verzichten.component.html',
    styleUrls: ['./zweite-impfung-verzichten.component.scss']
})
export class ZweiteImpfungVerzichtenComponent implements OnInit, OnDestroy {

    @Input()
    showAufZweiteImpfungVerzichten?: boolean;

    @Input()
    showEsKannNichtGeimpftWerden?: boolean;

    @Input()
    disableEsKannNichtGeimpftWerden?: boolean;

    @Input()
    registrierungsnummer?: string | undefined;

    @Input()
    impffolge?: ImpffolgeTS;

    public keineZweiteImpfungGroup!: UntypedFormGroup;
    public verzichtenBegruendungGroup!: UntypedFormGroup;

    public cancelOrMoveOptions = [
        {label: 'VERSCHIEBEN', value: 'VERSCHIEBEN'},
        {label: 'VERZICHTEN', value: 'VERZICHTEN'}
    ];

    private ngUnsubscribe$ = new Subject<void>();

    constructor(
        private fb: UntypedFormBuilder,
        private router: Router,
        private kontrolleService: KontrolleService,
        private impfdokumentationService: ImpfdokumentationService,
        private navigationService: NavigationService,
        public readonly swalTargets: SwalPortalTargets,
    ) {
    }

    ngOnInit(): void {
        this.initUI();
    }

    public ngOnDestroy(): void {
        this.ngUnsubscribe$.next(undefined);
        this.ngUnsubscribe$.complete();
    }

    private initUI(): void {
        this.keineZweiteImpfungGroup = this.fb.group({
            cancelOrMove: this.fb.control(undefined, Validators.required)
        });
        this.verzichtenBegruendungGroup = this.fb.group({
            begruendung: this.fb.control(undefined, [Validators.required, Validators.maxLength(2000)]),
            vollstaendigerImpfschutz: this.fb.control(undefined, []),
            positivGetestetDatum: this.fb.control(undefined, [
                requiredIfValidator(() => this.verzichtenBegruendungGroup?.get('vollstaendigerImpfschutz')?.value),
                Validators.minLength(4), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.pattern(DATE_PATTERN), parsableDateValidator(), datumInPastValidator(),
                minDateValidator(moment(MIN_DATE_FOR_POSITIV_GETESTET, 'DD.MM.YYYY').toDate())])
        });
        this.verzichtenBegruendungGroup.get('vollstaendigerImpfschutz')?.valueChanges
            .pipe(takeUntil(this.ngUnsubscribe$))
            .subscribe(vollstaendigValue => {
                if (!vollstaendigValue) {
                    this.verzichtenBegruendungGroup.get('positivGetestetDatum')?.setValue(undefined);
                }
            }, err => LOG.error(err));
    }

    isZweiteImpfung(): boolean {
        return this.impffolge === ImpffolgeTS.ZWEITE_IMPFUNG;
    }

    isZweiteImpfungToCancel(): boolean {
        return this.keineZweiteImpfungGroup.get('cancelOrMove')?.value === 'VERZICHTEN' ||
            !this.isZweiteImpfung();
    }

    confirmCancelZweiteImpfung(): void {
        if (this.isZweiteImpfung()) {
            FormUtil.doIfValid(this.keineZweiteImpfungGroup, () => {
                if (this.isZweiteImpfungToCancel()) {
                    this.zweiteImpfungVerzichten();
                } else {
                    Swal.clickConfirm();
                    this.resetToKontrolle();
                }
            });
        } else {
            // wenn direkt nach der 1. impfung verzichtet wird
            this.zweiteImpfungVerzichten();
        }
    }

    private zweiteImpfungVerzichten(): void {
        FormUtil.doIfValid(this.verzichtenBegruendungGroup, () => {
            Swal.clickConfirm();
            Swal.showLoading();
            this.verzichtenZweiteImpfung();
        });
    }

    verzichtenZweiteImpfung(): void {
        const jax: ZweiteImpfungVerzichtenJaxTS = this.formToModel();
        this.kontrolleService.impfkontrolleResourceVerzichtenZweiteImpfung(jax)
            .subscribe(
                () => void this.router.navigate(['dossier', this.registrierungsnummer]),
                error => LOG.error(error));
    }

    private formToModel(): ZweiteImpfungVerzichtenJaxTS {
        const hasVollstImpfschutzCausePCR = this.verzichtenBegruendungGroup.get('vollstaendigerImpfschutz')?.value;
        let testdatum: Date | undefined;
        if (!hasVollstImpfschutzCausePCR) {
            testdatum = undefined;
        } else {
            testdatum = DateUtil.parseDateAsMidday(this.verzichtenBegruendungGroup.get('positivGetestetDatum')?.value);
        }
        const model: ZweiteImpfungVerzichtenJaxTS = {
            krankheitIdentifier: KrankheitIdentifierTS.COVID, // TODO Affenpocken: Richtige Krankheit mitgeben
            registrierungsnummer: this.registrierungsnummer as string,
            begruendung: this.verzichtenBegruendungGroup.get('begruendung')?.value,
            vollstaendigerImpfschutz: hasVollstImpfschutzCausePCR,
            positivGetestetDatum: testdatum
        };
        return model;
    }

    resetToKontrolle(): void {
        this.impfdokumentationService.impfdokumentationResourceImpfungVerweigert(
            KrankheitIdentifierTS.COVID, // TODO Affenpocken: Richtige Krankheit mitgeben
            this.registrierungsnummer as string)
            .subscribe(
                () => this.back(),
                error => LOG.error(error));
    }

    public back(): void {
        void this.router.navigate(['']);
    }

    closeSwal(): void {
        Swal.clickCancel();
    }

    public hasVollstaendigerImpfschutz(): boolean {
        return this.verzichtenBegruendungGroup.get('vollstaendigerImpfschutz')?.value;
    }

    public vollstaendigerImpfschutzChanged(): void {
        if (this.hasVollstaendigerImpfschutz()) {
            this.verzichtenBegruendungGroup.get('begruendung')?.setValue('');
            this.verzichtenBegruendungGroup.get('begruendung')?.disable();
        } else {
            this.verzichtenBegruendungGroup.get('begruendung')?.enable();
        }
    }
}
