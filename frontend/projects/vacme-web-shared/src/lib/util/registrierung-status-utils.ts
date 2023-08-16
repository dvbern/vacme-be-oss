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

// noinspection MagicNumberJS

import {ImpfdossierStatusTS, RegistrierungStatusTS} from 'vacme-web-generated';

export function getAtLeastFreigegebenValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.FREIGEGEBEN,
        RegistrierungStatusTS.ODI_GEWAEHLT,
        RegistrierungStatusTS.GEBUCHT,
        RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        RegistrierungStatusTS.ABGESCHLOSSEN,
        RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        RegistrierungStatusTS.IMMUNISIERT,
        RegistrierungStatusTS.FREIGEGEBEN_BOOSTER,
        RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        RegistrierungStatusTS.GEBUCHT_BOOSTER,
        RegistrierungStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastFreigegeben(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastFreigegebenValues().indexOf(status) !== -1;
}

export function getAtLeastFreigegebenBoosterValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.FREIGEGEBEN_BOOSTER,
        RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        RegistrierungStatusTS.GEBUCHT_BOOSTER,
        RegistrierungStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isErsteImpfungDoneAndZweitePending(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }

    return RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT === status
        || RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT === status;
}

export function isAtLeastFreigegebenBooster(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastFreigegebenBoosterValues().indexOf(status) !== -1;
}

export function getAtLeastOdiGewaehltButNotYetGeimpftValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.ODI_GEWAEHLT,
        RegistrierungStatusTS.GEBUCHT,
        RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT,

        RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        RegistrierungStatusTS.GEBUCHT_BOOSTER,
        RegistrierungStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastOdiGewaehltButNotYetGeimpftValues(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastOdiGewaehltButNotYetGeimpftValues().indexOf(status) !== -1;
}

export function getAtLeastOdiGewaehltValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.ODI_GEWAEHLT,
        RegistrierungStatusTS.GEBUCHT,
        RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        RegistrierungStatusTS.ABGESCHLOSSEN,
        RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        RegistrierungStatusTS.IMMUNISIERT,
        RegistrierungStatusTS.FREIGEGEBEN_BOOSTER,
        RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        RegistrierungStatusTS.GEBUCHT_BOOSTER,
        RegistrierungStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastOdiGewaehlt(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastOdiGewaehltValues().indexOf(status) !== -1;
}

export function getAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.ODI_GEWAEHLT,
        RegistrierungStatusTS.GEBUCHT,
        RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT,

        RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        RegistrierungStatusTS.GEBUCHT_BOOSTER,
        RegistrierungStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues().indexOf(status) !== -1;
}

export function getAtLeastGebuchtOrOdiGewaehltValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.GEBUCHT,
        RegistrierungStatusTS.ODI_GEWAEHLT,
        RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        RegistrierungStatusTS.ABGESCHLOSSEN,
        RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        RegistrierungStatusTS.IMMUNISIERT,
        RegistrierungStatusTS.FREIGEGEBEN_BOOSTER,
        RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        RegistrierungStatusTS.GEBUCHT_BOOSTER,
        RegistrierungStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastGebuchtOrOdiGewaehlt(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastGebuchtOrOdiGewaehltValues().indexOf(status) !== -1;
}

export function getAtLeastGebuchtValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.GEBUCHT,
        RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        RegistrierungStatusTS.ABGESCHLOSSEN,
        RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        RegistrierungStatusTS.IMMUNISIERT,
        RegistrierungStatusTS.FREIGEGEBEN_BOOSTER,
        RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        RegistrierungStatusTS.GEBUCHT_BOOSTER,
        RegistrierungStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastGebucht(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastGebuchtValues().indexOf(status) !== -1;
}

export function getAtLeastOnceGeimpftValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT,
        RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        RegistrierungStatusTS.ABGESCHLOSSEN,
        RegistrierungStatusTS.IMMUNISIERT,
        RegistrierungStatusTS.FREIGEGEBEN_BOOSTER,
        RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        RegistrierungStatusTS.GEBUCHT_BOOSTER,
        RegistrierungStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

// TODO booster Verwendung pruefen.
//  Es heisst nicht, dass man eine Vacme-Impfung hat, man koennte auch externesZertifikat haben.
// Sonst mus man hasVacmeImpfung verwenden!
export function isAtLeastOnceGeimpft(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastOnceGeimpftValues().indexOf(status) !== -1;
}

export function getCurrentlyAbgeschlossenValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        RegistrierungStatusTS.ABGESCHLOSSEN,
        RegistrierungStatusTS.IMMUNISIERT,
    ];
}

export function isCurrentlyAbgeschlossen(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getCurrentlyAbgeschlossenValues().indexOf(status) !== -1;
}

export function getAnyStatusOfCurrentlyAbgeschlossenValues(): Array<RegistrierungStatusTS> {
    return [
        RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        RegistrierungStatusTS.ABGESCHLOSSEN,
        RegistrierungStatusTS.IMMUNISIERT,
    ];
}

