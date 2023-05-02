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

import {DOCUMENT, Location} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {Inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {API_URL} from '../constants';
import {FileInfoJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import {LogFactory} from '../logging';
import {ErrorMessageService} from './error-message.service';

const LOG = LogFactory.createLog('BlobRestService');

@Injectable({
    providedIn: 'root',
})
export class BlobRestService {

    private serviceURL: string;

    private readonly nineMB: number = 9000000;

    constructor(private http: HttpClient,
                private location: Location,
                private errorMessageService: ErrorMessageService,
                @Inject(DOCUMENT) private document: Document) {
        this.serviceURL = `${API_URL}/web/blob`;
    }

    public postFiles$(
        filesToUpl: File[],
        registrierungsnummer: string | undefined,
        krankheit: KrankheitIdentifierTS | undefined,
    ): Observable<Array<FileInfoJaxTS>> {
        const formData = new FormData();
        for (const file of filesToUpl) {
            formData.append('file', file, encodeURIComponent(file.name));
        }
        return this.postRequest$(registrierungsnummer, formData, krankheit);
    }

    private postRequest$(
        registrierungsnummer: string | undefined,
        formData: FormData,
        krankheit: KrankheitIdentifierTS | undefined,
    ): Observable<any> {
        const url = `${this.serviceURL}/krankheit/${krankheit}/registrierungsnummer/${registrierungsnummer}`;
        return this.http.post<any>(url, formData);
    }

    public checkFileSize(files: File[]): boolean {
        if (files && files.length !== 0) {
            let uploadSize = 0;
            for (const file of files) {
                uploadSize += file.size;
                if (uploadSize > this.nineMB) { // Maximale Filegroesse ist 9MB
                    LOG.error(`Files are too big: size: ${uploadSize}`);
                    this.errorMessageService.addMesageAsError('ERROR_FILE_TOO_BIG');
                    return false;
                }
            }
        }
        return true;
    }
}
