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

import {LebensumstaendeTS} from 'vacme-web-generated';
import {Option} from '../components/form-controls/input-select/option';
import {LogFactory} from '../logging';
import TenantUtil from './TenantUtil';

const LOG = LogFactory.createLog('FragebogenUtil');

export class FragebogenUtil {

    public static lebensumstaendeOptions(): Option[] {
        let options = Object.values(LebensumstaendeTS);
        if (TenantUtil.hasSimplifiedLebensumstaende()) {
            // TODO Affenpocken make its own Enum?
            options = [
                LebensumstaendeTS.MIT_BESONDERS_GEFAEHRDETEN_PERSON,
                LebensumstaendeTS.GEMEINSCHAFTEN,
                LebensumstaendeTS.ANDERE,
            ];
        }

        return Object.values(options).map(t => {
            return {label: t, value: t};
        });
    };
}
