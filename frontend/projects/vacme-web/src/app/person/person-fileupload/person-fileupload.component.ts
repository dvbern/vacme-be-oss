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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input} from '@angular/core';
import {FileSystemFileEntry, NgxFileDropEntry} from 'ngx-file-drop';
import {FileSaverService} from 'ngx-filesaver';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {DossierService, FileInfoJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BlobRestService} from '../../../../../vacme-web-shared/src/lib/service/blob-rest.service';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';

const LOG = LogFactory.createLog('PersonFileuploadComponent');

@Component({
    selector: 'app-person-fileupload',
    templateUrl: './person-fileupload.component.html',
    styleUrls: ['./person-fileupload.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonFileuploadComponent {

    @Input() public regNummer?: string | undefined;
    @Input() public krankheit: KrankheitIdentifierTS | undefined;
    @Input() public uploadedFiles: FileInfoJaxTS[] = [];
    @Input() public canUpload = false;
    @Input() public canDownload = false;


    constructor(
        private cdRef: ChangeDetectorRef,
        private blobService: BlobRestService,
        private errorMessageService: ErrorMessageService,
        private dossierService: DossierService,
        private filesaver: FileSaverService,
    ) {
    }

    public dropped(ngxFilesToUplaod: NgxFileDropEntry[]): void {
        const droppedFiles: FileSystemFileEntry[] =
            ngxFilesToUplaod
                .map(value => value.fileEntry as FileSystemFileEntry)
                .filter(droppedFile => droppedFile.isFile);
        this.handleUploadedFiles(droppedFiles);
    }

    private handleUploadedFiles(fileSystemEntries: FileSystemFileEntry[]): void {
        if (fileSystemEntries.length === 0) {
            return;
        }
        let foundAllFiles = false;
        const filesToUpload: File[] = [];
        fileSystemEntries
            .filter(droppedFile => droppedFile.isFile)
            .forEach(fileSysFile => fileSysFile.file(file => {

                filesToUpload.push(file);
                if (filesToUpload.length === fileSystemEntries.length) {
                    foundAllFiles = true;
                    this.doUpload$(filesToUpload)
                        .subscribe(
                            upldFiles => {
                                this.uploadedFiles = this.uploadedFiles.concat(upldFiles);
                                this.cdRef.detectChanges();
                            },
                            error => {
                                LOG.error(error);
                            }
                        );
                }
            }));
        setTimeout(() => {
            if (!foundAllFiles) {
                LOG.error(`Could not prepare all files for upload`);
                this.errorMessageService.addMesageAsError('UPLOAD_FILE_ERROR');
            }
        }, 1000 * 5);
    }



    private doUpload$(files: File[]): Observable<FileInfoJaxTS[]> {
        if (files && files.length !== 0) {
            // falls mehrere files auf einmal uebermittelt werden sollen darf ihre gesamtgroesse nicht zu gross sein
            if (!this.blobService.checkFileSize(files)) {
                this.errorMessageService.addMesageAsError('ERROR_COMBINED_FILES_TOO_BIG');
                throw new Error('Combined File size is too big');
            }
            // actually upload the files
            return this.blobService.postFiles$(files, this.regNummer, this.krankheit).pipe(
                map(result => {
                    return result;
                })
            );
        }
        return of(new Array<FileSystemFileEntry>());
    }

    public downloadDoc(id: string, name: string | undefined): void {
        if (this.canDownload) {
            if (id && this.regNummer) {
                this.dossierService.blobResourceDownloadFile(id, this.regNummer)
                    .subscribe(res => {
                            this.filesaver.save(res, name);
                        },
                        error => {
                            LOG.error(error);
                        }
                    );
            }
        } else{
            LOG.warn('Download is not allowed');
        }
    }
}
