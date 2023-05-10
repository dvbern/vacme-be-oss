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

import {ErkrankungDatumHerkunftTS, ErkrankungJaxTS, RegistrierungStatusTS} from 'vacme-web-generated';
import {isAnyStatusOfBooster} from './registrierung-status-utils';

export class ErkrankungUtil {

    public static isEditableErkrankungByImpfling(erkrankung: ErkrankungJaxTS): boolean {
        return this.isEditableErkrankungHerkunftByImpfling(erkrankung.erkrankungdatumHerkunft);
    }
    public static isEditableErkrankungHerkunftByImpfling(herkunft?: ErkrankungDatumHerkunftTS): boolean {
        return herkunft === ErkrankungDatumHerkunftTS.ERFASST_IMPFLING;
    }

    public static canViewErkrankungen(status?: RegistrierungStatusTS): boolean {
        // Erkrankungen (Selbstdeklaration) sollen nur im Booster-Status editiert werden koennen
        return isAnyStatusOfBooster(status);
    }

    public static canEditErkrankungen(status?: RegistrierungStatusTS): boolean {
        return this.canViewErkrankungen(status)
            && status !== RegistrierungStatusTS.KONTROLLIERT_BOOSTER;
    }

}