export function isAnyStatusOfCurrentlyAbgeschlossen(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAnyStatusOfCurrentlyAbgeschlossenValues().indexOf(status) !== -1;
}

export function getAnyStatusOfBooster(): Array<RegistrierungStatusTS> {
    // Die "alten" Abgeschlosen-Status sind hier explizit nicht dabei. Wir gehen davon aus,
    // dass mit dem Beginn der BoosterImpfungen alle mit erfolgter Grundimmunisierung direkt
    // in den Status IMMUNISIERT kommen. So koennen wir einfacher zwischen dem Status "vorher"
    // (mit den alten ageschlossen-Status) und den neuen Booster-Abfolge unterscheiden.
    return [
        RegistrierungStatusTS.IMMUNISIERT,
        RegistrierungStatusTS.FREIGEGEBEN_BOOSTER,
        RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER,
        RegistrierungStatusTS.GEBUCHT_BOOSTER,
        RegistrierungStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAnyStatusOfBooster(status: RegistrierungStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAnyStatusOfBooster().indexOf(status) !== -1;
}

/**
 * diese Methode definiert die Reihenfolge der Status.
 * Achtung, ab "Booster" darf die Reihenfolge nicht mehr einfach ungesehen auf aufsteigende Ordinalzahl geprueft werden
 * weil mehrere durchlauefe durch den Boosterprozess moeglich sind
 */
export function getStatusOrdinal(status: RegistrierungStatusTS): number {
    if (status === undefined || status === null) {
        return -1;
    } else {
        switch (status) {
            case RegistrierungStatusTS.REGISTRIERT:
                return 1;

            case RegistrierungStatusTS.FREIGEGEBEN:
                return 2;

            case RegistrierungStatusTS.ODI_GEWAEHLT:
                return 3;
            case RegistrierungStatusTS.GEBUCHT:
                return 4;

            case RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT:
                return 5;
            case RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT:
                return 6;
            case RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT:
                return 7;
            case RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT:
                return 8;
            case RegistrierungStatusTS.ABGESCHLOSSEN:
            case RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG:
            case RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN:
                return 9;
            case RegistrierungStatusTS.IMMUNISIERT:
                return 10;
            case RegistrierungStatusTS.FREIGEGEBEN_BOOSTER:
                return 11;
            case RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER:
                return 12;
            case RegistrierungStatusTS.GEBUCHT_BOOSTER:
                return 13;
            case RegistrierungStatusTS.KONTROLLIERT_BOOSTER:
                return 14;
        }
    }
}

export const isStatusLE = (status: RegistrierungStatusTS, otherStatus: RegistrierungStatusTS): boolean => {
    if (isAnyStatusOfBooster(status) && isAnyStatusOfBooster(otherStatus)) {
        throw Error('isStatusLE kann zwischen Booster-Stati nicht mehr verwendet werden, '
            + 'da diese in einer Schlaufe immer wieder durchlaufen werden! ');
    }
    const ordinal1: number = getStatusOrdinal(status);
    const ordinal2: number = getStatusOrdinal(otherStatus);
    return ordinal1 <= ordinal2;
};

export function fromImpfdossierStatus(dossierStatus: ImpfdossierStatusTS): RegistrierungStatusTS {
    switch (dossierStatus) {
        case ImpfdossierStatusTS.FREIGEGEBEN:
            return RegistrierungStatusTS.FREIGEGEBEN;
        case ImpfdossierStatusTS.ODI_GEWAEHLT:
            return RegistrierungStatusTS.ODI_GEWAEHLT;
        case ImpfdossierStatusTS.GEBUCHT:
            return RegistrierungStatusTS.GEBUCHT;
        case ImpfdossierStatusTS.IMPFUNG_1_KONTROLLIERT:
            return RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT;
        case ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT:
            return RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT;
        case ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT:
            return RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT;
        case ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT:
            return RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT;
        case ImpfdossierStatusTS.ABGESCHLOSSEN:
            return RegistrierungStatusTS.ABGESCHLOSSEN;
        case ImpfdossierStatusTS.AUTOMATISCH_ABGESCHLOSSEN:
            return RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN;
        case ImpfdossierStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG:
            return RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG;
        case ImpfdossierStatusTS.IMMUNISIERT:
            return RegistrierungStatusTS.IMMUNISIERT;
        case ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER:
            return RegistrierungStatusTS.FREIGEGEBEN_BOOSTER;
        case ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER:
            return RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER;
        case ImpfdossierStatusTS.GEBUCHT_BOOSTER:
            return RegistrierungStatusTS.GEBUCHT_BOOSTER;
        case ImpfdossierStatusTS.KONTROLLIERT_BOOSTER:
            return RegistrierungStatusTS.KONTROLLIERT_BOOSTER;
        case ImpfdossierStatusTS.NEU:
            return RegistrierungStatusTS.REGISTRIERT;
    }
}
