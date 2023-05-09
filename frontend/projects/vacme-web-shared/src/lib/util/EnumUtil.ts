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

import {Option} from '../components/form-controls/input-select/option';

export default class EnumUtil {

    /**
     * Sort the list to make sure an option with the value 'ANDERE' is always the last option
     *
     * @param a first option to compare
     * @param b second option to compare (currently ignored)
     */
    public static defaultEnumSorter(a: Option, b: Option): number {
        // if the value equals ANDERE it's "smaller" then the other option
        if (a.value !== undefined && a.value === 'ANDERE') {
            return 1;
        }

        // otherwise we keep the source sorting
        return 0;
    }

    public static ensureBackwardsCompatibleCache(cacheData: string): string {
        return cacheData
            // ZulassungsStatus
            .replace('EXTERNZUGELASSEN', 'EXTERN_ZUGELASSEN')
            .replace('NICHTZUGELASSEN', 'NICHT_ZUGELASSEN')
            .replace('NICHTWHOZUGELASSEN', 'NICHT_WHO_ZUGELASSEN')

            // Verarbreichungsart
            .replace('INTRAMUSKULAER', 'INTRA_MUSKULAER')

            // Verarbreichungsort
            .replace('ANDERERORT', 'ANDERER_ORT');
    }
}
