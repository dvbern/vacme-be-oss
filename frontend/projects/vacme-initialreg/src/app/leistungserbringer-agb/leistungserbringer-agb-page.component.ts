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

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {
    DossierService,
    ImpfdossiersOverviewJaxTS,
    ImpfdossierSummaryJaxTS,
    KrankheitIdentifierTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, Option} from '../../../../vacme-web-shared';
import {ErrorMessageService} from '../../../../vacme-web-shared/src/lib/service/error-message.service';
import FormUtil from '../../../../vacme-web-shared/src/lib/util/FormUtil';
import {extractKrankheitFromRoute} from '../../../../vacme-web-shared/src/lib/util/krankheit-utils';
import {NavigationService} from '../service/navigation.service';

const LOG = LogFactory.createLog('ExternGeimpftComponent');

@Component({
    templateUrl: './leistungserbringer-agb-page.component.html',
})
export class LeistungserbringerAgbPageComponent extends BaseDestroyableComponent implements OnInit {

    private saveRequestPending = false;

    public formGroup!: UntypedFormGroup;

    impfstoffOptions: Option[] = [];
    private impfdossiersOverviewJax!: ImpfdossiersOverviewJaxTS;
    private krankheitIdentifier: KrankheitIdentifierTS | undefined;

    constructor(
        private fb: UntypedFormBuilder,
        private navigationService: NavigationService,
        private activeRoute: ActivatedRoute,
        private errorMessageService: ErrorMessageService,
        private dossierService: DossierService,
    ) {
        super();
    }

    public ngOnInit(): void {
        this.formGroup = this.fb.group({
            agb:this.fb.control(false, [Validators.required, Validators.requiredTrue]),
            einwilligung:this.fb.control(false, [Validators.required, Validators.requiredTrue]),
        });
        this.initFromActiveRoute();
    }

    private initFromActiveRoute(): void {
        this.activeRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe(next => {
                if (next) {
                    this.impfdossiersOverviewJax = next.dossiersOverview;
                }
            }, error => {
                LOG.error(error);
            });

        this.krankheitIdentifier = extractKrankheitFromRoute(this.activeRoute.snapshot);
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.save();
        });
    }

    private save(): void {
        this.saveRequestPending = true;
        const krankheit = this.getKrankheit();
        const registrierungsnummer = this.impfdossiersOverviewJax?.registrierungsnummer;
        this.dossierService.dossierResourceRegAcceptLeistungserbringerAgb(krankheit, registrierungsnummer)
            .subscribe((updatedDossierSummary: ImpfdossierSummaryJaxTS) => {
                this.saveRequestPending = false;
                this.navigationService.navigateToDossierDetailCheckingPreConditions(
                    registrierungsnummer,
                    krankheit,
                    updatedDossierSummary.leistungerbringerAgbConfirmationNeeded,
                    updatedDossierSummary.externGeimpftConfirmationNeeded);
            }, error => this.onSaveError(error));
    }

    private onSaveError(err: any): void {
        LOG.error('HTTP Error', err);
        this.saveRequestPending = false;
    }

    private getKrankheit(): KrankheitIdentifierTS {
        if (this.krankheitIdentifier === undefined || this.krankheitIdentifier === null) {
            this.errorMessageService.addMesageAsError('KRANKHEIT NICHT GESETZT');
            throw new Error('Krankheit nicht gesetzt ' + this.impfdossiersOverviewJax?.registrierungsnummer);
        }
        return this.krankheitIdentifier;
    }
}
