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

import {ImpfdossierStatusTS} from 'vacme-web-generated';

export function getFreigegebenValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.FREIGEGEBEN,
        ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER,
    ];
}

export function isInFreigegebenStatus(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getFreigegebenValues().indexOf(status) !== -1;
}

export function getAtLeastFreigegebenValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.FREIGEGEBEN,
        ImpfdossierStatusTS.ODI_GEWAEHLT,
        ImpfdossierStatusTS.GEBUCHT,
        ImpfdossierStatusTS.IMPFUNG_1_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        ImpfdossierStatusTS.ABGESCHLOSSEN,
        ImpfdossierStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        ImpfdossierStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        ImpfdossierStatusTS.IMMUNISIERT,
        ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER,
        ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER,
        ImpfdossierStatusTS.GEBUCHT_BOOSTER,
        ImpfdossierStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastFreigegeben(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastFreigegebenValues().indexOf(status) !== -1;
}

export function getAtLeastFreigegebenBoosterValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER,
        ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER,
        ImpfdossierStatusTS.GEBUCHT_BOOSTER,
        ImpfdossierStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isErsteImpfungDoneAndZweitePending(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }

    return ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT === status
        || ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT === status;
}

export function isAtLeastFreigegebenBooster(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastFreigegebenBoosterValues().indexOf(status) !== -1;
}

export function getAtLeastOdiGewaehltButNotYetGeimpftValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.ODI_GEWAEHLT,
        ImpfdossierStatusTS.GEBUCHT,
        ImpfdossierStatusTS.IMPFUNG_1_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT,

        ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER,
        ImpfdossierStatusTS.GEBUCHT_BOOSTER,
        ImpfdossierStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastOdiGewaehltButNotYetGeimpftValues(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastOdiGewaehltButNotYetGeimpftValues().indexOf(status) !== -1;
}

export function getAtLeastOdiGewaehltValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.ODI_GEWAEHLT,
        ImpfdossierStatusTS.GEBUCHT,
        ImpfdossierStatusTS.IMPFUNG_1_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        ImpfdossierStatusTS.ABGESCHLOSSEN,
        ImpfdossierStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        ImpfdossierStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        ImpfdossierStatusTS.IMMUNISIERT,
        ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER,
        ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER,
        ImpfdossierStatusTS.GEBUCHT_BOOSTER,
        ImpfdossierStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastOdiGewaehlt(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastOdiGewaehltValues().indexOf(status) !== -1;
}

export function getAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.ODI_GEWAEHLT,
        ImpfdossierStatusTS.GEBUCHT,
        ImpfdossierStatusTS.IMPFUNG_1_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT,

        ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER,
        ImpfdossierStatusTS.GEBUCHT_BOOSTER,
        ImpfdossierStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues().indexOf(status) !== -1;
}

export function getAtLeastGebuchtOrOdiGewaehltValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.GEBUCHT,
        ImpfdossierStatusTS.ODI_GEWAEHLT,
        ImpfdossierStatusTS.IMPFUNG_1_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        ImpfdossierStatusTS.ABGESCHLOSSEN,
        ImpfdossierStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        ImpfdossierStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        ImpfdossierStatusTS.IMMUNISIERT,
        ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER,
        ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER,
        ImpfdossierStatusTS.GEBUCHT_BOOSTER,
        ImpfdossierStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastGebuchtOrOdiGewaehlt(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastGebuchtOrOdiGewaehltValues().indexOf(status) !== -1;
}

export function getAtLeastGebuchtValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.GEBUCHT,
        ImpfdossierStatusTS.IMPFUNG_1_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        ImpfdossierStatusTS.ABGESCHLOSSEN,
        ImpfdossierStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        ImpfdossierStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        ImpfdossierStatusTS.IMMUNISIERT,
        ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER,
        ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER,
        ImpfdossierStatusTS.GEBUCHT_BOOSTER,
        ImpfdossierStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAtLeastGebucht(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastGebuchtValues().indexOf(status) !== -1;
}

export function getAtLeastOnceGeimpftValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT,
        ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT,
        ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        ImpfdossierStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        ImpfdossierStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        ImpfdossierStatusTS.ABGESCHLOSSEN,
        ImpfdossierStatusTS.IMMUNISIERT,
        ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER,
        ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER,
        ImpfdossierStatusTS.GEBUCHT_BOOSTER,
        ImpfdossierStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

