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

import {formatDate, ViewportScroller} from '@angular/common';
import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {TranslationChangeEvent} from '@ngx-translate/core/lib/translate.service';
import {FileSaverService} from 'ngx-filesaver';
import {Subscription} from 'rxjs';
import Swal from 'sweetalert2/dist/sweetalert2.js';
import {
    AdresseJaxTS,
    DashboardJaxTS,
    DossierService,
    ImpffolgeTS,
    KantonaleBerechtigungTS,
    KrankheitIdentifierTS,
    OnboardingService,
    OrtDerImpfungBuchungJaxTS,
    OrtDerImpfungDisplayNameExtendedJaxTS,
    RegistrierungsCodeJaxTS,
    RegistrierungsEingangTS,
    RegistrierungService,
    RegistrierungStatusTS,
    SelectOrtDerImpfungJaxTS,
    TerminbuchungJaxTS,
    ZertifikatJaxTS,
} from 'vacme-web-generated';
import {LogFactory} from 'vacme-web-shared';
import {NextFreierTerminSearch} from '../../../../../vacme-web-shared';
import {MAX_ODI_DISTANCE} from '../../../../../vacme-web-shared/src/lib/constants';
import IOdiFilterOptions from '../../../../../vacme-web-shared/src/lib/model/IOdiFilterOptions';
import IOdiOption from '../../../../../vacme-web-shared/src/lib/model/IOdiOption';
import {AuthServiceRsService} from '../../../../../vacme-web-shared/src/lib/service/auth-service-rs.service';
import {ErrorMessageService} from '../../../../../vacme-web-shared/src/lib/service/error-message.service';
import {OdiFilterUtilService} from '../../../../../vacme-web-shared/src/lib/service/odi-filter-util.service';
import {TerminUtilService} from '../../../../../vacme-web-shared/src/lib/service/termin-util.service';
import {TerminfindungResetService} from '../../../../../vacme-web-shared/src/lib/service/terminfindung-reset.service';
import {TerminfindungService} from '../../../../../vacme-web-shared/src/lib/service/terminfindung.service';
import {VacmeSettingsService} from '../../../../../vacme-web-shared/src/lib/service/vacme-settings.service';
import {BlobUtil} from '../../../../../vacme-web-shared/src/lib/util/BlobUtil';
import {BoosterUtil} from '../../../../../vacme-web-shared/src/lib/util/booster-util';
import {
    requiredTrueIfValidator,
} from '../../../../../vacme-web-shared/src/lib/util/customvalidator/required-if-validator';
import DateUtil from '../../../../../vacme-web-shared/src/lib/util/DateUtil';
import {ErkrankungUtil} from '../../../../../vacme-web-shared/src/lib/util/erkrankung-util';
import FormUtil from '../../../../../vacme-web-shared/src/lib/util/FormUtil';
import {
    isAnyStatusOfCurrentlyAbgeschlossen,
    isAtLeastFreigegeben,
    isAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues,
    isAtLeastOdiGewaehltButNotYetGeimpftValues,
    isCurrentlyAbgeschlossen,
    isErsteImpfungDoneAndZweitePending,
} from '../../../../../vacme-web-shared/src/lib/util/registrierung-status-utils';
import TenantUtil from '../../../../../vacme-web-shared/src/lib/util/TenantUtil';
import {NavigationService} from '../../service/navigation.service';

const LOG = LogFactory.createLog('OverviewComponent');

/* eslint-disable @typescript-eslint/no-non-null-assertion */

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss'],
})
export class OverviewComponent implements OnInit, OnChanges, OnDestroy {

    private static STATIONARY_CACHE_KEY = 'DOSSIER_STATIONARY_POSSIBLE';

    @Input()
    public dashboardJax!: DashboardJaxTS;

    @Input()
    public set ortDerImpfungList(odiList: OrtDerImpfungDisplayNameExtendedJaxTS[] | undefined) {
        this._ortDerImpfungList = odiList;
        this.refreshOdiOptions();
    }

    public get ortDerImpfungList(): OrtDerImpfungDisplayNameExtendedJaxTS[] | undefined {
        return this._ortDerImpfungList;
    }

    // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
    private _ortDerImpfungList?: OrtDerImpfungDisplayNameExtendedJaxTS[];

    @Input()
    public selectedOdiId: string | undefined;

    public nichtVerwalteterOdiId = 'NICHT_VERWALTETER_ODI';
    private nichtVerwalteterOdiSelected = false;

    @Output()
    public reloadDossier = new EventEmitter<string>();

    @Output()
    public nextFreieTermin = new EventEmitter<NextFreierTerminSearch>();

    public stationaryImpfort!: boolean;
    public odiOptions: Array<IOdiOption> = [];
    public elektronischerAusweisGroup!: FormGroup;
    public isZertifikatEnabled = false;
    public zertifikatList?: ZertifikatJaxTS[];
    public hasMoreZertifikate = false;
    public showAlleZertifikate = false;

    private initialSelectedSlot1: string | undefined;
    private initialSelectedSlot2: string | undefined;
    private initialSelectedSlotN: string | undefined;

    private langChangeSubscription?: Subscription;

    public erkrankungenConfirmFromGroup!: FormGroup;
    public selbstzahlerConfirmFormGroup!: FormGroup;

    public odiFilterFormGroup!: FormGroup;
    public hideOdiFilters = true;
    public odiFilterActive = false;

    constructor(
        public terminfindungService: TerminfindungService,
        private terminUtilService: TerminUtilService,
        public dossierService: DossierService,
        private registrierungsService: RegistrierungService,
        private activeRoute: ActivatedRoute,
        private router: Router,
        private filesaver: FileSaverService,
        private authService: AuthServiceRsService,
        private errorMessageService: ErrorMessageService,
        public translationService: TranslateService,
        private fb: FormBuilder,
        private onboardingService: OnboardingService,
        private viewportScroller: ViewportScroller,
        public odiFilterUtil: OdiFilterUtilService,
        public vacmeSettingsService: VacmeSettingsService,
        public navigationService: NavigationService,
        private terminfindungResetService: TerminfindungResetService,
    ) {
    }

