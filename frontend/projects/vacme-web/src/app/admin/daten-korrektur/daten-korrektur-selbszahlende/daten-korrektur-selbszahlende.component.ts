import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    ImpfungSelbstzahlendeKorrekturJaxTS,
    KorrekturDashboardJaxTS,
    KorrekturService,
    KrankheitIdentifierTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {Option} from '../../../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {isAtLeastOnceGeimpft} from '../../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';
import {SelectOptionsUtil} from '../../../../../../vacme-web-shared/src/lib/util/select-options-util';
import DatenKorrekturService from '../daten-korrektur.service';
import {getAllowdRoles, TSDatenKorrekturTyp} from '../TSDatenKorrekturTyp';

const LOG = LogFactory.createLog('DatenKorrekturSelbszahlendeComponent');

@Component({
    selector: 'app-daten-korrektur-selbszahlende',
    templateUrl: './daten-korrektur-selbszahlende.component.html',
})
export class DatenKorrekturSelbszahlendeComponent implements OnInit {
    @Input()
    korrekturDashboard!: KorrekturDashboardJaxTS | undefined;

    @Output()
    public finished = new EventEmitter<boolean>();

    public impfungenListOptions: Option[] = [];
    public selbstzahlendeOptions: Option[] = [];
    public formGroup!: UntypedFormGroup;

    constructor(
        private authService: AuthServiceRsService,
        private datenKorrekturUtil: DatenKorrekturService,
        private fb: UntypedFormBuilder,
        private korrekturService: KorrekturService,
        private translationService: TranslateService,
    ) {
    }

    ngOnInit(): void {
        this.formGroup = this.fb.group({
            impfung: this.fb.control(null, [Validators.required]),
            selbstzahlende: this.fb.control(null, Validators.required),
        });
    }

    public correctIfValid(): void {
        if (this.hasRequiredRole()) {
            FormUtil.doIfValid(this.formGroup, () => {
                this.selbstzahlendeKorrigieren();
            });
        }
    }

    public availableImpfungAndImpffolgeOptions(): Option[] {
        if (!this.impfungenListOptions?.length) {
            this.impfungenListOptions =
                this.datenKorrekturUtil.availableImpfungAndImpffolgeOptions(this.korrekturDashboard);
        }
        return this.impfungenListOptions;
    }

    public availableSelbstzahlerOptions(): Option[] {
        if (!this.selbstzahlendeOptions?.length) {
            this.selbstzahlendeOptions = SelectOptionsUtil.zahlungstypOptions(this.getKrankheit());
        }
        return this.selbstzahlendeOptions;
    }

    public hasRequiredRole(): boolean {
        return this.authService.isOneOfRoles(getAllowdRoles(TSDatenKorrekturTyp.SELBSTZAHLENDE));
    }

    public reset(): void {
        this.korrekturDashboard = undefined;
        this.formGroup.reset();
        this.finished.emit(false);
    }

    private selbstzahlendeKorrigieren(): void {
        const data: ImpfungSelbstzahlendeKorrekturJaxTS = {
            impffolge: this.formGroup.get('impfung')?.value.impffolge,
            impffolgeNr: this.formGroup.get('impfung')?.value.impffolgeNr,
            selbstzahlende: this.formGroup.get('selbstzahlende')?.value,
            krankheitIdentifier: this.getKrankheit(),
        };
        const regNummer = this.korrekturDashboard?.registrierungsnummer;
        if (!regNummer || !data) {
            return;
        }
        this.korrekturService.korrekturResourceImpfungSelbstzahlendeKorrigieren(regNummer, data).subscribe(() => {
            Swal.fire({
                icon: 'success',
                text: this.translationService.instant('FACH-ADMIN.DATEN_KORREKTUR.SUCCESS'),
                showConfirmButton: true,
            }).then(() => {
                this.korrekturDashboard = undefined;
                this.formGroup.reset();
                this.finished.emit(true);
            });
        }, err => {
            LOG.error('Could not update Date of Impfung', err);
        });
    }

    public enabled(): boolean {
        if (this.korrekturDashboard?.registrierungsnummer) {
            return isAtLeastOnceGeimpft(this.korrekturDashboard.status);
        }
        return false;
    }

    // TODO Affenpocken VACME-2325
    private getKrankheit(): KrankheitIdentifierTS {
        if (this.korrekturDashboard?.krankheitIdentifier
            === undefined
            || this.korrekturDashboard.krankheitIdentifier
            === null) {
            console.warn('dashboard', this.korrekturDashboard);
            throw new Error('Krankheit nicht gesetzt ' + this.korrekturDashboard?.registrierungsnummer);
        }
        return this.korrekturDashboard.krankheitIdentifier;
    }
}
