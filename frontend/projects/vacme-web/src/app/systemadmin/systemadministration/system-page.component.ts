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
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {FileSaverService} from 'ngx-filesaver';
// falsch: import Swal from 'sweetalert2'; // Hier wird nicht nur das JS, sondern auch das CSS importiert
import Swal from 'sweetalert2/dist/sweetalert2.js'; // nur das JS importieren
import {
    ApplicationPropertyKeyTS,
    DevelopService,
    KrankheitIdentifierTS,
    PropertiesService,
    SystemadministrationService,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, Option} from '../../../../../vacme-web-shared';
import {DATE_PATTERN, DB_DEFAULT_MAX_LENGTH} from '../../../../../vacme-web-shared/src/lib/constants';
import {TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {
    parsableDateValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/parsable-date-validator';

const LOG = LogFactory.createLog('SystemadministrationPageComponent');

@Component({
    selector: 'app-reports-page',
    templateUrl: './system-page.component.html',
    styleUrls: ['./system-page.component.scss'],
})
export class SystemPageComponent extends BaseDestroyableComponent implements OnInit {

    formGroup!: UntypedFormGroup;
    actionInProgress = false;

    krankheitOptions: Option[] = [];
    readonly ALLE_KRANKHEITEN_OPTION_VALUE: string = 'ALLE';

    constructor(
        private fb: UntypedFormBuilder,
        private router: Router,
        private activeRoute: ActivatedRoute,
        private systemadministrationService: SystemadministrationService,
        private filesaver: FileSaverService,
        private authService: AuthServiceRsService,
        private developService: DevelopService,
        private propertiesService: PropertiesService,
    ) {
        super();
        this.krankheitOptions = this.getKrankheitOptions();
    }

    ngOnInit(): void {
        const minLength = 2;
        this.formGroup = this.fb.group({
            stichtag: this.fb.control(undefined,
                [
                    Validators.minLength(minLength), Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.pattern(DATE_PATTERN), Validators.required, parsableDateValidator(),
                ]),
            zertifikatEnabled: this.fb.control(undefined),
            krankheit: this.fb.control(this.ALLE_KRANKHEITEN_OPTION_VALUE),
        });

        this.propertiesService.applicationPropertyResourceIsZertifikatEnabled().subscribe(
            response => this.formGroup.get('zertifikatEnabled')?.setValue(response),
            error => LOG.error(error),
        );
    }

    public getKrankheitOptions(): Option[] {
        const options: Option[] = [];
        for (const krankheit of Object.keys(KrankheitIdentifierTS)) {
            options.push({label: krankheit as string, value: krankheit});
        }
        const optionAll: Option = {label: 'Alle', value: this.ALLE_KRANKHEITEN_OPTION_VALUE};
        return [optionAll].concat(options);
    }

    public isUserInroleAsRegistrationOi(): boolean {
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI);
    }

    public callVMDLUploadBatchJob(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.developService.developerResourceCallVMDLUploadBatchJob()
            .subscribe(res => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('VMDL-Upload konnten nicht gestartet werden', error);
            });
    }

    public callBoosterImmunisiertStatusImpfschutzService(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.developService.developerResourceCallBoosterImmunisiertStatusImpfschutzService()
            .subscribe(res => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('BoosterImmunisiertStatusImpfschutzService konnte nicht gestartet werden', error);
            });
    }

    public callBoosterFreigabeStatusService(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.developService.developerResourceCallBoosterFreigabeStatusService()
            .subscribe(res => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('BoosterFreigabeStatusService konnte nicht gestartet werden', error);
            });
    }

    public callBoosterRecalculationService(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        const selectedKrankheit = this.formGroup.get('krankheit')?.value;
        if (selectedKrankheit === this.ALLE_KRANKHEITEN_OPTION_VALUE) {
            this.developService.developerResourceCallBoosterRecalculation()
                .subscribe(() => {
                    Swal.hideLoading();
                    this.actionInProgress = false;
                }, error => {
                    Swal.hideLoading();
                    this.actionInProgress = false;
                    LOG.error('BoosterRuleengine konnte nicht gestartet werden', error);
                });
        } else {
            this.developService.developerResourceCallBoosterRecalculationForKrankheit(selectedKrankheit)
                .subscribe(() => {
                    Swal.hideLoading();
                    this.actionInProgress = false;
                }, error => {
                    Swal.hideLoading();
                    this.actionInProgress = false;
                    LOG.error('BoosterRuleengine konnte nicht gestartet werden', error);
                });
        }
    }

    private getFormControlValue(field: string): any {
        return this.formGroup.get(field)?.value;
    }

    public callPLZImportFromCSV(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.systemadministrationService.systemadministrationResourceImportPlzFromCsv()
            .subscribe(res => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('PLZ-import konnten nicht korrekt durchgefuehrt werden', error);
            });
    }

    public callPLZMedstatImportFromCSV(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.systemadministrationService.systemadministrationResourceImportPlzMedstatFromCsv()
            .subscribe(res => {
                this.actionInProgress = false;
            }, error => {
                this.actionInProgress = false;
                LOG.error('PLZ-Medstat-Import konnten nicht korrekt durchgefuehrt werden', error);
            });
    }

    public callGlnNummerUpdate(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.systemadministrationService.systemadministrationResourceImportMissingGln()
            .subscribe(res => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('GLN-import konnten nicht korrekt durchgefuehrt werden', error);
            });
    }

    public callDbValidationBatchJob(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.systemadministrationService.systemadministrationResourceRunApplicationHealthBatchJob()
            .subscribe(res => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('ApplicationHealth Batchjob konnte nicht gestartet werden', error);
            });
    }

    public callPriorityUpdateForGrowingChildren(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.developService.developerResourceCallPriorityUpdateForGrowingChildren()
            .subscribe(res => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('Children priority update konnte nicht gestartet werden', error);
            });
    }

    public saveZertifikatEnabled(): void {
        const stringValue = String(!!this.formGroup.get('zertifikatEnabled')?.value);
        this.propertiesService.applicationPropertyResourceUpdate({
            name: ApplicationPropertyKeyTS.COVID_ZERTIFIKAT_ENABLED,
            value: stringValue,
        }).subscribe(
            response => {
                Swal.fire({
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false,
                });
            }, error => LOG.error(error),
        );
    }

    public callPdfArchivierung(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.developService.developerResourceCallArchivierung()
            .subscribe(res => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('Archivierung konnte nicht gestartet werden', error);
            });
    }

    public callDeactivateUnusedUsers(): void {
        if (this.actionInProgress) {
            return;
        }
        this.actionInProgress = true;
        this.developService.developerResourceCallUserDeactivation()
            .subscribe(res => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('Unused User deactivation', error);
            });
    }

    public callGeocodeOdis(): void {
        this.developService.developerResourceCalculateOdiLatLn()
            .subscribe(() => {
                Swal.hideLoading();
            }, error => {
                Swal.hideLoading();
                LOG.error('Odi Geocoding konnte nicht gestartet werden', error);
            });
    }

    public callEnsureImpdossierExistsService(): void {

        this.actionInProgress = true;
        this.developService.developerResourceAddAllToImpfdossierEnsureQueue()
            .subscribe(() => {
                Swal.hideLoading();
                this.actionInProgress = false;
            }, error => {
                Swal.hideLoading();
                this.actionInProgress = false;
                LOG.error('Impfdossier erzeugung konnte nicht gestartet werden', error);
            });

    }
}