    ngOnInit(): void {
        if (this.isImmobil()) {
            this.stationaryImpfort = this.isStationaryPossibleCache();
        }
        if (this.isNichtVerwalteterOdi()) {
            this.selectedOdiId = this.nichtVerwalteterOdiId;
            this.nichtVerwalteterOdiSelected = true;
        }
        this.langChangeSubscription =
            this.translationService.onLangChange.subscribe((event: TranslationChangeEvent) => {
                this.refreshOdiOptions();
            }, (error: any) => LOG.error(error.message));

        this.elektronischerAusweisGroup = this.fb.group({
            elektronischerImpfausweis: this.fb.control(this.dashboardJax.elektronischerImpfausweis,
                Validators.requiredTrue),
        });

        if (this.dashboardJax.currentZertifikatInfo?.deservesZertifikat) {
            this.dossierService.dossierResourceRegIsZertifikatEnabled().subscribe(
                response => this.isZertifikatEnabled = response,
                reqErr => LOG.error(reqErr));
        }
        this.initOdiFilterForm();
        this.dossierService.dossierResourceRegGetAllZertifikate(this.dashboardJax.registrierungsnummer!)
            .subscribe(list => {
                this.zertifikatList = list;
                if (list.length > 1) {
                    this.hasMoreZertifikate = true;
                }
            }, err => {
                LOG.error(err);
            });

        // Confirm Erkrankungen
        this.erkrankungenConfirmFromGroup = this.fb.group({
            confirmed: this.fb.control(this.terminfindungService.confirmedErkrankungen,
                [requiredTrueIfValidator(() => this.isErkrankungConfirmRequired())]),
        });
        // immer sofort in service speichern, damit der Wert beim Termin waehlen erhalten bleibt
        this.erkrankungenConfirmFromGroup.get('confirmed')?.valueChanges.subscribe(value => {
            this.terminfindungService.confirmedErkrankungen = value;
        }, err => LOG.error(err));

        // Confirm Selbstzahler
        this.selbstzahlerConfirmFormGroup = this.fb.group({
            confirmedSelbstzahler: this.fb.control(this.isSelbstzahler(), []),
        });
        // immer sofort in service speichern, damit der Wert beim Termin waehlen erhalten bleibt
        this.selbstzahlerConfirmFormGroup.get('confirmedSelbstzahler')?.valueChanges.subscribe(value => {
            this.dossierService.dossierResourceRegChangeSelbstzahler(this.dashboardJax.registrierungsnummer!, value)
                .subscribe(() => {
                    this.dashboardJax.selbstzahler = value;
                    this.refreshOdiOptions();
                }, error => LOG.error(error));
        }, err => LOG.error(err));
    }

    ngOnChanges(): void {
        this.initialSelectedSlot1 = this.dashboardJax.termin1?.impfslot?.id;
        this.initialSelectedSlot2 = this.dashboardJax.termin2?.impfslot?.id;
        this.initialSelectedSlotN = this.dashboardJax.terminNPending?.impfslot?.id;
    }

    private refreshOdiOptions(): void {
        this.odiOptions = this.createOdiOptions();
    }

    public createOdiOptions(): Array<IOdiOption> {
        const freieTermineOptions: Array<IOdiOption> = [];
        const noFreieTermineOptions: Array<IOdiOption> = [];
        const odis = this.getOrtDerImpfungListOptions();
        const dashboard = this.dashboardJax;
        const filterObject = this.odiFilterFormGroup?.value as IOdiFilterOptions;
        this.odiFilterUtil.filterOdis(filterObject, odis, dashboard).forEach(odi => {
            let odiLabel = odi.name;
            if (odi.terminverwaltung && this.terminUtilService.hasOdiNoFreieTermineForMe(this.dashboardJax, odi)) {
                odiLabel += ' ' + this.translationService.instant('OVERVIEW.NO_TERMIN');
                noFreieTermineOptions.push(
                    {label: odiLabel, value: odi.id, disabled: true},
                );
            } else {
                const terminLabel = this.terminfindungService.getNextTerminLabel(odi, dashboard.status);
                odiLabel += terminLabel;
                freieTermineOptions.push(
                    {label: odiLabel, value: odi.id, disabled: false},
                );
            }
        });
        const options: Array<IOdiOption> = [
            {label: this.translationService.instant('OVERVIEW.IMPFORT_LABEL'), value: undefined, disabled: false},
        ];
        if (this.vacmeSettingsService.supportsTerminbuchungBeiNichtAufgefuehrtemOdi(this.getKrankheit())
            && (!this.isImmobil() || this.stationaryImpfort)) {
            options.push({
                label: this.translationService.instant('OVERVIEW.NICHT_VERWALTETER_ODI'),
                value: this.nichtVerwalteterOdiId,
                disabled: false,
            });
        }
        options.push(...freieTermineOptions);
        options.push(...noFreieTermineOptions);
        return options;
    }

    public showDistanceFilter(): boolean {
        if (this.vacmeSettingsService.geocodingEnabled) {
            const anyOdiWithinReach = this.getOrtDerImpfungListOptions().some(value => {
                return !!value.distanceToReg && (value.distanceToReg / 1000 < MAX_ODI_DISTANCE);
            });
            return anyOdiWithinReach;
        }
        return false;
    }

    public getOrtDerImpfung(): OrtDerImpfungBuchungJaxTS | undefined {

        return this.terminfindungService.ortDerImpfung;
    }

    public getOrtDerImpfungId(): string {
        return !!this.getOrtDerImpfung() ? this.getOrtDerImpfung()!.id! : '';
    }

    public setOrtDerImpfung(ortDerImpfung: OrtDerImpfungBuchungJaxTS | undefined): void {
        this.terminfindungService.ortDerImpfung = ortDerImpfung;
    }

    public isFreigegebenOderMehr(): boolean {
        return isAtLeastFreigegeben(this.terminfindungService.dashboard?.status);
    }

    public showGrundimpfungFreigegebenInfo(): boolean {
        return this.terminfindungService.dashboard?.status === RegistrierungStatusTS.FREIGEGEBEN;
    }

    public showOrtWahlInfo(): boolean {
        return (this.terminfindungService.dashboard?.status === RegistrierungStatusTS.FREIGEGEBEN
                || this.terminfindungService.dashboard?.status === RegistrierungStatusTS.FREIGEGEBEN_BOOSTER
                || this.isSelbstzahlerButNotYetGebuchtOrOdiGewaehlt())
            && !this.isNichtVerwalteterOdi();
    }

    private isSelbstzahlerButNotYetGebuchtOrOdiGewaehlt(): boolean {
        return this.terminfindungService.dashboard?.status === RegistrierungStatusTS.IMMUNISIERT
            && this.isSelbstzahler() && !this.isNichtVerwalteterOdi();
    }

    public isAtLeastOdiGewaehltButNotYetGeimpftValues(): boolean {
        return isAtLeastOdiGewaehltButNotYetGeimpftValues(this.terminfindungService.dashboard?.status);
    }

    public canSeeTermine(): boolean {
        return this.isFreigegebenOderMehr()
            && !!this.getOrtDerImpfung()
            && true === this.getOrtDerImpfung()?.terminverwaltung;
    }

    public warnDates(): boolean {
        return !this.isAlreadyGrundimmunisiert() // nur fuer Grundimpfungen warnen
            && this.invalidDateDiff()
            && !this.canTermineBearbeiten(); // Warn Text nur initial anzeigen.
        // Falls die Termine bereits gebucht sind, dann macht es keinen Sinn mehr
    }

