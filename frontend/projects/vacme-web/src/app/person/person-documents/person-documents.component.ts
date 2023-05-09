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

import {Component, Input, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {DownloadService, KrankheitIdentifierTS, RegistrierungStatusTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import ITermineTS from '../../../../../vacme-web-shared/src/lib/model/ITermine';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {BlobUtil} from '../../../../../vacme-web-shared/src/lib/util/BlobUtil';
import {isAnyStatusOfBooster} from '../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';

const LOG = LogFactory.createLog('PersonDocumentsComponent');

@Component({
    selector: 'app-person-documents',
    templateUrl: './person-documents.component.html',
    styleUrls: ['./person-documents.component.scss']
})
export class PersonDocumentsComponent implements OnInit {

    @Input()
    public krankheit!: KrankheitIdentifierTS;

    @Input()
    public hasVacmeImpfung = false;

    @Input()
    public status?: RegistrierungStatusTS;

    @Input()
    public registration!: string;

    @Input() public termininfos?: ITermineTS;

    constructor(private downloadService: DownloadService,
                private translationService: TranslateService,
                private authService: AuthServiceRsService,
                private vacmeSettingsService: VacmeSettingsService) {
    }

    ngOnInit(): void {
    }

    hasDocuments(): boolean {
        return this.status !== undefined;
    }

    hasPendingTermin(): boolean {
        if (!this.vacmeSettingsService.supportsTerminbuchung(this.krankheit)) {
            return false;
        }
        // dem Generator ist es egal ob wir einen termin oder ein gewunschtesOdi haben.
        // Er findet dann selber raus welche bestaetigung gebraucht wird
        if (!!this.termininfos) {
            if (isAnyStatusOfBooster(this.termininfos?.status)) {
                // Boostertermin: auch sofort nach 2. Impfung, dann interessiert der 2. Termin ja bereits nicht mehr
                return (!!this.termininfos.terminNPending || !!this.termininfos.gewuenschterOrtDerImpfung);
            } else {
                // bis zur 2 Impfung
                return !!this.termininfos.termin1 || !!this.termininfos.gewuenschterOrtDerImpfung;
            }
        }
        return false;
    }

    downloadRegistrationConfirmation(): void {
        this.downloadService.downloadResourceDownloadRegistrierungsBestaetigung(this.registration)
            .subscribe(BlobUtil.openInNewTab, error => {
                LOG.error('Could not download Registrierungsbestaetigung for registration ' + this.registration);
            });
    }

    downloadTerminConfirmation(): void {
        this.downloadService.downloadResourceDownloadTerminBestaetigung(
            this.krankheit,
            this.registration)
            .subscribe(BlobUtil.openInNewTab, error => {
                LOG.error('Could not download Terminbestaetigung for registration ' + this.registration);
            });
    }

    downloadImpfdokumentation(): void {
        this.downloadService.downloadResourceDownloadImpfdokumentation(
            this.krankheit,
            this.registration
        ).subscribe(BlobUtil.openInNewTab, error => {
                LOG.error('Could not download Impfdokumentation for registration ' + this.registration);
            });
    }

    /**
     * New window must be created in the event-handler initiated by a user action  or else the
     * popup-blocker might prevent the new window.
     */
    private createNewWindowForDL(): Window | null {
        return window.open('', '_blank');
    }

    public isOiDokumentation(): boolean {
        return this.authService.isOneOfRoles([TSRole.OI_KONTROLLE, TSRole.KT_IMPFDOKUMENTATION]);
    }

    public showEinwilligungserklaerungFormular(): boolean {
        return this.krankheit === 'AFFENPOCKEN';
    }
}
