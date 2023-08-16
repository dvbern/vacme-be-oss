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
import {TranslateService} from '@ngx-translate/core';
import {first} from 'rxjs/operators';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    ImpfstoffJaxTS,
    KundengruppeTS,
    MandantTS,
    OdiUserDisplayNameJaxTS,
    OrtDerImpfungJaxTS,
    OrtderimpfungService,
    OrtDerImpfungTypTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {BaseDestroyableComponent, Option} from '../../../../../vacme-web-shared';
import {DB_DEFAULT_MAX_LENGTH, MAX_LENGTH_TEXTAREA} from '../../../../../vacme-web-shared/src/lib/constants';
import {TSAppEventTyp, TSRole} from '../../../../../vacme-web-shared/src/lib/model';
import {ApplicationEventService} from '../../../../../vacme-web-shared/src/lib/service/application-event.service';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {VacmeSettingsService} from '../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {ImpfstoffUtil} from '../../../../../vacme-web-shared/src/lib/util/impfstoff-util';
import TenantUtil from '../../../../../vacme-web-shared/src/lib/util/TenantUtil';

const LOG = LogFactory.createLog('StammdatenPageComponent');

interface SelectOption {
    label: string;
    value: string | undefined;
}

@Component({
    selector: 'app-stammdaten-page',
    templateUrl: './stammdaten-page.component.html',
    styleUrls: ['./stammdaten-page.component.scss'],
})
export class StammdatenPageComponent extends BaseDestroyableComponent implements OnInit {

    public formGroup!: UntypedFormGroup;
    public formGroupAdresse!: UntypedFormGroup;

    public typOptions: Option[] = Object.values(OrtDerImpfungTypTS).map(t => {
        return {label: t, value: t};
    });

    public kundengruppeOptions: Option[] = Object.values(KundengruppeTS).map(t => {
        return {label: t, value: t};
    });

    public mandantOptions: Option[] = Object.values(MandantTS).map(m => {
        return {label: m, value: m};
    });

    public ortDerImpfung?: OrtDerImpfungJaxTS;
    public ortDerImpfungCodeExist = false;
    public ortDerImpfungCode?: string;
    public terminverwaltung?: boolean;

    public fachverantwortungbabList?: Array<OdiUserDisplayNameJaxTS>;
    public fachverantwortungbabMap?: Array<SelectOption>;
    public fachverantwortungbab?: SelectOption;
    public organisationsverantwortungList?: Array<OdiUserDisplayNameJaxTS>;
    public organisationsverantwortungMap?: Array<SelectOption>;
    public organisationsverantwortung?: SelectOption;
    public impfstoffOptions?: any = [];

    private impfstoffList: ImpfstoffJaxTS[] = [];

    private readonly INPUT_MIN_LENGTH = 2;

    constructor(
        private fb: UntypedFormBuilder,
        private router: Router,
        private ortderimpfungService: OrtderimpfungService,
        private activeRoute: ActivatedRoute,
        private authService: AuthServiceRsService,
        private translationService: TranslateService,
        private vacmeSettingsService: VacmeSettingsService,
        private applicationEventService: ApplicationEventService,
    ) {
        super();
    }

    ngOnInit(): void {
        this.initializeUI();

        this.activeRoute.data.pipe(
            this.takeUntilDestroyed(),
            first(),
        ).subscribe(next => {
            this.ortDerImpfung = next.ortDerImpfung;
            this.terminverwaltung = this.ortDerImpfung?.terminverwaltung;
            if (this.ortDerImpfung) {
                this.loadFachRolePersonen();
                this.formGroup.patchValue(this.ortDerImpfung);
                if (this.ortDerImpfung.adresse) {
                    this.formGroupAdresse.patchValue(this.ortDerImpfung.adresse);
                }
                if (this.ortDerImpfung.impfstoffe) {
                    this.formGroup.get('impfstoffe')?.setValue(
                        this.ortDerImpfung.impfstoffe.map((impfstoff: ImpfstoffJaxTS) => impfstoff.id));
                }
            }
            if (next.impfstoffList) {
                this.impfstoffList = next.impfstoffList;
                this.impfstoffOptions =
                    ImpfstoffUtil.createOptionsOfZugelassene(next.impfstoffList, this.translationService);
            }
        }, error => {
            LOG.error(error);
        });
    }