    public invalidDateDiff(): boolean {
        if (this.isAlreadyGrundimmunisiert()) {
            // Fuer Booster gibt es keinen Abstand
            return false;
        }
        if (this.canSeeTermine()
            && this.hasAllRequiredSlotsChosen()
            && !!this.terminfindungService.selectedSlot1 && !!this.terminfindungService.selectedSlot2) {
            const dateDff = this.terminUtilService.getDaysDiff(
                this.terminfindungService.selectedSlot2!,
                this.terminfindungService.selectedSlot1!);
            return !(dateDff <= this.getMaxDiff()
                && dateDff >= this.getMinDiff());
        }
        return false;
    }

    public getMinDiff(): number {
        return this.terminUtilService.getMinDiff();
    }

    public getMaxDiff(): number {
        return this.terminUtilService.getMaxDiff();
    }

    public getImpffolgen(): ImpffolgeTS[] {
        return this.terminfindungService.getImpffolgenToBook();
    }

    public showHintTerminSelberAbmachenOhneBuchungslink(): boolean {
        return this.showHintTerminSelberAbmachen()
            && !this.getOrtDerImpfung()?.externerBuchungslink;
    }

    public showHintTerminSelberAbmachenMitBuchungslink(): boolean {
        return this.showHintTerminSelberAbmachen()
            && !!this.getOrtDerImpfung()?.externerBuchungslink;
    }

    public getBuchungslink(): string {
        if (!!this.getOrtDerImpfung() && !!this.getOrtDerImpfung()!.externerBuchungslink) {
            return this.getOrtDerImpfung()!.externerBuchungslink!;
        }
        return '';
    }

    private showHintTerminSelberAbmachen(): boolean {
        return (this.isFreigegebenOderMehr() || this.isSelbstzahler())
            && !!(this.getOrtDerImpfung())
            && !this.getOrtDerImpfung()?.terminverwaltung
            && !this.getOrtDerImpfung()?.mobilerOrtDerImpfung;
    }

    public showHintTermineFuerKinderUnter12Jahren(): boolean {
        if (TenantUtil.zweiKinderProTerminErlaubt()) {
            if (this.dashboardJax
                && this.dashboardJax.geburtsdatum
                && this.getKrankheit() === KrankheitIdentifierTS.COVID) {
                const warnMinAgeImpfling = this.vacmeSettingsService.getWarnMinAgeImpfling(this.getKrankheit());
                if (warnMinAgeImpfling !== undefined) {
                    const geburtsdatum = this.dashboardJax.geburtsdatum;
                    const age = DateUtil.age(geburtsdatum);
                    return age < warnMinAgeImpfling;
                }
            }
        }
        return false;
    }

    public showHintTerminMobilesImpfteam(): boolean {
        return this.isFreigegebenOderMehr()
            && !!(this.getOrtDerImpfung())
            && !!this.getOrtDerImpfung()?.mobilerOrtDerImpfung;
    }

    public getHintAccordingToOdiType(): string | undefined {
        if (!this.getOrtDerImpfung()) {
            return undefined;
        }
        const hint = this.translationService.instant('OVERVIEW.HINT_ODI_TYPE.' + this.getOrtDerImpfung()?.typ);
        if (hint.length > 0) {
            return hint;
        }
        return undefined;
    }

    public selectOrtDerImpfung(): void {
        this.terminfindungService.selectedSlot1 = undefined;
        this.terminfindungService.selectedSlot2 = undefined;
        this.terminfindungService.selectedSlotN = undefined;
        this.nichtVerwalteterOdiSelected = false;
        this.setOrtDerImpfung(undefined);

        if (this.selectedOdiId === this.nichtVerwalteterOdiId) {
            this.nichtVerwalteterOdiSelected = true;
        } else {
            const selectedOrt = this.findOrtById(this.selectedOdiId);
            this.setOrtDerImpfung(selectedOrt);
        }
    }

    public showSaveButtonForOdiWithTermin(): boolean {
        return this.isFreigegebenOderMehr()
            && !!this.getOrtDerImpfung()
            && this.getOrtDerImpfung()?.terminverwaltung === true;

    }

    public showSaveButtonForOdiWithoutTermin(): boolean {
        return this.isFreigegebenOderMehr()
            && !this.isNichtVerwalteterOdi()
            && ((!!this.getOrtDerImpfung()
                && this.getOrtDerImpfung()?.terminverwaltung === false) || this.nichtVerwalteterOdiSelected);
    }

    public canSave(): boolean {
        return this.hasAllRequiredSlotsChosen()
            && !this.invalidDateDiff();
    }

    public hasAllRequiredSlotsChosen(): boolean {
        if (this.getOrtDerImpfung()?.terminverwaltung === false || this.nichtVerwalteterOdiSelected) {
            return true; // Termin muss haendisch abgemacht werden, nicht ueber VacMe
        }
        return this.terminfindungService.hasAllRequiredSlotsChosen();
    }

    // bei odi die keine Terminbuchung ueber das Tool haben muss man nur den ort wahlen statt buchen
    public ortWaehlen(): void {
        if (!this.terminfindungService.ortDerImpfung?.id && !this.nichtVerwalteterOdiSelected) {
            LOG.warn('Ort muss gewaehlt sein');
            return;
        }
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;
        if (!registrierungsnummer) {
            return;
        }

        FormUtil.doIfValid(this.erkrankungenConfirmFromGroup, () => {

            if (this.nichtVerwalteterOdiSelected) {
                this.dossierService.dossierResourceRegSelectNichtVerwalteterOrtDerImpfung(this.getKrankheit(),
                    registrierungsnummer)
                    .subscribe(
                        () => {
                            LOG.debug('Nicht verwalteter ODI gewaehlt');
                            this.doReloadDossier();
                        },
                        error => LOG.error(error.message));
            } else {
                const selectedOrt: SelectOrtDerImpfungJaxTS = {
                    odiId: this.terminfindungService.ortDerImpfung?.id,
                    registrierungsnummer: this.terminfindungService.dashboard.registrierungsnummer,
                };

                this.dossierService.dossierResourceRegSelectOrtDerImpfung(this.getKrankheit(), selectedOrt).subscribe(
                    () => {
                        LOG.debug('Ort Wahl  erfolgreich');
                        this.doReloadDossier();
                    },
                    error => LOG.error(error.message));
            }
        }, () => {
            this.viewportScroller.scrollToAnchor('confirm-erkrankungen-anchor');
        });
    }

