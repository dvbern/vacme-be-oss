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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    ImpfungVerabreichungKorrekturJaxTS,
    KorrekturDashboardJaxTS,
    KorrekturService,
    KrankheitIdentifierTS,
    VerarbreichungsartTS,
    VerarbreichungsortTS,
    VerarbreichungsseiteTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {Option} from '../../../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {isAtLeastOnceGeimpft} from '../../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';
import {SortByPipe} from '../../../../../../vacme-web-shared/src/lib/util/sort-by-pipe';
import DatenKorrekturService from '../daten-korrektur.service';
import {getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturImpfungVerabreichungComponent');

@Component({
    selector: 'app-daten-korrektur-impfung-verabreichung',
    templateUrl: './daten-korrektur-impfung-verabreichung.component.html',
    styleUrls: ['./daten-korrektur-impfung-verabreichung.component.scss'],
    providers: [SortByPipe],
})
export class DatenKorrekturImpfungVerabreichungComponent implements OnInit {

    @Input()
    korrekturDashboard: KorrekturDashboardJaxTS | undefined;

    @Output()
    public finished = new EventEmitter<boolean>();

    formGroup!: UntypedFormGroup;

    verabreichungsartOptions: Option[] = Object.values(VerarbreichungsartTS).map(value => {
        return {label: value, value};
    });

    verabreichungsortOptions: Option[] = Object.values(VerarbreichungsortTS).map(value => {
        return {label: value, value};
    });

    public verabreichungsseiteOptions: Option[] = Object.values(VerarbreichungsseiteTS).map(t => {
        return {label: t, value: t};
    });

    public impfungenListOptions: Option[] = [];

    constructor(
        private authService: AuthServiceRsService,
        private fb: UntypedFormBuilder,
        private translationService: TranslateService,
        private korrekturService: KorrekturService,
        private datenKorrekturUtil: DatenKorrekturService,
    ) {
    }

    ngOnInit(): void {
        this.formGroup = this.fb.group({
            impfung: this.fb.control(null, [Validators.required]),
            verabreichungsart: this.fb.control(null, [Validators.required]),
            verabreichungsort: this.fb.control(null, [Validators.required]),
            verabreichungsseite: this.fb.control(null, [Validators.required]),
        });
    }

    availableImpfungAndImpffolgeOptions(): Option[] {
        if (!this.impfungenListOptions?.length) {
            this.impfungenListOptions = this.datenKorrekturUtil.availableImpfungAndImpffolgeOptions(this.korrekturDashboard);
        }
        return this.impfungenListOptions;
    }

    public hasRequiredRole(): boolean {
        return this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.IMPFUNG_VERABREICHUNG));
    }

    public enabled(): boolean {
        if (this.korrekturDashboard?.registrierungsnummer) {
            return isAtLeastOnceGeimpft(this.korrekturDashboard.status);
        }
        return false;
    }

    public correctIfValid(): void {
        if (this.hasRequiredRole()) {
            FormUtil.doIfValid(this.formGroup, () => {
                this.correctData();
            });
        }
    }

    private correctData(): void {
        const data: ImpfungVerabreichungKorrekturJaxTS = {
            impffolge: this.formGroup.get('impfung')?.value.impffolge,
            impffolgeNr: this.formGroup.get('impfung')?.value.impffolgeNr,
            verabreichungsart: this.formGroup.get('verabreichungsart')?.value,
            verabreichungsort: this.formGroup.get('verabreichungsort')?.value,
            verabreichungsseite: this.formGroup.get('verabreichungsseite')?.value,
            krankheitIdentifier: this.getKrankheit(),
        };
        const regNummer = this.korrekturDashboard?.registrierungsnummer;
        if (!regNummer || !data) {
            return;
        }
        this.korrekturService.korrekturResourceImpfungVerabreichungKorrigieren(regNummer, data).subscribe(() => {
            void Swal.fire({
                icon: 'success',
                text: this.translationService.instant('FACH-ADMIN.DATEN_KORREKTUR.SUCCESS'),
                showConfirmButton: true,
            }).then(() => {
                this.korrekturDashboard = undefined;
                this.formGroup.reset();
                this.finished.emit(true);
            });
        }, err => {
            LOG.error('Could not update Verabreichung of Impfung', err);
        });
    }

    // TODO Affenpocken VACME-2325
    private getKrankheit(): KrankheitIdentifierTS {
        if (this.korrekturDashboard?.krankheitIdentifier === undefined || this.korrekturDashboard.krankheitIdentifier === null) {
            throw new Error('Krankheit nicht gesetzt ' + this.korrekturDashboard?.registrierungsnummer);
        }
        return this.korrekturDashboard.krankheitIdentifier;
    }

    public reset(): void {
        this.korrekturDashboard = undefined;
        this.formGroup.reset();
        this.finished.emit(false);
    }
}