    private initializeUI(): void {
        const ortDerImpfung: OrtDerImpfungJaxTS = {
            krankheiten: [],
        };

        this.formGroupAdresse = this.fb.group({
            adresse1: this.fb.control(ortDerImpfung.adresse?.adresse1,
                [
                    Validators.minLength(this.INPUT_MIN_LENGTH),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.required,
                ]),
            adresse2: this.fb.control(ortDerImpfung.adresse?.adresse2,
                [Validators.minLength(this.INPUT_MIN_LENGTH), Validators.maxLength(DB_DEFAULT_MAX_LENGTH)]),
            plz: this.fb.control(ortDerImpfung.adresse?.plz, Validators.required),
            ort: this.fb.control(ortDerImpfung.adresse?.ort,
                [
                    Validators.minLength(this.INPUT_MIN_LENGTH),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.required,
                ]),
        });

        this.formGroup = this.fb.group({
            identifier: this.fb.control(ortDerImpfung.identifier,
                [Validators.maxLength(DB_DEFAULT_MAX_LENGTH), Validators.required]),
            name: this.fb.control(ortDerImpfung.name,
                [
                    Validators.minLength(this.INPUT_MIN_LENGTH),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.required,
                ]),
            adresse: this.formGroupAdresse,
            oeffentlich: this.fb.control(ortDerImpfung.oeffentlich),
            mobilerOrtDerImpfung: this.fb.control(ortDerImpfung.mobilerOrtDerImpfung),
            terminverwaltung: this.fb.control(ortDerImpfung.terminverwaltung),
            externerBuchungslink: this.fb.control(ortDerImpfung.externerBuchungslink),
            personalisierterImpfReport: this.fb.control(ortDerImpfung.personalisierterImpfReport),
            deaktiviert: this.fb.control(ortDerImpfung.deaktiviert),
            booster: this.fb.control(ortDerImpfung.booster),
            impfstoffe: this.fb.control(ortDerImpfung.impfstoffe),
            zsrNummer: this.fb.control(ortDerImpfung.zsrNummer,
                [Validators.maxLength(DB_DEFAULT_MAX_LENGTH)]),
            glnNummer: this.fb.control(ortDerImpfung.glnNummer,
                [Validators.maxLength(DB_DEFAULT_MAX_LENGTH)]),
            typ: this.fb.control(ortDerImpfung.typ,
                [
                    Validators.minLength(this.INPUT_MIN_LENGTH),
                    Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                    Validators.required,
                ]),
            kommentar: this.fb.control(ortDerImpfung.kommentar,
                [Validators.maxLength(MAX_LENGTH_TEXTAREA)]),
            fachverantwortungbab: this.fb.control(this.fachverantwortungbab, [
                Validators.minLength(this.INPUT_MIN_LENGTH),
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
                Validators.required,
            ]),
            organisationsverantwortung: this.fb.control(this.organisationsverantwortung, [
                Validators.minLength(this.INPUT_MIN_LENGTH),
                Validators.maxLength(DB_DEFAULT_MAX_LENGTH),
            ]),
            impfungGegenBezahlung: this.fb.control(ortDerImpfung.impfungGegenBezahlung),
            kundengruppe: this.fb.control(ortDerImpfung.kundengruppe, [Validators.required]),
            mandant: this.fb.control(ortDerImpfung.mandant, [Validators.required]),
        });

        if (!this.isAsRegistrationOi()) { // darf nur der "Admin"
            this.formGroup.get('oeffentlich')?.disable();
            this.formGroup.get('mobilerOrtDerImpfung')?.disable();
            this.formGroup.get('terminverwaltung')?.disable();
            this.formGroup.get('externerBuchungslink')?.disable();
            this.formGroup.get('personalisierterImpfReport')?.disable();
            this.formGroup.get('deaktiviert')?.disable();
            this.formGroup.get('booster')?.disable();
            this.formGroup.get('impfungGegenBezahlung')?.disable();
            this.formGroup.get('impfstoffe')?.disable();
            this.formGroup.get('name')?.disable();
            this.formGroup.get('kundengruppe')?.disable();
            this.formGroup.get('mandant')?.disable();
            if (!TenantUtil.hasOdiAttributesEnabled()) {
                this.formGroup.get('typ')?.disable();
                this.formGroup.get('adresse')?.disable();
                this.formGroup.get('zsrNummer')?.disable();
                this.formGroup.get('glnNummer')?.disable();
            }
        }
    }