    // bei Odi die eine Terminbuchung im Tool haben muss man die Termine  waehlen und dann buchen
    public buchen(): void {
        if (!this.hasAllRequiredSlotsChosen()) {
            return;
        }
        FormUtil.doIfValid(this.erkrankungenConfirmFromGroup, () => {

            const buchung: TerminbuchungJaxTS = {
                registrierungsnummer: this.terminfindungService.dashboard.registrierungsnummer,
                slot1Id: this.terminfindungService.selectedSlot1?.id,
                slot2Id: this.terminfindungService.selectedSlot2?.id,
                slotNId: this.terminfindungService.selectedSlotN?.id,
                krankheit: this.getKrankheit(),
            };
            this.dossierService.dossierResourceRegTermineBuchen(buchung).subscribe(
                () => {
                    LOG.debug('Termine buchen erfolgreich');
                    this.resetStationaryPossibleCache();
                    this.doReloadDossier();
                },
                error => {
                    this.terminfindungService.selectedSlot1 = undefined;
                    this.terminfindungService.selectedSlot2 = undefined;
                    this.terminfindungService.selectedSlotN = undefined;
                    if (this.terminfindungService.isErrorTerminGebuchtVallidation(error)) {
                        LOG.warn('Termin war schon gebucht');
                    } else {
                        LOG.error(error.message);
                    }

                });
        }, () => {
            this.viewportScroller.scrollToAnchor('confirm-erkrankungen-anchor');
        });
    }

    public regbestaetigungEnabled(): boolean {
        if (this.terminbestaetigungEnabled()) {
            return false;
        }
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;

        if (!registrierungsnummer) {
            return false;
        }

        if (!this.terminfindungService.ortDerImpfung) {
            return true;
        }

        const status = this.terminfindungService.dashboard?.status;
        return status === RegistrierungStatusTS.REGISTRIERT || status === RegistrierungStatusTS.FREIGEGEBEN;
    }

    public terminbestaetigungEnabled(): boolean {
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;

        if (!registrierungsnummer) {
            return false;
        }

        if ((this.terminfindungService.dashboard.status === RegistrierungStatusTS.FREIGEGEBEN
                || this.terminfindungService.dashboard.status === RegistrierungStatusTS.FREIGEGEBEN_BOOSTER)
            && this.isNichtVerwalteterOdi()) {
            return true;
        }

        if (!this.terminfindungService.ortDerImpfung) {
            return false;
        }

        const status = this.terminfindungService.dashboard?.status;
        return status === RegistrierungStatusTS.GEBUCHT ||
            status === RegistrierungStatusTS.ODI_GEWAEHLT ||
            status === RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT ||
            status === RegistrierungStatusTS.IMPFUNG_1_DURCHGEFUEHRT ||
            status === RegistrierungStatusTS.IMPFUNG_2_KONTROLLIERT ||
            status === RegistrierungStatusTS.GEBUCHT_BOOSTER ||
            status === RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER ||
            status === RegistrierungStatusTS.KONTROLLIERT_BOOSTER;
    }

    public impfdokuEnabled(): boolean {
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;

        if (!registrierungsnummer) {
            return false;
        }

        return BoosterUtil.hasDashboardVacmeImpfung(this.dashboardJax);
    }

    public showZertifikatDownload(): boolean {
        return !!this.dashboardJax.currentZertifikatInfo?.hasCovidZertifikat;
    }

    public showExterneImpfinfobox(): boolean {
        if (this.showZertifikatBox() || this.isGeimpftAndKrankheitHasNoBAGZertifikat()) {
            return false; // Wenn Vacme-Zertifikatbox angezeigt wird, zeigen wir das externe Zertifikat nicht mehr an
        }
        return !!this.dashboardJax.externGeimpft && !!this.dashboardJax.externGeimpft.externGeimpft;
    }

    public showExterneImpfinfoCreateButton(): boolean {
        if (this.showExterneImpfinfobox()) {
            return false;
        }
        return this.canEditExternesZertifikat();
    }

    public canEditExternesZertifikat(): boolean {
        if (!this.dashboardJax || !this.vacmeSettingsService.supportsExternesZertifikat(this.getKrankheit())) {
            return false;
        }
        if (BoosterUtil.hasDashboardVacmeImpfung(this.dashboardJax)) {
            return false;
        }
        switch (this.dashboardJax.status) {
            case RegistrierungStatusTS.REGISTRIERT:
            case RegistrierungStatusTS.FREIGEGEBEN:
            case RegistrierungStatusTS.IMMUNISIERT:
            case RegistrierungStatusTS.FREIGEGEBEN_BOOSTER:
                return true;
            default:
                return false;
        }
    }

    public downloadRegistrationsbestaetigung(): void {
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;

        if (!registrierungsnummer) {
            return;
        }
        this.dossierService.dossierResourceRegDownloadRegistrierungsbestaetigung(
            registrierungsnummer).subscribe(res => {
            this.filesaver.save(res, this.translationService.instant('FILES.REGBESTAETIGUNG'));
        }, error => console.error('Registrierungsbestaetigung konnte nicht heruntergeladen werden'));
    }

    public downloadTerminbestaetigung(): void {
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;

        if (!registrierungsnummer) {
            return;
        }
        this.dossierService.dossierResourceRegDownloadTerminbestaetigung(
            this.getKrankheit(),
            registrierungsnummer,
        ).subscribe(
            res => {
                this.filesaver.save(res, this.translationService.instant('FILES.TERMINBESTAETIGUNG'));
            },
            error => {
                LOG.error('Terminbestaetigung konnte nicht heruntergeladen werden');
                // Wir wollen den Fehler ILLEGAL_STATE hier speziell abfangen, statt die allgemeine Uebersetzung
                // anzuzeigen:
                BlobUtil.parseErrorMessage(error.error, ['AppValidationMessage.ILLEGAL_STATE'])
                    .then(() => this.errorMessageService
                        .addMesageAsError(this.translationService.instant('OVERVIEW.DOWNLOAD_IlLEGAL_STATE')));
            });
    }

    public resendRegistrationbestaetigung(): void {
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;
        if (!registrierungsnummer) {
            return;
        }
        this.registrierungsService.registrierungResourceRegistrierungErneutSenden(registrierungsnummer)
            .subscribe(
                (res: RegistrierungsCodeJaxTS) =>
                    Swal.fire({
                        icon: 'success',
                        text: this.translationService.instant('REGISTRIERUNG.BESTAETIGUNG_ERNEUT_GESENDET',
                            {regnr: res.registrierungsnummer}),
                        timer: 2000,
                        showCloseButton: false,
                        showConfirmButton: false,
                    }),
                (err: any) => LOG.error('Could not resend Registrationsbestaetigung', err),
            );
    }

