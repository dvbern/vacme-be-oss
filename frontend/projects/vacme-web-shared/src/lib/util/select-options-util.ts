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

import {KrankheitIdentifierTS, VerarbreichungsartTS} from 'vacme-web-generated';
import {LogFactory} from '../logging';
import {Option} from '../components/form-controls/input-select/option';

const LOG = LogFactory.createLog('SelectOptionsUtil');

export class SelectOptionsUtil {

    public static zahlungstypOptions(krankheit: KrankheitIdentifierTS | undefined): Option[] {
        if (!krankheit) {
            return [];
        }
        // Default, wenn keine spezifische Kampagne
        let labelKrankenkassenbezahlung = 'ZAHLUNGSTYP_KRANKENKASSE';
        if (krankheit === KrankheitIdentifierTS.COVID) {
            labelKrankenkassenbezahlung = 'IMPFKAMPAGNE_COVID';
        }
        const zahlungstypOptions: Option[] = [
            {label: 'SELBSTZAHLENDE', value: true},
            {label: labelKrankenkassenbezahlung, value: false},
        ];
        return zahlungstypOptions;
    }

    public static verabreichungsartOptions(supportedVerabreichungsarten: Array<VerarbreichungsartTS>): Option[] {
        return supportedVerabreichungsarten.map(t => {
            return {label: t, value: t};
        });
    }
}
