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

import {KrankheitIdentifierTS, RegistrierungStatusTS} from 'vacme-web-generated';

/**
 * Entspricht ungefaehr den Felder die ueber JaxAuthAccessElement in das Cookie gesetzt werden.
 * Zusaetzliche Felder werden aus DB geladen.
 */
export default class TSPersonFolgetermin {

    public krankheit?: KrankheitIdentifierTS | undefined;
    public status: RegistrierungStatusTS | undefined;
    public freigegebenNaechsteImpfungAb?: Date;
    public terminNPending?: boolean;

    constructor(
        status: RegistrierungStatusTS | undefined,
        freigegebenNaechsteImpfungAb: Date | undefined,
        terminNPending: boolean | undefined,
        krankheit: KrankheitIdentifierTS | undefined
) {
        this.status = status;
        this.freigegebenNaechsteImpfungAb = freigegebenNaechsteImpfungAb;
        this.terminNPending = terminNPending;
        this.krankheit = krankheit;
    }
}
