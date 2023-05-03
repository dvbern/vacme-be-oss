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

import {Injectable} from '@angular/core';
/* eslint-disable max-len */
import {
    DashboardJaxTS,
    DateTimeRangeJaxTS,
    ImpffolgeTS,
    ImpfslotJaxTS,
    ImpfterminJaxTS,
    OrtDerImpfungBuchungJaxTS,
    OrtDerImpfungDisplayNameJaxTS,
    RegistrierungStatusTS,
} from 'vacme-web-generated';
import DateUtil from '../util/DateUtil';
import {
    isAnyStatusOfBooster,
    isAtLeastOnceGeimpft,
    isErsteImpfungDoneAndZweitePending,
} from '../util/registrierung-status-utils';
import {VacmeSettingsService} from './vacme-settings.service';

@Injectable({
    providedIn: 'root',
})
export class TerminUtilService {

    constructor(
        private vacmeSettingsService: VacmeSettingsService,
    ) {
    }

    public static determineImpffolgeNr(dashboardJax: DashboardJaxTS): number {
        let value = 1;
        value = dashboardJax.impfung1 ? value + 1 : value;
        value = dashboardJax.impfung2 ? value + 1 : value;
        if (dashboardJax.impfdossier && dashboardJax.impfdossier.impfdossiereintraege) {
            dashboardJax.impfdossier.impfdossiereintraege.forEach(eintrag => {
                value = eintrag.impfung ? value + 1 : value;
            });
        }
        if (dashboardJax.externGeimpft) {
            value += dashboardJax.externGeimpft.anzahlImpfungen || 0;
        }
        return value;
    }

    public static determineImpffolgeNrForImpfung1Or2(
        impffolge: ImpffolgeTS,
        dashboardJax: DashboardJaxTS
    ): number {
        let value = 0;
        switch (impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                value += 1;
                break;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                value += 2;
                break;
            default:
                throw new Error('falsche Impffolgenummer');
        }
        if (dashboardJax.externGeimpft) {
            value += dashboardJax.externGeimpft.anzahlImpfungen || 0;
        }
        return value;
    }

    public isCorrectAbstandZweiterTerminMinMaxAndSameOdi(
        slot1: ImpfslotJaxTS | undefined,
        slot2: ImpfslotJaxTS | undefined,
        status: RegistrierungStatusTS | undefined,
    ): boolean {
        // Der Abstand muss nur geprueft werden, wenn beide Termine gesetzt sind, und die Person noch nicht geimpft ist
        if (slot1 && slot2 && !isAtLeastOnceGeimpft(status)
        ) {
            if (!this.isCorrectAbstandZweiterTerminMinMax(slot1, slot2, status)) {
                return false;
            }
            if (slot1?.ortDerImpfung
                && slot2?.ortDerImpfung
                && slot1?.ortDerImpfung?.id !== slot2?.ortDerImpfung?.id) {
                return false;
            }
        }
        return true;
    }

    public isCorrectAbstandZweiterTerminMinMax(
        slot1: ImpfslotJaxTS | undefined,
        slot2: ImpfslotJaxTS | undefined,
        status: RegistrierungStatusTS | undefined,
    ): boolean {
        // Der Abstand muss nur geprueft werden, wenn beide Termine gesetzt sind, und die Person noch nicht geimpft ist
        if (slot1 && slot2 && !isAtLeastOnceGeimpft(status)
        ) {
            const dateDff = this.getDaysDiff(slot2, slot1);
            if (dateDff < this.getMinDiff()) {
                return false;
            }
            if (dateDff > this.getMaxDiff()) {
                return false;
            }
        }
        return true;
    }

    public isCorrectAbstandZweiterTermin(
        slot1: ImpfslotJaxTS | undefined,
        slot2: ImpfslotJaxTS | undefined,
        status: RegistrierungStatusTS | undefined,
    ): boolean {
        // Der Abstand muss nur geprueft werden, wenn beide Termine gesetzt sind, und die Person noch nicht geimpft ist
        if (slot1 && slot2 && !isAtLeastOnceGeimpft(status)
        ) {
            const dateDff = this.getDaysDiff(slot2, slot1);
            if (dateDff < this.getMinDiff()) {
                return false;
            }
        }
        return true;
    }