    public resendTerminbestaetigung(): void {
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;
        if (!registrierungsnummer) {
            return;
        }
        // TODO Affenpocken VACME-2326: do we pass CC-support as a Krankheit setting?
        this.dossierService.dossierResourceRegTerminbestaetigungErneutSenden(
            KrankheitIdentifierTS.COVID,
            registrierungsnummer
        )
            .subscribe(
                (res: RegistrierungsCodeJaxTS) =>
                    Swal.fire({
                        icon: 'success',
                        text: this.translationService.instant('REGISTRIERUNG.BESTAETIGUNG_ERNEUT_GESENDET',
                            {regnr: res.registrierungsnummer}),
                        timer: 2000,
                        showCloseButton: false,
                        showConfirmButton: false,
                    }),
                (err: any) => LOG.error('Could not resend Terminbestaetigung', err),
            );
    }

    public downloadImpfdokumentation(): void {
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;

        if (!registrierungsnummer) {
            return;
        }
        this.dossierService.dossierResourceRegDownloadImpfdokumentation(
            this.getKrankheit(),
            registrierungsnummer).subscribe(res => {
            this.filesaver.save(res, this.translationService.instant('FILES.IMPFDOKUMENTATION'));
        }, error => console.error('Impfdokumentation konnte nicht heruntergeladen werden'));
    }

    public downloadZertifikat(): void {
        const registrierungsnummer = this.terminfindungService.dashboard.registrierungsnummer;
        if (!registrierungsnummer) {
            return;
        }
        this.dossierService.dossierResourceRegDownloadBestZertifikatForRegistrierung(registrierungsnummer)
            .subscribe(res => {
                this.filesaver.save(res, this.translationService.instant('FILES.COVID_IMPFZERTIFIKAT'));
            }, error => console.error('Zertifikat konnte nicht heruntergeladen werden'));
    }

    public anyStatusOfAbgeschlossen(): boolean {
        if (!this.getDashboardRegistrierungsNummer()) {
            return false;
        }
        const status = this.getCurrentRegistrationStatus();
        return isAnyStatusOfCurrentlyAbgeschlossen(status);

    }

    public getBoosterFreigabetext(): string {
        if (!this.getDashboardRegistrierungsNummer()) {
            return '';
        }
        const status = this.getCurrentRegistrationStatus();

        // Freigegeben
        if (RegistrierungStatusTS.FREIGEGEBEN_BOOSTER === status) {
            return this.translationService.instant('OVERVIEW.FREIGEGEBEN_BOOSTER_' + this.getKrankheit());
        }

        if (RegistrierungStatusTS.IMMUNISIERT === status) {
            // nicht freigegeben, und nicht selbstzahler angeklickt
            if (this.vacmeSettingsService.supportsTerminFreigabeSMS(this.getKrankheit()) && !this.isSelbstzahler()) {
                const keyToTranslate = 'OVERVIEW.FREIGEGEBEN_BOOSTER_NICHT_' + this.getKrankheit();
                return this.translationService.instant(keyToTranslate, {
                    notificationMethod: this.getNotificationMethod(this.dashboardJax.eingang),
                    date: formatDate(this.dashboardJax.impfdossier?.impfschutzJax?.freigegebenNaechsteImpfungAb!,
                        DateUtil.dateFormatLong(this.translationService.currentLang),
                        this.translationService.currentLang),
                });
            }
        }

        // wenn wir in einem Boosterstatus sind aber nicht Immunisiert oder freigegeben
        return '';

    }

    private getNotificationMethod(eingangsart?: RegistrierungsEingangTS): string {
        if (eingangsart === undefined ||
            eingangsart === RegistrierungsEingangTS.ONLINE_REGISTRATION) {
            return this.translationService.instant('OVERVIEW.NOTIFICATION_METHOD_SMS');
        }

        return this.translationService.instant('OVERVIEW.NOTIFICATION_METHOD_LETTER');
    }

    public getInfotextForAnyStatusOfAbgeschlossen(): string {
        if (!this.getDashboardRegistrierungsNummer()) {
            return '';
        }
        const status = this.getCurrentRegistrationStatus();
        switch (status) {
            case RegistrierungStatusTS.IMPFUNG_2_DURCHGEFUEHRT:
            case RegistrierungStatusTS.ABGESCHLOSSEN:
                return this.translationService.instant('OVERVIEW.GEIMPFT');
            case RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN:
                return this.translationService.instant('OVERVIEW.GEIMPFT_AUTOMATISCH_ABGESCHLOSSEN');
            case RegistrierungStatusTS.ABGESCHLOSSEN_OHNE_ZWEITE_IMPFUNG:
                return this.translationService.instant('OVERVIEW.GEIMPFT_ZWEITE_IMFPUNG_VERZICHTET');
            case RegistrierungStatusTS.IMMUNISIERT:
                return ''; // immunisiert kann viel heissen... Grundimmunisiert bei uns, Booster gemacht etc..
            default:
                // andere Stati sollten nicht passieren, da dieser Text nur fuer abgeschlossene Stati erscheint
                return '';
        }
    }

    public isAutomatischAbgeschlossen(): boolean {
        if (!this.getDashboardRegistrierungsNummer()) {
            return false;
        }
        return this.getCurrentRegistrationStatus() === RegistrierungStatusTS.AUTOMATISCH_ABGESCHLOSSEN;
    }

    public getDashboardRegistrierungsNummer(): string | undefined {
        return this.terminfindungService.dashboard.registrierungsnummer;
    }

    public getCurrentRegistrationStatus(): RegistrierungStatusTS | undefined {
        return this.terminfindungService.dashboard?.status;
    }

    public getAdresse(): AdresseJaxTS | undefined {
        return this.getOrtDerImpfung()?.adresse;
    }

    private doReloadDossier(): void {
        if (this.dashboardJax.registrierungsnummer) {
            this.reloadDossier.emit(this.dashboardJax.registrierungsnummer);
        }
    }

    private findOrtById(ortId?: string): OrtDerImpfungBuchungJaxTS | undefined {
        if (!ortId || !this.ortDerImpfungList) {
            return undefined;
        }
        for (const ort of this.ortDerImpfungList) {
            if (ort.id === ortId) {
                return ort;
            }

        }
        return undefined;
    }

    private getOrtDerImpfungListOptions(): OrtDerImpfungDisplayNameExtendedJaxTS[] {
        let filtered: OrtDerImpfungDisplayNameExtendedJaxTS[];
        if (!this.ortDerImpfungList) {
            return [];
        }
        // Die stationaeren ODIs sollen vorgeschlagen werden, wenn ich entweder mobil bin, ODER
        // urspruenglich immobil angeklickt hatte, neu aber trotzdem ein stationaeres ODI besuchen kann.
        if (this.stationaryImpfort || !this.dashboardJax.immobil) {
            filtered = this.ortDerImpfungList.filter(value => !value.mobilerOrtDerImpfung);
        } else {
            filtered = this.ortDerImpfungList.filter(value => value.mobilerOrtDerImpfung);
        }
        return filtered;
    }

    getRegistrierungsnummer(): string {
        return this.dashboardJax.registrierungsnummer as string;
    }

    canTermineBearbeiten(): boolean {
        return this.canGebuchteTermineBearbeiten() || this.canZweitterminUmbuchen();
    }

