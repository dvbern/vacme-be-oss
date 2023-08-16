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

export enum TSRegistrierungViolationType {
    MAX_DAYS_NOT_VALID =  'MAX_DAYS_NOT_VALID',
    MIN_DAYS_NOT_VALID =  'MIN_DAYS_NOT_VALID',
    WRONG_IMPFGRUPPE =  'WRONG_IMPFGRUPPE',
    WRONG_STATUS =  'WRONG_STATUS',
    NO_TERMIN =  'NO_TERMIN',
    WRONG_TERMIN =  'WRONG_TERMIN',
    IMPFUNG_WAS_SAME_DAY =  'IMPFUNG_WAS_SAME_DAY',
    WRONG_ODI =  'WRONG_ODI',
    USER_NOT_FOR_ODI =  'USER_NOT_FOR_ODI',
    DIFFERENT_IMPFSTOFF =  'DIFFERENT_IMPFSTOFF',
    AGE_WARNING_CHILD = 'AGE_WARNING_CHILD',
    AGE_WARNING_ADULT = 'AGE_WARNING_ADULT',
    ODI_INAKTIV = 'ODI_INAKTIV',
    NICHT_FREIGEGEBEN_SELBSTZAHLER = 'NICHT_FREIGEGEBEN_SELBSTZAHLER',
    IMPFSTOFF_EINGESTELLT = 'IMPFSTOFF_EINGESTELLT',
}
