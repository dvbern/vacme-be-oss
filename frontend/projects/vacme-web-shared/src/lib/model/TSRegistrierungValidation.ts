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

import {ImpffolgeTS} from 'vacme-web-generated';
import {TSErrorLevel} from './TSErrorLevel';
import {TSRegistrierungViolationType} from './TSRegistrierungViolationType';

export default class TSRegistrierungValidation {
    public type: TSRegistrierungViolationType;
    public message: string;
    public impffolge?: ImpffolgeTS;
    public severity?: TSErrorLevel = TSErrorLevel.ERROR;
    public clearEvent = false; // bedeutet, dass eine Warnung _nicht_ gilt

    constructor(type: TSRegistrierungViolationType, message: string, impffolge?: ImpffolgeTS,
                clearEvent: boolean = false) {
        this.type = type;
        this.impffolge = impffolge;
        this.message = message;
        this.clearEvent = clearEvent;
    }

    public static createClearEvent(type: TSRegistrierungViolationType,
                                   impffolge?: ImpffolgeTS): TSRegistrierungValidation {
        const tsRegistrierungValidation = new TSRegistrierungValidation(type, '', impffolge, true);
        tsRegistrierungValidation.severity = undefined;
        return tsRegistrierungValidation;
    }

}
