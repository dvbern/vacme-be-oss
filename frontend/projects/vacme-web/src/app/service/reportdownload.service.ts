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

/* eslint-disable max-len */
import {HttpClient, HttpEvent, HttpHeaders, HttpParameterCodec, HttpParams, HttpResponse} from '@angular/common/http';
import {Inject, Injectable, Optional} from '@angular/core';
import {Observable} from 'rxjs';
import {BASE_PATH, Configuration} from 'vacme-web-generated';
import {CustomHttpParameterCodec} from '../../../../vacme-web-generated/src/lib/encoder';

/**
 * Dieser Service wurde ausgelagert, da openapi falsch (?) generiert und der responseType blob sein muss.
 * Der Service enthaelt neben der apiV1WebReportsDownloadDocumentqueueIdGet$ Methode auch noch die Methoden
 * zum direkten synchronen generieren der Reports welche zu einem sofortigen Download fuehren.
 * Diese werden nur noch fuer kleine reports gebraucht. Die anderen werden uber den report.service.getriggerd und dann
 * ueber die apiV1WebReportsDownloadDocumentqueueIdGet$ herungergeladen
 */
@Injectable({
    providedIn: 'root'
})
export class ReportDownloadService {

    protected basePath = 'http://localhost';
    public defaultHeaders = new HttpHeaders();
    public configuration = new Configuration();
    public encoder: HttpParameterCodec;

    constructor(
        protected httpClient: HttpClient,
        @Optional() @Inject(BASE_PATH) basePath: string,
        @Optional() configuration: Configuration
    ) {
        if (configuration) {
            this.configuration = configuration;
        }
        if (typeof this.configuration.basePath !== 'string') {
            if (typeof basePath !== 'string') {
                basePath = this.basePath;
            }
            this.configuration.basePath = basePath;
        }
        this.encoder = this.configuration.encoder || new CustomHttpParameterCodec();
    }

    private addToHttpParams(httpParams: HttpParams, value: any, key?: string): HttpParams {
        if (typeof value === 'object' && value instanceof Date === false) {
            httpParams = this.addToHttpParamsRecursive(httpParams, value);
        } else {
            httpParams = this.addToHttpParamsRecursive(httpParams, value, key);
        }
        return httpParams;
    }

    private addToHttpParamsRecursive(httpParams: HttpParams, value?: any, key?: string): HttpParams {
        if (value == null) {
            return httpParams;
        }

        if (typeof value === 'object') {
            if (Array.isArray(value)) {
                (value as any[]).forEach(elem => httpParams = this.addToHttpParamsRecursive(httpParams, elem, key));
            } else if (value instanceof Date) {
                if (key != null) {
                    httpParams = httpParams.append(key,
                        (value as Date).toISOString().substr(0, 10));
                } else {
                    throw Error('key may not be null if value is Date');
                }
            } else {
                Object.keys(value).forEach(k => httpParams = this.addToHttpParamsRecursive(
                    httpParams, value[k], key != null ? `${key}.${k}` : k));
            }
        } else if (key != null) {
            httpParams = httpParams.append(key, value);
        } else {
            throw Error('key may not be null if value is not object or array');
        }
        return httpParams;
    }