    canGebuchteTermineBearbeiten(): boolean {
        return this.canSeeTermine()
            && isAtLeastGebuchtOrOdiGewaehltButNotYetGeimpftValues(this.terminfindungService.dashboard?.status)
            && !this.isNichtVerwalteterOdi();
    }

    canZweitterminUmbuchen(): boolean {
        return isErsteImpfungDoneAndZweitePending(this.dashboardJax.status);
    }

    termineBearbeitenButtonText(): string {
        if (isErsteImpfungDoneAndZweitePending(this.dashboardJax.status)) {
            return 'OVERVIEW.TERMIN-2-UMBUCHEN';
        }
        return this.isAlreadyGrundimmunisiert()
            ? 'OVERVIEW.TERMIN-BEARBEITEN'
            : 'OVERVIEW.TERMINE-BEARBEITEN';
    }

    openTerminBearbeitung(): void {
        this.router.navigate(
            [],
            {
                relativeTo: this.activeRoute,
                queryParams: {modif: 'true'},
            });
    }

    isTerminReadOnly(impffolge: ImpffolgeTS): boolean {
        if (impffolge === ImpffolgeTS.ERSTE_IMPFUNG || impffolge === ImpffolgeTS.BOOSTER_IMPFUNG) {
            return this.isAtLeastOdiGewaehltButNotYetGeimpftValues();
        } else { // Es ist die zweite Impfung
            return this.isAtLeastOdiGewaehltButNotYetGeimpftValues()
                || this.terminfindungService.selectedSlot1 === undefined;
        }
    }

    /**
     * gibt true zurueck wenn in der gegebenen Impffolge eine ungespeicherte Aenderung da ist
     */
    isDelta(impffolge: ImpffolgeTS): boolean {
        switch (impffolge) {
            case ImpffolgeTS.ERSTE_IMPFUNG:
                return !!this.terminfindungService.selectedSlot1?.id &&
                    this.initialSelectedSlot1 !== this.terminfindungService.selectedSlot1?.id;
            case ImpffolgeTS.ZWEITE_IMPFUNG:
                return !!this.terminfindungService.selectedSlot2?.id &&
                    this.initialSelectedSlot2 !== this.terminfindungService.selectedSlot2?.id;
            case ImpffolgeTS.BOOSTER_IMPFUNG:
                return !!this.terminfindungService.selectedSlotN?.id &&
                    this.initialSelectedSlotN !== this.terminfindungService.selectedSlotN?.id;
        }
        return false;
    }

    doNextFreieTermin($event: NextFreierTerminSearch): void {
        this.nextFreieTermin.emit($event);
    }

    showAbsagen(): boolean {
        // Es kann nur bis zur ersten Impfung der Grundimmunisierung abgesagt werden, bzw. bis
        // zur Kontrolle des Boosters
        const isAbsagenAllowed = this.terminfindungService.dashboard.status === RegistrierungStatusTS.FREIGEGEBEN
            || this.terminfindungService.dashboard.status === RegistrierungStatusTS.GEBUCHT
            || this.terminfindungService.dashboard.status === RegistrierungStatusTS.ODI_GEWAEHLT
            || this.terminfindungService.dashboard.status === RegistrierungStatusTS.IMPFUNG_1_KONTROLLIERT
            || this.isStateBoosterFreigebenOrSelbstzahler()
            || this.terminfindungService.dashboard.status === RegistrierungStatusTS.GEBUCHT_BOOSTER
            || this.terminfindungService.dashboard.status === RegistrierungStatusTS.ODI_GEWAEHLT_BOOSTER
            || this.terminfindungService.dashboard.status === RegistrierungStatusTS.KONTROLLIERT_BOOSTER;
        if (!isAbsagenAllowed) {
            return false;
        }
        if (this.isNichtVerwalteterOdi()) {
            return true;
        }
        if (this.isAtLeastOdiGewaehltButNotYetGeimpftValues()) {
            const keineTerminverwaltung = !!this.getOrtDerImpfung()
                && !this.getOrtDerImpfung()?.mobilerOrtDerImpfung;
            const mobilerOdi = this.isImmobil()
                && !this.canTermineBearbeiten();
            return keineTerminverwaltung || mobilerOdi;
        }
        return false;
    }

    absagenButtonText(): string {
        if (this.isNichtVerwalteterOdi()) {
            return 'OVERVIEW.NICHT_VERWALTETER_ODI_ABSAGEN';
        }
        return 'OVERVIEW.SELBER_ABMACHEN_ABSAGEN.ABSAGEN';
    }

    absagenClicked(): void {
        let keyTitle = 'OVERVIEW.SELBER_ABMACHEN_ABSAGEN.QUESTION';
        if (this.isImmobil()) {
            keyTitle = 'OVERVIEW.MOBILES_IMPFTEAM_ABSAGEN.QUESTION';
        }

        if (this.isNichtVerwalteterOdi()) {
            // Keine Rueckfrage notwendig
            this.absagen();
        } else {
            Swal.fire({
                icon: 'question',
                text: this.translationService.instant(keyTitle),
                confirmButtonText: this.translationService.instant('OVERVIEW.SELBER_ABMACHEN_ABSAGEN.ABSAGEN'),
                showCancelButton: true,
                cancelButtonText: this.translationService.instant('OVERVIEW.SELBER_ABMACHEN_ABSAGEN.CANCEL'),
            }).then(r => {
                if (r.isConfirmed) {
                    this.absagen();
                }
            });
        }
    }

    private absagen(): void {
        const registrierungsNummer = this.dashboardJax.registrierungsnummer;
        if (registrierungsNummer) {
            this.dossierService.dossierResourceRegOdiAndTermineAbsagen(this.getKrankheit(), registrierungsNummer)
                .subscribe(res => {
                    // Die Auswahl im Dropdown zuruecksetzen. Es ist ein @Input und wir haben es im parent component
                    // versucht zurueckzusetzen, aber das hat nicht geklappt. VACME-1869
                    this.selectedOdiId = undefined;
                    this.nichtVerwalteterOdiSelected = false;
                    this.doReloadDossier();
                    Swal.fire({
                        icon: 'success',
                        showCancelButton: false,
                        showConfirmButton: false,
                        timer: 1500,
                    });
                }, error => {
                    LOG.error('Could not cancel selber abmachen termin', error);
                });
        }
    }

    openZweitterminUmbuchung(): void {
        this.openTerminBearbeitung();
    }

    isImmobil(): boolean {
        return this.dashboardJax.immobil !== false;
    }

    isNichtVerwalteterOdi(): boolean {
        return this.dashboardJax.nichtVerwalteterOdiSelected === true;
    }