    private findElement(
        selectOptions: SelectOption[],
        value: string | undefined,
    ): SelectOption | undefined {
        if (selectOptions && selectOptions.length > 0) {
            const element = selectOptions.find(e => e.value === value);
            return element;
        }
        return undefined;
    }

    public submitIfValid(): void {
        FormUtil.doIfValid(this.formGroup, () => {
            this.save();
        });
    }

    private save(): void {
        const value: OrtDerImpfungJaxTS = {
            // Angular does not include disabled fields in the formGroup values. The reasoning is that it would be
            // possible to enable the disabled field using browser tools. However this is an Admin GUI and we trust our
            // users so we use the raw value
            ...this.formGroup.getRawValue(),
            impfstoffe: this.retrieveJaxByImpfstoffIds(this.formGroup.value.impfstoffe),
            fachverantwortungbab: this.formGroup.value.fachverantwortungbab.value,
            organisationsverantwortung: this.formGroup.value.organisationsverantwortung
                ? this.formGroup.value.organisationsverantwortung.value
                : undefined,
        };

        // Reset externerBuchungslink if the odi has vacme Terminverwaltung
        if (value.terminverwaltung) {
            value.externerBuchungslink = undefined;
        }

        if (!this.ortDerImpfung) {
            this.ortderimpfungService.ortDerImpfungResourceErfassen(value)
                .pipe()
                .subscribe((result: OrtDerImpfungJaxTS) => {
                        this.ortDerImpfung = result;
                        //  send data to rest resource and receive generated code before we navigate
                        this.sendOdiChangedEvent(result);
                        if (this.hasTermine()) {
                            void this.router.navigate([
                                '/ortderimpfung/terminverwaltung/',
                                result.id, DateUtil.currentYear(), DateUtil.currentMonth(),
                            ]);
                        } else {
                            void Swal.fire({
                                icon: 'success',
                                timer: 1500,
                                showConfirmButton: false,
                            });
                        }
                    },
                    error => {
                        LOG.error(error);
                    });
        } else {
            // we have to give the ID back!
            value.id = this.ortDerImpfung.id;
            this.ortderimpfungService.ortDerImpfungResourceAktualisieren(value)
                .pipe()
                .subscribe(() => {
                        //  send data to rest resource and receive generated code before we navigate
                        this.sendOdiChangedEvent(value);
                        if (!this.terminverwaltung && value.terminverwaltung) {
                            void this.router.navigate([
                                '/ortderimpfung/terminverwaltung',
                                value.id, DateUtil.currentYear(), DateUtil.currentMonth(),
                            ]);
                        } else {
                            this.terminverwaltung = value.terminverwaltung;
                            void Swal.fire({
                                icon: 'success',
                                timer: 1500,
                                showConfirmButton: false,
                            });
                        }
                    },
                    error => {
                        LOG.error(error);
                    });
        }
    }

    public isBearbeitung(): boolean {
        return !!this.ortDerImpfung;
    }

    public doesExist(): boolean {
        if (this.isBearbeitung()) {
            return true;
        }
        return this.ortDerImpfungCodeExist;
    }

    public hasTermine(): boolean {
        // directly access the form control value as the control might be in a disabled state
        return !!this.formGroup.controls.terminverwaltung.value;
    }

    public isOeffentlich(): boolean {
        // directly access the form control value as the control might be in a disabled state
        return !!this.formGroup.controls.oeffentlich.value;
    }

    public checkExistence(): void {
        const value: OrtDerImpfungJaxTS = this.formGroup.value;
        if (value.identifier) {
            this.ortderimpfungService.ortDerImpfungResourceCheckOrtDerImpfungExists(value.identifier)
                .pipe()
                .subscribe(result => {
                        this.ortDerImpfungCodeExist = result.existence === true;
                        this.ortDerImpfungCode = value.identifier;
                        this.loadFachRolePersonen();
                    },
                    error => {
                        LOG.error(error);
                    });
        }
    }

