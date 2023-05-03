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

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import * as moment from 'moment';
import {debounceTime} from 'rxjs/operators';
// nur das JS importieren
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {ApplicationMessageJaxTS, ApplicationMessageStatusTS, MessagesService} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, Option} from '../../../../../vacme-web-shared';
import {
    DATE_TIME_FORMAT,
    DATE_TIME_PATTERN,
    DB_DEFAULT_MAX_LENGTH,
    MAX_LENGTH_TEXTAREA,
} from '../../../../../vacme-web-shared/src/lib/constants';
import {
    parsableDateTimeValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-datetime-validator';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('ApplicationMessageBearbeitenComponent');

@Component({
    selector: 'app-application-message-bearbeiten-page',
    templateUrl: './application-message-bearbeiten-page.component.html',
    styleUrls: ['./application-message-bearbeiten-page.component.scss'],
})
export class ApplicationMessageBearbeitenPageComponent extends BaseDestroyableComponent implements OnInit {

    public formGroup: FormGroup;
    public statusOptions: Option[] = Object.values(ApplicationMessageStatusTS).map(t => {
        return {label: t, value: t};
    });
    public provisionalMessage: ApplicationMessageJaxTS | undefined;
    private messageId: string | undefined;

    constructor(
        private fb: FormBuilder,
        private messagesService: MessagesService,
        private activatedRoute: ActivatedRoute,
        private router: Router,
    ) {
        super();
        const minLength = 2;
        this.formGroup = this.fb.group({
            title: this.fb.control(undefined,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            from: this.fb.control(undefined,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_TIME_PATTERN), Validators.required, parsableDateTimeValidator(),
                ]),
            to: this.fb.control(undefined,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_TIME_PATTERN), Validators.required, parsableDateTimeValidator(),
                ]),
            status: this.fb.control(undefined,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            content: this.fb.control(undefined, Validators.maxLength(MAX_LENGTH_TEXTAREA)),
        });
    }

    ngOnInit(): void {
        this.initFromActiveRoute();
        this.formGroup.valueChanges.pipe(debounceTime(2000))
            .subscribe((form) => {
                let message;
                if (this.formGroup.status === 'VALID') {
                    message = this.extractMessageFromForm(form);
                }
                this.provisionalMessage = message;
            }, (error) => {
                LOG.error(error);
            });
    }

    public save(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            Swal.showLoading();
            const message = this.extractMessageFromForm(this.formGroup.getRawValue());
            if (!this.messageId) {
                this.messagesService.applicationMessageResourceCreate(message).pipe().subscribe(
                    () => {
                        this.router.navigate(['appmessage']);
                        Swal.fire({
                            icon: 'success',
                            timer: 4000,
                            showConfirmButton: false,
                        });
                    },
                    () => {
                        Swal.hideLoading();
                    },
                );
            } else {
                this.messagesService.applicationMessageResourceUpdate(this.messageId, message).subscribe(
                    () => {
                        this.router.navigate(['appmessage']);
                        Swal.fire({
                            icon: 'success',
                            timer: 4000,
                            showConfirmButton: false,
                        });
                    },
                    () => {
                        Swal.hideLoading();
                    },
                );
            }
        });
    }

    public cancel(): void {
        this.router.navigate(['appmessage']);
    }

    private initFromActiveRoute(): void {
        this.activatedRoute.data
            .pipe(this.takeUntilDestroyed())
            .subscribe((next) => {
                if (!!next && !!next.message) {
                    this.formGroup.setValue({
                        title: next.message.title,
                        content: next.message.htmlContent,
                        from: DateUtil.dateAsLocalDateTimeString(next.message.zeitfenster?.von, DATE_TIME_FORMAT),
                        to: DateUtil.dateAsLocalDateTimeString(next.message.zeitfenster?.bis, DATE_TIME_FORMAT),
                        status: next.message.status,
                    });
                    this.provisionalMessage = next.message;
                    this.messageId = next.message.id;
                    if (next.message.zeitfenster && DateUtil.now().isAfter(next.message.zeitfenster.bis)) {
                        this.formGroup.disable();
                    }
                }
            }, (error) => {
                LOG.error(error);
            });
    }

    public extractMessageFromForm(formValue: any): ApplicationMessageJaxTS {
        const bis: Date = moment(formValue.to, DATE_TIME_FORMAT).toDate();
        const von: Date = moment(formValue.from, DATE_TIME_FORMAT).toDate();
        return {
            htmlContent: formValue.content,
            status: formValue.status,
            title: formValue.title,
            zeitfenster: {
                von,
                bis,
                vonDisplay: '',
                bisDisplay: '',
                exactDisplay: '',
            },
        };
    }

    public isCreate(): boolean {
        return !this.messageId;
    }
}
