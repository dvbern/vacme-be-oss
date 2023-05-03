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
import {FormBuilder, FormGroup} from '@angular/forms';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {DevelopService} from 'vacme-web-generated'; // nur das JS importieren
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent} from '../../../../../vacme-web-shared';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';

const LOG = LogFactory.createLog('SystemadministrationPageComponent');

@Component({
    selector: 'app-reports-page',
    templateUrl: './massenverarbeitung-page.component.html',
    styleUrls: ['./massenverarbeitung-page.component.scss'],
})
export class MassenverarbeitungPageComponent extends BaseDestroyableComponent implements OnInit {

    formGroup!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private developService: DevelopService,
        private authService: AuthServiceRsService
    ) {
        super();
    }

    ngOnInit(): void {
        this.initForm();
    }

    private initForm(): void {
        this.formGroup = this.fb.group({
            odiMigration: this.fb.control(''),
            duplikat: this.fb.control(''),
            registrierungenToDelete: this.fb.control(''),
            impfungenLoeschen: this.fb.control('')
        });
    }

    public submitMigration(): void {
        this.developService.developerResourceMoveImpfungenToOdi(this.formGroup.get('odiMigration')?.value)
            .subscribe(size => {
                LOG.info('Migrated {} Impfungen to new ODIs', size);
                this.showSuccessPopup();
                this.formGroup.get('odiMigration')?.setValue('');
            }, error => LOG.error(error));
    }

    public submitDuplikat(): void {
        this.developService.developerResourceExternalizeImpfungen(this.formGroup.get('duplikat')?.value)
            .subscribe(size => {
                this.showSuccessPopup();
                this.formGroup.get('duplikat')?.setValue('');
            }, error => LOG.error(error));
    }

    public deleteRegistrations(): void {
        this.developService.developerResourceDeleteRegistrierungen(this.formGroup.get('registrierungenToDelete')?.value)
            .subscribe(size => {
                this.showSuccessPopup();
                this.formGroup.get('registrierungenToDelete')?.setValue('');
            }, error => LOG.error(error));
    }

    public impfungenLoeschen(): void {
        this.developService.developerResourceImpfungenLoeschen(this.formGroup.get('impfungenLoeschen')?.value)
            .subscribe(size => {
                this.showSuccessPopup();
                this.formGroup.get('impfungenLoeschen')?.setValue('');
            }, error => LOG.error(error));
    }

    private showSuccessPopup(): void {
        Swal.fire({
            icon: 'success',
            timer: 1500,
            showCloseButton: false,
            showConfirmButton: false,
        });
    }

    public isUserInroleAsRegistrationOi(): boolean {
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI);
    }
}
