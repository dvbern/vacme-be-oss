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

import {DatePipe, DOCUMENT} from '@angular/common';
import {Component, Inject, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {
    DashboardJaxTS,
    DossierService,
    GeimpftService,
    ImpffolgeTS,
    ImpfungJaxTS,
    KontrolleService, KrankheitIdentifierTS,
    RegistrierungStatusTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {
    MissingForGrundimmunisiertTS,
} from '../../../../../vacme-web-generated/src/lib/model/missing-for-grundimmunisiert';
import {MAX_LENGTH_TEXTAREA} from '../../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {TerminUtilService} from '../../../../../vacme-web-shared/src/lib/service/termin-util.service';
import {BoosterUtil} from '../../../../../vacme-web-shared/src/lib/util/booster-util';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {ExternGeimpftUtil} from '../../../../../vacme-web-shared/src/lib/util/externgeimpft-util';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('ImpfdokumentationComponent');

interface ImpfungAndNr {
    impffolge: ImpffolgeTS;
    impffolgeNr: number;
    impfung: ImpfungJaxTS | undefined;
}

interface BemerkungEditor {
    key: string;
    impffolge: ImpffolgeTS;
    impffolgeNr: number;
    form: FormGroup;
}

@Component({
    selector: 'app-geimpft-impfungen-list',
    templateUrl: './geimpft-impfungen-list.component.html',
    styleUrls: ['./geimpft-impfungen-list.component.scss']
})
export class GeimpftImpfungenListComponent implements OnInit {

    @Input()
    public dashboardJax?: DashboardJaxTS;

    @Input()
    public editableLastBemerkung = false;

    @Input()
    public supportsImpffolgenEinsUndZwei = false;

    public saved = false;
    public impfungenList: ImpfungAndNr[] = [];
    public bemerkungEditor?: BemerkungEditor;

    constructor(
        private fb: FormBuilder,
        private activeRoute: ActivatedRoute,
        private errorService: ErrorMessageService,
        private translationService: TranslateService,
        private geimpftService: GeimpftService,
        private authService: AuthServiceRsService,
        private datePipe: DatePipe,
        private kontrolleService: KontrolleService,
        private dossierService: DossierService,
        @Inject(DOCUMENT) private document: Document
    ) {
    }

    ngOnInit(): void {
        this.createDisplayList();
        this.setupBemerkungenEditor();
        if (this.hasRoleDokumentation() && !this.isArchiviert()) {
            this.bemerkungEditor?.form?.enable();
        }
    }

    hasRoleDokumentation(): boolean {
        return this.authService.isOneOfRoles(
            [TSRole.OI_DOKUMENTATION, TSRole.KT_NACHDOKUMENTATION, TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION]);
    }

    public hasExternesZertifikat(): boolean {
        return this.dashboardJax?.externGeimpft?.grundimmunisiert as boolean;
    }

    private createDisplayList(): void {
        if (!this.dashboardJax) {
            return;
        }
        this.impfungenList = [];

        // Impfung 1 (falls vorhanden)
        if (this.dashboardJax?.impfung1) {
            this.impfungenList.push({
                impffolge: ImpffolgeTS.ERSTE_IMPFUNG,
                impffolgeNr: TerminUtilService.determineImpffolgeNrForImpfung1Or2(
                    ImpffolgeTS.ERSTE_IMPFUNG,
                    this.dashboardJax),
                impfung: this.dashboardJax?.impfung1
            });
        }
        // Impfung 1 noch ausstehend
        else if (this.supportsImpffolgenEinsUndZwei
            && !this.dashboardJax.impfung1
            && !this.isAbgeschlossen()
            && (!this.dashboardJax.externGeimpft?.externGeimpft
                || this.dashboardJax.externGeimpft.missingForGrundimmunisiertAfterDecision
                !== MissingForGrundimmunisiertTS.BRAUCHT_0_IMPFUNGEN)) {
            this.impfungenList.push({
                impffolge: ImpffolgeTS.ERSTE_IMPFUNG,
                impffolgeNr: TerminUtilService.determineImpffolgeNrForImpfung1Or2(
                    ImpffolgeTS.ERSTE_IMPFUNG,
                    this.dashboardJax),
                impfung: this.dashboardJax?.impfung1
            });
        }

        // Impfung 2 falls vorhanden
        if (this.dashboardJax?.impfung2) {
            this.impfungenList.push({
                impffolge: ImpffolgeTS.ZWEITE_IMPFUNG,
                impffolgeNr: TerminUtilService.determineImpffolgeNrForImpfung1Or2(
                    ImpffolgeTS.ZWEITE_IMPFUNG,
                    this.dashboardJax),
                impfung: this.getLastImpffolge() === ImpffolgeTS.ERSTE_IMPFUNG ? undefined : this.dashboardJax?.impfung2
            });
        }
        // Impfung 2 noch ausstehend
        else if (this.supportsImpffolgenEinsUndZwei
            && !this.dashboardJax.impfung2
            && !this.hasZweiteImpfungVerzichtet()
            && !this.isAbgeschlossen()
            && (!this.dashboardJax.externGeimpft?.externGeimpft
                || this.dashboardJax.externGeimpft.missingForGrundimmunisiertAfterDecision
                === MissingForGrundimmunisiertTS.BRAUCHT_VOLLE_GRUNDIMMUNISIERUNG)) {
            this.impfungenList.push({
                impffolge: ImpffolgeTS.ZWEITE_IMPFUNG,
                impffolgeNr: TerminUtilService.determineImpffolgeNrForImpfung1Or2(
                    ImpffolgeTS.ZWEITE_IMPFUNG,
                    this.dashboardJax),
                impfung: this.dashboardJax?.impfung2
            });
        }

        // Boosterimpfungen
        if (this.dashboardJax?.boosterImpfungen) {
            this.dashboardJax.boosterImpfungen.forEach((eintrag) => {
                this.impfungenList.push({
                    impffolge: ImpffolgeTS.BOOSTER_IMPFUNG,
                    impffolgeNr: eintrag.impfungNr,
                    impfung: eintrag
                });
            });
        }
    }

    private setupBemerkungenEditor(): void {
        const lastImpfung = this.getLastImpfung();

        // Bemerkungseditor fuer erste oder zweite verzichtete Impfung
        if (lastImpfung && lastImpfung === this.dashboardJax?.impfung1) {
            const impffolgeNrErsteImpfung = TerminUtilService.determineImpffolgeNrForImpfung1Or2(
                ImpffolgeTS.ERSTE_IMPFUNG,
                this.dashboardJax);
            if (this.isVerzichtetGrundEditable()) {
                this.createBemerkungEditor(
                    this.translationService.instant('GEIMPFT.IMPFUNG_VERZICHTET_GRUND'),
                    ImpffolgeTS.ZWEITE_IMPFUNG,
                    impffolgeNrErsteImpfung + 1,
                    this.dashboardJax?.zweiteImpfungVerzichtetGrund
                );
            } else {
                this.createBemerkungEditor(
                    this.translationService.instant('GEIMPFT.BEMERKUNGENIMPFUNG'),
                    ImpffolgeTS.ERSTE_IMPFUNG,
                    impffolgeNrErsteImpfung,
                    this.dashboardJax?.impfung1?.bemerkung
                );
            }
        }

        if (lastImpfung && lastImpfung === this.dashboardJax?.impfung2) {
            const impffolgeNrZweiteImpfung = TerminUtilService.determineImpffolgeNrForImpfung1Or2(
                ImpffolgeTS.ZWEITE_IMPFUNG,
                this.dashboardJax);
            this.createBemerkungEditor(
                this.translationService.instant('GEIMPFT.BEMERKUNGENIMPFUNG'),
                ImpffolgeTS.ZWEITE_IMPFUNG,
                impffolgeNrZweiteImpfung,
                this.dashboardJax?.impfung2?.bemerkung
            );
        }

        if (this.dashboardJax?.boosterImpfungen) {
            this.dashboardJax.boosterImpfungen.forEach((impfung) => {
                if (lastImpfung === impfung) {
                    this.createBemerkungEditor(
                        this.translationService.instant('GEIMPFT.BEMERKUNGENIMPFUNG'),
                        ImpffolgeTS.BOOSTER_IMPFUNG,
                        impfung.impfungNr,
                        impfung.bemerkung
                    );
                }
            });
        }
    }

    private createBemerkungEditor(
        key: string,
        impffolge: ImpffolgeTS,
        impffolgeNr: number,
        initialBemerkung?: string
    ): void {
        this.bemerkungEditor = {
            key,
            impffolge,
            impffolgeNr,
            form: this.fb.group({
                bemerkung: this.fb.control({
                    value: initialBemerkung,
                    disabled: true
                }, Validators.maxLength(MAX_LENGTH_TEXTAREA))
            })
        };
    }

    public hasZweiteImpfungVerzichtet(): boolean {
        return !!this.dashboardJax && !!this.dashboardJax.zweiteImpfungVerzichtetZeit;
    }

    public hasVollstaendigenImpfschutz(): boolean {
        const impfschutz = this.dashboardJax?.vollstaendigerImpfschutz;
        if (impfschutz) {
            return impfschutz;
        }
        return false;
    }

    public isAbgeschlossen(): boolean {
        return !!this.dashboardJax && !!this.dashboardJax.timestampZuletztAbgeschlossen;
    }

    public isArchiviert(): boolean {
        return !!this.dashboardJax && !!this.dashboardJax.timestampArchiviert;
    }

    public showBemerkungReadonly(impfung: ImpfungJaxTS): boolean {
        if (!impfung?.bemerkung) {
            return false;
        }
        // Speziallfall erste Impfung: kann Bemerkung zu 1 und Editor zu2 haben
        if (impfung.impffolge === ImpffolgeTS.ERSTE_IMPFUNG) {
            if (this.showBemerkungEditor(impfung)) {
                if (this.isVerzichtetGrundEditable()) {
                    return true; // wenn verzichtet-Editor, beides anzeigen
                } else {
                    return false; // wenn Bemerkung-Editor, readonly ausblenden
                }
            } else {
                return true; // wenn kein Bemerkung-Editor, readonly anzeigen
            }
        }
        // normal: wenn Editor sichtbar, keine Readonly anzeigen
        return !this.showBemerkungEditor(impfung);
    }

    public showBemerkungEditor(impfung: ImpfungJaxTS): boolean {
        return this.editableLastBemerkung && this.getLastImpfung() === impfung;
    }

    public isVerzichtetGrundEditable(): boolean {
        return this.getLastImpffolge() === ImpffolgeTS.ERSTE_IMPFUNG &&
            this.hasZweiteImpfungVerzichtet() &&
            !this.hasVollstaendigenImpfschutz();
    }


    public getTitle(impffolgeNr: number, impffolge?: ImpffolgeTS, impfung?: ImpfungJaxTS): string {
        if (impfung) {
            let textForFruehereImpfung = this.translationService.instant('GEIMPFT.IMPFUNGSDATEN', {
                impffolge: impffolgeNr,
                impfstoff: impfung.impfstoff?.displayName,
                timestampImpfung: DateUtil.dateAsLocalDateString(impfung.timestampImpfung),
            });
            if (ExternGeimpftUtil.showSchnellschema(this.getKrankheit())) {
                const schnellSchema = impfung.schnellschemaGesetztFuerImpfung;
                const schnellSchemaText =
                    this.translationService.instant('IMPFDOK.IMPF_SCHEMA_OPTION.' + (schnellSchema ?
                        'SCHNELLSCHEMA' :
                        'KONVENTIONELL'));
                textForFruehereImpfung += ` (${schnellSchemaText})`;
            }
            return textForFruehereImpfung;
        } else {
            return this.translationService.instant('GEIMPFT.IMPFUNG_AUSSTEHEND',
                {impffolge: impffolgeNr});
        }
    }

    public printDate(date: Date | undefined): string | null {
        return DateUtil.dateAsLocalDateString(date);
    }

    private getKrankheit(): KrankheitIdentifierTS {
        if (this.dashboardJax?.krankheitIdentifier === undefined
            || this.dashboardJax.krankheitIdentifier === null) {
            throw new Error('Krankheit nicht gesetzt ' + this.dashboardJax?.registrierungsnummer);
        }
        return this.dashboardJax.krankheitIdentifier;
    }

    public needsZweitimpfung(): boolean {
        if (!this.dashboardJax?.impfung1) {
            return false; // fehlernde Zweitimpfung nur anzeigen, wenn Erstimpfung exisiert
        }
        return ExternGeimpftUtil.needsZweitimpfung(this.dashboardJax.impfung1.impfstoff,
            this.dashboardJax?.externGeimpft);
    }

    public hasZweitimpfung(): boolean {
        return !!this.dashboardJax?.impfung2?.timestampImpfung;
    }

    public saveIfValid(): void {
        if (this.bemerkungEditor?.form) {
            FormUtil.doIfValid(this.bemerkungEditor?.form, () => {
                this.save();
            });
        }
    }

    private save(): void {
        if (this.dashboardJax
            && this.dashboardJax.registrierungsnummer
            && this.bemerkungEditor?.impffolge
            && this.bemerkungEditor?.impffolgeNr
        ) {
            this.geimpftService.geimpftResourceSaveBemerkung(
                this.bemerkungEditor?.impffolge,
                this.bemerkungEditor?.impffolgeNr,
                this.dashboardJax.registrierungsnummer,
                this.bemerkungEditor?.form?.get('bemerkung')?.value)
                .subscribe(value => this.onSaved(),
                    error => LOG.error(error));
        } else {
            LOG.warn('no data set');
        }
    }

    public canSave(): boolean {
        return this.editableLastBemerkung && !this.saved;
    }

    private onSaved(): void {
        this.saved = true;
        this.bemerkungEditor?.form.disable();
    }

    public showSaveButton(): boolean {
        return this.hasRoleDokumentation() &&
            !this.isArchiviert() &&
            this.dashboardJax?.status !== RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN;
    }

    public getLastImpfung(): ImpfungJaxTS | undefined {
        if (this.dashboardJax) {
            return BoosterUtil.getLatestVacmeImpfung(this.dashboardJax);
        }
        return undefined;
    }

    public getLastImpffolge(): ImpffolgeTS | undefined {
        if (this.dashboardJax) {
            return BoosterUtil.getLatestVacmeImpffolge(this.dashboardJax);
        }
        return undefined;
    }
}