    cacheStationaryPossible(): void {
        if (this.stationaryImpfort && this.dashboardJax.registrierungsnummer) {
            localStorage.setItem(OverviewComponent.STATIONARY_CACHE_KEY, this.dashboardJax.registrierungsnummer);
        } else {
            localStorage.removeItem(OverviewComponent.STATIONARY_CACHE_KEY);
        }
        this.refreshOdiOptions();
    }

    changeToMobil(): void {
        if (!!this.dashboardJax.registrierungsnummer) {
            Swal.fire({
                icon: 'question',
                text: this.translationService.instant('OVERVIEW.STATIONAER_MOEGLICH_CONFIRM'),
                showCancelButton: true,
            }).then(r => {
                if (r.isConfirmed) {
                    this.dossierService.dossierResourceRegChangeToMobil(
                        this.dashboardJax.registrierungsnummer!).subscribe(() => {
                        this.dashboardJax.immobil = false;
                        this.doReloadDossier();
                        this.refreshOdiOptions();
                    }, () => LOG.error('Registrierung konnte nicht auf mobil gesetzt werden'));
                }
            });
        }
    }

    public isCallcenter(): boolean {
        return this.authService.hasRoleCallCenter();
    }

    public isOnlineRegistration(): boolean {
        return this.dashboardJax.eingang === RegistrierungsEingangTS.ONLINE_REGISTRATION;
    }

    public registrationType(): string {
        return '(' + this.translationService.instant(
            'OVERVIEW.REGISTRATION_TYPE.' + this.dashboardJax.eingang) + ')';
    }

    public getImpfausweisWarten(): string {
        if (this.dashboardJax.eingang !== RegistrierungsEingangTS.ONLINE_REGISTRATION) {
            return 'OVERVIEW.IMPFAUSWEIS_WARTEN_NO_SMS';
        }
        return 'OVERVIEW.IMPFAUSWEIS_WARTEN';
    }

    private isStationaryPossibleCache(): boolean {
        const value = localStorage.getItem(OverviewComponent.STATIONARY_CACHE_KEY);

        // Cache value is associated with the dossier
        if (value === this.dashboardJax.registrierungsnummer) {
            return true;
        } else {
            // Cache is stale as it references another dossier
            this.resetStationaryPossibleCache();
            return false;
        }
    }

    private resetStationaryPossibleCache(): void {
        localStorage.removeItem(OverviewComponent.STATIONARY_CACHE_KEY);
    }

    public showHintNichtVerwalteterOdi(): boolean {
        return this.isNichtVerwalteterOdi()
            && (!isAnyStatusOfCurrentlyAbgeschlossen(this.terminfindungService.dashboard.status)
                || this.isSelbstzahler());
    }

    public printDate(termin?: Date): string {
        if (!termin) {
            return '';
        }
        return formatDate(termin,
            DateUtil.dateFormatVeryLong(this.translationService.currentLang),
            this.translationService.currentLang);
    }

    public showErkrankungBlock(): boolean {
        if (!this.vacmeSettingsService.supportsErkrankungen(this.getKrankheit())) {
            return false;
        }

        // wenn man nicht eh schon die Checkbox anzeigt und nur, wenn man Erkrankungen sehen darf
        return !this.showErkrankungConfirm() && ErkrankungUtil.canViewErkrankungen(this.dashboardJax?.status);
    }

    public canEditErkrankungen(): boolean {
        return ErkrankungUtil.canEditErkrankungen(this.dashboardJax?.status);
    }

    public showImmobilBlock(): boolean {
        return !!this.dashboardJax.immobil
            && this.isBoosterStateButNotGebuchtYet()
            && !this.isNichtVerwalteterOdi();
    }

    private isBoosterStateButNotGebuchtYet(): boolean {
        return this.terminfindungService.dashboard?.status === RegistrierungStatusTS.IMMUNISIERT
            || this.isStateBoosterFreigebenOrSelbstzahler();
    }

    public isStateBoosterFreigebenOrSelbstzahler(): boolean {
        return this.terminfindungService.dashboard?.status === RegistrierungStatusTS.FREIGEGEBEN_BOOSTER
            || this.isSelbstzahler();
    }

    public isSelbstzahler(): boolean {
        return !!this.dashboardJax.selbstzahler;
    }

    public showErkrankungConfirm(): boolean {
        if (!this.vacmeSettingsService.supportsErkrankungen(this.getKrankheit())) {
            return false;
        }

        return this.isStateBoosterFreigebenOrSelbstzahler() && !this.dashboardJax.nichtVerwalteterOdiSelected;
    }

    public isErkrankungConfirmRequired(): boolean {
        return this.showErkrankungConfirm();
    }

    public resetConfirmErkrankungen(): void {
        this.terminfindungService.confirmedErkrankungen = false;
    }

    public gotoErkrankungPage(): void {
        this.navigationService.navigateToErkrankungen(this.dashboardJax);
    }

    public getLastErkrankungDate(): string | undefined {
        if (this.dashboardJax?.impfdossier?.erkrankungen?.length) {
            const theDate = (this.dashboardJax.impfdossier!.erkrankungen![
                this.dashboardJax.impfdossier!.erkrankungen!.length - 1])!.date;
            return formatDate(theDate,
                DateUtil.dateFormatLong(this.translationService.currentLang),
                this.translationService.currentLang);
        }
        return undefined;
    }

    public showPlanungBlock(): boolean {
        return !this.anyStatusOfAbgeschlossen() || this.isStateBoosterFreigebenOrSelbstzahler();
    }

    public showZertifikatBox(): boolean {
        if (this.showAlleZertifikate) {
            return false; // entweder - oder
        }
        return this.showZertifikatBasic();
    }

    public showZertifikatBasic(): boolean {
        return !!this.dashboardJax.currentZertifikatInfo?.deservesZertifikat &&
            !(this.dashboardJax.elektronischerImpfausweis && !this.isZertifikatEnabled);
    }

    public showAlleZertifikatBox(): boolean {
        return this.showAlleZertifikate && this.showZertifikatBasic();
    }

    public downloadSpecificZertifikat(zertifikat: ZertifikatJaxTS): void {
        if (this.dashboardJax.registrierungsnummer && zertifikat.id) {
            this.dossierService.dossierResourceRegDownloadZertifikatWithId(
                this.dashboardJax.registrierungsnummer, zertifikat.id)
                .subscribe(res => {
                    this.filesaver.save(res, this.translationService.instant('FILES.COVID_IMPFZERTIFIKAT'));
                }, error => LOG.error('Zertifikat konnte nicht heruntergeladen werden'));
        }
    }

    public showZertifikatBeantragenButton(): boolean {
        return !this.dashboardJax.elektronischerImpfausweis // wurde noch nicht beantragt
            && !this.dashboardJax.currentZertifikatInfo?.hasCovidZertifikat // hat noch kein Zertifikat
            && !!this.dashboardJax.currentZertifikatInfo?.deservesZertifikat; // hat ein Zertifikat verdient
    }