    public getDaysDiff(
        slotX: ImpfslotJaxTS,
        slotY: ImpfslotJaxTS,
    ): number {
        return DateUtil.getDaysDiff(
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            slotX.zeitfenster!.von,
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            slotY.zeitfenster!.von);
    }

    public getMinDiff(): number {
        return this.vacmeSettingsService.distanceImpfungenMinimal();
    }

    public getMaxDiff(): number {
        return this.vacmeSettingsService.distanceImpfungenMaximal();
    }

    public createAdHocTermin(displaytext: string, odi: OrtDerImpfungDisplayNameJaxTS): ImpfslotJaxTS {
        const pseudoZeitfenster: DateTimeRangeJaxTS = {
            von: DateUtil.now().toDate(),
            vonDisplay: displaytext,
            bis: DateUtil.now().toDate(),
            bisDisplay: '',
            exactDisplay: displaytext,
        };
        const pseudoTermin: ImpfslotJaxTS = {
            ortDerImpfung: odi,
            zeitfenster: pseudoZeitfenster,
        };
        return pseudoTermin;
    }

    public hasTerminverwaltungInTerminOdi(termin: ImpfterminJaxTS | undefined): boolean {
        if (!termin || !termin.impfslot || !termin.impfslot.ortDerImpfung) {
            return false;
        }
        return !!termin.impfslot.ortDerImpfung.terminverwaltung;
    }

    isNotSameODI(impffolge: ImpffolgeTS, odiId: string | undefined, dashboardJax: DashboardJaxTS | undefined): boolean {
        if (!odiId) { // Kein odi. Keine Warnung!
            return false;
        }
        // keine Terminbuchung, keine Warnung
        if (dashboardJax?.krankheitIdentifier && !this.vacmeSettingsService.supportsTerminbuchung(dashboardJax?.krankheitIdentifier)) {
            return false;
        }
        switch (impffolge) { // haengt von der Impffolge welchen Termin wichtig ist
            case ImpffolgeTS.ERSTE_IMPFUNG:
                if (dashboardJax?.termin1?.impfslot?.ortDerImpfung?.id) { // Termin 1 hat ein ODI, dann danach kontrollieren
                    return odiId !== dashboardJax?.termin1?.impfslot?.ortDerImpfung?.id;
                }
                if (dashboardJax?.gewuenschterOrtDerImpfung) { // Kein Termin 1 definiert, dann nach dem gewuenschten ODI kontrollieren
                    return odiId !== dashboardJax?.gewuenschterOrtDerImpfung?.id;
                }
                return false; // kein Termin 1 und kein gewuenschter ODI, dann keine Warnung
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                if (dashboardJax?.termin2?.impfslot?.ortDerImpfung?.id) { // Termin 2 hat ein ODI, dann danach kontrollieren
                    return odiId !== dashboardJax?.termin2?.impfslot?.ortDerImpfung?.id;
                }
                if (dashboardJax?.gewuenschterOrtDerImpfung) { // Kein Termin 2 definiert, dann nach dem gewuenschten ODI kontrollieren
                    return odiId !== dashboardJax?.gewuenschterOrtDerImpfung?.id;
                }
                return false; // kein Termin 2 und kein gewuenschter ODI, dann keine Warnung
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                if (dashboardJax?.terminNPending?.impfslot?.ortDerImpfung?.id) { // Termin N hat ein ODI, dann danach kontrollieren
                    return odiId !== dashboardJax?.terminNPending?.impfslot?.ortDerImpfung?.id;
                }
                if (dashboardJax?.gewuenschterOrtDerImpfung) { // Kein Termin N definiert, dann nach dem gewuenschten ODI kontrollieren
                    return odiId !== dashboardJax?.gewuenschterOrtDerImpfung?.id;
                }
                return false;
        }
    }

    public hasOdiNoFreieTermineForMe(dashboard: DashboardJaxTS, odi: OrtDerImpfungBuchungJaxTS): boolean | undefined {
        if (isErsteImpfungDoneAndZweitePending(dashboard.status)) {
            // Reg hat erst eine Impfung, d.h. die freien Ersttermine interessieren nicht mehr
            return odi.noFreieTermine2;
        }
        if (isAnyStatusOfBooster(dashboard.status)) {
            // Reg ist schon grundimmunisiert, d.h. die freien Erst und zweittermine interessieren nicht mehr
            return odi.noFreieTermineN;
        }
        return odi.noFreieTermine1;
    }

}