    private loadFachRolePersonen(): void {
        this.ortderimpfungService.ortDerImpfungResourceGetAllFachverantwortungbab().pipe()
            .subscribe((result: Array<OdiUserDisplayNameJaxTS>) => {
                    this.fachverantwortungbabList = result;
                    this.fachverantwortungbabMap = this.fachverantwortungbabList.map(t => {
                        return {label: this.userDisplayNameToLabel(t), value: t.id};
                    });
                    this.fachverantwortungbab = this.findElement(this.fachverantwortungbabMap,
                        this.ortDerImpfung?.fachverantwortungbab);
                    const control = this.formGroup.controls.fachverantwortungbab;
                    control.setValue(this.fachverantwortungbab);
                },
                error => {
                    LOG.error(error);
                });

        this.ortderimpfungService.ortDerImpfungResourceGetAllOrganisationsverantwortung().pipe()
            .subscribe((result: Array<OdiUserDisplayNameJaxTS>) => {
                    this.organisationsverantwortungList = result;
                    this.organisationsverantwortungMap = this.organisationsverantwortungList.map(t => {
                        return {label: this.userDisplayNameToLabel(t), value: t.id};
                    });
                    if (this.ortDerImpfung?.organisationsverantwortung) {
                        this.organisationsverantwortung = this.findElement(this.organisationsverantwortungMap,
                            this.ortDerImpfung?.organisationsverantwortung);
                        const control = this.formGroup.controls.organisationsverantwortung;
                        control.setValue(this.organisationsverantwortung);
                    }
                },
                error => {
                    LOG.error(error);
                });
    }

    private userDisplayNameToLabel(user: OdiUserDisplayNameJaxTS): string {
        return user.firstName + ' ' + user.lastName + ' (' + user.username + ')';
    }

    public isAsRegistrationOi(): boolean {
        return this.authService.hasRole(TSRole.AS_REGISTRATION_OI);
    }

    public termineBearbeiten(): void {
        const value: OrtDerImpfungJaxTS = this.formGroup.value;
        if (this.ortDerImpfung) {
            value.id = this.ortDerImpfung.id;
            void this.router.navigate([
                '/ortderimpfung/terminverwaltung',
                value.id, DateUtil.currentYear(), DateUtil.currentMonth(),
            ]);
        }
    }

    public filterBearbeiten(): void {
        const value: OrtDerImpfungJaxTS = this.formGroup.value;
        if (this.ortDerImpfung) {
            value.id = this.ortDerImpfung.id;
            void this.router.navigate([
                '/ortderimpfung/odifilter',
                value.id,
            ]);
        }
    }

    hasMobileOrtDerImpfung(): boolean {
        return TenantUtil.hasMobilerOrtDerImpfungAdministration();
    }

    hasRoleBenutzerverwalter(): boolean {
        return this.authService.hasRole(TSRole.OI_BENUTZER_VERWALTER)
            || this.authService.hasRole(TSRole.AS_BENUTZER_VERWALTER);
    }

    private retrieveJaxByImpfstoffIds(ids?: string[]): ImpfstoffJaxTS[] {
        if (!ids) {
            return [];
        }
        const jaxes: ImpfstoffJaxTS[] = [];
        ids.forEach(id => {
            const jax = this.impfstoffList.find(impfstoff => impfstoff.id === id);
            if (jax) {
                jaxes.push(jax);
            } else {
                LOG.warn('Missing id in impfstoffList ' + id);
            }
        });
        return jaxes;
    }

    public hasTerminbuchungFuerSelbstzahler(): boolean {
        return this.vacmeSettingsService.selbstzahlerFachapplikationEnabled;
    }

    private sendOdiChangedEvent(value: OrtDerImpfungJaxTS): void {
        const eventData: any = {odi: value};
        const appEvent = {appEventTyp: TSAppEventTyp.ODI_MODIFIED, data: eventData};
        this.applicationEventService.broadcastEvent(appEvent);
    }
}
