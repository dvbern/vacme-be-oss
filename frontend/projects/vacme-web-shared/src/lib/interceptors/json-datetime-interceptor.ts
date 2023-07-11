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

import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse} from '@angular/common/http';
import {Injectable, Optional} from '@angular/core';
import cloneDeepWith from 'lodash/cloneDeepWith';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import DateUtil from '../util/DateUtil';

// noinspection MagicNumberJS
const MINUTE_MSECS = 60 * 1000;

/**
 * <strong>Ordering is important since entries are used to substring-search!</strong>
 */
const DEFAULT_DATE_PROPERTY_SUFFIXES = ['DateTime', 'Date', 'date', 'Datum', 'datum', 'Time', 'von', 'bis', 'day'];
export type MissingTimezoneConversion = 'treat-as-local' | 'treat-as-utc';

export interface Configuration {
    missingTimezoneConversion?: MissingTimezoneConversion;
}

export class Configurator {
    private readonly missingTimezoneConversion: MissingTimezoneConversion;

    constructor(opts?: Configuration) {
        this.missingTimezoneConversion = opts?.missingTimezoneConversion ?? 'treat-as-local';
    }

    getMissingTimezoneConversion(): MissingTimezoneConversion {
        return this.missingTimezoneConversion;
    }
}

@Injectable({
    providedIn: 'root',
})
export class NgxJsonDatetimeInterceptor implements HttpInterceptor {
    readonly config: Configurator;
    constructor(
        @Optional() config: Configurator | null,
    ) {
        this.config = config ?? new Configurator();
    }

    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        /**
         * Analog wie Angular den content-type bestimmt.
         * Denn dieser ist, wenn nicht explizit im request gesetzt, hier noch nicht im request
         * sondern wird erst spaeter ueber {@link HttpRequest.detectContentTypeHeader} erzeugt.
         */
        const contentType = req.headers.get('Content-Type') || req.detectContentTypeHeader();

        let actualRequest = req;
        if (contentType === 'application/json') {
            const bodyWithUTC = bodyToBackend(req.body, this.config);
            actualRequest = req.clone({body: bodyWithUTC});
        }

        return next.handle(actualRequest).pipe(
            map((val: HttpEvent<any>) => mapResponse(val, this.config)),
        );
    }
}

function mapResponse(response: HttpEvent<any>, config: Configurator): HttpEvent<any> {
    if (!(response instanceof HttpResponse)) {
        return response;
    }

    const contentType = response.headers.get('Content-Type');
    if (contentType !== 'application/json') {
        return response;
    }

    const body = bodyToFrontend(response.body, config);
    const newResponse = response.clone({body});
    return newResponse;
}

function isSuppportedProperty(key: number | string | undefined): boolean {
    const found = DEFAULT_DATE_PROPERTY_SUFFIXES
        .find(suffix => (typeof key === 'string' && key.endsWith(suffix)));

    const result = found !== undefined;

    return result;
}

export function bodyToFrontend(body: any, config: Configurator): any {
    if (body === null || body === undefined || (typeof body !== 'object')) {
        return;
    }

    // todo homa, kann man das korrekter machcen
    // @ts-ignore
    const result = cloneDeepWith(body, (value: any, key: number | string | undefined, obj, stack: any): any => {
        if (!isSuppportedProperty(key)) {
            return undefined;
        }
        // noinspection UnnecessaryLocalVariableJS
        const converted = valueToFrontend(value, config);

        return converted;
    });

    return result;

}



function valueToFrontend(value: any, config: Configurator): Date {
    if (typeof value !== 'string') {
        return value;
    }

    let parsed = DateUtil.localDateTimeToMoment(value);
    if (parsed === undefined) {
        // if not a datetime then try if it is a date
        parsed = DateUtil.localDateToMoment(value);
        if (parsed === undefined) {
            console.warn('could not parse date string ', value);
            return new Date(Date.parse(value));
        }
    }


    const result: Date = parsed.toDate();

    return result;
}

export function bodyToBackend(body: any, config: Configurator): any {
    if (body === null || body === undefined || (typeof body !== 'object')) {
        return;
    }

    const result = cloneDeepWith(body, (value: any, key: number | string | undefined): any => {
        if (!isSuppportedProperty(key)) {
            return undefined;
        }
        // noinspection UnnecessaryLocalVariableJS
        const converted = valueToBackend(value, config);

        return converted;
    });

    return result;
}

function valueToBackend(value: any, config: Configurator): any {
    if (!(value instanceof Date)) {
        return value;
    }

    let adjusted = value;
    if (config.getMissingTimezoneConversion() !== 'treat-as-utc') {
        adjusted = treatLocalAsUTC(value);
    }

    const result = chopZulu(adjusted.toISOString());
    return result;
}

function treatLocalAsUTC(value: Date): Date {
    const result = new Date(value);
    result.setMinutes(result.getMinutes() - result.getTimezoneOffset());
    return result;
}

function chopZulu(isoDate: string): string {
    const result = isoDate.replace(/Z$/, '');
    return result;
}