    public apiV1WebReportsAbrechnungDateVonDateBisPost$(
        dateBis: Date, dateVon: Date, language: string, observe?: 'body', reportProgress?: boolean,
        options?: { httpHeaderAccept?: undefined }
    ): Observable<any>;
    public apiV1WebReportsAbrechnungDateVonDateBisPost$(
        dateBis: Date, dateVon: Date, language: string, observe?: 'response', reportProgress?: boolean,
        options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpResponse<any>>;
    public apiV1WebReportsAbrechnungDateVonDateBisPost$(
        dateBis: Date, dateVon: Date, language: string, observe?: 'events', reportProgress?: boolean,
        options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpEvent<any>>;
    public apiV1WebReportsAbrechnungDateVonDateBisPost$(
        dateBis: Date, dateVon: Date, language: string, observe: any = 'body', reportProgress: boolean = false,
        options?: { httpHeaderAccept?: undefined }
    ): Observable<any> {
        if (dateBis === null || dateBis === undefined) {
            throw new Error('Required parameter dateBis was null or undefined when calling '
                + 'apiV1WebReportsAbrechnungDateVonDateBisPost.');
        }
        if (dateVon === null || dateVon === undefined) {
            throw new Error('Required parameter dateVon was null or undefined when calling '
                + 'apiV1WebReportsAbrechnungDateVonDateBisPost.');
        }
        if (language === null || language === undefined) {
            throw new Error('Required parameter language was null or undefined when calling '
                + 'apiV1WebReportsAbrechnungDateVonDateBisLanguagePost.');
        }

        let queryParameters = new HttpParams({encoder: this.encoder});
        if (dateBis !== undefined && dateBis !== null) {
            queryParameters = this.addToHttpParams(queryParameters,
                dateBis as any, 'dateBis');
        }
        if (dateVon !== undefined && dateVon !== null) {
            queryParameters = this.addToHttpParams(queryParameters,
                dateVon as any, 'dateVon');
        }

        let headers = this.defaultHeaders;

        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        return this.httpClient.post<any>(
            `${this.configuration.basePath}/api/v1/web/reports/sync/abrechnung/${encodeURIComponent(String(language))}`,
            null,
            {
                params: queryParameters,
                responseType: 'blob' as any,
                withCredentials: this.configuration.withCredentials,
                headers,
                observe,
                reportProgress
            }
        );
    }

    public apiV1WebReportsAbrechnungZhDateVonDateBisLanguagePost$(
        dateBis: Date, dateVon: Date, language: string, observe?: 'body', reportProgress?: boolean,
        options?: { httpHeaderAccept?: undefined }
    ): Observable<any>;
    public apiV1WebReportsAbrechnungZhDateVonDateBisLanguagePost$(
        dateBis: Date, dateVon: Date, language: string, observe?: 'response', reportProgress?: boolean,
        options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpResponse<any>>;
    public apiV1WebReportsAbrechnungZhDateVonDateBisLanguagePost$(
        dateBis: Date, dateVon: Date, language: string, observe?: 'events', reportProgress?: boolean,
        options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpEvent<any>>;
    public apiV1WebReportsAbrechnungZhDateVonDateBisLanguagePost$(
        dateBis: Date, dateVon: Date, language: string, observe: any = 'body', reportProgress: boolean = false,
        options?: { httpHeaderAccept?: undefined }
    ): Observable<any> {
        if (dateBis === null || dateBis === undefined) {
            throw new Error('Required parameter dateBis was null or undefined when calling apiV1WebReportsAbrechnungZhDateVonDateBisLanguagePost$.');
        }
        if (dateVon === null || dateVon === undefined) {
            throw new Error('Required parameter dateVon was null or undefined when calling apiV1WebReportsAbrechnungZhDateVonDateBisLanguagePost$.');
        }
        if (language === null || language === undefined) {
            throw new Error('Required parameter language was null or undefined when calling apiV1WebReportsAbrechnungZhDateVonDateBisLanguagePost$.');
        }

        let queryParameters = new HttpParams({encoder: this.encoder});
        if (dateBis !== undefined && dateBis !== null) {
            queryParameters = this.addToHttpParams(queryParameters,
                dateBis as any, 'dateBis');
        }
        if (dateVon !== undefined && dateVon !== null) {
            queryParameters = this.addToHttpParams(queryParameters,
                dateVon as any, 'dateVon');
        }

        let headers = this.defaultHeaders;

        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        return this.httpClient.post<any>(`${this.configuration.basePath}/api/v1/web/reports/sync/abrechnung-zh/${encodeURIComponent(String(language))}`,
            null,
            {
                params: queryParameters,
                responseType: 'blob' as any,
                withCredentials: this.configuration.withCredentials,
                headers,
                observe,
                reportProgress
            }
        );
    }

    /**
     * @param observe set whether or not to return the data Observable as the body,
     * response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public reportResourceGenerateKantonCSV$(observe?: 'body', reportProgress?: boolean,
                                               options?: { httpHeaderAccept?: undefined }): Observable<any>;
    public reportResourceGenerateKantonCSV$(observe?: 'response', reportProgress?: boolean,
                                               options?: { httpHeaderAccept?: undefined }): Observable<HttpResponse<any>>;
    public reportResourceGenerateKantonCSV$(observe?: 'events', reportProgress?: boolean,
                                               options?: { httpHeaderAccept?: undefined }): Observable<HttpEvent<any>>;
    public reportResourceGenerateKantonCSV$(observe: any = 'body', reportProgress: boolean = false,
                                               options?: { httpHeaderAccept?: undefined }): Observable<any> {

        let headers = this.defaultHeaders;

        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }


        let responseType: 'text' | 'json' = 'json';
        if (httpHeaderAcceptSelected && httpHeaderAcceptSelected.startsWith('text')) {
            responseType = 'text';
        }

        return this.httpClient.post<any>(`${this.configuration.basePath}/api/v1/web/reports/sync/kanton/statistik`,
            null,
            {
                responseType: 'blob' as any,
                withCredentials: this.configuration.withCredentials,
                headers,
                observe,
                reportProgress
            }
        );
    }

    /**
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public reportResourceGenerateReportingImpfungenCSVAsync$(observe?: 'body', reportProgress?: boolean,
                                                  options?: { httpHeaderAccept?: undefined }): Observable<any>;
    public reportResourceGenerateReportingImpfungenCSVAsync$(observe?: 'response', reportProgress?: boolean,
                                                  options?: { httpHeaderAccept?: undefined }): Observable<HttpResponse<any>>;
    public reportResourceGenerateReportingImpfungenCSVAsync$(observe?: 'events', reportProgress?: boolean,
                                                  options?: { httpHeaderAccept?: undefined }): Observable<HttpEvent<any>>;
    public reportResourceGenerateReportingImpfungenCSVAsync$(observe: any = 'body', reportProgress: boolean = false,
                                                  options?: { httpHeaderAccept?: undefined }): Observable<any> {

        let headers = this.defaultHeaders;

        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }


        let responseType: 'text' | 'json' = 'json';
        if (httpHeaderAcceptSelected && httpHeaderAcceptSelected.startsWith('text')) {
            responseType = 'text';
        }

        return this.httpClient.post<any>(`${this.configuration.basePath}/api/v1/web/reports/sync/impfungen/statistik`,
            null,
            {
                responseType: 'blob' as any,
                withCredentials: this.configuration.withCredentials,
                headers,
                observe,
                reportProgress
            }
        );
    }

    /**
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public reportResourceGenerateReportingOdisCSV$(
        observe?: 'body', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }
    ): Observable<any>;
    public reportResourceGenerateReportingOdisCSV$(
        observe?: 'response', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpResponse<any>>;
    public reportResourceGenerateReportingOdisCSV$(
        observe?: 'events', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpEvent<any>>;
    public reportResourceGenerateReportingOdisCSV$(
        observe: any = 'body', reportProgress: boolean = false, options?: { httpHeaderAccept?: undefined }
    ): Observable<any> {

        let headers = this.defaultHeaders;

        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        return this.httpClient.post<any>(`${this.configuration.basePath}/api/v1/web/reports/sync/odis/statistik`,
            null,
            {
                responseType: 'blob' as any,
                withCredentials: this.configuration.withCredentials,
                headers,
                observe,
                reportProgress
            }
        );
    }

    /**
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public reportResourceGenerateReportingTerminslotsCSV$(
        observe?: 'body', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }
    ): Observable<any>;
    public reportResourceGenerateReportingTerminslotsCSV$(
        observe?: 'response', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpResponse<any>>;
    public reportResourceGenerateReportingTerminslotsCSV$(
        observe?: 'events', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpEvent<any>>;
    public reportResourceGenerateReportingTerminslotsCSV$(
        observe: any = 'body', reportProgress: boolean = false, options?: { httpHeaderAccept?: undefined }
    ): Observable<any> {

        let headers = this.defaultHeaders;

        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        return this.httpClient.post<any>(`${this.configuration.basePath}/api/v1/web/reports/sync/terminslots/statistik`,
            null,
            {
                responseType: 'blob' as any,
                withCredentials: this.configuration.withCredentials,
                headers,
                observe,
                reportProgress
            }
        );
    }

    public reportResourceGenerateReportingKantonsarztCSV$(
        observe?: 'body', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }): Observable<any>;
    public reportResourceGenerateReportingKantonsarztCSV$(
        observe?: 'response', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }): Observable<HttpResponse<any>>;
    public reportResourceGenerateReportingKantonsarztCSV$(
        observe?: 'events', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }): Observable<HttpEvent<any>>;
    public reportResourceGenerateReportingKantonsarztCSV$(
        observe: any = 'body', reportProgress: boolean = false, options?: { httpHeaderAccept?: undefined }): Observable<any> {

        let headers = this.defaultHeaders;

        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        return this.httpClient.post<any>(`${this.configuration.basePath}/api/v1/web/reports/sync/kantonsarzt/statistik`,
            null,
            {
                responseType: 'blob' as any,
                withCredentials: this.configuration.withCredentials,
                headers,
                observe,
                reportProgress
            }
        );
    }

    /**
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public reportResourceGenerateReportingOdiImpfungenCSV$(
        language: string, observe?: 'body', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }
    ): Observable<any>;
    public reportResourceGenerateReportingOdiImpfungenCSV$(
        language: string, observe?: 'response', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpResponse<any>>;
    public reportResourceGenerateReportingOdiImpfungenCSV$(
        language: string, observe?: 'events', reportProgress?: boolean, options?: { httpHeaderAccept?: undefined }
    ): Observable<HttpEvent<any>>;
    public reportResourceGenerateReportingOdiImpfungenCSV$(
        language: string, observe: any = 'body', reportProgress: boolean = false, options?: { httpHeaderAccept?: undefined }
    ): Observable<any> {
        if (language === null || language === undefined) {
            throw new Error('Required parameter language was null or undefined when calling reportResourceGenerateReportingOdiImpfungenCSV$.');
        }

        let headers = this.defaultHeaders;

        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        return this.httpClient.post<any>(`${this.configuration.basePath}/api/v1/web/reports/sync/odiimpfungen/statistik/${encodeURIComponent(String(language))}`,
            null,
            {
                responseType: 'blob' as any,
                withCredentials: this.configuration.withCredentials,
                headers,
                observe,
                reportProgress
            }
        );
    }

    /**
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public apiV1WebReportsDownloadDocumentqueueIdGet$(id: string, observe?: 'body', reportProgress?: boolean,
                                                      options?: { httpHeaderAccept?: undefined }): Observable<any>;
    public apiV1WebReportsDownloadDocumentqueueIdGet$(id: string, observe?: 'response', reportProgress?: boolean,
                                                      options?: { httpHeaderAccept?: undefined }): Observable<HttpResponse<any>>;
    public apiV1WebReportsDownloadDocumentqueueIdGet$(id: string, observe?: 'events', reportProgress?: boolean,
                                                      options?: { httpHeaderAccept?: undefined }): Observable<HttpEvent<any>>;
    public apiV1WebReportsDownloadDocumentqueueIdGet$(id: string, observe: any = 'body', reportProgress: boolean = false,
                                                      options?: { httpHeaderAccept?: undefined }): Observable<any> {
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling apiV1WebReportsDownloadDocumentqueueIdGet$.');
        }

        let headers = this.defaultHeaders;

        let httpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (httpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [];
            httpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (httpHeaderAcceptSelected !== undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }


        let responseType: 'text' | 'json' = 'json';
        if (httpHeaderAcceptSelected && httpHeaderAcceptSelected.startsWith('text')) {
            responseType = 'text';
        }

        return this.httpClient.get<any>(`${this.configuration.basePath}/api/v1/web/reports/download/documentqueue/${encodeURIComponent(String(id))}`,
            {
                responseType: 'blob' as any,
                withCredentials: this.configuration.withCredentials,
                headers,
                observe,
                reportProgress
            }
        );
    }
}
