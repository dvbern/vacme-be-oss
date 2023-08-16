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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import {ImpfdossierSummaryJaxTS, ImpfungJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import {TSRole} from '../../../model';
import {AuthServiceRsService} from '../../../service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../service/vacme-settings.service';
import DateUtil from '../../../util/DateUtil';
import {ExternGeimpftUtil} from '../../../util/externgeimpft-util';
import {
    isAnyStatusOfBooster,
    isAtLeastFreigegeben,
    isAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues,
    isInFreigegebenStatus,
} from '../../../util/impfdossiert-status-utils';

@Component({
    selector: 'lib-impfdossier-summary-list-item',
    templateUrl: './impfdossier-summary-list-item.component.html',
    styleUrls: ['./impfdossier-summary-list-item.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImpfdossierSummaryListItemComponent {

    @Input() impfdossierSummary?: ImpfdossierSummaryJaxTS;
    @Input() fachapp = true;

    @Output()
    public downloadEvent = new EventEmitter<{ registrierungsnummer: string; krankheitIdentifier: KrankheitIdentifierTS }>();
    public krankheiten = KrankheitIdentifierTS;
    public dateUtil = DateUtil;

    constructor(
        public translationService: TranslateService,
        private authService: AuthServiceRsService,
        private vacmeSettingsService: VacmeSettingsService,
    ) {
    }

    public getDossierTitle(): string {
        return this.translationService.instant('KRANKHEITEN.' + this.getKrankheit());
    }

    public getLetzteImpfungAm(impfung?: ImpfungJaxTS): string {
        if (impfung) {
            let textForLetzteImpfung = this.translationService.instant('IMPFDOSSIER_SUMMARY.IMPFUNGSDATEN_LETZTE_IMPFUNG', {
                    impfstoff: impfung.impfstoff?.displayName,
                    timestampImpfung: DateUtil.dateAsLocalDateString(impfung.timestampImpfung),
                });

            if (ExternGeimpftUtil.showSchnellschema(this.getKrankheit())) {
                const schnellSchema = impfung.schnellschemaGesetztFuerImpfung;
                const schnellSchemaText = this.translationService.instant('IMPFDOK.IMPF_SCHEMA_OPTION.' + (schnellSchema ?
                    'SCHNELLSCHEMA' :
                    'KONVENTIONELL')) as string;
                textForLetzteImpfung += ` (${schnellSchemaText})`;
            }

            return textForLetzteImpfung;
        } else {
            return this.translationService.instant('IMPFDOSSIER_SUMMARY.IMPFUNG_AUSSTEHEND');
        }
    }

    public showTermin(): boolean {
        return isAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues(this.impfdossierSummary?.status)
            || !!this.impfdossierSummary?.nichtVerwalteterOdiSelected;
    }

    public getTerminText(): string {
        // Termin bei nicht verwaltetem ODI
        if (this.impfdossierSummary?.nichtVerwalteterOdiSelected) {
            return this.translationService.instant(
                'IMPFDOSSIERS_OVERVIEW.IMPFDOSSIER_SUMMARY_LIST_ITEM.TERMIN_MANUELL_ABGEMACHT');
        }

        // Maybe termin or Odi ohne terminbuchung
        if (this.impfdossierSummary?.nextTermin) {
            return this.translationService.instant('IMPFDOSSIERS_OVERVIEW.IMPFDOSSIER_SUMMARY_LIST_ITEM.TERMIN', {
                date: DateUtil.dateAsLocalDateString(this.impfdossierSummary?.nextTermin.impfslot?.zeitfenster?.von),
                time: DateUtil.dateAsLocalDateTimeString(this.impfdossierSummary.nextTermin.impfslot?.zeitfenster?.von,
                    'HH:mm'),
            });
        }
        return this.translationService.instant(
            'IMPFDOSSIERS_OVERVIEW.IMPFDOSSIER_SUMMARY_LIST_ITEM.TERMIN_MANUELL_ABGEMACHT');
    }

    showTermineAvailable(): boolean {
        // Kein Termin, freigegeben aber noch nicht gebucht
        if (!this.impfdossierSummary?.nextTermin
            && isInFreigegebenStatus(this.impfdossierSummary?.status)
            && !this.impfdossierSummary?.krankheit.noFreieTermine
            && !this.impfdossierSummary?.nichtVerwalteterOdiSelected) {
            return true;
        }
        return false;
    }

    hasRoleDokumentation(): boolean {
        return this.authService.isOneOfRoles(
            [TSRole.OI_DOKUMENTATION, TSRole.KT_NACHDOKUMENTATION, TSRole.KT_MEDIZINISCHE_NACHDOKUMENTATION]);
    }

    triggerDownloadImpfdokumentation(): void {
        if (this.impfdossierSummary) {
            this.downloadEvent.emit({
                    registrierungsnummer: this.impfdossierSummary.registrierungsnummer,
                    krankheitIdentifier: this.getKrankheit(),
                },
            );
        }
    }

    public getKrankheit(): KrankheitIdentifierTS {
        if (this.impfdossierSummary?.krankheit.identifier === undefined
            || this.impfdossierSummary.krankheit.identifier === null) {
            throw new Error('Krankheit nicht gesetzt ' + this.impfdossierSummary?.registrierungsnummer);
        }
        return this.impfdossierSummary.krankheit.identifier;
    }

    public showNoFreieTermine(): boolean {
        return !this.showTermin() && !!this.impfdossierSummary?.krankheit.noFreieTermine;
    }

    public hasNoFreigabeAndIsNotInBoosterstatus(): boolean {
        return !isAnyStatusOfBooster(this.impfdossierSummary?.status)
            && !isAtLeastFreigegeben(this.impfdossierSummary?.status);
    }

    public hasNoFreigabeAndIsInBoosterstatus(): boolean {
        const boosterStatus = isAnyStatusOfBooster(this.impfdossierSummary?.status);
        const hasNoFreigabe = this.hasNoFreigabeFromImpfschutz();
        const isNotAlreadyFreigegeben = !isInFreigegebenStatus(this.impfdossierSummary?.status);
        return boosterStatus && hasNoFreigabe && isNotAlreadyFreigegeben;
    }

    private hasNoFreigabeFromImpfschutz(): boolean {
        // TODO Affenpocken: und wenn es keine Freigabe gibt, weil keine weitere Impfung notwendig ist? z.B. Zecken
        const freigabeDate = this.impfdossierSummary?.freigabeNaechsteImpfung;
        return !freigabeDate || DateUtil.isAfterToday(freigabeDate);
    }

    public hasExterneImpfungFrageBeantwortet(): boolean {
        return this.impfdossierSummary?.externGeimpftConfirmationNeeded !== true;
    }

    public isBoosterFreigabeNotificationDisabled(): boolean {
        if (this.impfdossierSummary) {
            return !this.vacmeSettingsService.supportsTerminFreigabeSMS(this.impfdossierSummary.krankheit.identifier);
        }
        return this.vacmeSettingsService.boosterFreigabeNotificationDisabled;
    }
}
