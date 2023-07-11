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
import {Injectable, Injector} from '@angular/core';
// import * as HTTPStatusCodes from 'http-status-codes';
import {Observable, throwError as observableThrowError, from} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {HTTP_UNAUTHORIZED} from '../constants';
import {TSErrorLevel} from '../model/TSErrorLevel';
import {TSErrorType} from '../model/TSErrorType';
import TSExceptionReport from '../model/TSExceptionReport';
import {ErrorMessageService} from '../service/error-message.service';
import {BlobUtil} from '../util/BlobUtil';

@Injectable({
    providedIn: 'root',
})
export class HttpErrorInterceptorService implements HttpInterceptor {

    private readonly FORBIDDEN: number = 403;
    private readonly GATEWAY_TIMEOUT: number = 504;

    constructor(private injector: Injector) {

    }

    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const errorMessageService = this.injector.get(ErrorMessageService);

        return next.handle(req).pipe(
            catchError(response => {
                if (response instanceof HttpErrorResponse) {
                    if (response.status === this.FORBIDDEN) {
                        errorMessageService.addMesageAsError('ERROR_UNAUTHORIZED');
                        return observableThrowError(response);
                    }
                    if (response.status === this.GATEWAY_TIMEOUT) {
                        errorMessageService.addMesageAsError('ERROR_GATEWAY_TIMEOUT');
                        return observableThrowError(response);
                    }

                    // here we handle all errorcodes except 401, 504, and 403, 401 is handeld in HttpAuthInterceptor
                    if (response.status !== HTTP_UNAUTHORIZED) {
                        /**
                         * here we could analyze the http status of the response. But instead we check if the
                         * response has the format of a known response such as errortypes such as violationReport
                         * or ExceptionReport and transform it as such. If the response matches
                         * know expected format we createOrUpdate an unexpected error.
                         */
                        const errorPromise = this.handleErrorResponse(response);
                        const observableToReturn$ = from(errorPromise)
                            .pipe(
                                map((errors: TSExceptionReport[]) => {
                                    // Show the error messages (we want to do this before we throw the error because
                                    // people may want to show another message)
                                    errorMessageService.handleErrors(errors);
                                    // pass the error response on to the caller of the request
                                    throw(response);
                                }));
                        return observableToReturn$;

                    }

                }

                return observableThrowError(response);
            }));

    }

    /**
     * Tries to determine what kind of response data the error-response retunred and  handles the data object
     * of the response accordingly.
     *
     * The expected types are ViolationReport objects from JAXRS if there was a beanValidation error
     * or EbeguExceptionReports in case there was some other application exception
     *
     */
    private handleErrorResponse(response: HttpErrorResponse): Promise<Array<TSExceptionReport>> {
        let errors: Array<TSExceptionReport>;
        const rawError = response.error;

        // Error-Objekt auslesen: als Promise, weil der Response Type ein Blob sein kann
        const errorPromise = this.extractErrorobjectFromErrorResponse(rawError);
        return errorPromise.then((error: TSExceptionReport[]) => {
            // Alle daten loggen um das Debuggen zu vereinfachen
            if (this.isValidationResponse(error)) {
                errors = this.convertViolationReport(error);
            } else if (this.isDataDvbExceptionReport(error)) {
                errors = this.convertEbeguExceptionReport(error);
            } else {
                console.error(`ErrorStatus: "${response.status}" StatusText: "${response.statusText}"`);
                console.error('ResponseData:' + JSON.stringify(error));
                // the error objects is neither a ViolationReport nor a ExceptionReport. Create a generic error msg
                errors = [];
                errors.push(new TSExceptionReport({
                    type: TSErrorType.INTERNAL,
                    severity: TSErrorLevel.SEVERE,
                    msgKey: 'ERROR_UNEXPECTED',
                    args: error,
                }));

                /* else if (this.isFileUploadException(response.data)) {
                 errors = [];
                 errors.push(new TSExceptionReport(TSErrorType.INTERNAL, TSErrorLevel.SEVERE, 'ERROR_FILE_TOO_LARGE', response.data));
                 } */
            }

            return errors;
        });

    }

    private extractErrorobjectFromErrorResponse(error: any): Promise<any> {
        // (A) Wenn der Error als String vorliegt, muessen wir ihn zuerst parsen
        if (typeof error === 'string') {
            return new Promise<any>((resolve, reject): void => {
                try {
                    resolve(JSON.parse(error));
                } catch (e) {
                    // wenn es nicht klappt, lassen wir den error unberuehrt
                    console.error('Unable to parse string in error response as json: ' + error);
                    resolve(error);
                }
            });
        }
        // (B) Blob: wenn der ResponseType 'blob' ist, kommen auch die Errors als Blob. Blob lesen ist leider asynchron.
        if (error instanceof Blob) {
            try {
                return BlobUtil.readBlobAsJson(error);
            } catch (e) {
                // wenn es nicht klappt, lassen wir den error unberuehrt
                console.error('Unable to parse the error response as Blob: ' + error);
                return new Promise<any>((resolve, reject): void => resolve(error));
            }
        }

        // (C) Einfacher Fall: der Error ist bereits ein json-Objekt => Rueckgabe als Promise mit dem uspruenglichen
        // Objekt
        return new Promise<any>((resolve, reject): void => resolve(error));
    }

    private convertViolationReport(data: any): Array<TSExceptionReport> {
        const aggregatedExceptionReports: Array<TSExceptionReport> = [];
        return aggregatedExceptionReports
            .concat(this.convertVioToExceptionReport(data.violations));

    }

    /**
     * coverts violations to exception reports which are understood by our error message system
     *
     * @param violations these are the validation violations we go
     * @return a list of exceptionr eports
     */
    private convertVioToExceptionReport(violations: any): Array<TSExceptionReport> {
        const exceptionReports: Array<TSExceptionReport> = [];
        if (violations) {
            for (const violationEntry of violations) {
                const key: string = violationEntry.key;
                const message: string = violationEntry.message;
                const path: string = violationEntry.path;
                const value: string = violationEntry.value;
                const report = TSExceptionReport.createFromViolation(key, message, path, value);
                exceptionReports.push(report);
            }
        }

        return exceptionReports;

    }

    private convertEbeguExceptionReport(data: any): TSExceptionReport[] {
        const exceptionReport = TSExceptionReport.createFromExceptionReport(data);
        const exceptionReports: Array<TSExceptionReport> = [];
        exceptionReports.push(exceptionReport);

        return exceptionReports;

    }

    /**
     *
     * checks if response data json-object has the keys required to be a violationReport (from jaxRS)
     *
     * @param data object whose keys are checked
     * @returns  true if fields of violationReport are present
     */
    private isValidationResponse(data: any): boolean {
        // hier pruefen wir ob wir die Felder von org.jboss.resteasy.api.validation.ViolationReport.ViolationReport()
        if (data !== null && data !== undefined) {
            const hasType: boolean = data.hasOwnProperty('type');
            const hasViolations: boolean = data.hasOwnProperty('violations');

            return hasType && hasViolations;
        }

        return false;

    }

    private isDataDvbExceptionReport(data: any): boolean {
        if (data !== null && data !== undefined) {
            const hassErrorCodeEnum: boolean = data.hasOwnProperty('errorCodeEnum');
            const hasExceptionName: boolean = data.hasOwnProperty('exceptionName');
            const hasMethodName: boolean = data.hasOwnProperty('methodName');
            const hasStackTrace: boolean = data.hasOwnProperty('stackTrace');
            const hasTranslatedMessage: boolean = data.hasOwnProperty('translatedMessage');
            const hasCustomMessage: boolean = data.hasOwnProperty('customMessage');
            const hasArgumentList: boolean = data.hasOwnProperty('argumentList');

            return hassErrorCodeEnum && hasExceptionName && hasMethodName && hasStackTrace
                && hasTranslatedMessage && hasCustomMessage && hasArgumentList;
        }

        return false;

    }

}
