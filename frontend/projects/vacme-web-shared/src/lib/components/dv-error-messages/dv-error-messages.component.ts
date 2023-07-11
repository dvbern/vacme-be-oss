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

import {ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';

import {Subscription} from 'rxjs';

import {ErrorMessageService} from '../../service/error-message.service';
import {LogFactory} from '../../logging';
import TSExceptionReport from '../../model/TSExceptionReport';
import {TranslateService} from '@ngx-translate/core';
import Swal from 'sweetalert2/dist/sweetalert2.js';

const LOG = LogFactory.createLog('DvErrorMessagesComponent');

@Component({
    selector: 'lib-dv-error-messages',
    templateUrl: './dv-error-messages.component.html',
    styleUrls: ['./dv-error-messages.component.scss'],
})
export class DvErrorMessagesComponent implements OnInit, OnDestroy {

    public errorKey?: string;

    // wir haben hier ein dom element damit wir die translation auslesen koennen, stattdessen koennte man die snackbar mit einem
    // custom templates mit allen errorkeys oeffnen
    @ViewChild('translationDummy', {static: false})
    private translatedMsgKeyElement?: ElementRef;
    private subscription?: Subscription;

    constructor(private errorMessageService: ErrorMessageService,
                private translateService: TranslateService,
                private cdRef: ChangeDetectorRef) {

    }

    public showMessage(report: TSExceptionReport): void {

        if (report.msgKey && !report.translatedMessage) { // wenn noch nicht uebersetzt
            this.errorKey = report.msgKey;

            // now we have to force angular to updated the DOM so the translated variable is available
            this.cdRef.detectChanges();

            let translatedErrorText = this.translateService.instant(this.errorKey);

            // if translated error text exists but could not be translated display the key instead
            if (translatedErrorText && translatedErrorText.trim() === '') {
                translatedErrorText = this.errorKey;
            }

            const additionalClass = report.severity ? report.severity.toString() : '';

            Swal.fire({
                icon: 'warning',
                text: translatedErrorText,
                showCancelButton: false,
            });

        } else if (report.translatedMessage) {
            const additionalClass = report.severity ? report.severity.toString() : '';

            Swal.fire({
                icon: 'warning',
                text: report.translatedMessage,
                showCancelButton: false,
            });
        }
    }

    public ngOnInit(): void {
        this.subscription = this.errorMessageService.messageEventStream$.subscribe((element: TSExceptionReport) => {
            LOG.debug('pushing error message to bus', element);
            if (element) {
                this.showMessage(element);
            }

        }, (error) => {
            LOG.error(error);
        });

    }

    public ngOnDestroy(): void {
        // unsubscribe waere nicht noetig da es sich um ein singleton handelt aber es ist guter stil
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }

}
