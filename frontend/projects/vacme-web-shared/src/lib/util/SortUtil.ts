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

import {FormGroup} from '@angular/forms';

/**
 * Hilfsmethoden fuer die Sortierung
 */
export class SortUtil {

    /**
     * Gibt den Wert des ueber field referenzierten Properties zurueck.
     * Im Beispiel: data.mitglied.name
     *
     * @param data Datenobjekt oder FormGroup
     * @param field z.B. 'mitglied.name'
     */
    public static getData(data: any, field: string): any {
        const strings = field.split('.');

        let value = data;
        for (const item of strings) {
            if (value) {
                if (value instanceof FormGroup) {
                    // aus der FormGroup das Control und dann den Wert holen; z.B. bei MeldungItemTabelleComponent
                    value = value.get([item])?.value;
                } else {
                    value = value[item];
                }
            } else {
                break; // referenzierten Property nicht vorhanden
            }
        }
        return value;
    }

    /* eslint-disable complexity */
    /**
     * Behandelt den vergleich von NULL-Werten und Strings.
     * Bei anderen Typen wird ein Vergleich mit groesser/kleiner gleich durchgefuehrt.
     *
     * @param value1 Wert 1
     * @param value2 Wert 2
     */
    public static handleCompareStandard(value1?: any, value2?: any): number {

        // undefined wird bei der Sortierung wie null behandelt
        const value1Null = value1 === null || value1 === undefined;
        const value2Null = value2 === null || value2 === undefined;
        if (value1Null && !value2Null) {
            return 1;
        }

        if (!value1Null && value2Null) {
            return -1;
        }

        if (value1Null && value2Null) {
            return 0;
        }
        if (typeof value1 === 'string' && typeof value2 === 'string') {
            return this.localCompareStr(value1, value2);
        }

        return (value1 < value2) ? -1 : (value1 > value2) ? 1 : 0;
    }

    /**
     * Vergleicht zwei Strings.
     */
    public static localCompareStr(str1: string, str2: string): number {
        return str1.localeCompare(str2);
    }
}
