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

import {formatDate} from '@angular/common';
import {ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Subscription} from 'rxjs';
import {DashboardJaxTS, ImpffolgeTS, ImpfterminJaxTS, ImpfungJaxTS, RegistrierungStatusTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import ITermineTS from '../../../../../vacme-web-shared/src/lib/model/ITermine';
import {TSRegistrierungViolationType} from '../../../../../vacme-web-shared/src/lib/model/TSRegistrierungViolationType';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {isAnyStatusOfCurrentlyAbgeschlossen, isStatusLE} from '../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';

import {RegistrierungValidationService} from '../../service/registrierung-validation.service';

const LOG = LogFactory.createLog('PersonTerminComponent');

@Component({
    selector: 'app-person-termin',
    templateUrl: './person-termin.component.html',
    styleUrls: ['./person-termin.component.scss'],
})
export class PersonTerminComponent implements OnInit, OnDestroy {

    @Input() public status: RegistrierungStatusTS | undefined;
    @Input() public termine!: ITermineTS;
    @Input() public impffolge!: ImpffolgeTS;
    @Input() public impffolgeNr!: number;
    @Input() public showTerminumbuchungButtons = true;

    @Output() public adHocTermin1AndSelectTermin2Called = new EventEmitter<VoidFunction>();
    @Output() public termineUmbuchenCalled = new EventEmitter<VoidFunction>();

    private regValSub!: Subscription;

    public hasDateMaxError: boolean | undefined = false;
    public hasDateMinError: boolean | undefined = false;
    public hasImpfstoffError: boolean | undefined = false;
    public hasTermin1Error: boolean | undefined = false;
    public hasTermin2Error: boolean | undefined = false;
    public hasTerminNError: boolean | undefined = false;

    private wrongPlaceOrWrongTimeTermin1 = false;

    constructor(
        private translateService: TranslateService,
        private cdRef: ChangeDetectorRef,
        private registrierungValidationService: RegistrierungValidationService,
        private authServices: AuthServiceRsService
    ) {
    }

    ngOnInit(): void {
        // behandle relevante validierungsevents die wir anzeigen wollen
        this.regValSub = this.registrierungValidationService.registrierungValidationEventStrdeam$
            .subscribe(validations => {
                if (validations) {
                    LOG.debug('received validation events', validations);

                    const impffolge1Vals = [];
                    const impffolge2Vals = [];
                    const impffolgeNVals = [];

                    for (const validation of validations) {
                        switch (validation.type) {
                            case TSRegistrierungViolationType.WRONG_STATUS:
                                break;
                            case TSRegistrierungViolationType.NO_TERMIN:
                            case TSRegistrierungViolationType.WRONG_TERMIN:
                            case TSRegistrierungViolationType.WRONG_ODI:
                            case TSRegistrierungViolationType.USER_NOT_FOR_ODI:
                                if (validation.impffolge === ImpffolgeTS.ERSTE_IMPFUNG) {
                                    impffolge1Vals.push(validation);
                                } else if (validation.impffolge === ImpffolgeTS.ZWEITE_IMPFUNG) {
                                    impffolge2Vals.push(validation);
                                } else if (validation.impffolge === ImpffolgeTS.BOOSTER_IMPFUNG) {
                                    impffolgeNVals.push(validation);
                                }
                                break;
                            case TSRegistrierungViolationType.MAX_DAYS_NOT_VALID:
                                this.hasDateMaxError = !validation.clearEvent;
                                break;
                            case TSRegistrierungViolationType.MIN_DAYS_NOT_VALID:
                                this.hasDateMinError = !validation.clearEvent;
                                break;
                            case TSRegistrierungViolationType.DIFFERENT_IMPFSTOFF:
                                // can only be wrong on second impfung or booster
                                if (validation.impffolge === ImpffolgeTS.ZWEITE_IMPFUNG) {
                                    this.hasImpfstoffError = !validation.clearEvent;
                                } else if (validation.impffolge === ImpffolgeTS.BOOSTER_IMPFUNG) {
                                    this.hasImpfstoffError = !validation.clearEvent;
                                }
                                break;
                        }
                    }
                    // wenn eine relevante validierung vorhanden ist und eine der validations nicht gut ist dann zeigen
                    // wir das an
                    if (this.impffolge === ImpffolgeTS.ERSTE_IMPFUNG && impffolge1Vals.length !== 0) {
                        this.hasTermin1Error = !!impffolge1Vals.find(validation => !validation.clearEvent);
                        this.wrongPlaceOrWrongTimeTermin1 = this.hasTermin1Error;
                    }
                    if (this.impffolge === ImpffolgeTS.ZWEITE_IMPFUNG && impffolge2Vals.length !== 0) {
                        this.hasTermin2Error = !!impffolge2Vals.find(validation => !validation.clearEvent);
                    }
                    if (this.impffolge === ImpffolgeTS.BOOSTER_IMPFUNG && impffolgeNVals.length !== 0) {
                        this.hasTerminNError = !!impffolgeNVals.find(validation => !validation.clearEvent);
                    }

                    this.cdRef.detectChanges();
                }
            }, error => LOG.error(error));
    }

    public showTermin1(): boolean {
        return !this.termine.impfung1; // termin1 nur anzeigen wenn noch impfung1 noch nicht besteht
    }

    public showTermin2(): boolean {
        return !this.termine.impfung2; // termin2 nur anzeigen wenn noch impfung2 noch nicht besteht
    }

    public isAbgeschlossen(): boolean {
        return isAnyStatusOfCurrentlyAbgeschlossen(this.status);
    }

    public needsOnlyOneImpfung(): boolean {
        // Wir haben hier den Impfstoff nicht. Aber wenn der Status ABGESCHLOSSEN ist und es nur 1 Impfung gibt, kann
        // man den zweiten Termin/Impfung ausblenden
        return this.isAbgeschlossen() && !this.termine.termin2;
    }

    public getTerminOrImpfung1DatumText(): string {
        const currStatus = this.status;
        if (!currStatus) {
            return '';
        } else {
            if (isStatusLE(currStatus, RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT)) {
                return this.getTerminDateString(this.termine.termin1);
            } else {
                return this.getDateString(this.termine.impfung1?.timestampImpfung);
            }
        }
    }

    public getTerminOrImpfung2DatumText(): string {
        const currStatus = this.status;
        if (!currStatus) {
            return '';
        } else {
            if (this.needsOnlyOneImpfung()) {
                return this.translateService.instant('FACH-APP.KONTROLLE.TERMIN_NICHT_BENOETIGT');
            }
            if (isStatusLE(currStatus, RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT)) {
                return this.getTerminDateString(this.termine.termin2);
            } else {
                return this.getDateString(this.termine.impfung2?.timestampImpfung);
            }
        }
    }

    public getTerminDateString(termin: ImpfterminJaxTS | null | undefined): string {
        if (termin) {
            return this.getDateString(this.getTerminDatum(termin));
        }
        return this.translateService.instant('FACH-APP.KONTROLLE.TERMIN_NOCH_UNBEKANNT');
    }

    private getTerminDatum(termin: ImpfterminJaxTS | undefined): Date | null {
        if (!termin || !termin.impfslot || !termin.impfslot.zeitfenster || !termin.impfslot.zeitfenster.von) {
            return null;
        }
        return termin.impfslot.zeitfenster.von;
    }

    private getDateString(date: Date | undefined | null): string {
        if (date == null) {
            return '';
        }
        return formatDate(date, DateUtil.dateFormatMedium(this.translateService.currentLang), this.translateService.currentLang);
    }

    public getDataDiffStringTermin(impftermin: ImpfterminJaxTS | undefined): string {
        const date = this.extractDateFromTermin(impftermin);
        return date !== undefined ? this.getDataDiffString(date) : '';
    }

    private extractDateFromTermin(termin: ImpfterminJaxTS | undefined): Date | undefined {
        if (!termin) {
            return undefined;
        }
        const date: Date | null = this.getTerminDatum(termin);
        if (date === null || date === undefined) {
            return undefined;
        }
        return date;
    }

    private extractDateFromImpfung(impfung: ImpfungJaxTS | undefined): Date | undefined {
        if (!impfung) {
            return undefined;
        }
        const date: Date | null = impfung.timestampImpfung;
        if (date === null || date === undefined) {
            return undefined;
        }
        return date;
    }

    public getDataDiffStringImpfung(impfung: ImpfungJaxTS | undefined): string {
        const date = this.extractDateFromImpfung(impfung);
        return date !== undefined ? this.getDataDiffString(date) : '';
    }

    private getDataDiffString(date: Date): string {

        const daysDiff = DateUtil.getDaysDiff(date, DateUtil.now().toDate());
        if (daysDiff === 0) {
            return `(${this.translateService.instant('IMPFDOK.TERMIN.HEUTE')})`;
        }
        if (daysDiff > 0) {
            return `(${this.translateService.instant('IMPFDOK.TERMIN.IN')} ${daysDiff} ${this.translateService.instant('IMPFDOK.TERMIN.TAGEN')})`;
        }
        return `(${this.translateService.instant('IMPFDOK.TERMIN.SEIT')} ${Math.abs(daysDiff)} ${this.translateService.instant('IMPFDOK.TERMIN.TAGEN')})`;
    }

    public showTermin1or2Anpassen(): boolean {
        // Nur OI-Rollen duerfen Termine (um)buchen
        if (this.authServices.isOneOfRoles([TSRole.OI_KONTROLLE, TSRole.OI_DOKUMENTATION, TSRole.OI_IMPFVERANTWORTUNG])) {
            // Termine koennen nur verschoben werden, wenn mind. der Zweite noch nicht wahrgenommen wurde und
            // wenn der Benutzer ein Odi mit Terminbuchung hat (und daher das show flag gesetzt ist)
            return this.showTermin2() && this.showTerminumbuchungButtons && !this.isAbgeschlossen();
        }
        return false;
    }

    public showTerminBoosterAnpassen(): boolean {
        // Nur OI-Rollen duerfen Termine (um)buchen
        if (this.authServices.isOneOfRoles([TSRole.OI_KONTROLLE, TSRole.OI_DOKUMENTATION, TSRole.OI_IMPFVERANTWORTUNG])) {
            return this.showTerminumbuchungButtons && this.impffolge === ImpffolgeTS.BOOSTER_IMPFUNG;
        }
        return false;
    }

    public showAdHocTermin1(): boolean {
        // AdHoc Termin 1 kann nur estellt werden, solange Termin 1 noch nicht wahrgenommen wurde.
        // und wenn der Benutzer ein Odi mit Terminbuchung hat (und daher das show flag gesetzt ist)
        return this.showTermin1() && this.wrongPlaceOrWrongTimeTermin1 && this.showTerminumbuchungButtons;
    }

    public getImpfzentrumTermin1(): string {
        return this.getImpfzentrumTermin(this.termine.termin1);
    }

    public getImpfzentrumTermin2(): string {
        return this.getImpfzentrumTermin(this.termine.termin2);
    }

    public getImpfzentrumTerminN(): string {
        return this.getImpfzentrumTermin(this.termine.terminNPending);
    }

    private getImpfzentrumTermin(termin: ImpfterminJaxTS | undefined): string {
        if (!termin || !termin.impfslot || !termin.impfslot.ortDerImpfung || !termin.impfslot.ortDerImpfung.name) {
            return '';
        }
        return termin.impfslot.ortDerImpfung.name;
    }

    public getImpfstoffImpfung1(): string {
        return this.getImpfstoff(this.termine.impfung1);
    }

    public getImpfstoffImpfung2(): string {
        return this.getImpfstoff(this.termine.impfung2);
    }

    public getImpfstoff(impfung: ImpfungJaxTS | undefined): string {
        if (!impfung) {
            return '';
        }
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        return impfung.impfstoff?.displayName!;
    }

    public getBemerkungImpfung1(): string {
        return this.getBemerkung(this.termine.impfung1);
    }

    public getBemerkungImpfung2(): string {
        return this.getBemerkung(this.termine.impfung2);
    }

    public getBemerkung(impfung: ImpfungJaxTS | undefined): string {
        if (!impfung || !impfung.bemerkung) {
            return '';
        }
        return this.translateService.instant('FACH-APP.KONTROLLE.BEMERKUNGEN') + impfung.bemerkung;
    }

    ngOnDestroy(): void {
        this.regValSub.unsubscribe();
    }

    public termineAnpassen(): void {
        this.termineUmbuchenCalled.emit();
    }

    public adHocTermin1AndSelectTermin2(): void {
        this.adHocTermin1AndSelectTermin2Called.emit();
    }

}
