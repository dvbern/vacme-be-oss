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

import * as moment from 'moment';
import {KrankheitIdentifierTS} from 'vacme-web-generated';
import {
    MIN_DATE_FOR_EXTERNE_IMPFUNGEN,
    MIN_DATE_FOR_EXTERNE_IMPFUNGEN_COVID, MIN_DATE_FOR_IMPFUNGEN_AFFENPOCKEN, MIN_DATE_FOR_IMPFUNGEN_COVID,
    MOBILE_VORWAHLEN,
    REGEX_TELEFON,
} from '../constants';
import DateUtil from './DateUtil';

export default class ValidationUtil {

    public static hasSwissMobileVorwahl(telToCheck: string): boolean {
        if (telToCheck) {
            const capturedGroups = telToCheck.match(REGEX_TELEFON);
            // const regex = new RegExp(REGEX_TELEFON);
            if (capturedGroups && capturedGroups.length >= 3) {
                const vorwahlGroup = capturedGroups[2];
                const matchesVorwahl: boolean = MOBILE_VORWAHLEN.some(value => value === vorwahlGroup);
                return matchesVorwahl;
            }
        }
        return false;
    }

    public static getMinDateForLetzteImpfungEZ(krankheit: KrankheitIdentifierTS): Date {
        let minDateForLetzteImpfungAsString;
        if (KrankheitIdentifierTS.COVID === krankheit) {
            minDateForLetzteImpfungAsString = MIN_DATE_FOR_EXTERNE_IMPFUNGEN_COVID;
        } else {
            // Fuer andere Krankheiten "vernuenftigen" default
            minDateForLetzteImpfungAsString = MIN_DATE_FOR_EXTERNE_IMPFUNGEN;
        }
        return moment(minDateForLetzteImpfungAsString, DateUtil.dateFormatShort()).toDate();
    }

    public static getMinDateForImpfung(krankheit: KrankheitIdentifierTS | undefined): Date {
        let minDateForLetzteImpfungAsString;
        if (KrankheitIdentifierTS.COVID === krankheit) {
            minDateForLetzteImpfungAsString = MIN_DATE_FOR_IMPFUNGEN_COVID;
        } else if (KrankheitIdentifierTS.AFFENPOCKEN === krankheit) {
            minDateForLetzteImpfungAsString = MIN_DATE_FOR_IMPFUNGEN_AFFENPOCKEN;
        } else {
            // Fuer andere Krankheiten "vernuenftigen" default
            minDateForLetzteImpfungAsString = MIN_DATE_FOR_EXTERNE_IMPFUNGEN;
        }
        return moment(minDateForLetzteImpfungAsString, DateUtil.dateFormatShort()).toDate();
    }
}
