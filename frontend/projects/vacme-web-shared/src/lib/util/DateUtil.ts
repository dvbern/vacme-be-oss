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
import {DATE_FORMAT} from '../constants';

export default class DateUtil {

    // Wochentag und Datum
    public static dateFormatVeryLong(lang: string): string {
        switch (lang) {
            case 'fr':
                return 'EEEE d MMMM yyyy';
            case 'en':
                return 'EEEE d MMMM yyyy';
            default:
                return 'EEEE, d. MMMM yyyy'; // Donnerstag, 3. September 2021
        }
    }

    // Datum ausgeschrieben
    public static dateFormatLong(lang: string): string {
        switch (lang) {
            case 'fr':
                return 'd MMMM yyyy';
            case 'en':
                return 'd MMMM yyyy';
            default:
                return 'd. MMMM yyyy'; // 4. Februar 1978
        }
    }

    // Datum mit gekuerztem Monat
    public static dateFormatMedium(lang: string): string {
        switch (lang) {
            case 'fr':
                return 'd MMM yyyy'; // 3 nov. 2021
            case 'en':
                return 'MMM. d, yyyy'; // Nov. 3, 2021
            default:
                return 'd. MMM yyyy'; // 3. Nov. 2021
        }
    }

    // Datum nur Zahlen
    public static dateFormatShort(): string {
        return DATE_FORMAT; // 'DD.MM.yyyy'; // 04.07.2003
    }

    public static parseDateAsMidday(dateString: string): Date {
        // at midday to avoid timezone issues
        return moment(dateString, DATE_FORMAT).hour(12).toDate();
    }

    public static parseDateAsMiddayOrUndefined(dateString?: string): Date | undefined {
        return dateString ? this.parseDateAsMidday(dateString) : undefined;
    }

    public static localDateTimeToMoment(localDateTimeString: string): moment.Moment | undefined {
        const theMoment = moment(localDateTimeString,
            ['YYYY-MM-DDTHH:mm:ss.SSS', 'YYYY-MM-DDTHH:mm:ss', 'YYYY-MM-DDTHH:mm:ss.SSSZ'], true);

        return theMoment.isValid() ? theMoment : undefined;
    }

    public static localDateTimeToMomentWithFormat(localDateTimeString: string,
                                                  format: string): moment.Moment | undefined {
        const theMoment = moment(localDateTimeString, [format], true);

        return theMoment.isValid() ? theMoment : undefined;
    }

    public static momentToLocalDateTime(aMoment: moment.Moment): string | undefined {
        return DateUtil.momentToLocalDateTimeFormat(aMoment, 'YYYY-MM-DDTHH:mm:ss.SSS');
    }

    /**
     * Calls momentToLocalDateFormat with the format by default 'YYYY-MM-DD'
     */
    public static momentToLocalDate(aMoment: moment.Moment): string | undefined {
        return DateUtil.momentToLocalDateFormat(aMoment, 'YYYY-MM-DD');
    }

    /**
     */
    public static momentToLocalDateFormat(aMoment: moment.Moment, format: string): string | undefined {
        if (!aMoment) {
            return undefined;
        }

        return moment(aMoment).startOf('day').format(format);
    }

    /**
     */
    public static localDateToMoment(localDateString: string): moment.Moment | undefined {
        const theMoment = moment(localDateString, 'YYYY-MM-DD', true);

        return theMoment.isValid() ? theMoment : undefined;
    }

    public static today(): moment.Moment {
        return moment().startOf('day');
    }

    public static now(): moment.Moment {
        return moment();
    }

    public static atStartOfTomorrow(): moment.Moment {
        return DateUtil.now().add(1, 'day').startOf('day');
    }

    // public static nowDate(): Date {
    //     return moment().toDate();
    // }

    public static currentYear(): number {
        return moment().year();
    }

    public static dateAsLocalDateTimeString(date: Date | undefined, format: string): string {
        if (date) {
            const momentDate = moment(date);
            if (momentDate) {
                const dateAsString = moment(momentDate).format(format);
                if (dateAsString) {
                    return dateAsString;
                }
            }
        }
        return '';
    }

    public static dateAsLocalDateString(date: Date | undefined): string {
        if (date) {
            const momentDate = moment(date);
            if (momentDate) {
                const dateAsString = DateUtil.momentToLocalDateFormat(momentDate, this.dateFormatShort());
                if (dateAsString) {
                    return dateAsString;
                }
            }
        }
        return '';
    }

    /**
     * @param  aMoment time instance
     * @param format format for the time
     * @returns  a Date (YYYY-MM-DD) representation of the given moment. undefined when aMoment is invalid
     */
    private static momentToLocalDateTimeFormat(aMoment: moment.Moment, format: string): string | undefined {
        if (!aMoment) {
            return undefined;
        }

        return moment(aMoment).format(format);
    }

    static currentMonth(): number {
        return moment().month();
    }

    static firstDayOfMonth(month: number): moment.Moment {
        return moment().month(month).startOf('month');
    }

    static lastDayOfMonth(month: number): moment.Moment {
        return moment().month(month).endOf('month');
    }

    static ofMonthYear(month: number, year: number): moment.Moment {
        return moment().year(year).month(month);
    }

    static substractMonths(mMoment: moment.Moment, numberOfMonths: number): moment.Moment {
        return moment(mMoment).subtract(numberOfMonths, 'months');
    }

    static addMonths(mMoment: moment.Moment, numberOfMonths: number): moment.Moment {
        return moment(mMoment).add(numberOfMonths, 'months');
    }

    static isSameOrAfterToday(date: Date | undefined): boolean {
        return moment(date).startOf('day').isSameOrAfter(moment().startOf('day'));
    }

    static isAfterToday(date: Date | undefined): boolean {
        return moment(date).startOf('day').isAfter(moment().startOf('day'));
    }

    static isToday(date: Date | undefined): boolean {
        return moment(date).startOf('day').isSame(moment().startOf('day'));
    }

    static getDaysDiff(dateX: Date, dateY: Date): number {
        return moment(dateX).startOf('day').diff(moment(dateY).startOf('day'), 'days');
    }

    static getMinutesDiff(dateX: Date, dateY: Date): number {
        return moment(dateX).startOf('minute').diff(moment(dateY).startOf('minute'), 'minutes');
    }

    static getHoursDiff(startDate: moment.Moment, endDate: moment.Moment): number {
        return endDate.diff(startDate, 'hours');
    }

    static substractDays(mMoment: moment.Moment, numberOfDays: number): moment.Moment {
        return moment(mMoment).subtract(numberOfDays, 'days');
    }

    static addDays(mMoment: moment.Moment, numberOfDays: number): moment.Moment {
        return moment(mMoment).add(numberOfDays, 'days');
    }

    static addDaysDate(date: Date, numberOfDays: number): Date {
        return moment(date).add(numberOfDays, 'days').toDate();
    }

    static age(geburtsdatum: Date): number {
        return moment().diff(geburtsdatum, 'years');
    }

    static daysInMonth(month: number, year: number): number {
        // Here months are 0 indexed -> january is 0
        const date = DateUtil.ofMonthYear(month, year).startOf('month');
        return date.daysInMonth();
    }

    static getLaterDate(date1: Date | undefined, date2: Date | undefined): Date | undefined {
        if (!date1) {
            return date2;
        }
        if (!date2) {
            return date1;
        }

        if (moment(date1).startOf('day').isAfter(moment(date2).startOf('day'))) {
            return date1;
        }
        return date2;
    }
}
