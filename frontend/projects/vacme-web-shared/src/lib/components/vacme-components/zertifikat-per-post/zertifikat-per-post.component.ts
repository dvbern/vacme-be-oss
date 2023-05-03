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

import {DatePipe} from '@angular/common';
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import Swal from 'sweetalert2/dist/sweetalert2.js';

@Component({
    selector: 'lib-zertifikat-per-post',
    templateUrl: './zertifikat-per-post.component.html',
    styleUrls: ['./zertifikat-per-post.component.scss']
})
export class ZertifikatPerPostComponent {

    @Input()
    public timestampLetzterPostversand?: Date = undefined;

    @Output()
    public resendZertifikatEvent = new EventEmitter<VoidFunction>();

    public zertRetriggerInfoExpanded = false;
    public resendLinkVisible = true;

    constructor(
        private translationService: TranslateService,
        private datePipe: DatePipe
    ) {
    }

    retriggerZertifikat(): void {
        Swal.fire({
            icon: 'question',
            title: this.translationService.instant('OVERVIEW.IMPFAUSWEIS_REVOKE_AND_RESEND_POST_TITLE'),
            html: '<p>' + this.translationService.instant('OVERVIEW.IMPFAUSWEIS_REVOKE_AND_RESEND_POST_INFO') + '</p>' +
                this.getTextLastSentZertifikatText(),
            showCancelButton: true,
            cancelButtonText: this.translationService.instant('CONFIRMATION.NO'),
            confirmButtonText: this.translationService.instant('CONFIRMATION.YES'),
        }).then((result) => {
            if (result.isConfirmed) {
                this.resendLinkVisible = false;
                this.resendZertifikatEvent.emit();
            }
        }, () => {
            this.resendLinkVisible = true;
        });
    }

    public getTextLastSentZertifikatText(): string {
        if (this.timestampLetzterPostversand) {
            return this.translationService.instant('OVERVIEW.IMPFAUSWEIS_REVOKED_DATE', {
                timestampLetzterPostversand: this.datePipe.transform(
                    this.timestampLetzterPostversand, 'dd.MM.yyyy HH:mm')
            });
        }
        return '';
    }
}