    public showZertifikatWarten(): boolean {
        // wenn flag true aber dokument noch nicht da
        return !!this.dashboardJax.elektronischerImpfausweis // wurde beantragt
            && !this.dashboardJax.currentZertifikatInfo?.hasCovidZertifikat // hat noch kein Zertifikat
            && !!this.dashboardJax.currentZertifikatInfo?.deservesZertifikat; // hat ein Zertifikat verdient
    }

    public saveFlagForm(): void {
        FormUtil.doIfValid(this.elektronischerAusweisGroup, () => {
            if (this.dashboardJax.registrierungId) {
                this.registrierungsService
                    .registrierungResourceAcceptElektronischerImpfausweisWithId(this.dashboardJax.registrierungId)
                    .subscribe(response => {
                        this.dashboardJax.elektronischerImpfausweis = true;
                        Swal.fire({
                            icon: 'success',
                            timer: 1500,
                            showConfirmButton: false,
                        });
                    }, error => LOG.error(error));
            }
        });
    }

    public dataAnpassen(): void {
        this.router.navigate(['cc-adresse-edit', this.dashboardJax.registrierungsnummer]);
    }

    ngOnDestroy(): void {
        if (this.langChangeSubscription) {
            this.langChangeSubscription.unsubscribe();
        }
    }

    resendZertifikatPerPost(): void {
        if (this.dashboardJax.registrierungId) {
            this.dossierService.dossierResourceRegRecreatePerPost(this.dashboardJax.registrierungId)
                .subscribe(value => {
                    Swal.fire({
                        icon: 'success',
                        timer: 1500,
                        showConfirmButton: false,
                    });
                }, error => LOG.error(error));
        }
    }

    canResendOnboardingLetter(): boolean {
        const onboardingEnabled = TenantUtil.hasOnboarding();
        return onboardingEnabled
            && this.isCallcenter()
            && !this.dashboardJax.hasBenutzer;
    }

    resendOnboardingLetter(): void {
        const regNr = this.dashboardJax.registrierungsnummer;
        if (regNr) {
            Swal.fire({
                icon: 'question',
                text: this.translationService.instant('ONBOARDING.TRIGGER.CONFIRM.TEXT'),
                confirmButtonText: this.translationService.instant('ONBOARDING.TRIGGER.CONFIRM.CONFIRM'),
                showCancelButton: true,
                cancelButtonText: this.translationService.instant('ONBOARDING.TRIGGER.CONFIRM.CANCEL'),
            }).then(r => {
                if (r.isConfirmed) {
                    this.onboardingService.onboardingRegResourceTriggerOnboardingLetter(regNr)
                        .subscribe(() => {
                            Swal.fire({
                                icon: 'success',
                                text: this.translationService.instant('ONBOARDING.TRIGGER.BESTAETIGUNG'),
                                timer: 2000,
                                showCloseButton: false,
                                showConfirmButton: false,
                            });
                        }, error => LOG.error(error));
                }
            });
        }
    }

    public isGueltigeSchweizerAdresse(): boolean {
        return !!this.dashboardJax.gueltigeSchweizerAdresse;
    }

    public getInfoTextDistanceBetweenImpfungen(): string {
        return this.vacmeSettingsService.getInfoTextDistanceBetweenImpfungen();
    }

    public isAlreadyGrundimmunisiert(): boolean {
        return this.terminfindungService.isAlreadyGrundimmunisiert();
    }

    public toggleOdiFilter(): void {
        this.hideOdiFilters = !this.hideOdiFilters;
    }

    public filterOdis(): void {
        this.odiFilterActive = !this.odiFilterUtil.isDefaultFilter(this.odiFilterFormGroup.value);
        this.refreshOdiOptions();
        this.toggleOdiFilter();
    }

    public resetOdiFilter(): void {
        this.odiFilterFormGroup.reset(OdiFilterUtilService.defaultFilterOptions);
        this.refreshOdiOptions();
        this.odiFilterActive = false;
        this.toggleOdiFilter();
    }

    private initOdiFilterForm(): void {
        this.odiFilterFormGroup = this.fb.group(OdiFilterUtilService.defaultFilterOptions);
    }

    public showLinkSelbstzahler(): boolean {
        if (!this.vacmeSettingsService.selbstzahlerPortalEnabled) {
            return false;
        }
        // Anzeigen, wenn nicht sowieso schon FREIGEGEBEN_BOOSTER und
        // die Freigabe fuer Selbstzahler erreicht ist
        if (!isCurrentlyAbgeschlossen(this.dashboardJax.status) || this.dashboardJax.nichtVerwalteterOdiSelected) {
            return false;
        }
        const datumFreigabeSelbstzahler =
            this.dashboardJax.impfdossier?.impfschutzJax?.freigegebenAbSelbstzahler;
        const freigegebenSelbstzahler: boolean = datumFreigabeSelbstzahler == null
            ? false
            : !DateUtil.isAfterToday(datumFreigabeSelbstzahler);
        return freigegebenSelbstzahler;
    }

    public getKrankheit(): KrankheitIdentifierTS {
        if (this.dashboardJax.krankheitIdentifier === undefined || this.dashboardJax.krankheitIdentifier === null) {
            this.errorMessageService.addMesageAsError('KRANKHEIT NICHT GESETZT');
            throw new Error('Krankheit nicht gesetzt ' + this.dashboardJax.krankheitIdentifier);
        }
        return this.dashboardJax.krankheitIdentifier;
    }

    public getTranslatedKrankheit(): string {
        return this.translationService.instant('KRANKHEITEN.' + this.getKrankheit());
    }

    public showAffenpockenInfo(): boolean {
        return this.getKrankheit() === 'AFFENPOCKEN' && this.showPlanungBlock();
    }

    public showLinkZurueck(): boolean {
        return TenantUtil.BERN;
    }

    public navigateToOverviewPage(): void {
        this.terminfindungResetService.resetData();
        this.navigationService.navigateToStartpage(this.dashboardJax.registrierungsnummer, this.getKrankheit());
    }

    public hideQRCode(): boolean {
        return TenantUtil.showQRCode() === false;
    }

    private isGeimpftAndKrankheitHasNoBAGZertifikat(): boolean {

        const hasImpfung = !!this.dashboardJax.impfung1
            || !!this.dashboardJax.impfung2
            || (this.dashboardJax.boosterImpfungen !== undefined && this.dashboardJax.boosterImpfungen.length > 0);
        return !this.vacmeSettingsService.supportsZertifikat(this.getKrankheit()) && hasImpfung;

    }

    public showSupportLeistungserbringer(): boolean {
        return this.vacmeSettingsService.getKantonaleBerechtigung(this.getKrankheit())
            === KantonaleBerechtigungTS.LEISTUNGSERBRINGER;
    }
}
