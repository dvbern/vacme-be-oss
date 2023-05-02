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

import {DOCUMENT} from '@angular/common';
import {Component, EventEmitter, Inject, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {CurrentZertifikatInfoTS, DossierService, GeimpftService, RegistrierungsEingangTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {ZertifikatService} from '../../../../../vacme-web-generated/src/lib/api/zertifikat.service';
import {BlobUtil} from '../../../../../vacme-web-shared/src/lib/util/BlobUtil';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('PersonZertifikatComponent');

@Component({
    selector: 'app-person-zertifikat',
    templateUrl: './person-zertifikat.component.html',
    styleUrls: ['./person-zertifikat.component.scss'],
})
export class PersonZertifikatComponent implements OnInit, OnChanges {

    @Input()
    public registration!: string;

    @Input()
    public elektronischerImpfausweis = false;

    @Input()
    public registrierungsEingang?: RegistrierungsEingangTS;

    @Input()
    public currentZertifikatInfo?: CurrentZertifikatInfoTS;

    @Output()
    public zertifikatCreatedEvent = new EventEmitter<VoidFunction>();

    public elektronischerAusweisGroup!: FormGroup;
    public hasValidToken = false;

    public hasNoPendingZertifikatGeneration = false;

    constructor(
        private translationService: TranslateService,
        private geimpftService: GeimpftService,
        private fb: FormBuilder,
        private dossierService: DossierService,
        private zertifikatService: ZertifikatService,
        @Inject(DOCUMENT) private document: Document,
    ) {
    }

    public ngOnInit(): void {
        this.zertifikatService.zertifikatResourceHasValidToken()
            .subscribe(response => {
                this.hasValidToken = response;
            }, error => {
                LOG.error(error);
            });
        this.elektronischerAusweisGroup = this.fb.group({
            elektronischerImpfausweis: this.fb.control(undefined, Validators.requiredTrue),
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.currentZertifikatInfo) {
            this.hasNoPendingZertifikatGeneration = !this.currentZertifikatInfo.hasPendingZertifikatGeneration;
        }
    }

    public showDeservesNoZertifikatForNewestImpfung(): boolean {
        return this.elektronischerImpfausweis
            && this.hasNoPendingZertifikatGeneration
            && !this.currentZertifikatInfo?.deservesZertifikatForNewestImpfung;
    }

    public showDownloadZertifikatButton(): boolean {

        return this.elektronischerImpfausweis
            && this.hasNoPendingZertifikatGeneration
            && !!this.currentZertifikatInfo?.deservesZertifikatForNewestImpfung;
    }

    public downloadZertifikat(): void {
        this.dossierService.dossierResourceDownloadZertifikat(this.registration)
            .subscribe(res => {
                BlobUtil.openInNewTab(res, this.document);
            }, error => {
                // Fehlermeldung wird vom ErrorInterceptor angezeigt
                LOG.error(error);
            });
    }

    public showGenerateAndDownloadZertifikatButton(): boolean {
        return this.elektronischerImpfausweis
            && this.hasValidToken
            && !this.hasNoPendingZertifikatGeneration;
    }

    public generateAndDownloadZertifikat(): void {
        // generates the cert if not yet present
        this.dossierService.dossierResourceCreateAndDownload(this.registration)
            .subscribe(() => {
                this.hasNoPendingZertifikatGeneration = true;
                this.downloadZertifikat();
                this.zertifikatCreatedEvent.emit();
            }, error => LOG.error(error));
    }

    public saveImpfausweis(): void {
        FormUtil.doIfValid(this.elektronischerAusweisGroup, () => {
            if (this.registration) {
                this.geimpftService
                    .geimpftResourceAcceptElektronischerImpfausweis(this.registration)
                    .subscribe(response => {
                        this.elektronischerImpfausweis = true;
                        Swal.fire({
                            icon: 'success',
                            timer: 1500,
                            showConfirmButton: false,
                        });
                    }, error => LOG.error(error));
            }
        });
    }

    public isNotOnlineRegistrierung(): boolean {
        return this.registrierungsEingang !== RegistrierungsEingangTS.ONLINE_REGISTRATION;
    }
}
