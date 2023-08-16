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
    ImpfungOdiKorrekturJaxTS,
    KorrekturDashboardJaxTS,
    KorrekturService,
    KrankheitIdentifierTS,
    OrtDerImpfungJaxTS,
    OrtderimpfungService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {Option} from '../../../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {isAtLeastOnceGeimpft} from '../../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';
import {SortByPipe} from '../../../../../../vacme-web-shared/src/lib/util/sort-by-pipe';
import DatenKorrekturService from '../daten-korrektur.service';
import {getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturOdiComponent');

@Component({
    selector: 'app-daten-korrektur-odi',
    templateUrl: './daten-korrektur-odi.component.html',
    styleUrls: ['./daten-korrektur-odi.component.scss'],
    providers: [SortByPipe],
})
export class DatenKorrekturOdiComponent implements OnInit {

    @Input()
    set korrekturDashboardJax(value: KorrekturDashboardJaxTS | undefined) {
        this.korrekturDashboard = value;
        this.calculateOdiOptions();
    }

    korrekturDashboard: KorrekturDashboardJaxTS | undefined;

    @Output()
    public finished = new EventEmitter<boolean>();

    formGroup!: UntypedFormGroup;

    public ortDerImpfungList: OrtDerImpfungJaxTS[] = [];
    public odiOptions: Option[] = [];

    public impfungenListOptions: Option[] = [];

    constructor(
        private authService: AuthServiceRsService,
        private fb: UntypedFormBuilder,
        private translationService: TranslateService,
        private korrekturService: KorrekturService,
        private odiService: OrtderimpfungService,
        private sortPipe: SortByPipe,
        private translateService: TranslateService,
        private datenKorrekturUtil: DatenKorrekturService,
    ) {
    }

    ngOnInit(): void {
        this.authService.loadOdisForCurrentUserAndStoreInPrincipal$(false).pipe().subscribe(
            (list: Array<OrtDerImpfungJaxTS>) => {
                this.ortDerImpfungList = this.sortPipe.transform(list, 'asc', 'name');
                this.calculateOdiOptions();
            },
            (error: any) => {
                LOG.error(error);
            },
        );
        this.formGroup = this.fb.group({
            impfung: this.fb.control(null, [Validators.required]),
            odi: this.fb.control(null, [Validators.required]),
        });
    }

    private calculateOdiOptions(): void {
        if (this.korrekturDashboard?.krankheitIdentifier) {
            this.odiOptions = [];
            this.ortDerImpfungList.filter(odi => odi.krankheiten.map(krankheit => krankheit.identifier)
                .includes(this.getKrankheit())).forEach(odi => {
                const odiLabel = this.calculateLabelForOdi(odi);
                this.odiOptions.push(
                    {label: odiLabel, value: odi.id},
                );
            });
        }
    }

    private calculateLabelForOdi(odi: OrtDerImpfungJaxTS): string | undefined {
        let odiLabel = odi.name;
        if (odi.deaktiviert) {
            // Wird nur im Label angezeigt, nicht disabled, da Korrektur immer berechtigt ist
            odiLabel += ' ' + this.translationService.instant('OVERVIEW.ODI_INAKTIV');
        }
        return odiLabel;
    }

    availableImpfungAndImpffolgeOptions(): Option[] {
        if (!this.impfungenListOptions?.length) {
            this.impfungenListOptions = this.datenKorrekturUtil.availableImpfungAndImpffolgeOptions(this.korrekturDashboard);
        }
        return this.impfungenListOptions;
    }

    public hasRequiredRole(): boolean {
        return this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.IMPFUNG_ORT));
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
        const data: ImpfungOdiKorrekturJaxTS = {
            impffolge: this.formGroup.get('impfung')?.value.impffolge,
            impffolgeNr: this.formGroup.get('impfung')?.value.impffolgeNr,
            odi: this.formGroup.get('odi')?.value.value,
            krankheitIdentifier: this.getKrankheit()
        };
        const regNummer = this.korrekturDashboard?.registrierungsnummer;
        if (!regNummer || !data.odi) {
            return;
        }
        this.correctOdi(regNummer, data);
    }

    // TODO Affenpocken VACME-2325
    private getKrankheit(): KrankheitIdentifierTS {
        if (this.korrekturDashboard?.krankheitIdentifier === undefined || this.korrekturDashboard.krankheitIdentifier === null) {
            throw new Error('Krankheit nicht gesetzt ' + this.korrekturDashboard?.registrierungsnummer);
        }
        return this.korrekturDashboard.krankheitIdentifier;
    }

    private correctOdi(regNummer: string, data: ImpfungOdiKorrekturJaxTS): void {
        this.korrekturService.korrekturResourceImpfungOdiKorrigieren(regNummer, data).subscribe(() => {
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
            LOG.error('Could not update ODI of Impfung', err);
        });
    }

    public reset(): void {
        this.korrekturDashboard = undefined;
        this.formGroup.reset();
        this.finished.emit(false);
    }
}
