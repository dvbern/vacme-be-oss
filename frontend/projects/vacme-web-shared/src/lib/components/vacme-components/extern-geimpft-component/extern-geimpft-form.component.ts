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
import {ChangeDetectorRef, Component, Input, OnChanges, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {Subject} from 'rxjs';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import {ExternGeimpftJaxTS, KrankheitIdentifierTS, MissingForGrundimmunisiertTS} from 'vacme-web-generated';
import {AuthServiceRsService} from '../../../service/auth-service-rs.service';
import {ExternGeimpftUtil} from '../../../util/externgeimpft-util';
import {Option} from '../../form-controls/input-select/option';

@Component({
    selector: 'lib-extern-geimpft-form',
    templateUrl: './extern-geimpft-form.component.html',
    styleUrls: ['./extern-geimpft-form.component.scss'],
})
export class ExternGeimpftFormComponent implements OnInit, OnChanges, OnDestroy {

    private ngUnsubscribe$ = new Subject();

    @Input()
    public krankheit!: KrankheitIdentifierTS;

    @Input()
    public formGroup!: FormGroup;

    @Input()
    public impfstoffOptions: Option[] = [];

    @Input()
    public showTrotzdemVollstaendigeGrundimmunisierungWahrnehmen = false;

    @Input()
    public showKontrolliertCheckbox = false;

    @Input()
    public externGeimpftOriginal?: ExternGeimpftJaxTS;


    constructor(
        private fb: FormBuilder,
        private router: Router,
        private route: ActivatedRoute,
        private authService: AuthServiceRsService,
        public translate: TranslateService,
        private datePipe: DatePipe,
        private cdRef: ChangeDetectorRef,
    ) {
    }

    public myExternGeimpft(): ExternGeimpftJaxTS | null | undefined {
        return this.externGeimpftOriginal;
    }

    ngOnInit(): void {
        this.updateFormFromModelAndDetectChanges();
    }

    ngOnChanges(): void {
        if (!!this.formGroup) { // ngOnChanges kann vor und nach ngOnInit aufgerufen werden
            this.updateFormFromModelAndDetectChanges();
        }
    }

    private updateFormFromModelAndDetectChanges(): void {
        this.updateFormFromModel();
        this.cdRef.detectChanges();
    }

    private updateFormFromModel(): void {
        const model = this.myExternGeimpft();
        if (model) {
            ExternGeimpftUtil.updateFormFromModel(this.formGroup, model, this.impfstoffOptions, this.datePipe);
        }
    }

    public hasBeenGeimpft(): boolean {
        return this.formGroup?.get('externGeimpft')?.value;
    }

    ngOnDestroy(): void {
        this.ngUnsubscribe$.next();
        this.ngUnsubscribe$.complete();
    }

    public showGenesen(): boolean {
        return ExternGeimpftUtil.showGenesen(this.krankheit);
    }

    public showTrotzdemVollstaendigGrundimmunisieren(): boolean {
        return this.showTrotzdemVollstaendigeGrundimmunisierungWahrnehmen
            && ExternGeimpftUtil.showTrotzdemVollstaendigGrundimmunisieren(this.formGroup, this.krankheit);
    }

    public showPositivGetestetDatum(): boolean {
        return ExternGeimpftUtil.showPositivGetestetDatum(this.formGroup, this.krankheit);
    }

    public getAnzahlMissingImpfungen(): MissingForGrundimmunisiertTS | undefined {
        return ExternGeimpftUtil.calculateAnzahlMissingImpfungen(this.formGroup, this.krankheit);
    }

    public getMainQuestionText(): string {
        const translatedKrankheit = this.translate.instant('KRANKHEITEN.' + this.krankheit);
        const mainQuestionKey = ExternGeimpftUtil.getMainQuestion(this.krankheit);
        return this.translate.instant(mainQuestionKey, {krankheit: translatedKrankheit});
    }

    public showLetzteImpfungDatumUnknown(): boolean {
        return ExternGeimpftUtil.showLetzteImpfungDatumUnbekannt(this.krankheit);
    }

    public showDatumLetzteImpfung(): boolean {
        return ExternGeimpftUtil.showDatumLetzteImpfung(this.formGroup, this.krankheit);
    }

    public showSchnellschema(): boolean {
        return ExternGeimpftUtil.showSchnellschema(this.krankheit);
    }
}
