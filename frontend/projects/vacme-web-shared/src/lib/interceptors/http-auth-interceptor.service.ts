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

import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, throwError as observableThrowError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {ErrorHandlerService} from '../service/error-handler.service';
import {HTTP_UNAUTHORIZED} from '../constants';

@Injectable()
export class HttpAuthInterceptorService implements HttpInterceptor {

    constructor(private router: Router, private errorHandlerService: ErrorHandlerService) {
    }

    /*
     * this interceptor should handle 401 status codes returned from http by passing them to ther errorHandlerService
     * which will be checking if the user is logged in in keycloak and - if he is not - redirect him to the login
     */
    public intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {


        return next.handle(request).pipe(
            catchError(response => {
                if (response instanceof HttpErrorResponse) {
                    if (response.status === HTTP_UNAUTHORIZED) {
                        this.errorHandlerService.handleIf401(response);
                        return observableThrowError(response);
                    }
                }

                return observableThrowError(response);
            }));
    }
}