// TODO booster Verwendung pruefen.
//  Es heisst nicht, dass man eine Vacme-Impfung hat, man koennte auch externesZertifikat haben.
// Sonst mus man hasVacmeImpfung verwenden!
export function isAtLeastOnceGeimpft(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAtLeastOnceGeimpftValues().indexOf(status) !== -1;
}

export function getCurrentlyAbgeschlossenValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT,
        ImpfdossierStatusTS.ABGESCHLOSSEN,
        ImpfdossierStatusTS.IMMUNISIERT,
    ];
}

export function isCurrentlyAbgeschlossen(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getCurrentlyAbgeschlossenValues().indexOf(status) !== -1;
}

export function getAnyStatusOfCurrentlyAbgeschlossenValues(): Array<ImpfdossierStatusTS> {
    return [
        ImpfdossierStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG,
        ImpfdossierStatusTS.AUTOMATISCH_ABGESCHLOSSEN,
        ImpfdossierStatusTS.ABGESCHLOSSEN,
        ImpfdossierStatusTS.IMMUNISIERT,
    ];
}

export function isAnyStatusOfCurrentlyAbgeschlossen(status: ImpfdossierStatusTS | undefined): boolean {
    if (!status) {
        return false;
    }
    return getAnyStatusOfCurrentlyAbgeschlossenValues().indexOf(status) !== -1;
}

export function getAnyStatusOfBooster(): Array<ImpfdossierStatusTS> {
    // Die "alten" Abgeschlosen-Status sind hier explizit nicht dabei. Wir gehen davon aus,
    // dass mit dem Beginn der BoosterImpfungen alle mit erfolgter Grundimmunisierung direkt
    // in den Status IMMUNISIERT kommen. So koennen wir einfacher zwischen dem Status "vorher"
    // (mit den alten ageschlossen-Status) und den neuen Booster-Abfolge unterscheiden.
    return [
        ImpfdossierStatusTS.IMMUNISIERT,
        ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER,
        ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER,
        ImpfdossierStatusTS.GEBUCHT_BOOSTER,
        ImpfdossierStatusTS.KONTROLLIERT_BOOSTER,
    ];
}

export function isAnyStatusOfBooster(status: ImpfdossierStatusTS | undefined): boolean {
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
export function getStatusOrdinal(status: ImpfdossierStatusTS): number {
    if (status === undefined || status === null) {
        return -1;
    } else {
        switch (status) {
            case ImpfdossierStatusTS.NEU:
                return 1;
            case ImpfdossierStatusTS.FREIGEGEBEN:
                return 2;
            case ImpfdossierStatusTS.ODI_GEWAEHLT:
                return 3;
            case ImpfdossierStatusTS.GEBUCHT:
                return 4;
            case ImpfdossierStatusTS.IMPFUNG_1_KONTROLLIERT:
                return 5;
            case ImpfdossierStatusTS.IMPFUNG_1_DURCHGEFUEHRT:
                return 6;
            case ImpfdossierStatusTS.IMPFUNG_2_KONTROLLIERT:
                return 7;
            case ImpfdossierStatusTS.IMPFUNG_2_DURCHGEFUEHRT:
                return 8;
            case ImpfdossierStatusTS.ABGESCHLOSSEN:
            case ImpfdossierStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG:
            case ImpfdossierStatusTS.AUTOMATISCH_ABGESCHLOSSEN:
                return 9;
            case ImpfdossierStatusTS.IMMUNISIERT:
                return 10;
            case ImpfdossierStatusTS.FREIGEGEBEN_BOOSTER:
                return 11;
            case ImpfdossierStatusTS.ODI_GEWAEHLT_BOOSTER:
                return 12;
            case ImpfdossierStatusTS.GEBUCHT_BOOSTER:
                return 13;
            case ImpfdossierStatusTS.KONTROLLIERT_BOOSTER:
                return 14;
        }
    }
}

export const isStatusLE = (status: ImpfdossierStatusTS, otherStatus: ImpfdossierStatusTS): boolean => {
    if (isAnyStatusOfBooster(status) && isAnyStatusOfBooster(otherStatus)) {
        throw Error('isStatusLE kann zwischen Booster-Stati nicht mehr verwendet werden, '
            + 'da diese in einer Schlaufe immer wieder durchlaufen werden! ');
    }
    const ordinal1: number = getStatusOrdinal(status);
    const ordinal2: number = getStatusOrdinal(otherStatus);
    return ordinal1 <= ordinal2;
};
