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

import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable, Injector} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
// import * as HTTPStatusCodes from 'http-status-codes';
import {Observable} from 'rxjs';
import {API_URL} from '../constants';

@Injectable({
    providedIn: 'root',
})
export class HttpLocaleInterceptorService implements HttpInterceptor {

    constructor(private injector: Injector) {

    }

    public intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (!request.url?.startsWith(API_URL)) {
            return next.handle(request);
        }

        const translateService = this.injector.get(TranslateService);
        const currentLocale = translateService?.currentLang;

        if (currentLocale) {
            // Clone the request and replace the original headers with cloned headers, updated with the language.
            const clonedRequest = request.clone({
                headers: request.headers.set('Accept-Language', currentLocale),
            });

            // send cloned request with header to the next handler.
            return next.handle(clonedRequest);
        } else {
            return next.handle(request);
        }
    }
}
