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
import {first} from 'rxjs/operators';
import {FachRolleTS, OdibenutzerService, OdiUserJaxTS} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, Option} from '../../../../../vacme-web-shared';
import {
    DB_DEFAULT_MAX_LENGTH,
    EMAIL_PATTERN,
    REGEX_GLN_NUM,
    TEL_REGEX_NUMBER_INT,
} from '../../../../../vacme-web-shared/src/lib/constants';
import {TSAppEventTyp, TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {ApplicationEventService} from '../../../../../vacme-web-shared/src/lib/service/application-event.service';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';

const LOG = LogFactory.createLog('BenutzerErstellenPageComponent');

@Component({
    selector: 'app-benutzer-erstellen-page',
    templateUrl: './benutzer-erstellen-page.component.html',
    styleUrls: ['./benutzer-erstellen-page.component.scss'],
})
export class BenutzerErstellenPageComponent extends BaseDestroyableComponent implements OnInit {

    public formGroup!: FormGroup;

    public rolleOptions?: Option[];

    private odiUserJaxTS?: OdiUserJaxTS;
    private ortDerImpfungIdentifier?: string;
    private ortDerImpfungId?: string;
    public editUserMode = false;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private odibenutzerService: OdibenutzerService,
        private activeRoute: ActivatedRoute,
        private authService: AuthServiceRsService,
        private appEventService: ApplicationEventService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.initializeUI();

        const canFachBabDelegieren = this.authService.hasRole(TSRole.OI_FACHBAB_DELEGIEREN);

        const rolesArray = [
            FachRolleTS.FACHPERSONAL,
            FachRolleTS.FACHSUPERVISION,
            FachRolleTS.FACHVERANTWORTUNG_BAB_DELEGIERT,
            FachRolleTS.ORGANISATIONSSUPERVISION,
            FachRolleTS.PERSONAL,
        ];
        this.rolleOptions = rolesArray.map(t => {
            return {
                label: t,
                value: t,
                // nur Fachver_bab kann Fachver_bab delegieren
                disabled: !(canFachBabDelegieren || t !== FachRolleTS.FACHVERANTWORTUNG_BAB_DELEGIERT),
            };
        });

        this.activeRoute.params.pipe(first()).subscribe(next => {
            this.ortDerImpfungIdentifier = decodeURIComponent(next.ortDerImpfungIdentifier);
            this.ortDerImpfungId = decodeURIComponent(next.ortDerImpfungId);

            if (!this.ortDerImpfungIdentifier) {
                LOG.error('Ort der Impfung name not set!');
            }
            if (!this.ortDerImpfungId) {
                LOG.error('Ort der Impfung id not set!');
            }
        }, error => {
            LOG.error(error);
        });

        this.activeRoute.data.pipe(first()).subscribe(next => {
            this.odiUserJaxTS = next.user;
            if (this.odiUserJaxTS) {
                this.formGroup.patchValue(this.odiUserJaxTS);
                this.editUserMode = true;
            }
        }, error => {
            LOG.error(error);
        });

        if (this.editeduserIsInAdminRoleOrDisabled()) {
            this.formGroup.disable();
        }
    }

    private initializeUI(): void {
        const odiUserJaxTS: OdiUserJaxTS = {
            email: '', firstName: '', glnNummer: '', lastName: '', phone: '',
            username: '', fachRolle: FachRolleTS.PERSONAL,
        };
        const minLength = 2;
        const minLengthPhone = 4;
        this.formGroup = this.fb.group({
            lastName: this.fb.control(odiUserJaxTS.lastName,
                [Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            firstName: this.fb.control(odiUserJaxTS.firstName,
                [Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            email: this.fb.control(odiUserJaxTS.email,
                [
                    Validators.email,
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(EMAIL_PATTERN),
                    Validators.required,
                ]),
            phone: this.fb.control(odiUserJaxTS.phone,
                [
                    Validators.minLength(minLengthPhone),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(TEL_REGEX_NUMBER_INT),
                    Validators.required,
                ]),
            glnNummer: this.fb.control(odiUserJaxTS.glnNummer,
                [
                    Validators.minLength(minLength),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(REGEX_GLN_NUM),
                ]),
            fachRolle: this.fb.control(odiUserJaxTS.fachRolle,
                [
                    Validators.minLength(minLength),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.required,
                ]),
            username: this.fb.control({
                value: odiUserJaxTS.username,
                disabled: true,
            }),
        });
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.save();
        });
    }

    private save(): void {
        const value: OdiUserJaxTS = this.formGroup.value;

        if (this.odiUserJaxTS) {
            // Write id as marker for existing odiUser
            value.id = this.odiUserJaxTS.id;
            // dont change username for existing user
            value.username = this.odiUserJaxTS.username;
        } else {
            // at the moment we use email as username for new user.... may be wrong?
            value.username = value.email;
        }

        if (this.ortDerImpfungIdentifier != null) {
            this.odibenutzerService.odiBenutzerResourceCreateUserForGroup(this.ortDerImpfungIdentifier, value)
                .pipe()
                .subscribe(result => {
                        this.sendUserChangedEvent(value);
                        //  send data to rest resource and receive generated code before we navigate
                        this.router.navigate(['/ortderimpfung/stammdaten/' + this.ortDerImpfungId]);
                    },
                    error => {
                        LOG.error(error);
                    });
        }

    }

    private sendUserChangedEvent(value: OdiUserJaxTS): void {
        const eventData: any = {group: this.ortDerImpfungIdentifier, username: value.username};
        const appEvent = {appEventTyp: TSAppEventTyp.USER_CREATED, data: eventData};

        this.appEventService.broadcastEvent(appEvent);
    }

    public editeduserIsInAdminRoleOrDisabled(): boolean {
        if (this.odiUserJaxTS) {
            return (
                this.odiUserJaxTS.fachRolle === FachRolleTS.APPLIKATIONS_SUPPORT
                || this.odiUserJaxTS.fachRolle === FachRolleTS.FACHVERANTWORTUNG_BAB
                || this.odiUserJaxTS.fachRolle === FachRolleTS.ORGANISATIONSVERANTWORTUNG
                || !this.odiUserJaxTS.enabled); // enabled flag as set in keycloak (not vacme-db)
        }
        return false;
    }

    public back(): void {
        this.router.navigate(['/ortderimpfung/stammdaten/' + this.ortDerImpfungId]);
    }
}
