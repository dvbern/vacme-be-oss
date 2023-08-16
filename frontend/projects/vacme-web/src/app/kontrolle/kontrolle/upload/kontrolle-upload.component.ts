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
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import {DossierService, FileInfoJaxTS, KrankheitIdentifierTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BlobRestService} from '../../../../../../vacme-web-shared/src/lib/service/blob-rest.service';
import {ErrorMessageService} from '../../../../../../vacme-web-shared/src/lib/service/error-message.service';

const LOG = LogFactory.createLog('KontrolleComponent');

@Component({
    selector: 'app-kontrolle-upload',
    templateUrl: './kontrolle-upload.component.html',
    styleUrls: ['./kontrolle-upload.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KontrolleUploadComponent {

    @Input() public immediateUpload?: boolean;
    @Input() public regNummer: string | undefined;
    @Input() public krankheit: KrankheitIdentifierTS | undefined;
    @Input() public canSave = true; // Es darf nur einmal gespeichert werden

    @Input() public uploadedFiles: FileInfoJaxTS[] = [];

    public filesPreparedForUpl: FileSystemFileEntry[] = [];

    // ********** INIT ********************

    constructor(
        private dossierService: DossierService,
        private filesaver: FileSaverService,
        private cdRef: ChangeDetectorRef,
        private errorMessageService: ErrorMessageService,
        private blobService: BlobRestService,
    ) {

    }

    public uploadItsPreparedFiles(): void {
        this.uploadPreparedFiles(this.filesPreparedForUpl);
    }

    public uploadPreparedFiles(ngxFilesToUpload: FileSystemFileEntry[]): void {
        this.handleUploadedFiles(ngxFilesToUpload, true);
    }

    public dropped(ngxFilesToUplaod: NgxFileDropEntry[]): void {
        const droppedFiles: FileSystemFileEntry[] =
            ngxFilesToUplaod
                .map(value => value.fileEntry as FileSystemFileEntry)
                .filter(droppedFile => droppedFile.isFile);

        this.handleUploadedFiles(droppedFiles, !!this.immediateUpload);
    }

    private handleUploadedFiles(fileSystemEntries: FileSystemFileEntry[], immediateUpload: boolean): void {
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

                    if (immediateUpload) { // upload right now
                        this.doUpload$(filesToUpload)
                            .subscribe(
                                upldFiles => {
                                    this.uploadedFiles = this.uploadedFiles.concat(upldFiles);
                                    this.filesPreparedForUpl = new Array<FileSystemFileEntry>();
                                    this.cdRef.detectChanges();

                                },
                                error => {
                                    LOG.error(error);
                                }
                            );

                    } else {
                        // store for later. Will upload when reg ist saved
                        const success = this.blobService.checkFileSize(filesToUpload);
                        if (success) {
                            this.filesPreparedForUpl = this.filesPreparedForUpl.concat(fileSysFile);
                            this.cdRef.detectChanges();
                        }
                    }
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
    }
}
